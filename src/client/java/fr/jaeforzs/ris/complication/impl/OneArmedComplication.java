package fr.jaeforzs.ris.complication.impl;

import fr.jaeforzs.ris.complication.Complication;

/**
 * Однорукий - вторая рука отключена.
 */
public class OneArmedComplication implements Complication {
    @Override
    public String getId() {
        return "ris.comp.one_armed";
    }
}