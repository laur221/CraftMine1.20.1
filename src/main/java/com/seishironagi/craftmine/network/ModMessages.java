package com.seishironagi.craftmine.network;

import com.seishironagi.craftmine.CraftMine;
import com.seishironagi.craftmine.network.packet.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {
    private static SimpleChannel INSTANCE;

    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                // Use ResourceLocation.fromNamespaceAndPath
                .named(ResourceLocation.fromNamespaceAndPath(CraftMine.MODID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;
        
        // Register all packets
        net.messageBuilder(JoinTeamC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(JoinTeamC2SPacket::new)
                .encoder(JoinTeamC2SPacket::toBytes)
                .consumerMainThread(JoinTeamC2SPacket::handle)
                .add();
                
        net.messageBuilder(StartGameC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(StartGameC2SPacket::new)
                .encoder(StartGameC2SPacket::toBytes)
                .consumerMainThread(StartGameC2SPacket::handle)
                .add();
                
        net.messageBuilder(SetGameTimeC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SetGameTimeC2SPacket::new)
                .encoder(SetGameTimeC2SPacket::toBytes)
                .consumerMainThread(SetGameTimeC2SPacket::handle)
                .add();
                
        net.messageBuilder(GameTimerSyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(GameTimerSyncS2CPacket::new)
                .encoder(GameTimerSyncS2CPacket::toBytes)
                .consumerMainThread(GameTimerSyncS2CPacket::handle)
                .add();
    }
    
    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }
    
    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
    
    public static <MSG> void sendToAll(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }
}
