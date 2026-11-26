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
import com.fillthefridge.FillTheFridgeGame;
import com.fillthefridge.assets.AssetManager;

public class MainMenuScreen extends BaseScreen {
    private final Rectangle playButton = new Rectangle(104f, 463f, 396f, 48f);
    private final Rectangle continueButton = new Rectangle(104f, 407f, 396f, 48f);
    private final Rectangle tutorialButton = new Rectangle(104f, 351f, 396f, 48f);
    private final Rectangle settingsButton = new Rectangle(104f, 295f, 396f, 48f);
    private final Rectangle creditsButton = new Rectangle(104f, 239f, 396f, 48f);
    private final Rectangle exitButton = new Rectangle(104f, 183f, 396f, 48f);
    private final Rectangle[] menuButtons = {
        playButton, continueButton, tutorialButton, settingsButton, creditsButton, exitButton
    };
    private static final String[] MENU_LABELS = {
        "NEW RUN", "CONTINUE", "TUTORIAL", "SETTINGS", "CREDITS", "EXIT"
    };
    private static final String[] MENU_HINTS = {
        "BEGIN A NEW EXPEDITION", "RESUME YOUR LAST SHIFT", "LEARN SAFE STORAGE",
        "AUDIO AND DISPLAY", "FIELD NOTES", "LEAVE THE FACILITY"
    };
    private final MenuSelection menuSelection = new MenuSelection(menuButtons.length);

    public MainMenuScreen(FillTheFridgeGame game) {
        super(game);
    }

    @Override
    public void render(float delta) {
        updateTransition(delta);

        Vector2 mouse = getMouseWorldCoords();
        menuSelection.updatePointer(mouse.x, mouse.y, findHoveredMenuItem(mouse));
        boolean hasSavedProgress = game.getGameController().hasSavedProgress();
        if (Gdx.input.justTouched()) {
            game.getAudioFeedback().playClick();
            if (settingsButton.contains(mouse.x, mouse.y)) {
                transitionTo(new SettingsScreen(game));
            } else if (playButton.contains(mouse.x, mouse.y)) {
                transitionTo(new IntroScreen(game));
            } else if (continueButton.contains(mouse.x, mouse.y)) {
                if (hasSavedProgress) {
                    continueSavedRun();
                }
            } else if (creditsButton.contains(mouse.x, mouse.y)) {
                transitionTo(new CreditsScreen(game));
            } else if (tutorialButton.contains(mouse.x, mouse.y)) {
                game.getGameController().startTutorialRun();
                transitionTo(new GameplayScreen(game, true));
            } else if (exitButton.contains(mouse.x, mouse.y)) {
                Gdx.app.exit();
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            menuSelection.moveBy(-1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            menuSelection.moveBy(1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            activateKeyboardSelection(hasSavedProgress);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            transitionTo(new IntroScreen(game));
        } else if (hasSavedProgress && Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            continueSavedRun();
        }

        Gdx.gl.glClearColor(0.055f, 0.075f, 0.10f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        ShapeRenderer shapeRenderer = game.getShapeRenderer();
        SpriteBatch batch = game.getSpriteBatch();
        AssetManager assets = game.getAssetManager();

        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        renderBackdrop(shapeRenderer);
        renderFridgePhoto(shapeRenderer, batch, assets);
        renderMenuPanel(shapeRenderer, hasSavedProgress, menuSelection.getSelectedIndex());
        renderMenuText(batch, assets, hasSavedProgress, menuSelection.getSelectedIndex());

        renderTransitionOverlay(shapeRenderer);
    }

    private void continueSavedRun() {
        if (!game.getGameController().resumeSavedProgress()) {
            return;
        }
        switch (game.getGameState().getCurrentPhase()) {
            case MAP_SELECTION -> transitionTo(new RoguelikeMapScreen(game));
            case PLAYING -> transitionTo(new GameplayScreen(game));
            case PRE_BOSS_EVENT -> transitionTo(new PreBossEventScreen(game));
            case BOSS_INTRO -> transitionTo(new BossIntroScreen(game));
            case RUN_COMPLETE -> transitionTo(new RunCompleteScreen(game));
            default -> throw new IllegalStateException("Saved run restored to an unexpected phase.");
        }
    }

    private int findHoveredMenuItem(Vector2 mouse) {
        for (int i = 0; i < menuButtons.length; i++) {
            if (menuButtons[i].contains(mouse.x, mouse.y)) {
                return i;
            }
        }
        return -1;
    }

    private void renderBackdrop(ShapeRenderer shapes) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.rect(0f, 0f, VIRTUAL_WIDTH, VIRTUAL_HEIGHT,
            new Color(0.035f, 0.052f, 0.063f, 1f),
            new Color(0.035f, 0.052f, 0.063f, 1f),
            new Color(0.12f, 0.15f, 0.16f, 1f),
            new Color(0.12f, 0.15f, 0.16f, 1f));
        shapes.setColor(0.42f, 0.60f, 0.64f, 0.035f);
        for (int stripe = 0; stripe < 14; stripe++) {
            float x = 570f + stripe * 52f;
            shapes.rect(x, 0f, 1f + stripe % 3, VIRTUAL_HEIGHT);
        }
        shapes.setColor(0.015f, 0.024f, 0.028f, 0.48f);
        shapes.rect(0f, 0f, VIRTUAL_WIDTH, 86f);
        shapes.setColor(0.68f, 0.72f, 0.69f, 0.10f);
        shapes.rect(0f, 86f, VIRTUAL_WIDTH, 1f);
        shapes.end();
    }

    private void renderFridgePhoto(ShapeRenderer shapes, SpriteBatch batch, AssetManager assets) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.015f, 0.023f, 0.027f, 0.72f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, 658f, 135f, 568f, 370f, 8f);
        shapes.setColor(0.25f, 0.32f, 0.33f, 1f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, 670f, 147f, 544f, 346f, 6f);
        shapes.setColor(0.62f, 0.53f, 0.35f, 1f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, 677f, 154f, 530f, 332f, 4f);
        shapes.setColor(0.035f, 0.052f, 0.063f, 1f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, 682f, 159f, 520f, 322f, 3f);
        shapes.end();

        Texture fridgePhoto = assets.getTexture(AssetManager.FRIDGE_PHOTO);
        float photoWidth = 504f;
        float photoHeight = photoWidth * fridgePhoto.getHeight() / fridgePhoto.getWidth();
        float photoX = 690f;
        float photoY = 320f - photoHeight * 0.5f;
        batch.begin();
        batch.setColor(Color.WHITE);
        batch.draw(fridgePhoto, photoX, photoY, photoWidth, photoHeight);
        batch.setColor(Color.WHITE);
        batch.end();
    }

    private void renderMenuPanel(ShapeRenderer shapes, boolean hasSavedProgress, int activeMenuItem) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.015f, 0.025f, 0.028f, 0.70f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, 76f, 166f, 456f, 400f, 5f);
        shapes.setColor(0.54f, 0.45f, 0.30f, 0.46f);
        shapes.rect(84f, 166f, 1.5f, 400f);
        shapes.rect(522f, 166f, 1.5f, 400f);
        shapes.rect(84f, 564f, 438f, 1.5f);
        shapes.rect(84f, 166f, 438f, 1.5f);

        for (int i = 0; i < menuButtons.length; i++) {
            Rectangle bounds = menuButtons[i];
            boolean highlighted = i == activeMenuItem;
            boolean disabled = i == 1 && !hasSavedProgress;
            if (highlighted) {
                shapes.setColor(0.72f, 0.58f, 0.33f, 0.16f);
                shapes.rect(bounds.x - 5f, bounds.y - 4f, bounds.width + 10f, bounds.height + 8f);
            }
            shapes.setColor(disabled ? 0.075f : 0.105f, disabled ? 0.09f : 0.13f,
                disabled ? 0.095f : 0.13f, 0.96f);
            shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);
            shapes.setColor(highlighted ? 0.75f : 0.35f, highlighted ? 0.62f : 0.40f,
                highlighted ? 0.38f : 0.32f, highlighted ? 1f : 0.44f);
            shapes.rect(bounds.x, bounds.y, highlighted ? 3f : 1f, bounds.height);
            if (highlighted) {
                shapes.rect(bounds.x, bounds.y + bounds.height - 1f, bounds.width, 1f);
                shapes.rect(bounds.x, bounds.y, bounds.width, 1f);
                shapes.rect(bounds.x + bounds.width - 1f, bounds.y, 1f, bounds.height);
            }
        }
        shapes.end();
    }

    private void renderMenuText(
        SpriteBatch batch,
        AssetManager assets,
        boolean hasSavedProgress,
        int activeMenuItem
    ) {
        batch.begin();
        BitmapFont titleFont = assets.getTitleFont();
        BitmapFont subtitleFont = assets.getSubtitleFont();
        BitmapFont bodyFont = assets.getBodyFont();
        BitmapFont smallFont = assets.getSmallFont();

        titleFont.setColor(0.91f, 0.82f, 0.63f, 1f);
        titleFont.draw(batch, "FILL THE FRIDGE", 104f, 660f);
        subtitleFont.setColor(0.68f, 0.76f, 0.76f, 1f);
        subtitleFont.draw(batch, "A COLD STORAGE ROGUELIKE", 108f, 621f);
        smallFont.setColor(0.72f, 0.66f, 0.52f, 1f);
        smallFont.draw(batch, "SELECT AN OPERATION", 106f, 552f);

        for (int i = 0; i < menuButtons.length; i++) {
            Rectangle bounds = menuButtons[i];
            boolean disabled = i == 1 && !hasSavedProgress;
            Color labelColor = disabled
                ? new Color(0.48f, 0.52f, 0.52f, 1f)
                : i == activeMenuItem ? new Color(0.95f, 0.83f, 0.59f, 1f)
                : new Color(0.86f, 0.89f, 0.86f, 1f);
            smallFont.setColor(disabled
                ? new Color(0.46f, 0.49f, 0.49f, 1f)
                : i == activeMenuItem ? new Color(0.77f, 0.67f, 0.47f, 1f)
                : new Color(0.62f, 0.68f, 0.67f, 1f));
            smallFont.draw(batch, String.format("%02d", i + 1), bounds.x + 14f, bounds.y + 29f);
            bodyFont.setColor(labelColor);
            bodyFont.draw(batch, MENU_LABELS[i], bounds.x + 58f, bounds.y + 30f);
            smallFont.setColor(disabled ? new Color(0.40f, 0.44f, 0.44f, 1f) : new Color(0.62f, 0.68f, 0.67f, 1f));
            String hint = disabled ? "NO SAVED RUN" : MENU_HINTS[i];
            smallFont.draw(batch, hint, bounds.x + 58f, bounds.y + 13f);
        }

        batch.end();
    }

    private void activateKeyboardSelection(boolean hasSavedProgress) {
        switch (menuSelection.getSelectedIndex()) {
            case 0 -> {
                transitionTo(new IntroScreen(game));
            }
            case 1 -> {
                if (hasSavedProgress) {
                    continueSavedRun();
                }
            }
            case 2 -> {
                game.getGameController().startTutorialRun();
                transitionTo(new GameplayScreen(game, true));
            }
            case 3 -> transitionTo(new SettingsScreen(game));
            case 4 -> transitionTo(new CreditsScreen(game));
            case 5 -> Gdx.app.exit();
            default -> throw new IllegalStateException("Unknown main menu selection.");
        }
    }
}
