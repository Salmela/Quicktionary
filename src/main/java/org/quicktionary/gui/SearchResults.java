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

import java.util.ArrayList;

import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.AbstractListModel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import org.quicktionary.backend.SearchResultListener;
import org.quicktionary.backend.SearchItem;
import org.quicktionary.backend.WordEntry;

import org.quicktionary.backend.Configs;

/**
 * This component shows the search results after user typed into search box.
 */
public class SearchResults extends JList {
	static final long serialVersionUID = 1L;
	static final String REQUEST_SEARCH_RESULTS_EVENT = "request-search-results-event";

	private SearchResultRenderer renderer;
	private SearchResultModel    model;
	private ActionListener       listener;

	public SearchResults(ActionListener listener) {
		this.listener = listener;
		this.renderer = new SearchResultRenderer();
		this.model = new SearchResultModel();

		addMouseListener(new mouseHandler());
		setCellRenderer(renderer);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	public void inLayout() {
		computeFixedCellSize();
		setModel(model);
	}

	/**
	 * The method sets the item size for the list items. It inserts
	 * dummy item into list and measures it's size. The measured
	 * size is then set as item size for all list items.
	 */
	private void computeFixedCellSize() {
		JLabel label;
		Dimension size;
		Object dummyItem[];

		label = renderer;

		dummyItem = new Object[1];
		dummyItem[0] = new SearchItem("A", "A", null);
		setListData(dummyItem);

		/* fill the label */
		renderer.getListCellRendererComponent(this, dummyItem[0],
		                             0, false, false);

		/* set the same dummy item size for all list items */
		size = label.getPreferredSize();
		setFixedCellWidth(size.width);
		setFixedCellHeight(size.height);
	}

	public SearchResultListener getSearchResultListener() {
		return model;
	}

	public WordEntry[] getSelectedEntries() {
		int[] selectedIndices;
		WordEntry[] results;

		selectedIndices = getSelectedIndices();
		results = new WordEntry[selectedIndices.length];

		for(int i = 0; i < selectedIndices.length; i++) {
			int index = selectedIndices[i];
			results[i] = model.getSearchItemAt(index).getWordEntry();
		}
		return results;
	}

	/**
	 * Open the page of the search item when user clicks it.
	 */
	private class mouseHandler extends MouseAdapter {
		public void mouseClicked(MouseEvent event) {
			if (event.getClickCount() != 2) {
				return;
			}
			int i = getSelectedIndex();
			SearchItem item = model.getSearchItemAt(i);

			if(item != null) {
				PageLoadEvent emittedEvent;

				emittedEvent = new PageLoadEvent(this, item.getWordEntry());
				listener.actionPerformed(emittedEvent);
			}
		}
	}

	public class RequestSearchResultEvent extends ActionEvent {
		final static long serialVersionUID = 1L;
		private int startIndex, endIndex;

		public RequestSearchResultEvent(Object source, int startIndex, int endIndex) {
			super(source, ActionEvent.ACTION_PERFORMED, REQUEST_SEARCH_RESULTS_EVENT);
			this.startIndex = startIndex;
			this.endIndex = endIndex;
		}
		public int getStart() {
			return startIndex;
		}
		public int getEnd() {
			return endIndex;
		}
	}

	/**
	 * List model for the search results.
	 */
	private class SearchResultModel extends AbstractListModel implements SearchResultListener {
		static final long serialVersionUID = 1L;
		private final SearchItem loadingItem;
		private ArrayList<SearchItem> results;
		private boolean isLoading;
		private int oldLength;

		public SearchResultModel() {
			loadingItem = new SearchItem("Loading...", "", null);
			results = new ArrayList<SearchItem>();
			isLoading = true;
			oldLength = 0;
		}

		public Object getElementAt(int index) {
			/* request more search results if we are at last item */
			if(isLoading && index == results.size()) {
				RequestSearchResultEvent event;
				event = new RequestSearchResultEvent(this, 0, results.size() + getVisibleRowCount());
				listener.actionPerformed(event);

				return loadingItem;
			}
			return results.get(index);
		}

		public SearchItem getSearchItemAt(int index) {
			if(index == results.size()) return null;
			return results.get(index);
		}

		public int getSize() {
			if(isLoading) {
				return results.size() + 1;
			}
			return results.size();
		}

		/**
		 * The Searcher object inserts the search results with this method.
		 */
		public void appendSearchResult(SearchItem item) {
			if(item == null) {
				isLoading = false;
				return;
			}

			results.add(item);
		}

		public void showResults() {
			SwingUtilities.invokeLater(new DataListenerNotifier());
		}
		private void updateResults() {
			int lastIndex;
			if(isLoading) {
				lastIndex = results.size();
			} else {
				lastIndex = results.size() - 1;
			}
			if(oldLength > 0) {
				fireContentsChanged(this, oldLength, oldLength);
			}
			if(oldLength < lastIndex) {
				fireIntervalAdded(this, oldLength, lastIndex);
			}
			oldLength = results.size();
		}
		private class DataListenerNotifier implements Runnable {
			public void run() {
				updateResults();
			}
		}

		public void resetSearchResults() {
			isLoading = true;
			oldLength = 0;
			results.clear();
			fireIntervalRemoved(this, 0, results.size());
		}

		public void setStatistics(int itemCount, int time) {
		}
	}

	/**
	 * Custom cell renderer that transforms the backend objects to JLabels.
	 */
	private class SearchResultRenderer extends JLabel implements ListCellRenderer {
		static final long serialVersionUID = 1L;

		public SearchResultRenderer() {
			Border paddingBorder;
			paddingBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
			this.setBorder(paddingBorder);
		}

		public Component getListCellRendererComponent(JList list, Object object,
		                                              int index, boolean isSelected,
		                                              boolean cellHasFocus)
		{
			SearchItem item;

			item = (SearchItem)object;
			/* ugly way to format the search items */
			if(Configs.getOptionBoolean("gui.useHTML")) {
				if(item.getDescription() == null) {
					setText("<html><body><font size='+1'><b>" + item.getWord() + "</b></font></body></html>");
				} else {
					setText("<html><body><font size='+1'><b>" + item.getWord() + "</b></font>" +
						"<br>" + item.getDescription() + "</body></html>");
				}
			} else {
				setText(item.getWord() + " - " + item.getDescription());
			}

			if(isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			setFont(list.getFont());
			setOpaque(true);

			return this;
		}
	}
}
