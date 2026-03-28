package fr.jaeforzs.ris.complication.impl;

import fr.jaeforzs.ris.complication.Complication;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;

/**
 * НПС - только от 3 лица.
 */
public class NpcComplication implements Complication {
    @Override
    public String getId() {
        return "ris.comp.npc";
    }

    @Override
    public void onActivate() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options != null) {
            client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
        }
    }
}