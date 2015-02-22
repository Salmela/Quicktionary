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

	protected boolean isModified() {
		return modified;
	}

	protected void setModified(boolean modified) {
		this.modified = modified;
	}

	protected void setData(byte[] buffer) {
		DataInputStream input;
		String word, source;
		int length;

		word = source = null;

		try {
			input = new DataInputStream(new ByteArrayInputStream(buffer));

			length = input.readInt();
			buffer = new byte[length];
			word = new String(buffer, "UTF-8");

			length = input.readInt();
			buffer = new byte[length];
			source = new String(buffer, "UTF-8");
		} catch(UnsupportedEncodingException exception) {
		} catch(IOException exception) {
		}

		this.data = new WordEntry(word, source, null);
	}

	protected byte[] getData() {
		ByteArrayOutputStream stream;
		DataOutputStream output;
		String word, source;

		word = data.getWord();
		source = data.getSource();

		try {
			stream = new ByteArrayOutputStream();
			output = new DataOutputStream(stream);

			if(word != null) {
				output.writeInt(word.length());
				output.write(word.getBytes("UTF-8"));
			} else {
				output.writeInt(0);
			}

			if(source != null) {
				output.writeInt(source.length());
				output.write(source.getBytes("UTF-8"));
			} else {
				output.writeInt(0);
			}

			return stream.toByteArray();
		} catch(UnsupportedEncodingException exception) {
		} catch(IOException exception) {
		}
		return new byte[0];
	}
}