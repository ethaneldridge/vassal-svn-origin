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
package VASSAL.build.module.map;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.tools.LaunchButton;
import VASSAL.build.module.Map;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.build.module.map.boardPicker.board.MapGrid;
import VASSAL.build.*;
import VASSAL.counters.*;
import VASSAL.configure.*;
import VASSAL.configure.HotKeyConfigurer;

import java.awt.*;
import java.beans.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.ImageObserver;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;

/**
 * A class that allows the user to draw a straight line on a Map (LOS
 * = Line Of Sight).  No automatic detection of obstacles is
 * performed; the user must simply observe the thread against the
 * image of the map.  However, if the user clicks on a board with a
 * {@link Map Grid}, the thread may snap to the grid and report the
 * distance between endpoints of the line
 * */
public class LOS_Thread extends AbstractConfigurable implements
  MouseListener, MouseMotionListener,
  Drawable, Configurable {
  public static final String SNAP_LOS = "snapLOS";
  public static final String LOS_COLOR = "threadColor";
  public static final String HOTKEY = "hotkey";
  public static final String LABEL = "label";
  public static final String DRAW_RANGE = "drawRange";
  public static final String HIDE_COUNTERS = "hideCounters";
  public static final String RANGE_BACKGROUND = "rangeBg";
  public static final String RANGE_FOREGROUND = "rangeFg";
  public static Font RANGE_FONT = new Font("Dialog", 0, 11);

  protected boolean retainAfterRelease = false;
  protected long lastRelease = 0;

  protected Map map;
  protected LaunchButton launch;
  protected KeyStroke hotkey;
  protected Point anchor;
  protected Point arrow;
  protected boolean visible;
  protected boolean drawRange;
  protected boolean hideCounters;
  protected String fixedColor;
  protected Color threadColor = Color.black, rangeFg = Color.white, rangeBg = Color.black;

  public LOS_Thread() {
    anchor = new Point(0, 0);
    arrow = new Point(0, 0);
    visible = false;
    ActionListener al = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        launch();
      }
    };
    launch = new LaunchButton("Thread", LABEL, HOTKEY, al);
    URL imageURL = getClass().getResource("/images/thread.gif");
    if (imageURL != null) {
      launch.setIcon(new ImageIcon(imageURL));
    }
    else {
      launch.setText("overview");
    }
  }

  /**
   * @return whether the thread should be drawn
   */
  public boolean isVisible() {
    return visible;
  }

  /**
   * If true, draw the thread on the map
   */
  public void setVisible(boolean state) {
    visible = state;
  }

  /**
   * Expects to be added to a {@link Map}.  Adds a button to the map
   * window's toolbar.  Pushing the button pushes a MouseListener
   * onto the Map that draws the thread.  Adds some entries to
   * preferences
   *
   * @see Map#pushMouseListener*/
  public void addTo(Buildable b) {
    map = (Map) b;
    map.getView().addMouseMotionListener(this);
    map.addDrawComponent(this);
    map.getToolBar().add(launch);
    GameModule.getGameModule().getPrefs().addOption
      (getAttributeValueString(LABEL),
       new BooleanConfigurer(SNAP_LOS, "Snap Thread to grid"));
    if (fixedColor == null) {
      ColorConfigurer config = new ColorConfigurer(LOS_COLOR, "Thread Color");
      GameModule.getGameModule().getPrefs().addOption
        (getAttributeValueString(LABEL), config);
      config.addPropertyChangeListener(new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
          threadColor = (Color) evt.getNewValue();
        }
      });
      config.fireUpdate();
    }
  }

  public void removeFrom(Buildable b) {
    map = (Map) b;
    map.removeDrawComponent(this);
    map.getToolBar().remove(launch);
  }

  /**
   * The attributes of an LOS_Thread are:
   * <pre>
   * <code>LABEL</code>:  the label of the button
   * <code>HOTKEY</code>:  the hotkey equivalent of the button
   * <code>DRAW_RANGE</code>:  If true, draw the distance between endpoints of the thread
   * <code>RANGE_FOREGROUND</code>:  the color of the text when drawing the distance
   * <code>RANGE_BACKGROUND</code>:  the color of the background rectangle when drawing the distance
   * <code>HIDE_COUNTERS</code>:  If true, hide all {@link GamePiece}s on the map when drawing the thread
   * </pre>
   */
  public String[] getAttributeNames() {
    return new String[]{HOTKEY, LABEL, DRAW_RANGE, HIDE_COUNTERS, LOS_COLOR, RANGE_FOREGROUND, RANGE_BACKGROUND};
  }

  public void setAttribute(String key, Object value) {
    if (DRAW_RANGE.equals(key)) {
      if (value instanceof String) {
        value = new Boolean((String) value);
      }
      drawRange = ((Boolean) value).booleanValue();
    }
    else if (HIDE_COUNTERS.equals(key)) {
      if (value instanceof String) {
        value = new Boolean((String) value);
      }
      hideCounters = ((Boolean) value).booleanValue();
    }
    else if (RANGE_FOREGROUND.equals(key)) {
      if (value instanceof String) {
        value = ColorConfigurer.stringToColor((String) value);
      }
      rangeFg = (Color) value;
    }
    else if (RANGE_BACKGROUND.equals(key)) {
      if (value instanceof String) {
        value = ColorConfigurer.stringToColor((String) value);
      }
      rangeBg = (Color) value;
    }
    else if (LOS_COLOR.equals(key)) {
      if (value instanceof Color) {
        value = ColorConfigurer.colorToString((Color) value);
      }
      fixedColor = (String) value;
      threadColor = (Color) ColorConfigurer.stringToColor(fixedColor);
    }
    else {
      launch.setAttribute(key, value);
    }
  }

  public String getAttributeValueString(String key) {
    if (DRAW_RANGE.equals(key)) {
      return "" + drawRange;
    }
    else if (HIDE_COUNTERS.equals(key)) {
      return "" + hideCounters;
    }
    else if (RANGE_FOREGROUND.equals(key)) {
      return ColorConfigurer.colorToString(rangeFg);
    }
    else if (RANGE_BACKGROUND.equals(key)) {
      return ColorConfigurer.colorToString(rangeBg);
    }
    else if (LOS_COLOR.equals(key)) {
      return fixedColor;
    }
    else {
      return launch.getAttributeValueString(key);
    }
  }

  public void setup(boolean show) {
    launch.setEnabled(show);
  }

  public void draw(java.awt.Graphics g, Map m) {
    if (!visible) {
      return;
    }
    g.setColor(threadColor);
    Point mapAnchor = map.componentCoordinates(anchor);
    Point mapArrow = map.componentCoordinates(arrow);
    g.drawLine(mapAnchor.x, mapAnchor.y, mapArrow.x, mapArrow.y);
    Board b;
    if (drawRange
      && (b = map.findBoard(anchor)) != null
      && b.getGrid() != null) {
      drawRange(g, b.getGrid().range(anchor, arrow));
    }
  }

  protected void launch() {
    if (!visible) {
      map.pushMouseListener(this);
      if (hideCounters) {
        map.setPiecesVisible(false);
        map.repaint();
      }
      visible = true;
      anchor.move(0, 0);
      arrow.move(0, 0);
      retainAfterRelease = false;
    }
  }

  /** Since we register ourselves as a MouseListener using {@link
   * Map#pushMouseListener}, these mouse events are received in map
   * coordinates */
  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mouseClicked(MouseEvent e) {
  }

  public void mousePressed(MouseEvent e) {
    if (visible) {
      Point p = e.getPoint();
      if (Boolean.TRUE.equals
        (GameModule.getGameModule().getPrefs().getValue(SNAP_LOS))) {
        p = map.snapTo(p);
      }
      anchor = p;
    }
  }

  public void mouseReleased(MouseEvent e) {
    if (retainAfterRelease) {
      retainAfterRelease = false;
    }
    else if (e.getWhen() != lastRelease) {
      visible = false;
      map.setPiecesVisible(true);
      map.popMouseListener();
      map.repaint();
    }
    lastRelease = e.getWhen();
  }

  /** Since we register ourselves as a MouseMotionListener directly,
   * these mouse events are received in component
   * coordinates */
  public void mouseMoved(MouseEvent e) {
  }

  public void mouseDragged(MouseEvent e) {
    if (visible) {
      retainAfterRelease = true;

      Point p = e.getPoint();
      if (Boolean.TRUE.equals
        (GameModule.getGameModule().getPrefs().getValue(SNAP_LOS))) {
        p = map.componentCoordinates(map.snapTo(map.mapCoordinates(p)));
      }
      arrow = map.mapCoordinates(p);

      map.repaint();
    }
  }

  /**
   * Writes text showing the range
   *
   * @param range the range to display, in whatever units returned
   * by the {@link MapGrid} containing the thread */
  public void drawRange(Graphics g, int range) {
    Point mapArrow = map.componentCoordinates(arrow);
    Point mapAnchor = map.componentCoordinates(anchor);
    g.setColor(Color.black);
    g.setFont(RANGE_FONT);
    FontMetrics fm = g.getFontMetrics();
    int wid = fm.stringWidth(" Range 88 ");
    int hgt = fm.getAscent() + 2;
    int w = mapArrow.x - mapAnchor.x;
    int h = mapArrow.y - mapAnchor.y;
    int x0 = mapArrow.x + (int) ((wid / 2 + 20) * w / Math.sqrt(w * w + h * h));
    int y0 = mapArrow.y + (int) ((hgt / 2 + 20) * h / Math.sqrt(w * w + h * h));
    g.fillRect(x0 - wid / 2, y0 + hgt / 2 - fm.getAscent(), wid, hgt);
    g.setColor(Color.white);
    g.drawString("Range " + range,
                 x0 - wid / 2 + fm.stringWidth(" "), y0 + hgt / 2);
  }

  public static String getConfigureTypeName() {
    return "Line of Sight Thread";
  }

  public String getConfigureName() {
    return null;
  }

  public VASSAL.build.module.documentation.HelpFile getHelpFile() {
    File dir = new File("docs");
    dir = new File(dir, "ReferenceManual");
    try {
      return new HelpFile(null, new File(dir, "Map.htm"), "#LOS");
    }
    catch (MalformedURLException ex) {
      return null;
    }
  }

  public String[] getAttributeDescriptions() {
    return new String[]{"Hotkey",
                        "Button text",
                        "Draw Range",
                        "Hide Pieces while drawing",
                        "Thread color"};
  }

  public Class[] getAttributeTypes() {
    return new Class[]{KeyStroke.class,
                       String.class,
                       Boolean.class,
                       Boolean.class,
                       Color.class};
  }

  public Configurable[] getConfigureComponents() {
    return new Configurable[0];
  }

  public Class[] getAllowableConfigureComponents() {
    return new Class[0];
  }
}
