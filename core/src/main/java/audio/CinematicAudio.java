package audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Audio shared by the JSON cutscene and the animated title screen.
 * Music streams long recordings; Sound plays short one-shot effects.
 * The title screen becomes the owner when the intro transitions to it.
 */
public final class CinematicAudio implements AutoCloseable {

    private static final String EVENT_FILE = "story/events.json";

    private static final class Track {
        String id;
        Music music;
        Sound sound;
        float baseVolume;
        float currentVolume;
        boolean looping;
        boolean fading;
        float fadeFrom;
        float fadeTo;
        float fadeDuration;
        float fadeElapsed;
    }

    private final ObjectMap<String, Track> tracks = new ObjectMap<>();
    private boolean disposed;

    /** Used when running the title screen directly during development. */
    public CinematicAudio() {
        this(JsonParser.parseString(
            Gdx.files.internal(EVENT_FILE).readString("UTF-8")
        ).getAsJsonObject().getAsJsonObject("audio"));
    }

    /** Load the same audio catalog already parsed by the event system. */
    public CinematicAudio(JsonObject catalog) {
        if (catalog == null) {
            throw new GdxRuntimeException("Missing 'audio' catalog in " + EVENT_FILE);
        }
        try {
            for (java.util.Map.Entry<String, JsonElement> entry : catalog.entrySet()) {
                String id = entry.getKey();
                JsonObject json = entry.getValue().getAsJsonObject();
                String path = required(json, "file");
                String type = required(json, "type");
                Track track = new Track();
                track.id = id;
                track.baseVolume = json.has("volume") ? json.get("volume").getAsFloat() : 1f;
                if (track.baseVolume < 0f || track.baseVolume > 1f) {
                    throw new GdxRuntimeException("Volume out of range [0,1] for " + id);
                }
                track.currentVolume = track.baseVolume;
                track.looping = json.has("loop") && json.get("loop").getAsBoolean();

                if (!Gdx.files.internal(path).exists()) {
                    throw new GdxRuntimeException("Missing audio file for " + id + ": " + path);
                }
                if ("music".equals(type)) {
                    track.music = Gdx.audio.newMusic(Gdx.files.internal(path));
                    track.music.setLooping(track.looping);
                    track.music.setVolume(track.baseVolume);
                } else if ("sound".equals(type)) {
                    track.sound = Gdx.audio.newSound(Gdx.files.internal(path));
                } else {
                    throw new GdxRuntimeException("Unknown audio type for " + id + ": " + type);
                }
                tracks.put(id, track);
            }
        } catch (RuntimeException failure) {
            dispose(); // release any successfully loaded tracks before rethrowing
            throw failure;
        }
    }

    private static String required(JsonObject json, String key) {
        if (!json.has(key) || json.get(key).isJsonNull()) {
            throw new GdxRuntimeException("Audio entry missing: " + key);
        }
        return json.get(key).getAsString();
    }

    private Track track(String id) {
        if (disposed) throw new GdxRuntimeException("Audio has already been disposed");
        Track track = tracks.get(id);
        if (track == null) throw new GdxRuntimeException("Unknown audio track: " + id);
        return track;
    }

    /** Play a sound once, or start/restart a streaming track. */
    public void play(String id) {
        play(id, 0f);
    }

    /** sourceSeconds skips into MUSIC files. For Sound, use an edited source file. */
    public void play(String id, float sourceSeconds) {
        Track t = track(id);
        t.fading = false;
        t.currentVolume = t.baseVolume;
        if (t.music != null) {
            t.music.stop();
            t.music.setVolume(t.currentVolume);
            t.music.setLooping(t.looping);
            t.music.play();
            if (sourceSeconds > 0f) t.music.setPosition(sourceSeconds);
        } else {
            if (sourceSeconds != 0f) {
                throw new GdxRuntimeException("Cannot seek a Sound: " + id
                    + "; trim the file or set sourceSeconds to zero");
            }
            t.sound.play(t.baseVolume);
        }
    }

    /** Begin volume fade on a streaming Music track. */
    public void fade(String id, float toVolume, float seconds) {
        Track t = track(id);
        if (t.music == null) throw new GdxRuntimeException("Cannot fade Sound track: " + id);
        if (toVolume < 0f || toVolume > 1f || seconds < 0f) {
            throw new GdxRuntimeException("Invalid fade values for " + id);
        }
        t.fadeFrom = t.currentVolume;
        t.fadeTo = toVolume;
        t.fadeDuration = seconds;
        t.fadeElapsed = 0f;
        t.fading = seconds > 0f;
        if (!t.fading) {
            t.currentVolume = toVolume;
            t.music.setVolume(toVolume);
        }
    }

    public void stop(String id) {
        Track t = track(id);
        t.fading = false;
        if (t.music != null) t.music.stop();
        else t.sound.stop();
    }

    /** Call once per render frame, including after transfer to the main menu. */
    public void update(float delta) {
        if (disposed) return;
        for (Track t : tracks.values()) {
            if (!t.fading || t.music == null) continue;
            t.fadeElapsed += Math.max(0f, delta);
            float p = t.fadeDuration == 0f ? 1f
                : Math.min(1f, t.fadeElapsed / t.fadeDuration);
            float eased = p * p * (3f - 2f * p);
            t.currentVolume = t.fadeFrom + (t.fadeTo - t.fadeFrom) * eased;
            t.music.setVolume(t.currentVolume);
            if (p >= 1f) t.fading = false;
        }
    }

    @Override
    public void close() {
        dispose();
    }

    public void dispose() {
        if (disposed) return;
        disposed = true;
        for (Track t : tracks.values()) {
            if (t.music != null) t.music.dispose();
            if (t.sound != null) t.sound.dispose();
        }
        tracks.clear();
    }
}
