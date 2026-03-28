package fr.jaeforzs.ris.timer;

import fr.jaeforzs.ris.complication.ClientComplicationManager;
import fr.jaeforzs.ris.network.ClientNetworkManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientTimerManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("RIS/ClientTimer");

    
    private static long accumulatedMs = 0;
    private static long sessionStartMs = 0;
    private static boolean timerRunning = false;
    private static boolean completed = false;

    
    private static long localSyncTime = 0;
    private static long syncedAccumulatedMs = 0;

    
    private static Item targetItem = Items.DIAMOND;

    public static void initialize() {
        LOGGER.info("ClientTimerManager initialized");
    }

    /**
     * Синхронизация с сервером.
     */
    

    public static void syncFromServer(Item item, String complicationId,
                                      long accMs, long sessionMs,
                                      boolean running, boolean done) {
        targetItem = item;
        accumulatedMs = accMs;
        sessionStartMs = sessionMs;
        timerRunning = running;
        completed = done;

        syncedAccumulatedMs = accMs;
        localSyncTime = System.currentTimeMillis();

        ClientComplicationManager.setComplication(complicationId);

        
        if (running && !done) {
            ClientComplicationManager.activateCurrent();
        }

        LOGGER.debug("Synced: item={}, running={}, completed={}, accumulated={}ms",
                item, running, done, accMs);
    }

    /**
     * Запуск таймера (первое движение).
     */
    public static void start() {
        if (timerRunning || completed) {
            return;
        }

        timerRunning = true;
        sessionStartMs = System.currentTimeMillis();
        localSyncTime = sessionStartMs;
        syncedAccumulatedMs = accumulatedMs;

        ClientComplicationManager.activateCurrent();
        ClientNetworkManager.sendStartPacket();

        LOGGER.info("Timer started");
    }

    /**
     * Остановка таймера (победа).
     */
    public static void stop() {
        if (!timerRunning || completed) {
            return;
        }

        long finalTime = getCurrentTimeMs();
        accumulatedMs = finalTime;
        timerRunning = false;
        completed = true;

        ClientComplicationManager.deactivateCurrent();

        MinecraftClient client = MinecraftClient.getInstance();
        client.getSoundManager().play(
                PositionedSoundInstance.ui(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f)
        );

        ClientNetworkManager.sendStopPacket(finalTime);

        LOGGER.info("Timer stopped at {}ms", finalTime);
    }

    /**
     * Текущее время в миллисекундах.
     */
    public static long getCurrentTimeMs() {
        if (completed) {
            return accumulatedMs;
        }
        if (timerRunning) {
            
            long localDelta = System.currentTimeMillis() - localSyncTime;

            
            if (localDelta < 0) {
                localSyncTime = System.currentTimeMillis();
                localDelta = 0;
            }

            return syncedAccumulatedMs + localDelta;
        }
        return accumulatedMs;
    }

    /**
     * Форматированное время.
     */
    public static String getFormattedTime() {
        long ms = getCurrentTimeMs();
        long totalSeconds = ms / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        long millis = ms % 1000;

        if (hours > 0) {
            return String.format("%d:%02d:%02d.%03d", hours, minutes, seconds, millis);
        }
        return String.format("%02d:%02d.%03d", minutes, seconds, millis);
    }

    
    public static boolean isRunning() { return timerRunning; }
    public static boolean isCompleted() { return completed; }
    public static Item getTargetItem() { return targetItem; }

    /**
     * Полный сброс.
     */
    public static void reset() {
        accumulatedMs = 0;
        sessionStartMs = 0;
        syncedAccumulatedMs = 0;
        localSyncTime = 0;
        timerRunning = false;
        completed = false;
        targetItem = Items.DIAMOND;

        LOGGER.debug("Timer reset");
    }
}