package VASSAL.counters;

import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.command.NullCommand;
import VASSAL.configure.BooleanConfigurer;
import VASSAL.configure.HotKeyConfigurer;
import VASSAL.configure.IntConfigurer;
import VASSAL.configure.StringConfigurer;
import VASSAL.tools.FormattedString;
import VASSAL.tools.SequenceEncoder;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;
import java.io.File;
import java.net.MalformedURLException;

/*
 * $Id$
 *
 * Copyright (c) 2003 by Rodney Kinney
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

/**
 * Adds a menu item that applies a {@link GlobalCommand} to other pieces
 */
public class CounterGlobalKeyCommand extends Decorator implements EditablePiece {
  public static final String ID = "globalkey;";
  protected KeyCommand[] command;
  protected String commandName;
  protected KeyStroke key;
  protected KeyStroke globalKey;
  protected GlobalCommand globalCommand = new GlobalCommand();
  protected String propertiesFilter;
  protected boolean restrictRange;
  protected int range;

  public CounterGlobalKeyCommand() {
    this(ID, null);
  }

  public CounterGlobalKeyCommand(String type, GamePiece inner) {
    mySetType(type);
    setInner(inner);
  }

  public void mySetType(String type) {
    type = type.substring(ID.length());
    SequenceEncoder.Decoder st = new SequenceEncoder.Decoder(type, ';');
    commandName = st.nextToken("Global Command");
    key = st.nextKeyStroke('G');
    globalKey = st.nextKeyStroke('K');
    propertiesFilter = st.nextToken("");
    restrictRange = st.nextBoolean(false);
    range = st.nextInt(1);
    globalCommand.setReportSingle(st.nextBoolean(true));
    globalCommand.setKeyStroke(globalKey);
    command = null;
  }

  public String myGetType() {
    SequenceEncoder se = new SequenceEncoder(';');
    se.append(commandName)
        .append(key)
        .append(globalKey)
        .append(propertiesFilter)
        .append(restrictRange)
        .append(range)
        .append(globalCommand.isReportSingle());
    return ID + se.getValue();
  }

  protected KeyCommand[] myGetKeyCommands() {
    if (command == null) {
      if (commandName.length() > 0 && key != null) {
        command = new KeyCommand[]{new KeyCommand(commandName, key, Decorator.getOutermost(this))};
      }
      else {
        command = new KeyCommand[0];
      }
    }
    if (command.length > 0) {
      command[0].setEnabled(restrictRange || getMap() != null);
    }
    return command;
  }

  public String myGetState() {
    return "";
  }

  public Command myKeyEvent(KeyStroke stroke) {
    Command c = null;
    myGetKeyCommands();
    if (command[0].matches(stroke)) {
      apply();
    }
    return c;
  }

  public void mySetState(String newState) {
  }

  public Rectangle boundingBox() {
    return piece.boundingBox();
  }

  public void draw(Graphics g, int x, int y, Component obs, double zoom) {
    piece.draw(g, x, y, obs, zoom);
  }

  public String getName() {
    return piece.getName();
  }

  public Shape getShape() {
    return piece.getShape();
  }

  public PieceEditor getEditor() {
    return new Ed(this);
  }

  public String getDescription() {
    return "Global Key Command";
  }

  public HelpFile getHelpFile() {
    File dir = VASSAL.build.module.Documentation.getDocumentationBaseDir();
    dir = new File(dir, "ReferenceManual");
    try {
      return new HelpFile(null, new File(dir, "GlobalKeyCommand.htm"));
    }
    catch (MalformedURLException ex) {
      return null;
    }
  }

  public void apply() {
    PieceFilter filter = PropertiesPieceFilter.parse(new FormattedString(propertiesFilter).getText(Decorator.getOutermost(this)));
    Command c = new NullCommand();
    if (restrictRange) {
      filter = new BooleanAndPieceFilter(filter,new RangeFilter(getMap(), getPosition(),range));
    }
    for (Enumeration e = GameModule.getGameModule().getComponents(Map.class); e.hasMoreElements();) {
      Map m = (Map) e.nextElement();
      c = c.append(globalCommand.apply(m, filter));
    }
    GameModule.getGameModule().sendAndLog(c);
  }

  public static class Ed implements PieceEditor {
    protected StringConfigurer nameInput;
    protected HotKeyConfigurer keyInput;
    protected HotKeyConfigurer globalKey;
    protected StringConfigurer propertyMatch;
    protected BooleanConfigurer suppress;
    protected BooleanConfigurer restrictRange;
    protected IntConfigurer range;
    protected JPanel controls;

    public Ed(CounterGlobalKeyCommand p) {
      controls = new JPanel();
      controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));

      nameInput = new StringConfigurer(null, "Command name:  ", p.commandName);
      controls.add(nameInput.getControls());

      keyInput = new HotKeyConfigurer(null, "Keyboard Command:  ", p.key);
      controls.add(keyInput.getControls());

      globalKey = new HotKeyConfigurer(null, "Global Key Command:  ", p.globalKey);
      controls.add(globalKey.getControls());

      propertyMatch = new StringConfigurer(null, "Matching Properties:  ", p.propertiesFilter);
      controls.add(propertyMatch.getControls());

      restrictRange = new BooleanConfigurer(null, "Restrict Range", p.restrictRange);
      controls.add(restrictRange.getControls());

      range = new IntConfigurer(null, "Range:  ", new Integer(p.range));
      controls.add(range.getControls());
      PropertyChangeListener l = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
          range.getControls().setVisible(Boolean.TRUE.equals(restrictRange.getValue()));
          Window w = SwingUtilities.getWindowAncestor(range.getControls());
          if (w != null) {
            w.pack();
          }
        }
      };
      restrictRange.addPropertyChangeListener(l);
      l.propertyChange(null);

      suppress = new BooleanConfigurer(null, "Suppress individual reports", p.globalCommand.isReportSingle());
      controls.add(suppress.getControls());
    }

    public Component getControls() {
      return controls;
    }

    public String getType() {
      SequenceEncoder se = new SequenceEncoder(';');
      se.append(nameInput.getValueString())
          .append((KeyStroke) keyInput.getValue())
          .append((KeyStroke) globalKey.getValue())
          .append(propertyMatch.getValueString())
          .append(restrictRange.getValueString())
          .append(range.getValueString())
          .append(suppress.booleanValue().booleanValue());
      return ID + se.getValue();
    }

    public String getState() {
      return "";
    }
  }

}
