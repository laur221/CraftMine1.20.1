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
                .comment("Task message for Red Team (use %s for item name and time)")
                .define("redTeamTaskMessage", "Find %s within %d minutes!");

        blueTeamTaskMessageConfig = COMMON_BUILDER
                .comment("Task message for Blue Team")
                .define("blueTeamTaskMessage", "Prevent the Red Team from finding their target!");

        redTeamWinMessageConfig = COMMON_BUILDER
                .comment("Message when Red Team wins")
                .define("redTeamWinMessage", "The Red Team has won!");

        blueTeamWinMessageConfig = COMMON_BUILDER
                .comment("Message when Blue Team wins")
                .define("blueTeamWinMessage", "The Blue Team has won!");

        // Define default items for the game
        List<String> defaultItems = Arrays.asList(
                "minecraft:diamond",
                "minecraft:emerald",
                "minecraft:gold_ingot",
                "minecraft:iron_ingot");

        easyTimeConfig = COMMON_BUILDER
                .comment("Time (minutes) for EASY items")
                .defineInRange("easyItemTime", 5, 1, 120);
        mediumTimeConfig = COMMON_BUILDER
                .comment("Time (minutes) for MEDIUM items")
                .defineInRange("mediumItemTime", 10, 1, 120);
        hardTimeConfig = COMMON_BUILDER
                .comment("Time (minutes) for HARD items")
                .defineInRange("hardItemTime", 15, 1, 120);

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
}
