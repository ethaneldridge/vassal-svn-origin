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
package VASSAL.tools;

import VASSAL.configure.*;
import VASSAL.build.GameModule;

import javax.swing.*;
import java.awt.event.*;

/**
 * A JButton for placing into a VASSAL component's toolbar.
 * Handles configuration of a hotkey shortcut, maintains appropriate
 * tooltip text, etc.
 */
public class LaunchButton extends JButton {
  private String nameAtt;
  private String keyAtt;
  private String toolTipText;
  private KeyStrokeListener keyListener;
  private Configurer nameConfig, keyConfig;

  public LaunchButton(String name, String nameAttribute,
                      String hotkeyAttribute, ActionListener al) {
    super(name);
    nameAtt = nameAttribute;
    keyAtt = hotkeyAttribute;
    setAlignmentY(0.0F);
    keyListener = new KeyStrokeListener(al);
    GameModule.getGameModule().addKeyStrokeListener(keyListener);
    addActionListener(al);
  }

  public String getNameAttribute() {
    return nameAtt;
  }

  public String getHotkeyAttribute() {
    return keyAtt;
  }

  public String getAttributeValueString(String key) {
    if (key.equals(nameAtt)) {
      return getText();
    }
    else if (key.equals(keyAtt)) {
      return HotKeyConfigurer.encode(keyListener.getKeyStroke());
    }
    else {
      return null;
    }
  }

  public void setAttribute(String key, Object value) {
    if (key.equals(nameAtt)) {
      setText((String) value);
    }
    else if (key.equals(keyAtt)) {
      if (value instanceof String) {
        value = HotKeyConfigurer.decode((String) value);
      }
      keyListener.setKeyStroke((KeyStroke) value);
      setToolTipText(toolTipText);
    }
  }

  public void setToolTipText(String text) {
    toolTipText = text;
    if (keyListener.getKeyStroke() != null) {
      text =
        (text == null ? "" : text + " ")
        + "[" + HotKeyConfigurer.getString(keyListener.getKeyStroke()) + "]";
    }
    super.setToolTipText(text);
  }

  public Configurer getNameConfigurer() {
    if (nameConfig == null && nameAtt != null) {
      nameConfig = new StringConfigurer(nameAtt, "Button text", getText());
    }
    return nameConfig;
  }

  public Configurer getHotkeyConfigurer() {
    if (keyConfig == null && keyAtt != null) {
      keyConfig = new HotKeyConfigurer(keyAtt, "Hotkey", keyListener.getKeyStroke());
    }
    return keyConfig;
  }
}
