package com.fillthefridge.model;

public final class StageConfig {
    private final int stageNumber;
    private final int gridCols;
    private final int gridRows;
    private final int trapCount;
    private final int trayCapacity;
    private final int trashSlots;
    private final float timerMultiplier;
    private final float awkwardShapeProbability;
    private final long randomSeed;
    private final boolean prefilledItem;
    private final int specialShelfRow;
    private final int specialShelfMaxWeight;

    public StageConfig(
        int stageNumber,
        int gridCols,
        int gridRows,
        int trapCount,
        int trayCapacity,
        int trashSlots,
        float timerMultiplier,
        float awkwardShapeProbability,
        long randomSeed
    ) {
        this(
            stageNumber,
            gridCols,
            gridRows,
            trapCount,
            trayCapacity,
            trashSlots,
            timerMultiplier,
            awkwardShapeProbability,
            randomSeed,
            false,
            -1,
            Integer.MAX_VALUE
        );
    }

    public StageConfig(
        int stageNumber,
        int gridCols,
        int gridRows,
        int trapCount,
        int trayCapacity,
        int trashSlots,
        float timerMultiplier,
        float awkwardShapeProbability,
        long randomSeed,
        boolean prefilledItem,
        int specialShelfRow,
        int specialShelfMaxWeight
    ) {
        this.stageNumber = stageNumber;
        this.gridCols = gridCols;
        this.gridRows = gridRows;
        this.trapCount = trapCount;
        this.trayCapacity = trayCapacity;
        this.trashSlots = trashSlots;
        this.timerMultiplier = timerMultiplier;
        this.awkwardShapeProbability = awkwardShapeProbability;
        this.randomSeed = randomSeed;
        this.prefilledItem = prefilledItem;
        this.specialShelfRow = specialShelfRow;
        this.specialShelfMaxWeight = specialShelfMaxWeight;
    }

    public int getStageNumber() {
        return stageNumber;
    }

    public int getGridCols() {
        return gridCols;
    }

    public int getGridRows() {
        return gridRows;
    }

    public int getTrapCount() {
        return trapCount;
    }

    public int getTrayCapacity() {
        return trayCapacity;
    }

    public int getTrashSlots() {
        return trashSlots;
    }

    public float getTimerMultiplier() {
        return timerMultiplier;
    }

    public float getAwkwardShapeProbability() {
        return awkwardShapeProbability;
    }

    public long getRandomSeed() {
        return randomSeed;
    }

    public boolean hasPrefilledItem() {
        return prefilledItem;
    }

    public int getSpecialShelfRow() {
        return specialShelfRow;
    }

    public int getSpecialShelfMaxWeight() {
        return specialShelfMaxWeight;
    }
}
