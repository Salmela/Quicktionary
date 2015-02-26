/* Quicktionary gui - Word translator app
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
package org.quicktionary.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.quicktionary.backend.Quicktionary;
import org.quicktionary.backend.Configs;
import org.quicktionary.backend.WordEntry;
import org.quicktionary.backend.HistoryEvent;
import org.quicktionary.backend.SearchItem;
import org.quicktionary.backend.SearchResultListener;
import org.quicktionary.gui.dialogs.SettingsDialog;

/**
 * The Application class is the only class that communicates with the backend.
 */
public class Application implements ActionListener {
	static final long serialVersionUID = 1L;
	private static String mainPageTitle = "Main Page";
	private MainWindow mainWindow;
	private Quicktionary dictionary;

	public Application(Quicktionary dictionary) {
		this.mainWindow = new MainWindow(this);
		this.dictionary = dictionary;

		if(Configs.getOptionBoolean("gui.demo")) {
			dictionary.newWord("hello");
			dictionary.newWord("howdy");
			dictionary.newWord("hi");
			dictionary.newWord("hey");
			dictionary.newWord("bye");
			dictionary.newWord("cool");
			dictionary.newWord("test");
		}

		dictionary.setSearchResultListener(mainWindow.getSearchResultListener());

		/* add the main page to the history */
		WordEntry mainPage;
		mainPage = new WordEntry(mainPageTitle, "", mainWindow.getStartPage(), false);
		dictionary.storeEvent(new PageLoadHistoryEvent(mainPage));
		mainWindow.openPage(mainPageTitle, mainPage);
	}

	/**
	 * Start the gui.
	 */
	public void run() {
		mainWindow.setVisible(true);
	}

	/**
	 * Change the first letter of the word into upper case.
	 */
	private String capitalizeWord(String word) {
		return word.substring(0, 1).toUpperCase() + word.substring(1);
	}

	/**
	 * Open new page in main window.
	 */
	private void openPage(WordEntry page, boolean setEvent) {
		String pageTitle;

		if(page.getWord() == mainPageTitle) {
			pageTitle = mainPageTitle;
		} else {
			dictionary.getPageContent(page);
			pageTitle = capitalizeWord(page.getWord());
		}

		mainWindow.openPage(pageTitle, page);
		if(setEvent) {
			dictionary.storeEvent(new PageLoadHistoryEvent(page));
		}
	}

	public void actionPerformed(ActionEvent event) {
		if(event.getActionCommand() == SearchBox.SEARCH_EVENT) {
			handleSearchRequest(event);
		} else if(event.getActionCommand() == SearchBox.SEARCH_ENTER_EVENT) {
			handleSearchEnterRequest(event);
		} else if(event.getActionCommand() == PageLoadEvent.PAGE_LOAD_EVENT) {
			handlePageLoadRequest(event);
		} else if(event.getActionCommand() == SearchResults.REQUEST_SEARCH_RESULTS_EVENT) {
			handleSearchResultRequest(event);
		} else if(event.getActionCommand() == SettingsButton.READ_DATABASE_EVENT) {
			handleReadDatabaseRequest(event);
		} else if(event.getActionCommand() == SettingsButton.ASK_NEW_WORD_EVENT) {
			handleAskNewWordRequest(event);
		} else if(event.getActionCommand() == SettingsButton.ASK_REMOVE_WORD_EVENT) {
			handleAskRemoveWordRequest(event);
		} else if(event.getActionCommand() == SettingsButton.OPEN_PREFERENCES_EVENT) {
			new SettingsDialog(mainWindow, this);
		} else if(event.getActionCommand() == MainWindow.GO_NEXT_EVENT) {
			goInHistory(dictionary.getNextView(true));
		} else if(event.getActionCommand() == MainWindow.GO_BACK_EVENT) {
			goInHistory(dictionary.getPreviousView(true));
		} else {
			System.out.println("main window: unknown event (" +
			                   event.getActionCommand() + ")");
		}

		updateHistory();
	}

	private void updateHistory() {
		mainWindow.updateHistoryButtons("next", dictionary.getNextView(false));
		mainWindow.updateHistoryButtons("back", dictionary.getPreviousView(false));
	}

	private void goInHistory(HistoryEvent event) {
		if(event instanceof PageLoadHistoryEvent) {
			PageLoadHistoryEvent e = (PageLoadHistoryEvent)event;
			openPage(e.getWordEntry(), false);
		}
	}

	private void handleSearchRequest(ActionEvent event) {
		SearchBox.SearchEvent searchEvent;

		searchEvent = (SearchBox.SearchEvent)event;

		dictionary.search(searchEvent.getSearchQuery());
		dictionary.requestSearchResults(0, searchEvent.getSearchResultCount());
	}

	private void handleSearchEnterRequest(ActionEvent event) {
		SearchResultListener listener;
		SearchItem item;

		listener = mainWindow.getSearchResultListener();
		item = listener.getSearchItemAt(0);

		if(item != null) {
			handlePageLoadRequest(new PageLoadEvent(this, item.getWordEntry()));
		}
	}

	private void handlePageLoadRequest(ActionEvent event) {
		PageLoadEvent pageEvent;
		WordEntry entry;
		String name;

		pageEvent = (PageLoadEvent)event;
		name = pageEvent.getName();

		if(name != null) {
			entry = dictionary.getWordEntry(name);
		} else {
			entry = pageEvent.getWordEntry();
		}

		openPage(entry, true);
	}

	private void handleSearchResultRequest(ActionEvent event) {
		SearchResults.RequestSearchResultEvent requestEvent;

		requestEvent = (SearchResults.RequestSearchResultEvent)event;
		dictionary.requestSearchResults(requestEvent.getStart(), requestEvent.getEnd());
	}

	private void handleReadDatabaseRequest(ActionEvent event) {
		SettingsButton.ReadDatabaseEvent dataEvent;

		dataEvent = (SettingsButton.ReadDatabaseEvent) event;
		dictionary.readDatabase(dataEvent.getFilename());
	}

	private void handleAskNewWordRequest(ActionEvent event) {
		String name = (String)JOptionPane.showInputDialog(mainWindow,
		                      "Create a new word.\nWrite the name of the word into textbox?",
		                      "Create a word", JOptionPane.PLAIN_MESSAGE, null, null, null);
		if(name != null) {
			dictionary.newWord(name);
		}
		/*TODO update the search results */
	}

	private void handleAskRemoveWordRequest(ActionEvent event) {
		WordEntry[] results = mainWindow.getSelected();
		for(WordEntry item : results) {
			String word = item.getWord();
			int res = JOptionPane.showConfirmDialog(mainWindow,
				  "Are you sure that you wan't to remove word " + word + "?",
				  "Remove a word", JOptionPane.YES_NO_OPTION);
			if(res == JOptionPane.YES_OPTION) {
				dictionary.removeWord(word);
			}
		}
		/*TODO update the gui */
	}
}
