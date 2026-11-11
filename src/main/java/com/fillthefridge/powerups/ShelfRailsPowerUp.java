package com.fillthefridge.powerups;

import com.fillthefridge.assets.AssetManager;
import com.fillthefridge.model.GameState;

public class ShelfRailsPowerUp extends PowerUp {
    public ShelfRailsPowerUp() {
        super("shelf_rails", "Shelf Rails",
                "Retrieve one locked fridge item per stage to rethink its placement.",
                AssetManager.CHEESE, false);
    }

    @Override
    public void apply(GameState gameState) {
        gameState.getActivePowerUps().setShelfRailsEnabled(true);
        gameState.getActivePowerUps().recordPowerUp(this);
    }
}
