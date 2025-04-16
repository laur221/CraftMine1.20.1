package com.seishironagi.craftmine.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.seishironagi.craftmine.CraftMine;
import com.seishironagi.craftmine.Config;
import com.seishironagi.craftmine.network.ModMessages;
import com.seishironagi.craftmine.network.packet.JoinTeamC2SPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TeamSelectionScreen extends Screen {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CraftMine.MODID, "textures/gui/team_selection.png");
    private int guiLeft, guiTop;
    private final int xSize = 176;
    private final int ySize = 166;

    public TeamSelectionScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();

        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        // Red Team button
        this.addRenderableWidget(new Button.Builder(Component.literal(Config.redTeamName), (button) -> {
            ModMessages.sendToServer(new JoinTeamC2SPacket(true));
            this.onClose();
        }).bounds(guiLeft + 20, guiTop + 50, 140, 20).build());

        // Blue Team button
        this.addRenderableWidget(new Button.Builder(Component.literal(Config.blueTeamName), (button) -> {
            ModMessages.sendToServer(new JoinTeamC2SPacket(false));
            this.onClose();
        }).bounds(guiLeft + 20, guiTop + 80, 140, 20).build());

        // Back button
        this.addRenderableWidget(new Button.Builder(Component.literal("Back"), (button) -> {
            minecraft.setScreen(new GameControllerScreen(Component.literal("Game Controller")));
        }).bounds(guiLeft + 20, guiTop + 130, 140, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        guiGraphics.blit(TEXTURE, guiLeft, guiTop, 0, 0, xSize, ySize);

        guiGraphics.drawCenteredString(font, this.title, guiLeft + xSize / 2, guiTop + 10, 0xFFFFFF);
        guiGraphics.drawCenteredString(font, "Select your team:", guiLeft + xSize / 2, guiTop + 30, 0xFFFFFF);

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
