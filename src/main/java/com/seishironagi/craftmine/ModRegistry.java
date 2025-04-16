package com.seishironagi.craftmine;

import com.seishironagi.craftmine.items.GameControllerItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.seishironagi.craftmine.client.TimerHudOverlay;

public class ModRegistry {
    public static final DeferredRegister<Item> ITEMS = 
            DeferredRegister.create(ForgeRegistries.ITEMS, CraftMine.MODID);
            
    // Register items
    public static final RegistryObject<Item> GAME_CONTROLLER_ITEM = 
            ITEMS.register("game_controller", GameControllerItem::new);
            
    // Register everything
    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
    
    // Client-only registrations
    public static void registerClient(IEventBus modEventBus) {
        modEventBus.addListener(ModRegistry::registerGuiOverlays);
    }
    
    private static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("timer", TimerHudOverlay.HUD_TIMER);
    }
}
