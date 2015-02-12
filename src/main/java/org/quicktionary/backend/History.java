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
public class History {
	private ArrayList<Event> events;
	private int currentIndex;

	private class Event{
		public String event;
		public String data;
	}

	public History() {
		events = new ArrayList<Event>();
		currentIndex = -1;
	}

	private int indexOfLastEvent() {
		return events.size() - 1;
	}

	/**
	 * Store a event to the history.
	 */
	public void saveEvent(String event, String data) {
		Event e = new Event();

		/* truncate the list if we aren't at the end */
		if(indexOfLastEvent() > currentIndex) {
			events.subList(currentIndex + 1, events.size()).clear();
			currentIndex = events.size() - 1;
		}

		/* append the new event */
		e.event = event;
		e.data = data;
		events.add(e);
		currentIndex++;
	}

	private Object getRelative(int offset, boolean go) {
		int newIndex;

		newIndex = currentIndex + offset;

		if(go) {
			currentIndex = newIndex;
		}
		return events.get(newIndex);
	}

	/**
	 * Get the next page from history.
	 */
	public Object getNext(boolean go) {
		if(indexOfLastEvent() <= currentIndex) {
			return null;
		}
		return getRelative(1, go);
	}

	/**
	 * Get the previous page from history.
	 */
	public Object getPrevious(boolean go) {
		int index;
		if(currentIndex <= 0) {
			return null;
		}
		return getRelative(-1, go);
	}

	/**
	 * Return the current page.
	 */
	public Object getCurrent() {
		if(currentIndex == -1) {
			return null;
		}
		return events.get(currentIndex);
	}
}
