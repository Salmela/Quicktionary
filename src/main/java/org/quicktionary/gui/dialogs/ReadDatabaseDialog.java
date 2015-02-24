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

import java.io.File;

import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Container;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.quicktionary.backend.Configs;

/**
 * This dialog asks the filename of the wiki database dump.
 * TODO: Give some feedback to user if the file is not valid.
 */
public class ReadDatabaseDialog extends JDialog implements ActionListener {
	static final long serialVersionUID = 1L;

	static final String FILE_CHOOSER_EVENT = "file-chooser-button";
	static final String PARSE_EVENT  = "parse-button";
	static final String CANCEL_EVENT = "cancel-button";

	private String filename;
	private JTextField filenameField;

	public ReadDatabaseDialog(Frame frame, ActionListener listener) {
		super(frame, "Read a database", true);
		this.filename = new String();

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		makeComponents();
	}

	public static ReadDatabaseDialog createDialog(Component owner, ActionListener listener) {
		ReadDatabaseDialog dialog;
		JFrame frame;

		frame = (JFrame)SwingUtilities.windowForComponent(owner);
		dialog = new ReadDatabaseDialog(frame, listener);

		return dialog;
	}

	public void makeComponents() {
		JPanel  fileSelectorBox;
		JPanel  buttonBox;
		JButton fileSelectorButton;
		JButton button;
		String  descString;
		JLabel  output;

		descString = "Select the wikimedia database dump that you want to use.";

		/* create file chooser box */
		fileSelectorBox = new JPanel();
		fileSelectorBox.setLayout(new BoxLayout(fileSelectorBox, BoxLayout.X_AXIS));

		/* create the file chooser */
		filenameField      = new JTextField(20);
		fileSelectorButton = new JButton("Choose file");
		fileSelectorBox.add(filenameField);
		fileSelectorBox.add(fileSelectorButton);

		output = new JLabel("");

		/* create footer buttons */
		buttonBox = new JPanel();

		button = new JButton("Parse");
		button.addActionListener(this);
		button.setActionCommand(PARSE_EVENT);
		buttonBox.add(button);

		button = new JButton("Cancel");
		button.addActionListener(this);
		button.setActionCommand(CANCEL_EVENT);
		buttonBox.add(button);

		Container pane;
		pane = getContentPane();
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		pane.add(new JLabel(descString));
		pane.add(fileSelectorBox);
		pane.add(output);
		pane.add(buttonBox);

		fileSelectorButton.addActionListener(this);
		fileSelectorButton.setActionCommand(FILE_CHOOSER_EVENT);

		/* the following call will wait until the dialog is closed */
		//setSize(pane.getMinimumSize());
		pack();
		setVisible(true);
	}

	public void openFileChooser() {
		String titleStr = "Select the database file";

		if(Configs.getOptionBoolean("useNativeFileDialog")) {
			FileDialog dialog;

			dialog = new FileDialog(this, titleStr, FileDialog.LOAD);

			/* the following call will wait until the filedialog is closed */
			dialog.setVisible(true);
			if(dialog.getDirectory() != null && dialog.getFile() != null) {
				filename = dialog.getDirectory() + dialog.getFile();
			}

		} else {
			JFileChooser chooser;
			int returnValue;

			chooser = new JFileChooser();
			chooser.setDialogTitle(titleStr);

			/* the following call will wait until the filedialog is closed */
			returnValue = chooser.showOpenDialog(this);
			if(returnValue == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile().getName() != null) {
				filename = chooser.getSelectedFile().getPath();
			}
		}
		filenameField.setText(filename);
	}

	public void actionPerformed(ActionEvent event) {
		if(event.getActionCommand() == FILE_CHOOSER_EVENT) {
			openFileChooser();
		} else if(event.getActionCommand() == PARSE_EVENT) {
			filename = filenameField.getText();
			setVisible(false);
		} else if(event.getActionCommand() == CANCEL_EVENT) {
			filename = null;
			setVisible(false);
		}
	}

	public String getFilename() {
		return filename;
	}
}
