package battle;

import java.util.ArrayList;
import java.util.List;

/**
 * Data-driven combat move loaded from JSON.
 * This should match your move database fields closely.
 */
public class Move {

    public String id;
    public String name;

    /**
     * soul, modal, fusion, bebop, harmony, dissonance
     */
    public String element;

    /**
     * minor, mid, heavy, severe
     */
    public String tier;

    /**
     * single or aoe
     */
    public String targetType;

    public int mpCost;
    public int baseDamage;
    public Float critRate;
    public List<String> musicalEvidence = new ArrayList<>();

    /**
     * 0.0 to 1.0
     */
    public float hitRate = 1.0f;

    public List<String> tags = new ArrayList<>();

    public StatusEffect statusEffect = new StatusEffect();

    public Move() {
        // Required for JSON loading
    }

    public Move(String id, String name, String element, String tier, String targetType,
                int mpCost, int baseDamage, float hitRate) {
        this.id = id;
        this.name = name;
        this.element = element;
        this.tier = tier;
        this.targetType = targetType;
        this.mpCost = mpCost;
        this.baseDamage = baseDamage;
        this.hitRate = hitRate;
    }

    public boolean isSingleTarget() {
        return "single".equalsIgnoreCase(targetType);
    }

    public boolean isAoE() {
        return "aoe".equalsIgnoreCase(targetType) || "all".equalsIgnoreCase(targetType);
    }

    public boolean isCastableByMp(int currentMp) {
        return currentMp >= mpCost;
    }

    public boolean canInflictStatus() {
        return statusEffect != null
                && statusEffect.type != null
                && !statusEffect.type.isBlank()
                && !"null".equalsIgnoreCase(statusEffect.type)
                && statusEffect.chance > 0f;
    }

    public static class StatusEffect {
        public String type = "null";
        public float chance = 0f;

        public StatusEffect() {
        }

        public StatusEffect(String type, float chance) {
            this.type = type;
            this.chance = chance;
        }
    }
}