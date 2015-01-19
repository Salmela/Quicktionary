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

import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * The MainWindow is only class that communicates with the backend.
 */
public class SettingsButton extends JButton implements ActionListener {
	static final long serialVersionUID = 1L;
	static final String PRESSED_EVENT       = "pressed-event";

	/* menu item events */
	static final String NEW_WORD_ITEM_EVENT      = "new-word-item-event";
	static final String REMOVE_WORD_ITEM_EVENT   = "remove-word-item-event";
	static final String READ_DATABASE_ITEM_EVENT = "read-database-item-event";
	static final String PREFERENCES_ITEM_EVENT   = "preferences-item-event";

	/* events send to the parent class */
	static final String NEW_WORD_EVENT      = "new-word-event";
	static final String REMOVE_WORD_EVENT   = "remove-word-event";
	static final String READ_DATABASE_EVENT = "read-database-event";
	static final String PREFERENCES_EVENT   = "preferences-event";

	private ActionListener listener;
	private JPopupMenu menu;

	public SettingsButton(ActionListener listener) {
		this.listener = listener;
		setText("Settings");
		addActionListener(this);
		setActionCommand(PRESSED_EVENT);

		makeMenu();
	}

	private void makeMenu() {
		JMenuItem menuItem;

		menu = new JPopupMenu();

		menuItem = new JMenuItem("New word");
		menuItem.addActionListener(this);
		menuItem.setActionCommand(NEW_WORD_ITEM_EVENT);
		menu.add(menuItem);

		menuItem = new JMenuItem("Remove word");
		menuItem.addActionListener(this);
		menuItem.setActionCommand(REMOVE_WORD_ITEM_EVENT);
		menu.add(menuItem);

		menuItem = new JMenuItem("Read database");
		menuItem.addActionListener(this);
		menuItem.setActionCommand(READ_DATABASE_ITEM_EVENT);
		menu.add(menuItem);

		menu.addSeparator();

		menuItem = new JMenuItem("Preferences");
		menuItem.addActionListener(this);
		menuItem.setActionCommand(PREFERENCES_ITEM_EVENT);
		menu.add(menuItem);
	}

	public void actionPerformed(ActionEvent event) {
		if(event.getActionCommand().equals(PRESSED_EVENT)) {
			int menuWidth, buttonWidth, buttonHeight;

			menu.setVisible(true);
			menuWidth = menu.getWidth();
			buttonWidth  = this.getWidth();
			buttonHeight = this.getHeight();

			menu.show(this, buttonWidth - menuWidth, buttonHeight);

		} else if(event.getActionCommand().equals(READ_DATABASE_ITEM_EVENT)) {
			ReadDatabaseDialog dialog;
			ActionEvent event2;
			String filename;

			dialog = new ReadDatabaseDialog(this);
			dialog.showDialog();
			filename = dialog.getFilename();

			event2 = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, READ_DATABASE_EVENT);
			listener.actionPerformed(event2);
		} else {
			System.out.println("setting button: unknown event (" +
			                   event.getActionCommand() + ")");
		}
	}
}
