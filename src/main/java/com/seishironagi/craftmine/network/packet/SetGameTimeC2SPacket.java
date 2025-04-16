package com.seishironagi.craftmine.network.packet;

import com.seishironagi.craftmine.Config;
import com.seishironagi.craftmine.GameManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SetGameTimeC2SPacket {
    private final int gameTime;
    
    public SetGameTimeC2SPacket(int gameTime) {
        this.gameTime = gameTime;
    }
    
    public SetGameTimeC2SPacket(FriendlyByteBuf buf) {
        this.gameTime = buf.readInt();
    }
    
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(gameTime);
    }
    
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            if (gameTime == -1) {
                // Toggle item-specific times
                Config.useItemSpecificTimes = !Config.useItemSpecificTimes;
            } else {
                // Set game time
                GameManager.getInstance().setGameTime(gameTime);
            }
        });
        return true;
    }
}
