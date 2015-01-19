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

import javax.swing.JOptionPane;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.BoxLayout;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ReadDatabaseDialog extends JOptionPane implements ActionListener {
	static final long serialVersionUID = 1L;

	private String filename;
	private JDialog dialog;
	private JTextField filenameField;

	public ReadDatabaseDialog(Object message,
	                          int messageType, int optionType,
	                          Object[] options, Object initialValue) {
		super(message, messageType, optionType, null, options, initialValue);

		this.filename = new String();
	}

	public static ReadDatabaseDialog createDialog(JComponent parent) {
		JTextField filenameField;
		JPanel fileSelectorBox;
		JButton fileSelectorButton;
		ReadDatabaseDialog pane;
		JDialog dialog;
		String descString;

		descString = "Select the wikimedia database dump that you want to use.";

		fileSelectorBox = new JPanel();
		fileSelectorBox.setLayout(new BoxLayout(fileSelectorBox, BoxLayout.X_AXIS));

		/* create the file chooser */
		filenameField      = new JTextField(20);
		fileSelectorButton = new JButton("Choose file");
		fileSelectorBox.add(filenameField);
		fileSelectorBox.add(fileSelectorButton);

		Object[] components = {descString, fileSelectorBox};
		Object[] buttons = {"Parse", "Cancel"};

		pane = new ReadDatabaseDialog(components, JOptionPane.PLAIN_MESSAGE,
		                              JOptionPane.OK_CANCEL_OPTION,
		                              buttons, buttons[0]);
		dialog = pane.createDialog(parent, "Read a database");

		pane.dialog = dialog;
		pane.filenameField = filenameField;
		fileSelectorButton.addActionListener(pane);

		/* the following call will wait until the dialog is closed */
		dialog.setVisible(true);

		return pane;
	}

	public void actionPerformed(ActionEvent event) {
		FileDialog dialog;

		dialog = new FileDialog(this.dialog, "Select the database file", FileDialog.LOAD);

		/* the following call will wait until the filedialog is closed */
		dialog.setVisible(true);
		filename = dialog.getDirectory() + dialog.getFile();
		filenameField.setText(filename);
	}

	public String getFilename() {
		return filename;
	}
}
