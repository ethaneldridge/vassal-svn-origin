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

package VASSAL.build.module.gamepieceimage;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import VASSAL.configure.Configurer;
import VASSAL.configure.StringArrayConfigurer;

/**
 * Controls for configuring an individual ItemInstance  
 */
public class InstanceConfigurer extends Configurer {

  protected GamePieceImage defn;
  protected Box visBox;
  protected Visualizer visualizer = new Visualizer();
  protected JPanel panel;
  //protected TextPanel itemPanel;
  protected SymbolPanel symbolPanel;
  protected InstanceConfigurer me;

  protected InstanceConfigurer() {
    super(null, null);
    me = this;
  }

  protected InstanceConfigurer(String key, String name, GamePieceImage defn) {
    super(key, name);
    this.defn = defn;
    setValue(defn.getInstances());
    me = this;
  }

  public String getValueString() {
    return PropertiesToString((ArrayList) value);
  }
  
  public ArrayList getValueArrayList() {
    return (ArrayList) getValue();
  }

  public void setValue(String s) {
    setValue(StringToProperties(s, defn));
    if (symbolPanel != null) {
      symbolPanel.reset();
    }
  }
  
  public Component getControls() {
    if (panel == null) {

      panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

      Box filler = Box.createHorizontalBox();
      filler.setPreferredSize(new Dimension(50, 10));
      panel.add(filler);

      visBox = Box.createHorizontalBox();
      visBox.setAlignmentX(Box.CENTER_ALIGNMENT);
      visualizer = new Visualizer(defn);
      visBox.add(new JScrollPane(visualizer));
      panel.add(visBox);

      filler = Box.createHorizontalBox();
      filler.setPreferredSize(new Dimension(50, 10));
      panel.add(filler);

      symbolPanel = new SymbolPanel();
      panel.add(symbolPanel);

    }
    
    return panel;
  }

  public static String PropertiesToString(ArrayList props) {
    String[] p = new String[props.size()];
    Iterator e = props.iterator();
    int i = 0;
    while (e.hasNext()) {
      ItemInstance prop = (ItemInstance) e.next();
      p[i++] = prop.encode();
    }
    return StringArrayConfigurer.arrayToString(p);
  }
  
  public static ArrayList StringToProperties(String s, GamePieceImage defn) {
    ArrayList props = new ArrayList();
    String[] p = StringArrayConfigurer.stringToArray(s);
    for (int i = 0; i < p.length; i++) {
      if (p[i].startsWith(SymbolItem.TYPE)) {
        props.add(new SymbolItemInstance(p[i], defn));
      }
      else if (p[i].startsWith(TextBoxItem.TYPE)) {
        props.add(new TextBoxItemInstance(p[i], defn));
      }
      else if (p[i].startsWith(TextItem.TYPE)) {
        props.add(new TextItemInstance(p[i], defn));
      }
      else if (p[i].startsWith(ShapeItem.TYPE)) {
        props.add(new ShapeItemInstance(p[i], defn));
      }
      else if (p[i].startsWith(ImageItem.TYPE)) {
        props.add(new ImageItemInstance(p[i], defn));
      }
    }
    return props;
  }
  
  public void refresh() {
    if (symbolPanel != null) {
      symbolPanel.refresh();
    }
    visualizer.rebuild();
  }
  
  protected class SymbolPanel extends JPanel {

    protected JTable table;
    protected AbstractTableModel model;
    protected JScrollPane scrollPane;
    protected JButton addSymbolBtn, addTextBtn, remBtn;
    protected JPanel mainPanel;
    protected JPanel detailPanel;
    protected Component detailControls;
    protected int currentDetail;
    protected static final int NO_CURRENT_ITEM = -1;

    final int NAME_COL = 0;
    final int TYPE_COL = 1;
    final int LOC_COL = 2;
    final int MAX_COL = 2;
    
    public SymbolPanel() {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

      mainPanel = new JPanel();
      mainPanel.setBorder(BorderFactory.createLineBorder(Color.black));
      mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

      Box box = Box.createHorizontalBox();
      box.add(new JLabel("Items"));
      mainPanel.add(box);

      model = new SymbolTableModel();
      table = new JTable(model);
      table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      if (getValueArrayList() != null && getValueArrayList().size() > 0) {
        table.getSelectionModel().setSelectionInterval(0, 0);
      }
      ListSelectionModel rowSM = table.getSelectionModel();
      rowSM.addListSelectionListener(new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
          if (e.getValueIsAdjusting()) return;

          ListSelectionModel lsm = (ListSelectionModel) e.getSource();
          if (lsm.isSelectionEmpty()) {
            showItem(NO_CURRENT_ITEM);
          }
          else {
            int selectedRow = lsm.getMinSelectionIndex();
            showItem(selectedRow);
          }
        }
      });
      
      scrollPane = new JScrollPane(table);
      table.setPreferredScrollableViewportSize(new Dimension(300, 100));
      mainPanel.add(scrollPane);
      
      detailPanel = new JPanel();
      mainPanel.add(new JScrollPane(detailPanel));

      add(mainPanel);
      
      showItem(0);

    }
    
    protected void showItem(int itemNo) {

      if (detailControls != null) {
        detailPanel.remove(detailControls);
        detailControls = null;
        currentDetail = NO_CURRENT_ITEM;
      }

      int count = getValueArrayList().size();
      
      if (itemNo != NO_CURRENT_ITEM && count > 0 && itemNo < count) {
        ItemInstance instance = (ItemInstance) getValueArrayList().get(itemNo);
        instance.setConfig(me);
        Configurer c = instance.getConfigurer();
        detailControls = c.getControls();
        detailPanel.add(detailControls);
        currentDetail = itemNo;
      }

      reshow();
    }

    public void reset() {
      showItem(currentDetail);
    }
    
    public void reshow() {

      repack();
      detailPanel.repaint();

    }

    public void refresh() {
      showItem(currentDetail);
      reshow();
    }
    
    protected void repack() {

      Window w = SwingUtilities.getWindowAncestor(panel);
      if (w != null) {
        w.pack();
      }
    }
    
    class SymbolTableModel extends AbstractTableModel {

      private String[] columnNames = new String[] { "Name", "Type", "Position" };

      public int getColumnCount() {
        return columnNames.length;
      }

      public int getRowCount() {
        return getValueArrayList() == null ? 0 : getValueArrayList().size();
      }

      public String getColumnName(int col) {
        return columnNames[col];
      }

      public Object getValueAt(int row, int col) {
        if (col == NAME_COL) {
          return ((ItemInstance) getValueArrayList().get(row)).getName();
        }
        else if (col == TYPE_COL) {
          return ((ItemInstance) getValueArrayList().get(row)).getItem().getDisplayName();
        }
        else if (col == LOC_COL) {
          return ((ItemInstance) getValueArrayList().get(row)).getLocation();
        }
        else
          return null;
      }

      public Class getColumnClass(int col) {
           return String.class;
      }

      public boolean isCellEditable(int row, int col) {
        return false;
      }

      public void setValueAt(Object value, int row, int col) {

//        fireTableCellUpdated(row, col);
//        visualizer.rebuild();
      }

    }
  }



  public void rebuildViz() {
    if (visualizer != null) {
      visualizer.rebuild();
    }
  }
  
  public void repack() {
    if (panel != null) { 
      Window w = SwingUtilities.getWindowAncestor(panel);
      if (w != null) {
        w.pack();
      }
    }
    rebuildViz();    
  }

}
