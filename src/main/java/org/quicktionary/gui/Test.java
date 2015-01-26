package org.quicktionary.gui;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import org.quicktionary.backend.parsers.XMLParser;

/*
 * this file is for debugging the test cases.
 */
public class Test {
	private XMLParser parser;
	
	public Test() {
		parser = new XMLParser();

		emptyDocumentIsValid();
	}
	
	public void emptyDocumentIsValid() {
		String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><test><cool xml:space=\"preserve\"> foo </cool></test>";
		try {
			parser.parseFile(new StringReader(s));
		} catch(Exception e) {}
		parser.findElement("cool");
		parser.getAttribute("xml:space");
	}
}
