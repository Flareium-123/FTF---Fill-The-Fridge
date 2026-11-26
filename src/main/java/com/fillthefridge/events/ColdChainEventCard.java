package com.fillthefridge.events;

import com.fillthefridge.items.Item;
import com.fillthefridge.model.GameState;
import com.fillthefridge.model.ItemCategory;

/**
 * Cold Chain: Placing meat next to dairy gives both +1s timer bonus.
 * Simulates proper cold chain management — keeping cold items together maintains temperature.
 */
public class ColdChainEventCard extends EventCard {
    public ColdChainEventCard() {
        super("cold_chain", "Cold Chain",
            "Meat placed adjacent to dairy receives a +1s timer bonus for both items. "
            + "Keeping cold items together maintains the cold chain and slows bacterial growth!",
            "❄️", EventType.SYNERGY);
    }

    @Override
    public void apply(GameState gameState) {
        // This event is passive — the bonus is applied when items are placed.
        // The GameController checks for this event on placement.
    }

    /**
     * Check if a placed item should receive the cold chain bonus.
     * Returns the bonus seconds if applicable, 0 otherwise.
     */
    public float checkPlacementBonus(Item placedItem, GameState gameState) {
        if (placedItem == null || placedItem.isTrap() || !placedItem.isInFridge()) {
            return 0f;
        }

        boolean isMeatOrDairy = placedItem.getCategory() == ItemCategory.MEAT
            || placedItem.getCategory() == ItemCategory.DAIRY;
        if (!isMeatOrDairy) {
            return 0f;
        }

        // Check adjacent tiles for the other category
        ItemCategory otherCategory = placedItem.getCategory() == ItemCategory.MEAT
            ? ItemCategory.DAIRY : ItemCategory.MEAT;

        var pos = placedItem.getGridPosition();
        if (pos == null) return 0f;

        var fridge = gameState.getFridge();
        int[] deltas = {-1, 0, 1};
        for (int dc : deltas) {
            for (int dr : deltas) {
                if (dc == 0 && dr == 0) continue;
                int ac = pos.getCol() + dc;
                int ar = pos.getRow() + dr;
                if (ac < 0 || ac >= fridge.getCols() || ar < 0 || ar >= fridge.getRows()) continue;
                Item adjacent = fridge.getItemAt(new com.fillthefridge.util.GridPosition(ac, ar));
                if (adjacent != null && adjacent.getCategory() == otherCategory) {
                    // Found adjacent item of the other category — apply bonus to both
                    placedItem.addBonusTime(1f);
                    adjacent.addBonusTime(1f);
                    return 1f;
                }
            }
        }
        return 0f;
    }
}
