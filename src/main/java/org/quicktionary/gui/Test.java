package org.quicktionary.gui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import org.quicktionary.backend.parsers.XMLParser;
import org.quicktionary.backend.parsers.WikiMarkup;

/*
 * this file is for debugging the test cases.
 */
public class Test {
	private XMLParser  parserXML;
	private WikiMarkup parserWiki;

	String xmlDecl = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";

	public Test() {
		parserXML = new XMLParser();
		parserWiki = new WikiMarkup();

		xmlParserTest();

		wikiParserTest();
		wikiParserTest2();
		wikiParserTest3();
		wikiParserTest4();
		wikiParserTest5();
	}

	public static void main(String[] args) {
		new Test();
	}

	public void parseString(String xmlText) {
		try {
			parserXML.parseFile(new StringReader(xmlText));
		} catch(Exception e) {
		}
	}

	public void parse(String wikiText) {
		System.out.println("\nWiki test: " + wikiText);
		try {
			parserWiki.parse(new StringReader(wikiText));
		} catch(Exception e) {
		}
	}

	public void xmlParserTest() {
		parseString(xmlDecl + "<test><!-- this is comment --></test>");

		parserXML.findElement("");
		System.out.println(!parserXML.parsingErrorHappened());
	}

	public void wikiParserTest() {
		parse("== t'''est''' == \nhello");
	}
	public void wikiParserTest2() {
		parse("helloha{{lgh|uh'''gr}}eugh");
	}
	public void wikiParserTest3() {
		parse("hello[[ha{{lgh|uh]]gr}}eugh");
	}
	public void wikiParserTest4() {
		parse("hello{{hal|gh[[uh}}gr]]eugh");
	}
	public void wikiParserTest5() {
		parse("hello [uhgr]] eugh");
	}
}
