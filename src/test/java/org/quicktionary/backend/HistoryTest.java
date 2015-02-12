package org.quicktionary.backend;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class HistoryTest {
	private History history;

	public HistoryTest() {
		history = new History();
	}

	@Test
	public void getCurrentFromEmptyHistory() {
		assertNull(history.getCurrent());
	}

	@Test
	public void storeEvent() {
		history.saveEvent("search", "something");
		assertNotNull(history.getCurrent());
	}

	@Test
	public void getNextAtEndOfHistory() {
		history.saveEvent("search", "something");
		history.saveEvent("page", "something");
		assertNull(history.getNext(false));
	}

	@Test
	public void getNextAtStartOfHistory() {
		history.saveEvent("search", "something");
		history.saveEvent("page", "something");
		history.getPrevious(true);
		assertNotNull(history.getNext(false));
	}

	@Test
	public void getPreviousAtEndOfHistory() {
		history.saveEvent("search", "something");
		history.saveEvent("page", "something");
		assertNotNull(history.getPrevious(false));
	}

	@Test
	public void getPreviousAtStartOfHistory() {
		history.saveEvent("search", "something");
		history.saveEvent("page", "something");
		history.getPrevious(true);
		assertNull(history.getPrevious(false));
	}
}
