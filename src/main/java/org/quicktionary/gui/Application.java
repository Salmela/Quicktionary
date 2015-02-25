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
import org.quicktionary.backend.WordEntry;
import org.quicktionary.gui.dialogs.SettingsDialog;

/**
 * The Application class is the only class that communicates with the backend.
 */
public class Application implements ActionListener {
	static final long serialVersionUID = 1L;
	private MainWindow mainWindow;
	private Quicktionary dictionary;

	public Application(Quicktionary dictionary) {
		this.mainWindow = new MainWindow(this);
		this.dictionary = dictionary;

		dictionary.newWord("hello");
		dictionary.newWord("hey");
		dictionary.newWord("he");
		dictionary.newWord("hi");
		dictionary.newWord("howdy");
		dictionary.newWord("crody");
		dictionary.newWord("foggy");
		dictionary.newWord("hay");
		dictionary.newWord("bay");
		dictionary.newWord("say");
		dictionary.newWord("lay");

		dictionary.setSearchResultListener(mainWindow.getSearchResultListener());

		/* add the main page to the history */
		dictionary.storeEvent("page", "");
		mainWindow.setVisible(true);
	}

	/**
	 * Change the first letter of the word into upper case.
	 */
	private String capitalizeWord(String word) {
		return word.substring(0, 1).toUpperCase() + word.substring(1);
	}

	public void actionPerformed(ActionEvent event) {
		if(event.getActionCommand() == SearchBox.SEARCH_EVENT) {
			handleSearchRequest(event);
		} else if(event.getActionCommand() == SearchBox.SEARCH_ENTER_EVENT) {
			handleLoadFirstResultRequest(event);
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
				mainWindow.updateHistoryButtons("next", dictionary.getNextView(true));
		} else if(event.getActionCommand() == MainWindow.GO_BACK_EVENT) {
				mainWindow.updateHistoryButtons("back", dictionary.getPreviousView(true));
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

	private void handleSearchRequest(ActionEvent event) {
		SearchBox searchBox = (SearchBox)event.getSource();
		SearchBox.SearchEvent searchEvent;

		searchEvent = (SearchBox.SearchEvent)event;

		dictionary.search(searchEvent.getSearchQuery());
		dictionary.requestSearchResults(0, searchEvent.getSearchResultCount());
	}

	private void handleLoadFirstResultRequest(ActionEvent event) {
		handleLoadFirstResultRequest(event);
		SearchBox searchBox = (SearchBox)event.getSource();
		System.out.println("main window: search event " + searchBox.getText());

		//dictionary.search(searchBox.getText());
		/*TODO: show the first item's page */
	}

	private void handlePageLoadRequest(ActionEvent event) {
		PageLoadEvent pageEvent;
		WordEntry page;
		String pageTitle;

		pageEvent = (PageLoadEvent)event;
		page = dictionary.getPageContent(pageEvent.getWordEntry());
		pageTitle = capitalizeWord(page.getWord());

		mainWindow.openPage(pageTitle, page);
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
