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
	private byte previousChar;
	private boolean parsingError;
	private boolean saveTextContent;
	private boolean preserveWhitespaces;
	private StringBuilder textContent;

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
		attributes  = new ArrayList<XMLAttribute>(16);
		tagNames    = new HashMap<String, Integer>();
		tagName     = new StringBuilder(256);
		attributeBuilder = new StringBuilder(64);
		reader       = null;
		currentChar  = -1;
		previousChar = -1;
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
		saveTextContent     = false;

		/* initialize the currentChar */
		if(getNext() == -1) {
			appendLog("File is empty");
			return false;
		}

		/* read the first node */
		return parseNode();
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
			appendLog("The reader has already passed the root node.");
			return false;
		}

		while(getNextNode()) {
			if(this.nodeType == NodeType.ELEMENT) {
				return true;
			}
		}
		return false;
	}

	public boolean getNextNode() {
		return parseNode();
	}

	/**
	 * Go to the first element with the tagNameId.
	 */
	public boolean findElement(int tagNameId) {
		while(getNextNode()) {
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
		while(getNextNode()) {
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
		boolean saveTextContentOld;

		/* temporarily enable saveTextContent */
		saveTextContentOld = saveTextContent;
		saveTextContent    = true;

		if(!getNextNode()) {
			return null;
		}
		saveTextContent = saveTextContentOld;

		if (nodeType != NodeType.TEXT) {
			return null;
		}
		return textContent;
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

	public void setTextContentStoring(boolean storeTextContent) {
		this.saveTextContent = saveTextContent;
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
			previousChar = currentChar;
			currentChar = (byte)reader.read();
		} catch(Exception e) {
			currentChar = -1;
		}
		return currentChar;
	}

	private byte getPrevious() {
		return previousChar;
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

		while(getNext() != -1 && isWhitespace(currentChar));
	}

	private void expectChar(char wanted) {
		String errorString = "Expected '" + wanted +"' character, but was '" + currentChar + "'.";

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
		expectChar('=');

		skipWhitespaces(false);

		if(currentChar != '\"' && currentChar != '\'') {
			appendLog("Attribute's value must be enclosed inside quotes.");
			return false;
		}

		/* save the quote character, because the attribute value must end to it */
		quoteChar = (char)currentChar;
		getNext();

		/* get the value */
		attributeBuilder.setLength(0);
		while(currentChar != quoteChar && currentChar != '<' && currentChar != -1) {
			/* push this char to element name */
			attributeBuilder.append((char)currentChar);
			getNext();
		}
		attribute.value = new String(attributeBuilder);

		expectChar(quoteChar);

		attributes.add(attribute);
		return true;
	}

	/**
	 * Parse a tag from reader.
	 * Reader is expected to be start of the tag.
	 */
	private void parseTag() {
		tagType = TagType.START;

		/* read the first byte of the tag */
		expectChar('<');

		/* check if the element is comment */
		if(currentChar == '!') {
			parseComment();
		} else if(currentChar == '?') {
			parseXMLDeclaration();
		} else {
			parseElement();
		}
	}

	private void parseElement() {
		if(previousChar != '<') {
			throw new Error("This method should be only used by parseTag.");
		}

		nodeType = NodeType.ELEMENT;

		/* check if the element is end tag */
		if(currentChar == '/') {
			tagType = TagType.END;
			getNext();
		}

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

		expectChar('>');
	}

	private void parseTextContent() {
		nodeType = NodeType.TEXT;
		while(currentChar != -1 && currentChar != '<') {
			/* check if we want to just ignore all text content */
			if(!saveTextContent) {
				getNext();
			}

			/* handle whitespaces */
			if(!preserveWhitespaces && isWhitespace(currentChar)) {
				/* get next non whitespace */
				skipWhitespaces(true);

				if(currentChar == -1 || currentChar == '<') {
					return;
				}
				textContent.append(' ');
				continue;
			}

			textContent.append((char)currentChar);
			getNext();
		}
		return;
	}

	/**
	 * Check if the current node is valid outside of the root.
	 * This method should be only used in parseNode.
	 */
	private boolean isValidRootNode() {
		if(nodeType == NodeType.TEXT) {
			appendLog("Text content is not allowed outside of root node.");
			return false;
		}
		if(nodeType == NodeType.ELEMENT &&
		   tagType == TagType.EMPTY) {
			appendLog("The root node cannot be empty node.");
			return false;
		}
		/*TODO: disallow two root element */
		return true;
	}

	/**
	 * Update the parent array.
	 * This method should be only used in parseNode.
	 */
	private void updateParentArray() {
		if(nodeType != NodeType.ELEMENT) {
			return;
		}

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

		/* check if the nodes are before or after the root node */
		if(parentNodes.size() == 0) {
			if(!isValidRootNode()) {
				return false;
			}
		}

		updateParentArray();

		return true;
	}

	private void parseComment() {
		int dashCount;

		if(previousChar != '<') {
			throw new Error("This method should be only used by parseTag.");
		}
		expectChar('!');
		expectChar('-');
		expectChar('-');

		nodeType = NodeType.COMMENT;

		/* try to match the comment ending pattern for each character */
		do {
			dashCount = 0;

			while(currentChar == '-') {
				dashCount++;
				getNext();
				/* exit after two dashes followed by greater than sign '-->' */
				if(dashCount == 2 && currentChar == '>') break;
			}

		} while(getNext() == -1);
	}

	private void parseXMLDeclaration() {
		if(previousChar != '<') {
			throw new Error("This method should be only used by parseTag.");
		}
		expectChar('?');
		expectChar('x');
		expectChar('m');
		expectChar('l');

		if(parentNodes.size() != 0) {
			appendLog("The xml declaration must be at the start of file.");
		}

		skipWhitespaces(true);

		/* read the version attribute */
		parseAttribute();
		skipWhitespaces(true);

		/* read the encoding attribute */
		parseAttribute();
		skipWhitespaces(true);

		/* read the attributes */
		while(parseAttribute()) {
			if(!isWhitespace(currentChar)) break;
			skipWhitespaces(true);
		}

		expectChar('?');
		expectChar('>');
	}
}
