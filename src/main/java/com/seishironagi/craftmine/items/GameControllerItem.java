package com.seishironagi.craftmine.items;

import com.seishironagi.craftmine.CraftMine;
import com.seishironagi.craftmine.ModRegistry;
import com.seishironagi.craftmine.gui.GameMenuContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class GameControllerItem extends Item {
    public GameControllerItem() {
        super(new Item.Properties()
            .stacksTo(1)
            .rarity(Rarity.EPIC)
            .fireResistant() // Prevents accidental loss
        );
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            // Open the container-based GUI (chest-like interface)
            NetworkHooks.openScreen(serverPlayer, 
                new SimpleMenuProvider(
                    (windowId, playerInventory, playerEntity) -> new GameMenuContainer(windowId, playerInventory),
                    Component.literal("Game Controller")
                )
            );
        }
        
        return InteractionResultHolder.success(itemstack);
    }
    
    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item." + CraftMine.MOD_ID + ".game_controller");
    }
}
