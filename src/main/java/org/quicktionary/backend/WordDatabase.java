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
	private TreeMap<String, Integer> map;

	private String searchWord;
	private Map.Entry<String,Integer> currentEntry;

	public WordDatabase(Quicktionary dictionary) {
		this.dictionary = dictionary;
		this.map = new TreeMap<String, Integer>();
	}

	public void newWord(String word) {
		map.put(word, 1);
	}

	public void removeWord(String word) {
		map.remove(word);
	}

	public void fetchPage(SearchItem item) {
		throw new UnsupportedOperationException("Not implemented yet");
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
	public int fetchResults(Map.Entry<String, Integer>[] entries, int count) {
		int i;

		for(i = 0; currentEntry != null; i++) {
			if(!currentEntry.getKey().startsWith(searchWord)) break;

			entries[i] = currentEntry;
			currentEntry = map.higherEntry(currentEntry.getKey());
		}

		return i;
	}
}
