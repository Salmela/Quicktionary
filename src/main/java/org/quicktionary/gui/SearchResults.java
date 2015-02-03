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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import org.quicktionary.backend.SearchResultListener;
import org.quicktionary.backend.SearchItem;

/**
 * It seems that the JList UI doesn't update the setVisibileRowCount in Classpath runtime.
 * setVisibleRowCount(getPreferredSize().height / size.height);
 */
public class SearchResults extends JList {
	static final long serialVersionUID = 1L;
	static final String REQUEST_SEARCH_RESULTS_EVENT = "request-search-results-event";
	static final String PAGE_LOAD_EVENT = "page-load-event";

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

	/**
	 *
	 */
	public class mouseHandler extends MouseAdapter {
		public void mouseClicked(MouseEvent event) {
			if (event.getClickCount() == 2) {
				PageLoadEvent emittedEvent;
				int i = getSelectedIndex();

				System.out.println("double click");
				emittedEvent = new PageLoadEvent(this, (SearchItem)model.getElementAt(i));
				listener.actionPerformed(emittedEvent);
			}
		}
	}

	public class PageLoadEvent extends ActionEvent {
		final static long serialVersionUID = 1L;
		private String searchQuery;
		private SearchItem searchItem;

		public PageLoadEvent(Object source, SearchItem item) {
			super(source, ActionEvent.ACTION_PERFORMED, PAGE_LOAD_EVENT);
			this.searchItem = item;
			this.searchQuery = null;
		}

		public SearchItem getSearchItem() {
			return searchItem;
		}

		public void setSearchQuery(String query) {
			searchQuery = query;
		}

		public String getSearchQuery() {
			return searchQuery;
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

		public SearchResultModel() {
			loadingItem = new SearchItem("Loading...", "", null);
			results = new ArrayList<SearchItem>();
			isLoading = true;
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
			int index;

			if(item == null) {
				isLoading = false;
				fireIntervalRemoved(this, results.size(), results.size());
				return;
			}

			results.add(item);
			index = results.size() - 1;
			fireIntervalAdded(this, index, index);
		}

		public void resetSearchResults() {
			isLoading = true;
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
			if(Main.useHTML) {
				setText("<html><body><font size='+1'><b>" + item.getWord() + "</b></font>" +
				        "<br>" + item.getDescription() + "</body></html>");
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
