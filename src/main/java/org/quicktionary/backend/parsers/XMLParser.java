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

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * spec: http://www.w3.org/TR/REC-xml/
 * @author alesalme
 */
public class XMLParser {
	private BufferedReader reader;
	private byte currentChar;

	public XMLParser(File file) throws IOException {
		reader = new BufferedReader(new FileReader(file));
		currentChar = 0;
	}

	private boolean isAlphabet(byte letter) {
		if(letter >= 'a' && letter <= 'z') {
			return true;
		}
		if(letter >= 'A' && letter <= 'Z') {
			return true;
		}
		return false;
	}
	private boolean isWhitespace(byte letter) {
		if(letter == ' ' || letter == '\t' || letter == '\n' ||
		   letter == '\r') {
			return true;
		}
		return false;
	}

	private byte getNext() {
		try {
			currentChar = (byte)reader.read();
		} catch(Exception e) {
		}
		return currentChar;
	}

	private void skipWhitespaces(boolean atLeastOne) {
		/* check that there is whitespace */
		if(!isWhitespace(currentChar)) {
			if(atLeastOne) {
				throw new Error("We expected whitespace, but there was " + currentChar);
			}
			return;
		}
		while(isWhitespace(currentChar = getNext()));
	}

	private void readChar(char wanted, String errorString) {
		if(currentChar != (byte)wanted) {
			throw new Error(errorString);
		} else {
			getNext();
		}
	}

	/**
	 * Parse a attribute of the element. We must be at the start of attribute.
	 * some XML like languages allows attribute values to be unquoted
	 */
	private boolean parseAttribute() {
		char quoteChar;

		/* check that the name starts with letter */
		if(!isAlphabet(currentChar)) {
			return false;
		}

		/* get attribute name */
		while(isAlphabet(currentChar)) {
			/* push this char to element name */
			getNext();
		}

		skipWhitespaces(false);

		/* get equal sign */
		readChar('=', "Attribute must have equal sign.");

		skipWhitespaces(false);

		if(currentChar != '\"' && currentChar != '\'') {
			throw new Error("Attribute's value must be enclosed inside quotes.");
		} else {
			quoteChar = (char)currentChar;
			getNext();
		}

		/* get the value */
		while(currentChar != quoteChar && currentChar != '<') {
			/* push this char to element name */
			getNext();
		}

		if(currentChar != quoteChar) {
			/* some xml like languages allows attribute values to be unquoted */
			throw new Error("Expected " + quoteChar +" quote, but was " + currentChar + ".");
		} else {
			/* consume the quote and the following whitespaces */
			getNext();
		}
		return true;
	}

	private void parseTag() {
		boolean endTag;
		byte letter;

		endTag = false;

		/* read the first byte of the tag */
		readChar('<', "Element must start with less-than sign.");

		/* check if the element is end tag */
		if(currentChar == '/') {
			endTag = true;
			getNext();
		}

		/* read the name of element */
		while(isAlphabet(currentChar)) {
			/* push this char to element name */
			getNext();
		}

		skipWhitespaces(true);

		/* read the attributes */
		while(parseAttribute()) {
			skipWhitespaces(true);
		}

		/* check if this is inline element */
		if(currentChar == '/') {
			if(endTag) {
				throw new Error("The tag cannot be empty tag and end tag at the same time.");
			}
			getNext();
		}

		readChar('>', "Element must end with more-than sign.");
	}
}
