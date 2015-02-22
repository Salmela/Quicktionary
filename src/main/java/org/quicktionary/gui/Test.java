package org.quicktionary.gui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import org.quicktionary.backend.parsers.XMLParser;
import org.quicktionary.backend.parsers.WikiMarkup;
import org.quicktionary.backend.TextNode;

/**
 * This file is for debugging the test cases.
 */
public class Test {
	private XMLParser  parserXML;
	private WikiMarkup parserWiki;
	private TextNode node;

	private int testSuccess, testTotal;

	String xmlDecl = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";

	public Test() {
		parserXML = new XMLParser();
		parserWiki = new WikiMarkup();

		xmlParserTest();
		wikiParserTest();

		whitespaceAtStart();
		whitespaceAtEnd();
		whitespaceBefore();
		whitespaceAfter();
		whitespaceMiddle();

		unendingTextStyle();

		mergeTextStyleMarkups();
		quoteAtStartAtTextStyleMarkup();
		oneExtraQuoteAtTextStyleMarkup();
		threeExtraQuoteAtTextStyleMarkup();

		header();
		headerWithSpace();
		headerAndParagraph();
		headerAndtwoParagraphs();
		headerWithEmAndParagraph();
		headerAndTemplate();
		horizontalLineAndHeader();

		templateMarkup();
		linkMarkup();
		linkMarkupWithName();
		linkMarkupToOutside();

		templateWithPartialStrongMarkup();
		templateAndLinkInterleaved();
		linkAndTemplateInterleaved();
		linkWithExtraBracket();
		multpleValidLinksAndTemplates();

		multiLineTemplate();
		multiLineTemplate2();
		multiLineTemplate3();
		invalidMultiLineTemplate();

		listMarkupWithoutSpace();
		doubleUnorderedListMarkupWithoutSpace();

		listWithTwoLevels();
		listWithNoOwnItems();
		listWithSublistAtMiddle();
		listTypeChangeAtMiddle();
		listTypeChangeAtMiddleInsideList();

		simpleDefinitionList();
		definitionListWithNormalList();
		definitionListAndNormalListMix();

		System.out.println("\n\nresults " + testSuccess + " / " + testTotal);
	}

	private boolean result(boolean success) {
		if(success) {
			testSuccess++;
		} else {
			System.out.println("##################");
		}
		testTotal++;
		return success;
	}

	public static void main(String[] args) {
		new Test();
	}

	public void parseString(String xmlText) {
		try {
			parserXML.parseFile(new StringReader(xmlText));
		} catch(Exception e) {
		}
	}

	public TextNode parse(String wikiText) {
		System.out.println("\nWIKI TEST\n" + wikiText);
		try {
			parserWiki.parse(new StringReader(wikiText));
			return parserWiki.getRoot();
		} catch(IOException e) {
			System.out.println("!!! IOEXCEPTION !!!");
		}
		return null;
	}

	public void xmlParserTest() {
		parseString(xmlDecl + "<test><!-- this is comment --></test>");

		parserXML.findElement("");
		System.out.println(!parserXML.parsingErrorHappened());
	}
	public void wikiParserTest() {
	}

	/* WikiMarkup */
	private TextNode newNode(TextNode parent, String text, int type, String parameter) {
		TextNode node;
		if(parent == null) {
			node = new TextNode(type, parameter);
		} else {
			node = parent.appendChild(new TextNode(type, parameter));
		}
		if(text != null) {
			TextNode textNode;
			textNode = new TextNode(TextNode.PLAIN_TYPE);
			node.appendChild(textNode);
			textNode.setContent(text);
		}
		return node;
	}
	private TextNode newNode(TextNode parent, int type, String parameter) {
		return newNode(parent, null, type, parameter);
	}
	private TextNode newNode(TextNode parent, int type) {
		return newNode(parent, null, type, null);
	}
	private TextNode newNode(TextNode parent, String text, int type) {
		return newNode(parent, text, type, null);
	}
	private void addText(TextNode parent, String text) {
		TextNode textNode;

		if(parent == null) {
			throw new Error("Parent must be set");
		}

		textNode = new TextNode(TextNode.PLAIN_TYPE);
		parent.appendChild(textNode);
		textNode.setContent(text);
	}

	public void header() {
		TextNode wanted;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		newNode(wanted, "test", TextNode.HEADER_TYPE);

		node = parse("==test==\n");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
	public void headerWithSpace() {
		TextNode wanted;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		newNode(wanted, "test", TextNode.HEADER_TYPE);

		node = parse("== test == \n");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
	public void headerAndParagraph() {
		TextNode wanted;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		newNode(wanted, "test", TextNode.HEADER_TYPE);
		newNode(wanted, "hello", TextNode.PARAGRAPH_TYPE);

		node = parse("== test == \nhello");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
	public void headerAndtwoParagraphs() {
		TextNode wanted;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		newNode(wanted, "hello", TextNode.HEADER_TYPE);
		newNode(wanted, "test hello", TextNode.PARAGRAPH_TYPE);
		newNode(wanted, "cool lol", TextNode.PARAGRAPH_TYPE);

		node = parse("== hello ==\ntest\nhello\n\ncool\nlol");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
	public void headerWithEmAndParagraph() {
		TextNode wanted, header;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		header = newNode(wanted, TextNode.HEADER_TYPE);
		newNode(wanted, "hello", TextNode.PARAGRAPH_TYPE);

		addText(header, "t");
		newNode(header, "est", TextNode.STRONG_TYPE);
		addText(header, "er");

		node = parse("== t'''est'''er == \nhello");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
	public void headerAndTemplate() {
		TextNode wanted;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		newNode(wanted, "Hello", TextNode.PARAGRAPH_TYPE);
		newNode(wanted, "Test", TextNode.HEADER_TYPE);
		newNode(wanted, "en-noun", TextNode.TEMPLATE_TYPE);

		node = parse("Hello\n\n====Test====\n{{en-noun}}\n");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
	public void horizontalLineAndHeader() {
		TextNode wanted;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		newNode(wanted, TextNode.PARAGRAPH_TYPE);
		newNode(wanted, "test", TextNode.HEADER_TYPE);

		node = parse("----\n== test ==");
		System.out.println("Result: " + result(node.equals(wanted)));
	}

	public void whitespaceAtStart() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		addText(paragraph, "test ");
		newNode(paragraph, "cool", TextNode.STRONG_TYPE);

		node = parse(" test ''' cool'''");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
	public void whitespaceAtEnd() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		addText(paragraph, "test ");
		newNode(paragraph, "cool", TextNode.STRONG_TYPE);

		node = parse("test ''' cool''' ");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
	public void whitespaceBefore() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		addText(paragraph, "test ");
		newNode(paragraph, "cool", TextNode.STRONG_TYPE);

		node = parse("test ''' cool'''");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
	public void whitespaceAfter() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		newNode(paragraph, "cool ", TextNode.STRONG_TYPE);
		addText(paragraph, "test");

		node = parse("'''cool ''' test");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
	public void whitespaceMiddle() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		newNode(paragraph, "cool ", TextNode.STRONG_TYPE);
		newNode(paragraph, "test", TextNode.EM_TYPE);

		node = parse("'''cool ''' '' test''");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
	public void mergeTextStyleMarkups() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		/* verify */
		newNode(paragraph, "cool ", TextNode.STRONG_TYPE);
		newNode(paragraph, "test", TextNode.EM_TYPE);

		node = parse("'''cool ''''' test''");
		System.out.println("Result: " + result(node.equals(wanted)));
	}

	public void quoteAtStartAtTextStyleMarkup() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		newNode(paragraph, "'cool", TextNode.STRONG_TYPE);

		node = parse("''' 'cool '''");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
	public void oneExtraQuoteAtTextStyleMarkup() {
		TextNode wanted, paragraph, em;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		addText(paragraph, "'");
		em = newNode(paragraph, TextNode.EM_TYPE);
		newNode(em, "cool'", TextNode.STRONG_TYPE);
		addText(paragraph, "a");

		node = parse("''''''cool''''''a");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
	public void threeExtraQuoteAtTextStyleMarkup() {
		TextNode wanted, paragraph, em;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		addText(paragraph, "'''");
		em = newNode(paragraph, TextNode.EM_TYPE);
		newNode(em, "cool'''", TextNode.STRONG_TYPE);
		addText(paragraph, "a");

		node = parse("''''''''cool''''''''a");
		System.out.println("Result: " + result(node.equals(wanted)));
	}

	public void unendingTextStyle() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		addText(paragraph, "This is ");
		newNode(paragraph, "so cool", TextNode.STRONG_TYPE);

		node = parse("This is '''so cool");
		System.out.println("Result: " + result(node.equals(wanted)));
	}

	public void multiLineTemplate() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		addText(paragraph, "This is ");
		newNode(paragraph, "so cool", TextNode.TEMPLATE_TYPE, "smallcaps");

		node = parse("This is {{smallcaps\n|so cool\n\n}}");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
	public void multiLineTemplate2() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		addText(paragraph, "This is ");
		newNode(paragraph, "so cool", TextNode.TEMPLATE_TYPE, "smallcaps");

		node = parse("This is {{smallcaps\n|\nso cool\n\n}}");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
	public void multiLineTemplate3() {
		TextNode wanted, paragraph, parentTemplate;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, "hello", TextNode.PARAGRAPH_TYPE);
		paragraph = newNode(wanted, "Derived terms", TextNode.HEADER_TYPE);

		addText(paragraph, "This is ");
		parentTemplate = newNode(wanted, TextNode.TEMPLATE_TYPE, "der3");
		newNode(parentTemplate, TextNode.TEMPLATE_TYPE, "l||catckin");
		newNode(parentTemplate, TextNode.TEMPLATE_TYPE, "l||heat (in a cat)");
		newNode(parentTemplate, TextNode.TEMPLATE_TYPE, "l||pine marten");

		node = parse("{{g\n" +
			"|{{a}}\n" +
			"|{{b}}\n" +
			"|{{c}}\n" +
			"|{{d}}\n" +
			"}}\n==hello==");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
	public void invalidMultiLineTemplate() {
		TextNode wanted;
		wanted = newNode(null, TextNode.ROOT_TYPE);

		newNode(wanted, "This is {{smallcaps a|so cool", TextNode.PARAGRAPH_TYPE);
		newNode(wanted, "}}", TextNode.PARAGRAPH_TYPE);

		node = parse("This is {{smallcaps\na|so cool\n\n}}");
		System.out.println("Result: " + result(node.equals(wanted)));
	}

	public void templateMarkup() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		addText(paragraph, "hello ");
		newNode(paragraph, "world", TextNode.TEMPLATE_TYPE, "smallcaps");
		addText(paragraph, "!");

		node = parse("hello {{smallcaps|world}}!");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
	public void linkMarkup() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		addText(paragraph, "hello ");
		newNode(paragraph, "url", TextNode.LINK_TYPE);
		addText(paragraph, "!");

		node = parse("hello [[url]]!");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
	public void linkMarkupWithName() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		addText(paragraph, "hello ");
		newNode(paragraph, "url", TextNode.LINK_TYPE, "world");
		addText(paragraph, "!");

		node = parse("hello [[url|world]]!");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
	public void linkMarkupToOutside() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		addText(paragraph, "hello ");
		newNode(paragraph, "url", TextNode.LINK_TYPE, "world");
		addText(paragraph, "!");

		node = parse("hello [url|world]!");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
	public void templateWithPartialStrongMarkup() {
		TextNode wanted, paragraph, template;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);
		addText(paragraph, "helloha");

		template = newNode(paragraph, TextNode.TEMPLATE_TYPE, "smallcaps");
		addText(template, "uh");
		newNode(template, "gr", TextNode.STRONG_TYPE);

		addText(paragraph, "eugh");

		node = parse("helloha{{smallcaps|uh'''gr}}eugh");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
	public void templateAndLinkInterleaved() {
		node = parse("hello[[ha{{smallcaps|uh]]gr}}eugh");
	}
	public void linkAndTemplateInterleaved() {
		TextNode wanted;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		newNode(wanted, "hello{{smallcaps|gh[[uh}}gr]]eugh", TextNode.PARAGRAPH_TYPE);

		node = parse("hello{{smallcaps|gh[[uh}}gr]]eugh");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
	public void linkWithExtraBracket() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);
		addText(paragraph, "hello ");
		newNode(paragraph, "world", TextNode.LINK_TYPE);
		addText(paragraph, "] cool");

		node = parse("hello [world]] cool");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
	public void multpleValidLinksAndTemplates() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		addText(paragraph, "hello ");
		newNode(paragraph, "this", TextNode.LINK_TYPE);
		addText(paragraph, " is ");
		addText(paragraph, " complicated wikitext, ");
		newNode(paragraph, "url", TextNode.LINK_TYPE, "which");
		addText(paragraph, " contains ");
		newNode(paragraph, "lots", TextNode.TEMPLATE_TYPE, "smallcaps");
		addText(paragraph, " of ");
		newNode(paragraph, "stuff" + ((char)0) + "gerugh", TextNode.TEMPLATE_TYPE, "smallcaps");

		node = parse("hello [[this]] is [[pretty]] complicated wikitext, [[url|which]] contains {{smallcaps|lots}} of {{smallcaps|stuff|gerugh}}\n");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
	public void listMarkupWithoutSpace() {
		TextNode wanted, list, item;

		wanted = newNode(null, TextNode.ROOT_TYPE);

		list = newNode(wanted, TextNode.LIST_TYPE, "ol");
		item = newNode(list, "test", TextNode.LIST_ITEM_TYPE);

		list = newNode(item, TextNode.LIST_TYPE, "ul");
		newNode(list, "hello", TextNode.LIST_ITEM_TYPE);
		newNode(list, "cool", TextNode.LIST_ITEM_TYPE);

		node = parse("# test\n#* hello\n#* cool");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
	public void doubleUnorderedListMarkupWithoutSpace() {
		TextNode wanted, list, item;

		wanted = newNode(null, TextNode.ROOT_TYPE);

		list = newNode(wanted, TextNode.LIST_TYPE, "ul");
		item = newNode(list, "test", TextNode.LIST_ITEM_TYPE);

		list = newNode(item, TextNode.LIST_TYPE, "ul");
		newNode(list, "hello", TextNode.LIST_ITEM_TYPE);
		newNode(list, "cool", TextNode.LIST_ITEM_TYPE);

		node = parse("* test\n** hello\n** cool");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
	public void listWithTwoLevels() {
		TextNode wanted, list, item;

		wanted = newNode(null, TextNode.ROOT_TYPE);

		list = newNode(wanted, TextNode.LIST_TYPE, "ol");
		newNode(list, "test", TextNode.LIST_ITEM_TYPE);
		newNode(list, "hello", TextNode.LIST_ITEM_TYPE);
		item = newNode(list, "cool", TextNode.LIST_ITEM_TYPE);

		list = newNode(item, TextNode.LIST_TYPE, "ul");
		newNode(list, "test", TextNode.LIST_ITEM_TYPE);
		newNode(list, "cool", TextNode.LIST_ITEM_TYPE);

		node = parse("# test\n# hello\n# cool\n# * test\n# * cool");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
	public void listWithNoOwnItems() {
		TextNode wanted, list, item;

		wanted = newNode(null, TextNode.ROOT_TYPE);

		list = newNode(wanted, TextNode.LIST_TYPE, "ol");
		newNode(list, "test", TextNode.LIST_ITEM_TYPE);
		item = newNode(list, "hello", TextNode.LIST_ITEM_TYPE);

		list = newNode(item, TextNode.LIST_TYPE, "ul");
		item = newNode(list, TextNode.LIST_ITEM_TYPE);

		list = newNode(item, TextNode.LIST_TYPE, "ul");
		newNode(list, "test", TextNode.LIST_ITEM_TYPE);
		newNode(list, "cool", TextNode.LIST_ITEM_TYPE);

		node = parse("# test\n# hello\n# * * test\n# * * cool");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
	public void listWithSublistAtMiddle() {
		TextNode wanted, list, item;

		wanted = newNode(null, TextNode.ROOT_TYPE);

		list = newNode(wanted, TextNode.LIST_TYPE, "ol");
		newNode(list, "test", TextNode.LIST_ITEM_TYPE);
		item = newNode(list, "hello", TextNode.LIST_ITEM_TYPE);
		newNode(list, "cool", TextNode.LIST_ITEM_TYPE);

		list = newNode(item, TextNode.LIST_TYPE, "ul");
		newNode(list, "test", TextNode.LIST_ITEM_TYPE);

		node = parse("# test\n# hello\n# * test\n# cool");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
	public void listTypeChangeAtMiddle() {
		TextNode wanted, list;

		wanted = newNode(null, TextNode.ROOT_TYPE);

		list = newNode(wanted, TextNode.LIST_TYPE, "ul");
		newNode(list, "howdy", TextNode.LIST_ITEM_TYPE);
		newNode(list, "hello", TextNode.LIST_ITEM_TYPE);

		list = newNode(wanted, TextNode.LIST_TYPE, "ol");
		newNode(list, "test", TextNode.LIST_ITEM_TYPE);
		newNode(list, "cool", TextNode.LIST_ITEM_TYPE);

		node = parse("* howdy\n* hello\n# test\n# cool");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
	public void listTypeChangeAtMiddleInsideList() {
		TextNode wanted, mainList, list, item;

		wanted = newNode(null, TextNode.ROOT_TYPE);
		mainList = newNode(wanted, TextNode.LIST_TYPE, "ol");
		item = newNode(mainList, "test", TextNode.LIST_ITEM_TYPE);

		list = newNode(item, TextNode.LIST_TYPE, "ul");
		newNode(list, "howdy", TextNode.LIST_ITEM_TYPE);
		newNode(list, "hello", TextNode.LIST_ITEM_TYPE);

		list = newNode(item, TextNode.LIST_TYPE, "ol");
		newNode(list, "test", TextNode.LIST_ITEM_TYPE);
		newNode(list, "cool", TextNode.LIST_ITEM_TYPE);

		node = parse("# test\n# * howdy\n# * hello\n# # test\n# # cool");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
	public void simpleDefinitionList() {
		TextNode wanted, list;

		wanted = newNode(null, TextNode.ROOT_TYPE);
		list = newNode(wanted, TextNode.LIST_TYPE, "dl");

		newNode(list, "test", TextNode.DEFINITION_LABEL_TYPE);
		newNode(list, "cool", TextNode.DEFINITION_ITEM_TYPE);

		node = parse("; test\n: cool");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
	public void definitionListWithNormalList() {
		TextNode wanted, list, item;

		wanted = newNode(null, TextNode.ROOT_TYPE);
		list = newNode(wanted, TextNode.LIST_TYPE, "dl");
		item = newNode(list, TextNode.DEFINITION_ITEM_TYPE);
		list = newNode(item, TextNode.LIST_TYPE, "ul");

		newNode(list, "test", TextNode.LIST_ITEM_TYPE);
		newNode(list, "cool", TextNode.LIST_ITEM_TYPE);

		node = parse(":* test\n:* cool");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
	public void definitionListAndNormalListMix() {
		TextNode wanted, list, item;

		wanted = newNode(null, TextNode.ROOT_TYPE);
		list = newNode(wanted, TextNode.LIST_TYPE, "ul");
		newNode(list, "test", TextNode.LIST_ITEM_TYPE);

		list = newNode(wanted, TextNode.LIST_TYPE, "dl");
		item = newNode(list, TextNode.DEFINITION_ITEM_TYPE);
		list = newNode(item, TextNode.LIST_TYPE, "ul");
		newNode(list, "cool", TextNode.LIST_ITEM_TYPE);

		System.out.println("");
		node = parse("* test\n:* cool");
		System.out.println("Result: " + result(node.equals(wanted)));
	}
}
