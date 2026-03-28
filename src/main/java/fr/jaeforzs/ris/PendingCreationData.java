
package fr.jaeforzs.ris;

public class PendingCreationData {
    private static String targetItemId = null;
    private static String complicationId = null;

    private static boolean autoCreate = false;
    private static String autoName = null;
    private static String autoSeed = null;
    private static boolean isAutoCreating = false;

    private static String baseWorldName = null;
    private static int attemptCount = 1;

    public static void set(String item, String complication) {
        targetItemId = item;
        complicationId = complication;
    }

    public static void clear() {
        targetItemId = null;
        complicationId = null;
    }

    public static boolean hasData() { return targetItemId != null; }
    public static String getTargetItemId() { return targetItemId; }
    public static String getComplicationId() { return complicationId != null ? complicationId : ""; }
    public static void setComplicationPreview(String complication) { complicationId = complication; }

    public static void setAutoCreate(boolean create, String name, String seed) {
        autoCreate = create;
        autoName = name;
        autoSeed = seed;
        if (create) { isAutoCreating = true; }
    }

    public static boolean isAutoCreate() { return autoCreate; }
    public static String getAutoName() { return autoName; }
    public static String getAutoSeed() { return autoSeed; }
    public static boolean isAutoCreating() { return isAutoCreating; }
    public static void setAutoCreating(boolean val) { isAutoCreating = val; }

    public static void setBaseWorldName(String name) { baseWorldName = name; }
    public static String getBaseWorldName() { return baseWorldName; }

    public static void incrementAttempt() { attemptCount++; }
    public static int getAttemptCount() { return attemptCount; }
}