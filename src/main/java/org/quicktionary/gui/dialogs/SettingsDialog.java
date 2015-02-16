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
package org.quicktionary.gui.dialogs;

import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.BoxLayout;
import javax.swing.SwingUtilities;
import java.awt.Frame;
import java.awt.Container;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;

/**
 * Dialog contains list of tabs that enables the user to
 * change every aspect of the program.
 */
public class SettingsDialog extends JDialog implements ActionListener {
	static final long serialVersionUID = 1L;

	static final String OK_BUTTON_EVENT = "ok-button";
	static final String APPLY_BUTTON_EVENT  = "apply-button";
	static final String CANCEL_BUTTON_EVENT = "cancel-button";

	private String filename;
	private JTextField filenameField;

	public SettingsDialog(Frame frame, ActionListener listener) {
		super(frame, "Settings", true);
		this.filename = new String();

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		makeComponents();
	}

	public static SettingsDialog createDialog(Component owner, ActionListener listener) {
		SettingsDialog dialog;
		JFrame frame;

		frame = (JFrame)SwingUtilities.windowForComponent(owner);
		dialog = new SettingsDialog(frame, listener);

		return dialog;
	}

	private void makeComponents() {
		JTabbedPane pane;
		JButton button;
		JPanel vbox, basicsPanel, buttonBox;

		vbox = new JPanel();
		vbox.setLayout(new BoxLayout(vbox, BoxLayout.Y_AXIS));
		add(vbox);

		pane = new JTabbedPane();
		vbox.add(pane);

		basicsPanel = createBasicsPanel();
		pane.addTab("Basics", null, basicsPanel, "Basic settings");

		buttonBox = new JPanel();
		buttonBox.setLayout(new BoxLayout(buttonBox, BoxLayout.X_AXIS));
		vbox.add(buttonBox);

		button = new JButton("Ok");
		button.addActionListener(this);
		button.setActionCommand(OK_BUTTON_EVENT);
		buttonBox.add(button);

		button = new JButton("Apply");
		button.addActionListener(this);
		button.setActionCommand(APPLY_BUTTON_EVENT);
		buttonBox.add(button);

		button = new JButton("Cancel");
		button.addActionListener(this);
		button.setActionCommand(CANCEL_BUTTON_EVENT);
		buttonBox.add(button);

		pack();
		setVisible(true);
	}

	private JPanel createBasicsPanel() {
		JPanel panel, themePanel;
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		themePanel = new JPanel();
		themePanel.setBorder(BorderFactory.createTitledBorder("Theme"));
		themePanel.add(new JLabel("Test"));
		panel.add(themePanel);

		themePanel = new JPanel();
		themePanel.setBorder(BorderFactory.createTitledBorder("Others"));
		themePanel.add(new JCheckBox("Advanced options"));
		panel.add(themePanel);

		return panel;
	}

	public void actionPerformed(ActionEvent event) {
		if(event.getActionCommand() == OK_BUTTON_EVENT) {
			dispose();
		} else if(event.getActionCommand() == APPLY_BUTTON_EVENT) {
		} else if(event.getActionCommand() == CANCEL_BUTTON_EVENT) {
			dispose();
		}
	}
}
