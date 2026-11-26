package com.fillthefridge.events;

import com.fillthefridge.model.GameState;

/**
 * Represents a stage event card that modifies gameplay conditions.
 * Each event is themed around food safety and health scenarios.
 */
public abstract class EventCard {
    private final String id;
    private final String name;
    private final String description;
    private final String iconSymbol;
    private final EventType type;

    public enum EventType {
        CHALLENGE,
        BONUS,
        HAZARD,
        SYNERGY
    }

    protected EventCard(String id, String name, String description, String iconSymbol, EventType type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconSymbol = iconSymbol;
        this.type = type;
    }

    /**
     * Applied at the start of the stage when the event card is drawn.
     * Modifies timers, items, tray, or fridge state.
     */
    public abstract void apply(GameState gameState);

    /**
     * Optional per-frame update during the stage. Used for timed events like Power Outage.
     * Returns true if the event is still active, false if it has expired.
     */
    public boolean update(float deltaTime) {
        return false;
    }

    /**
     * Called when the stage ends. Cleans up any lingering state.
     */
    public void onStageEnd() {
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getIconSymbol() { return iconSymbol; }
    public EventType getType() { return type; }
}
