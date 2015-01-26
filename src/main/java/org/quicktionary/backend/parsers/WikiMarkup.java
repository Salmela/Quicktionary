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

public class WikiMarkup {
	private Reader reader;
	private char currentChar;

	public WikiMarkup(Reader reader) {
		this.reader = reader;
	}

	public void parse() {
		this.currentChar = 0;
		getNext();

		/*TODO: make this into loop */
		parseLine();
	}

	private boolean getNext() {
		int result;

		currentChar = '\0';
		try {
			result = reader.read();
		} catch(IOException e) {
			return false;
		}

		if(result == -1) {
			return false;
		}
		currentChar = (char)result;
		return true;
	}

	private void parseLine() {
		switch(currentChar) {
		case '=':
			//parseHeader();
			break;
		case '-':
			//parseRuler();
			break;
		case '*':
			//parseBulletItem();
			break;
		case '#':
			//parseNumberedItem();
			break;
		case ':':
			//parseIndentation();
			break;
		default:
			break;
		}

		/*TODO: make this into loop */
		parseMarkup();
	}

	private void parseMarkup() {
		switch(currentChar) {
		case '\'':
			//parseInline();
			break;
		case '[':
			//parseLink();
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
}
