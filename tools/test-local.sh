#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
# Offline fallback for a machine with the pinned jars already downloaded.
# Normal build: ./gradlew core:test
DEPS="${GUISE_TEST_DEPS:-/tmp}"
OUT="${GUISE_TEST_OUTPUT:-build/local-tests}"
mkdir -p "$OUT/main" "$OUT/test"
find core/src/main/java -name '*.java' > "$OUT/sources.txt"
java com.sun.tools.javac.Main --release 11 -encoding UTF-8 -cp "$DEPS/guise-gdx.jar:$DEPS/guise-gson.jar" -d "$OUT/main" @"$OUT/sources.txt"
java com.sun.tools.javac.Main --release 11 -encoding UTF-8 -cp "$OUT/main:$DEPS/guise-gdx.jar:$DEPS/guise-gson.jar:$DEPS/guise-junit.jar" -d "$OUT/test" core/src/test/java/slice/*.java
java -Dguise.assets=assets -jar "$DEPS/guise-junit.jar" execute --class-path "$OUT/main:$OUT/test:$DEPS/guise-gdx.jar:$DEPS/guise-gson.jar" --scan-class-path --disable-banner --details=summary --reports-dir "$OUT/results"
