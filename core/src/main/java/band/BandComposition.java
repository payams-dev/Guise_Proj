package band;

import battle.BattleEntity;
import java.util.*;

/** Matches role multiplicities to actual active instances; never consults reserves. */
public final class BandComposition {
    public static final class Assignment {
        public final ArrangementDefinition recipe;
        public final List<BattleEntity> performers;
        Assignment(ArrangementDefinition recipe, List<BattleEntity> performers) {
            this.recipe = recipe;
            this.performers = Collections.unmodifiableList(new ArrayList<>(performers));
        }
        public BattleEntity role(String race) { return performers.get(recipe.roles.indexOf(race)); }
        public Set<BattleEntity> unique() { return new LinkedHashSet<>(performers); }
        public Map<BattleEntity,Integer> costs() {
            Map<BattleEntity,Integer> costs = new LinkedHashMap<>();
            for (int i=0;i<performers.size();i++) costs.merge(performers.get(i), recipe.mpCosts.getOrDefault(recipe.roles.get(i),0), Integer::sum);
            return costs;
        }
        public String reason(BattleEntity initiator, int icons) {
            if (initiator == null || !unique().contains(initiator)) return "Initiator must participate";
            boolean allowed = recipe.initiatorRoles.isEmpty();
            for (int i=0;i<performers.size();i++) if (performers.get(i)==initiator && recipe.initiatorRoles.contains(recipe.roles.get(i))) allowed=true;
            if (!allowed) return "This musician cannot initiate this Arrangement";
            if (icons < recipe.iconCost) return "Need " + recipe.iconCost + " icons";
            for (int i=0;i<performers.size();i++) {
                BattleEntity e=performers.get(i);
                if (!e.canContribute()) return e.name + " cannot contribute (dead, disabled, waiting, or already committed)";
                String move=recipe.requiredMoves.get(recipe.roles.get(i));
                if (move!=null && !e.hasSkill(move)) return e.name + " needs " + move;
            }
            for (Map.Entry<BattleEntity,Integer> c:costs().entrySet()) if (!c.getKey().hasMp(c.getValue())) return c.getKey().name+" needs "+c.getValue()+" MP";
            return null;
        }
    }
    public List<Assignment> match(ArrangementDefinition recipe, List<BattleEntity> active) {
        List<Assignment> out=new ArrayList<>();
        search(recipe, active, new ArrayList<>(), out);
        return out;
    }
    private void search(ArrangementDefinition r,List<BattleEntity> active,List<BattleEntity> selected,List<Assignment> out) {
        int index=selected.size();
        if(index==r.roles.size()){out.add(new Assignment(r,selected));return;}
        String role=r.roles.get(index);
        Set<String> seen=new HashSet<>();
        for(BattleEntity e:active) {
            if(!seen.add(e.instanceId)) continue;
            boolean virtuoso="Virtuoso".equals(e.race);
            if(!(virtuoso?e.instrumentIdentities.contains(role):role.equals(e.race))) continue;
            int uses=Collections.frequency(selected,e);
            if(uses>0) {
                if(!virtuoso || !r.dualRoleCompatible || uses>=2) continue;
                boolean repeatedRole=false;
                for(int j=0;j<selected.size();j++) if(selected.get(j)==e && r.roles.get(j).equals(role)) repeatedRole=true;
                if(repeatedRole) continue;
            }
            selected.add(e);search(r,active,selected,out);selected.remove(selected.size()-1);
        }
    }
}
