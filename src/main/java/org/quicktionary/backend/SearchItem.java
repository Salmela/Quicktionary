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

/**
 * Container for all search item related data.
 * SearchItem objects are created in Searcher class and
 * send to the gui.
 */
public class SearchItem {
	private String word, description;
	private WordEntry entry;

	public SearchItem(String word, String description, WordEntry entry) {
		this.word = word;
		this.description = description;
		this.entry = entry;
	}

	public String getWord() {
		return word;
	}

	public String getDescription() {
		return description;
	}

	public WordEntry getWordEntry() {
		return entry;
	}
}
