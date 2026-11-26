package com.fillthefridge.events;

import com.fillthefridge.items.Item;
import com.fillthefridge.model.GameState;

/**
 * Mystery Box: One item in the tray is hidden until selected.
 * Could be a bonus item with extra time or a trap. Simulates unlabeled food —
 * always check expiration dates before consuming!
 */
public class MysteryBoxEventCard extends EventCard {
    private Item mysteryItem;

    public MysteryBoxEventCard() {
        super("mystery_box", "Mystery Box",
            "One item in the tray is hidden until selected — it could be a bonus or a trap! "
            + "Always check labels and expiration dates on unmarked food.",
            "❓", EventType.CHALLENGE);
    }

    @Override
    public void apply(GameState gameState) {
        // Pick a random item in the tray and hide it
        var trayItems = gameState.getTray().getActiveItems();
        if (trayItems.isEmpty()) return;

        // Find a non-trap item to hide
        for (Item item : trayItems) {
            if (!item.isTrap()) {
                mysteryItem = item;
                // Hide the item's identity by setting a placeholder display
                item.setHidden(true);
                break;
            }
        }
    }

    public Item getMysteryItem() {
        return mysteryItem;
    }

    public void revealItem() {
        if (mysteryItem != null) {
            mysteryItem.setHidden(false);
        }
    }
}
