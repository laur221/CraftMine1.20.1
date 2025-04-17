package com.seishironagi.craftmine.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.seishironagi.craftmine.CraftMine;
import com.seishironagi.craftmine.Config;
import com.seishironagi.craftmine.gui.util.ChestRenderer;
import com.seishironagi.craftmine.network.ModMessages;
import com.seishironagi.craftmine.network.packet.JoinTeamC2SPacket;
import com.seishironagi.craftmine.network.packet.OpenMainMenuC2SPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TeamSelectionScreen extends AbstractContainerScreen<GameMenuContainer> {
    public TeamSelectionScreen(GameMenuContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
        
        // Set 6-row chest size
        this.imageWidth = 176;
        this.imageHeight = 222; // 6 rows chest height
        
        // Position the inventory label for 6 rows
        this.inventoryLabelY = 128;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        ChestRenderer.renderSixRowChestBackground(guiGraphics, leftPos, topPos, imageWidth, imageHeight);
    }
    
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, 0x404040);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 0x404040);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle button clicks
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
            case 0: // Red Team
                ModMessages.sendToServer(new JoinTeamC2SPacket(true));
                this.onClose();
                break;
            case 1: // Blue Team
                ModMessages.sendToServer(new JoinTeamC2SPacket(false));
                this.onClose();
                break;
            case 2: // Back to main menu
                ModMessages.sendToServer(new OpenMainMenuC2SPacket());
                this.onClose();
                break;
        }
    }
}
