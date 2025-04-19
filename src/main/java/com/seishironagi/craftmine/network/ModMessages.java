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
                                .named(ResourceLocation.tryParse(CraftMine.MOD_ID + ":messages"))
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

                // Register OpenInfoScreenC2SPacket
                net.messageBuilder(OpenInfoScreenC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                                .decoder(OpenInfoScreenC2SPacket::new)
                                .encoder(OpenInfoScreenC2SPacket::toBytes)
                                .consumerMainThread(OpenInfoScreenC2SPacket::handle)
                                .add();

                net.messageBuilder(OpenTeamScreenC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                                .decoder(OpenTeamScreenC2SPacket::new)
                                .encoder(OpenTeamScreenC2SPacket::toBytes)
                                .consumerMainThread(OpenTeamScreenC2SPacket::handle)
                                .add();

                net.messageBuilder(OpenSettingsScreenC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                                .decoder(OpenSettingsScreenC2SPacket::new)
                                .encoder(OpenSettingsScreenC2SPacket::toBytes)
                                .consumerMainThread(OpenSettingsScreenC2SPacket::handle)
                                .add();

                // Register OpenMainMenuC2SPacket
                net.messageBuilder(OpenMainMenuC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                                .decoder(OpenMainMenuC2SPacket::new)
                                .encoder(OpenMainMenuC2SPacket::toBytes)
                                .consumerMainThread(OpenMainMenuC2SPacket::handle)
                                .add();

                // Register the new enhanced game data packet
                net.messageBuilder(GameDataSyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                                .decoder(GameDataSyncS2CPacket::new)
                                .encoder(GameDataSyncS2CPacket::toBytes)
                                .consumerMainThread(GameDataSyncS2CPacket::handle)
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
