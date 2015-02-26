/* Quicktionary gui - Word translator app
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

import java.awt.event.ActionEvent;

import org.quicktionary.backend.WordEntry;

/**
 * The action event for loading page.
 */
public class PageLoadEvent extends ActionEvent {
	final static long serialVersionUID = 1L;
	final static String PAGE_LOAD_EVENT = "page-load-event";

	private WordEntry entry;
	private String name;

	public PageLoadEvent(Object source, WordEntry entry) {
		super(source, ActionEvent.ACTION_PERFORMED, PAGE_LOAD_EVENT);
		this.entry = entry;
		this.name = null;
	}

	public PageLoadEvent(Object source, String name) {
		super(source, ActionEvent.ACTION_PERFORMED, PAGE_LOAD_EVENT);
		this.name = name;
		this.entry = null;
	}

	public WordEntry getWordEntry() {
		return entry;
	}

	/* ugly solution for links in PageArea */
	public String getName() {
		return name;
	}
}
