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
package VASSAL.counters;

import VASSAL.build.module.PrototypeDefinition;
import VASSAL.build.module.PrototypesContainer;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.configure.StringConfigurer;

import javax.swing.*;
import java.awt.*;

/**
 * This trait is a placeholder for a pre-defined series of traits specified
 * in a {@link VASSAL.build.module.PrototypeDefinition} object.  When a piece
 * that uses a prototype is defined in a module, it is simply assigned the name
 * of a particular prototype definition.  When that piece is during a game,
 * the UsePrototype trait is substituted for the list of traits in the prototype
 * definition.  From that point on, the piece has no record that those traits
 * were defined in a prototype instead of assigned to piece directly.  This is
 * necessary so that subsequent changes to a prototype definition don't invalidate
 * games that were saved using previous versions of the module.
 *
 */
public class UsePrototype extends Decorator implements EditablePiece {
  public static final String ID = "prototype;";
  private String prototypeName;
  private String lastCachedPrototype;
  private GamePiece prototype;

  public UsePrototype() {
    this(ID, null);
  }

  public UsePrototype(String type, GamePiece inner) {
    mySetType(type);
    setInner(inner);
  }

  public String getDescription() {
    return prototypeName != null && prototypeName.length() > 0 ? "Prototype - "+prototypeName : "Prototype";
  }

  public HelpFile getHelpFile() {
    return null;
  }

  public void mySetType(String type) {
    prototypeName = type.substring(ID.length());
  }

/*
  protected KeyCommand[] myGetKeyCommands() {
    KeyCommand[] comm = new KeyCommand[0];
    buildPrototype();
    if (prototype != null) {
      for (GamePiece p = prototype; p instanceof Decorator && p != piece; p = ((Decorator)p).getInner()) {
        KeyCommand[] c = ((Decorator)p).myGetKeyCommands();
        KeyCommand[] newValue = new KeyCommand[comm.length+c.length];
        System.arraycopy(comm,0,newValue,0,comm.length);
        System.arraycopy(c,0,newValue,comm.length,c.length);
        comm = newValue;
      }
    }
    return comm;
  }
*/

  protected KeyCommand[] myGetKeyCommands() {
    return new KeyCommand[0];
  }

  protected KeyCommand[] getKeyCommands() {
    return (KeyCommand[]) getExpandedInner().getProperty(Properties.KEY_COMMANDS);
  }

  protected void buildPrototype() {
    PrototypeDefinition def = PrototypesContainer.getPrototype(prototypeName);
    if (def != null) {
      String type = def.getPiece().getType(); // Check to see if prototype definition has changed
      if (!type.equals(lastCachedPrototype)) {
        lastCachedPrototype = type;
        prototype = new PieceCloner().clonePiece(def.getPiece());
        ((Decorator)Decorator.getInnermost(prototype).getProperty(Properties.OUTER)).setInner(piece);
        prototype.setProperty(Properties.OUTER, this);
      }
    }
    else {
      prototype = null;
    }
  }

  /**
   * Build a new GamePiece instance based on the traits in the referenced {@link PrototypeDefinition}.
   * Substitute the new instance for {@link #getInner} and return it.
   * If the referenced definition does not exist, return the default inner piece.
   * @return the new instance
   */
  public GamePiece getExpandedInner() {
    buildPrototype();
    return prototype != null ? prototype : piece;
  }

  public String myGetState() {
    return "";
  }

  public String myGetType() {
    return ID+prototypeName;
  }

  public Command keyEvent(KeyStroke stroke) {
    return getExpandedInner().keyEvent(stroke);
  }

  public Command myKeyEvent(KeyStroke stroke) {
    return null;
  }

  public void mySetState(String newState) {
  }

  public Rectangle boundingBox() {
    return getExpandedInner().boundingBox();
  }

  public void draw(Graphics g, int x, int y, Component obs, double zoom) {
    getExpandedInner().draw(g,x,y,obs,zoom);
  }

  public String getName() {
    return getExpandedInner().getName();
  }

  public Shape getShape() {
    return getExpandedInner().getShape();
  }

  public String getPrototypeName() {
    return prototypeName;
  }

  public PieceEditor getEditor() {
    return new Editor(this);
  }

  public static class Editor implements PieceEditor {
    private StringConfigurer nameConfig;

    public Editor(UsePrototype up) {
      nameConfig = new StringConfigurer(null,"Prototype name:  ",up.prototypeName);
    }

    public Component getControls() {
      return nameConfig.getControls();
    }

    public String getState() {
      return "";
    }

    public String getType() {
      return ID+nameConfig.getValueString();
    }
  }
}
