package org.erikaredmark.monkeyshines.editor.dialog;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.erikaredmark.monkeyshines.background.Background;
import org.erikaredmark.monkeyshines.background.FullBackground;
import org.erikaredmark.monkeyshines.background.SingleColorBackground;
import org.erikaredmark.monkeyshines.editor.resource.EditorResource;
import org.erikaredmark.monkeyshines.resource.WorldResource;

public class SetBackgroundDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private final Canvas backgroundViewer;
	
	// The model for this dialog: The user's selected background choice.
	private Background selectedBackground;
	
	public SetBackgroundDialog(final WorldResource rsrc, final Background currentBackground) {
		this.selectedBackground = currentBackground;
		// Three tabs: One for full backgrounds, one for patterns, and finally one for
		// a colour picker.
		// Show a 'selected background' to the right so they know which of the three tabs
		// is active and which background was picked.
		setTitle("Background Picker");
		setLayout(new FlowLayout() );
		
		/* --------------------- Full background Panel  --------------------- */
		JScrollPane fullBackgroundScrollPane = new JScrollPane();
		JPanel fullBackgroundPanel = new JPanel();
		fullBackgroundPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0) );
		for (int i = 0; i < rsrc.getBackgroundCount(); i++) {
			final Background next = rsrc.getBackground(i);
			BufferedImage image = EditorResource.generateThumbnailForBackground((FullBackground)next);
			JButton button = new JButton(new ImageIcon(image) );
			button.setMargin(new Insets(2, 2, 2, 2) );
			button.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					setSelectedBackground(next);
				}
			});
		}
		fullBackgroundScrollPane.add(fullBackgroundPanel);
		
		/* ------------------------- Pattern Panel  ------------------------- */
		JScrollPane patternScrollPane = new JScrollPane();
		JPanel patternPanel = new JPanel();
		patternPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0) );
		for (int i = 0; i < rsrc.getPatternCount(); i++) {
			final Background next = rsrc.getPattern(i);
			BufferedImage image = EditorResource.generateThumbnailForBackground((FullBackground)next);
			JButton button = new JButton(new ImageIcon(image) );
			button.setMargin(new Insets(2, 2, 2, 2) );
			button.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					setSelectedBackground(next);
				}
			});
		}
		patternScrollPane.add(patternPanel);
		
		/* ----------------------------- Colour  ---------------------------- */
		JPanel singleColorPanel = new JPanel();
		singleColorPanel.setLayout(new FlowLayout() );
		// If the background is initially a color background, use that as first color. Otherwise default black
		Color initialColor =   currentBackground instanceof SingleColorBackground
						     ? ((SingleColorBackground)currentBackground).getColor()
						     : Color.BLACK;
		final JColorChooser colorChooser = new JColorChooser(initialColor);
		colorChooser.getSelectionModel().addChangeListener(new ChangeListener() {
			@Override public void stateChanged(ChangeEvent event) {
				Color newColor = colorChooser.getColor();
				selectedBackground = new SingleColorBackground(newColor);
			}
		});
		singleColorPanel.add(colorChooser);
		
		JTabbedPane differentBackgroundsPane = new JTabbedPane();
		differentBackgroundsPane.addTab("Full", fullBackgroundPanel);
		differentBackgroundsPane.addTab("Pattern", patternPanel);
		differentBackgroundsPane.addTab("Solid", singleColorPanel);
		add(differentBackgroundsPane);
		
		/* ---------------------------- Viewer ------------------------------ */
		backgroundViewer = new Canvas() {
			private static final long serialVersionUID = 1L;

			@Override public void paint(Graphics g) {
				selectedBackground.draw((Graphics2D)g);
			}
		};
		backgroundViewer.setSize(100, 160);
		add(backgroundViewer);
		
		/* ------------------------ Okay and Cancel -------------------------- */
		JButton okay = new JButton(new AbstractAction("Okay") {
			private static final long serialVersionUID = 1L;
			@Override public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});

		add(okay);
		
		JButton cancel = new JButton(new AbstractAction("Cancel") {
			private static final long serialVersionUID = 1L;
			@Override public void actionPerformed(ActionEvent e) {
				// reset selected to original
				selectedBackground = currentBackground;
				setVisible(false);
			}
		});
		
		add(cancel);
		
		setSize(640, 400);
	}
	
	/**
	 * 
	 * Sets the users selected background preference, updating the 'current background'
	 * display
	 * 
	 */
	private void setSelectedBackground(Background background) {
		selectedBackground = background;
		backgroundViewer.repaint();
	}
	
	/**
	 * 
	 * Launches this dialog, allowing the user to choose a background. The user may choose an existing
	 * full background, pattern, or solid color. If the user closes the dialog without choosing, this
	 * will return {@code null}; null should signify 'no change'. 
	 * 
	 * @param rsrc
	 * 		the world resource to grab the backgrounds from
	 * 
	 * @param currentBackground
	 * 		the current background for the world. If the user makes no changes, this will be returned
	 * 
	 * @return
	 * 		a background instance the user chose, or the current background if they canceled. Never {@code null}
	 * 
	 */
	public static Background launch(WorldResource rsrc, Background currentBackground) {
		SetBackgroundDialog dialog = new SetBackgroundDialog(rsrc, currentBackground);
		dialog.setVisible(true);
		
		return dialog.selectedBackground;
	}
	
}
