package fr.jaeforzs.ris.complication.impl;

import fr.jaeforzs.ris.complication.Complication;

/**
 * Хрупкие кости - нельзя носить броню.
 */
public class FragileBonesComplication implements Complication {
    @Override
    public String getId() {
        return "ris.comp.fragile_bones";
    }
}