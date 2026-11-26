package com.fillthefridge.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

public final class AudioFeedback {
    private final Sound clickSound;
    private final Sound placeSound;
    private final Sound invalidSound;
    private final Sound trashSound;
    private final Sound freezeSound;
    private final Sound warningSound;
    private final Sound undoSound;
    private final Sound stageClearSound;
    private final Sound lifeLostSound;
    private boolean enabled;

    public AudioFeedback(boolean enabled) {
        this.enabled = enabled;
        this.clickSound = loadSound("assets/ui_click.wav");
        this.placeSound = loadSound("assets/audio/place_item.wav");
        this.invalidSound = loadSound("assets/audio/invalid.wav");
        this.trashSound = loadSound("assets/audio/trash.wav");
        this.freezeSound = loadSound("assets/audio/freeze.wav");
        this.warningSound = loadSound("assets/audio/warning.wav");
        this.undoSound = loadSound("assets/audio/undo.wav");
        this.stageClearSound = loadSound("assets/audio/stage_clear.wav");
        this.lifeLostSound = loadSound("assets/audio/life_lost.wav");
    }

    private Sound loadSound(String path) {
        try {
            FileHandle handle = Gdx.files.internal(path);
            if (handle.exists()) {
                return Gdx.audio.newSound(handle);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void play(Sound sound, float volume) {
        if (enabled && sound != null) {
            try {
                sound.play(volume);
            } catch (Exception ignored) {
            }
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void playClick() {
        play(clickSound, 0.18f);
    }

    public void playPlaceItem() {
        play(placeSound, 0.35f);
    }

    public void playInvalidPlacement() {
        play(invalidSound, 0.30f);
    }

    public void playTrash() {
        play(trashSound, 0.35f);
    }

    public void playFreeze() {
        play(freezeSound, 0.32f);
    }

    public void playWarning() {
        play(warningSound, 0.28f);
    }

    public void playUndo() {
        play(undoSound, 0.25f);
    }

    public void playStageClear() {
        play(stageClearSound, 0.35f);
    }

    public void playLifeLoss() {
        play(lifeLostSound, 0.38f);
    }

    public void dispose() {
        disposeSound(clickSound);
        disposeSound(placeSound);
        disposeSound(invalidSound);
        disposeSound(trashSound);
        disposeSound(freezeSound);
        disposeSound(warningSound);
        disposeSound(undoSound);
        disposeSound(stageClearSound);
        disposeSound(lifeLostSound);
    }

    private void disposeSound(Sound sound) {
        if (sound != null) {
            try {
                sound.dispose();
            } catch (Exception ignored) {
            }
        }
    }
}
