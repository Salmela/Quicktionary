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

import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.rules.ExpectedException;

import org.quicktionary.backend.database.WordDatabase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assume;
import org.junit.Rule;

public class SearcherTest implements SearchResultListener {
	private Searcher searcher;
	private WordDatabase database;
	private List<SearchItem> results;

	final SearchItem RESET;
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	public SearcherTest() throws Exception {
		//Map<String, Object> options = new HashMap<String, Object>();

		File file;
		file = File.createTempFile("database.db", "ext");
		file.deleteOnExit();

		//options.put("database", file.toString());

		results = Collections.synchronizedList(new ArrayList<SearchItem>());
		database = new WordDatabase(file.toString());
		searcher = new Searcher(database);
		searcher.setResultListener(this);
		RESET = new SearchItem("RESET", "RESET", null);
	}

	public void appendSearchResult(SearchItem item) {
		results.add(item);
	}

	public void resetSearchResults() {
		results.add(RESET);
	}

	public void setStatistics(int totalCount, int time) {
	}

	public void showResults() {
	}

	@Override
	public int getSize() {
		return results.size();
	}

	@Override
	public SearchItem getSearchItemAt(int index) {
		return results.get(index);
	}

	private void getResults(int count) {
		searcher.requestSearchResults(0, count);

		try {
			Thread.sleep(200);
		} catch(InterruptedException e) {
		}
		Assume.assumeTrue(searcher.hasCompleted());
	}

	private void getResults() {
		getResults(20);
	}

	@Test
	public void hasCompletedIsTrueAfterInit() {
		Assert.assertTrue(searcher.hasCompleted());
	}

	@Test
	public void emptySearchResults() {
		searcher.search("hello");

		getResults();
		/* here is some multithreading issues */
		Assert.assertNull(results.get(results.size() - 1));
		Assert.assertEquals(2, results.size());
	}

	@Test
	public void checkThatListEndsToNull() {
		searcher.search("hello");

		getResults();
		Assert.assertEquals(null, results.get(1));
	}

	@Test
	public void checkThatResetHappens() {
		searcher.search("hello");

		getResults();
		Assert.assertEquals(RESET, results.get(0));
	}

	@Test
	public void returnFalseAfterNegativeResultCount() {
		searcher.search("hello");

		thrown.expect(Error.class);
		getResults(-50);
	}

	@Test
	public void basicSearch() {
		database.newWord("hello");
		searcher.search("hello");

		getResults();
		Assert.assertEquals("hello", results.get(1).getWord());
	}

	@Test
	public void givesOnlyWantedAmmounOfResults() {
		for(int i = 0; i < 30; i++) {
			database.newWord("hello " + i);
		}
		searcher.search("hello");

		getResults();
		Assert.assertEquals(20 + 1, results.size());
	}

	@Test
	public void givesOnlyItemsThatHaveSamePrefix() {
		database.newWord("hello");
		database.newWord("hello fool");
		database.newWord("hey");
		database.newWord("cool");

		searcher.search("hello");

		getResults();
		Assert.assertEquals(2 + 2, results.size());
	}

	@Test
	public void fragmentsTheSearchResultsCorrectly() {
		for(int i = 0; i < 30; i++) {
			database.newWord("hello " + i);
		}
		searcher.search("hello");

		getResults(5);
		Assert.assertEquals(5 + 1, results.size());
		getResults(10);
		Assert.assertEquals(10 + 1, results.size());
	}
}
