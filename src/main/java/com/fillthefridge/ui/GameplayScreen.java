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
import com.badlogic.gdx.utils.Align;
import com.fillthefridge.FillTheFridgeGame;
import com.fillthefridge.assets.AssetManager;
import com.fillthefridge.items.Item;
import com.fillthefridge.logic.GameController;
import com.fillthefridge.model.GamePhase;
import com.fillthefridge.model.GameState;
import com.fillthefridge.util.GameEventListener;
import com.fillthefridge.util.GridPosition;
import java.util.List;

public class GameplayScreen extends BaseScreen implements GameEventListener {
    private static final Rectangle TUTORIAL_CONTINUE_BUTTON = new Rectangle(470f, 230f, 340f, 58f);
    private final ProceduralFridgeRenderer fridgeRenderer;
    private final HudRenderer hudRenderer;
    private final boolean tutorialMode;
    private GridPosition currentHoverGridPosition;
    private GridPosition keyboardGridPosition;
    private int keyboardTrayIndex;
    private boolean keyboardGridMode;
    private int tutorialStep;

    public GameplayScreen(FillTheFridgeGame game) {
        this(game, false);
    }

    public GameplayScreen(FillTheFridgeGame game, boolean tutorialMode) {
        super(game);
        this.fridgeRenderer = new ProceduralFridgeRenderer();
        this.hudRenderer = new HudRenderer();
        this.tutorialMode = tutorialMode;
    }

    @Override
    public void show() {
        super.show();
        game.getGameState().addEventListener(this);
        fridgeRenderer.updateLayout(game.getGameState().getFridge(), VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        hudRenderer.updateTrayAndTrashBounds(game.getGameState());
    }

    @Override
    public void hide() {
        super.hide();
        game.getGameState().removeEventListener(this);
    }

    @Override
    public void render(float delta) {
        updateTransition(delta);

        GameController controller = game.getGameController();
        GameState state = game.getGameState();
        boolean tutorialPaused = tutorialMode && tutorialStep % 2 == 0;
        float gameplayDelta = tutorialPaused ? 0f : delta;

        fridgeRenderer.updateLayout(state.getFridge(), VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        fridgeRenderer.update(gameplayDelta);
        hudRenderer.update(gameplayDelta, state);

        Vector2 mouse = getMouseWorldCoords();
        if (tutorialPaused) {
            handleTutorialBriefingInput(mouse.x, mouse.y);
        } else {
            updateHoverStates(state, mouse.x, mouse.y);
            handleUserInput(controller, state, mouse.x, mouse.y);
        }

        controller.update(gameplayDelta);
        if (tutorialMode && !tutorialPaused) {
            updateTutorialProgress(state);
        }

        if (state.getCurrentPhase() == GamePhase.MAP_SELECTION) {
            transitionTo(new RoguelikeMapScreen(game));
        } else if (state.getCurrentPhase() == GamePhase.POWERUP_SELECTION) {
            transitionTo(new PowerUpSelectionScreen(game));
        } else if (state.getCurrentPhase() == GamePhase.EDUCATIONAL_FEEDBACK) {
            transitionTo(new StageFeedbackScreen(game));
        } else if (state.getCurrentPhase() == GamePhase.GAME_OVER) {
            transitionTo(new GameOverScreen(game));
        } else if (state.getCurrentPhase() == GamePhase.RUN_COMPLETE) {
            transitionTo(new RunCompleteScreen(game));
        }

        Gdx.gl.glClearColor(0.45f, 0.78f, 0.96f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        ShapeRenderer shapeRenderer = game.getShapeRenderer();
        SpriteBatch batch = game.getSpriteBatch();

        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.setColor(0.82f, 0.90f, 1f, 1f);
        batch.draw(game.getAssetManager().getTexture(AssetManager.FRIDGE_PHOTO), 0f, 0f, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        batch.setColor(Color.WHITE);
        batch.end();
        fridgeRenderer.renderFridgeStructure(
            shapeRenderer,
            batch,
            game.getAssetManager().getSmallFont(),
            game.getAssetManager(),
            state.getFridge(),
            state.getTray().getSelectedItem(),
            currentHoverGridPosition,
            state.getActivePowerUps()
        );

        hudRenderer.renderHudAndTray(
            shapeRenderer,
            batch,
            game.getAssetManager(),
            state,
            currentHoverGridPosition,
            mouse.x,
            mouse.y,
            controller.getUndoCountUntilLifeLoss(),
            Input.Keys.toString(game.getSettings().getUndoKeyCode()),
            controller.getCurrentStageContract().getObjective()
        );
        if (state.getCurrentStageNumber() == com.fillthefridge.logic.ProgressiveDifficultyStrategy.FINAL_STAGE) {
            hudRenderer.renderBossHealth(
                shapeRenderer,
                batch,
                game.getAssetManager(),
                controller.getBossHealth()
            );
        }

        if (state.getCurrentPhase() == GamePhase.STAGE_CLEAR) {
            hudRenderer.renderStageClearBanner(
                shapeRenderer,
                batch,
                game.getAssetManager(),
                state.getCurrentStageNumber(),
                controller.getStageClearBannerTimer()
            );
        }

        if (tutorialMode && isTutorialPaused()) {
            renderTutorialBriefing(shapeRenderer, batch, mouse.x, mouse.y);
        }
        renderTransitionOverlay(shapeRenderer);
    }

    private void updateHoverStates(GameState state, float mouseX, float mouseY) {
        Item selectedItem = state.getTray().getSelectedItem();
        if (keyboardGridMode && selectedItem != null) {
            keyboardGridPosition = clampKeyboardGridPosition(state, keyboardGridPosition);
            currentHoverGridPosition = keyboardGridPosition;
        } else {
            currentHoverGridPosition = fridgeRenderer.screenToGridPosition(state.getFridge(), mouseX, mouseY);
        }
        state.getTrashBin().setHovered(state.getTrashBin().containsPoint(mouseX, mouseY));

        List<Item> activeItems = state.getTray().getActiveItems();
        List<Rectangle> slotRects = hudRenderer.getTraySlotBounds();
        for (int i = 0; i < activeItems.size(); i++) {
            Item item = activeItems.get(i);
            boolean hovered = (i < slotRects.size() && slotRects.get(i).contains(mouseX, mouseY))
                || (selectedItem == null && i == keyboardTrayIndex);
            item.setHovered(hovered);
        }
    }

    private void handleUserInput(GameController controller, GameState state, float mouseX, float mouseY) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            transitionTo(new MainMenuScreen(game));
            return;
        }
        if (state.getCurrentPhase() != GamePhase.PLAYING) {
            return;
        }

        handleKeyboardNavigation(controller, state);

        if (Gdx.input.isKeyJustPressed(Input.Keys.V)) {
            controller.confirmPlacements();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            controller.activateManualDeepFreeze();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            controller.activateFifoCaddy();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            controller.rotateSelectedItem();
        }
        if (Gdx.input.isKeyJustPressed(game.getSettings().getUndoKeyCode())) {
            controller.undoLastPlacement();
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            if (tutorialMode) {
                game.getAudioFeedback().playClick();
            }
            controller.undoLastPlacement();
            return;
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (tutorialMode) {
                game.getAudioFeedback().playClick();
            }
            if (hudRenderer.getMenuButtonBounds().contains(mouseX, mouseY)) {
                transitionTo(new MainMenuScreen(game));
                return;
            }
            if (hudRenderer.getConfirmButtonBounds().contains(mouseX, mouseY)) {
                controller.confirmPlacements();
                return;
            }
            if (hudRenderer.getUndoButtonBounds().contains(mouseX, mouseY)) {
                controller.undoLastPlacement();
                return;
            }
            if (hudRenderer.getFreezeButtonBounds().contains(mouseX, mouseY)) {
                controller.activateManualDeepFreeze();
                return;
            }
            if (state.getTrashBin().containsPoint(mouseX, mouseY)) {
                controller.trashSelectedItem();
                return;
            }

            List<Item> activeItems = state.getTray().getActiveItems();
            List<Rectangle> slotRects = hudRenderer.getTraySlotBounds();
            for (int i = 0; i < activeItems.size() && i < slotRects.size(); i++) {
                if (slotRects.get(i).contains(mouseX, mouseY)) {
                    keyboardTrayIndex = i;
                    keyboardGridMode = false;
                    keyboardGridPosition = clampKeyboardGridPosition(state, currentHoverGridPosition);
                    controller.selectTrayItem(activeItems.get(i));
                    return;
                }
            }
            GridPosition mouseGridPosition = fridgeRenderer.screenToGridPosition(state.getFridge(), mouseX, mouseY);
            if (mouseGridPosition != null) {
                keyboardGridMode = false;
                controller.handleFridgeLeftClick(mouseGridPosition);
            }
        }
    }

    private boolean handleKeyboardNavigation(GameController controller, GameState state) {
        List<Item> activeItems = state.getTray().getActiveItems();
        Item selectedItem = state.getTray().getSelectedItem();
        if (selectedItem == null) {
            if (activeItems.isEmpty()) {
                keyboardTrayIndex = 0;
                return false;
            }
            keyboardTrayIndex = Math.min(keyboardTrayIndex, activeItems.size() - 1);
            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                keyboardTrayIndex = Math.max(0, keyboardTrayIndex - 1);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                keyboardTrayIndex = Math.min(activeItems.size() - 1, keyboardTrayIndex + 1);
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                controller.selectTrayItem(activeItems.get(keyboardTrayIndex));
                keyboardGridPosition = clampKeyboardGridPosition(state, currentHoverGridPosition);
                keyboardGridMode = true;
                return true;
            }
            return false;
        }

        keyboardGridPosition = clampKeyboardGridPosition(state, keyboardGridPosition);
        int col = keyboardGridPosition.getCol();
        int row = keyboardGridPosition.getRow();
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            col--;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            col++;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            row++;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            row--;
        }
        keyboardGridPosition = clampKeyboardGridPosition(state, new GridPosition(col, row));
        keyboardGridMode = true;
        currentHoverGridPosition = keyboardGridPosition;
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            controller.handleFridgeLeftClick(keyboardGridPosition);
            return true;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
            controller.trashSelectedItem();
            return true;
        }
        return false;
    }

    private GridPosition clampKeyboardGridPosition(GameState state, GridPosition position) {
        Item selectedItem = state.getTray().getSelectedItem();
        int maxCol = state.getFridge().getCols() - (selectedItem == null ? 1 : selectedItem.getSize().getWidth());
        int maxRow = state.getFridge().getRows() - (selectedItem == null ? 1 : selectedItem.getSize().getHeight());
        int col = position == null ? 0 : position.getCol();
        int row = position == null ? 0 : position.getRow();
        return new GridPosition(
            Math.max(0, Math.min(maxCol, col)),
            Math.max(0, Math.min(maxRow, row))
        );
    }

    private void handleTutorialBriefingInput(float mouseX, float mouseY) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                    transitionTo(new MainMenuScreen(game));
                    return;
                }
                if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)
                    && TUTORIAL_CONTINUE_BUTTON.contains(mouseX, mouseY)) {
                    game.getAudioFeedback().playClick();
                    if (tutorialStep == 18) {
                        transitionTo(new MainMenuScreen(game));
                    } else {
                        tutorialStep++;
                    }
                }
            }

    private void updateTutorialProgress(GameState state) {
                switch (tutorialStep) {
                    case 1 -> {
                        if (isSelected(state, "soda_can")) {
                            tutorialStep++;
                        }
                    }
                    case 3 -> {
                        if (isPlaced(state, "soda_can")) {
                            tutorialStep++;
                        }
                    }
                    case 5 -> {
                        if (isSelected(state, "tomato")) {
                            tutorialStep++;
                        }
                    }
                    case 7 -> {
                        if (isPlaced(state, "tomato")) {
                            tutorialStep++;
                        }
                    }
                    case 9 -> {
                        if (isSelected(state, "sausage")) {
                            tutorialStep++;
                        }
                    }
                    case 11 -> {
                        if (isPlaced(state, "sausage")) {
                            tutorialStep++;
                        }
                    }
                    case 13 -> {
                        if (isConfirmed(state, "soda_can") && isConfirmed(state, "tomato") && isConfirmed(state, "sausage")) {
                            tutorialStep++;
                        }
                    }
                    case 15 -> {
                        if (isSelected(state, "empty_can")) {
                            tutorialStep++;
                        }
                    }
                    case 17 -> {
                        if (state.getTrashBin().getUsedSlots() > 0) {
                            tutorialStep++;
                        }
                    }
                    default -> {
                    }
                }
            }

    private boolean isSelected(GameState state, String itemId) {
                Item selected = state.getTray().getSelectedItem();
                return selected != null && itemId.equals(selected.getId());
            }

    private boolean isPlaced(GameState state, String itemId) {
                return state.getFridge().getPlacedItems().stream()
                    .anyMatch(item -> itemId.equals(item.getId()));
            }

    private boolean isConfirmed(GameState state, String itemId) {
                return state.getFridge().getPlacedItems().stream()
                    .anyMatch(item -> itemId.equals(item.getId()) && item.isConfirmed());
            }

    private boolean isTutorialPaused() {
                return tutorialMode && tutorialStep % 2 == 0;
            }

    private void renderTutorialBriefing(
                ShapeRenderer shapes,
                SpriteBatch batch,
                float mouseX,
                float mouseY
            ) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                shapes.begin(ShapeRenderer.ShapeType.Filled);
                shapes.setColor(0.025f, 0.045f, 0.07f, 0.72f);
                shapes.rect(0f, 0f, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
                shapes.setColor(0.08f, 0.13f, 0.19f, 0.98f);
                ProceduralFridgeRenderer.drawRoundedRect(shapes, 210f, 195f, 860f, 340f, 22f);
                shapes.setColor(0.30f, 0.60f, 0.47f, 1f);
                ProceduralFridgeRenderer.drawRoundedRect(shapes, 222f, 207f, 836f, 316f, 16f);
                shapes.setColor(0.08f, 0.13f, 0.19f, 0.98f);
                ProceduralFridgeRenderer.drawRoundedRect(shapes, 228f, 213f, 824f, 304f, 13f);
                drawStyledButtonShape(
                    shapes,
                    TUTORIAL_CONTINUE_BUTTON,
                    new Color(0.28f, 0.57f, 0.42f, 1f),
                    new Color(0.36f, 0.69f, 0.51f, 1f),
                    TUTORIAL_CONTINUE_BUTTON.contains(mouseX, mouseY)
                );
                shapes.end();

                String heading = tutorialHeading();
                String explanation = tutorialExplanation();
                batch.begin();
                BitmapFont title = game.getAssetManager().getSubtitleFont();
                BitmapFont body = game.getAssetManager().getBodyFont();
                BitmapFont small = game.getAssetManager().getSmallFont();
                title.setColor(0.98f, 0.86f, 0.48f, 1f);
                drawCenteredText(batch, title, "TUTORIAL - LIVE GAMEPLAY", new Rectangle(250f, 455f, 780f, 44f), title.getColor());
                body.setColor(Color.WHITE);
                drawCenteredText(batch, body, heading, new Rectangle(255f, 394f, 770f, 46f), Color.WHITE);
                body.setColor(0.88f, 0.92f, 0.97f, 1f);
                body.draw(batch, explanation, 285f, 366f, 710f, Align.center, true);
                drawCenteredText(
                    batch,
                    body,
                    "CLICK TO CONTINUE",
                    TUTORIAL_CONTINUE_BUTTON,
                    Color.WHITE
                );
                small.setColor(0.70f, 0.79f, 0.87f, 1f);
                drawCenteredText(
                    batch,
                    small,
                    "The game is paused while this explanation is open. Press Esc to leave.",
                    new Rectangle(270f, 169f, 740f, 28f),
                    small.getColor()
                );
                title.setColor(Color.WHITE);
                body.setColor(Color.WHITE);
                small.setColor(Color.WHITE);
                batch.end();
            }

    private String tutorialHeading() {
                return switch (tutorialStep) {
                    case 0 -> "Let's sort a real stage";
                    case 2 -> "The tray timer";
                    case 4 -> "Store liquids low";
                    case 6 -> "Find the right shelf";
                    case 8 -> "Keep food categories separated";
                    case 10 -> "Sort meat into its compartment";
                    case 12 -> "Confirm when you're satisfied";
                    case 14 -> "Spot non-food items";
                    case 16 -> "Keep trash out of the fridge";
                    case 18 -> "Fresh food belongs in the fridge";
                    default -> "";
                };
            }

    private String tutorialExplanation() {
                return switch (tutorialStep) {
                    case 0 -> "This is the actual game board. Select the soda can from the tray, then place it on the fridge grid. We'll pause between each lesson so you can read at your own pace.";
                    case 2 -> "Items in the tray lose freshness over time. Once safely placed in the fridge, their countdown stops. Click continue, then place the soda can on the bottom row.";
                    case 4 -> "The soda can counts as a liquid, so place it on the bottom row (the lower chiller).";
                    case 6 -> "Tomatoes are produce, so put this one in the green chiller compartment. Place it directly above the soda can; the can is sturdy enough to support it.";
                    case 8 -> "Compartmentalizing keeps categories organized and helps limit cross-contamination. In real food storage, keep raw foods sealed and separate from ready-to-eat food.";
                    case 10 -> "The game sorts meat into the top freezer compartment. Place the sausage directly above the tomato, which can support it. In a real fridge, keep raw meat sealed and separate from ready-to-eat food.";
                    case 12 -> "Click CONFIRM or press V to lock the placements. Confirm only when you're happy: confirmed food cannot be moved or undone.";
                    case 14 -> "The empty can is a trash trap, not food. Select it from the tray, then send it to the trash bin.";
                    case 16 -> "Trash inside a fridge can contaminate food and takes up safe storage space. Dispose of packaging and waste in an outside bin instead.";
                    case 18 -> "Don't throw away fresh food: it wastes edible food and resources. Only trash actual waste; keep fresh groceries in the fridge. Click to finish the tutorial.";
                    default -> "";
                };
    }

    @Override
    public void onLivesChanged(int currentLives, int changeAmount, String reason) {
        if (changeAmount < 0) {
            hudRenderer.triggerLifeShake();
            hudRenderer.showNotification(reason + " (" + currentLives + " lives left)", new Color(1.0f, 0.35f, 0.35f, 1f), 2.8f);
        } else {
            hudRenderer.showNotification(reason, new Color(0.35f, 0.95f, 0.55f, 1f), 2.5f);
        }
    }

    @Override
    public void onStageCleared(int stageNumber) {
        if (stageNumber == com.fillthefridge.logic.ProgressiveDifficultyStrategy.FINAL_STAGE) {
            hudRenderer.showNotification(
                "Boss hit! " + game.getGameController().getBossHealth() + " sorts remain.",
                new Color(1f, 0.46f, 0.48f, 1f),
                2.0f
            );
        } else {
            hudRenderer.showNotification("Stage " + stageNumber + " Complete!", new Color(0.35f, 0.98f, 0.55f, 1f), 2.0f);
        }
    }

    @Override
    public void onItemSpoiled(Item item) {
        hudRenderer.triggerLifeShake();
    }

    @Override
    public void onWarningMessage(String message) {
        hudRenderer.showNotification(message, new Color(0.98f, 0.84f, 0.24f, 1f), 2.4f);
    }
}
