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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.lang.StringBuilder;

/**
 * This class implements offline xml parser. It doesn't
 * allow custom markup defined inside file. The code
 * only does markup expansion only for html characters.
 *
 * The implementation tries to not allocate memory. That
 * is why it is a state machine. The parser has also
 * limitation that it can go only forward and you can
 * only ask tag names of the parent nodes.
 *
 * spec: http://www.w3.org/TR/REC-xml/
 * @author alesalme
 */
public class XMLParser {
	private BufferedReader          reader;
	private ArrayList<Integer>      parentNodes;
	private HashMap<CharSequence, Integer> tagNames;

	/**
	 * The current char must be at the start of next node
	 * after any method.
	 */
	private byte currentChar;
	private boolean parsingError;
	private boolean preserveWhitespaces;

	private NodeType      nodeType;
	private TagType       tagType;
	private StringBuilder tagName;
	private int tagNameId;

	public enum NodeType {
		ELEMENT,
		COMMENT,
		TEXT
	}

	private enum TagType {
		START,
		END,
		EMPTY
	}

	public XMLParser() {
		parentNodes = new ArrayList<Integer>(32);
		tagNames = new HashMap<CharSequence, Integer>();
		tagName  = new StringBuilder(256);
		reader   = null;
	}

	/**
	 * The method for starting the parser.
	 *
	 * Should we catch exceptions here and return boolean if the parsing was
	 * successful.
	 */
	public boolean parseFile(File file) throws IOException {
		boolean result;
		reader = new BufferedReader(new FileReader(file));
		currentChar = 0;
		preserveWhitespaces = true;

		/* initialize the currentChar */
		getNext();

		/* read the declaration */
		try {
			result = parseXMLDeclaration();
		} catch(Exception exception) {
			return false;
		}
		return result;
	}

	/* parsing interface */
	public int getTagNameId(String tagName) {
		return getTagNameId((CharSequence)tagName);
	}

	public int getTagNameId(CharSequence tagName) {
		int id;

		if(!tagNames.containsKey(tagName)) {
			id = tagNames.size();
			tagNames.put(tagName, id);
		} else {
			id = tagNames.get(tagName);
		}
		return id;
	}

	public boolean getRoot() {
		/* check that we are at the start of the file */
		if(parentNodes.size() != 0) {
				appendLog("The reader has already readed the root node.");
				return false;
		}

		return parseNode();
	}

	/**
	 * Go to the first element with the tagNameId.
	 */
	public boolean findElement(int tagNameId) {
		while(parseNode()) {
			if(this.tagNameId == tagNameId) {
				break;
			}
		}
		return false;
	}

	/**
	 * Go to the first element with the tagName.
	 */
	public boolean findElement(String tagName) {
		return findElement(getTagNameId(tagName));
	}

	/**
	 * Go to the end of the parent node.
	 */
	public boolean getParent() {
		int parentIndex = parentNodes.size();

		while(parseNode()) {
			if(parentNodes.size() == parentIndex - 1) {
				return true;
			}
		}
		return false;
	}

	public boolean getFirstChild() {
		return false;
	}

	public boolean getNextSibling() {
		int parentIndex = parentNodes.size();
		while(parseNode()) {
			if(parentNodes.size() == parentIndex) {
				return true;
			}
		}
		return false;
	}

	public boolean getTextContent() {
		if(!parseNode()) {
			return false;
		}
		return nodeType == NodeType.TEXT;
	}

	/**
	 * Get the value of specific attribute at current element.
	 */
	public String getAttribute(String attributeName) {
		return null;
	}

	public NodeType getNodeType() {
		return nodeType;
	}

	/**
	 * Get the tag name of current element. If current node isn't element, then
	 * return null.
	 */
	public String getElementName() {
		return tagName.toString();
	}
	/**
	 * Get next attribute at the current element. If any attributes haven't been
	 * parsed then the first one is given.
	 */
	public String getNextAttribute() {
		return null;
	}

	/**
	 * Change the default of ignoring whitespaces.
	 */
	public void setWhitespacePreserving(boolean preserveWhitespaces) {
		this.preserveWhitespaces = preserveWhitespaces;
	}

	/**
	 * This method prints parsing errors to log.
	 */
	private void appendLog(String message) {
		System.out.println("Parsing error occured: " + message);
		String trace = Arrays.toString(Thread.currentThread().getStackTrace());
		System.out.println(trace);
		parsingError = true;
	}

	private void appendLog(Exception exception) {
		System.out.println("Parsing error occured: " + exception.getMessage());
		exception.printStackTrace();
		parsingError = true;
	}

	/**
	 * Check if letter is in ascii alphabet.
	 * @param letter The character that we want to check
	 * @return True, if the letter was in ascii alphabet
	 */
	private boolean isAlphabet(byte letter) {
		if(letter >= 'a' && letter <= 'z') {
			return true;
		}
		if(letter >= 'A' && letter <= 'Z') {
			return true;
		}
		return false;
	}

	/**
	 * Check if letter is whitespace. The method consideres
	 * space, newline, tab, and carriage return as whitespaces.
	 * @param letter The character that we want to check
	 * @return True, if the letter was ascii whitespace
	 */
	private boolean isWhitespace(byte letter) {
		if(letter == ' ' || letter == '\t' || letter == '\n' ||
		   letter == '\r') {
			return true;
		}
		return false;
	}

	/**
	 * Read next character from file.
	 * @return The readed character
	 */
	private byte getNext() {
		try {
			currentChar = (byte)reader.read();
		} catch(Exception e) {
			currentChar = 0;
		}
		return currentChar;
	}

	/**
	 * Skip over all following whitespaces. The method throws exception if
	 * the current byte isn't whitespace and the atLeastOne parameter is set.
	 */
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
	 * Note: Some XML-like languages allows attribute values to be unquoted.
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
			appendLog("Attribute's value must be enclosed inside quotes.");
			return false;
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
			appendLog("Expected " + quoteChar +" quote, but was " + currentChar + ".");
			return false;
		} else {
			/* consume the quote */
			getNext();
		}
		return true;
	}

	/**
	 * Parse one tag from reader.
	 * Reader is expected to be start of the tag.
	 */
	private void parseTag() {
		tagType = TagType.START;

		/* reset the length */
		tagName.setLength(0);

		/* read the first byte of the tag */
		if(currentChar != '<') {
			appendLog("Element must start with less-than sign.");
			return;
		}
		getNext();

		/* check if the element is end tag */
		if(currentChar == '/') {
			tagType = TagType.END;
			getNext();

		/* check if the element is comment */
		} else if(currentChar == '-') {
			/*TODO implement this */
			parsingError = true;
			nodeType = NodeType.COMMENT;
			return;
		}
		nodeType = NodeType.ELEMENT;

		/* read the name of element */
		while(isAlphabet(currentChar)) {
			/* push this char to element name */
			tagName.append(getNext());
		}
		tagNameId = getTagNameId(tagName);

		skipWhitespaces(true);

		/* read the attributes */
		while(parseAttribute()) {
			skipWhitespaces(true);
		}

		/* check if this is inline element */
		if(currentChar == '/') {
			if(tagType == TagType.END) {
				appendLog("The tag cannot be empty tag and end tag at the same time.");
			}
			tagType = TagType.EMPTY;
			getNext();
		}

		readChar('>', "Element must end with more-than sign.");
	}

	private boolean parseTextContent() {
		nodeType = NodeType.TEXT;
		while(currentChar != '<') {
			getNext();
		}
		return true;
	}

	private boolean parseRootNode() {
		while(true) {
			if(!parseNode()) {
				return false;
			}

			if(nodeType == NodeType.TEXT) {
				appendLog("Text content is not allowed outside of root node.");
				return false;
			} else if(nodeType == NodeType.ELEMENT) {
				break;
			}
		}
		return true;
	}

	private boolean parseNode() {
		parsingError = false;

		if(preserveWhitespaces) {
			skipWhitespaces(false);
		}

		try {
			if(parentNodes.size() == 0) {
				parseRootNode();
			} else if(currentChar == '<') {
				parseTag();
			} else {
				parseTextContent();
			}
		} catch(Exception exception) {
			appendLog(exception);
		}

		if(parsingError) {
			return false;
		}

		/* update the parent node array */
		if(nodeType == NodeType.ELEMENT) {
			if(tagType == TagType.START) {
				parentNodes.add(tagNameId);

			} else if(tagType == TagType.END) {
				int index;
				index = parentNodes.size() - 1;
				if(parentNodes.get(index) == tagNameId) {
					appendLog("Mismatching tags.");
				} else {
					parentNodes.remove(index);
				}
			}
		}

		return true;
	}

	private boolean parseXMLDeclaration() {
		skipWhitespaces(false);

		readChar('<', "File must start with xml declaration.");
		readChar('?', "File must start with xml declaration.");
		readChar('x', "File must start with xml declaration.");
		readChar('m', "File must start with xml declaration.");
		readChar('l', "File must start with xml declaration.");

		skipWhitespaces(true);

		/* read the version attribute */
		parseAttribute();
		skipWhitespaces(true);

		/* read the encoding attribute */
		parseAttribute();
		skipWhitespaces(true);

		/* read the attributes */
		while(parseAttribute()) {
			skipWhitespaces(true);
		}

		readChar('?', "The xml declaration is expected to end with ?>");
		readChar('>', "The xml declaration is expected to end with ?>");

		return !parsingError;
	}
}
