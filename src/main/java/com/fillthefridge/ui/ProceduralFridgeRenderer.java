package com.fillthefridge.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.fillthefridge.assets.AssetManager;
import com.fillthefridge.items.Item;
import com.fillthefridge.model.ActivePowerUps;
import com.fillthefridge.model.Fridge;
import com.fillthefridge.model.FridgeCompartment;
import com.fillthefridge.model.PlacementResult;
import com.fillthefridge.model.Tile;
import com.fillthefridge.util.GridPosition;
import com.fillthefridge.util.GridSize;
import java.util.List;

public class ProceduralFridgeRenderer {
    private final Rectangle interiorBounds;
    private float tileWidth;
    private float tileHeight;
    private float animationClock;

    public ProceduralFridgeRenderer() {
        this.interiorBounds = new Rectangle(341f, 263f, 540f, 280f);
    }

    public void updateLayout(Fridge fridge, float screenWidth, float screenHeight) {
        float maxInteriorWidth = Math.min(540f, screenWidth * 0.44f);
        float maxInteriorHeight = Math.min(280f, screenHeight * 0.39f);

        int cols = fridge.getCols();
        int rows = fridge.getRows();

        this.tileWidth = maxInteriorWidth / cols;
        this.tileHeight = maxInteriorHeight / rows;

        float centerX = screenWidth * 0.477f;
        float centerY = screenHeight * 0.56f;
        float interiorX = centerX - maxInteriorWidth * 0.5f;
        float interiorY = centerY - maxInteriorHeight * 0.5f;

        interiorBounds.set(interiorX, interiorY, maxInteriorWidth, maxInteriorHeight);
        fridge.setBounds(interiorX, interiorY, maxInteriorWidth, maxInteriorHeight);
    }

    public void update(float deltaTime) {
        animationClock += deltaTime;
    }

    public GridPosition screenToGridPosition(Fridge fridge, float worldX, float worldY) {
        if (!interiorBounds.contains(worldX, worldY)) {
            return null;
        }
        int col = (int) ((worldX - interiorBounds.x) / tileWidth);
        int row = (int) ((worldY - interiorBounds.y) / tileHeight);
        col = MathUtils.clamp(col, 0, fridge.getCols() - 1);
        row = MathUtils.clamp(row, 0, fridge.getRows() - 1);
        return new GridPosition(col, row);
    }

    private void renderCompartmentLabels(SpriteBatch batch, BitmapFont font, Fridge fridge) {
        float labelX = interiorBounds.x + interiorBounds.width + 4f;
        float labelWidth = 124f;
        for (FridgeCompartment compartment : FridgeCompartment.values()) {
            int firstRow = -1;
            int lastRow = -1;
            for (int row = 0; row < fridge.getRows(); row++) {
                if (fridge.getCompartmentForRow(row) == compartment) {
                    if (firstRow < 0) {
                        firstRow = row;
                    }
                    lastRow = row;
                }
            }
            if (firstRow < 0) {
                continue;
            }
            float centerY = interiorBounds.y + ((firstRow + lastRow + 1) * tileHeight) * 0.5f;
            Color color = new Color(getCompartmentColor(compartment));
            color.a = 1f;
            font.setColor(color);
            drawCenteredLabel(batch, font, compartment.getDisplayName(), labelX, labelWidth, centerY + 6f);
            font.setColor(Color.WHITE);
            drawCenteredLabel(batch, font, compartment.getCategoryLabel(), labelX, labelWidth, centerY - 9f);
        }
        font.setColor(Color.WHITE);
    }

    private void drawCenteredLabel(
        SpriteBatch batch,
        BitmapFont font,
        String text,
        float x,
        float width,
        float baselineY
    ) {
        GlyphLayout layout = new GlyphLayout(font, text);
        font.draw(batch, text, x + (width - layout.width) * 0.5f, baselineY);
    }

    private Color getCompartmentColor(FridgeCompartment compartment) {
        return switch (compartment) {
            case LOWER_CHILLER -> new Color(0.30f, 0.67f, 0.94f, 0.20f);
            case CHILLER -> new Color(0.34f, 0.78f, 0.56f, 0.20f);
            case FREEZER -> new Color(0.70f, 0.54f, 0.96f, 0.20f);
        };
    }

    public void renderFridgeStructure(
        ShapeRenderer shapeRenderer,
        SpriteBatch batch,
        BitmapFont smallFont,
        AssetManager assetManager,
        Fridge fridge,
        Item selectedItem,
        GridPosition hoverGridPos,
        ActivePowerUps activePowerUps
    ) {
        Gdx.glEnableBlend();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        renderGridCellsAndShelves(shapeRenderer, fridge, selectedItem, activePowerUps);
        renderCompartmentLabelBackings(shapeRenderer, fridge);
        renderPlacedItemFootprintPads(shapeRenderer, fridge);
        if (selectedItem != null && hoverGridPos != null) {
            renderPlacementPreview(shapeRenderer, fridge, selectedItem, hoverGridPos);
        }
        shapeRenderer.end();

        batch.begin();
        renderPlacedItems(batch, smallFont, assetManager, fridge);
        renderCompartmentLabels(batch, smallFont, fridge);
        if (selectedItem != null && hoverGridPos != null) {
            renderGhostSpritePreview(batch, smallFont, assetManager, selectedItem, hoverGridPos);
        }
        batch.end();

    }

    private void renderGridCellsAndShelves(
        ShapeRenderer shapeRenderer,
        Fridge fridge,
        Item selectedItem,
        ActivePowerUps activePowerUps
    ) {
        int cols = fridge.getCols();
        int rows = fridge.getRows();
        boolean ghostFit = activePowerUps != null && activePowerUps.isGhostFitEnabled();

        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                float cellX = interiorBounds.x + c * tileWidth;
                float cellY = interiorBounds.y + r * tileHeight;
                Tile tile = fridge.getTile(c, r);
                shapeRenderer.setColor(getCompartmentColor(fridge.getCompartmentForRow(r)));
                shapeRenderer.rect(cellX + 1f, cellY + 1f, tileWidth - 2f, tileHeight - 2f);

                if (r == fridge.getSpecialShelfRow()) {
                    shapeRenderer.setColor(0.98f, 0.77f, 0.22f, 0.20f);
                    shapeRenderer.rect(cellX + 2f, cellY + 2f, tileWidth - 4f, tileHeight - 4f);
                }

                GridPosition packingTarget = fridge.getPackingOrderTarget();
                if (packingTarget != null && packingTarget.getCol() == c && packingTarget.getRow() == r) {
                    shapeRenderer.setColor(1f, 0.78f, 0.18f, 0.96f);
                    shapeRenderer.rect(cellX + 1f, cellY + 1f, tileWidth - 2f, 4f);
                    shapeRenderer.rect(cellX + 1f, cellY + tileHeight - 5f, tileWidth - 2f, 4f);
                    shapeRenderer.rect(cellX + 1f, cellY + 1f, 4f, tileHeight - 2f);
                    shapeRenderer.rect(cellX + tileWidth - 5f, cellY + 1f, 4f, tileHeight - 2f);
                }

                if (ghostFit && selectedItem != null && tile.isEmpty()) {
                    PlacementResult fit = fridge.evaluatePlacement(selectedItem, new GridPosition(c, r));
                    if (fit == PlacementResult.LEGAL) {
                        shapeRenderer.setColor(0.20f, 0.85f, 0.45f, 0.22f);
                        shapeRenderer.rect(cellX + 3f, cellY + 3f, tileWidth - 6f, tileHeight - 6f);
                    }
                }

                shapeRenderer.setColor(0.92f, 0.96f, 1f, 0.34f);
                shapeRenderer.rect(cellX, cellY, 1f, tileHeight);
                shapeRenderer.rect(cellX + tileWidth - 1f, cellY, 1f, tileHeight);
                shapeRenderer.rect(cellX, cellY, tileWidth, 1f);
                shapeRenderer.rect(cellX, cellY + tileHeight - 1f, tileWidth, 1f);
            }
        }

        for (int r = 0; r < rows; r++) {
            if (r == 0 || fridge.getCompartmentForRow(r) != fridge.getCompartmentForRow(r - 1)) {
                FridgeCompartment compartment = fridge.getCompartmentForRow(r);
                Color stripeColor = new Color(getCompartmentColor(compartment));
                stripeColor.a = 0.75f;
                shapeRenderer.setColor(stripeColor);
                shapeRenderer.rect(interiorBounds.x, interiorBounds.y + r * tileHeight, 3f, tileHeight);
            }
        }
    }

    private void renderCompartmentLabelBackings(ShapeRenderer shapeRenderer, Fridge fridge) {
        float labelX = interiorBounds.x + interiorBounds.width + 4f;
        float labelWidth = 124f;
        for (FridgeCompartment compartment : FridgeCompartment.values()) {
            int firstRow = -1;
            int lastRow = -1;
            for (int row = 0; row < fridge.getRows(); row++) {
                if (fridge.getCompartmentForRow(row) == compartment) {
                    if (firstRow < 0) {
                        firstRow = row;
                    }
                    lastRow = row;
                }
            }
            if (firstRow < 0) {
                continue;
            }

            float centerY = interiorBounds.y + ((firstRow + lastRow + 1) * tileHeight) * 0.5f;
            float labelHeight = 38f;
            shapeRenderer.setColor(0.035f, 0.065f, 0.09f, 0.78f);
            drawRoundedRect(shapeRenderer, labelX, centerY - labelHeight * 0.5f, labelWidth, labelHeight, 5f);
            Color accent = new Color(getCompartmentColor(compartment));
            accent.a = 0.95f;
            shapeRenderer.setColor(accent);
            shapeRenderer.rect(labelX, centerY - labelHeight * 0.5f, 3f, labelHeight);
        }
    }

    private void renderPlacedItemFootprintPads(ShapeRenderer shapeRenderer, Fridge fridge) {
        for (Item item : fridge.getPlacedItems()) {
            GridPosition pos = item.getGridPosition();
            if (pos == null) {
                continue;
            }
            GridSize size = item.getSize();
            float drawX = interiorBounds.x + pos.getCol() * tileWidth + 4f;
            float drawY = interiorBounds.y + pos.getRow() * tileHeight + 4f;
            float drawW = size.getWidth() * tileWidth - 8f;
            float drawH = size.getHeight() * tileHeight - 8f;

            Color borderColor;
            if (item.isTrap()) {
                borderColor = new Color(0.96f, 0.48f, 0.50f, 0.95f);
            } else if (item.isConfirmed()) {
                borderColor = new Color(0.66f, 0.88f, 1f, 0.95f);
            } else {
                borderColor = new Color(0.88f, 0.94f, 1f, 0.80f);
            }

            shapeRenderer.setColor(0.025f, 0.045f, 0.06f, 0.20f);
            shapeRenderer.rect(drawX, drawY, drawW, drawH);
            shapeRenderer.setColor(borderColor);
            shapeRenderer.rect(drawX, drawY, drawW, 2f);
            shapeRenderer.rect(drawX, drawY + drawH - 2f, drawW, 2f);
            shapeRenderer.rect(drawX, drawY, 2f, drawH);
            shapeRenderer.rect(drawX + drawW - 2f, drawY, 2f, drawH);
        }
    }

    private void renderPlacementPreview(
        ShapeRenderer shapeRenderer,
        Fridge fridge,
        Item selectedItem,
        GridPosition hoverPos
    ) {
        PlacementResult result = fridge.evaluatePlacement(selectedItem, hoverPos);
        GridSize size = selectedItem.getSize();

        Color fillColor;
        Color borderColor;
        float pulse = 0.5f + 0.5f * MathUtils.sin(animationClock * 8f);

        if (result == PlacementResult.LEGAL) {
            fillColor = new Color(0.18f, 0.86f, 0.38f, 0.32f + pulse * 0.10f);
            borderColor = new Color(0.10f, 0.72f, 0.26f, 0.95f);
        } else {
            fillColor = new Color(0.92f, 0.20f, 0.24f, 0.36f + pulse * 0.10f);
            borderColor = new Color(0.82f, 0.10f, 0.14f, 0.95f);
        }

        for (int c = hoverPos.getCol(); c < hoverPos.getCol() + size.getWidth(); c++) {
            for (int r = hoverPos.getRow(); r < hoverPos.getRow() + size.getHeight(); r++) {
                float cellX = interiorBounds.x + c * tileWidth;
                float cellY = interiorBounds.y + r * tileHeight;
                shapeRenderer.setColor(fillColor);
                shapeRenderer.rect(cellX + 2f, cellY + 2f, tileWidth - 4f, tileHeight - 4f);

                shapeRenderer.setColor(borderColor);
                shapeRenderer.rect(cellX + 2f, cellY + 2f, tileWidth - 4f, 3f);
                shapeRenderer.rect(cellX + 2f, cellY + tileHeight - 5f, tileWidth - 4f, 3f);
                shapeRenderer.rect(cellX + 2f, cellY + 2f, 3f, tileHeight - 4f);
                shapeRenderer.rect(cellX + tileWidth - 5f, cellY + 2f, 3f, tileHeight - 4f);
            }
        }
    }

    private void renderPlacedItems(
        SpriteBatch batch,
        BitmapFont smallFont,
        AssetManager assetManager,
        Fridge fridge
    ) {
        List<Item> items = fridge.getPlacedItems();
        for (Item item : items) {
            GridPosition pos = item.getGridPosition();
            if (pos == null) {
                continue;
            }
            GridSize size = item.getSize();
            float drawX = interiorBounds.x + pos.getCol() * tileWidth;
            float drawY = interiorBounds.y + pos.getRow() * tileHeight;
            float drawW = size.getWidth() * tileWidth;
            float drawH = size.getHeight() * tileHeight;

            item.renderItemSprite(batch, assetManager, smallFont, drawX, drawY, drawW, drawH, 1.0f);

            if (item.isTrap()) {
                Texture warnTex = assetManager.getTexture(AssetManager.WARNING_ICON);
                batch.draw(warnTex, drawX + drawW - 28f, drawY + drawH - 28f, 24f, 24f);
                smallFont.setColor(1.0f, 0.55f, 0.55f, 1f);
                smallFont.draw(batch, "TRAP!", drawX + 8f, drawY + 20f);
            }
        }
        smallFont.setColor(Color.WHITE);
    }

    private void renderGhostSpritePreview(
        SpriteBatch batch,
        BitmapFont smallFont,
        AssetManager assetManager,
        Item selectedItem,
        GridPosition hoverPos
    ) {
        GridSize size = selectedItem.getSize();
        float drawX = interiorBounds.x + hoverPos.getCol() * tileWidth;
        float drawY = interiorBounds.y + hoverPos.getRow() * tileHeight;
        float drawW = size.getWidth() * tileWidth;
        float drawH = size.getHeight() * tileHeight;

        selectedItem.renderItemSprite(batch, assetManager, smallFont, drawX, drawY, drawW, drawH, 0.55f);
    }

    public static void drawRoundedRect(
        ShapeRenderer shapeRenderer,
        float x,
        float y,
        float width,
        float height,
        float radius
    ) {
        float r = Math.min(radius, Math.min(width, height) * 0.5f);
        shapeRenderer.rect(x + r, y, width - 2f * r, height);
        shapeRenderer.rect(x, y + r, r, height - 2f * r);
        shapeRenderer.rect(x + width - r, y + r, r, height - 2f * r);
        shapeRenderer.arc(x + r, y + r, r, 180f, 90f, 12);
        shapeRenderer.arc(x + width - r, y + r, r, 270f, 90f, 12);
        shapeRenderer.arc(x + width - r, y + height - r, r, 0f, 90f, 12);
        shapeRenderer.arc(x + r, y + height - r, r, 90f, 90f, 12);
    }

    public Rectangle getInteriorBounds() {
        return interiorBounds;
    }

    private static final class Gdx {
        static void glEnableBlend() {
            com.badlogic.gdx.Gdx.gl.glEnable(GL20.GL_BLEND);
            com.badlogic.gdx.Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
    }
}
