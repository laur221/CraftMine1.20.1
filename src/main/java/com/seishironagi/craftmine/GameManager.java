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

        // Calculate game time
        if (Config.useItemSpecificTimes && Config.itemTimes.containsKey(targetItem)) {
            gameTimeMinutes = Config.itemTimes.get(targetItem);
        }

        // Set end time but don't start timer yet - it will start after delay
        gameEndTimeMillis = System.currentTimeMillis() + (gameTimeMinutes * 60 * 1000L);
        timerStarted = false;

        // Teleport players to random locations
        teleportPlayersToRandomLocations(server);

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
            // Skip items from nether or end dimensions or complex items
            String itemId = ForgeRegistries.ITEMS.getKey(item).toString();
            if (itemId == null)
                continue;

            // Skip items from dimensions that aren't the overworld
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
                    itemId.contains("decorated")) {
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

    private void teleportPlayersToRandomLocations(MinecraftServer server) {
        // Teleport all players to separate random locations
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        List<BlockPos> usedPositions = new ArrayList<>();

        for (ServerPlayer player : players) {
            // Restore health and food to maximum
            player.setHealth(player.getMaxHealth());
            player.getFoodData().setFoodLevel(20); // 20 is full food bar
            player.getFoodData().setSaturation(20.0f); // Full saturation

            BlockPos pos = getRandomPosition(player.serverLevel(), usedPositions);
            usedPositions.add(pos);

            player.teleportTo(player.serverLevel(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    player.getYRot(), player.getXRot());

            // Set spawn point
            player.setRespawnPosition(player.level().dimension(), pos, player.getYRot(), true, false);

            // Apply freeze effect based on team
            boolean isRedTeam = teamManager.isRedTeam(player);
            int freezeDuration = isRedTeam ? 5 : 15; // 5 seconds for red team, 15 for blue team
            applyFreezeEffect(player, freezeDuration);

            player.sendSystemMessage(Component.literal("Game starting! You've been teleported to a random location.")
                    .withStyle(ChatFormatting.GREEN));
            player.sendSystemMessage(Component.literal("Your health and food have been restored!")
                    .withStyle(ChatFormatting.GREEN));
            player.sendSystemMessage(Component.literal("You will be frozen for " + freezeDuration + " seconds.")
                    .withStyle(ChatFormatting.YELLOW));
            player.sendSystemMessage(Component.literal("Your spawn point has been set at this location.")
                    .withStyle(ChatFormatting.YELLOW));
        }
    }

    private BlockPos getRandomPosition(ServerLevel level, List<BlockPos> usedPositions) {
        Random random = new Random();
        BlockPos pos;
        boolean validPos = false;

        // Keep trying until we find a position that's far enough from all used
        // positions
        do {
            int minDistance = 500; // Increase minimum distance from spawn
            int maxDistance = 3000; // Increase maximum distance from spawn
            int distance = minDistance + random.nextInt(maxDistance - minDistance);
            double angle = random.nextDouble() * Math.PI * 2;

            int x = (int) (Math.cos(angle) * distance);
            int z = (int) (Math.sin(angle) * distance);

            pos = findSafeSpot(level, x, z);

            // Check if this position is far enough from all other positions
            validPos = true;
            for (BlockPos usedPos : usedPositions) {
                if (usedPos.distSqr(pos) < 2 * 2) { // Only 2 blocks minimum distance between players
                    validPos = false;
                    break;
                }
            }
        } while (!validPos);

        return pos;
    }

    private BlockPos findSafeSpot(ServerLevel level, int x, int z) {
        // First try to find a spot at the surface in an open area
        BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos(x, 0, z));

        // Always go to Y=128 minimum (above most caves)
        int y = Math.max(surfacePos.getY() + 5, 128);

        // Create a definitely safe position above ground
        BlockPos safePos = new BlockPos(x, y, z);

        // Make sure the position is safe (air blocks)
        while (!level.getBlockState(safePos).isAir() || !level.getBlockState(safePos.above()).isAir()) {
            safePos = safePos.above();
        }

        CraftMine.LOGGER.info("Created safe position at: " + safePos);
        return safePos;
    }

    // Maps to store player inventories
    private final Map<UUID, List<ItemStack>> playerInventories = new HashMap<>();
    private final Map<UUID, List<ItemStack>> playerExtraItems = new HashMap<>(); // New map for armor and offhand

    private void storeAndClearInventories(MinecraftServer server) {
        playerInventories.clear();
        playerExtraItems.clear(); // Clear extra items map too

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            // Store inventory (make deep copies of all items)
            List<ItemStack> inventory = new ArrayList<>();
            for (ItemStack stack : player.getInventory().items) {
                inventory.add(stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
            }
            playerInventories.put(player.getUUID(), inventory);

            // Also store armor and offhand items
            List<ItemStack> extraItems = new ArrayList<>();
            for (ItemStack stack : player.getInventory().armor) {
                extraItems.add(stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
            }
            extraItems.add(player.getInventory().offhand.get(0).copy());
            playerExtraItems.put(player.getUUID(), extraItems); // Store in the extra items map

            // Clear inventory completely including armor and offhand
            player.getInventory().clearContent();
        }

        CraftMine.LOGGER.info("Stored and cleared inventories for {} players", playerInventories.size());
    }

    private void restoreInventories(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID playerUUID = player.getUUID();

            // Clear current inventory first
            player.getInventory().clearContent();

            if (playerInventories.containsKey(playerUUID)) {
                // Restore saved main inventory
                List<ItemStack> savedInventory = playerInventories.get(playerUUID);
                for (int i = 0; i < Math.min(savedInventory.size(), player.getInventory().items.size()); i++) {
                    if (i == 0) {
                        // Skip the first slot - we'll put the controller there later
                        continue;
                    }

                    ItemStack stack = savedInventory.get(i);
                    if (!stack.isEmpty()) {
                        player.getInventory().items.set(i, stack.copy());
                    }
                }

                // Restore armor and offhand if saved
                if (playerExtraItems.containsKey(playerUUID)) { // Updated to use the new map
                    List<ItemStack> extraItems = playerExtraItems.get(playerUUID);

                    // Restore armor
                    for (int i = 0; i < Math.min(extraItems.size() - 1, player.getInventory().armor.size()); i++) {
                        ItemStack stack = extraItems.get(i);
                        if (!stack.isEmpty()) {
                            player.getInventory().armor.set(i, stack.copy());
                        }
                    }

                    // Restore offhand
                    if (extraItems.size() > player.getInventory().armor.size()) {
                        ItemStack offhandItem = extraItems.get(extraItems.size() - 1);
                        if (!offhandItem.isEmpty()) {
                            player.getInventory().offhand.set(0, offhandItem.copy());
                        }
                    }
                }
            }

            // Always add game controller to first hotbar slot (slot 0)
            ItemStack controllerItem = new ItemStack(ModRegistry.GAME_CONTROLLER_ITEM.get());
            player.getInventory().setItem(0, controllerItem);

            // Force inventory update
            player.inventoryMenu.broadcastChanges();
            player.sendSystemMessage(
                    Component.literal("Game ended. Inventory restored. Game Controller placed in first slot.")
                            .withStyle(ChatFormatting.GREEN));
        }

        CraftMine.LOGGER.info("Restored inventories for all players");
    }
}
