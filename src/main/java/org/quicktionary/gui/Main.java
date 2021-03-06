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

import java.lang.Runtime;
import org.quicktionary.backend.Quicktionary;

import java.util.HashMap;
import java.util.Map;

/**
 * The Main class parses the command line arguments and starts the application.
 */
public class Main {
	public static void init(Map<String, Object> defaults) {
		defaults.put("gui.useNativeFileDialog", new Boolean(false));
		defaults.put("gui.themeName", "native");
		defaults.put("gui.useHTML", new Boolean(true));
		defaults.put("gui.demo", new Boolean(false));
	}

	/**
	 * Read command line arguments. If the word isn't proper flag it is
	 * considered to be path to database. If there is multiple paths in
	 * the arguments, then only the last one is used.
	 * @param args the command line arguments
	 */
	public static void parseCommandlineArgs(Map<String, Object> options, String[] args) {
		int i;

		for(i = 0; i < args.length; i++) {
			String option = args[i];
			if(option.equals("--native-file-chooser") ||
			   option.equals("-n")) {
				options.put("gui.useNativeFileDialog", new Boolean(true));

			} else if(option.equals("--theme") ||
			          option.equals("-t")) {
				options.put("gui.themeName", args[++i]);

			} else if(option.equals("--no-html") ||
			          option.equals("-h")) {
				options.put("gui.useHTML", new Boolean(false));

			} else if(option.equals("--help") ||
			          option.equals("-h")) {
				System.out.println("TODO!");

			} else if(option.equals("--demo") ||
			          option.equals("-d")) {
				options.put("gui.demo", new Boolean(true));

			} else if(option.charAt(0) != '-') {
				options.put("databasePath", args[++i]);
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
		Map<String, Object> defaults;

		//Test test = new Test();

		/* get the gui defaults */
		defaults = new HashMap<String, Object>();
		Main.init(defaults);

		/* parse commandline arguments */
		options = new HashMap<String, Object>();
		Main.parseCommandlineArgs(options, args);

		/* create the main classes */
		quicktionary = new Quicktionary(defaults, options);
		application = new Application(quicktionary);

		/* write the changes to the database at exit */
		Runtime.getRuntime().addShutdownHook(new ExitHook(quicktionary));

		application.run();
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
