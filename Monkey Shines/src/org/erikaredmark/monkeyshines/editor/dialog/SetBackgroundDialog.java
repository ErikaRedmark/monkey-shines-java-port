package org.erikaredmark.monkeyshines.editor.dialog;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
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
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.erikaredmark.monkeyshines.background.Background;
import org.erikaredmark.monkeyshines.background.FullBackground;
import org.erikaredmark.monkeyshines.background.SingleColorBackground;
import org.erikaredmark.monkeyshines.editor.resource.EditorResource;
import org.erikaredmark.monkeyshines.resource.WorldResource;
import org.erikaredmark.util.swing.layout.WrapLayout;

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
		
		final SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		/* --------------------- Full background Panel  --------------------- */
		JPanel fullBackgroundPanel = new JPanel();
		createThumbnailSpan(fullBackgroundPanel, rsrc, true);
		JScrollPane fullBackgroundScrollPane = new JScrollPane(fullBackgroundPanel);
		fullBackgroundScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		/* ------------------------- Pattern Panel  ------------------------- */
		JPanel patternPanel = new JPanel();
		createThumbnailSpan(patternPanel, rsrc, false);
		JScrollPane patternScrollPane = new JScrollPane(patternPanel);
		patternScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		/* ----------------------------- Colour  ---------------------------- */
		// If the background is initially a color background, use that as first color. Otherwise default black
		Color initialColor =   currentBackground instanceof SingleColorBackground
						     ? ((SingleColorBackground)currentBackground).getColor()
						     : Color.BLACK;
		final JColorChooser colorChooser = new JColorChooser(initialColor);
		colorChooser.getSelectionModel().addChangeListener(new ChangeListener() {
			@Override public void stateChanged(ChangeEvent event) {
				Color newColor = colorChooser.getColor();
				setSelectedBackground(new SingleColorBackground(newColor) );
			}
		});
		
		JScrollPane singleColorPane = new JScrollPane(colorChooser);
		
		JTabbedPane differentBackgroundsPane = new JTabbedPane();
		differentBackgroundsPane.addTab("Full", fullBackgroundPanel);
		differentBackgroundsPane.addTab("Pattern", patternPanel);
		differentBackgroundsPane.addTab("Solid", singleColorPane);
		differentBackgroundsPane.setPreferredSize(new Dimension(700, 420) );
		

		
		/* ---------------------------- Viewer ------------------------------ */
		backgroundViewer = new Canvas() {
			private static final long serialVersionUID = 1L;

			@Override public void paint(Graphics g) {
				if (selectedBackground instanceof FullBackground) {
					BufferedImage thumbnail = EditorResource.generateThumbnailForBackground((FullBackground)selectedBackground);
					Graphics2D g2d = (Graphics2D) g;
					
					g2d.drawImage(thumbnail, 0, 0, 160, 100, 0, 0, 160, 100, null);
				} else {
					// Solid colour; technically this only draws a portion of it, but that doesn't matter.
					selectedBackground.draw((Graphics2D)g);
				}
			}
		};
		backgroundViewer.setSize(160, 100);

		
		/* ------------------------ Okay and Cancel -------------------------- */
		JButton okay = new JButton(new AbstractAction("Okay") {
			private static final long serialVersionUID = 1L;
			@Override public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});


		
		JButton cancel = new JButton(new AbstractAction("Cancel") {
			private static final long serialVersionUID = 1L;
			@Override public void actionPerformed(ActionEvent e) {
				// reset selected to original
				selectedBackground = currentBackground;
				setVisible(false);
			}
		});
		
		
		/* --------------- Layout Components -------------- */
		add(differentBackgroundsPane);
		springLayout.putConstraint(SpringLayout.WEST, backgroundViewer, 0, SpringLayout.EAST, this);
		springLayout.putConstraint(SpringLayout.NORTH, backgroundViewer, 0, SpringLayout.SOUTH, differentBackgroundsPane);
		add(backgroundViewer, BorderLayout.EAST);
		springLayout.putConstraint(SpringLayout.NORTH, okay, 2, SpringLayout.SOUTH, backgroundViewer);
		add(okay, BorderLayout.SOUTH);
		springLayout.putConstraint(SpringLayout.WEST, cancel, 4, SpringLayout.EAST, okay);
		springLayout.putConstraint(SpringLayout.NORTH, cancel, 0, SpringLayout.NORTH, okay);
		add(cancel, BorderLayout.SOUTH);
		
		setSize(710, 600);
	}
	
	/**
	 * 
	 * Adds to the given panel buttons with thumbnails all generated from the given 
	 * list of backgrounds, along with having each one set the background to
	 * the chosen one.
	 * 
	 * @param fullOrPattern
	 * 		{@code true} to create a span for full backgrounds, {@code false} for patterns
	 * 
	 */
	private void createThumbnailSpan(JPanel panel, WorldResource rsrc, boolean fullOrPattern) {
		panel.setLayout(new WrapLayout(FlowLayout.LEFT, 0, 0) );
		int count =   fullOrPattern
					? rsrc.getBackgroundCount()
				    : rsrc.getPatternCount();
					
		for (int i = 0; i < count; i++) {
			final Background next =   fullOrPattern 
									? rsrc.getBackground(i)
									: rsrc.getPattern(i);
									
			BufferedImage image = EditorResource.generateThumbnailForBackground((FullBackground)next);
			JButton button = new JButton(new ImageIcon(image) );
			button.setMargin(new Insets(2, 2, 2, 2) );
			button.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					setSelectedBackground(next);
				}
			});
			panel.add(button);
		}
		
		// Height will be dynamically calculated
		panel.setSize(700, 1);

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
		dialog.setModal(true);
		dialog.setVisible(true);
		
		return dialog.selectedBackground;
	}
	
}
