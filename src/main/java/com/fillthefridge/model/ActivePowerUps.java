package com.fillthefridge.model;

import com.fillthefridge.powerups.PowerUp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActivePowerUps {
    private float coldSnapBonusSeconds;
    private int deepFreezeCharges;
    private boolean ghostFitEnabled;
    private boolean sniffTestEnabled;
    private int extraTrashSlots;
    private int extraTrayCapacity;
    private float produceTimerBonusSeconds;
    private boolean reinforcedShelvesEnabled;
    private boolean smartThermostatEnabled;
    private boolean mealPrepPlanEnabled;
    private boolean efficientPackerEnabled;
    private boolean trapScoutEnabled;
    private boolean fifoCaddyEnabled;
    private boolean shelfRailsEnabled;
    private boolean reusableContainersEnabled;
    private int nextStageTrayBonus;
    private float nextStageTimerBonus;
    private float nextStageTimerPenalty;
    private int fifoCaddyCharges;
    private final List<PowerUp> acquiredPowerUps;

    public ActivePowerUps() {
        this.acquiredPowerUps = new ArrayList<>();
    }

    public void recordPowerUp(PowerUp powerUp) {
        if (powerUp != null) {
            acquiredPowerUps.add(powerUp);
        }
    }

    public void addColdSnapBonus(float seconds) {
        this.coldSnapBonusSeconds += seconds;
    }

    public float getColdSnapBonusSeconds() {
        return coldSnapBonusSeconds;
    }

    public void addDeepFreezeCharge(int charges) {
        this.deepFreezeCharges += charges;
    }

    public boolean consumeDeepFreezeCharge() {
        if (deepFreezeCharges > 0) {
            deepFreezeCharges--;
            return true;
        }
        return false;
    }

    public int getDeepFreezeCharges() {
        return deepFreezeCharges;
    }

    public void setDeepFreezeCharges(int charges) {
        deepFreezeCharges = Math.max(0, charges);
    }

    public boolean isGhostFitEnabled() {
        return ghostFitEnabled;
    }

    public void setGhostFitEnabled(boolean ghostFitEnabled) {
        this.ghostFitEnabled = ghostFitEnabled;
    }

    public boolean isSniffTestEnabled() {
        return sniffTestEnabled;
    }

    public void setSniffTestEnabled(boolean sniffTestEnabled) {
        this.sniffTestEnabled = sniffTestEnabled;
    }

    public void addExtraTrashSlots(int slots) {
        this.extraTrashSlots += slots;
    }

    public int getExtraTrashSlots() {
        return extraTrashSlots;
    }

    public void addExtraTrayCapacity(int slots) {
        extraTrayCapacity += slots;
    }

    public int getExtraTrayCapacity() {
        return extraTrayCapacity;
    }

    public void addProduceTimerBonus(float seconds) {
        produceTimerBonusSeconds += seconds;
    }

    public float getProduceTimerBonusSeconds() {
        return produceTimerBonusSeconds;
    }

    public boolean isReinforcedShelvesEnabled() {
        return reinforcedShelvesEnabled;
    }

    public void setReinforcedShelvesEnabled(boolean enabled) {
        reinforcedShelvesEnabled = enabled;
    }

    public boolean isSmartThermostatEnabled() {
        return smartThermostatEnabled;
    }

    public void setSmartThermostatEnabled(boolean enabled) {
        smartThermostatEnabled = enabled;
    }

    public boolean isMealPrepPlanEnabled() {
        return mealPrepPlanEnabled;
    }

    public void setMealPrepPlanEnabled(boolean enabled) {
        mealPrepPlanEnabled = enabled;
    }

    public boolean isEfficientPackerEnabled() {
        return efficientPackerEnabled;
    }

    public void setEfficientPackerEnabled(boolean enabled) {
        efficientPackerEnabled = enabled;
    }

    public boolean isTrapScoutEnabled() {
        return trapScoutEnabled;
    }

    public void setTrapScoutEnabled(boolean enabled) {
        trapScoutEnabled = enabled;
    }

    public boolean isFifoCaddyEnabled() {
        return fifoCaddyEnabled;
    }

    public void setFifoCaddyEnabled(boolean enabled) {
        fifoCaddyEnabled = enabled;
    }

    public boolean consumeFifoCaddyCharge() {
        if (fifoCaddyCharges <= 0) {
            return false;
        }
        fifoCaddyCharges--;
        return true;
    }

    public void refreshFifoCaddyCharge() {
        fifoCaddyCharges = fifoCaddyEnabled ? 1 : 0;
    }

    public int getFifoCaddyCharges() {
        return fifoCaddyCharges;
    }

    public boolean isShelfRailsEnabled() {
        return shelfRailsEnabled;
    }

    public void setShelfRailsEnabled(boolean enabled) {
        shelfRailsEnabled = enabled;
    }

    public boolean isReusableContainersEnabled() {
        return reusableContainersEnabled;
    }

    public void setReusableContainersEnabled(boolean enabled) {
        reusableContainersEnabled = enabled;
    }

    public void addNextStageTrayBonus(int slots) {
        nextStageTrayBonus += Math.max(0, slots);
    }

    public int consumeNextStageTrayBonus() {
        int bonus = nextStageTrayBonus;
        nextStageTrayBonus = 0;
        return bonus;
    }

    public void addNextStageTimerPenalty(float seconds) {
        nextStageTimerPenalty += Math.max(0f, seconds);
    }

    public float consumeNextStageTimerPenalty() {
        float penalty = nextStageTimerPenalty;
        nextStageTimerPenalty = 0f;
        return penalty;
    }

    public boolean hasColdSnapThermostatSynergy() {
        return hasPowerUp("cold_snap") && hasPowerUp("smart_thermostat");
    }

    public boolean hasProduceCrisperSynergy() {
        return hasPowerUp("produce_saver") && hasPowerUp("crisper_drawer");
    }

    public boolean hasFreezeCaddySynergy() {
        return hasPowerUp("deep_freeze") && hasPowerUp("fifo_caddy");
    }

    public void addNextStageTimerBonus(float seconds) {
        nextStageTimerBonus += Math.max(0f, seconds);
    }

    public float consumeNextStageTimerBonus() {
        float bonus = nextStageTimerBonus;
        nextStageTimerBonus = 0f;
        return bonus;
    }

    public List<PowerUp> getAcquiredPowerUps() {
        return Collections.unmodifiableList(acquiredPowerUps);
    }

    public boolean hasPowerUp(String id) {
        return acquiredPowerUps.stream().anyMatch(powerUp -> powerUp.getId().equals(id));
    }

    public List<String> getActiveSynergies() {
        List<String> synergies = new ArrayList<>();
        if (hasPowerUp("cold_snap") && hasPowerUp("smart_thermostat")) {
            synergies.add("Cold Snap + Smart Thermostat: +3s to the most urgent grocery.");
        }
        if (hasPowerUp("produce_saver") && hasPowerUp("crisper_drawer")) {
            synergies.add("Produce Saver + Crisper Drawer: +2s to produce timers.");
        }
        if (hasPowerUp("deep_freeze") && hasPowerUp("fifo_caddy")) {
            synergies.add("Deep Freeze + FIFO Caddy: +1 Deep Freeze charge each stage.");
        }
        if (hasPowerUp("efficient_packer") && hasPowerUp("meal_prep_plan")) {
            synergies.add("Efficient Packer + Meal Prep: extra life for a clean meat-and-produce sort.");
        }
        return Collections.unmodifiableList(synergies);
    }

    public void reset() {
        coldSnapBonusSeconds = 0f;
        deepFreezeCharges = 0;
        ghostFitEnabled = false;
        sniffTestEnabled = false;
        extraTrashSlots = 0;
        extraTrayCapacity = 0;
        produceTimerBonusSeconds = 0f;
        reinforcedShelvesEnabled = false;
        smartThermostatEnabled = false;
        mealPrepPlanEnabled = false;
        efficientPackerEnabled = false;
        trapScoutEnabled = false;
        fifoCaddyEnabled = false;
        shelfRailsEnabled = false;
        reusableContainersEnabled = false;
        nextStageTrayBonus = 0;
        nextStageTimerBonus = 0f;
        nextStageTimerPenalty = 0f;
        fifoCaddyCharges = 0;
        acquiredPowerUps.clear();
    }
}
