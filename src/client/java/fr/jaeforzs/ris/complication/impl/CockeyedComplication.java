package fr.jaeforzs.ris.complication.impl;

import fr.jaeforzs.ris.complication.Complication;

/**
 * Косоглазый - инверсия осей мыши X и Y.
 */
public class CockeyedComplication implements Complication {
    @Override
    public String getId() {
        return "ris.comp.cockeyed";
    }
}