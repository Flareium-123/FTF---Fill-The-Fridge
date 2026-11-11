package com.fillthefridge.model;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.fillthefridge.items.Item;
import com.fillthefridge.util.GridPosition;
import com.fillthefridge.util.GridSize;
import com.fillthefridge.util.InvalidPlacementException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Fridge extends GameObject {
    private int cols;
    private int rows;
    private Tile[][] tiles;
    private final List<Shelf> shelves;
    private final List<Item> placedItems;
    private boolean reinforcedShelves;
    private int specialShelfRow = -1;
    private int specialShelfMaxWeight;
    private GridPosition packingOrderTarget;
    private String packingOrderItemId;
    private String packingOrderItemName;
    private boolean hasPrefilledItem;

    public Fridge(int cols, int rows) {
        super(0f, 0f, 440f, 440f);
        this.shelves = new ArrayList<>();
        this.placedItems = new ArrayList<>();
        configureGrid(cols, rows);
    }

    public void configureGrid(int newCols, int newRows) {
        this.cols = Math.max(2, newCols);
        this.rows = Math.max(2, newRows);
        specialShelfRow = -1;
        specialShelfMaxWeight = 0;
        packingOrderTarget = null;
        packingOrderItemId = null;
        packingOrderItemName = null;
        hasPrefilledItem = false;
        this.tiles = new Tile[this.cols][this.rows];
        this.shelves.clear();
        this.placedItems.clear();

        for (int c = 0; c < this.cols; c++) {
            for (int r = 0; r < this.rows; r++) {
                tiles[c][r] = new Tile(new GridPosition(c, r));
            }
        }
        for (int r = 0; r < this.rows; r++) {
            shelves.add(new Shelf(r, this.cols, r == 0));
        }
    }

    @Override
    public void update(float deltaTime) {
        for (Item item : placedItems) {
            item.update(deltaTime);
        }
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, BitmapFont font) {
    }

    public boolean isWithinBounds(GridPosition position, GridSize size) {
        if (position == null || size == null) {
            return false;
        }
        int startCol = position.getCol();
        int startRow = position.getRow();
        return startCol >= 0
            && startRow >= 0
            && startCol + size.getWidth() <= cols
            && startRow + size.getHeight() <= rows;
    }

    public boolean areTilesEmpty(GridPosition position, GridSize size) {
        if (!isWithinBounds(position, size)) {
            return false;
        }
        for (int c = position.getCol(); c < position.getCol() + size.getWidth(); c++) {
            for (int r = position.getRow(); r < position.getRow() + size.getHeight(); r++) {
                if (!tiles[c][r].isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    public PlacementResult evaluatePlacement(Item item, GridPosition targetPosition) {
        if (item == null || targetPosition == null) {
            return PlacementResult.UNSTABLE;
        }
        GridSize size = item.getSize();
        if (!isWithinBounds(targetPosition, size)
            || !isCategoryAllowedInCompartment(item, targetPosition, size)
            || !areTilesEmpty(targetPosition, size)) {
            return PlacementResult.UNSTABLE;
        }
        if (specialShelfRow >= 0
            && targetPosition.getRow() <= specialShelfRow
            && targetPosition.getRow() + size.getHeight() > specialShelfRow
            && item.getWeight() > specialShelfMaxWeight) {
            return PlacementResult.UNSTABLE;
        }
        int row = targetPosition.getRow();
        if (row == 0) {
            return PlacementResult.LEGAL;
        }

        for (int c = targetPosition.getCol(); c < targetPosition.getCol() + size.getWidth(); c++) {
            Tile belowTile = tiles[c][row - 1];
            if (belowTile.isEmpty() || belowTile.getOccupyingItem() == null) {
                return PlacementResult.UNSTABLE;
            }
            Item belowItem = belowTile.getOccupyingItem();
            PlacementResult result = belowItem.onStackedUpon(item, this);
            if (result == PlacementResult.UNSTABLE && (!reinforcedShelves || belowItem.isTrap())) {
                return PlacementResult.UNSTABLE;
            }
        }

        return PlacementResult.LEGAL;
    }

    public void placeItem(Item item, GridPosition targetPosition) throws InvalidPlacementException {
        if (!isWithinBounds(targetPosition, item.getSize())) {
            throw new InvalidPlacementException("Item placement is outside the fridge grid!", targetPosition);
        }
        if (!isCategoryAllowedInCompartment(item, targetPosition, item.getSize())) {
            throw new InvalidPlacementException("That food belongs in a different fridge compartment!", targetPosition);
        }
        if (!areTilesEmpty(targetPosition, item.getSize())) {
            throw new InvalidPlacementException("Target fridge tiles are already occupied!", targetPosition);
        }
        item.setGridPosition(targetPosition);
        item.setInFridge(true);
        item.setSelected(false);
        item.triggerSnapAnimation();

        for (int c = targetPosition.getCol(); c < targetPosition.getCol() + item.getSize().getWidth(); c++) {
            for (int r = targetPosition.getRow(); r < targetPosition.getRow() + item.getSize().getHeight(); r++) {
                tiles[c][r].setOccupyingItem(item);
            }
        }
        if (!placedItems.contains(item)) {
            placedItems.add(item);
        }
    }

    public void removeItem(Item item) {
        if (item == null) {
            return;
        }
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                if (tiles[c][r].getOccupyingItem() == item) {
                    tiles[c][r].clear();
                }
            }
        }
        placedItems.remove(item);
        item.setInFridge(false);
        item.setConfirmed(false);
        item.setGridPosition(null);
    }

    public List<Item> findUnsupportedItems() {
        List<Item> unsupported = new ArrayList<>();
        for (Item item : new ArrayList<>(placedItems)) {
            GridPosition pos = item.getGridPosition();
            if (pos != null && pos.getRow() > 0) {
                boolean supported = true;
                for (int c = pos.getCol(); c < pos.getCol() + item.getSize().getWidth(); c++) {
                    if (tiles[c][pos.getRow() - 1].isEmpty()) {
                        supported = false;
                        break;
                    }
                }
                if (!supported) {
                    unsupported.add(item);
                }
            }
        }
        return unsupported;
    }

    public void confirmAllPlacements() {
        for (Item item : placedItems) {
            if (!item.isTrap()) {
                item.setConfirmed(true);
            }
        }
        refreshAllTileStates();
    }

    public void refreshAllTileStates() {
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                tiles[c][r].refreshState();
            }
        }
    }

    public boolean isEveryTileOccupiedByLegalItem() {
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                Item occupant = tiles[c][r].getOccupyingItem();
                if (occupant == null || occupant.isTrap()) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean hasTrapItems() {
        for (Item item : placedItems) {
            if (item.isTrap()) {
                return true;
            }
        }
        return false;
    }

    public Item getItemAt(GridPosition position) {
        if (position == null || position.getCol() < 0 || position.getCol() >= cols
            || position.getRow() < 0 || position.getRow() >= rows) {
            return null;
        }
        return tiles[position.getCol()][position.getRow()].getOccupyingItem();
    }

    public Tile getTile(int col, int row) {
        return tiles[col][row];
    }

    public FridgeCompartment getCompartmentForRow(int row) {
        if (row <= 0) {
            return FridgeCompartment.LOWER_CHILLER;
        }
        if (row >= rows - 1) {
            return FridgeCompartment.FREEZER;
        }
        return FridgeCompartment.CHILLER;
    }

    public FridgeCompartment getCompartmentFor(Item item) {
        if (item == null) {
            return null;
        }
        if (item.getCategory() == ItemCategory.MEAT) {
            return FridgeCompartment.FREEZER;
        }
        if (isLiquidItem(item)) {
            return FridgeCompartment.LOWER_CHILLER;
        }
        return FridgeCompartment.CHILLER;
    }

    public boolean isCategoryAllowedInCompartment(Item item, GridPosition position, GridSize size) {
        if (item == null || position == null || size == null || !isWithinBounds(position, size)) {
            return false;
        }
        if (item.isTrap()) {
            return true;
        }
        FridgeCompartment itemCompartment = getCompartmentFor(item);
        for (int row = position.getRow(); row < position.getRow() + size.getHeight(); row++) {
            if (getCompartmentForRow(row) != itemCompartment) {
                return false;
            }
        }
        return true;
    }

    public static boolean isLiquidItem(Item item) {
        return item != null && switch (item.getId()) {
            case "milk", "juice_bottle", "soda_can", "soup_container",
                "juice_box", "cup_with_straw", "bubble_tea" -> true;
            default -> false;
        };
    }

    public int getCols() {
        return cols;
    }

    public int getRows() {
        return rows;
    }

    public List<Shelf> getShelves() {
        return Collections.unmodifiableList(shelves);
    }

    public List<Item> getPlacedItems() {
        return Collections.unmodifiableList(placedItems);
    }

    public void setReinforcedShelves(boolean reinforcedShelves) {
        this.reinforcedShelves = reinforcedShelves;
    }

    public void setSpecialShelfRule(int row, int maxWeight) {
        if (row < -1 || row >= rows || maxWeight < 0) {
            throw new IllegalArgumentException("Invalid special shelf rule.");
        }
        specialShelfRow = row;
        specialShelfMaxWeight = maxWeight;
    }

    public int getSpecialShelfRow() {
        return specialShelfRow;
    }

    public int getSpecialShelfMaxWeight() {
        return specialShelfMaxWeight;
    }

    public void setPackingOrderTarget(GridPosition target, String itemId, String itemName) {
        packingOrderTarget = target;
        packingOrderItemId = itemId;
        packingOrderItemName = itemName;
    }

    public GridPosition getPackingOrderTarget() {
        return packingOrderTarget;
    }

    public String getPackingOrderItemName() {
        return packingOrderItemName;
    }

    public String getPackingOrderItemId() {
        return packingOrderItemId;
    }

    public void setHasPrefilledItem(boolean hasPrefilledItem) {
        this.hasPrefilledItem = hasPrefilledItem;
    }

    public boolean hasPrefilledItem() {
        return hasPrefilledItem;
    }
}
