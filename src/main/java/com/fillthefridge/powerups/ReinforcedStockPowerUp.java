package com.fillthefridge.powerups;

import com.fillthefridge.assets.AssetManager;
import com.fillthefridge.model.GameState;

/**
 * Hero power-up: The Reinforced Stock starts with reinforced shelves enabled.
 */
public class ReinforcedStockPowerUp extends PowerUp {
    public ReinforcedStockPowerUp() {
        super(
            "hero_reinforced_stock",
            "Reinforced Stock",
            "Start with Reinforced Shelves: items can support heavier stacks.",
            AssetManager.HERO_REINFORCED,
            true
        );
    }

    @Override
    public void apply(GameState gameState) {
        gameState.getActivePowerUps().setReinforcedShelvesEnabled(true);
        gameState.getActivePowerUps().recordPowerUp(this);
    }
}
