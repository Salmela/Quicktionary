package org.quicktionary.gui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import org.quicktionary.backend.parsers.XMLParser;
import org.quicktionary.backend.parsers.WikiMarkup;
import org.quicktionary.backend.parsers.WikiMarkup.TextFragment;

/*
 * this file is for debugging the test cases.
 */
public class Test {
	private XMLParser  parserXML;
	private WikiMarkup parserWiki;
	private TextFragment fragment;

	String xmlDecl = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";

	public Test() {
		parserXML = new XMLParser();
		parserWiki = new WikiMarkup();

		xmlParserTest();

		headerAndParagraph();
		twoParagraphs();

		templateWithPartialStrongMarkup();
		templateAndLinkInterleaved();
		linkAndTemplateInterleaved();
		linkWithExtraBracket();
		multpleValidLinksAndTemplates();

		listMarkupWithoutSpace();
		doubleUnorderedListMarkupWithoutSpace();

		listWithTwoLevels();
		listWithNoOwnItems();
		listWithSublistAtMiddle();
		listTypeChangeAtMiddle();
		listTypeChangeAtMiddleInsideList();
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

	public TextFragment parse(String wikiText) {
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
		TextFragment wanted, node;
		wanted = parserWiki.new TextFragment(TextFragment.ROOT_TYPE);
		wanted.appendChild(parserWiki.new TextFragment(TextFragment.HEADER_TYPE));
		wanted.appendChild(parserWiki.new TextFragment(TextFragment.PARAGRAPH_TYPE));

		fragment = parse("== t'''est''' == \nhello");
		System.out.println("Result: " + fragment.equals(wanted));
	}

	/* WikiMarkup */
	public void headerAndParagraph() {
		TextFragment wanted, node;
		wanted = parserWiki.new TextFragment(TextFragment.ROOT_TYPE);
		wanted.appendChild(parserWiki.new TextFragment(TextFragment.HEADER_TYPE));
		wanted.appendChild(parserWiki.new TextFragment(TextFragment.PARAGRAPH_TYPE));

		fragment = parse("== t'''est''' == \nhello");
		System.out.println("Result: " + fragment.equals(wanted));
	}
	public void templateWithPartialStrongMarkup() {
		fragment = parse("helloha{{lgh|uh'''gr}}eugh");
	}
	public void templateAndLinkInterleaved() {
		fragment = parse("hello[[ha{{lgh|uh]]gr}}eugh");
	}
	public void linkAndTemplateInterleaved() {
		fragment = parse("hello{{hal|gh[[uh}}gr]]eugh");
	}
	public void linkWithExtraBracket() {
		fragment = parse("hello [uhgr]] eugh");
	}
	public void multpleValidLinksAndTemplates() {
		fragment = parse("hello [[uhgr]] eugh [[ufhwuf]] heuw hgf [[algh|gjrehrgu]] hwfewf {ugeu|wgf} ehgg {fuwh|jrgh|gerugh}\n");
	}
	public void listMarkupWithoutSpace() {
		TextFragment wanted, list, item;

		wanted = parserWiki.new TextFragment(TextFragment.ROOT_TYPE);

		list = wanted.appendChild(parserWiki.new TextFragment(TextFragment.LIST_TYPE, "ol"));
		item = list.appendChild(parserWiki.new TextFragment(TextFragment.LIST_ITEM_TYPE));

		list = item.appendChild(parserWiki.new TextFragment(TextFragment.LIST_TYPE, "ul"));
		list.appendChild(parserWiki.new TextFragment(TextFragment.LIST_ITEM_TYPE));
		list.appendChild(parserWiki.new TextFragment(TextFragment.LIST_ITEM_TYPE));

		fragment = parse("# test\n#* hello\n#* cool");
		System.out.println("Result: " + fragment.equals(wanted));
	}
	public void doubleUnorderedListMarkupWithoutSpace() {
		TextFragment wanted, list, item;

		wanted = parserWiki.new TextFragment(TextFragment.ROOT_TYPE);

		list = wanted.appendChild(parserWiki.new TextFragment(TextFragment.LIST_TYPE, "ul"));
		item = list.appendChild(parserWiki.new TextFragment(TextFragment.LIST_ITEM_TYPE));

		list = item.appendChild(parserWiki.new TextFragment(TextFragment.LIST_TYPE, "ul"));
		list.appendChild(parserWiki.new TextFragment(TextFragment.LIST_ITEM_TYPE));
		list.appendChild(parserWiki.new TextFragment(TextFragment.LIST_ITEM_TYPE));

		fragment = parse("* test\n** hello\n** cool");
		System.out.println("Result: " + fragment.equals(wanted));
	}
	public void listWithTwoLevels() {
		TextFragment wanted, list, item;

		wanted = parserWiki.new TextFragment(TextFragment.ROOT_TYPE);
		wanted.appendChild(parserWiki.new TextFragment(TextFragment.HEADER_TYPE));

		list = wanted.appendChild(parserWiki.new TextFragment(TextFragment.LIST_TYPE, "ol"));
		list.appendChild(parserWiki.new TextFragment(TextFragment.LIST_ITEM_TYPE));
		list.appendChild(parserWiki.new TextFragment(TextFragment.LIST_ITEM_TYPE));
		item = list.appendChild(parserWiki.new TextFragment(TextFragment.LIST_ITEM_TYPE));

		list = item.appendChild(parserWiki.new TextFragment(TextFragment.LIST_TYPE, "ul"));
		list.appendChild(parserWiki.new TextFragment(TextFragment.LIST_ITEM_TYPE));
		list.appendChild(parserWiki.new TextFragment(TextFragment.LIST_ITEM_TYPE));

		fragment = parse("== hello ==\n# test\n# hello\n# cool\n# * test\n# * cool");
		System.out.println("Result: " + fragment.equals(wanted));
	}
	public void listWithNoOwnItems() {
		TextFragment wanted, list, item;

		wanted = parserWiki.new TextFragment(TextFragment.ROOT_TYPE);
		wanted.appendChild(parserWiki.new TextFragment(TextFragment.HEADER_TYPE));

		list = wanted.appendChild(parserWiki.new TextFragment(TextFragment.LIST_TYPE, "ol"));
		list.appendChild(parserWiki.new TextFragment(TextFragment.LIST_ITEM_TYPE));
		item = list.appendChild(parserWiki.new TextFragment(TextFragment.LIST_ITEM_TYPE));

		list = item.appendChild(parserWiki.new TextFragment(TextFragment.LIST_TYPE, "ul"));
		item = list.appendChild(parserWiki.new TextFragment(TextFragment.LIST_ITEM_TYPE));

		list = item.appendChild(parserWiki.new TextFragment(TextFragment.LIST_TYPE, "ul"));
		list.appendChild(parserWiki.new TextFragment(TextFragment.LIST_ITEM_TYPE));
		list.appendChild(parserWiki.new TextFragment(TextFragment.LIST_ITEM_TYPE));

		fragment = parse("== hello ==\n# test\n# hello\n# * * test\n# * * cool");
		System.out.println("Result: " + fragment.equals(wanted));
	}
	public void listWithSublistAtMiddle() {
		TextFragment wanted, list, item;

		wanted = parserWiki.new TextFragment(TextFragment.ROOT_TYPE);

		list = wanted.appendChild(parserWiki.new TextFragment(TextFragment.LIST_TYPE, "ol"));
		list.appendChild(parserWiki.new TextFragment(TextFragment.LIST_ITEM_TYPE));
		item = list.appendChild(parserWiki.new TextFragment(TextFragment.LIST_ITEM_TYPE));
		list.appendChild(parserWiki.new TextFragment(TextFragment.LIST_ITEM_TYPE));

		list = item.appendChild(parserWiki.new TextFragment(TextFragment.LIST_TYPE, "ul"));
		list.appendChild(parserWiki.new TextFragment(TextFragment.LIST_ITEM_TYPE));

		fragment = parse("# test\n# hello\n# * test\n# cool");
		System.out.println("Result: " + fragment.equals(wanted));
	}
	public void listTypeChangeAtMiddle() {
		TextFragment wanted, list;

		wanted = parserWiki.new TextFragment(TextFragment.ROOT_TYPE);

		list = wanted.appendChild(parserWiki.new TextFragment(TextFragment.LIST_TYPE, "ul"));
		list.appendChild(parserWiki.new TextFragment(TextFragment.LIST_ITEM_TYPE));
		list.appendChild(parserWiki.new TextFragment(TextFragment.LIST_ITEM_TYPE));

		list = wanted.appendChild(parserWiki.new TextFragment(TextFragment.LIST_TYPE, "ol"));
		list.appendChild(parserWiki.new TextFragment(TextFragment.LIST_ITEM_TYPE));
		list.appendChild(parserWiki.new TextFragment(TextFragment.LIST_ITEM_TYPE));

		fragment = parse("* howdy\n* hello\n# test\n# cool");
		System.out.println("Result: " + fragment.equals(wanted));
	}
	public void listTypeChangeAtMiddleInsideList() {
		TextFragment wanted, mainList, list, item;

		wanted = parserWiki.new TextFragment(TextFragment.ROOT_TYPE);
		mainList = wanted.appendChild(parserWiki.new TextFragment(TextFragment.LIST_TYPE, "ol"));
		item = mainList.appendChild(parserWiki.new TextFragment(TextFragment.LIST_ITEM_TYPE));

		list = item.appendChild(parserWiki.new TextFragment(TextFragment.LIST_TYPE, "ul"));
		list.appendChild(parserWiki.new TextFragment(TextFragment.LIST_ITEM_TYPE));
		list.appendChild(parserWiki.new TextFragment(TextFragment.LIST_ITEM_TYPE));

		list = item.appendChild(parserWiki.new TextFragment(TextFragment.LIST_TYPE, "ol"));
		list.appendChild(parserWiki.new TextFragment(TextFragment.LIST_ITEM_TYPE));
		list.appendChild(parserWiki.new TextFragment(TextFragment.LIST_ITEM_TYPE));

		fragment = parse("# test\n# * howdy\n# * hello\n# # test\n# # cool");
		System.out.println("Result: " + fragment.equals(wanted));
	}
	public void twoParagraphs() {
		TextFragment wanted, node;
		wanted = parserWiki.new TextFragment(TextFragment.ROOT_TYPE);
		wanted.appendChild(parserWiki.new TextFragment(TextFragment.HEADER_TYPE));
		wanted.appendChild(parserWiki.new TextFragment(TextFragment.PARAGRAPH_TYPE));
		wanted.appendChild(parserWiki.new TextFragment(TextFragment.PARAGRAPH_TYPE));

		fragment = parse("== hello ==\ntest\nhello\n\ncool\nlol");
		System.out.println("Result: " + fragment.equals(wanted));
	}
}
