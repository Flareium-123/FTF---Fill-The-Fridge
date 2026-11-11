package com.fillthefridge.model;

public enum PlacementResult {
    LEGAL("Legal Placement"),
    UNSTABLE("Unstable / Blocked");

    private final String description;

    PlacementResult(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
