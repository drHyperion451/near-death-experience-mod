package me.drhyperion451.neardeathexp;

import net.minecraft.entity.player.PlayerEntity;

public class HealthTracker {
    private float previousHealth = 20.0f;
    private float maxHealth = 20.0f;
    private boolean hasTriggered = false;

    public void update(PlayerEntity player) {
        float currentHealth = player.getHealth();
        float currentMaxHealth = player.getMaxHealth();

        // Actualizar max health si cambió
        if (currentMaxHealth != maxHealth) {
            maxHealth = currentMaxHealth;
        }

        // Verificar condiciones para activar el efecto
        if (shouldTriggerJesus(currentHealth, currentMaxHealth)) {
            triggerJesusEffect();
        }

        // Actualizar salud anterior
        previousHealth = currentHealth;

        // Reset del trigger si la salud se recupera significativamente
        if (currentHealth > maxHealth * 0.5f) {
            hasTriggered = false;
        }
    }

    private boolean shouldTriggerJesus(float currentHealth, float maxHealth) {
        // Condición 1: Estar a 1 corazón (2 HP)
        boolean nearDeath = currentHealth <= 2.0f;

        // Condición 2: Haber perdido 85% de la vida en un hit
        float healthLost = previousHealth - currentHealth;
        boolean massiveDamage = healthLost >= (maxHealth * 0.50f);

        // Solo activar si no se ha activado ya en esta "sesión cerca de la muerte"
        boolean canTrigger = !hasTriggered;

        return canTrigger && (nearDeath || massiveDamage);
    }

    private void triggerJesusEffect() {
        hasTriggered = true;
        NearDeathExperienceMod.getInstance().getJesusOverlay().showJesus();
        NearDeathExperienceMod.LOGGER.info("Jesus effect triggered!");
    }
}