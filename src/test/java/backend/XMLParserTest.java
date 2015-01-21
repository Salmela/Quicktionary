package backend;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.quicktionary.backend.parsers.XMLParser;

/**
 *
 * @author alesalme
 */
public class XMLParserTest {
	private XMLParser parser;
	private File xmlFile;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	public XMLParserTest() {
		xmlFile = null;
	}

	@Before
	public void setup() throws IOException {
		xmlFile = folder.newFile("test.xml");
		parser = new XMLParser(xmlFile);
	}

	public void testFile(String file) throws IOException {
		PrintWriter writer = new PrintWriter(xmlFile, "UTF-8");
		writer.print(file);
		writer.close();
	}

	@Test
	public void emptyDocument() throws IOException {
		testFile("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		parser.parse();
	}

	@Test
	public void emptyWithRootDocument() throws IOException {
		testFile("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><test></test>");
		parser.parse();
	}

	@Test
	public void notEndingNodeDocument() throws IOException {
		testFile("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><test><hello></test>");
		parser.parse();
		/* should fail */
	}

	@Test
	public void emptyNodeDocument() throws IOException {
		testFile("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><test><hello/></test>");
		parser.parse();
	}

	@Test
	public void nodeWithTextDocument() throws IOException {
		testFile("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><test><hello>hei</hello></test>");
		parser.parse();
	}
}
