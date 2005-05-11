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
package VASSAL.counters;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.configure.StringConfigurer;
import VASSAL.tools.SequenceEncoder;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;

/**
 * A generic Decorator that retains in its state the value of a
 * property.  That is, if setProperty() is invoked with a key that's one of
 * getKeys(), the String value of that property will be reflected in
 * the myGetState() method.  */
public class Marker extends Decorator implements EditablePiece {
  public static final String ID = "mark;";

  protected String keys[];
  protected String values[];

  public Marker() {
    this(ID, null);
  }

  public Marker(String type, GamePiece p) {
    mySetType(type);
    setInner(p);
  }

  public String[] getKeys() {
    return keys;
  }

  public void mySetType(String s) {
    s = s.substring(ID.length());
    SequenceEncoder.Decoder st = new SequenceEncoder.Decoder(s, ',');
    ArrayList v = new ArrayList();
    while (st.hasMoreTokens()) {
      v.add(st.nextToken());
    }
    keys = (String[])v.toArray(new String[v.size()]);
    values = new String[keys.length];
    for (int i = 0; i < keys.length; ++i) {
      values[i] = "";
    }
  }

  public void draw(java.awt.Graphics g, int x, int y, java.awt.Component obs, double zoom) {
    piece.draw(g, x, y, obs, zoom);
  }

  public String getName() {
    return piece.getName();
  }

  public java.awt.Rectangle boundingBox() {
    return piece.boundingBox();
  }

  public Shape getShape() {
    return piece.getShape();
  }

  public Object getProperty(Object key) {
    for (int i = 0; i < keys.length; ++i) {
      if (keys[i].equals(key)) {
        return values[i];
      }
    }
    return super.getProperty(key);
  }

  public void setProperty(Object key, Object value) {
    for (int i = 0; i < keys.length; ++i) {
      if (keys[i].equals(key)) {
        values[i] = (String) value;
        return;
      }
    }
    super.setProperty(key, value);
  }

  public String myGetState() {
    SequenceEncoder se = new SequenceEncoder(',');
    for (int i = 0; i < values.length; ++i) {
      se.append(values[i]);
    }
    return se.getValue();
  }

  public void mySetState(String state) {
    SequenceEncoder.Decoder st = new SequenceEncoder.Decoder(state, ',');
    int i = 0;
    while (st.hasMoreTokens()) {
      values[i++] = st.nextToken();
    }
  }

  public String myGetType() {
    SequenceEncoder se = new SequenceEncoder(',');
    for (int i = 0; i < keys.length; ++i) {
      se.append(keys[i]);
    }
    return ID + se.getValue();
  }

  protected KeyCommand[] myGetKeyCommands() {
    return new KeyCommand[0];
  }

  public Command myKeyEvent(KeyStroke stroke) {
    return null;
  }

  public String getDescription() {
    if (keys != null && keys.length > 0 && values.length > 0) {
      return "Marker - "+keys[0]+" = "+values[0];
    }
    else
      return "Marker";
  }

  public VASSAL.build.module.documentation.HelpFile getHelpFile() {
    File dir = VASSAL.build.module.Documentation.getDocumentationBaseDir();
    dir = new File(dir,"ReferenceManual");
    try {
      return new HelpFile(null,new File(dir,"PropertyMarker.htm"));
    }
    catch (MalformedURLException ex) {
      return null;
    }
  }

  public PieceEditor getEditor() {
    return new Ed(this);
  }

  private static class Ed implements PieceEditor {
    private StringConfigurer propName;
    private StringConfigurer propValue;
    private JPanel panel;
    private Ed(Marker m) {
      panel = new JPanel();
      panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
      propName = new StringConfigurer(null,"Property name:  ",m.keys.length == 0 ? "" : m.keys[0]);
      propValue = new StringConfigurer(null,"Property value:  ",m.values.length == 0 ? "" : m.values[0]);
      panel.add(propName.getControls());
      panel.add(propValue.getControls());
    }

    public Component getControls() {
      return panel;
    }

    public String getState() {
      return propValue.getValueString();
    }

    public String getType() {
      return Marker.ID+propName.getValueString();
    }
  }
}
