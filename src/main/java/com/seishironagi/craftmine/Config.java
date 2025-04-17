package com.seishironagi.craftmine;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

@Mod.EventBusSubscriber(modid = CraftMine.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    public static final ForgeConfigSpec COMMON_SPEC;
    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    
    // Config values with default values
    public static int defaultGameTime = 10;
    public static boolean useItemSpecificTimes = true;
    public static String welcomeMessage = "Welcome to CraftMine!";
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
    
    // ForgeConfigSpec values
    private static ForgeConfigSpec.IntValue defaultGameTimeConfig;
    private static ForgeConfigSpec.BooleanValue useItemSpecificTimesConfig;
    private static ForgeConfigSpec.ConfigValue<String> welcomeMessageConfig;
    private static ForgeConfigSpec.IntValue redTeamColorConfig;
    private static ForgeConfigSpec.IntValue blueTeamColorConfig;
    private static ForgeConfigSpec.ConfigValue<String> redTeamNameConfig;
    private static ForgeConfigSpec.ConfigValue<String> blueTeamNameConfig;
    private static ForgeConfigSpec.ConfigValue<String> gameStartMessageConfig;
    private static ForgeConfigSpec.ConfigValue<String> redTeamTaskMessageConfig;
    private static ForgeConfigSpec.ConfigValue<String> blueTeamTaskMessageConfig;
    private static ForgeConfigSpec.ConfigValue<String> redTeamWinMessageConfig;
    private static ForgeConfigSpec.ConfigValue<String> blueTeamWinMessageConfig;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> gameItemsConfig;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> itemTimesConfig;
    
    static {
        COMMON_BUILDER.comment("Game Settings");
        
        defaultGameTimeConfig = COMMON_BUILDER
                .comment("Default game time in minutes")
                .defineInRange("defaultGameTime", 10, 1, 60);
        
        useItemSpecificTimesConfig = COMMON_BUILDER
                .comment("Whether to use item-specific times")
                .define("useItemSpecificTimes", true);
        
        welcomeMessageConfig = COMMON_BUILDER
                .comment("Welcome message shown to players")
                .define("welcomeMessage", "Welcome to CraftMine!");
        
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
                "minecraft:iron_ingot"
        );
        
        gameItemsConfig = COMMON_BUILDER
                .comment("List of items that can be assigned as targets")
                .defineList("gameItems", defaultItems, s -> s instanceof String && ResourceLocation.isValidResourceLocation((String)s));
        
        List<String> defaultItemTimes = Arrays.asList(
                "minecraft:diamond:15",
                "minecraft:emerald:10",
                "minecraft:gold_ingot:8",
                "minecraft:iron_ingot:5"
        );
        
        itemTimesConfig = COMMON_BUILDER
                .comment("Map of item-specific times (in minutes)")
                .defineList("itemTimes", defaultItemTimes, s -> s instanceof String);
        
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
            welcomeMessage = welcomeMessageConfig.get();
            redTeamColor = redTeamColorConfig.get();
            blueTeamColor = blueTeamColorConfig.get();
            redTeamName = redTeamNameConfig.get();
            blueTeamName = blueTeamNameConfig.get();
            gameStartMessage = gameStartMessageConfig.get();
            redTeamTaskMessage = redTeamTaskMessageConfig.get();
            blueTeamTaskMessage = blueTeamTaskMessageConfig.get();
            redTeamWinMessage = redTeamWinMessageConfig.get();
            blueTeamWinMessage = blueTeamWinMessageConfig.get();
            
            gameItems.clear();
            for (String itemId : gameItemsConfig.get()) {
                try {
                    ResourceLocation id = ResourceLocation.tryParse(itemId);
                    if (id != null) {
                        Item item = ForgeRegistries.ITEMS.getValue(id);
                        if (item != null && item != Items.AIR) {
                            gameItems.add(item);
                        } else {
                            CraftMine.LOGGER.warn("Invalid or missing item in gameItems: {}", itemId);
                        }
                    }
                } catch (Exception e) {
                    CraftMine.LOGGER.warn("Invalid resource location in gameItems: {}", itemId, e);
                }
            }
            
            itemTimes.clear();
            for (String entry : itemTimesConfig.get()) {
                try {
                    String[] parts = entry.split(":");
                    if (parts.length == 2) {
                        ResourceLocation id = ResourceLocation.tryParse(parts[0]);
                        if (id != null) {
                            Item item = ForgeRegistries.ITEMS.getValue(id);
                            if (item != null && item != Items.AIR) {
                                itemTimes.put(item, Integer.parseInt(parts[1]));
                            } else {
                                CraftMine.LOGGER.warn("Invalid item in itemTimes: {}", parts[0]);
                            }
                        }
                    }
                } catch (Exception e) {
                    CraftMine.LOGGER.warn("Invalid resource location or time in itemTimes: {}", entry, e);
                }
            }
            
            CraftMine.LOGGER.info("Loaded CraftMine config: {} game items, {} item times",
                    gameItems.size(), itemTimes.size());
        } catch (Exception e) {
            CraftMine.LOGGER.error("Error loading config: {}", e.getMessage());
        }
    }
    
    public static int getTimeForItem(Item item) {
        return useItemSpecificTimes && itemTimes.containsKey(item)
                ? itemTimes.get(item)
                : defaultGameTime;
    }
}
