package com.seishironagi.craftmine;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = CraftMine.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", true);

    private static final ForgeConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ");

    // TEAM GAME CONFIG
    private static final ForgeConfigSpec.IntValue DEFAULT_GAME_TIME = BUILDER
            .comment("Default time in minutes for item search game")
            .defineInRange("defaultGameTime", 5, 1, 60);

    private static final ForgeConfigSpec.BooleanValue USE_ITEM_SPECIFIC_TIMES = BUILDER
            .comment("Whether to use item-specific times instead of a fixed time for all items")
            .define("useItemSpecificTimes", false);

    private static final ForgeConfigSpec.ConfigValue<String> WELCOME_MESSAGE = BUILDER
            .comment("Welcome message shown at game start to all players")
            .define("welcomeMessage", "Welcome to the Item Hunt Game! Choose your team and prepare for the challenge!");

    // Add missing config fields from previous step
    private static final ForgeConfigSpec.ConfigValue<String> RED_TEAM_TASK_MESSAGE = BUILDER
            .comment("Message shown to red team player when assigned an item. %s = item name, %d = time")
            .define("redTeamTaskMessage", "You need to find: %s. You have %d minutes!");

    private static final ForgeConfigSpec.ConfigValue<String> GAME_START_MESSAGE = BUILDER
            .comment("Message shown to all players when the game starts")
            .define("gameStartMessage", "The hunt has begun! Red team must find their target item!");

    private static final ForgeConfigSpec.ConfigValue<String> RED_TEAM_NAME = BUILDER
            .comment("Name for the red team (hunter)")
            .define("redTeamName", "Red Team");

    private static final ForgeConfigSpec.ConfigValue<String> BLUE_TEAM_NAME = BUILDER
            .comment("Name for the blue team (seekers)")
            .define("blueTeamName", "Blue Team");

    // Add missing config fields from previous step
    private static final ForgeConfigSpec.ConfigValue<String> RED_TEAM_WIN_MESSAGE = BUILDER
            .comment("Message when red team wins")
            .define("redTeamWinMessage", "Red team found their item! Red team wins!");

    private static final ForgeConfigSpec.ConfigValue<String> BLUE_TEAM_WIN_MESSAGE = BUILDER
            .comment("Message when blue team wins (time runs out)")
            .define("blueTeamWinMessage", "Time's up! Red team couldn't find their item. Blue team wins!");


    // a list of strings that are treated as resource locations for items
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of items to log on common setup.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), Config::validateItemName);

    // a list of strings that are treated as resource locations for obtainable items in the game
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> GAME_ITEM_STRINGS = BUILDER
            .comment("A list of items that can be randomly assigned as targets for the red team player.")
            .defineListAllowEmpty("gameItems", List.of(
                "minecraft:diamond",
                "minecraft:emerald",
                "minecraft:iron_ingot",
                "minecraft:gold_ingot",
                "minecraft:redstone",
                "minecraft:lapis_lazuli",
                "minecraft:coal",
                "minecraft:wheat",
                "minecraft:apple",
                "minecraft:bread"
            ), Config::validateItemName);

    // Mapping of items to their difficulty (time in minutes)
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ITEM_TIMES = BUILDER
            .comment("Time in minutes for specific items (format: namespace:path:minutes)") // Updated format description
            .defineListAllowEmpty("itemTimes", List.of(
                "minecraft:diamond:10",
                "minecraft:emerald:8",
                "minecraft:iron_ingot:5",
                "minecraft:gold_ingot:7",
                "minecraft:redstone:4",
                "minecraft:lapis_lazuli:6",
                "minecraft:coal:3",
                "minecraft:wheat:2",
                "minecraft:apple:2",
                "minecraft:bread:3"
            ), Config::validateItemTimeFormat);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean logDirtBlock;
    public static int magicNumber;
    public static String magicNumberIntroduction;
    public static Set<Item> items;

    // TEAM GAME VARIABLES
    public static int defaultGameTime;
    public static boolean useItemSpecificTimes;
    public static String welcomeMessage;
    public static String redTeamName;
    public static String blueTeamName;
    public static Set<Item> gameItems;
    public static java.util.Map<Item, Integer> itemTimes;
    // Add missing static fields
    public static String redTeamTaskMessage;
    public static String gameStartMessage;
    public static String redTeamWinMessage;
    public static String blueTeamWinMessage;


    private static boolean validateItemName(final Object obj)
    {
        // Use ResourceLocation.tryParse for validation
        return obj instanceof final String itemName && ResourceLocation.tryParse(itemName) != null && ForgeRegistries.ITEMS.containsKey(ResourceLocation.parse(itemName));
    }

    private static boolean validateItemTimeFormat(final Object obj)
    {
        if (!(obj instanceof String str)) {
            return false;
        }

        String[] parts = str.split(":");
        if (parts.length != 3) { // format: namespace:path:time
            return false;
        }

        // Validate item existence using tryParse and parse
        String itemName = parts[0] + ":" + parts[1];
        ResourceLocation itemLoc = ResourceLocation.tryParse(itemName);
        if (itemLoc == null || !ForgeRegistries.ITEMS.containsKey(itemLoc)) {
            return false;
        }

        // Validate time is a number
        try {
            Integer.parseInt(parts[2]);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        logDirtBlock = LOG_DIRT_BLOCK.get();
        magicNumber = MAGIC_NUMBER.get();
        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();

        // convert the list of strings into a set of items using parse
        items = ITEM_STRINGS.get().stream()
                .map(ResourceLocation::parse)
                .map(ForgeRegistries.ITEMS::getValue)
                .collect(Collectors.toSet());

        // Load team game config
        defaultGameTime = DEFAULT_GAME_TIME.get();
        useItemSpecificTimes = USE_ITEM_SPECIFIC_TIMES.get();
        welcomeMessage = WELCOME_MESSAGE.get();
        redTeamName = RED_TEAM_NAME.get();
        blueTeamName = BLUE_TEAM_NAME.get();
        // Load missing config values
        redTeamTaskMessage = RED_TEAM_TASK_MESSAGE.get();
        gameStartMessage = GAME_START_MESSAGE.get();
        redTeamWinMessage = RED_TEAM_WIN_MESSAGE.get();
        blueTeamWinMessage = BLUE_TEAM_WIN_MESSAGE.get();


        // Convert game items using parse
        gameItems = GAME_ITEM_STRINGS.get().stream()
                .map(ResourceLocation::parse)
                .map(ForgeRegistries.ITEMS::getValue)
                .collect(Collectors.toSet());

        // Parse item times map using parse
        itemTimes = new java.util.HashMap<>();
        ITEM_TIMES.get().forEach(entry -> {
            String[] parts = entry.split(":");
            String itemName = parts[0] + ":" + parts[1];
            int time = Integer.parseInt(parts[2]);
            Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(itemName));
            if (item != null) {
                itemTimes.put(item, time);
            }
        });
    }
}
