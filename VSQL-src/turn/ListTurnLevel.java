/*
 * $Id$
 *
 * Copyright (c) 2005 by Rodney Kinney, Brent Easton
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
 
package turn;

import javax.swing.JComponent;

import VASSAL.configure.StringArrayConfigurer;
import VASSAL.tools.SequenceEncoder;

public class ListTurnLevel extends TurnLevel {

  protected static final String LIST = "list";
  
  protected int first = 0;
  protected String[] list = new String[0];
  protected boolean[] active = new boolean[0]; 
  
  public ListTurnLevel() {
    super();
  }
  
  /*
   *  Reset counter to initial state
   */
  protected void reset() {
    super.reset();
    for (int i = 0; i < active.length; i++) {
      active[i] = true;
    }
    setLow();
  }
  
  protected void setLow() {
    current = 0;
    first = 0;
  }
  
  protected void setHigh() {
    current = first;
    current--;
    if (current < 0) {
      current = list.length-1;
    }
  }

  /* 
   * Generate the state of the level
   */
  protected String getState() {
    SequenceEncoder se = new SequenceEncoder(';');
    se.append(current);
    se.append(currentSubLevel);
    se.append(first);
    String s[] = new String[active.length];
    for (int i=0; i < s.length; i++) {
      s[i] = active[i] + "";
    }
    se.append(s);
    return se.getValue();
  }

  /* 
   * Set the state of the level
   */
  protected void setState(String code) {
    SequenceEncoder.Decoder sd = new SequenceEncoder.Decoder(code, ';');
    current = sd.nextInt(start);
    currentSubLevel = sd.nextInt(-1);
    first = sd.nextInt(0);
    
    String[] s = sd.nextStringArray(0);
    active = new boolean[s.length];
    for (int i=0; i < s.length; i++) {
      active[i] = s[i].equals("true");
    }
  }

  protected String getValueString() {
    if (current >= 0 && current <= (list.length-1)) {
      return list[current];
    }
    else {
      return "";
    }
  }

  /* (non-Javadoc)
   * @see turn.TurnLevel#getLongestValueName()
   */
  protected String getLongestValueName() {
    String s = "X";
    for (int i = 0; i < list.length; i++) {
      if (list[i].length() > s.length()) {
        s = list[i];
      }
    }
    return s;
  }

  /* 
   * Advance this level.
   * 1. If there are any sub-levels, Advance the current sub-level first.
   * 2. If the sublevels roll over, then advance the counter
   * 3. If LOOP is reached, roll over the counter  
   */
  protected void advance() {
    super.advance();

    if (getTurnLevelCount() == 0 || (getTurnLevelCount() > 0 && hasSubLevelRolledOver())) {
      int idx = current;
      boolean done = false;
      for (int i = 0; i < list.length && !done; i++) {
        idx++;
        if (idx >= list.length) {
          idx = 0;
        }
        if (idx == first) {
          rolledOver = true;
        }
        done = active[idx];
      }
      current = idx;
    }
    
  }

  protected void retreat() {
    super.retreat();

    if (getTurnLevelCount() == 0 || (getTurnLevelCount() > 0 && hasSubLevelRolledOver())) {
      int idx = current;
      boolean done = false;
      for (int i = 0; i < list.length && !done; i++) {
        if (idx == first) {
          rolledOver = true;
        }
        idx--;
        if (idx < 0) {
          idx = list.length-1;
        }
        done = active[idx];
      }
      current = idx;
    }    
  }

  /* (non-Javadoc)
   * @see turn.TurnLevel#getSetControls()
   */
  protected JComponent getSetControls() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see turn.TurnLevel#toggleConfigVisibility()
   */
  protected void toggleConfigVisibility() {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see turn.TurnLevel#setConfigVisibility(boolean)
   */
  protected void setConfigVisibility(boolean b) {
    // TODO Auto-generated method stub
    
  }

  public String[] getAttributeDescriptions() {
    String a[] = super.getAttributeDescriptions();
    String b[] = new String[] { "List:  " };
    String c[]= new String[a.length + b.length];
    System.arraycopy(a, 0, c, 0, a.length);
    System.arraycopy(b, 0, c, a.length, b.length);
    return c;
  }

  public Class[] getAttributeTypes() {
    Class a[] = super.getAttributeTypes();
    Class b[] = new Class[] { String[].class };
    Class c[]= new Class[a.length + b.length];
    System.arraycopy(a, 0, c, 0, a.length);
    System.arraycopy(b, 0, c, a.length, b.length);
    return c;
  }

  public String[] getAttributeNames() {
    String a[] = super.getAttributeNames();
    String b[] = new String[] { LIST };
    String c[]= new String[a.length + b.length];
    System.arraycopy(a, 0, c, 0, a.length);
    System.arraycopy(b, 0, c, a.length, b.length);
    return c;    
  }

  public void setAttribute(String key, Object value) {

    if (LIST.equals(key)) {
      if (value instanceof String) {
        value = StringArrayConfigurer.stringToArray((String) value);
      }
      list = ((String[]) value);
      active = new boolean[list.length];
      for (int i = 0; i < active.length; i++) {
        active[i] = true;
      }
    }
    else {
      super.setAttribute(key, value);
    }
    
  }

  public String getAttributeValueString(String key) {
    if (LIST.equals(key)) {
      return StringArrayConfigurer.arrayToString(list);
    }
    else
      return super.getAttributeValueString(key);
  }
  
  public static String getConfigureTypeName() {
    return "List";
  }

 }
