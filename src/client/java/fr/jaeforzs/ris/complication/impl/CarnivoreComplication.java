package fr.jaeforzs.ris.complication.impl;

import fr.jaeforzs.ris.complication.Complication;
import fr.jaeforzs.ris.complication.tools.HandShakeManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundEvents;

/**
 * Хищник - можно есть только мясо.
 */
public class CarnivoreComplication implements Complication {

    @Override
    public String getId() {
        return "ris.comp.carnivore";
    }

    @Override
    public boolean isItemUseBlocked(ItemStack stack, PlayerEntity player) {
        return stack.contains(DataComponentTypes.FOOD) && !stack.isIn(ItemTags.MEAT);
    }

    @Override
    public void onItemBlocked(PlayerEntity player, ItemStack stack) {
        HandShakeManager.triggerShake();

        MinecraftClient client = MinecraftClient.getInstance();
        client.getSoundManager().play(
                PositionedSoundInstance.ui(SoundEvents.ENTITY_VILLAGER_NO, 1.0f)
        );
    }
}