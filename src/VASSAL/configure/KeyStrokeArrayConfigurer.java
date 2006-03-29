/*
 * $Id$
 *
 * Copyright (c) 2004 by Rodney Kinney
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

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import VASSAL.tools.SequenceEncoder;

/**
 * Configures an array of keystrokes
 */
public class KeyStrokeArrayConfigurer extends Configurer {
  private List configs = new ArrayList();
  private Box controls;

  public KeyStrokeArrayConfigurer(String key, String name) {
    super(key, name);
  }

  public KeyStrokeArrayConfigurer(String key, String name, KeyStroke[] val) {
    super(key, name, val);
  }

  public Component getControls() {
    if (controls == null) {
      controls = Box.createVerticalBox();
      Box b = Box.createHorizontalBox();
      controls.add(b);
      JLabel l = new JLabel(getName());
      b.add(l);
      JButton button = new JButton("Add");
      b.add(button);
      button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          addKey(null);
        }
      });
      KeyStroke[] keyStrokes = (KeyStroke[]) value;
      if (keyStrokes != null) {
        for (int i = 0; i < keyStrokes.length; i++) {
          addKey(keyStrokes[i]);
        }
      }
      addKey(null);
    }
    return controls;
  }

  private void addKey(KeyStroke keyStroke) {
    HotKeyConfigurer config = new HotKeyConfigurer(null, null, keyStroke);
    configs.add(config);
    controls.add(config.getControls());
    Window w = SwingUtilities.getWindowAncestor(controls);
    if (w != null) {
      w.pack();
    }
  }

  public String getValueString() {
    return encode(getKeyStrokes());
  }

  public void setValue(String s) {
    setValue(decode(s));
  }

  public void setValue(Object o) {
    super.setValue(o);
    if (controls != null) {
      KeyStroke[] keyStrokes = (KeyStroke[]) o;
      if (keyStrokes == null) {
        keyStrokes = new KeyStroke[0];
      }
      for (int i = 0; i < keyStrokes.length; ++i) {
        if (i > configs.size()) {
          addKey(keyStrokes[i]);
        }
        else {
          ((HotKeyConfigurer) configs.get(i)).setValue(keyStrokes[i]);
        }
      }
      for (int i = keyStrokes.length; i < configs.size(); ++i) {
        ((HotKeyConfigurer) configs.get(i)).setValue(null);
      }
    }
  }

  public KeyStroke[] getKeyStrokes() {
    List l = new ArrayList();
    for (Iterator it = configs.iterator(); it.hasNext();) {
      HotKeyConfigurer hotKeyConfigurer = (HotKeyConfigurer) it.next();
      Object value = hotKeyConfigurer.getValue();
      if (value != null) {
        l.add(value);
      }
    }
    return (KeyStroke[]) l.toArray(new KeyStroke[l.size()]);
  }

  public static KeyStroke[] decode(String s) {
    if (s == null) {
      return null;
    }
    List l = new ArrayList();
    SequenceEncoder.Decoder st = new SequenceEncoder.Decoder(s, ',');
    while (st.hasMoreTokens()) {
      l.add(HotKeyConfigurer.decode(st.nextToken()));
    }
    return (KeyStroke[]) l.toArray(new KeyStroke[l.size()]);
  }

  public static String encode(KeyStroke[] keys) {
    if (keys == null) {
      return null;
    }
    SequenceEncoder se = new SequenceEncoder(',');
    for (int i = 0; i < keys.length; i++) {
      KeyStroke key = keys[i];
      se.append(HotKeyConfigurer.encode(key));
    }
    return se.getValue() != null ? se.getValue() : "";
  }
}
