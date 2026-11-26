package com.fillthefridge.powerups;

import com.fillthefridge.assets.AssetManager;
import com.fillthefridge.model.GameState;

/**
 * Hero power-up: The Space Optimizer starts with extra tray capacity.
 */
public class SpaceOptimizerPowerUp extends PowerUp {
    public SpaceOptimizerPowerUp() {
        super(
            "hero_space_optimizer",
            "Space Optimizer",
            "Start with +2 extra tray slots. Every inch counts.",
            AssetManager.HERO_SPACE,
            true
        );
    }

    @Override
    public void apply(GameState gameState) {
        gameState.getActivePowerUps().addExtraTrayCapacity(2);
        gameState.getActivePowerUps().recordPowerUp(this);
    }
}
