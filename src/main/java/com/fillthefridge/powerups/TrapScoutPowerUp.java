package com.fillthefridge.powerups;

import com.fillthefridge.assets.AssetManager;
import com.fillthefridge.model.GameState;

public class TrapScoutPowerUp extends PowerUp {
    public TrapScoutPowerUp() {
        super("trap_scout", "Trap Scout",
                "Reveal the next trap as it enters the tray in each stage.",
                AssetManager.PLASTIC_BAG, false);
    }

    @Override
    public void apply(GameState gameState) {
        gameState.getActivePowerUps().setTrapScoutEnabled(true);
        gameState.getActivePowerUps().recordPowerUp(this);
    }
}
