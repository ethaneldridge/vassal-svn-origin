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
 * User: rkinney
 * Date: Jul 14, 2002
 * Time: 4:25:21 PM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package VASSAL.counters;

import VASSAL.build.Configurable;
import VASSAL.build.GameModule;
import VASSAL.build.module.BasicCommandEncoder;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.widget.CardSlot;
import VASSAL.build.widget.PieceSlot;
import VASSAL.command.AddPiece;
import VASSAL.command.Command;
import VASSAL.configure.*;
import VASSAL.tools.ComponentPathBuilder;
import VASSAL.tools.SequenceEncoder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.File;
import java.net.MalformedURLException;

/**
 * This Decorator defines a key command to places another counter on top of this one.
 */
public class PlaceMarker extends Decorator implements EditablePiece {
  public static final String ID = "placemark;";
  protected KeyCommand command;
  protected KeyStroke key;
  protected String markerSpec;
  protected String markerText = "";
  protected int xOffset=0;
  protected int yOffset=0;
  protected boolean matchRotation=false;

  public PlaceMarker() {
    this(ID + "Place Marker;M;null;null", null);
  }

  public PlaceMarker(String type, GamePiece inner) {
    mySetType(type);
    setInner(inner);
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

  protected KeyCommand[] myGetKeyCommands() {
    command.setEnabled(getMap() != null
                       && markerSpec != null);
    return new KeyCommand[]{command};
  }

  public String myGetState() {
    return "";
  }

  public String myGetType() {
    SequenceEncoder se = new SequenceEncoder(';');
    se.append(command.getName());
    se.append(key);
    se.append(markerSpec == null ? "null" : markerSpec);
    se.append(markerText == null ? "null" : markerText);
    se.append(xOffset).append(yOffset);
    se.append(matchRotation);
    return ID + se.getValue();
  }

  public Command myKeyEvent(KeyStroke stroke) {
    myGetKeyCommands();
    if (command.matches(stroke)) {
      GamePiece marker = createMarker();
      Command c = null;
      if (marker != null) {
        GamePiece outer = getOutermost(this);
        Point p = getPosition();
        p.translate(xOffset,-yOffset);
        if (matchRotation) {
          FreeRotator myRotation = (FreeRotator) Decorator.getDecorator(outer,FreeRotator.class);
          FreeRotator markerRotation = (FreeRotator) Decorator.getDecorator(marker,FreeRotator.class);
          if (myRotation != null
            && markerRotation != null) {
            markerRotation.setAngle(myRotation.getAngle());
            Point2D myPosition = getPosition().getLocation();
            Point2D markerPosition = p.getLocation();
            markerPosition = AffineTransform.getRotateInstance(myRotation.getAngleInRadians(),myPosition.getX(), myPosition.getY()).transform(markerPosition,null);
            p = new Point((int)markerPosition.getX(),(int)markerPosition.getY());
          }
        }
        if (!Boolean.TRUE.equals(marker.getProperty(Properties.IGNORE_GRID))) {
          p = getMap().snapTo(p);
        }
        c = getMap().placeOrMerge(marker,p);
        KeyBuffer.getBuffer().remove(outer);
        KeyBuffer.getBuffer().add(marker);
        if (markerText != null && getMap() != null) {
          if (!Boolean.TRUE.equals(outer.getProperty(Properties.OBSCURED_TO_OTHERS))
              && !Boolean.TRUE.equals(outer.getProperty(Properties.OBSCURED_TO_ME))
              && !Boolean.TRUE.equals(outer.getProperty(Properties.INVISIBLE_TO_OTHERS))) {
            String location = getMap().locationName(getPosition());
            if (location != null) {
              Command display = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), " * " + location + ":  " + outer.getName() + " " + markerText + " * ");
              display.execute();
              c = c == null ? display : c.append(display);
            }
          }
        }
      }
      return c;
    }
    else {
      return null;
    }
  }

  protected GamePiece createMarker() {
    if (markerSpec == null) {
      return null;
    }
    GamePiece piece = null;
    if (markerSpec.startsWith(BasicCommandEncoder.ADD)) {
      AddPiece comm = (AddPiece) GameModule.getGameModule().decode(markerSpec);
      piece = comm.getTarget();
    }
    else {
      try {
        Configurable[] c = ComponentPathBuilder.getInstance().getPath(markerSpec);
        if (c[c.length - 1] instanceof PieceSlot) {
          piece = PieceCloner.getInstance().clonePiece(((PieceSlot) c[c.length - 1]).getPiece());
        }
      }
      catch (ComponentPathBuilder.PathFormatException e) {
      }
    }
    if (piece == null) {
      piece = new BasicPiece();
    }
    return piece;
  }

  public void mySetState(String newState) {
  }

  public Shape getShape() {
    return piece.getShape();
  }

  public String getDescription() {
    return "Place Marker";
  }

  public HelpFile getHelpFile() {
    File dir = VASSAL.build.module.Documentation.getDocumentationBaseDir();
    dir = new File(dir, "ReferenceManual");
    try {
      return new HelpFile(null, new File(dir, "Marker.htm"));
    }
    catch (MalformedURLException ex) {
      return null;
    }
  }

  public void mySetType(String type) {
    SequenceEncoder.Decoder st = new SequenceEncoder.Decoder(type, ';');
    st.nextToken();
    String name = st.nextToken();
    key = st.nextKeyStroke(null);
    command = new KeyCommand(name, key, this);
    markerSpec = st.nextToken();
    if ("null".equals(markerSpec)) {
      markerSpec = null;
    }
    markerText = st.nextToken("null");
    if ("null".equals(markerText)) {
      markerText = null;
    }
    xOffset = st.nextInt(0);
    yOffset = st.nextInt(0);
    matchRotation = st.nextBoolean(false);
  }

  public PieceEditor getEditor() {
    return new Ed(this);
  }

  protected static class Ed implements PieceEditor {
    private HotKeyConfigurer keyInput;
    private StringConfigurer commandInput;
    private PieceSlot pieceInput;
    private JPanel p = new JPanel();
    private String markerSlotPath;
    protected JButton defineButton = new JButton("Define Marker");
    protected JButton selectButton = new JButton("Select");
    protected IntConfigurer xOffsetConfig = new IntConfigurer(null,"Horizontal offset");
    protected IntConfigurer yOffsetConfig = new IntConfigurer(null,"Vertical offset");
    protected BooleanConfigurer matchRotationConfig;

    protected Ed(PlaceMarker piece) {
      matchRotationConfig = createMatchRotationConfig();
      keyInput = new HotKeyConfigurer(null,"Keyboard Command:  ",piece.key);
      commandInput = new StringConfigurer(null, "Command: ", piece.command.getName());
      GamePiece marker = piece.createMarker();
      pieceInput = new PieceSlot(marker);

      markerSlotPath = piece.markerSpec;

      p = new JPanel();
      p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
      p.add(commandInput.getControls());
      p.add(keyInput.getControls());
      Box b = Box.createHorizontalBox();
      b.add(pieceInput.getComponent());
      defineButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          markerSlotPath = null;
          new ConfigurerWindow(pieceInput.getConfigurer()).setVisible(true);
        }
      });
      b.add(defineButton);
      selectButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          ChoosePieceDialog d = new ChoosePieceDialog((Frame) SwingUtilities.getAncestorOfClass(Frame.class, p), PieceSlot.class);
          d.setVisible(true);
          if (d.getTarget() instanceof PieceSlot) {
            pieceInput.setPiece(((PieceSlot) d.getTarget()).getPiece());
          }
          if (d.getPath() != null) {
            markerSlotPath = ComponentPathBuilder.getInstance().getId(d.getPath());
          }
          else {
            markerSlotPath = null;
          }
        }
      });
      b.add(selectButton);
      p.add(b);
      xOffsetConfig.setValue(new Integer(piece.xOffset));
      p.add(xOffsetConfig.getControls());
      yOffsetConfig.setValue(new Integer(piece.yOffset));
      p.add(yOffsetConfig.getControls());
      matchRotationConfig.setValue(new Boolean(piece.matchRotation));
      p.add(matchRotationConfig.getControls());
    }

    protected BooleanConfigurer createMatchRotationConfig() {
      return new BooleanConfigurer(null,"Match Rotation");
    }

    public Component getControls() {
      return p;
    }

    public String getState() {
      return "";
    }

    public String getType() {
      SequenceEncoder se = new SequenceEncoder(';');
      se.append(commandInput.getValueString());
      se.append((KeyStroke)keyInput.getValue());
      if (pieceInput.getPiece() == null) {
        se.append("null");
      }
      else if (markerSlotPath != null) {
        se.append(markerSlotPath);
      }
      else {
        String spec = GameModule.getGameModule().encode(new AddPiece(pieceInput.getPiece()));
        se.append(spec);
      }
      se.append("null"); // Older versions specified a text message to echo.  Now performed by the ReportState trait, but we remain backward-compatible.
      se.append(xOffsetConfig.getValueString());
      se.append(yOffsetConfig.getValueString());
      se.append(matchRotationConfig.getValueString());
      return ID + se.getValue();
    }

    public static class ChoosePieceDialog extends ChooseComponentPathDialog {
      public ChoosePieceDialog(Frame owner, Class targetClass) {
        super(owner, targetClass);
      }

      protected boolean isValidTarget(Object selected) {
        return super.isValidTarget(selected) || CardSlot.class.isInstance(selected);
      }
    }
  }
}
