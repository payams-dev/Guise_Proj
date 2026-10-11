package slice;
import battle.*;
import band.*;
import game.GameSession;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class EndToEndTest {
 /** Fixture strategy, no stat changes: show one leader combination, then exploit authored weaknesses. */
 static void play(SliceBattle b,String firstRecipe){
  int commands=0;boolean attempted=false;
  while(b.outcome==SliceBattle.Outcome.ONGOING&&commands++<250){
   if(b.actor()==null){b.advancePhase();continue;}
   if(!b.playerPhase){assertTrue(b.enemyStep().accepted);continue;}
   BattleEntity actor=b.actor();
   if(actor.hasStatus("rushing")||actor.hasStatus("testified")){b.forcedAction();continue;}
   if(!attempted){Optional<BandComposition.Assignment> a=b.arrangements().stream().filter(x->x.recipe.id.equals(firstRecipe)&&x.reason(actor,b.turns.iconCount())==null).findFirst();if(a.isPresent()){BattleEntity target=b.enemies.stream().filter(BattleEntity::isAlive).sorted(Comparator.comparingInt(e->e.getAffinityFor("physical")==Affinity.WEAK?0:1)).findFirst().orElseThrow();BattleResult r=b.arrange(a.get(),target,target,"riff");assertTrue(r.accepted);attempted=true;continue;}}
   Move best=null;BattleEntity target=null;double bestValue=-1;
   Set<String> ids=new LinkedHashSet<>(actor.battleSkills);ids.add(actor.basicAttack);
   for(String id:ids){Move move=b.session.content.moves.get(id);if(!actor.hasMp(move.mpCost)||actor.hasStatus("muted")&&!id.equals(actor.basicAttack))continue;for(BattleEntity e:b.enemies)if(e.isAlive()){
    double damage=(move.baseDamage+2*("physical".equals(move.element)?actor.stats.str:actor.stats.mag))*e.getAffinityFor(move.element).modifier;
    double score=Math.min(e.currentHp,damage)*(e.getAffinityFor(move.element)==Affinity.WEAK?1.6:1)*Math.min(1,move.hitRate*actor.stats.agi/(double)Math.max(1,e.stats.agi));
    if(score>bestValue){best=move;target=e;bestValue=score;}
   }}
   assertNotNull(best);assertTrue(b.move(best.id,target).accepted);
  }
  assertTrue(commands<250,"Battle must terminate");
  System.out.println("Encounter "+b.encounterId+": "+b.outcome+", rounds="+b.groove.current().number+", Groove="+b.groove.score());
  assertEquals(SliceBattle.Outcome.VICTORY,b.outcome);
 }
 @Test void bothBandsRunThroughPersistentSession(){GameSession s=new GameSession(ContentBandTest.catalog());SliceBattle first=s.startBattle("1");play(first,"blue_note");int finalRound=first.groove.current().number;s.finishBattle(first);assertEquals(finalRound,s.lastPerformance.current().number);assertTrue(s.discoveries.contains("blue_note"));assertThrows(IllegalStateException.class,()->s.startBattle("1"));s.acquireReward();assertEquals(1,s.reserve.size());s.equip("trumpet");s.rest();SliceBattle second=s.startBattle("2");assertEquals(4,s.active.size());play(second,"call_response");s.finishBattle(second);assertTrue(s.resolvedEncounters.containsAll(Arrays.asList("1","2")));assertNotNull(s.lastPerformance);assertEquals(0,s.judgment.attemptsUsed());assertTrue(s.discoveries.contains("call_response"));}
 @Test void dungeonHasReachableEventsAndWalls(){GameSession s=new GameSession(ContentBandTest.catalog());int x=s.x,y=s.y;s.turn(-1);assertFalse(s.move(1));assertEquals(x,s.x);assertEquals(y,s.y);Set<String> seen=new HashSet<>();ArrayDeque<int[]> queue=new ArrayDeque<>();queue.add(new int[]{s.x,s.y});Set<Character> events=new HashSet<>();while(!queue.isEmpty()){int[] p=queue.remove();if(!seen.add(p[0]+","+p[1]))continue;events.add(s.map.at(p[0],p[1]));for(int d=0;d<4;d++){int nx=p[0]+dungeon.GridMap.DX[d],ny=p[1]+dungeon.GridMap.DY[d];if(s.map.walkable(nx,ny))queue.add(new int[]{nx,ny});}}assertTrue(events.containsAll(Arrays.asList('R','1','2','U','J')));}
}
