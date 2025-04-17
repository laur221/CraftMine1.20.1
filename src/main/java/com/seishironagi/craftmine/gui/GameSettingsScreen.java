package com.seishironagi.craftmine.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.seishironagi.craftmine.Config;
import com.seishironagi.craftmine.gui.util.ChestRenderer;
import com.seishironagi.craftmine.network.ModMessages;
import com.seishironagi.craftmine.network.packet.OpenMainMenuC2SPacket;
import com.seishironagi.craftmine.network.packet.SetGameTimeC2SPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GameSettingsScreen extends AbstractContainerScreen<GameSettingsContainer> {
    public GameSettingsScreen(GameSettingsContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
        
        // Use six-row chest dimensions
        this.imageWidth = 176;
        this.imageHeight = 222; // Standard height for 6-row chest
        this.inventoryLabelY = 128; // Adjusted for 6 rows
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        ChestRenderer.renderSixRowChestBackground(guiGraphics, leftPos, topPos, imageWidth, imageHeight);
        
        // Draw time information
        guiGraphics.drawString(font, "Game Time (minutes): " + Config.defaultGameTime, 
                leftPos + 20, topPos + 110, 0x404040);
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
        // Handle button clicks in the container
        for (int i = 0; i < 3; i++) {
            Slot slot = this.menu.slots.get(i);
            if (isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                handleButtonClick(i);
                return true;
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    private void handleButtonClick(int buttonId) {
        switch (buttonId) {
            case 0: // Time settings
                // Implement time changing dialog or increment by 5 minutes
                int newTime = (Config.defaultGameTime % 60) + 5;
                if (newTime > 60) newTime = 5;
                ModMessages.sendToServer(new SetGameTimeC2SPacket(newTime));
                break;
                
            case 1: // Toggle specific times
                ModMessages.sendToServer(new SetGameTimeC2SPacket(-1)); // -1 is our toggle signal
                break;
                
            case 2: // Back to main menu
                ModMessages.sendToServer(new OpenMainMenuC2SPacket());
                this.onClose();
                break;
        }
    }
}
