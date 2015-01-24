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

/**
 * The database class for the quicktionary.
 */
public class WordDatabase {
	private Quicktionary dictionary;
	private TreeMap<String, Integer> map;

	public WordDatabase(Quicktionary dictionary) {
		this.dictionary = dictionary;
		this.map = new TreeMap<String, Integer>();
	}

	protected void newWord(String word) {
		map.put(word, 1);
		//throw new UnsupportedOperationException("Not implemented yet");
	}

	protected void removeWord(String word) {
		map.remove(word);
		//throw new UnsupportedOperationException("Not implemented yet");
	}

	protected void fetchPage(SearchItem item) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	protected void requestResults(String word) {
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
