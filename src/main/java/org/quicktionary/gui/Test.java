package org.quicktionary.gui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import org.quicktionary.backend.parsers.XMLParser;

/*
 * this file is for debugging the test cases.
 */
public class Test {
	private XMLParser parser;

	String xmlDecl = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";

	public Test() {
		parser = new XMLParser();

		emptyDocumentIsValid();
	}

	public void parseString(String fileText) {
		try {
			parser.parseFile(new StringReader(fileText));
		} catch(Exception e) {
		}
	}

	public void emptyDocumentIsValid() {
		parseString(xmlDecl + "<test><!-- this is comment -></test>");

		parser.findElement("");
		System.out.println(!parser.parsingErrorHappened());
	}
}
