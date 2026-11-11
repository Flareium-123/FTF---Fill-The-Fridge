package com.fillthefridge.powerups;

import com.fillthefridge.assets.AssetManager;
import com.fillthefridge.model.GameState;

public class SmartThermostatPowerUp extends PowerUp {
    public SmartThermostatPowerUp() {
        super("smart_thermostat", "Smart Thermostat",
                "Give the most urgent grocery +5s each sort. With Cold Snap, it gets another +3s.",
                AssetManager.MILK, false);
    }

    @Override
    public void apply(GameState gameState) {
        gameState.getActivePowerUps().setSmartThermostatEnabled(true);
        gameState.getActivePowerUps().recordPowerUp(this);
    }
}
