package org.erikaredmark.monkeyshines.editor;

import java.util.List;

import org.erikaredmark.monkeyshines.AnimationSpeed;
import org.erikaredmark.monkeyshines.AnimationType;
import org.erikaredmark.monkeyshines.ImmutablePoint2D;
import org.erikaredmark.monkeyshines.ImmutableRectangle;
import org.erikaredmark.monkeyshines.LevelScreen;
import org.erikaredmark.monkeyshines.Sprite;
import org.erikaredmark.monkeyshines.Sprite.SpriteType;
import org.erikaredmark.monkeyshines.resource.WorldResource;
import org.erikaredmark.monkeyshines.tiles.TileType;

/**
 * This object forwards changes to the level screen, but for every change made stores it and provides functionality to write
 * the level to a disk. This is intended ONLY for the level editor.
 * TODO storing not implemented yet. Will become part of undo/redo system much later
 * 
 * @author Erika Redmark
 */
public class LevelScreenEditor {

	/* unmodifiable version from the level data.																		*/
	private final LevelScreen screen;
	
	private LevelScreenEditor(final LevelScreen screen) {
		this.screen = screen;
	}
	
	/**
	 *  Forwards call to the wrapped {@link LevelScreen}, and stores the change.
	 */
	public void setTile(int tileX, int tileY, TileType tileType, int tileId) {
		this.screen.setTile(tileX, tileY, tileType, tileId);
	}
	
	/**
	 * Forwards call to the wrapped {@link LevelScreen}, and stores the change.
	 */
	public void eraseTile(int i, int j) {
		this.screen.eraseTile(i, j);
	}

	public int getId() { return screen.getId(); }
	
	public ImmutablePoint2D getBonzoStartingLocation() { return screen.getBonzoStartingLocation(); }
	
	public ImmutablePoint2D getBonzoStartingLocationPixels() { return screen.getBonzoStartingLocationPixels(); }

	/**
	 * Creates a new level screen editor based on an existing level screen
	 * 
	 * @param screen
	 * 		actual screen, most likely returned from a valid {@link World} instance
	 * 
	 * @return
	 * 		a new instance of a level screen editor for the given screen
	 */
	public static LevelScreenEditor from(LevelScreen screen) {
		return new LevelScreenEditor(screen);
	}
	
	/** Forwarding call to wrapped {@code LevelEditor}
	 */
	public List<Sprite> getSpritesWithin(ImmutablePoint2D point, int size) {
		return this.screen.getSpritesWithin(point, size);
	}
	
	/**
	 * 
	 * Returns every sprite on the screen
	 * 
	 * @return
	 * 		all sprites
	 * 
	 */
	public List<Sprite> getSpritesOnScreen() {
		return this.screen.getSpritesOnScreen();
	}

	/**
	 * 
	 * Creates a sprite and adds it to the given world.
	 * 
	 * @param spriteId
	 * 		the id of the sprite, for graphics only
	 * 	
	 * @param spriteStartingLocation
	 * 		starting location of sprite
	 * 
	 * @param spriteBoundingBox
	 * 		bounding box of sprite
	 * 
	 * @param spriteVelocity
	 * 		speed and direction of sprite
	 * 
	 * @param rsrc
	 * 		graphics resource for skinning
	 * 
	 */
	public void addSprite(final int spriteId,
						  final ImmutablePoint2D spriteStartingLocation,
						  final ImmutableRectangle spriteBoundingBox,
						  final ImmutablePoint2D spriteVelocity,
						  final AnimationType animationType,
						  final AnimationSpeed animationSpeed,
						  final SpriteType spriteType,
						  final WorldResource rsrc) {
		
		Sprite s =
			Sprite.newSprite(spriteId, spriteStartingLocation, spriteBoundingBox, spriteVelocity, animationType, animationSpeed, spriteType, rsrc);
		
		screen.addSprite(s);
		
	}

	
	public void removeSprite(Sprite sprite) {
		screen.remove(sprite);
	}

}
