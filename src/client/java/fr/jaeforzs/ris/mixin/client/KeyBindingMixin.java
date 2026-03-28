package fr.jaeforzs.ris.mixin.client;

import fr.jaeforzs.ris.complication.ClientComplicationManager;
import fr.jaeforzs.ris.timer.ClientTimerManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyBinding.class)
public class KeyBindingMixin {

    /**
     * wasPressed() вызывается из handleInputEvents каждый кадр.
     * Если возвращает true — действие выполняется (в т.ч. swap offhand).
     * Возвращаем false для swapHandsKey когда активно one_armed.
     */
    @Inject(method = "wasPressed", at = @At("HEAD"), cancellable = true)
    private void ris$onWasPressed(CallbackInfoReturnable<Boolean> cir) {
        if (!ClientTimerManager.isRunning() || ClientTimerManager.isCompleted()) return;
        if (!"ris.comp.one_armed".equals(ClientComplicationManager.getCurrentComplicationId())) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options == null) return;

        KeyBinding self = (KeyBinding) (Object) this;
        if (self == client.options.swapHandsKey) {
            cir.setReturnValue(false);
        }
    }
}