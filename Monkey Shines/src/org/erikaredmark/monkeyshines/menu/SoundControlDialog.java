package org.erikaredmark.monkeyshines.menu;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.erikaredmark.monkeyshines.global.SoundType;
import org.erikaredmark.monkeyshines.global.SoundUtils;

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
	
	// Whenever the sound levels are changed, the sound demo is played at the appropriate
	// gain to demonstrate to the player an idea of how loud or soft they just made things
	private final Clip SOUND_DEMO;
	
	// Icons that flank the main slider will show either the music of general sound
	// icon to indicate low to high sound.
	private static final int LEFT_SIDE_DRAW_X = 40;
	private static final int LEFT_SIDE_DRAW_Y = 47;
	private static final int LEFT_SIDE_DRAW_X2 = 78;
	private static final int LEFT_SIDE_DRAW_Y2 = 84;
	
	private static final int RIGHT_SIDE_DRAW_X = 253;
	private static final int RIGHT_SIDE_DRAW_Y = 47;
	private static final int RIGHT_SIDE_DRAW_X2 = 291;
	private static final int RIGHT_SIDE_DRAW_Y2 = 84;

	// Sliders controls location
	private static final int SLIDER_DRAW_X = 78;
	private static final int SLIDER_DRAW_Y = 52;
	private static final int SLIDER_WIDTH = 175;
	private static final int SLIDER_HEIGHT = 29;
	
	// Button location
	private static final int OKAY_DRAW_X = 133;
	private static final int OKAY_DRAW_Y = 118;
	
	private static final int DIALOG_SIZE_X = 328;
	private static final int DIALOG_SIZE_Y = 180;
	
	// Outside of contructor so as not to clutter visual code: Simply sets the values of some constants, such as images.
	// These are NOT static, otherwise image data would persist in memory when not needed.
	{
		try {
			// Graphics
			SOUND_LEFT_SIDE = ImageIO.read(SoundControlDialog.class.getResourceAsStream("/resources/graphics/mainmenu/sound/soundLeftSide.png") );
		    SOUND_RIGHT_SIDE = ImageIO.read(SoundControlDialog.class.getResourceAsStream("/resources/graphics/mainmenu/sound/soundRightSide.png") );
			MUSIC_LEFT_SIDE = ImageIO.read(SoundControlDialog.class.getResourceAsStream("/resources/graphics/mainmenu/sound/musicLeftSide.png") );
			MUSIC_RIGHT_SIDE = ImageIO.read(SoundControlDialog.class.getResourceAsStream("/resources/graphics/mainmenu/sound/musicRightSide.png") );
			OKAY_ICON = new ImageIcon(ImageIO.read(SoundControlDialog.class.getResourceAsStream("/resources/graphics/mainmenu/sound/btnOK1.png") ) );
			OKAY_PUSHED_ICON = new ImageIcon(ImageIO.read(SoundControlDialog.class.getResourceAsStream("/resources/graphics/mainmenu/sound/btnOK2.png") ) );
			BACKGROUND = ImageIO.read(SoundControlDialog.class.getResourceAsStream("/resources/graphics/mainmenu/sound/soundMain.png") );
			
			// Sound
			SOUND_DEMO = SoundUtils.clipFromOggStream(SoundControlDialog.class.getResourceAsStream("/resources/sounds/mainmenu/soundDemo.ogg"), "soundDemo.ogg");
		} catch (IOException e) {
			throw new RuntimeException("Bad .jar, could not find graphics resources for sound system: " + e.getMessage(), e);
		} catch (UnsupportedAudioFileException e) {
			throw new RuntimeException("Bad .jar, demo sound not in .ogg format: " + e.getMessage(), e);
		} catch (LineUnavailableException e) {
			throw new RuntimeException("No sound device available: " + e.getMessage(), e);
		}
	}
	
	private SoundControlDialog(final SoundType type) {
		// add a JPanel containing all the components. We can custom paint the JPanel with
		// added components easier than we can with a dialog class.
		JPanel mainPanel = new JPanel() {
			private static final long serialVersionUID = 1L;
			// Direct painting is responsible for background and left/right sound images
			@Override public void paintComponent(Graphics g) {
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
				
				switch(type) {
				case MUSIC:
					leftSide = MUSIC_LEFT_SIDE;
					rightSide = MUSIC_RIGHT_SIDE;
					break;
				case SOUND:
					leftSide = SOUND_LEFT_SIDE;
					rightSide = SOUND_RIGHT_SIDE;
					break;
				default:
					throw new RuntimeException("Unknown soundtype " + type);
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
		};
		mainPanel.setLocation(0, 0);
		mainPanel.setSize(DIALOG_SIZE_X, DIALOG_SIZE_Y);
		mainPanel.setPreferredSize(new Dimension(DIALOG_SIZE_X, DIALOG_SIZE_Y) );
		mainPanel.setMinimumSize(new Dimension(DIALOG_SIZE_X, DIALOG_SIZE_Y) );
		mainPanel.setLayout(null);
		add(mainPanel);
		
		// Only the slider and button are components. Everything else is painted in the paint method.
		
		VolumeSlider volumeSlider = new VolumeSlider(type, SOUND_DEMO);
		volumeSlider.setLocation(SLIDER_DRAW_X, SLIDER_DRAW_Y);
		volumeSlider.setSize(SLIDER_WIDTH, SLIDER_HEIGHT);
		mainPanel.add(volumeSlider);
		
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
		
		
		
		mainPanel.add(okayButton);
	}
	
	/**
	 * 
	 * Launches the sound control dialog for the user to modify the sound. This dialog is modal, so
	 * control will not return from this method until the user closes the dialog.
	 * 
	 */
	public static void launch(final SoundType sound) {
		SoundControlDialog dialog = new SoundControlDialog(sound);
		dialog.setUndecorated(true);
		dialog.setModal(true);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}
}
