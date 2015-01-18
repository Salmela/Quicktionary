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
import javax.swing.ListCellRenderer;
import java.awt.*;

import org.quicktionary.backend.SearchItem;

public class SearchResults extends JList {
	static final long serialVersionUID = 1L;

	public SearchResults() {
		SearchItem[] data = {
			new SearchItem("hello", "ghsfgrkshrg"),
			new SearchItem("hi", "ehguje seusg"),
			new SearchItem("bye", "ouoq ewfrhw")
		};

		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setListData(data);
		setCellRenderer(new ResultRenderer());
	}

	/*
	 * Main window passes list of backend objects to this object.
	 */
	public void setResults(Object[] data) {
		setListData(data);
	}

	/*
	 * Custom cell renderer that transforms the backend objects to JLabels.
	 */
	private class ResultRenderer extends JLabel implements ListCellRenderer {
		static final long serialVersionUID = 1L;

		public Component getListCellRendererComponent(JList list, Object object,
		                                              int index, boolean isSelected,
		                                              boolean cellHasFocus)
		{
			SearchItem item;

			item = (SearchItem)object;
			/* ugly way to format the search items */
			setText("<html><body><font size='+2'><b>" + item.getWord() + "</b></font>" +
			        "<br><font size='+1'>" + item.getDescription() + "</font></body></html>");

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
