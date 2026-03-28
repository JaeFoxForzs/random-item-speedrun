package fr.jaeforzs.ris.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RisSyncPayload(
        String targetItemId,
        String complicationId,
        long accumulatedMs,
        long sessionStartMs,
        boolean timerRunning,
        boolean completed
) implements CustomPayload {

    public static final Id<RisSyncPayload> ID = new Id<>(Identifier.of("ris", "sync"));

    public static final PacketCodec<RegistryByteBuf, RisSyncPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, RisSyncPayload::targetItemId,
            PacketCodecs.STRING, RisSyncPayload::complicationId,
            PacketCodecs.VAR_LONG, RisSyncPayload::accumulatedMs,
            PacketCodecs.VAR_LONG, RisSyncPayload::sessionStartMs,
            PacketCodecs.BOOLEAN, RisSyncPayload::timerRunning,
            PacketCodecs.BOOLEAN, RisSyncPayload::completed,
            RisSyncPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}