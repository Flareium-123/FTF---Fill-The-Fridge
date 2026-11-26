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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.fillthefridge.FillTheFridgeGame;
import com.fillthefridge.assets.AssetManager;
import com.fillthefridge.logic.GameController;
import com.fillthefridge.logic.ProgressiveDifficultyStrategy;
import com.fillthefridge.model.GamePhase;
import com.fillthefridge.powerups.PowerUp;
import com.fillthefridge.model.RouteRoomType;

import java.util.ArrayList;
import java.util.List;

public class RoguelikeMapScreen extends BaseScreen {
    private static final float[] NODE_X = {190f, 474f, 758f};
    private static final float FIRST_STAGE_Y = 160f;
    private static final float STAGE_GAP = 50f;
    private static final Rectangle MAP_PANEL = new Rectangle(78f, 138f, 850f, 476f);
    private static final Rectangle DETAIL_PANEL = new Rectangle(954f, 205f, 270f, 392f);
    private static final Rectangle INVENTORY_PANEL = new Rectangle(954f, 60f, 270f, 130f);
    private final List<Rectangle> routeBounds = new ArrayList<>();
    private final List<Rectangle> inventoryCellBounds = new ArrayList<>();
    private boolean routeChosen = false;
    private int selectedChoice = -1;
    private int chosenStage = -1;
    private int hoveredInventoryIndex = -1;

    public RoguelikeMapScreen(FillTheFridgeGame game) {
        super(game);
    }

    @Override
    public void show() {
        super.show();
        this.routeChosen = false;
        this.selectedChoice = -1;
        this.chosenStage = -1;
        this.hoveredInventoryIndex = -1;
        this.inventoryCellBounds.clear();
    }

    @Override
    public void render(float delta) {
        updateTransition(delta);
        GameController controller = game.getGameController();
        List<PowerUp> choices = controller.getCurrentPowerUpChoices();
        List<RouteRoomType> roomTypes = controller.getCurrentRouteRoomTypes();
        updateRouteBounds(controller, choices);

        Vector2 mouse = getMouseWorldCoords();
        int hoveredChoice = routeChosen ? selectedChoice : findHoveredChoice(choices, mouse);
        hoveredInventoryIndex = findHoveredInventoryItem(mouse);
        int choice = -1;

        if (!routeChosen) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                transitionTo(new MapPauseScreen(game));
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) && !choices.isEmpty()) {
                choice = 0;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) && choices.size() > 1) {
                choice = 1;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) && choices.size() > 2) {
                choice = 2;
            } else if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                choice = hoveredChoice;
            }

            if (choice >= 0 && choice < choices.size()) {
                routeChosen = true;
                selectedChoice = choice;
                chosenStage = controller.getGameState().getCurrentStageNumber();
                game.getAudioFeedback().playFreeze();
                controller.choosePowerUp(choices.get(choice));
                if (game.getGameState().getCurrentPhase() == GamePhase.PRE_BOSS_EVENT) {
                    transitionTo(new PreBossEventScreen(game));
                } else if (game.getGameState().getCurrentPhase() == GamePhase.BOSS_INTRO) {
                    transitionTo(new BossIntroScreen(game));
                } else {
                    transitionTo(new GameplayScreen(game));
                }
            }
        }

        Gdx.gl.glClearColor(0.055f, 0.075f, 0.10f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        ShapeRenderer shapes = game.getShapeRenderer();
        SpriteBatch batch = game.getSpriteBatch();
        AssetManager assets = game.getAssetManager();
        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        renderBackdrop(shapes);
        renderMap(shapes, batch, controller, choices, hoveredChoice, assets);
        renderDetails(shapes, batch, choices, roomTypes, hoveredChoice, assets);
        renderPowerUpInventory(shapes, batch, controller, mouse, assets);
        renderHeader(batch, assets, controller);
        renderTransitionOverlay(shapes);
    }

    private int findHoveredChoice(List<PowerUp> choices, Vector2 mouse) {
        for (int i = 0; i < choices.size() && i < routeBounds.size(); i++) {
            if (routeBounds.get(i).contains(mouse.x, mouse.y)) {
                return i;
            }
        }
        return -1;
    }

    private int findHoveredInventoryItem(Vector2 mouse) {
        for (int i = 0; i < inventoryCellBounds.size(); i++) {
            if (inventoryCellBounds.get(i).contains(mouse.x, mouse.y)) {
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
                new Color(0.13f, 0.16f, 0.17f, 1f),
                new Color(0.13f, 0.16f, 0.17f, 1f));

        shapes.setColor(0.01f, 0.02f, 0.025f, 0.60f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, MAP_PANEL.x - 12f, MAP_PANEL.y - 12f,
                MAP_PANEL.width + 24f, MAP_PANEL.height + 24f, 10f);
        shapes.setColor(0.25f, 0.32f, 0.33f, 1f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, MAP_PANEL.x - 5f, MAP_PANEL.y - 5f,
                MAP_PANEL.width + 10f, MAP_PANEL.height + 10f, 7f);
        shapes.setColor(0.62f, 0.53f, 0.35f, 1f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, MAP_PANEL.x, MAP_PANEL.y,
                MAP_PANEL.width, MAP_PANEL.height, 5f);
        shapes.setColor(0.105f, 0.14f, 0.15f, 0.98f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, MAP_PANEL.x + 5f, MAP_PANEL.y + 5f,
                MAP_PANEL.width - 10f, MAP_PANEL.height - 10f, 4f);

        shapes.setColor(0.70f, 0.82f, 0.82f, 0.045f);
        for (int row = 0; row < ProgressiveDifficultyStrategy.FINAL_STAGE; row++) {
            shapes.rect(MAP_PANEL.x + 8f, MAP_PANEL.y + 22f + row * STAGE_GAP,
                    MAP_PANEL.width - 16f, 1f);
        }
        shapes.setColor(0.75f, 0.84f, 0.82f, 0.12f);
        shapes.circle(MAP_PANEL.x + 13f, MAP_PANEL.y + 13f, 2.5f);
        shapes.circle(MAP_PANEL.x + MAP_PANEL.width - 13f, MAP_PANEL.y + 13f, 2.5f);
        shapes.circle(MAP_PANEL.x + 13f, MAP_PANEL.y + MAP_PANEL.height - 13f, 2.5f);
        shapes.circle(MAP_PANEL.x + MAP_PANEL.width - 13f, MAP_PANEL.y + MAP_PANEL.height - 13f, 2.5f);
        shapes.end();
    }

    private void renderMap(
            ShapeRenderer shapes,
            SpriteBatch batch,
            GameController controller,
            List<PowerUp> choices,
            int hoveredChoice,
            AssetManager assets
    ) {
        int currentStage = routeChosen ? chosenStage : controller.getGameState().getCurrentStageNumber();
        List<Integer> path = controller.getSelectedMapNodes();
        List<Integer> availableNodes = controller.getCurrentMapNodeIndexes();
        drawRouteLines(shapes, controller, path, currentStage, hoveredChoice);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int stage = 1; stage <= ProgressiveDifficultyStrategy.FINAL_STAGE; stage++) {
            for (int node = 0; node < NODE_X.length; node++) {
                boolean completed = stage <= currentStage;
                boolean selected = stage == 1
                        ? node == 1
                        : stage - 2 < path.size() && path.get(stage - 2) == node;
                boolean reachable = stage == currentStage + 1 && availableNodes.contains(node);
                drawRoomNode(shapes, NODE_X[node], stageY(stage), selected, completed, reachable,
                        reachable && availableNodes.indexOf(node) == hoveredChoice);
            }
        }
        drawBossNode(shapes);
        shapes.end();

        batch.begin();
        BitmapFont small = assets.getSmallFont();
        small.setColor(0.66f, 0.77f, 0.79f, 0.92f);
        TextLayoutHelper.drawFittedAtBaseline(
                batch, small, "ROUTE SCHEMATIC  /  SECTOR 01", MAP_PANEL.x + 18f,
                MAP_PANEL.y + MAP_PANEL.height - 18f, 340f, small.getColor(), Align.left
        );
        for (int stage = 1; stage <= ProgressiveDifficultyStrategy.FINAL_STAGE; stage++) {
            small.setColor(0.71f, 0.80f, 0.80f, 0.86f);
            String label = stage == currentStage + 1 ? "NEXT" : "FLOOR " + stage;
            TextLayoutHelper.drawFittedAtBaseline(
                    batch, small, label, 102f, stageY(stage) + 4f, 74f,
                    small.getColor(), Align.left
            );
        }

        for (int node = 0; node < NODE_X.length; node++) {
            boolean reachable = currentStage < ProgressiveDifficultyStrategy.FINAL_STAGE
                    && availableNodes.contains(node);
            if (reachable) {
                int choiceIndex = availableNodes.indexOf(node);
                if (choiceIndex < choices.size()) {
                    Texture icon = assets.getTexture(choices.get(choiceIndex).getIconTextureName());
                    float y = stageY(currentStage + 1);
                    batch.draw(icon, NODE_X[node] - 18f, y - 18f, 36f, 36f);
                }
            }
        }

        Texture boss = assets.getTexture(AssetManager.JUNK_FOOD_MONSTER);
        batch.draw(boss, 474f - 27f, 570f - 24f, 54f, 48f);
        small.setColor(0.98f, 0.76f, 0.43f, 1f);
        TextLayoutHelper.drawFittedAtBaseline(
                batch, small, "BOSS", 453f, 624f, 48f, small.getColor(), Align.center
        );
        batch.end();

        renderStageOneBadge(shapes, batch, assets, currentStage);
    }

    private void updateRouteBounds(GameController controller, List<PowerUp> choices) {
        routeBounds.clear();
        int nextStage = (routeChosen ? chosenStage : controller.getGameState().getCurrentStageNumber()) + 1;
        List<Integer> availableNodes = controller.getCurrentMapNodeIndexes();
        for (int i = 0; i < choices.size() && i < availableNodes.size(); i++) {
            int node = availableNodes.get(i);
            float y = stageY(nextStage);
            routeBounds.add(new Rectangle(NODE_X[node] - 30f, y - 30f, 60f, 60f));
        }
    }

    private void renderStageOneBadge(ShapeRenderer shapes, SpriteBatch batch, AssetManager assets, int currentStage) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.24f, 0.67f, 0.48f, 1f);
        shapes.circle(NODE_X[1], stageY(1), 19f);
        shapes.end();
        batch.begin();
        Texture icon = assets.getTexture(AssetManager.CARROT);
        batch.draw(icon, NODE_X[1] - 13f, stageY(1) - 13f, 26f, 26f);
        batch.end();
    }

    private void drawRouteLines(
            ShapeRenderer shapes,
            GameController controller,
            List<Integer> path,
            int currentStage,
            int hoveredChoice
    ) {
        List<Integer> availableNodes = controller.getCurrentMapNodeIndexes();
        int previousNode = currentStage <= 1 || path.size() < currentStage - 1
                ? 1
                : path.get(currentStage - 2);
        int hoveredNode = hoveredChoice >= 0 && hoveredChoice < availableNodes.size()
                ? availableNodes.get(hoveredChoice)
                : -1;
        shapes.begin(ShapeRenderer.ShapeType.Line);
        for (int stage = 1; stage < ProgressiveDifficultyStrategy.FINAL_STAGE; stage++) {
            for (int source = 0; source < NODE_X.length; source++) {
                for (int target = Math.max(0, source - 1); target <= Math.min(2, source + 1); target++) {
                    boolean completedPath = stage == 1
                            ? source == 1 && !path.isEmpty() && path.get(0) == target
                            : stage - 1 < path.size()
                            && path.get(stage - 2) == source
                            && path.get(stage - 1) == target;
                    boolean reachable = stage == currentStage
                            && source == previousNode
                            && availableNodes.contains(target);
                    boolean hovered = reachable && target == hoveredNode;
                    if (completedPath) {
                        shapes.setColor(0.75f, 0.61f, 0.36f, 0.95f);
                        shapes.line(NODE_X[source], stageY(stage), NODE_X[target], stageY(stage + 1));
                    } else if (hovered) {
                        shapes.setColor(0.98f, 0.80f, 0.45f, 0.30f);
                        shapes.line(NODE_X[source], stageY(stage), NODE_X[target], stageY(stage + 1));
                        shapes.setColor(0.98f, 0.82f, 0.52f, 1f);
                        shapes.line(NODE_X[source], stageY(stage), NODE_X[target], stageY(stage + 1));
                    } else {
                        shapes.setColor(reachable
                                ? new Color(0.71f, 0.67f, 0.48f, 0.58f)
                                : new Color(0.56f, 0.67f, 0.68f, 0.26f));
                        drawDottedLine(shapes, NODE_X[source], stageY(stage), NODE_X[target], stageY(stage + 1));
                    }
                }
            }
        }
        shapes.setColor(0.70f, 0.33f, 0.29f, 0.75f);
        shapes.line(NODE_X[1], stageY(ProgressiveDifficultyStrategy.FINAL_STAGE), 474f, 570f);
        shapes.end();
    }

    private void drawDottedLine(ShapeRenderer shapes, float startX, float startY, float endX, float endY) {
        float deltaX = endX - startX;
        float deltaY = endY - startY;
        float length = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        float step = 13f;
        float dashLength = 4f;
        for (float distance = 0f; distance < length; distance += step) {
            float startRatio = distance / length;
            float endRatio = Math.min(1f, (distance + dashLength) / length);
            shapes.line(
                    startX + deltaX * startRatio,
                    startY + deltaY * startRatio,
                    startX + deltaX * endRatio,
                    startY + deltaY * endRatio
            );
        }
    }

    private void drawRoomNode(
            ShapeRenderer shapes,
            float x,
            float y,
            boolean selected,
            boolean completed,
            boolean reachable,
            boolean hovered
    ) {
        shapes.setColor(0.08f, 0.09f, 0.08f, 0.76f);
        shapes.circle(x + 2f, y - 3f, reachable ? 25f : 20f);
        Color ring = reachable
                ? new Color(0.78f, 0.69f, 0.46f, 1f)
                : selected ? new Color(0.64f, 0.78f, 0.73f, 1f)
                : completed ? new Color(0.49f, 0.60f, 0.58f, 0.88f)
                : new Color(0.41f, 0.49f, 0.49f, 0.58f);
        if (hovered) {
            shapes.setColor(0.93f, 0.77f, 0.45f, 0.34f);
            shapes.circle(x, y, 27f);
        }
        shapes.setColor(hovered ? new Color(0.98f, 0.86f, 0.58f, 1f) : ring);
        shapes.circle(x, y, reachable ? 22f : 18f);
        shapes.setColor(0.20f, 0.23f, 0.21f, 1f);
        shapes.circle(x, y, reachable ? 17f : 14f);
    }

    private void drawBossNode(ShapeRenderer shapes) {
        float x = 474f;
        float y = 570f;
        shapes.setColor(0.13f, 0.055f, 0.05f, 0.82f);
        shapes.circle(x + 2f, y - 3f, 34f);
        shapes.setColor(0.60f, 0.27f, 0.23f, 1f);
        shapes.circle(x, y, 31f);
        shapes.setColor(0.74f, 0.62f, 0.40f, 1f);
        shapes.circle(x, y, 26f);
        shapes.setColor(0.22f, 0.18f, 0.14f, 1f);
        shapes.circle(x, y, 22f);
    }

    private void renderDetails(
            ShapeRenderer shapes,
            SpriteBatch batch,
            List<PowerUp> choices,
            List<RouteRoomType> roomTypes,
            int hoveredChoice,
            AssetManager assets
    ) {
        int choiceIndex = hoveredChoice >= 0 ? hoveredChoice : choices.isEmpty() ? -1 : 0;
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.075f, 0.10f, 0.11f, 0.97f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, DETAIL_PANEL.x, DETAIL_PANEL.y,
                DETAIL_PANEL.width, DETAIL_PANEL.height, 8f);
        shapes.setColor(0.53f, 0.48f, 0.34f, 0.92f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, DETAIL_PANEL.x + 3f, DETAIL_PANEL.y + 3f,
                DETAIL_PANEL.width - 6f, DETAIL_PANEL.height - 6f, 6f);
        shapes.setColor(0.12f, 0.16f, 0.17f, 0.99f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, DETAIL_PANEL.x + 7f, DETAIL_PANEL.y + 7f,
                DETAIL_PANEL.width - 14f, DETAIL_PANEL.height - 14f, 4f);

        boolean hasChoice = choiceIndex >= 0 && choiceIndex < choices.size() && choiceIndex < roomTypes.size();
        float badgeSize = 56f;
        float badgeX = DETAIL_PANEL.x + (DETAIL_PANEL.width - badgeSize) * 0.5f;
        float badgeY = DETAIL_PANEL.y + 204f;
        if (hasChoice) {
            shapes.setColor(0.06f, 0.08f, 0.10f, 0.95f);
            ProceduralFridgeRenderer.drawRoundedRect(shapes, badgeX, badgeY, badgeSize, badgeSize, 8f);
            shapes.setColor(0.53f, 0.48f, 0.34f, 0.85f);
            ProceduralFridgeRenderer.drawRoundedRect(shapes, badgeX + 2f, badgeY + 2f, badgeSize - 4f, badgeSize - 4f, 6f);
            shapes.setColor(0.14f, 0.18f, 0.20f, 0.98f);
            ProceduralFridgeRenderer.drawRoundedRect(shapes, badgeX + 4f, badgeY + 4f, badgeSize - 8f, badgeSize - 8f, 5f);
        }
        shapes.end();

        batch.begin();
        BitmapFont subtitle = assets.getSubtitleFont();
        BitmapFont body = assets.getBodyFont();
        BitmapFont small = assets.getSmallFont();
        drawFittedText(
                batch, subtitle, "ROUTE REWARD",
                new Rectangle(DETAIL_PANEL.x + 14f, DETAIL_PANEL.y + 356f,
                        DETAIL_PANEL.width - 28f, 24f),
                new Color(0.95f, 0.82f, 0.57f, 1f)
        );
        if (hasChoice) {
            PowerUp powerUp = choices.get(choiceIndex);
            RouteRoomType roomType = roomTypes.get(choiceIndex);
            Texture icon = assets.getTexture(powerUp.getIconTextureName());
            drawFittedText(
                    batch, subtitle, roomType.getTitle(),
                    new Rectangle(DETAIL_PANEL.x + 14f, DETAIL_PANEL.y + 328f,
                            DETAIL_PANEL.width - 28f, 22f),
                    new Color(0.91f, 0.75f, 0.42f, 1f)
            );
            drawWrappedText(
                    batch, small, roomType.getDescription(),
                    new Rectangle(DETAIL_PANEL.x + 16f, DETAIL_PANEL.y + 270f,
                            DETAIL_PANEL.width - 32f, 54f),
                    new Color(0.84f, 0.87f, 0.82f, 1f), Align.center
            );
            float maxIconSize = 44f;
            float iconAspect = (float) icon.getWidth() / Math.max(1f, icon.getHeight());
            float iconW = iconAspect >= 1f ? maxIconSize : maxIconSize * iconAspect;
            float iconH = iconAspect <= 1f ? maxIconSize : maxIconSize / iconAspect;
            float iconX = badgeX + (badgeSize - iconW) * 0.5f;
            float iconY = badgeY + (badgeSize - iconH) * 0.5f;
            batch.draw(icon, iconX, iconY, iconW, iconH);

            drawFittedText(
                    batch, subtitle, powerUp.getName(),
                    new Rectangle(DETAIL_PANEL.x + 14f, DETAIL_PANEL.y + 172f,
                            DETAIL_PANEL.width - 28f, 24f), Color.WHITE
            );
            drawWrappedText(
                    batch, small, powerUp.getDescription(),
                    new Rectangle(DETAIL_PANEL.x + 16f, DETAIL_PANEL.y + 68f,
                            DETAIL_PANEL.width - 32f, 98f),
                    new Color(0.86f, 0.89f, 0.85f, 1f), Align.center
            );
            drawFittedText(
                    batch, small, "CLICK THE GLOWING NODE",
                    new Rectangle(DETAIL_PANEL.x + 16f, DETAIL_PANEL.y + 36f,
                            DETAIL_PANEL.width - 32f, 22f),
                    new Color(0.95f, 0.78f, 0.41f, 1f)
            );
        } else {
            drawWrappedText(
                    batch, body, "Choose an open room on the map to claim its fridge upgrade.",
                    new Rectangle(DETAIL_PANEL.x + 24f, DETAIL_PANEL.y + 180f,
                            DETAIL_PANEL.width - 48f, 90f),
                    new Color(0.86f, 0.84f, 0.75f, 1f), Align.center
            );
        }

        batch.end();
    }

    private void renderPowerUpInventory(
            ShapeRenderer shapes,
            SpriteBatch batch,
            GameController controller,
            Vector2 mouse,
            AssetManager assets
    ) {
        List<PowerUp> acquired = controller.getGameState().getActivePowerUps().getAcquiredPowerUps();
        inventoryCellBounds.clear();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.075f, 0.10f, 0.11f, 0.97f);
        ProceduralFridgeRenderer.drawRoundedRect(
                shapes, INVENTORY_PANEL.x, INVENTORY_PANEL.y,
                INVENTORY_PANEL.width, INVENTORY_PANEL.height, 8f
        );
        shapes.setColor(0.53f, 0.48f, 0.34f, 0.92f);
        ProceduralFridgeRenderer.drawRoundedRect(
                shapes, INVENTORY_PANEL.x + 3f, INVENTORY_PANEL.y + 3f,
                INVENTORY_PANEL.width - 6f, INVENTORY_PANEL.height - 6f, 6f
        );
        shapes.setColor(0.12f, 0.16f, 0.17f, 0.99f);
        ProceduralFridgeRenderer.drawRoundedRect(
                shapes, INVENTORY_PANEL.x + 7f, INVENTORY_PANEL.y + 7f,
                INVENTORY_PANEL.width - 14f, INVENTORY_PANEL.height - 14f, 4f
        );
        shapes.end();

        batch.begin();
        BitmapFont subtitle = assets.getSubtitleFont();
        BitmapFont small = assets.getSmallFont();
        drawFittedText(
                batch, subtitle, "YOUR POWER-UPS  (" + acquired.size() + ")  /  COMBOS  ("
                        + controller.getGameState().getActivePowerUps().getActiveSynergies().size() + ")",
                new Rectangle(INVENTORY_PANEL.x + 10f, INVENTORY_PANEL.y + 102f,
                        INVENTORY_PANEL.width - 20f, 22f),
                new Color(0.95f, 0.82f, 0.57f, 1f)
        );
        if (acquired.isEmpty()) {
            drawFittedText(
                    batch, small, "Choose a route reward to start your build.",
                    new Rectangle(INVENTORY_PANEL.x + 12f, INVENTORY_PANEL.y + 40f,
                            INVENTORY_PANEL.width - 24f, 32f),
                    new Color(0.84f, 0.87f, 0.82f, 1f)
            );
        } else {
            int visibleCount = Math.min(8, acquired.size());
            for (int i = 0; i < visibleCount; i++) {
                PowerUp powerUp = acquired.get(i);
                int column = i % 2;
                int row = i / 2;
                float cellX = INVENTORY_PANEL.x + 12f + column * 125f;
                float baselineY = INVENTORY_PANEL.y + 93f - row * 24f;
                Texture icon = assets.getTexture(powerUp.getIconTextureName());
                batch.draw(icon, cellX, baselineY - 15f, 20f, 20f);
                drawFittedText(
                        batch, small, inventoryLabel(powerUp),
                        new Rectangle(cellX + 24f, baselineY - 14f, 91f, 17f),
                        new Color(0.91f, 0.93f, 0.88f, 1f)
                );
                // Track cell bounds for hover detection
                inventoryCellBounds.add(new Rectangle(cellX - 4f, baselineY - 19f, 115f, 28f));
            }
        }
        batch.end();

        // Render hover tooltip
        if (hoveredInventoryIndex >= 0 && hoveredInventoryIndex < acquired.size()) {
            renderInventoryTooltip(shapes, batch, acquired.get(hoveredInventoryIndex), mouse, assets);
        }
    }

    private void renderInventoryTooltip(
            ShapeRenderer shapes,
            SpriteBatch batch,
            PowerUp powerUp,
            Vector2 mouse,
            AssetManager assets
    ) {
        BitmapFont small = assets.getSmallFont();
        BitmapFont subtitle = assets.getSubtitleFont();

        // Measure tooltip content
        GlyphLayout nameLayout = new GlyphLayout(subtitle, powerUp.getName());
        GlyphLayout descLayout = new GlyphLayout();
        descLayout.setText(small, powerUp.getDescription());
        float maxDescLineWidth = descLayout.width;

        float padding = 12f;
        float minW = 200f;
        float contentW = Math.max(minW, Math.max(nameLayout.width, maxDescLineWidth) + padding * 2);
        float contentH = nameLayout.height + 6f + descLayout.height + padding * 2;

        // Position tooltip above the cursor, keeping it on screen
        float tooltipX = mouse.x - contentW * 0.5f;
        float tooltipY = mouse.y + 16f;

        // Keep within screen bounds
        if (tooltipX < 10f) tooltipX = 10f;
        if (tooltipX + contentW > VIRTUAL_WIDTH - 10f) tooltipX = VIRTUAL_WIDTH - contentW - 10f;
        if (tooltipY + contentH > VIRTUAL_HEIGHT - 10f) {
            tooltipY = mouse.y - contentH - 16f;
        }
        if (tooltipY < 10f) tooltipY = 10f;

        // Draw tooltip background
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.06f, 0.09f, 0.12f, 0.95f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, tooltipX, tooltipY, contentW, contentH, 6f);
        shapes.setColor(0.53f, 0.48f, 0.34f, 0.85f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, tooltipX + 2f, tooltipY + 2f, contentW - 4f, contentH - 4f, 5f);
        shapes.setColor(0.10f, 0.14f, 0.16f, 0.98f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, tooltipX + 4f, tooltipY + 4f, contentW - 8f, contentH - 8f, 4f);
        shapes.end();

        // Draw tooltip text using TextLayoutHelper for proper baseline handling
        batch.begin();
        // Name: use drawFitted for proper baseline
        Rectangle nameBounds = new Rectangle(
            tooltipX + padding + 4f,
            tooltipY + padding,
            contentW - padding * 2 - 8f,
            nameLayout.height
        );
        drawFittedText(batch, subtitle, powerUp.getName(), nameBounds, new Color(0.95f, 0.82f, 0.57f, 1f));

        // Description: use drawWrapped for proper baseline and wrapping
        Rectangle descBounds = new Rectangle(
            tooltipX + padding + 4f,
            tooltipY + padding + nameLayout.height + 6f,
            contentW - padding * 2 - 8f,
            contentH - padding * 2 - nameLayout.height - 6f
        );
        drawWrappedText(batch, small, powerUp.getDescription(), descBounds, new Color(0.80f, 0.85f, 0.82f, 1f), Align.left);

        subtitle.setColor(Color.WHITE);
        small.setColor(Color.WHITE);
        batch.end();
    }

    private String inventoryLabel(PowerUp powerUp) {
        return switch (powerUp.getId()) {
            case "cold_snap" -> "Cold Snap";
            case "cold_snap_upgrade" -> "Cold Snap II";
            case "deep_freeze" -> "Deep Freeze";
            case "deep_freeze_upgrade" -> "Freeze II";
            case "ghost_fit" -> "Ghost Fit";
            case "sniff_test" -> "Sniff Test";
            case "trash_compactor" -> "Compactor";
            case "crisper_drawer" -> "Crisper";
            case "produce_saver" -> "Produce Saver";
            case "produce_saver_upgrade" -> "Produce II";
            case "reinforced_shelves" -> "Shelves";
            case "smart_thermostat" -> "Thermostat";
            case "meal_prep_plan" -> "Meal Prep";
            case "efficient_packer" -> "Packer";
            case "trap_scout" -> "Trap Scout";
            case "fifo_caddy" -> "FIFO Caddy";
            case "shelf_rails" -> "Shelf Rails";
            case "reusable_containers" -> "Containers";
            case "second_wind" -> "Second Wind";
            default -> powerUp.getName();
        };
    }

    private void renderHeader(SpriteBatch batch, AssetManager assets, GameController controller) {
        batch.begin();
        BitmapFont title = assets.getTitleFont();
        BitmapFont body = assets.getBodyFont();
        String header = "THE COLD STORAGE";
        title.setColor(0.95f, 0.82f, 0.57f, 1f);
        TextLayoutHelper.drawFittedAtBaseline(
                batch, title, header, 120f, 694f, VIRTUAL_WIDTH - 240f,
                new Color(0.95f, 0.82f, 0.57f, 1f), Align.center
        );

        int currentStage = routeChosen ? chosenStage : controller.getGameState().getCurrentStageNumber();
        String subtitleText = "FLOOR " + currentStage
                + " CLEARED   /   CHOOSE YOUR NEXT ROUTE";
        TextLayoutHelper.drawFittedAtBaseline(
                batch, body, subtitleText, 120f, 661f, VIRTUAL_WIDTH - 240f,
                new Color(0.78f, 0.82f, 0.79f, 1f), Align.center
        );

        BitmapFont small = assets.getSmallFont();
        List<PowerUp> recentlyUnlocked = controller.getRecentlyUnlockedPowerUps();
        if (!recentlyUnlocked.isEmpty()) {
            String unlockedText = "NEW UNLOCK: " + recentlyUnlocked.stream()
                    .map(PowerUp::getName)
                    .collect(java.util.stream.Collectors.joining(", "));
            TextLayoutHelper.drawFittedAtBaseline(
                    batch, small, unlockedText, 954f, 26f, 300f,
                    new Color(0.72f, 0.96f, 0.68f, 1f), Align.left
            );
        }
        TextLayoutHelper.drawFittedAtBaseline(
                batch, small, "MOUSE: INSPECT / SELECT       1  2  3: QUICK ROUTE       ESC: PAUSE",
                300f, 26f, 620f, small.getColor(), Align.left
        );
        batch.end();
    }

    private float stageY(int stage) {
        return FIRST_STAGE_Y + (stage - 1) * STAGE_GAP;
    }
}
