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
package VASSAL.build.module.map.boardPicker;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.IllegalBuildException;
import VASSAL.build.module.GameComponent;
import VASSAL.build.module.Map;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.map.BoardPicker;
import VASSAL.build.module.map.boardPicker.board.HexGrid;
import VASSAL.build.module.map.boardPicker.board.MapGrid;
import VASSAL.build.module.map.boardPicker.board.RegionGrid;
import VASSAL.build.module.map.boardPicker.board.SquareGrid;
import VASSAL.command.Command;
import VASSAL.configure.ColorConfigurer;
import VASSAL.configure.VisibilityCondition;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Hashtable;

public class Board extends AbstractConfigurable {
  /**
   * A Board is a piece of a Map.  A Map can cantain a set of boards
   * layed out in a rectangular grid.
   */
  public static final String NAME = "name";
  public static final String IMAGE = "image";
  public static final String WIDTH = "width";
  public static final String HEIGHT = "height";
  public static final String COLOR = "color";
  public static final String REVERSIBLE = "reversible";

  protected Image boardImage;

  /** @deprecated */
  protected Hashtable scaledCache = new Hashtable();

  protected Point pos = new Point(0, 0);
  protected Rectangle boundaries = new Rectangle(0, 0, 500, 500);
  protected String imageFile;
  protected String boardName = "Board 1";
  protected boolean reversible = false;
  protected boolean reversed = false;

  private Color color = null;

  private MapGrid grid = null;

  private Map map;

  /** Until a game is started that is using this board, the map will be null */
  public Map getMap() {
    return map;
  }

  public void setMap(Map map) {
    this.map = map;
  }

  public Board() {
    setConfigureName(boardName);
  }

  public String getName() {
    return boardName;
  }

  public void addTo(Buildable b) {
    BoardPicker picker = (BoardPicker) b;
    String s[] = picker.getAllowableBoardNames();
    for (int i = 0; i < s.length; ++i) {
      if (s[i].equals(getName())) {
        int count = 1;
        try {
          count = Integer.parseInt(s[i].substring(s[i].indexOf('-') + 1));
          setAttribute(NAME, s[i].substring(0, s[i].indexOf('-')) + "-" + (count + 1));
        }
        catch (NumberFormatException ex) {
          setAttribute(NAME, s[i] + "-2");
        }
      }
    }
  }

  public void removeFrom(Buildable b) {
    String s[] = ((BoardPicker) b).getAllowableBoardNames();
    if (s.length == 1 && s[0].equals(getName()))
      throw new IllegalBuildException
          ("You must define at least one map board");
  }

  public String[] getAttributeNames() {
    String s[] = {NAME, IMAGE, REVERSIBLE, WIDTH, HEIGHT, COLOR};
    return s;
  }

  public String[] getAttributeDescriptions() {
    return new String[]{"Board name",
                        "Board image",
                        "Reversible",
                        "Board width",
                        "Board height",
                        "Background color"};
  }

  public Class[] getAttributeTypes() {
    return new Class[]{String.class,
                       Image.class,
                       Boolean.class,
                       Integer.class,
                       Integer.class,
                       Color.class};
  }

  public String getConfigureName() {
    return super.getConfigureName();
  }

  public VisibilityCondition getAttributeVisibility(String name) {
    if (REVERSIBLE.equals(name)) {
      return new VisibilityCondition() {
        public boolean shouldBeVisible() {
          return imageFile != null;
        }
      };
    }
    else if (WIDTH.equals(name)
        || HEIGHT.equals(name)
        || COLOR.equals(name)) {
      return new VisibilityCondition() {
        public boolean shouldBeVisible() {
          return imageFile == null;
        }
      };
    }
    else {
      return null;
    }
  }

  public String getAttributeValueString(String key) {
    if (NAME.equals(key)) {
      return boardName;
    }
    else if (IMAGE.equals(key)) {
      return imageFile;
    }
    else if (WIDTH.equals(key)) {
      return imageFile == null ? "" + boundaries.width : null;
    }
    else if (HEIGHT.equals(key)) {
      return imageFile == null ? "" + boundaries.height : null;
    }
    else if (COLOR.equals(key)) {
      return imageFile == null ? ColorConfigurer.colorToString(color) : null;
    }
    else if (REVERSIBLE.equals(key)) {
      return "" + reversible;
    }
    return null;
  }

  public void setAttribute(String key, Object val) {
    if (NAME.equals(key)) {
      boardName = (String) val;
      setConfigureName(boardName);
    }
    else if (IMAGE.equals(key)) {
      if (val instanceof File) {
        val = ((File) val).getName();
      }
      imageFile = (String) val;
    }
    else if (WIDTH.equals(key)) {
      if (val instanceof String) {
        val = new Integer((String) val);
      }
      if (val != null) {
        boundaries.setSize(((Integer) val).intValue(),
                           boundaries.height);
      }
    }
    else if (HEIGHT.equals(key)) {
      if (val instanceof String) {
        val = new Integer((String) val);
      }
      if (val != null) {
        boundaries.setSize(boundaries.width,
                           ((Integer) val).intValue());
      }
    }
    else if (COLOR.equals(key)) {
      if (val instanceof String) {
        val = ColorConfigurer.stringToColor((String) val);
      }
      color = (Color) val;
    }
    else if (REVERSIBLE.equals(key)) {
      if (val instanceof String) {
        val = new Boolean("true".equals(val));
      }
      reversible = ((Boolean) val).booleanValue();
    }
  }


  public Class[] getAllowableConfigureComponents() {
    Class[] c = {HexGrid.class, SquareGrid.class, RegionGrid.class};
    return c;
  }

  public void draw(java.awt.Graphics g,
                   int x, int y, double zoom, Component obs) {
    drawRegion(g, new Point(x, y), new Rectangle(x, y, Math.round((float) zoom * boundaries.width), Math.round((float) zoom * boundaries.height)), zoom, obs);
  }

  public void drawRegion(final Graphics g, final Point location, Rectangle visibleRect, final double zoom, final Component obs) {
    Rectangle bounds = new Rectangle(location.x, location.y, Math.round(boundaries.width * (float) zoom), Math.round(boundaries.height * (float) zoom));
    if (visibleRect.intersects(bounds)) {
      visibleRect = visibleRect.intersection(bounds);
      if (boardImage != null) {
        Image scaled = getScaledImage(zoom, obs);
        g.drawImage(scaled, location.x, location.y, obs);
      }
      else {
        if (color != null) {
          g.setColor(color);
          g.fillRect(visibleRect.x, visibleRect.y, visibleRect.width, visibleRect.height);
        }
        else {
          g.clearRect(visibleRect.x, visibleRect.y, visibleRect.width, visibleRect.height);
        }
      }
      if (grid != null) {
        if (grid.isVisible()) {
          grid.draw(g, bounds, visibleRect, zoom, reversed);
        }
        if (grid.getGridNumbering() != null
            && grid.getGridNumbering().isVisible()) {
          grid.getGridNumbering().draw(g, bounds, visibleRect, zoom, reversed);
        }
      }
    }
  }

  public synchronized Image getScaledImage(double zoom, Component obs) {
    return GameModule.getGameModule().getDataArchive().getScaledImage(boardImage, zoom, reversed, false);
  }

  public void setReversed(boolean val) {
    if (reversible) {
      reversed = val;
    }
  }

  public boolean isReversed() {
    return reversed;
  }

  /**
   * If this board is reversed, return the location
   * in un-reversed coordinates
   */
  public Point localCoordinates(Point p) {
    if (reversed) {
      p = new Point(bounds().width - p.x, bounds().height - p.y);
    }
    return p;
  }

  /**
   * If this board is reversed, return the location in
   * reversed coordinates
   */
  public Point globalCoordinates(Point p) {
    return localCoordinates(p);
  }

  public void setGrid(MapGrid mg) {
    if (mg != null) {
      for (Enumeration e = getBuildComponents();
           e.hasMoreElements();) {
        if (e.nextElement() instanceof MapGrid) {
          throw new IllegalBuildException("Can only contain one grid");
        }
      }
    }
    grid = mg;
  }

  public MapGrid getGrid() {
    return grid;
  }

  public void fixImage(Component map) {
    Cleanup.init();
    Cleanup.getInstance().addBoard(this);
    if (imageFile == null)
      return;
    try {
      try {
        boardImage = GameModule.getGameModule().getDataArchive().getImage
            (GameModule.getGameModule().getDataArchive().getFileStream
             ("images/" + imageFile));
      }
      catch (IOException e) {
        boardImage = null;
      }

      if (boardImage == null && imageFile != null) {
        JOptionPane.showMessageDialog
            (null,
             "Error reading board image " + imageFile + " in "
             + GameModule.getGameModule().getDataArchive().getName(),
             "Not Found",
             JOptionPane.ERROR_MESSAGE);
        return;
      }


      MediaTracker track = new MediaTracker(map);

      try {
        track.addImage(boardImage, 0);
        track.waitForID(0);
      }
      catch (Exception eWaitIm) {
      }

      boundaries.setSize(boardImage.getWidth(map),
                         boardImage.getHeight(map));
    }
    catch (OutOfMemoryError err) {
      JOptionPane.showMessageDialog
          (null,
           "Insufficient memory to load board " + getName()
           + "\nTry setting your display to use fewer colors",
           "Out of memory",
           JOptionPane.ERROR_MESSAGE);
    }
  }

  public String locationName(Point p) {
    return grid == null ? null : grid.locationName(localCoordinates(p));
  }

  public Point snapTo(Point p) {
    return grid == null ? p : globalCoordinates(grid.snapTo(localCoordinates(p)));
  }

  public String fileName() {
    return imageFile;
  }

  /**
   * @return Position of this board relative to the other boards
   * (0,0) is the upper left, (0,1) is to the right, etc.
   */
  public Point relativePosition() {
    return pos;
  }

  /**
   * @return The (read-only) boundaries of this Board within the overall Map
   */
  public Rectangle bounds() {
    return new Rectangle(boundaries);
  }

  /** Translate the location of the board by the given number of pixels
   * @see #bounds()
   */
  public void translate(int x, int y) {
    boundaries.translate(x, y);
  }

  /** Set the location of this board
   * @see #bounds()
   */
  public void setLocation(int x, int y) {
    boundaries.setLocation(x, y);
  }

  public HelpFile getHelpFile() {
    File dir = VASSAL.build.module.Documentation.getDocumentationBaseDir();
    dir = new File(dir, "ReferenceManual");
    try {
      return new HelpFile(null, new File(dir, "Board.htm"));
    }
    catch (MalformedURLException ex) {
      return null;
    }
  }

  /**
   * Removes board images from the {@link VASSAL.tools.DataArchive} cache
   */
  public void cleanUp() {
    if (imageFile != null) {
      GameModule.getGameModule().getDataArchive().unCacheImage("images/" + imageFile);
    }
    if (boardImage != null) {
      GameModule.getGameModule().getDataArchive().unCacheImage(boardImage);
      boardImage = null;
    }
  }

  /**
   * Cleans up {@link Board}s (by invoking {@link Board#cleanUp}) when a game is closed
   */
  public static class Cleanup implements GameComponent {
    private static Cleanup instance;

    private HashSet toClean = new HashSet();

    private boolean gameStarted = false;

    public static void init() {
      if (instance == null) {
        instance = new Cleanup();
      }
    }

    private Cleanup() {
      GameModule.getGameModule().getGameState().addGameComponent(this);
    }

    public static Cleanup getInstance() {
      return instance;
    }

    /**
     * Mark this board as needing to be cleaned up when the game is closed
     * @param b
     */
    public void addBoard(Board b) {
      toClean.add(b);
    }

    public Command getRestoreCommand() {
      return null;
    }

    public void setup(boolean gameStarting) {
      if (gameStarted && !gameStarting) {
        for (Iterator iterator = toClean.iterator(); iterator.hasNext();) {
          Board board = (Board) iterator.next();
          board.cleanUp();
          iterator.remove();
        }
      }
      gameStarted = gameStarting;
      System.gc();
    }
  }
}
