package com.seishironagi.craftmine.client;

import com.seishironagi.craftmine.CraftMine;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CraftMine.MOD_ID, value = Dist.CLIENT)
public class KeyInputHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (ModKeyBindings.SHOW_RECIPE_KEY.consumeClick()) {
            // The G key was pressed, toggle recipe display
            RecipeDisplayOverlay.toggleRecipeDisplay();
        }
    }
}
