package com.fillthefridge.events;

import com.fillthefridge.items.Item;
import com.fillthefridge.model.Fridge;
import com.fillthefridge.model.GameState;
import com.fillthefridge.util.GridPosition;
import com.fillthefridge.util.InvalidPlacementException;
import java.util.List;
import java.util.Random;

/**
 * Fridge Cleanout: Start with 2-3 items already placed randomly (not optimally).
 * Simulates finding a messy fridge — you must work around existing items.
 */
public class FridgeCleanoutEventCard extends EventCard {
    private final Random random;

    public FridgeCleanoutEventCard(long seed) {
        super("fridge_cleanout", "Fridge Cleanout",
            "2-3 items are already placed in the fridge in random positions. "
            + "Work around them — a clean fridge starts with proper organization!",
            "🧹", EventType.CHALLENGE);
        this.random = new Random(seed);
    }

    @Override
    public void apply(GameState gameState) {
        Fridge fridge = gameState.getFridge();
        int count = 2 + random.nextInt(2); // 2 or 3 items

        for (int i = 0; i < count; i++) {
            // Pick a random item from the tray
            List<Item> trayItems = gameState.getTray().getActiveItems();
            if (trayItems.isEmpty()) break;

            Item item = trayItems.get(random.nextInt(trayItems.size()));
            if (item.isTrap()) continue;

            // Find a random valid position, avoiding blocked tiles
            int attempts = 0;
            while (attempts < 30) {
                int col = random.nextInt(fridge.getCols());
                int row = random.nextInt(fridge.getRows());
                GridPosition pos = new GridPosition(col, row);

                try {
                    fridge.placeItem(item, pos);
                    // Mark as confirmed so the player cannot move pre-placed items
                    item.setConfirmed(true);
                    gameState.getTray().removeActiveItem(item);
                    break;
                } catch (InvalidPlacementException e) {
                    attempts++;
                }
            }
        }
        // Refresh tile states after all items are placed
        fridge.refreshAllTileStates();
    }
}
