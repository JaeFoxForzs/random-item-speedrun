package fr.jaeforzs.ris.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RisStartPayload() implements CustomPayload {
    public static final CustomPayload.Id<RisStartPayload> ID = new CustomPayload.Id<>(Identifier.of("ris", "start"));
    public static final PacketCodec<RegistryByteBuf, RisStartPayload> CODEC = PacketCodec.unit(new RisStartPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}