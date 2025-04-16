package com.seishironagi.craftmine.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.seishironagi.craftmine.CraftMine;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GameInfoScreen extends Screen {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CraftMine.MODID, "textures/gui/double_chest.png");
    private int guiLeft, guiTop;
    private final int xSize = 176;
    private final int ySize = 222; // Double chest height

    public GameInfoScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();

        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        // Back button - positioned at bottom of chest
        this.addRenderableWidget(new Button.Builder(Component.literal("Back"), (button) -> {
            minecraft.setScreen(new GameControllerScreen(Component.literal("Game Controller")));
        }).bounds(guiLeft + 48, guiTop + 200, 80, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // Draw the double chest background
        guiGraphics.blit(TEXTURE, guiLeft, guiTop, 0, 0, xSize, ySize);

        // Draw title centered at the top of the chest
        guiGraphics.drawCenteredString(font, this.title, guiLeft + xSize / 2, guiTop + 8, 0x404040);

        // Draw info text with chest-style positioning
        int textY = guiTop + 30;
        guiGraphics.drawString(font, "How to play:", guiLeft + 28, textY, 0x404040);
        textY += 18; // Increased spacing for chest-like appearance
        guiGraphics.drawString(font, "1. Choose your team", guiLeft + 28, textY, 0x404040);
        textY += 18;
        guiGraphics.drawString(font, "2. Red team: Find the target item", guiLeft + 28, textY, 0x404040);
        textY += 18;
        guiGraphics.drawString(font, "3. Blue team: Stop red team", guiLeft + 28, textY, 0x404040);
        textY += 18;
        guiGraphics.drawString(font, "4. Red team wins if they find", guiLeft + 28, textY, 0x404040);
        textY += 18;
        guiGraphics.drawString(font, "   the item before time runs out", guiLeft + 28, textY, 0x404040);
        textY += 18;
        guiGraphics.drawString(font, "Game time can be set in settings", guiLeft + 28, textY, 0x404040);

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
