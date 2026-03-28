

package fr.jaeforzs.ris.inventory;

import fr.jaeforzs.ris.complication.ClientComplicationManager;
import fr.jaeforzs.ris.timer.ClientTimerManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Отслеживает инвентарь на наличие целевого предмета.
 */
public class InventoryTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger("RIS/InventoryTracker");

    /**
     * Инициализирует трекер.
     */
    public static void initialize() {
        LOGGER.info("InventoryTracker initialized");
    }

    /**
     * Проверяет инвентарь на наличие целевого предмета.
     * Вызывается при изменении слотов инвентаря.
     */
    public static void checkInventory() {
        if (!ClientTimerManager.isRunning() || ClientTimerManager.isCompleted()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        Item targetItem = ClientTimerManager.getTargetItem();
        int requiredCount = ClientComplicationManager.getRequiredItemCount();

        int totalItems = countItems(client.player, targetItem);

        if (totalItems >= requiredCount) {
            LOGGER.info("Goal achieved! Collected {}/{} items", totalItems, requiredCount);
            ClientTimerManager.stop();
        }
    }

    /**
     * Подсчитывает количество указанного предмета в инвентаре игрока.
     */
    private static int countItems(ClientPlayerEntity player, Item targetItem) {
        int total = 0;

        
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == targetItem) {
                total += stack.getCount();
            }
        }

        
        if (player.currentScreenHandler != null) {
            ItemStack cursorStack = player.currentScreenHandler.getCursorStack();
            if (cursorStack.getItem() == targetItem) {
                total += cursorStack.getCount();
            }
        }

        return total;
    }
}