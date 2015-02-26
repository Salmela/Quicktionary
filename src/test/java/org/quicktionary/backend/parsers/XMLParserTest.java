package org.quicktionary.backend.parsers;

import org.junit.Test;
import org.junit.Before;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.Timeout;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.io.StringReader;

import org.quicktionary.backend.parsers.XMLParser;

/**
 *
 */
public class XMLParserTest {
	private XMLParser parser;

	//@Rule
	//public Timeout globalTimeout = new Timeout(500);

	@Rule
	public ExpectedException thrown = ExpectedException.none();

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
	public void isntInitializedWhenEmptyDocument() throws IOException {
		parseString("");
		Assert.assertFalse(parser.isInitialized());
	}

	@Test
	public void isInitializedWithXmlDecl() throws IOException {
		parseString(xmlDecl);
		Assert.assertTrue(parser.isInitialized());
	}

	@Test
	public void documentWithOnlyDoctype() throws IOException {
		Assert.assertTrue(parseString(xmlDecl));
	}

	@Test
	public void invalidShortXmlDecl() throws IOException {
		parseString("<?xml ?>");

		parser.findElement("");
		Assert.assertFalse(!parser.parsingErrorHappened());
	}

	@Test
	public void invalidXmlDeclWithoutCharset() throws IOException {
		parseString("<?xml version=\"1.0\" ?>");

		parser.findElement("");
		Assert.assertFalse(!parser.parsingErrorHappened());
	}


	@Test
	public void failToGetRootOfEmptyDocument() throws IOException {
		String s = xmlDecl;
		parseString(s);

		Assert.assertFalse(parser.getRoot());
	}


	@Test
	public void emptyWithRoot() throws IOException {
		String s = xmlDecl + "<test></test>";
		parseString(s);

		parser.findElement("");
		Assert.assertTrue(!parser.parsingErrorHappened());
	}

	@Test
	public void getEmptyRootNode() throws IOException {
		String s = xmlDecl + "<test></test>";
		parseString(s);
		Assert.assertTrue(parser.getRoot());
	}

	@Test
	public void getCorrectRootNode() throws IOException {
		String s = xmlDecl + "<test><hello></hello></test>";
		parseString(s);
		parser.getRoot();
		Assert.assertEquals("test", parser.getElementName());
	}

	@Test
	public void failToGetRoot() throws IOException {
		String s = xmlDecl + "<test><hello></hello></test>";
		parseString(s);
		parser.findElement("hello");

		Assert.assertFalse(parser.getRoot());
	}

	@Test
	public void InvalidRootNode() throws IOException {
		String s = xmlDecl + "<test></hello>";
		parseString(s);

		parser.findElement("");
		Assert.assertFalse(!parser.parsingErrorHappened());
	}

	@Test
	public void TextOustideOfRootNode() throws IOException {
		String s = xmlDecl + "lolol<test></test>";
		parseString(s);

		parser.findElement("");
		Assert.assertFalse(!parser.parsingErrorHappened());
	}

	@Test
	public void TextAfterRootNode() throws IOException {
		String s = xmlDecl + "<test></test>lolol";
		parseString(s);

		Assert.assertFalse(parser.findElement(""));
	}

	@Test
	public void EmptyNodeAsRoot() throws IOException {
		String s = xmlDecl + "<test/>";
		parseString(s);

		parser.findElement("");
		Assert.assertFalse(!parser.parsingErrorHappened());
	}


	@Test
	public void notEndingElement() throws IOException {
		String s = xmlDecl + "<test><hello></test>";
		parseString(s);

		parser.findElement("");
		Assert.assertFalse(!parser.parsingErrorHappened());
	}

	@Test
	public void invalidElement() throws IOException {
		String s = xmlDecl + "<test><hello</test>";
		parseString(s);

		parser.findElement("");
		Assert.assertFalse(!parser.parsingErrorHappened());
	}

	@Test
	public void validCharactersInTagName() throws IOException {
		String s = xmlDecl + "<test><zaZA:ABC/></test>";
		parseString(s);

		parser.findElement("");
		Assert.assertTrue(!parser.parsingErrorHappened());
	}

	@Test
	public void invalidCharactersInTagName() throws IOException {
		String s = xmlDecl + "<test><hällo/></test>";
		parseString(s);

		parser.findElement("");
		Assert.assertFalse(!parser.parsingErrorHappened());
	}

	@Test
	public void numbersInTagName() throws IOException {
		String s = xmlDecl + "<test><h9llo/></test>";
		parseString(s);

		parser.findElement("");
		Assert.assertTrue(!parser.parsingErrorHappened());
	}

	@Test
	public void numberAsFirstLetterOfTagName() throws IOException {
		String s = xmlDecl + "<test><9allo/></test>";
		parseString(s);

		parser.findElement("");
		Assert.assertFalse(!parser.parsingErrorHappened());
	}

	@Test
	public void emptyNodeWithWhitespaces() throws IOException {
		String s = xmlDecl + "<test><hello \n/></test>";
		parseString(s);

		parser.findElement("");
		Assert.assertTrue(!parser.parsingErrorHappened());
	}

	@Test
	public void emptyNode() throws IOException {
		String s = xmlDecl + "<test><hello/></test>";
		parseString(s);
		parser.findElement("");
		Assert.assertTrue(!parser.parsingErrorHappened());
	}

	@Test
	public void commentNode() throws IOException {
		String s = xmlDecl + "<test><!-- this is comment --></test>";
		parseString(s);
		parser.findElement("");
		Assert.assertTrue(!parser.parsingErrorHappened());
	}

	@Test
	public void validCommentNode() throws IOException {
		String s = xmlDecl + "<test><!-- this is comment ----></test>";
		parseString(s);
		parser.findElement("");
		Assert.assertTrue(!parser.parsingErrorHappened());
	}

	@Test
	public void invalidCommentNode() throws IOException {
		String s = xmlDecl + "<test><!*- this is comment --></test>";
		parseString(s);
		parser.findElement("");
		Assert.assertFalse(!parser.parsingErrorHappened());
	}

	@Test
	public void commentNodeWithInvalidEnd() throws IOException {
		String s = xmlDecl + "<test><!-- this is comment -></test>";
		parseString(s);
		parser.findElement("");
		Assert.assertFalse(!parser.parsingErrorHappened());
	}

	@Test
	public void commentNodeWithInvalidStart() throws IOException {
		String s = xmlDecl + "<test><! this is comment --></test>";
		parseString(s);
		parser.findElement("");
		Assert.assertFalse(!parser.parsingErrorHappened());
	}

	@Test
	public void emptyInvalidCommentNode() throws IOException {
		String s = xmlDecl + "<test><!---></test>";
		parseString(s);
		parser.findElement("");
		Assert.assertFalse(!parser.parsingErrorHappened());
	}

	@Test
	public void emptierInvalidCommentNode() throws IOException {
		String s = xmlDecl + "<test><!--></test>";
		parseString(s);
		parser.findElement("");
		Assert.assertFalse(!parser.parsingErrorHappened());
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

		parser.findElement("hello");
		Assert.assertFalse(!parser.parsingErrorHappened());
	}


	@Test
	public void getElementName() throws IOException {
		String s = xmlDecl + "<test><cool space = \"preserve\"> foo </cool></test>";
		parseString(s);
		parser.findElement("cool");
		Assert.assertEquals("cool", parser.getElementName());
	}

	@Test
	public void findElementWithId() throws IOException {
		String s = xmlDecl + "<test><cool> foo </cool></test>";
		parseString(s);
		parser.setTagNameId("cool", 0);
		Assert.assertTrue(parser.findElement(0));
	}

	@Test
	public void setElementNameId() throws IOException {
		String s = xmlDecl + "<test><cool> foo </cool></test>";
		parseString(s);
		parser.setTagNameId("cool", 0);
		parser.findElement(0);
		Assert.assertEquals(0, parser.getElementNameId());
	}

	@Test
	public void setInvalidElementNameId() throws IOException {
		String s = xmlDecl + "<test><cool> foo </cool></test>";
		parseString(s);

		thrown.expect(XMLParser.ParserError.class);
		parser.setTagNameId("cool", 9);
	}

	@Test
	public void getElementsNodeType() throws IOException {
		String s = xmlDecl + "<test><cool space = \"preserve\"> foo </cool></test>";
		parseString(s);
		parser.findElement("cool");
		Assert.assertEquals(XMLParser.NodeType.ELEMENT, parser.getNodeType());
	}

	@Test
	public void getTextNodeType() throws IOException {
		String s = xmlDecl + "<test><cool space = \"preserve\"> foo </cool></test>";
		parseString(s);
		parser.findElement("cool");
		parser.getFirstChild();
		Assert.assertEquals(XMLParser.NodeType.TEXT, parser.getNodeType());
	}

	@Test
	public void getCommentsNodeType() throws IOException {
		String s = xmlDecl + "<test><cool space = \"preserve\"><!-- this is comment --></cool></test>";
		parseString(s);
		parser.findElement("cool");
		parser.getFirstChild();
		Assert.assertEquals(XMLParser.NodeType.COMMENT, parser.getNodeType());
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
	public void getTextContentOfNode() throws IOException {
		String s = xmlDecl + "<test><hello>hei</hello></test>";
		parseString(s);
		parser.findElement("hello");
		Assert.assertEquals("hei", parser.getTextContent());
	}

	@Test
	public void getTextContentOfNodeWithoutText() throws IOException {
		String s = xmlDecl + "<test><hello><jee/></hello></test>";
		parseString(s);
		parser.findElement("hello");
		Assert.assertEquals(null, parser.getTextContent());
	}

	@Test
	public void getTextContentOfEmptyNode() throws IOException {
		String s = xmlDecl + "<test><hello/></test>";
		parseString(s);
		parser.findElement("hello");
		Assert.assertEquals(null, parser.getTextContent());
	}

	@Test
	public void getTextNodeWithWhitespaces() throws IOException {
		String s = xmlDecl + "<test><hello>  \nhei </hello></test>";
		parseString(s);
		parser.findElement("hello");
		Assert.assertEquals("  \nhei ", parser.getTextContent(true));
	}

	@Test
	public void getSiblingOfNode() throws IOException {
		String s = xmlDecl + "<test><cool/><hello>hi!</hello></test>";
		parseString(s);

		parser.findElement("cool");
		Assert.assertTrue(parser.getNextSibling());
		Assert.assertEquals("hello", parser.getElementName());
	}

	@Test
	public void failToGetSiblingOfNode() throws IOException {
		String s = xmlDecl + "<test><cool/></test>";
		parseString(s);
		parser.findElement("cool");

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

		Assert.assertFalse(parser.getFirstChild());
	}

	@Test
	public void failToGetChildOfTextNode() throws IOException {
		String s = xmlDecl + "<test>hello</test>";
		parseString(s);
		parser.findElement("test");
		parser.getNextNode();

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
	public void getUndefinedAttribute() throws IOException {
		String s = xmlDecl + "<test><cool level=\"awesome\"/></test>";
		parseString(s);
		parser.findElement("cool");
		Assert.assertEquals(null, parser.getAttribute("hello"));
	}

	@Test
	public void getAttributeWithNamespace() throws IOException {
		String s = xmlDecl + "<test><cool xml:space=\"preserve\"> foo </cool></test>";
		parseString(s);
		parser.findElement("cool");
		Assert.assertEquals("preserve", parser.getAttribute("xml:space"));
	}

	@Test
	public void getAttributeAfterNodeWithAttribute() throws IOException {
		String s = xmlDecl + "<test><cool level=\"awesome\"/><cool another=\"hehe\"/></test>";
		parseString(s);
		parser.findElement("cool");
		parser.findElement("cool");
		Assert.assertNotSame("awesome", parser.getAttribute("level"));
	}

	@Test
	public void mismatchedQuotesInAttribute() throws IOException {
		String s = xmlDecl + "<test><cool space=\"preserve\'> foo </cool></test>";
		parseString(s);

		parser.findElement("");
		Assert.assertFalse(!parser.parsingErrorHappened());
	}

	@Test
	public void noQuotesInAttribute() throws IOException {
		String s = xmlDecl + "<test><cool space=preserve> foo </cool></test>";
		parseString(s);

		parser.findElement("");
		Assert.assertFalse(!parser.parsingErrorHappened());
	}

	@Test
	public void emptyValuedAttribute() throws IOException {
		String s = xmlDecl + "<test><cool space=> foo </cool></test>";
		parseString(s);

		parser.findElement("");
		Assert.assertFalse(!parser.parsingErrorHappened());
	}

	@Test
	public void nonEndingAttribute() throws IOException {
		String s = xmlDecl + "<test><cool space=\"preserve> foo </cool></test>";
		parseString(s);

		parser.findElement("");
		Assert.assertFalse(!parser.parsingErrorHappened());
	}

	@Test
	public void recoveryFromInvalidAttribute() throws IOException {
		String s = xmlDecl + "<test><cool space=> foo </cool><hello></hello></test>";
		parseString(s);

		Assert.assertTrue(parser.findElement("hello"));
		Assert.assertFalse(!parser.parsingErrorHappened());
	}

	@Test
	public void attributeWithSpaces() throws IOException {
		String s = xmlDecl + "<test><cool space = \"preserve\"> foo </cool></test>";
		parseString(s);
		parser.findElement("");
		Assert.assertTrue(!parser.parsingErrorHappened());
	}

	@Test
	public void attributeWithInvalidStartCharacterInName() throws IOException {
		String s = xmlDecl + "<test><cool 8space=\"hei\"> foo </cool></test>";
		parseString(s);

		parser.findElement("");
		Assert.assertFalse(!parser.parsingErrorHappened());
	}
}
