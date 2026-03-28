

package fr.jaeforzs.ris.mixin.client;

import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SimpleOption.OptionSliderWidgetImpl.class)
public interface OptionSliderWidgetAccessor {
    @Accessor("option")
    SimpleOption<?> getOption();
}