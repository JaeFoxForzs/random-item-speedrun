package fr.jaeforzs.ris.mixin;

import fr.jaeforzs.ris.RisWorldState;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BedBlock.class)
public class BedBlockMixin {

    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void ris$onUseBed(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (world.isClient()) return;

        MinecraftServer server = world.getServer();
        if (server == null) return;

        RisWorldState risState = RisWorldState.getServerState(server);
        if (!risState.timerRunning || risState.completed) return;

        if ("ris.comp.insomnia".equals(risState.complicationId)) {
            
            world.playSound(
                    null,
                    pos,
                    SoundEvents.ENTITY_VILLAGER_NO,
                    SoundCategory.BLOCKS,
                    1.0f, 1.0f
            );

            cir.setReturnValue(ActionResult.CONSUME);
        }
    }
}