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
package VASSAL.command;

import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.counters.GamePiece;
import VASSAL.counters.KeyBuffer;

/**
 * This Command removed a {@link GamePiece} from a game.  Its undo
 * Command is {@link AddPiece}.  */
public class RemovePiece extends Command {
  private Command undo = null;
  private GamePiece target;
  private String id;

  public RemovePiece(GamePiece p) {
    target = p;
  }

  public RemovePiece(String id) {
    this.id = id;
  }

  /**
   * Removes a piece by invoking {@link Map#removePiece} if the
   * piece belongs to a {@link Map}, followed by {@link
   * GameState#removePiece}.  */
  protected void executeCommand() {
    if (target == null) {
      target = GameModule.getGameModule().getGameState().getPieceForId(id);
    }
    if (target != null) {
      undo = new AddPiece(target, target.getState());
      java.awt.Rectangle r = null;
      Map m = target.getMap();
      if (m != null) {
        r = target.getParent() == null ?
          m.boundingBoxOf(target) : m.boundingBoxOf(target.getParent());
      }
      if (target.getMap() != null) {
        target.getMap().removePiece(target);
        target.setMap(null);
      }
      if (target.getParent() != null) {
        target.getParent().remove(target);
        target.setParent(null);
      }
      if (m != null) {
        m.repaint(r);
      }
      GameModule.getGameModule().getGameState().removePiece(target.getId());
      KeyBuffer.getBuffer().remove(target);
    }
  }

  protected Command myUndoCommand() {
    if (undo == null && target != null) {
      undo = new AddPiece(target);
    }
    return undo;
  }

  public GamePiece getTarget() {
    return target;
  }

  public String getId() {
    return target == null ? id : target.getId();
  }
}
