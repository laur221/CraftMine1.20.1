package com.seishironagi.craftmine.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

@OnlyIn(Dist.CLIENT)
public class TargetItemOverlay {

    public static final IGuiOverlay HUD_TARGET_ITEM = ((gui, graphics, partialTick, width, height) -> {
        if (!ClientGameData.isGameRunning() || !ClientGameData.isRedTeam() ||
                ClientGameData.getTargetItem() == null || ClientGameData.getTargetItem().isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui) {
            return;
        }

        // Get target item
        ItemStack targetItem = ClientGameData.getTargetItem();

        // Position next to hotbar for better visibility underground
        int x = width / 2 + 90; // Right side of hotbar
        int y = height - 19; // Aligned with hotbar

        // Draw target item with enhanced visibility for underground environments
        renderTargetItem(graphics, targetItem, x, y);
    });

    private static void renderTargetItem(GuiGraphics graphics, ItemStack stack, int x, int y) {
        Minecraft minecraft = Minecraft.getInstance();

        // Draw a glowing background to make the item stand out in dark environments
        int bgSize = 18;
        int padding = 2;

        // Draw pulsing background for visibility
        float pulse = (float) Math.sin(System.currentTimeMillis() / 300.0) * 0.2f + 0.8f;
        int alpha = (int) (pulse * 200) & 0xFF;

        // Use a bright yellow glow that's visible in caves
        int glowColor = (alpha << 24) | 0xFFFF00;

        // Draw glow background
        graphics.fill(x - padding, y - padding,
                x + bgSize + padding, y + bgSize + padding,
                glowColor);

        // Draw the item
        graphics.renderItem(stack, x, y);

        // Draw a mini "TARGET" indicator
        String label = "TARGET";
        int textX = x + bgSize / 2 - minecraft.font.width(label) / 2;
        int textY = y - 10;

        // Background behind text for better visibility in dark places
        int textWidth = minecraft.font.width(label);
        graphics.fill(textX - 2, textY - 1, textX + textWidth + 2, textY + 9, 0x90000000);

        // Draw text
        graphics.drawString(minecraft.font, label, textX, textY, 0xFFFF55, true);
    }
}
