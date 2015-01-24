package org.quicktionary.gui;

import java.io.ByteArrayInputStream;
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
		String file = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><test><hello/></test>";
		try {
			parser.parseFile(new ByteArrayInputStream(file.getBytes("UTF-8")));
		} catch(Exception e) {
		}
		parser.findElement("hello");
		parser.getNextSibling();
	}
}
