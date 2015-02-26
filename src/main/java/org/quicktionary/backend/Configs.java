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
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.AbstractMap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

/**
 * The config system that stores settings/options. The class
 * can also write the options to file and read the options from a file.
 */
public class Configs {
	private static HashMap<String, Object> options;
	private File configFile;

	protected Configs() {
		options = new HashMap<String, Object>();
	}

	/**
	 * Set the value of a option. This will overwrite
	 * the previous value.
	 * @param key The name of the option
	 * @param value The new value of the option
	 */
	protected void setOption(String key, Object value) {
		options.put(key, value);
		//System.out.println("config set " + key + " " + valueToString(value));
	}

	/**
	 * Get the value of a option as Object type.
	 * @param key The name of the option
	 * @return Object of the option
	 */
	public static Object getOption(String key) {
		Object obj = options.get(key);
		//System.out.println("config get " + key);

		if(obj == null) {
			throw new Error("Option \"" + key + "\" is not defined");
		}
		return obj;
	}

	/**
	 * Get the value of a boolean option.
	 * @param key The name of the option
	 * @return Value of the option
	 */
	public static boolean getOptionBoolean(String key) {
		Boolean bool = (Boolean)getOption(key);
		if(!(bool instanceof Boolean)) {
			throw new Error("Option is not boolean");
		}
		return bool.booleanValue();
	}

	/**
	 * Get the value of a string option.
	 * @param key The name of the option
	 * @return Value of the option
	 */
	public static String getOptionString(String key) {
		String str = (String)getOption(key);
		if(!(str instanceof String)) {
			throw new Error("Option is not string");
		}
		return str;
	}

	/**
	 * Get the value of a integer option.
	 * @param key The name of the option
	 * @return Value of the option
	 */
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
	 * @param file The config file
	 */
	protected void parseConfigFile(File file) {
		Scanner scanner;

		configFile = file;
		try {
			scanner = new Scanner(file);
		} catch(FileNotFoundException e) {
			writeConfigFile(true);
			return;
		}

		while(scanner.hasNextLine()) {
			Map.Entry<String, Object> option;

			option = parseLine(scanner.nextLine());
			if(option == null) {
				continue;
			}
			setOption(option.getKey(), option.getValue());
		}
	}

	/**
	 * Parse single line of the config file.
	 * @param line A line from config file
	 * @return The option as key-value pair
	 */
	private Map.Entry<String, Object> parseLine(String line) {
		String name, value;
		int i;

		if(line.length() == 0) {
			return null;
		}

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
	 * @param value The value from the file
	 * @return Object for the value
	 */
	private Object parseValue(String value) {
		/* try to parse the value as boolean */
		if(value.startsWith("true")) {
			return new Boolean(true);
		} else if(value.startsWith("false")) {
			return new Boolean(false);
		} else if(value.startsWith("null")) {
			return null;
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
		writeConfigFile(false);
	}
	public void writeConfigFile(boolean created) {
		Scanner scanner;
		String text;
		byte[] data;

		if(!configFile.exists()) {
			text = "";
			/* create the file if the reading of the file fails */
			try {
				configFile.createNewFile();
			} catch(IOException ioexception) {
				/* failed to create new file */
				return;
			}
		} else {
			try {
				/* read the current state of the config file */
				FileInputStream inStream = new FileInputStream(configFile);
				data = new byte[(int)configFile.length()];
				inStream.read(data);
				inStream.close();

				text = new String(data, "UTF-8");
			} catch(Exception exception) {
				return;
			}
		}
		scanner = new Scanner(text);
		try {
			overwriteConfigFile(scanner, created, null);
		} catch(IOException exception) {
		}
	}

	/**
	 * Overwrite the config file after the writeConfigFile readed it.
	 * @param scanner The line of the old file
	 * @throws IOException
	 */
	private void overwriteConfigFile(Scanner scanner, boolean created, Set<String> mask) throws IOException {
		FileOutputStream outStream;
		OutputStreamWriter printer;
		Set<String> writtenOptions = new HashSet<String>();

		/* open the config file again, but now open if for writing */
		outStream = new FileOutputStream(configFile, false);
		printer = new OutputStreamWriter(outStream, "UTF-8");

		if(created) {
			printer.write("# This file was generated automatically by Quicktionary\n\n");
		}

		/* write the new config file line by line */
		while(scanner.hasNextLine()) {
			Map.Entry<String, Object> option;
			String line = scanner.nextLine();

			/* parse line from the old config file */
			option = parseLine(line);

			/* write the line as is if it doesn't contain option */
			if(option == null) {
				printer.write(line + "\n");
				continue;
			}

			/* remove duplicates options */
			if(writtenOptions.contains(option.getKey())) {
				continue;
			}

			writtenOptions.add(option.getKey());

			/* ignore every other line expect the masked options */
			if(mask != null && !mask.contains(option.getKey())) {
				printer.write(line + "\n");
				continue;
			}

			/* write the unrecognised options as is back to the file */
			if(!options.containsKey(option.getKey())) {
				printer.write(line + "\n");
				continue;
			}

			Object valueObj = options.get(option.getKey());
			printer.write(option.getKey());
			printer.write(" ");
			printer.write(valueToString(valueObj));
			printer.write("\n");
		}

		/* go through rest of the options that aren't written yet */
		for(Map.Entry<String, Object> option : options.entrySet()) {
			if(writtenOptions.contains(option.getKey())) {
				continue;
			}

			printer.write(option.getKey());
			printer.write(" ");
			printer.write(valueToString(option.getValue()));
			printer.write("\n");
		}

		printer.flush();
		outStream.close();
	}

	/**
	 * Create a string for option value.
	 * @param obj The value of the option
	 * @return The value as string
	 */
	private String valueToString(Object obj) {
		if(obj instanceof Boolean) {
			boolean b = ((Boolean)obj).booleanValue();
			return b ? "true" : "false";
		} else if(obj instanceof Integer) {
			Integer i = (Integer)obj;
			return i.toString();
		} else if(obj instanceof String) {
			String str = (String)obj;
			return "\"" + str + "\"";
		}
		throw new Error("Unknown type for option");
	}
}
