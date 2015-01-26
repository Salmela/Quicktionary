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

import java.lang.UnsupportedOperationException;
import java.util.TreeMap;
import java.util.Map;

/**
 * The database class for the quicktionary.
 */
public class WordDatabase {
	private Quicktionary dictionary;
	private TreeMap<String, String> map;

	private String searchWord;
	private Map.Entry<String,String> currentEntry;

	public WordDatabase(Quicktionary dictionary) {
		this.dictionary = dictionary;
		this.map = new TreeMap<String, String>();
	}

	public void newWord(String word) {
		map.put(word, null);
	}

	/* temporary */
	public void newPage(String word, String page) {
		map.put(word, page);
	}

	public void removeWord(String word) {
		map.remove(word);
	}

	public String fetchPage(SearchItem item) {
		Map.Entry<String, String> entry;
		entry = (Map.Entry)item.getInternal();
		return entry.getValue();
	}

	/**
	 * Requests items with key that has the word as prefix. This method
	 * itself doesn't give the items. You have to call the fetchResults
	 * method to get the items.
	 *
	 * @param word The prefix of wanted keys
	 */
	public void requestResults(String word) {
		searchWord = word;
		currentEntry = map.ceilingEntry(word);
	}

	/**
	 * Fetches requested items and inserts them to the entries array.
	 *
	 * @param entries The list to be filled
	 * @param count   The number of items wanted
	 */
	public int fetchResults(Map.Entry<String, String>[] entries, int count) {
		int i;

		for(i = 0; i < count && currentEntry != null; i++) {
			if(!currentEntry.getKey().startsWith(searchWord)) break;

			entries[i] = currentEntry;
			currentEntry = map.higherEntry(currentEntry.getKey());
		}

		return i;
	}
}
