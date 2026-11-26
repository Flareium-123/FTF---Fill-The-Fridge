package com.fillthefridge.events;

import com.fillthefridge.model.ItemCategory;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Pool of event cards that can be drawn for a stage.
 * Events are drawn randomly, with a bias toward challenges in later stages.
 */
public class EventCardPool {
    private final Random random;

    public EventCardPool(long seed) {
        this.random = new Random(seed);
    }

    /**
     * Get a list of all available event card factories.
     */
    private List<EventCardFactory> getAllFactories() {
        List<EventCardFactory> factories = new ArrayList<>();
        
        // Hazard events
        factories.add(seed -> new PowerOutageEventCard());
        factories.add(seed -> {
            ItemCategory[] categories = {ItemCategory.MEAT, ItemCategory.DAIRY, ItemCategory.VEGETABLE};
            return new TemperatureFluctuationEventCard(categories[random.nextInt(categories.length)]);
        });

        // Challenge events
        factories.add(seed -> new BulkDeliveryEventCard(seed));
        factories.add(seed -> new FridgeCleanoutEventCard(seed));
        factories.add(seed -> new MysteryBoxEventCard());
        factories.add(seed -> new DoubleTroubleEventCard(seed));

        // Synergy events
        factories.add(seed -> new ColdChainEventCard());
        factories.add(seed -> new ProduceClusterEventCard());

        return factories;
    }

    /**
     * Draw a random event card for the given stage.
     * Earlier stages get easier events; later stages get harder ones.
     * Returns null if no event is drawn (30% chance for stages 1-3).
     */
    public EventCard drawEventCard(int stageNumber, long seed) {
        // Stages 1-2: 30% chance of no event
        // Stages 3-6: 50% chance of no event
        // Stage 7+: 70% chance of an event
        float noEventChance;
        if (stageNumber <= 2) {
            noEventChance = 0.3f;
        } else if (stageNumber <= 6) {
            noEventChance = 0.5f;
        } else {
            noEventChance = 0.3f;
        }

        if (random.nextFloat() < noEventChance) {
            return null;
        }

        List<EventCardFactory> factories = getAllFactories();
        
        // Bias toward challenges in later stages
        if (stageNumber >= 5 && random.nextFloat() < 0.4f) {
            // Prefer challenge/hazard events
            for (EventCardFactory factory : factories) {
                EventCard card = factory.create(seed + random.nextLong());
                if (card.getType() == EventCard.EventType.CHALLENGE || card.getType() == EventCard.EventType.HAZARD) {
                    return card;
                }
            }
        }

        // Random selection
        EventCardFactory factory = factories.get(random.nextInt(factories.size()));
        return factory.create(seed + random.nextLong());
    }

    @FunctionalInterface
    interface EventCardFactory {
        EventCard create(long seed);
    }
}
