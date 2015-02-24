/* Quicktionary backend - Word translator app
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
package org.quicktionary.backend;

import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;
import java.util.AbstractMap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class Configs {
	private static HashMap<String, Object> options;
	private File configFile;

	protected Configs() {
		options = new HashMap<String, Object>();
	}

	protected void setOption(String key, Object value) {
		options.put(key, value);
	}

	public static Object getOption(String key) {
		Object obj = options.get(key);

		if(obj == null) {
			throw new Error("Option \"" + key + "\" is not defined");
		}
		return obj;
	}

	public static boolean getOptionBoolean(String key) {
		Boolean bool = (Boolean)getOption(key);
		if(!(bool instanceof Boolean)) {
			throw new Error("Option is not boolean");
		}
		return bool.booleanValue();
	}

	public static String getOptionString(String key) {
		String str = (String)getOption(key);
		if(!(str instanceof String)) {
			throw new Error("Option is not string");
		}
		return str;
	}

	public static int getOptionInt(String key) {
		Integer num = (Integer)getOption(key);
		if(!(num instanceof Integer)) {
			throw new Error("Option is not integer");
		}
		return num.intValue();
	}

	/**
	 * Parse the config file.
	 * If the line starts with # then it is a comment.
	 */
	protected void parseConfigFile(File file) {
		Scanner scanner;

		configFile = file;
		try {
			scanner = new Scanner(file);
		} catch(FileNotFoundException e) {
			writeConfigFile();
			return;
		}

		while(scanner.hasNextLine()) {
			Map.Entry<String, Object> entry;

			entry = parseLine(scanner.nextLine());
			if(entry == null) {
				continue;
			}
			options.put(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Parse single line of the config file.
	 */
	private Map.Entry<String, Object> parseLine(String line) {
		String name, value;
		int i;

		/* ignore comment lines */
		if(line.charAt(0) == '#') {
			return null;
		}

		name = null;

		/* extract the name */
		for(i = 0; i < line.length(); i++) {
			if(line.charAt(i) == ' ' || line.charAt(i) == '\t') {
				name = line.substring(0, i);
				break;
			}
		}
		if(name == null) return null;

		/* extract the value */
		for(; i < line.length(); i++) {
			if(line.charAt(i) != ' ' && line.charAt(i) != '\t') {
				break;
			}
		}
		value = line.substring(i, line.length());
		if(value.length() > 0) {
			return new AbstractMap.SimpleEntry<String, Object>(name, parseValue(value));
		}
		return null;
	}

	/**
	 * Parse the value of a property.
	 */
	private Object parseValue(String value) {
		/* try to parse the value as boolean */
		if(value.startsWith("true")) {
			return new Boolean(true);
		} else if(value.startsWith("false")) {
			return new Boolean(false);
		}

		/* try to parse the value as string */
		if(value.charAt(0) == '\"') {
			/* find the end quote */
			int end = value.lastIndexOf('\"');
			if(end == -1) {
				return null;
			}
			value = value.substring(1, end);
			value = value.replace("\\\"", "\"");
			value = value.replace("\\\\", "\\");
			return value;
		}

		/* try to parse the value as number */
		try {
			return new Integer(Integer.parseInt(value));
		} catch(NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Write the config into file.
	 * NOTE: This doesn't handle multiple programs
	 * writing the config file at the same time.
	 */
	public void writeConfigFile() {
		FileOutputStream outStream;
		Scanner scanner;
		String text;
		byte[] data;

		try {
			/* read the current state of the config file */
			FileInputStream inStream = new FileInputStream(configFile);
			data = new byte[(int)configFile.length()];
			inStream.read(data);
			inStream.close();

			text = new String(data, "UTF-8");
		} catch(Exception exception) {
			text = "";
			/* create the file if the reading of the file fails */
			try {
				configFile.createNewFile();
			} catch(IOException ioexception) {
				/* failed to create new file */
				return;
			}
		}
		scanner = new Scanner(text);

		/* open the config file again, but now open if for writing */
		try {
			outStream = new FileOutputStream(configFile, false);
		} catch(FileNotFoundException exception) {
			return;
		}

		/* write the new config file line by line */
		while(scanner.hasNextLine()) {
			Map.Entry<String, Object> entry;

			entry = parseLine(scanner.nextLine());

			/* write the line */
			/*TODO*/
		}
		try {
			outStream.close();
		} catch(IOException exception) {
		}
	}
}
