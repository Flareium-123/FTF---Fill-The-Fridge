package com.fillthefridge.powerups;

import com.fillthefridge.assets.AssetManager;
import com.fillthefridge.model.GameState;

/**
 * Hero power-up: The Cold Storage Veteran starts with an extra life.
 */
public class ColdStorageVeteranPowerUp extends PowerUp {
    public ColdStorageVeteranPowerUp() {
        super(
            "hero_cold_storage_veteran",
            "Cold Storage Veteran",
            "Start the run with an extra life. Experience counts for something.",
            AssetManager.HERO_VETERAN,
            true
        );
    }

    @Override
    public void apply(GameState gameState) {
        gameState.getPlayerLives().healLives(1, "Cold Storage Veteran: extra life");
        gameState.getActivePowerUps().recordPowerUp(this);
    }
}
