package com.fillthefridge.powerups;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class PowerUpPool {
    private final Random random;

    public PowerUpPool(long seed) {
        this.random = new Random(seed);
    }

    public static PowerUp createPowerUp(String id) {
        return switch (id) {
            case "cold_snap" -> new ColdSnapPowerUp();
            case "cold_snap_upgrade" -> new ColdSnapUpgradePowerUp();
            case "deep_freeze" -> new DeepFreezePowerUp();
            case "deep_freeze_upgrade" -> new DeepFreezeUpgradePowerUp();
            case "ghost_fit" -> new GhostFitPowerUp();
            case "sniff_test" -> new SniffTestPowerUp();
            case "trash_compactor" -> new TrashCompactorPowerUp();
            case "crisper_drawer" -> new CrisperDrawerPowerUp();
            case "produce_saver" -> new ProduceSaverPowerUp();
            case "produce_saver_upgrade" -> new ProduceSaverUpgradePowerUp();
            case "reinforced_shelves" -> new ReinforcedShelvesPowerUp();
            case "smart_thermostat" -> new SmartThermostatPowerUp();
            case "meal_prep_plan" -> new MealPrepPlanPowerUp();
            case "efficient_packer" -> new EfficientPackerPowerUp();
            case "trap_scout" -> new TrapScoutPowerUp();
            case "fifo_caddy" -> new FifoCaddyPowerUp();
            case "shelf_rails" -> new ShelfRailsPowerUp();
            case "reusable_containers" -> new ReusableContainersPowerUp();
            case "second_wind" -> new SecondWindPowerUp();
            case "hero_cold_storage_veteran" -> new ColdStorageVeteranPowerUp();
            case "hero_fifo_master" -> new FifoMasterPowerUp();
            case "hero_produce_whisperer" -> new ProduceWhispererPowerUp();
            case "hero_space_optimizer" -> new SpaceOptimizerPowerUp();
            case "hero_trap_hunter" -> new TrapHunterPowerUp();
            case "hero_deep_freeze_specialist" -> new DeepFreezeSpecialistPowerUp();
            case "hero_reinforced_stock" -> new ReinforcedStockPowerUp();
            case "hero_time_bender" -> new TimeBenderPowerUp();
            default -> throw new IllegalArgumentException("Unknown saved power-up: " + id);
        };
    }

    public List<PowerUp> generateChoices(int count) {
        return generateChoices(count, Collections.emptyList());
    }

    public List<PowerUp> generateChoices(int count, List<PowerUp> acquiredPowerUps) {
        return generateChoices(count, acquiredPowerUps, Set.of(
            "cold_snap", "deep_freeze", "ghost_fit", "sniff_test", "trash_compactor",
            "crisper_drawer", "produce_saver", "reinforced_shelves", "smart_thermostat",
            "meal_prep_plan", "efficient_packer", "trap_scout", "fifo_caddy", "shelf_rails",
            "reusable_containers", "second_wind", "cold_snap_upgrade", "deep_freeze_upgrade",
            "produce_saver_upgrade"
        ));
    }

    public List<PowerUp> generateChoices(
        int count,
        List<PowerUp> acquiredPowerUps,
        Set<String> unlockedPowerUpIds
    ) {
        List<PowerUp> standardChoices = new ArrayList<>();
        standardChoices.add(new ColdSnapPowerUp());
        standardChoices.add(new DeepFreezePowerUp());
        standardChoices.add(new GhostFitPowerUp());
        standardChoices.add(new SniffTestPowerUp());
        standardChoices.add(new TrashCompactorPowerUp());
        standardChoices.add(new CrisperDrawerPowerUp());
        standardChoices.add(new ProduceSaverPowerUp());
        standardChoices.add(new ReinforcedShelvesPowerUp());
        standardChoices.add(new SmartThermostatPowerUp());
        standardChoices.add(new MealPrepPlanPowerUp());
        standardChoices.add(new EfficientPackerPowerUp());
        standardChoices.add(new TrapScoutPowerUp());
        standardChoices.add(new FifoCaddyPowerUp());
        standardChoices.add(new ShelfRailsPowerUp());
        standardChoices.add(new ReusableContainersPowerUp());
        standardChoices.removeIf(powerUp -> !unlockedPowerUpIds.contains(powerUp.getId()));

        Set<String> unavailableIds = new HashSet<>();
        if (acquiredPowerUps != null) {
            for (PowerUp acquired : acquiredPowerUps) {
                unavailableIds.add(acquired.getId());
            }
        }
        standardChoices.removeIf(powerUp -> unavailableIds.contains(powerUp.getId()));
        addUpgradeIfAvailable(standardChoices, unavailableIds, unlockedPowerUpIds, "cold_snap", "cold_snap_upgrade",
            new ColdSnapUpgradePowerUp());
        addUpgradeIfAvailable(standardChoices, unavailableIds, unlockedPowerUpIds, "deep_freeze", "deep_freeze_upgrade",
            new DeepFreezeUpgradePowerUp());
        addUpgradeIfAvailable(standardChoices, unavailableIds, unlockedPowerUpIds, "produce_saver", "produce_saver_upgrade",
            new ProduceSaverUpgradePowerUp());
        Collections.shuffle(standardChoices, random);

        List<PowerUp> selected = new ArrayList<>();
        boolean includeRare = unlockedPowerUpIds.contains("second_wind") && !unavailableIds.contains("second_wind")
            && (standardChoices.isEmpty() || random.nextFloat() < 0.28f);
        if (includeRare) {
            selected.add(new SecondWindPowerUp());
        }

        for (PowerUp powerUp : standardChoices) {
            if (selected.size() >= count) {
                break;
            }
            selected.add(powerUp);
        }

        Collections.shuffle(selected, random);
        return selected.size() > count ? new ArrayList<>(selected.subList(0, count)) : selected;
    }

    private void addUpgradeIfAvailable(
        List<PowerUp> choices,
        Set<String> unavailableIds,
        Set<String> unlockedPowerUpIds,
        String baseId,
        String upgradeId,
        PowerUp upgrade
    ) {
        if (unlockedPowerUpIds.contains(upgradeId)
            && unavailableIds.contains(baseId)
            && !unavailableIds.contains(upgradeId)) {
            choices.add(upgrade);
        }
    }
}
