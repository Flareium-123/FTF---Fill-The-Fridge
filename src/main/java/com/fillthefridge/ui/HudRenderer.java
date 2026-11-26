package com.fillthefridge.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.fillthefridge.assets.AssetManager;
import com.fillthefridge.items.Item;
import com.fillthefridge.model.ActivePowerUps;
import com.fillthefridge.model.Fridge;
import com.fillthefridge.model.GameState;
import com.fillthefridge.model.PlacementResult;
import com.fillthefridge.model.PlayerLives;
import com.fillthefridge.model.TrashBin;
import com.fillthefridge.model.Tray;
import com.fillthefridge.util.GridPosition;
import com.fillthefridge.util.GridSize;

import java.util.ArrayList;
import java.util.List;

public class HudRenderer {
    private final List<Rectangle> traySlotBounds;
    private final Rectangle confirmButtonBounds;
    private final Rectangle undoButtonBounds;
    private final Rectangle freezeButtonBounds;
    private final Rectangle menuButtonBounds;

    private String bannerMessage;
    private float bannerTimer;
    private Color bannerColor;
    private float lifeLossShakeTimer;
    private float uiClock;

    // Reusable instances to avoid per-frame GC pressure
    private final Color tempColor1 = new Color();
    private final Color tempColor2 = new Color();
    private final GlyphLayout tempLayout = new GlyphLayout();
    private final GlyphLayout tempLayout2 = new GlyphLayout();

    public HudRenderer() {
        this.traySlotBounds = new ArrayList<>();
        this.confirmButtonBounds = new Rectangle(1015f, 544f, 225f, 48f);
        this.undoButtonBounds = new Rectangle(1015f, 484f, 225f, 48f);
        this.freezeButtonBounds = new Rectangle(1015f, 424f, 225f, 48f);
        this.menuButtonBounds = new Rectangle(1140f, 662f, 120f, 42f);
        this.bannerMessage = "";
        this.bannerTimer = 0f;
        this.bannerColor = new Color(0.95f, 0.75f, 0.15f, 1f);
    }

    public void update(float deltaTime, GameState gameState) {
        uiClock += deltaTime;
        if (bannerTimer > 0f) {
            bannerTimer = Math.max(0f, bannerTimer - deltaTime);
        }
        if (lifeLossShakeTimer > 0f) {
            lifeLossShakeTimer = Math.max(0f, lifeLossShakeTimer - deltaTime);
        }
        updateTrayAndTrashBounds(gameState);
    }

    public void updateTrayAndTrashBounds(GameState gameState) {
        TrashBin trashBin = gameState.getTrashBin();
        trashBin.setBounds(96f, 250f, 162f, 214f);

        Tray tray = gameState.getTray();
        tray.setBounds(200f, 10f, 880f, 162f);

        traySlotBounds.clear();
        List<Item> activeItems = tray.getActiveItems();
        int count = activeItems.size();
        if (count == 0) {
            return;
        }

        float slotW = Math.min(192f, (tray.getWidth() - 18f * (count - 1)) / count);
        float slotH = 152f;
        float gap = 18f;
        float totalW = count * slotW + (count - 1) * gap;
        float startX = tray.getX() + (tray.getWidth() - totalW) * 0.5f;
        float slotY = 14f;

        for (int i = 0; i < count; i++) {
            Rectangle rect = new Rectangle(startX + i * (slotW + gap), slotY, slotW, slotH);
            traySlotBounds.add(rect);
            activeItems.get(i).setBounds(rect.x, rect.y, rect.width, rect.height);
        }
    }

    public void showNotification(String message, Color color, float duration) {
        this.bannerMessage = message;
        this.bannerColor = new Color(color);
        this.bannerTimer = duration;
    }

    public void triggerLifeShake() {
        this.lifeLossShakeTimer = 0.65f;
    }

    public void renderHudAndTray(
            ShapeRenderer shapeRenderer,
            SpriteBatch batch,
            AssetManager assetManager,
            GameState gameState,
            GridPosition hoverGridPos,
            float mouseX,
            float mouseY,
            int undosUntilLifeLoss,
            String undoKeyLabel,
            String stageObjective
    ) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        renderTopBarShapes(shapeRenderer, mouseX, mouseY);
        renderTrashBinShapes(shapeRenderer, gameState.getTrashBin(), gameState.getTray().getSelectedItem());
        renderTrayBasketShapes(shapeRenderer, gameState.getTray());
        renderSideActionButtonsShapes(shapeRenderer, gameState, mouseX, mouseY);
        if (bannerTimer > 0f && bannerMessage != null && !bannerMessage.isEmpty()) {
            renderToastBannerShape(shapeRenderer);
        }
        shapeRenderer.end();

        batch.begin();
        renderTopBarTextAndIcons(batch, assetManager, gameState, stageObjective);
        renderTrashBinSpriteAndText(batch, assetManager, gameState.getTrashBin());
        renderTrayItemsAndTimers(batch, assetManager, gameState.getTray());
        renderSideActionButtonsText(
            batch, assetManager, gameState, hoverGridPos, undosUntilLifeLoss, undoKeyLabel
        );
        if (bannerTimer > 0f && bannerMessage != null && !bannerMessage.isEmpty()) {
            renderToastBannerText(batch, assetManager);
        }
        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        renderTrayTimerBars(shapeRenderer, gameState.getTray());
        shapeRenderer.end();
    }

    private void renderTopBarShapes(ShapeRenderer shapeRenderer, float mouseX, float mouseY) {
        shapeRenderer.setColor(0.035f, 0.065f, 0.10f, 0.80f);
        shapeRenderer.rect(0f, 652f, 1280f, 68f);

        shapeRenderer.setColor(0.72f, 0.86f, 0.96f, 0.20f);
        shapeRenderer.rect(0f, 652f, 1280f, 1f);

        boolean menuHover = menuButtonBounds.contains(mouseX, mouseY);
        tempColor1.set(menuHover ? 0.23f : 0.10f, menuHover ? 0.34f : 0.17f, menuHover ? 0.44f : 0.24f, menuHover ? 0.96f : 0.88f);
        shapeRenderer.setColor(tempColor1);
        ProceduralFridgeRenderer.drawRoundedRect(
                shapeRenderer,
                menuButtonBounds.x,
                menuButtonBounds.y,
                menuButtonBounds.width,
                menuButtonBounds.height,
                10f
        );
    }

    private void renderTopBarTextAndIcons(
        SpriteBatch batch,
        AssetManager assetManager,
        GameState gameState,
        String stageObjective
    ) {
        BitmapFont subtitleFont = assetManager.getSubtitleFont();
        BitmapFont bodyFont = assetManager.getBodyFont();
        BitmapFont smallFont = assetManager.getSmallFont();

        subtitleFont.setColor(Color.WHITE);
        subtitleFont.draw(batch, "STAGE " + gameState.getCurrentStageNumber(), 28f, 694f);
        smallFont.setColor(0.82f, 0.92f, 1.0f, 1f);
        smallFont.draw(
                batch,
                "(" + gameState.getFridge().getCols() + "x" + gameState.getFridge().getRows() + ")",
                152f,
                689f
        );

        PlayerLives lives = gameState.getPlayerLives();
        Texture heartTex = assetManager.getTexture(AssetManager.HEART);
        float shakeX = lifeLossShakeTimer > 0f ? MathUtils.sin(uiClock * 45f) * 5f : 0f;

        bodyFont.setColor(Color.WHITE);
        bodyFont.draw(batch, "LIVES:", 236f + shakeX, 691f);

        for (int i = 0; i < Math.max(PlayerLives.INITIAL_LIVES, lives.getCurrentLives()); i++) {
            float hx = 300f + i * 36f + shakeX;
            float hy = 669f;
            if (i < lives.getCurrentLives()) {
                batch.setColor(1f, 1f, 1f, 1f);
            } else {
                batch.setColor(0.25f, 0.28f, 0.32f, 0.55f);
            }
            batch.draw(heartTex, hx, hy, 30f, 30f);
        }
        batch.setColor(Color.WHITE);

        ActivePowerUps powerUps = gameState.getActivePowerUps();
        List<String> tags = new ArrayList<>();
        if (powerUps.getDeepFreezeCharges() > 0) {
            tags.add("F Freeze x" + powerUps.getDeepFreezeCharges());
        }
        if (powerUps.isGhostFitEnabled()) {
            tags.add("Ghost Fit");
        }
        if (powerUps.isSniffTestEnabled()) {
            tags.add("Traps Revealed");
        }
        if (powerUps.isFifoCaddyEnabled()) {
            tags.add("C FIFO");
        }
        if (powerUps.isReusableContainersEnabled()) {
            tags.add("R Rotate");
        }
        String powerSummary = "POWER-UPS " + powerUps.getAcquiredPowerUps().size()
                + (tags.isEmpty() ? "   Earn upgrades on the route map." : "   " + String.join("   ", tags));
        float originalScaleX = smallFont.getData().scaleX;
        float originalScaleY = smallFont.getData().scaleY;
        tempLayout.setText(smallFont, powerSummary);
        float summaryScale = Math.min(1f, 650f / Math.max(1f, tempLayout.width));
        if (summaryScale < 1f) {
            smallFont.getData().setScale(originalScaleX * summaryScale, originalScaleY * summaryScale);
            tempLayout.setText(smallFont, powerSummary);
        }
        smallFont.setColor(0.92f, 0.96f, 1.0f, 1f);
        smallFont.draw(batch, powerSummary, 468f, 699f);
        smallFont.getData().setScale(originalScaleX, originalScaleY);

        Fridge fridge = gameState.getFridge();
        List<String> stageRules = new ArrayList<>();
        if (fridge.getSpecialShelfRow() >= 0) {
            stageRules.add(
                "GOLD SHELF (row " + (fridge.getSpecialShelfRow() + 1) + "): max weight "
                    + fridge.getSpecialShelfMaxWeight()
            );
        }
        if (fridge.getPackingOrderTarget() != null) {
            stageRules.add("PACK " + fridge.getPackingOrderItemName() + " in the gold slot");
        }
        if (fridge.hasPrefilledItem()) {
            stageRules.add("Locked grocery is a fixed obstacle");
        }
        List<String> objectiveParts = new ArrayList<>();
        if (stageObjective != null && !stageObjective.isBlank()) {
            objectiveParts.add("OBJECTIVE: " + stageObjective);
        }
        objectiveParts.addAll(stageRules);
        if (!objectiveParts.isEmpty()) {
            String objectiveText = String.join("  |  ", objectiveParts);
            tempLayout.setText(smallFont, objectiveText);
            float objectiveScale = Math.min(1f, 1080f / Math.max(1f, tempLayout.width));
            if (objectiveScale < 1f) {
                smallFont.getData().setScale(originalScaleX * objectiveScale, originalScaleY * objectiveScale);
                tempLayout.setText(smallFont, objectiveText);
            }
            smallFont.setColor(0.88f, 0.91f, 0.84f, 1f);
            smallFont.draw(batch, objectiveText, 28f, 660f);
            smallFont.getData().setScale(originalScaleX, originalScaleY);
        }
        smallFont.setColor(Color.WHITE);

        bodyFont.setColor(Color.WHITE);
        bodyFont.draw(batch, "MENU [ESC]", menuButtonBounds.x + 15f, menuButtonBounds.y + 27f);
    }

    private void renderTrashBinShapes(ShapeRenderer shapeRenderer, TrashBin trashBin, Item selectedItem) {
        float x = trashBin.getX();
        float y = trashBin.getY();
        float w = trashBin.getWidth();
        float h = trashBin.getHeight();

        boolean highlightTrash = selectedItem != null && selectedItem.isTrap();
        tempColor1.set(
            trashBin.isHovered() ? 0.18f : 0.07f,
            trashBin.isHovered() ? 0.30f : 0.12f,
            trashBin.isHovered() ? 0.42f : 0.17f,
            trashBin.isHovered() ? 0.92f : 0.84f
        );
        shapeRenderer.setColor(tempColor1);
        ProceduralFridgeRenderer.drawRoundedRect(shapeRenderer, x, y, w, h, 18f);

        if (highlightTrash) {
            float pulse = 0.5f + 0.5f * MathUtils.sin(uiClock * 8f);
            shapeRenderer.setColor(0.98f, 0.58f, 0.16f, 0.65f + pulse * 0.3f);
            shapeRenderer.rect(x + 3f, y + 3f, w - 6f, 4f);
            shapeRenderer.rect(x + 3f, y + h - 7f, w - 6f, 4f);
            shapeRenderer.rect(x + 3f, y + 3f, 4f, h - 6f);
            shapeRenderer.rect(x + w - 7f, y + 3f, 4f, h - 6f);
        }
    }

    private void renderTrashBinSpriteAndText(SpriteBatch batch, AssetManager assetManager, TrashBin trashBin) {
        Texture trashTex = assetManager.getTexture(AssetManager.TRASH_BIN);
        BitmapFont bodyFont = assetManager.getBodyFont();
        BitmapFont smallFont = assetManager.getSmallFont();

        float bounce = trashBin.getBounceAnimationTimer() > 0f
                ? MathUtils.sin(trashBin.getBounceAnimationTimer() * 18f) * 8f
                : 0f;

        float iconSize = 98f + bounce;
        float iconX = trashBin.getX() + (trashBin.getWidth() - iconSize) * 0.5f;
        float iconY = trashBin.getY() + 66f;
        batch.draw(trashTex, iconX, iconY, iconSize, iconSize);

        tempLayout.setText(bodyFont, "TRASH BIN");
        bodyFont.setColor(Color.WHITE);
        bodyFont.draw(
                batch,
                "TRASH BIN",
                trashBin.getX() + (trashBin.getWidth() - tempLayout.width) * 0.5f,
                trashBin.getY() + 194f
        );

        String slotStr = "Slots: " + trashBin.getUsedSlots() + " / " + trashBin.getMaxSlots();
        tempLayout.setText(smallFont, slotStr);
        tempColor1.set(
            trashBin.hasAvailableSlot() ? 0.55f : 1.0f,
            trashBin.hasAvailableSlot() ? 0.95f : 0.45f,
            trashBin.hasAvailableSlot() ? 0.62f : 0.45f,
            1f
        );
        smallFont.setColor(tempColor1);
        smallFont.draw(
                batch,
                slotStr,
                trashBin.getX() + (trashBin.getWidth() - tempLayout.width) * 0.5f,
                trashBin.getY() + 46f
        );

        String hintStr = "Click to Trash";
        tempLayout.setText(smallFont, hintStr);
        smallFont.setColor(0.78f, 0.86f, 0.96f, 1f);
        smallFont.draw(
                batch,
                hintStr,
                trashBin.getX() + (trashBin.getWidth() - tempLayout.width) * 0.5f,
                trashBin.getY() + 24f
        );
        smallFont.setColor(Color.WHITE);
    }

    private void renderTrayBasketShapes(ShapeRenderer shapeRenderer, Tray tray) {
        List<Item> activeItems = tray.getActiveItems();
        shapeRenderer.setColor(0.035f, 0.065f, 0.10f, 0.78f);
        ProceduralFridgeRenderer.drawRoundedRect(shapeRenderer, 200f, 10f, 880f, 162f, 12f);

        for (int i = 0; i < traySlotBounds.size() && i < activeItems.size(); i++) {
            Rectangle slot = traySlotBounds.get(i);
            Item item = activeItems.get(i);

            boolean cardHover = item.isHovered() || item.isSelected();
            tempColor1.set(
                cardHover ? 0.12f : 0.07f,
                cardHover ? 0.17f : 0.11f,
                cardHover ? 0.20f : 0.15f,
                cardHover ? 0.94f : 0.86f
            );
            shapeRenderer.setColor(tempColor1);
            ProceduralFridgeRenderer.drawRoundedRect(shapeRenderer, slot.x, slot.y, slot.width, slot.height, 10f);

            tempColor1.set(
                item.isSelected() ? 0.98f : 0.56f,
                item.isSelected() ? 0.76f : 0.77f,
                item.isSelected() ? 0.16f : 0.91f,
                item.isSelected() ? 0.96f : (item.isHovered() ? 0.92f : 0.54f)
            );
            shapeRenderer.setColor(tempColor1);
            shapeRenderer.rect(slot.x, slot.y + slot.height - 2f, slot.width, 2f);
            shapeRenderer.rect(slot.x, slot.y, slot.width, 2f);
            shapeRenderer.rect(slot.x, slot.y, 2f, slot.height);
            shapeRenderer.rect(slot.x + slot.width - 2f, slot.y, 2f, slot.height);

            renderMiniFootprintGrid(shapeRenderer, slot, item.getSize());

            if (item.isRevealedTrap()) {
                shapeRenderer.setColor(0.92f, 0.22f, 0.28f, 0.92f);
                ProceduralFridgeRenderer.drawRoundedRect(
                        shapeRenderer,
                        slot.x + 10f,
                        slot.y + slot.height - 30f,
                        68f,
                        20f,
                        6f
                );
            }
        }

        if (tray.getPendingCount() > 0) {
            shapeRenderer.setColor(0.12f, 0.20f, 0.34f, 0.94f);
            ProceduralFridgeRenderer.drawRoundedRect(shapeRenderer, 1065f, 38f, 190f, 104f, 16f);
        }
    }

    private void renderMiniFootprintGrid(ShapeRenderer shapeRenderer, Rectangle slot, GridSize size) {
        int cols = size.getWidth();
        int rows = size.getHeight();
        float cellSq = 8.5f;
        float gap = 2.5f;
        float totalW = cols * cellSq + (cols - 1) * gap;
        float totalH = rows * cellSq + (rows - 1) * gap;

        float badgePad = 5f;
        float badgeW = totalW + badgePad * 2f;
        float badgeH = totalH + badgePad * 2f;
        float badgeX = slot.x + slot.width - badgeW - 10f;
        float badgeY = slot.y + slot.height - badgeH - 10f;

        shapeRenderer.setColor(0.14f, 0.24f, 0.40f, 0.88f);
        ProceduralFridgeRenderer.drawRoundedRect(shapeRenderer, badgeX, badgeY, badgeW, badgeH, 5f);

        shapeRenderer.setColor(0.45f, 0.82f, 1.00f, 1f);
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                float sqX = badgeX + badgePad + c * (cellSq + gap);
                float sqY = badgeY + badgePad + r * (cellSq + gap);
                shapeRenderer.rect(sqX, sqY, cellSq, cellSq);
            }
        }
    }

    private void renderTrayItemsAndTimers(SpriteBatch batch, AssetManager assetManager, Tray tray) {
        BitmapFont smallFont = assetManager.getSmallFont();
        BitmapFont bodyFont = assetManager.getBodyFont();
        List<Item> activeItems = tray.getActiveItems();

        for (int i = 0; i < traySlotBounds.size() && i < activeItems.size(); i++) {
            Rectangle slot = traySlotBounds.get(i);
            Item item = activeItems.get(i);

            item.renderItemSprite(
                    batch,
                    assetManager,
                    smallFont,
                    slot.x + 28f,
                    slot.y + 42f,
                    slot.width - 56f,
                    slot.height - 62f,
                    1.0f
            );

            tempLayout.setText(smallFont, item.getDisplayName());
            float originalScaleX = smallFont.getData().scaleX;
            float originalScaleY = smallFont.getData().scaleY;
            float nameScale = Math.min(1f, (slot.width - 16f) / tempLayout.width);
            smallFont.getData().setScale(originalScaleX * nameScale, originalScaleY * nameScale);
            smallFont.setColor(0.94f, 0.96f, 0.98f, 1f);
            smallFont.draw(
                    batch,
                    item.getDisplayName(),
                    slot.x + (slot.width - tempLayout.width * nameScale) * 0.5f,
                    slot.y + 36f
            );
            smallFont.getData().setScale(originalScaleX, originalScaleY);

            if (item.isRevealedTrap()) {
                smallFont.setColor(Color.WHITE);
                smallFont.draw(batch, "TRAP!", slot.x + 20f, slot.y + slot.height - 15f);
            }

            if (item.getFreezeTimer() > 0f) {
                Texture snowTex = assetManager.getTexture(AssetManager.SNOWFLAKE);
                batch.draw(snowTex, slot.x + 12f, slot.y + 46f, 26f, 26f);
            } else if (item.isCriticalThresholdReached()) {
                float pulse = 0.8f + 0.25f * MathUtils.sin(uiClock * 14f);
                Texture warnTex = assetManager.getTexture(AssetManager.WARNING_ICON);
                float iconSize = 26f * pulse;
                batch.draw(warnTex, slot.x + 12f, slot.y + 46f, iconSize, iconSize);
            }
        }

        if (tray.getPendingCount() > 0) {
            bodyFont.setColor(Color.WHITE);
            bodyFont.draw(batch, "CART QUEUE", 1098f, 118f);
            smallFont.setColor(0.75f, 0.90f, 1.0f, 1f);
            smallFont.draw(batch, "+" + tray.getPendingCount() + " more item(s)", 1102f, 90f);
            smallFont.draw(batch, "waiting to pack", 1102f, 66f);
        }

        smallFont.setColor(Color.WHITE);
    }

    private void renderTrayTimerBars(ShapeRenderer shapeRenderer, Tray tray) {
        List<Item> activeItems = tray.getActiveItems();
        for (int i = 0; i < traySlotBounds.size() && i < activeItems.size(); i++) {
            Rectangle slot = traySlotBounds.get(i);
            Item item = activeItems.get(i);

            float barX = slot.x + 14f;
            float barY = slot.y + 11f;
            float barW = slot.width - 28f;
            float barH = 9f;

            shapeRenderer.setColor(0.20f, 0.26f, 0.34f, 1f);
            shapeRenderer.rect(barX, barY, barW, barH);

            float ratio = item.getShelfLifeRatio();
            if (item.getFreezeTimer() > 0f) {
                shapeRenderer.setColor(0.32f, 0.82f, 0.98f, 1f);
            } else if (item.isInGracePeriod()) {
                float flash = 0.5f + 0.5f * MathUtils.sin(uiClock * 28f);
                shapeRenderer.setColor(1.0f, 0.15f, 0.15f, flash);
                ratio = item.getGraceTimeRemaining() / Item.GRACE_PERIOD_SECONDS;
            } else if (ratio <= Item.CRITICAL_THRESHOLD_RATIO) {
                shapeRenderer.setColor(0.95f, 0.20f, 0.20f, 1f);
            } else if (ratio <= Item.WARNING_THRESHOLD_RATIO) {
                shapeRenderer.setColor(0.96f, 0.68f, 0.12f, 1f);
            } else {
                shapeRenderer.setColor(0.22f, 0.80f, 0.36f, 1f);
            }

            shapeRenderer.rect(barX, barY, barW * MathUtils.clamp(ratio, 0f, 1f), barH);
        }
    }

    private void renderSideActionButtonsShapes(
            ShapeRenderer shapeRenderer,
            GameState gameState,
            float mouseX,
            float mouseY
    ) {
        boolean confirmHover = confirmButtonBounds.contains(mouseX, mouseY);
        tempColor1.set(
            confirmHover ? 0.19f : 0.12f,
            confirmHover ? 0.56f : 0.40f,
            confirmHover ? 0.39f : 0.29f,
            confirmHover ? 0.97f : 0.92f
        );
        shapeRenderer.setColor(tempColor1);
        ProceduralFridgeRenderer.drawRoundedRect(
                shapeRenderer,
                confirmButtonBounds.x,
                confirmButtonBounds.y,
                confirmButtonBounds.width,
                confirmButtonBounds.height,
                8f
        );

        boolean undoHover = undoButtonBounds.contains(mouseX, mouseY);
        tempColor1.set(
            undoHover ? 0.31f : 0.16f,
            undoHover ? 0.39f : 0.23f,
            undoHover ? 0.46f : 0.30f,
            undoHover ? 0.97f : 0.90f
        );
        shapeRenderer.setColor(tempColor1);
        ProceduralFridgeRenderer.drawRoundedRect(
                shapeRenderer,
                undoButtonBounds.x,
                undoButtonBounds.y,
                undoButtonBounds.width,
                undoButtonBounds.height,
                8f
        );

        int freezeCharges = gameState.getActivePowerUps().getDeepFreezeCharges();
        boolean freezeHover = freezeButtonBounds.contains(mouseX, mouseY) && freezeCharges > 0;
        if (freezeCharges > 0) {
            tempColor1.set(
                freezeHover ? 0.20f : 0.12f,
                freezeHover ? 0.48f : 0.31f,
                freezeHover ? 0.62f : 0.42f,
                freezeHover ? 0.97f : 0.92f
            );
        } else {
            tempColor1.set(0.12f, 0.16f, 0.20f, 0.78f);
        }
        shapeRenderer.setColor(tempColor1);
        ProceduralFridgeRenderer.drawRoundedRect(
                shapeRenderer,
                freezeButtonBounds.x,
                freezeButtonBounds.y,
                freezeButtonBounds.width,
                freezeButtonBounds.height,
                8f
        );

        shapeRenderer.setColor(0.035f, 0.065f, 0.10f, 0.82f);
        ProceduralFridgeRenderer.drawRoundedRect(shapeRenderer, 1015f, 224f, 225f, 182f, 10f);
        shapeRenderer.setColor(0.035f, 0.065f, 0.10f, 0.82f);
        ProceduralFridgeRenderer.drawRoundedRect(shapeRenderer, 1015f, 186f, 225f, 32f, 8f);
    }

    private void renderSideActionButtonsText(
            SpriteBatch batch,
            AssetManager assetManager,
            GameState gameState,
            GridPosition hoverGridPos,
            int undosUntilLifeLoss,
            String undoKeyLabel
    ) {
        BitmapFont bodyFont = assetManager.getBodyFont();
        BitmapFont smallFont = assetManager.getSmallFont();

        bodyFont.setColor(Color.WHITE);
        bodyFont.draw(batch, "Lock [SPACE]", confirmButtonBounds.x + 44f, confirmButtonBounds.y + 31f);
        bodyFont.draw(
            batch,
            "UNDO [" + undoKeyLabel + "] (" + undosUntilLifeLoss + " LEFT)",
            undoButtonBounds.x + 34f,
            undoButtonBounds.y + 31f
        );

        int freezeCharges = gameState.getActivePowerUps().getDeepFreezeCharges();
        bodyFont.draw(
                batch,
                "Freeze [F] (" + freezeCharges + ")",
                freezeButtonBounds.x + 40f,
                freezeButtonBounds.y + 31f
        );

        smallFont.setColor(0.95f, 0.88f, 0.42f, 1f);
        smallFont.draw(batch, "CONTROLS & RULES", 1042f, 386f);

        smallFont.setColor(0.86f, 0.92f, 0.98f, 1f);
        smallFont.draw(batch, "L-Click: Select / Place", 1032f, 362f);
        smallFont.draw(batch, "Undo: button / " + undoKeyLabel + " / R-Click", 1032f, 340f);
        smallFont.draw(batch, "1 life per 3 undos", 1032f, 318f);
        smallFont.draw(batch, "Space: Lock placements", 1032f, 296f);
        smallFont.draw(batch, "Trash Bin: Trash traps", 1032f, 274f);
        ActivePowerUps activePowerUps = gameState.getActivePowerUps();
        if (activePowerUps.isFifoCaddyEnabled()) {
            smallFont.draw(batch, "C: Pause urgent food (" + activePowerUps.getFifoCaddyCharges() + ")",
                    1032f, 252f);
        }
        if (activePowerUps.isReusableContainersEnabled()) {
            smallFont.draw(batch, "R: Rotate selected grocery", 1032f, 232f);
        } else if (activePowerUps.isShelfRailsEnabled()) {
            smallFont.draw(batch, "Click locked food: Shelf Rails", 1032f, 232f);
        }

        Item selected = gameState.getTray().getSelectedItem();
        String statusText;
        Color statusColor;
        if (selected != null && hoverGridPos != null) {
            PlacementResult eval = gameState.getFridge().evaluatePlacement(selected, hoverGridPos);
            tempColor1.set(
                eval == PlacementResult.LEGAL ? 0.35f : 1.0f,
                eval == PlacementResult.LEGAL ? 0.96f : 0.42f,
                eval == PlacementResult.LEGAL ? 0.52f : 0.42f,
                1f
            );
            statusColor = tempColor1;
            statusText = "Preview: " + eval.name();
        } else if (selected != null) {
            tempColor1.set(0.65f, 0.90f, 1.0f, 1f);
            statusColor = tempColor1;
            statusText = "Holding: " + selected.getDisplayName();
        } else {
            tempColor1.set(0.72f, 0.78f, 0.86f, 1f);
            statusColor = tempColor1;
            statusText = "Select a tray item!";
        }
        TextLayoutHelper.drawFittedAtBaseline(
                batch,
                smallFont,
                statusText,
                1025f,
                206f,
                205f,
                statusColor,
                Align.center
        );
        smallFont.setColor(Color.WHITE);
    }

    private void renderToastBannerShape(ShapeRenderer shapeRenderer) {
        float alpha = Math.min(1f, bannerTimer * 2.5f);
        shapeRenderer.setColor(0.09f, 0.15f, 0.26f, 0.94f * alpha);
        ProceduralFridgeRenderer.drawRoundedRect(shapeRenderer, 310f, 604f, 660f, 40f, 12f);

        shapeRenderer.setColor(bannerColor.r, bannerColor.g, bannerColor.b, alpha);
        ProceduralFridgeRenderer.drawRoundedRect(shapeRenderer, 314f, 607f, 8f, 34f, 4f);
    }

    private void renderToastBannerText(SpriteBatch batch, AssetManager assetManager) {
        BitmapFont bodyFont = assetManager.getBodyFont();
        float alpha = Math.min(1f, bannerTimer * 2.5f);
        bodyFont.setColor(bannerColor.r, bannerColor.g, bannerColor.b, alpha);

        tempLayout.setText(bodyFont, bannerMessage);
        float textX = 640f - tempLayout.width * 0.5f;
        bodyFont.draw(batch, bannerMessage, textX, 630f);
        bodyFont.setColor(Color.WHITE);
    }

    public void renderStageClearBanner(
            ShapeRenderer shapeRenderer,
            SpriteBatch batch,
            AssetManager assetManager,
            int stageNumber,
            float remainingBannerTimer
    ) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        float progress = 1.0f - (remainingBannerTimer / 1.85f);
        float scale = MathUtils.clamp(0.75f + progress * 3.0f, 0.75f, 1f);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.05f, 0.10f, 0.18f, 0.68f);
        shapeRenderer.rect(0f, 0f, 1280f, 720f);

        for (int i = 0; i < 28; i++) {
            float cx = (i * 97f + progress * 240f * ((i % 2 == 0) ? 1f : -1f)) % 1280f;
            if (cx < 0f) {
                cx += 1280f;
            }

            float cy = 720f - ((i * 63f + progress * 520f) % 720f);
            shapeRenderer.setColor(
                    0.3f + (i % 3) * 0.3f,
                    0.4f + ((i + 1) % 3) * 0.25f,
                    0.35f + ((i + 2) % 3) * 0.3f,
                    0.9f
            );
            shapeRenderer.circle(cx, cy, 7f);
        }

        float boxW = 620f * scale;
        float boxH = 180f * scale;
        float boxX = 640f - boxW * 0.5f;
        float boxY = 360f - boxH * 0.5f;

        shapeRenderer.setColor(0.16f, 0.58f, 0.34f, 0.98f);
        ProceduralFridgeRenderer.drawRoundedRect(shapeRenderer, boxX, boxY, boxW, boxH, 24f);
        shapeRenderer.setColor(0.98f, 0.84f, 0.18f, 1f);
        ProceduralFridgeRenderer.drawRoundedRect(shapeRenderer, boxX + 8f, boxY + 8f, boxW - 16f, boxH - 16f, 18f);
        shapeRenderer.setColor(0.12f, 0.24f, 0.42f, 1f);
        ProceduralFridgeRenderer.drawRoundedRect(shapeRenderer, boxX + 14f, boxY + 14f, boxW - 28f, boxH - 28f, 14f);
        shapeRenderer.end();

        if (scale > 0.7f) {
            batch.begin();
            BitmapFont titleFont = assetManager.getTitleFont();
            BitmapFont subtitleFont = assetManager.getSubtitleFont();

            String headline = "STAGE " + stageNumber + " CLEARED!";
            tempLayout.setText(titleFont, headline);
            titleFont.setColor(0.98f, 0.90f, 0.25f, 1f);
            titleFont.draw(batch, headline, 640f - tempLayout.width * 0.5f, 395f);

            String sub = "Every fridge tile packed to perfection!";
            tempLayout2.setText(subtitleFont, sub);
            subtitleFont.setColor(Color.WHITE);
            subtitleFont.draw(batch, sub, 640f - tempLayout2.width * 0.5f, 338f);
            batch.end();
        }
    }

    public void renderBossHealth(
            ShapeRenderer shapeRenderer,
            SpriteBatch batch,
            AssetManager assetManager,
            int health
    ) {
        float barX = 1018f;
        float barY = 566f;
        float barWidth = 218f;
        float progress = MathUtils.clamp(health / 3f, 0f, 1f);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.08f, 0.13f, 0.22f, 0.94f);
        ProceduralFridgeRenderer.drawRoundedRect(shapeRenderer, barX - 4f, barY - 24f, barWidth + 8f, 66f, 10f);
        shapeRenderer.setColor(0.34f, 0.12f, 0.16f, 1f);
        shapeRenderer.rect(barX, barY, barWidth, 14f);
        shapeRenderer.setColor(0.92f, 0.20f, 0.28f, 1f);
        shapeRenderer.rect(barX, barY, barWidth * progress, 14f);
        shapeRenderer.end();

        batch.begin();
        BitmapFont smallFont = assetManager.getSmallFont();
        smallFont.setColor(1f, 0.76f, 0.78f, 1f);
        smallFont.draw(batch, "BOSS - SORTS LEFT: " + health + "/3", barX + 8f, barY + 27f);
        smallFont.setColor(Color.WHITE);
        batch.end();
    }

    public List<Rectangle> getTraySlotBounds() {
        return traySlotBounds;
    }

    public Rectangle getConfirmButtonBounds() {
        return confirmButtonBounds;
    }

    public Rectangle getUndoButtonBounds() {
        return undoButtonBounds;
    }

    public Rectangle getFreezeButtonBounds() {
        return freezeButtonBounds;
    }

    public Rectangle getMenuButtonBounds() {
        return menuButtonBounds;
    }
}
