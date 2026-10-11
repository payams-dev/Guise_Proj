package screens;

import audio.CinematicAudio;

import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class MenuAnimationTestScreen extends ScreenAdapter {

    private static final int FRAME_COUNT = 54;
    private static final float FRAME_DURATION = 1f / 15f;

    private static final String COPYRIGHT =
        "\u00A9 Placeholder Corporation 2026, published by Placeholder";

    private static final String PROMPT =
        "P R E S S   A N Y   B U T T O N";

    // One complete fade-in/fade-out cycle.
    private static final float PULSE_DURATION = 2.8f;

    // Title-screen layout: easy to adjust after each visual test.
    private static final float COPYRIGHT_Y = 0.985f;
    private static final float LOGO_TOP_Y = 0.93f;
    private static final float LOGO_WIDTH_RATIO = 0.42f;
    private static final float LOGO_MAX_HEIGHT_RATIO = 0.28f;
    private static final float PROMPT_Y = 0.14f;

    private final CinematicAudio audio;
    private final SpriteBatch batch;
    private final FitViewport viewport;
    private final Array<Texture> frameTextures;
    private final Animation<TextureRegion> animation;
    private final Texture logo;
    private final BitmapFont copyrightFont;
    private final BitmapFont promptFont;
    private final GlyphLayout layout;

    private float elapsedTime = 0f;

    /** Direct-launch test: play the menu audio without needing a boot cutscene. */
    public MenuAnimationTestScreen() {
        this(new CinematicAudio(), true);
    }

    /** Called by EventCutsceneScreen, which transfers the playing audio here. */
    public MenuAnimationTestScreen(CinematicAudio audio) {
        this(audio, false);
    }

    private MenuAnimationTestScreen(CinematicAudio audio, boolean standalone) {
        this.audio = audio;
        if (standalone) {
            audio.play("city_ambience");
            audio.play("main_menu_theme");
        }
        batch = new SpriteBatch();

        frameTextures = new Array<>();
        Array<TextureRegion> regions = new Array<>();

        // Load the 54 pixelated prerendered frames.
        for (int i = 0; i < FRAME_COUNT; i++) {
            String path = String.format(
                Locale.ROOT,
                "menu/title/frames/Menu_Frames/%04d.pixel-24bit.png",
                i
            );

            Texture texture = new Texture(Gdx.files.internal(path));
            texture.setFilter(
                Texture.TextureFilter.Nearest,
                Texture.TextureFilter.Nearest
            );

            frameTextures.add(texture);
            regions.add(new TextureRegion(texture));
        }

        Texture first = frameTextures.first();
        viewport = new FitViewport(first.getWidth(), first.getHeight());
        animation = new Animation<>(
            FRAME_DURATION,
            regions,
            Animation.PlayMode.LOOP
        );

        logo = new Texture(Gdx.files.internal("menu/title/GROOVE_LOGO.png"));
        logo.setFilter(
            Texture.TextureFilter.Nearest,
            Texture.TextureFilter.Nearest
        );

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
            Gdx.files.internal("fonts/Post Antiqua BE Regular.ttf")
        );

        float screenHeight = viewport.getWorldHeight();
        float screenWidth = viewport.getWorldWidth();

        // A smaller, more discreet copyright line.
        FreeTypeFontParameter smallParams = new FreeTypeFontParameter();
        smallParams.size = Math.max(12, Math.round(screenHeight * 0.018f));
        smallParams.characters =
            FreeTypeFontGenerator.DEFAULT_CHARS + "\u00A9";
        smallParams.minFilter = Texture.TextureFilter.Linear;
        smallParams.magFilter = Texture.TextureFilter.Linear;
        copyrightFont = generator.generateFont(smallParams);

        FreeTypeFontParameter promptParams = new FreeTypeFontParameter();
        promptParams.size = Math.max(20, Math.round(screenHeight * 0.044f));
        promptParams.minFilter = Texture.TextureFilter.Linear;
        promptParams.magFilter = Texture.TextureFilter.Linear;
        promptFont = generator.generateFont(promptParams);

        generator.dispose();

        layout = new GlyphLayout();
        fitText(copyrightFont, COPYRIGHT, screenWidth * 0.94f);
        fitText(promptFont, PROMPT, screenWidth * 0.90f);

        viewport.update(
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight(),
            true
        );
    }

    private void fitText(BitmapFont font, String text, float maximumWidth) {
        layout.setText(font, text);
        if (layout.width > maximumWidth) {
            font.getData().setScale(maximumWidth / layout.width);
        }
    }

    private void drawCentered(BitmapFont font, String text, float y) {
        layout.setText(font, text);
        float x = (viewport.getWorldWidth() - layout.width) / 2f;
        font.draw(batch, text, x, y);
    }

    @Override
    public void render(float delta) {
        audio.update(delta);
        elapsedTime += delta;

        ScreenUtils.clear(0f, 0f, 0f, 1f);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        float width = viewport.getWorldWidth();
        float height = viewport.getWorldHeight();

        batch.begin();

        // 1. Animated alley background at 15 FPS.
        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(
            animation.getKeyFrame(elapsedTime),
            0f, 0f,
            width, height
        );

        // 2. Small copyright line, raised toward the top edge.
        copyrightFont.setColor(0.88f, 0.85f, 0.79f, 0.90f);
        drawCentered(copyrightFont, COPYRIGHT, height * COPYRIGHT_Y);

        // 3. Larger logo, also raised, with its aspect ratio preserved.
        float aspect = (float) logo.getWidth() / logo.getHeight();
        float logoWidth = Math.min(
            width * LOGO_WIDTH_RATIO,
            height * LOGO_MAX_HEIGHT_RATIO * aspect
        );
        float logoHeight = logoWidth / aspect;
        float logoX = (width - logoWidth) / 2f;
        float logoY = height * LOGO_TOP_Y - logoHeight;

        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(logo, logoX, logoY, logoWidth, logoHeight);

        // 4. Gently materializing / dematerializing button prompt.
        float phase = elapsedTime * MathUtils.PI2 / PULSE_DURATION;
        float pulse = (MathUtils.sin(phase) + 1f) * 0.5f;
        pulse = pulse * pulse * (3f - 2f * pulse); // Smoothstep.
        float alpha = 0.04f + 0.96f * pulse;

        promptFont.setColor(0.94f, 0.90f, 0.83f, alpha);
        drawCentered(promptFont, PROMPT, height * PROMPT_Y);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        audio.dispose();
        batch.dispose();
        logo.dispose();
        copyrightFont.dispose();
        promptFont.dispose();

        for (Texture texture : frameTextures) {
            texture.dispose();
        }
    }
}
