package com.fillthefridge.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;

import java.util.ArrayList;
import java.util.List;

public final class TextLayoutHelper {
    private TextLayoutHelper() {
    }

    @FunctionalInterface
    public interface TextMeasurer {
        float measure(String text);
    }

    public interface FontStateTarget {
        float getScaleX();

        float getScaleY();

        void setScale(float scaleX, float scaleY);

        Color getColor();

        void setColor(Color color);
    }

    public record Layout(
        List<String> lines,
        List<Float> lineWidths,
        float scale,
        float width,
        float height,
        float lineAdvance,
        float topExtent,
        float descentExtent,
        float boundsX,
        float boundsY,
        float boundsWidth,
        float boundsHeight,
        int alignment
    ) {
        public float lineX(int lineIndex) {
            float lineWidth = lineWidths.get(lineIndex) * scale;
            if (alignment == Align.left) {
                return boundsX;
            }
            if (alignment == Align.right) {
                return boundsX + boundsWidth - lineWidth;
            }
            return boundsX + (boundsWidth - lineWidth) * 0.5f;
        }

        public float baselineY(int lineIndex) {
            float bottomBaseline = boundsY + (boundsHeight - height) * 0.5f + descentExtent * scale;
            return bottomBaseline + (lines.size() - lineIndex - 1) * lineAdvance * scale;
        }

        public float topY() {
            return baselineY(0) + topExtent * scale;
        }

        public float bottomY() {
            return baselineY(lines.size() - 1) - descentExtent * scale;
        }
    }

    public static Layout calculate(
        String text,
        float boundsX,
        float boundsY,
        float boundsWidth,
        float boundsHeight,
        float lineAdvance,
        float topExtent,
        float descent,
        int alignment,
        boolean wrap,
        TextMeasurer measurer
    ) {
        if (text == null || text.isEmpty() || boundsWidth <= 0f || boundsHeight <= 0f) {
            return new Layout(
                List.of(""),
                List.of(0f),
                1f,
                0f,
                0f,
                Math.max(0f, lineAdvance),
                Math.max(0f, topExtent),
                Math.max(0f, -descent),
                boundsX,
                boundsY,
                boundsWidth,
                boundsHeight,
                alignment
            );
        }

        float safeLineAdvance = Math.max(0f, lineAdvance);
        float safeTopExtent = Math.max(0f, topExtent);
        float descentExtent = Math.max(0f, -descent);

        if (!wrap) {
            List<String> lines = splitLines(text);
            List<Float> widths = measureLineWidths(lines, measurer);
            float textWidth = maxLineWidth(widths);
            float unscaledHeight = safeTopExtent + descentExtent
                + Math.max(0, lines.size() - 1) * safeLineAdvance;
            float widthScale = textWidth <= 0f ? 1f : boundsWidth / textWidth;
            float heightScale = unscaledHeight <= 0f ? 1f : boundsHeight / unscaledHeight;
            float scale = Math.min(1f, Math.min(widthScale, heightScale));

            return new Layout(
                List.copyOf(lines),
                List.copyOf(widths),
                Math.max(0f, scale),
                textWidth * Math.max(0f, scale),
                unscaledHeight * Math.max(0f, scale),
                safeLineAdvance,
                safeTopExtent,
                descentExtent,
                boundsX,
                boundsY,
                boundsWidth,
                boundsHeight,
                alignment
            );
        }

        // Multi-pass wrapped layout: initial pass at scale 1.0f
        List<String> baseLines = wrapLines(text, boundsWidth, measurer);
        List<Float> baseWidths = measureLineWidths(baseLines, measurer);
        float baseTextWidth = maxLineWidth(baseWidths);
        float baseUnscaledHeight = safeTopExtent + descentExtent
            + Math.max(0, baseLines.size() - 1) * safeLineAdvance;

        float widthScale = baseTextWidth <= 0f ? 1f : boundsWidth / baseTextWidth;
        float heightScale = baseUnscaledHeight <= 0f ? 1f : boundsHeight / baseUnscaledHeight;
        float directScale = Math.min(1f, Math.min(widthScale, heightScale));

        if (directScale >= 0.999f) {
            return new Layout(
                List.copyOf(baseLines),
                List.copyOf(baseWidths),
                1f,
                baseTextWidth,
                baseUnscaledHeight,
                safeLineAdvance,
                safeTopExtent,
                descentExtent,
                boundsX,
                boundsY,
                boundsWidth,
                boundsHeight,
                alignment
            );
        }

        // When downscaling is required, find the highest scale in [directScale, 1.0f]
        // where re-wrapping with effective width (boundsWidth / scale) allows text to fit.
        float low = Math.max(0.01f, directScale);
        float high = 1.0f;
        List<String> bestLines = baseLines;
        List<Float> bestWidths = baseWidths;
        float bestScale = directScale;
        float bestTextWidth = baseTextWidth;
        float bestUnscaledHeight = baseUnscaledHeight;

        for (int iter = 0; iter < 8; iter++) {
            float candidateScale = (low + high) * 0.5f;
            float effectiveWidth = boundsWidth / candidateScale;
            List<String> candidateLines = wrapLines(text, effectiveWidth, measurer);
            List<Float> candidateWidths = measureLineWidths(candidateLines, measurer);
            float candidateTextWidth = maxLineWidth(candidateWidths);
            float candidateHeight = safeTopExtent + descentExtent
                + Math.max(0, candidateLines.size() - 1) * safeLineAdvance;

            boolean fitsWidth = candidateTextWidth * candidateScale <= boundsWidth + 0.05f;
            boolean fitsHeight = candidateHeight * candidateScale <= boundsHeight + 0.05f;

            if (fitsWidth && fitsHeight) {
                bestScale = candidateScale;
                bestLines = candidateLines;
                bestWidths = candidateWidths;
                bestTextWidth = candidateTextWidth;
                bestUnscaledHeight = candidateHeight;
                low = candidateScale;
            } else {
                high = candidateScale;
            }
        }

        return new Layout(
            List.copyOf(bestLines),
            List.copyOf(bestWidths),
            Math.max(0f, bestScale),
            bestTextWidth * Math.max(0f, bestScale),
            bestUnscaledHeight * Math.max(0f, bestScale),
            safeLineAdvance,
            safeTopExtent,
            descentExtent,
            boundsX,
            boundsY,
            boundsWidth,
            boundsHeight,
            alignment
        );
    }

    private static List<Float> measureLineWidths(List<String> lines, TextMeasurer measurer) {
        List<Float> widths = new ArrayList<>(lines.size());
        for (String line : lines) {
            widths.add(Math.max(0f, measurer.measure(line)));
        }
        return widths;
    }

    private static float maxLineWidth(List<Float> widths) {
        float max = 0f;
        for (Float width : widths) {
            max = Math.max(max, width);
        }
        return max;
    }

    public static void drawFitted(
        SpriteBatch batch,
        BitmapFont font,
        String text,
        Rectangle bounds,
        Color color
    ) {
        drawFitted(batch, font, text, bounds, color, Align.center);
    }

    public static void drawFitted(
        SpriteBatch batch,
        BitmapFont font,
        String text,
        Rectangle bounds,
        Color color,
        int alignment
    ) {
        draw(batch, font, text, bounds.x, bounds.y, bounds.width, bounds.height, color, alignment, false);
    }

    public static void drawFittedText(
        SpriteBatch batch,
        BitmapFont font,
        String text,
        Rectangle bounds,
        Color color
    ) {
        drawFitted(batch, font, text, bounds, color, Align.center);
    }

    public static void drawFittedText(
        SpriteBatch batch,
        BitmapFont font,
        String text,
        Rectangle bounds,
        Color color,
        int alignment
    ) {
        draw(batch, font, text, bounds.x, bounds.y, bounds.width, bounds.height, color, alignment, false);
    }

    public static void drawFittedText(
        SpriteBatch batch,
        BitmapFont font,
        String text,
        float x,
        float y,
        float width,
        float height,
        Color color,
        int alignment
    ) {
        draw(batch, font, text, x, y, width, height, color, alignment, false);
    }

    public static void drawFittedAtBaseline(
        SpriteBatch batch,
        BitmapFont font,
        String text,
        float x,
        float baselineY,
        float width,
        Color color,
        int alignment
    ) {
        float topExtent = font.getCapHeight();
        float descent = font.getDescent();
        draw(
            batch,
            font,
            text,
            x,
            baselineY + descent,
            width,
            topExtent - descent,
            color,
            alignment,
            false
        );
    }

    public static void drawWrapped(
        SpriteBatch batch,
        BitmapFont font,
        String text,
        Rectangle bounds,
        Color color,
        int alignment
    ) {
        draw(batch, font, text, bounds.x, bounds.y, bounds.width, bounds.height, color, alignment, true);
    }

    public static void drawWrappedText(
        SpriteBatch batch,
        BitmapFont font,
        String text,
        Rectangle bounds,
        Color color,
        int alignment
    ) {
        draw(batch, font, text, bounds.x, bounds.y, bounds.width, bounds.height, color, alignment, true);
    }

    public static void drawWrappedText(
        SpriteBatch batch,
        BitmapFont font,
        String text,
        float x,
        float y,
        float width,
        float height,
        Color color,
        int alignment
    ) {
        draw(batch, font, text, x, y, width, height, color, alignment, true);
    }

    public static void withFontState(FontStateTarget target, Runnable drawing) {
        float originalScaleX = target.getScaleX();
        float originalScaleY = target.getScaleY();
        Color originalColor = new Color(target.getColor());
        try {
            drawing.run();
        } finally {
            target.setScale(originalScaleX, originalScaleY);
            target.setColor(originalColor);
        }
    }

    private static void draw(
        SpriteBatch batch,
        BitmapFont font,
        String text,
        float boundsX,
        float boundsY,
        float boundsWidth,
        float boundsHeight,
        Color color,
        int alignment,
        boolean wrap
    ) {
        if (font == null || text == null || text.isEmpty() || boundsWidth <= 0f || boundsHeight <= 0f) {
            return;
        }
        BitmapFontStateTarget state = new BitmapFontStateTarget(font);
        withFontState(state, () -> {
            float scaleX = font.getData().scaleX;
            float scaleY = font.getData().scaleY;
            Layout layout = calculate(
                text,
                boundsX,
                boundsY,
                boundsWidth,
                boundsHeight,
                font.getLineHeight(),
                font.getCapHeight(),
                font.getDescent(),
                alignment,
                wrap,
                value -> new GlyphLayout(font, value).width
            );
            font.getData().setScale(scaleX * layout.scale(), scaleY * layout.scale());
            font.setColor(color);
            for (int i = 0; i < layout.lines().size(); i++) {
                font.draw(batch, layout.lines().get(i), layout.lineX(i), layout.baselineY(i));
            }
        });
    }

    private static List<String> splitLines(String text) {
        String value = text == null ? "" : text;
        return List.of(value.split("\\r?\\n", -1));
    }

    private static List<String> wrapLines(String text, float width, TextMeasurer measurer) {
        List<String> result = new ArrayList<>();
        for (String paragraph : splitLines(text)) {
            if (paragraph.isBlank()) {
                result.add("");
                continue;
            }
            StringBuilder line = new StringBuilder();
            for (String word : paragraph.trim().split("\\s+")) {
                String candidate = line.isEmpty() ? word : line + " " + word;
                if (line.isEmpty() && measurer.measure(word) > width) {
                    result.addAll(breakWord(word, width, measurer));
                    continue;
                }
                if (!line.isEmpty() && measurer.measure(candidate) > width) {
                    result.add(line.toString());
                    line = new StringBuilder();
                    if (measurer.measure(word) > width) {
                        result.addAll(breakWord(word, width, measurer));
                    } else {
                        line.append(word);
                    }
                } else {
                    line.append(line.isEmpty() ? word : " " + word);
                }
            }
            if (!line.isEmpty()) {
                result.add(line.toString());
            }
        }
        return result.isEmpty() ? List.of("") : result;
    }

    private static List<String> breakWord(String word, float width, TextMeasurer measurer) {
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);
            String candidate = current.toString() + ch;
            if (measurer.measure(candidate) > width && !current.isEmpty()) {
                chunks.add(current.toString());
                current = new StringBuilder().append(ch);
            } else {
                current.append(ch);
            }
        }
        if (!current.isEmpty()) {
            chunks.add(current.toString());
        }
        return chunks;
    }

    private static final class BitmapFontStateTarget implements FontStateTarget {
        private final BitmapFont font;

        private BitmapFontStateTarget(BitmapFont font) {
            this.font = font;
        }

        @Override
        public float getScaleX() {
            return font.getData().scaleX;
        }

        @Override
        public float getScaleY() {
            return font.getData().scaleY;
        }

        @Override
        public void setScale(float scaleX, float scaleY) {
            font.getData().setScale(scaleX, scaleY);
        }

        @Override
        public Color getColor() {
            return font.getColor();
        }

        @Override
        public void setColor(Color color) {
            font.setColor(color);
        }
    }
}
