package fr.jaeforzs.ris.complication.impl;

import fr.jaeforzs.ris.complication.Complication;

/**
 * Черепаха - минимальная чувствительность мыши.
 */
public class TurtleComplication implements Complication {
    @Override
    public String getId() {
        return "ris.comp.turtle";
    }

}
