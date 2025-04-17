package com.seishironagi.craftmine.gui;

import com.seishironagi.craftmine.CraftMine;
import com.seishironagi.craftmine.gui.util.REIHelper;
import com.seishironagi.craftmine.network.ModMessages;
import com.seishironagi.craftmine.network.packet.OpenSettingsScreenC2SPacket;
import com.seishironagi.craftmine.network.packet.OpenTeamScreenC2SPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GameInfoScreen extends AbstractContainerScreen<GameInfoContainer> {
    public GameInfoScreen(GameInfoContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        
        // Set six row chest size
        this.imageWidth = 176;
        this.imageHeight = 222;
        
        // Position the inventory label
        this.inventoryLabelY = 128;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Use the helper to render the chest background
        REIHelper.renderSixRowChestBackground(guiGraphics, leftPos, topPos, imageWidth, imageHeight);
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
    
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, 0x404040);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 0x404040);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle button clicks for the four buttons
        for (int i = 0; i < 4; i++) {
            Slot slot = this.menu.slots.get(i);
            if (this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                handleButtonClick(i);
                return true;
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    private void handleButtonClick(int buttonId) {
        switch (buttonId) {
            case 0: // Start Game button
                this.minecraft.setScreen(null); // Close screen
                break;
            case 1: // Team Selection button
                ModMessages.sendToServer(new OpenTeamScreenC2SPacket());
                this.onClose();
                break;
            case 2: // Settings
                ModMessages.sendToServer(new OpenSettingsScreenC2SPacket());
                this.onClose();
                break;
            case 3: // Info button (already on info screen)
                break;
        }
    }
}
