package battle;

import java.util.*;

/** One committed command and its ordered facts. Presentation never reapplies them. */
public final class BattleResult {
    public enum Type { MOVE, HIT, MISS, WEAKNESS, CRITICAL, RESIST, NULLIFY, REPEL, ABSORB,
        STATUS, STATUS_REMOVED, DEATH, DEFEND, SKIP, SWAP, CONTRIBUTION,
        ARRANGEMENT_STARTED, ARRANGEMENT_COMPLETED, ARRANGEMENT_INTERRUPTED, DISCOVERY,
        PROTECTION_READY, PROTECTED, ENEMY_BAND_DISRUPTED, CUE, PHASE, OUTCOME }
    public static final class Fact {
        public final Type type;
        public final String actor, target, id;
        public final int amount;
        public final boolean actorPlayer, targetPlayer;
        public final List<String> participants;
        public Fact(Type type,String actor,String target,String id,int amount,boolean actorPlayer,boolean targetPlayer) {
            this(type,actor,target,id,amount,actorPlayer,targetPlayer,Collections.emptyList());
        }
        public Fact(Type type,String actor,String target,String id,int amount,boolean actorPlayer,boolean targetPlayer,List<String> participants) {
            this.type=type;this.actor=actor;this.target=target;this.id=id;this.amount=amount;
            this.actorPlayer=actorPlayer;this.targetPlayer=targetPlayer;
            this.participants=Collections.unmodifiableList(new ArrayList<>(participants));
        }
    }
    public final long commandId;
    public final boolean playerCommand;
    public final String signature;
    public final List<Fact> facts=new ArrayList<>();
    public final List<String> messages=new ArrayList<>();
    public int iconsBefore, iconsAfter;
    public PressTurnEngine.TurnResult turnResult=PressTurnEngine.TurnResult.NORMAL;
    public boolean accepted=true;
    public boolean hadAlternative;
    public BattleResult(long id,boolean player,String signature){this.commandId=id;this.playerCommand=player;this.signature=signature;}
    public static BattleResult invalid(String message){BattleResult r=new BattleResult(-1,true,"invalid");r.accepted=false;r.messages.add(message);return r;}
}
