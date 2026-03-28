

package fr.jaeforzs.ris.mixin.client;

import fr.jaeforzs.ris.complication.ClientComplicationManager;
import fr.jaeforzs.ris.timer.ClientTimerManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TexturedButtonWidget.class)
public class TexturedButtonWidgetMixin {

    @Shadow
    @Final
    protected ButtonTextures textures;

    @Inject(method = "drawIcon", at = @At("HEAD"), cancellable = true)
    private void ris$hideRecipeBookButton(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (!ClientTimerManager.isRunning() || ClientTimerManager.isCompleted()) return;
        if (!"ris.comp.ignoramus".equals(ClientComplicationManager.getCurrentComplicationId())) return;

        TexturedButtonWidget self = (TexturedButtonWidget) (Object) this;

        
        Identifier identifier = this.textures.get(self.isInteractable(), self.isSelected());

        
        if (identifier != null && identifier.getPath().contains("recipe_book")) {
            
            self.visible = false;
            self.active = false;

            
            ci.cancel();
        }
    }
}