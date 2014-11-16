package org.erikaredmark.monkeyshines.editor.persist;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.erikaredmark.monkeyshines.Conveyer.Rotation;
import org.erikaredmark.monkeyshines.editor.exception.BadEditorPersistantFormatException;
import org.erikaredmark.monkeyshines.editor.model.Template;
import org.erikaredmark.monkeyshines.editor.model.Template.TemplateTile;
import org.erikaredmark.monkeyshines.tiles.CollapsibleTile;
import org.erikaredmark.monkeyshines.tiles.CommonTile;
import org.erikaredmark.monkeyshines.tiles.ConveyerTile;
import org.erikaredmark.monkeyshines.tiles.HazardTile;
import org.erikaredmark.monkeyshines.tiles.TileType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * 
 * Writes out the templates in memory to an xml stream. The xml file will be created if one does not exist.
 * 
 * @author Erika Redmark
 *
 */
public final class TemplateXmlWriter {

	private TemplateXmlWriter() { }
	
	/**
	 * 
	 * Writes out all templates for the given world name to the given output file, creating it if it does not
	 * already exist.
	 * <p/>
	 * Writing out templates for a world will REPLACE the existing templates defined for that world (so the template
	 * list that is extracted originally must be maintained). This will NOT replace templates from OTHER worlds, however.
	 * 
	 * @param xmlFile
	 * 		the xmlFile to write to
	 * 
	 * @param worldName
	 * 		name of the world that this templates will be saved as. If templates already exist for a given world, all templates
	 * 		for that given world will be overwritten
	 * 
	 * @param templates
	 * 		a list of templates to write to the xml file under the given world name
	 * 
	 */
	public static void writeOutTemplatesForWorld(Path xmlFile, String worldName, List<Template> templates) throws BadEditorPersistantFormatException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		// Document is required outside of initial parse
		Document doc = null;
		try (InputStream in = Files.newInputStream(xmlFile) ) {
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.parse(in);
			
			// We will use XPath to query for all Template nodes that are nested under the given world name
			XPath worldTemplateQuery = XPathFactory.newInstance().newXPath();
			XPathExpression expression = worldTemplateQuery.compile("/msleveleditor/world[@name='" + worldName + "']/templates/template");
			
			NodeList nodes = (NodeList) expression.evaluate(doc, XPathConstants.NODESET);
		
			for (int i = 0; i < nodes.getLength(); ++i) {
				Node n = nodes.item(i);
				// This node should be a single <template> entry in the <templates> container. We can build a template from
				// its type
				if ("template".equals(n.getNodeName() ) ) {
					// Delete this node and recreate with template listing.
					NodeList preExisting = n.getChildNodes();
					for (int j = 0; j < preExisting.getLength(); ++j) {
						n.removeChild(preExisting.item(i) );
					}
					
					// Now add the template nodes.
					for (Template t : templates) {
						Element templateNode = doc.createElement("template");
						for (TemplateTile tempTile : t.getTilesInTemplate() ) {
							Element tileNode = doc.createElement("tile");
							tileNode.setAttribute("id", String.valueOf(tempTile.tile.getId() ) );
							tileNode.setAttribute("row", String.valueOf(tempTile.row) );
							tileNode.setAttribute("col", String.valueOf(tempTile.col) );
							tileNode.setAttribute("type", tileTypeToXml(tempTile.tile) );
							templateNode.appendChild(tileNode);
						}
						
						n.appendChild(templateNode);
					}
					
				}
			}

		} catch (IOException | ParserConfigurationException | SAXException | XPathExpressionException e) {
			throw new BadEditorPersistantFormatException(e);
		}
		
		
		// The DOM has been properly modified. Rewrite out to the file.
		assert (doc != null);
		try (OutputStream os = Files.newOutputStream(xmlFile) ) {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(os);
			transformer.transform(source, result);
			
		} catch (IOException | TransformerException e) {
			throw new BadEditorPersistantFormatException(e);
		}
	}
	
	private static String tileTypeToXml(TileType tile) {
		if (tile instanceof CommonTile) {
			switch ( ((CommonTile)tile).getUnderlyingType() ) {
			case NONE: return "empty";
			case SCENE: return "thru";
			case SOLID: return "solid";
			case THRU: return "scene";
			default: throw new RuntimeException("Unknown stateless tile type " +  ((CommonTile)tile).getUnderlyingType() );
			}
		} else if (tile instanceof CollapsibleTile) {
			return "collapsible";
		} else if (tile instanceof ConveyerTile) {
			if (((ConveyerTile)tile).getConveyer().getRotation() == Rotation.CLOCKWISE) {
				return "conveyer_clockwise";
			} else {
				return "conveyer_anti_clockwise";
			}
		} else if (tile instanceof HazardTile) {
			return "hazard";
		} 
		
		throw new RuntimeException("Xml decoding for templates cannot handle tiles of type " + tile.getClass().getName() );
	}
	
}
