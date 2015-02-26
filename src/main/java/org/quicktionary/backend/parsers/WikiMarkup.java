/* Quicktionary backend - Word dictionary app
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

import org.quicktionary.backend.TextNode;

/**
 * The WikiMarkup class parses the wiki markup.
 * This class is developed at another git branch.
 * TODO: Rename to WikiMarkupReader
 */
public class WikiMarkup extends Parser {
	private final StringBuilder lineBuffer;
	//private final StringBuilder content;

	private ArrayList<MarkupStart> lineMarkup;
	private ArrayList<TextNode> parentList;
	private int inlineFragmentIndex;
	private boolean inlineWhitespaceConsumed;
	private int openTemplates;
	private MarkupStart previousMarkup;

	private SymbolType[] symbolLut;

	/*TODO: rename to MarkupSymbol */
	private static class MarkupStart {
		public enum MarkupType {
			START,
			END,
			NONE
		}

		public SymbolType symbol;

		public String sourceLocation;
		public int location;
		public int count;

		public MarkupStart matchingMarkup;
		public MarkupType type;
	}

	private static class SymbolType {
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
		lineBuffer = new StringBuilder(256);
		lineMarkup = new ArrayList<MarkupStart>(16);
		parentList = new ArrayList<TextNode>();

		createSymbolLut();
	}

	private void printDebug(String str) {
		printDebug(str, true);
	}

	private void printDebug(String str, boolean newline) {
		//if(newline) {
		//	System.out.println(str);
		//} else {
		//	System.out.print(str);
		//}
	}

	/**
	 * Get the highest node.
	 */
	private TextNode getCurrentFragment() {
		if(parentList.size() == 0) {
			return null;
		}
		return parentList.get(parentList.size() - 1);
	}

	private void closePreviousFragment() {
		itemListTruncate(parentList.size() - 1);
	}

	private void itemListTruncate(int newSize) {
		if(newSize == parentList.size()) return;
		if(newSize > parentList.size()) {
			throw new Error("The new size must be less than the current size.");
		}

		/* normalize the text nodes */
		for(int i = parentList.size() - 1; i >= newSize; i--) {
			parentList.get(i).normalize();
		}

		parentList.subList(newSize, parentList.size()).clear();
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

	/**
	 * Parse the wikitext from reader.
	 * @param reader The wikitext to be readed
	 */
	public boolean parse(Reader reader) throws IOException {
		boolean status;

		status = super.parse(reader);
		if(!status) {
			return false;
		}

		parentList.clear();

		/* add the root node */
		parentList.add(new TextNode(0));
		inlineFragmentIndex = parentList.size();
		inlineWhitespaceConsumed = true;

		/* parse every line from the reader */
		while(currentChar != 0) {
			parseLine();
		}

		/* end all open nodes */
		itemListTruncate(1);

		//parentList.get(0).print(2);
		return true;
	}

	public TextNode getRoot() {
		return parentList.get(0);
	}

	/**
	 * Parse and generate parsing tree for single line of wikitext.
	 */
	private void parseLine() {
		MarkupStart previousMarkup, headerMarkup;

		/* reset the class variables for next line */
		lineBuffer.setLength(0);
		lineMarkup.clear();
		openTemplates = 0;
		previousMarkup = null;

		/* parse the line */
		parseLineStartMarkup();
		if(currentChar == '\n') {
			getNext();
			return;
		}

		parseInlineMarkup();

		/* check if the line is valid header */
		headerMarkup = createHeaderNode();

		/* generate the text content for the parsed markup (or the TextNode) */
		previousMarkup = headerMarkup;
		for(int i = 0; i < lineMarkup.size(); i++) {
			MarkupStart markupStart;

			markupStart = lineMarkup.get(i);
			if(markupStart.type == MarkupStart.MarkupType.START) {
				printDebug("START ", false);
			}

			printDebug("PROCESS markupStart " + markupStart.sourceLocation +
				", symbol: " + markupStart.symbol.character +
				", count: " + markupStart.count);

			if(markupStart.type == MarkupStart.MarkupType.NONE) {
				printDebug("Discarded the symbol");
				continue;
			}

			if(markupStart.symbol == symbolLut['=']) {
				continue;
			}

			handleMarkup(markupStart, previousMarkup);

			previousMarkup = markupStart;
		}

		createTextNode(null, previousMarkup);
	}

	/**
	 * Parse the markup that can be only at the start of line.
	 */
	private void parseLineStartMarkup() {
		/* trim the whitespace from the start of line */
		do {
			if(currentChar == '\n' || !isWhitespace(currentChar)) break;
		} while(getNext());

		if(currentChar == '\n') {
			itemListTruncate(1);
			return;
		}

		/* remove inline nodes from the parent list */
		if(parentList.size() > inlineFragmentIndex) {
			itemListTruncate(inlineFragmentIndex);
		}

		switch(currentChar) {
		/* header */
		case '=':
			parseHeaderMarkupStart();
			break;
		/* ruler */
		case '-':
			parseRuler();
			break;
		/* bullet list */
		case '*':
		/* numbered list */
		case '#':
		/* definition list */
		case ';':
		/* indentation */
		case ':':
			parseListItem();
			break;
		/* paragraph */
		case '\n':
			createParagraph();
			getNext();
			break;
		/* table */
		case '{':
			parseTable();
			break;
		default:
			createParagraphIfNeaded();
			break;
		}
		inlineWhitespaceConsumed = true;
		inlineFragmentIndex = parentList.size();
	}

	private void createParagraphIfNeaded() {
		if(getCurrentFragment().getType() != TextNode.PARAGRAPH_TYPE) {
			createParagraph();
		} else if(!inlineWhitespaceConsumed) {
			TextNode node;
			/* add extra whitespace */
			node = new TextNode(TextNode.PLAIN_TYPE);
			node.setTextContent(" ");
			getCurrentFragment().appendChild(node);
		}
	}

	private void parseRuler() {
		TextNode ruler;
		int i;

		for(i = 0; i < 4; i++) {
			if(currentChar != '-') break;
			lineBuffer.append(currentChar);
			getNext();
		}
		/*TODO: check the whitespaces */
		if(i != 4) {
			return;
		}

		printDebug("Ruler parsed.");

		itemListTruncate(1);

		ruler = new TextNode(TextNode.RULER_TYPE, null);
		getCurrentFragment().appendChild(ruler);
	}

	private void parseTable() {
	}

	/**
	 * Try to match the header markup for the line and create the TextNode for it.
	 */
	private MarkupStart createHeaderNode() {
		TextNode node;
		MarkupStart start, end;
		int i;

		/* check that the lineMarkup list isn't empty */
		if(lineMarkup.size() == 0) {
			return null;
		}

		/* header must start with equal symbol */
		start = lineMarkup.get(0);
		if(start.symbol != symbolLut['=']) {
			return null;
		}

		/* header must end with equal symbol */
		end = start.matchingMarkup;
		if(end == null || end != lineMarkup.get(lineMarkup.size() - 1)) {
			createParagraphIfNeaded();
			return null;
		}

		/* there must be only whitespace after the header */
		for(i = end.location + end.count; i < lineBuffer.length(); i++) {
			if(!isWhitespace(lineBuffer.charAt(i))) {
				createParagraphIfNeaded();
				return null;
			}
		}

		node = new TextNode(TextNode.HEADER_TYPE);
		getRoot().appendChild(node);
		itemListTruncate(1);
		parentList.add(node);

		/* remove the header symbols from lineMarkup list */
		lineMarkup.remove(0);
		lineMarkup.remove(lineMarkup.size() - 1);

		/* remove the end markup so that the text is trimmed just like the whitespace
		   before new line character */
		lineBuffer.setLength(end.location);

		return start;
	}

	/**
	 * Parse the inline markup at the line.
	 */
	private void parseInlineMarkup() {
		boolean wasWhitespace = false;
		do {
			/* return from the function at the newline character */
			if(currentChar == '\n') {
				/* handle the last markup of the line */
				handlePreviousMarkup();
				getNext();

				/* return if there isn't any multiline markup */
				if(openTemplates == 0) {
					return;
				}
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

		/* handle the last markup of the line */
		handlePreviousMarkup();
	}

	private MarkupStart appendMarkupStart(SymbolType symbol) {
		MarkupStart start = new MarkupStart();

		start.location = lineBuffer.length() - 1;
		start.sourceLocation = getSourceLocation();
		start.symbol = symbol;
		start.count = 1;
		start.type = MarkupStart.MarkupType.NONE;
		previousMarkup = start;
		lineMarkup.add(start);

		return start;
	}

	private void handleInlineMarkup(SymbolType symbol) {
		MarkupStart start;

		if(previousMarkup != null) {
			int lastMarkupIndex = lineMarkup.size() - 1;

			/* extend the previous markup if the symbol type allows it */
			if(previousChar == currentChar && symbol.multiple) {
				previousMarkup.count++;
				return;
			}

			/* process the previous markup */
			handlePreviousMarkup();
		}

		/* create new markup start for the symbol */
		start = appendMarkupStart(symbol);

		printDebug("Symbol: " + currentChar + " location: " + start.sourceLocation);
	}

	private void parseListItem() {
		int itemType = TextNode.LIST_ITEM_TYPE;
		boolean itemCreated = false;
		/* skip the root node */
		int i = 1;

		/* close the previous nodes */
		if(parentList.size() > i && parentList.get(i).getType() != TextNode.LIST_TYPE) {
			printDebug("Truncate, not list item " + parentList.get(i).getType() + " " + getSourceLocation());
			itemListTruncate(i);
		}

		do {
			boolean listSymbol;
			TextNode list;
			String parameter;

			listSymbol = true;
			parameter = null;

			switch(currentChar) {
			case '*':
				parameter = "ul";
				itemType = TextNode.LIST_ITEM_TYPE;
				break;
			case '#':
				parameter = "ol";
				itemType = TextNode.LIST_ITEM_TYPE;
				break;
			/*FIXME: following cases are not translated properly */
			case ';':
				parameter = "dl";
				itemType = TextNode.DEFINITION_LABEL_TYPE;
				break;
			case ':':
				parameter = "dl";
				itemType = TextNode.DEFINITION_ITEM_TYPE;
				break;
			case '\n':
				/*FIXME: this method shouldn't create anything if there is no text at the line */
				/*TODO: write some code to remove the generated TextNodes */
				return;
			default:
				listSymbol = false;
				break;
			}

			/* discard whitespace */
			if(isWhitespace(currentChar)) {
				getNext();
				continue;
			}

			/* end the loop if we hit the content of the list item */
			if(!listSymbol) {
				break;
			}

			/* get the current location in source file for debug messages */
			String sourceLocation = getSourceLocation();
			/* consume the list symbol */
			getNext();

			/* check if this is already in the parent list */
			if(i < parentList.size()) {
				list = parentList.get(i);

				/* previous sibling was list */
				if(list.getType() == TextNode.LIST_TYPE) {
					/* if the list doesn't have same type,
					   then end the previous list and create new one */
					if(!parameter.equals(list.getParameter())) {
						printDebug("New list, index: " + i + "/" + (parentList.size() - 1) + ", location" + sourceLocation);

						itemListTruncate(i);
						createList(parameter);
						i++;

						createListItem(itemType);
						itemCreated = true;
						i++;

						continue;
					}

					/* skip the list node */
					list = parentList.get(++i);
					itemCreated = false;

					/* just skip the list items */
					if(parameter.equals("dl") && (list.getType() == TextNode.DEFINITION_LABEL_TYPE ||
					   list.getType() == TextNode.DEFINITION_ITEM_TYPE)) {
						i++;
					} else if(list.getType() == TextNode.LIST_ITEM_TYPE) {
						i++;
					} else {
						getRoot().print(2);
						throw new Error("Should have been list item");
					}
				} else {
					getRoot().print(2);
					throw new Error("Should have been list. " + list.getType());
				}
				continue;
			}

			/* create new list and it's first item */
			createList(parameter);
			i++;

			createListItem(itemType);
			itemCreated = true;
			i++;
		} while(true);

		/* append new list item after the previous list item */
		if(!itemCreated) {
			itemListTruncate(i);
			createListItem(itemType);
		}
	}

	/**
	 * Helper method for creating a list TextNode.
	 */
	private void createList(String parameter) {
		TextNode list, parent;

		list = new TextNode(TextNode.LIST_TYPE, parameter);
		getCurrentFragment().appendChild(list);
		parentList.add(list);
	}

	/**
	 * Helper method for creating a list item TextNode.
	 */
	private void createListItem(int type) {
		TextNode item, list;

		list = getCurrentFragment();
		if(list.getType() == TextNode.LIST_ITEM_TYPE ||
		   list.getType() == TextNode.DEFINITION_ITEM_TYPE ||
		   list.getType() == TextNode.DEFINITION_LABEL_TYPE) {
			closePreviousFragment();
			list = getCurrentFragment();
		}

		if(list.getType() != TextNode.LIST_TYPE) {
			throw new Error("ERROR: must be list");
		}

		printDebug("New list item " + getSourceLocation());
		item = new TextNode(type);
		list.appendChild(item);
		parentList.add(item);
	}

	private void createParagraph() {
		TextNode paragraphNode;
		printDebug("New paragraph" + getSourceLocation());

		paragraphNode = new TextNode(TextNode.PARAGRAPH_TYPE);
		getRoot().appendChild(paragraphNode);
		itemListTruncate(1);
		parentList.add(paragraphNode);
	}

	private int getNextMarkupSymbolIndex(SymbolType symbol, int startIndex) {
		for(int i = startIndex - 1; i >= 0; i--) {
			MarkupStart start = lineMarkup.get(i);

			if(start.symbol == symbol) return i;
			if(start.symbol.priority > symbol.priority) break;
		}
		return -1;
	}

	private MarkupStart getMarkupSymbol(SymbolType symbol) {
		int index = getNextMarkupSymbolIndex(symbol, lineMarkup.size() - 1);

		if(index != -1) {
			return lineMarkup.get(index);
		}
		return null;
	}

	/**
	 * Validate the MarkupStart if the previous symbol was ending of inline markup.
	 */
	private void handlePreviousMarkup() {
		MarkupStart start, markup;
		int lastMarkupIndex = lineMarkup.size() - 1;

		if(previousMarkup == null) {
			return;
		}

		markup = lineMarkup.get(lastMarkupIndex);

		/* handle the markup ending */
		switch(previousMarkup.symbol.character) {
		case '\'':
			createTextStyleMarkup(markup);
			break;
		case ']':
			createLinkMarkup(markup);
			break;
		case '{':
			if(markup.count >= 2) {
				openTemplates++;
			}
			break;
		case '}':
			createTemplateMarkup(markup);
			break;
		case '>':
			start = getMarkupSymbol(symbolLut['<']);
			if(start == null) break;
			printDebug("HTML range " + start.location + ", " + markup.location);
			break;
		case '=':
			createHeaderMarkup(markup);
			break;
		default:
			break;
		}

		previousMarkup = null;
	}

	private void handleMarkup(MarkupStart markup, MarkupStart previousMarkup) {
		/* append the text content to previous TextNode */
		createTextNode(markup, previousMarkup);

		/* create TextNode for the markup */
		switch(markup.symbol.character) {
		case '\'':
			handleTextStyleMarkup(markup);
			break;

		case '[':
			createLinkNode(markup);
			break;
		case ']':
			endLinkNode(markup);
			break;

		case '{':
			createTemplateNode(markup);
			break;
		case '}':
			endTemplateNode(markup);
			break;

		case '<':
			break;
		case '>':
			MarkupStart start;
			start = getMarkupSymbol(symbolLut['<']);
			if(start == null) return;
			printDebug("HTML range " + start.location + ", " + markup.location);
			break;

		case '=':
			/* This is processed already */
			break;
		default:
			break;
		}
	}

	private void createTextNode(MarkupStart markup, MarkupStart previousMarkup) {
		TextNode node, parent;
		int startIndex, endIndex;

		/* check if the text is between last markup and line ending */
		if(markup == null) {
			endIndex = lineBuffer.length();
			/* trim the end */
			while(endIndex > 0 && isWhitespace(lineBuffer.charAt(endIndex - 1))) {
				endIndex--;
			}
			/* handle the case where the line had no markup */
			if(previousMarkup == null) {
				startIndex = 0;
			} else {
				startIndex = previousMarkup.location + previousMarkup.count;
			}
		} else {
			endIndex = markup.location;
			/* check if this is the first markup */
			if(previousMarkup == null) {
				startIndex = 0;
			} else {
				startIndex = previousMarkup.location + previousMarkup.count;
			}
		}

		/* remove whitespace from the start if the whitespace is given to previous node */
		if(inlineWhitespaceConsumed) {
			while(startIndex < lineBuffer.length() && isWhitespace(lineBuffer.charAt(startIndex))) {
				startIndex++;
			}
		}

		/* check if the substring is longer than zero */
		if(endIndex - startIndex <= 0) {
			return;
		}

		inlineWhitespaceConsumed = false;

		/* consume whitespace if the test ends with space */
		if(endIndex > 0 && isWhitespace(lineBuffer.charAt(endIndex - 1))) {
			inlineWhitespaceConsumed = true;
		}

		printDebug("Text start: " + startIndex + ", " + endIndex + " " + getSourceLocation());

		/* create the node for text content */
		parent = getCurrentFragment();
		node = new TextNode(TextNode.PLAIN_TYPE);
		node.setTextContent(lineBuffer.substring(startIndex, endIndex));

		/* append the text node into the parent */
		getCurrentFragment().appendChild(node);
	}

	/**
	 * Parse header start markup from the start of the line.
	 */
	private void parseHeaderMarkupStart() {
		MarkupStart start;
		int i;

		start = appendMarkupStart(symbolLut['=']);
		start.location = 0;

		for(i = 0; i < 6; i++) {
			if(currentChar != '=') break;
			lineBuffer.append(currentChar);
			getNext();
		}
		start.count = i;
		printDebug("Header parsed " + i + ".");
	}

	private void appendInlineTextNode(int type) {
		TextNode node;

		node = new TextNode(type);
		getCurrentFragment().appendChild(node);
		parentList.add(node);
	}

	private void createMarkupStart(MarkupStart start, MarkupStart end) {
		start.matchingMarkup = end;
		end.matchingMarkup = start;
		start.type = MarkupStart.MarkupType.START;
		end.type = MarkupStart.MarkupType.END;
	}

	/**
	 * Update the Markup start for text style markup.
	 */
	private void createTextStyleMarkup(MarkupStart end) {
		MarkupStart start;
		int quotes;

		/* find the start markup */
		start = getMarkupSymbol(end.symbol);
		if(start == null) return;
		if(start.matchingMarkup != null) return;

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
			start.location += start.count - quotes;
			start.count = quotes;
		}
		if(end.count > quotes) {
			end.location += end.count - quotes;
			end.count = quotes;
		}

		createMarkupStart(start, end);

		printDebug("Inline range " + start.location + ", " + end.location + "  " +
			lineBuffer.substring(start.location + start.count, end.location));
	}

	/**
	 * Generate TextNode for the text style markup.
	 */
	private void handleTextStyleMarkup(MarkupStart markup) {
		int quotes = markup.count;

		/* return if this end markup */
		if(markup.type == MarkupStart.MarkupType.END) {
			printDebug("End text style node");
			TextNode current = getCurrentFragment();
			if(current.getType() == TextNode.PLAIN_TYPE) {
				current = current.getParent();
			}
			if(quotes == 2) {
				if(current.getType() == TextNode.EM_TYPE) {
					closePreviousFragment();
				}
			} else if(quotes == 3) {
				if(current.getType() == TextNode.STRONG_TYPE) {
					closePreviousFragment();
				}
			} else if(quotes == 5) {
				if(current.getType() == TextNode.STRONG_TYPE) {
					current = current.getParent();
					if(current.getType() == TextNode.EM_TYPE) {
						closePreviousFragment();
						closePreviousFragment();
					}
				}
			}
			return;
		}

		/* put the quoted text into inline text node */
		if(quotes == 2) {
			appendInlineTextNode(TextNode.EM_TYPE);
		} else if(quotes == 3) {
			appendInlineTextNode(TextNode.STRONG_TYPE);
		} else if(quotes == 5) {
			TextNode parent, node;

			parent = getCurrentFragment();
			node = new TextNode(TextNode.EM_TYPE);
			parent.appendChild(node);
			parentList.add(node);

			appendInlineTextNode(TextNode.STRONG_TYPE);
		}
	}

	private void createLinkMarkup(MarkupStart end) {
		MarkupStart start;
		int brackets;

		start = getMarkupSymbol(symbolLut['[']);
		if(start == null) return;
		if(start.matchingMarkup != null) return;

		brackets = (end.count < start.count) ? end.count : start.count;
		if(brackets > 2) {
			brackets = 2;
		}
		if(start.count >= brackets) {
			start.location += start.count - brackets;
			start.count = brackets;
		}
		if(end.count >= brackets) {
			end.count = brackets;
		}

		createMarkupStart(start, end);

		printDebug("Link range " + start.location + ", " + end.location + "  " +
			lineBuffer.substring(start.location + start.count, end.location));
	}

	private void createLinkNode(MarkupStart start) {
		appendInlineTextNode(TextNode.LINK_TYPE);
	}

	private void endLinkNode(MarkupStart end) {
		TextNode current = getCurrentFragment();
		while(current.getType() != TextNode.LINK_TYPE) {
			current = current.getParent();
		}
		closePreviousFragment();
	}

	private void createTemplateMarkup(MarkupStart end) {
		MarkupStart start, prevLink;
		int markupIndex;

		/* there must be atleast two brackets */
		if(end.count == 1) {
			return;
		}
		openTemplates--;
		markupIndex = lineMarkup.size() - 1;

		/* find the start markup */
		while(true) {
			markupIndex = getNextMarkupSymbolIndex(symbolLut['{'], markupIndex);
			if(markupIndex == -1) return;

			start = lineMarkup.get(markupIndex);
			if(start.type == MarkupStart.MarkupType.START) {
				continue;
			}

			/* there must be atleast two brackets */
			if(start.count == 1) {
				continue;
			}
			break;
		}

		prevLink = getMarkupSymbol(symbolLut['[']);
		/* ignore the template if there is unfinished link */
		if(prevLink != null && start.location < prevLink.location && prevLink.type == MarkupStart.MarkupType.NONE) {
			printDebug("Warning: the template contains unfinished link.");
			return;
		}

		if(start.count > 2) {
			start.location += 2 - start.count;
			start.count = 2;
		}

		if(end.count > 2) {
			end.count = 2;
		}

		createMarkupStart(start, end);

		printDebug("Template range " + start.location + ", " + end.location + "  " +
			lineBuffer.substring(start.location + start.count, end.location));
	}

	private void createTemplateNode(MarkupStart start) {
		appendInlineTextNode(TextNode.TEMPLATE_TYPE);
	}

	private void endTemplateNode(MarkupStart end) {
		TextNode current = getCurrentFragment();
		while(current.getType() != TextNode.TEMPLATE_TYPE) {
			current = current.getParent();
		}
		closePreviousFragment();
	}

	private void createHeaderMarkup(MarkupStart end) {
		MarkupStart start;

		start = lineMarkup.get(0);

		if(start.symbol != symbolLut['=']) {
			return;
		}

		createMarkupStart(start, end);
	}
}
