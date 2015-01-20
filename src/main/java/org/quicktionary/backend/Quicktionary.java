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

/**
 * The backend class for the quicktionary.
 */
public class Quicktionary {
	private WordDatabase database;
	private Searcher     searcher;
	//private History    history;

	public Quicktionary() {
		database = new WordDatabase(this);
		searcher = new Searcher(this);
		//history = new History(this);
	}

	public void search(String query) {
		searcher.search(query);
	}

	public void readDatabase(String filename) {
	}

	public void setSearchResultListener(SearchResultListener listener) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	public void getPageContent(SearchItem item, String searchQuery) {
		//history.saveEvent("search", searchQuery);
		//history.saveEvent("page", "" + item.getID());
		database.fetchPage(item);
	}

	/**
	 * Get the database object.
	 * Used by the Searcher:search
	 */
	protected WordDatabase getDatabase() {
		return database;
	}
}
