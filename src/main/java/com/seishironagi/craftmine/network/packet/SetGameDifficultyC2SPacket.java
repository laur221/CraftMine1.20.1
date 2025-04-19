package com.seishironagi.craftmine.network.packet;

import com.seishironagi.craftmine.Config;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SetGameDifficultyC2SPacket {
    private final int difficulty;

    public SetGameDifficultyC2SPacket(int difficulty) {
        this.difficulty = difficulty;
    }

    public SetGameDifficultyC2SPacket(FriendlyByteBuf buf) {
        this.difficulty = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(difficulty);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // Update the game difficulty
            if (difficulty >= 0 && difficulty <= 2) {
                Config.gameDifficulty = difficulty;
            }
        });
        return true;
    }
}
