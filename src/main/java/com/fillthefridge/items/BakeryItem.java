package com.fillthefridge.items;

import com.fillthefridge.model.Fridge;
import com.fillthefridge.model.ItemCategory;
import com.fillthefridge.model.PlacementResult;
import com.fillthefridge.util.GridSize;

public class BakeryItem extends Item {
    public BakeryItem(
        String id,
        String displayName,
        String textureName,
        ItemCategory category,
        GridSize size,
        float maxShelfLife,
        int weight,
        int maxSupportWeight,
        String warningPhrase
    ) {
        super(
            id,
            displayName,
            textureName,
            category,
            size,
            maxShelfLife,
            weight,
            maxSupportWeight,
            warningPhrase
        );
    }

    @Override
    public String onTimerWarning() {
        return getWarningPhrase();
    }

    @Override
    public boolean canSupport(Item aboveItem) {
        if (aboveItem == null) {
            return false;
        }
        return aboveItem.getWeight() <= getMaxSupportWeight();
    }

    @Override
    public PlacementResult onStackedUpon(Item aboveItem, Fridge fridge) {
        if (!canSupport(aboveItem)) {
            return PlacementResult.UNSTABLE;
        }
        return PlacementResult.LEGAL;
    }

    @Override
    public Item createFreshCopy() {
        return new BakeryItem(
            getId(),
            getDisplayName(),
            getTextureName(),
            getCategory(),
            getSize(),
            getMaxShelfLife(),
            getWeight(),
            getMaxSupportWeight(),
            getWarningPhrase()
        );
    }
}
