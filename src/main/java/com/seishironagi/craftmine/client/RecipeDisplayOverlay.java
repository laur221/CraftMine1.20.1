package com.seishironagi.craftmine.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class RecipeDisplayOverlay {
    private static final ResourceLocation CRAFTING_TABLE_TEXTURE = new ResourceLocation("minecraft",
            "textures/gui/container/crafting_table.png");

    private static boolean showRecipe = false;
    private static long hideTime = 0;
    private static final int DISPLAY_DURATION = 5000; // 5 seconds

    public static final IGuiOverlay HUD_RECIPE = ((gui, graphics, partialTick, width, height) -> {
        if (!shouldRenderRecipe())
            return;

        ItemStack targetItem = ClientGameData.getTargetItem();
        if (targetItem.isEmpty())
            return;

        renderRecipeOverlay(graphics, targetItem, width, height);
    });

    private static boolean shouldRenderRecipe() {
        // Only show if game is running, player is on red team, and display is active
        if (!ClientGameData.isGameRunning() || !ClientGameData.isRedTeam() || !showRecipe)
            return false;

        // Check if we need to auto-hide after duration
        if (System.currentTimeMillis() > hideTime) {
            showRecipe = false;
            return false;
        }

        return true;
    }

    public static void toggleRecipeDisplay() {
        // Only allow red team to toggle recipe
        if (!ClientGameData.isRedTeam() || !ClientGameData.isGameRunning())
            return;

        showRecipe = !showRecipe;
        if (showRecipe) {
            hideTime = System.currentTimeMillis() + DISPLAY_DURATION;
        }
    }

    private static void renderRecipeOverlay(GuiGraphics graphics, ItemStack targetItem, int width, int height) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null)
            return;

        // Find the recipe for the target item
        RecipeManager recipeManager = minecraft.level.getRecipeManager();

        // Get all crafting recipes - fix type inference issues
        List<CraftingRecipe> allRecipes = recipeManager.getAllRecipesFor(RecipeType.CRAFTING);

        // Find a shaped recipe that produces the target item
        Optional<ShapedRecipe> recipeOptional = allRecipes.stream()
                .filter(recipe -> recipe instanceof ShapedRecipe)
                .map(recipe -> (ShapedRecipe) recipe)
                // Fix getResultItem() by providing registry access
                .filter(recipe -> ItemStack.isSameItem(
                        recipe.getResultItem(minecraft.level.registryAccess()),
                        targetItem))
                .findFirst();

        if (recipeOptional.isEmpty()) {
            // No recipe found, just show the item
            renderNoRecipeFound(graphics, targetItem, width, height);
            return;
        }

        // Recipe found, render the crafting table with ingredients
        ShapedRecipe recipe = recipeOptional.get();
        renderRecipe(graphics, recipe, targetItem, width, height);
    }

    private static void renderRecipe(GuiGraphics graphics, ShapedRecipe recipe, ItemStack resultItem, int width,
            int height) {
        // Position on left side of screen
        int recipeWidth = 176;
        int recipeHeight = 166;
        int x = 20;
        int y = height / 2 - recipeHeight / 2;

        // Background texture (crafting table)
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.9F);
        graphics.blit(CRAFTING_TABLE_TEXTURE, x, y, 0, 0, recipeWidth, recipeHeight);

        // Title
        graphics.drawCenteredString(Minecraft.getInstance().font,
                "Recipe for Target Item", x + recipeWidth / 2, y + 5, 0xFFFFFF);

        // Draw grid ingredients
        int gridX = x + 30;
        int gridY = y + 40;
        int slotSize = 18;

        // Get the ingredients
        net.minecraft.core.NonNullList<net.minecraft.world.item.crafting.Ingredient> ingredients = recipe
                .getIngredients();
        int width3x3 = Math.min(3, recipe.getWidth());
        int height3x3 = Math.min(3, recipe.getHeight());

        for (int row = 0; row < height3x3; row++) {
            for (int col = 0; col < width3x3; col++) {
                int index = row * recipe.getWidth() + col;
                if (index < ingredients.size()) {
                    net.minecraft.world.item.crafting.Ingredient ingredient = ingredients.get(index);
                    ItemStack[] matchingStacks = ingredient.getItems();

                    if (matchingStacks.length > 0) {
                        // Use the first matching item for display
                        ItemStack displayStack = matchingStacks[0];
                        int itemX = gridX + col * slotSize;
                        int itemY = gridY + row * slotSize;

                        graphics.renderItem(displayStack, itemX, itemY);
                        graphics.renderItemDecorations(Minecraft.getInstance().font, displayStack, itemX, itemY, null);
                    }
                }
            }
        }

        // Draw result
        int resultX = x + 130;
        int resultY = y + 60;
        graphics.renderItem(resultItem, resultX, resultY);
        graphics.renderItemDecorations(Minecraft.getInstance().font, resultItem, resultX, resultY, null);

        // Draw arrow
        int arrowX = x + 100;
        int arrowY = y + 60;
        graphics.fill(arrowX, arrowY + 3, arrowX + 20, arrowY + 5, 0xFFFFFFFF);
        graphics.fill(arrowX + 15, arrowY, arrowX + 20, arrowY + 8, 0xFFFFFFFF);
    }

    private static void renderNoRecipeFound(GuiGraphics graphics, ItemStack targetItem, int width, int height) {
        // Position on left side of screen
        int panelWidth = 160;
        int panelHeight = 80;
        int x = 20;
        int y = height / 2 - panelHeight / 2;

        // Draw background panel
        graphics.fill(x, y, x + panelWidth, y + panelHeight, 0xAA000000);
        graphics.fill(x, y, x + panelWidth, y + 1, 0xFFFFFFFF);
        graphics.fill(x, y, x + panelWidth, y + 1, 0xFFFFFFFF);
        graphics.fill(x, y, x + 1, y + panelHeight, 0xFFFFFFFF);
        graphics.fill(x + panelWidth - 1, y, x + panelWidth, y + panelHeight, 0xFFFFFFFF);
        graphics.fill(x, y + panelHeight - 1, x + panelWidth, y + panelHeight, 0xFFFFFFFF);

        // Title
        String title = "Target Item";
        graphics.drawCenteredString(Minecraft.getInstance().font, title, x + panelWidth / 2, y + 10, 0xFFFFFF);

        // Draw item
        int itemX = x + panelWidth / 2 - 8;
        int itemY = y + 40;
        graphics.renderItem(targetItem, itemX, itemY);

        // No recipe text
        String noRecipe = "No crafting recipe available";
        graphics.drawCenteredString(Minecraft.getInstance().font, noRecipe,
                x + panelWidth / 2, y + panelHeight - 15, 0xFFAAAA);
    }
}
