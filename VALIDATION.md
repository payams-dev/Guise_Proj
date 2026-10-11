# Reproducing validation

Normal project tasks: `./gradlew core:test` and `./gradlew lwjgl3:jar`. Gradle keeps the original Java 21 daemon configuration and Java 11 source target.

The supplied environment could not fetch the Gradle distribution through Java's direct network connection. The fallback compiled the real source with `java com.sun.tools.javac.Main --release 11` and ran JUnit Platform Console 1.10.2. It did not substitute LibGDX stubs.

`tools/test-local.sh` accepts `GUISE_TEST_DEPS` (default `/tmp`) containing `guise-gdx.jar` (1.14.0), `guise-gson.jar` (2.14.0), and `guise-junit.jar` (JUnit Console standalone 1.10.2). It compiles all core source and all seven test classes, then writes JUnit XML reports to `build/local-tests/results`.

`python tools/package-local.py /path/to/dependencies` additionally reads `guise-libs/*.jar`. The validated runtime set was gdx-backend-lwjgl3 1.14.0; gdx-platform 1.14.0 natives-desktop; gdx-jnigen-loader 2.5.2; LWJGL core/glfw/jemalloc/openal/opengl/stb 3.4.1 plus Linux x64/Windows x64 natives; jlayer 1.0.1-gdx; jorbis 0.0.17. The JAR contains input SHA-256 values in `META-INF/GUISE-runtime-inputs.json` and included license/notice files. This fallback is not a replacement for the existing Gradle build.

Compile the opt-in desktop smoke class against the finished JAR:

```
java com.sun.tools.javac.Main --release 11 -implicit:none -sourcepath lwjgl3/src/test/java -cp dist/GUISE-Band-A.jar -d build/desktop-smoke lwjgl3/src/test/java/screens/DesktopSmoke.java
java -cp "dist/GUISE-Band-A.jar:build/desktop-smoke" screens.DesktopSmoke
```

On Windows, use `;` between classpath entries. Run with a desktop display and from a directory without loose game assets to exercise packaged resources; adjust both classpath entries to absolute paths in that case. `-Dguise.smoke.output=/path/to/screenshots` controls capture location. The harness uses actual input handlers and rendering, resizes the window, asserts key state changes, and exits after checking the legacy entry and terminal Judgment guard.

The Linux validation used a temporary Xvfb X11 display and `LIBGL_ALWAYS_SOFTWARE=1`, `ALSOFT_DRIVERS=null`. These are validation-environment settings, not requirements or defaults for players.
