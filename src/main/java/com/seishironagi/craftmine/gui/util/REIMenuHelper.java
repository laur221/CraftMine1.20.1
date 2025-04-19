package com.seishironagi.craftmine.gui.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class REIMenuHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger("CraftMine-REI");
    private static final ResourceLocation CHEST_TEXTURE = ResourceLocation.tryParse("minecraft:textures/gui/container/generic_54.png");
    
    public static void renderSixRowChest(GuiGraphics guiGraphics, int leftPos, int topPos, int width, int height) {
        renderVanillaChest(guiGraphics, leftPos, topPos, width, height);
    }
    
    private static void renderVanillaChest(GuiGraphics guiGraphics, int leftPos, int topPos, int width, int height) {
        guiGraphics.blit(CHEST_TEXTURE, leftPos, topPos, 0, 0, width, 6 * 18 + 17);
        guiGraphics.blit(CHEST_TEXTURE, leftPos, topPos + 6 * 18 + 17, 0, 126, width, 96);
    }
}
