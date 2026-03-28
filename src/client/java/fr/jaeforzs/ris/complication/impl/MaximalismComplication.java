package fr.jaeforzs.ris.complication.impl;

import fr.jaeforzs.ris.complication.Complication;

/**
 * Просторы - мир с большими биомами, нельзя изменить.
 */
public class MaximalismComplication implements Complication {
    @Override
    public String getId() {
        return "ris.comp.maximalism";
    }
}