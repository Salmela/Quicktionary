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

import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Area;

import java.lang.UnsupportedOperationException;

/**
 *  Creates border with adjustable rounding per corner.
 */
public class RoundedBorder extends AbstractBorder {
	static final long serialVersionUID = 1L;

	static final int CORNER_TOP_LEFT = 0;
	static final int CORNER_TOP_RIGHT = 1;
	static final int CORNER_BOTTOM_RIGHT = 2;
	static final int CORNER_BOTTOM_LEFT = 3;

	static final int BORDER_TOP = 0;
	static final int BORDER_RIGHT = 1;
	static final int BORDER_BOTTOM = 2;
	static final int BORDER_LEFT = 3;

	private Color color;
	private int[] radii;
	private boolean[] thickness;

	public RoundedBorder(Color color) {
		this.color     = color;
		this.radii     = new int[4];
		this.thickness = new boolean[4];
	}

	private void checkRadii() {
		boolean throwException = false;
		if(radii[CORNER_TOP_LEFT] != 0) {
			if(thickness[BORDER_TOP] != thickness[BORDER_LEFT]) {
				throwException = true;
			}
		} else if(radii[CORNER_TOP_RIGHT] != 0) {
			if(thickness[BORDER_TOP] != thickness[BORDER_RIGHT]) {
				throwException = true;
			}
		} else if(radii[CORNER_BOTTOM_RIGHT] != 0) {
			if(thickness[BORDER_BOTTOM] != thickness[BORDER_RIGHT]) {
				throwException = true;
			}
		} else if(radii[CORNER_BOTTOM_LEFT] != 0) {
			if(thickness[BORDER_BOTTOM] != thickness[BORDER_LEFT]) {
				throwException = true;
			}
		}

		if(!throwException) {
			/* verify that the radii are zeroes when connecting edges have zero thickness */
			if(thickness[BORDER_TOP] == false) {
				radii[CORNER_TOP_LEFT] = 0;
				radii[CORNER_TOP_RIGHT] = 0;

			} else if(thickness[BORDER_BOTTOM] == false) {
				radii[CORNER_BOTTOM_RIGHT] = 0;
				radii[CORNER_BOTTOM_LEFT] = 0;
			} 
			return;
		}
		throw new UnsupportedOperationException("The radii must be zero where the thickness changes");
	}

	public void setRadii(int topLeft, int topRight, int bottomRight, int bottomLeft) {
		radii[CORNER_TOP_LEFT]     = topLeft;
		radii[CORNER_TOP_RIGHT]    = topRight;
		radii[CORNER_BOTTOM_RIGHT] = bottomRight;
		radii[CORNER_BOTTOM_LEFT]  = bottomLeft;
		checkRadii();
	}
	public void setThickness(boolean top, boolean right, boolean bottom, boolean left) {
		thickness[BORDER_TOP]    = top;
		thickness[BORDER_RIGHT]  = right;
		thickness[BORDER_BOTTOM] = bottom;
		thickness[BORDER_LEFT]   = left;
		checkRadii();
	}

	private void paintMasks(Component component, Graphics2D graphics2d,
		                    GeneralPath path) {
		Component parent;

		parent = component.getParent();

		/* Code inspired by Andrew Thompson's answer
		 * http://stackoverflow.com/questions/15025092/border-with-rounded-corners-transparency
		 */
		if(parent != null) {
			Color backgroundColor = parent.getBackground();
			Rectangle bounds = new Rectangle(0, 0, component.getWidth(), component.getHeight());
			Area borderRegion = new Area(bounds);

			/* Fill the corner area that is outside of the borders with
			 * color of parent widget's background.
			 */
			borderRegion.subtract(new Area(path));
			graphics2d.setClip(borderRegion);
			graphics2d.setColor(backgroundColor);
			graphics2d.fillRect(0, 0, bounds.width, bounds.height);
			graphics2d.setClip(null);
		}
		graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		                            RenderingHints.VALUE_ANTIALIAS_ON);

		graphics2d.setColor(color);
	    graphics2d.draw(path);
	}

	@Override
	public void paintBorder(Component component, Graphics graphics,
	                        int x, int y, int width, int height) {
		GeneralPath path;
		Graphics2D graphics2d = (Graphics2D) graphics;
		int radius;

		path = new GeneralPath();

		/* substract the line width*/
		width -= 1;
		height -= 1;

		/* draw the top-right corner */
		radius = radii[CORNER_TOP_LEFT];
		if(radius == 0) {
			path.moveTo(0, 0);
		} else {
			path.moveTo(0, radius);
			path.quadTo(0, 0, radius, 0);
		}

		/* draw the top border and the top-right corner */
		radius = radii[CORNER_TOP_RIGHT];
		if(radius == 0) {
			if(thickness[BORDER_TOP]) path.lineTo(width, 0);
			else path.moveTo(width, 0);
		} else  {
			path.lineTo(width - radius, 0);
			path.quadTo(width, 0, width, radius);
		}

		/* draw the right border and the bottom-right corner */
		radius = radii[CORNER_BOTTOM_RIGHT];
		if(radius == 0) {
			if(thickness[BORDER_RIGHT]) path.lineTo(width, height);
			else path.moveTo(width, height);
		} else {
			path.lineTo(width, height - radius);
			path.quadTo(width, height, width - radius, height);
		}

		/* draw the bottom border and the bottom-left corner */
		radius = radii[CORNER_BOTTOM_LEFT];
		if(radius == 0) {
			if(thickness[BORDER_BOTTOM]) path.lineTo(0, height);
			else path.moveTo(0, height);
		} else {
			path.lineTo(radius, height);
			path.quadTo(0, height, 0, height - radius);
		}

		/* draw the left border */
		radius = radii[CORNER_TOP_LEFT];
		if(radius == 0) {
			if(thickness[BORDER_LEFT]) path.lineTo(0, 0);
		} else {
			path.lineTo(0, radius);
		}

		paintMasks(component, graphics2d, path);
	}

	public Insets getBorderInsets(Component component) {
		Insets insets = new Insets(0, 0, 0, 0);

		if(thickness[BORDER_TOP])    insets.top = 1;
		if(thickness[BORDER_RIGHT])  insets.right = 1;
		if(thickness[BORDER_BOTTOM]) insets.bottom = 1;
		if(thickness[BORDER_LEFT])   insets.left = 1;

		return insets;
	}
}
