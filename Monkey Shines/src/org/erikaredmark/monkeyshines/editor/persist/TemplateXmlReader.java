package org.erikaredmark.monkeyshines.editor.persist;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.erikaredmark.monkeyshines.World;
import org.erikaredmark.monkeyshines.editor.exception.BadEditorPersistantFormatException;
import org.erikaredmark.monkeyshines.editor.model.Template;
import org.erikaredmark.monkeyshines.tiles.CommonTile;
import org.erikaredmark.monkeyshines.tiles.TileType;
import org.erikaredmark.monkeyshines.tiles.TileTypes;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * 
 * Interprets an xml stream as the save state for an editor, and allows for the parsing of a list of templates based on
 * the stream and the name of the world we are looking for templates for.
 * 
 * @author Erika Redmark
 *
 */
public final class TemplateXmlReader {

	/**
	 * 
	 * Reads the list of templates from the given stream for the given world name. The stream is NOT closed by the method.
	 * <p/>
	 * Because one bad template should not affect the others, issues are designated via callbacks. The most likely issue is
	 * in regards to a template defining a tile type whose id is out of range of the current graphics type. In this case, the
	 * specific template in error will be ignored and not returned.
	 * 
	 * @param s
	 * 		stream to read
	 * 
	 * @param world
	 * 		the world to load the templates for. This is used for searching as well as generating templates with
	 * 		tile types found in the world
	 * 
	 * @param badTemplateCallback
	 * 		if a template refers to tiles out-of-range of the current world, this is called with a reference to the 
	 * 		XML node of the {@code <template> } object that failed and a reason
	 * 
	 * @return
	 * 		list of templates for that world, an empty list if no templates are defined. Never {@code null}
	 * 
	 * @throws BadEditorPersistantFormatException
	 * 		if the given stream is not a valid editor persistant format
	 * 
	 */
	public static List<Template> read(InputStream s, World world, Consumer<TemplateIssue> badTemplateCallback) throws BadEditorPersistantFormatException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(s);
			
			// We will use XPath to query for all Template nodes that are nested under the given world name
			XPath worldTemplateQuery = XPathFactory.newInstance().newXPath();
			XPathExpression expression = worldTemplateQuery.compile("/msleveleditor/world[@name='" + world.getWorldName() + "']/templates/template");
			
			
			
			NodeList nodes = (NodeList) expression.evaluate(doc, XPathConstants.NODESET);
		
			List<Template> returnList = new ArrayList<>();
			for (int i = 0; i < nodes.getLength(); ++i) {
				Node n = nodes.item(i);
				// This node should be a single <template> entry in the <templates> container. We can build a template from
				// its type
				boolean skipTemplate = false;
				if ("template".equals(n.getNodeName() ) ) {
					Template.Builder templateBuilder = new Template.Builder();
					NodeList tiles = n.getChildNodes();
					for (int j = 0; j < tiles.getLength(); ++j) {
						Node tile = tiles.item(j);
						// Some nodes are just #text nodes. Skip them if they have not attributes
						NamedNodeMap attribs = tile.getAttributes();
						if (attribs == null) continue;
						
						Node rowNode = attribs.getNamedItem("row");
						Node colNode = attribs.getNamedItem("col");
						Node idNode = attribs.getNamedItem("id");
						Node typeNode = attribs.getNamedItem("type");
						
						if (rowNode == null || colNode == null || idNode == null || typeNode == null) {
							badTemplateCallback.accept(new TemplateIssue(tile, IssueType.TILE_MISSING_REQUIRED_ATTRIBUTES) );
							skipTemplate = true;
							break;
						}
						
						int row = Integer.parseInt(rowNode.getNodeValue() );
						int col = Integer.parseInt(colNode.getNodeValue() );
						int id = Integer.parseInt(idNode.getNodeValue() );
						
						TileType tileType = null;
						switch (typeNode.getNodeValue() ) {
						case SOLIDS:
							tileType = TileTypes.solidFromId(id);
							break;
						case THRUS:
							tileType = TileTypes.thruFromId(id);
							break;
						case SCENES:
							tileType = TileTypes.sceneFromId(id);
							break;
						case HAZARDS:
							if (!(TileTypes.canHazardFromId(id, world) ) ) {
								badTemplateCallback.accept(new TemplateIssue(tile, IssueType.TILE_ID_NOT_AVAILABLE) );
								skipTemplate = true;
							} else {
								tileType = TileTypes.hazardFromId(id, world);
							}
							break;
						case CONVEYER_CLOCKWISE:
							if (!(TileTypes.canConveyerFromId(id, world) ) ) {
								badTemplateCallback.accept(new TemplateIssue(tile, IssueType.TILE_ID_NOT_AVAILABLE) );
								skipTemplate = true;
							} else {
								tileType = TileTypes.clockwiseConveyerFromId(id, world);
							}
							break;
						case CONVEYER_ANTI_CLOCKWISE:
							if (!(TileTypes.canConveyerFromId(id, world) ) ) {
								badTemplateCallback.accept(new TemplateIssue(tile, IssueType.TILE_ID_NOT_AVAILABLE) );
								skipTemplate = true;
							} else {
								tileType = TileTypes.anticlockwiseConveyerFromId(id, world);
							}
							break;
						case COLLAPSIBLE:
							tileType = TileTypes.collapsibleFromId(id);
							break;
						case EMPTY:
							tileType = CommonTile.NONE;
							break;
						default:
							badTemplateCallback.accept(new TemplateIssue(tile, IssueType.TILE_TYPE_UNKNOWN) );
							skipTemplate = true;
							break;
						}
						
						// Breaks in switch statement are normal... check for template skipping to break from
						// for loop
						if (skipTemplate)  break;
						
						assert tile != null;
						
						templateBuilder.addTile(row, col, tileType);
					}
					
					// Skips this template due to an issue with a specific tile. Does not skip all templates.
					if (skipTemplate)  continue;
					
					// We didn't skip the template? Good, we have a valid template. Create and add to the list.
					returnList.add(templateBuilder.build() );
				}

			}
			
			return returnList;
		
		} catch (IOException | ParserConfigurationException | SAXException | XPathExpressionException e) {
			throw new BadEditorPersistantFormatException(e);
		}
	}
	
	/**
	 * 
	 * Represents an issue when parsing the xml. Wraps together both the node that failed along with the reason the node failed.
	 * 
	 * @author Erika Redmark
	 *
	 */
	public static class TemplateIssue {
		public IssueType issue;
		public Node issueNode;
		
		private TemplateIssue(final Node issueNode, final IssueType issue) {
			this.issue = issue;
			this.issueNode = issueNode;
		}

	}
	
	/**
	 * 
	 * Indicates a type of issue with the xml when parsing. Contained in {@code TemplateIssue}
	 * 
	 * @author Erika Redmark
	 *
	 */
	public enum IssueType {
		TILE_MISSING_REQUIRED_ATTRIBUTES("The <tile> element is missing required attributes. Must contain 'row', 'col', 'id', and 'type' "),
		TILE_TYPE_UNKNOWN("The <tile> element refers to a type that is not available"),
		TILE_ID_NOT_AVAILABLE("The <tile> element refers to an id of a tile that the given world does not have a graphics resource for");
		
		private IssueType(final String msg) {
			this.msg = msg;
		}
		
		public String getMessage() { return msg; }
		
		private final String msg;
	}
	
	// Tiletypes are they are named in the XML form
	private static final String SOLIDS = "solid";
	private static final String THRUS = "thru";
	private static final String SCENES = "scene";
	private static final String HAZARDS = "hazard";
	private static final String CONVEYER_CLOCKWISE = "conveyer_clockwise";
	private static final String CONVEYER_ANTI_CLOCKWISE = "conveyer_anti_clockwise";
	private static final String COLLAPSIBLE = "collapsible";
	private static final String EMPTY = "empty";

}
