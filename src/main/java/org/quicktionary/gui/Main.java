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

import org.quicktionary.backend.Quicktionary;

/**
 * The Main class parses the command line arguments and starts the main window.
 */
public class Main {
	static boolean useNativeFileDialog;
	static String databasePath;

	public static void init() {
		useNativeFileDialog = false;
		databasePath = null;
	}

	/**
	 * Read command line arguments. If the word isn't proper flag it is
	 * considered to be path to database. If there is multiple paths in
	 * the arguments, then only the last one is used.
	 * @param args the command line arguments
	 */
	public static void parseCommandlineArgs(String[] args) {
		int i;
		for(i = 0; i < args.length; i++) {
			String command = args[i];
			if(command.equals("--native-file-chooser") ||
			   command.equals("-n")) {
				useNativeFileDialog = true;

			} else if(command.charAt(0) != '-') {
				i++;
				databasePath = args[i];
			}
		}
	}

	/**
	 * Start the backend and open the main window.
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		Quicktionary quicktionary;
		MainWindow window;

		Main.init();
		Main.parseCommandlineArgs(args);

		quicktionary = new Quicktionary();
		window = new MainWindow(quicktionary);

		window.setVisible(true);
		System.out.println("Hello world!");
	}
}
