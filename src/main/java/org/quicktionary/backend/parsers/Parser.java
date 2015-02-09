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
package org.quicktionary.backend.parsers;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.IOException;

import java.util.Arrays;

public abstract class Parser {
	private BufferedReader reader;
	private long address;
	private long sourceLineNum, sourceColumnNum;

	/**
	 * The current char must be at the start of next node
	 * after any method.
	 */
	protected char currentChar;
	protected char previousChar;
	protected boolean parsingError;
	protected boolean parsingErrorHappened;

	public class ParserError extends Error {
		static final long serialVersionUID = 1L;

		public ParserError(String message) {
			super(message);
		}
	}

	public Parser() {
		reader       = null;
		currentChar  = 0;
		parsingErrorHappened = false;
	}

	/**
	 * The method for starting the parser.
	 *
	 * @param stream The input stream which will be readed
	 * @throws IOException
	 */
	public boolean parse(Reader reader) throws IOException {
		this.reader  = new BufferedReader(reader);
		previousChar = 0;
		currentChar  = 0;
		address      = 0;
		parsingErrorHappened = false;

		sourceLineNum = 0;
		sourceColumnNum = 0;

		/* initialize the currentChar */
		if(!getNext()) {
			appendLog("File is empty");
			return false;
		}

		return true;
	}

	public boolean isInitialized() {
		return currentChar != 0;
	}

	public long getAddress() {
		return address;
	}

	public long getLocation() {
		return address;
	}

	public boolean parsingErrorHappened() {
		return parsingErrorHappened;
	}

	/**
	 * This method is used for reporting where in the file something is.
	 */
	public String getSourceLocation() {
		return String.format("%d:%d", sourceLineNum, sourceColumnNum);
	}

	/**
	 * The method prints parsing errors to log and the current stack trace.
	 */
	protected void appendLog(String message) {
		System.out.println("Parsing error occured at " + address + ": " + message);
		String trace = Arrays.toString(Thread.currentThread().getStackTrace());
		System.out.println(trace);
		parsingError = parsingErrorHappened = true;
	}

	protected void appendLog(ParserError exception) {
		System.out.println("Parsing error occured at " + address + ": " + exception.getMessage());
		exception.printStackTrace();
		parsingError = parsingErrorHappened = true;
	}

	/**
	 * Check if letter is whitespace. The method considers
	 * space, newline, tab, and carriage return as whitespace.
	 *
	 * @param letter The character that we want to check
	 * @return True, if the letter was ascii whitespace
	 */
	protected boolean isWhitespace(char letter) {
		if(letter == ' ' || letter == '\t' || letter == '\n' ||
		   letter == '\r') {
			return true;
		}
		return false;
	}

	/**
	 * Read next character from file.
	 * TODO: return boolean
	 *
	 * @return True, if the read was successful
	 */
	protected boolean getNext() {
		int result;
		previousChar = currentChar;

		try {
			result = reader.read();
			address++;
		} catch(IOException e) {
			currentChar = 0;
			return false;
		}

		if(result != -1) {
			currentChar = (char)result;
		} else {
			currentChar = 0;
			return false;
		}

		if(currentChar == '\n') {
			sourceLineNum++;
			sourceColumnNum = 0;
		} else if(previousChar != '\n') {
			sourceColumnNum++;
		}
		return true;
	}

	/**
	 * Skip over all following whitespace. The method throws exception if
	 * the current char isn't whitespace and the atLeastOne parameter is set.
	 */
	protected void skipWhitespaces(boolean atLeastOne) {
		/* check that there is whitespace */
		if(!isWhitespace(currentChar)) {
			if(atLeastOne) {
				appendLog("We expected whitespace, but there was " + currentChar);
			}
			return;
		}

		while(getNext() && isWhitespace(currentChar));
	}

	/**
	 * Helper method for verifying the current character. The method also
	 * gives helpful error message if the character wasn't what we expected.
	 *
	 * @param wanted The character we expect current character to be
	 */
	protected void expectChar(char wanted) {
		String errorString = "Expected '" + wanted + "' character, but was '" + (char)currentChar + "'.";

		if(currentChar != wanted) {
			throw new ParserError(errorString);
		} else {
			getNext();
		}
	}
}
