package com.seishironagi.craftmine.gui;

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
import net.minecraft.world.item.enchantment.Enchantments;

public class GameInfoContainer extends AbstractContainerMenu {
    private final SimpleContainer buttonContainer = new SimpleContainer(4);

    public GameInfoContainer(int windowId, Inventory playerInventory) {
        super(ModRegistry.GAME_INFO_CONTAINER.get(), windowId);
        
        // Add button slots in a 2x2 grid
        addSlot(new ButtonSlot(buttonContainer, 0, 62, 20));  // Start Game
        addSlot(new ButtonSlot(buttonContainer, 1, 98, 20));  // Choose Team
        addSlot(new ButtonSlot(buttonContainer, 2, 62, 50));  // Settings
        addSlot(new ButtonSlot(buttonContainer, 3, 98, 50));  // Info
        
        // Add player inventory slots - positioned for a double chest
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }
        
        // Player hotbar slots
        for (int slot = 0; slot < 9; ++slot) {
            this.addSlot(new Slot(playerInventory, slot, 8 + slot * 18, 198));
        }
        
        setupButtons();
    }
    
    private void setupButtons() {
        // Create visually distinct buttons with appropriate items
        buttonContainer.setItem(0, createButtonItem(Items.EMERALD, "Start Game"));
        buttonContainer.setItem(1, createButtonItem(Items.SHIELD, "Choose Team"));
        buttonContainer.setItem(2, createButtonItem(Items.CLOCK, "Settings"));
        
        // Make the info button special (enchanted) since we're on the info screen
        ItemStack infoButton = createButtonItem(Items.BOOK, "Info");
        infoButton.enchant(Enchantments.UNBREAKING, 1);
        buttonContainer.setItem(3, infoButton);
    }
    
    private ItemStack createButtonItem(net.minecraft.world.item.Item item, String name) {
        ItemStack stack = new ItemStack(item);
        stack.setHoverName(Component.literal(name));
        return stack;
    }
    
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        
        if (slot != null && slot.hasItem()) {
            ItemStack slotItem = slot.getItem();
            itemstack = slotItem.copy();
            
            if (index < 4) {
                // Don't allow moving button items
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
        }
        
        return itemstack;
    }
    
    @Override
    public boolean stillValid(Player player) {
        return true;
    }
    
    // Slot that doesn't allow players to take or place items
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
