package fr.jaeforzs.ris.complication.impl;

import fr.jaeforzs.ris.complication.Complication;

/**
 * Ускорение - тикрейт мира x2 (40 тиков в секунду).
 */
public class AccelerationComplication implements Complication {
    @Override
    public String getId() {
        return "ris.comp.acceleration";
    }
}