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

import VASSAL.build.*;
import VASSAL.build.module.GameComponent;
import VASSAL.build.module.Map;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.tools.LaunchButton;
import VASSAL.tools.BackgroundTask;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Controls the zooming in/out of a Map Window
 */
public class Zoomer extends AbstractConfigurable implements GameComponent {
  private Map map;
  private double zoomStep = 1.5;
  private int zoomLevel = 0;
  private double[] zoomFactor;
  private int maxZoom = 3;
  private LaunchButton zoomInButton;
  private LaunchButton zoomOutButton;

  public Zoomer() {
    ActionListener zoomIn = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        zoomIn();
      }
    };
    ActionListener zoomOut = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (zoomLevel < zoomFactor.length - 1) {
          final JWindow w = new JWindow(SwingUtilities.getWindowAncestor(map.getView()));
          w.getContentPane().setBackground(Color.white);
          JLabel l = new JLabel("Scaling Map ...");
          l.setFont(new Font("Dialog",Font.PLAIN,48));
          l.setBackground(Color.white);
          l.setForeground(Color.black);
          l.setBorder(new BevelBorder(BevelBorder.RAISED,Color.lightGray,Color.darkGray));
          w.getContentPane().add(l);
          w.pack();
          Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
          w.setLocation(d.width/2-w.getSize().width/2,d.height/2-w.getSize().height/2);
          final Vector finished = new Vector();
          Runnable runnable = new Runnable() {
            public void run() {
              try {
                Thread.sleep(100);
                if (!finished.contains(w)) {
                  w.setVisible(true);
                }
              }
              catch (InterruptedException e1) {
              }
            }
          };
          new Thread(runnable).start();
          BackgroundTask task = new BackgroundTask() {
            public void doFirst() {
              scaleBoards(zoomFactor[zoomLevel + 1]);
            }

            public void doLater() {
              zoomOut();
              finished.add(w);
              w.dispose();
            }
          };
          task.start();
        }
      }
    };

    zoomInButton = new LaunchButton("Z", null, ZOOM_IN, zoomIn);
    zoomInButton.setToolTipText("Zoom in");
    zoomInButton.setEnabled(false);
    zoomOutButton = new LaunchButton("z", null, ZOOM_OUT, zoomOut);
    zoomOutButton.setToolTipText("Zoom out");

    setConfigureName(null);
  }

  public static String getConfigureTypeName() {
    return "Zoom capability";
  }

  public String[] getAttributeNames() {
    return new String[]{FACTOR, MAX, ZOOM_IN, ZOOM_OUT};
  }

  public String[] getAttributeDescriptions() {
    return new String[]{"Magnification factor",
                        "Number of zoom levels",
                        "Zoom in hotkey",
                        "Zoom out hotkey"};
  }

  public Class[] getAttributeTypes() {
    return new Class[]{Double.class,
                       Integer.class,
                       KeyStroke.class,
                       KeyStroke.class};
  }

  private static final String FACTOR = "factor";
  private static final String MAX = "max";
  private static final String ZOOM_IN = "zoomInKey";
  private static final String ZOOM_OUT = "zoomOutKey";

  public void addTo(Buildable b) {
    GameModule.getGameModule().getGameState().addGameComponent(this);

    map = (Map) b;

    Configurable c[] = map.getConfigureComponents();
    for (int i = 0; i < c.length; ++i) {
      if (c[i] instanceof Zoomer) {
        throw new IllegalBuildException("Only one Zoom allowed per map");
      }
    }
    map.setZoomer(this);
    map.getToolBar().add(zoomInButton);
    java.net.URL image = getClass().getResource("/images/zoomIn.gif");
    if (image != null) {
      zoomInButton.setIcon(new ImageIcon(image));
      zoomInButton.setText("");
    }
    map.getToolBar().add(zoomOutButton);
    image = getClass().getResource("/images/zoomOut.gif");
    if (image != null) {
      zoomOutButton.setIcon(new ImageIcon(image));
      zoomOutButton.setText("");
    }
  }

  public String getAttributeValueString(String key) {
    if (MAX.equals(key)) {
      return "" + maxZoom;
    }
    else if (FACTOR.equals(key)) {
      return "" + zoomStep;
    }
    else if (zoomInButton.getAttributeValueString(key) != null) {
      return zoomInButton.getAttributeValueString(key);
    }
    else {
      return zoomOutButton.getAttributeValueString(key);
    }
  }

  public void setAttribute(String key, Object val) {
    if (MAX.equals(key)) {
      if (val instanceof String) {
        val = new Integer((String) val);
      }
      if (val != null) {
        maxZoom = ((Integer) val).intValue();
      }
      initZoomFactors();
    }
    else if (FACTOR.equals(key)) {
      if (val instanceof String) {
        val = new Double((String) val);
      }
      if (val != null) {
        zoomStep = ((Double) val).doubleValue();
      }
      initZoomFactors();
    }
    else {
      zoomInButton.setAttribute(key, val);
      zoomOutButton.setAttribute(key, val);
    }
  }

  private void initZoomFactors() {
    zoomFactor = new double[maxZoom];
    zoomFactor[0] = 1.0;
    for (int i = 1; i < zoomFactor.length; ++i) {
      zoomFactor[i] = zoomFactor[i-1]/zoomStep;
    }
  }

  public Class[] getAllowableConfigureComponents() {
    return new Class[0];
  }

  public void removeFrom(Buildable b) {
    map = (Map) b;
    map.setZoomer(null);
    map.getToolBar().remove(zoomInButton);
    map.getToolBar().remove(zoomOutButton);
  }

  public double getZoomFactor() {
    return zoomFactor[zoomLevel];
  }

  private void scaleBoards(double zoom) {
    for (Enumeration e = map.getAllBoards(); e.hasMoreElements();) {
      Board b = (Board) e.nextElement();
      b.getScaledImage(zoom, map.getView());
    }
  }

  public void zoomIn() {
    if (zoomInButton.isEnabled()) {
      Rectangle r = map.getView().getVisibleRect();
      Point center = new Point(r.x + r.width / 2, r.y + r.height / 2);
      center = map.mapCoordinates(center);

      zoomLevel--;
      zoomInButton.setEnabled(zoomLevel > 0);
      zoomOutButton.setEnabled(zoomLevel < maxZoom - 1);

      map.centerAt(center);

      map.repaint(true);
      map.getView().revalidate();
    }
  }

  public void zoomOut() {
    if (zoomOutButton.isEnabled()) {
      Rectangle r = map.getView().getVisibleRect();
      Point center = new Point(r.x + r.width / 2, r.y + r.height / 2);
      center = map.mapCoordinates(center);

      zoomLevel++;
      zoomInButton.setEnabled(zoomLevel > 0);
      zoomOutButton.setEnabled(zoomLevel < maxZoom - 1);

      map.centerAt(center);

      map.repaint(true);
      map.getView().revalidate();
    }
  }

  public VASSAL.build.module.documentation.HelpFile getHelpFile() {
    File dir = VASSAL.build.module.Documentation.getDocumentationBaseDir();
    dir = new File(dir, "ReferenceManual");
    try {
      return new HelpFile(null, new File(dir, "Map.htm"), "#Zoom");
    }
    catch (MalformedURLException ex) {
      return null;
    }
  }

  public void setup(boolean gameStarting) {
    if (!gameStarting) {
      zoomLevel = 0;
      zoomInButton.setEnabled(false);
      zoomOutButton.setEnabled(true);
    }
  }

  public VASSAL.command.Command getRestoreCommand() {
    return null;
  }

}
