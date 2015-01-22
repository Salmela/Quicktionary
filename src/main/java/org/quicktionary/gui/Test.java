package org.quicktionary.gui;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import org.quicktionary.backend.parsers.XMLParser;

/*
 * this file is for debugging the test cases.
 */
public class Test {
	private XMLParser parser;
	private File xmlFile;
	
	public Test() throws IOException {
		xmlFile = new File("test.xml");
		xmlFile.createNewFile();
		parser = new XMLParser();
		
		emptyDocumentIsValid();
	}
	
	public void testFile(String file) throws IOException {
		PrintWriter writer = new PrintWriter(xmlFile, "UTF-8");
		writer.print(file);
		writer.close();
	}
	public void emptyDocumentIsValid() throws IOException {
		testFile("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><test><hello/></test>");
		parser.parseFile(xmlFile);
		parser.findElement("hello");
		parser.getNextSibling();
	}
}
