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

import VASSAL.build.Builder;
import VASSAL.build.Configurable;
import VASSAL.build.module.documentation.HelpWindow;
import VASSAL.build.module.documentation.HelpWindowExtension;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Window for editing properties of a {@link Configurable} object
 */
public class PropertiesWindow extends JDialog {
  private Configurer configurer;
  private Configurable target;
  private Element originalState;

  public PropertiesWindow(Frame owner, boolean modal, final Configurable target, HelpWindow helpWindow) {
    super(owner, modal);
    initialize(target, helpWindow);
  }

  protected void initialize(final Configurable target, HelpWindow helpWindow) {
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    this.target = target;
    originalState = target.getBuildElement(Builder.createNewDocument());
    Node child = originalState.getFirstChild();
    while (child != null) {
      Node nextChild = child.getNextSibling();
      if (Node.ELEMENT_NODE == child.getNodeType()) {
        originalState.removeChild(child);
      }
      child = nextChild;
    }
    getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
    configurer = target.getConfigurer();
    if (helpWindow != null
      && configurer instanceof HelpWindowExtension) {
      ((HelpWindowExtension)configurer).setBaseWindow(helpWindow);
    }
    target.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        if (Configurable.NAME_PROPERTY.equals(evt.getPropertyName())) {
          setTitle((String) evt.getNewValue());
        }
      }
    });
    getContentPane().add(configurer.getControls());
    setTitle(VASSAL.configure.ConfigureTree.getConfigureName(target));
    Box buttonBox = Box.createHorizontalBox();
    JButton okButton = new JButton("Ok");
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        save();
      }
    });
    buttonBox.add(okButton);
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancel();
      }
    });
    buttonBox.add(cancelButton);
    getContentPane().add(buttonBox);
    pack();
    setLocationRelativeTo(getParent());
  }

  public void cancel() {
    target.build(originalState);
    dispose();
  }

  public void save() {
    configurer.getValue();
    dispose();
  }
}
