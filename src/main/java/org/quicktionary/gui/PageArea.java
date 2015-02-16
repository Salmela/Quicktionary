/* Quicktionary gui - Word translator app
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
package org.quicktionary.gui;

import java.lang.StringBuilder;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JTextArea;
import javax.swing.JEditorPane;
import static org.quicktionary.backend.parsers.WikiMarkup.TextFragment;

public class PageArea extends JPanel {
	private JEditorPane pane;
	private JTextArea area;

	public PageArea() {
		super(new BorderLayout());

		if(Main.useHTML) {
			pane = new JEditorPane();
			pane.setContentType("text/html");
			pane.setEditable(false);
			add(pane, BorderLayout.CENTER);
		} else {
			area = new JTextArea();
			area.setLineWrap(true);
			add(area, BorderLayout.CENTER);
		}
	}

	public void setPage(TextFragment root) {
		StringBuilder source;
		source = new StringBuilder();

		if(Main.useHTML) {
			generateHTML(root, source);
			pane.setText(source.toString());
		} else {
			generateMarkdown(root, source);
			area.setText(source.toString());
		}
	}

	private String getStartTag(TextFragment fragment) {
		switch(fragment.getType()) {
		case TextFragment.ROOT_TYPE:
			return "<html><body>";
		case TextFragment.HEADER_TYPE:
			return "<h1>";
		case TextFragment.PARAGRAPH_TYPE:
			return "<p>";
		case TextFragment.STRONG_TYPE:
			return "<strong>";
		case TextFragment.EM_TYPE:
			return "<em>";
		default:
			return "";
		}
	}

	private String getEndTag(TextFragment fragment) {
		switch(fragment.getType()) {
		case TextFragment.ROOT_TYPE:
			return "</body></html>";
		case TextFragment.HEADER_TYPE:
			return "</h1>";
		case TextFragment.PARAGRAPH_TYPE:
			return "</p>";
		case TextFragment.STRONG_TYPE:
			return "</strong>";
		case TextFragment.EM_TYPE:
			return "</em>";
		default:
			return "";
		}
	}

	private void generateHTML(TextFragment root, StringBuilder src) {
		src.append(getStartTag(root));
		if(root.getContent() != null) {
			src.append(root.getContent());
		} else {
			for(TextFragment child : root.getChildren()) {
				generateHTML(child, src);
			}
		}
		src.append(getEndTag(root));
	}

	private String generateSubMarkdown(TextFragment fragment) {
		String markdown, content;

		content = markdown = "";
		if(fragment.getContent() != null) {
			content += fragment.getContent();
		} else {
			for(TextFragment child : fragment.getChildren()) {
				content += generateSubMarkdown(child);
			}
		}

		switch(fragment.getType()) {
		case TextFragment.STRONG_TYPE:
			markdown = "**" + content + "**";
			break;
		case TextFragment.EM_TYPE:
			markdown = "*" + content + "*";
			break;
		case TextFragment.LINK_TYPE:
			markdown = "[" + content + "](" + content + ")";
			break;
		default:
			markdown = content;
		}

		return markdown;
	}

	private void generateMarkdown(TextFragment root, StringBuilder src) {
		for(TextFragment child : root.getChildren()) {
			String md = generateSubMarkdown(child);

			if(child.getType() == TextFragment.HEADER_TYPE) {
				src.append(md + "\n");
				for(char a : md.toCharArray()) {
					src.append("=");
				}
				src.append("\n");
			} else if(child.getType() == TextFragment.PARAGRAPH_TYPE) {
				src.append("\n" + md + "\n");
			} else {
				src.append(md);
			}
		}
	}
}
