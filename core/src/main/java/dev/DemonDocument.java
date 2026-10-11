package dev;
import com.google.gson.*;
import content.ContentCatalog;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/** Lossless JSON-tree editing; unknown authored fields are never mapped through a reduced DTO. */
public final class DemonDocument {
    private static final Gson JSON=new GsonBuilder().serializeNulls().setPrettyPrinting().create();
    private final ContentCatalog catalog;
    public DemonDocument(ContentCatalog catalog){this.catalog=catalog;}
    public JsonObject parse(String text,String label){
        JsonObject raw=JsonParser.parseString(text).getAsJsonObject();
        ContentCatalog.validateDemon(raw,label,catalog.moves.keySet());
        if(catalog.races.values().stream().noneMatch(r->r.displayName.equals(raw.get("race").getAsString()))&& !"Human".equals(raw.get("race").getAsString()))throw new IllegalArgumentException(label+": unknown Race");
        int base=raw.has("base_level")?raw.get("base_level").getAsInt():raw.has("level")?raw.get("level").getAsInt():1;
        int max=raw.has("max_skill_level")?raw.get("max_skill_level").getAsInt():Integer.MAX_VALUE;
        if(raw.has("learnableSkills")){
            JsonArray learned=raw.getAsJsonArray("learnableSkills");
            if(raw.has("learnableCapacity")&&learned.size()>raw.get("learnableCapacity").getAsInt())throw new IllegalArgumentException(label+": learnable capacity exceeded");
            Set<String> ids=new HashSet<>();
            for(JsonElement e:learned){JsonObject skill=e.getAsJsonObject();int level=skill.get("level").getAsInt();if(level<=base||level>max)throw new IllegalArgumentException(label+": learn level outside (base_level, max_skill_level]");if(!ids.add(skill.get("skill").getAsString()))throw new IllegalArgumentException(label+": duplicate learnable skill");}
        }
        return raw;
    }
    public String format(JsonObject raw){return JSON.toJson(raw)+System.lineSeparator();}
    public JsonObject open(Path file)throws IOException{return parse(Files.readString(file),file.toString());}
    public void save(Path file,String text)throws IOException{
        JsonObject raw=parse(text,file.toString());Path absolute=file.toAbsolutePath();
        Path temp=Files.createTempFile(absolute.getParent(),"guise-demon-",".json");
        try{Files.writeString(temp,format(raw));try{Files.move(temp,absolute,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}catch(AtomicMoveNotSupportedException e){Files.move(temp,absolute,StandardCopyOption.REPLACE_EXISTING);}}finally{Files.deleteIfExists(temp);}
    }
    public List<String> runtimeDiagnostics(JsonObject raw){
        List<String> out=new ArrayList<>();Set<String> ids=new LinkedHashSet<>();if(raw.has("basicAttack"))ids.add(raw.get("basicAttack").getAsString());if(raw.has("battleSkills"))for(JsonElement e:raw.getAsJsonArray("battleSkills"))ids.add(e.getAsString());
        for(String id:ids)try{catalog.validateActiveMove(id);}catch(IllegalArgumentException e){out.add(e.getMessage());}return out;
    }
}
