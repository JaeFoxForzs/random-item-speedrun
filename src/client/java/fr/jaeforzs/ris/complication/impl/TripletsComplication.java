package fr.jaeforzs.ris.complication.impl;

import fr.jaeforzs.ris.complication.Complication;

/**
 * Тройняшки - нужно собрать 3 целевых предмета.
 */
public class TripletsComplication implements Complication {

    @Override
    public String getId() {
        return "ris.comp.triplets";
    }

    @Override
    public int getRequiredItemCount() {
        return 3;
    }
}