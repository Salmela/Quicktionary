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
package org.quicktionary.gui;

import org.quicktionary.backend.HistoryEvent;
import org.quicktionary.backend.WordEntry;

class PageLoadHistoryEvent implements HistoryEvent {
	private WordEntry entry;

	public PageLoadHistoryEvent(WordEntry entry) {
		this.entry = entry;
	}

	protected WordEntry getWordEntry() {
		return entry;
	}

	public String getEventType() {
		return "page-load";
	}

	public String toString() {
		return "Page " + entry.getWord();
	}

	public boolean truncateSimilar() {
		return false;
	}
}
