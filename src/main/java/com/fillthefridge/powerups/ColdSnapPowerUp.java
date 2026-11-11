package com.fillthefridge.powerups;

import com.fillthefridge.assets.AssetManager;
import com.fillthefridge.model.GameState;

public class ColdSnapPowerUp extends PowerUp {
    public ColdSnapPowerUp() {
        super(
            "cold_snap",
            "Cold Snap",
            "+2s to all tray timers. With Smart Thermostat, the most urgent grocery gets another +3s.",
            AssetManager.SNOWFLAKE,
            false
        );
    }

    @Override
    public void apply(GameState gameState) {
        gameState.getActivePowerUps().addColdSnapBonus(2.0f);
        gameState.getTray().addBonusTimeToAll(2.0f);
        gameState.getActivePowerUps().recordPowerUp(this);
    }
}
