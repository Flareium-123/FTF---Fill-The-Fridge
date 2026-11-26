package com.fillthefridge.model;

import com.fillthefridge.items.Item;
import com.fillthefridge.items.ItemCatalog;
import com.fillthefridge.util.GridPosition;
import com.fillthefridge.util.GridSize;
import com.fillthefridge.util.InvalidPlacementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FridgeTest {
    private Fridge fridge;

    @BeforeEach
    void setUp() {
        fridge = new Fridge(4, 4);
    }

    @Test
    void meatAllowedOnlyInTopFreezer() {
        Item chicken = ItemCatalog.getAllGroceryTemplates().stream()
            .filter(i -> i.getCategory() == ItemCategory.MEAT)
            .findFirst()
            .orElseThrow()
            .createFreshCopy();

        // Row 3 is top row (freezer in a 4-row fridge)
        assertTrue(fridge.isCategoryAllowedInCompartment(chicken, new GridPosition(0, 3), chicken.getSize()));

        // Row 0, 1, 2 should not allow meat
        assertFalse(fridge.isCategoryAllowedInCompartment(chicken, new GridPosition(0, 0), chicken.getSize()));
        assertFalse(fridge.isCategoryAllowedInCompartment(chicken, new GridPosition(0, 1), chicken.getSize()));
        assertFalse(fridge.isCategoryAllowedInCompartment(chicken, new GridPosition(0, 2), chicken.getSize()));
    }

    @Test
    void liquidsAllowedOnlyInLowerChiller() {
        Item milk = ItemCatalog.getAllGroceryTemplates().stream()
            .filter(i -> i.getId().equals("milk"))
            .findFirst()
            .orElseThrow()
            .createFreshCopy();

        // Row 0 is lower chiller
        assertTrue(fridge.isCategoryAllowedInCompartment(milk, new GridPosition(0, 0), milk.getSize()));

        // Higher rows should not allow liquids
        assertFalse(fridge.isCategoryAllowedInCompartment(milk, new GridPosition(0, 1), milk.getSize()));
        assertFalse(fridge.isCategoryAllowedInCompartment(milk, new GridPosition(0, 3), milk.getSize()));
    }

    @Test
    void placementOutsideBoundsIsRejected() {
        Item item = ItemCatalog.getAllGroceryTemplates().get(0).createFreshCopy();
        assertFalse(fridge.isWithinBounds(new GridPosition(-1, 0), item.getSize()));
        assertFalse(fridge.isWithinBounds(new GridPosition(4, 0), item.getSize()));
        assertFalse(fridge.isWithinBounds(new GridPosition(0, 4), item.getSize()));
    }

    @Test
    void baseRowAllowsPlacementWithoutSupport() {
        Item liquid = ItemCatalog.getAllGroceryTemplates().stream()
            .filter(Fridge::isLiquidItem)
            .filter(i -> i.getSize().getHeight() == 1)
            .findFirst()
            .orElseThrow()
            .createFreshCopy();

        PlacementResult result = fridge.evaluatePlacement(liquid, new GridPosition(0, 0));
        assertEquals(PlacementResult.LEGAL, result);
    }

    @Test
    void higherRowRequiresSupportBelow() {
        Item vegetable = ItemCatalog.getAllGroceryTemplates().stream()
            .filter(i -> i.getCategory() == ItemCategory.VEGETABLE)
            .filter(i -> i.getSize().getHeight() == 1)
            .findFirst()
            .orElseThrow()
            .createFreshCopy();

        // Placing on row 1 without anything on row 0 should be UNSTABLE
        PlacementResult result = fridge.evaluatePlacement(vegetable, new GridPosition(0, 1));
        assertEquals(PlacementResult.UNSTABLE, result);
    }

    @Test
    void placingAndRemovingItemUpdatesTiles() throws InvalidPlacementException {
        Item liquid = ItemCatalog.getAllGroceryTemplates().stream()
            .filter(Fridge::isLiquidItem)
            .filter(i -> i.getSize().getWidth() == 1 && i.getSize().getHeight() == 1)
            .findFirst()
            .orElseThrow()
            .createFreshCopy();

        GridPosition pos = new GridPosition(0, 0);
        fridge.placeItem(liquid, pos);

        assertFalse(fridge.getTile(0, 0).isEmpty());
        assertEquals(liquid, fridge.getItemAt(pos));

        fridge.removeItem(liquid);
        assertTrue(fridge.getTile(0, 0).isEmpty());
        assertNull(fridge.getItemAt(pos));
    }
}
