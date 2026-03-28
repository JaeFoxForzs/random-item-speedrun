

package fr.jaeforzs.ris.complication.impl;

import fr.jaeforzs.ris.complication.Complication;

/**
 * Дырявые карманы - только хотбар, основной инвентарь недоступен.
 */
public class HolesInPocketsComplication implements Complication {
    @Override
    public String getId() {
        return "ris.comp.holes_in_pockets";
    }
}