package com.fillthefridge.powerups;

import com.fillthefridge.assets.AssetManager;
import com.fillthefridge.model.GameState;

public class ReusableContainersPowerUp extends PowerUp {
    public ReusableContainersPowerUp() {
        super("reusable_containers", "Reusable Containers",
            "Press R once per stage with a rectangular grocery selected to rotate its footprint.",
            AssetManager.LEFTOVERS_CONTAINER, false);
    }

    @Override
    public void apply(GameState gameState) {
        gameState.getActivePowerUps().setReusableContainersEnabled(true);
        gameState.getActivePowerUps().recordPowerUp(this);
    }
}
