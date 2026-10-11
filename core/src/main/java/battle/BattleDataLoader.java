package battle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.SerializationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BattleDataLoader {

    private final Json json;

    public BattleDataLoader() {
        this.json = new Json();
        this.json.setIgnoreUnknownFields(true);
    }

    public BattleEntity loadBattleEntity(String internalPath) {
        FileHandle file = Gdx.files.internal(internalPath);

        DemonDefinition definition = json.fromJson(DemonDefinition.class, file);
        if (definition == null) {
            throw new SerializationException("Could not load demon data from: " + internalPath);
        }

        return definition.toBattleEntity();
    }

    public List<Move> loadMoves(String internalPath) {
        FileHandle file = Gdx.files.internal(internalPath);

        MoveDatabase database = json.fromJson(MoveDatabase.class, file);
        if (database == null || database.moves == null) {
            return new ArrayList<>();
        }

        return database.moves;
    }

    public Map<String, Move> loadMovesById(String internalPath) {
        List<Move> moves = loadMoves(internalPath);
        Map<String, Move> map = new HashMap<>();

        for (Move move : moves) {
            if (move != null && move.id != null && !move.id.isBlank()) {
                map.put(move.id, move);
            }
        }

        return map;
    }

    public Move loadMoveById(String internalPath, String moveId) {
        if (moveId == null || moveId.isBlank()) {
            return null;
        }

        for (Move move : loadMoves(internalPath)) {
            if (moveId.equals(move.id)) {
                return move;
            }
        }

        return null;
    }
}
