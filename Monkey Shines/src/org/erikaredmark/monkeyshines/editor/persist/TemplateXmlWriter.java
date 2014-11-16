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
import javax.xml.transform.OutputKeys;
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
		if (!(Files.exists(xmlFile) ) )  createEditorXmlFile(docFactory, xmlFile);
		// Document is required outside of initial parse
		Document doc = null;
		try (InputStream in = Files.newInputStream(xmlFile) ) {
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.parse(in);
			
			// We will use XPath to query for all Template nodes that are nested under the given world name
			XPath worldTemplateQuery = XPathFactory.newInstance().newXPath();
			
			XPathExpression worldTemplates = worldTemplateQuery.compile("/msleveleditor/world[@name='" + worldName + "']/templates");
			XPathExpression worldEntry = worldTemplateQuery.compile("/msleveleditor/world[@name='" + worldName + "']");
			
			Node templatesNode = (Node) worldTemplates.evaluate(doc, XPathConstants.NODE);
			// <world> .... </world> will always be an element
			Element worldNode = (Element) worldEntry.evaluate(doc, XPathConstants.NODE);
			
			// if worldNode doesn't exist, create it.
			if (worldNode == null) {
				Element root = doc.getDocumentElement();
				worldNode = doc.createElement("world");
				worldNode.setAttribute("name", worldName);
				root.appendChild(worldNode);
			}
			
			// If the node list is not empty, remove and replace.
			if (templatesNode != null ) {
				worldNode.removeChild(templatesNode);
				templatesNode = null;
			}

			// If a templates node existed, it's gone now. Create a new one.
			Element newTemplatesNode = doc.createElement("templates");
			addTemplatesToNode(doc, newTemplatesNode, templates);
			worldNode.appendChild(newTemplatesNode);


		} catch (IOException | ParserConfigurationException | SAXException | XPathExpressionException e) {
			throw new BadEditorPersistantFormatException(e);
		}
		
		
		// The DOM has been properly modified. Rewrite out to the file.
		assert (doc != null);
		saveDocToFile(doc, xmlFile);

	}
	
	private static void saveDocToFile(Document doc, Path xmlFile) throws BadEditorPersistantFormatException {
		try (OutputStream os = Files.newOutputStream(xmlFile) ) {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(os);
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.transform(source, result);
			
		} catch (IOException | TransformerException e) {
			throw new BadEditorPersistantFormatException(e);
		}
	}
	
	private static void createEditorXmlFile(DocumentBuilderFactory docFactory, Path xmlFile) throws BadEditorPersistantFormatException {
		try {
			Document doc = docFactory.newDocumentBuilder().newDocument();
			Element root = doc.createElement("msleveleditor");
			doc.appendChild(root);
			saveDocToFile(doc, xmlFile);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("Cannot create editor xml file due to " + e.getMessage(), e);
		}
	}
	
	private static String tileTypeToXml(TileType tile) {
		if (tile instanceof CommonTile) {
			switch ( ((CommonTile)tile).getUnderlyingType() ) {
			case NONE: return "empty";
			case SCENE: return "scene";
			case SOLID: return "solid";
			case THRU: return "thru";
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
	
	// Call on a node of name "templates"
	private static void addTemplatesToNode(Document doc, Node node, List<Template> templates) {
		assert "templates".equals(node.getNodeName() );
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
			
			node.appendChild(templateNode);
		}
	}
	
}
