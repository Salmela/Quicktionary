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
 * The backend class for the Quicktionary.
 */
public class Quicktionary {
	private WordDatabase database;
	private Searcher     searcher;
	private History      history;

	public Quicktionary() {
		database = new WordDatabase(this);
		searcher = new Searcher(this, database);
		history = new History();

		//searchThread = new Thread(searcher);
		//searchThread.start();
	}

	public void search(String searchQuery) {
		history.saveEvent("search", searchQuery);
		searcher.search(searchQuery);
	}

	public void requestSearchResults(int offset, int count) {
		searcher.requestSearchResults(offset, count);
	}

	public void newWord(String word) {
		database.newWord(word);
	}

	public void removeWord(String word) {
		database.removeWord(word);
	}

	public void readDatabase(String filename) {
		/* should we create file description here,
		 * so that testing would be easier?
		 */
		/* TODO: Implement some way to gui check if
		 * the file is valid database file, before
		 * parsing the whole file.
		 */
		WikiDBReader parser;
		Thread thread;
		parser = new WikiDBReader(database);
		thread = new Thread(parser);

		if(! parser.check(filename)) {
			/* send this info to the gui */
			System.out.println("The file couldn't be opened.");
		}
		thread.start();
	}

	public void setSearchResultListener(SearchResultListener listener) {
		searcher.setResultListener(listener);
	}

	public String getPageContent(SearchItem item, String searchQuery) {
		history.saveEvent("search", searchQuery);
		history.saveEvent("page", "" + item.getWord());
		return database.fetchPage(item);
	}

	public Object getNextView() {
		return history.getNext(false);
	}

	public Object getPreviousView() {
		return history.getPrevious(false);
	}
}
