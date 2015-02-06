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
package org.quicktionary.gui.theme;

import java.util.HashMap;
import java.util.ArrayList;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Paint;
import java.awt.GradientPaint;
import java.awt.geom.Point2D;

import java.lang.IllegalStateException;

/**
 * This class provides an easy way to color the custom widgets.
 */
public class StyleManager {
	static HashMap<String, Object> styles;
	static ArrayList<StyleListener> listeners;

	public StyleManager() {
		styles = new HashMap<String, Object>();
		listeners = new ArrayList<StyleListener>();
	}

	private void useDarkStyle() {
		GradientPaint button;
		button = new GradientPaint(0, 0, new Color(0x3e3d38),
		                           0, 1, new Color(0x3a3836), true);
		styles.put("header-bg",            new Color(0x3c3b38));
		styles.put("header-button-bg",     button);
		styles.put("header-button-active-bg", new Color(0x363632));
		styles.put("header-button-border", new Color(0x313330));
		styles.put("header-button-inline", true);
		styles.put("header-button-inline-color", new Color(0x4b4a46));
		styles.put("header-color",         new Color(0xe1d8c9));
	}

	private void useLightStyle() {
		GradientPaint button, buttonActive;
		button = new GradientPaint(0, 0, new Color(0xf6f6f6),
		                           0, 1, new Color(0xdcdcdc), true);
		buttonActive = new GradientPaint(0, 0, new Color(0xd6d6d6),
		                                 0, 1, new Color(0xe0e0e0), true);

		styles.put("header-bg",            new Color(0xededed));
		styles.put("header-button-bg",     button);
		styles.put("header-button-active-bg", buttonActive);
		styles.put("header-button-border", new Color(0xb2b6b2));
		styles.put("header-button-inline", false);
		styles.put("header-color",         new Color(0x303638));
	}

	private void refreshGui() {
		for(StyleListener listener : listeners) {
			listener.styleChanged();
		}
	}

	public void changeStyle(String name) {
		/* use default theme if name is null */
		if(name == null) {
			styles.clear();

		} else if(name.equals("dark")) {
			useDarkStyle();

		} else if(name.equals("light")) {
			useLightStyle();

		} else {
			styles.clear();
		}

		refreshGui();
	}

	public static void setSource(Graphics2D graphics2d,
	                             Paint source,
	                             int width, int height) {
		/* use default paint if a paint is not selected */
		if(source == null) {
			return;
		}
		if(source instanceof GradientPaint) {
			Point2D.Float p1, p2;
			GradientPaint gradient = (GradientPaint)source;

			/* stretch the gradient to the size of the component */
			p1 = (Point2D.Float)gradient.getPoint1();
			p2 = (Point2D.Float)gradient.getPoint2();
			p1.x *= width;
			p1.y *= height;
			p2.x *= width;
			p2.y *= height;
			source = new GradientPaint(p1, gradient.getColor1(),
			                           p2, gradient.getColor2());
		}
		graphics2d.setPaint(source);
	}

	public static Object getStyle(String styleId) {
		return styles.get(styleId);
	}

	public static Color getColor(String colorId) {
		Object color;
		color = getStyle(colorId);
		/* use default color if a color is not selected */
		if(color == null) {
			return null;
		}
		if(!(color instanceof Color)) {
			throw new IllegalStateException("Style must have Color type.");
		}
		return (Color)color;
	}

	public static void setStyleListener(StyleListener listener) {
		listeners.add(listener);
	}

	public interface StyleListener {
		void styleChanged();
	}
}
