package org.quicktionary.backend;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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

	public QuicktionaryTest() throws Exception {
		Map<String, Object> options = new HashMap<String, Object>();

		File file;
		file = File.createTempFile("database.db", "ext");
		file.deleteOnExit();

		options.put("database", file.toString());
		quicktionary = new Quicktionary(null, options);
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

	public void showResults() {
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
		WordEntry e = quicktionary.getWordEntry("Some word");
		assertEquals(e.getWord(), "Some word");
		assertEquals(quicktionary.getPageContent(e).getSource(), "Test page 2");
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

	private class E implements HistoryEvent {
		public int id;
		public E(int id) {
			this.id = id;
		}
		public String getEventType() {
			return "e";
		}

		public boolean truncateSimilar() {
			return false;
		}
	}

	@Test
	public void storeEvent() {
		/* check that there isn't exceptions */
		quicktionary.storeEvent(new E(1));
	}

	@Test
	public void getNextThatExists() {
		HistoryEvent e = new E(2);
		quicktionary.storeEvent(new E(1));
		quicktionary.storeEvent(e);
		quicktionary.getPreviousView(true);
		assertEquals(e, quicktionary.getNextView(false));
	}

	@Test
	public void getPreviousThatExists() {
		HistoryEvent e = new E(1);
		quicktionary.storeEvent(e);
		quicktionary.storeEvent(new E(2));
		assertEquals(e, quicktionary.getPreviousView(false));
	}
}
