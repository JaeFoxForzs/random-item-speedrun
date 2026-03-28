

package fr.jaeforzs.ris.mixin.client;

import fr.jaeforzs.ris.complication.ClientComplicationManager;
import fr.jaeforzs.ris.timer.ClientTimerManager;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Inject(method = "canStartSprinting", at = @At("HEAD"), cancellable = true)
    private void ris$onCanStartSprinting(CallbackInfoReturnable<Boolean> cir) {
        if (!ClientTimerManager.isRunning() || ClientTimerManager.isCompleted()) return;

        if ("ris.comp.snail".equals(ClientComplicationManager.getCurrentComplicationId())) {
            cir.setReturnValue(false);
        }
    }
}