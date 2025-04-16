package com.seishironagi.craftmine.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.seishironagi.craftmine.CraftMine;
import com.seishironagi.craftmine.network.ModMessages;
import com.seishironagi.craftmine.network.packet.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GameControllerScreen extends Screen {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CraftMine.MODID, "textures/gui/game_controller.png");
    private int guiLeft, guiTop;
    private final int xSize = 176;
    private final int ySize = 166;

    public GameControllerScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();

        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        // Start Game button
        this.addRenderableWidget(new Button.Builder(Component.literal("Start Game"), (button) -> {
            ModMessages.sendToServer(new StartGameC2SPacket());
            this.onClose();
        }).bounds(guiLeft + 20, guiTop + 30, 140, 20).build());

        // Choose Team button
        this.addRenderableWidget(new Button.Builder(Component.literal("Choose Team"), (button) -> {
            minecraft.setScreen(new TeamSelectionScreen(Component.literal("Select Team")));
        }).bounds(guiLeft + 20, guiTop + 60, 140, 20).build());

        // Settings button
        this.addRenderableWidget(new Button.Builder(Component.literal("Settings"), (button) -> {
            minecraft.setScreen(new GameSettingsScreen(Component.literal("Game Settings")));
        }).bounds(guiLeft + 20, guiTop + 90, 140, 20).build());

        // Info button
        this.addRenderableWidget(new Button.Builder(Component.literal("Info"), (button) -> {
            minecraft.setScreen(new GameInfoScreen(Component.literal("Game Info")));
        }).bounds(guiLeft + 20, guiTop + 120, 140, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(TEXTURE, guiLeft, guiTop, 0, 0, xSize, ySize);

        // Draw title
        guiGraphics.drawCenteredString(font, this.title, guiLeft + xSize / 2, guiTop + 10, 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    public void renderBackground(GuiGraphics guiGraphics) {
        super.renderBackground(guiGraphics);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
