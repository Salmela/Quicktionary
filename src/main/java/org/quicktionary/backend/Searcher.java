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
 * The Searcher class parses the search queries from WordDatabase and
 * sends results to the gui.
 */
class Searcher {
	private WordDatabase database;
	private int processedEntries;
	private boolean searchRunning;

	private SearchResultListener resultListener;

	public Searcher(WordDatabase database) {
		this.database = database;
		this.processedEntries = 0;
		this.searchRunning = false;
	}

	/**
	 * Search words from WordDatabase that are similar
	 * to search query. This method is called for all
	 * search events coming from gui.
	 * @param query The search query
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
	 * @param listener The search result listener
	 */
	public void setResultListener(SearchResultListener listener) {
		resultListener = listener;
	}

	private boolean isSearchResultDuplicate(WordEntry entry) {
		int size = resultListener.getSize();
		int i;

		for(i = 0;  i < size; i++) {
			SearchItem item;
			item = resultListener.getSearchItemAt(i);
			if(item != null && item.getWordEntry() == entry) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Fetch the words from WordDatabase that fit the search term
	 * and send them to SearchResultListener. This method also
	 * will possibly reorder the search results.
	 * @param offset First search result that is wanted
	 * @param count  The number of search results
	 * @return True if the request was successful
	 */
	public boolean requestSearchResults(int offset, int count) {
		WordEntry[] entries;
		int resultCount, requestCount, i;

		if(resultListener == null) {
			searchRunning = false;
			return false;
		}

		resultCount = 0;
		requestCount = offset + count - processedEntries;

		if(requestCount <= 0) {
			searchRunning = false;
			System.out.println("ERROR: Invalid search result request");
			return false;
		}

		entries = new WordEntry[requestCount];

		while(resultCount < requestCount) {
			/* check if we didn't get any words then exit */
			if(database.fetchResults(entries, requestCount) == 0) {
				/* inform the gui that there isn't more search results */
				resultListener.appendSearchResult(null);
				break;
			}

			/* go through all words that datbase gave to us */
			for(i = 0; i < requestCount && entries[i] != null; i++) {
				/* remove duplicates */
				if(isSearchResultDuplicate(entries[i])) continue;

				/* give the new item to gui */
				resultListener.appendSearchResult(new SearchItem(entries[i].getWord(), "Test", entries[i]));
				resultCount++;
			}
		}

		processedEntries += resultCount;
		searchRunning = false;
		return true;
	}

	/**
	 * Split the search query to parts. Currently the
	 * method does nothing.
	 */
	private String parseSearchQuery(String query) {
		return query;
	}
}
