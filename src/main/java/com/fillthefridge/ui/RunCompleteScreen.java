package com.fillthefridge.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.fillthefridge.FillTheFridgeGame;

public class RunCompleteScreen extends BaseScreen {
    private final Rectangle menuButton = new Rectangle(490f, 170f, 300f, 62f);

    public RunCompleteScreen(FillTheFridgeGame game) {
        super(game);
    }

    @Override
    public void render(float delta) {
        updateTransition(delta);
        Vector2 mouse = getMouseWorldCoords();
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
            || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && menuButton.contains(mouse.x, mouse.y))) {
            transitionTo(new MainMenuScreen(game));
        }

        Gdx.gl.glClearColor(0.08f, 0.18f, 0.20f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        ShapeRenderer shapes = game.getShapeRenderer();
        shapes.setProjectionMatrix(camera.combined);
        game.getSpriteBatch().setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.rect(0f, 0f, VIRTUAL_WIDTH, VIRTUAL_HEIGHT, new Color(0.08f, 0.18f, 0.20f, 1f),
            new Color(0.08f, 0.18f, 0.20f, 1f), new Color(0.20f, 0.42f, 0.50f, 1f),
            new Color(0.20f, 0.42f, 0.50f, 1f));
        shapes.setColor(0.08f, 0.13f, 0.22f, 0.94f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, 300f, 120f, 680f, 500f, 24f);
        drawStyledButtonShape(shapes, menuButton, new Color(0.18f, 0.66f, 0.42f, 1f),
            new Color(0.28f, 0.78f, 0.52f, 1f), menuButton.contains(mouse.x, mouse.y));
        shapes.end();

        game.getSpriteBatch().begin();
        BitmapFont title = game.getAssetManager().getTitleFont();
        BitmapFont body = game.getAssetManager().getBodyFont();
        drawFittedText(
            game.getSpriteBatch(),
            title,
            "BOSS DEFEATED!",
            new Rectangle(320f, 475f, 640f, 48f),
            new Color(0.98f, 0.90f, 0.25f, 1f)
        );
        drawWrappedText(
            game.getSpriteBatch(),
            body,
            "Three perfect fridge sorts kept every compartment safe.",
            new Rectangle(340f, 395f, 600f, 50f),
            Color.WHITE,
            Align.center
        );
        drawCenteredText(game.getSpriteBatch(), body, "MAIN MENU", menuButton, Color.WHITE);
        game.getSpriteBatch().end();
        renderTransitionOverlay(shapes);
    }
}
