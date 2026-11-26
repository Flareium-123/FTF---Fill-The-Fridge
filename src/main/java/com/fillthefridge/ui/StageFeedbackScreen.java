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
import com.fillthefridge.logic.GameController;
import com.fillthefridge.logic.ProgressiveDifficultyStrategy;
import com.fillthefridge.model.StageContract;

public class StageFeedbackScreen extends BaseScreen {
    private static final Rectangle CONTINUE_BUTTON = new Rectangle(435f, 85f, 410f, 66f);
    private static final Rectangle SAVE_BUTTON = new Rectangle(220f, 85f, 410f, 66f);
    private static final Rectangle ROUTE_BUTTON = new Rectangle(650f, 85f, 410f, 66f);

    public StageFeedbackScreen(FillTheFridgeGame game) {
        super(game);
    }

    @Override
    public void render(float delta) {
        updateTransition(delta);
        GameController controller = game.getGameController();
        Vector2 mouse = getMouseWorldCoords();
        boolean bossStage = game.getGameState().getCurrentStageNumber()
            == ProgressiveDifficultyStrategy.FINAL_STAGE;
        boolean canSave = !bossStage || controller.getBossHealth() > 0;
        if (canSave && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)
            && SAVE_BUTTON.contains(mouse.x, mouse.y)) {
            if (controller.saveProgress()) {
                transitionTo(new MainMenuScreen(game));
            }
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
            || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)
                && (canSave ? ROUTE_BUTTON : CONTINUE_BUTTON).contains(mouse.x, mouse.y))) {
            controller.continueAfterFeedback();
            if (game.getGameState().getCurrentPhase()
                == com.fillthefridge.model.GamePhase.MAP_SELECTION) {
                transitionTo(new RoguelikeMapScreen(game));
            } else if (game.getGameState().getCurrentPhase()
                == com.fillthefridge.model.GamePhase.RUN_COMPLETE) {
                transitionTo(new RunCompleteScreen(game));
            } else {
                transitionTo(new GameplayScreen(game));
            }
        }

        Gdx.gl.glClearColor(0.035f, 0.065f, 0.075f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        ShapeRenderer shapes = game.getShapeRenderer();
        SpriteBatch batch = game.getSpriteBatch();
        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.rect(0f, 0f, VIRTUAL_WIDTH, VIRTUAL_HEIGHT,
            new Color(0.035f, 0.065f, 0.075f, 1f),
            new Color(0.035f, 0.065f, 0.075f, 1f),
            new Color(0.13f, 0.20f, 0.18f, 1f),
            new Color(0.13f, 0.20f, 0.18f, 1f));
        shapes.setColor(0.10f, 0.15f, 0.15f, 0.98f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, 190f, 112f, 900f, 510f, 20f);
        shapes.setColor(0.28f, 0.56f, 0.43f, 1f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, 204f, 126f, 872f, 482f, 14f);
        shapes.setColor(0.075f, 0.12f, 0.13f, 0.98f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, 211f, 133f, 858f, 468f, 10f);
        drawStyledButtonShape(
            shapes,
            canSave ? SAVE_BUTTON : CONTINUE_BUTTON,
            new Color(0.24f, 0.49f, 0.38f, 1f),
            new Color(0.34f, 0.64f, 0.47f, 1f),
            (canSave ? SAVE_BUTTON : CONTINUE_BUTTON).contains(mouse.x, mouse.y)
        );
        if (canSave) {
            drawStyledButtonShape(
                shapes,
                ROUTE_BUTTON,
                new Color(0.20f, 0.48f, 0.72f, 1f),
                new Color(0.28f, 0.58f, 0.82f, 1f),
                ROUTE_BUTTON.contains(mouse.x, mouse.y)
            );
        }
        shapes.end();

        batch.begin();
        BitmapFont title = game.getAssetManager().getTitleFont();
        BitmapFont subtitle = game.getAssetManager().getSubtitleFont();
        BitmapFont body = game.getAssetManager().getBodyFont();
        BitmapFont small = game.getAssetManager().getSmallFont();
        title.setColor(0.96f, 0.83f, 0.54f, 1f);
        String heading = bossStage ? "BOSS ROUND CLEARED" : "SORT COMPLETE";
        drawCenteredText(batch, title, heading, new Rectangle(250f, 538f, 780f, 52f), title.getColor());

        StageContract contract = controller.getCurrentStageContract();
        if (contract != null) {
            subtitle.setColor(0.74f, 0.88f, 0.78f, 1f);
            drawCenteredText(batch, subtitle, contract.getTitle(), new Rectangle(270f, 476f, 740f, 38f),
                subtitle.getColor());
            body.setColor(0.89f, 0.92f, 0.89f, 1f);
            drawWrappedText(
                batch, body, contract.getObjective(), new Rectangle(300f, 414f, 680f, 50f),
                body.getColor(), Align.center
            );
        }

        subtitle.setColor(controller.didStageContractSucceed()
            ? new Color(0.43f, 0.92f, 0.62f, 1f)
            : new Color(1f, 0.72f, 0.42f, 1f));
        drawCenteredText(batch, subtitle, controller.getLastContractResult(),
            new Rectangle(270f, 382f, 740f, 34f), subtitle.getColor());

        small.setColor(0.95f, 0.79f, 0.48f, 1f);
        drawCenteredText(batch, small, "FOOD-SAFETY TAKEAWAYS",
            new Rectangle(300f, 343f, 680f, 25f), small.getColor());
        body.setColor(0.88f, 0.91f, 0.88f, 1f);
        drawWrappedText(batch, body, "Separate raw meat from ready-to-eat food to limit cross-contamination.",
            new Rectangle(300f, 307f, 680f, 21f), body.getColor(), Align.center);
        drawWrappedText(batch, body, "Keep trash outside: it can contaminate food and uses clean space.",
            new Rectangle(300f, 281f, 680f, 21f), body.getColor(), Align.center);
        drawWrappedText(batch, body, "Don't discard fresh food: that wastes food, money and resources.",
            new Rectangle(300f, 255f, 680f, 21f), body.getColor(), Align.center);
        small.setColor(0.81f, 0.87f, 0.82f, 1f);
        if (contract != null) {
            drawWrappedText(
                batch, small, "CONTRACT LESSON: " + contract.getEducationalNote(),
                new Rectangle(300f, 211f, 680f, 30f), small.getColor(), Align.center
            );
        }

        if (bossStage && controller.getBossHealth() > 0) {
            subtitle.setColor(1f, 0.61f, 0.38f, 1f);
            drawCenteredText(batch, subtitle,
                "NEXT: " + controller.getBossPhaseTitle(),
                new Rectangle(270f, 194f, 740f, 30f), subtitle.getColor());
            small.setColor(0.94f, 0.88f, 0.78f, 1f);
            drawWrappedText(
                batch, small, controller.getBossPhaseDescription(),
                new Rectangle(300f, 151f, 680f, 34f), small.getColor(), Align.center
            );
        } else if (!bossStage) {
            small.setColor(0.80f, 0.85f, 0.82f, 1f);
            drawWrappedText(
                batch, small, "Route rooms change the next challenge: choose a reward and read its contract.",
                new Rectangle(300f, 179f, 680f, 31f), small.getColor(), Align.center
            );
        }

        String buttonText = bossStage
            ? controller.getBossHealth() > 0 ? "CONTINUE TO TELEGRAPHED BOSS PHASE" : "COMPLETE RUN"
            : "CONTINUE TO THE ROUTE MAP";
        if (canSave) {
            drawCenteredText(batch, body, "SAVE & RETURN TO MENU", SAVE_BUTTON, Color.WHITE);
            drawCenteredText(batch, body, buttonText, ROUTE_BUTTON, Color.WHITE);
        } else {
            drawCenteredText(batch, body, buttonText, CONTINUE_BUTTON, Color.WHITE);
        }
        small.setColor(0.76f, 0.82f, 0.81f, 1f);
        drawCenteredText(batch, small, "Press Enter or Space to continue",
            new Rectangle(330f, 53f, 620f, 25f), small.getColor());
        title.setColor(Color.WHITE);
        subtitle.setColor(Color.WHITE);
        body.setColor(Color.WHITE);
        small.setColor(Color.WHITE);
        batch.end();
        renderTransitionOverlay(shapes);
    }
}
