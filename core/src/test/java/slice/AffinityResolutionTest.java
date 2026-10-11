package slice;

import battle.*;
import game.GameSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

class AffinityResolutionTest {
	@ParameterizedTest
	@CsvSource({ "neutral,NORMAL,3", "weak,WEAKNESS,4", "resist,RESIST,3", "null,NULLIFY,2", "repel,REPEL,0",
			"drain,ABSORB,0" })
	void ordinaryAffinityOutcomes(String affinity, String result, int icons) {
		GameSession s = new GameSession(ContentBandTest.catalog());
		SliceBattle b = SliceCombatTest.battle(s, new SliceCombatTest.FixedRandom());
		b.enemies.get(0).affinities.put("physical", affinity);
		BattleResult r = b.move(s.active.get(0).basicAttack, b.enemies.get(0));
		assertEquals(PressTurnEngine.TurnResult.valueOf(result), r.turnResult);
		assertEquals(icons, b.turns.iconCount());
	}

	@Test
	void critWeakDoesNotStackIcons() {
		GameSession s = new GameSession(ContentBandTest.catalog());
		s.content.moves.get("chord").critRate = 1f;
		SliceBattle b = SliceCombatTest.battle(s, new SliceCombatTest.FixedRandom());
		b.enemies.get(0).affinities.put("physical", "weak");
		BattleResult r = b.move("chord", b.enemies.get(0));
		assertTrue(r.facts.stream().anyMatch(f -> f.type == BattleResult.Type.CRITICAL));
		assertTrue(r.facts.stream().anyMatch(f -> f.type == BattleResult.Type.WEAKNESS));
		assertEquals(4, b.turns.iconCount());
	}

	@Test
	void multiTargetWeakResistBonusButWeakMissPenalty() {
		GameSession s = new GameSession(ContentBandTest.catalog());
		SliceBattle b = SliceCombatTest.battle(s, new SliceCombatTest.FixedRandom());
		BattleEntity second = ContentBandTest.actor("e2", "Bass");
		second.currentHp = second.maxHp = 500;
		b.enemies.add(second);
		b.enemies.get(0).affinities.put("physical", "weak");
		second.affinities.put("physical", "resist");
		BattleResult r = b.arrange(SliceCombatTest.recipe(b, "full_chorus"), b.enemies.get(0), null, null);
		assertEquals(PressTurnEngine.TurnResult.WEAKNESS, r.turnResult);
		assertEquals(2, b.turns.iconCount());
		s = new GameSession(ContentBandTest.catalog());
		b = SliceCombatTest.battle(s, new SliceCombatTest.FixedRandom());
		second = ContentBandTest.actor("e2", "Bass");
		second.stats.agi = 1000;
		b.enemies.add(second);
		b.enemies.get(0).affinities.put("physical", "weak");
		r = b.arrange(SliceCombatTest.recipe(b, "full_chorus"), b.enemies.get(0), null, null);
		assertEquals(PressTurnEngine.TurnResult.MISS, r.turnResult);
		assertEquals(0, b.turns.iconCount());
		assertFalse(b.groove.current().credited.contains("W"));
	}

	@Test
	void noContributionEvidenceForMissedIndividualMove() {
		GameSession s = new GameSession(ContentBandTest.catalog());
		SliceBattle b = SliceCombatTest.battle(s, new SliceCombatTest.FixedRandom());
		b.enemies.get(0).stats.agi = 1000;
		BattleResult r = b.move("chord", b.enemies.get(0));
		assertEquals(PressTurnEngine.TurnResult.MISS, r.turnResult);
		assertTrue(r.facts.stream().noneMatch(f -> f.type == BattleResult.Type.CONTRIBUTION));
	}

	@Test
	void samePartyVoluntaryTokenBreakCannotFarmRecovery() {
		GameSession s = new GameSession(ContentBandTest.catalog());
		s.resolvedEncounters.add("1");
		s.acquireReward();
		SliceBattle b = SliceCombatTest.battle(s, new SliceCombatTest.FixedRandom());
		b.defend();
		b.defend();
		b.turns.startTurn(true, 4);
		assertTrue(b.arrange(SliceCombatTest.recipe(b, "locked_in"), null, null, null).accepted);
		assertSame(s.active.get(3), b.actor());
		BattleResult r = b.swap(3, 0);
		assertTrue(r.accepted);
		assertTrue(r.facts.stream().noneMatch(f -> f.type == BattleResult.Type.ARRANGEMENT_INTERRUPTED));
		assertEquals(0, b.groove.current().score);
	}
}
