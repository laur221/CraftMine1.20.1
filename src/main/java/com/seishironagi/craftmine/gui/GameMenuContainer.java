package com.seishironagi.craftmine.gui;

import com.seishironagi.craftmine.ModRegistry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;

public class GameMenuContainer extends AbstractContainerMenu {
    private final SimpleContainer buttonContainer = new SimpleContainer(4);
    private final Player player;

    public GameMenuContainer(int windowId, Inventory playerInventory) {
        super(ModRegistry.GAME_MENU.get(), windowId);
        this.player = playerInventory.player;
        
        // Add button slots in first row
        addSlot(new ButtonSlot(buttonContainer, 0, 8, 18));   // Start Game
        addSlot(new ButtonSlot(buttonContainer, 1, 26, 18));  // Choose Team
        addSlot(new ButtonSlot(buttonContainer, 2, 44, 18));  // Settings
        addSlot(new ButtonSlot(buttonContainer, 3, 62, 18));  // Info

        // Add empty slots for the rest of the 6 rows
        for (int row = 0; row < 6; ++row) {
            for (int col = 0; col < 9; ++col) {
                if (row == 0 && col < 4) continue; // Skip button slots
                int x = 8 + col * 18;
                int y = 18 + row * 18;
                this.addSlot(new Slot(playerInventory, (row * 9 + col), x, y));
            }
        }
        
        // Player inventory slots
        int playerInvY = 140;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, playerInvY + row * 18));
            }
        }
        
        // Player hotbar
        for (int slot = 0; slot < 9; ++slot) {
            this.addSlot(new Slot(playerInventory, slot, 8 + slot * 18, playerInvY + 58));
        }
        
        // Setup the button items
        setupButtons();
    }
    
    private void setupButtons() {
        // Create visually distinctive buttons
        buttonContainer.setItem(0, createButtonItem(Items.EMERALD, "Start Game"));
        buttonContainer.setItem(1, createButtonItem(Items.SHIELD, "Choose Team"));
        buttonContainer.setItem(2, createButtonItem(Items.CLOCK, "Settings"));
        buttonContainer.setItem(3, createButtonItem(Items.BOOK, "Info"));
    }
    
    private ItemStack createButtonItem(net.minecraft.world.item.Item item, String name) {
        ItemStack stack = new ItemStack(item);
        stack.setHoverName(Component.literal(name));
        return stack;
    }
    
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // Standard quickMove implementation for container
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        
        if (slot != null && slot.hasItem()) {
            ItemStack slotItem = slot.getItem();
            itemstack = slotItem.copy();
            
            if (index < 4) {
                // Don't allow moving the button items
                return ItemStack.EMPTY;
            } else if (index < 31) {
                // Player inventory to hotbar
                if (!this.moveItemStackTo(slotItem, 31, 40, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < 40) {
                // Hotbar to player inventory
                if (!this.moveItemStackTo(slotItem, 4, 31, false)) {
                    return ItemStack.EMPTY;
                }
            }
            
            if (slotItem.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            
            if (slotItem.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }
            
            slot.onTake(player, slotItem);
        }
        
        return itemstack;
    }
    
    @Override
    public boolean stillValid(Player player) {
        return true;
    }
    
    // Custom slot that doesn't allow players to take or place items
    private static class ButtonSlot extends Slot {
        public ButtonSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }
        
        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
        
        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
