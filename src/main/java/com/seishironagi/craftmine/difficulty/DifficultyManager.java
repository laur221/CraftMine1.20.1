package com.seishironagi.craftmine.difficulty;

import com.seishironagi.craftmine.Config;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.*;

/**
 * Manages difficulty settings, item pools and time allocation for the game.
 */
public class DifficultyManager {
    private static DifficultyManager INSTANCE;

    // Difficulty levels
    public static final int DIFFICULTY_EASY = 0;
    public static final int DIFFICULTY_MEDIUM = 1;
    public static final int DIFFICULTY_HARD = 2;

    // Current difficulty level
    private int currentDifficulty = DIFFICULTY_MEDIUM; // Default to medium

    // Item pools
    private final List<Item> easyItemPool = new ArrayList<>();
    private final List<Item> mediumItemPool = new ArrayList<>();
    private final List<Item> hardItemPool = new ArrayList<>();

    // Time ranges per difficulty (in seconds)
    private final int[][] timeRanges = {
            { 120, 180 }, // Easy: 2-3 minutes
            { 240, 300 }, // Medium: 4-5 minutes
            { 360, 480 } // Hard: 6-8 minutes
    };

    private final Random random = new Random();

    private DifficultyManager() {
        initializeItemPools();
    }

    public static DifficultyManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DifficultyManager();
        }
        return INSTANCE;
    }

    /**
     * Initialize the item pools with appropriate items for each difficulty
     */
    private void initializeItemPools() {
        // Easy items - common, simple to obtain
        easyItemPool.addAll(Arrays.asList(
                Items.DIRT, Items.COBBLESTONE, Items.OAK_LOG, Items.BIRCH_LOG,
                Items.STICK, Items.CRAFTING_TABLE, Items.WOODEN_PICKAXE,
                Items.WOODEN_AXE, Items.TORCH, Items.APPLE, Items.COAL,
                Items.WHEAT_SEEDS, Items.SAND, Items.GRAVEL, Items.STONE_BUTTON,
                Items.WOODEN_SHOVEL, Items.WOODEN_HOE, Items.BOWL));

        // Medium items - moderately difficult to obtain
        mediumItemPool.addAll(Arrays.asList(
                Items.STONE_PICKAXE, Items.FURNACE, Items.IRON_INGOT,
                Items.BREAD, Items.BUCKET, Items.FISHING_ROD, Items.LEATHER,
                Items.BOW, Items.ARROW, Items.IRON_SWORD, Items.SHIELD,
                Items.STONE_AXE, Items.LEAD, Items.SHEARS, Items.IRON_NUGGET,
                Items.COOKED_BEEF, Items.COOKED_CHICKEN, Items.COOKED_PORKCHOP));

        // Hard items - rare or complex crafting
        hardItemPool.addAll(Arrays.asList(
                Items.DIAMOND, Items.ANVIL, Items.ENCHANTING_TABLE,
                Items.CLOCK, Items.COMPASS, Items.GOLDEN_APPLE,
                Items.BREWING_STAND, Items.ENDER_PEARL, Items.BLAZE_ROD,
                Items.DIAMOND_PICKAXE, Items.IRON_CHESTPLATE, Items.IRON_BOOTS,
                Items.REDSTONE_LAMP, Items.LAPIS_LAZULI, Items.CAKE,
                Items.PUMPKIN_PIE, Items.CROSSBOW, Items.BEEHIVE));
    }

    /**
     * Sets the current difficulty level
     * 
     * @param difficulty The difficulty level (0=Easy, 1=Medium, 2=Hard)
     */
    public void setDifficulty(int difficulty) {
        if (difficulty >= DIFFICULTY_EASY && difficulty <= DIFFICULTY_HARD) {
            this.currentDifficulty = difficulty;
            Config.gameDifficulty = difficulty; // Sync with Config for persistence
        }
    }

    /**
     * Cycles to the next difficulty level
     * 
     * @return The new difficulty level
     */
    public int cycleDifficulty() {
        currentDifficulty = (currentDifficulty + 1) % 3;
        Config.gameDifficulty = currentDifficulty; // Sync with Config
        return currentDifficulty;
    }

    /**
     * Gets the current difficulty level
     * 
     * @return The current difficulty (0=Easy, 1=Medium, 2=Hard)
     */
    public int getCurrentDifficulty() {
        return currentDifficulty;
    }

    /**
     * Gets the current difficulty name
     * 
     * @return String representation of the current difficulty
     */
    public String getDifficultyName() {
        return switch (currentDifficulty) {
            case DIFFICULTY_EASY -> "Easy";
            case DIFFICULTY_HARD -> "Hard";
            default -> "Medium";
        };
    }

    /**
     * Selects a random item based on the current difficulty
     * 
     * @return A randomly selected item from the appropriate pool
     */
    public Item selectRandomItem() {
        List<Item> selectedPool;

        // Select the pool based on current difficulty
        selectedPool = switch (currentDifficulty) {
            case DIFFICULTY_EASY -> easyItemPool;
            case DIFFICULTY_HARD -> hardItemPool;
            default -> mediumItemPool;
        };

        // Select a random item from the pool
        if (selectedPool.isEmpty()) {
            // Fallback to a stone if somehow the pool is empty
            return Items.STONE;
        }

        return selectedPool.get(random.nextInt(selectedPool.size()));
    }

    /**
     * Calculates the time allocation in seconds based on current difficulty
     * 
     * @return Time in seconds for the current difficulty
     */
    public int getTimeAllocation() {
        int[] range = timeRanges[currentDifficulty];
        // Random time within the defined range for the difficulty
        return range[0] + random.nextInt(range[1] - range[0] + 1);
    }

    /**
     * Gets a formatted string with the current difficulty settings
     * 
     * @param item        The selected target item
     * @param timeSeconds The allocated time in seconds
     * @return A formatted string describing the difficulty settings
     */
    public String getSettingsDescription(Item item, int timeSeconds) {
        int minutes = timeSeconds / 60;
        int seconds = timeSeconds % 60;
        String itemName = item.getDescription().getString();

        return String.format("Difficulty: %s — Your target item is: %s (Time: %d:%02d)",
                getDifficultyName(), itemName, minutes, seconds);
    }
}
