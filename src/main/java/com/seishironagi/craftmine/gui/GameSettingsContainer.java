package com.seishironagi.craftmine.gui;

import com.seishironagi.craftmine.Config;
import com.seishironagi.craftmine.ModRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;


public class GameSettingsContainer extends AbstractContainerMenu {
    private final SimpleContainer settingsContainer = new SimpleContainer(54); // Full chest size
    
    public GameSettingsContainer(int windowId, Inventory playerInventory) {
        super(ModRegistry.GAME_SETTINGS_CONTAINER.get(), windowId);
        
        // Add all 54 chest slots (6 rows x 9 columns)
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new ButtonSlot(settingsContainer, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }
        
        // Add player inventory slots - positioned for 6-row chest
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 174 + row * 18));
            }
        }
        
        // Player hotbar - positioned at bottom
        for (int slot = 0; slot < 9; ++slot) {
            this.addSlot(new Slot(playerInventory, slot, 8 + slot * 18, 232));
        }
        
        setupButtons();
    }
    
    private void setupButtons() {
        // Create distinct items for settings
        ItemStack timeItem = new ItemStack(Items.CLOCK);
        timeItem.setHoverName(Component.literal("Game Time: " + Config.defaultGameTime + " mins"));
        settingsContainer.setItem(0, timeItem);
        
        ItemStack toggleItem = new ItemStack(Items.COMPARATOR);
        toggleItem.setHoverName(Component.literal("Item-Specific Times: " + 
            (Config.useItemSpecificTimes ? "ON" : "OFF")));
        settingsContainer.setItem(1, toggleItem);
        
        ItemStack backItem = new ItemStack(Items.BARRIER);
        backItem.setHoverName(Component.literal("Back to Main Menu"));
        settingsContainer.setItem(2, backItem);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // Standard implementation
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        
        if (slot != null && slot.hasItem()) {
            ItemStack slotItem = slot.getItem();
            itemstack = slotItem.copy();
            
            if (index < 3) {
                // Don't move button items
                return ItemStack.EMPTY;
            } else if (index < 30) {
                if (!this.moveItemStackTo(slotItem, 30, 39, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < 39) {
                if (!this.moveItemStackTo(slotItem, 3, 30, false)) {
                    return ItemStack.EMPTY;
                }
            }
            
            if (slotItem.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        
        return itemstack;
    }
    
    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }
    
    // Button slot that prevents item movement
    private static class ButtonSlot extends Slot {
        public ButtonSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }
        
        @Override
        public boolean mayPickup(@NotNull Player player) {
            return false;
        }
        
        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return false;
        }
    }
}
