

package fr.jaeforzs.ris.mixin;

import fr.jaeforzs.ris.RisWorldState;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EquippableComponent.class)
public class EquippableMixin {

    @Shadow
    @Final
    private EquipmentSlot slot;

    @Inject(method = "equip", at = @At("HEAD"), cancellable = true)
    private void ris$onEquip(ItemStack stack, PlayerEntity player, CallbackInfoReturnable<ActionResult> cir) {
        World world = player.getEntityWorld();
        if (world.isClient()) return;

        MinecraftServer server = world.getServer();
        if (server == null) return;

        RisWorldState state = RisWorldState.getServerState(server);
        if (!state.timerRunning || state.completed) return;

        if ("ris.comp.fragile_bones".equals(state.complicationId)) {
            if (this.slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    serverPlayer.currentScreenHandler.syncState();
                }
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }
}