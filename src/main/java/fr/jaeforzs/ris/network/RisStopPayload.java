package fr.jaeforzs.ris.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RisStopPayload(long clientTime) implements CustomPayload {
    public static final Id<RisStopPayload> ID = new Id<>(Identifier.of("ris", "stop"));

    public static final PacketCodec<RegistryByteBuf, RisStopPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_LONG, RisStopPayload::clientTime,
            RisStopPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}