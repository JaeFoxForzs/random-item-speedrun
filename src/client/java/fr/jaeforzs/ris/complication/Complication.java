

package fr.jaeforzs.ris.complication;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

/**
 * Базовый интерфейс для всех усложнений спидрана.
 */
public interface Complication {

    /**
     * @return Уникальный идентификатор усложнения (например, "ris.comp.vegetarian")
     */
    String getId();

    /**
     * @return Количество предметов, которое нужно собрать для победы
     */
    default int getRequiredItemCount() {
        return 1;
    }

    /**
     * Проверяет, заблокировано ли использование предмета этим усложнением.
     *
     * @param stack Предмет, который игрок пытается использовать
     * @param player Игрок
     * @return true, если использование запрещено
     */
    default boolean isItemUseBlocked(ItemStack stack, PlayerEntity player) {
        return false;
    }

    /**
     * Вызывается, когда использование предмета заблокировано.
     * Можно использовать для визуальных/звуковых эффектов.
     *
     * @param player Игрок
     * @param stack Заблокированный предмет
     */
    default void onItemBlocked(PlayerEntity player, ItemStack stack) {
        
    }

    /**
     * Вызывается каждый клиентский тик, пока усложнение активно.
     */
    default void onClientTick() {
        
    }

    /**
     * Вызывается при активации усложнения (старт спидрана).
     */
    default void onActivate() {
        
    }

    /**
     * Вызывается при деактивации усложнения (конец спидрана).
     */
    default void onDeactivate() {
        
    }
}