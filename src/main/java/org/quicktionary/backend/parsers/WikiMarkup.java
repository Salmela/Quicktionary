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
	private final StringBuilder content;
	private ArrayList<MarkupStart> lineMarkup;
	private TextFragment rootFragment;
	private TextFragment currentFragment;
	private SymbolType[] symbolLut;

	public class TextFragment {
		private TextFragment parent;
		private int type;
		private String content;
		private ArrayList<TextFragment> childs;

		public TextFragment(TextFragment parent, int type) {
			this.parent = parent;
			this.type = type;
			this.content = null;
			this.childs = new ArrayList<TextFragment>();

			if(parent != null) {
				parent.addFragment(this);
			}
		}

		public void setContent(String content) {
			if(childs.size() != 0) {
				throw new Error("TextFragment must have only child fragments or text content.");
			}
			this.content = content;
		}

		public void addFragment(TextFragment fragment) {
			if(content != null) {
				throw new Error("TextFragment must have only child fragments or text content.");
			}
			this.childs.add(fragment);
		}

		public TextFragment getParent() {
			return parent;
		}
	}

	private class MarkupStart {
		public long location;
		public long sourceLocation;
		public SymbolType symbol;
		public int count;
		public int length;
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
		content = new StringBuilder(256);
		lineBuffer = new StringBuilder(256);
		lineMarkup = new ArrayList<MarkupStart>(16);

		rootFragment = null;
		currentFragment = null;

		createSymbolLut();
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

		rootFragment = new TextFragment(null, 0);
		currentFragment = rootFragment;

		/*TODO: make this into loop */
		parseLine();
		return true;
	}

	private void parseLine() {
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
			break;
		/* table */
		case '{':
			//parseTable();
			break;
		default:
			break;
		}

		/*TODO: create new paragraph if neaded */

		parseMarkup();

		/*TODO: handle partial markups */
		for(int i = lineMarkup.size() - 1; i >= 0; i--) {
			MarkupStart start = lineMarkup.get(i);
			System.out.println("markupStart " + start.sourceLocation + ", symbol: " + start.symbol.character + ", count: " + start.count);
		}
	}

	private void parseMarkup() {
		boolean wasWhitespace = false;
		do {
			/* return from the function at the newline character */
			if(currentChar == '\n') {
				return;
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

		System.out.println("Handle previous markup " + markup.symbol.character);

		switch(markup.symbol.character) {
		case '\'':
			break;
		case ']':
			break;
		case '}':
			break;
		case '>':
			break;
		default:
			break;
		}
	}

	private void parseHeaderMarkupStart() {
		SymbolType equalSymbol = new SymbolType('=', true, 0);
		MarkupStart start;
		int i;

		for(i = 0; i < 6; i++) {
			if(currentChar != '=') break;
			lineBuffer.append(currentChar);
			getNext();
		}
		start = appendMarkupStart(equalSymbol);
		start.count = i;
		System.out.println("Header parsed " + i + ".");
	}
}
