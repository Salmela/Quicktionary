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

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.quicktionary.backend.Quicktionary;

public class MainWindow extends JFrame {
	private Quicktionary dictionary;

	public MainWindow(Quicktionary dictionary) {
		this.dictionary = dictionary;

		setTitle("Quicktionary");
		setSize(600, 400);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
}
