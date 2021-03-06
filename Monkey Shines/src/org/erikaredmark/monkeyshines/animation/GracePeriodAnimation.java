package org.erikaredmark.monkeyshines.animation;

import org.erikaredmark.monkeyshines.Bonzo;
import org.erikaredmark.monkeyshines.GameConstants;
import org.erikaredmark.monkeyshines.resource.SlickWorldGraphics;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

/**
 * 
 * State-based animation for when bonzo dies and time must be given to show where he is respawning. It is up to renderers to
 * decide when to fire the grace period animation.
 * <p/>
 * The animation is designed such that it does not steal control from the main renderer. The renderer will operate at the 
 * same speed as always and will fire this objects paint method instead of the main one for drawing the universe. Each call
 * to paint will render the next frame until all frames are rendered and paint returns 'false' for no more frames, in which
 * the client should then do what is required to allow this object to be gc'd.
 * <p/>
 * The main purpose is simply to encapsulate an animation state machine. All rendering takes place in addition to any other
 * rendering the system chooses to make. For example, it is still the responsibility of the client to render the actual world
 * with bonzo on it; this renders the circle and get-ready text only.
 * 
 * @author Erika Redmark
 *
 */
public final class GracePeriodAnimation {

	/**
	 * 
	 * Constructs a new animation for the grace period. This animation will be allowed to run for 'frames'
	 * number of frames before no longer working.
	 * 
	 * @param Bonzo
	 * 		reference to bonzo. His current location will be used to set the animation, but further changes
	 *		to his state will NOT affect the animation.
	 * 
	 * @param frames
	 * 		number of frames this animation should last.
	 * 
	 * @param xOffset
	 * 		xOffset for bonzos location. This is if there are UI elements to either the left or right of where the playable
	 * 		area is on the current graphics context.
	 * 
	 * @param yOffset
	 * 		yOffset for bonzos location. This is if there are UI elements to either the top or bottom of where
	 * 		the playable area is on the current graphics context.
	 * 
	 */
	public GracePeriodAnimation(Bonzo bonzo, SlickWorldGraphics slickGraphics, int frames, int xOffset, int yOffset) {
		// final, constant parameters basic on data
		centerX = (bonzo.getCurrentLocation().x() + 20) + xOffset;
		centerY = (bonzo.getCurrentLocation().y() + 20) + yOffset;
		maxRadius = 300;
		minRadius = 30;
		radiusStep = ((double)(maxRadius - minRadius)) / (double)frames;
		assert radiusStep > 0 : "Infinite Loop: Radius steps must  be positive non-zero values";
		
		opacityInitial = 255f;
		opacityFinal = 0f;
		opacityStep = (opacityFinal - opacityInitial) / (float)frames;
		
		getReady = slickGraphics.getReady;
		getReadyX = (GameConstants.SCREEN_WIDTH / 2) - (getReady.getWidth() / 2);
		getReadyY = (GameConstants.SCREEN_HEIGHT / 2) - (getReady.getHeight() / 2);
		getReadyX2 = getReadyX + getReady.getWidth();
		getReadyY2 = getReadyY + getReady.getHeight();
		
		// Mutable state data initialisation
		currentRadius = maxRadius;
		currentOpacity = opacityInitial;
	}
	
	/**
	 * 
	 * Paints the current frame of animation.
	 * <p/>
	 * Do not ignore the return value of updating. Animating after a false return can cause unexpected behaviour.
	 * <p/>
	 * Note: The g2d is mutated with a new stroke, colour, and composite; be sure to save and restore those values if
	 * required.
	 * 
	 * @param g2d
	 * 
	 * @return
	 * 
	 */
	public void paint(Graphics g) {
		
		// Animation: Start circle at low opacity from center of bonzo, at a good radius, then decrease radius and increase opacity
		// at rates such that by 'ms' time, circle if fully opaque and surrounds Bonzo directly.
		g.setLineWidth(5);
		Color magentaWithTrans = new Color(Color.magenta.r, Color.magenta.g, Color.magenta.b, currentOpacity);
		g.setColor(magentaWithTrans);
		
		g.drawImage(getReady, 
					getReadyX, getReadyY, 
					getReadyX2, getReadyY2, 
					0, 0, 
					getReady.getWidth(), getReady.getHeight());
		
		g.drawOval((int)(centerX - 2), 
				   (int)(centerY - 2), 
				   4, 
				   4);
		
		g.drawOval((int)(centerX - currentRadius), 
				   (int)(centerY - currentRadius), 
				   (int)(currentRadius*2), 
				   (int)(currentRadius*2) );

	}
	
	/**
	 * 
	 * Updates the animation to the next frame, returning if there are no more animation frames.
	 * 
	 * @return
	 * 		If this was the last frame for this object, returns {@code false},
	 * 		otherwise returns {@code true}
	 */
	public boolean update() {
		currentRadius -= radiusStep;
		currentOpacity += opacityStep;
		
		if (currentRadius > minRadius)  return true;
		else							return false;
	}
	
	// Immutable data
	final int centerX;
	final int centerY;
	final int maxRadius;
	final int minRadius;
	final double radiusStep;
	
	final float opacityInitial;
	final float opacityFinal;
	final float opacityStep;
	
	final int getReadyX;
	final int getReadyY;
	final int getReadyX2;
	final int getReadyY2;
	final Image getReady;
	
	// State data
	double currentRadius;
	float currentOpacity;
	
}
