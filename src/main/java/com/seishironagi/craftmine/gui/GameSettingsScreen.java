package com.seishironagi.craftmine.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.seishironagi.craftmine.CraftMine;
import com.seishironagi.craftmine.Config;
import com.seishironagi.craftmine.network.ModMessages;
import com.seishironagi.craftmine.network.packet.SetGameTimeC2SPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GameSettingsScreen extends Screen {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CraftMine.MODID, "textures/gui/game_settings.png");
    private int guiLeft, guiTop;
    private final int xSize = 176;
    private final int ySize = 166;
    private EditBox timeEditBox;

    public GameSettingsScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();

        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        // Game time edit box
        this.timeEditBox = new EditBox(this.font, guiLeft + 100, guiTop + 50, 50, 20, Component.literal(""));
        this.timeEditBox.setValue(String.valueOf(Config.defaultGameTime));
        this.addRenderableWidget(timeEditBox);

        // Use item-specific times toggle
        this.addRenderableWidget(new Button.Builder(Component.literal(Config.useItemSpecificTimes ?
                "Item Times: ON" : "Item Times: OFF"), (button) -> {
            ModMessages.sendToServer(new SetGameTimeC2SPacket(-1));
            boolean newState = !Config.useItemSpecificTimes;
            button.setMessage(Component.literal(newState ? "Item Times: ON" : "Item Times: OFF"));
        }).bounds(guiLeft + 20, guiTop + 80, 140, 20).build());

        // Save button
        this.addRenderableWidget(new Button.Builder(Component.literal("Save"), (button) -> {
            try {
                int time = Integer.parseInt(timeEditBox.getValue());
                if (time > 0 && time <= 60) {
                    ModMessages.sendToServer(new SetGameTimeC2SPacket(time));
                    Config.defaultGameTime = time;
                }
            } catch (NumberFormatException ignored) {
            }
            minecraft.setScreen(new GameControllerScreen(Component.literal("Game Controller")));
        }).bounds(guiLeft + 20, guiTop + 110, 60, 20).build());

        // Cancel button
        this.addRenderableWidget(new Button.Builder(Component.literal("Cancel"), (button) -> {
            minecraft.setScreen(new GameControllerScreen(Component.literal("Game Controller")));
        }).bounds(guiLeft + 100, guiTop + 110, 60, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        guiGraphics.blit(TEXTURE, guiLeft, guiTop, 0, 0, xSize, ySize);

        guiGraphics.drawCenteredString(font, this.title, guiLeft + xSize / 2, guiTop + 10, 0xFFFFFF);
        guiGraphics.drawString(font, "Game Time (min):", guiLeft + 20, guiTop + 55, 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        timeEditBox.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    public void renderBackground(GuiGraphics guiGraphics) {
        super.renderBackground(guiGraphics);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
