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

import VASSAL.configure.VisibilityCondition;
import VASSAL.tools.SequenceEncoder;

public class CounterTurnLevel extends TurnLevel {

  protected static final String START = "start";
  protected static final String INCR = "incr";
  protected static final String LOOP = "loop";
  protected static final String LOOP_LIMIT = "loopLimit";
  
  protected int incr = 1;
  protected boolean loop = false;
  protected int loopLimit = -1;
  
  public CounterTurnLevel() {
    super();
  }
  
  /*
   *  Reset counter to initial state
   */
  protected void reset() {
    super.reset();
    current = start;    
  }

  /* 
   * Generate the state of the level
   */
  protected String getState() {
    SequenceEncoder se = new SequenceEncoder(';');
    se.append(current);
    se.append(currentSubLevel);
    se.append(loop);
    se.append(loopLimit);
    for (int i = 0; i < getTurnLevelCount(); i++) {
      se.append(getTurnLevel(i).getState());
    }
    return se.getValue();
  }

  /* 
   * Set the state of the level
   */
  protected void setState(String code) {
    SequenceEncoder.Decoder sd = new SequenceEncoder.Decoder(code, ';');
    current = sd.nextInt(start);
    currentSubLevel = sd.nextInt(-1);
    loop = sd.nextBoolean(false);
    loopLimit = sd.nextInt(-1);
    for (int i = 0; i < getTurnLevelCount(); i++) {
      getTurnLevel(i).setState(sd.nextToken(""));
    }
  }

  protected String getValueString() {
    return current + "";
  }

  /* (non-Javadoc)
   * @see turn.TurnLevel#getLongestValueName()
   */
  protected String getLongestValueName() {
    return start < 10000 ? "9999" : start+"";
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
      current++;
      if (loop && current > loopLimit) {
        current = start;
        setRolledOver(true);
      }
    }
    
  }

  /* (non-Javadoc)
   * @see turn.TurnLevel#retreat()
   */
  protected void retreat() {
    // TODO Auto-generated method stub
    
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
    return new String[] { "Name:  ", "Start Value:  ", "Increment By:  ", "Loop?", "Loop at:  " };
  }

  public Class[] getAttributeTypes() {
    return new Class[] { String.class, Integer.class, Integer.class, Boolean.class, Integer.class };
  }

  public String[] getAttributeNames() {
    return new String[] { NAME, START, INCR, LOOP, LOOP_LIMIT };
  }

  public void setAttribute(String key, Object value) {

    if (START.equals(key)) {
      if (value instanceof String) {
        value = new Integer((String) value);
      }
      start = ((Integer) value).intValue();
      current = start;
    }
    else if (INCR.equals(key)) {
      if (value instanceof String) {
        value = new Integer((String) value);
      }
      incr = ((Integer) value).intValue();
    }
    else if (LOOP.equals(key)) {
      if (value instanceof String) {
        value = new Boolean((String) value);
      }
      loop = ((Boolean) value).booleanValue();
    }
    else if (LOOP_LIMIT.equals(key)) {
      if (value instanceof String) {
        value = new Integer((String) value);
      }
      loopLimit = ((Integer) value).intValue();
    }
    else {
      super.setAttribute(key, value);
    }
    
  }

  public String getAttributeValueString(String key) {
    if (START.equals(key)) {
      return start + "";
    }
    else if (INCR.equals(key)) {
      return incr + "";
    }
    else if (LOOP.equals(key)) {
      return loop + "";
    }
    else if (LOOP_LIMIT.equals(key)) {
      return loopLimit + "";
    }
    else
      return super.getAttributeValueString(key);
  }
  
  public static String getConfigureTypeName() {
    return "Counter";
  }
  
  public VisibilityCondition getAttributeVisibility(String name) {
    if (LOOP_LIMIT.equals(name)) {
      return loopCond;
    }
    else {
      return null;
    }
  }

  private VisibilityCondition loopCond = new VisibilityCondition() {
    public boolean shouldBeVisible() {
      return loop;
    }
  };
 }
