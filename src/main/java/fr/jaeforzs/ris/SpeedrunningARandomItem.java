package fr.jaeforzs.ris;

import fr.jaeforzs.ris.complication.ServerComplicationManager;
import fr.jaeforzs.ris.network.ServerNetworkManager;
import fr.jaeforzs.ris.timer.ServerTimerManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpeedrunningARandomItem implements ModInitializer {

	public static final String MOD_ID = "ris";
	public static final Logger LOGGER = LoggerFactory.getLogger("RIS");

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Random Item Speedrun...");

		ServerNetworkManager.initialize();
		ServerTimerManager.initialize();
		ServerComplicationManager.initialize();

		registerItemRegistryReloader();
		registerServerEvents();

		LOGGER.info("Random Item Speedrun initialized!");
	}

	private void registerItemRegistryReloader() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(
				new SimpleSynchronousResourceReloadListener() {
					@Override
					public Identifier getFabricId() {
						return Identifier.of(MOD_ID, "item_registry_reloader");
					}

					@Override
					public void reload(ResourceManager manager) {
						ItemRegistry.initialize();
					}
				}
		);
	}

	private void registerServerEvents() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			RisWorldState state = RisWorldState.getServerState(server);

			
			validateTargetItem(state);

			
			applyPendingData(state);
		});
	}

	private void validateTargetItem(RisWorldState state) {
		if (state.targetItemId != null) {
			Identifier itemId = Identifier.tryParse(state.targetItemId);
			if (itemId == null || !Registries.ITEM.containsId(itemId)) {
				LOGGER.warn("Invalid target item '{}', resetting to diamond", state.targetItemId);
				state.targetItemId = "minecraft:diamond"; 
				state.markDirty();
			}
		}
	}

	private void applyPendingData(RisWorldState state) {
		if (PendingCreationData.hasData()) {
			state.targetItemId = PendingCreationData.getTargetItemId();
			state.complicationId = PendingCreationData.getComplicationId();
			state.accumulatedMs = 0L;
			state.sessionStartMs = 0L;
			state.timerRunning = false;  
			state.completed = false;
			state.markDirty();

			LOGGER.info("Applied pending data: item={}, comp={}",
					state.targetItemId, state.complicationId);

			PendingCreationData.clear();
		}
	}
}