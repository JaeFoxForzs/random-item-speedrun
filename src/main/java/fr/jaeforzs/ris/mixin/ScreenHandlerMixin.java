package fr.jaeforzs.ris.mixin;

import fr.jaeforzs.ris.RisWorldState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {

    @Inject(method = "onSlotClick", at = @At("HEAD"), cancellable = true)
    private void ris$onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (player.getEntityWorld().isClient()) return;
        if (slotIndex < 0) return;

        MinecraftServer server = player.getEntityWorld().getServer();
        if (server == null) return;

        RisWorldState state = RisWorldState.getServerState(server);
        if (!state.timerRunning || state.completed) return;

        ScreenHandler handler = (ScreenHandler) (Object) this;
        if (slotIndex >= handler.slots.size()) return;

        Slot slot = handler.getSlot(slotIndex);
        String comp = state.complicationId;

        // --- holes_in_pockets ---
        if ("ris.comp.holes_in_pockets".equals(comp)) {
            if (ris$isMainInventorySlot(slot)) {
                ci.cancel();
                return;
            }
        }

        // --- one_armed ---
        if ("ris.comp.one_armed".equals(comp)) {
            // Прямой клик по offhand-слоту в инвентарном экране
            if (ris$isOffhandSlot(slot)) {
                ci.cancel();
                return;
            }

            // Клавиша F над любым слотом: actionType=SWAP, button=40 означает offhand.
            // Minecraft отправляет именно button=40 для свапа с offhand-слотом,
            // тогда как button 0-8 означает своп с соответствующим слотом хотбара.
            if (actionType == SlotActionType.SWAP && button == 40) {
                ci.cancel();
                return;
            }
        }

        // --- fragile_bones ---
        if ("ris.comp.fragile_bones".equals(comp)) {
            if (ris$isArmorSlot(slot)) {
                ci.cancel();
            }
        }
    }

    @Unique
    private boolean ris$isMainInventorySlot(Slot slot) {
        if (!(slot.inventory instanceof PlayerInventory)) return false;
        int index = slot.getIndex();
        return index >= 9 && index <= 35;
    }

    @Unique
    private boolean ris$isOffhandSlot(Slot slot) {
        if (!(slot.inventory instanceof PlayerInventory)) return false;
        return slot.getIndex() == 40;
    }

    @Unique
    private boolean ris$isArmorSlot(Slot slot) {
        if (!(slot.inventory instanceof PlayerInventory)) return false;
        int index = slot.getIndex();
        return index >= 36 && index <= 39;
    }
}