package edu.nova.erikaredmark.monkeyshines.editor.dialog;

import java.awt.Canvas;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;

import com.google.common.base.Optional;

import edu.nova.erikaredmark.monkeyshines.Sprite;
import edu.nova.erikaredmark.monkeyshines.resource.WorldResource;

public final class SpriteChooserDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private Optional<Sprite> selectedSprite = Optional.absent();
	
	public SpriteChooserDialog(List<Sprite> sprites, WorldResource rsrc) {
		getContentPane().setLayout(new FlowLayout() );
		
		for (Sprite s : sprites) {
			Canvas c = new SpriteAnimationCanvas(s.getId(), s.getAnimationType(), s.getAnimationSpeed(), rsrc);
			c.addMouseListener(new SetSpriteMouseListener(s) );
			getContentPane().add(c);
		}
		
		JButton none = new JButton(new AbstractAction("None") {
			private static final long serialVersionUID = 1L;
			@Override public void actionPerformed(ActionEvent e) {
				selectAndQuit(Optional.<Sprite>absent() );
			}
		});
		
		getContentPane().add(none);

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
		dialog.setLocation(parent.getLocation() );
		dialog.setSize(200, 200);
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
