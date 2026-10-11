package groove;

import battle.BattleResult;
import battle.BattleResult.Fact;
import battle.BattleResult.Type;
import java.util.*;

/** Replaceable v1 performance calculation. No graphics, timers or parsed UI text. */
public final class GrooveCalculator {
    public static class Rules {
        public int formulaVersion=1,historyRounds=3,baseLevel=3,maxLevelOffset=8,recoveryWindow=3,repetitionThreshold=3;
        public double levelCoefficient=.25;
        public Map<String,Integer> weights=new LinkedHashMap<>();
    }
    public static final class Round {
        public final int number;
        public int score,arrangements,recoveries,adaptations;
        public final Set<String> credited=new HashSet<>();
        public final List<String> reasons=new ArrayList<>();
        public final Map<String,int[]> contributions=new LinkedHashMap<>();
        Round(int number){this.number=number;}
    }
    private static final class Incident {
        final int round; boolean recovered,adapted;
        Incident(int round){this.round=round;}
    }
    public final Rules rules;
    public final double preparation;
    private final List<Round> history=new ArrayList<>();
    private final Set<Long> processed=new HashSet<>();
    private final Map<String,Integer> families=new HashMap<>();
    private final Map<String,Incident> incidents=new LinkedHashMap<>();
    private final Map<String,String> replacements=new HashMap<>();
    private Round current=new Round(1);
    private String lastSignature="",lastFamily="";
    private int repetitions;
    public GrooveCalculator(Rules rules,double meanLevel){
        if(rules.historyRounds<1||rules.repetitionThreshold<2)throw new IllegalArgumentException("Invalid Groove rules");
        this.rules=rules;preparation=rules.levelCoefficient*Math.max(0,Math.min(rules.maxLevelOffset,meanLevel-rules.baseLevel));
    }
    public Round current(){return current;}
    public void nextRound(){history.add(current);current=new Round(current.number+1);current.reasons.add("New round: older performance leaves the window");}
    public List<Round> window(){List<Round> a=new ArrayList<>(history);a.add(current);return Collections.unmodifiableList(a.subList(Math.max(0,a.size()-rules.historyRounds),a.size()));}
    public double score(){return preparation+window().stream().mapToInt(r->r.score).sum()/(double)rules.historyRounds;}
    public int completedArrangements(){return window().stream().mapToInt(r->r.arrangements).sum();}
    public int recoveryCount(){return window().stream().mapToInt(r->r.recoveries).sum();}
    public int adaptationCount(){return window().stream().mapToInt(r->r.adaptations).sum();}
    public double brassShare(){double sum=0;int units=0;for(Round r:window())for(int[] v:r.contributions.values()){sum+=v[1]/(double)v[0];units++;}return units==0?0:sum/units;}
    private boolean fresh(Incident i){return current.number-i.round<rules.recoveryWindow;}
    private void credit(String key,String dedup,String why){
        if(!current.credited.add(dedup))return;
        int value=rules.weights.getOrDefault(key,0);current.score+=value;
        current.reasons.add(why+" "+(value>=0?"+":"")+value);
    }
    public void accept(BattleResult result,boolean hasAlternative){
        if(!result.accepted||!processed.add(result.commandId))return;
        if(result.playerCommand){
            if(lastSignature.equals(result.signature))repetitions++;else repetitions=1;
            lastSignature=result.signature;
            if(repetitions>=rules.repetitionThreshold&&hasAlternative)credit("T","T","Repeated pattern");
        }
        boolean weakness=false,bad=false;
        for(Fact f:result.facts){
            if(f.actorPlayer&&f.type==Type.WEAKNESS)weakness=true;
            if(f.actorPlayer&&Arrays.asList(Type.MISS,Type.NULLIFY,Type.REPEL,Type.ABSORB).contains(f.type))bad=true;
        }
        for(Fact f:result.facts){
            if(f.type==Type.CONTRIBUTION&&f.actorPlayer){int[] v=current.contributions.computeIfAbsent(f.actor,k->new int[2]);v[0]++;v[1]+=f.amount;}
            if(f.type==Type.MISS&&f.actorPlayer)credit("M","M","Missed attack");
            if(f.type==Type.DEATH&&f.targetPlayer){credit("D","D:"+f.target,"Musician lost");if(!f.actorPlayer)incidents.put(f.target,new Incident(current.number));}
            if(f.type==Type.STATUS&&f.targetPlayer&&!f.actorPlayer&&Arrays.asList("muted","rushing","testified").contains(f.id))incidents.putIfAbsent(f.target,new Incident(current.number));
            if(f.type==Type.ARRANGEMENT_INTERRUPTED&&f.actorPlayer){credit("B","B","Arrangement interrupted");if(f.target!=null)incidents.putIfAbsent(f.target,new Incident(current.number));}
            if(f.type==Type.ENEMY_BAND_DISRUPTED&&f.actorPlayer)credit("E","E","Enemy ensemble disrupted");
            if(f.type==Type.SWAP&&f.actorPlayer&&f.amount==1)replacements.put(f.target,f.actor);
            if(f.type==Type.ARRANGEMENT_COMPLETED&&f.actorPlayer){
                current.arrangements++;credit("C","C","Cooperation");
                if(weakness&&!bad)credit("W","W","Coordinated weakness");
                boolean other=families.entrySet().stream().anyMatch(e->!e.getKey().equals(f.id)&&current.number-e.getValue()<rules.historyRounds);
                if(other&&!lastFamily.equals(f.id))credit("V","V","Variety");
                lastFamily=f.id;families.put(f.id,current.number);
                for(Incident i:incidents.values())if(fresh(i)&&!i.recovered&&!current.credited.contains("R")){i.recovered=true;credit("R","R","Performance recovered");current.recoveries++;break;}
                for(String participant:f.participants){Incident i=incidents.get(replacements.get(participant));if(i!=null&&fresh(i)&&!i.adapted&&!current.credited.contains("A")){i.adapted=true;credit("A","A","Useful substitution");current.adaptations++;break;}}
            }
        }
    }
}
