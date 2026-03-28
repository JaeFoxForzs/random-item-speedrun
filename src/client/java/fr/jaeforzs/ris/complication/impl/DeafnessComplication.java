package fr.jaeforzs.ris.complication.impl;

import fr.jaeforzs.ris.complication.Complication;
import net.minecraft.client.MinecraftClient;

/**
 * Глухота - звук отключён.
 */
public class DeafnessComplication implements Complication {
    @Override
    public String getId() {
        return "ris.comp.deafness";
    }

    @Override
    public void onActivate() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getSoundManager() != null) {
            client.getSoundManager().stopAll();
        }
    }
}