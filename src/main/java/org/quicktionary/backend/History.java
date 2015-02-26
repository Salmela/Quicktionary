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

import java.util.ArrayList;

/**
 * The class stores browsing history and allows
 * user to go forward and back in history.
 * TODO: somehow compress many search events into one.
 */
class History {
	private ArrayList<HistoryEvent> events;
	private int currentIndex;

	public History() {
		events = new ArrayList<HistoryEvent>();
		currentIndex = -1;
	}

	private int indexOfLastEvent() {
		return events.size() - 1;
	}

	/**
	 * Store a event to the history.
	 */
	public void saveEvent(HistoryEvent event) {
		if(event.truncateSimilar() && currentIndex > 0) {
			HistoryEvent prev = getPrevious(false);
			if(prev != null && prev.getEventType() == event.getEventType()) {
				getPrevious(true);
			}
		}

		/* truncate the list if we aren't at the end */
		if(indexOfLastEvent() > currentIndex) {
			events.subList(currentIndex + 1, events.size()).clear();
			currentIndex = events.size() - 1;
		}

		/* append the new event */
		events.add(event);
		currentIndex++;
		//System.out.println("history now " + currentIndex + " / " + events.size());
	}

	private HistoryEvent getRelative(int offset, boolean go) {
		int newIndex;

		newIndex = currentIndex + offset;

		if(go) {
			currentIndex = newIndex;
		}
		//System.out.println("history current " + currentIndex + " / " + events.size());
		return events.get(newIndex);
	}

	/**
	 * Get the next page from history.
	 */
	public HistoryEvent getNext(boolean go) {
		if(indexOfLastEvent() <= currentIndex) {
			return null;
		}
		return getRelative(1, go);
	}

	/**
	 * Get the previous page from history.
	 */
	public HistoryEvent getPrevious(boolean go) {
		if(currentIndex <= 0) {
			return null;
		}
		return getRelative(-1, go);
	}

	/**
	 * Return the current page.
	 */
	public HistoryEvent getCurrent() {
		if(currentIndex == -1) {
			return null;
		}
		return events.get(currentIndex);
	}
}
