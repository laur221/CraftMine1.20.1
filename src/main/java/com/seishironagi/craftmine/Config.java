package com.seishironagi.craftmine;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

@Mod.EventBusSubscriber(modid = CraftMine.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    public static final ForgeConfigSpec COMMON_SPEC;
    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();

    // Config values with default values
    public static int defaultGameTime = 10;
    public static boolean useItemSpecificTimes = true;
    public static int redTeamColor = 0xFF0000;
    public static int blueTeamColor = 0x0000FF;
    public static String redTeamName = "Red Team";
    public static String blueTeamName = "Blue Team";
    public static String gameStartMessage = "The game has started!";
    public static String redTeamTaskMessage = "Find %s within %d minutes!";
    public static String blueTeamTaskMessage = "Prevent the Red Team from finding their target!";
    public static String redTeamWinMessage = "The Red Team has won!";
    public static String blueTeamWinMessage = "The Blue Team has won!";

    public static List<Item> gameItems = new ArrayList<>();
    public static Map<Item, Integer> itemTimes = new HashMap<>();

    // Game difficulty settings
    public static final int DIFFICULTY_EASY = 0;
    public static final int DIFFICULTY_MEDIUM = 1;
    public static final int DIFFICULTY_HARD = 2;

    public static int gameDifficulty = DIFFICULTY_MEDIUM; // Default to medium

    // ForgeConfigSpec values
    private static ForgeConfigSpec.IntValue defaultGameTimeConfig;
    private static ForgeConfigSpec.BooleanValue useItemSpecificTimesConfig;

    private static ForgeConfigSpec.IntValue redTeamColorConfig;
    private static ForgeConfigSpec.IntValue blueTeamColorConfig;
    private static ForgeConfigSpec.ConfigValue<String> redTeamNameConfig;
    private static ForgeConfigSpec.ConfigValue<String> blueTeamNameConfig;
    private static ForgeConfigSpec.ConfigValue<String> gameStartMessageConfig;
    private static ForgeConfigSpec.ConfigValue<String> redTeamTaskMessageConfig;
    private static ForgeConfigSpec.ConfigValue<String> blueTeamTaskMessageConfig;
    private static ForgeConfigSpec.ConfigValue<String> redTeamWinMessageConfig;
    private static ForgeConfigSpec.ConfigValue<String> blueTeamWinMessageConfig;
    // Remove old flat lists
    // private static ForgeConfigSpec.ConfigValue<List<? extends String>>
    // gameItemsConfig;
    // private static ForgeConfigSpec.ConfigValue<List<? extends String>>
    // itemTimesConfig;

    // New grouped lists
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> easyItemsConfig;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> mediumItemsConfig;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> hardItemsConfig;
    // Per‐group time settings
    private static ForgeConfigSpec.IntValue easyTimeConfig;
    private static ForgeConfigSpec.IntValue mediumTimeConfig;
    private static ForgeConfigSpec.IntValue hardTimeConfig;
    private static ForgeConfigSpec.IntValue gameDifficultyConfig;

    // Define sets of item IDs for dynamic estimation based on actual gameplay
    // difficulty
    private static final Set<String> EASY_ITEMS = Set.of(
            // Basic resources - very quick to obtain
            "minecraft:dirt", "minecraft:cobblestone", "minecraft:oak_planks", "minecraft:stick",
            "minecraft:sand", "minecraft:gravel", "minecraft:oak_log", "minecraft:birch_log",
            // Basic items - quick gathering
            "minecraft:flint", "minecraft:feather", "minecraft:string", "minecraft:leather",
            "minecraft:bone", "minecraft:gunpowder", "minecraft:spider_eye", "minecraft:rotten_flesh",
            // Basic food - early game
            "minecraft:bread", "minecraft:apple", "minecraft:carrot", "minecraft:potato",
            "minecraft:beetroot", "minecraft:melon_slice", "minecraft:pumpkin", "minecraft:egg",
            // Basic crafted items
            "minecraft:wooden_pickaxe", "minecraft:wooden_sword", "minecraft:wooden_axe",
            "minecraft:crafting_table", "minecraft:furnace", "minecraft:torch",
            // Dyes and flowers (moved from harder categories)
            "minecraft:white_dye", "minecraft:orange_dye", "minecraft:magenta_dye",
            "minecraft:light_blue_dye", "minecraft:yellow_dye", "minecraft:lime_dye",
            "minecraft:pink_dye", "minecraft:gray_dye", "minecraft:light_gray_dye",
            "minecraft:cyan_dye", "minecraft:purple_dye", "minecraft:blue_dye",
            "minecraft:brown_dye", "minecraft:green_dye", "minecraft:red_dye", "minecraft:black_dye",
            "minecraft:dandelion", "minecraft:poppy", "minecraft:blue_orchid", "minecraft:oxeye_daisy");

    private static final Set<String> MEDIUM_ITEMS = Set.of(
            // Mid-tier resources - require mining
            "minecraft:iron_ingot", "minecraft:gold_ingot", "minecraft:coal",
            "minecraft:lapis_lazuli", "minecraft:redstone", "minecraft:copper_ingot",
            // Mid-tier equipment
            "minecraft:iron_pickaxe", "minecraft:iron_sword", "minecraft:iron_axe",
            "minecraft:iron_shovel", "minecraft:iron_helmet", "minecraft:iron_chestplate",
            "minecraft:iron_leggings", "minecraft:iron_boots", "minecraft:shield",
            // Mid-tier weapons
            "minecraft:bow", "minecraft:crossbow", "minecraft:arrow", "minecraft:spectral_arrow",
            // Mid-tier crafted items
            "minecraft:fishing_rod", "minecraft:compass", "minecraft:clock",
            "minecraft:shears", "minecraft:bucket", "minecraft:cauldron",
            // Mid-tier food and farming
            "minecraft:golden_carrot", "minecraft:cooked_beef", "minecraft:cooked_porkchop",
            "minecraft:cake", "minecraft:pumpkin_pie", "minecraft:mushroom_stew");

    private static final Set<String> HARD_ITEMS = Set.of(
            // Diamond tier
            "minecraft:diamond", "minecraft:diamond_pickaxe", "minecraft:diamond_sword",
            "minecraft:diamond_axe", "minecraft:diamond_shovel", "minecraft:diamond_helmet",
            "minecraft:diamond_chestplate", "minecraft:diamond_leggings", "minecraft:diamond_boots",

            // Special overworld items that don't require Nether/End
            "minecraft:jukebox", "minecraft:composter",
            "minecraft:golden_block", "minecraft:obsidian", "minecraft:emerald",

            // Advanced redstone
            "minecraft:dispenser", "minecraft:dropper", "minecraft:observer",
            "minecraft:repeater", "minecraft:comparator", "minecraft:daylight_detector");

    static {
        COMMON_BUILDER.comment("Game Settings");

        defaultGameTimeConfig = COMMON_BUILDER
                .comment("Default game time in minutes")
                .defineInRange("defaultGameTime", 10, 1, 60);

        useItemSpecificTimesConfig = COMMON_BUILDER
                .comment("Whether to use item-specific times")
                .define("useItemSpecificTimes", true); // Enable per-item times by default

        redTeamColorConfig = COMMON_BUILDER
                .comment("Color for Red Team (RGB format)")
                .defineInRange("redTeamColor", 0xFF0000, 0, 0xFFFFFF);

        blueTeamColorConfig = COMMON_BUILDER
                .comment("Color for Blue Team (RGB format)")
                .defineInRange("blueTeamColor", 0x0000FF, 0, 0xFFFFFF);

        redTeamNameConfig = COMMON_BUILDER
                .comment("Name of the Red Team")
                .define("redTeamName", "Red Team");

        blueTeamNameConfig = COMMON_BUILDER
                .comment("Name of the Blue Team")
                .define("blueTeamName", "Blue Team");

        gameStartMessageConfig = COMMON_BUILDER
                .comment("Message shown when game starts")
                .define("gameStartMessage", "The game has started!");

        redTeamTaskMessageConfig = COMMON_BUILDER
                .comment("Task message for Red Team (use %s for item name and %d for seconds)")
                .define("redTeamTaskMessage", "Find %s within %d minutes!");

        blueTeamTaskMessageConfig = COMMON_BUILDER
                .comment("Task message for Blue Team (use %d for minutes)")
                .define("blueTeamTaskMessage", "Prevent the Red Team for %d minutes!");

        redTeamWinMessageConfig = COMMON_BUILDER
                .comment("Message when Red Team wins")
                .define("redTeamWinMessage", "The Red Team has won!");

        blueTeamWinMessageConfig = COMMON_BUILDER
                .comment("Message when Blue Team wins")
                .define("blueTeamWinMessage", "The Blue Team has won!");

        easyTimeConfig = COMMON_BUILDER
                .comment("Time (minutes) for EASY items")
                .defineInRange("easyItemTime", 5, 1, 120);
        mediumTimeConfig = COMMON_BUILDER
                .comment("Time (minutes) for MEDIUM items")
                .defineInRange("mediumItemTime", 15, 1, 120); // Increased from 10 to 15
        hardTimeConfig = COMMON_BUILDER
                .comment("Time (minutes) for HARD items")
                .defineInRange("hardItemTime", 35, 1, 120); // Increased from 25 to 35

        // define grouped item lists
        easyItemsConfig = COMMON_BUILDER
                .comment("Items classified as EASY")
                .defineList("easyItems",
                        Arrays.asList("minecraft:apple", "minecraft:stick"),
                        s -> s instanceof String && ResourceLocation.isValidResourceLocation((String) s));
        mediumItemsConfig = COMMON_BUILDER
                .comment("Items classified as MEDIUM")
                .defineList("mediumItems",
                        Arrays.asList("minecraft:iron_ingot", "minecraft:bow"),
                        s -> s instanceof String && ResourceLocation.isValidResourceLocation((String) s));
        hardItemsConfig = COMMON_BUILDER
                .comment("Items classified as HARD")
                .defineList("hardItems",
                        Arrays.asList("minecraft:diamond", "minecraft:elytra"),
                        s -> s instanceof String && ResourceLocation.isValidResourceLocation((String) s));

        gameDifficultyConfig = COMMON_BUILDER
                .comment("Game difficulty level (0=Easy, 1=Medium, 2=Hard)")
                .defineInRange("gameDifficulty", DIFFICULTY_MEDIUM, 0, 2);

        COMMON_SPEC = COMMON_BUILDER.build();
    }

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == COMMON_SPEC) {
            bakeConfig();
        }
    }

    public static void bakeConfig() {
        try {
            defaultGameTime = defaultGameTimeConfig.get();
            useItemSpecificTimes = useItemSpecificTimesConfig.get();
            redTeamColor = redTeamColorConfig.get();
            blueTeamColor = blueTeamColorConfig.get();
            redTeamName = redTeamNameConfig.get();
            blueTeamName = blueTeamNameConfig.get();
            gameStartMessage = gameStartMessageConfig.get();
            redTeamTaskMessage = redTeamTaskMessageConfig.get();
            blueTeamTaskMessage = blueTeamTaskMessageConfig.get();
            redTeamWinMessage = redTeamWinMessageConfig.get();
            blueTeamWinMessage = blueTeamWinMessageConfig.get();
            gameDifficulty = gameDifficultyConfig.get();

            // retrieve group times
            int tEasy = easyTimeConfig.get();
            int tMed = mediumTimeConfig.get();
            int tHard = hardTimeConfig.get();

            // rebuild items and times by group
            gameItems.clear();
            itemTimes.clear();

            for (String idStr : easyItemsConfig.get()) {
                ResourceLocation id = ResourceLocation.tryParse(idStr);
                Item it = ForgeRegistries.ITEMS.getValue(id);
                if (it != null && it != Items.AIR) {
                    gameItems.add(it);
                    itemTimes.put(it, tEasy);
                }
            }
            for (String idStr : mediumItemsConfig.get()) {
                ResourceLocation id = ResourceLocation.tryParse(idStr);
                Item it = ForgeRegistries.ITEMS.getValue(id);
                if (it != null && it != Items.AIR) {
                    gameItems.add(it);
                    itemTimes.put(it, tMed);
                }
            }
            for (String idStr : hardItemsConfig.get()) {
                ResourceLocation id = ResourceLocation.tryParse(idStr);
                Item it = ForgeRegistries.ITEMS.getValue(id);
                if (it != null && it != Items.AIR) {
                    gameItems.add(it);
                    itemTimes.put(it, tHard);
                }
            }

            CraftMine.LOGGER.info("Loaded CraftMine config: easy={}, med={}, hard={}",
                    easyItemsConfig.get().size(),
                    mediumItemsConfig.get().size(),
                    hardItemsConfig.get().size());
        } catch (Exception e) {
            CraftMine.LOGGER.error("Error loading config: {}", e.getMessage());
        }
    }

    public static int getTimeForItem(Item item) {
        if (useItemSpecificTimes && itemTimes.containsKey(item)) {
            return itemTimes.get(item);
        }
        return defaultGameTime;
    }

    public static String getDifficultyName() {
        return switch (gameDifficulty) {
            case DIFFICULTY_EASY -> "Easy";
            case DIFFICULTY_HARD -> "Hard";
            default -> "Medium";
        };
    }

    public static void cycleDifficulty() {
        gameDifficulty = (gameDifficulty + 1) % 3;
    }

    public static int getEstimatedTimeForItemDynamic(Item item) {
        String id = ForgeRegistries.ITEMS.getKey(item).toString();
        Random rnd = new Random(id.hashCode()); // deterministic per item

        // Base times for each difficulty (in seconds)
        int baseTime;
        int variationRange;

        // Remove specific handling for items we've excluded
        if (EASY_ITEMS.contains(id)) {
            // Easy items: 3-7 minutes
            baseTime = 180; // 3 minutes base
            variationRange = 240; // up to 4 additional minutes
        } else if (MEDIUM_ITEMS.contains(id)) {
            // Medium items: 8-15 minutes
            baseTime = 480; // 8 minutes base
            variationRange = 420; // up to 7 additional minutes
        } else if (HARD_ITEMS.contains(id)) {
            // Hard items: 15-35 minutes
            baseTime = 900; // 15 minutes base
            variationRange = 1200; // up to 20 additional minutes
        } else {
            // Unknown items: 10-20 minutes
            baseTime = 600; // 10 minutes base
            variationRange = 600; // up to 10 additional minutes
        }

        // Adjust based on game difficulty
        switch (gameDifficulty) {
            case DIFFICULTY_EASY -> {
                baseTime = (int) (baseTime * 1.5); // 50% more time on easy
                variationRange = (int) (variationRange * 1.2);
            }
            case DIFFICULTY_HARD -> {
                baseTime = (int) (baseTime * 0.7); // 30% less time on hard
                variationRange = (int) (variationRange * 0.8);
            }
        }

        return baseTime + rnd.nextInt(variationRange);
    }
}
