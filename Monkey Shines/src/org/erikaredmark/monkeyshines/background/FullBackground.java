package org.erikaredmark.monkeyshines.background;

/**
 * 
 * Represents a full, 640x400 background that is displayed taking up the entire window. These backgrounds are capable of
 * being very finely detailed, to the point of being substituted as scenery if so desired. They can be created from a single
 * 640x400 piece (the full background) or created from a Pattern (which is a smaller image that will be tiled onto
 * a 640x400 context)
 * <p/>
 * This does not hold graphics data. It merely refers to the id in the fullBackgrounds (or in patterned backgrounds)
 * for the given graphics context to where to find this background.
 * <p/>
 * Instances of this class are immutable. System should create one instance per full background and instances are accessible
 * via {@code WorldResource}
 * 
 * @author Erika Redmark
 *
 */
public class FullBackground extends Background {

	private final int id;
	private final boolean isPattern;

	/**
	 * Creates a full background of the given id. If isPattern is true, then the id
	 * will refer to the dynamically generated pattern backgrounds, meaning that whatever
	 * pattern is at the given id, a background made of that pattern will be selected. If
	 * isPattern is false, this will draw from the graphics resources' full backgrounds.
	 * @param id
	 * @param isPattern
	 */
	public FullBackground(int id, boolean isPattern) {
		this.id = id;
		this.isPattern = isPattern;
	}

	/**
	 * Returns the id of this background or patterned background.
	 */
	public int getId() { return id; }
	
	/**
	 * Returns whether this background was created from a pattern or as a full background.
	 */
	public boolean isPattern() { return isPattern; }
}
