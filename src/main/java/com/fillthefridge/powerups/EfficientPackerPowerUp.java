package com.fillthefridge.powerups;

import com.fillthefridge.assets.AssetManager;
import com.fillthefridge.model.GameState;

public class EfficientPackerPowerUp extends PowerUp {
    public EfficientPackerPowerUp() {
        super("efficient_packer", "Efficient Packer",
                "Complete a stage contract to recover one life. Synergizes with Meal Prep Plan.",
                AssetManager.EGG_CARTON, false);
    }

    @Override
    public void apply(GameState gameState) {
        gameState.getActivePowerUps().setEfficientPackerEnabled(true);
        gameState.getActivePowerUps().recordPowerUp(this);
    }
}
