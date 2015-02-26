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
package org.quicktionary.backend;

import java.util.ArrayList;

/**
 * Node in document tree. The node has type
 * and children or text content.
 */
public class TextNode {
	private TextNode parent;
	private int type;
	private String content;
	private ArrayList<TextNode> childs;
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

	public TextNode(int type, String parameter) {
		this.parent = null;
		this.type = type;
		this.content = null;
		this.childs = new ArrayList<TextNode>();
		this.parameter = parameter;
	}

	public TextNode(int type) {
		this(type, null);
	}

	public void setTextContent(String textContent) {
		if(childs.size() != 0) {
			throw new Error("TextNode must have only child nodes or text content.");
		}
		this.content = textContent;
	}

	/**
	 * Add child to the TextNode.
	 * The method returns added node, so that the unit tests can
	 * be written more compactly.
	 *
	 * @param node The new child for the node
	 * @return The added node
	 */
	public TextNode appendChild(TextNode node) {
		if(content != null) {
			throw new Error("TextNode must have only child nodes or text content.");
		}
		this.childs.add(node);
		node.parent = this;

		return node;
	}

	public TextNode prependChild(TextNode node) {
		if(content != null) {
			throw new Error("TextNode must have only child nodes or text content.");
		}
		this.childs.add(0, node);
		node.parent = this;

		return node;
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

		for(TextNode child : childs) {
			child.print(treeString, indentation + 2);
		}

		return;
	}

	/**
	 * Check that nodes are equal.
	 * This method should be only used in tests.
	 *
	 * @return True if the nodes are equal
	 */
	public boolean equals(Object object) {
		TextNode node;

		if(object == null) {
			return false;
		}

		if(!(object instanceof TextNode)) {
			return false;
		}

		node = (TextNode)object;

		if(node.type != type) {
			return false;
		}

		if(parameter != null && !parameter.equals(node.parameter)) {
			return false;
		/* check if one of the parameters is null and other isn't */
		} else if((parameter == null) != (node.parameter == null)) {
			return false;
		}

		if(content != null) {
			if(!content.equals(node.content)) {
				return false;
			}
			if(childs.size() != 0) {
				System.out.println("ERROR: the node has invalid content");
				return false;
			}
		}

		if(childs.size() != node.childs.size()) {
			return false;
		}

		for(int i = 0; i < childs.size(); i++) {
			if(!childs.get(i).equals(node.childs.get(i))) {
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
		TextNode firstText = null;

		normalizedText = new StringBuilder();
		for(int j = 0; j < childs.size(); j++) {
			if(childs.get(j).getType() == TextNode.PLAIN_TYPE) {
				normalizedText.append(childs.get(j).getTextContent());
				if(firstText != null) {
					removeChild(j);
					j--;
				} else {
					firstText = childs.get(j);
				}
			} else {
				if(firstText != null) {
					firstText.setTextContent(normalizedText.toString());
				}
				firstText = null;
				normalizedText.setLength(0);
			}
		}
		if(firstText != null) {
			firstText.setTextContent(normalizedText.toString());
		}
	}

	public TextNode getParent() {
		return parent;
	}

	public int getType() {
		return type;
	}

	public String getParameter() {
		return parameter;
	}

	public ArrayList<TextNode> getChildren() {
		return childs;
	}

	public String getTextContent() {
		return content;
	}
}
