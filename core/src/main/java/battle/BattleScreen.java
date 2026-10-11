package battle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class BattleScreen implements Screen {

    private final SpriteBatch batch;
    private final BitmapFont font;
    private final BattleContext context;
    private final BattleController controller;

    private final InputAdapter inputAdapter = new InputAdapter() {
        @Override
        public boolean keyDown(int keycode) {
            handleKeyDown(keycode);
            return true;
        }
    };

    public BattleScreen() {
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        this.font.setColor(Color.WHITE);

        BattleDataLoader loader = new BattleDataLoader();
        BattleEntity player = loader.loadBattleEntity("player/player.JSON");
        BattleEntity enemy = loader.loadBattleEntity("demons/JJJ.JSON");

        this.context = new BattleContext(player, enemy, loader.loadMoves("moves/moves.JSON"));
        this.controller = new BattleController(context);
    }

    @Override public void show() { Gdx.input.setInputProcessor(inputAdapter); }

    @Override
    public void render(float delta) {
        controller.update(delta);
        ScreenUtils.clear(0.02f, 0.02f, 0.03f, 1f); // Sleek modern SMT matte black finish

        batch.begin();
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();

        // --- HEADER BANNER: SYSTEMS AND PRESS ICONS ---
        font.setColor(Color.DARK_GRAY);
        font.draw(batch, "==========================================================================================", 40, height - 15f);
        font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "PHASE MONITOR", 40, height - 35f);
        
        drawTurnIcons(width - 340, height - 35f);

        // --- COMBAT STATS LAYOUT ---
        drawEntityPanel(context.getPlayer(), 60, height - 100f, Color.CYAN);
        drawEntityPanel(context.getEnemy(), width - 360, height - 100f, Color.MAROON);

        // --- INTERACTION / LOG NOTIFICATION FRAME ---
        if (controller.getCurrentState() == BattleState.PLAYING_EVENTS) {
            drawCinematicNotificationLog(width, height);
        } else {
            drawCommandSubmenu(60, 200f);
        }

        batch.end();
    }

    private void drawTurnIcons(float x, float y) {
        StringBuilder iconStr = new StringBuilder("POOL: ");
        for (PressTurnEngine.IconState state : controller.getTurnEngine().getIconPool()) {
            if (state == PressTurnEngine.IconState.FULL) iconStr.append("[ ⬥ ] ");
            else if (state == PressTurnEngine.IconState.FLASHING) iconStr.append("[ ⬦ ] ");
        }
        font.setColor(controller.getTurnEngine().isPlayerTurn() ? Color.GREEN : Color.ORANGE);
        font.draw(batch, iconStr.toString(), x, y);
    }

    private void drawEntityPanel(BattleEntity entity, float x, float y, Color themeColor) {
        font.setColor(themeColor);
        String guardLabel = entity.defending ? " [GUARD]" : "";
        font.draw(batch, entity.name.toUpperCase() + " // LV " + entity.level + " (" + entity.race + ")" + guardLabel, x, y);
        
        y -= 25f;
        font.setColor(Color.WHITE);
        font.draw(batch, "HP: " + entity.currentHp + " / " + entity.maxHp, x, y);
        y -= 20f;
        font.draw(batch, "MP: " + entity.currentMp + " / " + entity.maxMp, x, y);
        
        // Active status readout tracker
        if (!entity.activeStatuses.isEmpty()) {
            y -= 20f;
            font.setColor(Color.YELLOW);
            StringBuilder sb = new StringBuilder("STATUS: ");
            for (BattleEntity.ActiveStatus s : entity.activeStatuses) {
                sb.append("[").append(s.statusId.toUpperCase()).append(" (").append(s.remainingTurns).append("T)] ");
            }
            font.draw(batch, sb.toString(), x, y);
        }
    }

    private void drawCinematicNotificationLog(float screenWidth, float screenHeight) {
        float midY = screenHeight / 2f;
        font.setColor(Color.RED);
        font.draw(batch, ">> -------------------------------------------------------------------------------------- <<", 40, midY + 40f);
        font.setColor(Color.WHITE);
        font.draw(batch, "   " + controller.getCurrentMessage(), 60, midY);
        font.setColor(Color.RED);
        font.draw(batch, ">> -------------------------------------------------------------------------------------- <<", 40, midY - 40f);
    }

    private void drawCommandSubmenu(float x, float y) {
        font.setColor(Color.DARK_GRAY);
        font.draw(batch, "------------------------------------------------------------------------------------------", x, y);
        y -= 25f;
        
        switch (controller.getCurrentState()) {
            case PLAYER_ACTION_SELECT:
                font.setColor(Color.CYAN);
                font.draw(batch, "SELECT COMMAND:", x, y);
                font.setColor(Color.WHITE);
                font.draw(batch, "        [1] MELEE ATTACK        [2] SKILL MASTER MENU        [3] GUARD / DEFEND", x + 150f, y);
                break;
                
            case PLAYER_SKILL_SELECT:
                font.setColor(Color.YELLOW);
                font.draw(batch, "CHOOSE SKILL:", x, y);
                font.setColor(Color.WHITE);
                font.draw(batch, "        [1] SOUL MOTIF (3 MP)         [2] MODAL MOTIF (3 MP)         [3] BACK", x + 150f, y);
                break;
                
            default:
                font.setColor(Color.LIGHT_GRAY);
                font.draw(batch, "Awaiting system scheduling calculations...", x, y);
                break;
        }
    }

    private void handleKeyDown(int keycode) {
        if (controller.getCurrentState() == BattleState.PLAYER_ACTION_SELECT) {
            if (keycode == Input.Keys.NUM_1 || keycode == Input.Keys.NUMPAD_1) controller.submitAction(BattleAction.ATTACK);
            if (keycode == Input.Keys.NUM_2 || keycode == Input.Keys.NUMPAD_2) controller.submitAction(BattleAction.SKILL);
            if (keycode == Input.Keys.NUM_3 || keycode == Input.Keys.NUMPAD_3) controller.submitAction(BattleAction.DEFEND);
        } else if (controller.getCurrentState() == BattleState.PLAYER_SKILL_SELECT) {
            if (keycode == Input.Keys.NUM_1 || keycode == Input.Keys.NUMPAD_1) controller.submitSkill("soul_motif");
            if (keycode == Input.Keys.NUM_2 || keycode == Input.Keys.NUMPAD_2) controller.submitSkill("modal_motif");
            if (keycode == Input.Keys.NUM_3 || keycode == Input.Keys.NUMPAD_3) controller.setCurrentState(BattleState.PLAYER_ACTION_SELECT);
        }
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { Gdx.input.setInputProcessor(null); }
    @Override public void dispose() {
        batch.dispose();
        font.dispose();
    }
}