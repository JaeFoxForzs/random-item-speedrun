

package fr.jaeforzs.ris.input;

import fr.jaeforzs.ris.timer.ClientTimerManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Отслеживает ввод игрока для автоматического запуска таймера.
 */
public class InputManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("RIS/InputManager");

    private static float lastYaw = Float.NaN;
    private static float lastPitch = Float.NaN;

    /**
     * Инициализирует менеджер и регистрирует события.
     */
    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(InputManager::onClientTick);
        LOGGER.info("InputManager initialized");
    }

    private static void onClientTick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;

        if (player == null) {
            lastYaw = Float.NaN;
            lastPitch = Float.NaN;
            return;
        }

        
        if (ClientTimerManager.isRunning() || ClientTimerManager.isCompleted()) {
            return;
        }

        
        boolean mouseMoved = checkMouseMovement(player);

        
        boolean keyPressed = checkKeyPressed(client);

        if (mouseMoved || keyPressed) {
            ClientTimerManager.start();
        }
    }

    private static boolean checkMouseMovement(ClientPlayerEntity player) {
        if (Float.isNaN(lastYaw)) {
            lastYaw = player.getYaw();
            lastPitch = player.getPitch();
            return false;
        }

        boolean moved = player.getYaw() != lastYaw || player.getPitch() != lastPitch;

        lastYaw = player.getYaw();
        lastPitch = player.getPitch();

        return moved;
    }

    private static boolean checkKeyPressed(MinecraftClient client) {
        return client.options.forwardKey.isPressed() ||
                client.options.backKey.isPressed() ||
                client.options.leftKey.isPressed() ||
                client.options.rightKey.isPressed() ||
                client.options.jumpKey.isPressed() ||
                client.options.attackKey.isPressed() ||
                client.options.useKey.isPressed();
    }

    /**
     * Сбрасывает состояние отслеживания.
     */
    public static void reset() {
        lastYaw = Float.NaN;
        lastPitch = Float.NaN;
    }
}
