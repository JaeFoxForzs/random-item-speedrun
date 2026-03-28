package fr.jaeforzs.ris.complication.impl;

import fr.jaeforzs.ris.complication.Complication;

/**
 * Талассофоб - отбрасывает от океанов.
 */
public class ThalassophobeComplication implements Complication {
    @Override
    public String getId() {
        return "ris.comp.thalassophobe";
    }
}