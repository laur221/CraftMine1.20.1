package com.seishironagi.craftmine.network.packet;

import com.seishironagi.craftmine.difficulty.DifficultyManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
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
            ServerPlayer player = context.getSender();
            if (player != null) {
                // Update the difficulty level server-side
                DifficultyManager.getInstance().setDifficulty(difficulty);
            }
        });
        return true;
    }
}
