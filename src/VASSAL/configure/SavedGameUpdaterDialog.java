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

import VASSAL.build.module.documentation.HelpWindow;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.GameModule;
import VASSAL.tools.SavedGameUpdater;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.util.Properties;

public class SavedGameUpdaterDialog extends JDialog {
  private HelpWindow helpWindow;
  private DefaultListModel savedGamesModel;
  private SavedGameUpdater updater = new SavedGameUpdater();
  private Properties oldPieceInfo;
  private JFileChooser fc;
  private static final String VERSION_KEY = "moduleVerion";
  private static final String MODULE_NAME_KEY = "moduleName";
  private JButton updateButton;
  private JTextField versionField;

  public SavedGameUpdaterDialog(Frame owner, HelpWindow helpWindow) throws HeadlessException {
    super(owner, false);
    this.helpWindow = helpWindow;
    setTitle("Update Saved Games");
    initComponents();
    fc = new JFileChooser();
    fc.setCurrentDirectory(GameModule.getGameModule().getFileChooser().getCurrentDirectory());
  }

  private void initComponents() {
    getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
    Box versionBox = Box.createHorizontalBox();
    versionBox.add(new JLabel("Module version of saved games:  "));
    versionField = new JTextField(8);
    versionField.setEditable(false);
    versionField.setMaximumSize(new Dimension(versionField.getMaximumSize().width, versionField.getPreferredSize().height));
    versionBox.add(versionField);
    JButton importButton = new JButton("Import GamePiece info");
    importButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        importPieceInfo();
      }
    });
    versionBox.add(importButton);
    getContentPane().add(versionBox);
    JButton exportButton = new JButton("Export GamePiece info");
    exportButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        exportPieceInfo();
      }
    });
    Box importExportBox = Box.createHorizontalBox();
    importExportBox.add(importButton);
    importExportBox.add(exportButton);
    getContentPane().add(importExportBox);

    Box savedGamesBox = Box.createHorizontalBox();
    Box left = Box.createVerticalBox();
    left.add(new JLabel("Saved Games:"));
    JButton chooseGamesButton = new JButton("Choose");
    chooseGamesButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        chooseSavedGames();
      }
    });
    left.add(chooseGamesButton);
    savedGamesBox.add(left);
    savedGamesModel = new DefaultListModel();
    JList savedGamesList = new JList(savedGamesModel);
    savedGamesList.setVisibleRowCount(5);
    savedGamesList.setCellRenderer(new DefaultListCellRenderer() {
      public Component getListCellRendererComponent(
          JList list,
          Object value,
          int index,
          boolean isSelected,
          boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        setText(((File) value).getName());
        return this;
      }
    });
    savedGamesBox.add(new JScrollPane(savedGamesList));
    getContentPane().add(savedGamesBox);

    Box buttonsBox = Box.createHorizontalBox();
    updateButton = new JButton("Update games");
    updateButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateGames();
      }
    });
    updateButton.setEnabled(false);
    buttonsBox.add(updateButton);
    JButton helpButton = new JButton("Help");
    HelpFile hf = null;
    try {
      hf = new HelpFile(null, new File(new File(VASSAL.build.module.Documentation.getDocumentationBaseDir(), "ReferenceManual"), "SavedGameUpdater.htm"));
    }
    catch (MalformedURLException ex) {
    }
    helpButton.addActionListener(new ShowHelpAction(helpWindow, hf, null));
    buttonsBox.add(helpButton);
    JButton closeButton = new JButton("Close");
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dispose();
      }
    });
    buttonsBox.add(closeButton);
    getContentPane().add(buttonsBox);
    pack();
    setLocationRelativeTo(getOwner());
  }

  private void updateGames() {
    for (int i=0,n=savedGamesModel.size();i<n;++i) {
      try {
        updater.updateSavedGame(oldPieceInfo,(File)savedGamesModel.getElementAt(i));
      }
      catch (IOException e) {
        showErrorMessage(e,"Update failed","Unable to save file");
      }
    }
  }

  private void chooseSavedGames() {
    fc.setMultiSelectionEnabled(true);
    if (JFileChooser.CANCEL_OPTION != fc.showOpenDialog(this)) {
      File[] selectedFiles = fc.getSelectedFiles();
      if (selectedFiles != null) {
        savedGamesModel.clear();
        for (int i = 0; i < selectedFiles.length; i++) {
          savedGamesModel.addElement(selectedFiles[i]);
        }
      }
    }
  }

  private void exportPieceInfo() {
    fc.setMultiSelectionEnabled(false);
    if (JFileChooser.CANCEL_OPTION != fc.showOpenDialog(this)) {
      Properties p = updater.getPieceSlotsMap();
      p.put(MODULE_NAME_KEY, GameModule.getGameModule().getGameName());
      p.put(VERSION_KEY, GameModule.getGameModule().getGameVersion());
      try {
        p.store(new FileOutputStream(fc.getSelectedFile()),null);
      }
      catch (IOException e) {
        showErrorMessage(e,"Export failed","Unable to write info");
      }
    }
  }

  private void importPieceInfo() {
    fc.setMultiSelectionEnabled(false);
    if (JFileChooser.CANCEL_OPTION != fc.showOpenDialog(this)) {
      oldPieceInfo = new Properties();
      try {
        oldPieceInfo.load(new FileInputStream(fc.getSelectedFile()));
        String moduleVersion = oldPieceInfo.getProperty(VERSION_KEY);
        String moduleName = oldPieceInfo.getProperty(MODULE_NAME_KEY);
        if (!GameModule.getGameModule().getGameName().equals(moduleName)) {
          showErrorMessage(null,"Import failed","Imported info is from the wrong module:  "+moduleName);
          oldPieceInfo = null;
          versionField.setText(null);
        }
        else if (GameModule.getGameModule().getGameVersion().equals(moduleVersion)) {
          showErrorMessage(null,"Import failed","Imported info is from the current version, "+moduleVersion+".\nLoad the older version in the editor and export the GamePiece info,\nThen load this module again and import the older version's info");
          oldPieceInfo = null;
          versionField.setText(null);
        }
        else {
          versionField.setText(moduleVersion);
        }
      }
      catch (IOException e) {
        showErrorMessage(e,"Import failed","Unable to import info");
        oldPieceInfo = null;
      }
    }
    updateButton.setEnabled(oldPieceInfo != null);
  }

  private void showErrorMessage(Exception e, String title, String defaultMessage) {
    String msg = e == null ? null : e.getMessage();
    if (msg == null) {
      msg = defaultMessage;
    }
    JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);
  }
}
