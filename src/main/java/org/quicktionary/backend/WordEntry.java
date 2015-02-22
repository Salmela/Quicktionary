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
package org.quicktionary.backend;

import static org.quicktionary.backend.parsers.WikiMarkup.TextFragment;
import static org.quicktionary.backend.WordDatabaseIO.WordEntryIO;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

/**
 * This class contains all information about particular word.
 */
public class WordEntry {
	private String word, source;
	private TextFragment content;
	private WordEntryIO io;

	public WordEntry(String word, String source, TextFragment content) {
		this.word = word;
		this.source = source;
		this.content = content;
	}

	public WordEntry(String word) {
		this(word, null, null);
	}

	public void addSource(String source) {
		this.source = source;
	}

	public void setContent(TextFragment content) {
		this.content = content;
	}

	public String getWord() {
		return word;
	}

	public TextFragment getContent() {
		return content;
	}

	public String getSource() {
		return source;
	}

	public boolean isLoaded() {
		return content != null;
	}

	protected void setIO(WordEntryIO io) {
		this.io = io;
	}

	protected WordEntryIO getIO() {
		return io;
	}

	protected void setData(byte[] buffer) {
		DataInputStream input;
		byte[] buffer;
		int length;

		input = new DataInputStream(new ByteArrayInputStream(buffer));

		length = input.readInt();
		buffer = new byte[length];
		word = new String(buffer, "UTF-8");

		length = input.readInt();
		buffer = new byte[length];
		source = new String(buffer, "UTF-8");
	}

	protected byte[] getData() {
		DataOutputStream output;
		output = new DataOutputStream(new ByteArrayOutputStream());

		output.writeInt(word.length());
		output.write(word.getBytes("UTF-8"));

		output.writeInt(source.length());
		output.write(source.getBytes("UTF-8"));

		return output.toByteArray();
	}
}
