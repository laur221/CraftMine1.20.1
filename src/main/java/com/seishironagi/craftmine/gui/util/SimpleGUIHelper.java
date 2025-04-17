package com.seishironagi.craftmine.gui.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SimpleGUIHelper {
    private static final ResourceLocation CHEST_TEXTURE = ResourceLocation.tryParse("minecraft:textures/gui/container/generic_54.png");
    private static final ResourceLocation WIDGETS = ResourceLocation.tryParse("minecraft:textures/gui/widgets.png");

    /**
     * Randează fundalul unui container de tip cufăr cu 6 rânduri
     */
    public static void renderSixRowChestBackground(GuiGraphics guiGraphics, int leftPos, int topPos, int width, int height) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        guiGraphics.blit(CHEST_TEXTURE, leftPos, topPos, 0, 0, width, 6 * 18 + 17);
        guiGraphics.blit(CHEST_TEXTURE, leftPos, topPos + 6 * 18 + 17, 0, 126, width, 96);
    }

    /**
     * Randează un buton în stilul Minecraft
     */
    public static void renderButton(GuiGraphics guiGraphics, int x, int y, int width, int height, boolean hovered) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        int textureY = 46;
        if (hovered) {
            textureY = 66;
        }
        
        // Colțul din stânga sus
        guiGraphics.blit(WIDGETS, x, y, 0, textureY, 4, 4);
        // Partea de sus
        guiGraphics.blit(WIDGETS, x + 4, y, 4, textureY, width - 8, 4);
        // Colțul din dreapta sus
        guiGraphics.blit(WIDGETS, x + width - 4, y, 196, textureY, 4, 4);
        
        // Partea din stânga
        guiGraphics.blit(WIDGETS, x, y + 4, 0, textureY + 4, 4, height - 8);
        // Centru
        guiGraphics.blit(WIDGETS, x + 4, y + 4, 4, textureY + 4, width - 8, height - 8);
        // Partea din dreapta
        guiGraphics.blit(WIDGETS, x + width - 4, y + 4, 196, textureY + 4, 4, height - 8);
        
        // Colțul din stânga jos
        guiGraphics.blit(WIDGETS, x, y + height - 4, 0, textureY + 16 - 4, 4, 4);
        // Partea de jos
        guiGraphics.blit(WIDGETS, x + 4, y + height - 4, 4, textureY + 16 - 4, width - 8, 4);
        // Colțul din dreapta jos
        guiGraphics.blit(WIDGETS, x + width - 4, y + height - 4, 196, textureY + 16 - 4, 4, 4);
    }
    
    /**
     * Desenează un obiect cu efectul flotant
     */
    public static void renderFloatingItem(GuiGraphics guiGraphics, ItemStack stack, int x, int y) {
        RenderSystem.enableDepthTest();
        guiGraphics.renderFakeItem(stack, x, y);
        guiGraphics.renderItemDecorations(net.minecraft.client.Minecraft.getInstance().font, stack, x, y, "");
    }
}
