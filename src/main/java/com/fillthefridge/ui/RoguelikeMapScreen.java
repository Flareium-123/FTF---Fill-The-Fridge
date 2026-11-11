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

    public RoguelikeMapScreen(FillTheFridgeGame game) {
        super(game);
    }

    @Override
    public void render(float delta) {
        updateTransition(delta);
        GameController controller = game.getGameController();
        List<PowerUp> choices = controller.getCurrentPowerUpChoices();
        List<RouteRoomType> roomTypes = controller.getCurrentRouteRoomTypes();
        Vector2 mouse = getMouseWorldCoords();
        int hoveredChoice = findHoveredChoice(choices, mouse);
        int choice = -1;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            transitionTo(new MainMenuScreen(game));
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
            controller.choosePowerUp(choices.get(choice));
            if (game.getGameState().getCurrentPhase() == GamePhase.BOSS_INTRO) {
                transitionTo(new BossIntroScreen(game));
            } else {
                transitionTo(new GameplayScreen(game));
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
        renderPowerUpInventory(shapes, batch, controller, assets);
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

    private void renderBackdrop(ShapeRenderer shapes) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.rect(0f, 0f, VIRTUAL_WIDTH, VIRTUAL_HEIGHT,
                new Color(0.055f, 0.075f, 0.10f, 1f),
                new Color(0.055f, 0.075f, 0.10f, 1f),
                new Color(0.14f, 0.16f, 0.17f, 1f),
                new Color(0.14f, 0.16f, 0.17f, 1f));

        shapes.setColor(0.12f, 0.11f, 0.09f, 0.48f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, MAP_PANEL.x - 12f, MAP_PANEL.y - 12f,
                MAP_PANEL.width + 24f, MAP_PANEL.height + 24f, 10f);
        shapes.setColor(0.29f, 0.26f, 0.20f, 1f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, MAP_PANEL.x - 5f, MAP_PANEL.y - 5f,
                MAP_PANEL.width + 10f, MAP_PANEL.height + 10f, 7f);
        shapes.setColor(0.72f, 0.65f, 0.49f, 1f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, MAP_PANEL.x, MAP_PANEL.y,
                MAP_PANEL.width, MAP_PANEL.height, 5f);
        shapes.setColor(0.22f, 0.24f, 0.23f, 0.97f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, MAP_PANEL.x + 5f, MAP_PANEL.y + 5f,
                MAP_PANEL.width - 10f, MAP_PANEL.height - 10f, 4f);

        shapes.setColor(0.88f, 0.77f, 0.54f, 0.08f);
        for (int row = 0; row < ProgressiveDifficultyStrategy.FINAL_STAGE; row++) {
            shapes.rect(MAP_PANEL.x + 8f, MAP_PANEL.y + 22f + row * STAGE_GAP,
                    MAP_PANEL.width - 16f, 1f);
        }
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
        int currentStage = controller.getGameState().getCurrentStageNumber();
        List<Integer> path = controller.getSelectedMapNodes();
        List<Integer> availableNodes = controller.getCurrentMapNodeIndexes();
        drawRouteLines(shapes, controller, path);
        routeBounds.clear();

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
        Texture bossTexture = assets.getTexture(AssetManager.JUNK_FOOD_MONSTER);
        batch.draw(bossTexture, 450f, 548f, 48f, 48f);
        BitmapFont small = assets.getSmallFont();
        for (int stage = 1; stage <= ProgressiveDifficultyStrategy.FINAL_STAGE; stage++) {
            small.setColor(0.79f, 0.73f, 0.60f, 0.86f);
            String label = stage == currentStage + 1 ? "NEXT" : "FLOOR " + stage;
            small.draw(batch, label, 102f, stageY(stage) + 4f);
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
        small.draw(batch, "BOSS", 453f, 624f);
        batch.end();

        updateRouteBounds(controller, choices);
        renderStageOneBadge(shapes, batch, assets, currentStage);
    }

    private void updateRouteBounds(GameController controller, List<PowerUp> choices) {
        int nextStage = controller.getGameState().getCurrentStageNumber() + 1;
        List<Integer> availableNodes = controller.getCurrentMapNodeIndexes();
        for (int i = 0; i < choices.size() && i < availableNodes.size(); i++) {
            int node = availableNodes.get(i);
            float y = stageY(nextStage);
            while (routeBounds.size() <= i) {
                routeBounds.add(new Rectangle());
            }
            routeBounds.set(i, new Rectangle(NODE_X[node] - 30f, y - 30f, 60f, 60f));
        }
    }

    private void renderStageOneBadge(ShapeRenderer shapes, SpriteBatch batch, AssetManager assets, int currentStage) {
        if (currentStage > 1) {
            return;
        }
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.24f, 0.67f, 0.48f, 1f);
        shapes.circle(NODE_X[1], stageY(1), 19f);
        shapes.end();
        batch.begin();
        Texture icon = assets.getTexture(AssetManager.CARROT);
        batch.draw(icon, NODE_X[1] - 13f, stageY(1) - 13f, 26f, 26f);
        batch.end();
    }

    private void drawRouteLines(ShapeRenderer shapes, GameController controller, List<Integer> path) {
        int currentStage = controller.getGameState().getCurrentStageNumber();
        int previousNode;
        shapes.begin(ShapeRenderer.ShapeType.Line);
        for (int stage = 1; stage < ProgressiveDifficultyStrategy.FINAL_STAGE; stage++) {
            for (int source = 0; source < NODE_X.length; source++) {
                for (int target = Math.max(0, source - 1); target <= Math.min(2, source + 1); target++) {
                    boolean completedPath = stage == 1
                            ? source == 1 && !path.isEmpty() && path.get(0) == target
                            : stage - 1 < path.size()
                            && path.get(stage - 2) == source
                            && path.get(stage - 1) == target;
                    shapes.setColor(completedPath
                            ? new Color(0.92f, 0.72f, 0.34f, 0.95f)
                            : new Color(0.82f, 0.75f, 0.58f, 0.25f));
                    shapes.line(NODE_X[source], stageY(stage), NODE_X[target], stageY(stage + 1));
                }
            }
        }
        if (currentStage < ProgressiveDifficultyStrategy.FINAL_STAGE) {
            previousNode = currentStage <= 1 ? 1 : path.get(currentStage - 2);
            for (int node : controller.getCurrentMapNodeIndexes()) {
                shapes.setColor(0.98f, 0.78f, 0.40f, 0.96f);
                shapes.line(NODE_X[previousNode], stageY(currentStage), NODE_X[node], stageY(currentStage + 1));
            }
        }
        shapes.setColor(0.94f, 0.43f, 0.32f, 0.82f);
        shapes.line(NODE_X[1], stageY(ProgressiveDifficultyStrategy.FINAL_STAGE), 474f, 570f);
        shapes.end();
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
                ? new Color(0.78f, 0.72f, 0.52f, 1f)
                : selected ? new Color(0.68f, 0.77f, 0.53f, 1f)
                : completed ? new Color(0.57f, 0.58f, 0.47f, 0.90f)
                : new Color(0.45f, 0.48f, 0.45f, 0.68f);
        shapes.setColor(hovered ? Color.WHITE : ring);
        shapes.circle(x, y, reachable ? 22f : 18f);
        shapes.setColor(0.20f, 0.23f, 0.21f, 1f);
        shapes.circle(x, y, reachable ? 17f : 14f);
    }

    private void drawBossNode(ShapeRenderer shapes) {
        float x = 474f;
        float y = 570f;
        shapes.setColor(0.22f, 0.08f, 0.07f, 0.82f);
        shapes.circle(x + 2f, y - 3f, 34f);
        shapes.setColor(0.78f, 0.33f, 0.24f, 1f);
        shapes.circle(x, y, 31f);
        shapes.setColor(0.94f, 0.77f, 0.48f, 1f);
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
        shapes.setColor(0.10f, 0.12f, 0.12f, 0.96f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, DETAIL_PANEL.x, DETAIL_PANEL.y,
                DETAIL_PANEL.width, DETAIL_PANEL.height, 8f);
        shapes.setColor(0.68f, 0.59f, 0.42f, 0.88f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, DETAIL_PANEL.x + 3f, DETAIL_PANEL.y + 3f,
                DETAIL_PANEL.width - 6f, DETAIL_PANEL.height - 6f, 6f);
        shapes.setColor(0.14f, 0.16f, 0.15f, 0.98f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, DETAIL_PANEL.x + 7f, DETAIL_PANEL.y + 7f,
                DETAIL_PANEL.width - 14f, DETAIL_PANEL.height - 14f, 4f);
        shapes.end();

        batch.begin();
        BitmapFont subtitle = assets.getSubtitleFont();
        BitmapFont body = assets.getBodyFont();
        BitmapFont small = assets.getSmallFont();
        drawFittedText(
                batch, subtitle, "ROUTE REWARD",
                new Rectangle(DETAIL_PANEL.x + 16f, DETAIL_PANEL.y + DETAIL_PANEL.height - 47f,
                        DETAIL_PANEL.width - 32f, 30f),
                new Color(0.95f, 0.82f, 0.57f, 1f)
        );
        if (choiceIndex >= 0 && choiceIndex < choices.size()) {
            PowerUp powerUp = choices.get(choiceIndex);
            RouteRoomType roomType = roomTypes.get(choiceIndex);
            Texture icon = assets.getTexture(powerUp.getIconTextureName());
            drawFittedText(
                    batch, subtitle, roomType.getTitle(),
                    new Rectangle(DETAIL_PANEL.x + 16f, DETAIL_PANEL.y + 318f,
                            DETAIL_PANEL.width - 32f, 28f),
                    new Color(0.91f, 0.75f, 0.42f, 1f)
            );
            drawWrappedText(
                    batch, small, roomType.getDescription(),
                    new Rectangle(DETAIL_PANEL.x + 22f, DETAIL_PANEL.y + 276f,
                            DETAIL_PANEL.width - 44f, 38f),
                    new Color(0.84f, 0.87f, 0.82f, 1f), Align.left
            );
            batch.draw(icon, DETAIL_PANEL.x + 96f, DETAIL_PANEL.y + 198f, 78f, 78f);
            drawFittedText(
                    batch, subtitle, powerUp.getName(),
                    new Rectangle(DETAIL_PANEL.x + 14f, DETAIL_PANEL.y + 165f,
                            DETAIL_PANEL.width - 28f, 27f), Color.WHITE
            );
            drawWrappedText(
                    batch, body, powerUp.getDescription(),
                    new Rectangle(DETAIL_PANEL.x + 26f, DETAIL_PANEL.y + 61f,
                            DETAIL_PANEL.width - 52f, 90f),
                    new Color(0.85f, 0.85f, 0.78f, 1f), Align.left
            );
            drawFittedText(
                    batch, small, "CLICK THE GLOWING NODE",
                    new Rectangle(DETAIL_PANEL.x + 18f, DETAIL_PANEL.y + 37f,
                            DETAIL_PANEL.width - 36f, 22f),
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
            AssetManager assets
    ) {
        List<PowerUp> acquired = controller.getGameState().getActivePowerUps().getAcquiredPowerUps();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.10f, 0.12f, 0.12f, 0.96f);
        ProceduralFridgeRenderer.drawRoundedRect(
                shapes, INVENTORY_PANEL.x, INVENTORY_PANEL.y,
                INVENTORY_PANEL.width, INVENTORY_PANEL.height, 8f
        );
        shapes.setColor(0.68f, 0.59f, 0.42f, 0.88f);
        ProceduralFridgeRenderer.drawRoundedRect(
                shapes, INVENTORY_PANEL.x + 3f, INVENTORY_PANEL.y + 3f,
                INVENTORY_PANEL.width - 6f, INVENTORY_PANEL.height - 6f, 6f
        );
        shapes.setColor(0.14f, 0.16f, 0.15f, 0.98f);
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
            }
        }
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
        GlyphLayout headerLayout = new GlyphLayout(title, header);
        title.setColor(0.95f, 0.82f, 0.57f, 1f);
        title.draw(batch, header, (VIRTUAL_WIDTH - headerLayout.width) * 0.5f, 694f);

        String subtitleText = "FLOOR " + controller.getGameState().getCurrentStageNumber()
                + " CLEARED   /   CHOOSE YOUR NEXT ROUTE";
        GlyphLayout subtitleLayout = new GlyphLayout(body, subtitleText);
        body.setColor(0.78f, 0.82f, 0.79f, 1f);
        body.draw(batch, subtitleText, (VIRTUAL_WIDTH - subtitleLayout.width) * 0.5f, 661f);

        BitmapFont small = assets.getSmallFont();
        List<PowerUp> recentlyUnlocked = controller.getRecentlyUnlockedPowerUps();
        if (!recentlyUnlocked.isEmpty()) {
            String unlockedText = "NEW UNLOCK: " + recentlyUnlocked.stream()
                    .map(PowerUp::getName)
                    .collect(java.util.stream.Collectors.joining(", "));
            drawFittedText(
                    batch, small, unlockedText,
                    new Rectangle(954f, 26f, 300f, 22f),
                    new Color(0.72f, 0.96f, 0.68f, 1f)
            );
        }
        small.draw(batch, "MOUSE: INSPECT / SELECT       1  2  3: QUICK ROUTE       ESC: MENU", 300f, 26f);
        batch.end();
    }

    private float stageY(int stage) {
        return FIRST_STAGE_Y + (stage - 1) * STAGE_GAP;
    }
}
