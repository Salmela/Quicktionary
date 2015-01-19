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
	static final String PRESSED_EVENT = "pressed-event";

	private MainWindow window;
	private JPopupMenu menu;

	public SettingsButton(MainWindow window) {
		this.window = window;
		setText("Settings");
		addActionListener(this);
		setActionCommand(PRESSED_EVENT);

		makeMenu();
	}

	private void makeMenu() {
		JMenuItem menuItem;

		menu = new JPopupMenu();
		menuItem = new JMenuItem("New word");
		menu.add(menuItem);
		menuItem = new JMenuItem("Remove word");
		menu.add(menuItem);
		menuItem = new JMenuItem("Read database");
		menu.add(menuItem);
		menu.addSeparator();
		menuItem = new JMenuItem("Preferences");
		menu.add(menuItem);
	}

	public void actionPerformed(ActionEvent event) {
		if(event.getActionCommand().equals(PRESSED_EVENT)) {
			int menuWidth = menu.getWidth();
			int buttonWidth  = this.getWidth();
			int buttonHeight = this.getHeight();
			menu.show(this, buttonWidth - menuWidth, buttonHeight);
		}
	}
}
