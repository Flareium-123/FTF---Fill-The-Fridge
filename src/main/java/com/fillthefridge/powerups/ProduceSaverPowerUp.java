package com.fillthefridge.powerups;

import com.fillthefridge.assets.AssetManager;
import com.fillthefridge.model.GameState;

public class ProduceSaverPowerUp extends PowerUp {
    public ProduceSaverPowerUp() {
        super(
                "produce_saver",
                "Produce Saver",
                "Add 3s to produce timers. With Crisper Drawer, produce gets another +2s.",
                AssetManager.STRAWBERRIES,
                false
        );
    }

    @Override
    public void apply(GameState gameState) {
        gameState.getActivePowerUps().addProduceTimerBonus(3f);
        gameState.getActivePowerUps().recordPowerUp(this);
    }
}
