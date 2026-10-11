package band;

import java.util.*;

/** Prototype recipes are explicit data, not a scripting language. */
public class ArrangementDefinition {
    public enum Effect { BLUE_NOTE, LOCKED_IN, CALL_RESPONSE, WALKING_SOLO, FULL_CHORUS }
    public String id, displayName, effectFamily;
    public boolean prototype = true, dualRoleCompatible;
    public List<String> roles = new ArrayList<>();
    public List<String> initiatorRoles = new ArrayList<>();
    public Map<String,String> requiredMoves = new LinkedHashMap<>();
    public Map<String,Integer> mpCosts = new LinkedHashMap<>();
    public int iconCost;
    public Effect effect;
}
