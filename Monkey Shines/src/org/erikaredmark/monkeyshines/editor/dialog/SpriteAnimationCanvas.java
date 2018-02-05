package org.erikaredmark.monkeyshines.editor.dialog;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import org.erikaredmark.monkeyshines.AnimationSpeed;
import org.erikaredmark.monkeyshines.AnimationType;
import org.erikaredmark.monkeyshines.GameConstants;
import org.erikaredmark.monkeyshines.MonsterType;
import org.erikaredmark.monkeyshines.resource.AwtRenderer;
import org.erikaredmark.monkeyshines.resource.WorldResource;
import org.erikaredmark.monkeyshines.sprite.Monster;

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
	
	private Monster animatingSprite;
	
	private final WorldResource rsrc;
	
	Graphics bufferGraphics;
	
	public SpriteAnimationCanvas(final int spriteId, final AnimationType animationType, final AnimationSpeed speed, final WorldResource rsrc) {
		this.rsrc = rsrc;
		// Type is irrelevant for the canvas: no concept of collisions
		this.animatingSprite = Monster.newUnmovingMonster(spriteId, animationType, speed, MonsterType.NORMAL, rsrc);
		this.animatingSprite.setVisible(true);
		// Make sprite animate
		Timer animationTimer = new Timer(GameConstants.GAME_SPEED, new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				animatingSprite.update();
				SpriteAnimationCanvas.this.repaint();
			}
			
		});
		this.setPreferredSize(new Dimension(40, 40) );
		animationTimer.start();
	}
	
	public void setSpriteId(int id) {
		this.animatingSprite = Monster.newUnmovingMonster(id, this.animatingSprite.getAnimationType(), this.animatingSprite.getAnimationSpeed(), MonsterType.NORMAL, rsrc);
		this.animatingSprite.setVisible(true);
		this.repaint();
	}
	
	public void setAnimationType(AnimationType type) {
		this.animatingSprite = Monster.newUnmovingMonster(this.animatingSprite.getId(), type, this.animatingSprite.getAnimationSpeed(), MonsterType.NORMAL, rsrc);
		this.animatingSprite.setVisible(true);
	}
	
	public void setAnimationSpeed(AnimationSpeed speed) {
		this.animatingSprite = Monster.newUnmovingMonster(this.animatingSprite.getId(), this.animatingSprite.getAnimationType(), speed, MonsterType.NORMAL, rsrc);
		this.animatingSprite.setVisible(true);
	}
	
	@Override public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		AwtRenderer.paintMonster(g2d, animatingSprite, rsrc.getAwtGraphics());
	}
	
}
