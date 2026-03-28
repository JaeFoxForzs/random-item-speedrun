package fr.jaeforzs.ris;

import fr.jaeforzs.ris.complication.ClientComplicationManager;
import fr.jaeforzs.ris.complication.tools.ButtonShakeManager;
import fr.jaeforzs.ris.complication.tools.HandShakeManager;
import fr.jaeforzs.ris.complication.tools.SliderShakeManager;
import fr.jaeforzs.ris.hud.HudRenderer;
import fr.jaeforzs.ris.input.InputManager;
import fr.jaeforzs.ris.inventory.InventoryTracker;
import fr.jaeforzs.ris.network.ClientNetworkManager;
import fr.jaeforzs.ris.timer.ClientTimerManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpeedrunningARandomItemClient implements ClientModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("RIS/Client");

	@Override
	public void onInitializeClient() {
		LOGGER.info("Initializing Random Item Speedrun (Client)...");

		ClientTimerManager.initialize();
		ClientComplicationManager.initialize();
		ClientNetworkManager.initialize();
		InputManager.initialize();
		InventoryTracker.initialize();
		HudRenderer.initialize();
		SliderShakeManager.initialize();
		ButtonShakeManager.initialize();

		
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			ClientTimerManager.reset();
			ClientComplicationManager.reset();
			InputManager.reset();
			HudRenderer.reset();
			HandShakeManager.reset();
			LOGGER.debug("Client state reset on disconnect");
		});

		LOGGER.info("Random Item Speedrun (Client) initialized!");
	}
}