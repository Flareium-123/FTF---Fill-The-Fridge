package com.fillthefridge.powerups;

import com.fillthefridge.assets.AssetManager;
import com.fillthefridge.model.GameState;

public class DeepFreezeUpgradePowerUp extends PowerUp {
    public DeepFreezeUpgradePowerUp() {
        super("deep_freeze_upgrade", "Deep Freeze II",
                "Upgrade Deep Freeze: add one extra emergency freeze charge.",
                AssetManager.SHRIMP, true);
    }

    @Override
    public void apply(GameState gameState) {
        gameState.getActivePowerUps().addDeepFreezeCharge(1);
        gameState.getActivePowerUps().recordPowerUp(this);
    }
}
