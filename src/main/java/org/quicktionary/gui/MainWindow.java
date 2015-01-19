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
import java.awt.event.*;
import org.quicktionary.backend.Quicktionary;

/**
 * The MainWindow is only class that communicates with the backend.
 */
public class MainWindow extends JFrame implements ActionListener {
	static final long serialVersionUID = 1L;

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
		roundedBorder = new RoundedBorder(new Color(0x555753));
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

		return headerBar;
	}

	private void makeComponents() {
		JPanel     headerBar;
		JButton    backButton, nextButton, settingsButton;
		JTextField searchBox;

		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		/* create the header bar components */
		backButton = new JButton("Back");
		nextButton = new JButton("Next");
		searchBox  = new SearchBox(this);
		settingsButton = new SettingsButton(this);

		makeRoundedBorders(backButton, true, false, true, true);
		makeRoundedBorders(nextButton, true, true, true, false);
		makeRoundedBorders(settingsButton, true, true, true, true);
		/*TODO: ugly hack remove */
		/* add a right border to the backButton */
		((RoundedBorder)((CompoundBorder) backButton.getBorder()).getOutsideBorder()).setThickness(true, true, true, true);

		headerBar = makeHeaderBar(backButton, nextButton, settingsButton, searchBox);

		/* create components for the main view */
		mainPane = new JScrollPane();
		searchResults = new SearchResults();
		//dictionary.setSearchResultListener(searchResults);
		pageArea = new JTextArea();
		mainPane.setViewportView(searchResults);
		//mainPane.setViewportView(pageArea);

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
