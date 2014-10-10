package org.erikaredmark.monkeyshines.editor.dialog;

import java.awt.Canvas;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.erikaredmark.monkeyshines.Sprite;
import org.erikaredmark.monkeyshines.bounds.IPoint2D;
import org.erikaredmark.monkeyshines.resource.WorldResource;

import com.google.common.base.Optional;

public final class SpriteChooserDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private Optional<Sprite> selectedSprite = Optional.absent();
	
	public SpriteChooserDialog(List<Sprite> sprites, WorldResource rsrc) {
		getContentPane().setLayout(new FlowLayout(FlowLayout.LEFT) );
		
		for (Sprite s : sprites) {
			JPanel spriteSelector = new JPanel();
			spriteSelector.setLayout(new FlowLayout() );
			spriteSelector.addMouseListener(new SetSpriteMouseListener(s) );
			Canvas c = new SpriteAnimationCanvas(s.getId(), s.getAnimationType(), s.getAnimationSpeed(), rsrc);
			// Both have mouse listeners; for some reason the canvas steals the general JPanel click.
			c.addMouseListener(new SetSpriteMouseListener(s) );
			spriteSelector.add(c);
			IPoint2D loc = s.getCurrentBounds().getLocation();
			JLabel location = new JLabel(loc.x() + ", " + loc.y() );
			spriteSelector.add(location);
			
			getContentPane().add(spriteSelector);
		}

	}
	
	/** Sets dialog to have the given selection as the model, and quits the dialog.
	 */
	private void selectAndQuit(Optional<Sprite> selection) {
		this.selectedSprite = selection;
		setVisible(false);
	}
	
	public static Optional<Sprite> launch(JComponent parent, List<Sprite> spriteChoices, WorldResource rsrc) {
		SpriteChooserDialog dialog = new SpriteChooserDialog(spriteChoices, rsrc);
		dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		dialog.setModal(true);
		dialog.setSize(240, 200);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		
		// Dialog is over at this point. Set model based on how it was exited to tell client.
		return dialog.selectedSprite;
	}
	
	private final class SetSpriteMouseListener implements MouseListener {
		private final Sprite sprite;
		
		public SetSpriteMouseListener(final Sprite s) { this.sprite = s; }
		@Override public void mouseClicked(MouseEvent e) { 
			selectAndQuit(Optional.of(sprite) );
		}
		
		@Override public void mouseEntered(MouseEvent e) { }
		@Override public void mouseExited(MouseEvent e) { }
		@Override public void mousePressed(MouseEvent e) { }
		@Override public void mouseReleased(MouseEvent e) { }
	}
	
}
