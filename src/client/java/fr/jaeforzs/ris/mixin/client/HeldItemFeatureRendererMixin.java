

package fr.jaeforzs.ris.mixin.client;

import fr.jaeforzs.ris.complication.ClientComplicationManager;
import fr.jaeforzs.ris.timer.ClientTimerManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.state.ArmedEntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemFeatureRenderer.class)
public class HeldItemFeatureRendererMixin {

    @Inject(method = "renderItem", at = @At("HEAD"), cancellable = true)
    private void ris$onRenderItem(ArmedEntityRenderState entityState, ItemRenderState itemState, ItemStack stack,
                                  Arm arm, MatrixStack matrices, OrderedRenderCommandQueue queue,
                                  int light, CallbackInfo ci) {
        if (!ClientTimerManager.isRunning() || ClientTimerManager.isCompleted()) return;

        if ("ris.comp.one_armed".equals(ClientComplicationManager.getCurrentComplicationId())) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.options == null) return;

            
            Arm mainArm = client.options.getMainArm().getValue();

            
            Arm offhandArm = (mainArm == Arm.RIGHT) ? Arm.LEFT : Arm.RIGHT;

            if (arm == offhandArm) {
                ci.cancel();
            }
        }
    }
}