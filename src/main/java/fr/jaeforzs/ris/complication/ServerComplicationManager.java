package fr.jaeforzs.ris.complication;

import fr.jaeforzs.ris.RisWorldState;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class ServerComplicationManager {

    private static final float DEFAULT_TICK_RATE = 20.0f;
    private static final float ACCELERATED_TICK_RATE = 40.0f;

    public static void initialize() {
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            MinecraftServer server = newPlayer.getEntityWorld().getServer();

            assert server != null;
            RisWorldState state = RisWorldState.getServerState(server);
            applyComplicationToPlayer(newPlayer, state.complicationId, state.timerRunning);
        });
    }

    public static void applyComplicationToPlayer(ServerPlayerEntity player, String complicationId, boolean isActive) {
        EntityAttributeInstance scaleAttr = player.getAttributeInstance(EntityAttributes.SCALE);
        if (scaleAttr != null) {
            if (isActive) {
                switch (complicationId) {
                    case "ris.comp.giant" -> scaleAttr.setBaseValue(2.0);
                    case "ris.comp.midget" -> scaleAttr.setBaseValue(0.5);
                    default -> scaleAttr.setBaseValue(1.0);
                }
            } else {
                scaleAttr.setBaseValue(1.0);
            }
        }
    }

    public static void applyToAll(MinecraftServer server, String complicationId, boolean isActive) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            applyComplicationToPlayer(player, complicationId, isActive);
        }

        
        applyTickRate(server, complicationId, isActive);
    }

    private static void applyTickRate(MinecraftServer server, String complicationId, boolean isActive) {
        if (isActive && "ris.comp.acceleration".equals(complicationId)) {
            server.getTickManager().setTickRate(ACCELERATED_TICK_RATE);
        } else {
            server.getTickManager().setTickRate(DEFAULT_TICK_RATE);
        }
    }
}