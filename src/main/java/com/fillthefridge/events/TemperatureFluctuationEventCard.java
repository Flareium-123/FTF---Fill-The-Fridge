package com.fillthefridge.events;

import com.fillthefridge.items.Item;
import com.fillthefridge.model.GameState;
import com.fillthefridge.model.ItemCategory;

/**
 * Temperature Fluctuation: One random compartment has reduced timer for items placed there.
 * Simulates a fridge with a broken temperature zone — items in that zone spoil faster.
 */
public class TemperatureFluctuationEventCard extends EventCard {
    private final ItemCategory affectedCategory;

    public TemperatureFluctuationEventCard(ItemCategory category) {
        super("temperature_fluctuation", "Temperature Fluctuation",
            "The " + category.getDisplayName().toLowerCase() + " zone temperature is unstable! "
            + "Items placed there lose 3 seconds of timer. Check your labels and rotate stock!",
            "🌡️", EventType.HAZARD);
        this.affectedCategory = category;
    }

    @Override
    public void apply(GameState gameState) {
        // Reduce timers for items in the affected category
        for (Item item : gameState.getTray().getActiveItems()) {
            if (item.getCategory() == affectedCategory && !item.isTrap()) {
                item.addBonusTime(-3f);
            }
        }
    }
}
