package com.seishironagi.craftmine.gui;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class GameInfoContainer extends AbstractContainerMenu {
    public GameInfoContainer(int windowId, Inventory playerInventory) {
        super(MenuType.GENERIC_9x3, windowId);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
    
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // Implementation for quick-move (shift+click)
        return ItemStack.EMPTY;
    }
}
