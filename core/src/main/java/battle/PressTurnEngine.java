package battle;

import com.badlogic.gdx.utils.Array;

public class PressTurnEngine {
    
    public enum IconState {
        FULL,      // Solid action coin
        FLASHING   // Generated bonus action coin
    }

    public enum TurnResult {
        NORMAL,
        WEAKNESS,
        CRITICAL,
        MISS,
        RESIST,
        NULLIFY,
        REPEL,
        ABSORB
    }

    private final Array<IconState> iconPool = new Array<>();
    private boolean isPlayerTurn = true;
    private int maxTurnIconsThisPhase = 0;

    public void startTurn(boolean playerTurn, int combatantCount) {
        this.isPlayerTurn = playerTurn;
        this.iconPool.clear();
        this.maxTurnIconsThisPhase = Math.max(1, Math.min(4, combatantCount));
        
        for (int i = 0; i < maxTurnIconsThisPhase; i++) {
            iconPool.add(IconState.FULL);
        }
    }

    public void consumeIcons(TurnResult result) {
        if (iconPool.size == 0) return;

        switch (result) {
            case WEAKNESS:
            case CRITICAL:
                // SMT Rule: If consuming a solid FULL icon, convert it to FLASHING at the back
                if (iconPool.first() == IconState.FULL) {
                    iconPool.removeIndex(0);
                    iconPool.add(IconState.FLASHING);
                } else {
                    // Flashing icons are consumed normally
                    iconPool.removeIndex(0);
                }
                break;

            case NORMAL:
            case RESIST:
                // Standard deduction
                iconPool.removeIndex(0);
                break;

            case MISS:
            case NULLIFY:
                // Modern Atlus penalty: Blunder costs 2 complete icon instances
                removeIcons(2);
                break;

            case REPEL:
            case ABSORB:
                // Catastrophic penalty: Absolute loss of all remaining active icons
                iconPool.clear();
                break;
        }
    }

    private void removeIcons(int count) {
        for (int i = 0; i < count; i++) {
            if (iconPool.size > 0) {
                iconPool.removeIndex(0);
            }
        }
    }

    public boolean hasIconsLeft() { return iconPool.size > 0; }
    public Array<IconState> getIconPool() { return new Array<>(iconPool); }
    public int iconCount() { return iconPool.size; }
    public void reserveArrangementCost(int cost) {
        if (cost < 1 || iconPool.size < cost) throw new IllegalArgumentException("Not enough icons");
        removeIcons(cost - 1);
    }
    public boolean isPlayerTurn() { return isPlayerTurn; }
}