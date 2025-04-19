package com.seishironagi.craftmine.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.seishironagi.craftmine.CraftMine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class RecipeDisplayOverlay {
    private static final ResourceLocation CRAFTING_TABLE_TEXTURE = new ResourceLocation(
            "textures/gui/container/crafting_table.png");
    private static final ResourceLocation FURNACE_TEXTURE = new ResourceLocation("textures/gui/container/furnace.png");

    private static boolean showRecipe = false;
    private static long hideTime = 0;
    private static final int DISPLAY_DURATION = 5000; // 5 seconds

    // Mob drop data - mapping common items to mobs that drop them
    private static final Map<String, List<String>> MOB_DROPS = new HashMap<>();

    static {
        // Initialize mob drop data
        addMobDrop("minecraft:string", "Spider", "Cave Spider");
        addMobDrop("minecraft:bone", "Skeleton", "Wither Skeleton", "Skeleton Horse");
        addMobDrop("minecraft:rotten_flesh", "Zombie", "Drowned", "Husk", "Zombie Villager");
        addMobDrop("minecraft:ender_pearl", "Enderman", "Endermite");
        addMobDrop("minecraft:gunpowder", "Creeper", "Ghast", "Witch");
        addMobDrop("minecraft:blaze_rod", "Blaze");
        addMobDrop("minecraft:leather", "Cow", "Horse", "Llama", "Donkey", "Mule");
        addMobDrop("minecraft:feather", "Chicken");
        addMobDrop("minecraft:egg", "Chicken");
        addMobDrop("minecraft:porkchop", "Pig");
        addMobDrop("minecraft:beef", "Cow");
        addMobDrop("minecraft:mutton", "Sheep");
        addMobDrop("minecraft:chicken", "Chicken");
        addMobDrop("minecraft:rabbit", "Rabbit");
        addMobDrop("minecraft:slime_ball", "Slime");
        addMobDrop("minecraft:spider_eye", "Spider", "Cave Spider");
    }

    private static void addMobDrop(String itemId, String... mobs) {
        List<String> mobList = new ArrayList<>();
        for (String mob : mobs) {
            mobList.add(mob);
        }
        MOB_DROPS.put(itemId, mobList);
    }

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

        RecipeManager recipeManager = minecraft.level.getRecipeManager();
        String itemId = ForgeRegistries.ITEMS.getKey(targetItem.getItem()).toString();

        // Try to find a crafting recipe
        Optional<ShapedRecipe> craftingRecipe = recipeManager.getAllRecipesFor(RecipeType.CRAFTING).stream()
                .filter(recipe -> recipe instanceof ShapedRecipe)
                .map(recipe -> (ShapedRecipe) recipe)
                .filter(recipe -> ItemStack.isSameItem(recipe.getResultItem(minecraft.level.registryAccess()),
                        targetItem))
                .findFirst();

        // Try to find a furnace recipe
        Optional<SmeltingRecipe> smeltingRecipe = recipeManager.getAllRecipesFor(RecipeType.SMELTING).stream()
                .filter(recipe -> ItemStack.isSameItem(recipe.getResultItem(minecraft.level.registryAccess()),
                        targetItem))
                .findFirst();

        // Try to find a blast furnace recipe
        Optional<BlastingRecipe> blastingRecipe = recipeManager.getAllRecipesFor(RecipeType.BLASTING).stream()
                .filter(recipe -> ItemStack.isSameItem(recipe.getResultItem(minecraft.level.registryAccess()),
                        targetItem))
                .findFirst();

        // Try to find a smoker recipe
        Optional<SmokingRecipe> smokingRecipe = recipeManager.getAllRecipesFor(RecipeType.SMOKING).stream()
                .filter(recipe -> ItemStack.isSameItem(recipe.getResultItem(minecraft.level.registryAccess()),
                        targetItem))
                .findFirst();

        // Try to find a campfire recipe
        Optional<CampfireCookingRecipe> campfireRecipe = recipeManager.getAllRecipesFor(RecipeType.CAMPFIRE_COOKING)
                .stream()
                .filter(recipe -> ItemStack.isSameItem(recipe.getResultItem(minecraft.level.registryAccess()),
                        targetItem))
                .findFirst();

        // Check for mob drops
        List<String> mobsThatDrop = MOB_DROPS.get(itemId);

        if (craftingRecipe.isPresent()) {
            renderCraftingRecipe(graphics, craftingRecipe.get(), targetItem, width, height);
        } else if (smeltingRecipe.isPresent() || blastingRecipe.isPresent() ||
                smokingRecipe.isPresent() || campfireRecipe.isPresent()) {
            // Render the furnace recipe (we prioritize regular furnace)
            if (smeltingRecipe.isPresent()) {
                renderFurnaceRecipe(graphics, smeltingRecipe.get(), targetItem, "Furnace", width, height);
            } else if (blastingRecipe.isPresent()) {
                renderFurnaceRecipe(graphics, blastingRecipe.get(), targetItem, "Blast Furnace", width, height);
            } else if (smokingRecipe.isPresent()) {
                renderFurnaceRecipe(graphics, smokingRecipe.get(), targetItem, "Smoker", width, height);
            } else {
                renderFurnaceRecipe(graphics, campfireRecipe.get(), targetItem, "Campfire", width, height);
            }
        } else if (mobsThatDrop != null && !mobsThatDrop.isEmpty()) {
            // Render mob drop information
            renderMobDrops(graphics, targetItem, mobsThatDrop, width, height);
        } else {
            // Check if it's likely a mined block
            renderMiningInfo(graphics, targetItem, width, height);
        }
    }

    private static void renderCraftingRecipe(GuiGraphics graphics, ShapedRecipe recipe, ItemStack resultItem, int width,
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

        // Title with difficulty indicator
        graphics.drawCenteredString(Minecraft.getInstance().font,
                "Crafting Recipe", x + recipeWidth / 2, y + 5, 0xFFFFFF);

        // Draw difficulty info
        String difficulty = "Difficulty: " + ClientGameData.getDifficultyName();
        String timeEstimate = "Time: " + ClientGameData.getItemTimeMinutes() + " minutes";

        int difficultyColor;
        switch (ClientGameData.getGameDifficulty()) {
            case ClientGameData.DIFFICULTY_EASY:
                difficultyColor = 0x55FF55; // Green
                break;
            case ClientGameData.DIFFICULTY_HARD:
                difficultyColor = 0xFF5555; // Red
                break;
            default:
                difficultyColor = 0xFFAA00; // Orange/Yellow
        }

        graphics.drawString(Minecraft.getInstance().font, difficulty, x + 10, y + 20, difficultyColor);
        graphics.drawString(Minecraft.getInstance().font, timeEstimate, x + 100, y + 20, 0xFFFFFF);

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

    private static <T extends Recipe<?>> void renderFurnaceRecipe(GuiGraphics graphics, T recipe, ItemStack resultItem,
            String furnaceType, int width, int height) {
        // Position on left side of screen
        int recipeWidth = 176;
        int recipeHeight = 166;
        int x = 20;
        int y = height / 2 - recipeHeight / 2;

        // Background texture (furnace)
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.9F);
        graphics.blit(FURNACE_TEXTURE, x, y, 0, 0, recipeWidth, recipeHeight);

        // Title
        graphics.drawCenteredString(Minecraft.getInstance().font,
                furnaceType + " Recipe", x + recipeWidth / 2, y + 10, 0xFFFFFF);

        // After title, add difficulty indicator
        String difficulty = "Difficulty: " + ClientGameData.getDifficultyName();
        String timeEstimate = "Time: " + ClientGameData.getItemTimeMinutes() + " minutes";

        int difficultyColor;
        switch (ClientGameData.getGameDifficulty()) {
            case ClientGameData.DIFFICULTY_EASY:
                difficultyColor = 0x55FF55; // Green
                break;
            case ClientGameData.DIFFICULTY_HARD:
                difficultyColor = 0xFF5555; // Red
                break;
            default:
                difficultyColor = 0xFFAA00; // Orange/Yellow
        }

        graphics.drawString(Minecraft.getInstance().font, difficulty, x + 10, y + 25, difficultyColor);
        graphics.drawString(Minecraft.getInstance().font, timeEstimate, x + 100, y + 25, 0xFFFFFF);

        // Get the input item
        ItemStack inputStack = ItemStack.EMPTY;
        try {
            net.minecraft.world.item.crafting.Ingredient ingredient = recipe.getIngredients().get(0);
            if (ingredient.getItems().length > 0) {
                inputStack = ingredient.getItems()[0];
            }
        } catch (Exception e) {
            CraftMine.LOGGER.error("Error getting furnace ingredient: " + e.getMessage());
        }

        // Input position
        int inputX = x + 56;
        int inputY = y + 36;

        if (!inputStack.isEmpty()) {
            graphics.renderItem(inputStack, inputX, inputY);
            graphics.renderItemDecorations(Minecraft.getInstance().font, inputStack, inputX, inputY, null);
        }

        // Draw fuel
        ItemStack fuelStack = new ItemStack(Items.COAL);
        int fuelX = x + 56;
        int fuelY = y + 76;
        graphics.renderItem(fuelStack, fuelX, fuelY);

        // Result position
        int resultX = x + 116;
        int resultY = y + 54;
        graphics.renderItem(resultItem, resultX, resultY);
        graphics.renderItemDecorations(Minecraft.getInstance().font, resultItem, resultX, resultY, null);

        // Draw fire and arrow
        int fireX = x + 56;
        int fireY = y + 54;
        graphics.fill(fireX, fireY, fireX + 14, fireY + 14, 0xFFFF6600);

        int arrowX = x + 80;
        int arrowY = y + 54;
        graphics.fill(arrowX, arrowY + 4, arrowX + 24, arrowY + 6, 0xFFFFFFFF);
        graphics.fill(arrowX + 20, arrowY + 2, arrowX + 24, arrowY + 8, 0xFFFFFFFF);

        // Draw cooking time - fix for getCookingTime() method
        int cookingTime = 200; // Default cooking time
        if (recipe instanceof AbstractCookingRecipe) {
            cookingTime = ((AbstractCookingRecipe) recipe).getCookingTime();
        } else if (recipe instanceof SmeltingRecipe) {
            cookingTime = ((SmeltingRecipe) recipe).getCookingTime();
        } else if (recipe instanceof BlastingRecipe) {
            cookingTime = ((BlastingRecipe) recipe).getCookingTime();
        } else if (recipe instanceof SmokingRecipe) {
            cookingTime = ((SmokingRecipe) recipe).getCookingTime();
        } else if (recipe instanceof CampfireCookingRecipe) {
            cookingTime = ((CampfireCookingRecipe) recipe).getCookingTime();
        }

        String cookTime = "Cook time: " + cookingTime + " ticks";
        graphics.drawString(Minecraft.getInstance().font, cookTime, x + 50, y + 100, 0xFFAAAAAA);
    }

    private static void renderMobDrops(GuiGraphics graphics, ItemStack targetItem, List<String> mobs, int width,
            int height) {
        // Position on left side of screen
        int panelWidth = 200;
        int panelHeight = 150;
        int x = 20;
        int y = height / 2 - panelHeight / 2;

        // Draw background panel
        graphics.fill(x, y, x + panelWidth, y + panelHeight, 0xAA000000);
        graphics.fill(x, y, x + panelWidth, y + 1, 0xFFFFFFFF);
        graphics.fill(x, y, x + 1, y + panelHeight, 0xFFFFFFFF);
        graphics.fill(x + panelWidth - 1, y, x + panelWidth, y + panelHeight, 0xFFFFFFFF);
        graphics.fill(x, y + panelHeight - 1, x + panelWidth, y + panelHeight, 0xFFFFFFFF);

        // Title
        String title = "Mob Drop: " + targetItem.getHoverName().getString();
        graphics.drawCenteredString(Minecraft.getInstance().font, title, x + panelWidth / 2, y + 10, 0xFFFFFF);

        // Add after title
        String difficulty = "Difficulty: " + ClientGameData.getDifficultyName();
        String timeEstimate = "Time: " + ClientGameData.getItemTimeMinutes() + " minutes";

        int difficultyColor;
        switch (ClientGameData.getGameDifficulty()) {
            case ClientGameData.DIFFICULTY_EASY:
                difficultyColor = 0x55FF55;
                break;
            case ClientGameData.DIFFICULTY_HARD:
                difficultyColor = 0xFF5555;
                break;
            default:
                difficultyColor = 0xFFAA00;
        }

        graphics.drawString(Minecraft.getInstance().font, difficulty, x + 20, y + 25, difficultyColor);
        graphics.drawString(Minecraft.getInstance().font, timeEstimate, x + panelWidth - 120, y + 25, 0xFFFFFF);

        // Draw target item
        int itemX = x + panelWidth / 2 - 8;
        int itemY = y + 30;
        graphics.renderItem(targetItem, itemX, itemY);

        // Draw mob list information
        String mobLabel = "Dropped by:";
        graphics.drawString(Minecraft.getInstance().font, mobLabel, x + 20, y + 50, 0xFFFFAA);

        for (int i = 0; i < mobs.size(); i++) {
            String mob = mobs.get(i);
            graphics.drawString(Minecraft.getInstance().font, "• " + mob, x + 30, y + 65 + i * 15, 0xFFFFFF);
        }

        // Draw sword icon to indicate combat
        ItemStack swordItem = new ItemStack(Items.DIAMOND_SWORD);
        graphics.renderItem(swordItem, x + panelWidth - 30, y + 30);
    }

    private static void renderMiningInfo(GuiGraphics graphics, ItemStack targetItem, int width, int height) {
        // Position on left side of screen
        int panelWidth = 200;
        int panelHeight = 150;
        int x = 20;
        int y = height / 2 - panelHeight / 2;

        // Draw background panel
        graphics.fill(x, y, x + panelWidth, y + panelHeight, 0xAA000000);
        graphics.fill(x, y, x + panelWidth, y + 1, 0xFFFFFFFF);
        graphics.fill(x, y, x + 1, y + panelHeight, 0xFFFFFFFF);
        graphics.fill(x + panelWidth - 1, y, x + panelWidth, y + panelHeight, 0xFFFFFFFF);
        graphics.fill(x, y + panelHeight - 1, x + panelWidth, y + panelHeight, 0xFFFFFFFF);

        // Title
        String title = "How to Get: " + targetItem.getHoverName().getString();
        graphics.drawCenteredString(Minecraft.getInstance().font, title, x + panelWidth / 2, y + 10, 0xFFFFFF);

        // Add after title
        String difficulty = "Difficulty: " + ClientGameData.getDifficultyName();
        String timeEstimate = "Time: " + ClientGameData.getItemTimeMinutes() + " minutes";

        int difficultyColor;
        switch (ClientGameData.getGameDifficulty()) {
            case ClientGameData.DIFFICULTY_EASY:
                difficultyColor = 0x55FF55;
                break;
            case ClientGameData.DIFFICULTY_HARD:
                difficultyColor = 0xFF5555;
                break;
            default:
                difficultyColor = 0xFFAA00;
        }

        graphics.drawString(Minecraft.getInstance().font, difficulty, x + 20, y + 25, difficultyColor);
        graphics.drawString(Minecraft.getInstance().font, timeEstimate, x + panelWidth - 120, y + 25, 0xFFFFFF);

        // Draw target item
        int itemX = x + panelWidth / 2 - 8;
        int itemY = y + 30;
        graphics.renderItem(targetItem, itemX, itemY);

        // Determine appropriate tool type
        String toolType = determineToolType(targetItem);
        String locationHint = determineLocationHint(targetItem);

        // Draw mining information
        graphics.drawString(Minecraft.getInstance().font, "Obtain by mining", x + 20, y + 60, 0xFFFFAA);

        if (!toolType.isEmpty()) {
            graphics.drawString(Minecraft.getInstance().font, "Tool needed: " + toolType, x + 30, y + 75, 0xFFFFFF);
        } else {
            graphics.drawString(Minecraft.getInstance().font, "Can be mined with any tool", x + 30, y + 75, 0xFFFFFF);
        }

        if (!locationHint.isEmpty()) {
            graphics.drawString(Minecraft.getInstance().font, "Location: " + locationHint, x + 30, y + 90, 0xFFFFFF);
        }

        // Draw appropriate tool
        ItemStack toolItem = getToolItemForType(toolType);
        if (!toolItem.isEmpty()) {
            graphics.renderItem(toolItem, x + panelWidth - 30, y + 70);
        }

        // Provide general tip at bottom
        String tip = "Explore the world to find this resource!";
        graphics.drawCenteredString(Minecraft.getInstance().font, tip, x + panelWidth / 2, y + panelHeight - 25,
                0xAAAAFF);
    }

    private static String determineToolType(ItemStack stack) {
        String itemId = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();

        // Pickaxe items
        if (itemId.contains("_ore") || itemId.contains("stone") || itemId.contains("obsidian") ||
                itemId.contains("rock") || itemId.contains("brick") || itemId.contains("iron") ||
                itemId.contains("gold") || itemId.contains("diamond") || itemId.contains("emerald") ||
                itemId.contains("quartz") || itemId.contains("coal") || itemId.endsWith("andesite") ||
                itemId.contains("diorite") || itemId.contains("granite") || itemId.contains("deepslate") ||
                itemId.contains("copper") || itemId.contains("amethyst")) {
            return "Pickaxe";
        }

        // Axe items
        if (itemId.contains("_log") || itemId.contains("_wood") || itemId.contains("plank") ||
                itemId.contains("bamboo") || itemId.contains("stem")) {
            return "Axe";
        }

        // Shovel items
        if (itemId.contains("dirt") || itemId.contains("grass") || itemId.contains("sand") ||
                itemId.contains("gravel") || itemId.contains("clay") || itemId.contains("mud") ||
                itemId.contains("soul_sand") || itemId.contains("soul_soil")) {
            return "Shovel";
        }

        // Hoe items
        if (itemId.contains("leaves") || itemId.contains("hay") || itemId.contains("moss") ||
                itemId.contains("target") || itemId.contains("sponge")) {
            return "Hoe";
        }

        return "";
    }

    private static String determineLocationHint(ItemStack stack) {
        String itemId = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();

        if (itemId.contains("diamond") || itemId.contains("_ore") || itemId.contains("redstone") ||
                itemId.contains("lapis") || itemId.contains("gold") || itemId.contains("emerald")) {
            return "Underground, lower depths";
        }

        if (itemId.contains("iron") || itemId.contains("coal")) {
            return "Underground, most depths";
        }

        if (itemId.contains("stone") || itemId.contains("granite") || itemId.contains("andesite") ||
                itemId.contains("diorite")) {
            return "Underground, all depths";
        }

        if (itemId.contains("sand") || itemId.contains("gravel")) {
            return "Beaches, riverbeds, underground";
        }

        if (itemId.contains("dirt") || itemId.contains("grass")) {
            return "Surface world";
        }

        if (itemId.contains("log") || itemId.contains("wood") || itemId.contains("leaves")) {
            return "Forests";
        }

        if (itemId.contains("clay")) {
            return "Near water, riverbeds";
        }

        return "";
    }

    private static ItemStack getToolItemForType(String toolType) {
        switch (toolType) {
            case "Pickaxe":
                return new ItemStack(Items.DIAMOND_PICKAXE);
            case "Axe":
                return new ItemStack(Items.DIAMOND_AXE);
            case "Shovel":
                return new ItemStack(Items.DIAMOND_SHOVEL);
            case "Hoe":
                return new ItemStack(Items.DIAMOND_HOE);
            default:
                return ItemStack.EMPTY;
        }
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
        String noRecipe = "No information available";
        graphics.drawCenteredString(Minecraft.getInstance().font, noRecipe,
                x + panelWidth / 2, y + panelHeight - 15, 0xFFAAAA);
    }
}
