# GUISE — Band A playable vertical slice

This is the existing project with the approved Band A experiment implemented. The original 1v1 battle remains available from the title screen. Full implementation and validation details are in `IMPLEMENTATION_REPORT.md`.

## Play the included build

Extract the entire ZIP. With Java 17 or newer installed, run `RUN_GUISE.bat` on Windows or `./RUN_GUISE.sh` on Linux. Alternatively:

```
java -jar dist/GUISE-Band-A.jar
```

The bundled JAR includes Linux x64 and Windows x64 native libraries. Linux was tested; Windows was not available for a launch test. It does not bundle a Java runtime. Use the original Gradle platform tasks for other architectures/macOS.

## Controls and short demonstration

- **Enter:** start/resume, interact with the current dungeon tile, or commit the selected battle command.
- **Dungeon:** W/S or Up/Down move; A/D or Left/Right turn. Escape returns to the menu without resetting the session.
- **Battle:** Up/Down select a command; Left/Right select the main enemy target; Tab selects the response target; S toggles the Walking Solo move. **Space** acknowledges results and advances enemy/forced actions/phases.
- **B:** Book; B/Escape returns. PgUp/PgDn review the ordered result log.
- **I in dungeon:** Piano/Trumpet equipment, after acquiring Trumpet. Tab selects a demon seat; F exchanges it with the reserve.
- **Battle reserve swap:** R selects the outgoing demon seat; Q selects reserve; choose the Swap command. Entrants wait until next player phase.
- **M:** sound toggle. **F10 starts a completely new run**, discarding this run's in-memory progress.

1. Start at **R**, the rest point. Move to **1** and press Enter. From the spawn facing east, move forward three cells, turn right, then forward two cells.
2. Start with Piano, Saxophone, Bass, Percussion. Scroll below the individual moves to the compatible **Try combination** entries. Successful completion reveals a name and adds it to the Book. Inspect costs before committing.
3. Space advances each result. The enemy Trombone visibly arms one interruption cue. Disable its owner or avoid the marked performer. The enemy rhythm section can also ready protection.
4. On victory, Space returns to the same dungeon cell. Visit **U** and press Enter to unlock Trumpet and recruit JJJ once. Return to R to restore HP/MP if needed.
5. Press I to equip Trumpet while keeping the same three demons. Visit **2**. Its visibly logged TEST Bass physical weakness provides a Call & Response opportunity. Both encounters are one-shot victories; an ordinary defeat returns to R without consuming Judgment attempts.
6. Visit **J** for the live Judgment preview of the latest completed battle. P previews; Enter commits. The third failure is terminal; F10 starts a new run.

## Isolated harnesses

From the title, H opens harnesses. V starts a separate three-body Virtuoso/Piano+Saxophone test. Full Chorus still matches four roles and charges the combined role MP. J opens the developer Judgment harness: 1 = high performance/wrong fit; 2 = right fit/poor performance; 3 = pass; 4 = fail. Enter commits a preview. Returning from a harness preserves the normal campaign session. L on the title launches the original 1v1 battle.

## Eclipse / Gradle

Import this extracted `Guise_Proj` as an existing Gradle project in Eclipse. Preserve the configured Java 21 Gradle daemon and Java 11 source target. The usual tasks remain:

```
./gradlew core:test
./gradlew lwjgl3:run
./gradlew lwjgl3:jar
```

On Windows use `gradlew.bat`. The normal main class remains `com.payamsdev.oldsona.lwjgl3.Lwjgl3Launcher`. Its working directory is `assets` when run directly from Eclipse.

The editor remains `dev.DemonConfig`. Launch with `assets` as working directory or set `-Dguise.assets=/absolute/path/to/assets`. Its existing generator remains; **Open / Edit Existing JSON** supplies validated, lossless JSON-tree editing and Save As. Unsupported advanced moves are preserved and reported as deferred runtime behavior, not approximated.
