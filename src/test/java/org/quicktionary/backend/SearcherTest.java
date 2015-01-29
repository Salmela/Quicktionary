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

import java.util.ArrayList;

public class SearcherTest implements SearchResultListener {
	private Searcher searcher;
	private WordDatabase database;
	private ArrayList<SearchItem> results;

	public SearcherTest() {
		results = new ArrayList<SearchItem>();
		database = new WordDatabase(null);
		searcher = new Searcher(null, database);
		searcher.setResultListener(this);
	}

	public void appendSearchResult(SearchItem item) {
		results.add(item);
	}

	public void resetSearchResults() {
		results.add(null);
	}

	public void setStatistics(int totalCount, int time) {
	}

	private boolean getResults(int count) {
		boolean ret;
		ret = searcher.requestSearchResults(0, count);

		if(!ret) {
			Assert.assertTrue(searcher.hasCompleted());
		}
		/* this will fail when the searcher is threaded */
		Assume.assumeTrue(searcher.hasCompleted());
		return ret;
	}
	
	private boolean getResults() {
		return getResults(20);
	}

	@Test
	public void hasCompletedIsTrueAfterInit() {
		Assert.assertTrue(searcher.hasCompleted());
	}

	@Test
	public void emptySearchResults() {
		searcher.search("hello");

		getResults();
		Assert.assertEquals(1, results.size());
	}

	@Test
	public void checkThatResetHappens() {
		searcher.search("hello");

		getResults();
		Assert.assertEquals(null, results.get(0));
	}

	@Test
	public void returnFalseAfterNegativeResultCount() {
		searcher.search("hello");

		Assert.assertFalse(getResults(-50));
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
}
