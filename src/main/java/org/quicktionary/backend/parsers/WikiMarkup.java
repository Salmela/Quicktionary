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
	private int inlineFragmentIndex;
	private boolean inlineWhitespaceConsumed;

	private SymbolType[] symbolLut;

	/**
	 * Node in document tree. The node has type
	 * and children or text content.
	 *
	 * TODO: Rename to TextNode and move to own file.
	 */
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

		/*TODO: rename to setTextContent */
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

		public void removeChild(int index) {
			this.childs.remove(index);
		}

		public boolean isEmpty() {
			return (childs.size() == 0) && (content == null);
		}

		/**
		 * Print the parsing tree recursively.
		 *
		 * @param indentation The padding added to start of each line.
		 */
		public String print(int indentation) {
			StringBuilder treeString = new StringBuilder();
			print(treeString, indentation);
			System.out.print(treeString);
			return treeString.toString();
		}
		private void print(StringBuilder treeString, int indentation) {
			int i = indentation;

			while(i-- > 0) treeString.append(" ");
			treeString.append("Node type: " + type);

			if(parameter != null) {
				treeString.append(", parameter: " + parameter);
			}
			treeString.append("\n");

			if(content != null) {
				i = indentation + 2;
				while(i-- > 0) treeString.append(" ");

				treeString.append("Content: \"" + content + "\"\n");
				return;
			}

			for(TextFragment child : childs) {
				child.print(treeString, indentation + 2);
			}

			return;
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

			if(parameter != null && !parameter.equals(fragment.parameter)) {
				return false;
			/* check if one of the parameters is null and other isn't */
			} else if((parameter == null) != (fragment.parameter == null)) {
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
				return false;
			}

			for(int i = 0; i < childs.size(); i++) {
				if(!childs.get(i).equals(fragment.childs.get(i))) {
					return false;
				}
			}
			return true;
		}

		/**
		 * The method combines neighbour text childs into one.
		 */
		public void normalize() {
			StringBuilder normalizedText;
			TextFragment firstText = null;

			normalizedText = new StringBuilder();
			for(int j = 0; j < childs.size(); j++) {
				if(childs.get(j).getType() == TextFragment.PLAIN_TYPE) {
					normalizedText.append(childs.get(j).getTextContent());
					if(firstText != null) {
						removeChild(j);
						j--;
					} else {
						firstText = childs.get(j);
					}
				} else {
					if(firstText != null) {
						firstText.setContent(normalizedText.toString());
					}
					firstText = null;
					normalizedText.setLength(0);
				}
			}
			if(firstText != null) {
				firstText.setContent(normalizedText.toString());
			}
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

		public ArrayList<TextFragment> getChildren() {
			return childs;
		}

		public String getTextContent() {
			return content;
		}
	}

	/*TODO: rename to MarkupSymbol */
	private static class MarkupStart {
		public enum MarkupType {
			START,
			END,
			NONE
		}

		public SymbolType symbol;

		public long sourceLocation;
		public int location;
		public int count;

		/*TODO: remove */
		public int length;
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
		//content = new StringBuilder(256);
		lineBuffer = new StringBuilder(256);
		lineMarkup = new ArrayList<MarkupStart>(16);
		itemList = new ArrayList<TextFragment>();

		createSymbolLut();
	}

	/**
	 * Get the highest fragment.
	 */
	private TextFragment getCurrentFragment() {
		if(itemList.size() == 0) {
			return null;
		}
		return itemList.get(itemList.size() - 1);
	}

	private void itemListTruncate(int newSize) {
		if(itemList.size() == newSize) return;
		if(itemList.size() < newSize) {
			throw new Error("The new size must be less than the current size.");
		}

		/* normalize the text nodes */
		for(int i = itemList.size() - 1; i >= newSize; i--) {
			itemList.get(i).normalize();
		}

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

		itemList.clear();

		/* add the root node */
		itemList.add(new TextFragment(0));
		inlineFragmentIndex = itemList.size();
		inlineWhitespaceConsumed = true;

		/* parse every line from the reader */
		while(currentChar != 0) {
			parseLine();
		}

		/* end all open nodes */
		itemListTruncate(1);

		return true;
	}

	public TextFragment getRoot() {
		return itemList.get(0);
	}

	/**
	 * Parse and generate parsing tree for single line of wikitext.
	 */
	private void parseLine() {
		MarkupStart previousMarkup, headerMarkup;

		/* reset the class variables for next line */
		lineBuffer.setLength(0);
		lineMarkup.clear();

		/* parse the line */
		parseLineStartMarkup();
		parseInlineMarkup();

		/* check if the line is valid header */
		headerMarkup = finalizeHeader();

		/* generate the text content for the parsed markup (or the TextFragment) */
		previousMarkup = headerMarkup;
		for(int i = 0; i < lineMarkup.size(); i++) {
			MarkupStart markupStart;

			markupStart = lineMarkup.get(i);
			if(markupStart.length > 0) {
				System.out.print("START ");
			}

			System.out.println("PROCESS markupStart " + markupStart.sourceLocation +
				", symbol: " + markupStart.symbol.character +
				", count: " + markupStart.count);

			if(markupStart.type == MarkupStart.MarkupType.NONE) {
				System.out.println("Discarded the symbol");
				continue;
			}

			finalizeMarkup(markupStart, previousMarkup);

			previousMarkup = markupStart;
		}

		finalizeText(null, previousMarkup);
	}

	/**
	 * Parse the markup that can be only at the start of line.
	 */
	private void parseLineStartMarkup() {
		/* trim the whitespace from the start of line */
		do {
			if(currentChar == '\n' || !isWhitespace(currentChar)) break;
		} while(getNext());

		if(itemList.size() > inlineFragmentIndex) {
			itemListTruncate(inlineFragmentIndex);
		}

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
			/* create new paragraph if neaded */
			if(getCurrentFragment().getType() != TextFragment.PARAGRAPH_TYPE) {
				createParagraph();
			} else if(!inlineWhitespaceConsumed) {
				TextFragment fragment;
				/* add extra whitespace */
				fragment = new TextFragment(TextFragment.PLAIN_TYPE);
				fragment.setContent(" ");
				getCurrentFragment().appendChild(fragment);
				inlineWhitespaceConsumed = true;
				return;
			}
			break;
		}
		inlineWhitespaceConsumed = false;
		inlineFragmentIndex = itemList.size();
	}

	private void parseTable() {
	}

	/**
	 * Try to match the header markup for the line and create the TextFragment for it.
	 */
	private MarkupStart finalizeHeader() {
		TextFragment fragment;
		MarkupStart start, end;
		int i;

		/* if there is no change that this is header then return */
		if(lineMarkup.size() < 2) {
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
			return null;
		}

		/* there must be only whitespace after the header */
		for(i = end.location + end.count + 1; i < lineBuffer.length(); i++) {
			System.out.println("TEXT " + lineBuffer.charAt(i));
			if(!isWhitespace(lineBuffer.charAt(i))) {
				return null;
			}
		}
		System.out.println("length of the header " + start.length);

		fragment = new TextFragment(TextFragment.HEADER_TYPE);
		getRoot().appendChild(fragment);
		itemListTruncate(1);
		itemList.add(fragment);

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
				getNext();
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

		/* handle the last markup */
		handlePreviousMarkup();
	}

	private MarkupStart appendMarkupStart(SymbolType symbol) {
		MarkupStart start = new MarkupStart();
		start.location = lineBuffer.length() - 1;
		start.sourceLocation = getAddress();
		start.symbol = symbol;
		start.count = 1;
		start.length = -1;
		start.type = MarkupStart.MarkupType.NONE;
		lineMarkup.add(start);
		return start;
	}

	private void handleInlineMarkup(SymbolType symbol) {
		MarkupStart start;

		if(lineMarkup.size() > 0) {
			MarkupStart previousMarkup;
			int lastMarkupIndex = lineMarkup.size() - 1;

			previousMarkup = lineMarkup.get(lastMarkupIndex);

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

		System.out.println("Symbol: " + currentChar + " location: " + start.location);
	}

	private void parseListItem() {
		boolean itemCreated = false;
		/* skip the root node */
		int i = 1;

		/* close the previous nodes */
		if(itemList.size() > i && itemList.get(i).getType() != TextFragment.LIST_TYPE) {
			System.out.println("Truncate, not list item " + itemList.get(i).getType() + " " + getSourceLocation());
			itemListTruncate(i);
		}

		do {
			boolean listSymbol;
			TextFragment list;
			String parameter;

			listSymbol = true;
			parameter = null;

			switch(currentChar) {
			case '*':
				parameter = "ul";
				break;
			case '#':
				parameter = "ol";
				break;
			/*FIXME: following cases are not translated properly */
			case ';':
				parameter = "dt";
				break;
			case ':':
				parameter = "dd";
				break;
			case '\n':
				/*FIXME: this method shouldn't create anything if there is no text at the line */
				/*TODO: write some code to remove the generated TextFragments */
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
			/* consume the list character */
			getNext();

			/*TODO: simplify the following mess */

			/* check if there is already a list */
			if(i < itemList.size()) {
				list = itemList.get(i);

				/* it's a list */
				if(list.getType() == TextFragment.LIST_TYPE) {
					/* if the list doesn't have same type,
					   then end the previous list and create new one */
					if(!parameter.equals(list.getParameter())) {
						System.out.println("New list " + sourceLocation);

						itemListTruncate(i);
						createList(parameter);

						createListItem();
						itemCreated = true;
						i++;

						continue;
					}

					/* skip the list node */
					list = itemList.get(++i);
					itemCreated = false;

					/* just skip the list items */
					if(list.getType() == TextFragment.LIST_ITEM_TYPE) {
						i++;
					} else {
						System.out.println("ERROR ERROR impossible happened " + sourceLocation);
						return;
					}
				} else {
					System.out.println("ERROR ERROR impossible happened " + sourceLocation);
					return;
				}
				continue;
			}

			/* create new list and it's first item */
			System.out.println("New level list " + sourceLocation);
			list = createList(parameter);
			i++;

			createListItem();
			itemCreated = true;
			i++;
		} while(true);

		/* append new list item after the previous list item */
		if(!itemCreated) {
			itemListTruncate(i);
			createListItem();
		}
	}

	/**
	 * Helper method for creating a list TextFragment.
	 */
	private TextFragment createList(String parameter) {
		TextFragment list, parent;

		if(itemList.size() == 0) {
			parent = getRoot();
		} else {
			parent = itemList.get(itemList.size() - 1);
		}

		list = new TextFragment(TextFragment.LIST_TYPE, parameter);
		parent.appendChild(list);
		itemList.add(list);

		return list;
	}

	/**
	 * Helper method for creating a list item TextFragment.
	 */
	private void createListItem() {
		TextFragment item, list;

		list = itemList.get(itemList.size() - 1);
		if(list.getType() == TextFragment.LIST_ITEM_TYPE) {
			itemListTruncate(itemList.size() - 1);
			list = itemList.get(itemList.size() - 1);
		}

		if(list.getType() != TextFragment.LIST_TYPE) {
			System.out.println("ERROR: must be list");
		}

		System.out.println("New list item " + getSourceLocation());
		item = new TextFragment(TextFragment.LIST_ITEM_TYPE);
		list.appendChild(item);
		itemList.add(item);
	}

	private void createParagraph() {
		TextFragment paragraphNode;
		System.out.println("New paragraph" + getSourceLocation());

		paragraphNode = new TextFragment(TextFragment.PARAGRAPH_TYPE);
		getRoot().appendChild(paragraphNode);
		itemListTruncate(1);
		itemList.add(paragraphNode);
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

	/**
	 * Validate the MarkupStart if the previous symbol was ending of inline markup.
	 */
	private void handlePreviousMarkup() {
		MarkupStart start, markup;
		int lastMarkupIndex = lineMarkup.size() - 1;

		if(lineMarkup.size() == 0) {
			return;
		}

		markup = lineMarkup.get(lastMarkupIndex);

		/* handle the markup ending */
		switch(markup.symbol.character) {
		case '\'':
			createTextStyleMarkup(markup);
			break;
		case ']':
			createLinkMarkup(markup);
			break;
		case '}':
			createTemplateMarkup(markup);
			break;
		case '>':
			start = getMarkupSymbol(symbolLut['<']);
			if(start == null) return;
			System.out.println("HTML range " + start.location + ", " + markup.location);
			break;
		case '=':
			createHeaderMarkup(markup);
			break;
		default:
			break;
		}
	}

	private void finalizeMarkup(MarkupStart markup, MarkupStart previousMarkup) {
		/* append the text content to previous TextFragment */
		finalizeText(markup, previousMarkup);

		/* create TextFragment for the markup */
		switch(markup.symbol.character) {
		case '\'':
			finalizeTextStyleMarkup(markup);
			break;

		case '[':
			finalizeLinkMarkup(markup);
			break;
		case ']':
			finalizeLinkMarkupEnd(markup);
			break;

		case '{':
			finalizeTemplateMarkup(markup);
			break;
		case '}':
			finalizeTemplateMarkupEnd(markup);
			break;

		case '<':
			MarkupStart start;
			start = getMarkupSymbol(symbolLut['<']);
			if(start == null) return;
			System.out.println("HTML range " + start.location + ", " + markup.location);
			break;
		case '>':
			break;

		case '=':
			/* This is processed already */
			break;
		default:
			break;
		}
	}

	private void finalizeText(MarkupStart markup, MarkupStart previousMarkup) {
		TextFragment fragment, parent;
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

		System.out.println("Inline whitespace restored.");
		inlineWhitespaceConsumed = false;
		System.out.println("Text ends with \'" + lineBuffer.charAt(endIndex - 1) + "\'.");

		/* consume whitespace if the test ends with space */
		if(endIndex > 0 && isWhitespace(lineBuffer.charAt(endIndex - 1))) {
			System.out.println("Inline whitespace consumed.");
			inlineWhitespaceConsumed = true;
		}

		System.out.println("Text start: " + startIndex + ", " + endIndex + " " + getSourceLocation());

		/* create the node for text content */
		parent = itemList.get(itemList.size() - 1);
		fragment = new TextFragment(TextFragment.PLAIN_TYPE);
		fragment.setContent(lineBuffer.substring(startIndex, endIndex));

		/* append the text node into the parent */
		getCurrentFragment().appendChild(fragment);
	}

	/**
	 * Parse header start markup from the start of the line.
	 */
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

	private void appendInlineTextFragment(int type) {
		TextFragment fragment;

		fragment = new TextFragment(type);
		getCurrentFragment().appendChild(fragment);
		itemList.add(fragment);
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

		start.length = end.location - start.location;
		createMarkupStart(start, end);

		System.out.println("Inline range " + start.location + ", " + end.location + "  " +
			lineBuffer.substring(start.location + start.count, end.location));
	}

	/**
	 * Generate TextFragment for the text style markup.
	 */
	private void finalizeTextStyleMarkup(MarkupStart markup) {
		int quotes = markup.count;

		/* return if this end markup */
		if(markup.type == MarkupStart.MarkupType.END) {
			System.out.println("End text style node");
			TextFragment current = getCurrentFragment();
			if(current.getType() == TextFragment.PLAIN_TYPE) {
				current = current.getParent();
			}
			if(quotes == 2) {
				if(current.getType() == TextFragment.EM_TYPE) {
					itemListTruncate(itemList.size() - 1);
				}
			} else if(quotes == 3) {
				if(current.getType() == TextFragment.STRONG_TYPE) {
					itemListTruncate(itemList.size() - 1);
				}
			} else if(quotes == 5) {
				if(current.getType() == TextFragment.STRONG_TYPE) {
					current = current.getParent();
					if(current.getType() == TextFragment.EM_TYPE) {
						itemListTruncate(itemList.size() - 2);
					}
				}
			}
			return;
		}

		/* put the quoted text into inline text fragment */
		if(quotes == 2) {
			appendInlineTextFragment(TextFragment.EM_TYPE);
		} else if(quotes == 3) {
			appendInlineTextFragment(TextFragment.STRONG_TYPE);
		} else if(quotes == 5) {
			TextFragment parent, node;

			parent = getCurrentFragment();
			node = new TextFragment(TextFragment.EM_TYPE);
			parent.appendChild(node);
			itemList.add(node);

			appendInlineTextFragment(TextFragment.STRONG_TYPE);
		}
	}

	private void createLinkMarkup(MarkupStart end) {
		MarkupStart start;
		int brackets;

		start = getMarkupSymbol(symbolLut['[']);
		if(start == null) return;
		if(start.length > 0) return;

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

		start.length = end.location - start.location;
		createMarkupStart(start, end);

		System.out.println("Link range " + start.location + ", " + end.location + "  " +
			lineBuffer.substring(start.location + start.count, end.location));
	}

	private void finalizeLinkMarkup(MarkupStart start) {
		appendInlineTextFragment(TextFragment.LINK_TYPE);
	}

	private void finalizeLinkMarkupEnd(MarkupStart end) {
		TextFragment current = getCurrentFragment();
		while(current.getType() != TextFragment.LINK_TYPE) {
			current = current.getParent();
		}
		itemListTruncate(itemList.size() - 1);
	}

	private void createTemplateMarkup(MarkupStart end) {
		MarkupStart start, prevLink;

		/* there must be atleast two brackets */
		if(end.count == 1) {
			return;
		}

		start = getMarkupSymbol(symbolLut['{']);
		if(start == null) return;
		if(start.type == MarkupStart.MarkupType.START) {
			throw new Error("We should try to find earlier starting markup");
		}

		/* there must be atleast two brackets */
		if(start.count == 1) {
			throw new Error("We should try to find earlier starting markup");
		}

		prevLink = getMarkupSymbol(symbolLut['[']);
		/* ignore the template if there is unfinished link */
		if(prevLink != null && start.location < prevLink.location && prevLink.type == MarkupStart.MarkupType.NONE) {
			System.out.println("Warning: the template contains unfinished link.");
			return;
		}

		if(start.count > 2) {
			start.location += 2 - start.count;
			start.count = 2;
		}

		if(end.count > 2) {
			end.count = 2;
		}

		start.length = end.location - start.location;
		createMarkupStart(start, end);

		System.out.println("Template range " + start.location + ", " + end.location + "  " +
			lineBuffer.substring(start.location + start.count, end.location));
	}

	private void finalizeTemplateMarkup(MarkupStart start) {
		appendInlineTextFragment(TextFragment.TEMPLATE_TYPE);
	}

	private void finalizeTemplateMarkupEnd(MarkupStart end) {
		TextFragment current = getCurrentFragment();
		while(current.getType() != TextFragment.TEMPLATE_TYPE) {
			current = current.getParent();
		}
		itemListTruncate(itemList.size() - 1);
	}

	private void createHeaderMarkup(MarkupStart end) {
		MarkupStart start;

		start = lineMarkup.get(0);

		if(start.symbol != symbolLut['=']) {
			return;
		}

		start.length = end.location - start.location + start.count;
		createMarkupStart(start, end);
	}
}
