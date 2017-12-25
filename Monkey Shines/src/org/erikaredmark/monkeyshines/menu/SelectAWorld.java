package org.erikaredmark.monkeyshines.menu;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.erikaredmark.monkeyshines.FrozenWorld;
import org.erikaredmark.monkeyshines.encoder.EncodedWorld;
import org.erikaredmark.monkeyshines.encoder.WorldIO;
import org.erikaredmark.monkeyshines.encoder.exception.WorldRestoreException;
import org.erikaredmark.monkeyshines.graphics.exception.ResourcePackException;
import org.erikaredmark.util.BinaryLocation;

/**
 * 
 * Primary panel that allows the user to select from either a builtin world or a custom one.
 * 
 * @author Erika Redmark
 *
 */
public final class SelectAWorld extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private static final String CLASS_NAME = "org.erikaredmark.monkeyshines.menu.SelectAWorld";
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
	
	private static final int BIG_BUTTON_WIDTH = 104; 
	private static final int BIG_BUTTON_HEIGHT = 79;
	
	private static final int NON_OTHER_Y = 34;
	
	private static final int SPOOKED_X = 34;
	private static final int SPACED_OUT_X = 151;
	private static final int ABOUT_THE_HOUSE_X = 268;
	private static final int IN_THE_DRINK_X = 385;
	private static final int IN_THE_SWING_X = 502;
	
	private static final int OTHER_X = 385;
	private static final int OTHER_Y = 128;
	
	private BufferedImage background;
	
	// loading of all built in world button images
	private BufferedImage spookedDown;
	private BufferedImage spookedUp;
	private BufferedImage spacedDown;
	private BufferedImage spacedUp;
	private BufferedImage aboutTheHouseDown;
	private BufferedImage aboutTheHouseUp;
	private BufferedImage inTheDrinkDown;
	private BufferedImage inTheDrinkUp;
	private BufferedImage inTheSwingDown;
	private BufferedImage inTheSwingUp;
	private BufferedImage otherUp;
	private BufferedImage otherDown;
	
	/**
	 * 
	 * Creates the select world panel, with a callback that is fired whenever the user uses the panel to select a world. It is up to the client
	 * how to position this panel and control the lifetime of the panel.
	 * 
	 * @param callback
	 * 
	 */
	public SelectAWorld(final WorldSelectionCallback callback) {
		try {
			background = ImageIO.read(SelectAWorld.class.getResourceAsStream("/resources/graphics/mainmenu/selectworld/selectWorldBackground.png") );
			spookedDown = ImageIO.read(SelectAWorld.class.getResourceAsStream("/resources/graphics/mainmenu/selectworld/SpookedDown.png") );
			spookedUp = ImageIO.read(SelectAWorld.class.getResourceAsStream("/resources/graphics/mainmenu/selectworld/SpookedUp.png") );
			spacedDown = ImageIO.read(SelectAWorld.class.getResourceAsStream("/resources/graphics/mainmenu/selectworld/SpacedOutDown.png") );
			spacedUp = ImageIO.read(SelectAWorld.class.getResourceAsStream("/resources/graphics/mainmenu/selectworld/SpacedOutUp.png") );
			aboutTheHouseDown = ImageIO.read(SelectAWorld.class.getResourceAsStream("/resources/graphics/mainmenu/selectworld/AboutTheHouseDown.png") );
			aboutTheHouseUp = ImageIO.read(SelectAWorld.class.getResourceAsStream("/resources/graphics/mainmenu/selectworld/AboutTheHouseUp.png") );
			inTheDrinkDown = ImageIO.read(SelectAWorld.class.getResourceAsStream("/resources/graphics/mainmenu/selectworld/InTheDrinkDown.png") );
			inTheDrinkUp = ImageIO.read(SelectAWorld.class.getResourceAsStream("/resources/graphics/mainmenu/selectworld/InTheDrinkUp.png") );
			inTheSwingDown = ImageIO.read(SelectAWorld.class.getResourceAsStream("/resources/graphics/mainmenu/selectworld/InTheSwingDown.png") );
			inTheSwingUp = ImageIO.read(SelectAWorld.class.getResourceAsStream("/resources/graphics/mainmenu/selectworld/InTheSwingUp.png") );
			otherDown = ImageIO.read(SelectAWorld.class.getResourceAsStream("/resources/graphics/mainmenu/selectworld/OtherDown.png") );
			otherUp = ImageIO.read(SelectAWorld.class.getResourceAsStream("/resources/graphics/mainmenu/selectworld/OtherUp.png") );
		} catch (IOException e) {
			throw new RuntimeException("Failed to load resources expected in .jar: " + e.getMessage(), e);
		}
		
		setLayout(null);
		
		JButton spookedButton = new BigWorldButton(spookedUp, spookedDown);
		spookedButton.setLocation(SPOOKED_X, NON_OTHER_Y);
		spookedButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				FrozenWorld world = loadInternalWorld(SelectAWorld.this, InternalWorld.SPOOKED);
				if (world != null) {
					callback.worldSelected(world);
				}
			}
		});
		
		add(spookedButton);
		
		JButton spacedButton = new BigWorldButton(spacedUp, spacedDown);
		spacedButton.setLocation(SPACED_OUT_X, NON_OTHER_Y);
		spacedButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				FrozenWorld world = loadInternalWorld(SelectAWorld.this, InternalWorld.SPACED_OUT);
				if (world != null) {
					callback.worldSelected(world);
				}
			}
		});
		
		add(spacedButton);
		
		JButton aboutTheHouseButton = new BigWorldButton(aboutTheHouseUp, aboutTheHouseDown);
		aboutTheHouseButton.setLocation(ABOUT_THE_HOUSE_X, NON_OTHER_Y);
		aboutTheHouseButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				FrozenWorld world = loadInternalWorld(SelectAWorld.this, InternalWorld.ABOUT_THE_HOUSE);
				if (world != null) {
					callback.worldSelected(world);
				}
			}
		});
		
		add(aboutTheHouseButton);
		
		JButton inTheDrinkButton = new BigWorldButton(inTheDrinkUp, inTheDrinkDown);
		inTheDrinkButton.setLocation(IN_THE_DRINK_X, NON_OTHER_Y);
		inTheDrinkButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				FrozenWorld world = loadInternalWorld(SelectAWorld.this, InternalWorld.IN_THE_DRINK);
				if (world != null) {
					callback.worldSelected(world);
				}
			}
		});
		
		add(inTheDrinkButton);
		
		JButton inTheSwingButton = new BigWorldButton(inTheSwingUp, inTheSwingDown);
		inTheSwingButton.setLocation(IN_THE_SWING_X, NON_OTHER_Y);
		inTheSwingButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				FrozenWorld world = loadInternalWorld(SelectAWorld.this, InternalWorld.IN_THE_SWING);
				if (world != null) {
					callback.worldSelected(world);
				}
			}
		});
		
		add(inTheSwingButton);
		
		JButton otherButton = new BigWorldButton(otherUp, otherDown);
		otherButton.setLocation(OTHER_X, OTHER_Y);
		otherButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				Optional<FrozenWorld> world = loadCustomWorld(SelectAWorld.this);
				world.ifPresent(callback::worldSelected);
			}
		});
		
		add(otherButton);
		
		setSize(640, 480);
		setMinimumSize(new Dimension(640, 480) );
		setPreferredSize(new Dimension(640, 480) );
	}
	
	/**
	 * Returns an unloaded world from the stock ones in this .jar. If this fails, the .jar is bad, 
	 * and a dialog will appear
	 * with the exception info and an exception stacktrace will be logged. Otherwise, the returned object
	 * can be used with {@code SlickMonkeyShines} to load the world.
	 */
	private static FrozenWorld loadInternalWorld(Component parent, InternalWorld chosenWorld) {
		// Can skip WorldIO and just jump to Encoded since we have a stream.
		try (InputStream is = SelectAWorld.class.getResourceAsStream(chosenWorld.internalPath);
			 InputStream rsrcIs = SelectAWorld.class.getResourceAsStream(chosenWorld.internalResourcePath) ) {
			
			EncodedWorld world = EncodedWorld.fromStream(is);
			// Bit of a hack, since right now a valid File object is needed to use the entire resource loading
			// code which uses the ZipFile class. Extract .jar'ed zip into temporary filesystem.
			Path tempRsrcDir = Files.createTempDirectory("monkeyshines_temp_resources");
			Path tempRsrc = tempRsrcDir.resolve("rsrc.zip");
			Files.copy(rsrcIs, tempRsrc);
			
			// TODO use Slick based graphics. However, for time being, AWT is standin
			// until Slick infrastructure is ready.
//			WorldResource rsrc = PackReader.fromPackAwt(tempRsrc);
			
			// Clean up temporary files
			
			return new FrozenWorld(world, tempRsrc, true);
		} catch (Exception e) {
			LOGGER.severe(CLASS_NAME + ": Missing world " + chosenWorld.internalPath + " from .jar file. Possible .jar corruption.");
			handleWorldLoadException(parent, e);
		}
		
		return null;
	}

	/**
	 * Returns both the encoded world and a path to the resource pack (if available)
	 * after a selection.
	 * <p/>
	 * The world construction must be deferred until the init method of {@code SlickMonkeyShines}.
	 * The result of this is typically passed there.
	 * 
	 * @param parent
	 * 		the parent component for displaying the dialog on
	 * 
	 * @return
	 * 		the selected world, or {@code null} if no world was selected.
	 * 
	 */
	public static Optional<FrozenWorld> loadCustomWorld(final Component parent) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(BinaryLocation.BINARY_LOCATION.toFile() );
		System.out.println(fileChooser.getCurrentDirectory() );
		
		if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			Path worldFile = fileChooser.getSelectedFile().toPath();
			try {
				EncodedWorld world = WorldIO.restoreWorld(worldFile);
				// Try to load the resource pack
				String fileName = worldFile.getFileName().toString();
				// Remove .world extension so we can substitute with .zip.
				String worldName = fileName.substring(0, fileName.lastIndexOf('.') );
				Path packFile = worldFile.getParent().resolve(worldName + ".zip");
				return Optional.of(new FrozenWorld(world, packFile, false));
			} catch (Exception e) {
				// See method. Instances in if/else are the exception we expect to catch.
				// This technically catches everything but I see no reason why any exceptions
				// need propogate any further since handling means logging anyway.
				handleWorldLoadException(parent, e);
			}
		}
		
		// no world chosen if method hasn't returned yet
		return Optional.empty();
	}

	private static void handleWorldLoadException(final Component parent, Exception ex) {
		if (ex instanceof WorldRestoreException) {
			JOptionPane.showMessageDialog(parent,
			    "Cannot load world: Possibly corrupt or not a world file: " + ex.getMessage(),
			    "Loading Error",
			    JOptionPane.ERROR_MESSAGE);
			LOGGER.log(Level.WARNING, 
					   CLASS_NAME + ": Cannot load world: Possibly corrupt or not a world file: " + ex.getMessage(), 
					   ex);
		} else if (ex instanceof ResourcePackException) {
			JOptionPane.showMessageDialog(parent,
			    "Resource pack issues: " + ex.getMessage(),
			    "Loading Error",
			    JOptionPane.ERROR_MESSAGE);
			LOGGER.log(Level.WARNING, 
					   CLASS_NAME + ": Cannot load world: Possibly corrupt or not a world file: " + ex.getMessage(), 
					   ex);
		} else if (ex instanceof IOException) {
			JOptionPane.showMessageDialog(parent,
			    "Low level I/O error: " + ex.getMessage(),
			    "Loading Error",
			    JOptionPane.ERROR_MESSAGE);
			LOGGER.log(Level.WARNING, 
					   CLASS_NAME + ": " + ex.getMessage(), 
					   ex);
		} else {
			JOptionPane.showMessageDialog(parent,
			    "Unknown Error loading world " + ex.getMessage(),
			    "Loading Error",
			    JOptionPane.ERROR_MESSAGE);
			LOGGER.log(Level.WARNING, 
					   CLASS_NAME + ": " + ex.getMessage(), 
					   ex);
		}
	}
	
	/**
	 * 
	 * Paint the standard button components for clicking, but everything else can easily just be painted on since they
	 * aren't interactive.
	 * 
	 */
	@Override public void paintComponent(Graphics g) {
		g.drawImage(background, 0, 0, null);

		// Do not call; destroys background. Components are still painted regardless.
		// super.paintComponent(g);
	}
	
	/**
	 * 
	 * The big buttons that appear allowing the user to select a world. All buttons are the same size and 
	 * contain an image that will be shrunk and applied a special bevel effect. Some will load up a specific
	 * world and the 'Other' button will allow a JChooser to pick a custom level to load.
	 * <p/>
	 * All buttons are supplied a special callback object that, when clicked and finished processing the click,
	 * may call with a valid World object assuming they were able to get a hold of one.
	 * 
	 */
	private static final class BigWorldButton extends JButton {
		private static final long serialVersionUID = 1L;

		/**
		 * 
		 * Constructs the button with the appropriate pressed and unpressed images.
		 * 
		 * @param up
		 * 		image shown when button is not clicked
		 * 
		 * @param down
		 * 		image shown when button is clicked
		 * 
		 */
		public BigWorldButton(BufferedImage up, BufferedImage down) {
			super();
			setIcon(new ImageIcon(up) );
			setSize(BIG_BUTTON_WIDTH, BIG_BUTTON_HEIGHT);
			setPressedIcon(new ImageIcon(down) );
			//addActionListener(listener);
			MenuUtils.renderImageOnly(this);
		}
	}
	
	private enum InternalWorld {
		SPOOKED("/resources/worlds/Spooked/Spooked.world", "/resources/worlds/Spooked/Spooked.zip"),
		SPACED_OUT("/resources/worlds/SpacedOut/Spaced Out.world", "/resources/worlds/SpacedOut/Spaced Out.zip"),
		ABOUT_THE_HOUSE("/resources/worlds/AboutTheHouse/About The House.world", "/resources/worlds/AboutTheHouse/About The House.zip"),
		IN_THE_DRINK("/resources/worlds/InTheDrink/In The Drink.world", "/resources/worlds/InTheDrink/In The Drink.zip"),
		IN_THE_SWING("/resources/worlds/In The Swing/In The Swing.world", "/resources/worlds/In The Swing/In The Swing.zip");
		
		public final String internalPath;
		public final String internalResourcePath;
		
		private InternalWorld(final String path, final String resourcePath) {
			internalPath = path;
			internalResourcePath = resourcePath;
		}
	}
	
	/**
	 * 
	 * Effectively a runnable that is called with an {@code UnloadedWorld} reference. Used to communicate back to the client
	 * creating this panel the selected world. Typically this being called, the client will close or otherwise
	 * dispose of the panel.
	 * 
	 * @author Erika Redmark
	 *
	 */
	public interface WorldSelectionCallback {
		void worldSelected(FrozenWorld world);
	}
	
}
