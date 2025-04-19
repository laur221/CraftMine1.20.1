package com.seishironagi.craftmine;

import com.seishironagi.craftmine.client.AnnouncementOverlay;
import com.seishironagi.craftmine.client.TargetItemOverlay;
import com.seishironagi.craftmine.client.TimerHudOverlay;
import com.seishironagi.craftmine.items.GameControllerItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
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

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CraftMine.MOD_ID);

    public static final RegistryObject<Item> GAME_CONTROLLER_ITEM = ITEMS.register("game_controller",
            GameControllerItem::new);

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
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
        event.registerAboveAll("target_item", TargetItemOverlay.HUD_TARGET_ITEM);
        event.registerAboveAll("announcement", AnnouncementOverlay.HUD_ANNOUNCEMENT);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            try {
                // Noile interfețe grafice nu mai necesită înregistrare prin MenuScreens
                // deoarece sunt deschise direct prin Minecraft.getInstance().setScreen()
                LOGGER.info("Modern GUI system initialized successfully");
            } catch (Exception e) {
                LOGGER.error("Failed to initialize GUI system: " + e.getMessage());
            }
        });
    }
}
