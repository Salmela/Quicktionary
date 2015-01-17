/* Quicktionary gui - Word translator app
 * Copyright (C) 2015  Aleksi Salmela <aleksi.salmela at helsinki.fi>

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quicktionary.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.quicktionary.backend.Quicktionary;

/*
 * The MainWindow is only class that communicates with the backend.
 */
public class MainWindow extends JFrame implements ActionListener {
	private Quicktionary dictionary;
	private boolean showSearchResults;

	private JScrollPane mainPane;
	private JList searchResults;
	private JTextArea pageArea;

	public MainWindow(Quicktionary dictionary) {
		this.dictionary = dictionary;
		this.showSearchResults = false;

		setTitle("Quicktionary");
		setSize(600, 400);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		makeComponents();
	}

	private void makeComponents() {
		JPanel     headerBar;
		JTextField searchBox;
		JButton    backButton, nextButton;
		Dimension  headerSize;

		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		/* create the header bar components */
		backButton = new JButton("Back");
		nextButton = new JButton("Next");
		searchBox = new SearchBox(this);

		/* pack the components into header bar */
		headerBar = new JPanel();
		headerBar.setLayout(new BoxLayout(headerBar, BoxLayout.X_AXIS));
		headerBar.add(backButton);
		headerBar.add(nextButton);
		headerBar.add(searchBox);
		headerBar.add(new JButton("Settings"));

		/* set the headerBar height to same as backButton height */
		headerSize = headerBar.getPreferredSize();
		headerSize.width = Short.MAX_VALUE;
		System.out.println("size " + headerSize.width + ", "+ headerSize.height);
		headerBar.setMaximumSize(headerSize);

		/* create components for the main view */
		mainPane = new JScrollPane();
		searchResults = new SearchResults();
		pageArea = new JTextArea();
		mainPane.setViewportView(searchResults);

		this.add(headerBar);
		this.add(mainPane);
	}

	public void actionPerformed(ActionEvent event) {
		if(event.getActionCommand() == SearchBox.SEARCH_EVENT) {
			SearchBox searchBox = (SearchBox)event.getSource();
			System.out.println("main window: search event " + searchBox.getText());

			//dictionary.search(searchBox.getText());

		} else if(event.getActionCommand() == SearchBox.SEARCH_ENTER_EVENT) {
			SearchBox searchBox = (SearchBox)event.getSource();
			System.out.println("main window: search event " + searchBox.getText());

			//dictionary.search(searchBox.getText());
			//show the first item's page
		} else {
			System.out.println("main window: unknown event");
		}
	}
}
