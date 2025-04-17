package com.seishironagi.craftmine.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
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
                // Executăm pe partea client pentru a deschide ecranul
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    net.minecraft.client.Minecraft.getInstance().execute(() -> {
                        net.minecraft.client.Minecraft.getInstance().setScreen(
                            new com.seishironagi.craftmine.gui.GameMenuScreen()
                        );
                    });
                });
            }
        });
        return true;
    }
}
