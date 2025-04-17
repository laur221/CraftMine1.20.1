package com.seishironagi.craftmine.network.packet;

import com.seishironagi.craftmine.gui.GameMenuContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import java.util.function.Supplier;

public class OpenMainMenuC2SPacket {
    public OpenMainMenuC2SPacket() {}
    public OpenMainMenuC2SPacket(FriendlyByteBuf buf) {}
    public void toBytes(FriendlyByteBuf buf) {}
    
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                NetworkHooks.openScreen(player, new SimpleMenuProvider(
                    (windowId, inv, p) -> new GameMenuContainer(windowId, inv),
                    Component.literal("Game Controller")
                ));
            }
        });
        return true;
    }
}
