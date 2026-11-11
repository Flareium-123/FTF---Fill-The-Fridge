package com.fillthefridge.powerups;

import com.fillthefridge.assets.AssetManager;
import com.fillthefridge.model.GameState;

public class CrisperDrawerPowerUp extends PowerUp {
    public CrisperDrawerPowerUp() {
        super(
                "crisper_drawer",
                "Crisper Drawer",
                "Show one extra grocery in the tray. With Produce Saver, produce gets another +2s.",
                AssetManager.CARROT,
                false
        );
    }

    @Override
    public void apply(GameState gameState) {
        gameState.getActivePowerUps().addExtraTrayCapacity(1);
        gameState.getActivePowerUps().recordPowerUp(this);
    }
}
