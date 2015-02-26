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

	private class ETruncatable implements HistoryEvent {
		public int id;
		public ETruncatable(int id) {
			this.id = id;
		}
		public String getEventType() {
			return "f";
		}

		public boolean truncateSimilar() {
			return true;
		}
	}

	@Test
	public void getCurrentFromEmptyHistory() {
		assertNull(history.getCurrent());
	}

	@Test
	public void storeEvent() {
		HistoryEvent e = new E(0);
		history.saveEvent(e);
		assertEquals(e, history.getCurrent());
	}

	@Test
	public void getNextAtEndOfHistory() {
		HistoryEvent e = new E(1);
		HistoryEvent e2 = new E(2);
		history.saveEvent(e);
		history.saveEvent(e2);
		assertNull(history.getNext(false));
	}

	@Test
	public void getNextAtStartOfHistory() {
		HistoryEvent e = new E(1);
		HistoryEvent e2 = new E(2);
		history.saveEvent(e);
		history.saveEvent(e2);
		history.getPrevious(true);
		assertEquals(e2, history.getNext(false));
	}

	@Test
	public void getPreviousAtEndOfHistory() {
		HistoryEvent e = new E(1);
		HistoryEvent e2 = new E(2);
		history.saveEvent(e);
		history.saveEvent(e2);
		assertEquals(e, history.getPrevious(false));
	}

	@Test
	public void getPreviousAtStartOfHistory() {
		HistoryEvent e = new E(1);
		HistoryEvent e2 = new E(2);
		history.saveEvent(e);
		history.saveEvent(e2);
		history.getPrevious(true);
		assertNull(history.getPrevious(false));
	}


	@Test
	public void getCurrentForTruncatedEvent() {
		HistoryEvent e = new ETruncatable(1);
		HistoryEvent e2 = new ETruncatable(2);
		history.saveEvent(e);
		history.saveEvent(e2);
		assertEquals(e2, history.getCurrent());
	}

	@Test
	public void getPreviousForTruncatedEvent() {
		HistoryEvent e = new ETruncatable(1);
		HistoryEvent e2 = new ETruncatable(2);
		history.saveEvent(e);
		history.saveEvent(e2);
		assertNull(history.getPrevious(false));
	}

	@Test
	public void getPreviousAfterTruncatedEvents() {
		HistoryEvent e = new E(1);
		HistoryEvent e1 = new ETruncatable(2);
		HistoryEvent e2 = new ETruncatable(3);
		history.saveEvent(e);
		history.saveEvent(e1);
		history.saveEvent(e2);
		assertEquals(e, history.getPrevious(false));
	}

	@Test
	public void getNextAtStartOfTruncatedEvents() {
		HistoryEvent e = new E(1);
		HistoryEvent e1 = new ETruncatable(2);
		HistoryEvent e2 = new ETruncatable(3);
		history.saveEvent(e);
		history.saveEvent(e1);
		history.saveEvent(e2);
		history.getPrevious(true);
		assertEquals(e2, history.getNext(false));
	}
}
