package com.seishironagi.craftmine.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.seishironagi.craftmine.CraftMine;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class ModKeyBindings {
    public static final String KEY_CATEGORY_CRAFTMINE = "key.category." + CraftMine.MOD_ID;
    public static final String KEY_SHOW_RECIPE = "key.craftmine.show_recipe";

    public static final KeyMapping SHOW_RECIPE_KEY = new KeyMapping(
            KEY_SHOW_RECIPE,
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G, // G key
            KEY_CATEGORY_CRAFTMINE);
}
