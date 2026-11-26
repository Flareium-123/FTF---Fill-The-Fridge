package com.fillthefridge.powerups;

import com.fillthefridge.assets.AssetManager;
import com.fillthefridge.model.GameState;

/**
 * Hero power-up: The FIFO Master starts with FIFO Caddy enabled and a charge.
 */
public class FifoMasterPowerUp extends PowerUp {
    public FifoMasterPowerUp() {
        super(
            "hero_fifo_master",
            "FIFO Master",
            "Start with FIFO Caddy enabled and 1 charge. First in, first out — always.",
            AssetManager.HERO_FIFO,
            true
        );
    }

    @Override
    public void apply(GameState gameState) {
        gameState.getActivePowerUps().setFifoCaddyEnabled(true);
        gameState.getActivePowerUps().refreshFifoCaddyCharge();
        gameState.getActivePowerUps().recordPowerUp(this);
    }
}
