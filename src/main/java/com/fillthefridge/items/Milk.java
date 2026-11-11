package com.fillthefridge.items;

import com.fillthefridge.model.Fridge;
import com.fillthefridge.model.ItemCategory;
import com.fillthefridge.model.PlacementResult;
import com.fillthefridge.util.GridSize;

public class Milk extends Item {
    public Milk(
        String id,
        String displayName,
        String textureName,
        GridSize size,
        float maxShelfLife,
        int weight,
        String warningPhrase
    ) {
        super(
            id,
            displayName,
            textureName,
            ItemCategory.MILK,
            size,
            maxShelfLife,
            weight,
            0,
            warningPhrase
        );
    }

    @Override
    public String onTimerWarning() {
        return getWarningPhrase();
    }

    @Override
    public boolean canSupport(Item aboveItem) {
        return false;
    }

    @Override
    public PlacementResult onStackedUpon(Item aboveItem, Fridge fridge) {
        return canSupport(aboveItem) ? PlacementResult.LEGAL : PlacementResult.UNSTABLE;
    }

    @Override
    public Item createFreshCopy() {
        return new Milk(
            getId(),
            getDisplayName(),
            getTextureName(),
            getSize(),
            getMaxShelfLife(),
            getWeight(),
            getWarningPhrase()
        );
    }
}
