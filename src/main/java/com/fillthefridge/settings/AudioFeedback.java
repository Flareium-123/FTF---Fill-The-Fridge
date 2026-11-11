package com.fillthefridge.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public final class AudioFeedback {
    private final Sound clickSound;
    private boolean enabled;

    public AudioFeedback(boolean enabled) {
        this.enabled = enabled;
        this.clickSound = Gdx.audio.newSound(Gdx.files.internal("assets/ui_click.wav"));
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void playClick() {
        if (enabled) {
            clickSound.play(0.18f);
        }
    }

    public void dispose() {
        clickSound.dispose();
    }
}
