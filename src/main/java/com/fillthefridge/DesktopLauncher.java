package com.fillthefridge;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.Graphics;
import com.fillthefridge.settings.GameSettings;

public class DesktopLauncher {
    public static void main(String[] args) {
        boolean smokeTest = false;
        if (args != null) {
            for (String arg : args) {
                if ("--smoke-test".equalsIgnoreCase(arg)) {
                    smokeTest = true;
                    break;
                }
            }
        }

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Fill The Fridge - Roguelike Sorting Puzzle");
        GameSettings settings = new GameSettings();
        config.setBackBufferConfig(8, 8, 8, 8, 16, 0, settings.isHighQualityGraphics() ? 4 : 0);
        if (settings.isFullscreen()) {
            Graphics.DisplayMode target = Lwjgl3ApplicationConfiguration.getDisplayMode();
            for (Graphics.DisplayMode mode : Lwjgl3ApplicationConfiguration.getDisplayModes()) {
                if (mode.width == settings.getWidth() && mode.height == settings.getHeight()) {
                    target = mode;
                    break;
                }
            }
            config.setFullscreenMode(target);
        } else {
            config.setWindowedMode(settings.getWidth(), settings.getHeight());
            config.setMaximized(settings.isMaximized());
        }
        config.setForegroundFPS(60);
        config.useVsync(true);
        config.setResizable(true);

        new Lwjgl3Application(new FillTheFridgeGame(smokeTest), config);
    }
}
