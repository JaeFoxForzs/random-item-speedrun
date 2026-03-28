

package fr.jaeforzs.ris.mixin.client;

import fr.jaeforzs.ris.complication.ClientComplicationManager;
import fr.jaeforzs.ris.timer.ClientTimerManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public class PlayerEntityModelMixin<T extends BipedEntityRenderState> {

    @Shadow
    @Final
    public ModelPart leftArm;

    @Shadow
    @Final
    public ModelPart rightArm;

    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/BipedEntityRenderState;)V", at = @At("TAIL"))
    private void ris$onSetAngles(T state, CallbackInfo ci) {
        if (!ClientTimerManager.isRunning() || ClientTimerManager.isCompleted()) return;

        if ("ris.comp.one_armed".equals(ClientComplicationManager.getCurrentComplicationId())) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.options == null) return;

            
            Arm mainArm = client.options.getMainArm().getValue();

            
            if (mainArm == Arm.RIGHT) {
                
                this.leftArm.visible = false;
            } else {
                
                this.rightArm.visible = false;
            }
        }
    }
}