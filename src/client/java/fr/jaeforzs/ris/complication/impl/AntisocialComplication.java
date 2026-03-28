package fr.jaeforzs.ris.complication.impl;

import fr.jaeforzs.ris.complication.Complication;

/**
 * Антисоциал - жители отказываются торговать.
 */
public class AntisocialComplication implements Complication {
    @Override
    public String getId() {
        return "ris.comp.antisocial";
    }
}