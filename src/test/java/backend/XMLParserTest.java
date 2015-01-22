package backend;

import org.junit.Test;
import org.junit.Before;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.Timeout;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.quicktionary.backend.parsers.XMLParser;

/**
 *
 */
public class XMLParserTest {
	private XMLParser parser;
	private File xmlFile;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Rule
	public Timeout globalTimeout = new Timeout(100);

	public XMLParserTest() {
		xmlFile = null;
	}

	@Before
	public void setup() throws IOException {
		xmlFile = folder.newFile("test.xml");
		parser = new XMLParser();
	}

	public void testFile(String fileText) throws IOException {
		PrintWriter writer = new PrintWriter(xmlFile, "UTF-8");
		writer.print(fileText);
		writer.close();
	}

	String xmlDecl = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";

	@Test
	public void emptyDocument() throws IOException {
		testFile(xmlDecl);
		Assert.assertTrue(parser.parseFile(xmlFile));
	}

	@Test
	public void dontGetRootOfEmptyDocument() throws IOException {
		testFile(xmlDecl);
		parser.parseFile(xmlFile);
		Assert.assertFalse(parser.getRoot());
	}


	@Test
	public void emptyWithRoot() throws IOException {
		testFile(xmlDecl + "<test></test>");
		Assert.assertTrue(parser.parseFile(xmlFile));
	}

	@Test
	public void getEmptyRootNode() throws IOException {
		testFile(xmlDecl + "<test></test>");
		parser.parseFile(xmlFile);
		Assert.assertTrue(parser.getRoot());
	}

	@Test
	public void InvalidRootNode() throws IOException {
		testFile(xmlDecl + "<test></hello>");
		parser.parseFile(xmlFile);
		Assert.assertFalse(parser.getRoot());
	}


	@Test
	public void notEndingElement() throws IOException {
		testFile(xmlDecl + "<test><hello></test>");
		parser.parseFile(xmlFile);
		Assert.assertFalse(parser.findElement(""));
	}

	@Test
	public void emptyNode() throws IOException {
		testFile(xmlDecl + "<test><hello/></test>");
		Assert.assertTrue(parser.parseFile(xmlFile));
	}

	@Test
	public void getEmptyNode() throws IOException {
		testFile(xmlDecl + "<test><hello/></test>");
		parser.parseFile(xmlFile);
		Assert.assertTrue(parser.findElement("hello"));
	}

	@Test
	public void findChild() throws IOException {
		testFile(xmlDecl + "<test><t><hello>hei</hello></t></test>");
		parser.parseFile(xmlFile);
		parser.getFirstChild();
		Assert.assertTrue(parser.findElement("hello"));
	}


	@Test
	public void nodeWithText() throws IOException {
		testFile(xmlDecl + "<test><hello>hei</hello></test>");
		Assert.assertTrue(parser.parseFile(xmlFile));
	}

	@Test
	public void getNodeWithText() throws IOException {
		testFile(xmlDecl + "<test><hello>hei</hello></test>");
		parser.parseFile(xmlFile);
		parser.findElement("hello");
		parser.getFirstChild();
		Assert.assertEquals("hei", parser.getTextContent());
	}


	@Test
	public void getChildOfNode() throws IOException {
		testFile(xmlDecl + "<test><hello>hei</hello></test>");
		parser.parseFile(xmlFile);
		parser.findElement("hello");
		parser.getFirstChild();
		Assert.assertEquals("hei", parser.getTextContent());
	}

	@Test
	public void getSiblingOfChildOfNode() throws IOException {
		testFile(xmlDecl + "<test><cool/><hello>hei</hello></test>");
		parser.parseFile(xmlFile);
		parser.findElement("hello");
		parser.getFirstChild();
		parser.getNextSibling();
		Assert.assertEquals("hello", parser.getElementName());
	}
}
