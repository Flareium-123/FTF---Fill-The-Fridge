package com.fillthefridge.logic;

import com.fillthefridge.model.ActivePowerUps;
import com.fillthefridge.model.StageConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SolvableStageGeneratorTest {

    @Test
    void stagesGenerateValidSolvableLayoutsAcrossSeeds() {
        SolvableStageGenerator generator = new SolvableStageGenerator();
        ProgressiveDifficultyStrategy strategy = new ProgressiveDifficultyStrategy();
        ActivePowerUps powerUps = new ActivePowerUps();

        long[] testSeeds = {12345L, 67890L, 999999L, 42L};

        for (int stage = 1; stage <= 5; stage++) {
            for (long seed : testSeeds) {
                StageConfig config = strategy.createStageConfig(stage, seed);
                GeneratedStage generated = generator.generateStage(config, powerUps);

                assertNotNull(generated, "Stage " + stage + " with seed " + seed + " should not be null");
                assertTrue(generator.verifySolvability(generated, config),
                    "Generated stage " + stage + " with seed " + seed + " must be verified solvable");
                assertFalse(generated.getStageItems().isEmpty(), "Stage must have items");
            }
        }
    }
}
