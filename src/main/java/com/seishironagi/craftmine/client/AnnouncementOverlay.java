package com.seishironagi.craftmine.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

@OnlyIn(Dist.CLIENT)
public class AnnouncementOverlay {
    private static String announcement = "";
    private static long announcementEndTime = 0;
    private static int announcementColor = 0xFFFFFF;
    private static boolean showWinMessage = false;
    private static boolean redTeamWon = false;

    public static final IGuiOverlay HUD_ANNOUNCEMENT = ((gui, graphics, partialTick, width, height) -> {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui) {
            return;
        }

        // Render active announcement (if any)
        if (System.currentTimeMillis() < announcementEndTime && !announcement.isEmpty()) {
            renderAnnouncement(graphics, width, announcement, announcementColor);
        }

        // Render win message if game just ended
        if (showWinMessage) {
            String message = redTeamWon ? "§c§lRed Team Won!" : "§9§lBlue Team Won!";
            int color = redTeamWon ? 0xFF3333 : 0x3333FF;
            renderWinMessage(graphics, width, message, color);
        }
    });

    /**
     * Display an announcement at the top of the screen for a specified duration
     */
    public static void showAnnouncement(String message, int color, int durationMs) {
        announcement = message;
        announcementColor = color;
        announcementEndTime = System.currentTimeMillis() + durationMs;
    }

    /**
     * Show the game result message
     */
    public static void showGameResult(boolean redWon) {
        showWinMessage = true;
        redTeamWon = redWon;

        // Hide win message after 5 seconds
        new Thread(() -> {
            try {
                Thread.sleep(500);
                showWinMessage = false;
            } catch (InterruptedException e) {
                // Ignore interruption
            }
        }).start();
    }

    /**
     * Reset all announcements (used when game ends or player logs out)
     */
    public static void reset() {
        announcement = "";
        announcementEndTime = 0;
        showWinMessage = false;
    }

    private static void renderAnnouncement(GuiGraphics graphics, int screenWidth, String message, int color) {
        Minecraft minecraft = Minecraft.getInstance();
        int messageWidth = minecraft.font.width(message);

        // Draw at top center of screen with background
        int x = screenWidth / 2 - messageWidth / 2;
        int y = 10;

        // Background with fade effect
        long timeLeft = announcementEndTime - System.currentTimeMillis();
        float alpha = Math.min(1.0f, timeLeft / 500.0f);

        // Draw background
        int bgColor = 0x80000000;
        if (alpha < 1.0f) {
            bgColor = ((int) (alpha * 128) << 24);
        }

        // Draw text with shadow
        graphics.fill(x - 5, y - 2, x + messageWidth + 5, y + 13, bgColor);
        graphics.drawString(minecraft.font, message, x, y, color);
    }

    private static void renderWinMessage(GuiGraphics graphics, int screenWidth, String message, int color) {
        Minecraft minecraft = Minecraft.getInstance();
        int messageWidth = minecraft.font.width(message);

        // Position in the top center
        int x = screenWidth / 2 - messageWidth / 2;
        int y = 40;

        // Draw a larger background for win message
        graphics.fill(x - 10, y - 5, x + messageWidth + 10, y + 15, 0xC0000000);

        // Draw message with shadow and larger
        graphics.drawString(minecraft.font, message, x, y, color, true);

        // Draw additional decoration
        graphics.fill(x - 10, y - 5, x - 5, y + 15, color);
        graphics.fill(x + messageWidth + 5, y - 5, x + messageWidth + 10, y + 15, color);
    }
}
