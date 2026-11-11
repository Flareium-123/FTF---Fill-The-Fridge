package com.fillthefridge.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public final class GameSettings {
    private static final Preferences PREFERENCES = Preferences.userNodeForPackage(GameSettings.class);
    private static final int DEFAULT_WIDTH = 1920;
    private static final int DEFAULT_HEIGHT = 1080;
    private static final int DEFAULT_UNDO_KEY = Input.Keys.U;

    private int width;
    private int height;
    private boolean fullscreen;
    private boolean audioEnabled;
    private boolean highQualityGraphics;
    private boolean maximized;
    private int undoKeyCode;

    public GameSettings() {
        width = PREFERENCES.getInt("width", DEFAULT_WIDTH);
        height = PREFERENCES.getInt("height", DEFAULT_HEIGHT);
        fullscreen = PREFERENCES.getBoolean("fullscreen", false);
        audioEnabled = PREFERENCES.getBoolean("audioEnabled", true);
        highQualityGraphics = PREFERENCES.getBoolean("highQualityGraphics", true);
        maximized = PREFERENCES.getBoolean("maximized", true);
        undoKeyCode = PREFERENCES.getInt("undoKeyCode", DEFAULT_UNDO_KEY);
    }

    public void setResolution(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Resolution dimensions must be positive.");
        }
        this.width = width;
        this.height = height;
        maximized = false;
        PREFERENCES.putInt("width", width);
        PREFERENCES.putInt("height", height);
        PREFERENCES.putBoolean("maximized", false);
        savePreferences();
    }

    public boolean applyDisplaySettings() {
        if (fullscreen) {
            return Gdx.graphics.setFullscreenMode(getAvailableDisplayMode());
        }
        return Gdx.graphics.setWindowedMode(width, height);
    }

    private Graphics.DisplayMode getAvailableDisplayMode() {
        for (Graphics.DisplayMode mode : Gdx.graphics.getDisplayModes()) {
            if (mode.width == width && mode.height == height) {
                return mode;
            }
        }
        return Gdx.graphics.getDisplayMode();
    }

    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
        maximized = false;
        PREFERENCES.putBoolean("fullscreen", fullscreen);
        PREFERENCES.putBoolean("maximized", false);
        savePreferences();
    }

    public void setAudioEnabled(boolean enabled) {
        audioEnabled = enabled;
        PREFERENCES.putBoolean("audioEnabled", enabled);
        savePreferences();
    }

    public void setHighQualityGraphics(boolean enabled) {
        highQualityGraphics = enabled;
        PREFERENCES.putBoolean("highQualityGraphics", enabled);
        savePreferences();
    }

    public void setUndoKeyCode(int keyCode) {
        if (keyCode < 0 || keyCode > 255) {
            throw new IllegalArgumentException("Undo key code is invalid.");
        }
        undoKeyCode = keyCode;
        PREFERENCES.putInt("undoKeyCode", keyCode);
        savePreferences();
    }

    private void savePreferences() {
        try {
            PREFERENCES.flush();
        } catch (BackingStoreException e) {
            throw new IllegalStateException("Could not save game settings.", e);
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public boolean isAudioEnabled() {
        return audioEnabled;
    }

    public boolean isHighQualityGraphics() {
        return highQualityGraphics;
    }

    public boolean isMaximized() {
        return maximized;
    }

    public int getUndoKeyCode() {
        return undoKeyCode;
    }
}
