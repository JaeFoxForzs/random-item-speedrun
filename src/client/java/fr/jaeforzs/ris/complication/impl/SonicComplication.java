package fr.jaeforzs.ris.complication.impl;

import fr.jaeforzs.ris.complication.Complication;

/**
 * Соник - максимальная чувствительность мыши.
 */
public class SonicComplication implements Complication {
    @Override
    public String getId() {
        return "ris.comp.sonic";
    }
}