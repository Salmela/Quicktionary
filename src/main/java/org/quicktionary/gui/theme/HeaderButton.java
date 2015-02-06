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

import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;

/**
 * This is custom button component.
 * TODO: This should be removed or replaced with more elegant code.
 */
public class HeaderButton extends JButton implements ChangeListener {
	final static long serialVersionUID = 1L;
	private boolean pressed;

	public HeaderButton() {
		this("");
	}

	public HeaderButton(String text) {
		super(text);

		setForeground(StyleManager.getColor("header-color"));
		setContentAreaFilled(false);
		addChangeListener(this);

		pressed = false;
	}

	public void stateChanged(ChangeEvent event) {
		pressed = getModel().isPressed();
		repaint();
	}

	@Override
	protected void paintComponent(Graphics graphics) {
		Paint style;
		Graphics2D graphics2d;
		int width, height;

		width  = getWidth();
		height = getHeight();

		if(pressed) {
			style = (Paint)StyleManager.getStyle("header-button-active-bg");
		} else {
			style = (Paint)StyleManager.getStyle("header-button-bg");
		}

		/* use default rendering if theme isn't selected */
		if(style == null) {
			setContentAreaFilled(true);
			super.paintComponent(graphics);
			return;
		}

		graphics2d = (Graphics2D) graphics;
		StyleManager.setSource(graphics2d, style, width, height);
		graphics2d.fillRect(0, 0, width, height);

		super.paintComponent(graphics);
	}
}
