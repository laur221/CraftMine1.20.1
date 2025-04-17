package com.seishironagi.craftmine.data;

import com.seishironagi.craftmine.CraftMine;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CraftMine.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        
        // Add data providers as needed
        // Commenting out missing provider until implemented
        /*
        generator.addProvider(
            event.includeServer(),
            new CraftMineRecipeProvider(generator.getPackOutput())
        );
        */
    }
}
