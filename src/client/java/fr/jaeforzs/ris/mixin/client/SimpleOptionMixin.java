

package fr.jaeforzs.ris.mixin.client;

import fr.jaeforzs.ris.complication.ClientComplicationManager;
import fr.jaeforzs.ris.timer.ClientTimerManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SimpleOption.class)
public abstract class SimpleOptionMixin<T> {

    @Inject(method = "getValue", at = @At("HEAD"), cancellable = true)
    private void ris$onGetValue(CallbackInfoReturnable<T> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options == null) return;
        if (!ClientTimerManager.isRunning() || ClientTimerManager.isCompleted()) return;

        String comp = ClientComplicationManager.getCurrentComplicationId();
        GameOptions options = client.options;
        SimpleOption<?> self = (SimpleOption<?>) (Object) this;

        
        if (self == options.getMouseSensitivity()) {
            if ("ris.comp.turtle".equals(comp)) {
                cir.setReturnValue((T) Double.valueOf(0.0));
            } else if ("ris.comp.sonic".equals(comp)) {
                cir.setReturnValue((T) Double.valueOf(1.0));
            }
        }

        
        if (self == options.getViewDistance()) {
            if ("ris.comp.mole".equals(comp)) {
                cir.setReturnValue((T) Integer.valueOf(2));
            }
        }

        
        if ("ris.comp.cockeyed".equals(comp)) {
            if (self == options.getInvertMouseY()) {
                cir.setReturnValue((T) Boolean.TRUE);
            }
            if (self == options.getInvertMouseX()) {
                cir.setReturnValue((T) Boolean.TRUE);
            }
        }

        
        if ("ris.comp.deafness".equals(comp)) {
            if (ris$isSoundOption(options, self)) {
                cir.setReturnValue((T) Double.valueOf(0.0));
            }
        }
    }

    @Unique
    private boolean ris$isSoundOption(GameOptions options, SimpleOption<?> option) {
        for (SoundCategory category : SoundCategory.values()) {
            if (option == options.getSoundVolumeOption(category)) {
                return true;
            }
        }
        return false;
    }
}