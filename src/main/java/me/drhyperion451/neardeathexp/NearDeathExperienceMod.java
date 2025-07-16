package me.drhyperion451.neardeathexp;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NearDeathExperienceMod implements ClientModInitializer {
	public static final String MOD_ID = "near-death-experience";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Identifier BELL_SOUND = NearDeathExperienceMod.id("bell");
	public static final SoundEvent bellSound = SoundEvent.of(BELL_SOUND);

	private static NearDeathExperienceMod INSTANCE;
	private JesusOverlay jesusOverlay;
	private HealthTracker healthTracker;

	@Override
	public void onInitializeClient() {
		INSTANCE = this;

		// Inicialize components
		this.jesusOverlay = new JesusOverlay();
		this.healthTracker = new HealthTracker();

		// Registrar eventos
		ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
		Registry.register(Registries.SOUND_EVENT, NearDeathExperienceMod.BELL_SOUND, bellSound);

		LOGGER.info("Near Death Experience mod initialized!");
	}

	private void onClientTick(MinecraftClient client) {
		if (client.player != null && client.world != null) {
			healthTracker.update(client.player);
		}
	}

	public static NearDeathExperienceMod getInstance() {
		return INSTANCE;
	}

	public JesusOverlay getJesusOverlay() {
		return jesusOverlay;
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}