package com.seishironagi.craftmine;

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

        // Calculate game time
        if (Config.useItemSpecificTimes && Config.itemTimes.containsKey(targetItem)) {
            gameTimeMinutes = Config.itemTimes.get(targetItem);
        }

        // Set end time
        gameEndTimeMillis = System.currentTimeMillis() + (gameTimeMinutes * 60 * 1000);

        // Teleport players to random locations
        teleportPlayersToRandomLocations(server);

        // Send messages
        broadcastMessage(Config.gameStartMessage, ChatFormatting.GOLD);

        // Send target message to red team only
        String itemName = targetItem.getDescription().getString();
        String redMessage = String.format(Config.redTeamTaskMessage, itemName, gameTimeMinutes);
        redPlayer.sendSystemMessage(Component.literal(redMessage).withStyle(ChatFormatting.GOLD));

        // Start timer display
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
            }
        };

        gameTimer.scheduleAtFixedRate(displayTask, 0, 1000);
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

        gameRunning = false;
        targetItem = null;
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
                    itemId.contains("elytra") || itemId.contains("chorus")) {
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
            BlockPos pos = getRandomPosition(player.serverLevel(), usedPositions);
            usedPositions.add(pos);

            player.teleportTo(player.serverLevel(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    player.getYRot(), player.getXRot());

            player.sendSystemMessage(Component.literal("Game starting! You've been teleported to a random location.")
                    .withStyle(ChatFormatting.GREEN));
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
                if (usedPos.distSqr(pos) < 200 * 200) { // Increase to 200 blocks minimum distance between players
                    validPos = false;
                    break;
                }
            }
        } while (!validPos);

        return pos;
    }

    private BlockPos findSafeSpot(ServerLevel level, int x, int z) {
        // Start at y=320 (max world height in most dimensions) and scan down
        int startY = Math.min(320, level.getMaxBuildHeight() - 10);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(x, startY, z);

        // First, find the top solid block
        boolean foundSurface = false;

        // Scan down until we find a non-air block
        while (mutable.getY() > level.getMinBuildHeight() + 10) {
            BlockPos pos = mutable.immutable();

            // Check if current block is air and block below is solid
            boolean currentIsAir = level.getBlockState(pos).isAir();
            boolean belowIsSolid = !level.getBlockState(pos.below()).isAir() &&
                    !level.getBlockState(pos.below()).getFluidState().isSource();

            if (currentIsAir && belowIsSolid) {
                // We found a valid position (air with solid block below)
                CraftMine.LOGGER.info("Found safe teleport position at: " + pos);
                return pos;
            }

            mutable.move(0, -1, 0);
        }

        // If we reach here, we couldn't find a good spot
        // Get the height at this position then add 5 blocks to be safe
        BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(x, 0, z));
        BlockPos safePos = surfacePos.above(5);

        CraftMine.LOGGER.info("Using fallback position at: " + safePos);
        return safePos;
    }
}
