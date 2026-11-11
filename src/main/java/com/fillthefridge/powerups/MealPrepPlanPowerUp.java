package com.fillthefridge.powerups;

import com.fillthefridge.assets.AssetManager;
import com.fillthefridge.model.GameState;

public class MealPrepPlanPowerUp extends PowerUp {
    public MealPrepPlanPowerUp() {
        super("meal_prep_plan", "Meal Prep Plan",
            "Pairs with Efficient Packer: clean sorts containing meat and produce earn an extra life.",
            AssetManager.BREAD, false);
    }

    @Override
    public void apply(GameState gameState) {
        gameState.getActivePowerUps().setMealPrepPlanEnabled(true);
        gameState.getActivePowerUps().recordPowerUp(this);
    }
}
