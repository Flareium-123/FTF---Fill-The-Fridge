package com.fillthefridge.powerups;

import com.fillthefridge.assets.AssetManager;
import com.fillthefridge.model.GameState;

/**
 * Hero power-up: The Time Bender starts with +3s on all tray timers each stage.
 */
public class TimeBenderPowerUp extends PowerUp {
    public TimeBenderPowerUp() {
        super(
            "hero_time_bender",
            "Time Bender",
            "Start with Cold Snap: all tray items get +3s. Time is on your side.",
            AssetManager.HERO_TIME,
            true
        );
    }

    @Override
    public void apply(GameState gameState) {
        gameState.getActivePowerUps().addColdSnapBonus(3.0f);
        gameState.getTray().addBonusTimeToAll(3.0f);
        gameState.getActivePowerUps().recordPowerUp(this);
    }
}
