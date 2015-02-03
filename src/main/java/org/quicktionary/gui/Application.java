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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.quicktionary.backend.Quicktionary;

/**
 * The MainWindow is only class that communicates with the backend.
 */
public class Application implements ActionListener {
	static final long serialVersionUID = 1L;
	private MainWindow mainWindow;
	private Quicktionary dictionary;

	public Application(Quicktionary dictionary) {
		this.mainWindow = new MainWindow(dictionary, this);
		this.dictionary = dictionary;

		dictionary.newWord("hello");
		dictionary.newWord("hey");
		dictionary.newWord("he");
		dictionary.newWord("hi");
		dictionary.newWord("howdy");

		mainWindow.setVisible(true);
	}

	public void actionPerformed(ActionEvent event) {
		if(event.getActionCommand() == SearchBox.SEARCH_EVENT) {
		} else if(event.getActionCommand() == SearchBox.SEARCH_ENTER_EVENT) {
		} else if(event.getActionCommand() == SearchResults.PAGE_LOAD_EVENT) {
		} else if(event.getActionCommand() == SearchResults.REQUEST_SEARCH_RESULTS_EVENT) {
		} else if(event.getActionCommand() == SettingsButton.READ_DATABASE_EVENT) {
			handleReadDatabaseRequest(event);
		} else if(event.getActionCommand() == SettingsButton.ASK_NEW_WORD_EVENT) {
			handleAskNewWordRequest(event);
		} else if(event.getActionCommand() == SettingsButton.ASK_REMOVE_WORD_EVENT) {
			handleAskRemoveWordRequest(event);
		} else if(event.getActionCommand() == SettingsButton.OPEN_PREFERENCES_EVENT) {
			new SettingsDialog(mainWindow, this);
		} else {
			System.out.println("main window: unknown event (" +
			                   event.getActionCommand() + ")");
		}
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
	}

	private void handleAskRemoveWordRequest(ActionEvent event) {
		String word = null;
		int res = JOptionPane.showConfirmDialog(mainWindow,
		          "Are you sure that you wan't to remove word " + word + "?",
		          "Remove a word", JOptionPane.YES_NO_OPTION);
		if(res == JOptionPane.YES_OPTION) {
			dictionary.removeWord(word);
		}
	}
}
