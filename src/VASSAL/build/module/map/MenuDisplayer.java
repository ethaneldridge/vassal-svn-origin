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

import VASSAL.build.Buildable;
import VASSAL.build.IllegalBuildException;
import VASSAL.build.module.Map;
import VASSAL.counters.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;

public class MenuDisplayer extends MouseAdapter implements Buildable {
  public static Font POPUP_MENU_FONT = new Font("Dialog", 0, 10);

  protected Map map;
  protected PieceFinder targetSelector;

  public void addTo(Buildable b) {
    targetSelector = createTargetSelector();
    map = (Map) b;
    map.addLocalMouseListener(this);
  }

  /**
   * Return a {@link PieceFinder} instance that will select a
   * {@link GamePiece} whose menu will be displayed when the
   * user clicks on the map
   * @return
   */
  protected PieceFinder createTargetSelector() {
    return new PieceFinder.PieceInStack() {
      public Object visitDeck(Deck d) {
        if (d.getShape().contains(pt)) {
          return d;
        }
        else {
          return null;
        }
      }
    };
  }

  public void add(Buildable b) {
    throw new IllegalBuildException("Cannot contain children");
  }

  public Element getBuildElement(Document doc) {
    return doc.createElement(getClass().getName());
  }

  public void build(Element e) {
  }

  public static JPopupMenu createPopup(GamePiece target) {
    JPopupMenu popup = new JPopupMenu();
    KeyCommand c[] = (KeyCommand[]) target.getProperty(Properties.KEY_COMMANDS);
    if (c != null) {
      List commands = new ArrayList();
      List strokes = new ArrayList();
      HashMap subMenus = new HashMap(); // Maps instances of KeyCommandSubMenu to corresponding JMenu
      HashMap commandNames = new HashMap(); // Maps name to a list of commands with that name
      for (int i = 0; i < c.length; ++i) {
        KeyStroke stroke = c[i].getKeyStroke();
        if (c[i] instanceof KeyCommandSubMenu) {
          commands.add(c[i]);
          JMenu subMenu = new JMenu(c[i].getName());
          subMenu.setFont(POPUP_MENU_FONT);
          subMenus.put(c[i],subMenu);
        }
        else {
          if (strokes.contains(stroke)) {
            KeyCommand command = (KeyCommand) commands.get(strokes.indexOf(stroke));
            if (command.getName().length() < c[i].getName().length()) {
              commands.set(strokes.indexOf(stroke), c[i]);
            }
          }
          else {
            if (stroke != null) {
              strokes.add(stroke);
            }
            commands.add(c[i]);
          }
        }
        if (c[i].getName() != null
          && c[i].getName().length() > 0) {
          List l = (List) commandNames.get(c[i].getName());
          if (l == null) {
            l = new ArrayList();
            commandNames.put(c[i],l);
          }
          l.add(c[i]);
        }
      }
      // Move commands from main menu into submenus
      for (Iterator it = subMenus.keySet().iterator(); it.hasNext();) {
        KeyCommandSubMenu menuCommand = (KeyCommandSubMenu)it.next();
        JMenu subMenu = (JMenu) subMenus.get(menuCommand);
        for (Iterator it2 = menuCommand.getCommands(); it2.hasNext();) {
          List matchingCommands = (List) commandNames.get(it2.next());
          for (Iterator it3 = matchingCommands.iterator(); it.hasNext();) {
            KeyCommand subCommand = (KeyCommand)it3.next();
            subMenu.add(subCommand).setFont(POPUP_MENU_FONT);
            commands.remove(subCommand);
          }
        }
      }
      for (Iterator it = commands.iterator();
           it.hasNext();) {
        KeyCommand command = (KeyCommand) it.next();
        JMenu subMenu = (JMenu) subMenus.get(command);
        if (subMenu == null) {
          popup.add(subMenu);
        }
        else {
          popup.add(command).setFont(POPUP_MENU_FONT);
        }
      }
    }
    return popup;
  }

  public void mouseReleased(MouseEvent e) {
    if (e.isMetaDown()) {
      final GamePiece p = map.findPiece(e.getPoint(), targetSelector);
      if (p != null
          && (e.isShiftDown()
          || !Boolean.TRUE.equals(p.getProperty(Properties.IMMOBILE)))) {
        JPopupMenu popup = createPopup(p);
        Point pt = map.componentCoordinates(e.getPoint());
        popup.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
          public void popupMenuCanceled
              (javax.swing.event.PopupMenuEvent evt) {
            map.repaint();
          }

          public void popupMenuWillBecomeInvisible
              (javax.swing.event.PopupMenuEvent evt) {
            KeyBuffer.getBuffer().add(p);
            map.repaint();
          }

          public void popupMenuWillBecomeVisible
              (javax.swing.event.PopupMenuEvent evt) {
          }
        });
        KeyBuffer.getBuffer().clear();
        popup.show(map.getView(), pt.x, pt.y);
        e.consume();
      }
    }
  }
}
