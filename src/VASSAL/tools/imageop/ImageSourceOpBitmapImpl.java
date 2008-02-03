/*
 * $Id: ImageSourceOp.java 2734 2007-12-21 14:30:36Z uckelman $
 *
 * Copyright (c) 2007 by Joel Uckelman
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

package VASSAL.tools.imageop;

import java.awt.Dimension;
import java.awt.Image;

/**
 * An {@link ImageOp} which uses a fixed image as its source.
 * <code>ImageSourceOp</code> holds a reference to the <code>Image</code>
 * it was constructed with in order to prevent it from being garbage
 * collected from the soft cache, since this op has no way of recreating
 * the source image.
 *
 * @since 3.1.0
 * @author Joel Uckelman
 */
public class ImageSourceOpBitmapImpl extends AbstractTiledOpImpl
                                     implements SourceOp {
  private final Image image;
  private final int hash;

  /**
   * Constructs an <code>ImageOp</code> which will hold and return
   * the image it is constructed with.
   *
   * @param image the source image
   * @throws IllegalArgumentException if <code>image == null</code>.
   */
  public ImageSourceOpBitmapImpl(Image image) {
    if (image == null) throw new IllegalArgumentException();
    this.image = image;
    hash = image.hashCode();
  }

  /** {@inheritDoc} */
  public Image apply() {
    return image;
  }

  /** {@inheritDoc} */
  protected void fixSize() {
    size = new Dimension(image.getWidth(null), image.getHeight(null));

    tileSize = new Dimension(256,256);

    numXTiles = (int) Math.ceil((double)size.width/tileSize.width);
    numYTiles = (int) Math.ceil((double)size.height/tileSize.height);

    tiles = new ImageOp[numXTiles*numYTiles];
  }

  /** {@inheritDoc} */
  protected ImageOp getTileOp(int tileX, int tileY) {
    ImageOp top = tiles[tileY*numXTiles + tileX];
    if (top == null) {
      top = tiles[tileY*numXTiles + tileX]
          = new SourceTileOpBitmapImpl(this, tileX, tileY);
    }

    return top;
  }

  public ImageOp getSource() {
    return null;
  }

  public String getName() {
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || o.getClass() != this.getClass()) return false;
    return image.equals(((ImageSourceOpBitmapImpl) o).image);
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return hash;
  }
}
