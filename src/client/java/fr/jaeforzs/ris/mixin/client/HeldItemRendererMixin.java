

package fr.jaeforzs.ris.mixin.client;

import fr.jaeforzs.ris.complication.ClientComplicationManager;
import fr.jaeforzs.ris.complication.tools.HandShakeManager;
import fr.jaeforzs.ris.timer.ClientTimerManager;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {

    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"), cancellable = true)
    private void ris$onRenderFirstPersonItem(
            AbstractClientPlayerEntity player, float tickProgress, float pitch, Hand hand,
            float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
            OrderedRenderCommandQueue orderedRenderCommandQueue, int light, CallbackInfo ci
    ) {
        boolean isRunning = ClientTimerManager.isRunning() && !ClientTimerManager.isCompleted();

        
        if (isRunning && "ris.comp.one_armed".equals(ClientComplicationManager.getCurrentComplicationId())) {
            
            if (hand == Hand.OFF_HAND) {
                ci.cancel();
                return;
            }
        }

        
        if (HandShakeManager.isShaking()) {
            float time = HandShakeManager.getShakeTicks() - tickProgress;

            float shakeX = (float) Math.sin(time * 3.5f) * 0.05f;
            float shakeY = (float) Math.cos(time * 4.5f) * 0.05f;
            float shakeZ = (float) Math.sin(time * 5.5f) * 0.05f;

            matrices.translate(shakeX, shakeY, shakeZ);
        }
    }
}