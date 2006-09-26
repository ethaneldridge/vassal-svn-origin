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
import VASSAL.build.module.documentation.HelpFile;

import javax.swing.*;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.lang.reflect.Method;

/**
 * Configurer for specifying an AudioClip
 */
public class SoundConfigurer extends Configurer {
  public static final String DEFAULT = "default";
  private String defaultResource;
  private String clipName;
  private JPanel controls;
  private JTextField textField;
  private Method clipFactory;

  public SoundConfigurer(String key, String name, String defaultResource) {
    super(key, name);
    this.defaultResource = defaultResource;
    try {
      clipFactory = Applet.class.getMethod("newAudioClip",new Class[]{URL.class});
    }
    catch (NoSuchMethodException e) {
    }
    catch (SecurityException e) {
    }
    setValue(DEFAULT);
  }

  public Component getControls() {
    if (controls == null) {
      if (clipFactory == null) {
        controls = new JPanel();
        controls.add(new JLabel("Sound not supported on this platform"));
      }
      else {
      controls = new JPanel();
      controls.setLayout(new BoxLayout(controls,BoxLayout.X_AXIS));
      controls.add(new JLabel(name));
      JButton b = new JButton("Play");
      b.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          play();
        }
      });
      controls.add(b);
      b = new JButton("Default");
      b.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          setValue(DEFAULT);
        }
      });
      controls.add(b);
      b = new JButton("Select");
      b.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          chooseClip();
        }
      });
      controls.add(b);
      textField = new JTextField();
      textField.setMaximumSize(new Dimension(textField.getMaximumSize().width,textField.getPreferredSize().height));
      textField.setEditable(false);
      textField.setText(DEFAULT.equals(clipName) ? defaultResource : clipName);
      controls.add(textField);
      }
    }
    return controls;
  }

  public String getValueString() {
    String s = null;
    if (clipName != null) {
      s = clipName;
    }
    return s;
  }

  public void setValue(String s) {
    if (clipFactory == null) {
      return;
    }
    URL url = null;
    if (DEFAULT.equals(s)) {
      url = getClass().getResource("/images/"+defaultResource);
      clipName = s;
      if (textField != null) {
        textField.setText(defaultResource);
      }
    }
    else if (s != null) {
      try {
        url = HelpFile.toURL(new File(s));
        clipName = s;
      }
      catch (IOException e) {
        e.printStackTrace();
        clipName = null;
      }
      if (textField != null) {
        textField.setText(clipName);
      }
    }
    if (url != null) {
      try {
        setValue(clipFactory.invoke(null, new Object[]{url}));
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
    else {
      if (textField != null) {
        textField.setText(null);
      }
      setValue((Object) null);
    }
  }

  public void play() {
    AudioClip clip = (AudioClip) getValue();
    if (clip != null) {
      clip.play();
    }
  }

  public void chooseClip() {
    JFileChooser fc = GameModule.getGameModule().getFileChooser();
    fc.showOpenDialog(GameModule.getGameModule().getFrame());
    File f = fc.getSelectedFile();
    if (f == null) {
      setValue((String) null);
    }
    else {
      setValue(f.getName());
    }
  }
}
