package screens;

import band.*;
import battle.*;
import content.ContentCatalog;
import game.GameSession;
import judgment.*;
import com.payamsdev.oldsona.OldsonaGame;
import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import java.util.*;
import static com.badlogic.gdx.Input.Keys;

/** Keyboard-first test presentation. All combat decisions live in SliceBattle. */
public final class SliceScreen extends ScreenAdapter {
    private enum View { TITLE, FIELD, BATTLE, BOOK, JUDGMENT, HARNESS }
    private final OldsonaGame game;
    private final SpriteBatch batch=new SpriteBatch();
    private final BitmapFont font=new BitmapFont();
    private final ShapeRenderer shapes=new ShapeRenderer();
    private final FitViewport viewport=new FitViewport(1100,760);
    private final ContentCatalog content=new ContentCatalog(p->Gdx.files.internal(p).readString("UTF-8"));
    private final Sound tone;
    private final GameSession campaign=new GameSession(content);
    private GameSession session=campaign;
    private SliceBattle battle;
    private View view=View.TITLE,back=View.FIELD;
    private final List<String> log=new ArrayList<>();
    private final List<Command> commands=new ArrayList<>();
    private int selected,targetIndex,responseIndex,swapSlot=1,reserveIndex,bookOffset,logOffset;
    private boolean awaiting,mute,harnessBattle;
    private String solo="riff";
    private float flash;
    private JudgmentEvaluator.Evaluation preview;
    private String submission;
    private static final class Command {final String label;final Runnable action;String detail="";Command(String label,Runnable action){this.label=label;this.action=action;}}
    public SliceScreen(OldsonaGame game){this.game=game;tone=Gdx.audio.newSound(Gdx.files.internal("prototype/audio/confirm.wav"));log.add("Option A / noncanonical test dungeon. All effects marked TEST are provisional.");}
    @Override public void show(){Gdx.input.setInputProcessor(new InputAdapter(){@Override public boolean keyDown(int key){key(key);return true;}@Override public boolean scrolled(float x,float y){if(view==View.BOOK)bookOffset=Math.max(0,bookOffset+(int)y);return true;}});}
    private void note(String text){log.add(text);if(log.size()>200)log.remove(0);logOffset=0;}
    private void result(BattleResult r){for(String message:r.messages)note(message);if(r.accepted){awaiting=true;flash=.18f;if(!mute)tone.play(.14f);}rebuild();}
    private BattleEntity target(int index){if(battle==null||battle.enemies.isEmpty())return null;return battle.enemies.get(Math.floorMod(index,battle.enemies.size()));}
    private void start(String id){battle=session.startBattle(id);view=View.BATTLE;awaiting=false;selected=targetIndex=responseIndex=0;note("TEST encounter "+id+": enemy HP x"+session.config.enemyHpScale+", MP +"+session.config.enemyMpBonus);if("2".equals(id))note("TEST second configuration: "+session.config.secondEncounterAffinities);rebuild();}
    private void key(int key){
        if(key==Keys.PAGE_UP){logOffset=Math.min(Math.max(0,log.size()-4),logOffset+4);return;}
        if(key==Keys.PAGE_DOWN){logOffset=Math.max(0,logOffset-4);return;}
        if(key==Keys.ESCAPE&&view==View.FIELD){view=View.TITLE;return;}
        if(key==Keys.M){mute=!mute;note(mute?"Audio muted":"Audio on");return;}
        if(view==View.TITLE){if(key==Keys.ENTER){view=View.FIELD;}if(key==Keys.L)game.showLegacy();if(key==Keys.H)view=View.HARNESS;return;}
        if(view==View.BOOK){if(key==Keys.ESCAPE||key==Keys.B)view=back;if(key==Keys.DOWN)bookOffset++;if(key==Keys.UP)bookOffset=Math.max(0,bookOffset-1);return;}
        if(view==View.HARNESS){if(key==Keys.ESCAPE)view=View.TITLE;if(key==Keys.V)startVirtuoso();if(key==Keys.J){session=new GameSession(content);view=View.JUDGMENT;preview=null;submission=null;harnessBattle=true;}return;}
        if(view==View.JUDGMENT){judgmentKey(key);return;}
        if(key==Keys.B){back=view;view=View.BOOK;bookOffset=0;return;}
        if(view==View.FIELD){
            if(key==Keys.LEFT||key==Keys.A)session.turn(-1);if(key==Keys.RIGHT||key==Keys.D)session.turn(1);
            if(key==Keys.UP||key==Keys.W)session.move(1);if(key==Keys.DOWN||key==Keys.S)session.move(-1);
            if(key==Keys.I){String instrument="Piano".equals(session.active.get(0).race)?"trumpet":"piano";if(session.unlockedInstruments.contains(instrument)){session.equip(instrument);note("Equipped "+instrument);}else note("Trumpet is acquired at U after encounter 1.");}
            if(key==Keys.TAB)swapSlot=swapSlot%3+1;
            if(key==Keys.F&&!session.reserve.isEmpty()){session.fieldSwap(swapSlot,0);note("Swapped demon seat "+swapSlot+" with reserve.");}
            if(key==Keys.ENTER){char tile=session.map.at(session.x,session.y);if(tile=='R'){session.rest();note("Restored the Band.");}else if(tile=='U'){session.acquireReward();note(session.acquiredReward?"Trumpet unlocked; JJJ recruited once.":"Clear encounter 1 first.");}else if(tile=='1'||tile=='2'){String id=String.valueOf(tile);if(!session.resolvedEncounters.contains(id))start(id);else note("Encounter already cleared.");}else if(tile=='J'){view=View.JUDGMENT;preview=null;submission=null;}else note("No event on this tile.");}
            return;
        }
        if(view==View.BATTLE){
            if(key==Keys.LEFT){targetIndex=Math.floorMod(targetIndex-1,battle.enemies.size());rebuild();}if(key==Keys.RIGHT){targetIndex=(targetIndex+1)%battle.enemies.size();rebuild();}
            if(key==Keys.TAB){responseIndex=(responseIndex+1)%battle.enemies.size();rebuild();}
            if(key==Keys.S){solo=solo.equals("riff")?"modal_motif":"riff";rebuild();}
            if(key==Keys.R){swapSlot=swapSlot%3+1;rebuild();}if(key==Keys.Q&&!session.reserve.isEmpty()){reserveIndex=(reserveIndex+1)%session.reserve.size();rebuild();}
            if(key==Keys.SPACE){
                if(awaiting){awaiting=false;if(battle.outcome!=SliceBattle.Outcome.ONGOING){SliceBattle.Outcome outcome=battle.outcome;session.finishBattle(battle);battle=null;if(harnessBattle){session=campaign;harnessBattle=false;view=View.HARNESS;}else {view=View.FIELD;if(outcome==SliceBattle.Outcome.DEFEAT){session.x=session.map.spawnX;session.y=session.map.spawnY;session.rest();note("TEST defeat: returned to rest point.");}}return;}}
                if(!battle.turns.hasIconsLeft()||battle.actor()==null)result(battle.advancePhase());else if(!battle.playerPhase)result(battle.enemyStep());else if(battle.actor().hasStatus("rushing")||battle.actor().hasStatus("testified"))result(battle.forcedAction());rebuild();return;
            }
            if(!awaiting&&battle.playerPhase){if(key==Keys.DOWN)selected=Math.min(commands.size()-1,selected+1);if(key==Keys.UP)selected=Math.max(0,selected-1);if(key==Keys.ENTER&&!commands.isEmpty())commands.get(selected).action.run();}
        }
    }
    private void rebuild(){
        commands.clear();if(battle==null||!battle.playerPhase||battle.actor()==null)return;BattleEntity a=battle.actor();
        LinkedHashSet<String> moves=new LinkedHashSet<>();moves.add(a.basicAttack);moves.addAll(a.battleSkills);
        for(String id:moves){Move m=content.moves.get(id);commands.add(new Command(m.name+" / "+m.mpCost+" MP",()->result(battle.move(id,target(targetIndex)))));}
        for(BandComposition.Assignment arrangement:battle.arrangements()){
            String reason=arrangement.reason(a,battle.turns.iconCount());String name=session.discoveries.contains(arrangement.recipe.id)?arrangement.recipe.displayName:"Try combination";
            String roles=String.join(" + ",arrangement.recipe.roles);String label=name+" ["+roles+"] / "+arrangement.recipe.iconCost+" icons"+(reason==null?"":" / "+reason);
            Command command=new Command(label,()->result(battle.arrange(arrangement,target(targetIndex),target(responseIndex),solo)));
            List<String> costs=new ArrayList<>();arrangement.costs().forEach((e,mp)->costs.add(e.name+" "+mp+" MP"));
            command.detail="Initiator: "+a.name+" / "+String.join(", ",costs)+(arrangement.recipe.effect==ArrangementDefinition.Effect.WALKING_SOLO?" + solo move MP":"")+". "+(reason==null?"Ready":reason);
            commands.add(command);
        }
        commands.add(new Command("Defend",()->result(battle.defend())));
        if(!session.reserve.isEmpty())commands.add(new Command("Swap seat "+swapSlot+" -> "+session.reserve.get(reserveIndex).name+" (1 icon)",()->result(battle.swap(swapSlot,reserveIndex))));
        selected=Math.max(0,Math.min(selected,commands.size()-1));
    }
    private void startVirtuoso(){
        session=new GameSession(content);BattleEntity v=session.active.get(0);v.name="TEST Virtuoso";v.race="Virtuoso";v.instrumentIdentities.addAll(Arrays.asList("Piano","Saxophone"));v.addSkill("riff");session.active.remove(1);harnessBattle=true;start("virtuoso");note("Isolated harness: 3 bodies / 4 roles / Full Chorus costs Virtuoso 4 MP.");
    }
    private void judgmentKey(int key){
        if(key==Keys.ESCAPE){if(session.judgment.gameOver&&!harnessBattle)return;view=harnessBattle?View.HARNESS:View.FIELD;if(harnessBattle){session=campaign;harnessBattle=false;}return;}
        if(session.judgment.gameOver||session.judgment.success)return;
        JudgmentEvaluator evaluator=new JudgmentEvaluator();
        if(key==Keys.P){if(session.lastPerformance==null)note("No completed battle evidence yet.");else {preview=evaluator.evaluate(session.desire,session.lastPerformance);submission=UUID.randomUUID().toString();}}
        if(harnessBattle){if(key==Keys.NUM_1){preview=evaluator.evaluate(session.desire,0,0,0,8);submission=UUID.randomUUID().toString();}if(key==Keys.NUM_2){preview=evaluator.evaluate(session.desire,1,1,1,0);submission=UUID.randomUUID().toString();}if(key==Keys.NUM_3){preview=evaluator.evaluate(session.desire,1,1,1,3);submission=UUID.randomUUID().toString();}if(key==Keys.NUM_4){preview=evaluator.evaluate(session.desire,0,0,0,0);submission=UUID.randomUUID().toString();}}
        if(key==Keys.ENTER&&preview!=null){session.judgment.commit(submission,preview);note(preview.passed?"Judgment passed.":"Judgment failed: "+String.join(", ",preview.hints));preview=null;}
    }
    @Override public void render(float delta){
        flash=Math.max(0,flash-delta);ScreenUtils.clear(.025f,.02f,.05f,1);viewport.apply();batch.setProjectionMatrix(viewport.getCamera().combined);shapes.setProjectionMatrix(viewport.getCamera().combined);
        if(view==View.FIELD)drawDungeon();if(view==View.BATTLE)drawEnemies();
        batch.begin();font.setColor(Color.WHITE);text("GUISE  /  BAND + GROOVE  /  VERTICAL SLICE A",28,735);font.setColor(Color.LIGHT_GRAY);text("M sound "+(mute?"off":"on")+"    F10 new run",880,735);
        switch(view){case TITLE:drawTitle();break;case FIELD:drawField();break;case BATTLE:drawBattle();break;case BOOK:drawBook();break;case JUDGMENT:drawJudgment();break;case HARNESS:drawHarness();break;}
        font.setColor(Color.LIGHT_GRAY);int end=Math.max(0,log.size()-logOffset);int start=Math.max(0,end-4);for(int i=start;i<end;i++)text(log.get(i),28,95-(i-start)*20);batch.end();
    }
    private void text(String text,float x,float y){font.draw(batch,text,x,y);}
    private void wrap(String text,float x,float y,float width){font.draw(batch,text,x,y,width,com.badlogic.gdx.utils.Align.left,true);}
    private void drawTitle(){font.setColor(Color.GOLD);text("THE TEST ROOM",55,610);font.setColor(Color.WHITE);text("Enter  Begin / resume the dungeon",55,555);text("H      Isolated Judgment and Virtuoso harnesses",55,515);text("L      Original 1v1 battle (F10 returns)",55,475);wrap("Piano, Saxophone, Bass, Percussion. Discover coordinated actions, acquire Trumpet, and compare the same Band with a different leader. This is a mechanics experiment; fixture art, sound and Judgment text are not final game content.",55,395,760);text("Arrow keys + Enter. Space advances battle results. B opens the Book. PgUp/PgDn review results.",55,275);}
    private void drawField(){
        text("TEST DUNGEON   "+session.x+", "+session.y+"   "+new String[]{"N","E","S","W"}[session.facing]+"   Tile: "+session.map.at(session.x,session.y),30,680);
        text("W/S move  A/D turn  Enter interact  I instrument  B Book",30,350);text("Tab selects demon seat "+swapSlot+" / F swaps with reserve",30,326);
        roster(300);text("R rest  1/2 encounters  U Trumpet + JJJ  J Judgment / Escape menu",30,155);
        for(int y=0;y<session.map.rows.size();y++){String row=session.map.rows.get(y);for(int x=0;x<row.length();x++){font.setColor(x==session.x&&y==session.y?Color.GOLD:Color.GRAY);text(x==session.x&&y==session.y?"@":String.valueOf(row.charAt(x)),900+x*19,660-y*23);}}font.setColor(Color.WHITE);
    }
    private void roster(float y){for(int i=0;i<session.active.size();i++){BattleEntity a=session.active.get(i);text(i+" "+a.name+" / "+a.race+"   HP "+a.currentHp+"/"+a.maxHp+"   MP "+a.currentMp+"/"+a.maxMp+statuses(a),30,y-i*29);}}
    private String statuses(BattleEntity a){StringBuilder s=new StringBuilder();if(a.waitingForNextPhase)s.append(" [waiting]");if(a.arrangementCommitted)s.append(" [committed]");for(BattleEntity.ActiveStatus st:a.activeStatuses)s.append(" [").append(st.statusId).append(']');return s.toString();}
    private void drawBattle(){
        text((battle.playerPhase?"PLAYER":"ENEMY")+"   Icons "+battle.turns.getIconPool()+"   Round "+battle.groove.current().number,30,698);
        text(String.format(Locale.ROOT,"Groove %.2f  (preparation %.2f)",battle.groove.score(),battle.groove.preparation),685,698);
        for(int i=0;i<battle.enemies.size();i++){BattleEntity e=battle.enemies.get(i);font.setColor(i==targetIndex?Color.GOLD:Color.WHITE);text((i==targetIndex?"> ":"")+e.name+" HP "+e.currentHp+" MP "+e.currentMp,50+i*340,507);text(e.race+statuses(e),50+i*340,485);}font.setColor(Color.WHITE);
        roster(450);text(battle.cueDescription(),30,325);text(battle.protectionDescription(),30,672);
        text("Target: Left/Right | Tab response: "+target(responseIndex).name+" | S solo: "+solo,30,302);
        BattleEntity actor=battle.actor();text("Scheduled: "+(actor==null?"phase end":actor.name)+" | R swap seat / Q reserve",30,281);
        if(awaiting||battle.outcome!=SliceBattle.Outcome.ONGOING)text("SPACE: continue / acknowledge result",30,251);
        else if(!battle.playerPhase||actor==null||actor.hasStatus("rushing")||actor.hasStatus("testified"))text("SPACE: advance enemy / forced action / phase",30,251);
        else {int from=Math.max(0,selected-2);for(int i=from;i<Math.min(commands.size(),from+5);i++){font.setColor(i==selected?Color.GOLD:Color.LIGHT_GRAY);String label=commands.get(i).label;if(label.length()>123)label=label.substring(0,120)+"...";text((i==selected?"> ":"  ")+label,30,251-(i-from)*23);}font.setColor(Color.WHITE);if(selected<commands.size())wrap(commands.get(selected).detail,30,132,1040);}
        List<String> reasons=battle.groove.current().reasons;int from=Math.max(0,reasons.size()-3);for(int i=from;i<reasons.size();i++)wrap(reasons.get(i),785,450-(i-from)*42,280);
    }
    private void drawBook(){text("BOOK  /  B or Escape returns",30,670);int i=0;for(ArrangementDefinition d:content.arrangements.values()){if(!session.discoveries.contains(d.id))continue;if(i++<bookOffset)continue;float y=610-(i-bookOffset-1)*90;if(y<160)break;text(d.displayName+"  [TEST]  "+d.iconCost+" icons",35,y);text(String.join(" + ",d.roles)+" / MP "+d.mpCosts,35,y-23);List<BandComposition.Assignment> matches=new BandComposition().match(d,session.active);text(matches.isEmpty()?"Unavailable: required instrument combination missing":"Compatible composition; contributor state, MP and initiator checked in battle",35,y-46);}if(session.discoveries.isEmpty())text("No discoveries yet. Hint: let Piano accompany Saxophone.",35,610);}
    private void drawHarness(){text("ISOLATED TEST HARNESSES",35,650);text("V  Virtuoso: Piano + Saxophone identities, one musician",35,580);text("J  Judgment: preview synthetic gate combinations and commit attempts",35,535);text("Escape  Return to title",35,470);text("Harnesses create their own session; no Virtuoso acquisition is added to the game.",35,390);}
    private void drawJudgment(){text(harnessBattle?session.desire.name+" [DEVELOPER TEST]":"JUDGMENT / hidden test desire",35,660);text(session.judgment.nextName()+" / attempts "+session.judgment.attemptsUsed()+" of 3",35,610);text("P preview last battle evidence / Enter commit preview / Escape return",35,565);if(harnessBattle)text("1 high Groove, wrong fit | 2 right fit, poor performance | 3 pass | 4 fail",35,520);if(preview!=null){if(harnessBattle)text(String.format(Locale.ROOT,"Identity %.2f   Cooperation %.2f   Adaptation %.2f   Fit %.2f   Performance %.2f",preview.identity,preview.cooperation,preview.adaptation,preview.fit,preview.performance),35,450);text(preview.passed?"Preview: PASS":"Preview: FAIL / "+String.join(", ",preview.hints),35,408);}if(session.judgment.gameOver){text("GAME OVER / cutscene hook: judgment_third_failure",35,330);wrap("Reserved for your authored third-failure sequence. No story text has been invented.",35,290,800);}if(session.judgment.success)text("TEST SUCCESS / cutscene hook: judgment_test_success",35,330);}
    private void drawDungeon(){
        shapes.begin(ShapeRenderer.ShapeType.Filled);shapes.setColor(.12f,.09f,.18f,1);shapes.rect(30,380,820,280);shapes.setColor(.06f,.05f,.09f,1);shapes.rect(30,380,820,140);
        double angle=session.facing*Math.PI/2-Math.PI/2;
        for(int column=0;column<205;column++){double ray=angle+(column/204.0-.5)*Math.PI/2.6,dist=.03;for(;dist<12;dist+=.035){int x=(int)Math.floor(session.x+.5+Math.cos(ray)*dist),y=(int)Math.floor(session.y+.5+Math.sin(ray)*dist);if(!session.map.walkable(x,y))break;}double corrected=dist*Math.cos(ray-angle);float height=(float)Math.min(280,230/Math.max(.1,corrected));float shade=(float)(.65/(1+dist*.38));shapes.setColor(shade*.75f,shade*.64f,shade,1);shapes.rect(30+column*4,520-height/2,4,height);}
        shapes.end();
    }
    private void drawEnemies(){shapes.begin(ShapeRenderer.ShapeType.Filled);for(int i=0;i<battle.enemies.size();i++){BattleEntity e=battle.enemies.get(i);if(!e.isAlive())continue;float x=170+i*340;shapes.setColor(flash>0?Color.WHITE:new Color(.35f,.18f,.45f,1));shapes.circle(x,606,23,8);shapes.triangle(x,589,x-48,535,x+48,535);shapes.setColor(Color.GOLD);shapes.rect(x-4,569,38,7);}shapes.end();}
    @Override public void resize(int width,int height){viewport.update(width,height,true);}
    @Override public void hide(){Gdx.input.setInputProcessor(null);}
    @Override public void dispose(){tone.dispose();batch.dispose();font.dispose();shapes.dispose();}
}
