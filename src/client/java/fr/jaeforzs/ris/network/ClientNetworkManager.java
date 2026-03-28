package fr.jaeforzs.ris.network;

import fr.jaeforzs.ris.timer.ClientTimerManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientNetworkManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("RIS/ClientNetwork");

    public static void initialize() {
        registerReceivers();
        LOGGER.info("ClientNetworkManager initialized");
    }

    private static void registerReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(RisSyncPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                try {
                    Item item = parseItem(payload.targetItemId());

                    ClientTimerManager.syncFromServer(
                            item,
                            payload.complicationId(),
                            payload.accumulatedMs(),
                            payload.sessionStartMs(),
                            payload.timerRunning(),
                            payload.completed()
                    );
                } catch (Exception e) {
                    LOGGER.error("Failed to process sync payload", e);
                }
            });
        });
    }

    private static Item parseItem(String itemId) {
        Identifier id = Identifier.tryParse(itemId);
        if (id == null || !Registries.ITEM.containsId(id)) {
            LOGGER.warn("Invalid item ID: '{}', using diamond", itemId);
            return Items.DIAMOND;
        }
        return Registries.ITEM.get(id);
    }

    public static void sendStartPacket() {
        try {
            ClientPlayNetworking.send(new RisStartPayload());
            LOGGER.debug("Sent start packet");
        } catch (Exception e) {
            LOGGER.error("Failed to send start packet", e);
        }
    }

    public static void sendStopPacket(long timeMs) {
        try {
            ClientPlayNetworking.send(new RisStopPayload(timeMs));
            LOGGER.debug("Sent stop packet with time {}ms", timeMs);
        } catch (Exception e) {
            LOGGER.error("Failed to send stop packet", e);
        }
    }
}