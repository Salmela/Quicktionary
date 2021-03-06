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

import org.quicktionary.backend.database.WordDatabase;

import java.io.File;
import java.util.Map;

/**
 * The backend class for the Quicktionary.
 */
public class Quicktionary {
	private WordDatabase database;
	private Searcher     searcher;
	private History      history;
	private Configs      configs;

	public Quicktionary(Map<String, Object> uiDefaults, Map<String, Object> forcedOptions) {
		handleConfig(uiDefaults, forcedOptions);
		database = new WordDatabase();
		searcher = new Searcher(database);
		history = new History();
	}
	public Quicktionary() {
		this(null, null);
	}

	/**
	 * Set the default configs, read the config and
	 * overwrite the commandline.
	 */
	private void handleConfig(Map<String, Object> uiDefaults, Map<String, Object> forcedOptions) {
		String separator;

		separator = File.separator;
		configs = new Configs();

		/* set default */
		configs.setOption("appFolder", System.getProperty("user.home") + separator + ".quicktionary");
		configs.setOption("database", Configs.getOption("appFolder") + separator + "datastore.db");

		/* create the app directory if it doesn't already exist */
		new File((String)Configs.getOption("appFolder")).mkdirs();

		/* set gui defaults */
		if(uiDefaults != null) {
			for(Map.Entry<String, Object> entry : uiDefaults.entrySet()) {
				configs.setOption(entry.getKey(), entry.getValue());
			}
		}

		/* parse the config file */
		configs.parseConfigFile(new File(Configs.getOption("appFolder") + separator + "config"));

		/* set the commandline options */
		if(forcedOptions != null) {
			for(Map.Entry<String, Object> entry : forcedOptions.entrySet()) {
				configs.setOption(entry.getKey(), entry.getValue());
			}
		}
	}

	/**
	 * Query some search results from WordDatabase.
	 *
	 * @param searchQuery The search query
	 */
	public void search(String searchQuery) {
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
	 *
	 * @param word The word to be added
	 */
	public WordEntry newWord(String word) {
		return database.newWord(word);
	}

	/**
	 * Remove old word from the word database.
	 * TODO: change param to WordEntry
	 *
	 * @param word The word to be removed
	 */
	public void removeWord(String word) {
		database.removeWord(word);
	}

	/**
	 * Allow Gui to store an event to history.
	 *
	 * @param event The data of the event
	 */
	public void storeEvent(HistoryEvent event) {
		history.saveEvent(event);
	}

	/**
	 * Parse a Wiktionary database and push the words to the WordDatabase.
	 *
	 * @param filename The filename for the database
	 */
	public void readDatabase(String filename, boolean wait) {
		/* TODO: Implement some way to gui check if
		 * the file is valid database file, before
		 * parsing the whole file.
		 */
		WikiDBReader reader;
		reader = new WikiDBReader(database);

		if(! reader.check(filename)) {
			/* send this info to the gui */
			System.out.println("The file couldn't be opened.");
			return;
		}

		reader.readDatabase(filename, wait);
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
	 *
	 * @param item The SearchItem for the wanted page
	 * @return The data for the page
	 */
	public WordEntry getPageContent(WordEntry entry) {
		database.fetchPage(entry);
		return entry;
	}

	/**
	 * Get the word entry for the name.
	 * This is only used for page links.
	 *
	 * @param word The name of the page
	 * @return The WordEntry for the page
	 */
	public WordEntry getWordEntry(String word) {
		WordEntry entry;

		entry = database.fetchWordEntry(word);
		return entry;
	}

	/**
	 * Get the page for next view in the browsing history.
	 * @param go Load the next page
	 * @return The view
	 */
	public HistoryEvent getNextView(boolean go) {
		return history.getNext(go);
	}

	/**
	 * Get the page for previous view in the browsing history.
	 * @param go Load the previous page
	 * @return The view
	 */
	public HistoryEvent getPreviousView(boolean go) {
		return history.getPrevious(go);
	}

	public void close() {
		database.close();
	}
}
