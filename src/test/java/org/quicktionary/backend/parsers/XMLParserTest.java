package org.quicktionary.backend.parsers;

import org.junit.Test;
import org.junit.Before;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.Timeout;

import java.io.IOException;
import java.io.ByteArrayInputStream;

import org.quicktionary.backend.parsers.XMLParser;

/**
 *
 */
public class XMLParserTest {
	private XMLParser parser;

	@Rule
	public Timeout globalTimeout = new Timeout(100);

	@Before
	public void setup() throws IOException {
		parser = new XMLParser();
	}

	public boolean parseString(String fileText) throws IOException {
		return parser.parseFile(new ByteArrayInputStream(fileText.getBytes("UTF-8")));
	}

	String xmlDecl = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";

	@Test
	public void emptyDocument() throws IOException {
		Assert.assertTrue(parseString(xmlDecl));
	}

	@Test
	public void dontGetRootOfEmptyDocument() throws IOException {
		String s = xmlDecl;
		parseString(s);
		Assert.assertFalse(parser.getRoot());
	}


	@Test
	public void emptyWithRoot() throws IOException {
		String s = xmlDecl + "<test></test>";
		Assert.assertTrue(parseString(s));
	}

	@Test
	public void getEmptyRootNode() throws IOException {
		String s = xmlDecl + "<test></test>";
		parseString(s);
		Assert.assertTrue(parser.getRoot());
	}

	@Test
	public void InvalidRootNode() throws IOException {
		String s = xmlDecl + "<test></hello>";
		parseString(s);
		Assert.assertFalse(parser.findElement(""));
	}

	@Test
	public void TextOustideOfRootNode() throws IOException {
		String s = xmlDecl + "lolol<test></test>";
		parseString(s);
		Assert.assertFalse(parser.findElement(""));
	}


	@Test
	public void notEndingElement() throws IOException {
		String s = xmlDecl + "<test><hello></test>";
		parseString(s);
		Assert.assertFalse(parser.findElement(""));
	}

	@Test
	public void invalidElement() throws IOException {
		String s = xmlDecl + "<test><hello</test>";
		parseString(s);
		Assert.assertFalse(parser.findElement(""));
	}

	@Test
	public void emptyNode() throws IOException {
		String s = xmlDecl + "<test><hello/></test>";
		Assert.assertTrue(parseString(s));
	}

	@Test
	public void getEmptyNode() throws IOException {
		String s = xmlDecl + "<test><hello/></test>";
		parseString(s);
		Assert.assertTrue(parser.findElement("hello"));
	}

	@Test
	public void getEngingAndEmptyNode() throws IOException {
		String s = xmlDecl + "<test></hello/></test>";
		parseString(s);
		Assert.assertFalse(parser.findElement("hello"));
	}

	@Test
	public void findChild() throws IOException {
		String s = xmlDecl + "<test><t><hello>hei</hello></t></test>";
		parseString(s);
		parser.getRoot();
		Assert.assertTrue(parser.findElement("hello"));
	}


	@Test
	public void nodeWithText() throws IOException {
		String s = xmlDecl + "<test><hello>hei</hello></test>";
		Assert.assertTrue(parseString(s));
	}

	@Test
	public void getNodeWithText() throws IOException {
		String s = xmlDecl + "<test><hello>hei</hello></test>";
		parseString(s);
		parser.findElement("hello");
		parser.getFirstChild();
		Assert.assertEquals("hei", parser.getTextContent());
	}

	@Test
	public void getSiblingOfNode() throws IOException {
		String s = xmlDecl + "<test><cool/><hello>hi!</hello></test>";
		parseString(s);
		parser.findElement("cool");
		Assert.assertTrue(parser.getNextSibling());
	}

	@Test
	public void dontGetSiblingOfNode() throws IOException {
		String s = xmlDecl + "<test><cool/></test>";
		parseString(s);
		parser.findElement("cool");
		Assert.assertFalse(parser.getNextSibling());
	}


	@Test
	public void getAttribute() throws IOException {
		String s = xmlDecl + "<test><cool level=\"awesome\"/></test>";
		parseString(s);
		parser.findElement("cool");
		Assert.assertEquals("awesome", parser.getAttribute("level"));
	}

	@Test
	public void getAttributeWithNamespace() throws IOException {
		String s = xmlDecl + "<test><cool xml:space=\"preserve\"> foo </cool></test>";
		parseString(s);
		parser.findElement("cool");
		Assert.assertEquals("preserve", parser.getAttribute("xml:space"));
	}

	@Test
	public void nonEndingAttribute() throws IOException {
		String s = xmlDecl + "<test><cool space=\"preserve> foo </cool></test>";
		parseString(s);
		Assert.assertFalse(parser.findElement(""));
	}

	@Test
	public void attributeWithSpaces() throws IOException {
		String s = xmlDecl + "<test><cool space = \"preserve\"> foo </cool></test>";
		parseString(s);
		Assert.assertTrue(parser.findElement(""));
	}
}
