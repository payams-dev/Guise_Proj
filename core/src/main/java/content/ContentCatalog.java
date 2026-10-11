package content;

import band.ArrangementDefinition;
import battle.*;
import com.google.gson.*;
import java.util.*;
import java.util.function.Function;

/** Shared lossless source catalog. Only explicitly supported records enter the slice. */
public final class ContentCatalog {
    private final Gson gson = new Gson();
    private final Function<String,String> read;
    public final Map<String,Move> moves = new LinkedHashMap<>();
    public final Map<String,JsonObject> rawMoves = new LinkedHashMap<>();
    public final Map<String,RaceDefinition> races = new LinkedHashMap<>();
    public final Map<String,StatusDefinition> statuses = new LinkedHashMap<>();
    public final Map<String,ArrangementDefinition> arrangements = new LinkedHashMap<>();
    public final List<String> diagnostics = new ArrayList<>();
    public ContentCatalog(Function<String,String> read) {
        this.read=read;
        for(JsonElement e:object("moves/moves.JSON").getAsJsonArray("moves")) {
            JsonObject raw=e.getAsJsonObject(); Move m=gson.fromJson(raw,Move.class);
            require(m.id!=null&&!m.id.isBlank()&&!moves.containsKey(m.id),"moves/moves.JSON", "duplicate/missing move ID");
            moves.put(m.id,m);rawMoves.put(m.id,raw.deepCopy());
        }
        for(JsonElement e:array("races/races.json")) {
            RaceDefinition r=gson.fromJson(e,RaceDefinition.class);
            require(r.id!=null&&!races.containsKey(r.id)&&moves.containsKey(r.basicAttackId),"races/races.json","invalid Race "+r.id);
            require(r.statWeights!=null&&r.statWeights.length==5,"races/races.json","five stat weights required");races.put(r.id,r);
        }
        for(JsonElement e:array("moves/status.JSON")) {StatusDefinition s=gson.fromJson(e,StatusDefinition.class);require(s.id!=null&&!statuses.containsKey(s.id),"moves/status.JSON","duplicate status");statuses.put(s.id,s);}
        for(JsonElement e:array("arrangements/arrangements.json")) {
            ArrangementDefinition a=gson.fromJson(e,ArrangementDefinition.class);
            require(a.id!=null&&!arrangements.containsKey(a.id)&&a.effect!=null&&a.roles.size()>=2&&a.roles.size()<=4&&a.iconCost>=1&&a.iconCost<=4,"arrangements/arrangements.json","invalid recipe "+a.id);
            for(String role:a.roles) require(races.values().stream().anyMatch(r->r.displayName.equals(role)),a.id,"unknown Race "+role);
            for(String id:a.requiredMoves.values()) validateActiveMove(id);
            for(int cost:a.mpCosts.values()) require(cost>=0,a.id,"negative MP cost");
            arrangements.put(a.id,a);
        }
    }
    public JsonObject object(String path){return JsonParser.parseString(read.apply(path)).getAsJsonObject();}
    public JsonArray array(String path){return JsonParser.parseString(read.apply(path)).getAsJsonArray();}
    public <T>T load(String path,Class<T> type){return gson.fromJson(read.apply(path),type);}
    public DemonDefinition demon(String path) {
        JsonObject raw=object(path);DemonDefinition d=gson.fromJson(raw,DemonDefinition.class);
        validateDemon(raw,path,moves.keySet());
        if(raw.has("elements")) diagnostics.add(path+": legacy elements mapped to affinities; ailments preserved as deferred data");
        if(d.affinities.containsKey("phys")) diagnostics.add(path+": phys aliases physical");
        return d;
    }
    public BattleEntity musician(String path,String instanceId) {
        BattleEntity e=demon(path).toBattleEntity();e.instanceId=instanceId;
        for(String skill:e.battleSkills)validateActiveMove(skill);
        validateActiveMove(e.basicAttack);
        return e;
    }
    public RaceDefinition race(String id) {
        RaceDefinition r=races.get(id);
        if(r==null)throw new IllegalArgumentException("Unknown instrument: "+id);return r;
    }
    public void validateActiveMove(String id) {
        Move m=moves.get(id);require(m!=null,"moves/moves.JSON", "missing move "+id);
        Set<String> allowed=new HashSet<>(Arrays.asList("id","name","element","tier","targetType","mpCost","baseDamage","hitRate","tags","statusEffect","instrumentType","critRate","musicalEvidence"));
        for(String field:rawMoves.get(id).keySet()) require(allowed.contains(field),"moves/moves.JSON :: "+id,"unsupported active field "+field+" (retained in catalog)");
        require(m.mpCost>=0&&m.baseDamage>=0&&Float.isFinite(m.hitRate)&&m.hitRate>=0,"moves/moves.JSON :: "+id,"invalid cost/damage/accuracy");
        require(m.isSingleTarget()||m.isAoE(),id,"unsupported target type "+m.targetType);
        if(m.canInflictStatus()) {
            StatusDefinition s=statuses.get(m.statusEffect.type);
            require(s!=null,id,"missing status "+m.statusEffect.type);
            require(m.statusEffect.chance<=1,id,"status chance exceeds one");
            require(Arrays.asList("damage_over_time","stagger","move_lock","mute","delay").contains(s.effectType),id,"unsupported status effect "+s.effectType);
        }
    }
    public static void validateDemon(JsonObject raw,String path,Set<String> moveIds) {
        require(raw.has("id")&&!raw.get("id").getAsString().isBlank(),path,"nonempty ID required");
        require(raw.has("name")&&raw.has("race")&&raw.has("stats"),path,"name/race/stats required");
        require(raw.has("hp")&&raw.get("hp").getAsInt()>0&&raw.has("mp")&&raw.get("mp").getAsInt()>=0,path,"invalid HP/MP");
        for(String key:Arrays.asList("str","vit","mag","agi","luk"))require(raw.getAsJsonObject("stats").has(key)&&raw.getAsJsonObject("stats").get(key).getAsInt()>=0,path,"invalid stat "+key);
        if(raw.has("level"))require(raw.get("level").getAsInt()>0,path,"invalid level");
        if(raw.has("basicAttack"))require(moveIds.contains(raw.get("basicAttack").getAsString()),path,"unknown basicAttack");
        if(raw.has("battleSkills"))for(JsonElement v:raw.getAsJsonArray("battleSkills"))require(moveIds.contains(v.getAsString()),path,"unknown battle skill "+v);
        if(raw.has("learnableSkills"))for(JsonElement v:raw.getAsJsonArray("learnableSkills"))require(moveIds.contains(v.getAsJsonObject().get("skill").getAsString()),path,"unknown learnable skill "+v);
        String affinityKey=raw.has("affinities")?"affinities":"elements";
        if(raw.has(affinityKey))for(Map.Entry<String,JsonElement> a:raw.getAsJsonObject(affinityKey).entrySet())
            require(Arrays.asList("weak","neutral","resist","null","nullify","repel","absorb","drain").contains(a.getValue().getAsString().toLowerCase(Locale.ROOT)),path,"unknown affinity "+a.getKey()+"="+a.getValue());
    }
    private static void require(boolean condition,String file,String message){if(!condition)throw new IllegalArgumentException(file+": "+message);}
}
