package fr.jaeforzs.ris.complication.tools;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

public class SliderShakeManager {

    private static final float MAX_OFFSET = 40f;
    private static final float SPRING_STIFFNESS = 0.4f;
    private static final float SPRING_DAMPING = 0.55f;
    private static final int RESIST_TICKS = 12;

    
    private static SliderWidget activeSlider = null;

    private static boolean dragging = false;
    private static float currentOffsetX = 0f;
    private static float snapVelocity = 0f;
    private static int resistTicks = 0;

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(SliderShakeManager::tick);
    }

    /**
     * Начинает drag для конкретного слайдера.
     */
    public static void startDrag(SliderWidget slider) {
        activeSlider = slider;
        dragging = true;
        currentOffsetX = 0f;
        snapVelocity = 0f;
    }

    /**
     * Вычисляет rubber band смещение.
     */
    public static void updateFromSlider(SliderWidget slider, double attemptedValue, double lockedValue, int widgetWidth) {
        if (activeSlider != slider) {
            startDrag(slider);
        }
        dragging = true;

        float deltaValue = (float) (attemptedValue - lockedValue);
        float deltaPixels = deltaValue * widgetWidth;

        currentOffsetX = rubberBand(deltaPixels, MAX_OFFSET);
    }

    /**
     * Завершает drag для конкретного слайдера.
     */
    public static void endDrag(SliderWidget slider) {
        if (activeSlider != slider) return;
        if (!dragging) return;

        dragging = false;
        resistTicks = RESIST_TICKS;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && Math.abs(currentOffsetX) > 3f) {
            float pitch = 0.9f + Math.abs(currentOffsetX) / MAX_OFFSET * 0.4f;
            client.getSoundManager().play(
                    PositionedSoundInstance.ui(SoundEvents.ENTITY_ITEM_BREAK, pitch)
            );
        }
    }

    private static float rubberBand(float delta, float maxOffset) {
        if (delta == 0) return 0;
        float sign = Math.signum(delta);
        float abs = Math.abs(delta);
        float result = maxOffset * (1f - 1f / (1f + abs / maxOffset));
        return sign * result;
    }

    private static void tick(MinecraftClient client) {
        if (activeSlider == null) return;

        
        if (!dragging) {
            if (Math.abs(currentOffsetX) > 0.1f || Math.abs(snapVelocity) > 0.1f) {
                snapVelocity += -currentOffsetX * SPRING_STIFFNESS;
                snapVelocity *= SPRING_DAMPING;
                currentOffsetX += snapVelocity;

                if (Math.abs(currentOffsetX) < 0.1f && Math.abs(snapVelocity) < 0.1f) {
                    currentOffsetX = 0f;
                    snapVelocity = 0f;
                }
            }
        }

        if (resistTicks > 0) {
            resistTicks--;
        }

        
        if (!dragging && Math.abs(currentOffsetX) < 0.1f && resistTicks == 0) {
            activeSlider = null;
        }
    }

    /**
     * Проверяет, нужно ли применять эффект к данному слайдеру.
     */
    public static boolean isActiveSlider(SliderWidget slider) {
        return activeSlider == slider && (dragging || Math.abs(currentOffsetX) > 0.1f || resistTicks > 0);
    }

    public static float getOffsetX() {
        return currentOffsetX;
    }

    public static float getResistIntensity() {
        if (dragging) {
            return Math.min(1f, Math.abs(currentOffsetX) / MAX_OFFSET);
        }
        float offsetIntensity = Math.min(1f, Math.abs(currentOffsetX) / MAX_OFFSET);
        float tickIntensity = resistTicks > 0 ? resistTicks / (float) RESIST_TICKS : 0f;
        return Math.max(offsetIntensity, tickIntensity * 0.3f);
    }

    public static void reset() {
        activeSlider = null;
        dragging = false;
        currentOffsetX = 0f;
        snapVelocity = 0f;
        resistTicks = 0;
    }
}