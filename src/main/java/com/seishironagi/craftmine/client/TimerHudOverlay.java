package com.seishironagi.craftmine.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.seishironagi.craftmine.CraftMine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

@OnlyIn(Dist.CLIENT)
public class TimerHudOverlay {
    private static final ResourceLocation TIMER_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CraftMine.MODID, "textures/gui/timer.png");

    public static final IGuiOverlay HUD_TIMER = ((gui, guiGraphics, partialTick, width, height) -> {
        if (!ClientGameData.isGameRunning()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui) {
            return;
        }

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = width - 80;
        int y = 10;

        guiGraphics.blit(TIMER_TEXTURE, x, y, 0, 0, 70, 20, 70, 20);

        String timeText = ClientGameData.getFormattedTime();
        guiGraphics.drawCenteredString(minecraft.font, timeText, x + 35, y + 6, 0xFFFFFF);
    });
}
