package fr.jaeforzs.ris.complication.impl;

import fr.jaeforzs.ris.complication.Complication;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;

/**
 * Главный герой - только от 1 лица.
 */
public class ProtagonistComplication implements Complication {
    @Override
    public String getId() {
        return "ris.comp.protagonist";
    }

    @Override
    public void onActivate() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options != null) {
            client.options.setPerspective(Perspective.FIRST_PERSON);
        }
    }
}