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

/**
 * The Searcher class parses the search queries from gui and
 * sends results back to gui.
 */
public class Searcher {
	private Quicktionary dictionary;
	private WordDatabase database;
	private int processedEntries;
	private boolean searchRunning;

	private SearchResultListener resultListener;

	public Searcher(Quicktionary dictionary, WordDatabase database) {
		this.dictionary = dictionary;
		this.database = database;
		this.processedEntries = 0;
		this.searchRunning = false;
	}

	/**
	 * Search words from WordDatabase that are similar
	 * to search query. This method is called for all
	 * search events coming from gui.
	 */
	public void search(String query) {
		parseSearchQuery(query);

		processedEntries = 0;
		searchRunning = true;
		resultListener.resetSearchResults();

		/*TODO: move to another thread */
		database.requestResults(query);
	}

	/**
	 * The method returns true if all search results are
	 * given to the SearchResultListener.
	 *
	 * This is a helper method for tests.
	 * @return true if the search is complete
	 */
	public boolean hasCompleted() {
		return this.searchRunning == false;
	}

	/**
	 * Set the SearchResultListener. All future search results
	 * are given to the listener.
	 */
	public void setResultListener(SearchResultListener listener) {
		resultListener = listener;
	}

	private boolean isSearchResultDuplicate(WordDatabase.WordEntry entry) {
		int size = resultListener.getSize();
		int i;

		for(i = 0;  i < size; i++) {
			SearchItem item;
			item = resultListener.getSearchItemAt(i);
			if(item != null && item.getInternal() == entry) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Fetch the words from WordDatabase that fit the search term
	 * and send them to SearchResultListener. This method also
	 * will possibly reorder the search results.
	 */
	public boolean requestSearchResults(int offset, int count) {
		WordDatabase.WordEntry[] entries;
		int i;

		if(resultListener == null) {
			searchRunning = false;
			return false;
		}

		count = offset + count - processedEntries;
		if(count <= 0) {
			searchRunning = false;
			return false;
		}

		entries = new WordDatabase.WordEntry[count];
		database.fetchResults(entries, count);

		for(i = 0; i < count && entries[i] != null; i++) {
			/* remove duplicates */
			if(isSearchResultDuplicate(entries[i])) continue;
			resultListener.appendSearchResult(new SearchItem(entries[i].getWord(), "Test", entries[i]));
		}

		/* inform the gui that there isn't more search results */
		if(i != count) {
			System.out.println("end");
			resultListener.appendSearchResult(null);
		}
		processedEntries += i;

		searchRunning = false;
		return true;
	}

	/**
	 * Split the search query to parts. Currently the
	 * method is just nop.
	 */
	private String parseSearchQuery(String query) {
		return query;
	}
}
