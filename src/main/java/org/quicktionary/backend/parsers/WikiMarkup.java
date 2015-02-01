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

import java.util.ArrayList;
import java.lang.StringBuilder;

public class WikiMarkup extends Parser {
	private final StringBuilder content;
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

	public WikiMarkup() {
		super();
		content = new StringBuilder(256);

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
		/* trim the whitespace from the start of line */
		do {
			if(!isWhitespace(currentChar)) break;
		} while(getNext());

		switch(currentChar) {
		/* header */
		case '=':
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
	}

	private void parseMarkup() {
		switch(currentChar) {
		case '\'':
			//parseInline();
			break;
		case '[':
			parseLink();
			break;
		case '{':
			//parseTemplate();
			break;
		case '<':
			//parseHTML();
			break;
		default:
			break;
		}
	}

	private void parseHeader() {
		TextFragment fragment;
		int i;

		expectChar('=');
		for(i = 1; i < 6; i++) {
			if(currentChar != '=') break;
			getNext();
		}

		content.setLength(0);
		while(currentChar == '=') {
			content.append(currentChar);
			getNext();
		}

		for(; i >= 0; i--) {
			if(currentChar != '=') break;
			getNext();
		}
		if(i == 0) {
			appendLog("Invalid formatting of header");
		}
		expectChar('\n');

		fragment = new TextFragment(0);
		fragment.setContent(content.toString());
		fragments.add(fragment);

		return;
	}

	private void parseRuler() {
		expectChar('-');
		if(currentChar != '-') {
			return;
		}
	}

	private void parseListItem() {
	}

	private parseLink() {
		expectChar('[');
		expectChar('[');


		content.setLength(0);
		while(currentChar != ']' && currentChar != '|') {
			content.append(currentChar);
			getNext();
		}

		fragment = new TextFragment(1);
		fragment.setContent(content.toString());
		fragments.add(fragment);

		if(currentChar == '|') {
		}

		expectChar(']');
		expectChar(']');
	}
}
