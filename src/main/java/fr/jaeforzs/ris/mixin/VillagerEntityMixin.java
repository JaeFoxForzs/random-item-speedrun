package fr.jaeforzs.ris.mixin;

import fr.jaeforzs.ris.RisWorldState;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerEntity.class)
public class VillagerEntityMixin {

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void ris$onInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        VillagerEntity villager = (VillagerEntity) (Object) this;

        
        MinecraftServer server = villager.getEntityWorld().getServer();
        if (server == null) return;

        
        RisWorldState state = RisWorldState.getServerState(server);

        
        if (state.timerRunning && !state.completed &&
                "ris.comp.antisocial".equals(state.complicationId)) {

            
            villager.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1.0f, 1.0f);

            
            villager.setHeadRollingTimeLeft(40);

            
            cir.setReturnValue(ActionResult.CONSUME);
        }
    }
}