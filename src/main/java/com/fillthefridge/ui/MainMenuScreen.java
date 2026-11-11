package com.fillthefridge.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.fillthefridge.FillTheFridgeGame;
import com.fillthefridge.assets.AssetManager;

public class MainMenuScreen extends BaseScreen {
    private final Rectangle playButton = new Rectangle(360f, 282f, 270f, 54f);
    private final Rectangle continueButton = new Rectangle(650f, 282f, 270f, 54f);
    private final Rectangle creditsButton = new Rectangle(360f, 212f, 270f, 54f);
    private final Rectangle tutorialButton = new Rectangle(650f, 212f, 270f, 54f);
    private final Rectangle exitButton = new Rectangle(505f, 142f, 270f, 54f);
    private final Rectangle settingsButton = new Rectangle(1170f, 638f, 72f, 72f);
    private int keyboardSelection;
    private float menuTimer;

    public MainMenuScreen(FillTheFridgeGame game) {
        super(game);
    }

    @Override
    public void render(float delta) {
        menuTimer += delta;
        updateTransition(delta);

        Vector2 mouse = getMouseWorldCoords();
        boolean hasSavedProgress = game.getGameController().hasSavedProgress();
        if (Gdx.input.justTouched()) {
            game.getAudioFeedback().playClick();
            if (settingsButton.contains(mouse.x, mouse.y)) {
                transitionTo(new SettingsScreen(game));
            } else if (playButton.contains(mouse.x, mouse.y)) {
                game.getGameController().startNewRun();
                transitionTo(new GameplayScreen(game));
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
            keyboardSelection = Math.floorMod(keyboardSelection - 1, 6);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            keyboardSelection = Math.floorMod(keyboardSelection + 1, 6);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            keyboardSelection = switch (keyboardSelection) {
                case 1 -> 0;
                case 3 -> 2;
                default -> keyboardSelection;
            };
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            keyboardSelection = switch (keyboardSelection) {
                case 0 -> 1;
                case 2 -> 3;
                default -> keyboardSelection;
            };
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            activateKeyboardSelection(hasSavedProgress);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.getGameController().startNewRun();
            transitionTo(new GameplayScreen(game));
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

        batch.begin();
        batch.setColor(0.82f, 0.90f, 1f, 1f);
        batch.draw(assets.getTexture(AssetManager.FRIDGE_PHOTO), 0f, 0f, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        batch.setColor(Color.WHITE);
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.31f, 0.27f, 0.20f, 1f);
        ProceduralFridgeRenderer.drawRoundedRect(shapeRenderer, 310f, 30f, 660f, 610f, 14f);
        shapeRenderer.setColor(0.13f, 0.15f, 0.14f, 0.98f);
        ProceduralFridgeRenderer.drawRoundedRect(shapeRenderer, 317f, 37f, 646f, 596f, 10f);

        drawStyledButtonShape(
            shapeRenderer,
            playButton,
            new Color(0.43f, 0.56f, 0.34f, 1f),
            new Color(0.56f, 0.69f, 0.42f, 1f),
            playButton.contains(mouse.x, mouse.y) || keyboardSelection == 0
        );
        drawStyledButtonShape(
            shapeRenderer,
            continueButton,
            hasSavedProgress ? new Color(0.20f, 0.62f, 0.42f, 1f) : new Color(0.30f, 0.32f, 0.33f, 1f),
            hasSavedProgress ? new Color(0.28f, 0.74f, 0.52f, 1f) : new Color(0.30f, 0.32f, 0.33f, 1f),
            hasSavedProgress && (continueButton.contains(mouse.x, mouse.y) || keyboardSelection == 1)
        );
        drawStyledButtonShape(
            shapeRenderer,
            creditsButton,
            new Color(0.36f, 0.43f, 0.43f, 1f),
            new Color(0.47f, 0.55f, 0.54f, 1f),
            creditsButton.contains(mouse.x, mouse.y) || keyboardSelection == 2
        );
        drawStyledButtonShape(
            shapeRenderer,
            tutorialButton,
            new Color(0.53f, 0.43f, 0.31f, 1f),
            new Color(0.66f, 0.54f, 0.38f, 1f),
            tutorialButton.contains(mouse.x, mouse.y) || keyboardSelection == 3
        );
        drawStyledButtonShape(
            shapeRenderer,
            exitButton,
            new Color(0.55f, 0.29f, 0.25f, 1f),
            new Color(0.70f, 0.36f, 0.29f, 1f),
            exitButton.contains(mouse.x, mouse.y) || keyboardSelection == 4
        );
        drawSettingsGear(shapeRenderer, settingsButton, mouse);
        shapeRenderer.end();

        batch.begin();
        BitmapFont titleFont = assets.getTitleFont();
        BitmapFont subtitleFont = assets.getSubtitleFont();
        BitmapFont bodyFont = assets.getBodyFont();
        BitmapFont smallFont = assets.getSmallFont();

        String title = "FILL THE FRIDGE";
        GlyphLayout titleLayout = new GlyphLayout(titleFont, title);
        titleFont.setColor(0.95f, 0.82f, 0.57f, 1f);
        titleFont.draw(batch, title, (VIRTUAL_WIDTH - titleLayout.width) * 0.5f, 585f);

        String subtitle = "Roguelike Grocery Sorting & Shelf-Stacking Puzzle";
        GlyphLayout subLayout = new GlyphLayout(subtitleFont, subtitle);
        subtitleFont.setColor(0.78f, 0.82f, 0.79f, 1f);
        subtitleFont.draw(batch, subtitle, (VIRTUAL_WIDTH - subLayout.width) * 0.5f, 528f);

        String[] showcaseItems = new String[]{
            AssetManager.MILK,
            AssetManager.WHOLE_CHICKEN,
            AssetManager.PIZZA_BOX,
            AssetManager.CHEESE,
            AssetManager.BROCCOLI,
            AssetManager.JUICE_BOTTLE
        };
        for (int i = 0; i < showcaseItems.length; i++) {
            Texture tex = assets.getTexture(showcaseItems[i]);
            float bob = MathUtils.sin(menuTimer * 3.0f + i * 0.9f) * 8f;
            float iconX = 375f + i * 92f;
            float iconY = 405f + bob;
            batch.draw(tex, iconX, iconY, 64f, 64f);
        }

        drawCenteredText(batch, subtitleFont, "PLAY GAME", playButton, Color.WHITE);
        drawCenteredText(
            batch,
            bodyFont,
            "CONTINUE SAVED RUN",
            continueButton,
            hasSavedProgress ? Color.WHITE : new Color(0.62f, 0.64f, 0.65f, 1f)
        );
        drawCenteredText(batch, bodyFont, "CREDITS", creditsButton, Color.WHITE);
        drawCenteredText(batch, bodyFont, "TUTORIAL & SAFE STORAGE", tutorialButton, Color.WHITE);
        drawCenteredText(batch, bodyFont, "EXIT DESKTOP", exitButton, Color.WHITE);
        smallFont.setColor(Color.WHITE);
        batch.end();

        renderTransitionOverlay(shapeRenderer);
    }

    private void continueSavedRun() {
        if (!game.getGameController().resumeSavedProgress()) {
            return;
        }
        switch (game.getGameState().getCurrentPhase()) {
            case MAP_SELECTION -> transitionTo(new RoguelikeMapScreen(game));
            case PLAYING -> transitionTo(new GameplayScreen(game));
            case BOSS_INTRO -> transitionTo(new BossIntroScreen(game));
            case RUN_COMPLETE -> transitionTo(new RunCompleteScreen(game));
            default -> throw new IllegalStateException("Saved run restored to an unexpected phase.");
        }
    }

    private void drawSettingsGear(ShapeRenderer shapes, Rectangle bounds, Vector2 mouse) {
        float centerX = bounds.x + bounds.width * 0.5f;
        float centerY = bounds.y + bounds.height * 0.5f;
        boolean highlighted = bounds.contains(mouse.x, mouse.y) || keyboardSelection == 5;

        shapes.setColor(
            highlighted ? 0.47f : 0.31f,
            highlighted ? 0.58f : 0.42f,
            highlighted ? 0.48f : 0.34f,
            0.96f
        );
        ProceduralFridgeRenderer.drawRoundedRect(shapes, bounds.x, bounds.y, bounds.width, bounds.height, 16f);
        shapes.setColor(0.91f, 0.88f, 0.76f, 1f);
        for (int tooth = 0; tooth < 8; tooth++) {
            float angle = tooth * MathUtils.PI / 4f;
            shapes.circle(
                centerX + MathUtils.cos(angle) * 18f,
                centerY + MathUtils.sin(angle) * 18f,
                6f
            );
        }
        shapes.circle(centerX, centerY, 16f);
        shapes.setColor(0.20f, 0.28f, 0.25f, 1f);
        shapes.circle(centerX, centerY, 7f);
    }

    private void activateKeyboardSelection(boolean hasSavedProgress) {
        switch (keyboardSelection) {
            case 0 -> {
                game.getGameController().startNewRun();
                transitionTo(new GameplayScreen(game));
            }
            case 1 -> {
                if (hasSavedProgress) {
                    continueSavedRun();
                }
            }
            case 2 -> transitionTo(new CreditsScreen(game));
            case 3 -> {
                game.getGameController().startTutorialRun();
                transitionTo(new GameplayScreen(game, true));
            }
            case 4 -> Gdx.app.exit();
            case 5 -> transitionTo(new SettingsScreen(game));
            default -> throw new IllegalStateException("Unknown main menu selection.");
        }
    }
}
