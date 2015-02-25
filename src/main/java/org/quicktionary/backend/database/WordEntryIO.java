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
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

import org.quicktionary.backend.WordEntry;
import org.quicktionary.backend.TextNode;

public class WordEntryIO {
	protected long address;
	protected boolean modified;
	protected WordEntry data;

	protected WordEntryIO(WordEntry entry, long address) {
		this.data = entry;
		this.address = address;

		if(entry != null) {
			entry.setIO(this);
		}
	}

	public boolean isModified() {
		return modified;
	}

	protected void setModified(boolean modified) {
		this.modified = modified;
		this.data.setModified(modified);
	}

	protected void setData(byte[] buffer) {
		DataInputStream input;
		String word, source;
		TextNode root;
		int length;

		word = source = null;
		root = null;

		try {
			input = new DataInputStream(new ByteArrayInputStream(buffer));

			/* read the word */
			length = input.readInt();
			buffer = new byte[length];
			input.readFully(buffer);
			word = new String(buffer, "UTF-8");

			/* read the source */
			length = input.readInt();
			buffer = new byte[length];
			input.readFully(buffer);
			source = new String(buffer, "UTF-8");

			/* read the content of the page */
			length = input.readInt();
			if(length != 0) {
				buffer = new byte[length];
				input.readFully(buffer);
				root = TextNodeIO.decodeData(buffer);
			}

		} catch(UnsupportedEncodingException exception) {
		} catch(IOException exception) {
		}

		if(!this.data.getWord().equals(word)) {
			throw new Error("The word in the file doesn't match the WordEntry");
		}
		this.data.addSource(source);
		this.data.setContent(root);
	}

	protected byte[] getData() {
		ByteArrayOutputStream stream;
		DataOutputStream output;
		String word, source;

		word = data.getWord();
		source = data.getSource();

		try {
			byte[] buffer;

			stream = new ByteArrayOutputStream();
			output = new DataOutputStream(stream);

			/* write the word */
			if(word != null) {
				buffer = word.getBytes("UTF-8");
				output.writeInt(buffer.length);
				output.write(buffer);
			} else {
				output.writeInt(0);
			}

			/* write the source */
			if(source != null) {
				buffer = source.getBytes("UTF-8");
				output.writeInt(buffer.length);
				output.write(buffer);
			} else {
				output.writeInt(0);
			}

			/* write the content of the page */
			buffer = TextNodeIO.encodeData(data.getContent());
			output.writeInt(buffer.length);
			output.write(buffer);

			return stream.toByteArray();
		} catch(UnsupportedEncodingException exception) {
		} catch(IOException exception) {
		}
		return new byte[0];
	}
}
