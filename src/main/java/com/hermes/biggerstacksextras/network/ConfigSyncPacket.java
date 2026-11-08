package com.hermes.biggerstacksextras.network;

import com.hermes.biggerstacksextras.config.ConfigSync;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** 服务端 → 客户端的配置快照。 */
public record ConfigSyncPacket(CompoundTag data) {

    public static void encode(ConfigSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.data);
    }

    public static ConfigSyncPacket decode(FriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        return new ConfigSyncPacket(tag == null ? new CompoundTag() : tag);
    }

    public static void handle(ConfigSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> ConfigSync.apply(msg.data));
        context.setPacketHandled(true);
    }
}
