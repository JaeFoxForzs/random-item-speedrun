

package fr.jaeforzs.ris.complication.tools;

/**
 * Управляет эффектом тряски рук при блокировке действий.
 */
public class HandShakeManager {

    private static final int DEFAULT_SHAKE_DURATION = 15;

    private static int shakeTicks = 0;

    /**
     * Запускает эффект тряски на стандартную длительность.
     */
    public static void triggerShake() {
        triggerShake(DEFAULT_SHAKE_DURATION);
    }

    /**
     * Запускает эффект тряски на указанную длительность.
     *
     * @param ticks Длительность в тиках
     */
    public static void triggerShake(int ticks) {
        shakeTicks = Math.max(shakeTicks, ticks);
    }

    /**
     * Вызывается каждый клиентский тик.
     */
    public static void tick() {
        if (shakeTicks > 0) {
            shakeTicks--;
        }
    }

    /**
     * @return Оставшееся количество тиков тряски
     */
    public static int getShakeTicks() {
        return shakeTicks;
    }

    /**
     * @return true, если эффект тряски активен
     */
    public static boolean isShaking() {
        return shakeTicks > 0;
    }

    /**
     * Сбрасывает эффект тряски.
     */
    public static void reset() {
        shakeTicks = 0;
    }
}