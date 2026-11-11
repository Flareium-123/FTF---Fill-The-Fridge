package com.fillthefridge.powerups;

import com.fillthefridge.assets.AssetManager;
import com.fillthefridge.model.GameState;

public class FifoCaddyPowerUp extends PowerUp {
    public FifoCaddyPowerUp() {
        super("fifo_caddy", "FIFO Caddy",
                "Press C once per stage to pause the most urgent grocery for 4s. With Deep Freeze, gain another freeze charge each stage.",
                AssetManager.APPLE, false);
    }

    @Override
    public void apply(GameState gameState) {
        gameState.getActivePowerUps().setFifoCaddyEnabled(true);
        gameState.getActivePowerUps().recordPowerUp(this);
    }
}
