package game;

import battle.*;
import content.*;
import dungeon.GridMap;
import groove.GrooveCalculator;
import judgment.*;
import java.util.*;

/** Owns musicians and progression across screen changes; never a graphics resource owner. */
public final class GameSession {
    public static class Configuration {
        public String candidate,startInstrument,rewardInstrument,reserveReward;
        public long seed;
        public int playerLevel,enemyHpScale,enemyMpBonus;
        public List<String> startingMembers=new ArrayList<>();
        public Map<String,Map<String,String>> secondEncounterAffinities=new LinkedHashMap<>();
    }
    public final ContentCatalog content;
    public final Configuration config;
    public final List<BattleEntity> active=new ArrayList<>(),reserve=new ArrayList<>();
    public final Set<String> discoveries=new LinkedHashSet<>(),resolvedEncounters=new HashSet<>(),unlockedInstruments=new LinkedHashSet<>();
    public final GridMap map;
    public final JudgmentEvaluator.Desire desire;
    public final JudgmentState judgment;
    public GrooveCalculator lastPerformance;
    public int x,y,facing;
    public boolean acquiredReward;
    private SliceBattle currentBattle;
    public GameSession(ContentCatalog content){
        this.content=content;config=content.load("prototype/slice.json",Configuration.class);
        BattleEntity player=content.musician("player/player.JSON","player");player.level=config.playerLevel;active.add(player);
        int i=0;for(String path:config.startingMembers)active.add(content.musician(path,"ally:"+(++i)));
        unlockedInstruments.add(config.startInstrument);equip(config.startInstrument);
        map=content.load("prototype/dungeon.json",GridMap.class);x=map.spawnX;y=map.spawnY;facing=map.facing;
        desire=content.load("prototype/judgment-desire.json",JudgmentEvaluator.Desire.class);judgment=new JudgmentState(desire.id);
    }
    public void equip(String instrument){
        if(currentBattle!=null)throw new IllegalStateException("Change instruments outside battle");
        if(!unlockedInstruments.contains(instrument))throw new IllegalArgumentException("Instrument not acquired");
        RaceDefinition race=content.race(instrument);BattleEntity player=active.get(0);player.race=race.displayName;player.basicAttack=race.basicAttackId;
    }
    public boolean move(int direction){int nx=x+GridMap.DX[facing]*direction,ny=y+GridMap.DY[facing]*direction;if(!map.walkable(nx,ny))return false;x=nx;y=ny;return true;}
    public void turn(int delta){facing=Math.floorMod(facing+delta,4);}
    public void acquireReward(){
        if(acquiredReward||!resolvedEncounters.contains("1"))return;
        acquiredReward=true;reserve.add(content.musician(config.reserveReward,"ally:jjj"));unlockedInstruments.add(config.rewardInstrument);
    }
    public void fieldSwap(int slot,int reserveIndex){
        if(currentBattle!=null||slot<1||slot>=active.size())throw new IllegalArgumentException("Invalid field swap");
        BattleEntity old=active.set(slot,reserve.get(reserveIndex));reserve.set(reserveIndex,old);
    }
    public void rest(){if(currentBattle!=null)throw new IllegalStateException("No field rest during battle");for(BattleEntity e:allMusicians()){e.currentHp=e.maxHp;e.currentMp=e.maxMp;e.activeStatuses.clear();e.lockedMoveId=null;e.lockedTargetId=null;}}
    public List<BattleEntity> allMusicians(){List<BattleEntity> all=new ArrayList<>(active);all.addAll(reserve);return all;}
    public SliceBattle startBattle(String id){
        if(currentBattle!=null||resolvedEncounters.contains(id)||judgment.gameOver)throw new IllegalStateException("Encounter unavailable");
        List<BattleEntity> enemies=new ArrayList<>();
        String[] paths={"demons/JJJ.JSON","prototype/demons/test_bass.json","demons/Ambassador.JSON"};
        for(int i=0;i<paths.length;i++){
            BattleEntity e=content.musician(paths[i],"enemy:"+i);
            if("2".equals(id))e.affinities.putAll(config.secondEncounterAffinities.getOrDefault(e.race,Collections.emptyMap()));
            e.maxHp*=config.enemyHpScale;e.currentHp=e.maxHp;e.maxMp+=config.enemyMpBonus;e.currentMp=e.maxMp;enemies.add(e);
        }
        currentBattle=new SliceBattle(this,id,enemies,new Random(config.seed+id.hashCode()));return currentBattle;
    }
    public void finishBattle(SliceBattle battle){
        if(currentBattle!=battle)return;
        if(battle.outcome==SliceBattle.Outcome.ONGOING)throw new IllegalStateException("Battle unfinished");
        lastPerformance=battle.groove;
        if(battle.outcome==SliceBattle.Outcome.VICTORY)resolvedEncounters.add(battle.encounterId);
        for(BattleEntity e:allMusicians()){e.activeStatuses.clear();e.defending=false;e.arrangementCommitted=false;e.waitingForNextPhase=false;e.lastMoveId=null;e.lockedMoveId=null;e.lockedTargetId=null;e.replacedInstanceId=null;}
        currentBattle=null;
    }
}
