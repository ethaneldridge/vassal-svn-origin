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
package VASSAL.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Encodes a set of properties into a String, without using a '\n' character
 */
public class PropertiesEncoder {
  private Properties prop;
  private String stringValue;

  public PropertiesEncoder(Properties prop) {
    this.prop = prop;
    stringValue = encode(prop);
  }

  public PropertiesEncoder(String stringValue) throws IOException {
    this.stringValue = stringValue;
    prop = decode(stringValue);
  }

  private String encode(Properties p) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      p.store(out, null);
      // Strip away comments
      String s = new String(out.toByteArray(),"UTF-8");
      StringTokenizer st = new StringTokenizer(s, "\n\r", false);
      SequenceEncoder se = new SequenceEncoder('|');
      while (st.hasMoreTokens()) {
        String token = st.nextToken();
        if (!token.startsWith("#")) {
          se.append(token);
        }
      }
      return se.getValue();
    }
    catch (IOException e) {
      e.printStackTrace();
      return "";
    }
  }

  private Properties decode(String s) throws IOException {
    Properties p = new Properties();
    SequenceEncoder.Decoder st = new SequenceEncoder.Decoder(s,'|');
    StringBuffer buffer = new StringBuffer();
    while (st.hasMoreTokens()) {
      buffer.append(st.nextToken());
      if (st.hasMoreTokens()) {
        buffer.append('\n');
      }
    }
    ByteArrayInputStream in = new ByteArrayInputStream(buffer.toString().getBytes("UTF-8"));
    p.load(in);
    return p;
  }


  public Properties getProperties() {
    return prop;
  }

  public String getStringValue() {
    return stringValue;
  }
}
