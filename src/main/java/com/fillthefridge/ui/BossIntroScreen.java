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
import com.fillthefridge.assets.AssetManager;

public class BossIntroScreen extends BaseScreen {
    private static final Rectangle START_BUTTON = new Rectangle(465f, 82f, 350f, 66f);

    public BossIntroScreen(FillTheFridgeGame game) {
        super(game);
    }

    @Override
    public void render(float delta) {
        updateTransition(delta);
        Vector2 mouse = getMouseWorldCoords();
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
            || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)
                && START_BUTTON.contains(mouse.x, mouse.y))) {
            game.getGameController().startBossStage();
            transitionTo(new GameplayScreen(game));
        }

        Gdx.gl.glClearColor(0.16f, 0.045f, 0.065f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        ShapeRenderer shapes = game.getShapeRenderer();
        SpriteBatch batch = game.getSpriteBatch();
        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.rect(0f, 0f, VIRTUAL_WIDTH, VIRTUAL_HEIGHT, new Color(0.18f, 0.04f, 0.08f, 1f),
            new Color(0.18f, 0.04f, 0.08f, 1f), new Color(0.45f, 0.10f, 0.12f, 1f),
            new Color(0.45f, 0.10f, 0.12f, 1f));
        shapes.setColor(0.24f, 0.055f, 0.08f, 0.9f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, 300f, 130f, 680f, 520f, 24f);
        drawStyledButtonShape(shapes, START_BUTTON, new Color(0.78f, 0.22f, 0.12f, 1f),
            new Color(0.96f, 0.38f, 0.16f, 1f), START_BUTTON.contains(mouse.x, mouse.y));
        shapes.end();

        AssetManager assets = game.getAssetManager();
        batch.begin();
        BitmapFont title = assets.getTitleFont();
        title.setColor(1f, 0.80f, 0.31f, 1f);
        title.draw(batch, "BOSS STAGE", 472f, 620f);
        batch.draw(assets.getTexture(AssetManager.JUNK_FOOD_MONSTER), 535f, 275f, 210f, 190f);
        batch.end();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Color.WHITE);
        shapes.circle(594f, 405f, 10f);
        shapes.circle(684f, 405f, 10f);
        shapes.setColor(0.12f, 0.025f, 0.035f, 1f);
        shapes.rect(590f, 360f, 102f, 30f);
        shapes.setColor(1f, 0.86f, 0.46f, 1f);
        shapes.rect(600f, 369f, 15f, 12f);
        shapes.rect(625f, 369f, 15f, 12f);
        shapes.rect(650f, 369f, 15f, 12f);
        shapes.rect(675f, 369f, 9f, 12f);
        shapes.end();

        batch.begin();
        BitmapFont heading = assets.getSubtitleFont();
        BitmapFont body = assets.getBodyFont();
        BitmapFont small = assets.getSmallFont();
        var controller = game.getGameController();
        heading.setColor(1f, 0.48f, 0.32f, 1f);
        heading.draw(batch, "THE JUNK-FOOD MONSTER", 395f, 240f);
        body.setColor(Color.WHITE);
        body.draw(batch, "Three successful sorts deplete the boss's three health points.",
            385f, 205f, 510f, 1, true);
        small.setColor(1f, 0.78f, 0.47f, 1f);
        small.draw(batch, "PHASE 1 — " + controller.getBossPhaseTitle(),
            430f, 178f, 420f, 1, true);
        small.setColor(0.94f, 0.90f, 0.82f, 1f);
        small.draw(batch, controller.getBossPhaseDescription(),
            385f, 157f, 510f, 1, true);
        drawCenteredText(batch, heading, "FACE THE BOSS", START_BUTTON, Color.WHITE);
        small.setColor(0.90f, 0.90f, 0.94f, 1f);
        small.draw(batch, "Press ENTER or SPACE", 560f, 58f);
        batch.end();
        renderTransitionOverlay(shapes);
    }
}
