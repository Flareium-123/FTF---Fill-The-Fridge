package com.fillthefridge.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Defines different fridge layout types for procedural variety.
 * Each layout has a grid size and a set of blocked tiles that create irregular shapes.
 */
public enum FridgeLayout {
    STANDARD("Standard", 4, 4, Set.of()),
    MINI("Mini Fridge", 3, 2, Set.of()),
    FRENCH_DOOR("French Door", 4, 4, Set.of()),
    RETRO("Retro Fridge", 4, 4, Set.of("0,3", "3,0")),
    SIDE_BY_SIDE("Side-by-Side", 6, 3, Set.of("2,0", "2,1", "2,2"));

    private final String displayName;
    private final int cols;
    private final int rows;
    private final Set<String> blockedTiles;

    FridgeLayout(String displayName, int cols, int rows, Set<String> blockedTiles) {
        this.displayName = displayName;
        this.cols = cols;
        this.rows = rows;
        this.blockedTiles = blockedTiles;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getCols() {
        return cols;
    }

    public int getRows() {
        return rows;
    }

    /**
     * Check if a tile is blocked (not usable) in this layout.
     */
    public boolean isBlocked(int col, int row) {
        return blockedTiles.contains(col + "," + row);
    }

    /**
     * Get the set of blocked tile coordinates as strings "col,row".
     */
    public Set<String> getBlockedTiles() {
        return blockedTiles;
    }
}
