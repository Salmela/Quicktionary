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
import java.io.StringReader;
import java.io.IOException;

import java.util.Calendar;

import org.quicktionary.backend.parsers.XMLParser;
import org.quicktionary.backend.parsers.WikiMarkup;
import org.quicktionary.backend.database.WordDatabase;

/**
 * WikiDBReader extracts the wiki pages from a wikimedia database dump.
 */
public class WikiDBReader implements Runnable {
	private XMLParser parser;
	private WikiMarkup wikiParser;
	private WordDatabase database;
	private File file;
	private boolean dontOverwrite;

	private static final int NAMESPACE_TAG = 0;
	private static final int TITLE_TAG = 1;
	private static final int REVISION_TAG = 2;
	private static final int TEXT_TAG = 3;
	private static final int PAGE_TAG = 4;

	public WikiDBReader(WordDatabase database) {
		this.parser = new XMLParser();
		this.wikiParser = new WikiMarkup();
		this.database = database;
		this.dontOverwrite = true;

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

	private void createPage(String text, String title, String ns) {
		if(text == null || title == null || ns == null) {
			return;
		}
		/* check that the page is normal page */
		if(!"0".equals(ns)) {
			return;
		}

		WordEntry entry;

		if(dontOverwrite && database.containsWordEntry(title)) {
			return;
		}

		try {
			wikiParser.parse(new StringReader(text));
		} catch(IOException exception) {
			return;
		}

		TextNode root = wikiParser.getRoot();

		/* add a header to the page */
		TextNode header = new TextNode(TextNode.HEADER_TYPE, "1");
		header.setTextContent(title);
		root.prependChild(header);

		entry = database.newWord(title);
		entry.addSource(text);
		entry.setContent(root);
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

		createPage(text, title, ns);
	}

	public void run() {
		Calendar next;
		if(!parser.isInitialized()) {
			throw new Error("You have to run first the check method.");
		}

		next = Calendar.getInstance();
		next.roll(Calendar.MINUTE, 10);

		while(parser.findElement(PAGE_TAG)) {
			/* check if 10 minutes is gone */
			if(next.before(Calendar.getInstance())) {
				database.sync();
				next = Calendar.getInstance();
				next.roll(Calendar.MINUTE, 10);
			}
			readPage();
		}
		database.sync();
	}

	/**
	 * Start the parsing thread for WikDBReader.
	 */
	public void readDatabase(String filename, boolean wait) {
		Thread thread;

		thread = new Thread(this);
		thread.start();

		if(!wait) {
			return;
		}

		try {
			thread.join();
		} catch (InterruptedException ex) {
		}
	}
}
