package com.seishironagi.craftmine;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import com.seishironagi.craftmine.network.ModMessages;
import org.slf4j.Logger;

@Mod(CraftMine.MODID)
public class CraftMine {
    public static final String MODID = "craftmine";
    private static final Logger LOGGER = LogUtils.getLogger();

    public CraftMine() {
        // Use FMLJavaModLoadingContext.get() directly for operations
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the configuration
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC, "craftmine-common.toml");

        // Register mod components
        ModRegistry.register(modEventBus);

        // Register the commonSetup method for mod initialization
        modEventBus.addListener(this::commonSetup);

        // Register client setup listener on the mod event bus
        modEventBus.addListener(ClientModEvents::onClientSetup);

        // Register ourselves for server and other game events on the Forge bus
        MinecraftForge.EVENT_BUS.register(this);

        // Register ModEvents on the Forge bus
        MinecraftForge.EVENT_BUS.register(ModEvents.class);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Register network messages
        event.enqueueWork(ModMessages::register);

        LOGGER.info("CraftMine Mod initialized successfully!");
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Initialize the game manager
        GameManager.getInstance();

        LOGGER.info("CraftMine ready on server!");
    }

    // Client setup is now handled by the listener added in the constructor
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("CraftMine client initialization");

            // Register client-side components (like overlays)
            // This is now done via ModRegistry.registerClient called by the listener
            // ModRegistry.registerClient(FMLJavaModLoadingContext.get().getModEventBus()); // No longer needed here
        }
    }
}
