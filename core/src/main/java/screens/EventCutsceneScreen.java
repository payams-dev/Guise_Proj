package screens;

import audio.CinematicAudio;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Runs a timed cinematic event from assets/story/events.json.
 *
 * Supported step types: wait, image, dialogue, logo_reveal, open_screen.
 * Tags and IDs are retained as event metadata for future expansion.
 */
public final class EventCutsceneScreen extends ScreenAdapter {

    private static final String EVENT_FILE = "story/events.json";
    private static final float WORLD_WIDTH = 720f;
    private static final float WORLD_HEIGHT = 480f;

    // Must match MenuAnimationTestScreen's current logo layout.
    private static final float TITLE_LOGO_WIDTH_RATIO = 0.42f;
    private static final float TITLE_LOGO_MAX_HEIGHT_RATIO = 0.28f;
    private static final float TITLE_LOGO_TOP_RATIO = 0.93f;

    private final Game game;
    private final SpriteBatch batch = new SpriteBatch();
    private final FitViewport viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
    private final GlyphLayout glyphLayout = new GlyphLayout();
    private final ObjectMap<String, Texture> images = new ObjectMap<>();
    private final ObjectMap<String, BitmapFont> fonts = new ObjectMap<>();
    private final Array<Step> steps = new Array<>();
    private final Texture whitePixel;
    private CinematicAudio audio;
    private boolean audioTransferred;

    private boolean bootIntro;
    private boolean skippingMonologue;
    private float skipElapsed;
    private float skipStartAlpha;
    private int skipTarget;
    private static final float MONOLOGUE_SKIP_FADE_SECONDS = 0.9f;
    private InputProcessor previousInput;
    private final InputAdapter skipInput = new InputAdapter() {
        @Override public boolean keyDown(int keycode) { return requestMonologueSkip(); }
        @Override public boolean touchDown(int x, int y, int pointer, int button) {
            return requestMonologueSkip();
        }
    };

    @Override
    public void show() {
        previousInput = Gdx.input.getInputProcessor();
        Gdx.input.setInputProcessor(skipInput);
    }

    @Override
    public void hide() {
        if (Gdx.input.getInputProcessor() == skipInput) {
            Gdx.input.setInputProcessor(previousInput);
        }
        previousInput = null;
    }

    private boolean requestMonologueSkip() {
        if (skippingMonologue) return true; // Repeated presses cannot restart the fade.
        if (!bootIntro || stepIndex >= steps.size) return false;
        Step current = steps.get(stepIndex);
        if (!"dialogue".equals(current.type) || !current.tags.contains("prophecy", false)) {
            return false;
        }
        for (int i = stepIndex + 1; i < steps.size; i++) {
            if (!"logo_reveal".equals(steps.get(i).type)) continue;
            skipTarget = i;
            skipStartAlpha = fadeAlpha(current, stepElapsed);
            skipElapsed = 0f;
            skippingMonologue = true;
            audio.fade("prophecy_wind", 0f, MONOLOGUE_SKIP_FADE_SECONDS);
            return true;
        }
        return false;
    }

    /** Freeze the current card while it fades; never fire skipped narration cues. */
    private boolean advanceMonologueSkip(float elapsed) {
        if (!skippingMonologue) return false;
        skipElapsed += elapsed;
        if (skipElapsed >= MONOLOGUE_SKIP_FADE_SECONDS) {
            audio.stop("prophecy_wind");
            stepIndex = skipTarget;
            stepElapsed = 0f;
            skippingMonologue = false;
        }
        return true;
    }

    private int stepIndex = 0;
    private float stepElapsed = 0f;

    private static final class Step {
        String id;
        String type;
        String asset;
        String fontPath = "fonts/Post Antiqua BE Regular.ttf";
        String screenId;
        String textColor = "#EDE4D5";
        final Array<String> lines = new Array<>();
        final Array<String> tags = new Array<>();
        final Array<AudioCue> audioCues = new Array<>();

        float seconds = 0f;
        float fadeIn = 0f;
        float hold = 0f;
        float fadeOut = 0f;
        float black = 0f;
        float slide = 0f;
        float flash = 0f;
        float widthRatio = 0.55f;
        float maxHeightRatio = 0.38f;
        float fontSizeRatio = 0.047f;
        float maxTextWidthRatio = 0.86f;
        float lineSpacingMultiplier = 1.35f;

        float duration() {
            if ("wait".equals(type)) return seconds;
            if ("image".equals(type) || "dialogue".equals(type)) {
                return fadeIn + hold + fadeOut + black;
            }
            if ("logo_reveal".equals(type)) {
                return fadeIn + hold + slide + flash;
            }
            if ("open_screen".equals(type)) return 0f;
            throw new GdxRuntimeException("Unknown cutscene step type: " + type + " (" + id + ")");
        }
    }

    /** Cue times are relative to a step, not to the entire cinematic. */
    private static final class AudioCue {
        String at;
        String action;
        String track;
        float offsetSeconds;
        float sourceSeconds;
        float toVolume;
        float durationSeconds;
        boolean fired;

        float triggerTime(Step step) {
            float time;
            switch (at) {
                case "start": time = 0f; break;
                case "fadeOutStart": time = step.fadeIn + step.hold; break;
                case "flashStart": time = step.fadeIn + step.hold + step.slide; break;
                case "end": time = step.duration(); break;
                default: throw new GdxRuntimeException("Unknown audio timing: " + at);
            }
            return Math.max(0f, time + offsetSeconds);
        }
    }

    /** routeOrEventId may be "onBoot" or a named event such as "boot_intro". */
    public EventCutsceneScreen(Game game, String routeOrEventId) {
        this.game = game;

        Pixmap onePixel = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        onePixel.setColor(1f, 1f, 1f, 1f);
        onePixel.fill();
        whitePixel = new Texture(onePixel);
        onePixel.dispose();

        loadEvent(routeOrEventId);

        // Prepare the images and fonts once, before the cinematic begins.
        // This avoids a texture/font-generation hitch between narration cards.
        for (Step step : steps) {
            if ("image".equals(step.type) || "logo_reveal".equals(step.type)) {
                image(step.asset);
            } else if ("dialogue".equals(step.type)) {
                font(step);
            }
        }

        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    private void loadEvent(String routeOrEventId) {
        JsonObject document = JsonParser.parseString(
            Gdx.files.internal(EVENT_FILE).readString("UTF-8")
        ).getAsJsonObject();

        String eventId = routeOrEventId;
        JsonObject routes = document.getAsJsonObject("routes");
        if (routes != null && routes.has(routeOrEventId)
                && !routes.get(routeOrEventId).isJsonNull()) {
            eventId = routes.get(routeOrEventId).getAsString();
        }

        bootIntro = "boot_intro".equals(eventId);

        // Every audio asset is loaded before the first cinematic frame.
        audio = new CinematicAudio(document.getAsJsonObject("audio"));

        JsonObject allEvents = document.getAsJsonObject("events");
        JsonObject event = allEvents == null ? null : allEvents.getAsJsonObject(eventId);
        if (event == null) {
            throw new GdxRuntimeException("Cutscene event not found: " + eventId);
        }

        JsonArray jsonSteps = event.getAsJsonArray("steps");
        if (jsonSteps == null || jsonSteps.size() == 0) {
            throw new GdxRuntimeException("Cutscene has no steps: " + eventId);
        }

        for (JsonElement item : jsonSteps) {
            JsonObject json = item.getAsJsonObject();
            Step s = new Step();
            s.id = string(json, "id", "unnamed_step");
            s.type = string(json, "type", "");
            s.asset = string(json, "asset", null);
            s.fontPath = string(json, "font", s.fontPath);
            s.screenId = string(json, "screenId", null);
            s.textColor = string(json, "textColor", s.textColor);
            s.seconds = number(json, "seconds", 0f);
            s.fadeIn = number(json, "fadeInSeconds", 0f);
            s.hold = number(json, "holdSeconds", 0f);
            s.fadeOut = number(json, "fadeOutSeconds", 0f);
            s.black = number(json, "blackSeconds", 0f);
            s.slide = number(json, "slideSeconds", 0f);
            s.flash = number(json, "flashSeconds", 0f);
            s.widthRatio = number(json, "widthRatio", s.widthRatio);
            s.maxHeightRatio = number(json, "maxHeightRatio", s.maxHeightRatio);
            s.fontSizeRatio = number(json, "fontSizeRatio", s.fontSizeRatio);
            s.maxTextWidthRatio = number(json, "maxTextWidthRatio", s.maxTextWidthRatio);
            s.lineSpacingMultiplier = number(json, "lineSpacingMultiplier", s.lineSpacingMultiplier);

            JsonArray jsonLines = json.getAsJsonArray("lines");
            if (jsonLines != null) {
                for (JsonElement line : jsonLines) s.lines.add(line.getAsString());
            }
            JsonArray jsonTags = json.getAsJsonArray("tags");
            if (jsonTags != null) {
                for (JsonElement tag : jsonTags) s.tags.add(tag.getAsString());
            }

            JsonArray cues = json.getAsJsonArray("audioCues");
            if (cues != null) {
                for (JsonElement itemCue : cues) {
                    JsonObject jc = itemCue.getAsJsonObject();
                    AudioCue cue = new AudioCue();
                    cue.at = string(jc, "at", "start");
                    cue.action = string(jc, "action", "");
                    cue.track = string(jc, "track", "");
                    cue.offsetSeconds = number(jc, "offsetSeconds", 0f);
                    cue.sourceSeconds = number(jc, "sourceSeconds", 0f);
                    cue.toVolume = number(jc, "toVolume", 0f);
                    cue.durationSeconds = number(jc, "durationSeconds", 0f);
                    if (cue.triggerTime(s) > s.duration() + 0.0001f) {
                        throw new GdxRuntimeException("Audio cue after step end in " + s.id);
                    }
                    s.audioCues.add(cue);
                }
            }

            if (("image".equals(s.type) || "logo_reveal".equals(s.type))
                    && s.asset == null) {
                throw new GdxRuntimeException("Missing image asset for " + s.id);
            }
            if (s.duration() < 0f) {
                throw new GdxRuntimeException("Negative duration in " + s.id);
            }
            steps.add(s);
        }
    }

    private static String string(JsonObject json, String key, String fallback) {
        return json.has(key) && !json.get(key).isJsonNull()
            ? json.get(key).getAsString() : fallback;
    }

    private static float number(JsonObject json, String key, float fallback) {
        return json.has(key) && !json.get(key).isJsonNull()
            ? json.get(key).getAsFloat() : fallback;
    }

    private Texture image(String path) {
        Texture loaded = images.get(path);
        if (loaded != null) return loaded;

        loaded = new Texture(Gdx.files.internal(path));
        loaded.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        images.put(path, loaded);
        return loaded;
    }

    private BitmapFont font(Step step) {
        String key = step.fontPath + "@" + step.fontSizeRatio;
        BitmapFont loaded = fonts.get(key);
        if (loaded != null) return loaded;

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
            Gdx.files.internal(step.fontPath)
        );
        try {
            FreeTypeFontParameter params = new FreeTypeFontParameter();
            params.size = Math.max(12, Math.round(WORLD_HEIGHT * step.fontSizeRatio));
            // Include the curly quotation marks used in the prophecy.
            params.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "‘’“”—©";
            params.minFilter = Texture.TextureFilter.Linear;
            params.magFilter = Texture.TextureFilter.Linear;
            loaded = generator.generateFont(params);
            fonts.put(key, loaded);
            return loaded;
        } finally {
            generator.dispose();
        }
    }

    private static float smooth(float value) {
        float t = MathUtils.clamp(value, 0f, 1f);
        return t * t * (3f - 2f * t);
    }

    private static float fadeAlpha(Step s, float time) {
        if (s.fadeIn > 0f && time < s.fadeIn) {
            return smooth(time / s.fadeIn);
        }
        float fadeOutStart = s.fadeIn + s.hold;
        if (time < fadeOutStart) return 1f;
        if (s.fadeOut > 0f && time < fadeOutStart + s.fadeOut) {
            return 1f - smooth((time - fadeOutStart) / s.fadeOut);
        }
        return 0f; // blackSeconds, or the end of this step
    }

    /** Dispatch any cues that should have fired by the given step-local time. */
    private void dispatchAudioCues(Step step, float time) {
        for (AudioCue cue : step.audioCues) {
            if (cue.fired || time + 0.0001f < cue.triggerTime(step)) continue;
            cue.fired = true;
            Gdx.app.log("GUISE Audio", step.id + " @ " + cue.triggerTime(step)
                + "s: " + cue.action + " " + cue.track);
            switch (cue.action) {
                case "play":
                    audio.play(cue.track, cue.sourceSeconds);
                    break;
                case "fade":
                    audio.fade(cue.track, cue.toVolume, cue.durationSeconds);
                    break;
                case "stop":
                    audio.stop(cue.track);
                    break;
                default:
                    throw new GdxRuntimeException("Unknown audio action '"
                        + cue.action + "' in step " + step.id);
            }
        }
    }

    @Override
    public void render(float delta) {
        float elapsed = Math.max(0f, delta);
        audio.update(elapsed);
        if (!advanceMonologueSkip(elapsed)) stepElapsed += elapsed;

        // Process cues on *every* crossed step, including during a frame hitch.
        while (!skippingMonologue && stepIndex < steps.size) {
            Step current = steps.get(stepIndex);
            if ("open_screen".equals(current.type)) {
                openScreen(current);
                return;
            }
            float duration = current.duration();
            dispatchAudioCues(current, Math.min(stepElapsed, duration));
            if (stepElapsed < duration) break;
            stepElapsed -= duration;
            stepIndex++;
        }
        if (stepIndex >= steps.size) {
            throw new GdxRuntimeException("Cutscene ended without an open_screen step");
        }

        ScreenUtils.clear(0f, 0f, 0f, 1f);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.setColor(1f, 1f, 1f, 1f);
        batch.begin();

        Step step = steps.get(stepIndex);
        if ("image".equals(step.type)) {
            drawImage(step);
        } else if ("dialogue".equals(step.type)) {
            drawDialogue(step);
        } else if ("logo_reveal".equals(step.type)) {
            drawLogoReveal(step);
        }

        batch.setColor(1f, 1f, 1f, 1f);
        batch.end();
    }

    private void drawImage(Step step) {
        float alpha = fadeAlpha(step, stepElapsed);
        if (alpha <= 0f) return;

        Texture texture = image(step.asset);
        float aspect = (float) texture.getWidth() / texture.getHeight();
        float width = Math.min(
            WORLD_WIDTH * step.widthRatio,
            WORLD_HEIGHT * step.maxHeightRatio * aspect
        );
        float height = width / aspect;
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(texture,
            (WORLD_WIDTH - width) / 2f,
            (WORLD_HEIGHT - height) / 2f,
            width, height
        );
    }

    private void drawDialogue(Step step) {
        float alpha = skippingMonologue
            ? skipStartAlpha * (1f - smooth(skipElapsed / MONOLOGUE_SKIP_FADE_SECONDS))
            : fadeAlpha(step, stepElapsed);
        if (alpha <= 0f || step.lines.size == 0) return;

        BitmapFont text = font(step);
        text.getData().setScale(1f);

        // Preserve all the authored line breaks; shrink only if a line is too wide.
        float longestLine = 0f;
        for (String line : step.lines) {
            glyphLayout.setText(text, line);
            longestLine = Math.max(longestLine, glyphLayout.width);
        }
        float maxWidth = WORLD_WIDTH * step.maxTextWidthRatio;
        if (longestLine > maxWidth) {
            text.getData().setScale(maxWidth / longestLine);
        }

        com.badlogic.gdx.graphics.Color color =
            com.badlogic.gdx.graphics.Color.valueOf(step.textColor);
        text.setColor(color.r, color.g, color.b, alpha * color.a);

        float lineHeight = text.getLineHeight() * step.lineSpacingMultiplier;
        float baseline = WORLD_HEIGHT * 0.5f
            + (step.lines.size - 1) * lineHeight * 0.5f
            + text.getCapHeight() * 0.5f;

        for (String line : step.lines) {
            glyphLayout.setText(text, line);
            float x = (WORLD_WIDTH - glyphLayout.width) / 2f;
            text.draw(batch, line, x, baseline);
            baseline -= lineHeight;
        }
    }

    private void drawLogoReveal(Step step) {
        Texture texture = image(step.asset);
        float aspect = (float) texture.getWidth() / texture.getHeight();
        float width = Math.min(
            WORLD_WIDTH * TITLE_LOGO_WIDTH_RATIO,
            WORLD_HEIGHT * TITLE_LOGO_MAX_HEIGHT_RATIO * aspect
        );
        float height = width / aspect;

        float startY = (WORLD_HEIGHT - height) / 2f;
        float finalY = WORLD_HEIGHT * TITLE_LOGO_TOP_RATIO - height;
        float alpha = step.fadeIn <= 0f
            ? 1f : smooth(stepElapsed / step.fadeIn);

        float slideStart = step.fadeIn + step.hold;
        float slideProgress = step.slide <= 0f
            ? 1f : smooth((stepElapsed - slideStart) / step.slide);
        float y = MathUtils.lerp(startY, finalY, slideProgress);
        float x = (WORLD_WIDTH - width) / 2f;

        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(texture, x, y, width, height);

        // White flash occurs only AFTER the logo has reached its menu position.
        float flashStart = slideStart + step.slide;
        if (stepElapsed >= flashStart) {
            float flashProgress = step.flash <= 0f
                ? 1f : (stepElapsed - flashStart) / step.flash;
            float flashAlpha = smooth(flashProgress * 2f);
            batch.setColor(1f, 1f, 1f, flashAlpha);
            batch.draw(whitePixel, 0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);
        }
    }

    private void openScreen(Step step) {
        if (!"main_title".equals(step.screenId)) {
            throw new GdxRuntimeException("Unsupported screenId: " + step.screenId);
        }
        // Load menu frames first. Start the audio only when the menu is ready,
        // otherwise audio may get ahead during 54-texture initialization.
        MenuAnimationTestScreen title = new MenuAnimationTestScreen(audio);
        dispatchAudioCues(step, 0f);
        // The menu takes ownership of ALL tracks: the thunder tail is preserved.
        audioTransferred = true;
        game.setScreen(title);
        dispose(); // Game.setScreen() does not automatically dispose the old screen.
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        hide();
        if (!audioTransferred && audio != null) audio.dispose();
        batch.dispose();
        whitePixel.dispose();
        for (Texture texture : images.values()) texture.dispose();
        for (BitmapFont text : fonts.values()) text.dispose();
    }
}
