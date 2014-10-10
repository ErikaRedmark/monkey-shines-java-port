package org.erikaredmark.monkeyshines.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.erikaredmark.monkeyshines.AnimationSpeed;
import org.erikaredmark.monkeyshines.AnimationType;
import org.erikaredmark.monkeyshines.GameConstants;
import org.erikaredmark.monkeyshines.ImmutablePoint2D;
import org.erikaredmark.monkeyshines.ImmutableRectangle;
import org.erikaredmark.monkeyshines.LevelScreen;
import org.erikaredmark.monkeyshines.Sprite;
import org.erikaredmark.monkeyshines.Sprite.ForcedDirection;
import org.erikaredmark.monkeyshines.Sprite.SpriteType;
import org.erikaredmark.monkeyshines.Sprite.TwoWayFacing;
import org.erikaredmark.monkeyshines.background.Background;
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

	private final LevelScreen screen;
	private static final ImmutableRectangle PLAYABLE_FIELD = ImmutableRectangle.of(0, 0, GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT);
	
	private LevelScreenEditor(final LevelScreen screen) {
		this.screen = screen;
	}
	
	/**
	 *  Forwards call to the wrapped {@link LevelScreen}
	 */
	public void setTile(int tileX, int tileY, TileType tileType) {
		this.screen.setTile(tileX, tileY, tileType);
	}
	
	/**
	 * Forwards call to the wrapped {@link LevelScreen}
	 */
	public void eraseTile(int i, int j) {
		this.screen.eraseTile(i, j);
	}
	
	public Background getBackground() {
		return this.screen.getBackground();
	}
	
	/**
	 * Forwards call to {@code setBackground}
	 */
	public void setBackground(Background newBackground) {
		this.screen.setBackground(newBackground);
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
	 * Returns every sprite on the screen, screen being the level screen, not the displayable area.
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
	 * Returns every sprite that is currently out of bounds of the drawable area. This means that at least some part
	 * of the sprite is clipped by the edge of the drawable area, and includes sprites that are COMPLETELY clipped off
	 * the edge. Some sprites may start out of bounds and move inside bounds, or go in and out of bounds. This allows the
	 * editor to load up, modify, or delete sprites that cannot otherwise be clicked on.
	 * <p/>
	 * The returned list cannot be modified
	 * 
	 * @return
	 * 		list of sprites currently out of bounds. Cannot be modified
	 * 
	 */
	public List<Sprite> getSpritesOutOfBounds() {
		List<Sprite> outOfBounds = new ArrayList<>();
		for (Sprite s : this.screen.getSpritesOnScreen() ) {
			ImmutableRectangle rect = s.getCurrentBounds();
			if (rect.intersect(PLAYABLE_FIELD) == null) {
				outOfBounds.add(s);
			}
		}
		
		return Collections.unmodifiableList(outOfBounds);
	}

	/**
	 * 
	 * Creates a sprite and adds it to the given world.
	 * 
	 */
	public void addSprite(final int spriteId,
						  final ImmutablePoint2D spriteStartingLocation,
						  final ImmutableRectangle spriteBoundingBox,
						  final ImmutablePoint2D spriteVelocity,
						  final AnimationType animationType,
						  final AnimationSpeed animationSpeed,
						  final SpriteType spriteType,
						  final ForcedDirection forcedDirection,
						  final TwoWayFacing twoWayDirection,
						  final WorldResource rsrc) {
		
		Sprite s =
			Sprite.newSprite(spriteId, 
							 spriteStartingLocation, 
							 spriteBoundingBox, 
							 spriteVelocity, 
							 animationType, 
							 animationSpeed, 
							 spriteType,
							 forcedDirection,
							 twoWayDirection,
							 rsrc);
		
		// Game rules for visibility do not apply in the editor.
		s.setVisible(true);
		
		screen.addSprite(s);
		
	}
	

	/**
	 * 
	 * Forwards to {@code LevelScreen.replaceSprite(Sprite, Sprite) }
	 * 
	 */
	public void replaceSprite(Sprite sprite, Sprite newSprite) {
		screen.replaceSprite(sprite, newSprite);
	}

	public void removeSprite(Sprite sprite) {
		screen.removeSprite(sprite);
	}
	
	/**
	 * 
	 * Forwards to an internal, special method in World that stops sprites from moving. They no longer
	 * update or animate. This is intended ONLY for the level editor. Has no effect if sprites are already
	 * not animating.
	 * The editor is in a default state of animating sprites.
	 * 
	 */
	public void stopAnimatingSprites() {
		screen.setSpriteAnimation(false);
	}

	/**
	 * 
	 * Starts up the sprite animation if it was already stopped. Has no effect if sprites are already animating.
	 * 
	 */
	public void startAnimatingSprites() {
		screen.setSpriteAnimation(true);
	}

	/**
	 * 
	 * @return
	 * 		{@code true} if sprites are currently animating, {@code false} if otherwise
	 * 
	 */
	public boolean isAnimatingSprites() {
		return screen.getSpriteAnimation();
	}
	
	/**
	 * Forwards to {@code resetScreen} in {@code LevelScreen}
	 */
	public void resetCurrentScreen() {
		screen.resetScreen();
	}


}
