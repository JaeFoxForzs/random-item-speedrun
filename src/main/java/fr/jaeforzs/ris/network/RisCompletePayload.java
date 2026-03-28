package fr.jaeforzs.ris.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RisCompletePayload(int count) implements CustomPayload {
    public static final Id<RisCompletePayload> ID = new Id<>(Identifier.of("ris", "complete"));

    public static final PacketCodec<RegistryByteBuf, RisCompletePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, RisCompletePayload::count,
            RisCompletePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}