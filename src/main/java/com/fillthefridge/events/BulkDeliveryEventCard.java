package com.fillthefridge.events;

import com.fillthefridge.items.Item;
import com.fillthefridge.items.ItemCatalog;
import com.fillthefridge.model.GameState;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Bulk Delivery: 2 extra items appear in the tray, but tray capacity doesn't increase.
 * Simulates an unexpected large grocery delivery — you must prioritize what to save.
 */
public class BulkDeliveryEventCard extends EventCard {
    private final Random random;

    public BulkDeliveryEventCard(long seed) {
        super("bulk_delivery", "Bulk Delivery",
            "2 extra items appear in the tray without increasing capacity. "
            + "Prioritize the most perishable — some items may need to be left behind!",
            "📦", EventType.CHALLENGE);
        this.random = new Random(seed);
    }

    @Override
    public void apply(GameState gameState) {
        List<Item> templates = ItemCatalog.getAllGroceryTemplates();
        List<Item> extraItems = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            Item template = templates.get(random.nextInt(templates.size()));
            Item extra = template.createFreshCopy();
            // Give extra items slightly less time to create urgency
            extra.addBonusTime(-2f);
            extraItems.add(extra);
        }

        // Add extra items to the tray without increasing capacity
        for (Item item : extraItems) {
            gameState.getTray().addActiveItem(item);
        }
    }
}
