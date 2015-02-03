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

import java.lang.IllegalStateException;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.quicktionary.backend.Quicktionary;

/**
 * The MainWindow is only class that communicates with the backend.
 */
public class MainWindow extends JFrame implements ActionListener {
	static final long serialVersionUID = 1L;

	private final String appTitle;
	private String pageTitle;

	private Quicktionary dictionary;
	private final Application app;

	private boolean showSearchResults;
	private StyleManager styleManager;

	private JScrollPane mainPane;
	private SearchResults searchResults;
	private JTextArea pageArea;
	private JTextField searchBox;

	public MainWindow(Quicktionary dictionary, Application app) {
		this.app = app;
		this.dictionary = dictionary;
		this.showSearchResults = false;

		appTitle = "Quicktionary";
		pageTitle = null;

		setTitle(appTitle);
		setSize(600, 400);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		dictionary.newWord("hello");
		dictionary.newWord("hey");
		dictionary.newWord("he");
		dictionary.newWord("hi");
		dictionary.newWord("howdy");

		styleManager = new StyleManager();
		styleManager.changeStyle(Main.themeName);
		makeComponents();
	}

	private void makeRoundedBorders(JComponent component, boolean top, boolean right,
	                                boolean bottom, boolean left) {
		CompoundBorder compoundBorder;
		RoundedBorder  roundedBorder;
		int topLeft, topRight, bottomRight, bottomLeft;

		topLeft = topRight = bottomRight = bottomLeft = 4;

		if(!top) {
			topLeft = topRight = 0;
		}
		if(!right) {
			topRight = bottomRight = 0;
		}
		if(!bottom) {
			bottomLeft = bottomRight = 0;
		}
		if(!left) {
			topLeft = bottomLeft = 0;
		}

		if(!(component.getBorder() instanceof CompoundBorder)) {
			throw new IllegalStateException("The component must use compound border.");
		}

		compoundBorder = (CompoundBorder) component.getBorder();
		roundedBorder = new RoundedBorder(StyleManager.getColor("header-button-border"));
		roundedBorder.setThickness(top, right, bottom, left);
		roundedBorder.setRadii(topLeft, topRight, bottomRight, bottomLeft);

		compoundBorder = new CompoundBorder(roundedBorder, compoundBorder.getInsideBorder());
		component.setBorder(compoundBorder);
	}

	private JPanel makeHeaderBar(JButton backButton, JButton nextButton,
	                             JButton settingsButton, JTextField searchBox) {
		JPanel     headerBar;
		Box.Filler filler, filler2;
		Border     paddingBorder;
		Dimension  fillerDim, headerSize;

		/* create filler components */
		fillerDim = new Dimension(2, 2);
		filler  = new Box.Filler(fillerDim, fillerDim, fillerDim);
		filler2 = new Box.Filler(fillerDim, fillerDim, fillerDim);

		/* pack the components into header bar */
		headerBar = new JPanel();
		headerBar.setLayout(new BoxLayout(headerBar, BoxLayout.X_AXIS));
		headerBar.add(backButton);
		headerBar.add(nextButton);
		headerBar.add(filler);
		headerBar.add(searchBox);
		headerBar.add(filler2);
		headerBar.add(settingsButton);

		/* add some padding to the header */
		paddingBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
		headerBar.setBorder(paddingBorder);

		/* set the headerBar height to same as backButton height */
		headerSize = headerBar.getPreferredSize();
		headerSize.width = Short.MAX_VALUE;
		headerBar.setMaximumSize(headerSize);

		headerBar.setBackground(StyleManager.getColor("header-bg"));

		return headerBar;
	}

	private void makeComponents() {
		JPanel     headerBar;
		JButton    backButton, nextButton, settingsButton;

		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		/* create the header bar components */
		backButton = new HeaderButton("Back");
		nextButton = new HeaderButton("Next");
		searchBox  = new SearchBox(this);
		settingsButton = new SettingsButton(app);

		/*TODO: gui is currently not high priority, but this
		        should be cleaned up */
		makeRoundedBorders(backButton, true, false, true, true);
		makeRoundedBorders(nextButton, true, true, true, false);
		makeRoundedBorders(settingsButton, true, true, true, true);
		/* add a right border to the backButton */
		((RoundedBorder)((CompoundBorder) backButton.getBorder()).getOutsideBorder()).setThickness(true, true, true, true);

		headerBar = makeHeaderBar(backButton, nextButton, settingsButton, searchBox);

		/* create components for the main view */
		mainPane = new JScrollPane();
		searchResults = new SearchResults(this);
		pageArea = new JTextArea();
		pageArea.setText("Read database and write something to the search box.");
		mainPane.setViewportView(pageArea);

		this.add(headerBar);
		this.add(mainPane);

		searchResults.inLayout();
		dictionary.setSearchResultListener(searchResults.getSearchResultListener());
	}

	/**
	 * Change the first letter of the word into upper case.
	 */
	private String capitalizeWord(String word) {
		return word.substring(0, 1).toUpperCase() + word.substring(1);
	}

	private void changeView(boolean changeToSearchResults) {
		showSearchResults = changeToSearchResults;

		if(changeToSearchResults) {
			mainPane.setViewportView(searchResults);
			setTitle(appTitle + " \u2014 Search results");
		} else {
			mainPane.setViewportView(pageArea);
			setTitle(appTitle + " \u2014 " + pageTitle);
		}
	}

	public void actionPerformed(ActionEvent event) {
		if(event.getActionCommand() == SearchBox.SEARCH_EVENT) {
			SearchBox searchBox = (SearchBox)event.getSource();
			System.out.println("main window: search event " + searchBox.getText());

			changeView(true);
			dictionary.search(searchBox.getText());
			dictionary.requestSearchResults(0, searchResults.getVisibleRowCount());

		} else if(event.getActionCommand() == SearchBox.SEARCH_ENTER_EVENT) {
			System.out.println("main window: search event " + searchBox.getText());

			//dictionary.search(searchBox.getText());
			/*TODO: show the first item's page */

		} else if(event.getActionCommand() == SearchResults.PAGE_LOAD_EVENT) {
			SearchResults.PageLoadEvent pageEvent;
			String text;

			pageEvent = (SearchResults.PageLoadEvent)event;
			text = dictionary.getPageContent(pageEvent.getSearchItem(), searchBox.getText());
			pageArea.setText(text);
			pageTitle = capitalizeWord(pageEvent.getSearchItem().getWord());
			changeView(false);

		} else if(event.getActionCommand() == SearchResults.REQUEST_SEARCH_RESULTS_EVENT) {
			SearchResults.RequestSearchResultEvent requestEvent;

			requestEvent = (SearchResults.RequestSearchResultEvent)event;
			dictionary.requestSearchResults(requestEvent.getStart(), requestEvent.getEnd());

		} else if(event.getActionCommand() == SettingsButton.READ_DATABASE_EVENT) {
			SettingsButton.ReadDatabaseEvent dataEvent;

			dataEvent = (SettingsButton.ReadDatabaseEvent) event;
			dictionary.readDatabase(dataEvent.getFilename());

		} else if(event.getActionCommand() == SettingsButton.ASK_NEW_WORD_EVENT) {
			String name = (String)JOptionPane.showInputDialog(this,
			                      "Create a new word.\nWrite the name of the word into textbox?",
			                      "Create a word", JOptionPane.PLAIN_MESSAGE, null, null, null);
			if(name != null) {
				dictionary.newWord(name);
			}

		} else if(event.getActionCommand() == SettingsButton.ASK_REMOVE_WORD_EVENT) {
			String word = null;
			int res = JOptionPane.showConfirmDialog(this,
			          "Are you sure that you wan't to remove word " + word + "?",
			          "Remove a word", JOptionPane.YES_NO_OPTION);
			if(res == JOptionPane.YES_OPTION) {
				dictionary.removeWord(word);
			}

		} else if(event.getActionCommand() == SettingsButton.OPEN_PREFERENCES_EVENT) {
			new SettingsDialog(this, this);
		} else {
			System.out.println("main window: unknown event (" +
			                   event.getActionCommand() + ")");
		}
	}
}
