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
package VASSAL.build;

import VASSAL.build.module.*;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.command.Logger;
import VASSAL.configure.DirectoryConfigurer;
import VASSAL.preferences.Prefs;
import VASSAL.tools.ArchiveWriter;
import VASSAL.tools.DataArchive;
import VASSAL.tools.KeyStrokeListener;
import VASSAL.tools.KeyStrokeSource;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * The GameModule class is the base class for a VASSAL module.  It is
 * the root of the {@link Buildable} containment hierarchy.
 * Components which are added directly to the GameModule are contained
 * in the <code>VASSAL.build.module</code> package
 *
 * It is a singleton, and contains access points for many other classes,
 * such as {@link DataArchive}, {@link ServerConnection}, {@link Logger},
 * and {@link Prefs} */
public abstract class GameModule extends AbstractConfigurable implements CommandEncoder {
  protected static final String DEFAULT_NAME = "Unnamed module";
  public static final String MODULE_NAME = "name";
  public static final String MODULE_VERSION = "version";
  public static final String VASSAL_VERSION_CREATED = "VassalVersion";
  /** The System property of this name will return a version identifier for the version of VASSAL being run */
  public static final String VASSAL_VERSION_RUNNING = "runningVassalVersion";

  private static GameModule theModule;

  protected String moduleVersion = "0.0";
  protected String vassalVersionCreated = "0.0";
  protected String gameName = DEFAULT_NAME;
  protected String lastSavedConfiguration;
  protected JFileChooser fileChooser;
  protected FileDialog fileDialog;

  protected JTextField status;
  protected JPanel chatPanel = new JPanel();

  private JToolBar toolBar = new JToolBar();
  private JMenu fileMenu = new JMenu("File");

  protected GameState theState;
  protected DataArchive archive;
  protected Prefs preferences;
  protected Prefs globalPrefs;
  protected Logger logger;
  protected Chatter chat;
  protected Random RNG;

  protected JFrame frame = new JFrame();

  protected Vector keyStrokeSources = new Vector();
  protected Vector keyStrokeListeners = new Vector();
  protected CommandEncoder[] commandEncoders = new CommandEncoder[0];

  /**
   * @return the top-level frame of the controls window
   */
  public JFrame getFrame() {
    return frame;
  }

  protected GameModule(DataArchive archive) {
    frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    this.archive = archive;
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        quit();
      }
    });

    frame.getContentPane().setLayout(new BorderLayout());
    frame.setJMenuBar(new JMenuBar());

    fileMenu.setMnemonic('F');
    frame.getJMenuBar().add(fileMenu);

    status = new JTextField();
    status.setEditable(false);
    frame.getContentPane().add(status, BorderLayout.NORTH);
    chatPanel.setLayout(new BorderLayout());
    chatPanel.add("North", toolBar);
    addKeyStrokeSource
      (new KeyStrokeSource
        (toolBar,
         JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT));
    frame.getContentPane().add(chatPanel, BorderLayout.CENTER);
  }

  /**
   * Initialize the module
   */
  protected abstract void build();

  public void setAttribute(String name, Object value) {
    if (MODULE_NAME.equals(name)) {
      gameName = (String) value;
      setConfigureName(gameName);
    }
    else if (MODULE_VERSION.equals(name)) {
      moduleVersion = (String) value;
    }
    else if (VASSAL_VERSION_CREATED.equals(name)) {
      vassalVersionCreated = (String) value;
      String runningVersion = System.getProperty(VASSAL_VERSION_RUNNING);
      if (compareVersions(vassalVersionCreated, runningVersion) > 0) {
        javax.swing.JOptionPane.showMessageDialog
          (null,
           "This module was created using version " + value
           + " of the VASSAL engine\nYou are using version "
           + runningVersion + "\nIt's recommended you upgrade to the latest version of the VASSAL engine.",
           "Older version in use",
           javax.swing.JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  public String getAttributeValueString(String name) {
    if (MODULE_NAME.equals(name)) {
      return gameName;
    }
    else if (MODULE_VERSION.equals(name)) {
      return moduleVersion;
    }
    else if (VASSAL_VERSION_CREATED.equals(name)) {
      return vassalVersionCreated;
    }
    else if (VASSAL_VERSION_RUNNING.equals(name)) {
      return System.getProperty(VASSAL_VERSION_RUNNING);
    }
    return null;
  }

  /**
   *
   * A valid verson format is "w.x.y[bz]", where
   * 'w','x','y', and 'z' are integers.
   * @return a negative number if <code>v2</code> is a later version
   * the <code>v1</code>, a positive number if an earlier version,
   * or zero if the versions are the same.
   *
   */
  public static int compareVersions(String v1, String v2) {
    try {
      int beta1 = v1.indexOf("b");
      int beta2 = v2.indexOf("b");
      if (beta1 > 0) {
        if (beta2 > 0) {
          return compareVersions(v1.substring(0, beta1), v2.substring(0, beta2)) < 0 ?
            -1 : Integer.parseInt(v1.substring(beta1 + 1))
            - Integer.parseInt(v2.substring(beta2 + 1));
        }
        else {
          return compareVersions(v1.substring(0, beta1), v2)
            > 0 ? 1 : -1;
        }
      }
      else if (beta2 > 0) {
        return compareVersions(v1, v2.substring(0, beta2))
          < 0 ? -1 : 1;
      }
      else {
        StringTokenizer s1 = new StringTokenizer(v1, ".");
        StringTokenizer s2 = new StringTokenizer(v2, ".");
        while (s1.hasMoreTokens()
          && s2.hasMoreTokens()) {
          int comp = Integer.parseInt(s1.nextToken())
            - Integer.parseInt(s2.nextToken());
          if (comp != 0) {
            return comp;
          }
        }
        if (s1.hasMoreTokens()) {
          return 1;
        }
        else if (s2.hasMoreTokens()) {
          return -1;
        }
        else {
          return 0;
        }
      }
    }
    catch (NumberFormatException ex) {
      System.err.println("Invalid version format :" + v1 + ", " + v2);
      return 0;
    }
  }

  public void addTo(Buildable b) {
    throw new IllegalBuildException("Module cannot be contained");
  }

  public static String getConfigureTypeName() {
    return "Module";
  }

  public void removeFrom(Buildable parent) {
    throw new IllegalBuildException("Module cannot be contained");
  }

  public HelpFile getHelpFile() {
    File dir = new File("docs");
    dir = new File(dir, "ReferenceManual");
    try {
      return new HelpFile(null, new File(dir, "GameModule.htm"));
    }
    catch (MalformedURLException ex) {
      return null;
    }
  }

  public String[] getAttributeNames() {
    return new String[]{MODULE_NAME, MODULE_VERSION, VASSAL_VERSION_CREATED};
  }

  public String[] getAttributeDescriptions() {
    return new String[]{"Game Name", "Version No."};
  }

  public Class[] getAttributeTypes() {
    return new Class[]{String.class, String.class};
  }

  public Class[] getAllowableConfigureComponents() {
    Class[] c = {Map.class, PieceWindow.class, DiceButton.class,
                 ChartWindow.class, PrivateMap.class, PlayerHand.class, NotesWindow.class};
    return c;
  }

  /**
   * The GameModule acts as the mediator for hotkey events.
   *
   * Components that wish to fire hotkey events when they have the
   * focus should register themselves using this method.  These events will be
   * forwarded to all listeners that have registered themselves with {@link #addKeyStrokeListener}
   */
  public void addKeyStrokeSource(KeyStrokeSource src) {
    keyStrokeSources.addElement(src);
    for (int i = 0; i < keyStrokeListeners.size(); ++i) {
      ((KeyStrokeListener) keyStrokeListeners.elementAt(i))
        .addKeyStrokeSource(src);
    }
  }

  /**
   * The GameModule acts as the mediator for hotkey events.
   *
   * Objects that react to hotkey events should register themselves
   * using this method.  Any component that has been registered with {@link #addKeyStrokeSource}
   * will forward hotkey events to listeners registered with this method.
   */
  public void addKeyStrokeListener(KeyStrokeListener l) {
    keyStrokeListeners.addElement(l);
    for (int i = 0; i < keyStrokeSources.size(); ++i) {
      l.addKeyStrokeSource
        ((KeyStrokeSource) keyStrokeSources.elementAt(i));
    }
  }

  /**
   * @return the name of the game for this module
   */
  public String getGameName() {
    return gameName;
  }

  public String getGameVersion() {
    return moduleVersion;
  }

  /** The {@link Prefs} key for the user's real name */
  public static final String REAL_NAME = "RealName";
  /** The {@link Prefs} key for the user's secret name */
  public static final String SECRET_NAME = "SecretName";
  /** The {@link Prefs} key for the user's personal info */
  public static final String PERSONAL_INFO = "Profile";

  /**
   * @return the preferences for this module
   */
  public Prefs getPrefs() {
    if (preferences == null) {
      (new Prefs(System.getProperty("user.dir")
                 + java.io.File.separator + "Preferences")).addTo(this);
    }
    return preferences;
  }

  /**
   * A set of preferences that applies to all modules
   * @return
   */
  public Prefs getGlobalPrefs() {
    getPrefs();
    return globalPrefs;
  }

  /**
   * This method adds a {@link CommandEncoder} to the list of objects
   * that will attempt to decode/encode a command
   *
   * @see #decode
   * @see #encode
   */
  public void addCommandEncoder(CommandEncoder ce) {
    CommandEncoder[] oldValue = commandEncoders;
    commandEncoders = new CommandEncoder[oldValue.length+1];
    System.arraycopy(oldValue,0,commandEncoders,0,oldValue.length);
    commandEncoders[oldValue.length] = ce;
  }

  /**
   * This method removes a {@link CommandEncoder} from the list of objects
   * that will attempt to decode/encode a command
   *
   *
   * @see #decode
   * @see #encode
   */
  public void removeCommandEncoder(CommandEncoder ce) {
    for (int i=0;i<commandEncoders.length;++i) {
      if (ce.equals(commandEncoders[i])) {
        CommandEncoder[] oldValue = commandEncoders;
        commandEncoders = new CommandEncoder[oldValue.length-1];
        System.arraycopy(oldValue,0,commandEncoders,0,i);
        if (i < commandEncoders.length-1) {
          System.arraycopy(oldValue,i+1,commandEncoders,i,oldValue.length-i-1);
        }
        break;
      }
    }
  }

  /**
   * Display the given text in the control window's status line
   */
  public void warn(String s) {
    status.setText(s);
  }

  /**
   * @return a single Random number generator that all objects may share
   */
  public Random getRNG() {
    if (RNG == null) {
      RNG = new Random();
    }
    return RNG;
  }

  /**
   * @return the object responsible for logging commands to a logfile
   */
  public Logger getLogger() {
    return logger;
  }

  /**
   * set the object that displays chat text
   */
  public void setChatter(Chatter c) {
    if (chat != null) {
      chatPanel.remove(chat);
    }
    chat = c;
    chatPanel.add("Center", chat);
  }

  /**
   * @return the object that displays chat text
   */
  public Chatter getChatter() {
    return chat;
  }

  public void setPrefs(Prefs p) {
    preferences = p;
    globalPrefs = new Prefs(preferences,"VASSAL");
  }

  /**
   * Uses the registered  {@link CommandEncoder}s
   * to decode a String into a {@link Command}.
   */
  public Command decode(String command) {
    if (command == null) {
      return null;
    }
    else {
      Command c = null;
      for (int i=0;i<commandEncoders.length && c == null;++i) {
        c = commandEncoders[i].decode(command);
      }
      if (c == null) {
        System.err.println("Failed to decode " + command);
      }
      return c;
    }
  }

  /**
   * Uses the registered {@link CommandEncoder}s to encode a {@link Command} into a String object
   */
  public String encode(Command c) {
    if (c == null) {
      return null;
    }
    String s = null;
    for (int i=0;i<commandEncoders.length && s == null;++i) {
      s = commandEncoders[i].encode(c);
    }
    if (s == null) {
      System.err.println("Failed to encode " + c);
    }
    return s;
  }

  private static final String SAVE_DIR = "SaveDir";

  /**
   * @return a common FileChooser so that recent file locations
   * can be remembered
   */
  public JFileChooser getFileChooser() {
    if (fileChooser == null) {
      getPrefs().addOption(null, new DirectoryConfigurer(SAVE_DIR, null));
      fileChooser = new JFileChooser();
      File f = (File) getPrefs().getValue(SAVE_DIR);
      if (f != null) {
        fileChooser.setCurrentDirectory(f);
      }
    }
    else {
      fileChooser.rescanCurrentDirectory();
    }
    return fileChooser;
  }

  public FileDialog getFileDialog() {
    if (fileDialog == null) {
      getPrefs().addOption(null, new DirectoryConfigurer(SAVE_DIR, null));
      fileDialog = new FileDialog(getFrame());
      File f = (File) getPrefs().getValue(SAVE_DIR);
      if (f != null) {
        fileDialog.setDirectory(f.getPath());
      }
      fileDialog.setModal(true);
    }
    else {
      fileDialog.setDirectory(fileDialog.getDirectory());
    }
    return fileDialog;
  }

  /**
   * @return the JToolBar of the command window
   */
  public JToolBar getToolBar() {
    return toolBar;
  }

  /**
   * @return the File menu of the command window
   */
  public JMenu getFileMenu() {
    return fileMenu;
  }

  /**
   * Append the string to the title of the controls window and all Map windows
   * @param s If null, set the title to the defaul
   */
  public void appendToTitle(String s) {
    if (s == null) {
      frame.setTitle(gameName+" controls");
    }
    else {
      frame.setTitle(frame.getTitle()+s);
    }
    for (Enumeration e = getComponents(Map.class); e.hasMoreElements();) {
      ((Map)e.nextElement()).appendToTitle(s);
    }
  }

  /**
   * Exit the application, prompting user to save if necessary
   */
  public void quit() {
    boolean cancelled = false;
    try {
      if (getGameState().isModified()) {
        switch (JOptionPane.showConfirmDialog
          (null, "Save Game?",
           "", JOptionPane.YES_NO_CANCEL_OPTION)) {
          case JOptionPane.YES_OPTION:
            getGameState().saveGame();
            break;
          case JOptionPane.CANCEL_OPTION:
            cancelled = true;
        }
      }
      if (!cancelled) {
        if (fileDialog != null
          && fileDialog.getDirectory() != null) {
          getPrefs().getOption(SAVE_DIR).setValue(fileDialog.getDirectory());
        }
        else if (fileChooser != null) {
          getPrefs().getOption(SAVE_DIR).setValue(fileChooser.getCurrentDirectory());
        }
        getPrefs().write();
        if (getDataArchive() instanceof ArchiveWriter
          && !buildString().equals(lastSavedConfiguration)) {
          switch (JOptionPane.showConfirmDialog
            (frame, "Save Module?",
             "", JOptionPane.YES_NO_CANCEL_OPTION)) {
            case JOptionPane.YES_OPTION:
              save();
              break;
            case JOptionPane.CANCEL_OPTION:
              cancelled = true;
          }
        }
        else if (getArchiveWriter() != null) {
          for (Enumeration e = getComponents(ModuleExtension.class); e.hasMoreElements();) {
            ModuleExtension ext = (ModuleExtension) e.nextElement();
            cancelled = !ext.confirmExit();
          }
        }
      }
    }
    finally {
      if (!cancelled) {
        System.exit(0);
      }
    }
  }

  /**
   * Encode the {@link Command}, send it to the server and write it
   * to a logfile (if any is open)
   *
   * @see #encode
   */
  public void sendAndLog(Command c) {
    if (c != null && !c.isNull()) {
      getServer().sendToOthers(c);
      getLogger().log(c);
    }
  }

  private static String userId = null;

  /**
   * @return a String that uniquely identifies the user
   */
  public static String getUserId() {
    return userId;
  }

  /**
   * Set the identifier for the user
   */
  public static void setUserId(String newId) {
    userId = newId;
  }

  /**
   * @return the object reponsible for sending messages to the server
   */
  public abstract ServerConnection getServer();

  /**
   * Set the singleton GameModule and invoke {@link #build} on it
   */
  public static void init(GameModule module) throws IOException {
    if (theModule != null) {
      throw new IOException("Module " + theModule.getDataArchive().getName()
                            + " is already open");
    }
    else {
      theModule = module;
      try {
        theModule.build();
      }
      catch (Exception ex) {
        ex.printStackTrace();
        theModule = null;
        throw new IOException(ex.getMessage());
      }
    }
    if (theModule.getDataArchive() instanceof ArchiveWriter) {
      theModule.lastSavedConfiguration = theModule.buildString();
    }
  }

  /**
   * @return the object which stores data for the module
   */
  public DataArchive getDataArchive() {
    return archive;
  }

  /**
   * If the module is being edited, return the writeable archive for the module
   */
  public ArchiveWriter getArchiveWriter() {
    return archive.getWriter();
  }

  /**
   * @return the singleton instance of GameModule
   */
  public static GameModule getGameModule() {
    return theModule;
  }

  /**
   * Return the object responsible for tracking the state of a game.
   * Only one game in progress is allowed;
   */
  public GameState getGameState() {
    return theState;
  }

  public void saveAs() {
    save(true);
  }
  /**
   * If the module is being edited, write the module data
   */
  public void save() {
    save(false);
  }

  protected void save(boolean saveAs) {
    vassalVersionCreated = System.getProperty(VASSAL_VERSION_RUNNING);
    try {
      String save = buildString();
      getArchiveWriter().addFile
        ("buildFile",
         new java.io.ByteArrayInputStream(save.getBytes()));
      if (saveAs) {
        getArchiveWriter().saveAs();
      }
      else {
        getArchiveWriter().write();
      }
      lastSavedConfiguration = save;
    }
    catch (IOException err) {
      err.printStackTrace();
      JOptionPane.showMessageDialog
        (frame,
         "Couldn't save module.\n" + err.getMessage(),
         "Unable to save",
         JOptionPane.ERROR_MESSAGE);
    }
  }

  protected String buildString() {
    org.w3c.dom.Document doc = Builder.createNewDocument();
    doc.appendChild(getBuildElement(doc));
    return Builder.toString(doc);
  }

}
