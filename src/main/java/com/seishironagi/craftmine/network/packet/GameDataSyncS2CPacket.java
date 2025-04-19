package com.seishironagi.craftmine.network.packet;

import com.seishironagi.craftmine.client.ClientGameData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class GameDataSyncS2CPacket {
    private final int remainingSeconds;
    private final boolean gameRunning;
    private final boolean isRedTeam;
    private final ItemStack targetItem;
    private final boolean redTeamWon;

    public GameDataSyncS2CPacket(int remainingSeconds, boolean gameRunning, boolean isRedTeam,
            ItemStack targetItem, boolean redTeamWon) {
        this.remainingSeconds = remainingSeconds;
        this.gameRunning = gameRunning;
        this.isRedTeam = isRedTeam;
        this.targetItem = targetItem;
        this.redTeamWon = redTeamWon;
    }

    public GameDataSyncS2CPacket(FriendlyByteBuf buf) {
        this.remainingSeconds = buf.readInt();
        this.gameRunning = buf.readBoolean();
        this.isRedTeam = buf.readBoolean();
        this.targetItem = buf.readItem();
        this.redTeamWon = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(remainingSeconds);
        buf.writeBoolean(gameRunning);
        buf.writeBoolean(isRedTeam);
        buf.writeItem(targetItem);
        buf.writeBoolean(redTeamWon);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // Update client-side game data
            ClientGameData.setRemainingSeconds(remainingSeconds);
            ClientGameData.setGameRunning(gameRunning);
            ClientGameData.setRedTeam(isRedTeam);
            ClientGameData.setTargetItem(targetItem);

            if (!gameRunning) {
                ClientGameData.setGameResult(redTeamWon);
            }
        });
        return true;
    }
}
