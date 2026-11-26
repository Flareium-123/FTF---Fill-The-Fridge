package com.fillthefridge.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.fillthefridge.FillTheFridgeGame;
import com.fillthefridge.assets.AssetManager;

public class TutorialScreen extends BaseScreen {
    private static final Rectangle BOARD = new Rectangle(500f, 245f, 300f, 270f);
    private static final Rectangle TRASH = new Rectangle(550f, 160f, 200f, 56f);
    private static final Rectangle BACK = new Rectangle(930f, 55f, 220f, 54f);
    private static final Rectangle[] TRAY_CARDS = {
        new Rectangle(190f, 430f, 235f, 68f),
        new Rectangle(190f, 340f, 235f, 68f),
        new Rectangle(190f, 250f, 235f, 68f),
        new Rectangle(190f, 160f, 235f, 68f)
    };
    private static final String[] ITEM_IDS = {"soda_can", "sausage", "tomato", "empty_can"};
    private static final String[] ITEM_NAMES = {"Soda can", "Sausage", "Tomato", "Empty can"};
    private static final String[] TEXTURE_NAMES = {
        AssetManager.SODA_CAN, AssetManager.SAUSAGE, AssetManager.TOMATO, AssetManager.EMPTY_CAN
    };
    private static final String[] STEP_TEXT = {
        "Step 1: Select the soda can from the tray.",
        "Step 2: Place it on the blue LOWER CHILLER shelf. Grouping drinks together keeps the fridge organized and makes them easy to find.",
        "Step 3: Select the sausage from the tray.",
        "Step 4: Put meat in the purple top compartment in this game. In a real fridge, keep raw meat sealed and below ready-to-eat food.",
        "Step 5: Select the tomato from the tray.",
        "Step 6: Put produce in the green CHILLER. Grouping and separating foods helps prevent cross-contamination and keeps storage organized.",
        "Step 7: Select the empty can. This is a trap item, not a grocery.",
        "Step 8: Put the empty can in the trash. Trash in a fridge can contaminate food and takes up safe space; throwing away fresh food wastes it.",
        "Tutorial complete! You sorted groceries by compartment and disposed of trash safely."
    };

    private final String[] placedItemIds = new String[3];
    private int step;
    private String selectedItemId;
    private String feedback = "";

    public TutorialScreen(FillTheFridgeGame game) {
        super(game);
    }

    @Override
    public void render(float delta) {
        updateTransition(delta);
        Vector2 mouse = getMouseWorldCoords();
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            transitionTo(new MainMenuScreen(game));
        } else if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            handleClick(mouse.x, mouse.y);
        }

        Gdx.gl.glClearColor(0.08f, 0.15f, 0.25f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        ShapeRenderer shapes = game.getShapeRenderer();
        SpriteBatch batch = game.getSpriteBatch();
        AssetManager assets = game.getAssetManager();
        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.rect(0f, 0f, VIRTUAL_WIDTH, VIRTUAL_HEIGHT, new Color(0.08f, 0.15f, 0.25f, 1f),
            new Color(0.08f, 0.15f, 0.25f, 1f), new Color(0.18f, 0.35f, 0.56f, 1f),
            new Color(0.18f, 0.35f, 0.56f, 1f));
        shapes.setColor(0.08f, 0.13f, 0.22f, 0.96f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, 140f, 125f, 1000f, 545f, 24f);
        drawTutorialBoard(shapes);
        drawTrayCards(shapes);
        shapes.setColor(0.18f, 0.54f, 0.82f, 1f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, TRASH.x, TRASH.y, TRASH.width, TRASH.height, 12f);
        drawStyledButtonShape(shapes, BACK, new Color(0.20f, 0.50f, 0.86f, 1f),
            new Color(0.28f, 0.60f, 0.96f, 1f), BACK.contains(mouse.x, mouse.y));
        shapes.end();

        batch.begin();
        drawTutorialText(batch, assets);
        drawTrayItems(batch, assets);
        drawPlacedItems(batch, assets);
        drawCenteredText(batch, assets.getSmallFont(), "TRASH BIN", TRASH, Color.WHITE);
        drawCenteredText(batch, assets.getBodyFont(), "BACK TO MENU", BACK, Color.WHITE);
        batch.end();
        renderTransitionOverlay(shapes);
    }

    private void handleClick(float x, float y) {
        if (BACK.contains(x, y)) {
            transitionTo(new MainMenuScreen(game));
            return;
        }
        if (step >= STEP_TEXT.length - 1) {
            return;
        }
        if (step % 2 == 0) {
            int cardIndex = step / 2;
            if (cardIndex < TRAY_CARDS.length && TRAY_CARDS[cardIndex].contains(x, y)) {
                selectedItemId = ITEM_IDS[cardIndex];
                step++;
                feedback = "";
            } else {
                feedback = "Select the highlighted item in the tray.";
            }
            return;
        }

        if (step == 7) {
            if (TRASH.contains(x, y) && "empty_can".equals(selectedItemId)) {
                selectedItemId = null;
                step++;
                feedback = "";
            } else {
                feedback = "Place the empty can in the TRASH BIN, not the fridge.";
            }
            return;
        }

        if (!BOARD.contains(x, y)) {
            feedback = "Place the selected grocery inside the fridge.";
            return;
        }
        int clickedRow = Math.min(2, (int) ((y - BOARD.y) / (BOARD.height / 3f)));
        int requiredRow = switch (step) {
            case 1 -> 0;
            case 3 -> 2;
            case 5 -> 1;
            default -> -1;
        };
        if (clickedRow != requiredRow) {
            feedback = "Not quite. Follow the compartment labels and matching tile color.";
            return;
        }
        placedItemIds[clickedRow] = selectedItemId;
        selectedItemId = null;
        step++;
        feedback = "";
    }

    private void drawTutorialBoard(ShapeRenderer shapes) {
        Color[] colors = {
            new Color(0.30f, 0.67f, 0.94f, 0.35f),
            new Color(0.34f, 0.78f, 0.56f, 0.35f),
            new Color(0.70f, 0.54f, 0.96f, 0.35f)
        };
        float rowHeight = BOARD.height / 3f;
        for (int row = 0; row < 3; row++) {
            float rowY = BOARD.y + row * rowHeight;
            for (int col = 0; col < 3; col++) {
                shapes.setColor(colors[row]);
                shapes.rect(BOARD.x + col * 100f + 2f, rowY + 2f, 96f, rowHeight - 4f);
            }
            shapes.setColor(0.82f, 0.90f, 0.96f, 0.8f);
            shapes.rect(BOARD.x, rowY, BOARD.width, 2f);
        }
        shapes.setColor(0.82f, 0.90f, 0.96f, 0.8f);
        shapes.rect(BOARD.x, BOARD.y + BOARD.height, BOARD.width, 2f);
        for (int col = 0; col <= 3; col++) {
            shapes.rect(BOARD.x + col * 100f, BOARD.y, 2f, BOARD.height);
        }
    }

    private void drawTrayCards(ShapeRenderer shapes) {
        for (int i = 0; i < TRAY_CARDS.length; i++) {
            boolean highlighted = step / 2 == i && step < 8 && step % 2 == 0;
            shapes.setColor(highlighted
                ? new Color(0.20f, 0.62f, 0.88f, 1f)
                : new Color(0.14f, 0.24f, 0.36f, 1f));
            Rectangle card = TRAY_CARDS[i];
            ProceduralFridgeRenderer.drawRoundedRect(shapes, card.x, card.y, card.width, card.height, 10f);
        }
    }

    private void drawTutorialText(SpriteBatch batch, AssetManager assets) {
        BitmapFont title = assets.getTitleFont();
        BitmapFont heading = assets.getSubtitleFont();
        BitmapFont body = assets.getBodyFont();
        BitmapFont small = assets.getSmallFont();

        TextLayoutHelper.drawFittedText(batch, title, "LEARN BY SORTING", 400f, 610f, 480f, 45f,
            new Color(0.98f, 0.90f, 0.25f, 1f), Align.center);
        TextLayoutHelper.drawFittedText(batch, heading, "Grocery tray", 190f, 535f, 235f, 30f,
            new Color(0.45f, 0.82f, 1f, 1f), Align.left);
        TextLayoutHelper.drawFittedText(batch, heading, "Your fridge", 500f, 535f, 300f, 30f,
            new Color(0.45f, 0.82f, 1f, 1f), Align.center);
        TextLayoutHelper.drawFittedText(batch, heading, "Step-by-step", 850f, 535f, 260f, 30f,
            new Color(0.45f, 0.82f, 1f, 1f), Align.left);

        TextLayoutHelper.drawWrappedText(
            batch,
            body,
            STEP_TEXT[Math.min(step, STEP_TEXT.length - 1)],
            850f,
            365f,
            260f,
            155f,
            Color.WHITE,
            Align.left
        );

        small.setColor(0.85f, 0.91f, 0.98f, 1f);
        small.draw(batch, "FREEZER / MEAT", BOARD.x + 10f, BOARD.y + 3f * (BOARD.height / 3f) - 12f);
        small.draw(batch, "CHILLER / PRODUCE", BOARD.x + 10f, BOARD.y + 2f * (BOARD.height / 3f) - 12f);
        small.draw(batch, "LOWER CHILLER / LIQUIDS", BOARD.x + 10f, BOARD.y + BOARD.height / 3f - 12f);

        if (!feedback.isEmpty()) {
            TextLayoutHelper.drawWrappedText(
                batch,
                small,
                feedback,
                850f,
                300f,
                260f,
                55f,
                new Color(1f, 0.76f, 0.38f, 1f),
                Align.left
            );
        }

        TextLayoutHelper.drawWrappedText(
            batch,
            small,
            "Select a tray item, then click a shelf or the bin as instructed.",
            850f,
            230f,
            260f,
            60f,
            new Color(0.76f, 0.86f, 0.96f, 1f),
            Align.left
        );
    }

    private void drawTrayItems(SpriteBatch batch, AssetManager assets) {
        for (int i = 0; i < TRAY_CARDS.length; i++) {
            Rectangle card = TRAY_CARDS[i];
            Texture texture = assets.getTexture(TEXTURE_NAMES[i]);
            batch.draw(texture, card.x + 10f, card.y + 8f, 50f, 50f);
            BitmapFont small = assets.getSmallFont();
            TextLayoutHelper.drawFittedText(
                batch,
                small,
                ITEM_NAMES[i],
                card.x + 68f,
                card.y + 32f,
                card.width - 76f,
                24f,
                Color.WHITE,
                Align.left
            );
            if (ITEM_IDS[i].equals(selectedItemId)) {
                TextLayoutHelper.drawFittedText(
                    batch,
                    small,
                    "SELECTED",
                    card.x + 68f,
                    card.y + 10f,
                    card.width - 76f,
                    20f,
                    new Color(0.98f, 0.86f, 0.30f, 1f),
                    Align.left
                );
            }
        }
    }

    private void drawPlacedItems(SpriteBatch batch, AssetManager assets) {
        for (int row = 0; row < placedItemIds.length; row++) {
            String itemId = placedItemIds[row];
            if (itemId == null) {
                continue;
            }
            int index = indexForItem(itemId);
            batch.draw(assets.getTexture(TEXTURE_NAMES[index]), BOARD.x + 220f,
                BOARD.y + row * (BOARD.height / 3f) + 16f,
                58f, 58f);
        }
    }

    private int indexForItem(String itemId) {
        for (int i = 0; i < ITEM_IDS.length; i++) {
            if (ITEM_IDS[i].equals(itemId)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Unknown tutorial item: " + itemId);
    }
}
