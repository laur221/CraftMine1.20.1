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
    private static final ResourceLocation TIMER_TEXTURE = ResourceLocation
            .tryParse(CraftMine.MOD_ID + ":textures/gui/timer.png");

    public static final IGuiOverlay HUD_TIMER = ((gui, guiGraphics, partialTick, width, height) -> {
        if (!ClientGameData.isGameRunning()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui) {
            return;
        }

        // Calculate time values for color effect
        int totalSeconds = ClientGameData.getRemainingSeconds();
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        String timeText = String.format("%02d:%02d", minutes, seconds);

        // Base position
        int x = width - 80;
        int y = 10;
        int timeWidth = 70;
        int timeHeight = 20;

        // Draw a stylish background with gradient based on remaining time
        int colorStart, colorEnd;
        if (totalSeconds < 30) {
            // Red alert for last 30 seconds
            colorStart = 0xAAFF0000;
            colorEnd = 0xAA990000;
        } else if (totalSeconds < 60) {
            // Orange warning for last minute
            colorStart = 0xAAFF7700;
            colorEnd = 0xAA994400;
        } else {
            // Normal blue for regular time
            colorStart = 0xAA0077FF;
            colorEnd = 0xAA004499;
        }

        // Draw time panel with pulsing effect
        float pulse = (float) Math.sin(System.currentTimeMillis() / 200.0) * 0.1f + 0.9f;
        int pulseAlpha = (int) (pulse * 255) & 0xFF;
        int pulseColor = (pulseAlpha << 24) | (colorStart & 0x00FFFFFF);

        // Draw main panel
        guiGraphics.fillGradient(x, y, x + timeWidth, y + timeHeight, colorStart, colorEnd);

        // Draw border
        int borderSize = 1;
        guiGraphics.fill(x, y, x + timeWidth, y + borderSize, pulseColor);
        guiGraphics.fill(x, y, x + borderSize, y + timeHeight, pulseColor);
        guiGraphics.fill(x + timeWidth - borderSize, y, x + timeWidth, y + timeHeight, pulseColor);
        guiGraphics.fill(x, y + timeHeight - borderSize, x + timeWidth, y + timeHeight, pulseColor);

        // Draw time text with glow effect
        guiGraphics.drawCenteredString(minecraft.font, "TIME", x + timeWidth / 2, y + 2, 0xFFFFFF);
        guiGraphics.drawCenteredString(minecraft.font, timeText, x + timeWidth / 2, y + 11, 0xFFFFFF);
    });
}
