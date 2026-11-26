package com.fillthefridge.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MenuSelectionTest {
    @Test
    void pointerHoverSelectsTheHoveredOption() {
        MenuSelection selection = new MenuSelection(6);

        selection.updatePointer(120f, 420f, 1);

        assertEquals(1, selection.getSelectedIndex());
    }

    @Test
    void pointerCanLeaveAndReturnToSelectAnOption() {
        MenuSelection selection = new MenuSelection(6);
        selection.updatePointer(120f, 420f, 4);

        selection.updatePointer(120f, 390f, -1);
        assertEquals(4, selection.getSelectedIndex());

        selection.updatePointer(120f, 420f, 1);

        assertEquals(1, selection.getSelectedIndex());
    }

    @Test
    void keyboardNavigationTakesOverUntilThePointerMoves() {
        MenuSelection selection = new MenuSelection(6);
        selection.updatePointer(120f, 420f, 1);

        selection.moveBy(1);
        selection.updatePointer(120f, 420f, 1);

        assertEquals(2, selection.getSelectedIndex());
    }

    @Test
    void keyboardNavigationWrapsAtBothEnds() {
        MenuSelection selection = new MenuSelection(6);

        selection.moveBy(-1);
        assertEquals(5, selection.getSelectedIndex());

        selection.moveBy(1);

        assertEquals(0, selection.getSelectedIndex());
    }
}
