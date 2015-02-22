/* Quicktionary backend - The data structure for the word information
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
package org.quicktionary.backend.database;

import org.quicktionary.backend.WordEntry;

public class WordEntryIO {
	protected long address;
	protected boolean modified;
	WordEntry data;

	protected WordEntryIO(WordEntry entry, long address) {
		this.data = entry;
		this.address = address;

		entry.setIO(this);
	}

	protected boolean isModified() {
		return modified;
	}

	protected void setModified(boolean modified) {
		this.modified = modified;
	}
}
