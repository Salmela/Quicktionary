/* Quicktionary backend - Word translator app
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
package org.quicktionary.backend;

import org.quicktionary.backend.TextNode;
import org.quicktionary.backend.database.WordEntryIO;
import java.lang.ref.SoftReference;

/**
 * This class contains all information about particular word.
 */
public class WordEntry {
	private String word, source;
	private TextNode content;
	private WordEntryIO io;
	private boolean storable;

	private SoftReference<String> sourceWeak;
	private SoftReference<TextNode> contentWeak;

	public WordEntry(String word, String source, TextNode content, boolean storable) {
		this.word = word;
		this.source = source;
		this.content = content;
		this.storable = storable;

		this.sourceWeak = null;
		this.contentWeak = null;

		/**/
		if(source != null) {
			this.sourceWeak = new SoftReference<String>(source);
		}
		if(content != null) {
			this.contentWeak = new SoftReference<TextNode>(content);
		}
		/**/
	}
	public WordEntry(String word, String source, TextNode content) {
		this(word, source, content, true);
	}

	public WordEntry(String word) {
		this(word, null, null);
	}

	//public void addSource(String source) {
	//	this.source = source;
	//}
	//public void setContent(TextNode content) {
	//	this.content = content;
	//}
	public void addSource(String source) {
		this.source = source;
		if(source != null) {
			sourceWeak = new SoftReference<String>(source);
		}
	}

	public void setContent(TextNode content) {
		this.content = content;
		if(content != null) {
			contentWeak = new SoftReference<TextNode>(content);
		}
	}

	public String getWord() {
		return word;
	}

	//public TextNode getContent() {
	//	return content;
	//}
	//public String getSource() {
	//	return source;
	//}
	//public boolean isLoaded() {
	//	return content != null;
	//}
	public TextNode getContent() {
		if(content != null) return content;
		return (contentWeak != null) ? contentWeak.get() : null;
	}

	public String getSource() {
		if(source != null) return source;
		return (sourceWeak != null) ? sourceWeak.get() : null;
	}

	public boolean isLoaded() {
		if(contentWeak != null) {
			TextNode weak = contentWeak.get();
			if(weak != null) return true;
		}
		return content != null;
	}

	public boolean isStorable() {
		return storable;
	}

	/**
	 * This must only be called from WordEntryIO.
	 */
	public void setModified(boolean modified) {
		if(!modified) {
			/* remove hard references */
			source = null;
			content = null;
		}
	}

	public void setIO(WordEntryIO io) {
		this.io = io;
	}

	public WordEntryIO getIO() {
		return io;
	}
}
