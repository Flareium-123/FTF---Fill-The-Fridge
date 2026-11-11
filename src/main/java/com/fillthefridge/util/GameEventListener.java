package com.fillthefridge.util;

import com.fillthefridge.items.Item;
public interface GameEventListener {
    void onLivesChanged(int currentLives, int changeAmount, String reason);
    void onStageCleared(int stageNumber);
    void onItemSpoiled(Item item);
    void onWarningMessage(String message);
}
