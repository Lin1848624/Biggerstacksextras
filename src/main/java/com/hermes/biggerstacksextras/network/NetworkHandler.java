package com.hermes.biggerstacksextras.network;

import com.hermes.biggerstacksextras.BiggerStacksExtras;
import com.hermes.biggerstacksextras.config.ConfigSync;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * 只发一种包：把服务端的数值发给客户端。
 * 客户端必须在装上本模组后才能进服（和堆叠上限的判定一致）。
 */
public final class NetworkHandler {

    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            BiggerStacksExtras.id("main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals);

    private NetworkHandler() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, ConfigSyncPacket.class,
                ConfigSyncPacket::encode, ConfigSyncPacket::decode, ConfigSyncPacket::handle);
    }

    public static void syncTo(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ConfigSyncPacket(ConfigSync.snapshot()));
    }

    public static void syncAll() {
        CHANNEL.send(PacketDistributor.ALL.noArg(), new ConfigSyncPacket(ConfigSync.snapshot()));
    }
}
