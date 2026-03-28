package fr.jaeforzs.ris.complication.impl;

import fr.jaeforzs.ris.complication.Complication;

/**
 * Гидрофоб - моментальная смерть при касании воды.
 */
public class HydrophobeComplication implements Complication {
    @Override
    public String getId() {
        return "ris.comp.hydrophobe";
    }
}