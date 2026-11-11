package com.fillthefridge.powerups;

import com.fillthefridge.assets.AssetManager;
import com.fillthefridge.model.GameState;

public class ColdSnapUpgradePowerUp extends PowerUp {
    public ColdSnapUpgradePowerUp() {
        super("cold_snap_upgrade", "Cold Snap II",
                "Upgrade Cold Snap: add another +2 seconds to every grocery timer in future stages.",
                AssetManager.WATERMELON, true);
    }

    @Override
    public void apply(GameState gameState) {
        gameState.getActivePowerUps().addColdSnapBonus(2f);
        gameState.getActivePowerUps().recordPowerUp(this);
    }
}
