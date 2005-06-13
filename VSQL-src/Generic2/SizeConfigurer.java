/*
 * $Id$
 * 
 * Copyright (c) 2005 by Rodney Kinney, Brent Easton
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License (LGPL) as published by
 * the Free Software Foundation.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, copies are available at
 * http://www.opensource.org.
 */

package Generic2;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

public class SizeConfigurer extends StringEnumConfigurer {

  public SizeConfigurer(String key, String name) {
    super(key, name, Symbol.NatoUnitSymbolSet.getSymbolSizes());
  }

  public JComboBox getComboBox() {
    return (JComboBox) new SizeComboBox();
  }

  public class SizeComboBox extends JComboBox {

    public SizeComboBox() {
      String[] s = Symbol.NatoUnitSymbolSet.getSymbolSizes();
      for (int i = 0; i < s.length; ++i) {
        addItem(s[i]);
      }
      SizeRenderer renderer = new SizeRenderer();
      setRenderer(renderer);
    }

    public SizeComboBox(ItemListener l) {
      this();
      addItemListener(l);
    }

    public SizeComboBox(ItemListener l, String sizeName) {
      this();
      setSelectedItem(sizeName);
      addItemListener(l);
    }

    public class SizeRenderer extends JLabel implements ListCellRenderer {

      public SizeRenderer() {
        setOpaque(true);
        setHorizontalAlignment(LEFT);
        setVerticalAlignment(CENTER);
      }

      /*
       * This method finds the image and text corresponding to the selected
       * value and returns the label, set up to display the text and image.
       */
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
          boolean cellHasFocus) {

        if (isSelected) {
          setBackground(list.getSelectionBackground());
          setForeground(list.getSelectionForeground());
        }
        else {
          setBackground(list.getBackground());
          setForeground(list.getForeground());
        }


        final int sample_w = 6;
        final int sample_h = 12;
        final int sample_g = 1;
        
        final int w = sample_w*6 + sample_g*5 + 1;
        final int h = sample_h+1;
        
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, w, h);
        g.setColor(Color.black);
        g.drawRect(0, 0, w-1, h-1);
        
        BufferedImage bi = Symbol.NatoUnitSymbolSet.buildSizeImage((String) value, sample_w, sample_h, sample_g);
        int x = (w/2) - (bi.getWidth()/2);
        g.drawImage(bi, x, 0, null);       
        
        ImageIcon icon = new ImageIcon(image);

        setIcon(icon);
        setText((String) value);
        this.setHorizontalTextPosition(SwingConstants.LEFT);
        this.setHorizontalAlignment(SwingConstants.RIGHT);
        setFont(list.getFont());

        return this;
      }
    }
  }
}