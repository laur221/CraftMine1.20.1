package com.seishironagi.craftmine;

import com.seishironagi.craftmine.network.ModMessages;
import com.seishironagi.craftmine.network.packet.GameDataSyncS2CPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.*;

public class GameManager {
    private static GameManager INSTANCE;

    private final TeamManager teamManager;
    private Item targetItem;
    private int gameTimeMinutes = Config.defaultGameTime;
    private long gameEndTimeMillis;
    private boolean gameRunning = false;
    private final Timer gameTimer = new Timer("GameTimer");
    private TimerTask displayTask;

    // Map to track frozen players and when they should unfreeze
    private final Map<UUID, Long> frozenPlayers = new HashMap<>();
    private boolean timerStarted = false;
    private long timerStartDelayMillis = 0;

    private GameManager() {
        this.teamManager = new TeamManager();
    }

    public static GameManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GameManager();
        }
        return INSTANCE;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public boolean isGameRunning() {
        return gameRunning;
    }

    public void setGameTime(int minutes) {
        if (minutes > 0) {
            this.gameTimeMinutes = minutes;
        }
    }

    public int getGameTime() {
        return gameTimeMinutes;
    }

    public long getRemainingTimeMillis() {
        if (!gameRunning)
            return 0;

        if (!timerStarted)
            return gameTimeMinutes * 60 * 1000L;

        return Math.max(0, gameEndTimeMillis - System.currentTimeMillis());
    }

    public int getRemainingTimeSeconds() {
        return (int) (getRemainingTimeMillis() / 1000);
    }

    public Item getTargetItem() {
        return targetItem;
    }

    public void startGame() {
        if (gameRunning)
            return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null)
            return;

        Player redPlayer = teamManager.getRedTeamPlayer(server);
        if (redPlayer == null) {
            broadcastMessage("Cannot start game: No red team player assigned.", ChatFormatting.RED);
            return;
        }

        // Select random item from overworld items
        List<Item> availableItems = getAllOverworldItems();
        if (availableItems.isEmpty()) {
            // Fallback to configured items if no overworld items found
            availableItems = new ArrayList<>(Config.gameItems);
        }

        if (availableItems.isEmpty()) {
            broadcastMessage("Cannot start game: No items configured.", ChatFormatting.RED);
            return;
        }

        targetItem = availableItems.get(new Random().nextInt(availableItems.size()));

        // Store and clear player inventories
        storeAndClearInventories(server);

        // Set game time
        gameTimeMinutes = Config.defaultGameTime;

        // Set end time but don't start timer yet - it will start after delay
        gameEndTimeMillis = System.currentTimeMillis() + (gameTimeMinutes * 60 * 1000L);
        timerStarted = false;

        // Get all players in the game
        List<ServerPlayer> players = server.getPlayerList().getPlayers();

        // Call WorldManager to handle world regeneration and teleportation
        com.seishironagi.craftmine.world.WorldManager.startNewGame(players);

        // Apply freeze effect to players
        for (ServerPlayer player : players) {
            boolean isRedTeam = teamManager.isRedTeam(player);
            int freezeDuration = isRedTeam ? 5 : 15;
            applyFreezeEffect(player, freezeDuration);
        }

        // Send messages
        broadcastMessage(Config.gameStartMessage, ChatFormatting.GOLD);

        // Send target message to red team only
        String itemName = targetItem.getDescription().getString();
        String redMessage = String.format(Config.redTeamTaskMessage, itemName, gameTimeMinutes);
        redPlayer.sendSystemMessage(Component.literal(redMessage).withStyle(ChatFormatting.GOLD));

        // Start timer check task
        startTimerDisplay();

        gameRunning = true;
    }

    private void startTimerDisplay() {
        if (displayTask != null) {
            displayTask.cancel();
        }

        displayTask = new TimerTask() {
            @Override
            public void run() {
                checkGameStatus();
                updateFreezeStatus();
            }
        };

        gameTimer.scheduleAtFixedRate(displayTask, 0, 100); // Run more frequently (100ms)
    }

    private void updateFreezeStatus() {
        // Check if timer needs to be started
        if (!timerStarted && System.currentTimeMillis() >= timerStartDelayMillis) {
            timerStarted = true;
            gameEndTimeMillis = System.currentTimeMillis() + (gameTimeMinutes * 60 * 1000L);
            broadcastMessage("Timer has started! The game is now in progress.", ChatFormatting.GOLD);
        }
    }

    private void checkGameStatus() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null)
            return;

        if (getRemainingTimeMillis() <= 0) {
            endGame(false);
            return;
        }

        Player redPlayer = teamManager.getRedTeamPlayer(server);
        if (redPlayer != null) {
            // Check if red player has the item
            for (ItemStack stack : redPlayer.getInventory().items) {
                if (!stack.isEmpty() && stack.getItem() == targetItem) {
                    endGame(true);
                    return;
                }
            }
        }

        if (server != null) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                boolean isRedTeam = teamManager.isRedTeam(player);
                ItemStack targetItemStack = ItemStack.EMPTY;
                if (targetItem != null) {
                    targetItemStack = new ItemStack(targetItem);
                }

                // Send with item-specific time and current difficulty
                ModMessages.sendToPlayer(new GameDataSyncS2CPacket(
                        getRemainingTimeSeconds(), // current time
                        true, // game running
                        isRedTeam, // is red team
                        targetItemStack, // target item
                        false, // who won (not relevant during game)
                        Config.gameDifficulty, // game difficulty
                        gameTimeMinutes // current item time
                ), player);
            }
        }
    }

    public void endGame(boolean redTeamWin) {
        if (!gameRunning)
            return;

        if (displayTask != null) {
            displayTask.cancel();
            displayTask = null;
        }

        if (redTeamWin) {
            broadcastMessage(Config.redTeamWinMessage, ChatFormatting.GOLD);
        } else {
            broadcastMessage(Config.blueTeamWinMessage, ChatFormatting.GOLD);
        }

        // Restore player inventories with game controller in first slot
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            restoreInventories(server);

            // Send game end notification to all clients
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                boolean isRedTeam = teamManager.isRedTeam(player);

                // Send with empty item and game no longer running
                ModMessages.sendToPlayer(new GameDataSyncS2CPacket(
                        0, // time 0
                        false, // game not running
                        isRedTeam,
                        ItemStack.EMPTY, // no target item
                        redTeamWin // who won
                ), player);
            }
        }

        // Reset game state
        gameRunning = false;
        targetItem = null;
        timerStarted = false;
        frozenPlayers.clear();
    }

    public void resetGame() {
        if (gameRunning) {
            endGame(false);
        }

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            teamManager.resetTeams(server);
        }

        gameTimeMinutes = Config.defaultGameTime;
        targetItem = null;
        gameRunning = false;
        timerStarted = false;
        frozenPlayers.clear();
    }

    public void applyFreezeEffect(Player player, int seconds) {
        long unfreezeTime = System.currentTimeMillis() + (seconds * 1000L);
        frozenPlayers.put(player.getUUID(), unfreezeTime);

        // If this is the red team player, also delay the timer start
        if (teamManager.isRedTeam(player)) {
            timerStarted = false;
            timerStartDelayMillis = System.currentTimeMillis() + (seconds * 1000L);
        }
    }

    public boolean isPlayerFrozen(Player player) {
        if (!frozenPlayers.containsKey(player.getUUID())) {
            return false;
        }

        long unfreezeTime = frozenPlayers.get(player.getUUID());
        if (System.currentTimeMillis() >= unfreezeTime) {
            // Player is no longer frozen
            frozenPlayers.remove(player.getUUID());

            // Notify player
            player.sendSystemMessage(Component.literal("You are now unfrozen! Go!")
                    .withStyle(ChatFormatting.GREEN));
            return false;
        }

        return true;
    }

    private void broadcastMessage(String message, ChatFormatting color) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null)
            return;

        Component component = Component.literal(message).withStyle(color);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.sendSystemMessage(component);
        }
    }

    private List<Item> getAllOverworldItems() {
        List<Item> overworldItems = new ArrayList<>();

        // Get all registered items
        for (Item item : ForgeRegistries.ITEMS) {
            String itemId = ForgeRegistries.ITEMS.getKey(item).toString();
            if (itemId == null)
                continue;

            // Skip items from nether or end dimensions or complex items
            if (itemId.contains("nether") || itemId.contains("end") ||
                    itemId.contains("dragon") || itemId.contains("shulker") ||
                    itemId.contains("elytra") || itemId.contains("chorus") ||
                    itemId.contains("quartz") || itemId.contains("ancient_debris") ||
                    itemId.contains("glowstone") || itemId.contains("soul_") ||
                    itemId.contains("warped_") || itemId.contains("crimson_") ||
                    itemId.contains("netherite") || itemId.contains("basalt") ||
                    itemId.contains("blackstone") || itemId.contains("magma") ||
                    itemId.contains("shroomlight") || itemId.contains("wither") ||
                    itemId.contains("gilded") || itemId.contains("blaze") ||
                    itemId.contains("ghast") || itemId.contains("weeping") ||
                    // Additional excluded items
                    itemId.contains("skulk") || itemId.contains("oxidized") ||
                    itemId.contains("exposed") || itemId.contains("weathered") ||
                    itemId.contains("copper_block") || itemId.contains("amethyst") ||
                    itemId.contains("budding") || itemId.contains("calcite") ||
                    itemId.contains("tuff") || itemId.contains("suspicious") ||
                    itemId.contains("decorated") ||
                    // Exclude silk touch obtainable blocks
                    itemId.contains("_ore") || // All ore blocks
                    itemId.contains("ice") || // All ice variants
                    itemId.contains("glass") || // Glass blocks
                    itemId.contains("mushroom_block") || // Mushroom blocks
                    itemId.contains("grass_block") || // Grass blocks
                    itemId.contains("deepslate") || // Deepslate variants
                    itemId.contains("amethyst") || // Amethyst related
                    itemId.contains("mycelium") || // Mycelium
                    itemId.contains("bookshelf") || // Bookshelves
                    itemId.contains("campfire") || // Campfires
                    itemId.contains("beehive")) { // Beehives
                continue;
            }

            // Skip non-obtainable items and special items
            if (item == Items.AIR || item == Items.BARRIER || item == Items.COMMAND_BLOCK ||
                    item == Items.STRUCTURE_BLOCK || item == Items.JIGSAW ||
                    item == Items.STRUCTURE_VOID || item == Items.DEBUG_STICK ||
                    item == Items.SPAWNER || item == Items.ENCHANTED_GOLDEN_APPLE ||
                    itemId.contains("infested") || itemId.contains("command") ||
                    itemId.contains("spawn_egg") || itemId.contains("chipped") ||
                    itemId.contains("damaged") || itemId.contains("reinforced") ||
                    itemId.contains("sherd") || itemId.contains("pottery") ||
                    itemId.contains("Wet sponge") || itemId.contains("sponge") ||
                    itemId.contains("petrified") || itemId.contains("bedrock")) {
                continue;
            }

            // Skip other creative-only items
            if (item == Items.KNOWLEDGE_BOOK || item == Items.WRITTEN_BOOK ||
                    item == Items.BUNDLE || item == Items.FILLED_MAP) {
                continue;
            }

            overworldItems.add(item);
        }

        return overworldItems;
    }

    private BlockPos findSafeSpot(ServerLevel level, int x, int z) {
        Random random = new Random();

        // Try multiple attempts to find a good spawn location
        for (int attempts = 0; attempts < 100; attempts++) {
            // Randomize positions slightly after first attempt
            int testX = attempts == 0 ? x : x + random.nextInt(500) - 250;
            int testZ = attempts == 0 ? z : z + random.nextInt(500) - 250;

            // Get surface position using WORLD_SURFACE (excludes trees, leaves, etc.)
            BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos(testX, 0, testZ));

            // Skip if we're too low (likely a cave entrance)
            if (surfacePos.getY() < 60) {
                CraftMine.LOGGER.info("Position too low at " + surfacePos + ", likely a cave. Trying new position.");
                continue;
            }

            // Check if the biome is an ocean
            if (level.getBiome(surfacePos).value().getBaseTemperature() < 0.5f &&
                    level.getBiome(surfacePos).unwrapKey().map(key -> key.location().getPath().contains("ocean"))
                            .orElse(false)) {
                CraftMine.LOGGER.info("Found ocean at " + surfacePos + ", trying new position");
                continue;
            }

            // Check if block below is solid and not a fluid
            BlockPos posBelow = surfacePos.below();
            if (!level.getBlockState(posBelow).isSolid() ||
                    !level.getFluidState(posBelow).isEmpty()) {
                CraftMine.LOGGER
                        .info("Block below is not solid or is fluid at " + surfacePos + ", trying new position");
                continue;
            }

            // Check for proper air space for the player (2 blocks)
            BlockPos playerPos = surfacePos.above();
            if (!level.getBlockState(playerPos).isAir() ||
                    !level.getBlockState(playerPos.above()).isAir()) {
                CraftMine.LOGGER.info("Not enough air space at " + playerPos + ", trying new position");
                continue;
            }

            // Final safety check - no water or lava
            if (level.getFluidState(playerPos).isEmpty() &&
                    level.getFluidState(playerPos.above()).isEmpty()) {
                CraftMine.LOGGER.info("Found safe spawn position at: " + playerPos);
                return playerPos;
            }
        }

        // If all attempts failed, use the world spawn point (safest fallback)
        BlockPos worldSpawn = level.getSharedSpawnPos();
        CraftMine.LOGGER.warn("Couldn't find safe position after 100 attempts, using world spawn at: " + worldSpawn);
        return worldSpawn.above();
    }

    private void createNewGameWorld(MinecraftServer server) {
        // Since world creation is complex and requires many Minecraft internals,
        // we'll just use the existing overworld for now
        ServerLevel overworld = server.getLevel(net.minecraft.world.level.Level.OVERWORLD);

        // Reset player gamemodes
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        }

        // Get a fresh spawn point far from world spawn
        Random random = new Random();
        int distance = 1000 + random.nextInt(5000);
        double angle = random.nextDouble() * Math.PI * 2;
        int x = (int) (Math.cos(angle) * distance);
        int z = (int) (Math.sin(angle) * distance);

        BlockPos spawnPos = findSafeSpot(overworld, x, z);
        CraftMine.LOGGER.info("New game area located at: " + spawnPos);
    }

    private void startGameInNewWorld(MinecraftServer server) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();

        // Find a new game area
        Random random = new Random();
        int distance = 1000 + random.nextInt(5000);
        double angle = random.nextDouble() * Math.PI * 2;
        int x = (int) (Math.cos(angle) * distance);
        int z = (int) (Math.sin(angle) * distance);

        BlockPos gameSpawnPos = findSafeSpot(server.overworld(), x, z);

        for (ServerPlayer player : players) {
            // Reset player state
            player.setHealth(player.getMaxHealth());
            player.getFoodData().setFoodLevel(20);
            player.getFoodData().setSaturation(20.0f);

            // Clear inventory
            player.getInventory().clearContent();

            // Teleport to game spawn
            player.teleportTo(player.serverLevel(),
                    gameSpawnPos.getX() + 0.5, gameSpawnPos.getY(), gameSpawnPos.getZ() + 0.5,
                    player.getYRot(), player.getXRot());

            // Set spawn point
            player.setRespawnPosition(player.level().dimension(), gameSpawnPos, player.getYRot(), true, false);

            // Apply freeze
            boolean isRedTeam = teamManager.isRedTeam(player);
            int freezeDuration = isRedTeam ? 5 : 15;
            applyFreezeEffect(player, freezeDuration);

            // Send messages
            player.sendSystemMessage(
                    Component.literal("Game starting! All players have been teleported to a random location.")
                            .withStyle(ChatFormatting.GREEN));
            player.sendSystemMessage(Component.literal("You will be frozen for " + freezeDuration + " seconds!")
                    .withStyle(ChatFormatting.YELLOW));
        }
    }

    private void storeAndClearInventories(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            // Just clear inventory
            player.getInventory().clearContent();
        }
    }

    private void restoreInventories(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            // Clear current inventory first
            player.getInventory().clearContent();

            // Add game controller to first hotbar slot (slot 0)
            ItemStack controllerItem = new ItemStack(ModRegistry.GAME_CONTROLLER_ITEM.get());
            player.getInventory().setItem(0, controllerItem);

            // Force inventory update
            player.inventoryMenu.broadcastChanges();
        }
    }
}
