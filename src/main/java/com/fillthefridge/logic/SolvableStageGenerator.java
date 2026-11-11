package com.fillthefridge.logic;

import com.fillthefridge.items.Item;
import com.fillthefridge.items.ItemCatalog;
import com.fillthefridge.items.TrapItem;
import com.fillthefridge.model.ActivePowerUps;
import com.fillthefridge.model.Fridge;
import com.fillthefridge.model.PlacementAction;
import com.fillthefridge.model.PlacementResult;
import com.fillthefridge.model.StageConfig;
import com.fillthefridge.util.GridPosition;
import com.fillthefridge.util.InvalidPlacementException;
import com.fillthefridge.util.StageGenerator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class SolvableStageGenerator implements StageGenerator {
    @Override
    public GeneratedStage generateStage(StageConfig config, ActivePowerUps activePowerUps) {
        Random random = new Random(config.getRandomSeed());
        int cols = config.getGridCols();
        int rows = config.getGridRows();

        Fridge buildFridge = new Fridge(cols, rows);
        buildFridge.setSpecialShelfRule(config.getSpecialShelfRow(), config.getSpecialShelfMaxWeight());
        List<PlacementAction> solutionPlacements = new ArrayList<>();
        List<Item> allTemplates = ItemCatalog.getAllGroceryTemplates();
        Set<String> selectedItemIds = new HashSet<>();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!buildFridge.getTile(c, r).isEmpty()) {
                    continue;
                }
                GridPosition targetPos = new GridPosition(c, r);
                Item chosenTemplate = selectCompatibleItem(
                    buildFridge,
                    allTemplates,
                    targetPos,
                    rows,
                    config,
                    random,
                    selectedItemIds
                );
                Item placedInstance = chosenTemplate.createFreshCopy();
                try {
                    buildFridge.placeItem(placedInstance, targetPos);
                    solutionPlacements.add(new PlacementAction(placedInstance, targetPos));
                    selectedItemIds.add(chosenTemplate.getId());
                } catch (InvalidPlacementException e) {
                    throw new IllegalStateException("Failed to tile guaranteed stage solution", e);
                }
            }
        }

        List<Item> stageItems = new ArrayList<>();
        PlacementAction prefilledPlacement = config.hasPrefilledItem()
            ? findPrefilledPlacement(solutionPlacements)
            : null;
        PlacementAction packingOrderPlacement = config.getStageNumber() >= 2
            ? findPackingOrderPlacement(solutionPlacements, prefilledPlacement)
            : null;
        float coldSnapBonus = activePowerUps != null ? activePowerUps.getColdSnapBonusSeconds() : 0f;
        boolean sniffTest = activePowerUps != null && activePowerUps.isSniffTestEnabled();

        for (PlacementAction action : solutionPlacements) {
            if (action == prefilledPlacement) {
                continue;
            }
            Item itemCopy = action.getItem().createFreshCopy();
            boolean shortLife = ItemCatalog.isShortLifeItem(itemCopy);
            itemCopy.applyTimerMultiplier(config.getTimerMultiplier(), shortLife);
            if (coldSnapBonus > 0f) {
                itemCopy.addBonusTime(coldSnapBonus);
            }
            stageItems.add(itemCopy);
        }

        List<TrapItem> trapTemplates = ItemCatalog.getAllTrapTemplates();
        for (int i = 0; i < config.getTrapCount(); i++) {
            TrapItem trapTemplate = trapTemplates.get(random.nextInt(trapTemplates.size()));
            TrapItem trapInstance = (TrapItem) trapTemplate.createFreshCopy();
            if (coldSnapBonus > 0f) {
                trapInstance.addBonusTime(coldSnapBonus);
            }
            if (sniffTest) {
                trapInstance.setRevealedTrap(true);
            }
            int insertIndex = stageItems.isEmpty() ? 0 : random.nextInt(stageItems.size() + 1);
            stageItems.add(insertIndex, trapInstance);
        }

        GeneratedStage generatedStage = new GeneratedStage(
            config,
            solutionPlacements,
            stageItems,
            prefilledPlacement,
            packingOrderPlacement
        );
        if (!verifySolvability(generatedStage, config)) {
            throw new IllegalStateException("Generated stage failed solvability verification!");
        }
        return generatedStage;
    }

    private PlacementAction findPrefilledPlacement(List<PlacementAction> placements) {
        return placements.stream()
            .filter(action -> action.getPosition().getRow() == 0)
            .filter(action -> action.getItem().getSize().getHeight() == 1)
            .filter(action -> isUniqueItemId(placements, action.getItem().getId()))
            .findFirst()
            .orElse(null);
    }

    private PlacementAction findPackingOrderPlacement(
        List<PlacementAction> placements,
        PlacementAction prefilledPlacement
    ) {
        return placements.stream()
            .filter(action -> action != prefilledPlacement)
            .filter(action -> isUniqueItemId(placements, action.getItem().getId()))
            .findFirst()
            .orElse(null);
    }

    private boolean isUniqueItemId(List<PlacementAction> placements, String itemId) {
        return placements.stream().filter(action -> action.getItem().getId().equals(itemId)).count() == 1;
    }

    private Item selectCompatibleItem(
        Fridge fridge,
        List<Item> templates,
        GridPosition targetPos,
        int totalRows,
        StageConfig config,
        Random random,
        Set<String> selectedItemIds
    ) {
        List<Item> weightedCandidates = new ArrayList<>();
        List<Item> unusedWeightedCandidates = new ArrayList<>();
        List<Item> fallbackCandidates = new ArrayList<>();
        List<Item> unusedFallbackCandidates = new ArrayList<>();

        for (Item candidate : templates) {
            if (!fridge.isCategoryAllowedInCompartment(candidate, targetPos, candidate.getSize())) {
                continue;
            }
            PlacementResult placementResult = fridge.evaluatePlacement(candidate, targetPos);
            if (placementResult != PlacementResult.LEGAL) {
                continue;
            }
            int topRowAfterPlacement = targetPos.getRow() + candidate.getSize().getHeight();
            boolean reachesCeiling = topRowAfterPlacement == totalRows;

            if (!reachesCeiling) {
                if (candidate.getMaxSupportWeight() < 1) {
                    continue;
                }
            }

            fallbackCandidates.add(candidate);
            if (!selectedItemIds.contains(candidate.getId())) {
                unusedFallbackCandidates.add(candidate);
            }

            int weightCopies = 2;
            if (candidate.getSize().getArea() > 1 && random.nextFloat() < config.getAwkwardShapeProbability()) {
                weightCopies += 4;
            }
            for (int i = 0; i < weightCopies; i++) {
                weightedCandidates.add(candidate);
                if (!selectedItemIds.contains(candidate.getId())) {
                    unusedWeightedCandidates.add(candidate);
                }
            }
        }

        if (!unusedWeightedCandidates.isEmpty()) {
            return unusedWeightedCandidates.get(random.nextInt(unusedWeightedCandidates.size()));
        }
        if (!weightedCandidates.isEmpty()) {
            return weightedCandidates.get(random.nextInt(weightedCandidates.size()));
        }
        if (!unusedFallbackCandidates.isEmpty()) {
            return unusedFallbackCandidates.get(random.nextInt(unusedFallbackCandidates.size()));
        }
        if (!fallbackCandidates.isEmpty()) {
            return fallbackCandidates.get(random.nextInt(fallbackCandidates.size()));
        }

        for (Item template : templates) {
            if (!fridge.isCategoryAllowedInCompartment(template, targetPos, template.getSize())) {
                continue;
            }
            if (template.getSize().getWidth() == 1
                && template.getSize().getHeight() == 1
                && template.getWeight() == 1
                && template.getMaxSupportWeight() >= 2) {
                return template;
            }
        }
        return templates.get(0);
    }

    @Override
    public boolean verifySolvability(GeneratedStage generatedStage, StageConfig config) {
        Fridge testFridge = new Fridge(config.getGridCols(), config.getGridRows());
        testFridge.setSpecialShelfRule(config.getSpecialShelfRow(), config.getSpecialShelfMaxWeight());
        for (PlacementAction action : generatedStage.getSolutionPlacements()) {
            Item testItem = action.getItem().createFreshCopy();
            PlacementResult eval = testFridge.evaluatePlacement(testItem, action.getPosition());
            if (eval != PlacementResult.LEGAL) {
                return false;
            }
            try {
                testFridge.placeItem(testItem, action.getPosition());
            } catch (InvalidPlacementException e) {
                return false;
            }
        }
        return testFridge.isEveryTileOccupiedByLegalItem()
            && !testFridge.hasTrapItems();
    }
}
