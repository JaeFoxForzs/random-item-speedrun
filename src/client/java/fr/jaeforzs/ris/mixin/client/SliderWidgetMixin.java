

package fr.jaeforzs.ris.mixin.client;

import fr.jaeforzs.ris.complication.ClientComplicationManager;
import fr.jaeforzs.ris.complication.tools.SliderShakeManager;
import fr.jaeforzs.ris.timer.ClientTimerManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.OptionSliderWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SliderWidget.class)
public abstract class SliderWidgetMixin extends ClickableWidget.InactivityIndicatingWidget {

    @Shadow
    protected double value;

    @Unique
    private boolean ris$effectActive = false;

    @Unique
    private double ris$lockedValue = -1;

    protected SliderWidgetMixin(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    @Unique
    private SliderWidget ris$self() {
        return (SliderWidget) (Object) this;
    }

    @Unique
    private boolean ris$isOptionBlocked() {
        if (!((Object) this instanceof OptionSliderWidget optionWidget)) return false;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options == null) return false;
        if (!ClientTimerManager.isRunning() || ClientTimerManager.isCompleted()) return false;

        SimpleOption<?> option = ((OptionSliderWidgetAccessor) optionWidget).getOption();
        String comp = ClientComplicationManager.getCurrentComplicationId();
        GameOptions options = client.options;

        
        if (option == options.getMouseSensitivity()) {
            return "ris.comp.turtle".equals(comp) || "ris.comp.sonic".equals(comp);
        }

        
        if (option == options.getViewDistance()) {
            return "ris.comp.mole".equals(comp);
        }

        
        if ("ris.comp.deafness".equals(comp)) {
            for (SoundCategory category : SoundCategory.values()) {
                if (option == options.getSoundVolumeOption(category)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Inject(method = "setValue", at = @At("HEAD"), cancellable = true)
    private void ris$onSetValue(double value, CallbackInfo ci) {
        if (!ris$isOptionBlocked()) {
            ris$lockedValue = -1;
            return;
        }

        if (ris$lockedValue < 0) {
            ris$lockedValue = this.value;
        }

        SliderShakeManager.updateFromSlider(ris$self(), value, ris$lockedValue, this.getWidth());

        this.value = ris$lockedValue;
        ci.cancel();
    }

    @Inject(method = "onClick", at = @At("HEAD"))
    private void ris$onClickHead(Click click, boolean doubled, CallbackInfo ci) {
        if (ris$isOptionBlocked()) {
            ris$lockedValue = this.value;
            SliderShakeManager.startDrag(ris$self());
        }
    }

    @Inject(method = "onRelease", at = @At("HEAD"))
    private void ris$onRelease(Click click, CallbackInfo ci) {
        if (ris$lockedValue >= 0) {
            SliderShakeManager.endDrag(ris$self());
            this.value = ris$lockedValue;
            ris$lockedValue = -1;
        }
    }

    @Inject(method = "onDrag", at = @At("HEAD"), cancellable = true)
    private void ris$onDrag(Click click, double offsetX, double offsetY, CallbackInfo ci) {
        if (ris$isOptionBlocked()) {
            double newValue = (click.x() - (double)(this.getX() + 4)) / (double)(this.width - 8);
            newValue = Math.max(0.0, Math.min(1.0, newValue));
            SliderShakeManager.updateFromSlider(ris$self(), newValue, ris$lockedValue, this.getWidth());
            ci.cancel();
        }
    }

    @Inject(method = "renderWidget", at = @At("HEAD"))
    private void ris$beforeRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ris$effectActive = false;

        if (!((Object) this instanceof OptionSliderWidget)) return;

        if (ris$lockedValue >= 0 && ris$isOptionBlocked()) {
            this.value = ris$lockedValue;
        }

        if (!SliderShakeManager.isActiveSlider(ris$self())) return;

        ris$effectActive = true;
        float offsetX = SliderShakeManager.getOffsetX();

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(offsetX, 0f);
    }

    @Inject(method = "renderWidget", at = @At("RETURN"))
    private void ris$afterRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!ris$effectActive) return;

        float intensity = SliderShakeManager.getResistIntensity();
        if (intensity > 0.01f) {
            int alpha = Math.min(255, (int) (intensity * 255));
            int color = (alpha << 24) | 0xFF4444;

            int x = this.getX();
            int y = this.getY();
            int w = this.getWidth();
            int h = this.getHeight();

            int thickness = 2;

            context.fill(x, y, x + w, y + thickness, color);
            context.fill(x, y + h - thickness, x + w, y + h, color);
            context.fill(x, y + thickness, x + thickness, y + h - thickness, color);
            context.fill(x + w - thickness, y + thickness, x + w, y + h - thickness, color);
        }

        context.getMatrices().popMatrix();

        ris$effectActive = false;
    }
}