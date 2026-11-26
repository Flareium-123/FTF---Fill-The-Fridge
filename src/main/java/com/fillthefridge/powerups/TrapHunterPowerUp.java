package com.fillthefridge.powerups;

import com.fillthefridge.assets.AssetManager;
import com.fillthefridge.model.GameState;

/**
 * Hero power-up: The Trap Hunter starts with Trap Scout enabled.
 */
public class TrapHunterPowerUp extends PowerUp {
    public TrapHunterPowerUp() {
        super(
            "hero_trap_hunter",
            "Trap Hunter",
            "Start with Trap Scout: non-food items are revealed in the tray.",
            AssetManager.HERO_TRAP,
            true
        );
    }

    @Override
    public void apply(GameState gameState) {
        gameState.getActivePowerUps().setTrapScoutEnabled(true);
        gameState.getActivePowerUps().recordPowerUp(this);
    }
}
