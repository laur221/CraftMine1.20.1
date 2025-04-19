package com.seishironagi.craftmine.gui.util;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Helper class for rendering consistent GUI elements across different screens
 */
public class GuiTheme {
    // Common theme colors
    public static final int PRIMARY_COLOR = 0xFF3366AA;
    public static final int SECONDARY_COLOR = 0xFF33AA66;
    public static final int ACCENT_COLOR = 0xFFEECC44;
    public static final int TEXT_COLOR = 0xFFEEEEEE;
    public static final int BACKGROUND_START = 0x90102030;
    public static final int BACKGROUND_END = 0x90203040;
    
    // Rendering dimensions
    public static final int BUTTON_HEIGHT = 25;
    public static final int BUTTON_WIDTH = 140;
    public static final int BUTTON_SPACING = 8;
    
    /**
     * Renders a standard panel with animated border
     */
    public static void renderPanel(GuiGraphics graphics, int x, int y, int width, int height, int startColor, int endColor) {
        // Main panel gradient
        graphics.fillGradient(x, y, x + width, y + height, startColor, endColor);
        
        // Animated border
        int borderSize = 2;
        int time = (int)(System.currentTimeMillis() / 100 % 60);
        int borderColor = PRIMARY_COLOR + (time * 4 << 16);
        
        // Draw the borders
        graphics.fill(x, y, x + width, y + borderSize, borderColor);
        graphics.fill(x, y, x + borderSize, y + height, borderColor);
        graphics.fill(x + width - borderSize, y, x + width, y + height, borderColor);
        graphics.fill(x, y + height - borderSize, x + width, y + height, borderColor);
        
        // Corner decoration
        int cornerSize = 6;
        graphics.fill(x, y, x + cornerSize, y + cornerSize, ACCENT_COLOR);
        graphics.fill(x + width - cornerSize, y, x + width, y + cornerSize, ACCENT_COLOR);
        graphics.fill(x, y + height - cornerSize, x + cornerSize, y + height, ACCENT_COLOR);
        graphics.fill(x + width - cornerSize, y + height - cornerSize, x + width, y + height, ACCENT_COLOR);
    }
    
    /**
     * Renders a themed heading
     */
    public static void renderHeading(GuiGraphics graphics, String title, String subtitle, int centerX, int y) {
        // Draw main title (centered)
        graphics.drawCenteredString(
            net.minecraft.client.Minecraft.getInstance().font, 
            net.minecraft.network.chat.Component.literal("§l" + title), 
            centerX, y, ACCENT_COLOR
        );
        
        // Draw subtitle if provided
        if (subtitle != null && !subtitle.isEmpty()) {
            graphics.drawCenteredString(
                net.minecraft.client.Minecraft.getInstance().font,
                net.minecraft.network.chat.Component.literal("§7" + subtitle),
                centerX, y + 14, TEXT_COLOR
            );
        }
    }
}
