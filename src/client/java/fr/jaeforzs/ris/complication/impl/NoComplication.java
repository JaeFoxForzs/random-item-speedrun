package fr.jaeforzs.ris.complication.impl;

import fr.jaeforzs.ris.complication.Complication;

/**
 * Пустое усложнение (без эффекта).
 */
public class NoComplication implements Complication {

    @Override
    public String getId() {
        return "ris.comp.none";
    }
}