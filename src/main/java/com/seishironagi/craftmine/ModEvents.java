package com.seishironagi.craftmine;

import com.seishironagi.craftmine.network.ModMessages;
import com.seishironagi.craftmine.network.packet.GameDataSyncS2CPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = CraftMine.MOD_ID)
public class ModEvents {

    private static int tickCounter = 0;
    // Track players who have already received the controller item
    private static final Set<UUID> playersReceivedController = new HashSet<>();

    @SubscribeEvent
    public static void onPlayerJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() && event.getEntity() instanceof Player) {
            return;
        }

        if (!event.getLevel().isClientSide() && event.getEntity() instanceof ServerPlayer player) {
            // Give game controller item ONLY if player has never received it before
            if (!playersReceivedController.contains(player.getUUID())) {
                ItemStack controllerItem = new ItemStack(ModRegistry.GAME_CONTROLLER_ITEM.get());
                if (!player.getInventory().contains(controllerItem)) {
                    player.getInventory().add(controllerItem);
                    playersReceivedController.add(player.getUUID());
                    CraftMine.LOGGER.info("Gave controller to new player: {}", player.getName().getString());
                }
            }

            // Sync game state with enhanced data
            boolean isRedTeam = GameManager.getInstance().getTeamManager().isRedTeam(player);
            ItemStack targetItem = ItemStack.EMPTY;
            if (GameManager.getInstance().isGameRunning() && GameManager.getInstance().getTargetItem() != null) {
                targetItem = new ItemStack(GameManager.getInstance().getTargetItem());
            }

            ModMessages.sendToPlayer(new GameDataSyncS2CPacket(
                    GameManager.getInstance().getRemainingTimeSeconds(),
                    GameManager.getInstance().isGameRunning(),
                    isRedTeam,
                    targetItem,
                    false // This value doesn't matter for initial sync
            ), player);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tickCounter++;

            // Sync timer every second (20 ticks)
            if (tickCounter >= 20) {
                tickCounter = 0;

                if (GameManager.getInstance().isGameRunning()) {
                    // For each player, send customized data based on their team
                    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                    if (server != null) {
                        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                            boolean isRedTeam = GameManager.getInstance().getTeamManager().isRedTeam(player);
                            ItemStack targetItem = ItemStack.EMPTY;
                            if (GameManager.getInstance().getTargetItem() != null) {
                                targetItem = new ItemStack(GameManager.getInstance().getTargetItem());
                            }

                            ModMessages.sendToPlayer(new GameDataSyncS2CPacket(
                                    GameManager.getInstance().getRemainingTimeSeconds(),
                                    true,
                                    isRedTeam,
                                    targetItem,
                                    false), player);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerInventoryChange(PlayerEvent.ItemPickupEvent event) {
        if (event.getEntity().level().isClientSide())
            return;

        Player player = event.getEntity();

        // Check if game is running and player is in red team
        if (GameManager.getInstance().isGameRunning() &&
                GameManager.getInstance().getTeamManager().isRedTeam(player)) {

            // Check if the picked up item is the target item
            if (event.getStack().getItem() == GameManager.getInstance().getTargetItem()) {
                GameManager.getInstance().endGame(true);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide())
            return;

        // Check if a player died
        if (!(event.getEntity() instanceof Player victim))
            return;

        // Check if the player is from red team and game is running
        if (!GameManager.getInstance().isGameRunning() ||
                !GameManager.getInstance().getTeamManager().isRedTeam(victim)) {
            return;
        }

        // Check if killed by another player
        DamageSource source = event.getSource();
        if (source.getEntity() instanceof Player killer &&
                GameManager.getInstance().getTeamManager().isBlueTeam(killer)) {
            // Red team player was killed by blue team player
            victim.sendSystemMessage(Component.literal("§cYou were killed by a blue team player! Your team wins!")
                    .withStyle(ChatFormatting.RED));

            killer.sendSystemMessage(Component.literal("§cYou killed a red team player! Their team wins!")
                    .withStyle(ChatFormatting.RED));

            // Red team wins
            GameManager.getInstance().endGame(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        // Explicitly DO NOT give controller item on respawn
        // The item should only be restored after a game ends via
        // GameManager.restoreInventories()
    }

    @SubscribeEvent
    public static void onGameStart(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().level().isClientSide())
            return;

        Player player = event.getEntity();
        ServerPlayer serverPlayer = (ServerPlayer) player;

        // Give game controller item for first-time logins only
        if (!playersReceivedController.contains(player.getUUID())) {
            ItemStack controllerItem = new ItemStack(ModRegistry.GAME_CONTROLLER_ITEM.get());
            if (!player.getInventory().contains(controllerItem)) {
                player.getInventory().add(controllerItem);
                playersReceivedController.add(player.getUUID());
            }
        }

        // Welcome message
        player.sendSystemMessage(Component.literal("Welcome to CraftMine! Use the Game Controller to begin.")
                .withStyle(ChatFormatting.GREEN));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END || event.player.level().isClientSide())
            return;

        // Check if the player is frozen
        if (GameManager.getInstance().isGameRunning() && GameManager.getInstance().isPlayerFrozen(event.player)) {
            // Prevent movement by resetting position and motion
            event.player.setDeltaMovement(0, event.player.getDeltaMovement().y, 0);

            // Allow slight vertical movement (falling)
            if (event.player.getDeltaMovement().y > 0) {
                event.player.setDeltaMovement(0, 0, 0);
            }
        }
    }

    private static void teleportToRandomLocation(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null)
            return;

        Random random = new Random();

        // Get current world
        net.minecraft.server.level.ServerLevel level = player.serverLevel();

        // Calculate random coordinates with increased distance range
        int minDistance = 500;
        int maxDistance = 3000;
        int distance = minDistance + random.nextInt(maxDistance - minDistance);
        double angle = random.nextDouble() * Math.PI * 2;

        int x = (int) (Math.cos(angle) * distance);
        int z = (int) (Math.sin(angle) * distance);

        // Find a safe spot to teleport
        BlockPos pos = findSafeSpot(level, x, z);

        // Teleport the player
        player.teleportTo(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, player.getYRot(),
                player.getXRot());
        player.sendSystemMessage(Component.literal("You have been teleported to a random location!")
                .withStyle(ChatFormatting.GREEN));
    }

    private static BlockPos findSafeSpot(net.minecraft.server.level.ServerLevel level, int x, int z) {
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
        BlockPos surfacePos = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                new BlockPos(x, 0, z));
        BlockPos safePos = surfacePos.above(5);

        CraftMine.LOGGER.info("Using fallback position at: " + safePos);
        return safePos;
    }

    // Helper method to mark player as having received controller
    public static void markPlayerReceivedController(UUID playerId) {
        playersReceivedController.add(playerId);
    }
}
