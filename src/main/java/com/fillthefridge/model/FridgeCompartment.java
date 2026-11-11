package com.fillthefridge.model;

public enum FridgeCompartment {
    LOWER_CHILLER("LOWER CHILLER", "LIQUIDS"),
    CHILLER("CHILLER", "MIXED FOODS"),
    FREEZER("FREEZER", "MEAT");

    private final String displayName;
    private final String categoryLabel;

    FridgeCompartment(String displayName, String categoryLabel) {
        this.displayName = displayName;
        this.categoryLabel = categoryLabel;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCategoryLabel() {
        return categoryLabel;
    }
}
