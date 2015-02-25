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

import java.util.TreeMap;
import java.util.Map;
import java.io.File;

import org.quicktionary.backend.WordEntry;
import org.quicktionary.backend.Configs;

/**
 * The database class for the quicktionary.
 */
public class WordDatabase {
	private DataStoreIO io;
	private TreeMap<String, WordEntryIO> map;

	private String searchWord;
	private Map.Entry<String, WordEntryIO> currentEntry;
	private Object lock;

	/**
	 * Create the database.
	 */
	public WordDatabase() {
		String filename;

		lock = this;
		filename = Configs.getOptionString("database");
		io = new DataStoreIO(new File(filename));
		map = this.io.getIndex();

		if(map == null) {
			map = new TreeMap<String, WordEntryIO>();
		}
	}

	/**
	 * Add new word to the database.
	 * @param word The word that we want to create
	 * @return A WordEntry for the word
	 */
	public WordEntry newWord(String word) {
		WordEntry entry = new WordEntry(word);
		synchronized(lock) {
			map.put(word, io.createNewEntry(entry));
		}
		return entry;
	}

	/**
	 * Remove a word from the database.
	 * @param word The word that we want to remove
	 */
	public void removeWord(String word) {
		synchronized(lock) {
			map.remove(word);
		}
	}

	/**
	 * Mark the word entry so that it will be written
	 * next time to the database.
	 * @param word The word that was modified
	 */
	public void updateWord(WordEntry entry) {
		io.markAsChanged(entry.getIO());
	}

	/**
	 * Check if the database already has the word.
	 * @param word The word to be checked
	 * @return True if the word is already in database
	 */
	public boolean containsWordEntry(String word) {
		return map.containsKey(word);
	}

	/**
	 * Get the data for the word.
	 * @param wordEntry The entry that we want to be filled.
	 */
	public void fetchPage(WordEntry wordEntry) {
		if(wordEntry.isLoaded()) {
			io.fetchWordEntry(wordEntry.getIO());
		}
	}

	/**
	 * Get a page for the word.
	 * @param word The word that we want to fetch
	 * @return The WordEntry for the word
	 */
	public WordEntry fetchWordEntry(String word) {
		WordEntryIO entry;

		synchronized(lock) {
			entry = map.get(word);
			if(entry == null) {
				return new WordEntry(word, null, null);
			}
		}
		return entry.data;
	}

	/**
	 * Requests items with key that has the word as prefix. This method
	 * itself doesn't give the items. You have to call the fetchResults
	 * method to get the items.
	 *
	 * @param word The prefix of wanted keys
	 */
	public void requestResults(String word) {
		synchronized(lock) {
			searchWord = word;
			currentEntry = map.ceilingEntry(word);
		}
	}

	/**
	 * Fetches requested items and inserts them to the entries array.
	 *
	 * @param entries The list to be filled
	 * @param count The number of items wanted
	 * @return The number of items found
	 */
	public int fetchResults(WordEntry[] entries, int count) {
		int i;

		synchronized(lock) {
			for(i = 0; i < count && currentEntry != null; i++) {
				if(!currentEntry.getKey().startsWith(searchWord)) break;

				entries[i] = currentEntry.getValue().data;
				currentEntry = map.higherEntry(currentEntry.getKey());
			}
		}

		/* append null terminator to the list */
		if(i < count) {
			entries[i] = null;
		}

		return i;
	}

	/**
	 * Write the WordEntry changes to the file.
	 */
	public void sync() {
		io.syncFile(map);
	}

	/**
	 * Close the database safely.
	 */
	public void close() {
		sync();
	}
}
