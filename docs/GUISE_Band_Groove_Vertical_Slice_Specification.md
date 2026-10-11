GUISE — Band/Groove Vertical Slice Specification

Version 0.1 • October 3, 2026 • Design proposal awaiting Michael’s approval

**Status and authority.** This is a specification and implementation plan. None of the systems proposed here has been implemented by this deliverable. The approved Project State Report remains the technical baseline. Michael’s new brief supersedes its unresolved questions wherever the brief now supplies an answer. No game files or uploaded source documents were modified.

Labels used throughout: **Intended** = explicit requirement from Michael; **Confirmed** = established from supplied content; **Temporary** = proposed slice rule needing approval; **Unresolved** = deliberately undecided. All numerical costs, formulas, test content, Arrangement effects, and prototype Judgment thresholds below are Temporary unless explicitly stated otherwise. Candidate Bands are alternatives, not canon and not ranked.

**Settled design.** Races are the instruments. A normal Band contains the protagonist and three demons. Individual demons retain distinct stats, attacks, skills, identities, and personalities even when their Race matches. Grid-by-grid first-person exploration is intended. Ordinary combat has no musical timing inputs; Nutville’s rhythm minigame is separate. Exactly three Judgments exist per playthrough; the third failed Judgment leads to Game Over with Michael-authored cutscene content. Reload semantics are unresolved. Virtuoso’s two identities are intended, but only an isolated experiment belongs here.

**Content check, not a repeated audit.** The new move attachment contains HTML-escaped whitespace from its Markdown export. After decoding that formatting for comparison, its parsed data equals the reviewed 82-move catalog. DemonConfig’s new attachment matches the reviewed source. The five pasted status records match the existing concepts. The original attachments remain untouched; the Markdown paste itself is not a directly loadable JSON file until its formatting is decoded. Existing move names and Race names are authoritative vocabulary.

**Decision requested.** Select one of the four Band pairs in section C, then approve or amend the Temporary rules. The selected pair determines which three-to-five Arrangement recipes and which small fixture roster are implemented. The other candidates remain paper designs. Section D specifies each option so you can make that choice without another speculative implementation round.

**A. The smallest playable loop.** Target a short, repeatable test session, approximately 15–25 minutes rather than a miniature full campaign.

1. Enter a small noncanonical test dungeon with four active musicians. See their actual Race names. One candidate-specific protagonist instrument is equipped.
2. Walk one grid cell at a time, turn, collide with a wall, and interact with one musician/event. Use brief placeholder interface text only; final story/dialogue belongs to Michael.
3. Encounter a fixed, visible test battle trigger. Suspend exploration at a recorded cell and facing; construct combat from existing session-owned musicians.
4. Perform individual musical attacks using their authored names. The first battle presents a safe opportunity to discover a pair Arrangement.
5. Open Arrangements, see an unnamed compatible combination, select its participating musicians, and attempt it. Its first meaningful successful resolution reveals its name and adds it to the Book of Arrangements.
6. See the actual coordination effects, Press Turn outcome, and an explained Groove change. No button timing or background-track synchronization affects the result.
7. Face a telegraphed disruption. Decide whether to prevent it, use another combination, accept its cost, or substitute a musician.
8. Win. Commit HP/MP and encounter results once, preserve discovery history, clear temporary battle statuses, and return to the same dungeon position/facing.
9. Acquire the candidate’s required reserve musician(s) and alternate protagonist instrument through one-shot test rewards. One acquisition event can grant the two musicians needed for pair C. Their IDs prevent duplicate claims. Swap/form the second Band and discover the different actions it enables.
10. Fight a short second configuration of the test enemy group. Use what was learned rather than merely repeating the first Band’s strongest attack.
11. Inspect a performance summary and optionally enter a separate Judgment test harness. The harness exposes the evaluation concept; it is not the final God fight or ending.

Progression for this slice is acquisition, instrument access, Arrangement knowledge, and demonstrated recovery/adaptation. Full XP leveling is not required; a controlled level comparison in the harness tests the formula’s small progression term. A field rest point restores HP/MP without awarding Groove. No procedural encounters are needed to prove this loop.

**B. Minimal content roster and supported vocabulary.**

| Existing content | Confirmed role | Proposed slice use |
| --- | --- | --- |
| JJJ | Trombone, level 3; Blast, Blow, Yeah Man; later The Stankface | Active/reserve musician or separately instantiated enemy, depending on candidate |
| Ambassador | Percussion, level 3; Fill, Yeah Man, Crash; later Rimshot/Time Out | Active musician and independent enemy fixture source |
| Sirabhorn | Guitar, level 15; Lick, Diatonic Harmony, Minor Dissonance, Jam Session, Vamp | Preserve unchanged. Reference for Guitar’s authored vocabulary; exclude from the initial level-3 roster to avoid unapproved downscaling and advanced-effect implementation |
| Player | Human legacy race; Soul Motif, Modal Motif, Fusion Motif, Crash | Protagonist uses a separate equipped Race/instrument identity for Band matching; preserve Human as legacy origin metadata |
| Dummy/template | Development records, not production musicians | Test fixtures only; do not relabel them as finalized characters |

**New fixture data, only when selected:** TEST Saxophone (Riff, Modal Motif, Chop Buster); TEST Bass (Groove, Blow); TEST Trumpet (Blast, Blow); TEST Trombone B (Blast, Yeah Man). Generate starting stats with the existing Race tendencies at level 3, then inspect balance. Their IDs, portraits, names, and descriptions explicitly identify them as test content. No invented final character biography. JJJ and TEST Trombone B deliberately differ in skills, and a harness variant can differ in stats, to verify that equal Race does not mean equal combatant.

The protagonist’s fixture-specific instrument unlocks grant the associated basic attack while equipped. Candidate D additionally grants Vamp as a clearly marked test unlock. These grants do not change the authored player catalog silently. Race, learned skills, and basic-attack authorization remain distinct checks.

The existing basic move named **Groove**, ID `groove`, stays named Groove. Band Groove is a different state value; it is not awarded automatically when that attack is used. Similarly, the existing fusion damage element is not the future demon-fusion system, and the dissonance element is not automatically an Arrangement-interruption command.

**Minimum runtime move support:** existing instrument basic attacks; Blow; the player’s three Motifs; Yeah Man’s all-enemy targeting; Crash’s single status; Chop Buster; and the subset of learnables actually granted in the selected fixture. Candidate D additionally needs Vamp’s authored accuracy reduction. Do not expose an unsupported move as an ordinary approximation. Keep all 82 authored records; active fixture loadouts must pass capability validation. Later skills not unlocked in the slice remain visible as future content where appropriate, not deleted.

**Temporary status interpretation required by the slice:**

| Status | Preserve | Proposed executable meaning |
| --- | --- | --- |
| Impression | 10 base damage; damage_over_time | 10 direct HP damage at the end of the affected side’s phase, for three such phase ends |
| Stank | 5 base damage; damage_over_time | Same timing, 5 HP damage; separate from Impression |
| Rushing | stagger; unable to act temporarily | On the next scheduled command, skip it, spend one NORMAL icon, then remove Rushing; until then cannot join an Arrangement |
| Testified | move_lock; repeats last move until MP depleted | Capture last committed individual move and target rule. Force it on future commands until its MP cost cannot be paid, then clear and allow normal selection. A zero-MP move remains locked until battle end/removal: this is an explicit risky interpretation to approve, not a hidden cap. No valid last individual move means application fails with an explanation. Arrangements are not copied as individual moves |
| null | Existing sentinel | No runtime status instance |
| Muted — new definition for existing referenced ID | Chop Buster already references muted | For two affected-side phase ends, block Arrangement contribution and nonbasic moves; permit the equipped basic attack and Defend. This escape option is Temporary |
| Dragging — new definition for existing referenced ID | Crash already references dragging | Move affected actor to the end of its next phase’s order once, then remove; it can still contribute if otherwise eligible |

Status instances are unique by actor/status ID; reapplication refreshes rather than stacks damage. A status removed at the end of a phase affects that phase. Battle end clears these battle-only statuses. While in reserve, a musician keeps its HP/MP/status state but does not tick phase-based statuses; returning does not automatically cure it. Testified also prevents Arrangement contribution while its forced-move lock applies. The existing guard/resistance status-protection behavior stays initially, with visible reasons when application is prevented. DOT is not an attack, cannot crit, and does not independently consume icons. A lethal tick still produces one death/result check.

The zero-MP Testified case, Muted’s basic-attack exception, and these durations require explicit approval. If an effect is not approved, its moves must be marked unavailable in test loadouts rather than silently stripped of that effect. No behavior is invented for out_of_pocket, polyrhythmic, or stage_fright in this slice.

**C. Four candidate Band pairs.** P denotes the protagonist’s equipped instrument; TEST denotes new noncanonical content. Every listed Band has four physical musicians, including P. These are experimental comparisons, not final Band names.

| Pair | First Band | Second Band | Question this comparison tests |
| --- | --- | --- | --- |
| A — change the leader’s instrument | P Piano + TEST Saxophone + TEST Bass + Ambassador/Percussion | P Trumpet + the same three demons | Does one instrument unlock change the Band’s verbs while the roster stays constant? |
| B — rhythm section versus horn front line | P Piano + TEST Saxophone + TEST Bass + Ambassador/Percussion | P Trumpet + TEST Saxophone + JJJ/Trombone + Ambassador/Percussion | Is trading Bass support for Trombone pressure a meaningful choice rather than a numerical upgrade? |
| C — repeated brass versus mixed horns | P Trumpet + TEST Trumpet + JJJ/Trombone + TEST Trombone B | P Trumpet + TEST Saxophone + JJJ/Trombone + Ambassador/Percussion | Can specialization with duplicate Races be as expressive as variety? |
| D — recovery/control versus horn timing | P Guitar + TEST Bass + JJJ/Trombone + Ambassador/Percussion | P Trumpet + the same three demons | Does a coordinated substitution/control option play differently from coordinated enemy-order disruption? |

**Pair A details.** Piano enables BLUE NOTE, WALKING SOLO, and FULL CHORUS; Trumpet enables CALL & RESPONSE; both can use LOCKED IN. Piano converts/positions follow-up opportunities and supports an ensemble climax; Trumpet reacts to a prepared weakness/status opportunity with a conditional response. Existing Ambassador and the catalog’s Riff, Chord, Groove, Fill, Blast, Modal Motif, and Yeah Man are sufficient vocabulary. Add TEST Saxophone/Bass, Piano/Trumpet unlock definitions, and the five Arrangement records. Recruit existing JJJ as a reserve to test substitution without inventing a fifth demon. This comparison deliberately isolates the protagonist instrument first; roster experimentation is still available afterward.

**Pair B details.** First Band has BLUE NOTE, LOCKED IN, and WALKING SOLO. Second has CALL & RESPONSE and TEST BRASS RELAY. It trades protection and precise follow-up scheduling for exploiting openings and changing enemy order. Existing JJJ and Ambassador directly populate the comparison; TEST Saxophone/Bass fill the missing Races. Add Piano/Trumpet unlocks and five Arrangement records. Full Chorus stays outside this pair’s playable catalog; four-member resolution is covered by a small isolated fixture test. The two lineups are not asserted to be equally strong before playtesting.

**Pair C details.** Duplicate brass enables TEST ANTIPHONY and TEST BRASS RELAY; mixed horns enables CALL & RESPONSE and TEST BRASS RELAY. Antiphony separates enemies’ ability to perform together; Relay manipulates the next enemy phase’s order. This explicitly tests repeated Race requirements and keeps duplicate instruments meaningful. Add TEST Trumpet, TEST Trombone B, and TEST Saxophone; reuse JJJ and Ambassador. The union has five recruitable demons, three active at a time. The enemy demonstration also needs one enemy-only TEST Bass definition; the other pairs reuse their playable Bass definition. Three player Arrangement records suffice, plus enemy-only Locked In. A three-member matcher/resolver fixture is test-only. This pair does not earn a Groove penalty merely for having duplicate Races.

**Pair D details.** Both Bands have LOCKED IN. Guitar adds TEST TURNAROUND: Vamp-led control followed by an otherwise unavailable substitution window. Trumpet adds TEST BRASS RELAY. Existing JJJ/Ambassador and the catalog’s Lick, Vamp, Groove, Fill, Blast, and Blow ground the proposal; Sirabhorn demonstrates the existing Guitar vocabulary but is not downgraded or given a partial pretend implementation. Add TEST Bass and TEST Saxophone (reserve for the substitution experiment), Guitar/Trumpet unlocks, and three Arrangement records. Add real Vamp debuff handling; it is the extra runtime capability this option requires. Three-member play is live; four-member matching remains an isolated fixture test.

**D. Arrangement matching, cost, and resolution.** These rules apply only to the selected candidate’s recipes.

**Composition.** Match a multiset of Race requirements against active musicians. Ordinary musicians supply one Race role each. Reserves do not count. Two Trumpet requirements need two distinct Trumpet instances. Do not infer Race from a skill name, damage element, or instrumentType tag: JJJ remains Trombone while using Blast. Resolve `phys`/`physical` and Drain/Absorb at the data boundary, not by changing musician identity.

Return both structural compatibility and present usability. A dead/Muted musician still occupies a roster seat, but cannot contribute. The UI should show why a known Arrangement is unavailable. A Band may enable multiple overlapping Arrangements; it does not receive exactly one prescribed “Band class.”

**Temporary action economy.** One icon per living active musician is created at phase start, up to the existing four-icon limit. Rushing/Muted do not secretly remove phase-start icons; their effects operate explicitly. Stop if no living actors remain rather than calling startTurn with zero. Seat order is deterministic and circular; each accepted command advances past its initiator, skipping dead musicians and new entrants still waiting until next phase. Rushing is an explicit paid skip, not an invisible scheduler skip; Testified supplies a forced command. Extra Press Turn opportunities continue that order. There is no per-actor once-per-phase individual-action budget. For the slice, defeat occurs when no active ally remains alive, not automatically when the protagonist falls; this is a Temporary rule needing approval. No mid-battle revival is required. After victory, a fallen protagonist can still be carried through the abstract field-navigation test until the rest point; this is fixture convenience, not finalized death lore.

An Arrangement is initiated on one participating musician’s scheduled command. It costs K icon instances from the shared pool plus listed MP. Other contributors do **not** each lose a separate ordinary command; participation is a coordinated contribution, not K invisible individual turns. However, each participant may join at most one committed Arrangement per own phase. Individual attacks remain available afterward. This explicit lock prevents endlessly reusing the same accompanists and is a Temporary balance rule.

Most pair/trio Arrangements cost K=2; four-person ones cost K=3. Require at least K actual icons, regardless of FULL/FLASHING state. At commitment, validate all participants, target choices, MP, recipe prerequisites, and icon availability. Reserve/remove K−1 front icons, then resolve exactly one final TurnResult through the existing engine. Component attacks never consume icons separately. MP is charged once as the total listed cost, including any embedded move costs.

| Aggregate result | Final call after reserving K−1 | Effective cost from K initial FULL icons |
| --- | --- | --- |
| NORMAL / RESIST | Existing NORMAL / RESIST handling | K icons |
| WEAKNESS / CRITICAL | Existing bonus handling, once per Arrangement | K−1 removed and one FULL converted to FLASHING |
| MISS / NULLIFY | Existing two-icon penalty | Up to K+1 icons removed, limited by pool size |
| REPEL / ABSORB | Clear remaining pool | Entire remaining phase lost |
| Interrupted after commitment | NORMAL, not a second miss penalty | K icons; MP and participation commitment remain spent |
| Invalid before commitment | No call, no mutation | Zero; no MP, cooldown, RNG draw, or Groove change |

Example from four FULL icons: a two-icon normal Arrangement leaves two FULL; a weakness leaves two FULL plus one FLASHING; a miss leaves one FULL. A three-icon weakness leaves one FULL plus one FLASHING. Existing FIFO behavior is preserved. A FLASHING final icon is consumed even on weakness, just as the current engine specifies.

**Aggregation for multiple components/targets.** Apply damage and statuses per component/target, but reduce Press Turn exactly once. Proposed severity: REPEL or ABSORB overrides everything; otherwise MISS or NULLIFY overrides bonuses; otherwise any WEAKNESS or CRITICAL gives one bonus; otherwise any RESIST gives RESIST; otherwise NORMAL. Resistance does not erase a separate successful weakness in this provisional rule. No bonus stacks across several targets. Stop remaining component attacks after a repel/absorb, a participant death, or battle termination. Already applied damage is not rolled back. A killed target is not attacked again or randomly retargeted; optional response targets are chosen in the original command. Distinguish “skipped because already dead” from MISS. This policy is explicit and tunable, not a claim about every SMT title.

**Proposed recipes.** User-proposed names retain their spelling. Names prefixed TEST are proposals, not final authored attacks. Components invoke existing moves without renaming the normal move or changing its standalone behavior. All required component moves must be known or authorized basic attacks for their performers.

| Arrangement / candidate | Participants and requirements | Total cost | Specific interaction / completion condition |
| --- | --- | --- | --- |
| BLUE NOTE / A, B | Piano + Saxophone; Piano knows Modal Motif; Saxophone has Riff; either may initiate | 2 icons; Piano 3 MP, Saxophone 1 MP | Piano performs Modal Motif. If it deals damage, Saxophone follows with Riff checking modal affinity for this follow-up only; strength scaling/physical crit eligibility remain Riff’s. Both hit the selected target; if it dies on the first hit, completion counts and the unnecessary follow-up is skipped. This lets accompaniment change an attack’s affinity interaction rather than just its damage number |
| LOCKED IN / A, B, D | Bass + Percussion; either initiates; no unexpired token already active | 2 icons; 2 MP each | Ready one protection token. The next enemy damaging hit on an ally is halved after ordinary calculation, and that protected musician goes first next player phase. Token expires at that phase’s start. If either contributor dies/becomes unable to contribute before the hit, token breaks. Completion/Groove credit happens when protection actually triggers, not when merely readied |
| CALL & RESPONSE / A, B, C | Trumpet + Saxophone; Trumpet initiates with Blast; choose response target in advance | 2 icons; 2 MP each | If Blast deals damage and either exploits weakness, crits, or hits an enemy already carrying Stank from another musician, Saxophone immediately uses Riff on the selected live response target. Completion requires the response to land, or the triggering Blast to end the battle. Otherwise this remains a paid uncompleted call with no cooperation award. Targets may differ; it is not free repeated full commands |
| WALKING SOLO / A, B | Piano + Bass + Saxophone; Piano or Bass initiates; choose Saxophone’s Riff or Modal Motif | 2 icons; 2 MP each accompanist; Saxophone pays the selected move’s normal MP | Saxophone performs that move now, then becomes the next scheduled actor if icons remain. That next command still pays its normal icon cost; no icon is created. Other contributors retain their ordinary command opportunities. Completion requires a damaging hit. Tests whether accompaniment buys control over who can exploit the resulting opportunity |
| FULL CHORUS / A | Piano + Bass + Saxophone + Percussion; Percussion knows Yeah Man; any participant initiates | 3 icons; Percussion 6 MP, others 2 MP each | Percussion leads the existing Yeah Man against all live enemies, once each. If at least one target takes damage, prevent one chosen enemy Arrangement from being readied during the next enemy phase. Does not silence their ordinary moves. It creates a group-level suppression window, not just another larger damage multiplier |
| TEST BRASS RELAY / B, C, D | Trumpet + Trombone; either initiates; both have Blast | 2 icons; 2 MP each | Two Blasts against one enemy, one from each musician. If both damage it and at least two enemies survive, swap the first two actors in the next enemy phase’s base order. If the first Blast kills the target, skip the second, count coordinated completion, but no reorder. With one enemy, the reorder benefit is absent and clearly previewed |
| TEST ANTIPHONY / C | Two distinct Trumpets + two distinct Trombones; any initiates; select two live enemies | 3 icons; 2 MP per musician | Alternate the four basic Blasts across the two selected enemies, each at 50% ordinary damage before affinities/guard. If each target is damaged, those two enemies cannot participate together in the next enemy phase’s Arrangement. Other combinations and individual moves remain legal. If one dies, its remaining assigned hits skip; disrupting the enemy ensemble through that death also completes the technique |
| TEST TURNAROUND / D | Guitar + Bass + Percussion; Guitar knows Vamp; choose a nonparticipant ally and legal reserve, or choose no swap | 2 icons; Guitar 8 MP, Bass/Percussion 2 MP each | Guitar uses Vamp with its authored damage and 25% accuracy reduction. On a damaging hit, optionally substitute the chosen nonparticipant with the chosen reserve as part of the result. Incoming musician cannot initiate or contribute until next player phase; no icons are added. Completion requires the debuff to apply or a legal substitution to occur. A failed Vamp does not grant the swap |

Vamp’s reduction lasts through two affected-side phase ends, multiplies the accuracy factor by 0.75, and does not stack with itself; refreshing is explicit. This is a proposed duration for an existing field, not an alteration to its authored 25% amount. If a guaranteed-hit rule is approved, that rule bypasses this multiplier explicitly.

Recipe-specific effects are small Java branches selected by an enum-like effect kind, not scripts embedded in JSON. BLUE NOTE’s changed affinity element is local to the coordinated action; ordinary Riff remains physical. Existing crit/weakness damage stacking and guard rules are retained initially, including the previously identified guard double reduction, until Michael approves a separate change.

**Shared Groove and discovery behavior.** All recipes emit a meaningful-completion result as defined above. Section E awards at most one cooperation credit per round. Mere selection, protected-token setup, animation playback, and English text do not award anything. Discovery occurs the first time a previously unknown recipe meaningfully completes; record its ID once in the session Book. An interrupted attempt remains undiscovered. A compatible unknown recipe displays its visible participants and “Try combination,” not its final name. A discovered but unavailable recipe remains in the Book with a reason. The slice need not reveal every undiscovered requirement list; one partial hint slot is sufficient.

**Interruption and enemy Band demonstration.** Use one fixture group: a Bass enemy, an Ambassador-based Percussion enemy, and a JJJ-based Trombone enemy. Reuse definitions but give each runtime instance its own ID, HP/MP, statuses, and enemy ownership. A short deterministic policy replaces general AI: protect with LOCKED IN when legal, otherwise use an authored legal attack; on one scripted opportunity per encounter, arm the test interruption below. Enemy use never discovers player recipes or directly gains player Groove. If the selected player candidate does not use LOCKED IN, its one enemy-only record is the only additional live recipe.

The new, explicitly noncanonical encounter action **TEST: Interruption cue** costs the enemy one NORMAL icon to arm, 0 MP, and targets one displayed player musician. It can be armed once per encounter by the enemy Trombone and expires at the next enemy phase start. If the player commits an Arrangement containing the marked musician while the cue owner remains alive and able to contribute, the cue applies Muted to that musician and cancels the Arrangement before any component attack. The cue is consumed, so it cannot interrupt again. The committed icon/MP cost is retained. No additional reactive icon penalty or hidden damage is introduced. Killing or disabling the cue owner disarms it; choosing an Arrangement without the marked musician bypasses it.

This is a single scripted reaction at a named resolution checkpoint. It does not reinterpret Chop Buster or the dissonance element as an interrupt, introduce real-time counters, or require a reaction framework. Ordinary Chop Buster independently provides the authored damage/status route to break a Band. A direct debuff, death, or completed suppressive Arrangement that removes an actually ready enemy interaction can emit EnemyBandDisrupted; hitting an unready musician does not farm that credit. Aim for roughly three-to-five rounds in the second test encounter. If original level-3 enemy durability cannot support that, use a visibly logged encounter-fixture HP multiplier and tune it during the harness milestone; never modify JJJ/Ambassador's canonical HP to disguise test balancing. Such multipliers do not change their levels or the Groove preparation term.

**Substitution.** Between battles, freely change the active three demons and protagonist instrument from acquired options. During battle, a living unforced initiator may spend one NORMAL icon to replace a different demon or itself with a living reserve; the protagonist cannot be removed. No MP cost. The incoming demon waits until the next player phase to act or contribute. Existing icons neither increase nor reset. Permit replacing a dead demon via another living actor’s command; no revival occurs. A removed musician cannot rejoin in the same phase. Pending Arrangements/tokens involving it invalidate explicitly. Swapping alone earns no Groove. Guitar’s Turnaround creates a different substitution opportunity by bundling a successful musical action with that otherwise separate command.

**Virtuoso test, isolated from progression.** One fixture has Race Virtuoso and supplied identities Piano/Saxophone. It may satisfy one Piano and one Saxophone role simultaneously in a recipe marked dual-role-compatible, alongside Bass and Percussion. It still has one HP/MP pool, one seat, one phase-start icon, and one command position. Charge the sum of both role MP costs; one participation commitment blocks both identities for the rest of the phase. Death/Muted disables both. It may not satisfy two Piano roles, invent an arbitrary third identity, or become two physical musicians for Judgment’s future FOUR VOICES concept. Test the dual-role version of FULL CHORUS with three physical performers and the normal four-person version side by side. Do not grant this fixture in the normal slice or finalize its acquisition/mastery rules.

**E. Provisional Groove model: recent performance, not a spendable meter.** There is no 100% bar, automatic attack-count reward, passive racial-diversity bonus, or Groove purchase cost in this experiment. Groove is an explained score of recent performance. It can be negative, and it does not directly multiply damage in v0.1. Combat is affected by Arrangements; performance is assessed by Groove and the separate Judgment harness. This avoids tuning a feedback loop before knowing whether coordination is fun.

A round means one player phase plus its following enemy phase. At battle start, open round 1. Keep the current round’s score S0 and the previous two completed round scores S1 and S2; missing history is zero. The displayed score is:

G = L + (S0 + S1 + S2) / 3

L = 0.25 × clamp(mean starting active-member level − 3, 0, 8)

L is frozen for that battle, from at most four physical musicians, including the protagonist. Dead members and subsequent swaps do not change it; Virtuoso counts once. At levels 3, 5, and 11+, L is respectively 0, 0.5, and 2. This deliberately small preparation term lets progression matter without buying successful coordination or rewarding bringing a high-level reserve.

Each S is the sum of the following bounded event credits. Keep every contributing reason in a ledger. Caps apply to categories per round, not to a hidden overall score; penalties cannot disappear behind a saturated bar.

| Event term | Points / cap per round | Exact qualifying condition |
| --- | --- | --- |
| Cooperation C | +4 once | First meaningful completed Arrangement; mere readiness is insufficient |
| Coordinated affinity W | +2 once | A completed Arrangement deals weakness damage with no aggregated MISS/NULLIFY/REPEL/ABSORB result |
| Variety V | +1 once | Meaningful completion of a second different Arrangement effect family within the current or preceding two rounds; first family must still be in that window. V is claimed only on completion of a family different from the most recently completed family |
| Recovery R | +3 once | Complete an Arrangement after a recorded enemy-caused interruption or contribution-disabling incident from this or either prior round; consume the incident credit so it cannot pay twice |
| Adaptation A | +2 once | After a hostile disruption, a replacement musician helps complete an Arrangement that the unavailable outgoing musician could not currently perform. Consume that disruption credit’s adaptation flag once |
| Enemy ensemble disruption E | +2 once | An action removes an actually ready enemy Band interaction, protection token, or armed cue |
| Repetition T | −2 once | Third consecutive committed player command with the same action signature, provided another legal non-Defend action exists. Signature ignores actor/target identity so alternating identical attacks cannot hide repetition |
| Miss M | −2 once | One or more offensive components miss in a player command; not per hit/target |
| Broken Arrangement B | −4 once | Enemy interruption or hostile invalidation breaks a committed/readied Arrangement. Ordinary miss is M, not automatically B |
| Member loss D | −3 per distinct musician | Player-side musician dies this round; no duplicate penalty for repeated death checks |

Recovery and adaptation may both reward a genuinely useful substitution once, but random swapping has no reward. Player-caused cancellation, voluntary withdrawal that breaks one’s own token, self-generated status, and entering a menu do not create recovery credits. Same-ID recipes have stable effect-family IDs, so renaming a move cannot manufacture variety. A performer’s distinct Race does not create another family automatically. Pending protection that never triggers gives no cooperation credit. Enemies that are already dead or interactions already broken cannot pay again.

There is no positivity requirement to win an ordinary battle. A rational repeated attack can still be the right survival choice even if its musical score is lower. If testing shows a repetition penalty discourages necessary play, first try losing extra coordination rewards instead of taking points; that is a tuning option, not current canon.

**Worked calculation.** A level-5 starting Band has L=0.5. These are example ledgers for a sufficiently long harness battle, not a claim that every candidate produces these events in one encounter.

| Round | Scored evidence | Round score | Display after listed evidence |
| --- | --- | --- | --- |
| 1 | Coordinated success +4, coordinated weakness +2, a second distinct effect family completes +1 | 7 | 0.5 + (7+0+0)/3 = 2.83 |
| 2 | Coordination +4, qualifying family change +1, third repeated command −2 | 3 | 0.5 + (3+7+0)/3 = 3.83 |
| 3 | Committed Arrangement interrupted −4; a musician dies −3 | −7 | 0.5 + (−7+3+7)/3 = 1.50 |
| 4 | Coordination +4, recovery +3, useful replacement +2, qualifying family change +1 | 10 | 0.5 + (10−7+3)/3 = 2.50 |

A normal basic attack with no qualifying event gives zero, not +1 merely for acting. Its third repeated use can trigger −2 once that round. Two physical misses in one command give −2, not −4. An interrupted attempt gives −4, not an additional invented miss penalty. Simultaneous member loss remains a separate consequence. At the beginning of the next round, shift history and open S0=0; display an explicit “new round / older performance left the window” reason if the number changes. Victory freezes the final partial/full round rather than appending an empty one. Starting another battle resets its recent-performance window; summaries persist separately, not as farmable lifetime XP.

**Known weaknesses and tunable knobs.** The cooperation weight may overfavor whichever recipe is easiest to complete. One credited success per round might make players ignore later successful coordination. A three-round window causes visible score changes at boundaries. Variety could encourage alternating a shallow script. Repetition may punish efficient play or disadvantaged loadouts. Long easy fights can still manufacture recovery opportunities; the bounded history reduces accumulation but does not solve all farming. Current level averaging does not capture mastery or level relationships. The weights, window length, category caps, recovery age, signature rule, level coefficient, and readiness/completion definitions live in one prototype rule set. Test zero level coefficient as a control. No final formula is declared.

**Playtest evidence needed:** Can players explain three recent changes without reading a design document? Do they choose different actions after swapping Bands? Are there at least two effective strategies per encounter? Do they intentionally prolong easy fights? Does recovery feel achievable? Does specialization remain expressive? Log action reasons, legal alternatives, composition, results, and player explanations alongside score. A high score alone is not proof of success.

**F. Minimal combat integration and ownership.** Preserve the PressTurnEngine rule implementation except controlled access/validation needed for cost reservation. Keep its existing tests as characterization before adding Arrangement calls. The surrounding battle code must stop assuming exactly one player and one enemy.

| Existing file / area | Proposed change | Concrete problem solved |
| --- | --- | --- |
| BattleContext | Replace single player/enemy ownership with two small actor lists, stable instance IDs, actor order/cursor, authoritative result, and references to session members | Four musicians, multiple enemies, targeting, and persistent HP/MP |
| BattleController | Validate commands; schedule actors; invoke resolvers; finish battle exactly once; remove text timers/English queue from rules | Current outcome mismatch and inability to test without presentation timing |
| BattleEntity | Preserve per-instance stats/skills; carry definition ID, musician instance ID, effective Race, canonical affinities, status state, participation/entry timing | Same-Race musicians remain distinct; no mutable definition sharing |
| DemonDefinition / loader | Carry authored level/affinities/basicAttack/learnset; validate references and supported active effects | Existing data loss must be fixed before evaluating Bands |
| Move / MoveDatabase | Preserve fields; explicitly support the selected slice’s behavior, retaining other authored data as deferred content | No silent approximation of authored attacks |
| PressTurnEngine | Add a controlled reserve/cost operation or adapter around existing consumption; return read-only snapshots for UI | Supports K-cost Arrangements without component double-charging or external pool mutation |
| battle.BattleScreen | Render actor lists, actual learned moves, targets, Arrangements, Book, and Groove ledger; issue commands, not direct state assignments | Hardcoded skills and 1v1 layout cannot test composition |
| BattleState | Remove or mark unused transitional placeholders only where replaced; distinguish awaiting command, presenting result, and terminal transition | Avoid overlapping outcome/presentation states |
| OldsonaGame | Own one GameSession and screen transitions; make disposal responsibility explicit | Battle construction must not reset the roster or leak resources |

**Small proposed Java boundaries, not existing API claims:** BandComposition evaluates active Race-role assignments and usability; ArrangementResolver accepts a selected recipe/participant assignment/targets plus battle state and returns a BattleResult; GrooveCalculator consumes typed performance facts and returns updated GrooveState plus reasoned deltas; JudgmentEvaluator accepts an immutable evaluation input and returns scores/evidence/hint IDs. Ordinary move resolution can be extracted from BattleController into MoveResolver once so Arrangements reuse exactly the same affinity/damage rules. Use straightforward constructors and ordinary Java 11 classes. No record syntax is required, no ECS/DI/event-bus framework.

Band composition does not own HP, icons, audio, or campaign flags. Groove cannot award itself recursively: combat results feed it once, and generated GrooveDelta records are presentation output. Judgment does not reach into screens or mutate combat. GameSession owns recruited instances, active slots, instrument unlocks, Book IDs, encounter/reward flags, position/facing, performance summaries, and in-memory Judgment attempt state. The battle uses those musician instances with battle-only wrappers; it does not create fresh full-health copies on entry.

A command returns one ordered result containing a unique command ID, component facts, damage/status changes, actor deaths, icon before/after, participation changes, Arrangement completion/interruption, swaps, and outcome. Facts have actor/target instance IDs and recipe/move IDs, not only strings. Use a seedable RNG instance passed to resolution. Rejected commands consume no RNG draws. Presentation can replay typed facts against snapshots without reapplying logical damage, status, or Groove changes. Battle completion is committed before/with an explicit presentation handoff, avoiding the old outcome-early-return trap.

**G. Minimal exploration integration.** Implement one small hand-authored grid, for example 8×8 cells, with walls/floor, spawn/facing, two encounter triggers, one acquisition/event point, and a rest point. This is fixture geometry, not a finalized location. Logical position is integer x/y plus cardinal facing. A command turns 90 degrees or requests a one-cell move; collision checks the destination before commitment. Smooth visual interpolation is optional and never permits free movement or a second logical command during the step. Backward movement can use the same legality check. No height changes, procedural generation, streaming, or pathfinding are required.

Proposed rendering experiment: simple textured corridor geometry with a first-person camera anchored to the logical cell/facing, nearest-filtered test textures, and a deliberate low-resolution presentation test. Exact internal resolution and art direction need a visual check, not a permanent engine-wide assumption. DungeonScreen draws; a small grid model handles legality/triggers. One event occurs after a committed step. Encounter transition records the originating trigger ID and position; victory marks it resolved before returning so standing on it does not instantly refight. Defeat enters a clearly labeled test failure/restart flow. Ordinary defeat does not spend a Judgment attempt. Temporary keyboard actions are Forward, Back, Turn Left/Right, Interact, Band, and Menu; display bindings rather than baking them into rule code.

The loop needs session persistence across screens, not disk saves yet. Add DTO-friendly stable IDs and version fields in anticipation of saving, but do not decide anti-reload Judgment behavior by accident. Keep one in-memory restart/new-test-run control distinct from continuing the same playthrough.

**H. Minimal player interface.**

| Moment | Required information |
| --- | --- |
| Forming a Band | Four named musicians, Race each, protagonist’s equipped instrument, active/reserve distinction, known Arrangement changes, unavailable reasons |
| Unknown combination | Visible participant portraits/names/Races, generic Try combination entry; do not print the undiscovered final name |
| First successful discovery | NEW ARRANGEMENT, actual name, participants, and one concise mechanical explanation; Book entry recorded once |
| Selecting an Arrangement | Who initiates/contributes, MP per person, K icons, target choices, conditional follow-up, readiness/commitment, and telegraphed interruption risk |
| Resolving it | Ordered component attack names and effects, one final Press Turn result, one coherent result sequence |
| Groove rises/falls | Number with the source round and reasons, e.g. Cooperation +4; Arrangement interrupted −4; history decay labeled separately |
| Swapping musicians | Gained/lost known options, waiting-until-next-phase marker, no fictitious score reward simply for swapping |
| Enemy Band | Visible ready protection/cue and associated musicians; feedback when losing one actually removes the interaction |
| Battle end | Outcome, final short performance summary, discoveries/acquisitions, return to exploration |
| Judgment harness | Desire hidden from the normal test-player view, evaluation hints, attempts remaining; developer view exposes weights/evidence for verification |

Use an explicit viewport/resize policy and fonts that contain required glyphs. Icons may be drawn shapes or supported glyphs; no need for a full UI asset suite. Timed text is presentation-only and can be skipped/advanced. One representative demon animation and a short ambient cue/silence transition can establish tone; no layered reactive soundtrack or MIDI loading is required. Michael supplies final character writing, visual identity, and God dialogue.

**I. Focused test plan and acceptance evidence.** Rules tests run without a window, asset textures, or sleeps. Add a small Java test setup compatible with the existing build after approval. Fixed seeds plus controllable hit/result inputs cover outcome branches. This specification does not claim those tests have run.

| Test group | Minimum cases and expected invariant |
| --- | --- |
| Content contract | Existing level/affinity/basic attack survive loading; legacy aliases resolve visibly; all selected skills/statuses exist; unsupported active behavior fails with file/ID/field; case-exact packaged asset paths work |
| Normal matching | Piano+Sax matches Blue Note; Piano alone does not; extra unrelated active musician does not invalidate a subset recipe; reserve Sax does not count |
| Duplicate instruments | Antiphony requires two unique Trumpets and two unique Trombones; one actor cannot fill two normal slots; different same-Race actors retain independent stats/skills/HP |
| Virtuoso | Piano/Sax fixture fills those two distinct roles only when permitted; not two Pianos; one HP/MP/icon; combined MP cost; Muted/death disables both; reserves cannot supply identities |
| Discovery | Invalid pair never records; interrupted attempt never records; successful pair/trio/quartet records exactly once; ready-but-unused Locked In is not discovered; enemy use does not unlock player Book |
| Resolution | Normal, weak, resist, nullify, repel, absorb, miss, crit; dead/Muted/Rushing participant rejected before payment; source basic/skill authorization enforced; target death skips remaining invalid hits |
| Multi-outcome | Weak+resist gives one bonus; weak+miss gives miss penalty; crit+weak never two bonuses; any repel/absorb clears remaining pool once; component damage is not rolled back |
| Cost | K=2/3 with full and flashing pools; insufficient pool rejects; invalid command leaves MP/icons/RNG unchanged; committed interruption spends K/MP once; no child move icon/MP double charge |
| Actor scheduling | Circular actor order; supported contributors keep ordinary commands; once-per-phase Arrangement participation; Walking Solo schedules Sax next without granting icons; Dragging and death remain deterministic |
| Statuses | DOT exact tick count; Rushing skips once; Testified tracks correct move/MP and explicit zero-MP behavior; null creates nothing; Muted/Dragging clear at specified time; Vamp only for D; refresh versus stacking |
| Enemy demonstration | Kill/disable Bass or Percussion breaks ready Locked In; kill/disable cue owner disarms cue; bypass marked musician avoids interrupt; no repeated reward from already broken readiness |
| Groove | C once per round; repeated action signature; variety does not come from renamed/same-family attacks; miss capped per round; interruption and death separately explained; enemy-caused recovery once; no positive basic-attack counter |
| Adaptation | Swap alone gives zero; useless replacement gives zero; replacement enabling an unavailable interaction after hostile disruption gives A once; player-created break cannot farm R/A |
| History/levels | Worked arithmetic; round roll reason; final partial round frozen; old performance ages out; no accumulating combat XP; fixed L ignores reserves, death, swaps, and Virtuoso double roles |
| Transitions | Encounter enters once; result/rewards commit once; return cell/facing correct; dead actors cannot act; no re-entry loop; scene changes retain roster/Book; ordinary defeat not a Judgment attempt |
| Judgment harness | Diverse interpretations can pass same desire; four brass with poor performance can fail; exact attempt count; duplicate submission idempotent; fail 1/2 returns feedback; fail 3 terminal; error/preview consumes no attempt |

Tests for trio/quartet algorithms can use tiny synthetic definitions even if that candidate does not expose both sizes in play; do not add unnecessary recruitable content just to satisfy a unit test. Manual playtests then verify menu comprehension, Book discovery, sound/silence, resize behavior, and whether two Bands actually encourage different decisions. Capture one packaged-launch smoke test outside Eclipse; continue developing in Eclipse.

Acceptance gate: a tester completes the exploration/battle/return loop, changes Band, demonstrates at least two different interaction types, explains a positive and negative Groove change, reacts to enemy disruption, discovers a recipe without prior recipe-name disclosure, and can articulate a reason to prefer each tested Band in some circumstance. If the loop plays like the same attacks with different damage numbers, the slice fails even if its unit tests pass.

**J. Data/schema changes and proposed file placement.** All new paths are proposals under Guise_Proj, not existing files. Keep authored names and IDs. Do not mass-rewrite the catalog before a round-trip comparison.

| File/class | Exact contract change |
| --- | --- |
| assets/demons/*.JSON; DemonDefinition | Add schemaVersion to migrated documents; model existing level, base_level, max_skill_level, basicAttack, affinities, learnableSkills without dropping values. Preserve each demon’s race string and definition ID |
| assets/player/player.JSON | Preserve legacy Human value as originRace during explicit migration; add unlockedInstrumentIds and equippedInstrumentId. Its effective Band Race comes from equipped instrument; do not count Human as a musical role |
| assets/races/races.json — new | Stable instrument IDs plus authored display names; basicAttackId, statWeights, optional musical evidence tags. Starts with current eight Races; data list remains extensible. Race defaults do not overwrite a demon’s individually authored stats/skills/basic attack |
| assets/moves/moves.JSON; Move | Keep all fields/names. Add typed access for only active effects: target aliases, status, critRate when granted, accuracyDebuffPercent for D. Preserve unknown/deferred fields in imported raw content and distinguish deferred from executable; no silent field discard on save |
| assets/moves/status.JSON | Retain five records. Add explicit lifecycle parameters to active effects and new definitions for muted/dragging, both already referenced by moves. Do not invent remaining statuses merely to silence validation |
| assets/arrangements/arrangements.json — new | id, displayName, prototype flag, required role list with multiplicities, permitted initiator roles, required component move IDs, iconCost, per-role MP costs, effectKind, small effect parameters, effectFamily, completion/discovery rule, optional dual-role compatibility |
| assets/prototype/slice.json — new | Selected candidate ID, fixture roster/loadout grants, acquisition flags, encounter definitions, explicit skill availability, protagonist unlocks, starting level and seed; no changes disguised as canonical demon balancing |
| assets/prototype/groove-rules.json — new | formulaVersion, historyRounds, event weights/caps, recovery window, repetition threshold, level coefficient/clamp |
| assets/prototype/judgment-desire.json — new | One TEST BRASS desire definition with evidence weights, diagnostic categories, threshold, hint IDs, and prototype marker |
| assets/prototype/dungeon.json — new | Grid cells, spawn/facing, event/encounter/rest trigger IDs and one-shot flags |
| dev/DemonConfig.java | Share Race/content DTOs and validation; preserve existing budget/stat generation; add open/save round trip without deleting unknown move fields; validate IDs, capacities, learn-level bounds and referenced skills; runtime preview reports unsupported selected behavior |

Temporary canonicalization: accept old phys as physical; accept Drain as the runtime absorption state while preserving the display label; accept all and aoe as all-enemy targeting in these offensive records. Do not normalize instrumentType drums into a musician Race automatically. Races remain Percussion, Trombone, etc.; move tags are separate metadata. Old elements/ailments data is imported with a visible migration report; unsupported ailment-affinity behavior stays deferred rather than lost. Keep existing agility-scaled accuracy as the prototype baseline. For Time Out specifically, if its later skill is granted, treat its existing guaranteed_hit tag as bypassing the accuracy roll; otherwise leave it ungranted. Do not clamp its authored hitRate 2.0 and silently assume that implements the intended guarantee.

Proposed Java locations, all beneath core/src/main/java unless indicated:

- content/: RaceDefinition, canonical DTOs/validation, catalog loading. Existing battle definitions can move here incrementally after imports/tests prove behavior; no relocation-only rewrite required.
- band/: BandComposition, ArrangementDefinition, participant assignments. A handful of ordinary classes, no generalized matching language.
- battle/: retain entities/context/controller/engine; extract MoveResolver and ArrangementResolver and add typed BattleResult/facts. Small related result types may live together initially.
- groove/: GrooveCalculator, GrooveState, PerformanceSummary; calculation has no dependency on a Screen or Gdx graphics.
- judgment/: DivineDesireDefinition, JudgmentInput, JudgmentEvaluation, JudgmentState; one evaluator for the one test desire.
- game/: GameSession and recruited MusicianInstance/protagonist state, if separating persistent data from battle wrappers is clearer during implementation.
- dungeon/: GridMap and grid movement/trigger logic; retain screens/DungeonScreen as the view.
- tests: core/src/test/java with content fixtures under core/src/test/resources.

Separating DemonConfig into its own Gradle tools module is optional after the shared contract works. It solves shipping editor-only dependencies, but does not block the first playable experiment. Keep Java 11-compatible source conventions unless a separate toolchain decision is approved.

**K. Incremental migration and preservation.**

1. Tag/copy the approved source baseline in version control. Retain the current Player-versus-JJJ scenario as a selectable regression fixture.
2. Add adapters/validation that read the old documents correctly. Compare names, IDs, stats, skills, levels, affinities, and basic attacks before and after conversion. Fix filename references/case explicitly.
3. Establish one authoritative Race mapping shared by the runtime and DemonConfig. Export/import a demon round trip and prove that no authored information disappears. Emit a migration report for aliases and deferred fields.
4. Add the selected fixture manifest and new Arrangement data beside existing catalogs. Keep fixture grants visible and separate. No silent Sirabhorn downleveling, attack renaming, or generic-class substitution.
5. Expand one-versus-one context into actor collections while running the original scenario unchanged in intended behavior. Correct authoritative outcome handling as a distinct bug fix.
6. Extract move resolution and typed results; prove damage/affinity/icon equivalence before applying new coordination recipes. Preserve named component moves inside recipe playback.
7. Add Band matching and one Arrangement at a time. Only then feed their typed facts into Groove. Never infer a musical event by parsing battle text.
8. Add enemy readiness/disruption, discovery/Book, and swapping. Keep each change behind the fixture scenario until its focused tests pass.
9. Add the separate Virtuoso/Judgment harnesses, then the tiny grid map and session transitions. Protect the original combat harness as a faster regression entry point.

If approved schema work becomes a rewrite of individual DTOs, the reason is concrete: existing models discard authored behavior and the tool/runtime disagree. Incremental migration is sufficient; preserve old-file readers until conversion is verified, retain backups, compare exports, and document any intentionally unavailable behavior. No engine-wide replacement is justified.

**L. Explicit non-goals.** No mass demon/location/act authoring; no completed endings or God dialogue; no final Groove formula; no full fusion system; no complete recruitment negotiation; no full leveling economy; no disk saves, cloud saves, or anti-reload policy; no permanent Virtuoso progression; no all-82-move implementation; no full enemy Band AI; no procedural dungeons; no Nutville rhythm minigame; no ordinary rhythm-timed combat; no MIDI import; no live stem-mixing requirement; no ECS, DI/event-bus/plugin framework, or scripting language; no Steam integration or broad platform certification. Keep valid future content and boundary data rather than erasing these ideas.

Future fusion needs only stable definition/instance IDs and a roster transaction boundary: given selected acquired instances, a later fusion policy proposes consumed IDs and a resulting instance, then session validation commits atomically. No recipes, inheritance algebra, or narrative explanation is selected here. Future saves serialize session state, stable IDs and versioned data; never screens, fonts, event playback, or mutable shared definitions.

**M. Implementation order and exit conditions — after approval only.**

| Milestone | Bounded work | Exit condition |
| --- | --- | --- |
| 0. Approve the experiment | Michael selects pair and alters/accepts Temporary rules | One selected manifest, three-to-five player recipes, explicit disputed-rule decisions |
| 1. Stabilize content contract | Case-correct paths, lossless DTOs/aliases, selected-effect validation, existing 1v1 result fix, test setup | JJJ’s level/affinities/Blast survive loading; an unsupported active effect fails clearly; original scenario reaches a committed result |
| 2. Model Band composition | Active/reserve identity, protagonist equipment, multiplicity matching and usability reasons | Pair/trio/quartet and duplicate tests pass; no rendering required; Race is distinct from individual identity |
| 3. Extract results and expand combat | Ordinary resolver, actor lists/order/targets, seedable RNG, typed results | Four allies versus fixture enemies can finish a battle; no double charge, stale outcome, or presentation-driven rules |
| 4. First two Arrangements | Selected pair’s simplest two contrasting recipes and K-icon contract | Normal/weak/resist/miss/interruption-invalid boundary tests pass; visible gameplay difference demonstrated in harness |
| 5. Complete selected recipes and interaction loop | Remaining selected recipes, supported statuses, swapping, enemy readiness/cue, Book discovery | Both Bands usable, one enemy interaction can be broken, first successful discovery records once |
| 6. Add provisional Groove | Typed event ledger, bounded categories, history and examples | Numeric examples/tests match; UI explains changes; basic attacks/swap alone cannot farm positive credits |
| 7. Small harnesses and combat playtest | Isolated dual-identity Virtuoso and one-desire Judgment; deterministic replay fixture and player feedback | Multiple desire interpretations pass tests, exactly three failures terminate test run, users can explain the two Bands and score |
| 8. Tiny grid dungeon | Grid movement/turn/collision, one event/rest, two triggers, representative presentation | Enter/leave battle preserves musicians and cell/facing; no immediate re-trigger; no free movement |
| 9. Progression loop and packaged slice | Acquisition/instrument rewards, shared session, first-discovery flow, readable UI, minimal audio/animation, packaged launch check | A fresh tester completes the A loop and meets section I’s behavioral acceptance gate |
| 10. Evaluate | Review choices, failures, balance, workflow cost and whether it feels like GUISE | Decide what to revise before expanding content; do not call the core proven merely because implementation is complete |

Milestones do not imply one enormous change each; split code changes into reviewable increments with the named exit conditions. Stabilize first, prove combat interaction next, build the tiny dungeon afterward. No production deadline is asserted by this spec.

**N. Minimal Judgment harness: one concept, multiple interpretations.** Intended: one Divine Desire per playthrough, learned through clues; three opportunities named The Audition, The Rebuttal, and The Final Performance. Temporary harness: one hidden TEST BRASS desire and structured result data. Do not build the campaign clue network or final dialogue. A test clue card can show an author-supplied hint ID; developer mode may expose all evidence/weights.

JudgmentInput contains a snapshot of current musicians and their effective identities, per-musician performance contributions, last-three-round Groove ledger with formula version, disruption/recovery facts, and desire ID. Definition IDs and runtime instance IDs are separate. No evaluation reads English move names or invents Race from names. The player presents a performed interpretation, not just an equipped list.

Temporary BRASS evidence uses three normalized dimensions. I is the share of effective musical contributions credited as brass over the evaluated window, with each musician contributing at most one unit per round; use the fraction of that musician’s successful contributions carrying brass evidence, so one token brass action cannot relabel everything they did. Trumpet/Trombone performances may carry that evidence under the test registry. Virtuoso contributes one actor’s evidence, not two votes. A specifically approved non-brass move such as Brass Sting may carry brass evidence independently of the actor’s Race; it does not turn a Saxophone into Trumpet for Arrangement matching. These evidence tags are proposed authored metadata, not automatically inferred from the word brass.

C = min(1, meaningful completed Arrangements / 2) over those three rounds. A = min(1, 0.5 × credited recovery incidents + 0.5 × credited useful substitutions), using the same deduplicated hostile-incident facts as Groove. Compute desire fit F = 0.5I + 0.3C + 0.2A. For this harness only, pass when F ≥ 0.60 and recent performance G−L ≥ 2.0. The preparation term cannot purchase interpretation or replace performance. Keep all raw dimensions/evidence so the result can explain shortcomings.

| Synthetic evaluation | I | C | A | F | Recent performance | Test result |
| --- | --- | --- | --- | --- | --- | --- |
| Four brass, only uncoordinated repetition | 1.0 | 0 | 0 | 0.50 | 0 | Fail despite exact instrumentation |
| Three brass plus a contributing Piano/Saxophone Virtuoso | 0.75 | 1 | 0 | 0.675 | 3 | Pass |
| Two brass in a coordinated, adaptive mixed Band | 0.5 | 1 | 0.5 | 0.65 | 3 | Pass |
| Nonstandard Band using approved brass-bearing abilities in most meaningful contributions | 0.75 | 1 | 0 | 0.675 | 3 | Pass without an exact Race recipe |
| Same interpretation, poor recent performance | 0.75 | 1 | 0 | 0.675 | 1 | Fail on performance, not instrumentation |

These are evaluator fixtures, not new playable demons or final solutions. The formula itself could become a checklist if exposed as the God’s final logic or supported by only one content route. The test therefore explicitly requires multiple routes and explanation quality; full semantic interpretation remains a design task. Do not introduce a “four Trumpets required” hard gate. A later desire can weight different evidence with small explicit policies; do not build a general rules language for one test.

Output: passed/failed, dimension values, supporting/missing evidence IDs, and hint categories such as IDENTITY_UNCLEAR, COORDINATION_MISSING, or PERFORMANCE_DISRUPTED. Michael supplies the God’s words. Failed attempts one and two return actionable hint categories and permit continued exploration. On the third failure, set the terminal result and emit the special-cutscene hook, then Game Over. On success, end this test sequence with a success hook; do not choose an ending.

JudgmentState stores playthroughId, desireId, attemptsUsed, evaluation history, committed submission IDs, and terminal state. Preview/evaluation errors consume no attempt. A valid committed submission increments once whether pass or fail; duplicate delivery cannot consume another. No fourth committed submission is accepted. A developer reset creates a new explicitly labeled test playthrough; it does not quietly refund an attempt. Disk persistence, crash recovery, abandoned attempts, and reload rules remain unresolved until save design. In-memory semantics are sufficient to test this boundary.

**O. Approval checklist and remaining decisions.** The new brief already answers movement, rhythm combat, and the meaning of three attempts; those are not being asked again.

Before implementation, choose the Band pair and review these decisions:

1. Which pair A–D should define the live roster and recipes? You may mix proposals, but then we should explicitly recalculate the fixture/recipe scope.
2. Accept or change shared K-icon cost plus once-per-phase contributor commitment, with ordinary contributor commands retained?
3. Accept or change the concrete Arrangement effects and the single telegraphed test interruption? All are prototypes, including effects attached to your proposed names.
4. Accept or change the status/death interpretations, especially zero-MP Testified, Muted allowing basic attacks, proposed durations, and whole-Band rather than protagonist-only defeat? Preserve the source descriptions even if the selected playable subset must wait for this answer.
5. Accept the explained three-round Groove experiment and small capped level term as a starting hypothesis? It is not your final formula.
6. Accept one TEST BRASS Judgment evaluator plus an isolated dual-identity Virtuoso test, with no final story content or save/reload policy?

Implementation stops here until those choices are approved. The intended outcome to test remains: the player can explain not only that the demons have instruments, but that the Band they made changed what they could do.
