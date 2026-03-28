

package fr.jaeforzs.ris.mixin;

import fr.jaeforzs.ris.RisWorldState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {

    @Shadow
    @Final
    public PlayerEntity player;

    /**
     * Блокируем вставку предметов в слоты инвентаря (кроме хотбара)
     * Хотбар: слоты 0-8
     * Основной инвентарь: слоты 9-35
     * Броня: 36-39
     * Offhand: 40
     */
    @Inject(method = "insertStack(ILnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void ris$onInsertStack(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (ris$isInventoryBlocked(slot)) {
            cir.setReturnValue(false);
        }
    }

    /**
     * Блокируем подбор предметов в основной инвентарь
     */
    @Inject(method = "getOccupiedSlotWithRoomForStack", at = @At("HEAD"), cancellable = true)
    private void ris$onGetOccupiedSlot(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (!ris$isComplicationActive()) return;

        
        PlayerInventory inv = (PlayerInventory) (Object) this;
        for (int i = 0; i < 9; i++) {
            if (ris$canStackAddMore(inv.getStack(i), stack)) {
                cir.setReturnValue(i);
                return;
            }
        }
        cir.setReturnValue(-1);
    }

    /**
     * Блокируем поиск пустых слотов в основном инвентаре
     */
    @Inject(method = "getEmptySlot", at = @At("HEAD"), cancellable = true)
    private void ris$onGetEmptySlot(CallbackInfoReturnable<Integer> cir) {
        if (!ris$isComplicationActive()) return;

        
        PlayerInventory inv = (PlayerInventory) (Object) this;
        for (int i = 0; i < 9; i++) {
            if (inv.getStack(i).isEmpty()) {
                cir.setReturnValue(i);
                return;
            }
        }
        cir.setReturnValue(-1);
    }

    private boolean ris$isInventoryBlocked(int slot) {
        
        
        if (slot < 9 || slot > 35) return false;
        return ris$isComplicationActive();
    }

    private boolean ris$isComplicationActive() {
        if (player.getEntityWorld().isClient()) return false;

        MinecraftServer server = player.getEntityWorld().getServer();
        if (server == null) return false;

        RisWorldState state = RisWorldState.getServerState(server);
        return state.timerRunning && !state.completed &&
                "ris.comp.holes_in_pockets".equals(state.complicationId);
    }

    private boolean ris$canStackAddMore(ItemStack existingStack, ItemStack stack) {
        return !existingStack.isEmpty() &&
                ItemStack.areItemsAndComponentsEqual(existingStack, stack) &&
                existingStack.isStackable() &&
                existingStack.getCount() < existingStack.getMaxCount();
    }
}