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

import java.io.IOException;
import java.lang.Runtime;
import org.quicktionary.backend.Quicktionary;

/**
 * The Main class parses the command line arguments and starts the application.
 */
public class Main {
	public static boolean useNativeFileDialog;
	public static boolean useHTML;
	public static String databasePath;
	public static String themeName;

	public static void init() {
		useNativeFileDialog = false;
		databasePath = null;
		themeName = null;
		useHTML = true;
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
			String option = args[i];
			if(option.equals("--native-file-chooser") ||
			   option.equals("-n")) {
				useNativeFileDialog = true;

			} else if(option.equals("--theme") ||
			          option.equals("-t")) {
				themeName = args[++i];

			} else if(option.equals("--no-html") ||
			          option.equals("-h")) {
				useHTML = false;

			} else if(option.equals("--help") ||
			          option.equals("-h")) {
				System.out.println("TODO!");

			} else if(option.charAt(0) != '-') {
				databasePath = args[++i];
			}
		}
	}

	/**
	 * Start the backend and open the main window.
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		Quicktionary quicktionary;
		Application application;
		Map<String, Object> options;

		//Test test = new Test();

		options = new HashMap<String, Object>();
		Main.init();
		Main.parseCommandlineArgs(args);

		quicktionary = new Quicktionary(options);
		application = new Application(quicktionary);

		/* write the changes to the database at exit */
		Runtime.getRuntime().addShutdownHook(new ExitHook(quicktionary));
	}

	private static final class ExitHook extends Thread {
		private Quicktionary quicktionary;
		public ExitHook(Quicktionary quicktionary) {
			this.quicktionary = quicktionary;
		}
		public void run() {
			quicktionary.close();
		}
	}
}
