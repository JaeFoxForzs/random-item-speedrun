package fr.jaeforzs.ris;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;

public class RisWorldState extends PersistentState {
    public String targetItemId;
    public String complicationId;

    
    public long accumulatedMs;     
    public long sessionStartMs;    
    public boolean timerRunning;   
    public boolean completed;      

    public static final Codec<RisWorldState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("targetItemId", "minecraft:diamond").forGetter(s -> s.targetItemId),
            Codec.STRING.optionalFieldOf("complicationId", "ris.comp.none").forGetter(s -> s.complicationId),
            Codec.LONG.optionalFieldOf("accumulatedMs", 0L).forGetter(s -> s.accumulatedMs),
            Codec.LONG.optionalFieldOf("sessionStartMs", 0L).forGetter(s -> s.sessionStartMs),
            Codec.BOOL.optionalFieldOf("timerRunning", false).forGetter(s -> s.timerRunning),
            Codec.BOOL.optionalFieldOf("completed", false).forGetter(s -> s.completed)
    ).apply(instance, RisWorldState::new));

    private static final PersistentStateType<RisWorldState> STATE_TYPE = new PersistentStateType<>(
            SpeedrunningARandomItem.MOD_ID + "_state",
            RisWorldState::new,
            CODEC,
            null
    );

    public RisWorldState() {
        this("minecraft:diamond", "ris.comp.none", 0L, 0L, false, false);
    }

    public RisWorldState(String targetItemId, String complicationId,
                         Long accumulatedMs, Long sessionStartMs,
                         Boolean timerRunning, Boolean completed) {
        this.targetItemId = targetItemId != null ? targetItemId : "minecraft:diamond";
        this.complicationId = complicationId != null ? complicationId : "ris.comp.none";
        this.accumulatedMs = accumulatedMs != null ? accumulatedMs : 0L;
        this.sessionStartMs = sessionStartMs != null ? sessionStartMs : 0L;
        this.timerRunning = timerRunning != null && timerRunning;
        this.completed = completed != null && completed;
    }

    public static RisWorldState getServerState(MinecraftServer server) {
        PersistentStateManager manager = server.getOverworld().getPersistentStateManager();
        return manager.getOrCreate(STATE_TYPE);
    }

    /**
     * Получить текущее время таймера.
     */
    public long getCurrentTimeMs() {
        if (completed || !timerRunning) {
            return accumulatedMs;
        }
        return accumulatedMs + (System.currentTimeMillis() - sessionStartMs);
    }

    /**
     * Пауза таймера (при выходе из мира).
     */
    public void pause() {
        if (timerRunning && !completed) {
            accumulatedMs = getCurrentTimeMs();
            timerRunning = false;
            sessionStartMs = 0;
            markDirty();
        }
    }

    /**
     * Возобновление таймера (при заходе в мир).
     */
    public void resume() {
        if (!timerRunning && !completed && accumulatedMs > 0) {
            sessionStartMs = System.currentTimeMillis();
            timerRunning = true;
            markDirty();
        }
    }
}