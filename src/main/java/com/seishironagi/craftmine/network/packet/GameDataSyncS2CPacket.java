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
    private final int gameDifficulty;
    private final int itemTimeMinutes;

    public GameDataSyncS2CPacket(int remainingSeconds, boolean gameRunning, boolean isRedTeam,
            ItemStack targetItem, boolean redTeamWon) {
        this(remainingSeconds, gameRunning, isRedTeam, targetItem, redTeamWon, 1, 10);
    }

    public GameDataSyncS2CPacket(int remainingSeconds, boolean gameRunning, boolean isRedTeam,
            ItemStack targetItem, boolean redTeamWon, int gameDifficulty, int itemTimeMinutes) {
        this.remainingSeconds = remainingSeconds;
        this.gameRunning = gameRunning;
        this.isRedTeam = isRedTeam;
        this.targetItem = targetItem;
        this.redTeamWon = redTeamWon;
        this.gameDifficulty = gameDifficulty;
        this.itemTimeMinutes = itemTimeMinutes;
    }

    public GameDataSyncS2CPacket(FriendlyByteBuf buf) {
        this.remainingSeconds = buf.readInt();
        this.gameRunning = buf.readBoolean();
        this.isRedTeam = buf.readBoolean();
        this.targetItem = buf.readItem();
        this.redTeamWon = buf.readBoolean();
        this.gameDifficulty = buf.readInt();
        this.itemTimeMinutes = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(remainingSeconds);
        buf.writeBoolean(gameRunning);
        buf.writeBoolean(isRedTeam);
        buf.writeItem(targetItem);
        buf.writeBoolean(redTeamWon);
        buf.writeInt(gameDifficulty);
        buf.writeInt(itemTimeMinutes);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // Always update running state and timer
            ClientGameData.setGameRunning(gameRunning);
            ClientGameData.setRemainingSeconds(remainingSeconds);
            ClientGameData.setRedTeam(isRedTeam);
            ClientGameData.setTargetItem(targetItem);

            // Only override client‐side difficulty/time if a game is active
            if (gameRunning) {
                ClientGameData.setGameDifficulty(gameDifficulty);
                ClientGameData.setItemTimeMinutes(itemTimeMinutes);
            }
        });
        return true;
    }
}
