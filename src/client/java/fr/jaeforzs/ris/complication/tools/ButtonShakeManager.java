package fr.jaeforzs.ris.complication.tools;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

public class ButtonShakeManager {

    private static final int SHAKE_DURATION_TICKS = 20; 

    private static int shakeTicks = 0;
    private static long shakeStartTime = 0;

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(ButtonShakeManager::tick);
    }

    /**
     * Запускает эффект тряски.
     */
    public static void triggerShake() {
        shakeTicks = SHAKE_DURATION_TICKS;
        shakeStartTime = System.currentTimeMillis();
    }

    private static void tick(MinecraftClient client) {
        if (shakeTicks > 0) {
            shakeTicks--;
        }
    }

    /**
     * Проверяет, активен ли эффект.
     */
    public static boolean isShaking() {
        return shakeTicks > 0;
    }

    /**
     * Возвращает смещение X для тряски.
     */
    public static float getShakeOffsetX() {
        if (shakeTicks <= 0) return 0f;

        long elapsed = System.currentTimeMillis() - shakeStartTime;
        float progress = 1f - (shakeTicks / (float) SHAKE_DURATION_TICKS);

        
        float decay = 1f - progress;
        float shake = (float) Math.sin(elapsed * 0.05) * 3f * decay;

        return shake;
    }

    /**
     * Возвращает интенсивность красной обводки (0..1).
     */
    public static float getBorderIntensity() {
        if (shakeTicks <= 0) return 0f;
        return shakeTicks / (float) SHAKE_DURATION_TICKS;
    }

    public static void reset() {
        shakeTicks = 0;
    }
}