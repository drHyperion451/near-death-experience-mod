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

/**
 * Main mod class for the Near Death Experience mod.
 * Handles initialization, event registration, and component management.
 * This is a client-side only mod that triggers visual effects when the player is near death.
 */
public class NearDeathExperienceMod implements ClientModInitializer {
	// Mod identifier used for resource locations and registrations
	public static final String MOD_ID = "near-death-experience";
	// Logger for debugging and information output
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// Sound event identifiers and registrations
	public static final Identifier BELL_SOUND = NearDeathExperienceMod.id("bell");
	public static final SoundEvent bellSound = SoundEvent.of(BELL_SOUND);

	// Singleton instance for global access
	private static NearDeathExperienceMod INSTANCE;

	// Core mod components
	private JesusOverlay jesusOverlay;    // Handles the visual overlay effect
	private HealthTracker healthTracker;  // Monitors player health changes

	/**
	 * Called when the mod is initialized on the client side.
	 * Sets up all components, registers events, and prepares the mod for use.
	 */
	@Override
	public void onInitializeClient() {
		// Store singleton instance for global access
		INSTANCE = this;

		// Initialize core components
		this.jesusOverlay = new JesusOverlay();
		this.healthTracker = new HealthTracker();

		// Register event listeners
		// This will call onClientTick every game tick
		ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);

		// Register the bell sound effect with Minecraft's sound system
		Registry.register(Registries.SOUND_EVENT, NearDeathExperienceMod.BELL_SOUND, bellSound);

		LOGGER.info("Near Death Experience mod initialized!");
	}

	/**
	 * Called every client tick (20 times per second).
	 * Updates the health tracker if the player and world exist.
	 *
	 * @param client The Minecraft client instance
	 */
	private void onClientTick(MinecraftClient client) {
		// Only update if player exists and is in a world
		// Prevents null pointer exceptions during loading/menu screens
		if (client.player != null && client.world != null) {
			healthTracker.update(client.player);
		}
	}

	/**
	 * Gets the singleton instance of this mod.
	 * Used by other classes to access mod components.
	 *
	 * @return The mod instance
	 */
	public static NearDeathExperienceMod getInstance() {
		return INSTANCE;
	}

	/**
	 * Gets the Jesus overlay component.
	 * Used by the health tracker to trigger visual effects.
	 *
	 * @return The Jesus overlay instance
	 */
	public JesusOverlay getJesusOverlay() {
		return jesusOverlay;
	}

	/**
	 * Creates a namespaced identifier for mod resources.
	 * Convenience method for creating resource locations.
	 *
	 * @param path The resource path
	 * @return A namespaced identifier for this mod
	 */
	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}