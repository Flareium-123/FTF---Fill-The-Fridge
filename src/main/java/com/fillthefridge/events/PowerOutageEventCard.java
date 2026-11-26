package com.fillthefridge.events;

import com.fillthefridge.items.Item;
import com.fillthefridge.model.GameState;
import com.fillthefridge.model.ItemCategory;
import java.util.List;

/**
 * Power Outage: Timers tick 50% faster for the first 10 seconds, then normalize.
 * Simulates a fridge losing power — food spoils faster until power is restored.
 */
public class PowerOutageEventCard extends EventCard {
    private static final float DURATION = 10f;
    private static final float TIMER_SPEED_MULTIPLIER = 1.5f;

    private float remainingTime;
    private GameState gameState;

    public PowerOutageEventCard() {
        super("power_outage", "Power Outage",
            "Timers tick 50% faster for 10 seconds — food spoils quicker without power. "
            + "Prioritize the most perishable items first!",
            "⚡", EventType.HAZARD);
    }

    @Override
    public void apply(GameState gameState) {
        this.gameState = gameState;
        this.remainingTime = DURATION;
        // Reduce all tray item timers by 20% to simulate the initial shock
        for (Item item : gameState.getTray().getActiveItems()) {
            if (!item.isTrap()) {
                item.addBonusTime(-item.getMaxShelfLife() * 0.2f);
            }
        }
    }

    @Override
    public boolean update(float deltaTime) {
        if (remainingTime <= 0f) {
            return false;
        }
        remainingTime -= deltaTime;
        // Apply extra timer decay as a bonus time penalty instead of calling update() directly.
        // The normal game loop already calls item.update() via tray/fridge, so we only add
        // the additional decay component here to avoid double-updating.
        if (gameState != null && remainingTime > 0f) {
            float extraDecay = deltaTime * (TIMER_SPEED_MULTIPLIER - 1f);
            for (Item item : gameState.getTray().getActiveItems()) {
                item.addBonusTime(-extraDecay);
            }
            for (Item item : gameState.getFridge().getPlacedItems()) {
                item.addBonusTime(-extraDecay);
            }
        }
        return remainingTime > 0f;
    }

    public float getRemainingTime() {
        return remainingTime;
    }

    public boolean isActive() {
        return remainingTime > 0f;
    }
}
