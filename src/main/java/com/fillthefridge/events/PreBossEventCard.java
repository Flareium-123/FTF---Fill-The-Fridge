package com.fillthefridge.events;

import com.fillthefridge.model.GameState;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A special event card that appears mandatory before the boss stage.
 * Presents a dramatic food-safety scenario with a choice of 3 outcomes,
 * each granting a different buff or debuff for the boss fight.
 */
public class PreBossEventCard extends EventCard {
    private final List<EventChoice> choices;
    private EventChoice selectedChoice;

    public static class EventChoice {
        private final String title;
        private final String description;
        private final String iconSymbol;

        EventChoice(String title, String description, String iconSymbol) {
            this.title = title;
            this.description = description;
            this.iconSymbol = iconSymbol;
        }

        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getIconSymbol() { return iconSymbol; }
    }

    public PreBossEventCard(long seed) {
        super("pre_boss_event", "FINAL INSPECTION",
            "The health inspector arrives before the boss fight. Choose your strategy.",
            "⚠️", EventType.CHALLENGE);

        Random random = new Random(seed);
        List<EventChoice> allChoices = new ArrayList<>();

        // Pool of possible choices
        allChoices.add(new EventChoice(
            "EMERGENCY COLD CHAIN",
            "Activate backup cooling: all boss stage timers start +3s, but traps deal double spoilage damage.",
            "❄️"
        ));
        allChoices.add(new EventChoice(
            "CROSS-CONTAMINATION ALERT",
            "Seal all raw meat zones: meat items gain +4s timer, but dairy items lose -2s timer.",
            "🦠"
        ));
        allChoices.add(new EventChoice(
            "FRIDGE ORGANIZER DROP",
            "A delivery of shelf organizers arrives: fridge weight support increases by +2 for the boss fight.",
            "📦"
        ));
        allChoices.add(new EventChoice(
            "TEMPERATURE LOG REVIEW",
            "Study past temperature logs: preview the boss phase modifiers, gain Sniff Test for the boss fight.",
            "📋"
        ));
        allChoices.add(new EventChoice(
            "STAFF TRAINING DRILL",
            "Quick training session: gain 1 extra undo action for the boss fight, but timers start -1s.",
            "🎓"
        ));
        allChoices.add(new EventChoice(
            "ICE PACK RESERVE",
            "Deploy emergency ice packs: Deep Freeze charges +2 for the boss fight.",
            "🧊"
        ));
        allChoices.add(new EventChoice(
            "EXPIRED STOCK CLEARANCE",
            "Clear out old items: start the boss fight with 1 extra life, but tray capacity is -1.",
            "🗑️"
        ));
        allChoices.add(new EventChoice(
            "HEALTH DEPARTMENT BLESSING",
            "The inspector is impressed: all items gain +2s, but the boss gains an extra health point.",
            "⭐"
        ));

        // Shuffle and pick 3
        List<EventChoice> shuffled = new ArrayList<>(allChoices);
        for (int i = shuffled.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            EventChoice temp = shuffled.get(i);
            shuffled.set(i, shuffled.get(j));
            shuffled.set(j, temp);
        }
        this.choices = new ArrayList<>(shuffled.subList(0, 3));
    }

    @Override
    public void apply(GameState gameState) {
        // Actual effect is applied when a choice is selected
    }

    public List<EventChoice> getChoices() {
        return choices;
    }

    public void selectChoice(int index) {
        if (index < 0 || index >= choices.size()) {
            return;
        }
        this.selectedChoice = choices.get(index);
    }

    public EventChoice getSelectedChoice() {
        return selectedChoice;
    }

    /**
     * Apply the selected choice's effect to the game state.
     */
    public void applySelectedChoice(GameState gameState) {
        if (selectedChoice == null) {
            return;
        }

        switch (selectedChoice.getTitle()) {
            case "EMERGENCY COLD CHAIN":
                // All boss stage timers start +3s, but traps deal double spoilage damage
                gameState.getActivePowerUps().addNextStageTimerBonus(3f);
                gameState.getActivePowerUps().setDoubleTrapDamage(true);
                break;
            case "CROSS-CONTAMINATION ALERT":
                // Meat items gain +4s timer, but dairy items lose -2s timer
                gameState.getActivePowerUps().setMeatTimerBonus(4f);
                gameState.getActivePowerUps().setDairyTimerPenalty(2f);
                break;
            case "FRIDGE ORGANIZER DROP":
                // Fridge weight support increases by +2 for the boss fight
                gameState.getActivePowerUps().setReinforcedShelvesEnabled(true);
                break;
            case "TEMPERATURE LOG REVIEW":
                // Preview boss phase modifiers, gain Sniff Test for the boss fight
                gameState.getActivePowerUps().setSniffTestEnabled(true);
                break;
            case "STAFF TRAINING DRILL":
                // Gain 1 extra undo action for the boss fight, but timers start -1s
                gameState.getActivePowerUps().addExtraUndoActions(1);
                gameState.getActivePowerUps().addNextStageTimerPenalty(1f);
                break;
            case "ICE PACK RESERVE":
                // Deep Freeze charges +2 for the boss fight
                gameState.getActivePowerUps().addDeepFreezeCharge(2);
                break;
            case "EXPIRED STOCK CLEARANCE":
                // Start the boss fight with 1 extra life, but tray capacity is -1
                gameState.getPlayerLives().healLives(1, "Stock clearance: +1 life.");
                gameState.getActivePowerUps().setTrayCapacityPenalty(1);
                break;
            case "HEALTH DEPARTMENT BLESSING":
                // All items gain +2s, but the boss gains an extra health point
                gameState.getActivePowerUps().addNextStageTimerBonus(2f);
                gameState.getActivePowerUps().setBossExtraHp(1);
                break;
            default:
                break;
        }
    }
}
