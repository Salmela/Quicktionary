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

import org.quicktionary.backend.SearchResultListener;
import org.quicktionary.gui.theme.HeaderButton;
import org.quicktionary.gui.theme.StyleManager;

/**
 * The MainWindow initializes the window components
 * and updates them as neaded.
 */
public class MainWindow extends JFrame implements ActionListener {
	static final long serialVersionUID = 1L;
	static final String GO_NEXT_EVENT = "go-next-event";
	static final String GO_BACK_EVENT = "go-back-event";

	private final String appTitle;
	private String pageTitle;

	private final Application app;
	private StyleManager styleManager;

	/* main components */
	private JScrollPane mainPane;
	private SearchResults searchResults;
	private PageArea pageArea;
	private JTextField searchBox;

	/* history buttons */
	private JButton backButton, nextButton;

	public MainWindow(Application app) {
		this.app = app;

		appTitle = "Quicktionary";
		pageTitle = null;

		setTitle(appTitle);
		setSize(600, 400);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		styleManager = new StyleManager();
		styleManager.changeStyle(Main.themeName);

		makeComponents();
		updateHistoryButtons("next", null);
		updateHistoryButtons("back", null);
	}

	/*TODO: remove/move somewhere else */
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

	/**
	 * Create the child components.
	 */
	private void makeComponents() {
		JPanel     headerBar;
		JButton    settingsButton;

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

		pageArea = new PageArea();
		mainPane.setViewportView(pageArea);

		/* add event listeners for the history buttons */
		nextButton.addActionListener(app);
		nextButton.setActionCommand(GO_NEXT_EVENT);
		backButton.addActionListener(app);
		backButton.setActionCommand(GO_BACK_EVENT);

		this.add(headerBar);
		this.add(mainPane);

		searchResults.inLayout();
	}

	/**
	 * Get the search result listener from searchResults.
	 */
	public SearchResultListener getSearchResultListener() {
		return searchResults.getSearchResultListener();
	}

	/**
	 * Change the content of the main area.
	 */
	private void changeView(boolean changeToSearchResults) {
		/* swap to search results */
		if(changeToSearchResults) {
			mainPane.setViewportView(searchResults);
			setTitle(appTitle + " \u2014 Search results");
		/* swap to page area */
		} else {
			mainPane.setViewportView(pageArea);
			setTitle(appTitle + " \u2014 " + pageTitle);
		}
	}

	/**
	 * Set the fetched page, given by Application class.
	 */
	public void openPage(String title, String text) {
		pageArea.setText(text);
		pageTitle = title;
		changeView(false);
	}

	/**
	 * Disable or enable one of the history buttons.
	 */
	public void updateHistoryButtons(String buttonString, Object view) {
		JButton button;

		if("next".equals(buttonString)) {
			button = nextButton;
		} else if("back".equals(buttonString)) {
			button = backButton;
		} else {
			return;
		}

		button.setEnabled(view != null);
	}

	/**
	 * Listen events from the searchBox. The method sets the
	 * wanted search result count and passes the event to the app.
	 */
	public void actionPerformed(ActionEvent event) {
		/* set the search result count for SearchEvents */
		if(event.getActionCommand() == SearchBox.SEARCH_EVENT ||
		   event.getActionCommand() == SearchBox.SEARCH_ENTER_EVENT) {
			SearchBox.SearchEvent e = (SearchBox.SearchEvent)event;
			e.setSearchResultCount(searchResults.getVisibleRowCount());
			changeView(true);
			app.actionPerformed(event);

		/* set the search query for PageLoadEvents */
		} else if(event.getActionCommand() == SearchResults.PAGE_LOAD_EVENT) {
			SearchResults.PageLoadEvent e = (SearchResults.PageLoadEvent)event;
			e.setSearchQuery(searchBox.getText());
			app.actionPerformed(event);

		/* do nothing special for search result requests */
		} else if(event.getActionCommand() == SearchResults.REQUEST_SEARCH_RESULTS_EVENT) {
			app.actionPerformed(event);
		}
	}
}
