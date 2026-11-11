package com.fillthefridge.powerups;

import com.fillthefridge.assets.AssetManager;
import com.fillthefridge.model.GameState;

public class ReinforcedShelvesPowerUp extends PowerUp {
    public ReinforcedShelvesPowerUp() {
        super(
                "reinforced_shelves",
                "Reinforced Shelves",
                "Support heavier items stacked on safe, stable bases",
                AssetManager.PIZZA_BOX,
                false
        );
    }

    @Override
    public void apply(GameState gameState) {
        gameState.getActivePowerUps().setReinforcedShelvesEnabled(true);
        gameState.getActivePowerUps().recordPowerUp(this);
    }
}
