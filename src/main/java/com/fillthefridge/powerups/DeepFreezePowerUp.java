package com.fillthefridge.powerups;

import com.fillthefridge.assets.AssetManager;
import com.fillthefridge.items.Item;
import com.fillthefridge.model.GamePhase;
import com.fillthefridge.model.GameState;

public class DeepFreezePowerUp extends PowerUp {
    public DeepFreezePowerUp() {
        super(
                "deep_freeze",
                "Deep Freeze",
                "Pauses urgent food now and stores 1 charge ([F]). With FIFO Caddy, gain another freeze charge each stage.",
                AssetManager.FISH,
                false
        );
    }

    @Override
    public void apply(GameState gameState) {
        gameState.getActivePowerUps().addDeepFreezeCharge(1);
        if (gameState.getCurrentPhase() == GamePhase.PLAYING) {
            Item urgent = gameState.getTray().findMostUrgentItem();
            if (urgent != null) {
                urgent.applyDeepFreeze(3.0f);
            }
        } else {
            gameState.getActivePowerUps().setPendingInitialDeepFreeze(true);
        }
        gameState.getActivePowerUps().recordPowerUp(this);
    }
}
