package screens;

import battle.SliceBattle;
import game.GameSession;
import com.payamsdev.oldsona.OldsonaGame;
import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.*;
import java.lang.reflect.Field;
import java.util.List;

/** Real OpenGL/packaged-assets smoke; deliberately opt-in, not a headless JUnit test. */
public final class DesktopSmoke extends OldsonaGame {
    private int frames,stage;
    private static Object field(Object object,String name){try{Field f=object.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(object);}catch(Exception e){throw new AssertionError(e);}}
    private static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    private void key(int key){Gdx.input.getInputProcessor().keyDown(key);}
    private void fresh(){Screen old=getScreen();setScreen(new SliceScreen(this));old.dispose();}
    private void choose(String contains){List<?> commands=(List<?>)field(getScreen(),"commands");int index=-1;for(int i=0;i<commands.size();i++)if(((String)field(commands.get(i),"label")).contains(contains)){index=i;break;}require(index>=0,"Missing command "+contains);for(int i=0;i<30;i++)key(Input.Keys.UP);for(int i=0;i<index;i++)key(Input.Keys.DOWN);key(Input.Keys.ENTER);}
    private void capture(String name){Pixmap p=Pixmap.createFromFrameBuffer(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight());try{PixmapIO.writePNG(Gdx.files.absolute(new java.io.File(System.getProperty("guise.smoke.output","build/smoke")+"/"+name+".png").getAbsolutePath()),p,-1,true);}finally{p.dispose();}System.out.println("SMOKE capture: "+name);}
    @Override public void render(){
        super.render();if(++frames%12!=0)return;
        try{
            switch(stage++){
                case 0:capture("01-title");key(Input.Keys.ENTER);break;
                case 1:capture("02-dungeon");Gdx.graphics.setWindowedMode(720,480);break;
                case 2:capture("03-resized-dungeon");Gdx.graphics.setWindowedMode(1280,960);key(Input.Keys.UP);key(Input.Keys.UP);key(Input.Keys.UP);key(Input.Keys.RIGHT);key(Input.Keys.UP);key(Input.Keys.UP);key(Input.Keys.ENTER);break;
                case 3:require(field(getScreen(),"battle")!=null,"Dungeon encounter did not start");capture("04-party-battle");choose("[Piano + Saxophone]");break;
                case 4:GameSession s=(GameSession)field(getScreen(),"session");require(s.discoveries.contains("blue_note"),"UI Blue Note did not discover");capture("05-arrangement-result");key(Input.Keys.B);break;
                case 5:capture("06-book");fresh();key(Input.Keys.H);key(Input.Keys.J);key(Input.Keys.NUM_1);break;
                case 6:capture("07-judgment-preview");key(Input.Keys.ENTER);key(Input.Keys.NUM_4);key(Input.Keys.ENTER);key(Input.Keys.NUM_4);key(Input.Keys.ENTER);break;
                case 7:require(((GameSession)field(getScreen(),"session")).judgment.gameOver,"Third failure did not terminate");capture("08-third-failure");key(Input.Keys.ESCAPE);key(Input.Keys.V);break;
                case 8:SliceBattle b=(SliceBattle)field(getScreen(),"battle");require(b.session.active.size()==3&&b.turns.iconCount()==3,"Virtuoso became two bodies");capture("09-virtuoso");choose("3 icons");break;
                case 9:require(((GameSession)field(getScreen(),"session")).discoveries.contains("full_chorus"),"Virtuoso Full Chorus did not resolve");capture("10-virtuoso-result");fresh();key(Input.Keys.L);break;
                case 10:require(getScreen() instanceof battle.BattleScreen,"Legacy entry failed");capture("11-legacy");fresh();key(Input.Keys.ENTER);GameSession terminal=(GameSession)field(getScreen(),"session");judgment.JudgmentEvaluator evaluator=new judgment.JudgmentEvaluator();for(int i=0;i<3;i++)terminal.judgment.commit("terminal:"+i,evaluator.evaluate(terminal.desire,0,0,0,0));terminal.x=6;terminal.y=6;key(Input.Keys.ENTER);key(Input.Keys.ESCAPE);require("JUDGMENT".equals(field(getScreen(),"view").toString()),"Terminal Judgment escaped into encounter flow");System.out.println("DESKTOP_SMOKE_PASS: title, dungeon, resize, encounter, Blue Note, costs/discovery, Book, three Judgment failures, Virtuoso, legacy; packaged assets loaded.");Gdx.app.exit();break;
            }
        }catch(Throwable error){error.printStackTrace();System.exit(1);}
    }
    public static void main(String[] args){Lwjgl3ApplicationConfiguration config=new Lwjgl3ApplicationConfiguration();config.setTitle("GUISE desktop validation");config.setWindowedMode(1280,960);config.setForegroundFPS(60);config.useVsync(false);new Lwjgl3Application(new DesktopSmoke(),config);}
}
