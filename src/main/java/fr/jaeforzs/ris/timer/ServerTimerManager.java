package fr.jaeforzs.ris.timer;

import fr.jaeforzs.ris.RisWorldState;
import fr.jaeforzs.ris.complication.ServerComplicationManager;
import fr.jaeforzs.ris.network.ServerNetworkManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Управляет таймером на сервере.
 * Больше не использует тики — всё на основе System.currentTimeMillis().
 */
public class ServerTimerManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("RIS/ServerTimer");

    public static void initialize() {
        
        ServerLifecycleEvents.SERVER_STOPPING.register(ServerTimerManager::onServerStopping);

        
        ServerLifecycleEvents.SERVER_STARTED.register(ServerTimerManager::onServerStarted);

        LOGGER.info("ServerTimerManager initialized");
    }

    private static void onServerStarted(MinecraftServer server) {
        RisWorldState state = RisWorldState.getServerState(server);

        
        if (state.timerRunning) {
            
            
            state.sessionStartMs = System.currentTimeMillis();
            state.markDirty();
            LOGGER.info("Timer resumed, accumulated: {}ms", state.accumulatedMs);
        }
    }

    private static void onServerStopping(MinecraftServer server) {
        RisWorldState state = RisWorldState.getServerState(server);

        
        if (state.timerRunning && !state.completed) {
            state.accumulatedMs = state.getCurrentTimeMs();
            
            state.markDirty();
            LOGGER.info("Timer paused at {}ms", state.accumulatedMs);
        }
    }

    /**
     * Запускает таймер (первое движение игрока).
     */
    public static void start(MinecraftServer server) {
        RisWorldState state = RisWorldState.getServerState(server);

        if (state.timerRunning || state.completed) {
            return;
        }

        state.sessionStartMs = System.currentTimeMillis();
        state.timerRunning = true;
        state.markDirty();

        ServerComplicationManager.applyToAll(server, state.complicationId, true);
        ServerNetworkManager.broadcastSync(server);

        LOGGER.info("Timer started");
    }

    /**
     * Останавливает таймер (победа).
     */
    public static void stop(MinecraftServer server, long clientTime) {
        RisWorldState state = RisWorldState.getServerState(server);

        if (!state.timerRunning || state.completed) {
            return;
        }

        state.accumulatedMs = clientTime;
        state.timerRunning = false;
        state.completed = true;
        state.markDirty();

        ServerComplicationManager.applyToAll(server, state.complicationId, false);
        ServerNetworkManager.broadcastSync(server);

        LOGGER.info("Timer stopped at {}ms", clientTime);
    }
}