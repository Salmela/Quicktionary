package org.quicktionary.backend.parsers;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import static org.junit.Assert.*;

import org.quicktionary.backend.TextNode;

public class TextNodeTest {
	@Rule
	public ExpectedException exception = ExpectedException.none();

	public TextNodeTest() {
	}

	@Test
	public void checkGetType() {
		TextNode f;
		f = new TextNode(0, null);
		assertEquals(0, f.getType());
	}

	@Test
	public void checkSetContent() {
		TextNode f;
		f = new TextNode(0, null);
		f.setTextContent("hey");
		assertEquals("hey", f.getTextContent());
	}

	@Test
	public void appendChildToTextNode() {
		TextNode f;
		f = new TextNode(0, null);
		f.setTextContent("hey");

		exception.expect(Error.class);
		f.appendChild(new TextNode(0, null));
	}

	@Test
	public void setTextContentToAppend() {
		TextNode f;
		f = new TextNode(0, null);
		f.appendChild(new TextNode(0, null));

		exception.expect(Error.class);
		f.setTextContent("hey");
	}

	@Test
	public void checkIsNotEmptyWhenThereIsChild() {
		TextNode f;
		f = new TextNode(0, null);
		f.appendChild(new TextNode(0, null));

		assertFalse(f.isEmpty());
	}

	@Test
	public void checkIsNotEmptyWhenThereText() {
		TextNode f;
		f = new TextNode(0, null);
		f.setTextContent("");

		assertFalse(f.isEmpty());
	}

	@Test
	public void checkIsEmptyWhenThereText() {
		TextNode f;
		f = new TextNode(0, null);

		assertTrue(f.isEmpty());
	}

	@Test
	public void checkPrintout() {
		TextNode f, g;
		f = new TextNode(0, "test");
		g = new TextNode(1, null);
		f.appendChild(g);
		g.setTextContent("hello");

		assertEquals("  Node type: 0, parameter: test\n" +
				"    Node type: 1\n" +
				"      Content: \"hello\"\n", f.print(2));
	}

	@Test
	public void normalizeText() {
		TextNode f, g, t;
		f = new TextNode(0, null);
		f.appendChild(t = new TextNode(TextNode.PLAIN_TYPE, null));
		t.setTextContent("Hello ");
		f.appendChild(t = new TextNode(TextNode.PLAIN_TYPE, null));
		t.setTextContent("world");
		f.appendChild(t = new TextNode(TextNode.PLAIN_TYPE, null));
		t.setTextContent("!\n");

		g = new TextNode(0, null);
		g.appendChild(t = new TextNode(TextNode.PLAIN_TYPE, null));
		t.setTextContent("Hello world!\n");

		f.normalize();
		assertEquals(g, f);
	}

	@Test
	public void checkFragmentEquals() {
		TextNode f;
		f = new TextNode(0, null);
		assertEquals(new TextNode(0, null), f);
	}

	@Test
	public void checkFragmentEqualsToNull() {
		TextNode f;
		f = null;
		assertFalse(new TextNode(0, null).equals(f));
	}

	@Test
	public void checkFragmentEqualsToObject() {
		Object f;
		f = new Object();
		assertFalse(new TextNode(0, null).equals(f));
	}

	@Test
	public void checkFragmentNotEquals() {
		TextNode f;
		f = new TextNode(0, null);
		assertFalse(new TextNode(1, null).equals(f));
	}

	@Test
	public void checkFragmentWithTextEquals() {
		TextNode f, g;
		f = new TextNode(0, null);
		f.setTextContent("hello");
		g = new TextNode(0, null);
		g.setTextContent("hello");
		assertEquals(g, f);
	}

	@Test
	public void checkFragmentWithTextNotEquals() {
		TextNode f, g;
		f = new TextNode(0, null);
		f.setTextContent("hello");
		g = new TextNode(0, null);
		g.setTextContent("hey");
		assertFalse(g.equals(f));
	}

	@Test
	public void checkFragmentWithParameterEquals() {
		TextNode f, g;
		f = new TextNode(0, "hello");
		g = new TextNode(0, "hello");
		assertEquals(g, f);
	}

	@Test
	public void checkFragmentWithParameterNotEquals() {
		TextNode f, g;
		f = new TextNode(0, "hello");
		g = new TextNode(0, "hey");
		assertFalse(g.equals(f));
	}

	@Test
	public void checkFragmentWithAndWithoutParameter() {
		TextNode f, g;
		f = new TextNode(0, "hello");
		g = new TextNode(0, null);
		assertFalse(g.equals(f));
	}

	@Test
	public void checkFragmentsWithDifferentAmmounOfChilds() {
		TextNode f, g;
		f = new TextNode(0, null);
		f.appendChild(new TextNode(1, null));
		f.appendChild(new TextNode(1, null));
		g = new TextNode(0, null);
		g.appendChild(new TextNode(1, null));
		assertFalse(g.equals(f));
	}

	@Test
	public void checkFragmentWithChildEquals() {
		TextNode f, g;
		f = new TextNode(0, null);
		f.appendChild(new TextNode(1, null));
		g = new TextNode(0, null);
		g.appendChild(new TextNode(1, null));
		assertEquals(g, f);
	}

	@Test
	public void checkFragmentWithChildNotEquals() {
		TextNode f, g;
		f = new TextNode(0, null);
		f.appendChild(new TextNode(1, null));
		g = new TextNode(0, null);
		g.appendChild(new TextNode(5, null));
		assertFalse(g.equals(f));
	}

	@Test
	public void checkFragmentWithSameChild() {
		TextNode f, g, c;
		c = new TextNode(1, null);
		f = new TextNode(0, null);
		f.appendChild(c);
		g = new TextNode(0, null);
		g.appendChild(c);

		assertSame(g.getChildren().get(0), f.getChildren().get(0));
	}
}
