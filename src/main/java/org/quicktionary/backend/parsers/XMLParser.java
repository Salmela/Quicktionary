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
 * This parser isn't designed for attribute heavy xml
 * documents.
 *
 * spec: http://www.w3.org/TR/REC-xml/
 */
public class XMLParser {
	private BufferedReader          reader;
	private ArrayList<Integer>      parentNodes;
	private HashMap<String, Integer> tagNames;

	/**
	 * The current char must be at the start of next node
	 * after any method.
	 */
	private byte currentChar;
	private boolean parsingError;
	private boolean preserveWhitespaces;

	/* variables for current node */
	private NodeType      nodeType;
	private TagType       tagType;
	private StringBuilder tagName;
	private int tagNameId;
	private ArrayList<XMLAttribute> attributes;
	private StringBuilder attributeBuilder;

	public enum NodeType {
		ELEMENT,
		COMMENT,
		TEXT,
		NONE
	}

	private enum TagType {
		EMPTY,
		START,
		END,
		NONE
	}

	private class XMLAttribute {
		public String name;
		public String value;
	}

	public XMLParser() {
		parentNodes = new ArrayList<Integer>(32);
		attributes = new ArrayList<XMLAttribute>(16);
		tagNames = new HashMap<String, Integer>();
		tagName  = new StringBuilder(256);
		attributeBuilder = new StringBuilder(64);
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
		currentChar = -1;
		preserveWhitespaces = false;

		/* initialize the currentChar */
		if(getNext() == -1) {
			appendLog("File is empty");
		}

		/* read the declaration */
		try {
			result = parseXMLDeclaration();
		} catch(Exception exception) {
			currentChar = -1;
			return false;
		}
		return result;
	}

	public boolean isInitialized() {
		return currentChar != -1;
	}

	/* parsing interface */
	public int getTagNameId(String tagName) {
		int id;

		if(!tagNames.containsKey(tagName)) {
			id = tagNames.size();
			tagNames.put(tagName, id);
		} else {
			id = tagNames.get(tagName);
		}
		return id;
	}

	public int getTagNameId(CharSequence tagName) {
		return getTagNameId(tagName.toString());
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
			if(this.nodeType == NodeType.ELEMENT &&
			   this.tagNameId == tagNameId) {
				return true;
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

	private boolean goToLevel(int level) {
		while(parseNode()) {
			int levelCurrent = parentNodes.size();
			if(levelCurrent == level) {
				return true;
			} else if(levelCurrent < level) {
				return false;
			}
		}
		return false;
	}

	/**
	 * Go to the end of the parent node.
	 */
	public boolean getParent() {
		return goToLevel(parentNodes.size() - 1);
	}

	public boolean getFirstChild() {
		return goToLevel(parentNodes.size() + 1);
	}

	public boolean getNextSibling() {
		return goToLevel(parentNodes.size());
	}

	public String getTextContent() {
		if(!parseNode()) {
			return null;
		}
		throw new Error("Not implemented yet");

		if (nodeType != NodeType.TEXT) {
			return null;
		}
		return null;//textContent;
	}

	/**
	 * Get the value of specific attribute at current element.
	 */
	public String getAttribute(String attributeName) {
		for(XMLAttribute attribute : attributes) {
			if(attribute.name == attributeName) {
				return attribute.value;
			}
		}
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
		throw new Error("Not implemented");
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
	 * Check if letter is valid xml name character.
	 * @param letter The character that we want to check
	 * @return True, if the letter was in alphabet
	 */
	private boolean isAlphabet(byte letter, boolean first) {
		if(letter >= 'a' && letter <= 'z') {
			return true;
		}
		if(letter >= 'A' && letter <= 'Z') {
			return true;
		}
		if(letter == ':' || letter == '_') {
			return true;
		}
		if(first && letter >= '0' && letter <= '9') {
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
			currentChar = -1;
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
		XMLAttribute attribute;
		char quoteChar;

		attribute = new XMLAttribute();

		/* check that the name starts with letter */
		if(!isAlphabet(currentChar, true)) {
			return false;
		}

		/* get attribute name */
		attributeBuilder.setLength(0);
		while(isAlphabet(currentChar, false)) {
			/* push this char to element name */
			attributeBuilder.append(currentChar);
			getNext();
		}
		attribute.name = new String(attributeBuilder);

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
		attributeBuilder.setLength(0);
		while(currentChar != quoteChar && currentChar != '<' && currentChar != -1) {
			/* push this char to element name */
			attributeBuilder.append((char)currentChar);
			getNext();
		}
		attribute.value = new String(attributeBuilder);

		if(currentChar != quoteChar) {
			/* some xml like languages allows attribute values to be unquoted */
			appendLog("Expected " + quoteChar +" quote, but was " + currentChar + ".");
			return false;
		} else {
			/* consume the quote */
			getNext();
		}

		attributes.add(attribute);
		return true;
	}

	/**
	 * Parse one tag from reader.
	 * Reader is expected to be start of the tag.
	 */
	private void parseTag() {
		tagType = TagType.START;

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
		tagName.setLength(0);
		if(isAlphabet(currentChar, true)) {
			while(isAlphabet(currentChar, false)) {
				/* push this char to element name */
				tagName.append((char)currentChar);
				getNext();
			}
		}
		tagNameId = getTagNameId(tagName);

		skipWhitespaces(false);

		/* read the attributes */
		while(parseAttribute()) {
			if(!isWhitespace(currentChar)) {
				break;
			}
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

	private void parseTextContent() {
		nodeType = NodeType.TEXT;
		while(currentChar != -1 && currentChar != '<') {
			getNext();
		}
		return;
	}

	private void parseRootNode() {
		while(true) {
			parseTag();
			if(parsingError) {
				return;
			}

			if(nodeType == NodeType.TEXT) {
				appendLog("Text content is not allowed outside of root node.");
				return;
			} else if(nodeType == NodeType.ELEMENT) {
				break;
			}
		}
	}

	private boolean parseNode() {
		/* initialize variables for current node */
		parsingError = false;
		attributes.clear();
		nodeType = NodeType.NONE;
		tagType = TagType.NONE;
		tagNameId = 0;

		if(!preserveWhitespaces) {
			skipWhitespaces(false);
		}

		try {
			if(currentChar == -1) {
				return false;
			} else if(parentNodes.size() == 0) {
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
