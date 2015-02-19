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

import static org.quicktionary.backend.parsers.WikiMarkup.TextFragment;

public class WikiMarkupTest {
	private WikiMarkup parserWiki;
	private TextFragment fragment;

	@Rule
	public Timeout globalTimeout = new Timeout(100);

	public WikiMarkupTest() {
		parserWiki = new WikiMarkup();
	}

	private TextFragment parse(String markupString) {
		try {
			parserWiki.parse(new StringReader(markupString));
			return parserWiki.getRoot();
		} catch(IOException e) {
			Assume.assumeTrue(false);
		}
		return null;
	}

	private TextFragment newNode(TextFragment parent, String text, int type, String parameter) {
		TextFragment node;
		if(parent == null) {
			node = new TextFragment(type, parameter);
		} else {
			node = parent.appendChild(new TextFragment(type, parameter));
		}
		if(text != null) {
			TextFragment textNode;
			textNode = new TextFragment(TextFragment.PLAIN_TYPE);
			node.appendChild(textNode);
			textNode.setContent(text);
		}
		return node;
	}
	private TextFragment newNode(TextFragment parent, int type, String parameter) {
		return newNode(parent, null, type, parameter);
	}
	private TextFragment newNode(TextFragment parent, int type) {
		return newNode(parent, null, type, null);
	}
	private TextFragment newNode(TextFragment parent, String text, int type) {
		return newNode(parent, text, type, null);
	}
	private void addText(TextFragment parent, String text) {
		TextFragment textNode;

		if(parent == null) {
			throw new Error("Parent must be set");
		}

		textNode = new TextFragment(TextFragment.PLAIN_TYPE);
		parent.appendChild(textNode);
		textNode.setContent(text);
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
		TextFragment wanted;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		newNode(wanted, "test", TextFragment.HEADER_TYPE);

		fragment = parse("==test==\n");
		assertEquals(wanted, fragment);
	}
	@Test
	public void headerWithSpace() {
		TextFragment wanted;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		newNode(wanted, "test", TextFragment.HEADER_TYPE);

		fragment = parse("== test == \n");
		assertEquals(wanted, fragment);
	}
	@Test
	public void headerAndParagraph() {
		TextFragment wanted;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		newNode(wanted, "test", TextFragment.HEADER_TYPE);
		newNode(wanted, "hello", TextFragment.PARAGRAPH_TYPE);

		fragment = parse("== test == \nhello");
		assertEquals(wanted, fragment);
	}
	@Test
	public void notValidHeader() {
		TextFragment wanted;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		newNode(wanted, "== test ==b", TextFragment.PARAGRAPH_TYPE);
		newNode(wanted, "hello", TextFragment.PARAGRAPH_TYPE);

		fragment = parse("== test ==b\nhello");
		assertEquals(wanted, fragment);
	}
	@Test
	public void notEndingHeader() {
		TextFragment wanted, paragraph;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		paragraph = newNode(wanted, "== te", TextFragment.PARAGRAPH_TYPE);
		newNode(paragraph, "st", TextFragment.EM_TYPE);
		newNode(wanted, "hello", TextFragment.PARAGRAPH_TYPE);

		fragment = parse("== te''st'' \nhello");
		assertEquals(wanted, fragment);
	}
	@Test
	public void headerAndtwoParagraphs() {
		TextFragment wanted;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		newNode(wanted, "hello", TextFragment.HEADER_TYPE);
		newNode(wanted, "test hello", TextFragment.PARAGRAPH_TYPE);
		newNode(wanted, "cool lol", TextFragment.PARAGRAPH_TYPE);

		fragment = parse("== hello ==\ntest\nhello\n\ncool\nlol");
		assertEquals(wanted, fragment);
	}
	@Test
	public void headerWithEmAndParagraph() {
		TextFragment wanted, header;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		header = newNode(wanted, TextFragment.HEADER_TYPE);
		newNode(wanted, "hello", TextFragment.PARAGRAPH_TYPE);

		addText(header, "t");
		newNode(header, "est", TextFragment.STRONG_TYPE);
		addText(header, "er");

		fragment = parse("== t'''est'''er == \nhello");
		assertEquals(wanted, fragment);
	}
	@Test
	public void headerAndTemplate() {
		TextFragment wanted;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		newNode(wanted, "Hello", TextFragment.PARAGRAPH_TYPE);
		newNode(wanted, "Test", TextFragment.HEADER_TYPE);
		newNode(wanted, "en-noun", TextFragment.TEMPLATE_TYPE);

		fragment = parse("Hello\n\n====Test====\n{{en-noun}}\n");
		assertEquals(wanted, fragment);
	}
	/*@Test
	public void horizontalLineAndHeader() {
		TextFragment wanted;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		newNode(wanted, TextFragment.PARAGRAPH_TYPE);
		newNode(wanted, "test", TextFragment.HEADER_TYPE);

		fragment = parse("----\n== test ==");
		assertEquals(wanted, fragment);
	}*/
	@Test
	public void headerAfterList() {
		TextFragment wanted, list, item;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		list = newNode(wanted, TextFragment.LIST_TYPE, "ol");
		item = newNode(list, "hello", TextFragment.LIST_ITEM_TYPE);
		list = newNode(item, TextFragment.LIST_TYPE, "ol");
		item = newNode(list, "cool", TextFragment.LIST_ITEM_TYPE);
		newNode(wanted, "test", TextFragment.HEADER_TYPE);

		fragment = parse("#hello\n##cool\n== test ==");
		assertEquals(wanted, fragment);
	}

	@Test
	public void whitespaceAtStart() {
		TextFragment wanted, paragraph;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		paragraph = newNode(wanted, TextFragment.PARAGRAPH_TYPE);

		addText(paragraph, "test ");
		newNode(paragraph, "cool", TextFragment.STRONG_TYPE);

		fragment = parse(" test ''' cool'''");
		assertEquals(wanted, fragment);
	}
	@Test
	public void whitespaceAtEnd() {
		TextFragment wanted, paragraph;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		paragraph = newNode(wanted, TextFragment.PARAGRAPH_TYPE);

		addText(paragraph, "test ");
		newNode(paragraph, "cool", TextFragment.STRONG_TYPE);

		fragment = parse("test ''' cool''' ");
		assertEquals(wanted, fragment);
	}
	@Test
	public void whitespaceBefore() {
		TextFragment wanted, paragraph;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		paragraph = newNode(wanted, TextFragment.PARAGRAPH_TYPE);

		addText(paragraph, "test ");
		newNode(paragraph, "cool", TextFragment.STRONG_TYPE);

		fragment = parse("test ''' cool'''");
		assertEquals(wanted, fragment);
	}
	@Test
	public void whitespaceAfter() {
		TextFragment wanted, paragraph;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		paragraph = newNode(wanted, TextFragment.PARAGRAPH_TYPE);

		newNode(paragraph, "cool ", TextFragment.STRONG_TYPE);
		addText(paragraph, "test");

		fragment = parse("'''cool ''' test");
		assertEquals(wanted, fragment);
	}
	@Test
	public void whitespaceMiddle() {
		TextFragment wanted, paragraph;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		paragraph = newNode(wanted, TextFragment.PARAGRAPH_TYPE);

		newNode(paragraph, "cool ", TextFragment.STRONG_TYPE);
		newNode(paragraph, "test", TextFragment.EM_TYPE);

		fragment = parse("'''cool ''' '' test''");
		assertEquals(wanted, fragment);
	}

	@Test
	public void unbalancedQuotes() {
		TextFragment wanted, paragraph;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		paragraph = newNode(wanted, TextFragment.PARAGRAPH_TYPE);

		newNode(paragraph, "cool'", TextFragment.STRONG_TYPE);

		fragment = parse("'''cool''''");
		assertEquals(wanted, fragment);
	}
	@Test
	public void unbalancedQuotes2() {
		TextFragment wanted, paragraph;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		paragraph = newNode(wanted, TextFragment.PARAGRAPH_TYPE);

		addText(paragraph, "\'");
		newNode(paragraph, "cool", TextFragment.STRONG_TYPE);

		fragment = parse("''''cool'''");
		assertEquals(wanted, fragment);
	}
	/*@Test
	public void mergeTextStyleMarkups() {
		TextFragment wanted, paragraph;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		paragraph = newNode(wanted, TextFragment.PARAGRAPH_TYPE);

		newNode(paragraph, "cool ", TextFragment.STRONG_TYPE);
		newNode(paragraph, "test", TextFragment.EM_TYPE);

		fragment = parse("'''cool ''''' test''");
		assertEquals(wanted, fragment);
	}

	@Test
	public void quoteAtStartAtTextStyleMarkup() {
		TextFragment wanted, paragraph;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		paragraph = newNode(wanted, TextFragment.PARAGRAPH_TYPE);

		newNode(paragraph, "'cool", TextFragment.STRONG_TYPE);

		fragment = parse("''' 'cool '''");
		assertEquals(wanted, fragment);
	}*/
	@Test
	public void oneExtraQuoteAtTextStyleMarkup() {
		TextFragment wanted, paragraph, em;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		paragraph = newNode(wanted, TextFragment.PARAGRAPH_TYPE);

		addText(paragraph, "'");
		em = newNode(paragraph, TextFragment.EM_TYPE);
		newNode(em, "cool'", TextFragment.STRONG_TYPE);
		addText(paragraph, "a");

		fragment = parse("''''''cool''''''a");
		assertEquals(wanted, fragment);
	}
	@Test
	public void threeExtraQuoteAtTextStyleMarkup() {
		TextFragment wanted, paragraph, em;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		paragraph = newNode(wanted, TextFragment.PARAGRAPH_TYPE);

		addText(paragraph, "'''");
		em = newNode(paragraph, TextFragment.EM_TYPE);
		newNode(em, "cool'''", TextFragment.STRONG_TYPE);
		addText(paragraph, "a");

		fragment = parse("''''''''cool''''''''a");
		assertEquals(wanted, fragment);
	}

	/*@Test
	public void unendingTextStyle() {
		TextFragment wanted, paragraph;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		paragraph = newNode(wanted, TextFragment.PARAGRAPH_TYPE);

		addText(paragraph, "This is ");
		newNode(paragraph, "so cool", TextFragment.STRONG_TYPE);

		fragment = parse("This is '''so cool");
		assertEquals(wanted, fragment);
	}*/

	/*@Test
	public void multiLineTemplate() {
		TextFragment wanted, paragraph;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		paragraph = newNode(wanted, TextFragment.PARAGRAPH_TYPE);

		addText(paragraph, "This is ");
		newNode(paragraph, "so cool", TextFragment.TEMPLATE_TYPE, "smallcaps");

		fragment = parse("This is {{smallcaps\n|so cool\n\n}}");
		assertEquals(wanted, fragment);
	}
	@Test
	public void multiLineTemplate2() {
		TextFragment wanted, paragraph;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		paragraph = newNode(wanted, TextFragment.PARAGRAPH_TYPE);

		addText(paragraph, "This is ");
		newNode(paragraph, "so cool", TextFragment.TEMPLATE_TYPE, "smallcaps");

		fragment = parse("This is {{smallcaps\n|\nso cool\n\n}}");
		assertEquals(wanted, fragment);
	}
	@Test
	public void multiLineTemplate3() {
		TextFragment wanted, paragraph, parentTemplate;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		paragraph = newNode(wanted, "hello", TextFragment.PARAGRAPH_TYPE);
		paragraph = newNode(wanted, "Derived terms", TextFragment.HEADER_TYPE);

		addText(paragraph, "This is ");
		parentTemplate = newNode(wanted, TextFragment.TEMPLATE_TYPE, "der3");
		newNode(parentTemplate, TextFragment.TEMPLATE_TYPE, "l||catckin");
		newNode(parentTemplate, TextFragment.TEMPLATE_TYPE, "l||heat (in a cat)");
		newNode(parentTemplate, TextFragment.TEMPLATE_TYPE, "l||pine marten");

		fragment = parse("hello\n\n====Derived terms====\n{{der3\n" +
			"|{{l||catkin}}\n" +
			"|{{l||heat (in a cat)}}\n" +
			"|{{l||pine marten}}\n" +
			"}}");
		assertEquals(wanted, fragment);
	}
	@Test
	public void invalidMultiLineTemplate() {
		TextFragment wanted;
		wanted = newNode(null, TextFragment.ROOT_TYPE);

		newNode(wanted, "This is {{smallcaps a|so cool", TextFragment.PARAGRAPH_TYPE);
		newNode(wanted, "}}", TextFragment.PARAGRAPH_TYPE);

		fragment = parse("This is {{smallcaps\na|so cool\n\n}}");
		assertEquals(wanted, fragment);
	}

	@Test
	public void templateMarkup() {
		TextFragment wanted, paragraph;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		paragraph = newNode(wanted, TextFragment.PARAGRAPH_TYPE);

		addText(paragraph, "hello ");
		newNode(paragraph, "world", TextFragment.TEMPLATE_TYPE, "smallcaps");
		addText(paragraph, "!");

		fragment = parse("hello {{smallcaps|world}}!");
		assertEquals(wanted, fragment);
	}*/
	@Test
	public void linkMarkup() {
		TextFragment wanted, paragraph;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		paragraph = newNode(wanted, TextFragment.PARAGRAPH_TYPE);

		addText(paragraph, "hello ");
		newNode(paragraph, "url", TextFragment.LINK_TYPE);
		addText(paragraph, "!");

		fragment = parse("hello [[url]]!");
		assertEquals(wanted, fragment);
	}
	@Test
	public void templateLonelyEndMarkup() {
		TextFragment wanted, paragraph;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		paragraph = newNode(wanted, TextFragment.PARAGRAPH_TYPE);

		addText(paragraph, "hello }} a");

		fragment = parse("hello }} a");
		assertEquals(wanted, fragment);
	}
	/*@Test
	public void linkMarkupWithName() {
		TextFragment wanted, paragraph;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		paragraph = newNode(wanted, TextFragment.PARAGRAPH_TYPE);

		addText(paragraph, "hello ");
		newNode(paragraph, "url", TextFragment.LINK_TYPE, "world");
		addText(paragraph, "!");

		fragment = parse("hello [[url|world]]!");
		assertEquals(wanted, fragment);
	}
	@Test
	public void linkMarkupToOutside() {
		TextFragment wanted, paragraph;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		paragraph = newNode(wanted, TextFragment.PARAGRAPH_TYPE);

		addText(paragraph, "hello ");
		newNode(paragraph, "url", TextFragment.LINK_TYPE, "world");
		addText(paragraph, "!");

		fragment = parse("hello [url|world]!");
		assertEquals(wanted, fragment);
	}
	@Test
	public void templateWithPartialStrongMarkup() {
		TextFragment wanted, paragraph, template;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		paragraph = newNode(wanted, TextFragment.PARAGRAPH_TYPE);
		addText(paragraph, "helloha");

		template = newNode(paragraph, TextFragment.TEMPLATE_TYPE, "smallcaps");
		addText(template, "uh");
		newNode(template, "gr", TextFragment.STRONG_TYPE);

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
		TextFragment wanted;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		newNode(wanted, "hello{{smallcaps|gh[[uh}}gr]]eugh", TextFragment.PARAGRAPH_TYPE);

		fragment = parse("hello{{smallcaps|gh[[uh}}gr]]eugh");
		assertEquals(wanted, fragment);
	}
	@Test
	public void linkWithExtraBracket() {
		TextFragment wanted, paragraph;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		paragraph = newNode(wanted, TextFragment.PARAGRAPH_TYPE);
		addText(paragraph, "hello ");
		newNode(paragraph, "world", TextFragment.LINK_TYPE);
		addText(paragraph, "] cool");

		fragment = parse("hello [world]] cool");
		assertEquals(wanted, fragment);
	}
	/*@Test
	public void multpleValidLinksAndTemplates() {
		TextFragment wanted, paragraph;
		wanted = newNode(null, TextFragment.ROOT_TYPE);
		paragraph = newNode(wanted, TextFragment.PARAGRAPH_TYPE);

		addText(paragraph, "hello ");
		newNode(paragraph, "this", TextFragment.LINK_TYPE);
		addText(paragraph, " is ");
		addText(paragraph, " complicated wikitext, ");
		newNode(paragraph, "url", TextFragment.LINK_TYPE, "which");
		addText(paragraph, " contains ");
		newNode(paragraph, "lots", TextFragment.TEMPLATE_TYPE, "smallcaps");
		addText(paragraph, " of ");
		newNode(paragraph, "stuff" + ((char)0) + "gerugh", TextFragment.TEMPLATE_TYPE, "smallcaps");

		fragment = parse("hello [[this]] is [[pretty]] complicated wikitext, [[url|which]] contains {{smallcaps|lots}} of {{smallcaps|stuff|gerugh}}\n");
		assertEquals(wanted, fragment);
	}*/
	@Test
	public void listMarkupWithoutSpace() {
		TextFragment wanted, list, item;

		wanted = newNode(null, TextFragment.ROOT_TYPE);

		list = newNode(wanted, TextFragment.LIST_TYPE, "ol");
		item = newNode(list, "test", TextFragment.LIST_ITEM_TYPE);

		list = newNode(item, TextFragment.LIST_TYPE, "ul");
		newNode(list, "hello", TextFragment.LIST_ITEM_TYPE);
		newNode(list, "cool", TextFragment.LIST_ITEM_TYPE);

		fragment = parse("# test\n#* hello\n#* cool");
		assertEquals(wanted, fragment);
	}
	@Test
	public void doubleUnorderedListMarkupWithoutSpace() {
		TextFragment wanted, list, item;

		wanted = newNode(null, TextFragment.ROOT_TYPE);

		list = newNode(wanted, TextFragment.LIST_TYPE, "ul");
		item = newNode(list, "test", TextFragment.LIST_ITEM_TYPE);

		list = newNode(item, TextFragment.LIST_TYPE, "ul");
		newNode(list, "hello", TextFragment.LIST_ITEM_TYPE);
		newNode(list, "cool", TextFragment.LIST_ITEM_TYPE);

		fragment = parse("* test\n** hello\n** cool");
		assertEquals(wanted, fragment);
	}
	@Test
	public void listWithTwoLevels() {
		TextFragment wanted, list, item;

		wanted = newNode(null, TextFragment.ROOT_TYPE);

		list = newNode(wanted, TextFragment.LIST_TYPE, "ol");
		newNode(list, "test", TextFragment.LIST_ITEM_TYPE);
		newNode(list, "hello", TextFragment.LIST_ITEM_TYPE);
		item = newNode(list, "cool", TextFragment.LIST_ITEM_TYPE);

		list = newNode(item, TextFragment.LIST_TYPE, "ul");
		newNode(list, "test", TextFragment.LIST_ITEM_TYPE);
		newNode(list, "cool", TextFragment.LIST_ITEM_TYPE);

		fragment = parse("# test\n# hello\n# cool\n# * test\n# * cool");
		assertEquals(wanted, fragment);
	}
	@Test
	public void listWithNoOwnItems() {
		TextFragment wanted, list, item;

		wanted = newNode(null, TextFragment.ROOT_TYPE);

		list = newNode(wanted, TextFragment.LIST_TYPE, "ol");
		newNode(list, "test", TextFragment.LIST_ITEM_TYPE);
		item = newNode(list, "hello", TextFragment.LIST_ITEM_TYPE);

		list = newNode(item, TextFragment.LIST_TYPE, "ul");
		item = newNode(list, TextFragment.LIST_ITEM_TYPE);

		list = newNode(item, TextFragment.LIST_TYPE, "ul");
		newNode(list, "test", TextFragment.LIST_ITEM_TYPE);
		newNode(list, "cool", TextFragment.LIST_ITEM_TYPE);

		fragment = parse("# test\n# hello\n# * * test\n# * * cool");
		assertEquals(wanted, fragment);
	}
	@Test
	public void listWithSublistAtMiddle() {
		TextFragment wanted, list, item;

		wanted = newNode(null, TextFragment.ROOT_TYPE);

		list = newNode(wanted, TextFragment.LIST_TYPE, "ol");
		newNode(list, "test", TextFragment.LIST_ITEM_TYPE);
		item = newNode(list, "hello", TextFragment.LIST_ITEM_TYPE);
		newNode(list, "cool", TextFragment.LIST_ITEM_TYPE);

		list = newNode(item, TextFragment.LIST_TYPE, "ul");
		newNode(list, "test", TextFragment.LIST_ITEM_TYPE);

		fragment = parse("# test\n# hello\n# * test\n# cool");
		assertEquals(wanted, fragment);
	}
	@Test
	public void listTypeChangeAtMiddle() {
		TextFragment wanted, list;

		wanted = newNode(null, TextFragment.ROOT_TYPE);

		list = newNode(wanted, TextFragment.LIST_TYPE, "ul");
		newNode(list, "howdy", TextFragment.LIST_ITEM_TYPE);
		newNode(list, "hello", TextFragment.LIST_ITEM_TYPE);

		list = newNode(wanted, TextFragment.LIST_TYPE, "ol");
		newNode(list, "test", TextFragment.LIST_ITEM_TYPE);
		newNode(list, "cool", TextFragment.LIST_ITEM_TYPE);

		fragment = parse("* howdy\n* hello\n# test\n# cool");
		assertEquals(wanted, fragment);
	}
	@Test
	public void listTypeChangeAtMiddleInsideList() {
		TextFragment wanted, mainList, list, item;

		wanted = newNode(null, TextFragment.ROOT_TYPE);
		mainList = newNode(wanted, TextFragment.LIST_TYPE, "ol");
		item = newNode(mainList, "test", TextFragment.LIST_ITEM_TYPE);

		list = newNode(item, TextFragment.LIST_TYPE, "ul");
		newNode(list, "howdy", TextFragment.LIST_ITEM_TYPE);
		newNode(list, "hello", TextFragment.LIST_ITEM_TYPE);

		list = newNode(item, TextFragment.LIST_TYPE, "ol");
		newNode(list, "test", TextFragment.LIST_ITEM_TYPE);
		newNode(list, "cool", TextFragment.LIST_ITEM_TYPE);

		fragment = parse("# test\n# * howdy\n# * hello\n# # test\n# # cool");
		assertEquals(wanted, fragment);
	}
}
