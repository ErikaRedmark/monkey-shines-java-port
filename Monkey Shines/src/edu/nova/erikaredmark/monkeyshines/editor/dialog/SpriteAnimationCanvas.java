package edu.nova.erikaredmark.monkeyshines.editor.dialog;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import edu.nova.erikaredmark.monkeyshines.AnimationSpeed;
import edu.nova.erikaredmark.monkeyshines.AnimationType;
import edu.nova.erikaredmark.monkeyshines.Sprite;
import edu.nova.erikaredmark.monkeyshines.graphics.WorldResource;

/**
 * 
 * Simply displays a sprite animating continously and not moving. Designed for the level editor.
 * <p/>
 * The class is created with a valid graphics resource. The sprite in question can be changed by setting the id of this
 * canvas.
 * 
 * @author Erika Redmark
 *
 */
public class SpriteAnimationCanvas extends Canvas {
	private static final long serialVersionUID = 1L;
	
	private Sprite animatingSprite;
	
	private final WorldResource rsrc;
	
	Graphics bufferGraphics;
	
	public SpriteAnimationCanvas(final int spriteId, final AnimationType animationType, final AnimationSpeed speed, final WorldResource rsrc) {
		this.rsrc = rsrc;
		this.animatingSprite = Sprite.newUnmovingSprite(spriteId, animationType, speed, rsrc);
		// Make sprite animate
		Timer animationTimer = new Timer(100, new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				animatingSprite.update();
				SpriteAnimationCanvas.this.repaint();
			}
			
		});
		animationTimer.start();
	}
	
	public void setSpriteId(int id) {
		this.animatingSprite = Sprite.newUnmovingSprite(id, this.animatingSprite.getAnimationType(), this.animatingSprite.getAnimationSpeed(), rsrc);
		this.repaint();
	}
	
	public void setAnimationType(AnimationType type) {
		this.animatingSprite = Sprite.newUnmovingSprite(this.animatingSprite.getId(), type, this.animatingSprite.getAnimationSpeed(), rsrc);
	}
	
	public void setAnimationSpeed(AnimationSpeed speed) {
		this.animatingSprite = Sprite.newUnmovingSprite(this.animatingSprite.getId(), this.animatingSprite.getAnimationType(), speed, rsrc);
	}
	
	@Override public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		// TODO animate
		animatingSprite.paint(g2d);
	}
	
}
