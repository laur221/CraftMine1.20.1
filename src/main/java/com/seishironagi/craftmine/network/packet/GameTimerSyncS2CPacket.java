package com.seishironagi.craftmine.network.packet;

import com.seishironagi.craftmine.client.ClientGameData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class GameTimerSyncS2CPacket {
    private final int remainingSeconds;
    private final boolean gameRunning;
    
    public GameTimerSyncS2CPacket(int remainingSeconds, boolean gameRunning) {
        this.remainingSeconds = remainingSeconds;
        this.gameRunning = gameRunning;
    }
    
    public GameTimerSyncS2CPacket(FriendlyByteBuf buf) {
        this.remainingSeconds = buf.readInt();
        this.gameRunning = buf.readBoolean();
    }
    
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(remainingSeconds);
        buf.writeBoolean(gameRunning);
    }
    
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // Update client-side game data
            ClientGameData.setGameRunning(gameRunning);
            ClientGameData.setRemainingSeconds(remainingSeconds);
        });
        return true;
    }
}
