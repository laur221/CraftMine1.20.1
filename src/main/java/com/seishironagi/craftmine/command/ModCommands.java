package com.seishironagi.craftmine.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.seishironagi.craftmine.CraftMine;

@Mod.EventBusSubscriber(modid = CraftMine.MOD_ID)
public class ModCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        // Register the stop command
        StopGameCommand.register(dispatcher);

        CraftMine.LOGGER.info("CraftMine commands registered");
    }
}
