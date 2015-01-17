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
import java.awt.event.*;
import javax.swing.event.*;

/*
 * Search box widget that is at the header of the main window.
 * TODO: add placeholder text
 * TODO: add a button aligned to the right.
 */
public class SearchBox extends JTextField {

	public SearchBox(ActionListener searchListener) {
		DocumentListener changeListener;
		changeListener = new onChangeEvents(this, searchListener);
		getDocument().addDocumentListener(changeListener);
	}

	private class onChangeEvents implements DocumentListener {
		private final ActionListener listener;
		private final Object source;

		public onChangeEvents(Object source, ActionListener listener) {
			this.listener = listener;
			this.source   = source;
		}
		private void emitEvent() {
			listener.actionPerformed(new ActionEvent(source, ActionEvent.ACTION_PERFORMED, "search-event"));
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
}
