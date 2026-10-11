
package battle;

public enum Affinity {
	WEAK(1.5f), NEUTRAL(1.0f), RESIST(0.5f), NULLIFY(0.0f), REPEL(0.0f), ABSORB(-1.0f); // Negative value denotes
																						// healing

	public final float modifier;

	Affinity(float modifier) {
		this.modifier = modifier;
	}

	public static Affinity fromString(String val) {
		if (val == null)
			return NEUTRAL;
		switch (val.toLowerCase().trim()) {
		case "weak":
			return WEAK;
		case "resist":
			return RESIST;
		case "null":
		case "nullify":
			return NULLIFY;
		case "repel":
			return REPEL;
		case "drain":
        case "absorb":
			return ABSORB;
		default:
			return NEUTRAL;
		}
	}
}