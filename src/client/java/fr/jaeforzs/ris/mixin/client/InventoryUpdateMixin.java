

package fr.jaeforzs.ris.mixin.client;

import fr.jaeforzs.ris.inventory.InventoryTracker;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class InventoryUpdateMixin {

    @Inject(method = "onScreenHandlerSlotUpdate", at = @At("TAIL"))
    private void onSlotUpdate(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo ci) {
        InventoryTracker.checkInventory();
    }
}