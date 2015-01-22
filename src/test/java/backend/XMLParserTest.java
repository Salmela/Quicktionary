package backend;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.quicktionary.backend.parsers.XMLParser;

/**
 *
 * @author alesalme
 */
public class XMLParserTest {
	private XMLParser parser;
	private File xmlFile;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	public XMLParserTest() {
		xmlFile = null;
	}

	@Before
	public void setup() throws IOException {
		xmlFile = folder.newFile("test.xml");
		parser = new XMLParser();
	}

	public void testFile(String file) throws IOException {
		PrintWriter writer = new PrintWriter(xmlFile, "UTF-8");
		writer.print(file);
		writer.close();
	}

	String xmlDecl = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";

	@Test
	public void emptyDocument() throws IOException {
		testFile(xmlDecl);
		Assert.assertTrue(parser.parseFile(file));
	}

	@Test
	public void emptyDocument() throws IOException {
		testFile(xmlDecl);
		parser.parseFile(file)
		Assert.assertFalse(parser.getRoot());
	}


	@Test
	public void emptyWithRoot() throws IOException {
		testFile(xmlDecl + "<test></test>");
		Assert.assertTrue(parser.parseFile(file));
	}

	@Test
	public void getEmptyRootNode() throws IOException {
		testFile(xmlDecl + "<test></test>");
		parser.parseFile(file);
		Assert.assertTrue(parser.getRoot());
	}

	@Test
	public void InvalidRootNode() throws IOException {
		testFile(xmlDecl + "<test></hello>");
		parser.parseFile(file);
		Assert.assertFalse(parser.getRoot());
	}


	@Test
	public void notEndingElement() throws IOException {
		testFile(xmlDecl + "<test><hello></test>");
		Assert.assertFalse(parser.parseFile(file));
	}

	@Test
	public void emptyNode() throws IOException {
		testFile(xmlDecl + "<test><hello/></test>");
		Assert.assertTrue(parser.parseFile(file));
	}

	@Test
	public void getEmptyNode() throws IOException {
		testFile(xmlDecl + "<test><hello/></test>");
		parser.parseFile(file);
		Assert.assertTrue(parser.getElement("hello"));
	}


	@Test
	public void nodeWithText() throws IOException {
		testFile(xmlDecl + "<test><hello>hei</hello></test>");
		Assert.assertTrue(parser.parseFile(file));
	}

	@Test
	public void getNodeWithText() throws IOException {
		testFile(xmlDecl + "<test><hello>hei</hello></test>");
		parser.parseFile(file);
		parser.getFirstChild();
		Assert.assertTrue(parser.getElement("hello"));
	}

	@Test
	public void getNodeWithText() throws IOException {
		testFile(xmlDecl + "<test><hello>hei</hello></test>");
		parser.parseFile(file);
		parser.getElement("hello");
		parser.getFirstChild();
		Assert.assertEquals("hei", parser.getTextContent());
	}


	@Test
	public void getChildOfNode() throws IOException {
		testFile(xmlDecl + "<test><hello>hei</hello></test>");
		parser.parseFile(file);
		parser.getElement("hello");
		parser.getFirstChild();
		Assert.assertEquals("hei", parser.getTextContent());
	}

	@Test
	public void getSiblingOfNode() throws IOException {
		testFile(xmlDecl + "<test><cool/><hello>hi!</hello></test>");
		parser.parseFile(file);
		parser.getElement("hello");
		Assert.assertTrue(parser.getFirstChild());
	}

	@Test
	public void dontGetSiblingOfNode() throws IOException {
		testFile(xmlDecl + "<test><cool/></test>");
		parser.parseFile(file);
		parser.getElement("hello");
		parser.getFirstChild();
		Assert.assertFalse(parser.getFirstChild());
	}


	@Test
	public void getAttribute() throws IOException {
		testFile(xmlDecl + "<test><cool level=\"awesome\"/></test>");
		parser.parseFile(file);
		parser.getElement("hello");
		parser.getFirstChild();
		Assert.assertEquals("awesome", parser.getAttribute("level"));
	}

	@Test
	public void getAttributeWithNamespace() throws IOException {
		testFile(xmlDecl + "<test><cool xml:space=\"preserve\"> foo </cool></test>");
		parser.parseFile(file);
		parser.getElement("hello");
		parser.getFirstChild();
		Assert.assertEquals("preserve", parser.getAttribute("xml:space"));
	}
}
