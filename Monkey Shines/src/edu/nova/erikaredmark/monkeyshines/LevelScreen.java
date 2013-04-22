package edu.nova.erikaredmark.monkeyshines;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.base.Optional;

import edu.nova.erikaredmark.monkeyshines.Tile.TileType;
import edu.nova.erikaredmark.monkeyshines.encoder.Tile;

/**
 * Improving the screen-by-screen architecture is NOT something I will be doing.
 * This provides the classic Monkey Shines feel: Each screen is one part of a level. Going off to the side of a screen
 * sends you to the next screen
 * <p/>
 * The initial screen is screen 1000. The Bonus screen can be determined by a constant (To allow more flexibility; the 
 * original used 10000.)
 * <p/>
 * This contains a lot of elements similar to World, but World is what contains the objects. These are only pointers.
 * The large number of pointers allow the LevelScreen to draw itself and handle it's own collisions.
 * <p/>
 * Changes to a level screen are not persisted to a level. The level editor must use {@link LevelScreenEditor} objects
 * that wrap level screens and store a list of changes to them to write out level data. This object, by itself, only
 * represents the level as it is in one-instance of the game.
 * 
 */
public class LevelScreen {
	private final int screenID;
	private final Tile screenTiles[][]; // 32 width by 20 height
	
	// These are all pointers to what is stored in world.
	private final BufferedImage solidTiles;
	private final BufferedImage thruTiles;
	private final BufferedImage sceneTiles;
	
	// A loaded image
	private       BufferedImage background;
	
	// Sprites from world
	private final BufferedImage spritePointer[];
	private       Sprite spritesOnScreen[];
	//boolean justOnce; // every time a screen is reloaded,
	
	// Bonzo's starting location
	private       Point2D bonzoStart;
	// Bonzo's starting location when entered from another screen. Set by the World, set to null when bonzo leaves.
	private       Point2D bonzoCameFrom;
	
	/**
	 * Constructs a new level screen, using the supplied screenName and ID. Only the {@link World} should construct these
	 * objects, as it has the required graphics pointers the level screen needs to know how to draw itself
	 * <p/>
	 * TODO hardcoded to load from XML. Needs redesign.
	 * 
	 * @param screenID
	 * @param screenName
	 * @param spritePointer
	 * @param solidTiles
	 * @param thruTiles
	 * @param sceneTiles
	 * 
	 */
	public LevelScreen(final int screenID, 
					   final String screenName, 
					   final BufferedImage[] spritePointer, 
					   final World		   worldPointer) {
		
		this.spritePointer = spritePointer;
		this.screenID = screenID;
		screenTiles = new Tile[20][32]; // 20 rows, 32 cols
		// DEBUG: initialise solidTiles TODO
		
		this.solidTiles = worldPointer.getTileSheetSolid();
		this.thruTiles = worldPointer.getTileSheetThru();
		this.sceneTiles = worldPointer.getTileSheetScene();
		
		parseXmlScreen("" + screenName + screenID + ".xml", worldPointer);
		
	}
	
	/** Returns the screen id of this screen																			*/
	public int getId() { return this.screenID; }
	
	private void parseXmlScreen(String name, World worldPointer){
		//get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document dom;
		try {

			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
			InputStream xmlFile = getClass().getResourceAsStream("/resources/worlds/" 
					+ worldPointer.getWorldName() + "/" + name);
			dom = db.parse(xmlFile);
		
			Element docEle = dom.getDocumentElement();

			// Put tiles
			NodeList nl = docEle.getElementsByTagName("tile");
			if(nl != null && nl.getLength() > 0) {
				for(int i = 0 ; i < nl.getLength();i++) {

					//get the employee element
					Element el = (Element)nl.item(i);

					// Send the tile element to addTile for processing.
					addTile(el);
				}
			}
			
			// Bonzo
			nl = docEle.getElementsByTagName("bonzo");
			if (nl != null && nl.getLength() > 0) {
				Element bonzoEl = (Element)nl.item(0);
				
				bonzoStart = Point2D.of(GameConstants.getIntValue(bonzoEl, "x"),
						                GameConstants.getIntValue(bonzoEl, "y" ) );
				
				/* Bonzo's initial entry into this current screen will be from the starting point.						*/
				if (bonzoCameFrom != null) throw new IllegalStateException("Screen can not have 'cameFrom' data before bonzo's starting location is loaded");
				bonzoCameFrom = Point2D.of(bonzoStart);
			}
			
			// Sprites
			nl = docEle.getElementsByTagName("sprite");
			if (nl != null && nl.getLength() > 0) {
				spritesOnScreen = new Sprite[nl.getLength() ];
				for (int i = 0; i < nl.getLength(); i++) {
					Element spriteEl = (Element)nl.item(i);
				
					ClippingRectangle boundingBox = ClippingRectangle.of(
							GameConstants.getIntValue(spriteEl, "width"),
							GameConstants.getIntValue(spriteEl, "height"), 
							GameConstants.getIntValue(spriteEl, "bound1x") ,
							GameConstants.getIntValue(spriteEl, "bound1y") );
				
					Point2D spriteLocation = Point2D.of(
							GameConstants.getIntValue(spriteEl, "startx"),
							GameConstants.getIntValue(spriteEl, "starty") );
				
					int speedx = GameConstants.getIntValue(spriteEl, "speedx");
					int speedy = GameConstants.getIntValue(spriteEl, "speedy");
					
					// Tells which sprite sheet to use.
					int id = GameConstants.getIntValue(spriteEl, "id");
					
					spritesOnScreen[i] = new Sprite(spritePointer[id], spriteLocation, speedx, speedy, boundingBox);
				}
				
				//public Sprite(final BufferedImage spriteSheet, final Point2D currentLocation, 
				//final int speedX, final int speedY, final ClippingRectangle boundingBox)
				/*<uniqueid>0</uniqueid>
				<id>2</id>
				<type>0</type>*/
			}
			
			// Background
			
			nl = docEle.getElementsByTagName("screeninfo");
			if (nl != null && nl.getLength() > 0) {
				Element backgroundEl = (Element)nl.item(0);
				
				try {
					InputStream temp = getClass().getResourceAsStream("/resources/graphics/" 
							+ worldPointer.getWorldName() + "/" 
							+ GameConstants.getTextValue(backgroundEl, "background") );
				    background = ImageIO.read(temp);
				} catch (IOException e) {
					System.out.println("Quand est la affiche?");
				}
			}

			

		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
		
		// Now that we have got a document, let's extract the elements and create all the tiles and sprites where they need be.
		
		
	}
	
	public void addTile(Element tileEl) {

		//for each <tile> value, get the locationX, locationY, and the tile type
		int tileLocX = GameConstants.getIntValue(tileEl, "locationX");
		int tileLocY = GameConstants.getIntValue(tileEl, "locationY");
		int tileid = GameConstants.getIntValue(tileEl, "tileid");
		int tiletype = GameConstants.getIntValue(tileEl, "tiletype");

		// add the tile
		BufferedImage sheetPointer = getCorrectSheet(TileType.fromId(tiletype) );
		screenTiles[tileLocY][tileLocX] = new Tile(sheetPointer, tileLocY, tileLocX, tileid, TileType.fromId(tiletype) );
	}
	
	// Helper method. Takes a tile type and returns what it's sheet should be
	private BufferedImage getCorrectSheet(final TileType tileType) {
		BufferedImage sheetPointer;
		switch(tileType) {
		case SOLID:
			sheetPointer = solidTiles;
			break;
		case THRU:
			sheetPointer = thruTiles;
			break;
		case SCENE:
			sheetPointer = sceneTiles;
			break;
		default:
			throw new RuntimeException("Bad enum type: " + tileType.toString() );
		}
		return sheetPointer;
	}
	
	/**
	 * 
	 * Gets the starting position of Bonzo in the level. The returned point may be freely modified and assigned as-is to any
	 * object.
	 * 
	 * @return
	 * 		a new instance of a point that represents the starting location of bonzo in the level, never {@code null}
	 */
	public Point2D newPointFromBonzoStart() {
		return Point2D.of(bonzoStart);
	}
	
	/**
	 * 
	 * Returns a new point representing a location on the screen that bonzo entered from. The returned point may be freely
	 * modified and assigned as-is to any object.
	 * 
	 * @return
	 * 		a new instance of a point that represents the location bonzo entered this screen. This initially is set to his
	 * 		starting location on the screen but is changed as he moves through the screens. Value never {@code null}
	 * 
	 */
	public Point2D newPointFromWhereBonzoCame() {
		return Point2D.of(bonzoCameFrom);
	}
	
	/**
	 * Called when Bonzo enters the screen from another Screen. Sets the location he came from so if he dies on this screen, 
	 * he can return to that position. A copy of the passed point is made, so that object may be re-used.
	 * 
	 * @param bonzoCameFrom 
	 * 		the location Bonzo entered the screen from
	 */
	public void setBonzoCameFrom(final Point2D bonzoCameFrom) {
		this.bonzoCameFrom = Point2D.of(bonzoCameFrom);
	}
	
	/**
	 * If, for some reason, the location Bonzo Came from becomes invalid, this resets it.
	 */
	public void resetBonzoCameFrom() {
		this.bonzoCameFrom = Point2D.of(bonzoStart);
	}
	
	// Sprites
	/**
	 * Sprites move around the screen. This makes them return to where they spawn when the screen is first entered. 
	 * Typically called alongside resetBonzo when Bonzo dies.
	 */
	public void resetSprites() {
		if (spritesOnScreen != null) {
			for (Sprite nextSprite : spritesOnScreen) {
				nextSprite.resetSpritePosition();
			}
		}
	}

	
	// TODO Refactor these functions into one. They do basically the same thing.
	// The actual location on screen for bonzoX and Y, NOT the tile position
	/**
	 * 
	 * checks
	 * 
	 * @param bonzoX
	 * @param bonzoY
	 * @return
	 */
	public boolean checkForTile(int bonzoX, int bonzoY) {
		int tileX = bonzoX / GameConstants.TILE_SIZE_X;
		int tileY = bonzoY / GameConstants.TILE_SIZE_Y;
		// If out of bounds, allow to slip by
		if (tileX < 0 || tileX >= GameConstants.TILES_IN_ROW || tileY < 0 || tileY >= GameConstants.TILES_IN_COL)
			return false;
		// if there is no tile, well, false
		if (screenTiles[tileY][tileX] == null) {
			//System.out.println("No Tile");
			return false;
		}
		if (screenTiles[tileY][tileX].getType() == TileType.SOLID )
			return true;
		return false;
	}
	
	// Careful! This is return by reference
	public Sprite[] getSpritesOnScreen() {
		return spritesOnScreen;
	}
	
	/**
	 * Returns either nothing if there is no tile (or a non-solid tile). Otherwise, returns the TileType of the solid tile
	 * 
	 * @param bonzoX
	 * 		position of bonzo to look for tile.
	 * 
	 * @param bonzoY
	 *
	 * @return
	 * 		a tile type if the tile exists AND is solid, or nothing if there is no solid tile as ground
	 */
	public Optional<TileType> checkForGroundTile(int bonzoX, int bonzoY) {
		int tileX = bonzoX / GameConstants.TILE_SIZE_X;
		int tileY = bonzoY / GameConstants.TILE_SIZE_Y;
		// If out of bounds, allow to slip by
		if (tileX < 0 || tileX >= GameConstants.TILES_IN_ROW || tileY < 0 || tileY >= GameConstants.TILES_IN_COL)
			return Optional.absent();
		// if there is no tile, well, false
		if (screenTiles[tileY][tileX] == null) {
			return Optional.absent();
		}
		
		TileType type = screenTiles[tileY][tileX].getType();
		
		if (type == TileType.SOLID || type == TileType.THRU) return Optional.of(type);
		else return Optional.absent();
	}
	
	/*
	 * Editor functions. None of these effects are saved until the level is saved.
	 * Save often!
	 * @param g2d
	 *
	 */
	
	/**
	 * Sets the tile at tileX, tileY, to the indicated tile.
	 * @param tileX x location, terms of grid, not pixels.
	 * @param tileY y location, terms of grid, not pixels.
	 * @param tileType Whether this is a Solid, Thru, or scenery tile.
	 * @param tileId  the ID of the tile, which is basically the graphic to use when rendering.
	 */
	public void setTile(int tileX, int tileY, TileType tileType, int tileId) {
		screenTiles[tileY][tileX] = null;
		BufferedImage sheetPointer = getCorrectSheet(tileType);
		screenTiles[tileY][tileX] = new Tile(sheetPointer, tileY, tileX, tileId, tileType );
	}
	
	/**
	 * Removes the tile at position tileX, tileY
	 * @param tileX x location, terms of grid, not pixels.
	 * @param tileY y location, terms of grid, not pixels.
	 */
	
	public void eraseTile(int tileX, int tileY) {
		screenTiles[tileY][tileX] =  null;
	}
	
	/**
	 * Draw background, tiles, and sprites in one swoop.
	 * TODO draws entire screen. May require a more intelligent algorithm to run on slower
	 * machines
	 * @param g2d
	 */
	public void paint(Graphics2D g2d) {
		if (background != null)
			g2d.drawImage(background, 0, 0, null);
		for (int i = 0; i < GameConstants.TILES_IN_COL; i++) { // for every tile in the row
			for (int j = 0; j < GameConstants.TILES_IN_ROW; j++) {
				if (screenTiles[i][j] != null)
					screenTiles[i][j].paint(g2d);
			}
		}
		if (spritesOnScreen != null) {
			for (int i = 0; i < spritesOnScreen.length; i++) {
				spritesOnScreen[i].update();
				spritesOnScreen[i].paint(g2d);
			}
		}
	}

	/**
	 * <strong> NOT PUBLIC API</strong>
	 * This method returns the backing 2 dimensional array of tiles in the level. This method is reserved only for
	 * encoders that need access to internal information to save the object.
	 * 
	 * @return
	 * 		2d array of tiles. Changes to the array <strong> will cause issues. Do not modify</strong>
	 */
	public Tile[][] internalGetTiles() { return this.screenTiles; }
}
