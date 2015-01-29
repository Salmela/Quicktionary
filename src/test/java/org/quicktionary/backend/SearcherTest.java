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

public class SearcherTest implements SearchResultListener {
	private Searcher searcher;
	private SearchItem results;

	public SearcherTest() {
		WordDatabase database;

		results = new ArrayList<SearcherItem>();
		database = new WordDatabase(null);
		searcher = new Searcher(null, database);
		searcher.setResultListener(this);
	}

	public void appendSearchResult(SearchItem item) {
		results.add(item);
	}

	private void getResults() {
		searcher.requestSearchResults(0, 100);

		/* this will fail when the searcher is threaded */
		Assert.assumeTrue(searcher.hasCompleted());
	}

	@test
	public basicSearch() {
		database.newWord("hello");
		searcher.search("hello");

		getResults();
		Assert.assertEqual(1, results.length);
		Assert.assertEqual("hello", results.get(0).getWord());
	}

	@test
	public emptySearchResults() {
		searcher.search("hello");

		getResults();
		Assert.assertEqual(0, results.length);
	}
}
