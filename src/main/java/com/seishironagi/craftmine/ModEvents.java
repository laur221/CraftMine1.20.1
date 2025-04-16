package com.seishironagi.craftmine;

import com.seishironagi.craftmine.items.GameControllerItem;
import com.seishironagi.craftmine.network.ModMessages;
import com.seishironagi.craftmine.network.packet.GameTimerSyncS2CPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = CraftMine.MODID)
public class ModEvents {
    
    private static int tickCounter = 0;
    
    @SubscribeEvent
    public static void onPlayerJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() && event.getEntity() instanceof Player) {
            return;
        }
        
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof ServerPlayer player) {
            // Send welcome message
            player.sendSystemMessage(Component.literal(Config.welcomeMessage)
                    .withStyle(ChatFormatting.GOLD));
            
            // Give game controller item
            ItemStack controllerItem = new ItemStack(ModRegistry.GAME_CONTROLLER_ITEM.get());
            if (!player.getInventory().contains(controllerItem)) {
                player.getInventory().add(controllerItem);
            }
            
            // Sync game state
            ModMessages.sendToPlayer(new GameTimerSyncS2CPacket(
                    GameManager.getInstance().getRemainingTimeSeconds(),
                    GameManager.getInstance().isGameRunning()), 
                player);
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
                    // Send timer update to all players
                    ModMessages.sendToAll(new GameTimerSyncS2CPacket(
                            GameManager.getInstance().getRemainingTimeSeconds(),
                            true));
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void onPlayerInventoryChange(PlayerEvent.ItemPickupEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        
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
}
