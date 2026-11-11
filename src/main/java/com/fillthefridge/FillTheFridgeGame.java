package com.fillthefridge;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.fillthefridge.assets.AssetManager;
import com.fillthefridge.logic.DifficultyScalingStrategy;
import com.fillthefridge.logic.GameController;
import com.fillthefridge.logic.ProgressiveDifficultyStrategy;
import com.fillthefridge.logic.SolvableStageGenerator;
import com.fillthefridge.model.GameState;
import com.fillthefridge.model.GamePhase;
import com.fillthefridge.ui.GameplayScreen;
import com.fillthefridge.ui.MainMenuScreen;
import com.fillthefridge.ui.RoguelikeMapScreen;
import com.fillthefridge.powerups.PowerUp;
import com.fillthefridge.settings.AudioFeedback;
import com.fillthefridge.settings.GameSettings;
import com.fillthefridge.util.StageGenerator;

import java.util.List;

public class FillTheFridgeGame extends Game {
    private static final long DEFAULT_SEED = 20261102L;

    private final boolean smokeTestMode;
    private int smokeTestFrames;

    private SpriteBatch spriteBatch;
    private ShapeRenderer shapeRenderer;
    private AssetManager assetManager;
    private GameState gameState;
    private GameController gameController;
    private GameSettings settings;
    private AudioFeedback audioFeedback;

    public FillTheFridgeGame() {
        this(false);
    }

    public FillTheFridgeGame(boolean smokeTestMode) {
        this.smokeTestMode = smokeTestMode;
    }

    @Override
    public void create() {
        this.spriteBatch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.settings = new GameSettings();
        this.assetManager = new AssetManager();
        this.assetManager.loadAll();
        this.assetManager.setHighQualityGraphics(settings.isHighQualityGraphics());
        this.audioFeedback = new AudioFeedback(settings.isAudioEnabled());

        this.gameState = new GameState();
        DifficultyScalingStrategy strategy = new ProgressiveDifficultyStrategy();
        StageGenerator generator = new SolvableStageGenerator();
        this.gameController = new GameController(gameState, strategy, generator, DEFAULT_SEED);

        if (smokeTestMode) {
            this.gameController.startNewRun();
            setScreen(new GameplayScreen(this));
        } else {
            setScreen(new MainMenuScreen(this));
        }
    }

    @Override
    public void render() {
        super.render();
        if (smokeTestMode) {
            smokeTestFrames++;
            if (smokeTestFrames == 5) {
                smokeTestProgression();
            }
            if (smokeTestFrames >= 12) {
                Gdx.app.exit();
            }
        }
    }

    private void smokeTestProgression() {
        for (int stage = 1; stage < ProgressiveDifficultyStrategy.FINAL_STAGE; stage++) {
            gameController.startStage(stage);
            gameState.setCurrentPhase(GamePhase.STAGE_CLEAR);
            gameController.update(2f);
            if (gameState.getCurrentPhase() != GamePhase.EDUCATIONAL_FEEDBACK) {
                throw new IllegalStateException("Stage " + stage + " did not reach educational feedback.");
            }
            gameController.continueAfterFeedback();
            if (gameState.getCurrentPhase() != GamePhase.MAP_SELECTION) {
                throw new IllegalStateException("Stage " + stage + " did not reach the route map.");
            }

            List<PowerUp> choices = gameController.getCurrentPowerUpChoices();
            if (choices.isEmpty() || choices.size() != gameController.getCurrentMapNodeIndexes().size()) {
                throw new IllegalStateException("Stage " + stage + " did not offer valid map rewards.");
            }
            gameController.choosePowerUp(choices.get(0));
            GamePhase expectedPhase = stage == ProgressiveDifficultyStrategy.FINAL_STAGE - 1
                    ? GamePhase.BOSS_INTRO
                    : GamePhase.PLAYING;
            if (gameState.getCurrentPhase() != expectedPhase) {
                throw new IllegalStateException("Map reward did not advance from stage " + stage + ".");
            }
        }

        if (gameController.getSelectedMapNodes().size() != ProgressiveDifficultyStrategy.FINAL_STAGE - 1) {
            throw new IllegalStateException("The selected route did not reach the final map node.");
        }
        gameController.startBossStage();
        if (gameState.getCurrentStageNumber() != ProgressiveDifficultyStrategy.FINAL_STAGE
                || gameController.getBossHealth() != GameController.BOSS_HEALTH) {
            throw new IllegalStateException("The route map did not start the boss stage correctly.");
        }

        gameController.startNewRun();
        gameState.setCurrentPhase(GamePhase.STAGE_CLEAR);
        gameController.update(2f);
        if (gameState.getCurrentPhase() != GamePhase.EDUCATIONAL_FEEDBACK) {
            throw new IllegalStateException("The map preview feedback could not be prepared for rendering.");
        }
        gameController.continueAfterFeedback();
        if (gameState.getCurrentPhase() != GamePhase.MAP_SELECTION) {
            throw new IllegalStateException("The map preview could not be prepared for rendering.");
        }
        setScreen(new RoguelikeMapScreen(this));
    }

    @Override
    public void dispose() {
        if (screen != null) {
            screen.dispose();
        }
        if (assetManager != null) {
            assetManager.dispose();
        }
        if (audioFeedback != null) {
            audioFeedback.dispose();
        }
        if (spriteBatch != null) {
            spriteBatch.dispose();
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }

    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public GameState getGameState() {
        return gameState;
    }

    public GameController getGameController() {
        return gameController;
    }

    public GameSettings getSettings() {
        return settings;
    }

    public AudioFeedback getAudioFeedback() {
        return audioFeedback;
    }
}
