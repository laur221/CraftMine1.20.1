package com.seishironagi.craftmine.network.packet;

import com.seishironagi.craftmine.GameManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class JoinTeamC2SPacket {
    private final boolean joinRedTeam;
    
    public JoinTeamC2SPacket(boolean joinRedTeam) {
        this.joinRedTeam = joinRedTeam;
    }
    
    public JoinTeamC2SPacket(FriendlyByteBuf buf) {
        this.joinRedTeam = buf.readBoolean();
    }
    
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(joinRedTeam);
    }
    
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // Get the player who sent the packet
            ServerPlayer player = context.getSender();
            if (player != null) {
                // Add player to team
                if (joinRedTeam) {
                    GameManager.getInstance().getTeamManager().addToRedTeam(player);
                } else {
                    GameManager.getInstance().getTeamManager().addToBlueTeam(player);
                }
            }
        });
        return true;
    }
}
