package battle;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Queue;

public class BattleController {

    private final BattleContext context;
    private final PressTurnEngine turnEngine;
    private BattleState currentState;

    private final Queue<String> eventTextQueue = new Queue<>();
    private String currentMessage = "";
    private float messageDisplayTimer = 0f;
    private static final float MESSAGE_DURATION = 1.35f; 

    private BattleEntity currentAttacker;
    private BattleEntity currentDefender;
    private Move pendingMove;

    public BattleController(BattleContext context) {
        this.context = context;
        this.turnEngine = new PressTurnEngine();
        this.currentState = BattleState.INIT;
    }
    
    public void setCurrentState(BattleState state) {
        this.currentState = state;
    }

    public void update(float delta) {
        if (context == null || context.isBattleOver()) return;

        switch (currentState) {
            case INIT:
                currentState = BattleState.PLAYER_TURN_START;
                break;

            case PLAYER_TURN_START:
                context.getPlayer().setDefending(false); // Drop guard at top of turn
                turnEngine.startTurn(true, 1);
                eventTextQueue.addLast("PLAYER PHASE - BEGIN");
                advanceToEventPlayback(BattleState.PLAYER_ACTION_SELECT);
                break;

            case PLAYER_ACTION_SELECT:
            case PLAYER_SKILL_SELECT:
            case PLAYER_ITEM_SELECT:
            case PLAYER_TARGET_SELECT:
                break;

            case ENEMY_TURN_START:
                context.getEnemy().setDefending(false); 
                turnEngine.startTurn(false, 1);
                eventTextQueue.addLast("ENEMY PHASE - BEGIN");
                advanceToEventPlayback(BattleState.ENEMY_DECIDE);
                break;

            case ENEMY_DECIDE:
                currentAttacker = context.getEnemy();
                currentDefender = context.getPlayer();
                pendingMove = context.getBasicAttackMove();
                currentState = BattleState.QUEUE_ACTION;
                break;

            case QUEUE_ACTION:
                buildActionSequence();
                break;

            case PLAYING_EVENTS:
                updateEventPlayback(delta);
                break;

            case APPLY_EFFECTS:
                if (checkWinLoss()) return;
                
                if (turnEngine.hasIconsLeft()) {
                    currentState = turnEngine.isPlayerTurn() ? BattleState.PLAYER_ACTION_SELECT : BattleState.ENEMY_DECIDE;
                } else {
                    currentState = turnEngine.isPlayerTurn() ? BattleState.ENEMY_TURN_START : BattleState.PLAYER_TURN_START;
                }
                break;

            case VICTORY:
                context.setOutcome(BattleContext.BattleOutcome.PLAYER_VICTORY);
                break;
            case DEFEAT:
                context.setOutcome(BattleContext.BattleOutcome.ENEMY_VICTORY);
                break;
        }
    }

    private void advanceToEventPlayback(BattleState nextStateAfterPlayback) {
        this.context.setNextStateBackup(nextStateAfterPlayback);
        this.currentState = BattleState.PLAYING_EVENTS;
        this.messageDisplayTimer = 0f;
        this.currentMessage = "";
    }

    private void updateEventPlayback(float delta) {
        messageDisplayTimer -= delta;
        if (messageDisplayTimer <= 0f) {
            if (eventTextQueue.size > 0) {
                currentMessage = eventTextQueue.removeFirst();
                messageDisplayTimer = MESSAGE_DURATION;
            } else {
                currentMessage = "";
                currentState = context.getNextStateBackup();
            }
        }
    }

    public void submitAction(BattleAction action) {
        if (currentState != BattleState.PLAYER_ACTION_SELECT) return;
        context.setSelectedAction(action);

        currentAttacker = context.getPlayer();
        currentDefender = context.getEnemy();

        if (action == BattleAction.ATTACK) {
            pendingMove = context.getBasicAttackMove();
            currentState = BattleState.QUEUE_ACTION;
        } else if (action == BattleAction.SKILL) {
            currentState = BattleState.PLAYER_SKILL_SELECT;
        } else if (action == BattleAction.DEFEND) {
            currentAttacker.setDefending(true);
            eventTextQueue.addLast(currentAttacker.name + " adopts a defensive stance!");
            turnEngine.consumeIcons(PressTurnEngine.TurnResult.NORMAL);
            advanceToEventPlayback(BattleState.APPLY_EFFECTS);
        }
    }

    public void submitSkill(String moveId) {
        if (currentState != BattleState.PLAYER_SKILL_SELECT) return;
        pendingMove = context.getMove(moveId);
        if (pendingMove == null) return;

        context.setSelectedMove(pendingMove);
        currentState = BattleState.QUEUE_ACTION;
    }

    private void buildActionSequence() {
        if (currentAttacker == null || pendingMove == null) return;

        eventTextQueue.addLast(currentAttacker.name + " casts " + pendingMove.name + ".");

        if (!currentAttacker.hasMp(pendingMove.mpCost)) {
            eventTextQueue.addLast("Execution failed! Insufficient MP.");
            advanceToEventPlayback(turnEngine.isPlayerTurn() ? BattleState.PLAYER_ACTION_SELECT : BattleState.ENEMY_DECIDE);
            return;
        }
        currentAttacker.spendMp(pendingMove.mpCost);

        // Agility and baseline tracking for accuracy check calculation
        float accuracyChance = pendingMove.hitRate * ((float) currentAttacker.stats.agi / Math.max(1, currentDefender.stats.agi));
        if (MathUtils.random() > accuracyChance) {
            eventTextQueue.addLast("CATASTROPHIC MISS!!");
            turnEngine.consumeIcons(PressTurnEngine.TurnResult.MISS);
            advanceToEventPlayback(BattleState.APPLY_EFFECTS);
            return;
        }

        // Elemental Affinity Check
        Affinity affinity = currentDefender.getAffinityFor(pendingMove.element);
        PressTurnEngine.TurnResult turnResult = PressTurnEngine.TurnResult.NORMAL;

        if (affinity == Affinity.NULLIFY) {
            eventTextQueue.addLast("BLOCK! The attack was entirely nullified!");
            turnEngine.consumeIcons(PressTurnEngine.TurnResult.NULLIFY);
            advanceToEventPlayback(BattleState.APPLY_EFFECTS);
            return;
        }
        if (affinity == Affinity.REPEL) {
            eventTextQueue.addLast("REPEL!! The kinetic energy shatters back!");
            int reflectedDamage = calculateRawDamage(currentAttacker, pendingMove);
            currentAttacker.takeDamage(reflectedDamage);
            eventTextQueue.addLast(currentAttacker.name + " suffers " + reflectedDamage + " points of reflected impact!");
            turnEngine.consumeIcons(PressTurnEngine.TurnResult.REPEL);
            advanceToEventPlayback(BattleState.APPLY_EFFECTS);
            return;
        }
        if (affinity == Affinity.ABSORB) {
            int absorbedHealing = calculateRawDamage(currentAttacker, pendingMove);
            currentDefender.healHp(absorbedHealing);
            eventTextQueue.addLast("ABSORB! " + currentDefender.name + " converts the dynamic elemental energy into healing!");
            turnEngine.consumeIcons(PressTurnEngine.TurnResult.ABSORB);
            advanceToEventPlayback(BattleState.APPLY_EFFECTS);
            return;
        }

        // Critical Hit Evaluation
        boolean canCrit = pendingMove.tags != null && pendingMove.tags.contains("physical") && !currentDefender.defending;
        boolean isCrit = false;
        if (canCrit) {
            float baseCritChance = 0.05f + ((float) (currentAttacker.stats.luk - currentDefender.stats.luk) * 0.01f);
            isCrit = MathUtils.random() < MathUtils.clamp(baseCritChance, 0.02f, 0.40f);
        }

        int rawDamage = calculateRawDamage(currentAttacker, pendingMove);
        
        // Apply final damage modification logic pass
        float multiplier = affinity.modifier;
        if (isCrit) {
            multiplier *= 1.5f;
            turnResult = PressTurnEngine.TurnResult.CRITICAL;
        } else if (affinity == Affinity.WEAK) {
            turnResult = PressTurnEngine.TurnResult.WEAKNESS;
        } else if (affinity == Affinity.RESIST) {
            turnResult = PressTurnEngine.TurnResult.RESIST;
        }

        int finalDamage = Math.max(1, MathUtils.round((float) rawDamage * multiplier / (currentDefender.defending ? 2.0f : 1.0f)));
        currentDefender.takeDamage(finalDamage);

        // Queue chronological feedback messages
        if (isCrit) {
            eventTextQueue.addLast("CRITICAL HIT!!! Crucial structural integrity bypass!");
        } else if (affinity == Affinity.WEAK) {
            eventTextQueue.addLast("WEAKNESS EXPLOITED!! System vulnerability struck!");
        } else if (affinity == Affinity.RESIST) {
            eventTextQueue.addLast("The strike was resisted.");
        }

        eventTextQueue.addLast(currentDefender.name + " takes " + finalDamage + " damage.");

        // Check and apply statuses if applicable
        if (pendingMove.canInflictStatus() && !currentDefender.defending && affinity != Affinity.RESIST) {
            if (MathUtils.random() < pendingMove.statusEffect.chance) {
                BattleEntity.ActiveStatus newStatus = new BattleEntity.ActiveStatus(
                    pendingMove.statusEffect.type, 3, 1, pendingMove.id
                );
                currentDefender.addStatus(newStatus);
                eventTextQueue.addLast(currentDefender.name + " is afflicted with [" + pendingMove.statusEffect.type.toUpperCase() + "]!");
            }
        }

        turnEngine.consumeIcons(turnResult);
        advanceToEventPlayback(BattleState.APPLY_EFFECTS);
    }

    private int calculateRawDamage(BattleEntity attacker, Move move) {
        int base = move.baseDamage;
        int scalingStat = (move.tags != null && move.tags.contains("physical")) ? attacker.stats.str : attacker.stats.mag;
        return base + (scalingStat * 2); 
    }

    private boolean checkWinLoss() {
        if (context.getEnemy().isDead()) {
            eventTextQueue.addLast("TARGET TERMINATED. VICTORY ACHIEVED.");
            advanceToEventPlayback(BattleState.VICTORY);
            return true;
        }
        if (context.getPlayer().isDead()) {
            eventTextQueue.addLast("PLAYER SQUAD WIPED. COMBAT SIMULATION TERMINATED.");
            advanceToEventPlayback(BattleState.DEFEAT);
            return true;
        }
        return false;
    }

    public String getCurrentMessage() { return currentMessage; }
    public BattleState getCurrentState() { return currentState; }
    public PressTurnEngine getTurnEngine() { return turnEngine; }
}