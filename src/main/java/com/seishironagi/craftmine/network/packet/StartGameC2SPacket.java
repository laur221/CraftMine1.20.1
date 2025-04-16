 package com.seishironagi.craftmine.network.packet;

import com.seishironagi.craftmine.GameManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class StartGameC2SPacket {
    
    public StartGameC2SPacket() {
    }
    
    public StartGameC2SPacket(FriendlyByteBuf buf) {
    }
    
    public void toBytes(FriendlyByteBuf buf) {
    }
    
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // Start the game
            GameManager.getInstance().startGame();
        });
        return true;
    }
}
