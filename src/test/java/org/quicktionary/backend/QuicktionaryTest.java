package org.quicktionary.backend;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class QuicktionaryTest implements SearchResultListener {
	private Quicktionary quicktionary;
	/* These tests only check the first search item.
	   More complete tests for searching are done at Searcher test suite.
	 */
	private SearchItem firstSearchItem;

	public QuicktionaryTest() {
		quicktionary = new Quicktionary();
		firstSearchItem = null;
	}

	public void appendSearchResult(SearchItem item) {
		if(firstSearchItem != null) return;
		firstSearchItem = item;
	}

	public void resetSearchResults() {
		firstSearchItem = null;
	}

	public void setStatistics(int totalCount, int time) {
		
	}

	public int getSize() {
		return (firstSearchItem == null) ? 0 : 1;
	}

	public SearchItem getSearchItemAt(int index) {
		return (index == 0) ? firstSearchItem : null;
	}


	@Test
	public void searchListenerCanBeSet() {
		/* verify that any exceptions are not thrown */
		quicktionary.setSearchResultListener(this);
	}

	@Test
	public void createWordAndSearch() {
		quicktionary.setSearchResultListener(this);
		quicktionary.newWord("test");
		quicktionary.search("test");
		quicktionary.requestSearchResults(0, 1);
		assertEquals(firstSearchItem.getWord(), "test");
	}

	@Test
	public void removeWordAndSearch() {
		quicktionary.setSearchResultListener(this);
		quicktionary.newWord("test");
		quicktionary.removeWord("test");
		quicktionary.search("test");
		quicktionary.requestSearchResults(0, 1);
		assertEquals(firstSearchItem, null);
	}

	@Test
	public void readWikiDump() {
		quicktionary.readDatabase("src/test/java/org/quicktionary/backend/test_pages.xml", true);

		quicktionary.setSearchResultListener(this);
		quicktionary.search("Some");
		quicktionary.requestSearchResults(0, 1);
		assertEquals(firstSearchItem.getWord(), "Some word");
		assertEquals(quicktionary.getPageContent(firstSearchItem, ""), "Test page 2");
	}

	@Test
	public void readUnExistingWikiDump() {
		/* verify that any exceptions are not thrown */
		quicktionary.readDatabase("not-real-file");
	}

	@Test
	public void getNextThatDoesntExist() {
		assertNull(quicktionary.getNextView(false));
	}

	@Test
	public void getPreviousThatDoesntExist() {
		assertNull(quicktionary.getPreviousView(false));
	}

	@Test
	public void getNextThatExists() {
		quicktionary.setSearchResultListener(this);
		quicktionary.search("test");
		quicktionary.search("test");
		quicktionary.getPreviousView(true);
		assertNotNull(quicktionary.getNextView(false));
	}

	@Test
	public void getPreviousThatExists() {
		quicktionary.setSearchResultListener(this);
		quicktionary.search("test");
		quicktionary.search("test");
		assertNotNull(quicktionary.getPreviousView(false));
	}
}
