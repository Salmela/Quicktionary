package org.quicktionary.backend;

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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class HttpFetcherTest {
}
/*
public class HttpFetcherTest implements FetchListener {
	private HttpFetcher fetcher;
	private String title, text;
	final String DEFAULT_URL = "www.example.com";
	final String DEFAULT_RESPONSE = "{\"warnings\":{\"query\":{\"*\":\"Are you sure what you are doing?\"}},\"query\":{\"pages\":{\"117985\":{\"pageid\":117985,\"ns\":0,\"title\":\"Car\",\"revisions\":[{\"contentformat\":\"text/x-wiki\",\"contentmodel\":\"wikitext\",\"*\":\"Hello\"}]}}}}";

	public HttpFetcherTest() {
		fetcher = new HttpFetcher();
		setFetchListener(this);
	}

	public pageFetched(String title, String text) {
		assertEquals("Incorrect title provided", title, this.title);
		assertEquals("Incorrect text data returned", text, this.text);
	}

	private fetchPage(String title, String domain, String wantedText, String receivedText) throws Exception {
		this.title = title;
		this.wantedText = wantedText;

		fetcher.testingPage(receivedText);
		fetcher.fetchPage(title, domain);
	}

	@Test(expected = Exception.ThatDoesntExistYet)
	public void failIfTitleIsEmpty() {
		fetchPage("", DEFAULT_URL, null, DEFAULT_RESPONSE);
	}

	@Test(expected = Exception.ThatDoesntExistYet2)
	public void failIfResponceIsEmpty() {
		fetchPage("Car", DEFAULT_URL, null, "");
	}

	@Test
	public void successIfThePageDataIsReceived() {
		fetchPage("Car", DEFAULT_URL, "Hello", DEFAULT_RESPONSE);
	}
}
*/