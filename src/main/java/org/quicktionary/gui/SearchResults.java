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

import org.quicktionary.backend.SearchResultListener;
import org.quicktionary.backend.SearchItem;

public class SearchResults extends JList {
	static final long serialVersionUID = 1L;

	private SearchResultRenderer renderer;
	private SearchResultModel model;

	public SearchResults() {
		renderer = new SearchResultRenderer();
		model = new SearchResultModel();

		setCellRenderer(renderer);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		setModel(model);
	}

	public SearchResultListener getSearchResultListener() {
		return model;
	}

	/**
	 * List model for the search results.
	 */
	private class SearchResultModel extends AbstractListModel implements SearchResultListener {
		static final long serialVersionUID = 1L;
		private ArrayList<SearchItem> results;

		public SearchResultModel() {
			results = new ArrayList<SearchItem>();
		}

		public Object getElementAt(int index) {
			return results.get(index);
		}

		public int getSize() {
			return results.size();
		}

		/**
		 * The Searcher object inserts the search results with this method.
		 */
		public void appendSearchResult(SearchItem item) {
			int index;
			results.add(item);
			index = results.size() - 1;
			fireIntervalAdded(this, index, index);
		}

		public void resetSearchResults() {
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
			setText("<html><body><font size='+1'><b>" + item.getWord() + "</b></font>" +
			        "<br>" + item.getDescription() + "</body></html>");

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
