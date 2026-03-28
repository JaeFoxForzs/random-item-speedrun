

package fr.jaeforzs.ris.complication;

import fr.jaeforzs.ris.complication.tools.HandShakeManager;
import fr.jaeforzs.ris.timer.ClientTimerManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Управляет активным усложнением на клиенте.
 */
public class ClientComplicationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("RIS/ComplicationManager");

    private static Complication currentComplication = null;
    private static String currentComplicationId = "ris.comp.none";

    /**
     * Инициализирует менеджер и регистрирует события.
     */
    public static void initialize() {
        
        ComplicationRegistry.initialize();

        
        currentComplication = ComplicationRegistry.get(currentComplicationId);

        
        registerEvents();

        LOGGER.info("ClientComplicationManager initialized");
    }

    private static void registerEvents() {
        
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClient()) {
                return ActionResult.PASS;
            }

            
            if (!ClientTimerManager.isRunning() || ClientTimerManager.isCompleted()) {
                return ActionResult.PASS;
            }

            Complication comp = getCurrentComplication();
            ItemStack stack = player.getStackInHand(hand);

            if (comp.isItemUseBlocked(stack, player)) {
                comp.onItemBlocked(player, stack);
                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });

        
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            
            HandShakeManager.tick();

            
            Complication comp = getCurrentComplication();
            if (ClientTimerManager.isRunning() && !ClientTimerManager.isCompleted()) {
                comp.onClientTick();
            }
        });
    }

    /**
     * Устанавливает текущее усложнение по ID.
     */
    public static void setComplication(String complicationId) {
        
        if (currentComplication != null) {
            currentComplication.onDeactivate();
        }

        currentComplicationId = complicationId;
        currentComplication = ComplicationRegistry.get(complicationId);

        LOGGER.debug("Complication set to: {}", complicationId);
    }

    /**
     * Активирует текущее усложнение (вызывается при старте спидрана).
     */
    public static void activateCurrent() {
        if (currentComplication != null) {
            currentComplication.onActivate();
            LOGGER.debug("Activated complication: {}", currentComplicationId);
        }
    }

    /**
     * Деактивирует текущее усложнение (вызывается при завершении спидрана).
     */
    public static void deactivateCurrent() {
        if (currentComplication != null) {
            currentComplication.onDeactivate();
            LOGGER.debug("Deactivated complication: {}", currentComplicationId);
        }
    }

    /**
     * @return Текущее активное усложнение
     */
    public static Complication getCurrentComplication() {
        if (currentComplication == null) {
            currentComplication = ComplicationRegistry.get(currentComplicationId);
        }
        return currentComplication;
    }

    /**
     * @return ID текущего усложнения
     */
    public static String getCurrentComplicationId() {
        return currentComplicationId;
    }

    /**
     * @return Количество предметов, которое нужно собрать
     */
    public static int getRequiredItemCount() {
        return getCurrentComplication().getRequiredItemCount();
    }

    /**
     * Сбрасывает состояние (при выходе из мира).
     */
    public static void reset() {
        if (currentComplication != null) {
            currentComplication.onDeactivate();
        }
        HandShakeManager.reset();
    }
}