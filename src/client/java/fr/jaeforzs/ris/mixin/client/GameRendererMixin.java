package fr.jaeforzs.ris.mixin.client;

import fr.jaeforzs.ris.complication.ClientComplicationManager;
import fr.jaeforzs.ris.timer.ClientTimerManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Pool;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    @Final
    private Pool pool;

    @Shadow
    @Nullable
    private Identifier postProcessorId;

    @Shadow
    private boolean postProcessorEnabled;

    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("RIS/NoirShader");

    @Unique
    private boolean ris$noirActive = false;

    @Unique
    private static final Identifier NOIR_SHADER = Identifier.of("ris", "noir");

    @Inject(method = "tick", at = @At("TAIL"))
    private void ris$tickNoirShader(CallbackInfo ci) {
        boolean shouldBeActive = "ris.comp.noir".equals(ClientComplicationManager.getCurrentComplicationId())
                && ClientTimerManager.isRunning()
                && !ClientTimerManager.isCompleted();

        if (shouldBeActive) {
            if (!ris$noirActive) {
                ris$noirActive = true;
                LOGGER.info("Noir shader enabled");
            }
        } else if (ris$noirActive) {
            ris$noirActive = false;
            LOGGER.info("Noir shader disabled");
        }
    }

    
    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/Pool;decrementLifespan()V"
            )
    )
    private void ris$applyNoirAfterGui(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        if (ris$noirActive && this.client.world != null) {
            PostEffectProcessor noirProcessor = this.client.getShaderLoader()
                    .loadPostEffect(NOIR_SHADER, DefaultFramebufferSet.MAIN_ONLY);

            if (noirProcessor != null) {
                noirProcessor.render(this.client.getFramebuffer(), this.pool);
            }
        }
    }

    
    
    @Inject(method = "onCameraEntitySet", at = @At("HEAD"), cancellable = true)
    private void ris$preventVanillaShaderOnCameraChange(@Nullable Entity entity, CallbackInfo ci) {
        if (ris$noirActive) {
            ci.cancel();
        }
    }
}