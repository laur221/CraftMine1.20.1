package com.seishironagi.craftmine.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.seishironagi.craftmine.GameManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class StopGameCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands.literal("craftmine")
                .then(Commands.literal("stop")
                        .requires(source -> source.hasPermission(2)) // Require permission level 2 (op)
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            if (GameManager.getInstance().isGameRunning()) {
                                // Force stop the game without declaring a winner
                                GameManager.getInstance().stopGame();
                                source.sendSuccess(() -> Component.literal("Game forcefully stopped."), true);
                            } else {
                                source.sendFailure(Component.literal("No game is currently running."));
                            }

                            return 1;
                        }));

        dispatcher.register(commandBuilder);
    }
}
