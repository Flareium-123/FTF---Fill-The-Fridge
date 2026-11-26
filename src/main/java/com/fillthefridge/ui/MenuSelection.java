package com.fillthefridge.ui;

public class MenuSelection {
    private final int itemCount;
    private int selectedIndex;
    private float lastPointerX;
    private float lastPointerY;
    private boolean pointerInitialized;

    public MenuSelection(int itemCount) {
        if (itemCount <= 0) {
            throw new IllegalArgumentException("A menu must contain at least one item.");
        }
        this.itemCount = itemCount;
    }

    public void updatePointer(float x, float y, int hoveredIndex) {
        boolean pointerMoved = !pointerInitialized || x != lastPointerX || y != lastPointerY;
        lastPointerX = x;
        lastPointerY = y;
        pointerInitialized = true;

        if (pointerMoved && hoveredIndex >= 0 && hoveredIndex < itemCount) {
            selectedIndex = hoveredIndex;
        }
    }

    public void moveBy(int delta) {
        selectedIndex = Math.floorMod(selectedIndex + delta, itemCount);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }
}
