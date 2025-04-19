package com.seishironagi.craftmine;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CraftMine.MOD_ID)
public class CraftMine {
    // Make this public static final so it can be used throughout the mod consistently
    public static final String MOD_ID = "craftmine";
    // Make logger public static
    public static final Logger LOGGER = LogManager.getLogger();
    
    public CraftMine() {
        // The get() methods are deprecated but still functional
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
        
        // Setup event bus
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        
        // Register registry objects
        ModRegistry.register(modEventBus);
        if (FMLEnvironment.dist.isClient()) {
            ModRegistry.registerClient(modEventBus);
        }
        
        // Register ourselves for server and other game events
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    private void setup(final FMLCommonSetupEvent event) {
        // Handle mod setup after registry objects are registered
        event.enqueueWork(() -> {
            // Setup network messages
            com.seishironagi.craftmine.network.ModMessages.register();
        });
        
        // Use a safe default welcome message in case config hasn't loaded yet
        // LOGGER.info(Config.welcomeMessage != null ? Config.welcomeMessage : "Welcome to CraftMine!");
    }
}
