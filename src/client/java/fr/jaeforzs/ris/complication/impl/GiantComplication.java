package fr.jaeforzs.ris.complication.impl;

import fr.jaeforzs.ris.complication.Complication;

/**
 * Гигант - увеличивает рост игрока в 2 раза.
 * (Основная логика работает на сервере в ServerComplicationManager)
 */
public class GiantComplication implements Complication {
    @Override
    public String getId() {
        return "ris.comp.giant";
    }
}