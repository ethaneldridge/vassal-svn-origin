/*
 * $Id$
 *
 * Copyright (c) 2000-2003 by Rodney Kinney
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

import VASSAL.build.GameModule;
import VASSAL.build.module.ModuleExtension;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Editing window for a module extension
 */
public class ExtensionEditWindow extends VASSAL.configure.ModuleEditWindow {
  private ModuleExtension extension;

  public ExtensionEditWindow(ModuleExtension extension) {
    this.extension = extension;
    initExtensionComponents();
  }

  protected void initComponents(Component view) {
  }

  private void initExtensionComponents() {
    super.initComponents(new JScrollPane(new VASSAL.configure.ExtensionTree(GameModule.getGameModule(),helpWindow, extension)));
    toolbar.addSeparator();
    toolbar.add(extension.getEditAction(this));
  }

  protected void refreshTitle() {
    if (extension != null) {
      setTitle("Edit "+extension.getName());
    }
    else {
      setTitle("Edit Extension");
    }
  }

  protected void save() {
    try {
      extension.save();
      refreshTitle();
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog(this,e.getMessage(),"Save Failed",JOptionPane.ERROR_MESSAGE);
    }
  }

  protected void saveAs() {
    try {
      extension.saveAs();
      refreshTitle();
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog(this,e.getMessage(),"Save Failed",JOptionPane.ERROR_MESSAGE);
    }
  }
}
