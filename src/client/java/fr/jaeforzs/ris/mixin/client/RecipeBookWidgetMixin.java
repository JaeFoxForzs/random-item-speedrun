

package fr.jaeforzs.ris.mixin.client;

import fr.jaeforzs.ris.complication.ClientComplicationManager;
import fr.jaeforzs.ris.timer.ClientTimerManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeBookWidget.class)
public class RecipeBookWidgetMixin {

    /**
     * Заставляем систему думать, что книга всегда закрыта.
     * Это предотвращает сдвиг интерфейса (инвентарь центрируется) и убирает панель крафтов.
     */
    @Inject(method = "isOpen", at = @At("HEAD"), cancellable = true)
    private void ris$forceClose(CallbackInfoReturnable<Boolean> cir) {
        if (ClientTimerManager.isRunning() && !ClientTimerManager.isCompleted()) {
            if ("ris.comp.ignoramus".equals(ClientComplicationManager.getCurrentComplicationId())) {
                cir.setReturnValue(false);
            }
        }
    }

    /**
     * Блокируем саму отрисовку виджета книги, если он всё же попытается нарисоваться.
     */
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void ris$blockRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (ClientTimerManager.isRunning() && !ClientTimerManager.isCompleted()) {
            if ("ris.comp.ignoramus".equals(ClientComplicationManager.getCurrentComplicationId())) {
                ci.cancel();
            }
        }
    }
}