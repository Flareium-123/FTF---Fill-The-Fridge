package com.fillthefridge.events;

import com.fillthefridge.items.Item;
import com.fillthefridge.model.GameState;
import com.fillthefridge.model.ItemCategory;
import com.fillthefridge.util.GridPosition;

/**
 * Produce Cluster: Placing 3+ vegetables adjacent gives all of them +0.5s.
 * Simulates proper produce storage — keeping vegetables together in the crisper drawer
 * maintains humidity and extends freshness.
 */
public class ProduceClusterEventCard extends EventCard {
    public ProduceClusterEventCard() {
        super("produce_cluster", "Produce Cluster",
            "Placing 3 or more vegetables adjacent gives all of them +0.5s timer bonus. "
            + "Storing produce together in the crisper maintains humidity and extends freshness!",
            "🥬", EventType.SYNERGY);
    }

    @Override
    public void apply(GameState gameState) {
        // Passive event — bonus applied on placement
    }

    /**
     * Check if a placed vegetable should receive the produce cluster bonus.
     */
    public float checkPlacementBonus(Item placedItem, GameState gameState) {
        if (placedItem == null || placedItem.isTrap() || !placedItem.isInFridge()) {
            return 0f;
        }

        if (placedItem.getCategory() != ItemCategory.VEGETABLE) {
            return 0f;
        }

        var pos = placedItem.getGridPosition();
        if (pos == null) return 0f;

        var fridge = gameState.getFridge();

        // Count adjacent vegetables (including the placed item)
        int adjacentVegCount = 1; // the placed item itself
        java.util.List<Item> adjacentVeggies = new java.util.ArrayList<>();
        adjacentVeggies.add(placedItem);

        int[] deltas = {-1, 0, 1};
        for (int dc : deltas) {
            for (int dr : deltas) {
                if (dc == 0 && dr == 0) continue;
                int ac = pos.getCol() + dc;
                int ar = pos.getRow() + dr;
                if (ac < 0 || ac >= fridge.getCols() || ar < 0 || ar >= fridge.getRows()) continue;
                Item adjacent = fridge.getItemAt(new GridPosition(ac, ar));
                if (adjacent != null && adjacent.getCategory() == ItemCategory.VEGETABLE) {
                    adjacentVegCount++;
                    adjacentVeggies.add(adjacent);
                }
            }
        }

        if (adjacentVegCount >= 3) {
            float bonus = 0.5f;
            for (Item veg : adjacentVeggies) {
                veg.addBonusTime(bonus);
            }
            return bonus;
        }
        return 0f;
    }
}
