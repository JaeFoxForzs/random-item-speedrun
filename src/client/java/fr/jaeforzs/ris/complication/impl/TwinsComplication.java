

package fr.jaeforzs.ris.complication.impl;

import fr.jaeforzs.ris.complication.Complication;

/**
 * Двойняшки - нужно собрать 2 целевых предмета.
 */
public class TwinsComplication implements Complication {

    @Override
    public String getId() {
        return "ris.comp.twins";
    }

    @Override
    public int getRequiredItemCount() {
        return 2;
    }
}