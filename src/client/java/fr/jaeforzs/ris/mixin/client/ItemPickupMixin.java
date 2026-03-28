

package fr.jaeforzs.ris.mixin.client;

import fr.jaeforzs.ris.inventory.InventoryTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ItemPickupMixin {

    @Inject(method = "onItemPickupAnimation", at = @At("HEAD"))
    private void onItemPickup(ItemPickupAnimationS2CPacket packet, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;
        if (packet.getCollectorEntityId() != client.player.getId()) return;

        InventoryTracker.checkInventory();
    }
}