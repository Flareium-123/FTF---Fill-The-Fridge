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
import com.badlogic.gdx.utils.Array;
import com.fillthefridge.FillTheFridgeGame;
import com.fillthefridge.assets.AssetManager;
import com.fillthefridge.powerups.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Intro screen shown before a run begins. Presents 3 random hero power-ups
 * for the player to choose from, similar to Slay the Spire's character selection.
 */
public class IntroScreen extends BaseScreen {
    private static final int CHOICE_COUNT = 3;

    private final Rectangle choice1Button = new Rectangle(60f, 170f, 360f, 430f);
    private final Rectangle choice2Button = new Rectangle(460f, 170f, 360f, 430f);
    private final Rectangle choice3Button = new Rectangle(860f, 170f, 360f, 430f);
    private final Rectangle[] choiceButtons = {choice1Button, choice2Button, choice3Button};

    private final List<PowerUp> choices = new ArrayList<>();
    private final MenuSelection menuSelection = new MenuSelection(CHOICE_COUNT);
    private boolean choiceMade = false;
    private float choiceFlashTimer = 0f;

    // Reusable objects to avoid per-frame GC pressure
    private final Color tempColor = new Color();
    private final Rectangle tempBounds = new Rectangle();

    public IntroScreen(FillTheFridgeGame game) {
        super(game);
        generateChoices();
    }

    private void generateChoices() {
        List<PowerUp> allHeroes = new ArrayList<>();
        allHeroes.add(new ColdStorageVeteranPowerUp());
        allHeroes.add(new FifoMasterPowerUp());
        allHeroes.add(new ProduceWhispererPowerUp());
        allHeroes.add(new SpaceOptimizerPowerUp());
        allHeroes.add(new TrapHunterPowerUp());
        allHeroes.add(new DeepFreezeSpecialistPowerUp());
        allHeroes.add(new ReinforcedStockPowerUp());
        allHeroes.add(new TimeBenderPowerUp());

        Random random = new Random(System.nanoTime());
        Collections.shuffle(allHeroes, random);

        for (int i = 0; i < CHOICE_COUNT && i < allHeroes.size(); i++) {
            choices.add(allHeroes.get(i));
        }
    }

    @Override
    public void render(float delta) {
        updateTransition(delta);

        Vector2 mouse = getMouseWorldCoords();
        menuSelection.updatePointer(mouse.x, mouse.y, findHoveredChoice(mouse));

        if (!choiceMade && Gdx.input.justTouched()) {
            for (int i = 0; i < choiceButtons.length; i++) {
                if (choiceButtons[i].contains(mouse.x, mouse.y)) {
                    selectChoice(i);
                    break;
                }
            }
        }

        if (!choiceMade) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                menuSelection.moveBy(-1);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                menuSelection.moveBy(1);
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                selectChoice(menuSelection.getSelectedIndex());
            }
        }

        if (choiceMade) {
            choiceFlashTimer += delta;
            if (choiceFlashTimer >= 0.5f) {
                transitionTo(new GameplayScreen(game));
            }
        }

        Gdx.gl.glClearColor(0.035f, 0.052f, 0.063f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        ShapeRenderer shapeRenderer = game.getShapeRenderer();
        SpriteBatch batch = game.getSpriteBatch();

        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        // Phase 1: All ShapeRenderer calls
        renderBackdrop(shapeRenderer);
        renderHeroSectionShapes(shapeRenderer);
        renderChoiceCardShapes(shapeRenderer, mouse.x, mouse.y);
        if (choiceMade) {
            renderChoiceFlash(shapeRenderer, choiceFlashTimer);
        }

        // Phase 2: All SpriteBatch calls
        renderHeroSectionText(batch);
        renderChoiceCardText(batch, mouse.x, mouse.y);

        renderTransitionOverlay(shapeRenderer);
    }

    private void selectChoice(int index) {
        if (index < 0 || index >= choices.size()) return;
        choiceMade = true;
        game.getAudioFeedback().playClick();
        PowerUp selected = choices.get(index);
        // Start the run with the hero power-up applied before stage 1 setup
        game.getGameController().startNewRun(selected);
    }

    private int findHoveredChoice(Vector2 mouse) {
        for (int i = 0; i < choiceButtons.length; i++) {
            if (choiceButtons[i].contains(mouse.x, mouse.y)) {
                return i;
            }
        }
        return -1;
    }

    private void renderBackdrop(ShapeRenderer shapes) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.035f, 0.052f, 0.063f, 1f);
        shapes.rect(0f, 0f, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        shapes.end();
    }

    private void renderHeroSectionShapes(ShapeRenderer shapes) {
        // Small decorative hero fridge icon — positioned between subtitle and cards
        // Cards top edge is at Y=600, subtitle baseline at Y=665
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        float heroX = 590f;
        float heroY = 610f;
        float heroW = 100f;
        float heroH = 40f;

        // Fridge body
        shapes.setColor(0.55f, 0.62f, 0.68f, 0.9f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, heroX, heroY, heroW, heroH, 6f);
        shapes.setColor(0.35f, 0.42f, 0.50f, 0.85f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, heroX + 3f, heroY + 3f, heroW - 6f, heroH - 6f, 4f);

        // Fridge door line
        shapes.setColor(0.70f, 0.78f, 0.84f, 0.7f);
        shapes.rect(heroX + heroW * 0.7f, heroY + 5f, 2f, heroH - 10f);

        // Handle
        shapes.setColor(0.80f, 0.86f, 0.92f, 0.9f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, heroX + heroW * 0.72f, heroY + 10f, 3f, 16f, 2f);

        // Snowflake accents
        shapes.setColor(0.60f, 0.85f, 1.0f, 0.5f);
        shapes.circle(heroX + 12f, heroY + 10f, 2.5f);
        shapes.circle(heroX + 28f, heroY + 28f, 1.5f);
        shapes.circle(heroX + 65f, heroY + 8f, 2f);

        shapes.end();
    }

    private void renderHeroSectionText(SpriteBatch batch) {
        batch.begin();
        BitmapFont titleFont = game.getAssetManager().getTitleFont();
        BitmapFont subtitleFont = game.getAssetManager().getSubtitleFont();

        // Title and subtitle at the top of the screen (height 720)
        // Title baseline at 700, subtitle baseline at 665
        // Hero icon sits between subtitle and cards at Y=618-668
        titleFont.setColor(0.92f, 0.84f, 0.64f, 1f);
        tempColor.set(0.92f, 0.84f, 0.64f, 1f);
        TextLayoutHelper.drawFittedAtBaseline(
            batch, titleFont, "CHOOSE YOUR HERO", 430f, 700f, 420f,
            tempColor, com.badlogic.gdx.utils.Align.center
        );

        subtitleFont.setColor(0.68f, 0.76f, 0.76f, 1f);
        tempColor.set(0.68f, 0.76f, 0.76f, 1f);
        TextLayoutHelper.drawFittedAtBaseline(
            batch, subtitleFont, "SELECT A STARTING BONUS FOR YOUR RUN", 400f, 665f, 480f,
            tempColor, com.badlogic.gdx.utils.Align.center
        );

        titleFont.setColor(Color.WHITE);
        subtitleFont.setColor(Color.WHITE);
        batch.end();
    }

    private void renderChoiceCardShapes(ShapeRenderer shapes, float mouseX, float mouseY) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        for (int i = 0; i < choices.size(); i++) {
            Rectangle bounds = choiceButtons[i];
            boolean highlighted = i == menuSelection.getSelectedIndex();
            boolean hovered = bounds.contains(mouseX, mouseY);

            // Card background
            shapes.begin(ShapeRenderer.ShapeType.Filled);

            if (highlighted) {
                shapes.setColor(0.72f, 0.58f, 0.33f, 0.15f);
                ProceduralFridgeRenderer.drawRoundedRect(shapes, bounds.x - 6f, bounds.y - 6f, bounds.width + 12f, bounds.height + 12f, 20f);
            }

            shapes.setColor(
                hovered ? 0.14f : 0.08f,
                hovered ? 0.20f : 0.12f,
                hovered ? 0.26f : 0.16f,
                0.95f
            );
            ProceduralFridgeRenderer.drawRoundedRect(shapes, bounds.x, bounds.y, bounds.width, bounds.height, 16f);

            // Card border
            shapes.setColor(
                highlighted ? 0.85f : 0.35f,
                highlighted ? 0.70f : 0.42f,
                highlighted ? 0.42f : 0.35f,
                highlighted ? 1f : 0.5f
            );
            shapes.rect(bounds.x, bounds.y, 3f, bounds.height);
            shapes.rect(bounds.x, bounds.y, bounds.width, 2f);
            shapes.rect(bounds.x, bounds.y + bounds.height - 2f, bounds.width, 2f);
            shapes.rect(bounds.x + bounds.width - 2f, bounds.y, 2f, bounds.height);

            // Icon area background — moved up to give room for text below
            float iconY = bounds.y + bounds.height - 140f;
            float iconSize = 80f;
            float iconX = bounds.x + (bounds.width - iconSize) * 0.5f;
            shapes.setColor(0.06f, 0.10f, 0.14f, 0.7f);
            ProceduralFridgeRenderer.drawRoundedRect(shapes, iconX - 10f, iconY - 10f, iconSize + 20f, iconSize + 20f, 12f);

            shapes.end();
        }
    }

    private void renderChoiceCardText(SpriteBatch batch, float mouseX, float mouseY) {
        AssetManager assets = game.getAssetManager();
        BitmapFont bodyFont = assets.getBodyFont();
        BitmapFont smallFont = assets.getSmallFont();

        batch.begin();

        for (int i = 0; i < choices.size(); i++) {
            PowerUp powerUp = choices.get(i);
            Rectangle bounds = choiceButtons[i];
            boolean highlighted = i == menuSelection.getSelectedIndex();

            // Icon — moved up to give room for text below
            float iconY = bounds.y + bounds.height - 140f;
            float iconSize = 80f;
            float iconX = bounds.x + (bounds.width - iconSize) * 0.5f;

            Texture iconTexture = assets.getTexture(powerUp.getIconTextureName());
            batch.setColor(Color.WHITE);
            batch.draw(iconTexture, iconX, iconY, iconSize, iconSize);
            batch.setColor(Color.WHITE);

            // Power-up name (below icon)
            float nameY = iconY - 50f;
            if (highlighted) {
                bodyFont.setColor(0.95f, 0.83f, 0.59f, 1f);
            } else {
                bodyFont.setColor(0.86f, 0.89f, 0.86f, 1f);
            }
            tempBounds.set(bounds.x + 16f, nameY, bounds.width - 32f, 36f);
            drawCenteredText(batch, bodyFont, powerUp.getName(), tempBounds, bodyFont.getColor());

            // Description (between card top and name)
            float descBottom = nameY - 8f;
            float descTop = bounds.y + 24f;
            float descHeight = descBottom - descTop;
            smallFont.setColor(0.70f, 0.76f, 0.82f, 1f);
            tempBounds.set(bounds.x + 16f, descTop, bounds.width - 32f, descHeight);
            drawWrappedText(batch, smallFont, powerUp.getDescription(), tempBounds, smallFont.getColor(), com.badlogic.gdx.utils.Align.center);

            // Rare indicator — positioned below the icon area
            if (powerUp.isRare()) {
                smallFont.setColor(0.92f, 0.72f, 0.35f, 0.8f);
                tempBounds.set(bounds.x + 20f, bounds.y + bounds.height - 48f, bounds.width - 40f, 20f);
                drawFittedText(batch, smallFont, "★ HERO BONUS ★", tempBounds, smallFont.getColor());
            }

            // Card number — positioned below the icon area
            if (highlighted) {
                smallFont.setColor(0.77f, 0.67f, 0.47f, 1f);
            } else {
                smallFont.setColor(0.50f, 0.56f, 0.62f, 1f);
            }
            tempBounds.set(bounds.x + 16f, bounds.y + bounds.height - 28f, 30f, 20f);
            drawFittedText(batch, smallFont, String.format("[%d]", i + 1), tempBounds, smallFont.getColor());
        }

        bodyFont.setColor(Color.WHITE);
        smallFont.setColor(Color.WHITE);
        batch.end();
    }

    private void renderChoiceFlash(ShapeRenderer shapes, float timer) {
        float alpha = Math.min(1f, timer * 2f) * 0.4f;
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.72f, 0.58f, 0.33f, alpha);
        shapes.rect(0f, 0f, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        shapes.end();
    }
}
