

package fr.jaeforzs.ris.mixin.client;

import fr.jaeforzs.ris.inventory.SlotWrapper;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen$CreativeSlot")
public class CreativeSlotMixin implements SlotWrapper {

    @Shadow
    @Final
    public Slot slot;

    @Override
    public Slot ris$getRealSlot() {
        return this.slot;
    }
}