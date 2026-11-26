package com.fillthefridge.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextLayoutHelperTest {
    private final TextLayoutHelper.TextMeasurer monospaced = text -> text.codePointCount(0, text.length()) * 10f;

    @Test
    void centersTextInsideItsBounds() {
        TextLayoutHelper.Layout layout = layout("AB", 20f, 30f, 100f, 40f, false);

        assertEquals(60f, layout.lineX(0), 0.001f);
        assertEquals(20f, layout.width(), 0.001f);
        assertEquals(48f, layout.baselineY(0), 0.001f);
    }

    @Test
    void uniformlyScalesOversizedTextToFitBothDimensions() {
        TextLayoutHelper.Layout layout = TextLayoutHelper.calculate(
            "ABCDEFGHIJ", 5f, 10f, 50f, 8f,
            12f, 8f, -4f, Align.left, false, monospaced
        );

        assertEquals(0.5f, layout.scale(), 0.001f);
        assertTrue(layout.width() <= 50f);
        assertTrue(layout.height() <= 8f);
        assertTrue(layout.topY() <= 18f);
        assertTrue(layout.bottomY() >= 10f);
    }

    @Test
    void wrapsAtWordBoundariesToFitTheAvailableWidth() {
        TextLayoutHelper.Layout layout = layout("one two three", 0f, 0f, 70f, 40f, true);

        assertEquals(2, layout.lines().size());
        assertEquals("one two", layout.lines().get(0));
        assertEquals("three", layout.lines().get(1));
        assertTrue(layout.lineWidths().stream().allMatch(width -> width <= 70f));
    }

    @Test
    void breaksLongUnspacedWordsIntoFittingLines() {
        TextLayoutHelper.Layout layout = layout("abcdefgh", 0f, 0f, 30f, 80f, true);

        assertEquals(3, layout.lines().size());
        assertEquals("abc", layout.lines().get(0));
        assertEquals("def", layout.lines().get(1));
        assertEquals("gh", layout.lines().get(2));
        assertTrue(layout.lineWidths().stream().allMatch(width -> width <= 30f));
    }

    @Test
    void accountsForDescendersWhenVerticallyCenteringWrappedText() {
        TextLayoutHelper.Layout layout = TextLayoutHelper.calculate(
            "gyp\nq", 0f, 100f, 80f, 40f,
            20f, 9f, -5f, Align.left, false, monospaced
        );

        assertTrue(layout.bottomY() >= 100f);
        assertTrue(layout.topY() <= 140f);
        assertEquals(103f, layout.bottomY(), 0.001f);
        assertEquals(137f, layout.topY(), 0.001f);
    }

    @Test
    void preservesFullScaleWhenTextFitsBoundsWithoutOverflow() {
        // "Short text" with 10 chars per word fits easily within 200px width and 50px height
        TextLayoutHelper.Layout layout = TextLayoutHelper.calculate(
            "Short text", 10f, 20f, 200f, 50f,
            18f, 14f, -4f, Align.left, true, monospaced
        );

        assertEquals(1.0f, layout.scale(), 0.001f);
        assertTrue(layout.width() <= 200f);
        assertTrue(layout.height() <= 50f);
        assertTrue(layout.topY() <= 70f);
        assertTrue(layout.bottomY() >= 20f);
    }

    @Test
    void downscalesAndRewrapsWhenHeightConstrainedWithoutExceedingBounds() {
        // Multi-line text needing wrapping where height is tight
        String text = "The quick brown fox jumps over the lazy dog and runs across the field";
        float boundsWidth = 150f;
        float boundsHeight = 40f;
        TextLayoutHelper.Layout layout = TextLayoutHelper.calculate(
            text, 50f, 50f, boundsWidth, boundsHeight,
            16f, 12f, -4f, Align.center, true, monospaced
        );

        assertTrue(layout.scale() <= 1.0f);
        assertTrue(layout.scale() > 0.1f);
        assertTrue(layout.width() <= boundsWidth + 0.1f, "Width " + layout.width() + " should not exceed " + boundsWidth);
        assertTrue(layout.height() <= boundsHeight + 0.1f, "Height " + layout.height() + " should not exceed " + boundsHeight);
        assertTrue(layout.topY() <= 50f + boundsHeight + 0.1f);
        assertTrue(layout.bottomY() >= 50f - 0.1f);
    }

    @Test
    void fitsLongDescriptionInsideBoundsWithoutClipping() {
        String longDescription = "Clear all frosted tiles instantly. Unlocks bonus compartment space when completed.";
        float boundsWidth = 240f;
        float boundsHeight = 98f;
        TextLayoutHelper.Layout layout = TextLayoutHelper.calculate(
            longDescription, 890f, 66f, boundsWidth, boundsHeight,
            16f, 12f, -4f, Align.left, true, monospaced
        );

        assertTrue(layout.scale() > 0.5f);
        assertTrue(layout.width() <= boundsWidth + 0.1f);
        assertTrue(layout.height() <= boundsHeight + 0.1f);
        assertTrue(layout.topY() <= 66f + boundsHeight + 0.1f);
        assertTrue(layout.bottomY() >= 66f - 0.1f);
    }

    @Test
    void handlesZeroOrNegativeDimensionsGracefully() {
        TextLayoutHelper.Layout zeroWidth = TextLayoutHelper.calculate(
            "test", 0f, 0f, 0f, 50f,
            12f, 8f, -4f, Align.left, true, monospaced
        );
        assertEquals(0f, zeroWidth.width(), 0.001f);
        assertEquals(0f, zeroWidth.height(), 0.001f);

        TextLayoutHelper.Layout nullText = TextLayoutHelper.calculate(
            null, 0f, 0f, 100f, 50f,
            12f, 8f, -4f, Align.left, true, monospaced
        );
        assertEquals(0f, nullText.width(), 0.001f);
    }

    @Test
    void restoresFontScaleAndColorEvenWhenDrawingFails() {
        FakeFontState target = new FakeFontState();
        Color originalColor = new Color(target.color);

        assertThrows(IllegalStateException.class, () -> TextLayoutHelper.withFontState(target, () -> {
            target.setScale(0.25f, 0.5f);
            target.setColor(Color.RED);
            throw new IllegalStateException("test failure");
        }));

        assertEquals(1f, target.scaleX, 0.001f);
        assertEquals(1f, target.scaleY, 0.001f);
        assertEquals(originalColor, target.color);
        assertFalse(target.color.equals(Color.RED));
    }

    private TextLayoutHelper.Layout layout(String text, float x, float y, float width, float height, boolean wrap) {
        return TextLayoutHelper.calculate(
            text, x, y, width, height, 12f, 8f, -4f,
            Align.center, wrap, monospaced
        );
    }

    private static final class FakeFontState implements TextLayoutHelper.FontStateTarget {
        private float scaleX = 1f;
        private float scaleY = 1f;
        private final Color color = new Color(0.2f, 0.3f, 0.4f, 1f);

        @Override
        public float getScaleX() {
            return scaleX;
        }

        @Override
        public float getScaleY() {
            return scaleY;
        }

        @Override
        public void setScale(float nextScaleX, float nextScaleY) {
            scaleX = nextScaleX;
            scaleY = nextScaleY;
        }

        @Override
        public Color getColor() {
            return color;
        }

        @Override
        public void setColor(Color nextColor) {
            color.set(nextColor);
        }
    }
}
