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

import java.io.BufferedReader;
import java.io.Reader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.lang.StringBuilder;

/**
 * This class implements offline xml parser. It doesn't
 * allow custom markup defined inside file.
 *
 * The implementation tries to not allocate memory. That
 * is why it is implemented as state machine. The parser has
 * big limitation that you can only query nodes that are
 * after current node. Furthermore, you can query data only
 * from current node.
 *
 * This parser isn't designed for attribute heavy xml
 * documents. Only ascii characters are allowed inside
 * element nodes. The parser isn't full implementation
 * of xml spec.
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
	private int  currentDepth;
	private char currentChar;
	private char previousChar;
	private boolean parsingError;
	private boolean saveTextContent;
	private boolean preserveWhitespaces;
	private boolean verbose;
	final private StringBuilder textContent;

	/* variables for current node */
	private NodeType      nodeType;
	private TagType       tagType;
	private int           tagNameId;
	final private StringBuilder tagName;
	final private ArrayList<XMLAttribute> attributes;
	final private StringBuilder attributeBuilder;

	private boolean wasStartTag;

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

	public class XMLParserError extends Error {
		static final long serialVersionUID = 1L;

		public XMLParserError(String message) {
			super(message);
		}
	}

	public XMLParser() {
		parentNodes = new ArrayList<Integer>(32);
		attributes  = new ArrayList<XMLAttribute>(16);
		tagNames    = new HashMap<String, Integer>();
		tagName     = new StringBuilder(256);
		attributeBuilder = new StringBuilder(64);
		textContent  = new StringBuilder(4096);
		reader       = null;
		currentChar  = 0;
		verbose = false;
	}

	/**
	 * The method for starting the parser.
	 *
	 * @param stream The input stream which will be readed
	 */
	public boolean parseFile(Reader reader) throws IOException {
		this.reader  = new BufferedReader(reader);
		previousChar = 0;
		currentChar  = 0;
		currentDepth = 0;
		preserveWhitespaces = false;
		saveTextContent     = false;

		/* initialize the currentChar */
		if(!getNext()) {
			appendLog("File is empty");
			return false;
		}

		return true;
	}

	public boolean isInitialized() {
		return currentChar != 0;
	}

	/**
	 * Get a id for tagName. The method creates new id
	 * if the tagName doesn't have a id yet.
	 *
	 * @param tagName The name of the element
	 * @return Unique id for the element
	 */
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

	/**
	 * Give specific id for the tagName. The method allows client
	 * class to use constant values inside switch statement. If
	 * you try to give bad id or use it the method too late, then
	 * method will throw exception.
	 *
	 * You could use the method following way.
	 *
	 *   final static int ELEM_TAG = 0;
	 *
	 *   setTagNameId("elem", ELEM_TAG);
	 *
	 *   getNextNode();
	 *   switch(getElementNameId()) {
	 *       case ELEM_TAG:
	 *           // do something
	 *           break;
	 *       default:
	 *           break;
	 *   }
	 *
	 * @param tagName The name of the element
	 * @param id The id you want use
	 */
	public void setTagNameId(String tagName, int id) {
		if(tagNames.containsKey(tagName)) {
			throw new XMLParserError("The tag name has already id.");
		} else if(id != tagNames.size()) {
			throw new XMLParserError("The id is already used.");
		} else {
			tagNames.put(tagName, id);
		}
	}

	public int getTagNameId(CharSequence tagName) {
		return getTagNameId(tagName.toString());
	}

	/**
	 * Go to the root element. The root element must be first element in
	 * the document and there should not be textNodes before it.
	 */
	public boolean getRoot() {
		if(verbose) {
			System.out.println("getRoot()");
		}
		/* check that we are at the start of the file */
		if(parentNodes.size() != 0) {
			appendLog("The reader has already passed the root node.");
			return false;
		}

		while(getNextNode()) {
			if(nodeType == NodeType.ELEMENT) {
				return tagType == TagType.START;
			}
		}
		return false;
	}

	/**
	 * Get the next node. This method is just user friendlier name for
	 * parseNode.
	 */
	public boolean getNextNode() {
		return parseNode();
	}

	/**
	 * Go to the first element with the tagNameId.
	 */
	public boolean findElement(int tagNameId) {
		while(getNextNode()) {
			if(this.nodeType == NodeType.ELEMENT &&
			   this.tagType  == TagType.START &&
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

	private boolean goToDepth(int depth) {
		if(verbose) {
			System.out.println("goToDepth(" + depth + ")");
		}

		/* if the wanted depth is deeper that current node,
		   then check if current node can have childs */
		if(currentDepth < depth) {
			if(nodeType != NodeType.ELEMENT) {
				return false;
			}
			if(tagType != TagType.START) {
				return false;
			}
		}

		while(getNextNode()) {
			if(currentDepth == depth) {
				return true;
			} else if(currentDepth < depth) {
				return false;
			}
		}
		return false;
	}

	/**
	 * Go to the end of the parent node.
	 */
	public boolean getParent() {
		return goToDepth(currentDepth - 1);
	}

	public boolean getFirstChild() {
		return goToDepth(currentDepth + 1);
	}

	public boolean getNextSibling() {
		return goToDepth(currentDepth);
	}

	public String getTextContent() {
		return getTextContent(false);
	}

	public String getTextContent(boolean preserveWhitespaces) {
		boolean saveTextContentOld;
		boolean preserveWhitespacesOld;

		/* save the old state */
		saveTextContentOld = saveTextContent;
		preserveWhitespacesOld = preserveWhitespaces;
		saveTextContent    = true;
		this.preserveWhitespaces = preserveWhitespaces;

		/* read the text node */
		if(!getFirstChild()) {
			return null;
		}

		if (nodeType != NodeType.TEXT) {
			return null;
		}

		/* restore the old state */
		saveTextContent = saveTextContentOld;
		preserveWhitespaces = preserveWhitespacesOld;

		/* consume the end tag */
		getNextNode();

		return textContent.toString();
	}

	/**
	 * Get the value of specific attribute at current element.
	 */
	public String getAttribute(String attributeName) {
		for(XMLAttribute attribute : attributes) {
			if(attribute.name.equals(attributeName)) {
				return attribute.value;
			}
		}
		return null;
	}

	/**
	 * Get the node type of the current node.
	 */
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
	 * Get the tag name id of current element. If current node isn't element, then
	 * return -1.
	 */
	public int getElementNameId() {
		return tagNameId;
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
	 * The method prints parsing errors to log and the current stack trace.
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
	 *
	 * @param letter The character that we want to check
	 * @return True, if the letter was in alphabet
	 */
	private boolean isAlphabet(char letter, boolean first) {
		if(letter >= 'a' && letter <= 'z') {
			return true;
		}
		if(letter >= 'A' && letter <= 'Z') {
			return true;
		}
		if(letter == ':' || letter == '_') {
			return true;
		}
		if(!first && letter >= '0' && letter <= '9') {
			return true;
		}
		return false;
	}

	/**
	 * Check if letter is whitespace. The method consideres
	 * space, newline, tab, and carriage return as whitespaces.
	 *
	 * @param letter The character that we want to check
	 * @return True, if the letter was ascii whitespace
	 */
	private boolean isWhitespace(char letter) {
		if(letter == ' ' || letter == '\t' || letter == '\n' ||
		   letter == '\r') {
			return true;
		}
		return false;
	}

	/**
	 * Read next character from file.
	 * TODO: return boolean
	 *
	 * @return True, if the read was successful
	 */
	private boolean getNext() {
		int result;
		previousChar = currentChar;

		try {
			result = reader.read();
		} catch(IOException e) {
			currentChar = 0;
			return false;
		}

		if(result != -1) {
			currentChar = (char)result;
		} else {
			currentChar = 0;
			return false;
		}
		return true;
	}

	/**
	 * Skip over all following whitespaces. The method throws exception if
	 * the current char isn't whitespace and the atLeastOne parameter is set.
	 */
	private void skipWhitespaces(boolean atLeastOne) {
		/* check that there is whitespace */
		if(!isWhitespace(currentChar)) {
			if(atLeastOne) {
				throw new Error("We expected whitespace, but there was " + currentChar);
			}
			return;
		}

		while(getNext() && isWhitespace(currentChar));
	}

	/**
	 * Helper method for verifying the current character. The method also
	 * gives helpful error message if the character wasn't what we expected.
	 *
	 * @param wanted The character we expect current character to be
	 */
	private void expectChar(char wanted) {
		String errorString = "Expected '" + wanted + "' character, but was '" + (char)currentChar + "'.";

		if(currentChar != wanted) {
			System.out.print("Error in: ");
			printCurrentNode();

			throw new Error(errorString);
		} else {
			getNext();
		}
	}

	/**
	 * Parse single node from xml document.
	 * You propably should use alias method called getNextNode.
	 */
	private boolean parseNode() {
		/* initialize variables for current node */
		parsingError = false;
		attributes.clear();
		nodeType = NodeType.NONE;
		tagType = TagType.NONE;
		tagNameId = -1;

		if(!preserveWhitespaces) {
			skipWhitespaces(false);
		}

		try {
			if(currentChar == 0) {
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
			if(verbose) {
				System.out.println("Parsing error encountered.");
			}
			return false;
		}

		/* check if the nodes are before or after the root node */
		if(parentNodes.size() == 0) {
			if(!isValidRootNode()) {
				return false;
			}
		}

		updateParentArray();

		if(verbose) {
			printCurrentNode();
		}

		return true;
	}

	/**
	 * Parse a tag from reader.
	 * Reader is expected to be start of the tag.
	 */
	private void parseTag() {
		tagType = TagType.START;

		/* read the first char of the tag */
		expectChar('<');

		/* check if the type of the tag */
		if(currentChar == '?') {
			parseXMLDeclaration();
		} else if(currentChar == '!') {
			parseComment();
		} else {
			parseElement();
		}
	}

	/**
	 * Parse the xml declaration from reader.
	 * This method should be only used in from parseTag.
	 */
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

	/**
	 * Parse a comment tag from reader.
	 * This method should be only used in from parseTag.
	 */
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
				/* exit after atleast two dashes followed by greater than sign '-->' */
				if(dashCount >= 2 && currentChar == '>') break;
			}

		} while(getNext());
	}

	/**
	 * Parse a element from reader.
	 * This method should be only used in from parseTag.
	 */
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
		while(currentChar != quoteChar && currentChar != 0 &&
		      currentChar != '<' && currentChar != '>') {
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
	 * Parse following text from reader.
	 * This method should be only used in parseNode.
	 */
	private void parseTextContent() {
		nodeType = NodeType.TEXT;

		/* reset the buffer */
		textContent.setLength(0);

		while(currentChar != 0 && currentChar != '<') {
			/* check if we want to just ignore all text content */
			if(!saveTextContent) {
				getNext();
				continue;
			}

			/* handle whitespaces */
			if(!preserveWhitespaces && isWhitespace(currentChar)) {
				/* get next non whitespace */
				skipWhitespaces(true);

				if(currentChar == 0 || currentChar == '<') {
					return;
				}
				textContent.append(' ');
				continue;
			}

			textContent.append(currentChar);
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
		/* update the current depth so that child nodes
		   have their depth one greater than the parent */
		if(wasStartTag) {
			currentDepth++;
			wasStartTag = false;
		}

		if(nodeType != NodeType.ELEMENT) {
			return;
		}

		if(tagType == TagType.START) {
			parentNodes.add(tagNameId);
			wasStartTag = true;

		} else if(tagType == TagType.END) {
			int index;

			index = parentNodes.size() - 1;
			if(parentNodes.get(index) != tagNameId) {
				appendLog("Mismatching tags expected: " + parentNodes.get(index) +
				          ", was: " + tagName + "(" + tagNameId + ").");
			} else {
				parentNodes.remove(index);
			}
			currentDepth--;
		}
	}

	/**
	 * Print the current node to the stdout. This method
	 * is used in parseNode, when verbose is set to true.
	 */
	public void printCurrentNode() {
		int i;
		System.out.print("VERBOSE");

		/* print the depth of the node */
		System.out.print(" " + currentDepth + " ");

		/* add some indentation to make scanning the lines easier */
		for(i = 0; i < currentDepth; i++) {
			System.out.print(" ");
		}

		/* print the node type*/
		switch(nodeType) {
		case ELEMENT:
			System.out.print("element");
			switch(tagType) {
			case START:
				System.out.print("/start");
				break;
			case END:
				System.out.print("/end");
				break;
			case EMPTY:
				System.out.print("/empty");
				break;
			case NONE:
				System.out.print("/none");
				break;
			}
			System.out.print(" " + tagName);
			break;
		case TEXT:
			System.out.print("text");
			break;
		case COMMENT:
			System.out.print("comment");
			break;
		case NONE:
			System.out.print("none");
			break;
		default:
			System.out.print("unknown");
			break;
		}

		System.out.println("");
	}
}
