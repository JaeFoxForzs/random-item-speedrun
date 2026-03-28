
package fr.jaeforzs.ris.mixin.client;

import fr.jaeforzs.ris.PendingCreationData;
import fr.jaeforzs.ris.complication.ClientComplicationManager;
import fr.jaeforzs.ris.gui.AutoCreateLoadingScreen;
import fr.jaeforzs.ris.timer.ClientTimerManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeathScreen.class)
public class DeathScreenMixin extends Screen {

    protected DeathScreenMixin(Text title) { super(title); }

    @Inject(method = "init", at = @At("TAIL"))
    private void ris$addRestartButtons(CallbackInfo ci) {
        if (this.client != null && this.client.isInSingleplayer()) {
            int yRestart = this.height / 4 + 120;
            int yReset = this.height / 4 + 144;

            this.addDrawableChild(ButtonWidget.builder(Text.translatable("ris.button.restart_same_seed"), button -> {
                ris$triggerRestart(true);
            }).dimensions(this.width / 2 - 100, yRestart, 200, 20).build());

            this.addDrawableChild(ButtonWidget.builder(Text.translatable("ris.button.restart_new_seed"), button -> {
                ris$triggerRestart(false);
            }).dimensions(this.width / 2 - 100, yReset, 200, 20).build());
        }
    }

    @Unique
    private void ris$triggerRestart(boolean keepSeed) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getServer() == null) return;

        if (PendingCreationData.getBaseWorldName() == null) {
            String currentName = client.getServer().getSaveProperties().getLevelName();
            
            currentName = currentName.replaceAll("\\s*\\((Attempt|Попытка) \\d+\\)$", "");
            PendingCreationData.setBaseWorldName(currentName);
        }

        PendingCreationData.incrementAttempt();

        
        String attemptSuffix = I18n.translate("ris.world.attempt", PendingCreationData.getAttemptCount());
        String baseName = PendingCreationData.getBaseWorldName();
        String newName = (baseName == null || baseName.trim().isEmpty()) ? attemptSuffix : baseName + " " + attemptSuffix;

        String currentSeed = String.valueOf(client.getServer().getOverworld().getSeed());

        Item targetItem = ClientTimerManager.getTargetItem();
        String targetItemId = Registries.ITEM.getId(targetItem).toString();
        String complicationId = ClientComplicationManager.getCurrentComplicationId();

        PendingCreationData.set(targetItemId, complicationId);
        PendingCreationData.setAutoCreate(true, newName, keepSeed ? currentSeed : null);

        if (client.world != null) {
            client.world.disconnect(Text.empty());
        }
        client.disconnect(Text.empty());
        client.setScreen(new AutoCreateLoadingScreen());
    }
}