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
 
package Generic2;

import VASSAL.build.AutoConfigurable;
import VASSAL.configure.Configurer;
import VASSAL.configure.ConfigurerFactory;
import VASSAL.tools.SequenceEncoder;

public class SymbolItemInstance extends ItemInstance {

  public static final String SIZE = "size";
  public static final String SYMBOL1 = "symbol1";
  public static final String SYMBOL2 = "symbol2";
  
  protected String size;
  protected String symbol1;
  protected String symbol2;
  
  public SymbolItemInstance() {
    super();
  }
  
  public SymbolItemInstance(String nam, String typ, String loc, String sz, String s1, String s2) {
    super(nam, typ, loc);
    setSize(sz);
    setSymbol1(s1);
    setSymbol2(s2);
   }

  public SymbolItemInstance(String code, ImageDefn defn) {
    super(defn);
    decode(code);
  }
  
  public String encode() {
    SequenceEncoder se = new SequenceEncoder(';');
    se.append(getType());
    se.append(getName());
    se.append(getLocation());
    se.append(getFgColor().encode());
    se.append(getBgColor().encode());
    se.append(getSize());
    se.append(getSymbol1());
    se.append(getSymbol2());
    return se.getValue();
  }
  
  public void decode(String code) {
    SequenceEncoder.Decoder sd = new SequenceEncoder.Decoder(code, ';');
    setType(sd.nextToken(""));
    setName(sd.nextToken(""));
    setLocation(sd.nextToken(""));
    setFgColor(new ColorSwatch(sd.nextToken("")));
    setBgColor(new ColorSwatch(sd.nextToken("")));
    setSize(sd.nextToken(""));
    setSymbol1(sd.nextToken(""));
    setSymbol2(sd.nextToken(""));
  }
  
  public void setSize(String size) {
    this.size = size;
  }

  public String getSize() {
    return size;
  }

  public void setSymbol1(String symbol1) {
    this.symbol1 = symbol1;
  }

  public String getSymbol1() {
    return symbol1;
  }

  public void setSymbol2(String symbol2) {
    this.symbol2 = symbol2;
  }

  public String getSymbol2() {
    return symbol2;
  }

  public String[] getAttributeDescriptions() {
    return new String[] { "Unit Size:  ", "1st Symbol:  ", "2nd Symbol:  ", "Symbol Color:  ", "Background Color:  " };
  }

  public Class[] getAttributeTypes() {
    return new Class[] { SizeConfig.class, Symbol1Config.class, Symbol2Config.class, FgColorSwatchConfig.class, BgColorSwatchConfig.class, };
  }
  
  public String[] getAttributeNames() {
    return new String[] { SIZE, SYMBOL1, SYMBOL2, FG_COLOR, BG_COLOR };
  }

  public void setAttribute(String key, Object value) {
    if (SIZE.equals(key)) {
      size = (String) value;
    }
    else if (SYMBOL1.equals(key)) {
      symbol1 = (String) value; 
    }
    else if (SYMBOL2.equals(key)) {
      symbol2 = (String) value;
    }
    else if (FG_COLOR.equals(key)) {
      if (value instanceof String) {
        value = new ColorSwatch((String) value);
      }
      fgColor = (ColorSwatch) value;
    }
    else if (BG_COLOR.equals(key)) {
        if (value instanceof String) {
          value = new ColorSwatch((String) value);
        }
        bgColor = (ColorSwatch) value;
    }
    if (myConfig != null) {
      myConfig.rebuildViz();
    }

  }

  public String getAttributeValueString(String key) {
    if (SIZE.equals(key)) {
      return size;
    }
    else if (SYMBOL1.equals(key)) {
      return symbol1;
    }
    else if (SYMBOL2.equals(key)) {
      return symbol2;
    }
    else if (FG_COLOR.equals(key)) {
      return fgColor.encode();
    }
    else if (BG_COLOR.equals(key)) {
      return bgColor.encode();
    }
    else
      return null;
  }
  
  public static class SizeConfig implements ConfigurerFactory {
    public Configurer getConfigurer(AutoConfigurable c, String key, String name) {
      return new SizeConfigurer(key, name);
    }
  }
  
  public static class Symbol1Config implements ConfigurerFactory {
    public Configurer getConfigurer(AutoConfigurable c, String key, String name) {
      return new SymbolConfigurer(key, name);
    }
  }

  public static class Symbol2Config implements ConfigurerFactory {
    public Configurer getConfigurer(AutoConfigurable c, String key, String name) {
      return new SymbolConfigurer(key, name);
    }
  }
  
  public static class BgColorSwatchConfig implements ConfigurerFactory {
    public Configurer getConfigurer(AutoConfigurable c, String key, String name) {
      return new ColorSwatchConfigurer(key, name, ((SymbolItemInstance) c).getBgColor());
    }
  }
  
  public static class FgColorSwatchConfig implements ConfigurerFactory {
    public Configurer getConfigurer(AutoConfigurable c, String key, String name) {
      return new ColorSwatchConfigurer(key, name, ((SymbolItemInstance) c).getFgColor());
    }
  }
  
}
