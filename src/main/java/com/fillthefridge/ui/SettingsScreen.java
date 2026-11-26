package com.fillthefridge.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.fillthefridge.FillTheFridgeGame;
import com.fillthefridge.settings.GameSettings;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public class SettingsScreen extends BaseScreen {
    private static final Rectangle BACK_BUTTON = new Rectangle(470f, 45f, 340f, 56f);
    private static final Rectangle RESOLUTION_PREVIOUS = new Rectangle(450f, 480f, 64f, 48f);
    private static final Rectangle RESOLUTION_NEXT = new Rectangle(766f, 480f, 64f, 48f);
    private static final Rectangle FULLSCREEN_TOGGLE = new Rectangle(540f, 385f, 290f, 56f);
    private static final Rectangle AUDIO_TOGGLE = new Rectangle(540f, 295f, 290f, 56f);
    private static final Rectangle GRAPHICS_TOGGLE = new Rectangle(540f, 205f, 290f, 56f);
    private static final Rectangle UNDO_KEY_BINDING = new Rectangle(540f, 115f, 290f, 56f);

    private final List<Resolution> resolutions;
    private int selectedResolution;
    private String statusMessage = "";
    private boolean waitingForUndoKey;
    private final Supplier<BaseScreen> returnTarget;

    public SettingsScreen(FillTheFridgeGame game) {
        this(game, () -> new MainMenuScreen(game));
    }

    public SettingsScreen(FillTheFridgeGame game, Supplier<BaseScreen> returnTarget) {
        super(game);
        resolutions = availableResolutions();
        selectedResolution = findSelectedResolution(game.getSettings());
        this.returnTarget = returnTarget;
    }

    @Override
    public void render(float delta) {
        updateTransition(delta);
        Vector2 mouse = getMouseWorldCoords();
        if (waitingForUndoKey) {
            captureUndoKey();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            transitionTo(returnTarget.get());
        } else if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            handleClick(mouse.x, mouse.y);
        }

        Gdx.gl.glClearColor(0.055f, 0.075f, 0.10f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        ShapeRenderer shapes = game.getShapeRenderer();
        SpriteBatch batch = game.getSpriteBatch();
        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.07f, 0.11f, 0.16f, 0.96f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, 260f, 38f, 760f, 640f, 22f);
        drawButton(shapes, RESOLUTION_PREVIOUS, mouse);
        drawButton(shapes, RESOLUTION_NEXT, mouse);
        drawButton(shapes, FULLSCREEN_TOGGLE, mouse);
        drawButton(shapes, AUDIO_TOGGLE, mouse);
        drawButton(
            shapes,
            GRAPHICS_TOGGLE,
            mouse
        );
        drawButton(shapes, UNDO_KEY_BINDING, mouse);
        drawButton(shapes, BACK_BUTTON, mouse);
        shapes.end();

        batch.begin();
        BitmapFont title = game.getAssetManager().getTitleFont();
        BitmapFont body = game.getAssetManager().getBodyFont();
        BitmapFont small = game.getAssetManager().getSmallFont();
        title.setColor(0.95f, 0.82f, 0.57f, 1f);
        drawCentered(batch, title, "SETTINGS", 640f, 610f);
        body.setColor(Color.WHITE);
        body.draw(batch, "Resolution", 355f, 510f);
        small.setColor(0.78f, 0.85f, 0.93f, 1f);
        drawCentered(batch, body, resolutions.get(selectedResolution).label(), 640f, 505f);
        drawCentered(batch, body, "<", 482f, 512f);
        drawCentered(batch, body, ">", 798f, 512f);
        body.setColor(Color.WHITE);
        body.draw(batch, "Fullscreen", 355f, 421f);
        body.draw(batch, "Audio", 355f, 331f);
        body.draw(batch, "Graphics quality", 355f, 241f);
        body.draw(batch, "Undo placement", 355f, 151f);
        drawCentered(batch, body, game.getSettings().isFullscreen() ? "ON" : "OFF", 685f, 421f);
        drawCentered(batch, body, game.getSettings().isAudioEnabled() ? "ON" : "OFF", 685f, 331f);
        drawCentered(
            batch,
            body,
            game.getSettings().isHighQualityGraphics() ? "HIGH" : "STANDARD",
            685f,
            241f
        );
        drawCentered(
            batch,
            body,
            waitingForUndoKey ? "PRESS A KEY..." : Input.Keys.toString(game.getSettings().getUndoKeyCode()),
            685f,
            151f
        );
        drawCentered(batch, body, "BACK TO MENU", 640f, 83f);
        small.setColor(0.70f, 0.80f, 0.90f, 1f);
        drawCentered(batch, small, "Click the undo key and press a new key. Esc cancels.", 640f, 184f);
        if (!statusMessage.isEmpty()) {
            small.setColor(1f, 0.73f, 0.46f, 1f);
            drawCentered(batch, small, statusMessage, 640f, 555f);
        }
        body.setColor(Color.WHITE);
        batch.end();
        renderTransitionOverlay(shapes);
    }

    private void drawButton(ShapeRenderer shapes, Rectangle bounds, Vector2 mouse) {
        boolean hovered = bounds.contains(mouse.x, mouse.y);
        shapes.setColor(hovered ? 0.30f : 0.20f, hovered ? 0.48f : 0.34f, hovered ? 0.55f : 0.42f, 1f);
        ProceduralFridgeRenderer.drawRoundedRect(shapes, bounds.x, bounds.y, bounds.width, bounds.height, 12f);
    }

    private void drawCentered(SpriteBatch batch, BitmapFont font, String text, float centerX, float baselineY) {
        GlyphLayout layout = new GlyphLayout(font, text);
        font.draw(batch, text, centerX - layout.width * 0.5f, baselineY);
    }

    private void handleClick(float x, float y) {
        if (BACK_BUTTON.contains(x, y)) {
            game.getAudioFeedback().playClick();
            transitionTo(returnTarget.get());
        } else if (UNDO_KEY_BINDING.contains(x, y)) {
            game.getAudioFeedback().playClick();
            waitingForUndoKey = true;
            statusMessage = "Press a key to bind Undo. Esc cancels.";
        } else if (RESOLUTION_PREVIOUS.contains(x, y) || RESOLUTION_NEXT.contains(x, y)) {
            game.getAudioFeedback().playClick();
            int direction = RESOLUTION_NEXT.contains(x, y) ? 1 : -1;
            int previousResolution = selectedResolution;
            selectedResolution = Math.floorMod(selectedResolution + direction, resolutions.size());
            Resolution resolution = resolutions.get(selectedResolution);
            game.getSettings().setResolution(resolution.width(), resolution.height());
            if (!applyDisplaySettings()) {
                selectedResolution = previousResolution;
                Resolution previous = resolutions.get(previousResolution);
                game.getSettings().setResolution(previous.width(), previous.height());
                game.getSettings().applyDisplaySettings();
            }
        } else if (FULLSCREEN_TOGGLE.contains(x, y)) {
            game.getAudioFeedback().playClick();
            boolean previous = game.getSettings().isFullscreen();
            game.getSettings().setFullscreen(!previous);
            if (!applyDisplaySettings()) {
                game.getSettings().setFullscreen(previous);
                game.getSettings().applyDisplaySettings();
                statusMessage = "That display mode could not be applied.";
            } else {
                statusMessage = "";
            }
        } else if (AUDIO_TOGGLE.contains(x, y)) {
            game.getAudioFeedback().playClick();
            boolean enabled = !game.getSettings().isAudioEnabled();
            game.getSettings().setAudioEnabled(enabled);
            game.getAudioFeedback().setEnabled(enabled);
            statusMessage = "";
        } else if (GRAPHICS_TOGGLE.contains(x, y)) {
            game.getAudioFeedback().playClick();
            boolean highQuality = !game.getSettings().isHighQualityGraphics();
            game.getSettings().setHighQualityGraphics(highQuality);
            game.getAssetManager().setHighQualityGraphics(highQuality);
            statusMessage = "Texture filtering updated. Restart to apply edge smoothing.";
        }
    }

    private void captureUndoKey() {
        for (int keyCode = 0; keyCode <= 255; keyCode++) {
            if (!Gdx.input.isKeyJustPressed(keyCode)) {
                continue;
            }
            if (keyCode == Input.Keys.ESCAPE) {
                waitingForUndoKey = false;
                statusMessage = "Undo key unchanged.";
                return;
            }
            if (isReservedGameplayKey(keyCode)) {
                statusMessage = "That key is already used by another game action.";
                return;
            }

            game.getSettings().setUndoKeyCode(keyCode);
            waitingForUndoKey = false;
            statusMessage = "Undo key set to " + Input.Keys.toString(keyCode) + ".";
            return;
        }
    }

    private boolean isReservedGameplayKey(int keyCode) {
        return keyCode == Input.Keys.SPACE
            || keyCode == Input.Keys.V
            || keyCode == Input.Keys.F
            || keyCode == Input.Keys.C
            || keyCode == Input.Keys.R
            || keyCode == Input.Keys.X
            || keyCode == Input.Keys.LEFT
            || keyCode == Input.Keys.RIGHT
            || keyCode == Input.Keys.UP
            || keyCode == Input.Keys.DOWN;
    }

    private boolean applyDisplaySettings() {
        boolean applied = game.getSettings().applyDisplaySettings();
        if (!applied) {
            statusMessage = "That display mode could not be applied.";
        }
        return applied;
    }

    private int findSelectedResolution(GameSettings settings) {
        for (int i = 0; i < resolutions.size(); i++) {
            Resolution resolution = resolutions.get(i);
            if (resolution.width() == settings.getWidth() && resolution.height() == settings.getHeight()) {
                return i;
            }
        }
        resolutions.add(new Resolution(settings.getWidth(), settings.getHeight()));
        resolutions.sort(Comparator.comparingInt(Resolution::width));
        for (int i = 0; i < resolutions.size(); i++) {
            Resolution resolution = resolutions.get(i);
            if (resolution.width() == settings.getWidth() && resolution.height() == settings.getHeight()) {
                return i;
            }
        }
        throw new IllegalStateException("Saved resolution is missing from the settings list.");
    }

    private List<Resolution> availableResolutions() {
        List<Resolution> result = new ArrayList<>();
        for (com.badlogic.gdx.Graphics.DisplayMode mode : Gdx.graphics.getDisplayModes()) {
            if (mode.width >= 960 && Math.abs((mode.width / (float) mode.height) - (16f / 9f)) < 0.04f) {
                Resolution resolution = new Resolution(mode.width, mode.height);
                if (!result.contains(resolution)) {
                    result.add(resolution);
                }
            }
        }
        if (result.isEmpty()) {
            com.badlogic.gdx.Graphics.DisplayMode current = Gdx.graphics.getDisplayMode();
            result.add(new Resolution(current.width, current.height));
        }
        result.sort(Comparator.comparingInt(Resolution::width));
        return result;
    }

    private record Resolution(int width, int height) {
        String label() {
            return width + " x " + height;
        }
    }
}
