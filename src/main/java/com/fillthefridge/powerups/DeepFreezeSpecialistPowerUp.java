package com.fillthefridge.powerups;

import com.fillthefridge.assets.AssetManager;
import com.fillthefridge.model.GameState;

/**
 * Hero power-up: The Deep Freeze Specialist starts with 2 Deep Freeze charges.
 */
public class DeepFreezeSpecialistPowerUp extends PowerUp {
    public DeepFreezeSpecialistPowerUp() {
        super(
            "hero_deep_freeze_specialist",
            "Deep Freeze Specialist",
            "Start with 2 Deep Freeze charges. Pause the most urgent items twice.",
            AssetManager.HERO_FREEZE,
            true
        );
    }

    @Override
    public void apply(GameState gameState) {
        gameState.getActivePowerUps().addDeepFreezeCharge(2);
        gameState.getActivePowerUps().recordPowerUp(this);
    }
}
