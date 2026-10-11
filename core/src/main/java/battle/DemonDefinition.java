package battle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;

public class DemonDefinition {

	public String id;
	public String name;
	public String race;
    public int schemaVersion = 1;
    public int level = 1;
    public int base_level;
    public int max_skill_level;
    public String basicAttack = "attack";
    public HashMap<String,String> affinities = new HashMap<>();
    public HashMap<String,String> elements = new HashMap<>();
    public HashMap<String,String> ailments = new HashMap<>();

	public int hp;
	public int mp;

	public Stats stats = new Stats();

	public List<String> battleSkills = new ArrayList<>();
	public List<LearnableSkill> learnableSkills = new ArrayList<>();

	public BattleEntity toBattleEntity() {
		BattleEntity entity = new BattleEntity(id, name, race, hp, mp);

		entity.level = level;
        entity.basicAttack = basicAttack;
        Map<String,String> source = affinities.isEmpty() ? elements : affinities;
        for (Map.Entry<String,String> entry : source.entrySet()) {
            String key = entry.getKey().toLowerCase(Locale.ROOT);
            entity.affinities.put(key.equals("phys") ? "physical" : key, entry.getValue());
        }
		entity.stats = new BattleEntity.Stats(stats.str, stats.vit, stats.mag, stats.agi, stats.luk);

		if (battleSkills != null) {
			entity.battleSkills.addAll(battleSkills);
		}

		return entity;
	}

	public static class Stats {
		public int str;
		public int vit;
		public int mag;
		public int agi;
		public int luk;
	}

	public static class LearnableSkill {
		public int level;
		public String skill;
	}
}