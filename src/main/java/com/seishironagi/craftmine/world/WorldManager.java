package com.seishironagi.craftmine.world;

import com.mojang.serialization.Lifecycle;
import com.seishironagi.craftmine.CraftMine;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.lang.reflect.Field;

/**
 * Manages custom game worlds for the CraftMine mod
 * Handles creation, deletion, and player teleportation
 */
@Mod.EventBusSubscriber(modid = CraftMine.MOD_ID)
public class WorldManager {
    // World identification constants
    public static final String GAME_WORLD_ID = "game_world";
    public static final ResourceLocation GAME_WORLD_LOCATION = ResourceLocation
            .tryParse(CraftMine.MOD_ID + ":" + GAME_WORLD_ID);
    public static final ResourceKey<Level> GAME_WORLD_KEY = ResourceKey.create(Registries.DIMENSION,
            GAME_WORLD_LOCATION);

    // Tracking variables
    private static boolean isGeneratingWorld = false;
    private static final Random random = new Random();
    private static long currentWorldSeed = 0;

    // Number of chunks to preload around spawn for smoother gameplay
    private static final int PRELOAD_RADIUS = 6;

    /**
     * Main method to start a new game with fresh world
     * This coordinates world creation and player teleportation
     * 
     * @param players    List of players to teleport to the new world
     * @param onComplete Callback to run when world is ready and players are
     *                   teleported
     */
    public static void startNewGame(List<ServerPlayer> players, Consumer<Boolean> onComplete) {
        if (isGeneratingWorld) {
            CraftMine.LOGGER.warn("World generation already in progress, cannot start new game");
            if (onComplete != null)
                onComplete.accept(false);
            return;
        }

        isGeneratingWorld = true;
        CraftMine.LOGGER.info("Starting new game with {} players", players.size());

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            CraftMine.LOGGER.error("Cannot start game: Server not available");
            isGeneratingWorld = false;
            if (onComplete != null)
                onComplete.accept(false);
            return;
        }

        // First move all players to lobby (overworld) and inform them
        for (ServerPlayer player : players) {
            teleportToLobby(player);
            player.sendSystemMessage(Component.literal("Preparing game world, please wait...")
                    .withStyle(ChatFormatting.GOLD));
        }

        // Generate new world seed
        currentWorldSeed = random.nextLong();

        // Delete old world and create new one
        recreateGameWorld(server).thenAccept(success -> {
            if (!success) {
                CraftMine.LOGGER.error("Failed to create game world");
                for (ServerPlayer player : players) {
                    player.sendSystemMessage(Component.literal("Failed to create game world!")
                            .withStyle(ChatFormatting.RED));
                }
                isGeneratingWorld = false;
                if (onComplete != null)
                    onComplete.accept(false);
                return;
            }

            // Get the game world
            ServerLevel gameWorld = server.getLevel(GAME_WORLD_KEY);
            if (gameWorld == null) {
                CraftMine.LOGGER.error("Game world not available after creation!");
                isGeneratingWorld = false;
                if (onComplete != null)
                    onComplete.accept(false);
                return;
            }

            // Find a safe spawn location and pre-generate chunks around it
            BlockPos spawnPos = findSafeSurfaceLocation(gameWorld, 0, 0);
            CraftMine.LOGGER.info("Game spawn selected at {}", spawnPos);

            // Pre-generate chunks around spawn for smoother gameplay
            preloadChunks(gameWorld, spawnPos).thenAccept(preloadSuccess -> {
                // Teleport all players to the new world at safe locations
                teleportPlayersToGameWorld(players, gameWorld, spawnPos);

                CraftMine.LOGGER.info("All players teleported to game world successfully");
                isGeneratingWorld = false;

                // Call the completion callback (e.g., to start the game timer)
                if (onComplete != null)
                    onComplete.accept(true);
            });
        });
    }

    /**
     * Teleports players to slightly different positions around the spawn point
     * to prevent player stacking
     */
    private static void teleportPlayersToGameWorld(List<ServerPlayer> players, ServerLevel gameWorld,
            BlockPos spawnPos) {
        int i = 0;
        for (ServerPlayer player : players) {
            // Calculate offset in a circular pattern to avoid player collision
            double angle = (Math.PI * 2) * ((double) i / players.size());
            int offsetX = (int) (Math.sin(angle) * 3.0);
            int offsetZ = (int) (Math.cos(angle) * 3.0);

            // Find specific safe location for this player
            BlockPos playerPos = findSafeSurfaceLocation(gameWorld,
                    spawnPos.getX() + offsetX, spawnPos.getZ() + offsetZ);

            // Teleport player
            player.teleportTo(gameWorld,
                    playerPos.getX() + 0.5, playerPos.getY(), playerPos.getZ() + 0.5,
                    player.getYRot(), player.getXRot());

            // Set respawn point in game world
            player.setRespawnPosition(gameWorld.dimension(), playerPos, 0.0F, true, false);

            // Send success message
            player.sendSystemMessage(Component.literal("Teleported to game world!")
                    .withStyle(ChatFormatting.GREEN));

            i++;
        }
    }

    /**
     * Pre-generates chunks around spawn to prevent lag when players move
     */
    private static CompletableFuture<Boolean> preloadChunks(ServerLevel level, BlockPos center) {
        ChunkPos centerChunk = new ChunkPos(center);
        CraftMine.LOGGER.info("Pre-generating {} chunks around {}",
                (PRELOAD_RADIUS * 2 + 1) * (PRELOAD_RADIUS * 2 + 1), centerChunk);

        return CompletableFuture.supplyAsync(() -> {
            try {
                for (int x = -PRELOAD_RADIUS; x <= PRELOAD_RADIUS; x++) {
                    for (int z = -PRELOAD_RADIUS; z <= PRELOAD_RADIUS; z++) {
                        if (x * x + z * z <= PRELOAD_RADIUS * PRELOAD_RADIUS) {
                            level.getChunk(centerChunk.x + x, centerChunk.z + z);
                        }
                    }
                }
                return true;
            } catch (Exception e) {
                CraftMine.LOGGER.error("Error pre-generating chunks", e);
                return false;
            }
        });
    }

    /**
     * Recreates the game world with a new random seed
     * This deletes the old world if it exists
     */
    private static CompletableFuture<Boolean> recreateGameWorld(MinecraftServer server) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Get the game world if it exists
                ServerLevel oldGameWorld = server.getLevel(GAME_WORLD_KEY);

                // Force unload any players from the dimension first
                if (oldGameWorld != null) {
                    CraftMine.LOGGER.info("Unloading existing game world...");
                    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                        if (player.level().dimension().equals(GAME_WORLD_KEY)) {
                            teleportToLobby(player);
                        }
                    }
                }

                // Delete the dimension folder to force regeneration
                try {
                    File worldFolder = new File(server.getWorldPath(LevelResource.ROOT).toFile(), GAME_WORLD_ID);
                    if (worldFolder.exists()) {
                        CraftMine.LOGGER.info("Deleting old game world directory at {}", worldFolder.getAbsolutePath());
                        FileUtils.deleteDirectory(worldFolder);
                    }
                } catch (IOException e) {
                    CraftMine.LOGGER.error("Failed to delete game world directory", e);
                    return false;
                }

                // Apply new random seed for world generation via reflection
                try {
                    LevelSettings settings = server.getWorldData().getLevelSettings();
                    Field seedField = LevelSettings.class.getDeclaredField("seed");
                    seedField.setAccessible(true);
                    seedField.setLong(settings, currentWorldSeed);
                    CraftMine.LOGGER.info("Applied new world seed: {}", currentWorldSeed);
                } catch (Exception e) {
                    CraftMine.LOGGER.error("Failed to apply seed via reflection", e);
                }

                // Force the server to create/load the dimension
                CraftMine.LOGGER.info("Creating new game world with seed {}", currentWorldSeed);
                ServerLevel newGameWorld = server.getLevel(GAME_WORLD_KEY);
                if (newGameWorld == null) {
                    CraftMine.LOGGER.error("Failed to load game world! Check dimension registration.");
                    return false;
                }

                CraftMine.LOGGER.info("Game world recreated with new seed");
                return true;
            } catch (Exception e) {
                CraftMine.LOGGER.error("Error recreating game world", e);
                return false;
            }
        });
    }

    /**
     * Finds a safe location on the surface of the world
     * This ensures players don't spawn underground, in water, or in dangerous areas
     */
    public static BlockPos findSafeSurfaceLocation(ServerLevel level, int x, int z) {
        // Try multiple attempts to find a suitable location
        for (int attempt = 0; attempt < 25; attempt++) {
            // For subsequent attempts, add some randomness to coordinates
            if (attempt > 0) {
                x += random.nextInt(200) - 100;
                z += random.nextInt(200) - 100;
            }

            // Get the surface position, excluding trees and other non-solid surfaces
            BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    new BlockPos(x, 0, z));

            // Skip if we're in an ocean biome
            if (level.getBiome(surfacePos).unwrapKey().isPresent() &&
                    level.getBiome(surfacePos).unwrapKey().get().location().getPath().contains("ocean")) {
                continue;
            }

            // Check if we're above water or lava
            BlockPos checkPos = surfacePos.below();
            BlockState blockBelow = level.getBlockState(checkPos);

            if (!blockBelow.getFluidState().isEmpty()) {
                // Standing on water or lava, not safe
                continue;
            }

            // Check if the ground is solid
            if (!blockBelow.isSolid() || blockBelow.is(Blocks.BEDROCK)) {
                continue;
            }

            // Make sure there's enough air space for the player (2 blocks)
            BlockPos playerPos = surfacePos;
            if (!level.getBlockState(playerPos).isAir() ||
                    !level.getBlockState(playerPos.above()).isAir()) {

                // Try moving up until we find air
                for (int i = 0; i < 5; i++) {
                    playerPos = playerPos.above();
                    if (level.getBlockState(playerPos).isAir() &&
                            level.getBlockState(playerPos.above()).isAir()) {
                        break;
                    }

                    // If we can't find air after 5 blocks, this spot isn't good
                    if (i == 4)
                        continue;
                }
            }

            // We found a safe spot
            CraftMine.LOGGER.info("Found safe spot at {}", playerPos);
            return playerPos;
        }

        // If we couldn't find a good spot after many attempts, use the world spawn as
        // fallback
        CraftMine.LOGGER.warn("Couldn't find safe location after 25 attempts, using fallback");
        BlockPos fallbackPos = level.getSharedSpawnPos().above();

        // Make sure the fallback is actually safe
        while (!level.getBlockState(fallbackPos).isAir() ||
                !level.getBlockState(fallbackPos.above()).isAir()) {
            fallbackPos = fallbackPos.above();
        }

        return fallbackPos;
    }

    /**
     * Teleports a player to the lobby (overworld)
     */
    public static void teleportToLobby(ServerPlayer player) {
        ServerLevel overworld = player.getServer().overworld();
        BlockPos spawn = overworld.getSharedSpawnPos();

        player.teleportTo(overworld,
                spawn.getX() + 0.5, spawn.getY() + 1, spawn.getZ() + 0.5,
                player.getYRot(), player.getXRot());
    }

    /**
     * Teleports a player to the game world at a safe location
     */
    public static void teleportToGameWorld(ServerPlayer player) {
        ServerLevel gameWorld = player.getServer().getLevel(GAME_WORLD_KEY);
        if (gameWorld == null) {
            CraftMine.LOGGER.error("Game world not available!");
            return;
        }

        // Find a safe spot near world spawn
        BlockPos spawnPos = findSafeSurfaceLocation(gameWorld, 0, 0);

        player.teleportTo(gameWorld,
                spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                player.getYRot(), player.getXRot());
    }

    /**
     * Checks if a world generation is currently in progress
     */
    public static boolean isGeneratingWorld() {
        return isGeneratingWorld;
    }
}
