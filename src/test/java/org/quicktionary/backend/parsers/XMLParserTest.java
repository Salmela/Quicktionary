package org.quicktionary.backend.parsers;

import org.junit.Test;
import org.junit.Before;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.Timeout;

import java.io.IOException;
import java.io.StringReader;

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
		return parser.parseFile(new StringReader(fileText));
	}

	String xmlDecl = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";

	@Test
	public void emptyDocument() throws IOException {
		Assert.assertFalse(parseString(""));
	}

	@Test
	public void documentWithOnlyDoctype() throws IOException {
		Assert.assertTrue(parseString(xmlDecl));
	}

	@Test
	public void failToGetRootOfEmptyDocument() throws IOException {
		String s = xmlDecl;
		parseString(s);
		exception.expect(XMLParserError.class);
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
	public void failToGetRoot() throws IOException {
		String s = xmlDecl + "<test><hello></hello></test>";
		parseString(s);
		parser.findElement("hello");

		exception.expect(XMLParserError.class);
		Assert.assertFalse(parser.getRoot());
	}

	@Test
	public void InvalidRootNode() throws IOException {
		String s = xmlDecl + "<test></hello>";
		parseString(s);

		exception.expect(XMLParserError.class);
		Assert.assertFalse(parser.findElement(""));
	}

	@Test
	public void TextOustideOfRootNode() throws IOException {
		String s = xmlDecl + "lolol<test></test>";
		parseString(s);

		exception.expect(XMLParserError.class);
		Assert.assertFalse(parser.findElement(""));
	}


	@Test
	public void notEndingElement() throws IOException {
		String s = xmlDecl + "<test><hello></test>";
		parseString(s);

		exception.expect(XMLParserError.class);
		Assert.assertFalse(parser.findElement(""));
	}

	@Test
	public void invalidElement() throws IOException {
		String s = xmlDecl + "<test><hello</test>";
		parseString(s);

		exception.expect(XMLParserError.class);
		Assert.assertFalse(parser.findElement(""));
	}

	@Test
	public void invalidCharactersInTagName() throws IOException {
		String s = xmlDecl + "<test><hällo/></test>";
		parseString(s);

		exception.expect(XMLParserError.class);
		Assert.assertFalse(parser.findElement(""));
	}

	@Test
	public void numbersInTagName() throws IOException {
		String s = xmlDecl + "<test><h9llo/></test>";
		parseString(s);

		Assert.assertTrue(parser.findElement(""));
	}

	@Test
	public void numberAsFirstLetterOfTagName() throws IOException {
		String s = xmlDecl + "<test><9allo/></test>";
		parseString(s);

		exception.expect(XMLParserError.class);
		Assert.assertFalse(parser.findElement(""));
	}

	@Test
	public void emptyNodeWithWhitespaces() throws IOException {
		String s = xmlDecl + "<test><hello \n/></test>";
		Assert.assertTrue(parseString(s));
	}

	@Test
	public void emptyNode() throws IOException {
		String s = xmlDecl + "<test><hello/></test>";
		parseString(s);
		Assert.assertTrue(parser.findElement(""));
	}

	@Test
	public void commentNode() throws IOException {
		String s = xmlDecl + "<test><!-- this is comment --></test>";
		parseString(s);
		Assert.assertTrue(parser.findElement(""));
	}


	@Test
	public void getEmptyNode() throws IOException {
		String s = xmlDecl + "<test><hello/></test>";
		parseString(s);
		Assert.assertTrue(parser.findElement("hello"));
	}

	@Test
	public void getEndingAndEmptyNode() throws IOException {
		String s = xmlDecl + "<test></hello/></test>";
		parseString(s);

		exception.expect(XMLParserError.class);
		Assert.assertFalse(parser.findElement("hello"));
	}


	@Test
	public void getElementName() throws IOException {
		String s = xmlDecl + "<test><cool space = \"preserve\"> foo </cool></test>";
		parseString(s);
		parser.findElement("cool");
		Assert.assertEquals("cool", parser.getElementName());
	}

	@Test
	public void getElementsNodeType() throws IOException {
		String s = xmlDecl + "<test><cool space = \"preserve\"> foo </cool></test>";
		parseString(s);
		parser.findElement("cool");
		Assert.assertEquals(NodeType.ELEMENT, parser.getNodeType());
	}

	@Test
	public void getTextNodeType() throws IOException {
		String s = xmlDecl + "<test><cool space = \"preserve\"> foo </cool></test>";
		parseString(s);
		parser.findElement("cool");
		parser.getFirstChild();
		Assert.assertEquals(NodeType.TEXT, parser.getNodeType());
	}

	@Test
	public void getCommentsNodeType() throws IOException {
		String s = xmlDecl + "<test><cool space = \"preserve\"><!-- this is comment --></cool></test>";
		parseString(s);
		parser.findElement("cool");
		parser.getFirstChild();
		Assert.assertEquals(NodeType.COMMENT, parser.getNodeType());
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
	public void failToGetSiblingOfNode() throws IOException {
		String s = xmlDecl + "<test><cool/></test>";
		parseString(s);
		parser.findElement("cool");

		exception.expect(XMLParserError.class);
		Assert.assertFalse(parser.getNextSibling());
	}


	@Test
	public void getParentOfNode() throws IOException {
		String s = xmlDecl + "<test><cool><jee/></cool></test>";
		parseString(s);
		parser.findElement("jee");

		Assert.assertTrue(parser.getParent());
	}


	@Test
	public void getChildOfNode() throws IOException {
		String s = xmlDecl + "<test><cool><jee/></cool></test>";
		parseString(s);
		parser.findElement("cool");
		parser.getNextNode();

		exception.expect(XMLParserError.class);
		Assert.assertFalse(parser.getFirstChild());
	}

	@Test
	public void failToGetChildOfTextNode() throws IOException {
		String s = xmlDecl + "<test>hello</test>";
		parseString(s);
		parser.findElement("test");
		parser.getNextNode();

		exception.expect(XMLParserError.class);
		Assert.assertFalse(parser.getFirstChild());
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
	public void mismatchedQuotesInAttribute() throws IOException {
		String s = xmlDecl + "<test><cool space=\"preserve\'> foo </cool></test>";
		parseString(s);

		exception.expect(XMLParserError.class);
		Assert.assertFalse(parser.findElement(""));
	}

	@Test
	public void emptyValuedAttribute() throws IOException {
		String s = xmlDecl + "<test><cool space=> foo </cool></test>";
		parseString(s);

		exception.expect(XMLParserError.class);
		Assert.assertFalse(parser.findElement(""));
	}

	@Test
	public void nonEndingAttribute() throws IOException {
		String s = xmlDecl + "<test><cool space=\"preserve> foo </cool></test>";
		parseString(s);

		exception.expect(XMLParserError.class);
		Assert.assertFalse(parser.findElement(""));
	}

	@Test
	public void recoveryFromInvalidAttribute() throws IOException {
		String s = xmlDecl + "<test><cool space=> foo </cool><hello></hello></test>";
		parseString(s);

		exception.expect(XMLParserError.class);
		Assert.assertFalse(parser.findElement("cool"));
		Assert.assertTrue(parser.findElement("hello"));
	}

	@Test
	public void attributeWithSpaces() throws IOException {
		String s = xmlDecl + "<test><cool space = \"preserve\"> foo </cool></test>";
		parseString(s);
		Assert.assertTrue(parser.findElement(""));
	}
}
