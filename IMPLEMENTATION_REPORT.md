W# GUISE Band A — implementation report

## Outcome

The Band A prototype is runnable end-to-end: grid exploration, two encounters, four-member commands, Arrangement costs/results, Groove, discovery, persistent party/Book, acquisition, instrument change, reserves, and Judgment/Virtuoso harnesses. The shipped rendering and story placeholders are a mechanics test, not final presentation.

**Final rules suite: 47 tests started, 47 passed, 0 failed, 0 skipped.** Real LibGDX/LWJGL desktop rendering, resize, and packaged-asset smoke checks passed. The actual packaged `java -jar` launcher was also exercised outside Eclipse, from a directory with no project assets.

## Implemented behavior

- Only option A: Piano/Saxophone/Bass/Percussion versus Trumpet with the same demons. JJJ is an optional acquired reserve. Options B–D are not implemented.
- Five recipes: Blue Note, Locked In, Call & Response, Walking Solo, Full Chorus. Structural role matching is separate from eligibility. Distinct instances, duplicate roles, once-per-phase commitment, active/reserve exclusion, MP per performer, and K-icon reservation are enforced.
- Coordinated components use the same party resolver as ordinary moves. The aggregate Press Turn result is applied once; no child action double-charge. Weak/resist/miss/null/repel/absorb/crit behavior and FIFO flashing icons are tested.
- Locked In completes on an actual protected hit, not readiness. Full Chorus suppresses the next enemy protection setup; merely preventing future readiness gives no ensemble-disruption credit. An actually ready interaction must be removed for that credit.
- A visible, one-shot Trombone cue can interrupt a committed player Arrangement. The cost remains spent, no component executes, Muted is applied, and no discovery occurs. Cue owners can be disabled/killed. Voluntary withdrawal cannot manufacture hostile recovery credit.
- Typed command facts feed capped, recent three-round Groove. Fixed starting-level preparation, repetition, variety, recovery, adaptation, misses/deaths, and successful contribution evidence are covered. Ordinary attacks do not produce an automatic positive score.
- DOT, Rushing, Muted, Dragging, Testified and null-status handling are implemented for the selected subset. Basic/learned move authorization is enforced; unsupported active behavior fails explicitly while all 82 move records remain available as authored data.
- Session-owned musicians keep HP/MP and identity through field/battle transitions. Rewards and encounters commit once. Discovery remains in the Book; unavailable known combinations remain listed. Judgment previews consume no attempt, submissions are idempotent, three failures terminate the run, and the isolated harness cannot overwrite campaign progress.
- DemonConfig uses the shared Race definitions without replacing its stat/budget generator. Open/Edit/Save As preserves unknown JSON fields, including explicit nulls; validates references, declared learnable capacity and learn levels; and reports unsupported selected runtime behavior.

## Files and boundaries

`validation/changed-files.json` contains the exact changed source/content/tooling paths against the uploaded baseline. Main groups:

| Area | Files / change |
| --- | --- |
| Existing battle data | `battle/DemonDefinition.java`, `BattleEntity.java`, `Move.java`, `Affinity.java`: preserve level/basic/affinity data, runtime identity/status/eligibility, aliases and selected move metadata |
| Existing economy | `battle/PressTurnEngine.java`: snapshot access and validated Arrangement reservation; original FIFO rules retained |
| Existing regression path | `battle/BattleController.java`, `battle/BattleScreen.java`: terminal outcome assignment and case-exact paths; legacy serializer compatibility tested |
| Party rules | `battle/SliceBattle.java`, `BattleResult.java`: active controller, ordered typed facts, scheduling, status resolution, enemy fixture, Arrangement execution |
| Band | `band/ArrangementDefinition.java`, `BandComposition.java`: data and instance-based role assignment |
| Content | `content/ContentCatalog.java`, `ContentFiles.java`, `RaceDefinition.java`, `StatusDefinition.java`: catalog, validation, shared tool/runtime definitions |
| Progression | `game/GameSession.java`, `dungeon/GridMap.java`: persistent in-memory roster, unlocks, encounters, grid legality |
| Performance | `groove/GrooveCalculator.java`, `judgment/JudgmentEvaluator.java`, `JudgmentState.java` |
| Presentation | `screens/SliceScreen.java`, `com/payamsdev/oldsona/OldsonaGame.java`: connected prototype views, viewport, keyboard controls, lifecycle, legacy entry |
| Editor | `dev/DemonConfig.java`, `dev/DemonDocument.java`: shared Race mapping and lossless validated document editing |
| Content files | `assets/arrangements/`, `assets/races/`, `assets/prototype/`; player/status metadata; original demon files gain schemaVersion only |
| Tests/build | Seven classes under `core/src/test/java/slice/`; `lwjgl3/src/test/java/screens/DesktopSmoke.java`; JUnit configuration in `core/build.gradle`; optional local validation/package tools and run scripts |

All original demon JSON values were compared with the baseline and remain identical after removing the added `schemaVersion`. The 82-move catalog is unchanged. The authoritative specification was not rewritten. The original controller remains the 1v1 regression entry; the already-established party controller was extended, not replaced.

## Validation evidence

- `validation/automated-tests.txt`: final 47/47 results, including all original 14 tests.
- `validation/packaged-smoke.txt`: successful real OpenGL screen/input smoke using the packaged JAR and no loose asset directory.
- `validation/jar-launch.txt`: actual manifest launcher stayed running for five seconds, then was intentionally terminated.
- `validation/screenshots/`: title, dungeon, resized dungeon (720×480), party battle, resolved Arrangement, Book, Judgment preview/third failure, Virtuoso and original 1v1 screen.
- Seeded end-to-end strategy clears encounter 1 in **5 rounds**, encounter 2 in **3 rounds**. Final Groove is respectively **−2.3333** and **0.6667**; survival does not require positive Groove or passing Judgment.
- Tests cover duplicate/dual-role matching, invalid-command nonmutation/RNG, all ordinary affinity outcomes, mixed multi-target outcomes, precise MP/icon costs, status timing, cue interruption/disarm, voluntary-token cancellation, immutable icon snapshots, phase scheduling, discovery timing, round-history scoring, contribution evidence, recovery/adaptation, one-shot rewards, map reachability, Judgment gating/idempotency, and lossless editor round trips.
- No pre-existing automated test files were found in the uploaded baseline. Legacy LibGDX deserialization and rendered 1v1 entry were added as explicit regression checks.

Bugs fixed during final validation: unknown explicit-null loss in the new JSON editor; abstract Map incompatibility with the legacy LibGDX loader; unreachable scripted cue due to enemy seating; incorrect identity contribution from failed/interrupted attempts; reserve ownership during voluntary token cancellation; and escaping normal third-failure Judgment back into encounter controls. An incomplete package-write was corrected with atomic archive replacement and CRC verification.

## Explicit provisional choices

- Encounter-only HP multiplier is **2**, with **+12 MP**; canonical demon HP/MP are unchanged. The second encounter's TEST Bass has physical weakness via fixture configuration. These settings are visibly logged and live in `assets/prototype/slice.json`.
- Enemy seating is Trombone, Bass, Percussion so its one scripted cue is reachable under circular Press Turn scheduling.
- When Muted makes a Testified nonbasic locked move impossible, the lock releases without spending an action. An affordable zero-MP locked basic persists, as specified.
- Encounters/rewards use Enter on their map tiles. Ordinary defeat explicitly returns to rest; it does not consume a Judgment attempt. F10 explicitly resets the entire in-memory test run.
- The corridor renderer is a small flat-shaded raycast fixture rather than a new textured 3D engine. Confirmation tone and geometric figures are placeholders.

## Limits, kept separate from passing checks

- This is a mechanics vertical slice: no disk saves, final dialogue/cutscenes, authored art/animation suite, layered soundtrack, full campaign or implementation of every advanced catalog move. Judgment success/failure ends at named test hooks.
- Recent Groove/fixture balance still needs a human playtest. The scripted wins prove connectivity, not that every strategy is balanced or that the experience is fun.
- Desktop/packaged runtime was tested on Linux x64 with Java 17 and software OpenGL. Windows x64 natives are included but Windows was not available to test. Other platforms should use the original Gradle packaging targets.
- Audio was loaded through the desktop backend with a null output device; audible quality was not verified. The temporary display emits a harmless `XDG_RUNTIME_DIR` warning before choosing X11.
- The original Gradle wrapper could not download Gradle 9.5.0 in this environment: `java.net.SocketException: Network is unreachable`. This is separate from the successful compiled/packaged runtime. Validation therefore used the pinned dependencies, Java compiler with `--release 11`, JUnit Console, and the documented local packaging fallback. A full Gradle/Eclipse build was **not** claimed as passing; configured versions/toolchains remain unchanged.
- Source remains ordinary Java/LibGDX. Python is used only by the optional offline packaging helper, not by the game.
