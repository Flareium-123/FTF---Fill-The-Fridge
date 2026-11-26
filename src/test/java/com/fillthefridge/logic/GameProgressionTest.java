package com.fillthefridge.logic;

import com.fillthefridge.model.GamePhase;
import com.fillthefridge.model.GameState;
import com.fillthefridge.powerups.PowerUp;
import com.fillthefridge.powerups.SpaceOptimizerPowerUp;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameProgressionTest {

    @Test
    void completeRunProgressionThroughAllStagesToBoss() {
        GameState gameState = new GameState();
        DifficultyScalingStrategy strategy = new ProgressiveDifficultyStrategy();
        SolvableStageGenerator generator = new SolvableStageGenerator();
        GameController controller = new GameController(gameState, strategy, generator, 1337L);

        // Start new run with a hero power-up
        controller.startNewRun(new SpaceOptimizerPowerUp());
        assertEquals(1, gameState.getCurrentStageNumber());
        assertEquals(GamePhase.PLAYING, gameState.getCurrentPhase());

        for (int stage = 1; stage < ProgressiveDifficultyStrategy.FINAL_STAGE; stage++) {
            assertEquals(stage, gameState.getCurrentStageNumber());

            // Simulate clearing the stage
            gameState.setCurrentPhase(GamePhase.STAGE_CLEAR);
            controller.update(2.5f);
            assertEquals(GamePhase.EDUCATIONAL_FEEDBACK, gameState.getCurrentPhase());

            // Move past feedback to map
            controller.continueAfterFeedback();
            assertEquals(GamePhase.MAP_SELECTION, gameState.getCurrentPhase());

            List<PowerUp> choices = controller.getCurrentPowerUpChoices();
            assertFalse(choices.isEmpty(), "Choices must be offered at stage " + stage);
            assertEquals(choices.size(), controller.getCurrentMapNodeIndexes().size());

            // Choose first power-up
            controller.choosePowerUp(choices.get(0));

            if (stage == ProgressiveDifficultyStrategy.FINAL_STAGE - 1) {
                // Stage 7 -> Before boss, pre-boss event occurs
                assertEquals(GamePhase.PRE_BOSS_EVENT, gameState.getCurrentPhase());
                assertNotNull(controller.getPreBossEventCard());
                assertEquals(3, controller.getPreBossEventCard().getChoices().size());

                // Select and confirm pre-boss choice
                controller.getPreBossEventCard().selectChoice(0);
                controller.confirmPreBossEvent();
                assertEquals(GamePhase.BOSS_INTRO, gameState.getCurrentPhase());

                // Start boss
                controller.startBossStage();
                assertEquals(ProgressiveDifficultyStrategy.FINAL_STAGE, gameState.getCurrentStageNumber());
                assertEquals(GamePhase.PLAYING, gameState.getCurrentPhase());
                assertEquals(GameController.BOSS_HEALTH, controller.getBossHealth());
            } else {
                assertEquals(GamePhase.PLAYING, gameState.getCurrentPhase());
                assertEquals(stage + 1, gameState.getCurrentStageNumber());
            }
        }
    }
}
