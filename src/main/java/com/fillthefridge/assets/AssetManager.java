package com.fillthefridge.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.fillthefridge.util.AssetLoadException;
import com.fillthefridge.util.Registry;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AssetManager {
    public static final String WHOLE_CHICKEN = "whole_chicken.png";
    public static final String STEAK = "steak.png";
    public static final String FISH = "fish.png";
    public static final String SAUSAGE = "sausage.png";
    public static final String MILK = "milk.png";
    public static final String YOGURT_CUP = "yogurt_cup.png";
    public static final String CHEESE = "cheese.png";
    public static final String BUTTER = "butter.png";
    public static final String EGG_CARTON = "egg_carton.png";
    public static final String LETTUCE = "lettuce.png";
    public static final String CARROT = "carrot.png";
    public static final String TOMATO = "tomato.png";
    public static final String BROCCOLI = "broccoli.png";
    public static final String CABBAGE = "cabbage.png";
    public static final String APPLE = "apple.png";
    public static final String AVOCADO = "avocado.png";
    public static final String BANANA = "banana.png";
    public static final String CHERRIES = "cherries.png";
    public static final String GRAPES = "grapes.png";
    public static final String STRAWBERRIES = "strawberries.png";
    public static final String BREAD = "bread.png";
    public static final String PIZZA_BOX = "pizza_box.png";
    public static final String JAM_JAR = "jam_jar.png";
    public static final String JUICE_BOTTLE = "juice_bottle.png";
    public static final String SODA_CAN = "soda_can.png";
    public static final String JUICE_BOX = "juice_box.png";
    public static final String CUP_WITH_STRAW = "cup_with_straw.png";
    public static final String BUBBLE_TEA = "bubble_tea.png";
    public static final String PEACH = "peach.png";
    public static final String CORN = "corn.png";
    public static final String MUSHROOM = "mushroom.png";
    public static final String WATERMELON = "watermelon.png";
    public static final String SHRIMP = "shrimp.png";
    public static final String HAM = "ham.png";
    public static final String CHICKEN_BREAST = "chicken_breast.png";
    public static final String RAW_SALMON = "salmon.png";
    public static final String LEFTOVERS_CONTAINER = "leftovers_container.png";
    public static final String SOUP_CONTAINER = "soup_container.png";
    public static final String TAKEOUT_BOX = "takeout_box.png";

    public static final String EMPTY_MILK_CARTON = "empty_milk_carton.png";
    public static final String EMPTY_CAN = "empty_can.png";
    public static final String BANANA_PEEL = "banana_peel.png";
    public static final String PLASTIC_BAG = "plastic_bag.png";

    public static final String TRASH_BIN = "trash_bin.png";
    public static final String HEART = "heart.png";
    public static final String WARNING_ICON = "warning_icon.png";
    public static final String SNOWFLAKE = "snowflake.png";
    public static final String FRIDGE_BACKGROUND = "fridge_bg.png";
    public static final String FRIDGE_PHOTO = "fridge_photo.jpg";
    public static final String JUNK_FOOD_MONSTER = "junk_food_monster.png";

    // Hero power-up icons (procedurally generated)
    public static final String HERO_VETERAN = "hero_veteran.png";
    public static final String HERO_FIFO = "hero_fifo.png";
    public static final String HERO_PRODUCE = "hero_produce.png";
    public static final String HERO_SPACE = "hero_space.png";
    public static final String HERO_TRAP = "hero_trap.png";
    public static final String HERO_FREEZE = "hero_freeze.png";
    public static final String HERO_REINFORCED = "hero_reinforced.png";
    public static final String HERO_TIME = "hero_time.png";

    private static final String ITEMS_DIRECTORY = "assets/items";
    private static final String FALLBACK_ASSETS_DIRECTORY = "assets";
    private static final String FONT_FILE_PATH = "assets/fonts/OpenSans.ttf";
    private static final String CREDITS_FILE_PATH = "assets/credits.txt";
    private static final int FONT_RASTER_SIZE = 48;
    private static final float DEFAULT_FONT_SIZE = 15f;

    private final Registry<String, Texture> textureRegistry;
    private final Map<String, Boolean> placeholderFlags;
    private final List<String> discoveredFiles;
    private final List<String> missingAssets;
    private final Set<String> mipmappedTextures;
    private boolean highQualityGraphics;

    private BitmapFont smallFont;
    private BitmapFont bodyFont;
    private BitmapFont subtitleFont;
    private BitmapFont titleFont;

    public AssetManager() {
        this.textureRegistry = new Registry<>();
        this.placeholderFlags = new LinkedHashMap<>();
        this.discoveredFiles = new ArrayList<>();
        this.missingAssets = new ArrayList<>();
        this.mipmappedTextures = new HashSet<>();
        this.highQualityGraphics = true;
    }

    public void loadAll() {
        initializeFonts();
        scanAvailableFiles();
        for (String catalogFile : getCatalogFilenames()) {
            loadCatalogTexture(catalogFile);
        }
        generateHeroIcons();
    }

    private void initializeFonts() {
        FreeTypeFontGenerator generator;
        try {
            generator = new FreeTypeFontGenerator(Gdx.files.internal(FONT_FILE_PATH));
        } catch (RuntimeException e) {
            throw new AssetLoadException("Failed to load font", FONT_FILE_PATH, e);
        }
        try {
            smallFont = createScaledFont(generator, 0.92f);
            bodyFont = createScaledFont(generator, 1.15f);
            subtitleFont = createScaledFont(generator, 1.55f);
            titleFont = createScaledFont(generator, 2.45f);
        } catch (RuntimeException e) {
            disposeFonts();
            throw new AssetLoadException("Failed to generate game fonts", FONT_FILE_PATH, e);
        } finally {
            generator.dispose();
        }
    }

    private BitmapFont createScaledFont(FreeTypeFontGenerator generator, float scale) {
        FreeTypeFontGenerator.FreeTypeFontParameter parameters =
            new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameters.size = FONT_RASTER_SIZE;
        parameters.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "—°";
        parameters.spaceX = 2;
        parameters.spaceY = 2;
        parameters.genMipMaps = true;
        parameters.minFilter = Texture.TextureFilter.MipMapLinearLinear;
        parameters.magFilter = Texture.TextureFilter.Linear;

        BitmapFont font = generator.generateFont(parameters);
        font.getData().setScale(scale * DEFAULT_FONT_SIZE / FONT_RASTER_SIZE);
        font.setUseIntegerPositions(false);
        return font;
    }

    private void disposeFonts() {
        if (smallFont != null) {
            smallFont.dispose();
            smallFont = null;
        }
        if (bodyFont != null) {
            bodyFont.dispose();
            bodyFont = null;
        }
        if (subtitleFont != null) {
            subtitleFont.dispose();
            subtitleFont = null;
        }
        if (titleFont != null) {
            titleFont.dispose();
            titleFont = null;
        }
    }

    public List<String> getCatalogFilenames() {
        return List.of(
            WHOLE_CHICKEN, STEAK, FISH, SAUSAGE,
            MILK, YOGURT_CUP, CHEESE, BUTTER, EGG_CARTON,
            LETTUCE, CARROT, TOMATO, BROCCOLI, CABBAGE, APPLE,
            AVOCADO, BANANA, CHERRIES, GRAPES, STRAWBERRIES,
            BREAD, PIZZA_BOX, JAM_JAR, JUICE_BOTTLE, SODA_CAN,
            JUICE_BOX, CUP_WITH_STRAW, BUBBLE_TEA,
            PEACH, CORN, MUSHROOM, WATERMELON, SHRIMP, HAM, CHICKEN_BREAST, RAW_SALMON,
            LEFTOVERS_CONTAINER, SOUP_CONTAINER, TAKEOUT_BOX,
            EMPTY_MILK_CARTON, EMPTY_CAN, BANANA_PEEL, PLASTIC_BAG,
            TRASH_BIN, HEART, WARNING_ICON, SNOWFLAKE, JUNK_FOOD_MONSTER, FRIDGE_PHOTO
        );
    }

    private void scanAvailableFiles() {
        discoveredFiles.clear();
        File dir = new File(ITEMS_DIRECTORY);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile() && isImageFile(f.getName())) {
                        discoveredFiles.add(f.getName());
                    }
                }
            }
        }
        Collections.sort(discoveredFiles);
    }

    private boolean isImageFile(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg");
    }

    private void loadCatalogTexture(String expectedFilename) {
        FileHandle resolvedHandle = resolveImageFileHandle(expectedFilename);
        if (resolvedHandle != null && resolvedHandle.exists()) {
            try {
                Pixmap rawPixmap = new Pixmap(resolvedHandle);
                Pixmap trimmedPixmap = trimTransparentBorders(rawPixmap);
                Texture texture = new Texture(trimmedPixmap, true);
                trimmedPixmap.dispose();
                mipmappedTextures.add(expectedFilename);
                texture.setFilter(
                    highQualityGraphics ? Texture.TextureFilter.MipMapLinearLinear : Texture.TextureFilter.Linear,
                    Texture.TextureFilter.Linear
                );
                textureRegistry.register(expectedFilename, texture);
                placeholderFlags.put(expectedFilename, false);
                return;
            } catch (Exception ignored) {
            }
        }
        missingAssets.add(expectedFilename);
        Texture placeholder = generatePlaceholderTexture(expectedFilename);
        textureRegistry.register(expectedFilename, placeholder);
        placeholderFlags.put(expectedFilename, true);
    }

    private Pixmap trimTransparentBorders(Pixmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int minX = width;
        int minY = height;
        int maxX = -1;
        int maxY = -1;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = source.getPixel(x, y);
                int alpha = pixel & 0xFF;
                if (alpha > 16) {
                    if (x < minX) {
                        minX = x;
                    }
                    if (x > maxX) {
                        maxX = x;
                    }
                    if (y < minY) {
                        minY = y;
                    }
                    if (y > maxY) {
                        maxY = y;
                    }
                }
            }
        }

        if (maxX < minX || maxY < minY) {
            return source;
        }

        int trimmedWidth = maxX - minX + 1;
        int trimmedHeight = maxY - minY + 1;
        if (trimmedWidth == width && trimmedHeight == height) {
            return source;
        }

        Pixmap trimmed = new Pixmap(trimmedWidth, trimmedHeight, Pixmap.Format.RGBA8888);
        trimmed.setBlending(Pixmap.Blending.None);
        trimmed.drawPixmap(source, 0, 0, minX, minY, trimmedWidth, trimmedHeight);
        source.dispose();
        return trimmed;
    }

    private FileHandle resolveImageFileHandle(String expectedFilename) {
        String[] searchRoots = new String[]{ITEMS_DIRECTORY, FALLBACK_ASSETS_DIRECTORY, "."};
        for (String root : searchRoots) {
            String candidatePath = root.equals(".") ? expectedFilename : root + "/" + expectedFilename;
            FileHandle handle = getFileHandleSafely(candidatePath);
            if (handle != null && handle.exists() && !handle.isDirectory()) {
                return handle;
            }
        }

        String bestMatch = findBestSimilarFilename(expectedFilename);
        if (bestMatch != null) {
            for (String root : searchRoots) {
                String candidatePath = root.equals(".") ? bestMatch : root + "/" + bestMatch;
                FileHandle handle = getFileHandleSafely(candidatePath);
                if (handle != null && handle.exists() && !handle.isDirectory()) {
                    return handle;
                }
            }
        }
        return null;
    }

    private FileHandle getFileHandleSafely(String path) {
        if (Gdx.files != null) {
            FileHandle internal = Gdx.files.internal(path);
            if (internal.exists()) {
                return internal;
            }
            FileHandle local = Gdx.files.local(path);
            if (local.exists()) {
                return local;
            }
        }
        File rawFile = new File(path);
        if (rawFile.exists() && rawFile.isFile()) {
            return new FileHandle(rawFile);
        }
        return null;
    }

    private String findBestSimilarFilename(String expectedFilename) {
        String expectedStem = normalizeStem(expectedFilename);
        String bestCandidate = null;
        int bestScore = 0;

        for (String candidate : discoveredFiles) {
            if (candidate.equalsIgnoreCase("fridge_bg.png")) {
                continue;
            }
            String candidateStem = normalizeStem(candidate);
            int score = computeSimilarityScore(expectedStem, candidateStem);
            if (score > bestScore) {
                bestScore = score;
                bestCandidate = candidate;
            }
        }
        return bestScore >= 40 ? bestCandidate : null;
    }

    private String normalizeStem(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        String base = dotIndex > 0 ? filename.substring(0, dotIndex) : filename;
        return base.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private int computeSimilarityScore(String expected, String candidate) {
        if (expected.equals(candidate)) {
            return 100;
        }
        if (expected.contains(candidate) || candidate.contains(expected)) {
            return 75;
        }
        if (expected.contains("chicken") && candidate.contains("chicken")) {
            return 65;
        }
        if (expected.contains("steak") && candidate.contains("meat")) {
            return 60;
        }
        if (expected.contains("fish") && candidate.contains("fish")) {
            return 65;
        }
        if (expected.contains("yogurt") && candidate.contains("yogurt")) {
            return 65;
        }
        if (expected.contains("leftover") && candidate.contains("leftover")) {
            return 65;
        }
        return 0;
    }

    private Texture generatePlaceholderTexture(String filename) {
        int size = 128;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        Color baseColor = pickColorForFilename(filename);
        pixmap.setColor(baseColor);
        pixmap.fillRectangle(6, 6, size - 12, size - 12);

        Color borderColor = new Color(
            Math.max(0f, baseColor.r - 0.22f),
            Math.max(0f, baseColor.g - 0.22f),
            Math.max(0f, baseColor.b - 0.22f),
            1f
        );
        pixmap.setColor(borderColor);
        for (int i = 0; i < 4; i++) {
            pixmap.drawRectangle(6 + i, 6 + i, size - 12 - i * 2, size - 12 - i * 2);
        }

        Texture texture = new Texture(pixmap, true);
        texture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    private Color pickColorForFilename(String filename) {
        int hash = Math.abs(filename.hashCode());
        float r = 0.40f + ((hash & 0xFF) / 255f) * 0.45f;
        float g = 0.40f + (((hash >> 8) & 0xFF) / 255f) * 0.45f;
        float b = 0.40f + (((hash >> 16) & 0xFF) / 255f) * 0.45f;
        return new Color(r, g, b, 1f);
    }

    public Texture getTexture(String name) {
        return textureRegistry.get(name).orElseGet(() -> {
            loadCatalogTexture(name);
            return textureRegistry.get(name).orElseThrow(
                () -> new AssetLoadException("Unable to load or generate texture", name)
            );
        });
    }

    public boolean isPlaceholder(String name) {
        return placeholderFlags.getOrDefault(name, false);
    }

    public void setHighQualityGraphics(boolean enabled) {
        highQualityGraphics = enabled;
        for (Map.Entry<String, Texture> entry : textureRegistry.getAll().entrySet()) {
            Texture.TextureFilter minFilter = enabled && mipmappedTextures.contains(entry.getKey())
                ? Texture.TextureFilter.MipMapLinearLinear
                : Texture.TextureFilter.Linear;
            entry.getValue().setFilter(minFilter, Texture.TextureFilter.Linear);
        }
    }

    public List<String> getDiscoveredFiles() {
        return Collections.unmodifiableList(discoveredFiles);
    }

    public List<String> getMissingAssets() {
        return Collections.unmodifiableList(missingAssets);
    }

    public List<String> loadCreditsLines() throws AssetLoadException {
        FileHandle handle = getFileHandleSafely(CREDITS_FILE_PATH);
        if (handle == null || !handle.exists()) {
            handle = getFileHandleSafely("credits.txt");
        }
        if (handle == null || !handle.exists()) {
            throw new AssetLoadException("Credits file not found", CREDITS_FILE_PATH);
        }
        try {
            String rawContent = handle.readString("UTF-8");
            String[] lines = rawContent.split("\\r?\\n");
            List<String> result = new ArrayList<>();
            Collections.addAll(result, lines);
            return result;
        } catch (Exception e) {
            throw new AssetLoadException("Failed to read credits file", CREDITS_FILE_PATH, e);
        }
    }

    public BitmapFont getSmallFont() {
        return smallFont;
    }

    public BitmapFont getBodyFont() {
        return bodyFont;
    }

    public BitmapFont getSubtitleFont() {
        return subtitleFont;
    }

    public BitmapFont getTitleFont() {
        return titleFont;
    }

    void generateHeroIcons() {
        // Cold Storage Veteran — shield with heart
        textureRegistry.register(HERO_VETERAN, createHeroIcon(
            new Color(0.82f, 0.22f, 0.22f, 1f),
            new Color(0.95f, 0.60f, 0.30f, 1f),
            HeroIconType.HEART_SHIELD
        ));
        placeholderFlags.put(HERO_VETERAN, false);

        // FIFO Master — rotating arrows
        textureRegistry.register(HERO_FIFO, createHeroIcon(
            new Color(0.22f, 0.62f, 0.82f, 1f),
            new Color(0.95f, 0.85f, 0.30f, 1f),
            HeroIconType.ARROWS
        ));
        placeholderFlags.put(HERO_FIFO, false);

        // Produce Whisperer — leaf
        textureRegistry.register(HERO_PRODUCE, createHeroIcon(
            new Color(0.22f, 0.72f, 0.32f, 1f),
            new Color(0.60f, 0.95f, 0.50f, 1f),
            HeroIconType.LEAF
        ));
        placeholderFlags.put(HERO_PRODUCE, false);

        // Space Optimizer — grid
        textureRegistry.register(HERO_SPACE, createHeroIcon(
            new Color(0.55f, 0.35f, 0.82f, 1f),
            new Color(0.85f, 0.65f, 0.95f, 1f),
            HeroIconType.GRID
        ));
        placeholderFlags.put(HERO_SPACE, false);

        // Trap Hunter — eye
        textureRegistry.register(HERO_TRAP, createHeroIcon(
            new Color(0.82f, 0.62f, 0.15f, 1f),
            new Color(0.95f, 0.90f, 0.40f, 1f),
            HeroIconType.EYE
        ));
        placeholderFlags.put(HERO_TRAP, false);

        // Deep Freeze Specialist — crystal
        textureRegistry.register(HERO_FREEZE, createHeroIcon(
            new Color(0.22f, 0.55f, 0.82f, 1f),
            new Color(0.70f, 0.90f, 0.95f, 1f),
            HeroIconType.CRYSTAL
        ));
        placeholderFlags.put(HERO_FREEZE, false);

        // Reinforced Stock — anvil
        textureRegistry.register(HERO_REINFORCED, createHeroIcon(
            new Color(0.55f, 0.55f, 0.58f, 1f),
            new Color(0.85f, 0.85f, 0.88f, 1f),
            HeroIconType.ANVIL
        ));
        placeholderFlags.put(HERO_REINFORCED, false);

        // Time Bender — hourglass
        textureRegistry.register(HERO_TIME, createHeroIcon(
            new Color(0.75f, 0.45f, 0.15f, 1f),
            new Color(0.95f, 0.75f, 0.35f, 1f),
            HeroIconType.HOURGLASS
        ));
        placeholderFlags.put(HERO_TIME, false);
    }

    enum HeroIconType {
        HEART_SHIELD, ARROWS, LEAF, GRID, EYE, CRYSTAL, ANVIL, HOURGLASS
    }

    private Texture createHeroIcon(Color bgColor, Color accentColor, HeroIconType type) {
        int size = 128;
        Pixmap px = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        px.setBlending(Pixmap.Blending.None);

        // Background circle
        px.setColor(bgColor);
        drawCircle(px, size / 2, size / 2, size / 2 - 8);

        // Inner ring
        px.setColor(new Color(0.10f, 0.15f, 0.20f, 0.5f));
        drawCircleStroke(px, size / 2, size / 2, size / 2 - 14, 3);

        // Icon symbol
        px.setColor(accentColor);
        int cx = size / 2;
        int cy = size / 2;
        switch (type) {
            case HEART_SHIELD -> drawHeartShield(px, cx, cy);
            case ARROWS -> drawArrows(px, cx, cy);
            case LEAF -> drawLeaf(px, cx, cy);
            case GRID -> drawGrid(px, cx, cy);
            case EYE -> drawEye(px, cx, cy);
            case CRYSTAL -> drawCrystal(px, cx, cy);
            case ANVIL -> drawAnvil(px, cx, cy);
            case HOURGLASS -> drawHourglass(px, cx, cy);
        }

        Texture texture = new Texture(px, true);
        texture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        px.dispose();
        return texture;
    }

    private void drawCircle(Pixmap px, int cx, int cy, int r) {
        for (int y = -r; y <= r; y++) {
            for (int x = -r; x <= r; x++) {
                if (x * x + y * y <= r * r) {
                    px.drawPixel(cx + x, cy + y);
                }
            }
        }
    }

    private void drawCircleStroke(Pixmap px, int cx, int cy, int r, int thickness) {
        for (int y = -r - thickness; y <= r + thickness; y++) {
            for (int x = -r - thickness; x <= r + thickness; x++) {
                int dist = (int) Math.sqrt(x * x + y * y);
                if (Math.abs(dist - r) <= thickness / 2) {
                    px.drawPixel(cx + x, cy + y);
                }
            }
        }
    }

    private void drawHeartShield(Pixmap px, int cx, int cy) {
        // Shield outline
        px.setColor(new Color(0.95f, 0.85f, 0.30f, 1f));
        for (int y = -28; y <= 10; y++) {
            for (int x = -22; x <= 22; x++) {
                boolean inShield = (y < -5)
                    ? (x * x <= 22 * 22 && (y + 5) * (y + 5) + x * x <= 22 * 22 + 100)
                    : (Math.abs(x) <= 20 - (y + 5) * 0.4f);
                boolean inShieldInner = (y < -3)
                    ? (x * x <= 18 * 18 && (y + 5) * (y + 5) + x * x <= 18 * 18 + 80)
                    : (Math.abs(x) <= 16 - (y + 5) * 0.4f);
                if (inShield && !inShieldInner) {
                    px.drawPixel(cx + x, cy + y - 5);
                }
            }
        }
        // Heart inside
        px.setColor(new Color(0.95f, 0.30f, 0.30f, 1f));
        for (int y = -12; y <= 4; y++) {
            for (int x = -14; x <= 14; x++) {
                int dx1 = x + 5, dy1 = y + 4;
                int dx2 = x - 5, dy2 = y + 4;
                boolean lobe1 = (dx1 * dx1 + dy1 * dy1) <= 9 * 9;
                boolean lobe2 = (dx2 * dx2 + dy2 * dy2) <= 9 * 9;
                boolean bottom = (y > 0 && Math.abs(x) <= 5 - y);
                if (lobe1 || lobe2 || bottom) {
                    px.drawPixel(cx + x, cy + y);
                }
            }
        }
    }

    private void drawArrows(Pixmap px, int cx, int cy) {
        // Two curved arrows forming a cycle
        px.setColor(new Color(0.95f, 0.85f, 0.30f, 1f));
        // Top arrow (rightward)
        for (int a = 0; a < 160; a++) {
            double angle = Math.toRadians(a);
            int x = (int) (18 * Math.cos(angle));
            int y = (int) (12 * Math.sin(angle));
            px.drawPixel(cx + x, cy - 10 + y);
        }
        // Arrowhead top
        for (int dy = -4; dy <= 4; dy++) {
            for (int dx = 14; dx <= 20; dx++) {
                if (Math.abs(dy) <= 4 - (dx - 14)) {
                    px.drawPixel(cx + dx, cy - 10 + dy);
                }
            }
        }
        // Bottom arrow (leftward)
        for (int a = 20; a < 180; a++) {
            double angle = Math.toRadians(a);
            int x = (int) (18 * Math.cos(angle));
            int y = (int) (12 * Math.sin(angle));
            px.drawPixel(cx + x, cy + 10 + y);
        }
        // Arrowhead bottom
        for (int dy = -4; dy <= 4; dy++) {
            for (int dx = -20; dx <= -14; dx++) {
                if (Math.abs(dy) <= 4 - (14 + dx)) {
                    px.drawPixel(cx + dx, cy + 10 + dy);
                }
            }
        }
    }

    private void drawLeaf(Pixmap px, int cx, int cy) {
        px.setColor(new Color(0.60f, 0.95f, 0.50f, 1f));
        for (int y = -24; y <= 24; y++) {
            for (int x = -14; x <= 14; x++) {
                double nx = x / 14.0;
                double ny = y / 24.0;
                if (nx * nx + ny * ny * 0.6 <= 1.0) {
                    px.drawPixel(cx + x, cy + y);
                }
            }
        }
        // Vein
        px.setColor(new Color(0.30f, 0.70f, 0.25f, 1f));
        for (int y = -22; y <= 22; y++) {
            px.drawPixel(cx, cy + y);
        }
        // Side veins
        for (int y = -16; y <= 16; y += 8) {
            for (int x = 1; x <= 8; x++) {
                px.drawPixel(cx + x, cy + y - x);
                px.drawPixel(cx - x, cy + y - x);
            }
        }
    }

    private void drawGrid(Pixmap px, int cx, int cy) {
        px.setColor(new Color(0.85f, 0.65f, 0.95f, 1f));
        int cellSize = 12;
        int gap = 4;
        for (int row = -1; row <= 1; row++) {
            for (int col = -1; col <= 1; col++) {
                int x1 = cx + col * (cellSize + gap) - cellSize / 2;
                int y1 = cy + row * (cellSize + gap) - cellSize / 2;
                for (int dy = 0; dy < cellSize; dy++) {
                    for (int dx = 0; dx < cellSize; dx++) {
                        px.drawPixel(x1 + dx, y1 + dy);
                    }
                }
            }
        }
    }

    private void drawEye(Pixmap px, int cx, int cy) {
        // Eye shape
        px.setColor(new Color(0.95f, 0.90f, 0.40f, 1f));
        for (int y = -16; y <= 16; y++) {
            for (int x = -24; x <= 24; x++) {
                double nx = x / 24.0;
                double ny = y / 16.0;
                if (nx * nx + ny * ny <= 1.0) {
                    px.drawPixel(cx + x, cy + y);
                }
            }
        }
        // Iris
        px.setColor(new Color(0.20f, 0.50f, 0.80f, 1f));
        for (int y = -10; y <= 10; y++) {
            for (int x = -10; x <= 10; x++) {
                if (x * x + y * y <= 10 * 10) {
                    px.drawPixel(cx + x, cy + y);
                }
            }
        }
        // Pupil
        px.setColor(new Color(0.10f, 0.10f, 0.15f, 1f));
        for (int y = -4; y <= 4; y++) {
            for (int x = -4; x <= 4; x++) {
                if (x * x + y * y <= 4 * 4) {
                    px.drawPixel(cx + x, cy + y);
                }
            }
        }
        // Highlight
        px.setColor(new Color(1f, 1f, 1f, 1f));
        for (int y = -2; y <= 1; y++) {
            for (int x = -2; x <= 1; x++) {
                if (x * x + y * y <= 4) {
                    px.drawPixel(cx - 3, cy - 4 + y);
                }
            }
        }
    }

    private void drawCrystal(Pixmap px, int cx, int cy) {
        px.setColor(new Color(0.70f, 0.90f, 0.95f, 1f));
        // Hexagonal crystal
        int[] hx = {0, 14, 14, 0, -14, -14};
        int[] hy = {-24, -10, 10, 24, 10, -10};
        fillPolygon(px, cx, cy, hx, hy);
        // Inner facets
        px.setColor(new Color(0.40f, 0.70f, 0.85f, 0.6f));
        int[] ix = {0, 8, 0, -8};
        int[] iy = {-18, 0, 18, 0};
        fillPolygon(px, cx, cy, ix, iy);
        // Highlight
        px.setColor(new Color(1f, 1f, 1f, 0.5f));
        for (int y = -20; y <= -5; y++) {
            for (int x = -3; x <= 3; x++) {
                px.drawPixel(cx + x, cy + y);
            }
        }
    }

    private void fillPolygon(Pixmap px, int ox, int oy, int[] hx, int[] hy) {
        int minH = Integer.MAX_VALUE, maxH = Integer.MIN_VALUE;
        for (int h : hy) {
            minH = Math.min(minH, h);
            maxH = Math.max(maxH, h);
        }
        for (int y = minH; y <= maxH; y++) {
            int[] edges = new int[hx.length];
            int count = 0;
            for (int i = 0; i < hx.length; i++) {
                int j = (i + 1) % hx.length;
                if ((hy[i] <= y && hy[j] > y) || (hy[j] <= y && hy[i] > y)) {
                    float t = (float) (y - hy[i]) / (hy[j] - hy[i]);
                    edges[count++] = (int) (hx[i] + t * (hx[j] - hx[i]));
                }
            }
            java.util.Arrays.sort(edges, 0, count);
            for (int i = 0; i < count; i += 2) {
                for (int x = edges[i]; x <= edges[i + 1]; x++) {
                    px.drawPixel(ox + x, oy + y);
                }
            }
        }
    }

    private void drawAnvil(Pixmap px, int cx, int cy) {
        px.setColor(new Color(0.85f, 0.85f, 0.88f, 1f));
        // Top surface
        for (int y = -10; y <= -4; y++) {
            for (int x = -20; x <= 20; x++) {
                px.drawPixel(cx + x, cy + y);
            }
        }
        // Middle body
        for (int y = -4; y <= 8; y++) {
            for (int x = -14; x <= 14; x++) {
                px.drawPixel(cx + x, cy + y);
            }
        }
        // Base
        for (int y = 8; y <= 16; y++) {
            for (int x = -18; x <= 18; x++) {
                px.drawPixel(cx + x, cy + y);
            }
        }
        // Horn (left protrusion)
        for (int y = -10; y <= -4; y++) {
            for (int x = -28; x <= -20; x++) {
                if (Math.abs(x + 24) <= 8 - Math.abs(y + 7)) {
                    px.drawPixel(cx + x, cy + y);
                }
            }
        }
        // Highlight
        px.setColor(new Color(1f, 1f, 1f, 0.4f));
        for (int x = -18; x <= 18; x++) {
            px.drawPixel(cx + x, cy - 10);
        }
    }

    private void drawHourglass(Pixmap px, int cx, int cy) {
        px.setColor(new Color(0.95f, 0.75f, 0.35f, 1f));
        // Glass outline
        for (int y = -24; y <= 24; y++) {
            int width;
            if (Math.abs(y) <= 2) {
                width = 3;
            } else if (y < 0) {
                width = 18 - (24 + y) * 14 / 24;
            } else {
                width = 18 - y * 14 / 24;
            }
            for (int x = -width; x <= width; x++) {
                boolean edge = (x == -width || x == width);
                if (edge) {
                    px.drawPixel(cx + x, cy + y);
                }
            }
        }
        // Sand (top)
        px.setColor(new Color(0.80f, 0.55f, 0.20f, 1f));
        for (int y = -22; y <= -6; y++) {
            int w = 14 - (22 + y) * 10 / 16;
            for (int x = -w; x <= w; x++) {
                px.drawPixel(cx + x, cy + y);
            }
        }
        // Sand (bottom)
        for (int y = 6; y <= 22; y++) {
            int w = 14 - (22 - y) * 10 / 16;
            for (int x = -w; x <= w; x++) {
                px.drawPixel(cx + x, cy + y);
            }
        }
        // Sand stream
        px.setColor(new Color(0.70f, 0.50f, 0.15f, 1f));
        for (int y = -4; y <= 4; y++) {
            px.drawPixel(cx - 1, cy + y);
            px.drawPixel(cx + 1, cy + y);
        }
        // Frame top/bottom
        px.setColor(new Color(0.95f, 0.75f, 0.35f, 1f));
        for (int x = -20; x <= 20; x++) {
            px.drawPixel(cx + x, cy - 24);
            px.drawPixel(cx + x, cy + 24);
        }
    }

    public void dispose() {
        for (Texture texture : textureRegistry.getAll().values()) {
            if (texture != null) {
                texture.dispose();
            }
        }
        textureRegistry.clear();
        disposeFonts();
    }
}
