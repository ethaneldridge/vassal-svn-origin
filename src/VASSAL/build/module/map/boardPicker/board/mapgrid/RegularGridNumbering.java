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
 * Date: Jul 21, 2002
 * Time: 10:18:26 PM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package VASSAL.build.module.map.boardPicker.board.mapgrid;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.configure.*;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.MalformedURLException;

/**
 * Abstract base class for grid numbering classes for hexagonal and rectangular grids
 */
public abstract class RegularGridNumbering extends AbstractConfigurable implements GridNumbering {
  protected PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
  protected char first = 'H';
  protected String sep = "";
  protected char hType = 'N';
  protected char vType = 'N';
  protected int hLeading = 1;
  protected int vLeading = 1;
  protected int hOff = 1;
  protected int vOff = 1;
  protected boolean visible = false;
  protected int fontSize = 9;
  protected Color color = Color.black;
  protected JComponent visualizer;

  public static final String FIRST = "first";
  public static final String SEP = "sep";
  public static final String H_TYPE = "hType";
  public static final String V_TYPE = "vType";
  public static final String H_LEADING = "hLeading";
  public static final String V_LEADING = "vLeading";
  public static final String H_OFF = "hOff";
  public static final String V_OFF = "vOff";
  public static final String FONT_SIZE = "fontSize";
  public static final String COLOR = "color";
  public static final String VISIBLE = "visible";

  public String getAttributeValueString(String key) {
    if (FIRST.equals(key)) {
      return "" + first;
    }
    else if (SEP.equals(key)) {
      return sep;
    }
    else if (H_TYPE.equals(key)) {
      return "" + hType;
    }
    else if (V_TYPE.equals(key)) {
      return "" + vType;
    }
    else if (H_LEADING.equals(key)) {
      return "" + hLeading;
    }
    else if (V_LEADING.equals(key)) {
      return "" + vLeading;
    }
    else if (H_OFF.equals(key)) {
      return "" + hOff;
    }
    else if (V_OFF.equals(key)) {
      return "" + vOff;
    }
    else if (FONT_SIZE.equals(key)) {
      return "" + fontSize;
    }
    else if (COLOR.equals(key)) {
      return ColorConfigurer.colorToString(color);
    }
    else if (VISIBLE.equals(key)) {
      return "" + visible;
    }
    else {
      return null;
    }
  }

  public void setAttribute(String key, Object value) {
    if (FIRST.equals(key)) {
      first = ((String) value).charAt(0);
    }
    else if (SEP.equals(key)) {
      sep = (String) value;
    }
    else if (H_TYPE.equals(key)) {
      hType = ((String) value).charAt(0);
    }
    else if (V_TYPE.equals(key)) {
      vType = ((String) value).charAt(0);
    }
    else if (H_LEADING.equals(key)) {
      if (value instanceof String) {
        value = new Integer((String) value);
      }
      hLeading = ((Integer) value).intValue();
    }
    else if (V_LEADING.equals(key)) {
      if (value instanceof String) {
        value = new Integer((String) value);
      }
      vLeading = ((Integer) value).intValue();
    }
    else if (H_OFF.equals(key)) {
      if (value instanceof String) {
        value = new Integer((String) value);
      }
      hOff = ((Integer) value).intValue();
    }
    else if (V_OFF.equals(key)) {
      if (value instanceof String) {
        value = new Integer((String) value);
      }
      vOff = ((Integer) value).intValue();
    }
    else if (FONT_SIZE.equals(key)) {
      if (value instanceof String) {
        value = new Integer((String) value);
      }
      fontSize = ((Integer) value).intValue();
    }
    else if (COLOR.equals(key)) {
      if (value instanceof String) {
        value = ColorConfigurer.stringToColor((String) value);
      }
      color = (Color) value;
    }
    else if (VISIBLE.equals(key)) {
      if (value instanceof String) {
        value = new Boolean((String) value);
      }
      visible = ((Boolean) value).booleanValue();
    }
  }

  public boolean isVisible() {
    return visible;
  }

  public abstract int getRow(Point p);

  public abstract int getColumn(Point p);

  public void addPropertyChangeListener(PropertyChangeListener l) {
    propSupport.addPropertyChangeListener(l);
  }

  public Class[] getAllowableConfigureComponents() {
    return new Class[0];
  }

  public String[] getAttributeNames() {
    return new String[]{FIRST, SEP, H_TYPE, H_LEADING, H_OFF, V_TYPE, V_LEADING, V_OFF, VISIBLE, FONT_SIZE, COLOR};
  }

  public String[] getAttributeDescriptions() {
    return new String[]{"Order",
                        "Separator",
                        "Horizontal numbering",
                        "Leading zeros in horizontal",
                        "Starting number in horizontal",
                        "Vertical numbering",
                        "Leading zeros in vertical",
                        "Starting number in vertical",
                        "Draw Numbering",
                        "Font size",
                        "Color"};
  }

  public static class F extends StringEnum {
    public String[] getValidValues() {
      return new String[]{"Horizontal first", "Vertical first"};
    }
  }

  public static class T extends StringEnum {
    public String[] getValidValues() {
      return new String[]{"Numerical", "Alphabetic"};
    }
  }

  public Class[] getAttributeTypes() {
    return new Class[]{F.class,
                       String.class,
                       T.class,
                       Integer.class,
                       Integer.class,
                       T.class,
                       Integer.class,
                       Integer.class,
                       Boolean.class,
                       Integer.class,
                       Color.class};

  }

  /**
   * Return a component that shows how the grid will draw itself
   */
  protected abstract JComponent getGridVisualizer();

  public VisibilityCondition getAttributeVisibility(String name) {
    if (FONT_SIZE.equals(name)
      || COLOR.equals(name)) {
      VisibilityCondition cond = new VisibilityCondition() {
        public boolean shouldBeVisible() {
          return visible;
        }
      };
      return cond;
    }
    else if (H_LEADING.equals(name)) {
      return new VisibilityCondition() {
        public boolean shouldBeVisible() {
          return hType == 'N';
        }
      };
    }
    else if (V_LEADING.equals(name)) {
      return new VisibilityCondition() {
        public boolean shouldBeVisible() {
          return vType == 'N';
        }
      };
    }
    else {
      return super.getAttributeVisibility(name);
    }
  }

  public Configurer getConfigurer() {
    AutoConfigurer c = (AutoConfigurer) super.getConfigurer();
    String[] s = getAttributeNames();
    for (int i = 0; i < s.length; ++i) {
      c.getConfigurer(s[i]).addPropertyChangeListener(new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
          visualizer.repaint();
        }
      });
    }
    ((Container) c.getControls()).add(getGridVisualizer());
    return c;
  }

  public static String getConfigureTypeName() {
    return "Grid Numbering";
  }

  public HelpFile getHelpFile() {
    File dir = VASSAL.build.module.Documentation.getDocumentationBaseDir();
    dir = new File(dir, "ReferenceManual");
    try {
      return new HelpFile(null, new File(dir, "GridNumbering.htm"));
    }
    catch (MalformedURLException ex) {
      return null;
    }
  }

  protected String getName(int row, int column) {
    String rowName = getName(row + vOff, vType, vLeading);
    String colName = getName(column + hOff, hType, hLeading);
    switch (first) {
      case 'H':
        return colName + sep + rowName;
      default:
        return rowName + sep + colName;
    }
  }

  public String locationName(Point pt) {
    int row = getRow(pt);
    int col = getColumn(pt);
    return getName(row, col);
  }

  public static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

  protected String getName(int rowOrColumn, char type, int leading) {
    String val = rowOrColumn < 0 ? "-" : "";
    rowOrColumn = Math.abs(rowOrColumn);
    switch (type) {
      case 'A': // Alphabetic
        do {
          val += ALPHABET.charAt(rowOrColumn % 26);
          rowOrColumn -= 26;
        }
        while (rowOrColumn >= 0);
        return val;
      default: // Numeric
        while (leading > 0 && rowOrColumn < Math.pow(10.0, (double) leading--)) {
          val += "0";
        }
        return val + rowOrColumn;
    }
  }

}
