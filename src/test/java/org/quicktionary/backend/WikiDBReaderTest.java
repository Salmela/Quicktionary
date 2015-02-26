/* Quicktionary test
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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import org.junit.Assume;

import org.quicktionary.backend.database.WordDatabase;
//import org.quicktionary.backend.SearchItem;

public class WikiDBReaderTest {
	private WordDatabase db;
	private WikiDBReader reader;

	public WikiDBReaderTest() {
		db = new WordDatabase();
		reader = new WikiDBReader(db);
	}

	@Test
	public void parseTheHeader() {
		Assert.assertTrue(reader.check("src/test/java/org/quicktionary/backend/test_pages.xml"));
	}

	@Test
	public void checkThatWordWasAdded() {
		reader.check("src/test/java/org/quicktionary/backend/test_pages.xml");
		reader.run();

		WordEntry[] entries = new WordEntry[5];

		db.requestResults("");
		Assert.assertEquals(1, db.fetchResults(entries, 5));
	}

	@Test
	public void fetchThePageOfWord() {
		reader.check("src/test/java/org/quicktionary/backend/test_pages.xml");
		reader.run();

		WordEntry[] entries = new WordEntry[5];

		db.requestResults("");
		db.fetchResults(entries, 5);
		Assert.assertEquals("Test page 2", entries[0].getSource());
	}
}
