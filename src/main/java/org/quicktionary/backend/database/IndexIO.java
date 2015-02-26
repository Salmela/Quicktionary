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

import java.lang.String;
import java.io.File;
import java.io.IOException;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;

import java.util.Set;
import java.util.AbstractSet;
import java.util.Map;
import java.util.AbstractMap;
import java.util.TreeMap;
import java.util.SortedMap;
import java.util.Iterator;
import java.util.Comparator;
import java.io.UnsupportedEncodingException;
import java.lang.UnsupportedOperationException;

import org.quicktionary.backend.WordEntry;

class IndexIO {
	private final static byte[] INDEX_HEADER_SIGNATURE = {'I', 'D', 'B', 0};

	private DataStoreIO io;
	private File indexFile;

	private final class SortedArray<K, V> extends AbstractMap<K, V> implements SortedMap<K, V> {
		private Map.Entry<K,V>[] entries;

		public SortedArray(Map.Entry<K,V>[] entries) {
			this.entries = entries;
		}

		private final class SortedSet extends AbstractSet<Map.Entry<K, V>>  {
			private final class SortedIterator implements Iterator<Map.Entry<K, V>> {
				private int index;
				public boolean hasNext() {
					return index == entries.length - 1;
				}
				public Map.Entry<K, V> next() {
					return entries[index++];
				}
				public void remove() {
					throw new UnsupportedOperationException();
				}
			}
			public boolean add(Map.Entry<K, V> e) {
				throw new UnsupportedOperationException();
			}
			public boolean contains(Object o) {
				return SortedArray.this.containsValue(o);
			}
			public boolean isEmpty() {
				return SortedArray.this.isEmpty();
			}
			public Iterator<Map.Entry<K, V>> iterator() {
				return new SortedIterator();
			}
			public Object[] toArray() {
				return entries;
			}
			public int size() {
				return entries.length;
			}
		}

		public Comparator<K> comparator() {
			return null;
		}
		public Set<Map.Entry<K, V>> entrySet() {
			return new SortedSet();
		}
		public K firstKey() {
			return entries[0].getKey();
		}
		public K lastKey() {
			return entries[entries.length - 1].getKey();
		}
		public SortedMap<K,V> headMap(K toKey) {
			throw new Error("Unimplemented");
		}
		public SortedMap<K,V> tailMap(K fromKey) {
			throw new Error("Unimplemented");
		}
		public SortedMap<K,V> subMap(K fromKey, K toKey) {
			throw new Error("Unimplemented");
		}
	}

	public IndexIO(DataStoreIO io, File indexFile) {
		this.io = io;
		this.indexFile = indexFile;
	}

	/**
	 * Read the index of the database.
	 */
	public TreeMap<String, WordEntryIO> readIndex() throws IOException {
		byte[] signature = new byte[INDEX_HEADER_SIGNATURE.length];
		DataInputStream stream;
		TreeMap<String, WordEntryIO> map;

		System.out.println("DB: read the index at " + indexFile);

		try {
			stream = new DataInputStream(new FileInputStream(indexFile));
		} catch(IOException exception) {
			System.out.println("Failed to open the file " + exception);
			return null;
		}

		map = null;
		try {
			/* check the file signature */
			stream.read(signature);
			if(INDEX_HEADER_SIGNATURE.equals(signature)) {
				stream.close();
				throw new Error("This file is not database index.");
			}

			/* read the data and put it into TreeMap */
			map = new TreeMap<String, WordEntryIO>(readSortedList(stream));
		} catch(UnsupportedEncodingException exception) {
		} catch(IOException exception) {
		}

		stream.close();
		return map;
	}

	/**
	 * Write the index of the database to another file.
	 */
	public void writeIndex(TreeMap<String, WordEntryIO> map) throws IOException {
		DataOutputStream stream;
		String filename, oldFilename;

		/* create filename for the new index file */
		oldFilename = indexFile.toString();
		if(oldFilename.charAt(oldFilename.length() - 1) == '2') {
			filename = oldFilename.substring(0, oldFilename.length() - 1);
		} else {
			filename = oldFilename + "2";
		}
		System.out.println("DB: write temporary index at " + filename);

		stream = null;
		try {
			stream = new DataOutputStream(new FileOutputStream(filename, false));
			/* write the file signature */
			stream.write(INDEX_HEADER_SIGNATURE);

			/* write the data from the TreeMap */
			writeSortedList(stream, map);
		} catch(UnsupportedEncodingException exception) {
		} catch(IOException exception) {
		}

		if(stream != null) {
			stream.close();
		}
		io.writeDataStoreHeader(filename);
	}

	/**
	 * Read a sorted array from the index.
	 */
	private SortedArray<String, WordEntryIO> readSortedList(DataInputStream stream) throws IOException {
		Map.Entry<String, WordEntryIO>[] entries;

		int size = stream.readInt();
		entries = (Map.Entry<String, WordEntryIO>[])new Map.Entry[size];

		/* read sorted list of the words and their addresses */
		for(int i = 0; i < size; i++) {
			WordEntryIO entry;
			int length;
			long address;
			byte[] buffer;
			String word, description;

			/* read the word */
			length = stream.readInt();
			buffer = new byte[length];
			stream.read(buffer);
			word = new String(buffer, "UTF-8");

			/* read the description */
			length = stream.readInt();
			buffer = new byte[length];
			stream.read(buffer);
			description = new String(buffer, "UTF-8");

			/* append the address of the word */
			address = stream.readLong();

			/* create new entry to the entries array */
			entry = createWordEntry(word, address);
			entries[i] = new AbstractMap.SimpleEntry<String, WordEntryIO>(word, entry);

			if(description.length() > 0) {
				WordEntry data = entry.data;
				data.setDescription(description);
			}
		}
		return new SortedArray<String, WordEntryIO>(entries);
	}

	private WordEntryIO createWordEntry(String word, long address) {
		WordEntryIO entryIO;
		WordEntry entry;

		entry = new WordEntry(word, null, null);
		entryIO = new WordEntryIO(entry, address);

		return entryIO;
	}

	private void writeSortedList(DataOutputStream stream, TreeMap<String, WordEntryIO> map) throws IOException {
		stream.writeInt(map.size());

		for(Map.Entry<String, WordEntryIO> entry : map.entrySet()) {
			byte[] buffer;

			/* write the word */
			buffer = entry.getKey().getBytes("UTF-8");
			stream.writeInt(buffer.length);
			stream.write(buffer);

			/* write the description */
			WordEntry data = entry.getValue().data;
			if(data.getDescription() != null) {
				buffer = data.getDescription().getBytes("UTF-8");
				stream.writeInt(buffer.length);
				stream.write(buffer);
			} else {
				stream.writeInt(0);
			}

			/* append the address of the word */
			stream.writeLong(entry.getValue().address);
		}
	}
}
