package battle;

import band.*;
import content.*;
import game.GameSession;
import groove.GrooveCalculator;
import java.util.*;
import static battle.BattleResult.Type.*;
import static battle.PressTurnEngine.TurnResult;

/** Experimental party controller. Legacy BattleController remains the 1v1 regression path. */
public final class SliceBattle {
    public enum Outcome { ONGOING, VICTORY, DEFEAT }
    public final GameSession session;
    public final String encounterId;
    public final List<BattleEntity> enemies;
    public final PressTurnEngine turns=new PressTurnEngine();
    public final GrooveCalculator groove;
    public Outcome outcome=Outcome.ONGOING;
    public boolean playerPhase=true;
    private final Random random;
    private final BandComposition composition=new BandComposition();
    private final List<BattleEntity> order=new ArrayList<>();
    private final Set<String> swappedOut=new HashSet<>();
    private int cursor;
    private long sequence;
    private BattleEntity priority,cueOwner,cueTarget;
    private boolean cueUsed,suppressEnemy;
    private Protection playerProtection,enemyProtection;
    private static final class Protection {
        final BandComposition.Assignment assignment;
        Protection(BandComposition.Assignment a){assignment=a;}
        boolean intact(){for(BattleEntity e:assignment.unique())if(!e.isAlive()||e.waitingForNextPhase||e.hasStatus("muted")||e.hasStatus("rushing")||e.hasStatus("testified"))return false;return true;}
    }
    public SliceBattle(GameSession session,String id,List<BattleEntity> enemies,Random random){
        this.session=session;encounterId=id;this.enemies=enemies;this.random=random;
        groove=new GrooveCalculator(session.content.load("prototype/groove-rules.json",GrooveCalculator.Rules.class),session.active.stream().mapToInt(e->e.level).average().orElse(3));
        beginPhase(true);
    }
    public List<BattleEntity> side(){return playerPhase?session.active:enemies;}
    public List<BattleEntity> opponents(){return playerPhase?enemies:session.active;}
    public BattleEntity actor(){
        if(outcome!=Outcome.ONGOING||!turns.hasIconsLeft())return null;
        if(priority!=null&&side().contains(priority)&&priority.isAlive()&&!priority.waitingForNextPhase)return priority;
        for(int i=0;i<order.size();i++){BattleEntity e=order.get(Math.floorMod(cursor+i,order.size()));if(side().contains(e)&&e.isAlive()&&!e.waitingForNextPhase){cursor=Math.floorMod(cursor+i,order.size());return e;}}
        return null;
    }
    public List<BandComposition.Assignment> arrangements(){List<BandComposition.Assignment> out=new ArrayList<>();for(ArrangementDefinition d:session.content.arrangements.values())out.addAll(composition.match(d,side()));return out;}
    private boolean ally(BattleEntity e){return e!=null&&(session.active.contains(e)||session.reserve.contains(e));}
    private void fact(BattleResult r,BattleResult.Type type,BattleEntity a,BattleEntity t,String id,int amount){r.facts.add(new BattleResult.Fact(type,a==null?null:a.instanceId,t==null?null:t.instanceId,id,amount,ally(a),ally(t)));}
    private BattleResult begin(String signature){BattleResult r=new BattleResult(++sequence,playerPhase&&!"phase".equals(signature),signature);r.iconsBefore=turns.iconCount();r.hadAlternative=hasAlternative(signature);return r;}
    private String blocked(){BattleEntity a=actor();if(a==null)return "No scheduled actor";if(a.hasStatus("rushing"))return "Rushing: advance the forced action";if(a.hasStatus("testified")&&a.lockedMoveId!=null&&a.hasMp(session.content.moves.get(a.lockedMoveId).mpCost))return "Testified: advance the forced action";return null;}
    public BattleResult move(String id,BattleEntity target){return move(id,target,false);}
    private BattleResult move(String id,BattleEntity target,boolean forced){
        if(!forced&&blocked()!=null)return BattleResult.invalid(blocked());
        BattleEntity a=actor();Move m=session.content.moves.get(id);
        if(a==null||m==null||!a.hasSkill(id)||!a.hasMp(m.mpCost)||(!id.equals(a.basicAttack)&&a.hasStatus("muted")))return BattleResult.invalid("Move unavailable");
        if(target==null||!target.isAlive()||!opponents().contains(target))return BattleResult.invalid("Select a living opponent");
        session.content.validateActiveMove(id);
        BattleResult r=begin("move:"+id);a.spendMp(m.mpCost);a.lastMoveId=id;a.lastTargetId=target.instanceId;a.defending=false;
        component(r,a,m,target,null);return finish(r,a,true);
    }
    public BattleResult defend(){if(blocked()!=null)return BattleResult.invalid(blocked());BattleEntity a=actor();BattleResult r=begin("defend");a.defending=true;fact(r,DEFEND,a,a,"guard",0);r.messages.add(a.name+" guards.");return finish(r,a,true);}
    public BattleResult forcedAction(){
        BattleEntity a=actor();if(a==null)return BattleResult.invalid("No actor");
        if(a.hasStatus("rushing")){BattleResult r=begin("skip:rushing");a.removeStatusById("rushing");fact(r,SKIP,a,a,"rushing",0);r.messages.add(a.name+" loses this action to Rushing.");return finish(r,a,true);}
        if(a.hasStatus("testified")){
            Move m=session.content.moves.get(a.lockedMoveId);
            if(m==null||!a.hasMp(m.mpCost)||(a.hasStatus("muted")&&!m.id.equals(a.basicAttack))){a.removeStatusById("testified");a.lockedMoveId=null;a.lockedTargetId=null;return BattleResult.invalid("Testified released: locked move unavailable");}
            BattleEntity target=opponents().stream().filter(e->e.isAlive()&&e.instanceId.equals(a.lockedTargetId)).findFirst().orElseGet(()->opponents().stream().filter(BattleEntity::isAlive).findFirst().orElse(null));
            return move(m.id,target,true);
        }
        return BattleResult.invalid("No forced action");
    }
    public BattleResult swap(int slot,int reserveIndex){
        if(!playerPhase||blocked()!=null)return BattleResult.invalid("Swap unavailable");
        if(slot<1||slot>=session.active.size()||reserveIndex<0||reserveIndex>=session.reserve.size())return BattleResult.invalid("Invalid swap slots");
        BattleEntity incoming=session.reserve.get(reserveIndex),outgoing=session.active.get(slot),a=actor();
        if(!incoming.isAlive()||swappedOut.contains(incoming.instanceId))return BattleResult.invalid("Reserve unavailable this phase");
        BattleResult r=begin("swap");boolean disabled=!outgoing.isAlive()||outgoing.hasStatus("muted")||outgoing.hasStatus("rushing")||outgoing.hasStatus("testified");
        fact(r,SWAP,outgoing,incoming,"reserve",disabled?1:0);
        session.active.set(slot,incoming);session.reserve.set(reserveIndex,outgoing);swappedOut.add(outgoing.instanceId);incoming.waitingForNextPhase=true;outgoing.waitingForNextPhase=true;
        r.messages.add(outgoing.name+" -> "+incoming.name+" (ready next phase)");checkReadied(r,a);return finish(r,a,true);
    }
    public BattleResult arrange(BandComposition.Assignment a,BattleEntity target,BattleEntity response,String solo){
        if(blocked()!=null)return BattleResult.invalid(blocked());
        if(a==null||session.content.arrangements.get(a.recipe.id)!=a.recipe||!side().containsAll(a.unique()))return BattleResult.invalid("Assignment is not active");
        BattleEntity initiator=actor();String reason=a.reason(initiator,turns.iconCount());if(reason!=null)return BattleResult.invalid(reason);
        ArrangementDefinition d=a.recipe;
        if(d.effect!=ArrangementDefinition.Effect.LOCKED_IN&&(target==null||!opponents().contains(target)||!target.isAlive()))return BattleResult.invalid("Select a living target");
        if(d.effect==ArrangementDefinition.Effect.CALL_RESPONSE&&(response==null||!opponents().contains(response)||!response.isAlive()))return BattleResult.invalid("Select response target");
        if(d.effect==ArrangementDefinition.Effect.LOCKED_IN&&(playerPhase?playerProtection:enemyProtection)!=null)return BattleResult.invalid("Protection already readied");
        Map<BattleEntity,Integer> costs=a.costs();
        Move soloMove=null;
        if(d.effect==ArrangementDefinition.Effect.WALKING_SOLO){soloMove=session.content.moves.get(solo);BattleEntity sax=a.role("Saxophone");if(soloMove==null||!("riff".equals(solo)||"modal_motif".equals(solo))||!sax.hasSkill(solo))return BattleResult.invalid("Solo must be a learned Riff or Modal Motif");costs.merge(sax,soloMove.mpCost,Integer::sum);}
        for(Map.Entry<BattleEntity,Integer> c:costs.entrySet())if(!c.getKey().hasMp(c.getValue()))return BattleResult.invalid("Insufficient performer MP");
        BattleResult r=begin("arrangement:"+d.id);turns.reserveArrangementCost(d.iconCost);costs.forEach(BattleEntity::spendMp);for(BattleEntity e:a.unique()){e.arrangementCommitted=true;e.defending=false;}
        fact(r,ARRANGEMENT_STARTED,initiator,target,d.id,0);
        if(playerPhase&&cueOwner!=null&&a.unique().contains(cueTarget)&&cueOwner.canContribute()){
            applyStatus(r,cueOwner,cueTarget,"muted","test_interruption_cue");fact(r,ARRANGEMENT_INTERRUPTED,initiator,cueTarget,d.id,0);r.messages.add("TEST cue interrupts the ensemble; committed costs remain spent.");cueOwner=null;cueTarget=null;
            return finish(r,initiator,true);
        }
        boolean complete=false;
        switch(d.effect){
            case BLUE_NOTE:
                boolean first=component(r,a.role("Piano"),session.content.moves.get("modal_motif"),target,null);
                if(first&&target.isAlive()&&canContinue(a,r))complete=component(r,a.role("Saxophone"),session.content.moves.get("riff"),target,"modal");
                else complete=first&&!target.isAlive();break;
            case LOCKED_IN:
                if(playerPhase)playerProtection=new Protection(a);else enemyProtection=new Protection(a);
                fact(r,PROTECTION_READY,initiator,null,d.id,0);r.messages.add("Protection readied: next incoming damaging hit.");break;
            case CALL_RESPONSE:
                boolean stank=target.activeStatuses.stream().anyMatch(s->"stank".equals(s.statusId)&&s.sourceActorId!=null&&!s.sourceActorId.equals(a.role("Trumpet").instanceId));
                boolean hit=component(r,a.role("Trumpet"),session.content.moves.get("blast"),target,null);
                boolean trigger=stank||r.facts.stream().anyMatch(f->f.type==WEAKNESS||f.type==CRITICAL);
                if(hit&&trigger&&canContinue(a,r)){if(opponents().stream().noneMatch(BattleEntity::isAlive))complete=true;else if(response.isAlive())complete=component(r,a.role("Saxophone"),session.content.moves.get("riff"),response,null);}
                break;
            case WALKING_SOLO:
                complete=component(r,a.role("Saxophone"),soloMove,target,null);if(complete)priority=a.role("Saxophone");break;
            case FULL_CHORUS:
                complete=component(r,a.role("Percussion"),session.content.moves.get("yeah_man"),target,null);
                if(complete&&playerPhase){suppressEnemy=true;if(enemyProtection!=null)fact(r,ENEMY_BAND_DISRUPTED,initiator,null,"locked_in",0);enemyProtection=null;r.messages.add("Enemy Locked In suppressed for its next phase.");}break;
        }
        if(complete)complete(r,a);
        return finish(r,initiator,true);
    }
    private boolean canContinue(BandComposition.Assignment a,BattleResult r){return r.turnResult!=TurnResult.REPEL&&r.turnResult!=TurnResult.ABSORB&&a.unique().stream().allMatch(BattleEntity::isAlive);}
    private void complete(BattleResult r,BandComposition.Assignment a){
        BattleEntity e=a.performers.get(0);List<String> ids=new ArrayList<>();for(BattleEntity p:a.unique()){ids.add(p.instanceId);contribution(r,p,null);}
        r.facts.add(new BattleResult.Fact(ARRANGEMENT_COMPLETED,e.instanceId,null,a.recipe.effectFamily,0,ally(e),false,ids));
        r.messages.add(a.recipe.displayName+" completed.");
        if(ally(e)&&session.discoveries.add(a.recipe.id)){fact(r,DISCOVERY,e,null,a.recipe.id,0);r.messages.add("Discovered: "+a.recipe.displayName);}
    }
    private void contribution(BattleResult r,BattleEntity a,Move move){
        if(r.facts.stream().anyMatch(f->f.type==CONTRIBUTION&&a.instanceId.equals(f.actor)))return;
        boolean brass=move!=null&&move.musicalEvidence.contains("brass");
        for(RaceDefinition race:session.content.races.values())if(race.displayName.equals(a.race)||a.instrumentIdentities.contains(race.displayName))brass|=Arrays.asList(race.musicalEvidence).contains("brass");
        fact(r,CONTRIBUTION,a,null,a.race,brass?1:0);
    }
    private boolean component(BattleResult r,BattleEntity a,Move m,BattleEntity selected,String override){
        fact(r,MOVE,a,selected,m.id,0);r.messages.add(a.name+" uses "+m.name+".");boolean damaged=false;
        List<BattleEntity> targets=m.isAoE()?new ArrayList<>(ally(a)?enemies:session.active):Collections.singletonList(selected);
        for(BattleEntity t:targets){
            if(!a.isAlive()||r.turnResult==TurnResult.REPEL||r.turnResult==TurnResult.ABSORB)break;if(t==null||!t.isAlive())continue;
            if(random.nextDouble()>Math.min(1,m.hitRate*a.stats.agi/(double)Math.max(1,t.stats.agi))){fact(r,MISS,a,t,m.id,0);aggregate(r,TurnResult.MISS);r.messages.add(a.name+" misses "+t.name);continue;}
            String element=override==null?m.element:override;boolean physical="physical".equals(m.element)||"phys".equals(m.element);
            Affinity affinity=t.getAffinityFor(element);boolean crit=physical&&!t.defending&&random.nextDouble()<(m.critRate==null?Math.max(.02,Math.min(.4,.05+(a.stats.luk-t.stats.luk)*.01)):m.critRate);
            int damage=Math.max(1,m.baseDamage+2*(physical?a.stats.str:a.stats.mag));
            if(affinity==Affinity.NULLIFY){fact(r,NULLIFY,a,t,m.id,0);aggregate(r,TurnResult.NULLIFY);continue;}
            if(affinity==Affinity.REPEL){damage(r,t,a,damage,m.id);fact(r,REPEL,a,t,m.id,damage);aggregate(r,TurnResult.REPEL);continue;}
            if(affinity==Affinity.ABSORB){t.healHp(damage);fact(r,ABSORB,a,t,m.id,damage);aggregate(r,TurnResult.ABSORB);continue;}
            if(affinity==Affinity.WEAK){damage=(int)(damage*1.5);fact(r,WEAKNESS,a,t,m.id,0);aggregate(r,TurnResult.WEAKNESS);}
            if(affinity==Affinity.RESIST){damage=Math.max(1,damage/2);fact(r,RESIST,a,t,m.id,0);aggregate(r,TurnResult.RESIST);}
            if(crit){damage=(int)(damage*1.5);fact(r,CRITICAL,a,t,m.id,0);aggregate(r,TurnResult.CRITICAL);}
            if(t.defending)damage=Math.max(1,damage/2);
            Protection p=ally(t)?playerProtection:enemyProtection;
            if(p!=null&&p.intact()){
                damage=Math.max(1,damage/2);fact(r,PROTECTED,a,t,"locked_in",damage);complete(r,p.assignment);
                if(ally(t)){playerProtection=null;priority=t;}else enemyProtection=null;
            }
            damage(r,a,t,damage,m.id);damaged=true;contribution(r,a,m);
            if(t.isAlive()&&!t.defending&&affinity!=Affinity.RESIST&&m.canInflictStatus()&&random.nextDouble()<m.statusEffect.chance)applyStatus(r,a,t,m.statusEffect.type,m.id);
            checkReadied(r,a);
        }
        return damaged;
    }
    private void damage(BattleResult r,BattleEntity a,BattleEntity t,int amount,String id){boolean alive=t.isAlive();t.takeDamage(amount);fact(r,HIT,a,t,id,amount);r.messages.add(t.name+" takes "+amount+" damage.");if(alive&&!t.isAlive())fact(r,DEATH,a,t,id,0);}
    private void applyStatus(BattleResult r,BattleEntity a,BattleEntity t,String id,String move){
        StatusDefinition d=session.content.statuses.get(id);if(d==null||"null".equals(id))return;
        if("testified".equals(id)){if(t.lastMoveId==null){r.messages.add("Testified failed: no previous individual move.");return;}t.lockedMoveId=t.lastMoveId;t.lockedTargetId=t.lastTargetId;}
        BattleEntity.ActiveStatus s=new BattleEntity.ActiveStatus(id,d.duration,1,move);s.sourceActorId=a.instanceId;t.addStatus(s);fact(r,STATUS,a,t,id,0);r.messages.add(t.name+": "+d.name);
    }
    private void checkReadied(BattleResult r,BattleEntity a){
        if(playerProtection!=null&&!playerProtection.intact()){if(a!=null&&!ally(a))fact(r,ARRANGEMENT_INTERRUPTED,playerProtection.assignment.performers.get(0),playerProtection.assignment.unique().stream().filter(e->!e.isAlive()||e.hasStatus("muted")||e.waitingForNextPhase).findFirst().orElse(null),"locked_in",0);playerProtection=null;}
        if(enemyProtection!=null&&!enemyProtection.intact()){if(ally(a))fact(r,ENEMY_BAND_DISRUPTED,a,null,"locked_in",0);enemyProtection=null;}
        if(cueOwner!=null&&!cueOwner.canContribute()){if(ally(a))fact(r,ENEMY_BAND_DISRUPTED,a,cueOwner,"cue",0);cueOwner=null;cueTarget=null;}
    }
    private static int severity(TurnResult t){switch(t){case ABSORB:case REPEL:return 4;case MISS:case NULLIFY:return 3;case WEAKNESS:case CRITICAL:return 2;case RESIST:return 1;default:return 0;}}
    private void aggregate(BattleResult r,TurnResult t){if(severity(t)>severity(r.turnResult))r.turnResult=t;}
    private BattleResult finish(BattleResult r,BattleEntity a,boolean consume){
        if(consume)turns.consumeIcons(r.turnResult);r.iconsAfter=turns.iconCount();
        if(priority==a)priority=null;cursor=order.isEmpty()?0:(order.indexOf(a)+1)%order.size();
        checkReadied(r,a);checkOutcome(r);groove.accept(r,r.hadAlternative);r.messages.add("Press Turn: "+r.turnResult+" / "+r.iconsBefore+" -> "+r.iconsAfter);return r;
    }
    private boolean hasAlternative(String signature){
        BattleEntity e=actor();if(e==null||e.hasStatus("rushing")||e.hasStatus("testified"))return false;
        Set<String> ids=new HashSet<>(e.battleSkills);ids.add(e.basicAttack);
        for(String id:ids){Move m=session.content.moves.get(id);if(m!=null&&e.hasMp(m.mpCost)&&(!e.hasStatus("muted")||id.equals(e.basicAttack))&&!signature.equals("move:"+id))return true;}
        for(BandComposition.Assignment a:arrangements())if(a.reason(e,turns.iconCount())==null&&!signature.equals("arrangement:"+a.recipe.id))return true;
        return playerPhase&&session.reserve.stream().anyMatch(x->x.isAlive()&&!swappedOut.contains(x.instanceId));
    }
    private void checkOutcome(BattleResult r){if(enemies.stream().noneMatch(BattleEntity::isAlive))outcome=Outcome.VICTORY;else if(session.active.stream().noneMatch(BattleEntity::isAlive))outcome=Outcome.DEFEAT;if(outcome!=Outcome.ONGOING){fact(r,OUTCOME,null,null,outcome.name(),0);r.messages.add(outcome.name());}}
    private void beginPhase(boolean player){
        playerPhase=player;swappedOut.clear();order.clear();order.addAll(side());
        order.sort(Comparator.comparingInt(e->e.hasStatus("dragging")?1:0));cursor=0;
        for(BattleEntity e:side()){e.waitingForNextPhase=false;e.arrangementCommitted=false;e.defending=false;e.removeStatusById("dragging");}
        if(player)playerProtection=null;else {enemyProtection=null;cueOwner=null;cueTarget=null;}
        turns.startTurn(player,(int)side().stream().filter(BattleEntity::isAlive).count());
    }
    /** Explicit phase transition lets the UI show each result without owning combat timing. */
    public BattleResult advancePhase(){
        if(outcome!=Outcome.ONGOING||turns.hasIconsLeft()&&actor()!=null)return BattleResult.invalid("Phase not finished");
        BattleResult r=begin("phase");
        for(BattleEntity e:new ArrayList<>(side()))if(e.isAlive())for(BattleEntity.ActiveStatus s:new ArrayList<>(e.activeStatuses)){
            if(!e.isAlive())break;
            StatusDefinition d=session.content.statuses.get(s.statusId);if(d==null)continue;
            if("damage_over_time".equals(d.effectType)){BattleEntity source=all().stream().filter(x->x.instanceId.equals(s.sourceActorId)).findFirst().orElse(null);damage(r,source,e,d.baseDamage,s.statusId);checkReadied(r,source);}
            if(s.remainingTurns>0&&--s.remainingTurns==0){e.removeStatusById(s.statusId);fact(r,STATUS_REMOVED,e,e,s.statusId,0);}
        }
        checkReadied(r,null);checkOutcome(r);groove.accept(r,false);
        if(outcome==Outcome.ONGOING){boolean next=!playerPhase;if(next){groove.nextRound();suppressEnemy=false;}beginPhase(next);r.messages.add(next?"Player phase":"Enemy phase");}return r;
    }
    private List<BattleEntity> all(){List<BattleEntity> a=new ArrayList<>(session.allMusicians());a.addAll(enemies);return a;}
    public BattleResult enemyStep(){
        if(playerPhase)return BattleResult.invalid("Player phase");BattleEntity a=actor();if(a==null)return advancePhase();
        if(a.hasStatus("rushing")||a.hasStatus("testified")){BattleResult forced=forcedAction();if(forced.accepted)return forced;}
        if(!cueUsed&&"Trombone".equals(a.race)&&a.canContribute()){
            cueUsed=true;cueOwner=a;cueTarget=session.active.stream().filter(e->e.isAlive()&&"Saxophone".equals(e.race)).findFirst().orElse(session.active.get(0));
            BattleResult r=begin("test:cue");fact(r,CUE,a,cueTarget,"test_interruption_cue",0);r.messages.add("TEST interruption cue marks "+cueTarget.name+". Disable "+a.name+" or change performers.");return finish(r,a,true);
        }
        if(!suppressEnemy)for(BandComposition.Assignment candidate:arrangements())if(candidate.recipe.effect==ArrangementDefinition.Effect.LOCKED_IN&&candidate.reason(a,turns.iconCount())==null&&enemyProtection==null)return arrange(candidate,null,null,null);
        BattleEntity target=session.active.stream().filter(BattleEntity::isAlive).findFirst().orElse(null);
        BattleResult result=move(a.basicAttack,target);if(!turns.hasIconsLeft())suppressEnemy=false;return result;
    }
    public String protectionDescription(){return "Protection: allies "+(playerProtection==null?"none":names(playerProtection))+" / enemies "+(enemyProtection==null?"none":names(enemyProtection));}
    private String names(Protection p){List<String> names=new ArrayList<>();for(BattleEntity e:p.assignment.unique())names.add(e.name);return String.join(" + ",names);}
    public String cueDescription(){return cueOwner==null?"No interruption cue":"TEST cue: "+cueOwner.name+" -> "+cueTarget.name;}
}
