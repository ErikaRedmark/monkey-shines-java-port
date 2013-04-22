package edu.nova.erikaredmark.monkeyshines.encoder;

import java.awt.image.BufferedImage;
import java.io.File;
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

import edu.nova.erikaredmark.monkeyshines.GameConstants;
import edu.nova.erikaredmark.monkeyshines.Goodie;
import edu.nova.erikaredmark.monkeyshines.LevelScreen;
import edu.nova.erikaredmark.monkeyshines.Point2D;
import edu.nova.erikaredmark.monkeyshines.World;

/**
 * Utility class for decoding worlds from a file. Unlike encoder, this skips the {@code EncodedObject} and returns 
 * directly worlds.
 * <p/>
 * This class is a singleton so it can use calls to {@link getClass() } to load external resources.
 * 
 * @author Erika Redmark
 *
 */
public enum Decoder {
	INSTANCE;
	
	/**
	 * Decodes an input stream into a world. All worlds are stored in an XML format.
	 * 
	 * @param xmlStream
	 * @return
	 */
	public World decodeWorld(final InputStream xmlStream) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document dom;
		
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			dom = db.parse(location);
		
			Element docEle = dom.getDocumentElement();

			NodeList nl = docEle.getElementsByTagName("worldinfo");
			//NodeList nl = docEle.get
			if(nl != null && nl.getLength() > 0) {
					// Get the single level parameters
				Element el = (Element)nl.item(0);
					
				String tilename = GameConstants.getTextValue(el, "baseTileName");
				String screenName = GameConstants.getTextValue(el, "baseScreenName");
				NodeList specifics = el.getElementsByTagName("screen");
				NodeList spritesNl = el.getElementsByTagName("sprite");
				
				// Load graphics data BEFORE screens
				/* The XML file tells what prefix to look for. */
				try {
					InputStream solidTilesFile = getClass().getResourceAsStream("/resources/graphics/" 
							+ name + "/" + tilename + "tilesolid.gif");
					InputStream thruTilesFile = getClass().getResourceAsStream("/resources/graphics/"
							+ name + "/" + tilename + "tilesthru.gif"); 
					InputStream sceneTilesFile = getClass().getResourceAsStream("/resources/graphics/"
							+ name + "/" + tilename + "tilesscene.gif");
				    solidTiles = ImageIO.read(solidTilesFile);
				    thruTiles = ImageIO.read(thruTilesFile);
				    sceneTiles = ImageIO.read(sceneTilesFile);
				    
				    //sceneTiles = ImageIO.read(new File("" + tilename + "tilesscene.gif") );
					    
				} catch (IOException e) {
					System.out.println("Quand est ce tile?");
				}
				
				// Load Sprite data also before screens
				
				if (spritesNl != null && spritesNl.getLength() > 0) {
					sprites = new BufferedImage[spritesNl.getLength() ];
					for (int i = 0; i < spritesNl.getLength(); i++) {
						Element spriteEl = (Element)spritesNl.item(i);
						
						int spriteId = GameConstants.getIntValue(spriteEl, "id");
						String imageLocation = GameConstants.getTextValue(spriteEl, "imagefile");
						
						try {
							InputStream temp = getClass().getResourceAsStream("/resources/graphics/"
									+ name + "/" + imageLocation);
						    sprites[spriteId] = ImageIO.read(temp);
							    
						} catch (IOException e) {
							System.out.println("Quand est ce sprite?");
						}
					}
				}
				/*<sprite>
				<id>2</id>
				<imagefile>bee.gif</imagefile>
			</sprite>*/
				
				// Load all the screens and let them know where their XML data is so they can parse their's.
				if (specifics != null && specifics.getLength() > 0) {
					for (int i = 0; i < specifics.getLength(); i++) {
						Element screenEl = (Element)specifics.item(i);
						
						int id = GameConstants.getIntValue(screenEl, "id");
						worldScreens.put(id, new LevelScreen(id, screenName, sprites, this) );
					}
				}
				
				// Set the goodies
				specifics = el.getElementsByTagName("goodie");
				if (specifics != null && specifics.getLength() > 0) {
					for (int i = 0; i < specifics.getLength(); i++) {
						Element goodieEl = (Element)specifics.item(i);
						
						int type = GameConstants.getIntValue(goodieEl, "type");
						int row = GameConstants.getIntValue(goodieEl, "row");
						int col = GameConstants.getIntValue(goodieEl, "col");
						int screenID = GameConstants.getIntValue(goodieEl, "screenID");
	
						String checker = "" + screenID + "X" + col + "," + row;
						goodiesInWorld.put(checker, new Goodie(this, type, Point2D.of(col, row), screenID) );
					}
				}
					
				
			}

		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
