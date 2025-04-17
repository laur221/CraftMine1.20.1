package com.seishironagi.craftmine;

import com.seishironagi.craftmine.items.GameControllerItem;
import com.seishironagi.craftmine.gui.*;
import com.seishironagi.craftmine.client.TimerHudOverlay;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger("CraftMine-Registry");
    
    public static final DeferredRegister<Item> ITEMS = 
            DeferredRegister.create(ForgeRegistries.ITEMS, CraftMine.MOD_ID);

    public static final DeferredRegister<MenuType<?>> MENUS = 
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, CraftMine.MOD_ID);
    
    public static final RegistryObject<Item> GAME_CONTROLLER_ITEM = 
            ITEMS.register("game_controller", GameControllerItem::new);

    public static final RegistryObject<MenuType<GameMenuContainer>> GAME_MENU = 
            MENUS.register("game_menu", () -> 
                    IForgeMenuType.create((windowId, inv, data) -> 
                            new GameMenuContainer(windowId, inv)));

    public static final RegistryObject<MenuType<GameInfoContainer>> GAME_INFO_CONTAINER =
            MENUS.register("game_info", () -> 
                    IForgeMenuType.create((windowId, inv, data) -> 
                            new GameInfoContainer(windowId, inv)));

    // Register container for settings screen
    public static final RegistryObject<MenuType<GameSettingsContainer>> GAME_SETTINGS_CONTAINER =
            MENUS.register("game_settings", () -> 
                    IForgeMenuType.create((windowId, inv, data) -> 
                            new GameSettingsContainer(windowId, inv)));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        MENUS.register(modEventBus);
        modEventBus.addListener(ModRegistry::addCreative);
    }

    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(GAME_CONTROLLER_ITEM);
        }
    }

    public static void registerClient(IEventBus modEventBus) {
        modEventBus.addListener(ModRegistry::registerGuiOverlays);
        modEventBus.addListener(ModRegistry::onClientSetup);
    }

    private static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("timer", TimerHudOverlay.HUD_TIMER);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            try {
                MenuScreens.register(GAME_MENU.get(), GameMenuScreen::new);
                MenuScreens.register(GAME_INFO_CONTAINER.get(), GameInfoScreen::new);
                MenuScreens.register(GAME_SETTINGS_CONTAINER.get(), GameSettingsScreen::new);
                LOGGER.info("Registered all menu screens successfully");
            } catch (Exception e) {
                LOGGER.error("Failed to register menu screens: " + e.getMessage());
            }
        });
    }
}
