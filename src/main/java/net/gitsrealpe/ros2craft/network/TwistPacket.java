package net.gitsrealpe.ros2craft.network;

import org.joml.Vector3f;

import io.netty.buffer.ByteBuf;
import net.gitsrealpe.ros2craft.ROS2Craft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TwistPacket(int robotId, Vector3f twist_linear, Vector3f twist_angular) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<TwistPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(ROS2Craft.MODID, "twist_update"));

    public static final StreamCodec<ByteBuf, TwistPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            TwistPacket::robotId,
            ByteBufCodecs.VECTOR3F,
            TwistPacket::twist_linear,
            ByteBufCodecs.VECTOR3F,
            TwistPacket::twist_angular,
            TwistPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
