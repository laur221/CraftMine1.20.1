package com.seishironagi.craftmine.gui.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@OnlyIn(Dist.CLIENT)
public class REIHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger("CraftMine-UI");
    private static final ResourceLocation CHEST_TEXTURE = ResourceLocation.tryParse("minecraft:textures/gui/container/generic_54.png");

    /**
     * Renders a six-row chest background using vanilla rendering
     */
    public static void renderSixRowChestBackground(GuiGraphics guiGraphics, int leftPos, int topPos, int width, int height) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        // Draw chest background for six rows
        guiGraphics.blit(CHEST_TEXTURE, leftPos, topPos, 0, 0, width, 6 * 18 + 17);
        guiGraphics.blit(CHEST_TEXTURE, leftPos, topPos + 6 * 18 + 17, 0, 126, width, 96);
    }
}
