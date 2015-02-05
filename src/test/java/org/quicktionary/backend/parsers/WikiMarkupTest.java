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

import java.io.IOException;
import org.junit.Test;
import org.junit.Before;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.Timeout;

import java.io.StringReader;
import org.junit.Assume;

import org.quicktionary.backend.parsers.WikiMarkup;

public class WikiMarkupTest {
	private WikiMarkup parser;

	@Rule
	public Timeout globalTimeout = new Timeout(100);

	public WikiMarkupTest() {
		parser = new WikiMarkup();
	}

	private boolean parse(String markupString) {
		try {
			return parser.parse(new StringReader(markupString));
		} catch(IOException e) {
			Assume.assumeTrue(false);
		}
		return false;
	}

	@Test
	public void emptyDocument() {
		Assert.assertFalse(parse(""));
	}

	@Test
	public void justText() {
		Assert.assertTrue(parse("hello\nhe"));
	}

	@Test
	public void headerAndText() {
		parse("==hello==\nhe");
	}

	@Test
	public void headerAndTextWithSpaces() {
		parse("== hello ==\nhe");
	}

	@Test
	public void headerWithOneEqualSignAtStart() {
		parse("=== hello ==\nhe");
	}

	@Test
	public void headerWithOneEqualSignAtEnd() {
		parse("== hello ===\nhe");
	}

	@Test
	public void headerWithNonEndingTextStyleMarkup() {
		parse("== he'''llo ==\nhe");
	}

	@Test
	public void linkAndTemplateMerged() {
		parse("te[[st {{smallcaps| thin]]g}}.");
	}

	@Test
	public void linkWithNonEndingBold() {
		parse("[[li'''nk]].");
	}

	@Test
	public void linkWithBold() {
		parse("he'''llo, [[link]] this.");
	}
}
