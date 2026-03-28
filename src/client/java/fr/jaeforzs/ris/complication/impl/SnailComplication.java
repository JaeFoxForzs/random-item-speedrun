package fr.jaeforzs.ris.complication.impl;

import fr.jaeforzs.ris.complication.Complication;
import net.minecraft.client.MinecraftClient;

/**
 * Улитка - нельзя бегать (спринт отключён).
 */
public class SnailComplication implements Complication {
    @Override
    public String getId() {
        return "ris.comp.snail";
    }

    @Override
    public void onActivate() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && client.player.isSprinting()) {
            client.player.setSprinting(false);
        }
    }
}