package battle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Runtime battle instance for a demon / ally / enemy.
 * Fully optimized for Atlus-style Press Turn affinity interactions.
 */
public class BattleEntity {

    public String id;
    public String instanceId;
    public String basicAttack = "attack";
    public List<String> instrumentIdentities = new ArrayList<>();
    public boolean arrangementCommitted;
    public boolean waitingForNextPhase;
    public String lastMoveId;
    public String lastTargetId;
    public String lockedMoveId;
    public String lockedTargetId;
    public String replacedInstanceId;
    public String name;
    public String race;
    public int level;

    public int maxHp;
    public int currentHp;
    public int maxMp;
    public int currentMp;

    public Stats stats = new Stats();

    /**
     * Maps element ids ("soul", "modal", etc.) to resistance strings ("WEAK", "RESIST", "NULL", "REPEL", "ABSORB")
     */
    public Map<String, String> affinities = new HashMap<>();

    public List<String> battleSkills = new ArrayList<>();
    public List<ActiveStatus> activeStatuses = new ArrayList<>();
    public boolean defending = false;

    public BattleEntity() {
        // Required for JSON loading
    }

    public BattleEntity(String id, String name, String race, int maxHp, int maxMp) {
        this.id = id;
        this.instanceId = id;
        this.name = name;
        this.race = race;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.maxMp = maxMp;
        this.currentMp = maxMp;
    }

    /**
     * Determines the entity's elemental resistance profile.
     * Guarding alters weaknesses to neutral handling.
     */
    public Affinity getAffinityFor(String element) {
        if (element == null) return Affinity.NEUTRAL;
        String resistanceStr = affinities.get(element.toLowerCase().trim());
        Affinity affinity = Affinity.fromString(resistanceStr);

        if (defending) {
            if (affinity == Affinity.WEAK) {
                return Affinity.NEUTRAL; 
            }
            if (affinity == Affinity.NEUTRAL) {
                return Affinity.RESIST;
            }
        }
        return affinity;
    }

    public boolean isAlive() { return currentHp > 0; }
    public boolean isDead() { return currentHp <= 0; }
    public boolean hasMp(int amount) { return currentMp >= amount; }

    public void takeDamage(int amount) {
        currentHp = Math.max(0, currentHp - Math.max(0, amount));
    }

    public void healHp(int amount) {
        currentHp = Math.min(maxHp, currentHp + Math.max(0, amount));
    }

    public void spendMp(int amount) {
        currentMp = Math.max(0, currentMp - Math.max(0, amount));
    }

    public void restoreMp(int amount) {
        currentMp = Math.min(maxMp, currentMp + Math.max(0, amount));
    }

    public void setDefending(boolean defending) {
        this.defending = defending;
    }

    public boolean hasSkill(String skillId) { return skillId != null && (skillId.equals(basicAttack) || battleSkills.contains(skillId)); }

    public boolean canContribute() {
        return isAlive() && !waitingForNextPhase && !arrangementCommitted
            && !hasStatus("muted") && !hasStatus("rushing") && !hasStatus("testified");
    }

    public void addSkill(String skillId) {
        if (skillId != null && !skillId.isBlank() && !battleSkills.contains(skillId)) {
            battleSkills.add(skillId);
        }
    }

    public void addStatus(ActiveStatus status) {
        if (status != null) {
            removeStatusById(status.statusId);
            activeStatuses.add(status);
        }
    }

    public void removeStatusById(String statusId) {
        activeStatuses.removeIf(s -> s != null && statusId != null && statusId.equals(s.statusId));
    }

    public boolean hasStatus(String statusId) {
        for (ActiveStatus status : activeStatuses) {
            if (status != null && statusId != null && statusId.equals(status.statusId)) {
                return true;
            }
        }
        return false;
    }

    public static class Stats {
        public int str;
        public int vit;
        public int mag;
        public int agi;
        public int luk;

        public Stats() {}

        public Stats(int str, int vit, int mag, int agi, int luk) {
            this.str = str;
            this.vit = vit;
            this.mag = mag;
            this.agi = agi;
            this.luk = luk;
        }
    }

    public static class ActiveStatus {
        public String statusId;
        public int remainingTurns;
        public int stacks;
        public String sourceMoveId;
        public String sourceActorId;

        public ActiveStatus() {}

        public ActiveStatus(String statusId, int remainingTurns, int stacks, String sourceMoveId) {
            this.statusId = statusId;
            this.remainingTurns = remainingTurns;
            this.stacks = stacks;
            this.sourceMoveId = sourceMoveId;
        }
    }
}