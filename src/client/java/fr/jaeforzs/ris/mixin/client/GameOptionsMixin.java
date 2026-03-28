package fr.jaeforzs.ris.mixin.client;

import fr.jaeforzs.ris.complication.ClientComplicationManager;
import fr.jaeforzs.ris.complication.tools.ButtonShakeManager;
import fr.jaeforzs.ris.timer.ClientTimerManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
public class GameOptionsMixin {

    @Shadow
    private Perspective perspective;

    @Inject(method = "setPerspective", at = @At("HEAD"), cancellable = true)
    private void ris$onSetPerspective(Perspective perspective, CallbackInfo ci) {
        if (!ClientTimerManager.isRunning() || ClientTimerManager.isCompleted()) return;

        String comp = ClientComplicationManager.getCurrentComplicationId();
        Perspective requiredPerspective = null;

        if ("ris.comp.protagonist".equals(comp)) {
            requiredPerspective = Perspective.FIRST_PERSON;
        } else if ("ris.comp.npc".equals(comp)) {
            requiredPerspective = Perspective.THIRD_PERSON_BACK;
        }

        if (requiredPerspective == null) return;

        
        if (perspective != requiredPerspective) {
            
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null) {
                client.getSoundManager().play(
                        PositionedSoundInstance.ui(SoundEvents.ENTITY_ITEM_BREAK, 1.0f)
                );
            }

            
            this.perspective = requiredPerspective;

            ci.cancel();
        }
    }
}