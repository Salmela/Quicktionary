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

import java.lang.Runnable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.quicktionary.backend.parsers.XMLParser;

/**
 * WikiDBReader extracts the wiki pages from a wikimedia database dump.
 */
public class WikiDBReader implements Runnable {
	private XMLParser parser;
	private WordDatabase database;
	private File file;

	private static final int NAMESPACE_TAG = 0;
	private static final int TITLE_TAG = 1;
	private static final int REVISION_TAG = 2;
	private static final int TEXT_TAG = 3;
	private static final int PAGE_TAG = 4;

	public WikiDBReader(WordDatabase database) {
		this.parser = new XMLParser();
		this.database = database;

		parser.setTagNameId("ns", NAMESPACE_TAG);
		parser.setTagNameId("title", TITLE_TAG);
		parser.setTagNameId("revision", REVISION_TAG);
		parser.setTagNameId("text", TEXT_TAG);
		parser.setTagNameId("page", PAGE_TAG);
	}

	public boolean check(String filename) {
		file = new File(filename);
		try {
			parser.parseFile(new FileReader(file));

		} catch(IOException exception) {
			return false;
		}

		/* get the root node */
		if(!parser.getRoot()) {
			return false;
		}
		if(!("mediawiki".equals(parser.getElementName()))) {
			return false;
		}

		return true;
	}

	private void readPage() {
		String text, title, ns;
		ns = null;
		title = null;
		text = null;

		//System.out.println("read page");

		/* get the first child node of the page*/
		if(!parser.getFirstChild()) {
			System.out.println("getting first child failed");
			return;
		}

		/* go through all child elements of the page */
		do {
			switch(parser.getElementNameId()) {
			case TITLE_TAG:
				title = parser.getTextContent();
				break;

			case NAMESPACE_TAG:
				ns = parser.getTextContent();
				break;

			case REVISION_TAG:
				parser.findElement(TEXT_TAG);
				text = parser.getTextContent(true);
				break;
			default:
				break;
			}
		} while(parser.getNextSibling());

		if("0".equals(ns)) {
			database.newWord(title);
			database.newPage(title, text);
		}
	}

	public void run() {
		if(!parser.isInitialized()) {
			throw new Error("You have to run first the check method.");
		}

		while(parser.findElement(PAGE_TAG)) {
			readPage();
		}
	}
}
