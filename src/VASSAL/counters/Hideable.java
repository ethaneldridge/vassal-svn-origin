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

import VASSAL.build.GameModule;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.command.ChangeTracker;
import VASSAL.configure.ColorConfigurer;
import VASSAL.tools.SequenceEncoder;
import VASSAL.Info;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.event.InputEvent;
import java.io.File;
import java.net.MalformedURLException;

public class Hideable extends Decorator implements EditablePiece {

  private static boolean allHidden;
  public static final String ID = "hide;";
  public static final String HIDDEN_BY = "hiddenBy";

  private String hiddenBy;
  private char hideKey;
  private String command = "Invisible";

  private Color bgColor;

  private KeyCommand[] commands;

  public void setProperty(Object key, Object val) {
    if (HIDDEN_BY.equals(key)) {
      hiddenBy = (String) val;
    }
    else {
      super.setProperty(key, val);
    }
  }

  public Object getProperty(Object key) {
    if (HIDDEN_BY.equals(key)) {
      return hiddenBy;
    }
    else if (Properties.INVISIBLE_TO_ME.equals(key)) {
      return new Boolean(invisibleToMe());
    }
    else if (Properties.INVISIBLE_TO_OTHERS.equals(key)) {
      return new Boolean(invisibleToOthers());
    }
    else {
      return super.getProperty(key);
    }
  }


  public Hideable() {
    this(ID + "I", null);
  }

  public Hideable(String type, GamePiece p) {
    setInner(p);
    mySetType(type);
  }

  public void mySetType(String type) {
    SequenceEncoder.Decoder st = new SequenceEncoder.Decoder(type, ';');
    st.nextToken();
    hideKey = st.nextChar('I');
    if (st.hasMoreTokens()) {
      command = st.nextToken();
    }
    if (st.hasMoreTokens()) {
      bgColor = ColorConfigurer.stringToColor(st.nextToken());
    }
    else {
      bgColor = null;
    }
    commands = null;
  }

  public void mySetState(String in) {
    hiddenBy = "null".equals(in) ? null : in;
  }

  public String myGetType() {
    return command == null ?
        ID + hideKey
        : ID + hideKey + ';' + command
        + (bgColor == null ? ";" : ';' + ColorConfigurer.colorToString(bgColor));
  }

  public String myGetState() {
    return hiddenBy == null ? "null" : hiddenBy;
  }

  public boolean invisibleToMe() {
    return hiddenBy != null
        && (allHidden || !hiddenBy.equals(GameModule.getUserId()));
  }

  public boolean invisibleToOthers() {
    return hiddenBy != null
        && (allHidden || hiddenBy.equals(GameModule.getUserId()));
  }

  public Shape getShape() {
    if (invisibleToMe()) {
      return new Rectangle();
    }
    else {
      return piece.getShape();
    }
  }

  public Rectangle boundingBox() {
    if (invisibleToMe()) {
      return new Rectangle();
    }
    else {
      return piece.boundingBox();
    }
  }

  public void draw(Graphics g, int x, int y, Component obs, double zoom) {
    if (invisibleToMe()) {
      return;
    }
    else if (invisibleToOthers()) {
      if (bgColor != null) {
        if (Info.is2dEnabled()) {
          Graphics2D g2d = (Graphics2D) g;
          g.setColor(bgColor);
          AffineTransform t = AffineTransform.getScaleInstance(zoom, zoom);
          t.translate(x / zoom, y / zoom);
          g2d.fill(t.createTransformedShape(piece.getShape()));
        }
        else {
          Rectangle r = (Rectangle) piece.getShape();
          g.setColor(bgColor);
          g.fillRect(x + (int) (zoom * r.x),
                     y + (int) (zoom * r.y),
                     (int) (zoom * r.width),
                     (int) (zoom * r.height));
        }
      }
      if (g instanceof Graphics2D) {
        Graphics2D g2d = (Graphics2D) g;
        Composite oldComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3F));
        piece.draw(g, x, y, obs, zoom);
        g2d.setComposite(oldComposite);
      }
    }
    else {
      piece.draw(g, x, y, obs, zoom);
    }
  }

  public String getName() {
    if (invisibleToMe()) {
      return "";
    }
    else if (invisibleToOthers()) {
      return piece.getName() + "(" + command + ")";
    }
    else {
      return piece.getName();
    }
  }

  public KeyCommand[] myGetKeyCommands() {
    if (commands == null) {
      commands = new KeyCommand[1];
      commands[0] = new KeyCommand(command,
                                   KeyStroke.getKeyStroke(hideKey, InputEvent.CTRL_MASK),
                                   Decorator.getOutermost(this));
    }
    return commands;
  }

  public Command myKeyEvent(KeyStroke stroke) {
    if (KeyStroke.getKeyStroke(hideKey, InputEvent.CTRL_MASK) == stroke) {
      ChangeTracker tracker = new ChangeTracker(this);
      if (invisibleToOthers()) {
        hiddenBy = null;
      }
      else if (!invisibleToMe()) {
        hiddenBy = GameModule.getUserId();
      }
      return tracker.getChangeCommand();
    }
    return null;
  }

  public String getDescription() {
    return "Can be Invisible";
  }

  public HelpFile getHelpFile() {
    File dir = VASSAL.build.module.Documentation.getDocumentationBaseDir();
    dir = new File(dir, "ReferenceManual");
    try {
      return new HelpFile(null, new File(dir, "Hideable.htm"));
    }
    catch (MalformedURLException ex) {
      return null;
    }
  }

  public PieceEditor getEditor() {
    return new Ed(this);
  }

  /**
   * If true, then all hidden pieces are considered invisible to all players.
   * Used to temporarily draw pieces as they appear to other players
   * @param allHidden
   */
  public static void setAllHidden(boolean allHidden) {
    Hideable.allHidden = allHidden;
  }

  private static class Ed implements PieceEditor {
    private KeySpecifier hideKeyInput;
    private JTextField hideCommandInput;
    private ColorConfigurer colorConfig;
    private JPanel controls;

    public Ed(Hideable p) {
      controls = new JPanel();
      controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));

      Box b = Box.createHorizontalBox();
      hideKeyInput = new KeySpecifier('H');
      hideKeyInput.setKey(p.hideKey);
      b.add(new JLabel("Key command:  "));
      b.add(hideKeyInput);
      controls.add(b);

      b = Box.createHorizontalBox();
      hideCommandInput = new JTextField(16);
      hideCommandInput.setMaximumSize(hideCommandInput.getPreferredSize());
      hideCommandInput.setText(p.command);
      b.add(new JLabel("Menu Text:  "));
      b.add(hideCommandInput);
      controls.add(b);

      colorConfig = new ColorConfigurer(null, "Background color", p.bgColor);
      controls.add(colorConfig.getControls());
    }

    public String getState() {
      return "null";
    }

    public String getType() {
      return ID + hideKeyInput.getKey() + ";" + hideCommandInput.getText()
          + (colorConfig.getValue() == null ? "" : ";" + colorConfig.getValueString());
    }

    public Component getControls() {
      return controls;
    }
  }
}
