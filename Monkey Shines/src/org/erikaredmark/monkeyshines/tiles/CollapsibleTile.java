package org.erikaredmark.monkeyshines.tiles;

/**
 * 
 * Represents a specific collapsible tile in the level. There is no analog 'Collapsible' class to
 * store immutable state like the other tiles; the only thing immutable about this is the id for
 * drawing. Everything else is state dependent.
 * <p/>
 * Tiles collapse a bit when {@code collapse} is called. Collapsible tiles have 10 frames of
 * animation and 20 ticks of collapsibility before they are gone.
 * <p/>
 * When not collapsed, they act like thru tiles. When collapsed, they act like scene tiles.
 * 
 * @author Erika Redmark
 *
 */
public final class CollapsibleTile implements TileType {
	// Only immutable state; id of which collapsing tile this in graphics resource
	private final int id;

	// the level of damage the tile has currently taken. Once it hits 20 that effectively 
	// makes it 'dead'. Lower values mean LESS damage.
	int damage;
	
	public CollapsibleTile(final int id) {
		this.id = id;
		this.damage = 0;
	}
	
	/** 
	 * Returns the current damage this collapsible has suffered. 
	 * Collapsibles function in either collapsed or not but drawing utilities will draw them in
	 * intermediate states. 
	 */
	public int getDamange() { return damage; }

	@Override public int getId() { return id; }
	
	/**
	 * Collapsible tiles take on the characterstics of thrus when not destroyed, but lose that power
	 * when destroyed.
	 */
	@Override public boolean isThru() { return damage < 20; }
	
	@Override public boolean isSolid() { return false; }
	
	@Override public boolean isLandable() { return isSolid() || isThru(); } 
	
	@Override public void reset(boolean oddElseEven) { 
		damage = 0;
	}	
	
	@Override public void update() { 
		/* Damaging a tile is done per tick IF bonzo is standing on it. We don't have that context here so
		 * update does nothing. Collision logic must check for collapsing tile and call specific methods.
		 */
	}
	
	@Override public CollapsibleTile copy() {
		return new CollapsibleTile(this.id);
	}
	
	/**
	 * 
	 * Performs 1 tick of collapsing. This is typically called if bonzo is standing on a collapsible tile.
	 * This method can safely be called if the collapsible tile is gone; it just won't do anything.
	 * 
	 */
	public void collapse() {
		if (damage < 20)  ++damage;
	}
	
	@Override public boolean equals(Object o) {
		if (o == this)  return true;
		if (!(o instanceof CollapsibleTile) )  return false;
		
		CollapsibleTile other = (CollapsibleTile) o;
		
		return this.id == other.id;
	}
	
	@Override public int hashCode() {
		int result = 17;
		result += result * 31 + id;
		return result;
	}
	
	@Override public String toString() {
		return "Collapsible Tile of id " + id + " with damange " + damage;
	}
}
