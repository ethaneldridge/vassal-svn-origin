/*
 * $Id$
 *
 * Copyright (c) 2000-2005 by Rodney Kinney, Brent Easton
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
 
 package VSQL;

import VASSAL.counters.GamePiece;
import VASSAL.counters.Hideable;
 
 public class VSQLHideable extends Hideable {
   
   public VSQLHideable() {
     super();
   }
   
   public VSQLHideable(String type, GamePiece p) {
     super(type, p);
   }
   
   public Object getProperty(Object key) {
     if ("Location".equals(key)) {
       if (invisibleToMe() || invisibleToOthers()) {
         return new Boolean(true);
       }
       else {
         return null;
       }
     }
     else {
       return super.getProperty(key);
     }
   }
   
 }