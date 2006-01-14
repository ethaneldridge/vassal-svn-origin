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
package VASSAL.build.module.documentation;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import VASSAL.build.widget.HtmlChart;
import VASSAL.build.widget.HtmlChart.XTMLEditorKit;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * A Window that displays HTML content, with navigation
 */
public class HelpWindow extends JFrame implements HyperlinkListener {
  private JEditorPane pane;

  public HelpWindow(String title, URL contents) {
    super(title);
    setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    pane = new JEditorPane();
    pane.setEditable(false);
    pane.addHyperlinkListener(this);
    
    /*
     * Allow <src> tag to display images from the module DataArchive 
     * where no pathname included in the image name.
     */
    pane.setContentType("text/html");
    XTMLEditorKit myHTMLEditorKit = (new HtmlChart()).new XTMLEditorKit();
    pane.setEditorKit(myHTMLEditorKit);
    
    JScrollPane scroll = new JScrollPane(pane);
    getContentPane().add(scroll);
    update(contents);
    pack();
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    int width = Math.max(d.width / 2, getSize().width);
    int height = Math.max(d.height / 2, getSize().height);
    width = Math.min(width, d.width * 2 / 3);
    height = Math.min(height, d.height * 2 / 3);
    setSize(width, height);
    setLocation(d.width / 2 - width / 2, 0);
  }

  public void hyperlinkUpdate(HyperlinkEvent e) {
    if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
      if (e.getURL() != null) {
        update(e.getURL());
      }
    }
  }

  public void update(URL contents) {
    if (contents != null) {
      try {
        pane.setPage(contents);
      }
      catch (IOException e) {
        pane.setText("Unable to read "+contents);
      }
    }
    else {
      pane.setText("");
    }
  }

}
