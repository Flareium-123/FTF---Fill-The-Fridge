package com.fillthefridge.powerups;

import com.fillthefridge.assets.AssetManager;
import com.fillthefridge.model.GameState;

/**
 * Hero power-up: The Produce Whisperer starts with Produce Saver and extra produce time.
 */
public class ProduceWhispererPowerUp extends PowerUp {
    public ProduceWhispererPowerUp() {
        super(
            "hero_produce_whisperer",
            "Produce Whisperer",
            "Start with Produce Saver: vegetables and fruits get +3s on their timers.",
            AssetManager.HERO_PRODUCE,
            true
        );
    }

    @Override
    public void apply(GameState gameState) {
        gameState.getActivePowerUps().addProduceTimerBonus(3.0f);
        gameState.getActivePowerUps().recordPowerUp(this);
    }
}
