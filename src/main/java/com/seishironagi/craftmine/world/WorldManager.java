package com.seishironagi.craftmine.world;

import com.seishironagi.craftmine.CraftMine;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = CraftMine.MOD_ID)
public class WorldManager {
    // Define our game world dimension
    public static final String GAME_WORLD_ID = "game_world";
    public static final ResourceLocation GAME_WORLD_LOCATION = new ResourceLocation(CraftMine.MOD_ID, GAME_WORLD_ID);
    public static final ResourceKey<Level> GAME_WORLD_KEY = ResourceKey.create(Registries.DIMENSION,
            GAME_WORLD_LOCATION);

    private static boolean isInitialized = false;
    private static final Random random = new Random();

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        if (!isInitialized) {
            CraftMine.LOGGER.info("Initializing World Manager...");
            isInitialized = true;
        }
    }

    /**
     * Start a new game by regenerating the game world and teleporting players
     */
    public static void startNewGame(List<ServerPlayer> players) {
        CraftMine.LOGGER.info("Starting new game with " + players.size() + " players");

        // Get server instance
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            CraftMine.LOGGER.error("Server not available!");
            return;
        }

        // First move all players to overworld (lobby)
        for (ServerPlayer player : players) {
            teleportToLobby(player);
            player.sendSystemMessage(Component.literal("Preparing game world, please wait...")
                    .withStyle(ChatFormatting.GOLD));
        }

        // Regenerate the game world
        regenerateGameWorld(server).thenAccept(success -> {
            if (success) {
                // Find a safe spawn location
                ServerLevel gameWorld = server.getLevel(GAME_WORLD_KEY);
                if (gameWorld != null) {
                    // Generate a random spawn far from origin
                    int distance = 1000 + random.nextInt(3000);
                    double angle = random.nextDouble() * Math.PI * 2;
                    int x = (int) (Math.cos(angle) * distance);
                    int z = (int) (Math.sin(angle) * distance);

                    // Find safe spot and pre-generate chunks
                    BlockPos spawnPos = findSafeSpot(gameWorld, x, z);
                    preGenerateChunks(gameWorld, new ChunkPos(spawnPos), 5);

                    // Teleport all players to game world
                    for (ServerPlayer player : players) {
                        // Add small offset to prevent player stacking
                        int offsetX = random.nextInt(6) - 3;
                        int offsetZ = random.nextInt(6) - 3;

                        teleportToGameWorld(player, spawnPos.getX() + offsetX, spawnPos.getZ() + offsetZ);
                        player.sendSystemMessage(Component.literal("Game started! Find the target item!")
                                .withStyle(ChatFormatting.GREEN));
                    }

                    CraftMine.LOGGER.info("Game started with spawn at " + spawnPos);
                }
            } else {
                // Notify players of failure
                for (ServerPlayer player : players) {
                    player.sendSystemMessage(Component.literal("Failed to start game!")
                            .withStyle(ChatFormatting.RED));
                }
            }
        });
    }

    /**
     * Regenerate the game world by deleting and recreating it
     */
    private static CompletableFuture<Boolean> regenerateGameWorld(MinecraftServer server) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Get game world
                ServerLevel gameWorld = server.getLevel(GAME_WORLD_KEY);
                if (gameWorld == null) {
                    CraftMine.LOGGER.error("Game world dimension not registered! Check your dimension registration.");
                    return false;
                }

                // Unload current world data - using the correct method signature for 1.20.1
                // Instead of setting spawn, we'll just make sure all players are out of the
                // dimension
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    if (player.level().dimension().equals(GAME_WORLD_KEY)) {
                        teleportToLobby(player);
                    }
                }

                // Delete world files (this will force regeneration on next access)
                try {
                    File worldFolder = new File(server.getWorldPath(LevelResource.ROOT).toFile(),
                            GAME_WORLD_LOCATION.getPath());

                    if (worldFolder.exists()) {
                        CraftMine.LOGGER.info("Deleting game world files at: " + worldFolder.getAbsolutePath());
                        FileUtils.deleteDirectory(worldFolder);
                    }
                } catch (Exception e) {
                    CraftMine.LOGGER.error("Failed to delete game world files", e);
                    return false;
                }

                // Success
                CraftMine.LOGGER.info("Game world regenerated successfully");
                return true;

            } catch (Exception e) {
                CraftMine.LOGGER.error("Error regenerating game world", e);
                return false;
            }
        });
    }

    /**
     * Find a safe spawn location on land (not in caves or ocean)
     */
    private static BlockPos findSafeSpot(ServerLevel level, int x, int z) {
        // Try multiple attempts to find a suitable location
        for (int attempts = 0; attempts < 10; attempts++) {
            if (attempts > 0) {
                // Adjust coordinates on retry
                x += random.nextInt(200) - 100;
                z += random.nextInt(200) - 100;
            }

            // Get surface position (excludes trees, water)
            BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos(x, 0, z));

            // Skip if we're in ocean biome
            if (isOceanBiome(level, surfacePos)) {
                CraftMine.LOGGER.info("Skipping ocean biome at " + surfacePos);
                continue;
            }

            // Skip if position is in water
            if (level.getFluidState(surfacePos).isSource() ||
                    level.getFluidState(surfacePos.below()).isSource()) {
                continue;
            }

            // Ensure there's air for the player
            BlockPos playerPos = surfacePos.above();
            if (level.getBlockState(playerPos).isAir() &&
                    level.getBlockState(playerPos.above()).isAir()) {
                return playerPos;
            }
        }

        // Fallback to a position with adjusted height
        BlockPos fallback = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos(x, 0, z)).above();
        CraftMine.LOGGER.warn("Using fallback position: " + fallback);
        return fallback;
    }

    /**
     * Check if the location is in an ocean biome
     */
    private static boolean isOceanBiome(ServerLevel level, BlockPos pos) {
        try {
            return level.getBiome(pos).unwrapKey()
                    .map(key -> key.location().getPath().contains("ocean"))
                    .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Pre-generate chunks around spawn for smoother gameplay
     */
    private static void preGenerateChunks(ServerLevel level, ChunkPos center, int radius) {
        CraftMine.LOGGER.info("Pre-generating chunks around " + center + " with radius " + radius);

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                level.getChunk(center.x + x, center.z + z);
            }
        }
    }

    /**
     * Teleport a player to the game world at specific coordinates
     */
    public static void teleportToGameWorld(ServerPlayer player, int x, int z) {
        ServerLevel gameWorld = player.getServer().getLevel(GAME_WORLD_KEY);
        if (gameWorld == null) {
            CraftMine.LOGGER.error("Game world not available!");
            return;
        }

        // Find safe spot at the requested coordinates
        BlockPos safePos = findSafeSpot(gameWorld, x, z);

        // Teleport player
        player.teleportTo(gameWorld,
                safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5,
                player.getYRot(), player.getXRot());
    }

    /**
     * Teleport a player back to the lobby (overworld)
     */
    public static void teleportToLobby(ServerPlayer player) {
        ServerLevel overworld = player.getServer().overworld();
        BlockPos spawnPos = overworld.getSharedSpawnPos();

        player.teleportTo(overworld,
                spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                player.getYRot(), player.getXRot());
    }
}
