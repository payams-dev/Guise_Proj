package slice;

import band.*;
import battle.*;
import content.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ContentBandTest {
    static ContentCatalog catalog(){return new ContentCatalog(p->{try{return Files.readString(Path.of(System.getProperty("guise.assets","assets"),p));}catch(Exception e){throw new IllegalArgumentException(p,e);}});}
    static BattleEntity actor(String id,String race){BattleEntity e=new BattleEntity(id,id,race,100,50);e.level=3;e.stats=new BattleEntity.Stats(5,5,5,5,5);return e;}
    @Test void existingContentSurvives(){ContentCatalog c=catalog();BattleEntity e=c.musician("demons/JJJ.JSON","jjj:1");assertEquals(3,e.level);assertEquals("blast",e.basicAttack);assertEquals(Affinity.WEAK,e.getAffinityFor("soul"));assertEquals(Affinity.RESIST,e.getAffinityFor("physical"));assertEquals(82,c.moves.size());assertEquals(Affinity.ABSORB,Affinity.fromString("Drain"));}
    @Test void unsupportedMoveFailsInsteadOfPretending(){assertTrue(assertThrows(IllegalArgumentException.class,()->catalog().validateActiveMove("isotope")).getMessage().contains("unsupported active field"));}
    @Test void duplicateInstrumentsNeedDistinctMusicians(){ArrangementDefinition d=new ArrangementDefinition();d.roles=Arrays.asList("Trumpet","Trumpet","Trombone");BattleEntity a=actor("a","Trumpet"),b=actor("b","Trumpet"),t=actor("t","Trombone");BandComposition m=new BandComposition();assertTrue(m.match(d,Arrays.asList(a,t)).isEmpty());assertFalse(m.match(d,Arrays.asList(a,b,t)).isEmpty());assertTrue(m.match(d,Arrays.asList(a,a,t)).isEmpty());}
    @Test void virtuosoUsesTwoDistinctRolesNotTwoBodies(){ContentCatalog c=catalog();BattleEntity v=actor("v","Virtuoso"),b=actor("b","Bass"),p=actor("p","Percussion");v.instrumentIdentities.addAll(Arrays.asList("Piano","Saxophone"));BandComposition m=new BandComposition();assertEquals(1,m.match(c.arrangements.get("full_chorus"),Arrays.asList(v,b,p)).size());ArrangementDefinition r=new ArrangementDefinition();r.dualRoleCompatible=true;r.roles=Arrays.asList("Piano","Piano");assertTrue(m.match(r,Arrays.asList(v)).isEmpty());}
    @Test void deadAndMutedAssignmentsAreStructuralButUnusable(){ContentCatalog c=catalog();BattleEntity p=actor("p","Piano"),s=actor("s","Saxophone");p.addSkill("modal_motif");s.basicAttack="riff";BandComposition.Assignment a=new BandComposition().match(c.arrangements.get("blue_note"),Arrays.asList(p,s)).get(0);assertNull(a.reason(p,4));s.currentHp=0;assertNotNull(a.reason(p,4));s.currentHp=10;s.addStatus(new BattleEntity.ActiveStatus("muted",2,1,"test"));assertNotNull(a.reason(p,4));}
    @Test void costsPreserveFifoPressTurn(){PressTurnEngine e=new PressTurnEngine();e.startTurn(true,4);e.reserveArrangementCost(2);e.consumeIcons(PressTurnEngine.TurnResult.WEAKNESS);assertEquals(3,e.iconCount());assertEquals(PressTurnEngine.IconState.FLASHING,e.getIconPool().peek());e.getIconPool().clear();assertEquals(3,e.iconCount());e.startTurn(true,4);e.reserveArrangementCost(2);e.consumeIcons(PressTurnEngine.TurnResult.MISS);assertEquals(1,e.iconCount());}
    @Test void legacyLibGdxSerializerStillLoadsDemonAndMoveCatalog(){
        com.badlogic.gdx.utils.Json json=new com.badlogic.gdx.utils.Json();json.setIgnoreUnknownFields(true);
        ContentCatalog c=catalog();DemonDefinition d=json.fromJson(DemonDefinition.class,c.object("demons/JJJ.JSON").toString());
        assertEquals(Affinity.RESIST,d.toBattleEntity().getAffinityFor("physical"));
        MoveDatabase moves=json.fromJson(MoveDatabase.class,c.object("moves/moves.JSON").toString());assertEquals(82,moves.moves.size());
    }
}
