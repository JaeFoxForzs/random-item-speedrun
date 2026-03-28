

package fr.jaeforzs.ris.mixin.client;

import fr.jaeforzs.ris.complication.ClientComplicationManager;
import fr.jaeforzs.ris.timer.ClientTimerManager;
import fr.jaeforzs.ris.inventory.SlotWrapper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {

    @Inject(method = "drawSlot", at = @At("HEAD"), cancellable = true)
    private void ris$onDrawSlot(DrawContext context, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        if (!ClientTimerManager.isRunning() || ClientTimerManager.isCompleted()) return;

        String comp = ClientComplicationManager.getCurrentComplicationId();

        
        if ("ris.comp.holes_in_pockets".equals(comp)) {
            if (ris$isMainInventorySlot(slot)) {
                ris$drawBlockedSlot(context, slot);
                ci.cancel();
                return;
            }
        }

        
        if ("ris.comp.one_armed".equals(comp)) {
            if (ris$isOffhandSlot(slot)) {
                ris$drawBlockedSlot(context, slot);
                ci.cancel();
                return;
            }
        }

        
        if ("ris.comp.fragile_bones".equals(comp)) {
            if (ris$isArmorSlot(slot)) {
                ris$drawBlockedSlot(context, slot);
                ci.cancel();
            }
        }
    }

    @Unique
    private Slot ris$getRealSlot(Slot slot) {
        
        if (slot instanceof SlotWrapper wrapper) {
            return wrapper.ris$getRealSlot();
        }
        return slot;
    }

    @Unique
    private boolean ris$isMainInventorySlot(Slot slot) {
        slot = ris$getRealSlot(slot);
        if (!(slot.inventory instanceof PlayerInventory)) return false;
        int index = slot.getIndex();
        return index >= 9 && index <= 35;
    }

    @Unique
    private boolean ris$isOffhandSlot(Slot slot) {
        slot = ris$getRealSlot(slot);
        if (!(slot.inventory instanceof PlayerInventory)) return false;
        return slot.getIndex() == 40;
    }

    @Unique
    private boolean ris$isArmorSlot(Slot slot) {
        slot = ris$getRealSlot(slot);

        String className = slot.getClass().getSimpleName().toLowerCase();
        if (className.contains("armor")) {
            return true;
        }

        if (slot.inventory instanceof PlayerInventory) {
            int index = slot.getIndex();
            return index >= 36 && index <= 39;
        }

        return false;
    }

    @Unique
    private void ris$drawBlockedSlot(DrawContext context, Slot slot) {
        int x = slot.x;
        int y = slot.y;
        context.fill(x, y, x + 16, y + 16, 0xAA000000);
    }
}