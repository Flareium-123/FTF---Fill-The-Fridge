package com.fillthefridge.model;

public enum RouteRoomType {
    GROCERY_RUN(
        "GROCERY RUN",
        "A short cold-chain bonus; contract: First to Safety.",
        StageContract.PRIORITIZE_PERISHABLE
    ),
    MARKET(
        "MARKET",
        "A wider tray for the next sort; contract: Clean Handling.",
        StageContract.CLEAN_HANDLING
    ),
    REST_STOP(
        "REST STOP",
        "Recover one life before the next sort; contract: Clean Handling.",
        StageContract.CLEAN_HANDLING
    ),
    INSPECTION(
        "INSPECTION",
        "RISK: groceries start with 1s less. REWARD: a completed contract grants an extra life.",
        StageContract.COLD_CHAIN
    );

    private final String title;
    private final String description;
    private final StageContract contract;

    RouteRoomType(String title, String description, StageContract contract) {
        this.title = title;
        this.description = description;
        this.contract = contract;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public StageContract getContract() {
        return contract;
    }

    public static RouteRoomType forNode(int stage, int node) {
        RouteRoomType[] types = values();
        return types[Math.floorMod(stage + node, types.length)];
    }
}
