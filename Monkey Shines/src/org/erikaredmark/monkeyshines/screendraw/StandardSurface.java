package org.erikaredmark.monkeyshines.screendraw;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.image.VolatileImage;

import org.erikaredmark.monkeyshines.GameConstants;
import org.erikaredmark.monkeyshines.GameWorldLogic;
import org.erikaredmark.monkeyshines.resource.AwtRenderer;
import org.erikaredmark.monkeyshines.resource.AwtWorldGraphics;

import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.*;

/**
 * 
 * Assumes a drawing size of 640x480 and draws the universe represented by the {@code GameWorldLogic} into that
 * size constraint. This only handles rendering the entire scene to an image, not how that image is then actually
 * painted to the screen.
 * <p/>
 * This is only intended for use with the Level Editor. It uses {@code AwtWorldGraphics} from the {@code WorldResource}
 * and cannot operate properly in normal game context.
 * <p/>
 * This surface has a 1:1 analog to the logical world. This means that a 20x20 unit tile is drawn at 20x20 pixels, no
 * graphics substitutions. Screen drawers that then draw the final graphics context on the screen may at their discretion
 * stretch or blow up the final result, however.
 * <p/>
 * The object itself is not drawn to anything, it merely provides the ability to render new images
 * that will be drawn to something.
 * 
 * @author Erika Redmark
 *
 */
public final class StandardSurface {
	
	private final GameWorldLogic universe;
	
	/**
	 * 
	 * Creates a new instance of a standard surface for the given world.
	 * 
	 * @param universe
	 * 
	 */
	public StandardSurface(final GameWorldLogic universe) {
		this.universe = universe;
	}
	/**
	 * 
	 * Draws the entire world at the current origin of the graphics context, by an area of 640x480, including
	 * UI.
	 * 
	 * @param g2d
	 * 		graphics configuration to create a compatible buffered image.
	 * 
	 * @param showUI
	 * 		{@code true} to draw the UI at 0,0 and game at 0,80, {@code false} to draw game at 0,0.
	 * 		this is typically {@code false} for when the splash screen is visible and {@code true} otherwise.
	 * 
	 */
	public void renderDirect(Graphics2D g2d, boolean showUI) {
		renderToGraphics(g2d, showUI);
	}
	
	/**
	 * 
	 * Draws the entire world onto a 640x480 image that will be stored in video memory. 
	 * Because of this, the image returned may suddenly cease to exist at any point. If
	 * it does, this method must be called again before repainting. It is important that
	 * clients check for lost contents of the image returned before using it.
	 * 
	 * @param configuration
	 * 		graphics configuration for the current hardware
	 * 
	 * @param showUI
	 * 		{@code true} to draw the UI at 0,0 and game at 0,80, {@code false} to draw game at 0,0.
	 * 		this is typically {@code false} for when the splash screen is visible and {@code true} otherwise
	 * 
	 */
	public VolatileImage renderVolatile(GraphicsConfiguration configuration, boolean showUI) {
		VolatileImage page = configuration.createCompatibleVolatileImage(SURFACE_SIZE_X, SURFACE_SIZE_Y);
		
		do {
			Graphics2D g2d = page.createGraphics();
			try {
				renderToGraphics(g2d, showUI);
			} finally {
				g2d.dispose();
			}
		} while (page.contentsLost() );
		
		// Successful draw; return the image.
		return page;
	}
	
	/**
	 * 
	 * Renders the world to the graphics context. 
	 * 
	 * @param showUI
	 * 		{@code true} to draw the UI at 0,0 and game at 0,80, {@code false} to draw game at 0,0.
	 * 		this is typically {@code false} for when the splash screen is visible and {@code true} otherwise.
	 * 
	 * @param g2d
	 */
	private void renderToGraphics(Graphics2D g2d, boolean showUI) {
		// Clear
		g2d.clearRect(0, 0, GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT + GameConstants.UI_HEIGHT);
		AwtWorldGraphics awtGraphics = universe.getWorld().getResource().getAwtGraphics();
		
		if (showUI) {
			AwtRenderer.paintUI(g2d, universe, awtGraphics);
		}
		
		/* ----------------------- Actual Game -------------------------- */
		// game is drawn at 80 pixels down if UI was drawn
		if (showUI) {
			g2d.translate(0, 80);
			AwtRenderer.paintUniverse(g2d, universe, awtGraphics);
			g2d.translate(0, -80);
		} else {
			AwtRenderer.paintUniverse(g2d, universe, awtGraphics);
		}
		
	}

}
