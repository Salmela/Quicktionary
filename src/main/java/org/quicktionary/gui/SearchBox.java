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
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Search box widget that is at the header of the main window.
 */
public class SearchBox extends JTextField implements FocusListener, ActionListener {
	static final long serialVersionUID = 1L;

	static final String SEARCH_EVENT = "search-event";
	static final String SEARCH_ENTER_EVENT = "search-enter-event";

	private boolean hasPlaceHolder;
	private boolean blockDocumentEvent;
	private final Color placeHolderColor;
	private final ActionListener searchListener;

	public SearchBox(ActionListener searchListener) {
		DocumentListener changeListener;

		this.searchListener = searchListener;

		changeListener = new onChangeEvents(searchListener);
		getDocument().addDocumentListener(changeListener);
		addFocusListener(this);
		addActionListener(this);

		blockDocumentEvent = false;
		hasPlaceHolder = true;
		setText("Search");

		placeHolderColor = Color.GRAY;
		setForeground(placeHolderColor);
	}

	public class SearchEvent extends ActionEvent {
		final static long serialVersionUID = 1L;
		private String searchQuery;
		private int searchResultCount;

		public SearchEvent(String command) {
			super(SearchBox.this, ActionEvent.ACTION_PERFORMED, command);
			this.searchQuery = SearchBox.this.getText();
			searchResultCount = 0;
		}

		public void setSearchResultCount(int count) {
			searchResultCount = count;
		}

		public int getSearchResultCount() {
			return searchResultCount;
		}

		public String getSearchQuery() {
			return searchQuery;
		}
	}

	private class onChangeEvents implements DocumentListener {
		private final ActionListener listener;

		public onChangeEvents(ActionListener listener) {
			this.listener  = listener;
		}
		/**
		 * Sends the search signal when user changes the text
		 */
		private void emitEvent() {
			ActionEvent event;

			/* ignore the first event, which is caused by removing of the place holder
			 */
			if(blockDocumentEvent) {
				blockDocumentEvent = false;
				return;
			}

			if(hasPlaceHolder) {
				return;
			}
			event = new SearchEvent(SEARCH_EVENT);
			listener.actionPerformed(event);
		}
		public void changedUpdate(DocumentEvent event) {
			emitEvent();
		}
		public void insertUpdate(DocumentEvent event) {
			emitEvent();
		}
		public void removeUpdate(DocumentEvent event) {
			emitEvent();
		}
	}

	public void focusGained(FocusEvent event) {
		if(hasPlaceHolder) {
			setForeground(Color.BLACK);
			hasPlaceHolder = false;
			blockDocumentEvent = true;
			setText("");
		}
	}
	public void focusLost(FocusEvent event) {
		if(getText().length() == 0) {
			setForeground(placeHolderColor);
			hasPlaceHolder = true;
			setText("Search");
		}
	}
	public void actionPerformed(ActionEvent e) {
		ActionEvent event;
		event = new SearchEvent(SEARCH_ENTER_EVENT);
		searchListener.actionPerformed(event);
	}
}
