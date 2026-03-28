
package fr.jaeforzs.ris.mixin.client;

import fr.jaeforzs.ris.PendingCreationData;
import fr.jaeforzs.ris.RandomTab;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends Screen {

    @Shadow @Final public WorldCreator worldCreator;
    @Shadow protected abstract void createLevel();

    @Unique private RandomTab ris$randomTab;
    @Unique private WorldCreator.WorldType ris$savedWorldType = null;
    @Unique private String ris$lastComplication = "";

    protected CreateWorldScreenMixin(Text title) { super(title); }

    @ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TabNavigationWidget$Builder;tabs([Lnet/minecraft/client/gui/tab/Tab;)Lnet/minecraft/client/gui/widget/TabNavigationWidget$Builder;"), index = 0)
    private Tab[] ris$addCustomTab(Tab[] originalTabs) {
        Tab[] newTabs = new Tab[originalTabs.length + 1];
        System.arraycopy(originalTabs, 0, newTabs, 0, originalTabs.length);
        if (this.ris$randomTab == null) { this.ris$randomTab = new RandomTab(); }
        newTabs[newTabs.length - 1] = this.ris$randomTab;
        return newTabs;
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void ris$onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.worldCreator == null) return;

        
        if (PendingCreationData.isAutoCreate()) {

            
            String nameToSet = PendingCreationData.getAutoName();
            String seedToSet = PendingCreationData.getAutoSeed();

            
            PendingCreationData.setAutoCreate(false, null, null);

            
            if (nameToSet != null) {
                this.worldCreator.setWorldName(nameToSet);
            }

            if (seedToSet != null) {
                this.worldCreator.setSeed(seedToSet); 
            } else {
                this.worldCreator.setSeed(""); 
            }

            this.worldCreator.setGameMode(WorldCreator.Mode.SURVIVAL);

            this.createLevel();

            ci.cancel();
            return;
        }

        
        String currentComp = PendingCreationData.getComplicationId();
        if (currentComp == null) currentComp = "";

        if (!currentComp.equals(ris$lastComplication)) {
            ris$handleComplicationChange(currentComp);
            ris$lastComplication = currentComp;
        }
    }

    @Inject(method = "createLevel", at = @At("HEAD"))
    private void ris$onCreateLevel(CallbackInfo ci) {
        if (PendingCreationData.isAutoCreating()) {
            PendingCreationData.setAutoCreating(false);
            return;
        }
        if (this.ris$randomTab != null) {
            PendingCreationData.set(
                    Registries.ITEM.getId(this.ris$randomTab.getCurrentItem()).toString(),
                    this.ris$randomTab.getCurrentComplication()
            );
        }
    }

    @Unique
    private void ris$handleComplicationChange(String newComp) {
        boolean wasOverride = ris$isWorldTypeOverride(ris$lastComplication);
        boolean isOverride = ris$isWorldTypeOverride(newComp);

        if (isOverride) {
            if (!wasOverride) { ris$savedWorldType = this.worldCreator.getWorldType(); }
            WorldCreator.WorldType requiredType = ris$findRequiredWorldType(newComp);
            if (requiredType != null) { this.worldCreator.setWorldType(requiredType); }
        } else if (wasOverride) {
            if (ris$savedWorldType != null) {
                this.worldCreator.setWorldType(ris$savedWorldType);
                ris$savedWorldType = null;
            }
        }
    }

    @Unique
    private boolean ris$isWorldTypeOverride(String comp) {
        return "ris.comp.vastness".equals(comp) || "ris.comp.maximalism".equals(comp);
    }

    @Unique
    private WorldCreator.WorldType ris$findRequiredWorldType(String comp) {
        String targetId = "ris.comp.vastness".equals(comp) ? "large_biomes" : "ris.comp.maximalism".equals(comp) ? "amplified" : "";
        if (targetId.isEmpty()) return null;

        for (WorldCreator.WorldType type : this.worldCreator.getNormalWorldTypes()) {
            if (type.preset() != null) {
                String id = type.preset().getKey().map(key -> key.getValue().toString()).orElse("");
                if (id.contains(targetId)) return type;
            }
        }
        return null;
    }
}