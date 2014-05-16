package edu.nova.erikaredmark.monkeyshines.encoder;

/**
 * 
 * Hazards are a bit more difficult than stateless tile types. They have additional information that must be saved for
 * the system to know exactly what kind of hazard they are.
 * 
 * @author Erika Redmark
 *
 */
public final class EncodedHazardTileType implements EncodableTileType {
	private static final long serialVersionUID = 1L;
	
	// This is the index in the encoded hazards for the save form of the world that this specific tile identifies
	// with.
	private final int hazardId;
	
	/**
	 * 
	 * Unlike other encoded forms, this is NOT created from the HazardTile itself; that contains irrelevant state. It
	 * is created from just the EncodedHazard itself. An entry in the EncodedTiles[][] array represents one unique
	 * hazard tile type for that location. This whole interface 'EncodableTileType' is done so that both Stateless
	 * tile types and hazards have compatible types for storage.
	 * 
	 * @param template
	 * 		the encoded hazard template to base this specific tile type on when decoded
	 * 
	 */
	public EncodedHazardTileType(final int hazardId) {
		this.hazardId = hazardId;
	}
	
	public int getHazardId() { return hazardId; }

}
