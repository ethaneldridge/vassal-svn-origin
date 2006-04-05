/*
 * $Id$
 *
 * Copyright (c) 2000-2006 by Rodney Kinney
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

import java.util.Collections;
import java.util.List;

import VASSAL.build.module.PlayerRoster;

/**
 * Access is granted if the {@link VASSAL.build.module.PlayerRoster#getMySide()}
 * is in a specified list
 * 
 * @author rkinney
 * 
 */
public class SpecifiedSideAccess implements PieceAccess {
  private List sides;

  public SpecifiedSideAccess(List sides) {
    this.sides = Collections.unmodifiableList(sides);
  }

  public String getCurrentPlayerId() {
    return PlayerRoster.getMySide();
  }

  public boolean currentPlayerHasAccess(String id) {
    return id == null || (!GlobalAccess.isHideAll() && sides.contains(id));
  }
  
  public List getSides() {
    return sides;
  }

  public boolean canOwn(String id) {
    return sides.contains(getCurrentPlayerId());
  }
}
