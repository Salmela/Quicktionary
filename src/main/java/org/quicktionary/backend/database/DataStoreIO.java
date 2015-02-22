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
package org.quicktionary.backend.database;

import java.lang.String;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.nio.channels.FileChannel;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Collections;
import java.io.UnsupportedEncodingException;

import org.quicktionary.backend.WordEntry;

class DataStoreIO {
	private final static byte[] DATASTORE_HEADER_SIGNATURE = {'W', 'D', 'B', 0};

	private IndexIO index;
	private RandomAccessFile dataStore;
	private File dataStoreFile;

	private TreeMap<String, WordEntryIO> map;
	private List<WordEntryIO> changedEntries;

	public DataStoreIO(File dataStoreFile) {
		changedEntries = Collections.synchronizedList(new LinkedList<WordEntryIO>());
		System.out.println("DB: read new database at " + dataStoreFile);

		try {
			dataStore = new RandomAccessFile(dataStoreFile, "rw");
		} catch(IOException exception) {
			dataStore = null;
			return;
		}

		this.dataStoreFile = dataStoreFile;
		this.map = null;
		try {
			readDataStoreHeader();
		} catch(IOException exception) {
		}
	}

	public WordEntryIO createNewEntry(WordEntry entry) {
		WordEntryIO ioEntry;

		ioEntry = new WordEntryIO(entry, 0);
		markAsChanged(ioEntry);

		return ioEntry;
	}

	public void markAsChanged(WordEntryIO entry) {
		if(entry.isModified()) {
			return;
		}
		System.out.println("DB: a word marked as modified: " + entry.data.getWord() + ".");
		changedEntries.add(entry);
		entry.setModified(true);
	}

	private void readDataStoreHeader() throws IOException {
		File indexFile;
		String indexFilename;
		byte[] signature = new byte[DATASTORE_HEADER_SIGNATURE.length];
		FileChannel channel = dataStore.getChannel();

		if(channel.size() == 0) {
			initializeDatabase();
			return;
		}

		dataStore.seek(0);
		dataStore.read(signature);
		if(DATASTORE_HEADER_SIGNATURE.equals(signature)) {
			throw new Error("This is not word database file");
		}
		indexFilename = readIndexFilename();

		System.out.println("DB: loaded database index at \"" + indexFilename + "\"");

		indexFile = new File(indexFilename);

		/* check if the index file is accidentally removed */
		if(indexFile.exists()) {
			index = new IndexIO(this, indexFile);
			map = index.readIndex();
		} else {
			/*TODO: reconstruct the index */
			initializeDatabase();
		}
	}

	public TreeMap<String, WordEntryIO> getIndex() {
		return map;
	}

	protected String readIndexFilename() throws IOException {
		byte[] filenameBuffer;
		String filename;
		int length;

		length = dataStore.readInt();
		if(length >= 256 || length < 0) {
			throw new Error("The file is corrupted.");
		}

		filenameBuffer = new byte[length];
		dataStore.read(filenameBuffer);

		try {
			filename = new String(filenameBuffer, "UTF-8");
		} catch(UnsupportedEncodingException exception) {
			throw new Error("Your runtime doesn't support UTF-8.");
		}

		return filename;
	}

	protected void writeDataStoreHeader(String filename) throws IOException {
		dataStore.seek(0);
		dataStore.write(DATASTORE_HEADER_SIGNATURE);
		writeIndexFilename(filename);
	}

	private void writeIndexFilename(String filename) throws IOException {
		byte[] filenameBuffer = new byte[255];
		byte[] buffer;

		System.out.println("DB: write filename " + filename);
		try {
			buffer = filename.getBytes("UTF-8");
		} catch(UnsupportedEncodingException exception) {
			throw new Error("Your runtime doesn't support UTF-8.");
		}

		if(buffer.length >= 256) {
			throw new Error("The filename must be shorter than 255 bytes.");
		}

		System.arraycopy(buffer, 0, filenameBuffer, 0, buffer.length);
		dataStore.writeInt(buffer.length);
		dataStore.write(filenameBuffer);
	}

	/**
	 * Create new index file and write its filename to data store file.
	 */
	private void initializeDatabase() throws IOException {
		String indexFilename;

		if(dataStoreFile.getParent() == null) {
			indexFilename = "index.db";
		} else {
			indexFilename = dataStoreFile.getParent() + File.separator + "index.db";
		}
		System.out.println("DB: create new index at " + indexFilename);

		/* clear the file */
		dataStore.setLength(0);

		/* write new header */
		writeDataStoreHeader(indexFilename);
		index = new IndexIO(this, new File(indexFilename));
	}

	/**
	 * Read a single word entry from data store.
	 * @param entry The entry to be filled
	 */
	public void readWordEntry(WordEntryIO entry) throws IOException {
		byte[] buffer;
		int size;

		if(dataStore == null) {
			return;
		}

		dataStore.seek(entry.address);
		size = dataStore.readInt();
		buffer = new byte[size];

		dataStore.readFully(buffer);
		entry.data.setData(buffer);
	}

	/**
	 * Write all the changes to the database files.
	 * @param map The index of the database
	 */
	public void syncFile(TreeMap map) {
		try {
			pushChanges();
			index.writeIndex(map);
		} catch(IOException exception) {
		}
	}


	/**
	 * Write all words that are marked as modified to the data store.
	 */
	private void pushChanges() throws IOException {
		if(dataStore == null) {
			return;
		}

		System.out.println("DB: push changes");

		/* skip to the end of file */
		dataStore.seek(dataStore.length());

		/* block access to changedEntries list from every other thread */
		synchronized(changedEntries) {
			Iterator<WordEntryIO> iter = changedEntries.iterator();

			while(iter.hasNext()) {
				writeWord(iter.next());
				iter.remove();
			}
		}
	}

	private void writeWord(WordEntryIO entry) throws IOException {
		byte[] buffer;
		System.out.println("DB: word " + entry.data.getWord());

		/* get the data from the data structures */
		buffer = entry.data.getData();

		/* save the new location of the word */
		entry.address = dataStore.getFilePointer();
		entry.setModified(false);

		/* write data to the file */
		dataStore.writeInt(buffer.length);
		dataStore.write(buffer);
	}
}
