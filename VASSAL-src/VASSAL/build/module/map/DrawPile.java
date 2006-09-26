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
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.widget.CardSlot;
import VASSAL.build.widget.PieceSlot;
import VASSAL.command.*;
import VASSAL.configure.*;
import VASSAL.counters.*;
import VASSAL.tools.SequenceEncoder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.Vector;

public class DrawPile extends AbstractConfigurable implements Drawable, GameComponent, CommandEncoder, MouseListener {
  protected Map map;
  protected String id;
  protected Stack contents;
  protected Point pos = new Point();
  protected Dimension size = new Dimension(40, 40);
  protected int dragCount = 0;
  protected PieceMover mover;
  protected boolean shuffle = true;
  protected boolean faceDown = false;
  private String faceDownOption = ALWAYS;
  private String shuffleOption = ALWAYS;
  private boolean allowMultipleDraw = false;
  private boolean allowSelectDraw = false;
  private boolean reversible = false;
  private boolean drawOutline = true;
  private Color outlineColor = Color.black;
  private Vector nextDraw;

  private static final String HIDDEN_TO_ALL = "Yendor117";

  protected Action faceDownAction;

  protected JPopupMenu buildPopup() {
    JPopupMenu popup = new JPopupMenu();
    if (USE_MENU.equals(shuffleOption)) {
      popup.add(new AbstractAction("Shuffle") {
        public void actionPerformed(ActionEvent e) {
          GameModule.getGameModule().sendAndLog(shuffle());
          map.repaint();
        }
      }).setFont(MenuDisplayer.POPUP_MENU_FONT);
    }
    if (USE_MENU.equals(faceDownOption)) {
      faceDownAction = new AbstractAction(faceDown ? "Face up" : "Face down") {
        public void actionPerformed(ActionEvent e) {
          GameModule.getGameModule().sendAndLog(toggleFaceDown());
          map.repaint();
        }
      };
      popup.add(faceDownAction).setFont(MenuDisplayer.POPUP_MENU_FONT);
    }
    if (reversible) {
      popup.add(new AbstractAction("Reverse order") {
        public void actionPerformed(ActionEvent e) {
          GameModule.getGameModule().sendAndLog(reverse());
          map.repaint();
        }
      }).setFont(MenuDisplayer.POPUP_MENU_FONT);
    }
    if (allowMultipleDraw) {
      popup.add(new AbstractAction("Draw multiple cards") {
        public void actionPerformed(ActionEvent e) {
          promptForDragCount();
        }
      }).setFont(MenuDisplayer.POPUP_MENU_FONT);
    }
    if (allowSelectDraw) {
      popup.add(new AbstractAction("Draw specific cards") {
        public void actionPerformed(ActionEvent e) {
          promptForNextDraw();
        }
      }).setFont(MenuDisplayer.POPUP_MENU_FONT);
    }
    return popup.getComponentCount() > 0 ? popup : null;
  }

  private void promptForNextDraw() {
    final JDialog d = new JDialog((Frame)SwingUtilities.getAncestorOfClass(Frame.class, map.getView()),true);
    d.setTitle("Draw");
    d.getContentPane().setLayout(new BoxLayout(d.getContentPane(),BoxLayout.Y_AXIS));
    final String[] pieces = new String[contents.getPieceCount()];
    for (int i=0;i<pieces.length;++i) {
      pieces[i] = Decorator.getInnermost(contents.getPieceAt(i)).getName();
    }
    final JList list = new JList(pieces);
    list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    d.getContentPane().add(new JScrollPane(list));
    d.getContentPane().add(new JLabel("Select cards to draw"));
    d.getContentPane().add(new JLabel("Then click and drag from the deck."));
    Box box = Box.createHorizontalBox();
    JButton b = new JButton("Ok");
    b.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        nextDraw = new Vector();
        int[] selection = list.getSelectedIndices();
        for (int i=0;i<selection.length;++i) {
          nextDraw.addElement(contents.getPieceAt(selection[i]));
        }
        d.dispose();
      }
    });
    box.add(b);
    b = new JButton("Cancel");
    b.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        d.dispose();
      }
    });
    box.add(b);
    d.getContentPane().add(box);
    d.pack();
    d.setLocationRelativeTo(d.getOwner());
    d.setVisible(true);
  }

  public void addTo(Buildable b) {
    map = (Map) b;
    mover = new PieceMover();
    map.addDrawComponent(this);
    map.addLocalMouseListener(this);
    int count = 0;
    for (Enumeration e = GameModule.getGameModule().getComponents(Map.class); e.hasMoreElements();) {
      Map m = (Map) e.nextElement();
      for (Enumeration e2 = m.getComponents(DrawPile.class); e2.hasMoreElements();) {
        e2.nextElement();
        count++;
      }
    }
    // Our map doesn't yet appear in the GameModule
    for (Enumeration e = map.getComponents(DrawPile.class); e.hasMoreElements();) {
        e.nextElement();
        count++;
    }
    setId("Deck" + count);

    GameModule.getGameModule().addCommandEncoder(this);
    GameModule.getGameModule().getGameState().addGameComponent(this);
  }

  public void removeFrom(Buildable b) {
    if (map != b) {
      throw new IllegalBuildException("Parent is not " + b);
    }
    map.removeDrawComponent(this);
    GameModule.getGameModule().removeCommandEncoder(this);
    GameModule.getGameModule().getGameState().removeGameComponent(this);
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public final static String X_POSITION = "x";
  public final static String Y_POSITION = "y";
  public final static String WIDTH = "width";
  public final static String HEIGHT = "height";
  public static final String ALLOW_MULTIPLE = "allowMultiple";
  public static final String ALLOW_SELECT = "allowSelect";
  public static final String FACE_DOWN = "faceDown";
  public static final String SHUFFLE = "shuffle";
  public static final String REVERSIBLE = "reversible";
  public static final String DRAW = "draw";
  public static final String COLOR = "color";

  public static final String ALWAYS = "Always";
  public static final String NEVER = "Never";
  public static final String USE_MENU = "Via right-click Menu";

  public String[] getAttributeNames() {
    return new String[]{X_POSITION, Y_POSITION, WIDTH, HEIGHT, ALLOW_MULTIPLE, ALLOW_SELECT, FACE_DOWN, SHUFFLE, REVERSIBLE, DRAW, COLOR};
  }

  public String[] getAttributeDescriptions() {
    return new String[]{"X position",
                        "Y position",
                        "Width",
                        "Height",
                        "Allow Multiple Cards to be Drawn",
                        "Allow Specific Cards to be Drawn",
                        "Contents are Face-down",
                        "Re-shuffle",
                        "Reversible",
                        "Draw Outline when empty",
                        "Color"};
  }

  public static class Prompt extends StringEnum {
    public String[] getValidValues() {
      return new String[]{ALWAYS, NEVER, USE_MENU};
    }
  }

  public Class[] getAttributeTypes() {
    return new Class[]{Integer.class,
                       Integer.class,
                       Integer.class,
                       Integer.class,
                       Boolean.class,
                       Boolean.class,
                       Prompt.class,
                       Prompt.class,
                       Boolean.class,
                       Boolean.class,
                       Color.class};
  }

  public String getAttributeValueString(String key) {
    if (X_POSITION.equals(key)) {
      return "" + pos.x;
    }
    else if (Y_POSITION.equals(key)) {
      return "" + pos.y;
    }
    else if (WIDTH.equals(key)) {
      return "" + size.width;
    }
    else if (HEIGHT.equals(key)) {
      return "" + size.height;
    }
    else if (FACE_DOWN.equals(key)) {
      return faceDownOption;
    }
    else if (SHUFFLE.equals(key)) {
      return shuffleOption;
    }
    else if (REVERSIBLE.equals(key)) {
      return "" + reversible;
    }
    else if (ALLOW_MULTIPLE.equals(key)) {
      return "" + allowMultipleDraw;
    }
    else if (ALLOW_SELECT.equals(key)) {
      return "" + allowSelectDraw;
    }
    else if (DRAW.equals(key)) {
      return "" + drawOutline;
    }
    else if (COLOR.equals(key)) {
      return ColorConfigurer.colorToString(outlineColor);
    }
    else {
      return null;
    }
  }


  public void setAttribute(String key, Object value) {
    if (value == null) {
      return;
    }
    if (X_POSITION.equals(key)) {
      if (value instanceof String) {
        value = new Integer((String) value);
      }
      pos.x = ((Integer) value).intValue();
    }
    else if (Y_POSITION.equals(key)) {
      if (value instanceof String) {
        value = new Integer((String) value);
      }
      pos.y = ((Integer) value).intValue();
    }
    else if (WIDTH.equals(key)) {
      if (value instanceof String) {
        value = new Integer((String) value);
      }
      size.width = ((Integer) value).intValue();
    }
    else if (HEIGHT.equals(key)) {
      if (value instanceof String) {
        value = new Integer((String) value);
      }
      size.height = ((Integer) value).intValue();
    }
    else if (FACE_DOWN.equals(key)) {
      faceDownOption = (String) value;
      faceDown = !faceDownOption.equals(NEVER);
    }
    else if (SHUFFLE.equals(key)) {
      shuffleOption = (String) value;
    }
    else if (REVERSIBLE.equals(key)) {
      if (value instanceof Boolean) {
        reversible = Boolean.TRUE.equals(value);
      }
      else {
        reversible = "true".equals(value);
      }
    }
    else if (ALLOW_MULTIPLE.equals(key)) {
      if (value instanceof Boolean) {
        allowMultipleDraw = Boolean.TRUE.equals(value);
      }
      else {
        allowMultipleDraw = "true".equals(value);
      }
    }
    else if (ALLOW_SELECT.equals(key)) {
      if (value instanceof Boolean) {
        allowSelectDraw = Boolean.TRUE.equals(value);
      }
      else {
        allowSelectDraw = "true".equals(value);
      }
    }
    else if (DRAW.equals(key)) {
      if (value instanceof Boolean) {
        drawOutline = Boolean.TRUE.equals(value);
      }
      else {
        drawOutline = "true".equals(value);
      }
    }
    else if (COLOR.equals(key)) {
      if (value instanceof String) {
        value = ColorConfigurer.stringToColor((String) value);
      }
      outlineColor = (Color) value;
    }
  }

  public VisibilityCondition getAttributeVisibility(String name) {
    if (COLOR.equals(name)) {
      return new VisibilityCondition() {
      public boolean shouldBeVisible() {
        return drawOutline;
      }
    };
    }
    else {
      return null;
    }
  }

  /** Shuffle the contents of the Deck */
  public Command shuffle() {
    Vector indices = new Vector();
    for (int i = 0; i < contents.getPieceCount(); ++i) {
      indices.addElement(new Integer(i));
    }
    Vector newContents = new Vector();
    DragBuffer.getBuffer().clear();
    for (int count = contents.getPieceCount(); count > 0; --count) {
      int i = (int) (GameModule.getGameModule().getRNG().nextFloat()
        * indices.size());
      int index = ((Integer) indices.elementAt(i)).intValue();
      indices.removeElementAt(i);
      newContents.addElement(contents.getPieceAt(index));
    }
    return setContents(newContents.elements());
  }

  /** Set the contents of this Deck to an Enumeration of GamePieces */
  protected Command setContents(Enumeration e) {
    TrackPiece track = new TrackPiece(contents);
    contents.removeAll();
    while (e.hasMoreElements()) {
      contents.add((GamePiece) e.nextElement());
    }
    track.finalize();
    return track;
  }

  /** Reverse the order of the contents of the Deck */
  public Command reverse() {
    Vector v = new Vector();
    for (Enumeration e = contents.getPiecesInReverseOrder();
         e.hasMoreElements();) {
      v.addElement(e.nextElement());
    }
    return setContents(v.elements());
  }

  public Command toggleFaceDown() {
    faceDown = !faceDown;
    Command c = new NullCommand();
    for (Enumeration e = contents.getPieces(); e.hasMoreElements();) {
      GamePiece p = (GamePiece) e.nextElement();
      TrackPiece comm = new TrackPiece(p);
      p.setProperty(Obscurable.ID, faceDown ? HIDDEN_TO_ALL : null);
      comm.finalize();
      c.append(comm);
    }
    return c.append(new SetContents(this, contents.getId(), faceDown));
  }

  public Class[] getAllowableConfigureComponents() {
    return new Class[]{CardSlot.class};
  }

  public void draw(java.awt.Graphics g, Map map) {
    int count = 0;
    if (contents != null
      && (count = contents.getPieceCount()) > 0) {
      Point p = map.componentCoordinates(pos);
      GamePiece top = contents.topPiece();
      Rectangle r = top.selectionBounds();
      r.translate(pos.x - top.getPosition().x, pos.y - top.getPosition().y);
      r.setLocation(map.componentCoordinates(r.getLocation()));
      r.setSize((int) (map.getZoom() * r.width), (int) (map.getZoom() * r.height));
      count = count > 10 ? 10 : count;
      for (int i = 0; i < count - 1; ++i) {
        g.setColor(Color.white);
        g.fillRect(r.x + (int) (map.getZoom() * 2 * i),
                   r.y - (int) (map.getZoom() * 2 * i), r.width, r.height);
        g.setColor(Color.black);
        g.drawRect(r.x + (int) (map.getZoom() * 2 * i),
                   r.y - (int) (map.getZoom() * 2 * i), r.width, r.height);
      }
      if (faceDown && top.getProperty(Obscurable.ID) !=  null) {
        Object oldValue = top.getProperty(Obscurable.ID);
        top.setProperty(Obscurable.ID,HIDDEN_TO_ALL);
        top.draw(g, p.x + (int) (map.getZoom() * 2 * (count - 1)),
                 p.y - (int) (map.getZoom() * 2 * (count - 1)), map.getView(), map.getZoom());
        top.setProperty(Obscurable.ID,oldValue);
      }
      else {
      top.draw(g, p.x + (int) (map.getZoom() * 2 * (count - 1)),
               p.y - (int) (map.getZoom() * 2 * (count - 1)), map.getView(), map.getZoom());
      }
    }
    else {
      if (drawOutline) {
        Rectangle r = boundingBox();
        r.setLocation(map.componentCoordinates(r.getLocation()));
        r.setSize((int) (map.getZoom() * r.width), (int) (map.getZoom() * r.height));
        g.setColor(outlineColor);
        g.drawRect(r.x, r.y, r.width, r.height);
      }
    }
  }

  public Rectangle boundingBox() {
    Rectangle r = null;
    if (contents != null
      && contents.getPieceCount() > 0) {
      GamePiece p = contents.topPiece();
      r = new Rectangle(p.selectionBounds());
      r.translate(-p.getPosition().x + pos.x, -p.getPosition().y + pos.y);
      for (int i = 0,j = contents.getPieceCount(); i < j && i < 10; ++i) {
        r.setSize(r.width + 2, r.height + 2);
        r.y -= 2;
      }
    }
    else {
      r = new Rectangle(pos.x - size.width / 2, pos.y - size.height / 2, size.width, size.height);
    }
    return r;
  }

  /**
   * Add the given number of GamePieces to the Drag buffer
   */
  protected void addToDragBuffer(int count) {
    DragBuffer.getBuffer().clear();
    if (ALWAYS.equals(shuffleOption)) {
      Vector indices = new Vector();
      for (int i = 0; i < contents.getPieceCount(); ++i) {
        indices.addElement(new Integer(i));
      }
      while (count-- > 0) {
        int i = (int) (GameModule.getGameModule().getRNG().nextFloat()
          * indices.size());
        int index = ((Integer) indices.elementAt(i)).intValue();
        indices.removeElementAt(i);
        GamePiece p = contents.getPieceAt(index);
        if (p.getProperty(Obscurable.ID) != null) {
          p.setProperty(Obscurable.ID, GameModule.getGameModule().getUserId());
        }
        DragBuffer.getBuffer().add(p);
      }
    }
    else {
      Enumeration e = contents.getPiecesInReverseOrder();
      while (count-- > 0 && e.hasMoreElements()) {
        GamePiece p = (GamePiece) e.nextElement();
        if (p.getProperty(Obscurable.ID) != null) {
          p.setProperty(Obscurable.ID, GameModule.getUserId());
        }
        DragBuffer.getBuffer().add(p);
      }
    }
  }

  public void promptForDragCount() {
    while (true) {
      String s = JOptionPane.showInputDialog("Enter number to grab.\nThen click and drag to draw that number.");
      if (s != null) {
        try {
          dragCount = Integer.parseInt(s);
          dragCount = Math.min(dragCount, contents.getPieceCount());
          if (dragCount >= 0) {
            break;
          }
        }
        catch (NumberFormatException ex) {
        }
      }
    }
  }

  public void mousePressed(MouseEvent evt) {
    if (boundingBox().contains(evt.getPoint())
      && contents.getPieceCount() > 0) {
      if (!evt.isMetaDown()) {
        if (nextDraw != null) {
          DragBuffer.getBuffer().clear();
          for (Enumeration e = nextDraw.elements(); e.hasMoreElements();) {
            GamePiece p = (GamePiece) e.nextElement();
            if (p.getProperty(Obscurable.ID) != null) {
              p.setProperty(Obscurable.ID, GameModule.getUserId());
            }
            DragBuffer.getBuffer().add(p);
          }
        }
        else {
          if (dragCount == 0) {
            dragCount = 1;
          }
          addToDragBuffer(dragCount);
        }
      }
    }
    else {
      dragCount = 0;
      nextDraw = null;
    }
  }

  public void mouseReleased(MouseEvent evt) {
    if (boundingBox().contains(evt.getPoint())) {
      if (evt.isMetaDown()) {
        JPopupMenu popup = buildPopup();
        if (popup != null) {
          popup.show(map.getView(), evt.getX(), evt.getY());
        }
      }
      else {
        GamePiece p = map.findPiece(evt.getPoint(), PieceFinder.MOVABLE);
        if (p != null) {
          String oldContents = contents.getState();
          GameModule.getGameModule().sendAndLog
            (addToContents(p).append
             (new ChangePiece(contents.getId(),
                              oldContents,
                              contents.getState())));
        }
      }
    }
    if (!evt.isMetaDown()) {
      dragCount = 0;
    }
  }

  public Map getMap() {
    return map;
  }

  public Command addToContents(GamePiece p) {
    Command comm;
    if (p instanceof Stack) {
      Command c = new NullCommand();
      for (Enumeration e = ((Stack) p).getPieces();
           e.hasMoreElements();) {
        GamePiece sub = (GamePiece) e.nextElement();
        c = c.append(addToContents(sub));
      }
      Command c2 = new RemovePiece(p);
      c2.execute();
      comm = c.append(c2);
    }
    else if (ALWAYS.equals(faceDownOption)) {
      String oldState = p.getState();
      p.setProperty(Obscurable.ID, HIDDEN_TO_ALL);
      contents.add(p);
      comm = new ChangePiece(p.getId(),
                             oldState,
                             p.getState());
    }
    else if (NEVER.equals(faceDownOption)) {
      String oldState = p.getState();
      p.setProperty(Obscurable.ID, null);
      contents.add(p);
      comm = new ChangePiece(p.getId(),
                             oldState,
                             p.getState());
    }
    else {
      contents.add(p);
      comm = new NullCommand();
    }
    return comm;
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mouseClicked(MouseEvent e) {
  }

  public void setup(boolean gameStarting) {
    if (!gameStarting) {
      contents = null;
    }
    else if (contents == null) {
      contents = new Stack();
      Configurable[] c = getConfigureComponents();
      for (int i = 0; i < c.length; ++i) {
        GamePiece p = ((PieceSlot) c[i]).getPiece();
        p = ((AddPiece) GameModule.getGameModule().decode
          (GameModule.getGameModule().encode
           (new AddPiece(p)))).getTarget();
        p.setState(((PieceSlot) c[i]).getPiece().getState());
        if (faceDown) {
          p.setProperty(Obscurable.ID, HIDDEN_TO_ALL);
        }
        GameModule.getGameModule().getGameState().addPiece(p);
        contents.add(p);
      }
      GameModule.getGameModule().getGameState().addPiece(contents);
    }
  }

  public Command getRestoreCommand() {
    return new SetContents(this, contents == null ? "null" : contents.getId(), faceDown);
  }

  public static class SetContents extends Command {
    private DrawPile deck;
    private String contentsId;
    private boolean faceDown;

    public SetContents(DrawPile deck, String contentsId, boolean faceDown) {
      this.deck = deck;
      this.contentsId = contentsId;
      this.faceDown = faceDown;
    }

    public void executeCommand() {
      deck.contents = (Stack) GameModule.getGameModule().getGameState().getPieceForId(contentsId);
      deck.faceDown = faceDown;
    }

    public Command myUndoCommand() {
      return null;
    }
  }

  public Command decode(String s) {
    if (s.startsWith(getId() + '\t')) {
      SequenceEncoder.Decoder st = new SequenceEncoder.Decoder(s, '\t');
      st.nextToken();
      String contentsId = st.nextToken();
      boolean faceDown = st.hasMoreTokens() && "true".equals(st.nextToken());
      return new SetContents(this, contentsId, faceDown);
    }
    else {
      return null;
    }
  }

  public String encode(Command c) {
    if (c instanceof SetContents
      && ((SetContents) c).deck == this) {
      SequenceEncoder se = new SequenceEncoder(getId(), '\t');
      se.append(((SetContents) c).contentsId);
      se.append("" + ((SetContents) c).faceDown);
      return se.getValue();
    }
    else {
      return null;
    }
  }

  public VASSAL.build.module.documentation.HelpFile getHelpFile() {
    File dir = new File("docs");
    dir = new File(dir, "ReferenceManual");
    try {
      return new HelpFile(null, new File(dir, "Deck.htm"));
    }
    catch (MalformedURLException ex) {
      return null;
    }
  }

  public static String getConfigureTypeName() {
    return "Deck";
  }
}
