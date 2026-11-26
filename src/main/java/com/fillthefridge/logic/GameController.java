package com.fillthefridge.logic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.fillthefridge.events.ColdChainEventCard;
import com.fillthefridge.events.EventCard;
import com.fillthefridge.events.EventCardPool;
import com.fillthefridge.events.MysteryBoxEventCard;
import com.fillthefridge.events.PreBossEventCard;
import com.fillthefridge.events.PowerOutageEventCard;
import com.fillthefridge.events.ProduceClusterEventCard;
import com.fillthefridge.model.FridgeLayout;
import com.fillthefridge.items.Item;
import com.fillthefridge.items.ItemCatalog;
import com.fillthefridge.model.Fridge;
import com.fillthefridge.model.GamePhase;
import com.fillthefridge.model.GameState;
import com.fillthefridge.model.ItemCategory;
import com.fillthefridge.model.PlacementAction;
import com.fillthefridge.model.PlacementResult;
import com.fillthefridge.model.RouteRoomType;
import com.fillthefridge.model.StageConfig;
import com.fillthefridge.model.StageContract;
import com.fillthefridge.model.TrashBin;
import com.fillthefridge.model.Tray;
import com.fillthefridge.powerups.PowerUp;
import com.fillthefridge.powerups.PowerUpPool;
import com.fillthefridge.util.GridPosition;
import com.fillthefridge.util.InvalidPlacementException;
import com.fillthefridge.util.StageGenerator;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class GameController {
    public static final int BOSS_HEALTH = 5;
    public static final int UNDO_ACTIONS_PER_LIFE = 3;

    private final GameState gameState;
    private final DifficultyScalingStrategy difficultyStrategy;
    private final StageGenerator stageGenerator;
    private PowerUpPool powerUpPool;
    private final long baseSeed;
    private final List<Integer> selectedMapNodes;
    private List<RouteRoomType> currentRouteRoomTypes;

    private long runSeed;
    private GeneratedStage currentGeneratedStage;
    private List<PowerUp> currentPowerUpChoices;
    private List<Integer> currentMapNodeIndexes;
    private float stageClearBannerTimer;
    private int bossHealth;
    private StageContract currentStageContract;
    private boolean stageContractComplete;
    private boolean firstFoodPlacementChecked;
    private boolean shelfRailUsed;
    private boolean reusableContainerUsed;
    private String lastEducationalInsight;
    private String lastContractResult;
    private boolean inspectionRouteActive;
    private final List<String> recentlyUnlockedPowerUpIds;
    private int undoActionsSinceLifeLoss;

    // Event card system
    private EventCardPool eventCardPool;
    private EventCard currentEventCard;
    private PreBossEventCard preBossEventCard;

    private static final List<String> STARTING_POWER_UP_IDS = List.of(
        "cold_snap", "deep_freeze", "ghost_fit", "sniff_test", "trash_compactor",
        "crisper_drawer", "produce_saver", "reinforced_shelves", "smart_thermostat",
        "meal_prep_plan", "second_wind"
    );

    public GameController(
        GameState gameState,
        DifficultyScalingStrategy difficultyStrategy,
        StageGenerator stageGenerator,
        long baseSeed
    ) {
        this.gameState = gameState;
        this.difficultyStrategy = difficultyStrategy;
        this.stageGenerator = stageGenerator;
        this.baseSeed = baseSeed;
        this.runSeed = baseSeed;
        this.powerUpPool = new PowerUpPool(baseSeed);
        this.currentPowerUpChoices = new ArrayList<>();
        this.currentMapNodeIndexes = new ArrayList<>();
        this.selectedMapNodes = new ArrayList<>();
        this.currentRouteRoomTypes = new ArrayList<>();
        this.recentlyUnlockedPowerUpIds = new ArrayList<>();
        this.bossHealth = BOSS_HEALTH;
        this.undoActionsSinceLifeLoss = 0;
    }

    public void startNewRun() {
        startNewRun(null);
    }

    public void startNewRun(PowerUp startingPowerUp) {
        gameState.resetRun();
        undoActionsSinceLifeLoss = 0;
        runSeed = ThreadLocalRandom.current().nextLong() ^ baseSeed;
        powerUpPool = new PowerUpPool(runSeed);
        eventCardPool = new EventCardPool(runSeed);
        currentEventCard = null;
        bossHealth = BOSS_HEALTH;
        inspectionRouteActive = false;
        recentlyUnlockedPowerUpIds.clear();
        selectedMapNodes.clear();
        currentPowerUpChoices.clear();
        currentMapNodeIndexes.clear();
        currentRouteRoomTypes.clear();
        currentStageContract = StageContract.PRIORITIZE_PERISHABLE;
        if (startingPowerUp != null) {
            startingPowerUp.apply(gameState);
        }
        startStage(1);
    }

    public void startTutorialRun() {
        startNewRun();
        List<Item> tutorialItems = new ArrayList<>();
        List<Item> groceries = ItemCatalog.getAllGroceryTemplates();
        tutorialItems.add(requireItem(groceries, "soda_can").createFreshCopy());
        tutorialItems.add(requireItem(groceries, "tomato").createFreshCopy());
        tutorialItems.add(requireItem(groceries, "sausage").createFreshCopy());

        Item trashItem = ItemCatalog.getAllTrapTemplates().stream()
            .filter(item -> item.getId().equals("empty_can"))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Tutorial trash item is missing."));
        tutorialItems.add(trashItem.createFreshCopy());

        gameState.getTray().loadStageItems(tutorialItems, tutorialItems.size(), false);
        gameState.getTray().addBonusTimeToAll(120f);
    }

    public boolean hasSavedProgress() {
        if (Gdx.app == null) {
            return false;
        }
        return Gdx.app.getPreferences("fill-the-fridge-save").getBoolean("available", false);
    }

    public boolean saveProgress() {
        if (Gdx.app == null) {
            return false;
        }
        if (gameState.getCurrentPhase() == GamePhase.EDUCATIONAL_FEEDBACK) {
            if (isBossStage() && bossHealth <= 0) {
                return false;
            }
            continueAfterFeedback();
        } else if (gameState.getCurrentPhase() != GamePhase.MAP_SELECTION) {
            return false;
        }

        boolean bossCheckpoint = gameState.getCurrentStageNumber() == ProgressiveDifficultyStrategy.FINAL_STAGE
            && gameState.getCurrentPhase() == GamePhase.PLAYING;
        if (!bossCheckpoint && gameState.getCurrentPhase() != GamePhase.MAP_SELECTION) {
            return false;
        }

        Preferences preferences = Gdx.app.getPreferences("fill-the-fridge-save");
        preferences.putInteger("version", 1);
        preferences.putInteger("stage", gameState.getCurrentStageNumber());
        preferences.putLong("runSeed", runSeed);
        preferences.putInteger("bossHealth", bossHealth);
        preferences.putInteger("lives", gameState.getPlayerLives().getCurrentLives());
        preferences.putInteger("undoActionsSinceLifeLoss", undoActionsSinceLifeLoss);
        preferences.putInteger("deepFreezeCharges", gameState.getActivePowerUps().getDeepFreezeCharges());
        preferences.putBoolean("bossCheckpoint", bossCheckpoint);
        preferences.putString("selectedMapNodes", joinIntegers(selectedMapNodes));
        preferences.putString("mapNodes", bossCheckpoint ? "" : joinIntegers(currentMapNodeIndexes));
        preferences.putString("powerUps", joinPowerUpIds(gameState.getActivePowerUps().getAcquiredPowerUps()));
        preferences.putString("choices", bossCheckpoint ? "" : joinPowerUpIds(currentPowerUpChoices));
        preferences.putBoolean("available", true);
        preferences.flush();
        return true;
    }

    public boolean resumeSavedProgress() {
        if (Gdx.app == null) {
            return false;
        }
        Preferences preferences = Gdx.app.getPreferences("fill-the-fridge-save");
        if (!preferences.getBoolean("available", false)) {
            return false;
        }
        if (preferences.getInteger("version", -1) != 1) {
            throw new IllegalStateException("The saved run uses an unsupported save format.");
        }

        int savedStage = preferences.getInteger("stage", -1);
        int savedBossHealth = preferences.getInteger("bossHealth", -1);
        int savedLives = preferences.getInteger("lives", -1);
        int savedUndoActionsSinceLifeLoss = preferences.getInteger("undoActionsSinceLifeLoss", 0);
        int savedDeepFreezeCharges = preferences.getInteger("deepFreezeCharges", 0);
        long savedRunSeed = preferences.getLong("runSeed", 0L);
        boolean bossCheckpoint = preferences.getBoolean("bossCheckpoint", false);
        List<Integer> savedRoute = parseIntegers(preferences.getString("selectedMapNodes", ""));
        List<Integer> savedMapNodes = parseIntegers(preferences.getString("mapNodes", ""));
        List<String> savedPowerUps = parseStrings(preferences.getString("powerUps", ""));
        List<String> savedChoices = parseStrings(preferences.getString("choices", ""));

        if (savedStage < 1 || savedStage > ProgressiveDifficultyStrategy.FINAL_STAGE
            || savedBossHealth < 0 || savedBossHealth > BOSS_HEALTH
            || savedLives < 1 || savedLives > gameState.getPlayerLives().getMaxLives()
            || savedUndoActionsSinceLifeLoss < 0
            || savedUndoActionsSinceLifeLoss >= UNDO_ACTIONS_PER_LIFE
            || savedDeepFreezeCharges < 0
            || savedRoute.size() != savedStage - 1
            || savedRoute.stream().anyMatch(node -> node < 0 || node > 2)) {
            throw new IllegalStateException("The saved run data is invalid.");
        }
        if (bossCheckpoint && (savedStage != ProgressiveDifficultyStrategy.FINAL_STAGE || savedBossHealth == 0)) {
            throw new IllegalStateException("The saved boss checkpoint is invalid.");
        }
        if (!bossCheckpoint && (savedMapNodes.isEmpty() || savedMapNodes.size() != savedChoices.size()
            || savedMapNodes.size() > 3
            || savedMapNodes.stream().anyMatch(node -> node < 0 || node > 2))) {
            throw new IllegalStateException("The saved route choices are invalid.");
        }

        List<PowerUp> restoredPowerUps = new ArrayList<>();
        for (String id : savedPowerUps) {
            restoredPowerUps.add(PowerUpPool.createPowerUp(id));
        }
        List<PowerUp> restoredChoices = new ArrayList<>();
        for (String id : savedChoices) {
            restoredChoices.add(PowerUpPool.createPowerUp(id));
        }

        gameState.resetRun();
        runSeed = savedRunSeed;
        powerUpPool = new PowerUpPool(runSeed);
        bossHealth = savedBossHealth;
        undoActionsSinceLifeLoss = savedUndoActionsSinceLifeLoss;
        selectedMapNodes.clear();
        selectedMapNodes.addAll(savedRoute);
        currentMapNodeIndexes = new ArrayList<>(savedMapNodes);
        currentPowerUpChoices = restoredChoices;
        currentRouteRoomTypes = new ArrayList<>();
        int nextStage = savedStage + 1;
        for (int node : savedMapNodes) {
            currentRouteRoomTypes.add(RouteRoomType.forNode(nextStage, node));
        }
        for (PowerUp powerUp : restoredPowerUps) {
            powerUp.apply(gameState);
        }
        gameState.getActivePowerUps().setDeepFreezeCharges(savedDeepFreezeCharges);
        restoreLives(savedLives);
        gameState.setCurrentStageNumber(savedStage);

        if (bossCheckpoint) {
            currentStageContract = StageContract.COLD_CHAIN;
            startStage(ProgressiveDifficultyStrategy.FINAL_STAGE);
        } else {
            currentStageContract = StageContract.PRIORITIZE_PERISHABLE;
            stageContractComplete = true;
            gameState.setCurrentPhase(GamePhase.MAP_SELECTION);
        }
        return true;
    }

    private void restoreLives(int lives) {
        gameState.getPlayerLives().reset();
        int difference = lives - gameState.getPlayerLives().getCurrentLives();
        if (difference < 0) {
            gameState.getPlayerLives().loseLives(-difference, "Restored saved progress.");
        } else if (difference > 0) {
            gameState.getPlayerLives().healLives(difference, "Restored saved progress.");
        }
    }

    private String joinIntegers(List<Integer> values) {
        return values.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(","));
    }

    private String joinPowerUpIds(List<PowerUp> powerUps) {
        return powerUps.stream().map(PowerUp::getId)
            .collect(java.util.stream.Collectors.joining(","));
    }

    private List<Integer> parseIntegers(String encoded) {
        List<Integer> values = new ArrayList<>();
        for (String value : parseStrings(encoded)) {
            try {
                values.add(Integer.parseInt(value));
            } catch (NumberFormatException exception) {
                throw new IllegalStateException("The saved route data is invalid.", exception);
            }
        }
        return values;
    }

    private List<String> parseStrings(String encoded) {
        if (encoded.isEmpty()) {
            return List.of();
        }
        return List.of(encoded.split(",", -1));
    }

    private Item requireItem(List<Item> items, String id) {
        return items.stream()
            .filter(item -> item.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Tutorial item is missing: " + id));
    }

    public void startStage(int stageNumber) {
        gameState.setCurrentStageNumber(stageNumber);
        gameState.getUndoStack().clear();

        // Draw event card for this stage (skip boss stages)
        if (stageNumber != ProgressiveDifficultyStrategy.FINAL_STAGE && eventCardPool != null) {
            if (currentEventCard != null) {
                currentEventCard.onStageEnd();
            }
            currentEventCard = eventCardPool.drawEventCard(stageNumber, runSeed ^ stageNumber);
            if (currentEventCard != null) {
                gameState.notifyWarningMessage("EVENT: " + currentEventCard.getName() + " — " + currentEventCard.getDescription());
            }
        } else {
            if (currentEventCard != null) {
                currentEventCard.onStageEnd();
                currentEventCard = null;
            }
        }

        StageConfig config = difficultyStrategy.createStageConfig(stageNumber, runSeed);
        if (stageNumber == ProgressiveDifficultyStrategy.FINAL_STAGE) {
            config = configureBossPhase(config);
            currentStageContract = StageContract.COLD_CHAIN;
        }
        currentGeneratedStage = stageGenerator.generateStage(config, gameState.getActivePowerUps());
        float routeTimerBonus = gameState.getActivePowerUps().consumeNextStageTimerBonus();
        float routeTimerPenalty = gameState.getActivePowerUps().consumeNextStageTimerPenalty();
        float produceTimerBonus = gameState.getActivePowerUps().getProduceTimerBonusSeconds();
        if (gameState.getActivePowerUps().hasProduceCrisperSynergy()) {
            produceTimerBonus += 2f;
        }
        for (Item item : currentGeneratedStage.getStageItems()) {
            if (item.getCategory() == ItemCategory.VEGETABLE) {
                item.addBonusTime(produceTimerBonus);
            }
            if (!item.isTrap()) {
                item.addBonusTime(routeTimerBonus);
                item.addBonusTime(-routeTimerPenalty);
            }
        }

        // Select a random fridge layout for this stage
        FridgeLayout layout = selectFridgeLayout(stageNumber);
        gameState.getFridge().configureGrid(config.getGridCols(), config.getGridRows(), layout);
        if (layout != FridgeLayout.STANDARD) {
            gameState.notifyWarningMessage("Fridge layout: " + layout.getDisplayName());
        }
        gameState.getFridge().setReinforcedShelves(gameState.getActivePowerUps().isReinforcedShelvesEnabled());
        gameState.getFridge().setSpecialShelfRule(config.getSpecialShelfRow(), config.getSpecialShelfMaxWeight());
        PlacementAction packingOrder = currentGeneratedStage.getPackingOrderPlacement();
        if (packingOrder != null) {
            gameState.getFridge().setPackingOrderTarget(
                packingOrder.getPosition(),
                packingOrder.getItem().getId(),
                packingOrder.getItem().getDisplayName()
            );
        }
        PlacementAction prefilled = currentGeneratedStage.getPrefilledPlacement();
        if (prefilled != null) {
            Item fixedItem = prefilled.getItem().createFreshCopy();
            try {
                gameState.getFridge().placeItem(fixedItem, prefilled.getPosition());
                fixedItem.setConfirmed(true);
                gameState.getFridge().setHasPrefilledItem(true);
                gameState.getFridge().refreshAllTileStates();
            } catch (InvalidPlacementException exception) {
                throw new IllegalStateException("Generated pre-filled grocery could not be placed.", exception);
            }
        }
        gameState.getTrashBin().resetForStage(
            config.getTrashSlots(),
            gameState.getActivePowerUps().getExtraTrashSlots()
        );
        gameState.getTray().loadStageItems(
            currentGeneratedStage.getStageItems(),
            config.getTrayCapacity() + gameState.getActivePowerUps().getExtraTrayCapacity()
                + gameState.getActivePowerUps().consumeNextStageTrayBonus(),
            gameState.getActivePowerUps().isSniffTestEnabled()
        );
        if (gameState.getActivePowerUps().isTrapScoutEnabled()
            && !gameState.getActivePowerUps().isSniffTestEnabled()) {
            gameState.getTray().revealNextTrap();
        }
        if (gameState.getActivePowerUps().isSmartThermostatEnabled()) {
            gameState.getTray().addBonusTimeToMostUrgent(
                gameState.getActivePowerUps().hasColdSnapThermostatSynergy() ? 8f : 5f
            );
        }
        if (gameState.getActivePowerUps().consumePendingInitialDeepFreeze()) {
            Item urgent = gameState.getTray().findMostUrgentItem();
            if (urgent != null) {
                urgent.applyDeepFreeze(3.0f);
            }
        }
        gameState.getActivePowerUps().refreshFifoCaddyCharge();
        if (gameState.getActivePowerUps().hasFreezeCaddySynergy()) {
            gameState.getActivePowerUps().addDeepFreezeCharge(1);
        }
        shelfRailUsed = false;
        reusableContainerUsed = false;
        firstFoodPlacementChecked = false;
        stageContractComplete = currentStageContract != StageContract.PRIORITIZE_PERISHABLE;

        // Apply event card after stage is set up
        if (currentEventCard != null) {
            currentEventCard.apply(gameState);
        }

        gameState.setCurrentPhase(GamePhase.PLAYING);
        stageClearBannerTimer = 0f;
    }

    private StageConfig configureBossPhase(StageConfig config) {
        int traps = config.getTrapCount();
        int trashSlots = config.getTrashSlots();
        int trayCapacity = config.getTrayCapacity();
        float timerMultiplier = config.getTimerMultiplier();
        float awkwardShapeProb = config.getAwkwardShapeProbability();

        // 5 boss phases, each with escalating difficulty
        switch (bossHealth) {
            case 5 -> {
                // Phase 1: Rush Order — 30% less timer, more awkward shapes
                timerMultiplier *= 0.70f;
                awkwardShapeProb = Math.min(1f, awkwardShapeProb + 0.2f);
            }
            case 4 -> {
                // Phase 2: Crowded Tray — reduced tray capacity, faster timers
                trayCapacity = Math.max(2, trayCapacity - 1);
                timerMultiplier *= 0.85f;
            }
            case 3 -> {
                // Phase 3: Trap Wave — extra traps, extra trash slots needed
                traps += 2;
                trashSlots += 1;
                timerMultiplier *= 0.90f;
            }
            case 2 -> {
                // Phase 4: Overstocked — even more items, tighter space
                trayCapacity = Math.max(2, trayCapacity - 2);
                traps += 1;
                timerMultiplier *= 0.80f;
                awkwardShapeProb = Math.min(1f, awkwardShapeProb + 0.3f);
            }
            case 1 -> {
                // Phase 5: Final Stand — everything at once
                traps += 2;
                trashSlots += 1;
                timerMultiplier *= 0.70f;
                awkwardShapeProb = Math.min(1f, awkwardShapeProb + 0.4f);
            }
            default -> {
                // Boss defeated
            }
        }

        return new StageConfig(
            config.getStageNumber(), config.getGridCols(), config.getGridRows(),
            traps, trayCapacity, trashSlots, timerMultiplier,
            awkwardShapeProb, config.getRandomSeed(),
            config.hasPrefilledItem(), config.getSpecialShelfRow(), config.getSpecialShelfMaxWeight()
        );
    }

    public void update(float deltaTime) {
        if (gameState.getCurrentPhase() == GamePhase.STAGE_CLEAR) {
            gameState.getFridge().update(deltaTime);
            stageClearBannerTimer -= deltaTime;
            if (stageClearBannerTimer <= 0f) {
                gameState.setCurrentPhase(GamePhase.EDUCATIONAL_FEEDBACK);
            }
            return;
        }
        if (gameState.getCurrentPhase() != GamePhase.PLAYING) {
            return;
        }

        // Update event card (e.g., Power Outage timer)
        if (currentEventCard != null) {
            currentEventCard.update(deltaTime);
        }

        gameState.getFridge().update(deltaTime);
        gameState.getTrashBin().update(deltaTime);
        gameState.getTray().update(deltaTime);

        checkTrayTimersAndSpoilage();

        if (gameState.getPlayerLives().isDepleted()) {
            gameState.setCurrentPhase(GamePhase.GAME_OVER);
            return;
        }

        if (gameState.isStageWinConditionMet()) {
            confirmPlacements();
        }
    }

    private void checkTrayTimersAndSpoilage() {
        Tray tray = gameState.getTray();
        List<Item> snapshot = new ArrayList<>(tray.getActiveItems());

        for (Item item : snapshot) {
            if (item.isWarningThresholdReached() && !item.isWarningAnnounced()) {
                item.setWarningAnnounced(true);
                String warning = item.onTimerWarning();
                gameState.notifyWarningMessage(warning);
            }

            if (item.isSpoiledAfterGrace()) {
                if (!item.isTrap() && currentStageContract == StageContract.COLD_CHAIN) {
                    stageContractComplete = false;
                }
                gameState.notifyItemSpoiled(item);
                gameState.getPlayerLives().loseLives(1, item.getDisplayName() + " spoiled outside the fridge!");

                if (gameState.getPlayerLives().isDepleted()) {
                    return;
                }

                if (item.isTrap()) {
                    tray.removeActiveItem(item);
                    revealNextTrapIfScouting(tray);
                } else {
                    item.resetTimerToFull();
                    gameState.notifyWarningMessage("Fresh replacement added for spoiled " + item.getDisplayName() + "!");
                }
            }
        }
    }

    public void selectTrayItem(Item item, boolean holdShift) {
        if (gameState.getCurrentPhase() != GamePhase.PLAYING) {
            return;
        }
        Tray tray = gameState.getTray();
        if (tray.getSelectedItem() == item) {
            // When holding Shift, keep the item selected instead of toggling off
            if (!holdShift) {
                tray.clearSelection();
            }
        } else {
            tray.selectItem(item);
            // Reveal mystery box item when selected
            if (currentEventCard instanceof MysteryBoxEventCard mysteryBox) {
                if (mysteryBox.getMysteryItem() == item) {
                    mysteryBox.revealItem();
                    gameState.notifyWarningMessage("Mystery revealed: " + item.getDisplayName() + "!");
                }
            }
        }
    }

    public void selectTrayItem(Item item) {
        selectTrayItem(item, false);
    }

    public boolean handleFridgeLeftClick(GridPosition clickedPosition) {
        if (gameState.getCurrentPhase() != GamePhase.PLAYING || clickedPosition == null) {
            return false;
        }
        Tray tray = gameState.getTray();
        Fridge fridge = gameState.getFridge();
        Item selected = tray.getSelectedItem();

        if (selected == null) {
            Item existing = fridge.getItemAt(clickedPosition);
            if (existing != null) {
                if (existing.isTrap() || !existing.isConfirmed()) {
                    pickupItemFromFridge(existing);
                    return true;
                } else if (gameState.getActivePowerUps().isShelfRailsEnabled() && !shelfRailUsed) {
                    shelfRailUsed = true;
                    pickupItemFromFridge(existing);
                    gameState.notifyWarningMessage(
                        "Shelf Rails used: rethink the placement while keeping food safely separated."
                    );
                    return true;
                } else {
                    gameState.notifyWarningMessage(existing.getDisplayName() + " is locked in place!");
                    return false;
                }
            }
            return false;
        }

        Item occupantAtClick = fridge.getItemAt(clickedPosition);
        if (occupantAtClick != null && occupantAtClick.isTrap()) {
            pickupItemFromFridge(occupantAtClick);
            return true;
        }

        try {
            attemptPlaceSelectedItem(selected, clickedPosition);
            return true;
        } catch (InvalidPlacementException e) {
            if (currentStageContract == StageContract.CLEAN_HANDLING) {
                stageContractComplete = false;
            }
            gameState.notifyWarningMessage(e.getMessage());
            return false;
        }
    }

    private void pickupItemFromFridge(Item itemInFridge) {
        Fridge fridge = gameState.getFridge();
        Tray tray = gameState.getTray();

        fridge.removeItem(itemInFridge);
        returnUnsupportedItemsToTray();
        tray.returnItemToTray(itemInFridge);
        tray.selectItem(itemInFridge);

        if (itemInFridge.isTrap()) {
            gameState.notifyWarningMessage("Selected trap " + itemInFridge.getDisplayName() + " - Click Trash Bin!");
        } else {
            gameState.notifyWarningMessage("Picked up " + itemInFridge.getDisplayName() + " from fridge.");
        }
    }

    private void returnUnsupportedItemsToTray() {
        Fridge fridge = gameState.getFridge();
        Tray tray = gameState.getTray();
        boolean changed = true;
        while (changed) {
            List<Item> unsupported = fridge.findUnsupportedItems();
            if (unsupported.isEmpty()) {
                changed = false;
            } else {
                for (Item floating : unsupported) {
                    fridge.removeItem(floating);
                    tray.returnItemToTray(floating);
                }
            }
        }
    }

    private void attemptPlaceSelectedItem(Item selected, GridPosition targetPos) throws InvalidPlacementException {
        Fridge fridge = gameState.getFridge();
        GridPosition requiredTarget = fridge.getPackingOrderTarget();
        if (requiredTarget != null && !selected.isTrap()) {
            if (selected.getId().equals(fridge.getPackingOrderItemId())) {
                if (!requiredTarget.equals(targetPos)) {
                    throw new InvalidPlacementException(
                        selected.getDisplayName() + " must go in the marked packing-order slot.",
                        targetPos
                    );
                }
            } else if (targetPos.getCol() <= requiredTarget.getCol()
                && targetPos.getCol() + selected.getSize().getWidth() > requiredTarget.getCol()
                && targetPos.getRow() <= requiredTarget.getRow()
                && targetPos.getRow() + selected.getSize().getHeight() > requiredTarget.getRow()) {
                throw new InvalidPlacementException(
                    "Place " + fridge.getPackingOrderItemName() + " in its marked slot first.",
                    targetPos
                );
            }
        }
        if (fridge.isWithinBounds(targetPos, selected.getSize())
            && !fridge.isCategoryAllowedInCompartment(selected, targetPos, selected.getSize())) {
            throw new InvalidPlacementException(
                selected.getDisplayName() + " belongs in the "
                    + fridge.getCompartmentFor(selected).getDisplayName().toLowerCase()
                    + " compartment. Sorting by category keeps the fridge organized.",
                targetPos
            );
        }
        PlacementResult result = fridge.evaluatePlacement(selected, targetPos);

        if (result == PlacementResult.UNSTABLE) {
            throw new InvalidPlacementException(
                "Unstable placement! Check grid bounds, empty tiles, and weight support below.",
                targetPos
            );
        }

        if (!selected.isTrap() && !firstFoodPlacementChecked) {
            firstFoodPlacementChecked = true;
            if (currentStageContract == StageContract.PRIORITIZE_PERISHABLE) {
                stageContractComplete = gameState.getTray().findMostUrgentItem() == selected;
            }
        }
        fridge.placeItem(selected, targetPos);
        gameState.getTray().removeActiveItem(selected);
        gameState.getUndoStack().push(new PlacementAction(selected, targetPos));

        // Check synergy event bonuses
        if (currentEventCard != null && !selected.isTrap()) {
            if (currentEventCard instanceof ColdChainEventCard coldChain) {
                float bonus = coldChain.checkPlacementBonus(selected, gameState);
                if (bonus > 0f) {
                    gameState.notifyWarningMessage("Cold Chain bonus: +" + bonus + "s to adjacent items!");
                }
            } else if (currentEventCard instanceof ProduceClusterEventCard produceCluster) {
                float bonus = produceCluster.checkPlacementBonus(selected, gameState);
                if (bonus > 0f) {
                    gameState.notifyWarningMessage("Produce Cluster bonus: +" + bonus + "s to adjacent vegetables!");
                }
            }
        }

        warnIfTrashPlaced(selected);
        checkWinAfterAction();
    }

    private void warnIfTrashPlaced(Item item) {
        if (item.isTrap()) {
            gameState.notifyWarningMessage(
                "Trash does not belong in the fridge. Use UNDO or right-click to remove it; 1 life is used per 3 undos."
            );
        }
    }

    public boolean undoLastPlacement() {
        if (gameState.getCurrentPhase() != GamePhase.PLAYING) {
            return false;
        }
        Optional<PlacementAction> next = gameState.getUndoStack().peek();
        if (next.isEmpty()) {
            gameState.notifyWarningMessage("No unconfirmed placement to undo.");
            return false;
        }
        PlacementAction action = next.get();
        Item item = action.getItem();
        if (item.isConfirmed()) {
            gameState.notifyWarningMessage("Placement is already locked!");
            return false;
        }
        gameState.getUndoStack().pop();
        gameState.getFridge().removeItem(item);
        returnUnsupportedItemsToTray();
        gameState.getTray().returnItemToTray(item);
        gameState.getTray().selectItem(item);
        undoActionsSinceLifeLoss++;
        if (undoActionsSinceLifeLoss == UNDO_ACTIONS_PER_LIFE) {
            undoActionsSinceLifeLoss = 0;
            gameState.notifyWarningMessage("Undid " + item.getDisplayName() + ". Three undos used: lost 1 life.");
            gameState.getPlayerLives().loseLives(1, "Three undos used.");
        } else {
            gameState.notifyWarningMessage(
                "Undid " + item.getDisplayName() + ". "
                    + getUndoCountUntilLifeLoss() + " undo(s) until the next life is used."
            );
        }
        return true;
    }

    public int getUndoCountUntilLifeLoss() {
        return UNDO_ACTIONS_PER_LIFE - undoActionsSinceLifeLoss;
    }

    public void confirmPlacements() {
        if (gameState.getCurrentPhase() != GamePhase.PLAYING) {
            return;
        }

        if (gameState.isStageWinConditionMet()) {
            finishStage();
        } else {
            gameState.getFridge().confirmAllPlacements();
            gameState.getUndoStack().clear();
        }

        if (gameState.getCurrentPhase() != GamePhase.PLAYING) {
            return;
        }
        if (gameState.getFridge().hasTrapItems()) {
            gameState.notifyWarningMessage("Locked legal items! Remove and trash trap items in the fridge to finish.");
        } else {
            gameState.notifyWarningMessage("Placements locked!");
        }
    }

    public boolean trashSelectedItem() {
        if (gameState.getCurrentPhase() != GamePhase.PLAYING) {
            return false;
        }
        Tray tray = gameState.getTray();
        TrashBin trashBin = gameState.getTrashBin();
        Item selected = tray.getSelectedItem();

        if (selected == null) {
            gameState.notifyWarningMessage("Select a trap item first, then click the Trash Bin!");
            return false;
        }
        if (!selected.isTrap()) {
            if (currentStageContract == StageContract.CLEAN_HANDLING) {
                stageContractComplete = false;
            }
            gameState.notifyWarningMessage(
                "Do not throw fresh food away: it wastes food. Only trap items belong in the bin."
            );
            return false;
        }

        try {
            trashBin.trashItem(selected);
            tray.removeActiveItem(selected);
            revealNextTrapIfScouting(tray);

            gameState.notifyWarningMessage("Trashed " + selected.getDisplayName() + "!");
            checkWinAfterAction();
            return true;
        } catch (InvalidPlacementException e) {
            gameState.notifyWarningMessage(e.getMessage());
            return false;
        }
    }

    public boolean activateManualDeepFreeze() {
        if (gameState.getCurrentPhase() != GamePhase.PLAYING) {
            return false;
        }
        if (gameState.getActivePowerUps().getDeepFreezeCharges() <= 0) {
            gameState.notifyWarningMessage("No Deep Freeze charges available!");
            return false;
        }
        Item target = gameState.getTray().getSelectedItem();
        if (target == null) {
            target = gameState.getTray().findMostUrgentItem();
        }
        if (target != null && gameState.getActivePowerUps().consumeDeepFreezeCharge()) {
            target.applyDeepFreeze(3.0f);
            gameState.notifyWarningMessage("Deep Freeze paused " + target.getDisplayName() + " for 3s!");
            return true;
        }
        return false;
    }

    public boolean activateFifoCaddy() {
        if (gameState.getCurrentPhase() != GamePhase.PLAYING
            || !gameState.getActivePowerUps().isFifoCaddyEnabled()) {
            return false;
        }
        Item urgent = gameState.getTray().findMostUrgentItem();
        if (urgent == null || !gameState.getActivePowerUps().consumeFifoCaddyCharge()) {
            gameState.notifyWarningMessage("The FIFO Caddy has no charge left this stage.");
            return false;
        }
        urgent.applyDeepFreeze(4f);
        gameState.notifyWarningMessage(
            "FIFO Caddy paused " + urgent.getDisplayName()
                + ". A short pause gives you time to plan its safe placement."
        );
        return true;
    }

    public void rotateSelectedItem() {
        if (gameState.getCurrentPhase() != GamePhase.PLAYING
            || !gameState.getActivePowerUps().isReusableContainersEnabled()) {
            return;
        }
        Item selected = gameState.getTray().getSelectedItem();
        if (selected == null || selected.isTrap()) {
            gameState.notifyWarningMessage("Select a rectangular grocery before rotating it.");
            return;
        }
        if (reusableContainerUsed || !selected.rotateFootprint()) {
            gameState.notifyWarningMessage("Reusable Containers can rotate one rectangular grocery per stage.");
            return;
        }
        reusableContainerUsed = true;
        gameState.notifyWarningMessage(
            selected.getDisplayName() + " rotated. Make sure the new footprint fits its safe compartment."
        );
    }

    public void choosePowerUp(PowerUp chosenPowerUp) {
        GamePhase phase = gameState.getCurrentPhase();
        if ((phase != GamePhase.POWERUP_SELECTION && phase != GamePhase.MAP_SELECTION)
            || chosenPowerUp == null) {
            return;
        }
        int choiceIndex = currentPowerUpChoices.indexOf(chosenPowerUp);
        if (choiceIndex < 0 || (phase == GamePhase.MAP_SELECTION
            && (choiceIndex >= currentMapNodeIndexes.size()
                || choiceIndex >= currentRouteRoomTypes.size()))) {
            return;
        }
        chosenPowerUp.apply(gameState);
        int nextStage = gameState.getCurrentStageNumber() + 1;
        if (phase == GamePhase.MAP_SELECTION) {
            int selectedNode = currentMapNodeIndexes.get(choiceIndex);
            selectedMapNodes.add(selectedNode);
            RouteRoomType roomType = currentRouteRoomTypes.get(choiceIndex);
            currentStageContract = roomType.getContract();
            switch (roomType) {
                case GROCERY_RUN -> gameState.getActivePowerUps().addNextStageTimerBonus(3f);
                case MARKET -> gameState.getActivePowerUps().addNextStageTrayBonus(1);
                case REST_STOP -> gameState.getPlayerLives().healLives(1, "Rest Stop recovered 1 life.");
                case INSPECTION -> gameState.getActivePowerUps().addNextStageTimerPenalty(1f);
            }
            inspectionRouteActive = roomType == RouteRoomType.INSPECTION;
        }
        if (nextStage == ProgressiveDifficultyStrategy.FINAL_STAGE) {
            gameState.setCurrentStageNumber(nextStage);
            // Create the pre-boss event card with the run seed
            preBossEventCard = new PreBossEventCard(runSeed);
            gameState.setCurrentPhase(GamePhase.PRE_BOSS_EVENT);
        } else {
            startStage(nextStage);
        }
    }

    /**
     * Called when the player confirms their choice on the pre-boss event screen.
     * Applies the selected choice and transitions to the boss intro.
     */
    public void confirmPreBossEvent() {
        if (gameState.getCurrentPhase() != GamePhase.PRE_BOSS_EVENT || preBossEventCard == null) {
            return;
        }
        preBossEventCard.applySelectedChoice(gameState);
        preBossEventCard = null;
        gameState.setCurrentPhase(GamePhase.BOSS_INTRO);
    }

    public PreBossEventCard getPreBossEventCard() {
        return preBossEventCard;
    }

    public void startBossStage() {
        if (gameState.getCurrentPhase() == GamePhase.BOSS_INTRO) {
            startStage(ProgressiveDifficultyStrategy.FINAL_STAGE);
        }
    }

    private void checkWinAfterAction() {
        if (gameState.isStageWinConditionMet()) {
            finishStage();
        }
    }

    private void finishStage() {
        gameState.getFridge().confirmAllPlacements();
        gameState.getUndoStack().clear();
        int livesBeforeReward = gameState.getPlayerLives().getCurrentLives();
        if (stageContractComplete) {
            gameState.getPlayerLives().healLives(1, "Stage contract complete: recovered 1 life.");
            if (gameState.getActivePowerUps().isEfficientPackerEnabled()) {
                gameState.getPlayerLives().healLives(1, "Efficient Packer contract bonus: +1 life.");
            }
            if (gameState.getActivePowerUps().isEfficientPackerEnabled()
                && gameState.getActivePowerUps().isMealPrepPlanEnabled()
                && hasProteinAndProduce()) {
                gameState.getPlayerLives().healLives(1, "Meal Prep + Efficient Packer synergy: +1 life.");
            }
            if (inspectionRouteActive) {
                gameState.getPlayerLives().healLives(1, "Inspection route bonus: +1 life.");
            }
        }
        int livesRecovered = gameState.getPlayerLives().getCurrentLives() - livesBeforeReward;
        lastContractResult = !stageContractComplete
            ? "CONTRACT MISSED — no recovery this time."
            : livesRecovered > 0
                ? "CONTRACT COMPLETE — recovered " + livesRecovered
                    + (livesRecovered == 1 ? " life." : " lives.")
                : "CONTRACT COMPLETE — your life meter is already full.";
        lastEducationalInsight = getEducationalInsight();
        if (isBossStage()) {
            bossHealth = Math.max(0, bossHealth - 1);
            lastEducationalInsight += " The boss phase is over; use the next telegraphed rule to plan your storage order.";
        }
        recordRunProgress();
        inspectionRouteActive = false;
        stageClearBannerTimer = 1.85f;
        gameState.setCurrentPhase(GamePhase.STAGE_CLEAR);
        gameState.notifyStageCleared(gameState.getCurrentStageNumber());
    }

    private boolean isBossStage() {
        return gameState.getCurrentStageNumber() == ProgressiveDifficultyStrategy.FINAL_STAGE;
    }

    private void revealNextTrapIfScouting(Tray tray) {
        if (gameState.getActivePowerUps().isTrapScoutEnabled()
            && !gameState.getActivePowerUps().isSniffTestEnabled()) {
            tray.revealNextTrap();
        }
    }

    private boolean hasProteinAndProduce() {
        boolean protein = false;
        boolean produce = false;
        for (Item item : gameState.getFridge().getPlacedItems()) {
            protein |= item.getCategory() == ItemCategory.MEAT;
            produce |= item.getCategory() == ItemCategory.VEGETABLE;
        }
        return protein && produce;
    }

    private String getEducationalInsight() {
        if (isBossStage()) {
            return "Raw meat should be sealed and stored separately from ready-to-eat foods. In a real refrigerator, keep it on the lowest shelf; this game uses a marked meat zone as a simplified sorting rule.";
        }
        return switch (gameState.getCurrentStageNumber() % 4) {
            case 1 -> "Separate food zones make groceries easier to find and reduce mix-ups. Keep raw foods sealed away from ready-to-eat foods.";
            case 2 -> "Keep trash out: it uses clean space and can contaminate nearby food. Dispose of waste in a bin outside.";
            case 3 -> "Don't discard fresh edible food: preventing waste saves money and production resources.";
            default -> "Keep the fridge at or below 4°C / 40°F; cold slows bacterial growth, but does not stop it.";
        };
    }

    public void continueAfterFeedback() {
        if (gameState.getCurrentPhase() != GamePhase.EDUCATIONAL_FEEDBACK) {
            return;
        }
        if (isBossStage()) {
            if (bossHealth > 0) {
                startStage(ProgressiveDifficultyStrategy.FINAL_STAGE);
            } else {
                gameState.setCurrentPhase(GamePhase.RUN_COMPLETE);
            }
            return;
        }

        currentMapNodeIndexes = getReachableMapNodes();
        currentRouteRoomTypes = new ArrayList<>();
        int nextStage = gameState.getCurrentStageNumber() + 1;
        for (int node : currentMapNodeIndexes) {
            currentRouteRoomTypes.add(RouteRoomType.forNode(nextStage, node));
        }
        currentPowerUpChoices = powerUpPool.generateChoices(
            currentMapNodeIndexes.size(),
            gameState.getActivePowerUps().getAcquiredPowerUps(),
            getUnlockedPowerUpIds()
        );
        currentMapNodeIndexes = new ArrayList<>(
            currentMapNodeIndexes.subList(0, currentPowerUpChoices.size())
        );
        currentRouteRoomTypes = new ArrayList<>(
            currentRouteRoomTypes.subList(0, currentPowerUpChoices.size())
        );
        gameState.setCurrentPhase(GamePhase.MAP_SELECTION);
    }

    public String getBossPhaseTitle() {
        return switch (bossHealth) {
            case 5 -> "RUSH ORDER";
            case 4 -> "CROWDED TRAY";
            case 3 -> "TRAP WAVE";
            case 2 -> "OVERSTOCKED";
            case 1 -> "FINAL STAND";
            default -> "BOSS DEFEATED";
        };
    }

    public String getBossPhaseDescription() {
        return switch (bossHealth) {
            case 5 -> "Telegraphed: groceries start with 30% less time and more awkward shapes. Prioritize the most perishable!";
            case 4 -> "Telegraphed: reduced tray capacity and faster timers. Plan your placement carefully!";
            case 3 -> "Telegraphed: extra traps join the sort. Keep waste out of the fridge — check labels!";
            case 2 -> "Telegraphed: even tighter space with more traps and awkward shapes. Every tile counts!";
            case 1 -> "Telegraphed: everything at once — maximum traps, fastest timers, most awkward shapes. Hold the line!";
            default -> "The fridge is safe.";
        };
    }

    public GameState getGameState() {
        return gameState;
    }

    public GeneratedStage getCurrentGeneratedStage() {
        return currentGeneratedStage;
    }

    public List<PowerUp> getCurrentPowerUpChoices() {
        return currentPowerUpChoices;
    }

    public List<Integer> getCurrentMapNodeIndexes() {
        return currentMapNodeIndexes;
    }

    public List<RouteRoomType> getCurrentRouteRoomTypes() {
        return List.copyOf(currentRouteRoomTypes);
    }

    public StageContract getCurrentStageContract() {
        return currentStageContract;
    }

    public boolean didStageContractSucceed() {
        return stageContractComplete;
    }

    public String getLastEducationalInsight() {
        return lastEducationalInsight == null ? "" : lastEducationalInsight;
    }

    public String getLastContractResult() {
        return lastContractResult == null ? "" : lastContractResult;
    }

    public List<Integer> getSelectedMapNodes() {
        return List.copyOf(selectedMapNodes);
    }

    public Set<String> getUnlockedPowerUpIds() {
        if (Gdx.app == null) {
            Set<String> unlocked = new LinkedHashSet<>(STARTING_POWER_UP_IDS);
            unlocked.addAll(List.of(
                "efficient_packer", "trap_scout", "cold_snap_upgrade",
                "fifo_caddy", "shelf_rails", "deep_freeze_upgrade",
                "reusable_containers", "produce_saver_upgrade"
            ));
            return Set.copyOf(unlocked);
        }
        Preferences profile = Gdx.app.getPreferences("fill-the-fridge-profile");
        Set<String> unlocked = new LinkedHashSet<>(STARTING_POWER_UP_IDS);
        unlocked.addAll(parseStrings(profile.getString("unlockedPowerUps", "")));
        int bestStage = profile.getInteger("bestStage", 1);
        if (bestStage >= 2) {
            unlocked.addAll(List.of("efficient_packer", "trap_scout", "cold_snap_upgrade"));
        }
        if (bestStage >= 4) {
            unlocked.addAll(List.of("fifo_caddy", "shelf_rails", "deep_freeze_upgrade"));
        }
        if (bestStage >= 6) {
            unlocked.addAll(List.of("reusable_containers", "produce_saver_upgrade"));
        }
        return Set.copyOf(unlocked);
    }

    public List<PowerUp> getRecentlyUnlockedPowerUps() {
        return recentlyUnlockedPowerUpIds.stream()
            .map(PowerUpPool::createPowerUp)
            .toList();
    }

    private void recordRunProgress() {
        if (Gdx.app == null) {
            return;
        }
        Preferences profile = Gdx.app.getPreferences("fill-the-fridge-profile");
        int previousBestStage = profile.getInteger("bestStage", 1);
        profile.putInteger("bestStage", Math.max(previousBestStage, gameState.getCurrentStageNumber()));

        Set<String> previousUnlocks = new LinkedHashSet<>(STARTING_POWER_UP_IDS);
        previousUnlocks.addAll(parseStrings(profile.getString("unlockedPowerUps", "")));
        Set<String> currentUnlocks = getUnlockedPowerUpIds();
        recentlyUnlockedPowerUpIds.clear();
        for (String id : currentUnlocks) {
            if (!previousUnlocks.contains(id)) {
                recentlyUnlockedPowerUpIds.add(id);
            }
        }
        profile.putString(
            "unlockedPowerUps",
            String.join(",", currentUnlocks.stream().sorted().toList())
        );
        profile.flush();
    }

    private List<Integer> getReachableMapNodes() {
        int currentStage = gameState.getCurrentStageNumber();
        int currentNode = currentStage <= 1 ? 1 : selectedMapNodes.get(currentStage - 2);
        List<Integer> reachableNodes = new ArrayList<>();
        for (int node = Math.max(0, currentNode - 1); node <= Math.min(2, currentNode + 1); node++) {
            reachableNodes.add(node);
        }
        return reachableNodes;
    }

    public float getStageClearBannerTimer() {
        return stageClearBannerTimer;
    }

    public int getBossHealth() {
        return bossHealth;
    }

    public EventCard getCurrentEventCard() {
        return currentEventCard;
    }

    private FridgeLayout selectFridgeLayout(int stageNumber) {
        // Early stages: mostly standard layout
        // Later stages: more variety with irregular layouts
        float irregularChance;
        if (stageNumber <= 2) {
            irregularChance = 0f;
        } else if (stageNumber <= 5) {
            irregularChance = 0.25f;
        } else {
            irregularChance = 0.5f;
        }

        if (ThreadLocalRandom.current().nextFloat() >= irregularChance) {
            return FridgeLayout.STANDARD;
        }

        // Choose from non-standard layouts
        FridgeLayout[] irregularLayouts = {
            FridgeLayout.MINI,
            FridgeLayout.RETRO,
            FridgeLayout.SIDE_BY_SIDE
        };
        return irregularLayouts[ThreadLocalRandom.current().nextInt(irregularLayouts.length)];
    }
}
