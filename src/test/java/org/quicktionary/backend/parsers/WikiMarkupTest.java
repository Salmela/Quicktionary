/* Quicktionary test
 * Copyright (C) 2015  Aleksi Salmela <aleksi.salmela at helsinki.fi>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quicktionary.backend.parsers;

import java.io.IOException;
import org.junit.Test;
import org.junit.Before;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Assume;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import java.io.StringReader;

import org.quicktionary.backend.TextNode;

public class WikiMarkupTest {
	private WikiMarkup parserWiki;
	private TextNode fragment;

	@Rule
	public Timeout globalTimeout = new Timeout(100);

	public WikiMarkupTest() {
		parserWiki = new WikiMarkup();
	}

	private TextNode parse(String markupString) {
		try {
			parserWiki.parse(new StringReader(markupString));
			return parserWiki.getRoot();
		} catch(IOException e) {
			Assume.assumeTrue(false);
		}
		return null;
	}

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
			textNode.setTextContent(text);
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
		textNode.setTextContent(text);
	}

	@Test
	public void emptyDocument() throws IOException {
		Assert.assertFalse(parserWiki.parse(new StringReader("")));
	}

	@Test
	public void shortDocument() throws IOException {
		Assert.assertTrue(parserWiki.parse(new StringReader("a")));
	}

	@Test
	public void header() {
		TextNode wanted;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		newNode(wanted, "test", TextNode.HEADER_TYPE, "2");

		fragment = parse("==test==\n");
		assertEquals(wanted, fragment);
	}
	@Test
	public void headerWithSpace() {
		TextNode wanted;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		newNode(wanted, "test", TextNode.HEADER_TYPE, "2");

		fragment = parse("== test == \n");
		assertEquals(wanted, fragment);
	}
	@Test
	public void headerAndParagraph() {
		TextNode wanted;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		newNode(wanted, "test", TextNode.HEADER_TYPE, "2");
		newNode(wanted, "hello", TextNode.PARAGRAPH_TYPE);

		fragment = parse("== test == \nhello");
		assertEquals(wanted, fragment);
	}
	@Test
	public void notValidHeader() {
		TextNode wanted;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		newNode(wanted, "== test ==b", TextNode.PARAGRAPH_TYPE);
		newNode(wanted, "hello", TextNode.PARAGRAPH_TYPE);

		fragment = parse("== test ==b\nhello");
		assertEquals(wanted, fragment);
	}
	@Test
	public void notEndingHeader() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, "== te", TextNode.PARAGRAPH_TYPE);
		newNode(paragraph, "st", TextNode.EM_TYPE);
		newNode(wanted, "hello", TextNode.PARAGRAPH_TYPE);

		fragment = parse("== te''st'' \nhello");
		assertEquals(wanted, fragment);
	}
	@Test
	public void headerAndtwoParagraphs() {
		TextNode wanted;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		newNode(wanted, "hello", TextNode.HEADER_TYPE, "2");
		newNode(wanted, "test hello", TextNode.PARAGRAPH_TYPE);
		newNode(wanted, "cool lol", TextNode.PARAGRAPH_TYPE);

		fragment = parse("== hello ==\ntest\nhello\n\ncool\nlol");
		assertEquals(wanted, fragment);
	}
	@Test
	public void headerWithEmAndParagraph() {
		TextNode wanted, header;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		header = newNode(wanted, TextNode.HEADER_TYPE, "2");
		newNode(wanted, "hello", TextNode.PARAGRAPH_TYPE);

		addText(header, "t");
		newNode(header, "est", TextNode.STRONG_TYPE);
		addText(header, "er");

		fragment = parse("== t'''est'''er == \nhello");
		assertEquals(wanted, fragment);
	}
	@Test
	public void headerAndTemplate() {
		TextNode wanted;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		newNode(wanted, "Hello", TextNode.PARAGRAPH_TYPE);
		newNode(wanted, "Test", TextNode.HEADER_TYPE, "4");
		newNode(wanted, "en-noun", TextNode.TEMPLATE_TYPE);

		fragment = parse("Hello\n\n====Test====\n{{en-noun}}\n");
		assertEquals(wanted, fragment);
	}
	@Test
	public void horizontalLineAndHeader() {
		TextNode wanted;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		newNode(wanted, TextNode.RULER_TYPE);
		newNode(wanted, "test", TextNode.HEADER_TYPE, "2");

		fragment = parse("----\n== test ==");
		assertEquals(wanted, fragment);
	}
	@Test
	public void headerAfterList() {
		TextNode wanted, list, item;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		list = newNode(wanted, TextNode.LIST_TYPE, "ol");
		item = newNode(list, "hello", TextNode.LIST_ITEM_TYPE);
		list = newNode(item, TextNode.LIST_TYPE, "ol");
		item = newNode(list, "cool", TextNode.LIST_ITEM_TYPE);
		newNode(wanted, "test", TextNode.HEADER_TYPE, "2");

		fragment = parse("#hello\n##cool\n== test ==");
		assertEquals(wanted, fragment);
	}

	@Test
	public void whitespaceAtStart() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		addText(paragraph, "test ");
		newNode(paragraph, "cool", TextNode.STRONG_TYPE);

		fragment = parse(" test ''' cool'''");
		assertEquals(wanted, fragment);
	}
	@Test
	public void whitespaceAtEnd() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		addText(paragraph, "test ");
		newNode(paragraph, "cool", TextNode.STRONG_TYPE);

		fragment = parse("test ''' cool''' ");
		assertEquals(wanted, fragment);
	}
	@Test
	public void whitespaceBefore() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		addText(paragraph, "test ");
		newNode(paragraph, "cool", TextNode.STRONG_TYPE);

		fragment = parse("test ''' cool'''");
		assertEquals(wanted, fragment);
	}
	@Test
	public void whitespaceAfter() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		newNode(paragraph, "cool ", TextNode.STRONG_TYPE);
		addText(paragraph, "test");

		fragment = parse("'''cool ''' test");
		assertEquals(wanted, fragment);
	}
	@Test
	public void whitespaceMiddle() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		newNode(paragraph, "cool ", TextNode.STRONG_TYPE);
		newNode(paragraph, "test", TextNode.EM_TYPE);

		fragment = parse("'''cool ''' '' test''");
		assertEquals(wanted, fragment);
	}

	@Test
	public void unbalancedQuotes() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		newNode(paragraph, "cool'", TextNode.STRONG_TYPE);

		fragment = parse("'''cool''''");
		assertEquals(wanted, fragment);
	}
	@Test
	public void unbalancedQuotes2() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		addText(paragraph, "\'");
		newNode(paragraph, "cool", TextNode.STRONG_TYPE);

		fragment = parse("''''cool'''");
		assertEquals(wanted, fragment);
	}
	/*@Test
	public void mergeTextStyleMarkups() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		newNode(paragraph, "cool ", TextNode.STRONG_TYPE);
		newNode(paragraph, "test", TextNode.EM_TYPE);

		fragment = parse("'''cool ''''' test''");
		assertEquals(wanted, fragment);
	}

	@Test
	public void quoteAtStartAtTextStyleMarkup() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		newNode(paragraph, "'cool", TextNode.STRONG_TYPE);

		fragment = parse("''' 'cool '''");
		assertEquals(wanted, fragment);
	}*/
	@Test
	public void oneExtraQuoteAtTextStyleMarkup() {
		TextNode wanted, paragraph, em;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		addText(paragraph, "'");
		em = newNode(paragraph, TextNode.EM_TYPE);
		newNode(em, "cool'", TextNode.STRONG_TYPE);
		addText(paragraph, "a");

		fragment = parse("''''''cool''''''a");
		assertEquals(wanted, fragment);
	}
	@Test
	public void threeExtraQuoteAtTextStyleMarkup() {
		TextNode wanted, paragraph, em;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		addText(paragraph, "'''");
		em = newNode(paragraph, TextNode.EM_TYPE);
		newNode(em, "cool'''", TextNode.STRONG_TYPE);
		addText(paragraph, "a");

		fragment = parse("''''''''cool''''''''a");
		assertEquals(wanted, fragment);
	}

	/*@Test
	public void unendingTextStyle() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		addText(paragraph, "This is ");
		newNode(paragraph, "so cool", TextNode.STRONG_TYPE);

		fragment = parse("This is '''so cool");
		assertEquals(wanted, fragment);
	}*/

	/*@Test
	public void multiLineTemplate() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		addText(paragraph, "This is ");
		newNode(paragraph, "so cool", TextNode.TEMPLATE_TYPE, "smallcaps");

		fragment = parse("This is {{smallcaps\n|so cool\n\n}}");
		assertEquals(wanted, fragment);
	}
	@Test
	public void multiLineTemplate2() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		addText(paragraph, "This is ");
		newNode(paragraph, "so cool", TextNode.TEMPLATE_TYPE, "smallcaps");

		fragment = parse("This is {{smallcaps\n|\nso cool\n\n}}");
		assertEquals(wanted, fragment);
	}
	@Test
	public void multiLineTemplate3() {
		TextNode wanted, paragraph, parentTemplate;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, "hello", TextNode.PARAGRAPH_TYPE);
		paragraph = newNode(wanted, "Derived terms", TextNode.HEADER_TYPE, "4");

		addText(paragraph, "This is ");
		parentTemplate = newNode(wanted, TextNode.TEMPLATE_TYPE, "der3");
		newNode(parentTemplate, TextNode.TEMPLATE_TYPE, "l||catckin");
		newNode(parentTemplate, TextNode.TEMPLATE_TYPE, "l||heat (in a cat)");
		newNode(parentTemplate, TextNode.TEMPLATE_TYPE, "l||pine marten");

		fragment = parse("hello\n\n====Derived terms====\n{{der3\n" +
			"|{{l||catkin}}\n" +
			"|{{l||heat (in a cat)}}\n" +
			"|{{l||pine marten}}\n" +
			"}}");
		assertEquals(wanted, fragment);
	}
	@Test
	public void invalidMultiLineTemplate() {
		TextNode wanted;
		wanted = newNode(null, TextNode.ROOT_TYPE);

		newNode(wanted, "This is {{smallcaps a|so cool", TextNode.PARAGRAPH_TYPE);
		newNode(wanted, "}}", TextNode.PARAGRAPH_TYPE);

		fragment = parse("This is {{smallcaps\na|so cool\n\n}}");
		assertEquals(wanted, fragment);
	}

	@Test
	public void templateMarkup() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		addText(paragraph, "hello ");
		newNode(paragraph, "world", TextNode.TEMPLATE_TYPE, "smallcaps");
		addText(paragraph, "!");

		fragment = parse("hello {{smallcaps|world}}!");
		assertEquals(wanted, fragment);
	}*/
	@Test
	public void linkMarkup() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		addText(paragraph, "hello ");
		newNode(paragraph, "url", TextNode.LINK_TYPE);
		addText(paragraph, "!");

		fragment = parse("hello [[url]]!");
		assertEquals(wanted, fragment);
	}
	@Test
	public void templateLonelyEndMarkup() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		addText(paragraph, "hello }} a");

		fragment = parse("hello }} a");
		assertEquals(wanted, fragment);
	}
	/*@Test
	public void linkMarkupWithName() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		addText(paragraph, "hello ");
		newNode(paragraph, "url", TextNode.LINK_TYPE, "world");
		addText(paragraph, "!");

		fragment = parse("hello [[url|world]]!");
		assertEquals(wanted, fragment);
	}
	@Test
	public void linkMarkupToOutside() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);

		addText(paragraph, "hello ");
		newNode(paragraph, "url", TextNode.LINK_TYPE, "world");
		addText(paragraph, "!");

		fragment = parse("hello [url|world]!");
		assertEquals(wanted, fragment);
	}
	@Test
	public void templateWithPartialStrongMarkup() {
		TextNode wanted, paragraph, template;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);
		addText(paragraph, "helloha");

		template = newNode(paragraph, TextNode.TEMPLATE_TYPE, "smallcaps");
		addText(template, "uh");
		newNode(template, "gr", TextNode.STRONG_TYPE);

		addText(paragraph, "eugh");

		fragment = parse("helloha{{smallcaps|uh'''gr}}eugh");
		assertEquals(wanted, fragment);
	}*/
	@Test
	public void templateAndLinkInterleaved() {
		fragment = parse("hello[[ha{{smallcaps|uh]]gr}}eugh");
	}
	@Test
	public void linkAndTemplateInterleaved() {
		TextNode wanted;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		newNode(wanted, "hello{{smallcaps|gh[[uh}}gr]]eugh", TextNode.PARAGRAPH_TYPE);

		fragment = parse("hello{{smallcaps|gh[[uh}}gr]]eugh");
		assertEquals(wanted, fragment);
	}
	@Test
	public void linkWithExtraBracket() {
		TextNode wanted, paragraph;
		wanted = newNode(null, TextNode.ROOT_TYPE);
		paragraph = newNode(wanted, TextNode.PARAGRAPH_TYPE);
		addText(paragraph, "hello ");
		newNode(paragraph, "world", TextNode.LINK_TYPE);
		addText(paragraph, "] cool");

		fragment = parse("hello [world]] cool");
		assertEquals(wanted, fragment);
	}
	/*@Test
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

		fragment = parse("hello [[this]] is [[pretty]] complicated wikitext, [[url|which]] contains {{smallcaps|lots}} of {{smallcaps|stuff|gerugh}}\n");
		assertEquals(wanted, fragment);
	}*/
	@Test
	public void listMarkupWithoutSpace() {
		TextNode wanted, list, item;

		wanted = newNode(null, TextNode.ROOT_TYPE);

		list = newNode(wanted, TextNode.LIST_TYPE, "ol");
		item = newNode(list, "test", TextNode.LIST_ITEM_TYPE);

		list = newNode(item, TextNode.LIST_TYPE, "ul");
		newNode(list, "hello", TextNode.LIST_ITEM_TYPE);
		newNode(list, "cool", TextNode.LIST_ITEM_TYPE);

		fragment = parse("# test\n#* hello\n#* cool");
		assertEquals(wanted, fragment);
	}
	@Test
	public void doubleUnorderedListMarkupWithoutSpace() {
		TextNode wanted, list, item;

		wanted = newNode(null, TextNode.ROOT_TYPE);

		list = newNode(wanted, TextNode.LIST_TYPE, "ul");
		item = newNode(list, "test", TextNode.LIST_ITEM_TYPE);

		list = newNode(item, TextNode.LIST_TYPE, "ul");
		newNode(list, "hello", TextNode.LIST_ITEM_TYPE);
		newNode(list, "cool", TextNode.LIST_ITEM_TYPE);

		fragment = parse("* test\n** hello\n** cool");
		assertEquals(wanted, fragment);
	}
	@Test
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

		fragment = parse("# test\n# hello\n# cool\n# * test\n# * cool");
		assertEquals(wanted, fragment);
	}
	@Test
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

		fragment = parse("# test\n# hello\n# * * test\n# * * cool");
		assertEquals(wanted, fragment);
	}
	@Test
	public void listWithSublistAtMiddle() {
		TextNode wanted, list, item;

		wanted = newNode(null, TextNode.ROOT_TYPE);

		list = newNode(wanted, TextNode.LIST_TYPE, "ol");
		newNode(list, "test", TextNode.LIST_ITEM_TYPE);
		item = newNode(list, "hello", TextNode.LIST_ITEM_TYPE);
		newNode(list, "cool", TextNode.LIST_ITEM_TYPE);

		list = newNode(item, TextNode.LIST_TYPE, "ul");
		newNode(list, "test", TextNode.LIST_ITEM_TYPE);

		fragment = parse("# test\n# hello\n# * test\n# cool");
		assertEquals(wanted, fragment);
	}
	@Test
	public void listTypeChangeAtMiddle() {
		TextNode wanted, list;

		wanted = newNode(null, TextNode.ROOT_TYPE);

		list = newNode(wanted, TextNode.LIST_TYPE, "ul");
		newNode(list, "howdy", TextNode.LIST_ITEM_TYPE);
		newNode(list, "hello", TextNode.LIST_ITEM_TYPE);

		list = newNode(wanted, TextNode.LIST_TYPE, "ol");
		newNode(list, "test", TextNode.LIST_ITEM_TYPE);
		newNode(list, "cool", TextNode.LIST_ITEM_TYPE);

		fragment = parse("* howdy\n* hello\n# test\n# cool");
		assertEquals(wanted, fragment);
	}
	@Test
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

		fragment = parse("# test\n# * howdy\n# * hello\n# # test\n# # cool");
		assertEquals(wanted, fragment);
	}
}
