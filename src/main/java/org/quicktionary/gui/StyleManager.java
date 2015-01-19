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

import java.util.HashMap;
import java.awt.Color;

public class StyleManager {
	static HashMap<String, Color> colors;

	public StyleManager() {
		colors = new HashMap<String, Color>();
	}

	private void useDarkStyle() {
		colors.put("header-bg",            new Color(0x3c3b38));
		colors.put("header-button-top",    new Color(0x3e3d38));
		colors.put("header-button-bottom", new Color(0x3a3836));
		colors.put("header-button-border", new Color(0x313330));
		colors.put("header-color",         new Color(0xe1d8c9));
	}

	private void useLightStyle() {
		colors.put("header-bg",            new Color(0xf3f3f3));
		colors.put("header-button-top",    new Color(0xf6f6f6));
		colors.put("header-button-bottom", new Color(0xdcdcdc));
		colors.put("header-button-border", new Color(0xb2b6b2));
		colors.put("header-color",         new Color(0x303638));
	}

	public void changeStyle(String name) {
		if(name.equals("dark")) {
			useDarkStyle();
		} else if(name.equals("light")) {
			useLightStyle();
		} else {
			colors.clear();
		}
	}

	static Color getColor(String colorId) {
		return colors.get(colorId);
	}
}
