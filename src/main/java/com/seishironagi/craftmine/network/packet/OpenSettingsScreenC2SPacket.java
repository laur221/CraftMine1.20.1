package com.seishironagi.craftmine.network.packet;

import com.seishironagi.craftmine.gui.GameSettingsContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import java.util.function.Supplier;

public class OpenSettingsScreenC2SPacket {
    public OpenSettingsScreenC2SPacket() {}
    public OpenSettingsScreenC2SPacket(FriendlyByteBuf buf) {}
    public void toBytes(FriendlyByteBuf buf) {}
    
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                NetworkHooks.openScreen(player, new SimpleMenuProvider(
                    (windowId, inv, p) -> new GameSettingsContainer(windowId, inv),
                    Component.literal("Game Settings")
                ));
            }
        });
        return true;
    }
}
