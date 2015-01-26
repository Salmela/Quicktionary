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

import java.util.Map;

/**
 * The search queries send by the interface are parsed and processed in this class.
 */
public class Searcher {
	private Quicktionary dictionary;
	private WordDatabase database;
	private int processedEntries;

	private SearchResultListener resultListener;

	public Searcher(Quicktionary dictionary, WordDatabase database) {
		this.dictionary = dictionary;
		this.database = database;
		this.processedEntries = 0;
	}

	public void search(String query) {
		parseSearchQuery(query);

		processedEntries = 0;
		database.requestResults(query);
		resultListener.resetSearchResults();
	}

	public void setResultListener(SearchResultListener listener) {
		resultListener = listener;
	}

	public void requestSearchResults(int offset, int count) {
		Map.Entry<String, String>[] entries;
		int i;

		if(resultListener == null) return;

		count = offset + count - processedEntries;
		entries = new Map.Entry[count];
		count = database.fetchResults(entries, count);

		for(i = 0; i < count; i++) {
			resultListener.appendSearchResult(new SearchItem(entries[i].getKey(), "Test", entries[i]));
		}
		processedEntries += count;
	}

	private String parseSearchQuery(String query) {
		return query;
	}
}
