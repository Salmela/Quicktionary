/* Quicktionary test
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
package org.quicktionary.backend.parsers;

import org.junit.Test;
import org.junit.Before;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.Timeout;

import java.io.StringReader;

import org.quicktionary.backend.parsers.WikiMarkup;

public class WikiMarkupTest {
	private WikiMarkup parser;

	@Rule
	public Timeout globalTimeout = new Timeout(100);

	public WikiMarkupTest() {
		parser = new WikiMarkup();
	}

	private parse(String markupString) {
		parser.parse(new StringReader(markupString));
	}

	@test
	public emptyDocument() {
		Asser.assertFalse(parse(""));
	}

	@test
	public justText() {
		Asser.assertTrue(parse("hello\nhe"));
	}

	@test
	public headerAndText() {
		Asser.assertTrue(parse("==hello==\nhe"));
	}
}
