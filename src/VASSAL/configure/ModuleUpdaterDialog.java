/*
 * $Id$
 *
 * Copyright (c) 2005 by Rodney Kinney
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, copies are available
 * at http://www.opensource.org.
 */
package VASSAL.configure;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import VASSAL.build.GameModule;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.documentation.HelpWindow;
import VASSAL.tools.ZipUpdater;

public class ModuleUpdaterDialog extends JDialog {
  private HelpWindow helpWindow;

  public ModuleUpdaterDialog(Frame owner, HelpWindow w) throws HeadlessException {
    super(owner, false);
    this.helpWindow = w;
    setTitle("Module Updater");
    getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
    final FileConfigurer fileConfig = new FileConfigurer(null, "File containing older version:  ");
    getContentPane().add(fileConfig.getControls());
    Box b = Box.createHorizontalBox();
    final JButton saveButton = new JButton("Create Updater");
    saveButton.setEnabled(false);
    fileConfig.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        saveButton.setEnabled(fileConfig.getValue() != null);
      }
    });
    saveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        FileDialog fd = GameModule.getGameModule().getFileDialog();
        fd.setMode(FileDialog.SAVE);
        fd.setVisible(true);
        if (fd.getFile() != null) {
          File output = new File(fd.getDirectory(), fd.getFile());
          ZipUpdater updater = null;
          try {
            updater = new ZipUpdater((File) fileConfig.getValue());
            updater.createUpdater(new File(GameModule.getGameModule().getArchiveWriter().getArchive().getName()), output);
          }
          catch (IOException e1) {
            String msg = e1.getMessage();
            if (msg == null) {
              msg = "Unable to create updater.";
            }
            JOptionPane.showMessageDialog(ModuleUpdaterDialog.this, msg, "Error writing updater", JOptionPane.ERROR_MESSAGE);
          }
        }
      }
    });
    JButton cancelButton = new JButton("Close");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dispose();
      }
    });
    JButton helpButton = new JButton("Help");
    getContentPane().add(b);
    HelpFile hf = null;
    try {
      hf = new HelpFile(null, new File(new File(VASSAL.build.module.Documentation.getDocumentationBaseDir(), "ReferenceManual"), "ModuleUpdater.htm"));
    }
    catch (MalformedURLException ex) {
    }

    helpButton.addActionListener(new ShowHelpAction(helpWindow, hf, null));
    b.add(saveButton);
    b.add(helpButton);
    b.add(cancelButton);
    getContentPane().add(b);
    pack();
    setLocationRelativeTo(getOwner());
  }

}
