package com.fillthefridge.logic;

import com.fillthefridge.model.StageConfig;

public class ProgressiveDifficultyStrategy implements DifficultyScalingStrategy {
    public static final int FINAL_STAGE = 8;

    @Override
    public StageConfig createStageConfig(int stageNumber, long baseSeed) {
        int safeStage = Math.max(1, stageNumber);

        int gridCols = 4;
        int gridRows = 3;
        if (safeStage >= 3 && safeStage <= 4) {
            gridCols = 4;
            gridRows = 4;
        } else if (safeStage >= 5 && safeStage <= 6) {
            gridCols = 5;
            gridRows = 4;
        } else if (safeStage >= 7) {
            gridCols = 6;
            gridRows = 4;
        }

        int trapCount = Math.min(6, 1 + (safeStage / 2));

        float awkwardShapeProbability = Math.min(0.65f, 0.18f + (safeStage - 1) * 0.08f);
        int trayCapacity = safeStage <= 3 ? 4 : 3;
        int extraTrashBuffer = safeStage <= 4 ? 2 : 1;
        int trashSlots = trapCount + extraTrashBuffer;

        float timerMultiplier;
        if (safeStage >= 6) {
            timerMultiplier = Math.max(0.85f, 1.15f - (safeStage - 5) * 0.04f);
        } else {
            timerMultiplier = 1.15f;
        }

        long stageSeed = baseSeed + safeStage * 982451653L;

        return new StageConfig(
            safeStage,
            gridCols,
            gridRows,
            trapCount,
            trayCapacity,
            trashSlots,
            timerMultiplier,
            awkwardShapeProbability,
            stageSeed,
            safeStage >= 4,
            safeStage >= 5 ? 1 : -1,
            1
        );
    }
}
