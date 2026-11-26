package com.fillthefridge.logic;

import com.fillthefridge.items.Item;
import com.fillthefridge.model.ActivePowerUps;
import com.fillthefridge.model.GamePhase;
import com.fillthefridge.model.GameState;
import com.fillthefridge.model.PlayerLives;
import com.fillthefridge.powerups.ColdStorageVeteranPowerUp;
import com.fillthefridge.powerups.FifoMasterPowerUp;
import com.fillthefridge.powerups.SpaceOptimizerPowerUp;
import com.fillthefridge.util.GridPosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameControllerTest {
    private GameState gameState;
    private GameController controller;

    @BeforeEach
    void setUp() {
        gameState = new GameState();
        DifficultyScalingStrategy strategy = new ProgressiveDifficultyStrategy();
        SolvableStageGenerator generator = new SolvableStageGenerator();
        controller = new GameController(gameState, strategy, generator, 42L);
    }

    @Test
    void startingRunWithSpaceOptimizerAppliesExtraCapacityToStageOne() {
        SpaceOptimizerPowerUp hero = new SpaceOptimizerPowerUp();
        controller.startNewRun(hero);

        assertEquals(GamePhase.PLAYING, gameState.getCurrentPhase());
        assertEquals(1, gameState.getCurrentStageNumber());
        assertTrue(gameState.getActivePowerUps().hasPowerUp("hero_space_optimizer"));
        assertEquals(2, gameState.getActivePowerUps().getExtraTrayCapacity());

        // Stage 1 default capacity is 4, plus 2 from hero = 6 active items in tray
        assertEquals(6, gameState.getTray().getActiveItems().size());
    }

    @Test
    void startingRunWithColdStorageVeteranHealsExtraLife() {
        ColdStorageVeteranPowerUp hero = new ColdStorageVeteranPowerUp();
        controller.startNewRun(hero);

        assertEquals(PlayerLives.INITIAL_LIVES + 1, gameState.getPlayerLives().getCurrentLives());
        assertTrue(gameState.getActivePowerUps().hasPowerUp("hero_cold_storage_veteran"));
    }

    @Test
    void startingRunWithFifoMasterEnablesFifoCaddyAndCharge() {
        FifoMasterPowerUp hero = new FifoMasterPowerUp();
        controller.startNewRun(hero);

        assertTrue(gameState.getActivePowerUps().isFifoCaddyEnabled());
        assertEquals(1, gameState.getActivePowerUps().getFifoCaddyCharges());
    }

    @Test
    void handleFridgeLeftClickWithNoSelectionReturnsFalse() {
        controller.startNewRun();
        gameState.getTray().clearSelection();

        boolean result = controller.handleFridgeLeftClick(new GridPosition(0, 0));
        assertFalse(result);
    }

    @Test
    void trashSelectedItemWithNoSelectionReturnsFalse() {
        controller.startNewRun();
        gameState.getTray().clearSelection();

        boolean result = controller.trashSelectedItem();
        assertFalse(result);
    }
}
