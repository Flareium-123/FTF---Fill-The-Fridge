package com.fillthefridge.events;

import com.fillthefridge.items.Item;
import com.fillthefridge.items.ItemCatalog;
import com.fillthefridge.model.GameState;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Double Trouble: Two identical items appear, forcing you to find space for both.
 * Simulates buying in bulk — great for savings, but you need room for everything!
 */
public class DoubleTroubleEventCard extends EventCard {
    private final Random random;

    public DoubleTroubleEventCard(long seed) {
        super("double_trouble", "Double Trouble",
            "Two identical items appear in the tray — you must find space for both! "
            + "Buying in bulk saves money, but plan your storage carefully.",
            "🔄", EventType.CHALLENGE);
        this.random = new Random(seed);
    }

    @Override
    public void apply(GameState gameState) {
        List<Item> templates = ItemCatalog.getAllGroceryTemplates();
        // Pick a random template and add two copies
        Item template = templates.get(random.nextInt(templates.size()));
        
        for (int i = 0; i < 2; i++) {
            Item copy = template.createFreshCopy();
            gameState.getTray().addActiveItem(copy);
        }
    }
}
