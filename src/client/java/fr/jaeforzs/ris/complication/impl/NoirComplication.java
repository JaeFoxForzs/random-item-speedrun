package fr.jaeforzs.ris.complication.impl;

import fr.jaeforzs.ris.complication.Complication;

/**
 * Нуар - ЧБ Видение (Включает кастомный шейдер).
 */
public class NoirComplication implements Complication {

    @Override
    public String getId() {
        return "ris.comp.noir";
    }
}