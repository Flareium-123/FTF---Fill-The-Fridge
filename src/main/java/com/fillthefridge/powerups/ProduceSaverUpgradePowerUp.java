package com.fillthefridge.powerups;

import com.fillthefridge.assets.AssetManager;
import com.fillthefridge.model.GameState;

public class ProduceSaverUpgradePowerUp extends PowerUp {
    public ProduceSaverUpgradePowerUp() {
        super("produce_saver_upgrade", "Produce Saver II",
                "Upgrade Produce Saver: add another +3 seconds to produce in future stages.",
                AssetManager.BROCCOLI, true);
    }

    @Override
    public void apply(GameState gameState) {
        gameState.getActivePowerUps().addProduceTimerBonus(3f);
        gameState.getActivePowerUps().recordPowerUp(this);
    }
}
