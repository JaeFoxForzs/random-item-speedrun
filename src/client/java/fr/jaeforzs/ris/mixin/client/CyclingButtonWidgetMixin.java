package fr.jaeforzs.ris.mixin.client;

import fr.jaeforzs.ris.PendingCreationData;
import fr.jaeforzs.ris.complication.ClientComplicationManager;
import fr.jaeforzs.ris.complication.tools.ButtonShakeManager;
import fr.jaeforzs.ris.timer.ClientTimerManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CyclingButtonWidget.class)
public abstract class CyclingButtonWidgetMixin<T> extends PressableWidget {

    @Shadow
    public abstract T getValue();

    @Shadow
    @Final
    private Text optionText;

    @Unique
    private boolean ris$effectActive = false;

    protected CyclingButtonWidgetMixin(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    /**
     * Перехватываем цикл (переключение значений) и блокируем если нужно
     */
    @Inject(method = "cycle", at = @At("HEAD"), cancellable = true)
    private void ris$onCycle(int amount, CallbackInfo ci) {
        if (ris$shouldBlockInteraction()) {
            ris$playBlockedSound();
            ButtonShakeManager.triggerShake();
            ci.cancel();
        }
    }

    /**
     * Блокируем setValue для полной защиты
     */
    @Inject(method = "setValue", at = @At("HEAD"), cancellable = true)
    private void ris$onSetValue(T value, CallbackInfo ci) {
        if (ris$shouldBlockInteraction()) {
            
            T currentValue = this.getValue();
            if (currentValue instanceof WorldCreator.WorldType && value instanceof WorldCreator.WorldType) {
                if (!ris$isMatchingRequiredType((WorldCreator.WorldType) value)) {
                    ci.cancel();
                }
                return;
            }
            ci.cancel();
        }
    }

    /**
     * Блокируем скролл мыши
     */
    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void ris$onMouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
        if (ris$shouldBlockInteraction()) {
            ris$playBlockedSound();
            ButtonShakeManager.triggerShake();
            cir.setReturnValue(true);
        }
    }

    /**
     * Проверяет, нужно ли блокировать взаимодействие с этой кнопкой
     */
    @Unique
    private boolean ris$shouldBlockInteraction() {
        T value = this.getValue();

        
        if (value instanceof WorldCreator.WorldType) {
            return ris$isWorldTypeOverrideActive();
        }

        
        if (value instanceof Boolean) {
            return ris$isMouseInversionBlocked();
        }

        return false;
    }

    /**
     * Проверяет, заблокирована ли кнопка инверсии мыши
     */
    @Unique
    private boolean ris$isMouseInversionBlocked() {
        if (!ClientTimerManager.isRunning() || ClientTimerManager.isCompleted()) return false;

        String comp = ClientComplicationManager.getCurrentComplicationId();
        if (!"ris.comp.cockeyed".equals(comp)) return false;

        
        String optionName = this.optionText.getString().toLowerCase();
        return optionName.contains("invert") ||
                optionName.contains("инверс") ||
                optionName.contains("инвертир");
    }

    @Unique
    private boolean ris$isWorldTypeOverrideActive() {
        String comp = PendingCreationData.getComplicationId();
        return "ris.comp.vastness".equals(comp) || "ris.comp.maximalism".equals(comp);
    }

    @Unique
    private boolean ris$isMatchingRequiredType(WorldCreator.WorldType type) {
        if (type.preset() == null) return false;
        String comp = PendingCreationData.getComplicationId();
        String id = type.preset().getKey()
                .map(key -> key.getValue().toString())
                .orElse("");

        if ("ris.comp.vastness".equals(comp)) return id.contains("large_biomes");
        if ("ris.comp.maximalism".equals(comp)) return id.contains("amplified");

        return true;
    }

    @Unique
    private void ris$playBlockedSound() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            client.getSoundManager().play(
                    PositionedSoundInstance.ui(SoundEvents.ENTITY_ITEM_BREAK, 1.0f)
            );
        }
    }

    @Inject(method = "drawIcon", at = @At("HEAD"))
    private void ris$beforeDrawIcon(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        ris$effectActive = false;

        if (!ButtonShakeManager.isShaking()) return;
        if (!ris$shouldBlockInteraction()) return;

        ris$effectActive = true;
        float offsetX = ButtonShakeManager.getShakeOffsetX();
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(offsetX, 0f);
    }

    @Inject(method = "drawIcon", at = @At("RETURN"))
    private void ris$afterDrawIcon(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (!ris$effectActive) return;

        
        float intensity = ButtonShakeManager.getBorderIntensity();
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