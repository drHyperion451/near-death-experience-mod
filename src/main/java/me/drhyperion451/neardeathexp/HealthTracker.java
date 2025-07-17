package me.drhyperion451.neardeathexp;

import net.minecraft.entity.player.PlayerEntity;

/**
 * Tracks player health changes to trigger near-death experience effects.
 * Monitors for critical health conditions and massive damage events.
 */
public class HealthTracker {
    // Store the player's health from the previous tick for comparison
    private float previousHealth = 20.0f;
    // Track the player's maximum health (can change with mods/effects)
    private float maxHealth = 20.0f;
    // Prevent multiple triggers during the same near-death episode
    private boolean hasTriggered = false;

    /**
     * Updates the health tracker with current player state.
     * Called every client tick to monitor health changes.
     *
     * @param player The current player entity
     */
    public void update(PlayerEntity player) {
        float currentHealth = player.getHealth();
        float currentMaxHealth = player.getMaxHealth();

        // Update max health if it changed (due to effects, mods, etc.)
        if (currentMaxHealth != maxHealth) {
            maxHealth = currentMaxHealth;
        }

        // Check if conditions are met to trigger the Jesus effect
        if (shouldTriggerJesus(currentHealth, currentMaxHealth)) {
            triggerJesusEffect();
        }

        // Store current health for next tick's comparison
        previousHealth = currentHealth;

        // Reset trigger flag when player recovers above 50% health
        // This allows the effect to trigger again in future near-death situations
        if (currentHealth > maxHealth * 0.5f) {
            hasTriggered = false;
        }
    }

    /**
     * Determines if the Jesus effect should be triggered based on health conditions.
     *
     * @param currentHealth Player's current health points
     * @param maxHealth Player's maximum health points
     * @return true if the effect should trigger, false otherwise
     */
    private boolean shouldTriggerJesus(float currentHealth, float maxHealth) {
        // Condition 1: Player is at critical health (1 heart = 2 HP)
        boolean nearDeath = currentHealth <= 2.0f;

        // Condition 2: Player took massive damage (50% or more of max health in one hit)
        float healthLost = previousHealth - currentHealth;
        boolean massiveDamage = healthLost >= (maxHealth * 0.50f);

        // Only trigger if we haven't already triggered in this near-death session
        boolean canTrigger = !hasTriggered;

        // Trigger if either condition is met AND we can still trigger
        return canTrigger && (nearDeath || massiveDamage);
    }

    /**
     * Activates the Jesus overlay effect and logs the trigger event.
     * Sets the trigger flag to prevent repeated activations.
     */
    private void triggerJesusEffect() {
        hasTriggered = true;
        NearDeathExperienceMod.getInstance().getJesusOverlay().showJesus();
        NearDeathExperienceMod.LOGGER.info("Jesus effect triggered!");
    }
}