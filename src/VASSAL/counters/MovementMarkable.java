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
/*
 * Created by IntelliJ IDEA.
 * User: unknown
 * Date: Dec 30, 2002
 * Time: 12:42:01 PM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package VASSAL.counters;

import VASSAL.build.GameModule;
import VASSAL.build.module.GlobalOptions;
import VASSAL.build.module.Map;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.configure.IconConfigurer;
import VASSAL.configure.IntConfigurer;
import VASSAL.tools.SequenceEncoder;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Enumeration;

/**
 * A GamePiece with this trait will automatically be marked whenever it is moved.  A marked piece is
 * indicated by drawing a specified image at a specified location
 */
public class MovementMarkable extends Decorator implements EditablePiece {
  public static final String ID = "markmoved;";

  private static final KeyStroke markStroke = KeyStroke.getKeyStroke('M', java.awt.event.InputEvent.CTRL_MASK);
  private int xOffset = 0;
  private int yOffset = 0;
  private IconConfigurer movedIcon = new IconConfigurer(null, "Marker Image", "/images/moved.gif");
  private boolean hasMoved = false;

  public MovementMarkable() {
    this(ID + "moved;0;0", null);
  }

  public MovementMarkable(String type, GamePiece p) {
    mySetType(type);
    setInner(p);
  }

  public boolean isMoved() {
    return hasMoved;
  }

  public void setMoved(boolean b) {
    hasMoved = b;
  }

  public void mySetType(String type) {
    SequenceEncoder.Decoder st = new SequenceEncoder.Decoder(type, ';');
    st.nextToken();
    movedIcon.setValue(st.nextToken());
    xOffset = st.nextInt(0);
    yOffset = st.nextInt(0);
  }

  public void mySetState(String newState) {
    hasMoved = "true".equals(newState);
  }

  public String myGetState() {
    return "" + hasMoved;
  }

  public String myGetType() {
    SequenceEncoder se = new SequenceEncoder(';');
    se.append(movedIcon.getValueString()).append("" + xOffset).append("" + yOffset);
    return ID + se.getValue();
  }

  protected KeyCommand[] myGetKeyCommands() {
    return new KeyCommand[]{new KeyCommand("Mark Moved", markStroke, Decorator.getOutermost(this))};
  }

  public Command myKeyEvent(javax.swing.KeyStroke stroke) {
    if (stroke.equals(markStroke)) {
      ChangeTracker c = new ChangeTracker(this);
      hasMoved = !hasMoved;
      return c.getChangeCommand();
    }
    else {
      return null;
    }
  }

  public Shape getShape() {
    return piece.getShape();
  }

  public Rectangle boundingBox() {
    Rectangle r = piece.boundingBox();
    Rectangle r2 = piece.getShape().getBounds();
    Dimension d = getImageSize();
    Rectangle r3 = new Rectangle(xOffset, yOffset, d.width, d.height);
    r2 = r2.union(r3);
    return r.union(r2);
  }

  public String getName() {
    return piece.getName();
  }

  public void draw(Graphics g, int x, int y, Component obs, double zoom) {
    piece.draw(g, x, y, obs, zoom);
    if (hasMoved
        && movedIcon.getIconValue() != null) {
      Graphics2D g2d = (Graphics2D) g;
      AffineTransform transform = g2d.getTransform();
      g2d.scale(zoom, zoom);
      movedIcon.getIconValue().paintIcon(obs, g,
                                         (int) Math.round(x / zoom) + xOffset,
                                         (int) Math.round(y / zoom) + yOffset);
      g2d.setTransform(transform);
    }
  }

  private Dimension getImageSize() {
    Icon icon = movedIcon.getIconValue();
    return icon != null ? new Dimension(icon.getIconWidth(), icon.getIconHeight()) : new Dimension();
  }

  public String getDescription() {
    return "Can be marked moved";
  }

  public VASSAL.build.module.documentation.HelpFile getHelpFile() {
    File dir = VASSAL.build.module.Documentation.getDocumentationBaseDir();
    dir = new File(dir, "ReferenceManual");
    try {
      return new HelpFile(null, new File(dir, "MarkMoved.htm"));
    }
    catch (MalformedURLException ex) {
      return null;
    }
  }

  public Object getProperty(Object key) {
    if (Properties.MOVED.equals(key)) {
      return new Boolean(isMoved());
    }
    else {
      return super.getProperty(key);
    }
  }

  public void setProperty(Object key, Object val) {
    if (Properties.MOVED.equals(key)) {
      setMoved(Boolean.TRUE.equals(val));
    }
    else {
      super.setProperty(key, val);
    }
  }

  public PieceEditor getEditor() {
    return new Ed(this);
  }

  private static class Ed implements PieceEditor {
    private IconConfigurer iconConfig;
    private IntConfigurer xOff;
    private IntConfigurer yOff;
    private Box box;

    private Ed(MovementMarkable p) {
      iconConfig = p.movedIcon;
      box = Box.createVerticalBox();
      box.add(iconConfig.getControls());
      xOff = new IntConfigurer(null, "Horizontal Offset:  ", new Integer(p.xOffset));
      yOff = new IntConfigurer(null, "Vertical Offset:  ", new Integer(p.yOffset));
      box.add(xOff.getControls());
      box.add(yOff.getControls());
    }

    public Component getControls() {
      boolean enabled = false;
      for (Enumeration e = GameModule.getGameModule().getComponents(Map.class); e.hasMoreElements();) {
        Map m = (Map) e.nextElement();
        String value = m.getAttributeValueString(Map.MARK_MOVED);
        enabled = enabled
            || GlobalOptions.ALWAYS.equals(value)
            || GlobalOptions.PROMPT.equals(value);
      }
      if (!enabled) {
        Runnable runnable = new Runnable() {
          public void run() {
            JOptionPane.showMessageDialog(box, "You must enable the \"Mark Pieces that Move\" option in one or more Map Windows", "Option not enabled", JOptionPane.WARNING_MESSAGE);
          }
        };
        SwingUtilities.invokeLater(runnable);
      }
      return box;
    }

    public String getType() {
      SequenceEncoder se = new SequenceEncoder(';');
      se.append(iconConfig.getValueString()).append(xOff.getValueString()).append(yOff.getValueString());
      return ID + se.getValue();
    }

    public String getState() {
      return "false";
    }
  }
}
