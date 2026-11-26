package com.fillthefridge.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.fillthefridge.FillTheFridgeGame;
import com.fillthefridge.assets.AssetManager;
import com.fillthefridge.events.PreBossEventCard;

/**
 * Screen displayed before the boss stage. Presents a mandatory event card
 * with 3 choices — the player picks one to gain a buff/debuff for the boss fight.
 * Follows the Slay the Spire campfire/event node style.
 */
public class PreBossEventScreen extends BaseScreen {
    private static final int CHOICE_COUNT = 3;

    private final Rectangle choice1Button = new Rectangle(60f, 180f, 360f, 380f);
    private final Rectangle choice2Button = new Rectangle(460f, 180f, 360f, 380f);
    private final Rectangle choice3Button = new Rectangle(860f, 180f, 360f, 380f);
    private final Rectangle[] choiceButtons = {choice1Button, choice2Button, choice3Button};

    private final PreBossEventCard eventCard;
    private final MenuSelection menuSelection = new MenuSelection(CHOICE_COUNT);
    private final GlyphLayout layout = new GlyphLayout();

    // Reusable objects to avoid per-frame GC pressure
    private final Color tempColor = new Color();
    private final Rectangle tempBounds = new Rectangle();

    public PreBossEventScreen(FillTheFridgeGame game) {
        super(game);
        PreBossEventCard card = game.getGameController().getPreBossEventCard();
        if (card == null) {
            throw new IllegalStateException("PreBossEventCard must not be null when creating PreBossEventScreen");
        }
        this.eventCard = card;
    }

    @Override
    public void render(float delta) {
        updateTransition(delta);

        Vector2 mouse = getMouseWorldCoords();

        // Handle input
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            menuSelection.selectPrev();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            menuSelection.selectNext();
        }

        boolean clicked = false;
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            for (int i = 0; i < choiceButtons.length; i++) {
                if (choiceButtons[i].contains(mouse.x, mouse.y)) {
                    menuSelection.setSelectedIndex(i);
                    clicked = true;
                    break;
                }
            }
        }

        if (eventCard != null && (clicked || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
            || Gdx.input.isKeyJustPressed(Input.Keys.SPACE))) {
            int choice = menuSelection.getSelectedIndex();
            eventCard.selectChoice(choice);
            game.getAudioFeedback().playClick();
            game.getGameController().confirmPreBossEvent();
            transitionTo(new BossIntroScreen(game));
        }


        Gdx.gl.glClearColor(0.08f, 0.12f, 0.18f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        ShapeRenderer shapes = game.getShapeRenderer();
        SpriteBatch batch = game.getSpriteBatch();
        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        // === Shape rendering phase ===
        renderBackground(shapes);
        renderTitleShapes(shapes);
        renderEventDescriptionShapes(shapes);
        renderChoiceCardShapes(shapes, mouse);

        // === Text rendering phase ===
        renderTitleText(batch);
        renderEventDescriptionText(batch);
        renderChoiceCardText(batch);
        renderTransitionOverlay(shapes);
    }

    /* ---------- Shape rendering ---------- */

    private void renderBackground(ShapeRenderer shapes) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        // Dark blue background with subtle gradient
        shapes.setColor(0.08f, 0.12f, 0.18f, 1f);
        shapes.rect(0f, 0f, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        shapes.end();
    }

    private void renderTitleShapes(ShapeRenderer shapes) {
        // Decorative line under title
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.77f, 0.67f, 0.47f, 0.6f);
        shapes.rect(300f, 680f, 260f, 3f);
        shapes.end();
    }

    private void renderEventDescriptionShapes(ShapeRenderer shapes) {
        // Event card background
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.12f, 0.18f, 0.26f, 0.9f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, 300f, 600f, 260f, 70f, 10f);
        shapes.end();
    }

    private void renderChoiceCardShapes(ShapeRenderer shapes, Vector2 mouse) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < choiceButtons.length; i++) {
            Rectangle bounds = choiceButtons[i];
            boolean highlighted = i == menuSelection.getSelectedIndex();
            boolean hovered = bounds.contains(mouse.x, mouse.y);

            // Card background
            if (highlighted) {
                shapes.setColor(0.18f, 0.28f, 0.42f, 0.95f);
            } else if (hovered) {
                shapes.setColor(0.15f, 0.24f, 0.38f, 0.9f);
            } else {
                shapes.setColor(0.10f, 0.16f, 0.24f, 0.85f);
            }
            ProceduralFridgeRenderer.drawRoundedRect(shapes, bounds.x, bounds.y, bounds.width, bounds.height, 14f);

            // Card border
            if (highlighted) {
                shapes.setColor(0.77f, 0.67f, 0.47f, 0.9f);
            } else {
                shapes.setColor(0.30f, 0.38f, 0.46f, 0.6f);
            }
            shapes.rect(bounds.x, bounds.y, bounds.width, 2f);
            shapes.rect(bounds.x, bounds.y + bounds.height - 2f, bounds.width, 2f);
            shapes.rect(bounds.x, bounds.y, 2f, bounds.height);
            shapes.rect(bounds.x + bounds.width - 2f, bounds.y, 2f, bounds.height);
        }
        shapes.end();
    }

    /* ---------- Text rendering ---------- */

    private void renderTitleText(SpriteBatch batch) {
        AssetManager assets = game.getAssetManager();
        BitmapFont titleFont = assets.getTitleFont();
        BitmapFont subtitleFont = assets.getSubtitleFont();

        batch.begin();

        // Title
        titleFont.setColor(0.92f, 0.84f, 0.64f, 1f);
        tempColor.set(0.92f, 0.84f, 0.64f, 1f);
        TextLayoutHelper.drawFittedAtBaseline(
            batch, titleFont, "FINAL INSPECTION", 430f, 710f, 420f,
            tempColor, com.badlogic.gdx.utils.Align.center
        );

        // Subtitle
        subtitleFont.setColor(0.68f, 0.76f, 0.76f, 1f);
        tempColor.set(0.68f, 0.76f, 0.76f, 1f);
        TextLayoutHelper.drawFittedAtBaseline(
            batch, subtitleFont, "CHOOSE YOUR STRATEGY BEFORE THE BOSS FIGHT", 400f, 675f, 500f,
            tempColor, com.badlogic.gdx.utils.Align.center
        );

        batch.end();
    }

    private void renderEventDescriptionText(SpriteBatch batch) {
        AssetManager assets = game.getAssetManager();
        BitmapFont smallFont = assets.getSmallFont();

        batch.begin();

        // Event icon symbol
        smallFont.setColor(0.92f, 0.72f, 0.35f, 1f);
        TextLayoutHelper.drawFittedAtBaseline(
            batch, smallFont, eventCard.getIconSymbol(), 430f, 655f, 60f,
            smallFont.getColor(), com.badlogic.gdx.utils.Align.center
        );

        // Event description
        smallFont.setColor(0.82f, 0.88f, 0.92f, 1f);
        tempBounds.set(320f, 610f, 220f, 50f);
        drawWrappedText(batch, smallFont, eventCard.getDescription(), tempBounds,
            smallFont.getColor(), Align.center);

        batch.end();
    }

    private void renderChoiceCardText(SpriteBatch batch) {
        AssetManager assets = game.getAssetManager();
        BitmapFont smallFont = assets.getSmallFont();
        BitmapFont bodyFont = assets.getBodyFont();

        batch.begin();

        PreBossEventCard.EventChoice[] choices = eventCard.getChoices().toArray(new PreBossEventCard.EventChoice[0]);

        for (int i = 0; i < choiceButtons.length && i < choices.length; i++) {
            Rectangle bounds = choiceButtons[i];
            boolean highlighted = i == menuSelection.getSelectedIndex();
            PreBossEventCard.EventChoice choice = choices[i];

            // Icon symbol
            if (highlighted) {
                smallFont.setColor(0.77f, 0.67f, 0.47f, 1f);
            } else {
                smallFont.setColor(0.60f, 0.70f, 0.80f, 1f);
            }
            TextLayoutHelper.drawFittedAtBaseline(
                batch, smallFont, choice.getIconSymbol(),
                bounds.x + bounds.width * 0.5f, bounds.y + bounds.height - 30f, 60f,
                smallFont.getColor(), com.badlogic.gdx.utils.Align.center
            );

            // Choice title
            if (highlighted) {
                bodyFont.setColor(0.92f, 0.84f, 0.64f, 1f);
            } else {
                bodyFont.setColor(0.78f, 0.86f, 0.92f, 1f);
            }
            tempBounds.set(bounds.x + 16f, bounds.y + bounds.height - 70f,
                bounds.width - 32f, 30f);
            drawFittedText(batch, bodyFont, choice.getTitle(), tempBounds, bodyFont.getColor());

            // Choice description
            if (highlighted) {
                smallFont.setColor(0.82f, 0.88f, 0.92f, 1f);
            } else {
                smallFont.setColor(0.60f, 0.68f, 0.76f, 1f);
            }
            tempBounds.set(bounds.x + 16f, bounds.y + 20f,
                bounds.width - 32f, bounds.height - 100f);
            drawWrappedText(batch, smallFont, choice.getDescription(), tempBounds,
                smallFont.getColor(), Align.center);

            // Card number hint
            if (highlighted) {
                smallFont.setColor(0.77f, 0.67f, 0.47f, 0.7f);
            } else {
                smallFont.setColor(0.40f, 0.46f, 0.52f, 0.7f);
            }
            tempBounds.set(bounds.x + 10f, bounds.y + bounds.height - 14f, 30f, 14f);
            drawFittedText(batch, smallFont, String.format("[%d]", i + 1), tempBounds, smallFont.getColor());
        }

        // Keyboard hint
        smallFont.setColor(0.50f, 0.56f, 0.62f, 0.8f);
        TextLayoutHelper.drawFittedAtBaseline(
            batch, smallFont, "Use LEFT/RIGHT to select, ENTER to confirm", 430f, 140f, 500f,
            smallFont.getColor(), com.badlogic.gdx.utils.Align.center
        );

        batch.end();
    }

    private static class MenuSelection {
        private final int size;
        private int selectedIndex;

        MenuSelection(int size) {
            this.size = size;
            this.selectedIndex = 0;
        }

        void selectPrev() {
            selectedIndex = (selectedIndex - 1 + size) % size;
        }

        void selectNext() {
            selectedIndex = (selectedIndex + 1) % size;
        }

        void setSelectedIndex(int index) {
            selectedIndex = Math.max(0, Math.min(index, size - 1));
        }

        int getSelectedIndex() {
            return selectedIndex;
        }
    }
}
