package fr.jaeforzs.ris.complication.impl;

import fr.jaeforzs.ris.complication.Complication;

/**
 * Бессонница - нельзя спать.
 */
public class IgnoramusComplication implements Complication {
    @Override
    public String getId() {
        return "ris.comp.ignoramus";
    }
}