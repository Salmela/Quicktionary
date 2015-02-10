/* Quicktionary backend - Word translator app
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

import java.io.Reader;
import java.io.IOException;

import java.util.Arrays;
import java.util.ArrayList;
import java.lang.StringBuilder;

public class WikiMarkup extends Parser {
	private final StringBuilder lineBuffer;
	//private final StringBuilder content;

	private ArrayList<MarkupStart> lineMarkup;
	private ArrayList<TextFragment> itemList;
	private MarkupStart prevLineMarkup;

	private TextFragment rootFragment;
	private TextFragment currentFragment;
	private SymbolType[] symbolLut;

	public class TextFragment {
		private TextFragment parent;
		private int type;
		private String content;
		private ArrayList<TextFragment> childs;
		private String parameter;

		public final static int ROOT_TYPE = 0;
		public final static int PLAIN_TYPE = 1;
		public final static int HEADER_TYPE = 2;
		public final static int PARAGRAPH_TYPE = 3;
		public final static int STRONG_TYPE = 4;
		public final static int EM_TYPE = 5;
		public final static int LINK_TYPE = 6;
		public final static int RULER_TYPE = 7;
		public final static int TEMPLATE_TYPE = 8;
		public final static int LIST_TYPE = 9;
		public final static int LIST_ITEM_TYPE = 10;
		public final static int TABLE_TYPE = 11;
		public final static int TABLE_ROW_TYPE = 12;
		public final static int TABLE_CELL_TYPE = 13;
		public final static int TABLE_HEADING_TYPE = 14;
		public final static int TABLE_CAPTION_TYPE = 15;
		public final static int DEFINITION_LABEL_TYPE = 16;
		public final static int DEFINITION_ITEM_TYPE = 17;
		public final static int MISC_TYPE = 100;

		public TextFragment(int type, String parameter) {
			this.parent = null;
			this.type = type;
			this.content = null;
			this.childs = new ArrayList<TextFragment>();
			this.parameter = parameter;
		}

		public TextFragment(int type) {
			this(type, null);
		}

		public void setContent(String content) {
			if(childs.size() != 0) {
				throw new Error("TextFragment must have only child fragments or text content.");
			}
			this.content = content;
		}

		/**
		 * Add child to the TextFragment.
		 * The method returns added fragment, so that the unit tests can
		 * be writen more compactly.
		 *
		 * @param fragment The new child for the fragment
		 * @return The added fragment
		 */
		public TextFragment appendChild(TextFragment fragment) {
			if(content != null) {
				throw new Error("TextFragment must have only child fragments or text content.");
			}
			this.childs.add(fragment);
			fragment.parent = this;

			return fragment;
		}

		/**
		 * Print the parsing tree recursively.
		 *
		 * @param indentation The padding added to start of each line.
		 */
		public void print(int indentation) {
			int i = indentation;
			while(i-- > 0) System.out.print(" ");
			System.out.println("Node type: " + type);

			if(content != null) {
				i = indentation + 2;
				while(i-- > 0) System.out.print(" ");

				System.out.println("Content: " + content);
				return;
			}

			for(TextFragment child : childs) {
				child.print(indentation + 2);
			}
		}

		/**
		 * Check that fragments are equal.
		 * This method should be only used in tests.
		 *
		 * @return True if the fragments are equal
		 */
		public boolean equals(TextFragment fragment) {
			if(fragment.type != type) {
				return false;
			}

			if(parameter != null && parameter.equals(fragment.parameter)) {
				return false;
			}

			if(content != null) {
				if(!content.equals(fragment.content)) {
					return false;
				}
				if(childs.size() != 0) {
					System.out.println("ERROR: the fragment has invalid content");
					return false;
				}
			}

			if(childs.size() != fragment.childs.size()) {
				System.out.println("ERROR: the fragment has invalid content");
				return false;
			}

			for(int i = 0; i < childs.size(); i++) {
				if(!childs.get(i).equals(fragment.childs.get(i))) {
					return false;
				}
			}
			return true;
		}

		public TextFragment getParent() {
			return parent;
		}

		public int getType() {
			return type;
		}

		public String getParameter() {
			return parameter;
		}
	}

	private class MarkupStart {
		public SymbolType symbol;

		public long sourceLocation;
		public int location;
		public int count;

		public int length;
		public MarkupStart endMarkup;
	}

	private class SymbolType {
		public char character;
		public boolean multiple;
		public boolean end;
		public int priority;

		public SymbolType(char character, boolean multiple, int priority, boolean end) {
			this.character = character;
			this.multiple = multiple;
			this.priority = priority;
			this.end = end;
		}

		public SymbolType(char character, boolean multiple, int priority) {
			this(character, multiple, priority, false);
		}
	}

	public WikiMarkup() {
		super();
		//content = new StringBuilder(256);
		lineBuffer = new StringBuilder(256);
		lineMarkup = new ArrayList<MarkupStart>(16);
		itemList = new ArrayList<TextFragment>();

		rootFragment = null;
		currentFragment = null;

		createSymbolLut();
	}

	private void itemListTruncate(int newSize) {
		if(itemList.size() == newSize) return;
		itemList.subList(newSize, itemList.size()).clear();
	}

	/**
	 * Create the look up table that is used in parseMarkup.
	 */
	public void createSymbolLut() {
		symbolLut = new SymbolType[127];
		Arrays.fill(symbolLut, null);
		/* text style */
		symbolLut['\''] = new SymbolType('\'', true, 0, true);
		/* link */
		symbolLut['['] = new SymbolType('[', true, 1);
		symbolLut[']'] = new SymbolType(']', true, 1, true);
		/* html tag */
		symbolLut['<'] = new SymbolType('<', false, 2);
		symbolLut['>'] = new SymbolType('>', false, 2, true);
		/* template */
		symbolLut['{'] = new SymbolType('{', true, 3);
		symbolLut['}'] = new SymbolType('}', true, 3, true);
		/* header end */
		symbolLut['='] = new SymbolType('=', true, 0, true);
	}

	public boolean parse(Reader reader) throws IOException {
		boolean status;

		status = super.parse(reader);
		if(!status) {
			return false;
		}

		rootFragment = new TextFragment(0);
		currentFragment = rootFragment;
		itemList.clear();

		/*TODO: make this into loop */
		parseLine();
		return true;
	}

	private void parseLine() {
		prevLineMarkup = null;
		lineBuffer.setLength(0);
		lineMarkup.clear();

		/* trim the whitespace from the start of line */
		do {
			if(!isWhitespace(currentChar)) break;
		} while(getNext());

		switch(currentChar) {
		/* header */
		case '=':
			parseHeaderMarkupStart();
			break;
		/* ruler */
		case '-':
			//parseRuler();
			break;
		/* bullet list */
		case '*':
		/* numbered list */
		case '#':
		/* definition list */
		case ';':
		/* indentation */
		case ':':
			//parseListItem();
			break;
		/* paragraph */
		case '\n':
			handleParagraph();
			break;
		/* table */
		case '{':
			parseTable();
			break;
		default:
			break;
		}

		/*TODO: create new paragraph if neaded */

		parseMarkup();

		finalizeHeader();

		/*TODO: handle partial markups */
		for(int i = 0; i < lineMarkup.size(); i++) {
			MarkupStart start = lineMarkup.get(i);
			if(start.length > 0) {
				System.out.print("START ");
			}
			System.out.println("PROCESS markupStart " + start.sourceLocation + ", symbol: " + start.symbol.character + ", count: " + start.count);
		}
	}

	private void parseTable() {
	}

	private void finalizeHeader() {
		MarkupStart start, end;
		int i;

		/* header must start with equal symbol */
		start = lineMarkup.get(0);
		if(start.symbol != symbolLut['=']) {
			return;
		}

		/* header must end with equal symbol */
		end = start.endMarkup;
		if(end == null || end != lineMarkup.get(lineMarkup.size() - 1)) {
			return;
		}

		/* there must be only whitespace after the header */
		for(i = end.location + end.count + 1; i < lineBuffer.length(); i++) {
			System.out.println("TEXT " + lineBuffer.charAt(i));
			if(!isWhitespace(lineBuffer.charAt(i))) {
				return;
			}
		}
		lineBuffer.delete(0, start.location + start.count);
		lineBuffer.setLength(start.length);
	}

	private void parseMarkup() {
		boolean wasWhitespace = false;
		do {
			/* return from the function at the newline character */
			if(currentChar == '\n') {
				break;
			/* ignore whitespace */
			} else if(isWhitespace(currentChar)) {
				wasWhitespace = true;
				continue;
			}

			/* replace multiple whitespaces with one space character */
			if(wasWhitespace) {
				lineBuffer.append(' ');
				wasWhitespace = false;
			}

			/* add every non-whitespace character into lineBuffer */
			lineBuffer.append(currentChar);

			/* parse a markup */
			if(currentChar < symbolLut.length && symbolLut[currentChar] != null) {
				handleInlineMarkup(symbolLut[currentChar]);
			}
		} while(getNext());

		handlePreviousMarkup();
	}

	private TextFragment getTextFragment(int type) {
		TextFragment fragment = currentFragment;

		while(fragment != null) {
			if(fragment.type == type) return fragment;
			fragment = fragment.getParent();
		}
		return null;
	}

	private MarkupStart appendMarkupStart(SymbolType symbol) {
		MarkupStart start = new MarkupStart();
		start.location = lineBuffer.length() - 1;
		start.sourceLocation = getAddress();
		start.symbol = symbol;
		start.count = 1;
		start.length = -1;
		lineMarkup.add(start);
		return start;
	}

	private void handleInlineMarkup(SymbolType symbol) {
		MarkupStart start;

		if(lineMarkup.size() > 0) {
			MarkupStart markup;
			int lastMarkupIndex = lineMarkup.size() - 1;

			markup = lineMarkup.get(lastMarkupIndex);

			if(previousChar == currentChar && symbol.multiple) {
				markup.count++;
				return;
			}

			handlePreviousMarkup();
		}

		start = appendMarkupStart(symbol);

		System.out.println("Symbol: " + currentChar + " location: " + start.location);
	}

	/* remove this method */
	private void handleParagraph() {
		TextFragment oldParagraph, parent;

		oldParagraph = getTextFragment(TextFragment.PARAGRAPH_TYPE);
		if(oldParagraph == null) {
			parent = rootFragment;
		} else {
			parent = oldParagraph.getParent();
		}
		currentFragment = new TextFragment(TextFragment.PARAGRAPH_TYPE);
		rootFragment.appendChild(currentFragment);
	}

	private MarkupStart getMarkupSymbol(SymbolType symbol) {
		int i;
		for(i = lineMarkup.size() - 2; i >= 0; i--) {
			MarkupStart start = lineMarkup.get(i);

			if(start.symbol == symbol) return start;
			if(start.symbol.priority > symbol.priority) break;
		}
		return null;
	}

	private void handlePreviousMarkup() {
		MarkupStart start, markup;
		int lastMarkupIndex = lineMarkup.size() - 1;

		if(lineMarkup.size() == 0) {
			return;
		}

		markup = lineMarkup.get(lastMarkupIndex);

		/* handle only the end markups */
		if(!markup.symbol.end) {
			return;
		}

		switch(markup.symbol.character) {
		case '\'':
			parseTextStyleMarkup(markup);
			break;
		case ']':
			parseLinkMarkup(markup);
			break;
		case '}':
			parseTemplateMarkup(markup);
			break;
		case '>':
			start = getMarkupSymbol(symbolLut['<']);
			if(start == null) return;
			System.out.println("HTML range " + start.location + ", " + markup.location);
			break;
		case '=':
			parseHeaderMarkup(markup);
			break;
		default:
			break;
		}
	}

	private void createTextFragment(MarkupStart markup) {
		MarkupStart start;

		switch(markup.symbol.character) {
		case '\'':
			finalizeTextStyleMarkup(markup);
			break;
		case ']':
			finalizeLinkMarkup(markup);
			break;
		case '}':
			finalizeTemplateMarkup(markup);
			break;
		case '>':
			start = getMarkupSymbol(symbolLut['<']);
			if(start == null) return;
			System.out.println("HTML range " + start.location + ", " + markup.location);
			break;
		case '=':
			finalizeHeaderMarkup(markup);
			break;
		default:
			break;
		}
	}

	private void parseHeaderMarkupStart() {
		MarkupStart start;
		int i;

		for(i = 0; i < 6; i++) {
			if(currentChar != '=') break;
			lineBuffer.append(currentChar);
			getNext();
		}
		start = appendMarkupStart(symbolLut['=']);
		start.count = i;
		System.out.println("Header parsed " + i + ".");
	}

	private void appendInlineTextFragment(int type, int startIndex, int endIndex) {
		TextFragment fragment;
		String text;

		text = lineBuffer.substring(startIndex, endIndex);

		fragment = new TextFragment(TextFragment.PLAIN_TYPE);
		fragment.setContent(text);
		currentFragment.getParent().appendChild(fragment);
	}

	private void parseTextStyleMarkup(MarkupStart end) {
		MarkupStart start;
		int quotes;

		start = getMarkupSymbol(end.symbol);
		if(start == null) return;
		if(start.length > 0) return;

		quotes = (end.count < start.count) ? end.count : start.count;
		if(quotes >= 5) {
			quotes = 5;
		} else if(quotes >= 3) {
			quotes = 3;
		} else if(quotes >= 2) {
			quotes = 2;
		} else if(quotes == 1) {
			return;
		}

		if(start.count > quotes) {
			start.location += start.count - (quotes + 1);
			start.count = quotes;
		}
		if(end.count > quotes) {
			end.location += end.count - (quotes + 1);
			end.count = quotes;
		}

		start.length = end.location - start.location;
		start.endMarkup = end;

		System.out.println("Inline range " + start.location + ", " + end.location + "  " +
			lineBuffer.substring(start.location + start.count, end.location));
	}

	private void finalizeTextStyleMarkup(MarkupStart start) {
		MarkupStart end = start.endMarkup;
		int quotes = start.count;

		/* put the previous text into plain text fragment */
		appendInlineTextFragment(TextFragment.PLAIN_TYPE, prevLineMarkup.location + prevLineMarkup.count, start.location);

		/* put the quoted text into inline text fragment */
		if(quotes == 2) {
			appendInlineTextFragment(TextFragment.EM_TYPE, start.location + start.count, end.location);
		} else if(quotes == 3) {
			appendInlineTextFragment(TextFragment.STRONG_TYPE, start.location + start.count, end.location);
		} else if(quotes == 5) {
			TextFragment parent;

			parent = currentFragment;
			currentFragment = new TextFragment(TextFragment.EM_TYPE);
			parent.appendChild(currentFragment);

			appendInlineTextFragment(TextFragment.STRONG_TYPE, start.location + start.count, end.location);
			currentFragment = parent;
		}
	}

	private void parseLinkMarkup(MarkupStart end) {
		MarkupStart start;
		int brackets;

		start = getMarkupSymbol(symbolLut['[']);
		if(start == null) return;
		if(start.length > 0) return;

		brackets = (end.count < start.count) ? end.count : start.count;
		if(start.count >= brackets) {
			start.location += start.count - brackets;
			start.count = brackets;
		}
		if(end.count >= brackets) {
			end.count = brackets;
		}

		start.length = end.location - start.location;
		start.endMarkup = end;

		System.out.println("Link range " + start.location + ", " + end.location + "  " +
			lineBuffer.substring(start.location + start.count, end.location));
	}

	private void finalizeLinkMarkup(MarkupStart end) {
	}

	private void parseTemplateMarkup(MarkupStart end) {
		MarkupStart start, prevLink;

		start = getMarkupSymbol(symbolLut['{']);
		if(start == null) return;
		if(start.length > 0) return;

		prevLink = getMarkupSymbol(symbolLut['[']);
		/* ignore the template if there is unfinished link */
		if(prevLink != null && start.location < prevLink.location && prevLink.length == -1) {
			System.out.println("Warning: the template contains unfinished link.");
			return;
		}
		start.length = end.location - start.location;
		start.endMarkup = end;

		System.out.println("Template range " + start.location + ", " + end.location + "  " +
			lineBuffer.substring(start.location + start.count, end.location));
	}

	private void finalizeTemplateMarkup(MarkupStart end) {
	}

	private void parseHeaderMarkup(MarkupStart end) {

	}

	private void finalizeHeaderMarkup(MarkupStart end) {
	}
}
