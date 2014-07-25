package org.erikaredmark.monkeyshines.menu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;

import org.erikaredmark.monkeyshines.global.SoundSettings;

/**
 * 
 * Primary dialog for modifying the sound levels of both sound and music (which once is based on
 * the passed launching parameter). As with other game dialogs, this uses an absolute layout to
 * best recreate the original game.
 * 
 * @author Erika Redmark
 *
 */
public final class SoundControlDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private final BufferedImage SOUND_LEFT_SIDE;
	private final BufferedImage SOUND_RIGHT_SIDE;
	private final BufferedImage MUSIC_LEFT_SIDE;
	private final BufferedImage MUSIC_RIGHT_SIDE;
	
	private final ImageIcon OKAY_ICON;
	private final ImageIcon OKAY_PUSHED_ICON;
	
	private final BufferedImage BACKGROUND;
	
	// Icons that flank the main slider will show either the music of general sound
	// icon to indicate low to high sound.
	private static final int LEFT_SIDE_DRAW_X = 39;
	private static final int LEFT_SIDE_DRAW_Y = 46;
	private static final int LEFT_SIDE_DRAW_X2 = 77;
	private static final int LEFT_SIDE_DRAW_Y2 = 83;
	
	private static final int RIGHT_SIDE_DRAW_X = 252;
	private static final int RIGHT_SIDE_DRAW_Y = 46;
	private static final int RIGHT_SIDE_DRAW_X2 = 290;
	private static final int RIGHT_SIDE_DRAW_Y2 = 83;

	// Sliders controls location
	private static final int SLIDER_DRAW_X = 85;
	private static final int SLIDER_DRAW_Y = 58;
	
	// Button location
	private static final int OKAY_DRAW_X = 134;
	private static final int OKAY_DRAW_Y = 120;
	
	// Outside of contructor so as not to clutter visual code: Simply sets the values of some constants, such as images.
	// These are NOT static, otherwise image data would persist in memory when not needed.
	{
		try {
			SOUND_LEFT_SIDE = ImageIO.read(SoundControlDialog.class.getResourceAsStream("/resources/graphics/mainmenu/sound/soundLeftSide.png") );
		    SOUND_RIGHT_SIDE = ImageIO.read(SoundControlDialog.class.getResourceAsStream("/resources/graphics/mainmenu/sound/soundRightSide.png") );
			MUSIC_LEFT_SIDE = ImageIO.read(SoundControlDialog.class.getResourceAsStream("/resources/graphics/mainmenu/sound/musicLeftSide.png") );
			MUSIC_RIGHT_SIDE = ImageIO.read(SoundControlDialog.class.getResourceAsStream("/resources/graphics/mainmenu/sound/musicRightSide.png") );
			OKAY_ICON = new ImageIcon(ImageIO.read(SoundControlDialog.class.getResourceAsStream("/resources/graphics/mainmenu/sound/btnOK1.png") ) );
			OKAY_PUSHED_ICON = new ImageIcon(ImageIO.read(SoundControlDialog.class.getResourceAsStream("/resources/graphics/mainmenu/sound/btnOK2.png") ) );
			BACKGROUND = ImageIO.read(SoundControlDialog.class.getResourceAsStream("/resources/graphics/mainmenu/sound/soundMain.png") );
		} catch (IOException e) {
			throw new RuntimeException("Bad .jar, could not find graphics resources for sound system: " + e.getMessage(), e);
		}
	}
	

	private final SoundType soundType;
	
	private SoundControlDialog(final SoundType type) {
		this.soundType = type;
		setLayout(null);
		
		// Display the name (either music or sound) and the slider that will delegate
		// value adjustments to changing the volume for the appropriate thing
		// Only the slider and button are components. Everything else is painted in the paint method.
		
		//VolumeSlider volumeSlider = new VolumeSlider(type);
		
		//add(volumeSlider);
		
		JButton okayButton = new JButton(new AbstractAction("", OKAY_ICON) {
			private static final long serialVersionUID = 1L;
			@Override public void actionPerformed(ActionEvent arg0) {
				// close window. All changes from slider have already propogated.
				setVisible(false);
			}
		});
		okayButton.setLocation(OKAY_DRAW_X, OKAY_DRAW_Y);
		okayButton.setSize(OKAY_ICON.getIconWidth(), OKAY_ICON.getIconHeight() );
		okayButton.setPressedIcon(OKAY_PUSHED_ICON);
		MenuUtils.renderImageOnly(okayButton);
		
		
		
		add(okayButton);
	}
	
	// Direct painting is responsible for background and left/right sound images
	@Override public void paint(Graphics g) {
		super.paint(g);
		// We now paint over the components. Proper transparency and location painting will ensure
		// that components added to the contain pane normally are not painted over.
		
		g.drawImage(BACKGROUND, 
					0, 0, 
					BACKGROUND.getWidth(), BACKGROUND.getHeight(), 
					0, 0, 
					BACKGROUND.getWidth(), BACKGROUND.getHeight(), 
					null);
		
		// Switch statement over polymorphism, as this class is responsible for maintaining the
		// graphics resources, not the static enum.
		BufferedImage leftSide = null;
		BufferedImage rightSide = null;
		
		switch(this.soundType) {
		case MUSIC:
			leftSide = MUSIC_LEFT_SIDE;
			rightSide = MUSIC_RIGHT_SIDE;
			break;
		case SOUND:
			leftSide = SOUND_LEFT_SIDE;
			rightSide = SOUND_RIGHT_SIDE;
			break;
		default:
			throw new RuntimeException("Unknown soundtype " + this.soundType);
		}
		
		assert leftSide != null;
		assert rightSide != null;
		
		g.drawImage(leftSide, 
					LEFT_SIDE_DRAW_X, LEFT_SIDE_DRAW_Y, 
					LEFT_SIDE_DRAW_X2, LEFT_SIDE_DRAW_Y2, 
					0, 0, 
					leftSide.getWidth(), leftSide.getHeight(), 
					null);
		
		g.drawImage(rightSide, 
					RIGHT_SIDE_DRAW_X, RIGHT_SIDE_DRAW_Y, 
					RIGHT_SIDE_DRAW_X2, RIGHT_SIDE_DRAW_Y2, 
					0, 0, 
					rightSide.getWidth(), rightSide.getHeight(), 
					null);
	}
	
	/**
	 * 
	 * Launches the sound control dialog for the user to modify the sound. This dialog is modal, so
	 * control will not return from this method until the user closes the dialog.
	 * 
	 */
	public static void launch(final SoundType sound) {
		SoundControlDialog dialog = new SoundControlDialog(sound);
		dialog.setSize(328, 180);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}

	
	public enum SoundType {

		
		SOUND() {
			@Override public void adjustPercentage(int value) {
				SoundSettings.setSoundVolumePercent(value);
			}
		},
		MUSIC() {
			@Override public void adjustPercentage(int value) {
				SoundSettings.setMusicVolumePercent(value);
			}
		};
		
		/**
		 * 
		 * Called when slider updates the 'sound'. Depending on the sound type, the appropriate 
		 * thing (sound or music) will be adjusted.
		 * 
		 * @param value
		 * 		percentage from 0 - 100 of how loud the sound should be,
		 * 
		 */
		public abstract void adjustPercentage(int value);
	}
	
}
