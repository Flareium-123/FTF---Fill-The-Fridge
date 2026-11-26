package com.fillthefridge.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.fillthefridge.FillTheFridgeGame;

/**
 * Pause screen shown when the player presses ESC on the Roguelike map.
 * Offers Resume, Save & Quit to Menu, and Settings options.
 */
public class MapPauseScreen extends BaseScreen {
    private final Rectangle resumeButton = new Rectangle(440f, 400f, 400f, 56f);
    private final Rectangle saveQuitButton = new Rectangle(440f, 310f, 400f, 56f);
    private final Rectangle settingsButton = new Rectangle(440f, 220f, 400f, 56f);
    private final Rectangle[] menuButtons = {
        resumeButton, saveQuitButton, settingsButton
    };
    private static final String[] MENU_LABELS = {
        "RESUME", "SAVE & QUIT TO MENU", "SETTINGS"
    };
    private static final String[] MENU_HINTS = {
        "CONTINUE BROWSING THE MAP", "SAVE PROGRESS AND LEAVE", "AUDIO AND DISPLAY"
    };
    private final MenuSelection menuSelection = new MenuSelection(menuButtons.length);

    public MapPauseScreen(FillTheFridgeGame game) {
        super(game);
    }

    @Override
    public void render(float delta) {
        updateTransition(delta);

        Vector2 mouse = getMouseWorldCoords();
        menuSelection.updatePointer(mouse.x, mouse.y, findHoveredMenuItem(mouse));

        if (Gdx.input.justTouched()) {
            game.getAudioFeedback().playClick();
            if (resumeButton.contains(mouse.x, mouse.y)) {
                resumeMap();
            } else if (saveQuitButton.contains(mouse.x, mouse.y)) {
                saveAndQuit();
            } else if (settingsButton.contains(mouse.x, mouse.y)) {
                transitionTo(new SettingsScreen(game, () -> new MapPauseScreen(game)));
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            resumeMap();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.getAudioFeedback().playClick();
            activateKeyboardSelection();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            menuSelection.moveBy(-1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            menuSelection.moveBy(1);
        }

        Gdx.gl.glClearColor(0.035f, 0.052f, 0.063f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        ShapeRenderer shapeRenderer = game.getShapeRenderer();
        SpriteBatch batch = game.getSpriteBatch();

        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        // Phase 1: All ShapeRenderer calls
        renderPausedBackgroundShapes(shapeRenderer);
        renderPauseOverlayShapes(shapeRenderer, mouse.x, mouse.y);

        // Phase 2: All SpriteBatch calls
        renderPausedBackgroundBatch(batch);
        renderPauseOverlayText(batch, mouse.x, mouse.y);

        renderTransitionOverlay(shapeRenderer);
    }

    private void resumeMap() {
        // Return to the map screen by transitioning back
        transitionTo(new RoguelikeMapScreen(game));
    }

    private void saveAndQuit() {
        // Attempt to save progress, then return to main menu
        game.getGameController().saveProgress();
        transitionTo(new MainMenuScreen(game));
    }

    private int findHoveredMenuItem(Vector2 mouse) {
        for (int i = 0; i < menuButtons.length; i++) {
            if (menuButtons[i].contains(mouse.x, mouse.y)) {
                return i;
            }
        }
        return -1;
    }

    private void activateKeyboardSelection() {
        switch (menuSelection.getSelectedIndex()) {
            case 0 -> resumeMap();
            case 1 -> saveAndQuit();
            case 2 -> transitionTo(new SettingsScreen(game, () -> new MapPauseScreen(game)));
            default -> throw new IllegalStateException("Unknown pause menu selection.");
        }
    }

    private void renderPausedBackgroundShapes(ShapeRenderer shapeRenderer) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.05f, 0.08f, 0.12f, 0.65f);
        shapeRenderer.rect(0f, 0f, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        shapeRenderer.end();
    }

    private void renderPausedBackgroundBatch(SpriteBatch batch) {
        batch.begin();
        batch.setColor(0.82f, 0.90f, 1f, 1f);
        batch.draw(game.getAssetManager().getTexture(com.fillthefridge.assets.AssetManager.FRIDGE_PHOTO), 0f, 0f, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        batch.setColor(Color.WHITE);
        batch.end();
    }

    private void renderPauseOverlayShapes(ShapeRenderer shapeRenderer, float mouseX, float mouseY) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Panel background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.06f, 0.10f, 0.15f, 0.92f);
        ProceduralFridgeRenderer.drawRoundedRect(shapeRenderer, 380f, 170f, 520f, 340f, 18f);
        shapeRenderer.setColor(0.35f, 0.55f, 0.62f, 0.6f);
        shapeRenderer.rect(390f, 170f, 2f, 340f);
        shapeRenderer.rect(888f, 170f, 2f, 340f);
        shapeRenderer.rect(390f, 508f, 500f, 2f);
        shapeRenderer.rect(390f, 170f, 500f, 2f);
        shapeRenderer.end();

        // Button shapes
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < menuButtons.length; i++) {
            Rectangle bounds = menuButtons[i];
            boolean highlighted = i == menuSelection.getSelectedIndex();
            boolean hovered = bounds.contains(mouseX, mouseY);

            if (highlighted) {
                shapeRenderer.setColor(0.72f, 0.58f, 0.33f, 0.12f);
                shapeRenderer.rect(bounds.x - 4f, bounds.y - 3f, bounds.width + 8f, bounds.height + 6f);
            }

            shapeRenderer.setColor(
                hovered ? 0.18f : 0.12f,
                hovered ? 0.26f : 0.17f,
                hovered ? 0.32f : 0.20f,
                0.94f
            );
            shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);

            shapeRenderer.setColor(
                highlighted ? 0.75f : 0.35f,
                highlighted ? 0.62f : 0.40f,
                highlighted ? 0.38f : 0.32f,
                highlighted ? 1f : 0.44f
            );
            shapeRenderer.rect(bounds.x, bounds.y, highlighted ? 3f : 1f, bounds.height);
        }
        shapeRenderer.end();
    }

    private void renderPauseOverlayText(SpriteBatch batch, float mouseX, float mouseY) {
        batch.begin();
        BitmapFont titleFont = game.getAssetManager().getTitleFont();
        BitmapFont bodyFont = game.getAssetManager().getBodyFont();
        BitmapFont smallFont = game.getAssetManager().getSmallFont();

        titleFont.setColor(0.92f, 0.84f, 0.64f, 1f);
        titleFont.draw(batch, "PAUSED", 560f, 490f);

        // Menu button text
        for (int i = 0; i < menuButtons.length; i++) {
            Rectangle bounds = menuButtons[i];
            boolean highlighted = i == menuSelection.getSelectedIndex();

            smallFont.setColor(highlighted ? new Color(0.77f, 0.67f, 0.47f, 1f) : new Color(0.62f, 0.68f, 0.67f, 1f));
            smallFont.draw(batch, String.format("%02d", i + 1), bounds.x + 16f, bounds.y + 36f);

            bodyFont.setColor(highlighted ? new Color(0.95f, 0.83f, 0.59f, 1f) : new Color(0.86f, 0.89f, 0.86f, 1f));
            bodyFont.draw(batch, MENU_LABELS[i], bounds.x + 64f, bounds.y + 38f);

            smallFont.setColor(new Color(0.62f, 0.68f, 0.67f, 1f));
            smallFont.draw(batch, MENU_HINTS[i], bounds.x + 64f, bounds.y + 20f);
        }

        smallFont.setColor(new Color(0.55f, 0.62f, 0.68f, 1f));
        smallFont.draw(batch, "Press Esc to resume", 530f, 195f);

        titleFont.setColor(Color.WHITE);
        bodyFont.setColor(Color.WHITE);
        smallFont.setColor(Color.WHITE);
        batch.end();
    }
}
