package battle;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds all runtime data for a battle.
 * This is the "battle model" that the controller reads from and writes to.
 */
public class BattleContext {

    public enum BattleOutcome {
        ONGOING,
        PLAYER_VICTORY,
        ENEMY_VICTORY,
        ESCAPED
    }

    private final BattleEntity player;
    private final BattleEntity enemy;

    private final Map<String, Move> movesById;

    private BattleAction selectedAction;
    private Move selectedMove;

    private BattleOutcome outcome = BattleOutcome.ONGOING;
    private int turnNumber = 1;

    public BattleContext(BattleEntity player, BattleEntity enemy, List<Move> moves) {
        this.player = player;
        this.enemy = enemy;
        this.movesById = new HashMap<>();

        if (moves != null) {
            for (Move move : moves) {
                if (move != null && move.id != null && !move.id.isBlank()) {
                    movesById.put(move.id, move);
                }
            }
        }
    }

    public BattleEntity getPlayer() {
        return player;
    }

    public BattleEntity getEnemy() {
        return enemy;
    }

    public Map<String, Move> getMovesById() {
        return Collections.unmodifiableMap(movesById);
    }

    public Move getMove(String moveId) {
        if (moveId == null || moveId.isBlank()) {
            return null;
        }
        return movesById.get(moveId);
    }

    public Move getBasicAttackMove() {
        return getMove("attack");
    }

    public BattleAction getSelectedAction() {
        return selectedAction;
    }

    public void setSelectedAction(BattleAction selectedAction) {
        this.selectedAction = selectedAction;
    }

    public Move getSelectedMove() {
        return selectedMove;
    }

    public void setSelectedMove(Move selectedMove) {
        this.selectedMove = selectedMove;
    }

    public void setSelectedMoveById(String moveId) {
        this.selectedMove = getMove(moveId);
    }

    public void clearSelection() {
        selectedAction = null;
        selectedMove = null;
    }

    public BattleOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(BattleOutcome outcome) {
        this.outcome = outcome == null ? BattleOutcome.ONGOING : outcome;
    }

    public boolean isBattleOver() {
        return outcome != BattleOutcome.ONGOING;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public void nextTurn() {
        turnNumber++;
    }
    
    private BattleState nextStateBackup = BattleState.INIT;

    public void setNextStateBackup(BattleState state) { this.nextStateBackup = state; }
    public BattleState getNextStateBackup() { return this.nextStateBackup; }
}
