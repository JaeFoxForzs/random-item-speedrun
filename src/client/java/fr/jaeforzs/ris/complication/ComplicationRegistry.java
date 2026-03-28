package fr.jaeforzs.ris.complication;

import fr.jaeforzs.ris.complication.impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Реестр всех доступных усложнений.
 */
public class ComplicationRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger("RIS/ComplicationRegistry");
    private static final Map<String, Complication> REGISTRY = new LinkedHashMap<>();
    private static boolean initialized = false;

    /**
     * Инициализирует реестр и регистрирует все усложнения.
     */
    public static void initialize() {
        if (initialized) {
            LOGGER.warn("ComplicationRegistry already initialized!");
            return;
        }

        register(new NoComplication());

        register(new VegetarianComplication());
        register(new CarnivoreComplication());
        register(new TwinsComplication());
        register(new TripletsComplication());
        register(new GiantComplication());
        register(new MidgetComplication());
        register(new TurtleComplication());
        register(new SonicComplication());
        register(new MoleComplication());
        register(new VastnessComplication());
        register(new MaximalismComplication());
        register(new NoirComplication());
        register(new CockeyedComplication());
        register(new AntisocialComplication());
        register(new ProtagonistComplication());
        register(new NpcComplication());
        register(new SnailComplication());
        register(new AccelerationComplication());
        register(new HolesInPocketsComplication());
        register(new HydrophobeComplication());
        register(new ThalassophobeComplication());
        register(new DeafnessComplication());
        register(new InsomniaComplication());
        register(new OneArmedComplication());
        register(new FragileBonesComplication());
        register(new IgnoramusComplication());

        initialized = true;
        LOGGER.info("Registered {} complications", REGISTRY.size());
    }

    /**
     * Регистрирует новое усложнение.
     */
    public static void register(Complication complication) {
        String id = complication.getId();
        if (REGISTRY.containsKey(id)) {
            LOGGER.warn("Complication with id '{}' already registered, overwriting!", id);
        }
        REGISTRY.put(id, complication);
        LOGGER.debug("Registered complication: {}", id);
    }

    /**
     * Получает усложнение по ID.
     *
     * @return Усложнение или NoComplication, если не найдено
     */
    public static Complication get(String id) {
        Complication comp = REGISTRY.get(id);
        if (comp == null) {
            LOGGER.warn("Unknown complication: '{}', using NoComplication", id);
            return REGISTRY.getOrDefault("ris.comp.none", new NoComplication());
        }
        return comp;
    }

    /**
     * @return Неизменяемый список всех ID усложнений
     */
    public static List<String> getAllIds() {
        return List.copyOf(REGISTRY.keySet());
    }

    /**
     * @return Неизменяемая коллекция всех усложнений
     */
    public static Collection<Complication> getAll() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    /**
     * Проверяет, существует ли усложнение с данным ID.
     */
    public static boolean exists(String id) {
        return REGISTRY.containsKey(id);
    }
}