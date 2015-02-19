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

	/**
	 * Query some search results from WordDatabase.
	 *
	 * @param searchQuery The search query
	 */
	public void search(String searchQuery) {
		history.saveEvent("search", searchQuery);
		searcher.search(searchQuery);
	}

	/**
	 * Request search items to the search listener.
	 *
	 * @param offset Starting offset of wanted search items
	 * @param count  Number of search items wanted
	 */
	public void requestSearchResults(int offset, int count) {
		searcher.requestSearchResults(offset, count);
	}

	/**
	 * Add new word to the WordDatabase.
	 * TODO: improve the api.
	 *
	 * @param word The word to be added
	 */
	public void newWord(String word) {
		database.newWord(word);
	}

	/**
	 * Remove old word from the word database.
	 * TODO: improve the api.
	 *
	 * @param word The word to be removed
	 */
	public void removeWord(String word) {
		database.removeWord(word);
	}

	/**
	 * Allow Gui to store an event to history.
	 * This is sort of hack, so try not use this method.
	 * It is currently only used to store the opening page at gui.
	 *
	 * @param type The type of the event
	 * @param data The internal data of the event
	 */
	public void storeEvent(String type, String data) {
		history.saveEvent(type, data);
	}

	/**
	 * Parse a Wiktionary database and push the words to the WordDatabase.
	 * TODO: Somehow reduce the size of this method
	 *
	 * @param filename The filename for the database
	 */
	public void readDatabase(String filename, boolean wait) {
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
			return;
		}
		thread.start();

		if(!wait) {
			return;
		}

		try {
			thread.join();
		} catch (InterruptedException ex) {
		}
	}

	public void readDatabase(String filename) {
		readDatabase(filename, false);
	}

	/**
	 * Set the search result listener for Searcher class.
	 *
	 * @param listener The SearchResultListener object
	 */
	public void setSearchResultListener(SearchResultListener listener) {
		searcher.setResultListener(listener);
	}

	/**
	 * Get the content of the page for word.
	 * TODO: remove the searchQuery.
	 *
	 * @param item The SearchItem for the wanted page
	 * @param searchQuery Search query used to find the page
	 * @return The data for the page
	 */
	public WordEntry getPageContent(SearchItem item, String searchQuery) {
		WordEntry entry;
		history.saveEvent("search", searchQuery);
		history.saveEvent("page", "" + item.getWord());

		entry = item.getWordEntry();
		if(entry.isLoaded()) {
			database.fetchPage(entry);
		}
		return entry;
	}

	/**
	 * Get the page for next view in the browsing history.
	 * @param go Load the next page
	 * @return The view
	 */
	public Object getNextView(boolean go) {
		return history.getNext(go);
	}

	/**
	 * Get the page for previous view in the browsing history.
	 * @param go Load the previous page
	 * @return The view
	 */
	public Object getPreviousView(boolean go) {
		return history.getPrevious(go);
	}
}
