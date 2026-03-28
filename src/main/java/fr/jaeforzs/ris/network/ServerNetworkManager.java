package fr.jaeforzs.ris.network;

import fr.jaeforzs.ris.RisWorldState;
import fr.jaeforzs.ris.complication.ServerComplicationManager;
import fr.jaeforzs.ris.timer.ServerTimerManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerNetworkManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("RIS/ServerNetwork");

    public static void initialize() {
        registerPayloads();
        registerReceivers();
        registerConnectionEvents();

        LOGGER.info("ServerNetworkManager initialized");
    }

    private static void registerPayloads() {
        PayloadTypeRegistry.playS2C().register(RisSyncPayload.ID, RisSyncPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RisStartPayload.ID, RisStartPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RisStopPayload.ID, RisStopPayload.CODEC);
    }

    private static void registerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(RisStartPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerTimerManager.start(context.server());
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(RisStopPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerTimerManager.stop(context.server(), payload.clientTime());
                LOGGER.info("Timer stopped by {} at {}ms",
                        context.player().getName().getString(), payload.clientTime());
            });
        });
    }

    private static void registerConnectionEvents() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            syncToPlayer(handler.player, server);

            RisWorldState state = RisWorldState.getServerState(server);
            ServerComplicationManager.applyComplicationToPlayer(
                    handler.player, state.complicationId, state.timerRunning);
        });
    }

    public static void syncToPlayer(ServerPlayerEntity player, MinecraftServer server) {
        RisSyncPayload payload = createSyncPayload(server);
        ServerPlayNetworking.send(player, payload);
    }

    public static void broadcastSync(MinecraftServer server) {
        RisSyncPayload payload = createSyncPayload(server);

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    private static RisSyncPayload createSyncPayload(MinecraftServer server) {
        RisWorldState state = RisWorldState.getServerState(server);
        return new RisSyncPayload(
                state.targetItemId,
                state.complicationId,
                state.accumulatedMs,
                state.sessionStartMs,
                state.timerRunning,
                state.completed
        );
    }
}