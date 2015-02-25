/* Quicktionary backend - The data structure for the word information
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
package org.quicktionary.backend.database;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataInput;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

import java.util.ArrayList;

import org.quicktionary.backend.TextNode;

class TextNodeIO {
	protected static TextNode decodeData(byte[] buffer) {
		DataInputStream input;

		try {
			input = new DataInputStream(new ByteArrayInputStream(buffer));
			return readTextNode(input, null);

		} catch(UnsupportedEncodingException exception) {
		} catch(IOException exception) {
			System.out.println("Failed to decode the data");
		}
		return null;
	}

	private static TextNode readTextNode(DataInput input, TextNode parent) throws IOException {
		TextNode node;
		int length, type, childCount;
		String parameter;
		byte[] buffer;

		type = input.readInt();
		childCount = input.readInt();

		/* read the parameter */
		length = input.readInt();
		buffer = new byte[length];
		input.readFully(buffer);
		parameter = new String(buffer, "UTF-8");

		/* create the node */
		node = new TextNode(type, parameter);
		if(parent != null) {
			parent.appendChild(node);
		}

		if(childCount == -1) {
			String textContent;

			length = input.readInt();
			buffer = new byte[length];
			input.readFully(buffer);
			textContent = new String(buffer, "UTF-8");
			node.setTextContent(textContent);

			return node;
		}

		/* read the childs */
		for(int i = 0; i < childCount; i++) {
			readTextNode(input, node);
		}
		return node;
	}

	protected static byte[] encodeData(TextNode node) {
		ByteArrayOutputStream stream;
		DataOutputStream output;

		if(node == null) {
			return new byte[0];
		}

		try {
			stream = new ByteArrayOutputStream();
			output = new DataOutputStream(stream);

			writeTextNode(output, node);

			return stream.toByteArray();

		} catch(UnsupportedEncodingException exception) {
		} catch(IOException exception) {
			System.out.println("Failed to encode the data");
		}
		return new byte[0];
	}

	private static void writeTextNode(DataOutput output, TextNode node) throws IOException {
		ArrayList<TextNode> childs;
		String parameter, textContent;
		byte[] buffer;
		int childCount;

		childs = node.getChildren();
		textContent = node.getTextContent();
		parameter = node.getParameter();
		childCount = childs.size();

		/* mark that the node contains only text */
		if(textContent != null) {
			childCount = -1;
		}

		output.writeInt(node.getType());
		output.writeInt(childCount);

		/* write the parameter */
		if(parameter != null) {
			buffer = parameter.getBytes("UTF-8");
			output.writeInt(buffer.length);
			output.write(buffer);
		} else {
			output.writeInt(0);
		}

		if(textContent != null) {
			buffer = textContent.getBytes("UTF-8");
			output.writeInt(buffer.length);
			output.write(buffer);
			return;
		}

		/* read the childs */
		for(TextNode child : childs) {
			writeTextNode(output, child);
		}
	}
}
