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

import VASSAL.tools.TransparentFilter;

import java.awt.*;
import java.awt.image.FilteredImageSource;

/**
 * A class that draws a GamePiece with a specifyable level of transparency
 */
public class Transparent {
  private double alpha = 0.2;
  private PieceImage opaque;
  private Image im;
  private GamePiece piece;
  private Point offset;

  public Transparent(GamePiece p) {
    setPiece(p);
  }

  public void setPiece(GamePiece p) {
    piece = p;
    opaque = new PieceImage(p);
  }

  public GamePiece getPiece() {
    return piece;
  }

  public void setAlpha(double val) {
    alpha = val;
  }

  public void draw(Graphics g, int x, int y, Component obs, double zoom) {
    if (alpha == 1.0) {
      piece.draw(g, x, y, obs, zoom);
      return;
    }
    if (opaque.isChanged()) {
      int trans = TransparentFilter.getOffscreenEquivalent(obs.getBackground().getRGB(), obs);
      TransparentFilter filter = new TransparentFilter();
      filter.setAlpha(alpha);
      filter.setAlpha(0.0, trans);
      im = opaque.getImage(obs);
      Image im2 = obs.createImage(im.getWidth(obs), im.getHeight(obs));
      Graphics gg = im2.getGraphics();
      gg.drawImage(im, 0, 0, obs);
      im = obs.createImage(new FilteredImageSource
        (im2.getSource(), filter));
      offset = new Point(piece.boundingBox().x,
                         piece.boundingBox().y);

    }
    g.drawImage(im,
                x + (int) (zoom * offset.x),
                y + (int) (zoom * offset.y),
                (int) (zoom * im.getWidth(obs)),
                (int) (zoom * im.getHeight(obs)),
                obs);
  }
}
