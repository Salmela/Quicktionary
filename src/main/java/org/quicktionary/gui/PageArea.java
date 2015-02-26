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
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.quicktionary.backend.TextNode;

import org.quicktionary.backend.Configs;

/**
 * This component renderes the current page.
 */
public class PageArea extends JPanel {
	public static final long serialVersionUID = 1;

	public static final String PAGE_CHANGE_EVENT = "page-change-event";
	private Application app;
	private JEditorPane pane;
	private JTextArea area;

	public PageArea(Application app) {
		super(new BorderLayout());
		this.app = app;

		if((Boolean)Configs.getOptionBoolean("gui.useHTML")) {
			HTMLEditorKit htmlEditor = new HTMLEditorKit();
			pane = new JEditorPane();
			pane.setEditorKit(htmlEditor);
			pane.setEditable(false);
			pane.addHyperlinkListener(new LinkListener());
			generateStyleSheet(htmlEditor);
			add(pane, BorderLayout.CENTER);
		} else {
			area = new JTextArea();
			area.setLineWrap(true);
			add(area, BorderLayout.CENTER);
		}
	}

	class LinkListener implements HyperlinkListener {
		public void hyperlinkUpdate(HyperlinkEvent event) {
			if(event.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
				return;
			}
			app.actionPerformed(new PageLoadEvent(this, event.getDescription()));
		}
	}

	private StyleSheet generateStyleSheet(HTMLEditorKit kit) {
		StyleSheet styleSheet = kit.getStyleSheet();
		styleSheet.addRule("body {font-family: Cantarell;}");
		styleSheet.addRule("h1, h2, h3, h4, h5, h6 {border-bottom: 1px solid #444444; padding-bottom: 5px}");
		return styleSheet;
	}

	public void setPage(TextNode root) {
		String source;

		if(root == null) {
			return;
		}

		if(Configs.getOptionBoolean("gui.useHTML")) {
			source = generateHTML(root);
			pane.setText(source);
		} else {
			source = generateMarkdown(root);
			area.setText(source);
		}
	}

	private String generateHTML(TextNode node) {
		StringBuilder content = new StringBuilder();

		if(node.getTextContent() != null) {
			content.append(node.getTextContent());
		} else {
			for(TextNode child : node.getChildren()) {
				content.append(generateHTML(child));
			}
		}
		switch(node.getType()) {
		case TextNode.ROOT_TYPE:
			return "<html><body>" + content + "</body></html>";
		case TextNode.HEADER_TYPE:
			String level = "6";
			if(node.getParameter() != null) {
				level = node.getParameter();
			}
			return "<h" + level + ">" + content + "</h" + level + ">";
		case TextNode.PARAGRAPH_TYPE:
			return "<p>" + content + "</p>";
		case TextNode.STRONG_TYPE:
			return "<strong>" + content + "</strong>";
		case TextNode.EM_TYPE:
			return "<em>" + content + "</em>";
		case TextNode.LINK_TYPE:
			return "<a href=\'" + content + "\'>" + content + "</a>";
		case TextNode.LIST_TYPE:
			return "<ul>" + content + "</ul>";
		case TextNode.LIST_ITEM_TYPE:
			return "<li>" + content + "</li>";
		case TextNode.PLAIN_TYPE:
			return content.toString();
		default:
			return "<u>" + content.toString() + "</u>";
		}
	}

	private String generateMarkdown(TextNode root) {
		StringBuilder src = new StringBuilder();

		for(TextNode child : root.getChildren()) {
			String md = generateSubMarkdown(child);

			if(child.getType() == TextNode.HEADER_TYPE) {
				src.append(md);
				src.append("\n");

				for(char a : md.toCharArray()) {
					src.append("=");
				}
				src.append("\n");
			} else if(child.getType() == TextNode.PARAGRAPH_TYPE) {
				src.append("\n");
				src.append(md);
				src.append("\n");
			} else {
				src.append(md);
			}
		}
		return src.toString();
	}

	private String generateSubMarkdown(TextNode node) {
		String markdown, content;

		content = markdown = "";
		if(node.getTextContent() != null) {
			content += node.getTextContent();
		} else {
			for(TextNode child : node.getChildren()) {
				content += generateSubMarkdown(child);
			}
		}

		switch(node.getType()) {
		case TextNode.STRONG_TYPE:
			markdown = "**" + content + "**";
			break;
		case TextNode.EM_TYPE:
			markdown = "*" + content + "*";
			break;
		case TextNode.LINK_TYPE:
			markdown = "[" + content + "](" + content + ")";
			break;
		default:
			markdown = content;
		}

		return markdown;
	}
}
