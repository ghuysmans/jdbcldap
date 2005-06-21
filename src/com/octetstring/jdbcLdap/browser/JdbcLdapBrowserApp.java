/* **************************************************************************
*
* Copyright (C) 2002-2005 Octet String, Inc. All Rights Reserved.
*
* THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND
* TREATIES. USE, MODIFICATION, AND REDISTRIBUTION OF THIS WORK IS SUBJECT
* TO VERSION 2.0.1 OF THE OPENLDAP PUBLIC LICENSE, A COPY OF WHICH IS
* AVAILABLE AT HTTP://WWW.OPENLDAP.ORG/LICENSE.HTML OR IN THE FILE "LICENSE"
* IN THE TOP-LEVEL DIRECTORY OF THE DISTRIBUTION. ANY USE OR EXPLOITATION
* OF THIS WORK OTHER THAN AS AUTHORIZED IN VERSION 2.0.1 OF THE OPENLDAP
* PUBLIC LICENSE, OR OTHER PRIOR WRITTEN CONSENT FROM OCTET STRING, INC., 
* COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL LIABILITY.
******************************************************************************/
package com.octetstring.jdbcLdap.browser;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;

import java.sql.*;

import com.octetstring.jdbcLdap.jndi.*;

import java.util.*;
import java.util.prefs.Preferences;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.*;
import org.eclipse.jface.resource.*;
import java.net.*;
import com.octetstring.jdbcLdap.util.*;
import java.io.*;
/**
 * @author Marc Boorshtein
 *
 */
public class JdbcLdapBrowserApp {
	public static final String VERSION = "2.1";
	public static final String BUILD = "6117M";
	
	
	static JdbcLdapBrowserApp app;
	Shell shell;
	Display display;
	
	CTabFolder tabs;
	
	HashMap browsers;
	
	Menu menuBar;
	Menu fileMenu;
	
	static Image open;
	static Image close;
	static Image closeAll;
	static Image ldap;
	static Image appIcon;
	static Image toLdif;
	static Image rebind;
	static Image add;
	static Image copy;
	static Image refresh;
	static Image search;
	static Image db;
	
	Font tabFont;
	int sizeLimit;
	int timeLimit;
	public boolean autoExec;
	public String driversDir;
	public int historyLimit;
	static Image ws;
	public JdbcLdapBrowserApp() throws Exception  {
		app = this;
		this.loadPrefs();
		display = new Display();
		
		//open = ImageDescriptor.createFromURL(new URL("file:icons/fileopen.png")).createImage();
		open = new Image(display,JdbcLdapBrowserApp.class.getResourceAsStream("fileopen.gif"));
		
		close = new Image(display,JdbcLdapBrowserApp.class.getResourceAsStream("kill.gif"));
		closeAll = new Image(display,JdbcLdapBrowserApp.class.getResourceAsStream("cancel.gif"));
		ldap = new Image(display,JdbcLdapBrowserApp.class.getResourceAsStream("ldap.gif"));
		appIcon = new Image(display,JdbcLdapBrowserApp.class.getResourceAsStream("appIcon.gif"));
		toLdif = new Image(display,JdbcLdapBrowserApp.class.getResourceAsStream("toldif.gif"));
		rebind = new Image(display,JdbcLdapBrowserApp.class.getResourceAsStream("rebind.gif"));
		add = new Image(display,JdbcLdapBrowserApp.class.getResourceAsStream("add.gif"));
		copy = new Image(display,JdbcLdapBrowserApp.class.getResourceAsStream("copy.gif"));
		refresh = new Image(display,JdbcLdapBrowserApp.class.getResourceAsStream("refresh.gif"));
		search = new Image(display,JdbcLdapBrowserApp.class.getResourceAsStream("search.gif"));
		db = new Image(display,JdbcLdapBrowserApp.class.getResourceAsStream("db.gif"));
		ws = new Image(display,JdbcLdapBrowserApp.class.getResourceAsStream("ws.gif"));
		
		
		browsers = new HashMap();
		//System.out.println("before create");
		
		//System.out.println("before draw");
		//w.setBlockOnOpen(true);
		//w.open();

		final Shell shell = new Shell(display);
		shell.setImage(appIcon);
		this.shell = shell;
		shell.setText("SQL Directory Browser By Octet String Inc.");
		//RowLayout rl = new RowLayout();
	
		GridLayout gr = new GridLayout();
		gr.numColumns = 1;
		
		GridData grd;
		
		shell.setLayout(gr);
		
		
		menuBar = new Menu(shell,SWT.BAR);
		shell.setMenuBar(menuBar);
		Menu fileMenu = new Menu(shell.getMenuBar());
		MenuItem newBrowser = new MenuItem(menuBar,SWT.CASCADE);
		newBrowser.setText("File");
		newBrowser.setMenu(fileMenu);
		
		MenuItem newBrowserItem = new MenuItem(fileMenu,SWT.PUSH);
		newBrowserItem.setText("New Browser");
		newBrowserItem.setImage(open);
		newBrowserItem.addSelectionListener(new NewBrowserPressed(this));
		
		MenuItem rebindMenuItem = new MenuItem(fileMenu,SWT.PUSH);
		rebindMenuItem.addSelectionListener(new LoadRebind(this));
		rebindMenuItem.setText("Rebind");
		rebindMenuItem.setImage(rebind);
		
		MenuItem close = new MenuItem(fileMenu,SWT.CASCADE);
		close.setText("Close...");
		close.setImage(this.close);
		close.addSelectionListener(new CloseDropDown(this));
		
		
		Menu closeMenu = new Menu(close);
		closeMenu.addMenuListener(new DrawCloseMenu(this));
		//MenuItem test = new MenuItem(closeMenu,SWT.CASCADE);
		//test.setMenu(fileMenu);
		//test.setText("test");
		
		close.setMenu(closeMenu);
		MenuItem closeAll = new MenuItem(fileMenu,SWT.PUSH);
		closeAll.setText("Close All");
		closeAll.setImage(this.closeAll);
		closeAll.addSelectionListener(new CloseAllPressed(this));
		new MenuItem(fileMenu,SWT.SEPARATOR);
		
		MenuItem quit = new MenuItem(fileMenu,SWT.PUSH);
		quit.setText("Quit");
		quit.addSelectionListener(new AppQuitPressed(shell));
		//fileMenu.setVisible(true);
		//System.out.println("enable : " + fileMenu.isEnabled() + " " + fileMenu.isVisible());
		
		
		Menu editMenu = new Menu(shell.getMenuBar());
		MenuItem editName = new MenuItem(menuBar,SWT.CASCADE);
		editName.setText("Edit");
		editName.setMenu(editMenu);
		
		MenuItem copyName = new MenuItem(editMenu,SWT.PUSH);
		copyName.setText("Copy Name");
		copyName.setImage(copy);
		copyName.addSelectionListener(new CopyName(this));
		
		Menu genSQLMenu = new Menu(shell.getMenuBar());
		MenuItem genSQLMenuName = new MenuItem(menuBar,SWT.CASCADE);
		genSQLMenuName.setText("Generate SQL");
		genSQLMenuName.setMenu(genSQLMenu);
		JdbcLdapBrowserApp.drawEditMenu(genSQLMenu,false);
		
		
		Menu helpMenu = new Menu(shell.getMenuBar());
		MenuItem helpName = new MenuItem(menuBar,SWT.CASCADE);
		helpName.setText("Help");
		helpName.setMenu(helpMenu);
		
		MenuItem prefsName = new MenuItem(helpMenu,SWT.PUSH);
		prefsName.setText("SQL Directory Browser Preferences");
		prefsName.addSelectionListener(new OpenPreferences());
		
		MenuItem aboutName = new MenuItem(helpMenu,SWT.PUSH);
		aboutName.setText("About SQL Directory Browser");
		aboutName.addSelectionListener(new AboutPressed(shell));
		
		//addBrowser();
		//shell.pack();
		grd = new GridData();
		grd.horizontalAlignment = GridData.BEGINNING;
		//CoolBar coolBar = new CoolBar(shell,SWT.NONE);
		//coolBar.setLayoutData(grd);
		ToolBar toolBar = new ToolBar(shell,SWT.HORIZONTAL | SWT.FLAT);
		toolBar.setLayoutData(grd);
		
		ToolItem newBrowserToolItem = new ToolItem(toolBar,SWT.PUSH);
		newBrowserToolItem.setToolTipText("New Browser");
		newBrowserToolItem.setImage(open);
		newBrowserToolItem.setText("New Browser");
		newBrowserToolItem.addSelectionListener(new NewBrowserPressed(this));
		
		ToolItem closeBrowserItem = new ToolItem(toolBar,SWT.DROP_DOWN);
		closeBrowserItem.setToolTipText("Close");
		closeBrowserItem.setText("Close");
		closeBrowserItem.setImage(this.close);
		closeBrowserItem.addSelectionListener(new CloseDropDown(this));
		
		ToolItem closeAllBrowserItem = new ToolItem(toolBar,SWT.PUSH);
		closeAllBrowserItem.setToolTipText("Close All");
		closeAllBrowserItem.setText("Close All");
		closeAllBrowserItem.setImage(this.closeAll);
		closeAllBrowserItem.addSelectionListener(new CloseAllPressed(this));
		
		//new ToolItem(toolBar,SWT.SEPARATOR);
		
		//ToolItem refsItem = new ToolItem(toolBar,SWT.CHECK);
		//refsItem.setText("Follow Referrals");
		//refsItem.setImage(toLdif);
		
		
		new ToolItem(toolBar,SWT.SEPARATOR);
		ToolItem rebindItem = new ToolItem(toolBar,SWT.PUSH);
		rebindItem.setToolTipText("Rebind");
		rebindItem.setText("Rebind");
		rebindItem.setImage(this.rebind);
		rebindItem.addSelectionListener(new LoadRebind(this));
		
		ToolItem toLdifItem = new ToolItem(toolBar,SWT.PUSH);
		toLdifItem.setToolTipText("Export Current Qury To LDIF");
		toLdifItem.setText("To LDIF");
		toLdifItem.setImage(toLdif);
		toLdifItem.addSelectionListener(new ToLdif(this));
		
		ToolItem toCsvItem = new ToolItem(toolBar,SWT.PUSH);
		toCsvItem.setToolTipText("Export Current Qury To CSV");
		toCsvItem.setText("To CSV");
		toCsvItem.setImage(toLdif);
		toCsvItem.addSelectionListener(new ToCSV(this));
		
		toolBar.pack();
		//coolBar.pack();
		
		this.tabs = new CTabFolder(shell,SWT.TOP | SWT.BORDER);
		this.tabs.setSimple(false);
		FontData fd = new FontData();
		fd.setName(tabs.getFont().getFontData()[0].getName());
		fd.setHeight(tabs.getFont().getFontData()[0].getHeight());
		fd.setStyle(SWT.BOLD);
		
		
		tabFont = new Font(display,fd);
		
		tabs.setFont(tabFont);
		tabs.setSelectionForeground(display.getSystemColor(SWT.COLOR_TITLE_FOREGROUND));
		/*tabs.setSelectionBackground(new Color[]{display.getSystemColor(SWT.COLOR_TITLE_BACKGROUND), 
			display.getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT),
			display.getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT), 
			display.getSystemColor(SWT.COLOR_WHITE)},
			new int[] {75, 75, 75},true);*/
		tabs.setSelectionBackground(display.getSystemColor(SWT.COLOR_TITLE_BACKGROUND));
		grd = new GridData();
		grd.grabExcessHorizontalSpace = true;
		grd.grabExcessVerticalSpace = true;
		grd.verticalAlignment = GridData.FILL;
		grd.horizontalAlignment = GridData.FILL;
		tabs.setLayoutData(grd);
		shell.open();
		//shell.addControlListener(new ResizeEvent(w));

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		open.dispose();
		appIcon.dispose();
		close.dispose();
		closeAll.dispose();
		tabFont.dispose();
		toLdif.dispose();
		rebind.dispose();
		copy.dispose();
		add.dispose();
		//Display.getCurrent().dispose();
	}
	
	protected void addBrowser() throws Exception {
		ConnectionInfo con = new ConnectionInfo();
						con.showWindow(display);
						
						if (! con.doOpen) return;
				JdbcLdapBrowser w = null;
				
				if (! con.bjdbc) {
					w = new JdbcLdapBrowser(
							con.sserver,
							con.sport,
							con.sbase,
							con.suser,
							con.spassword,con.bfollowReferrals,con.bIsDSML,con.bSsl,con.bspml,con.textraURL);
				} else {
					w = new JdbcLdapBrowser(
							con.sserver,
							con.sport,
							
							con.suser,
							con.spassword);
				}
							
		
				
				w.setName(con.sname);
				
				if (this.browsers.containsKey(con.sname)) throw new Exception(con.sname + " Already Exists");
				
				CTabItem ti = new CTabItem(tabs,SWT.NONE);
				
				ti.setText(con.sname);
				
				if (con.bjdbc) {
					ti.setImage(db);
				} else if (con.bIsDSML || con.bspml) {
					ti.setImage(ws);
				}
				
				else {
					ti.setImage(ldap);
				}
				
				Composite c = new Composite(tabs,SWT.NONE);
				ti.setControl(c);
		

		

				w.createContents(c);
				
				Browser b = new Browser();
				b.browser = w;
				b.tab = ti;
				
				this.browsers.put(con.sname,b);
				
				ti.getParent().setSelection(ti);
	}
	
	public void removeBrowser(String browser) {
		//System.out.println("removing " + browser);
		Browser b = (Browser) browsers.remove(browser);
		JdbcLdapBrowser jdbcLdapBrowser = b.browser;
		CTabItem tab = b.tab;
		
		tab.dispose();
		try {
			jdbcLdapBrowser.con.close();
		}
		catch (Exception e) {
			
		}
	}
	
	public JdbcLdapBrowser getCurrentBrowser() {
		 
			CTabFolder tabs = this.tabs;
			CTabItem tab = tabs.getSelection();
			if (tab != null) {
				String name = tab.getText();
				
				return ((Browser) browsers.get(name)).browser;
			} else {
				return null;
			}
	}
	
	

	public static void main(String[] args) throws Exception {
		new JdbcLdapBrowserApp();
	}
	
	/**
	 * @param popup
	 */
	public static void drawEditMenu(Menu popup,boolean addCopy) {
		MenuItem mi;
		if (addCopy) {
			mi = new MenuItem(popup,SWT.PUSH);
			  mi.setText("Copy Name");
			  mi.setImage(JdbcLdapBrowserApp.copy);
			  mi.addSelectionListener(
			  		new SelectionAdapter() {
						public void widgetSelected(SelectionEvent event) {
							if (JdbcLdapBrowserApp.app.getCurrentBrowser() == null) {
								MessageDialog.openError(JdbcLdapBrowserApp.app.shell,"No Browsers Opened","No Browsers Opened");
								return;
							}	
							JdbcLdapBrowserApp.app.getCurrentBrowser().copyName();							
						}
			  		}
			  
			  		
			  );
		}
		  
		  
		mi = new MenuItem(popup,SWT.PUSH);
		  mi.setText("Generate Search SQL...");
		  mi.setImage(JdbcLdapBrowserApp.search);
		  mi.addSelectionListener(
		  		new SelectionAdapter() {
					public void widgetSelected(SelectionEvent event) {
						if (JdbcLdapBrowserApp.app.getCurrentBrowser() == null) {
							MessageDialog.openError(JdbcLdapBrowserApp.app.shell,"No Browsers Opened","No Browsers Opened");
							return;
						}
						
						String base;
						if (JdbcLdapBrowserApp.app.getCurrentBrowser().tv.getTree().getSelection().length == 0) {
							base = JdbcLdapBrowserApp.app.getCurrentBrowser().baseDN;
						} else {
							base = ((TreeObject) JdbcLdapBrowserApp.app.getCurrentBrowser().tv.getTree().getSelection()[0].getData()).getBase();
						}
						//System.out.println("base : " + base);
						JdbcLdapBrowser browser = JdbcLdapBrowserApp.app.getCurrentBrowser(); 
						browser.SQL.setText((new SearchDialog(Display.getCurrent(),base)).sql);
						if (JdbcLdapBrowserApp.app.autoExec) {
							browser.executeSQL();
						}
						
					}
		  		}
		  );
		
		  mi = new MenuItem(popup,SWT.PUSH);
		  mi.setText("Generate SQL To Add New Entry...");
		  mi.setImage(JdbcLdapBrowserApp.add);
		  mi.addSelectionListener(
		  		new SelectionAdapter() {
					public void widgetSelected(SelectionEvent event) {
						if (JdbcLdapBrowserApp.app.getCurrentBrowser() == null) {
							MessageDialog.openError(JdbcLdapBrowserApp.app.shell,"No Browsers Opened","No Browsers Opened");
							return;
						}
						String base;
						if (JdbcLdapBrowserApp.app.getCurrentBrowser().tv.getTree().getSelection().length == 0) {
							base = JdbcLdapBrowserApp.app.getCurrentBrowser().baseDN;
						} else {
							base = ((TreeObject) JdbcLdapBrowserApp.app.getCurrentBrowser().tv.getTree().getSelection()[0].getData()).getBase();
						}
						//System.out.println("base : " + base);
						JdbcLdapBrowserApp.app.getCurrentBrowser().SQL.setText((new AddEntry(Display.getCurrent(),base)).sql);
						JdbcLdapBrowser browser = JdbcLdapBrowserApp.app.getCurrentBrowser();
						if (JdbcLdapBrowserApp.app.autoExec) {
							browser.executeSQL();
						}
					}
		  		}
		  );
		  
		  mi = new MenuItem(popup,SWT.PUSH);
		  mi.setText("Generate SQL To Modify Current Entry...");
		  mi.setImage(JdbcLdapBrowserApp.add);
		  mi.addSelectionListener(
		  		new SelectionAdapter() {
					public void widgetSelected(SelectionEvent event) {
						if (JdbcLdapBrowserApp.app.getCurrentBrowser() == null) {
							MessageDialog.openError(JdbcLdapBrowserApp.app.shell,"No Browsers Opened","No Browsers Opened");
							return;
						}
						String base;
						if (JdbcLdapBrowserApp.app.getCurrentBrowser().tv.getTree().getSelection().length == 0) {
							base = JdbcLdapBrowserApp.app.getCurrentBrowser().baseDN;
						} else {
							base = ((TreeObject) JdbcLdapBrowserApp.app.getCurrentBrowser().tv.getTree().getSelection()[0].getData()).getBase();
						}
						//System.out.println("base : " + base);
						JdbcLdapBrowserApp.app.getCurrentBrowser().SQL.setText((new ModifyEntry(Display.getCurrent(),base)).sql);
						JdbcLdapBrowser browser = JdbcLdapBrowserApp.app.getCurrentBrowser();
						if (JdbcLdapBrowserApp.app.autoExec) {
							browser.executeSQL();
						}
					}
		  		}
		  );
		  
		  mi = new MenuItem(popup,SWT.PUSH);
		  mi.setText("Generate SQL To Delete Current Entry");
		  mi.setImage(JdbcLdapBrowserApp.close);
		  mi.addSelectionListener(
		  		new SelectionAdapter() {
					public void widgetSelected(SelectionEvent event) {
						if (JdbcLdapBrowserApp.app.getCurrentBrowser() == null) {
							MessageDialog.openError(JdbcLdapBrowserApp.app.shell,"No Browsers Opened","No Browsers Opened");
							return;
						}
						String base;
						if (JdbcLdapBrowserApp.app.getCurrentBrowser().tv.getTree().getSelection().length == 0) {
							base = JdbcLdapBrowserApp.app.getCurrentBrowser().baseDN;
						} else {
							base = ((TreeObject) JdbcLdapBrowserApp.app.getCurrentBrowser().tv.getTree().getSelection()[0].getData()).getBase();
						}
						//System.out.println("base : " + base);
						JdbcLdapBrowserApp.app.getCurrentBrowser().SQL.setText("DELETE FROM " + base);
						JdbcLdapBrowser browser = JdbcLdapBrowserApp.app.getCurrentBrowser();
						if (JdbcLdapBrowserApp.app.autoExec) {
							if (MessageDialog.openConfirm(JdbcLdapBrowserApp.app.shell,"Delete Entry?","Delete : " + browser.SQL.getText())) {
								browser.executeSQL();
							}
						}
					}
		  		}
		  );
		  
		  
		  
		  if (addCopy) {
			  mi = new MenuItem(popup,SWT.PUSH);
			  mi.setText("Refresh Children");
			  mi.setImage(JdbcLdapBrowserApp.refresh);
			  mi.addSelectionListener(
			  		new SelectionAdapter() {
						public void widgetSelected(SelectionEvent event) {
							if (JdbcLdapBrowserApp.app.getCurrentBrowser() == null) {
								MessageDialog.openError(JdbcLdapBrowserApp.app.shell,"No Browsers Opened","No Browsers Opened");
								return;
							}
							//String base = ((TreeObject) browser.tv.getTree().getSelection()[0].getData()).getBase();
							JdbcLdapBrowserApp.app.getCurrentBrowser().tv.refresh(JdbcLdapBrowserApp.app.getCurrentBrowser().tv.getTree().getSelection()[0].getData());	
						}
			  		}
			  );
		  }
	}
	
	public void loadPrefs() {
		
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		this.sizeLimit = Integer.parseInt(prefs.get("JDBC_LDAP_SIZE_LIMIT","0"));
		this.timeLimit = Integer.parseInt(prefs.get("JDBC_LDAP_TIME_LIMIT","0"));
		this.autoExec = prefs.getBoolean("JDBC_LDAP_AUTO_EXEC",true);
		this.driversDir = prefs.get("JDBC_LDAP_DRIVER_PATH",System.getProperty("user.home") + File.separator + "jdbcDrivers");
		this.historyLimit = Integer.parseInt(prefs.get("JDBC_LDAP_HIST_LIMIT","16"));
	}
	
	public void setPrefs() {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		prefs.put("JDBC_LDAP_SIZE_LIMIT",Integer.toString(this.sizeLimit));
		prefs.put("JDBC_LDAP_TIME_LIMIT",Integer.toString(this.timeLimit));
		prefs.put("JDBC_LDAP_AUTO_EXEC",Boolean.toString(this.autoExec));
		prefs.put("JDBC_LDAP_DRIVER_PATH",this.driversDir);
		prefs.putInt("JDBC_LDAP_HIST_LIMIT",this.historyLimit);
	}
}

class Browser {
	JdbcLdapBrowser browser;
	CTabItem tab;
}

class NewBrowserPressed extends SelectionAdapter {
	JdbcLdapBrowserApp app;
	public NewBrowserPressed(JdbcLdapBrowserApp app) {
		this.app = app;
	}
	
	public void widgetSelected(SelectionEvent event) {
		try {
			app.addBrowser();
		}
		catch (Exception e) {
			e.printStackTrace(System.out);
			MessageDialog.openError(app.shell,"Error Occurred While Adding Browser",e.toString());
		}
	}
}


class AboutPressed extends SelectionAdapter {
	Shell shell;
	
	public AboutPressed(Shell shell) {
		this.shell = shell;
		
	}
	
	public void widgetSelected(SelectionEvent event) {
		
		MessageDialog.openInformation(shell,"Octet String, Inc.","SQL Directory Browser Version " + JdbcLdapBrowserApp.VERSION + "\nBuild Number " + JdbcLdapBrowserApp.BUILD + "\nCopyright (C) Octet String, Inc. 2004-2005.  All rights reserved.");
		//System.exit(0);	
	}
}



class AppQuitPressed extends SelectionAdapter {
	Shell shell;
	
	public AppQuitPressed(Shell shell) {
		this.shell = shell;
		
	}
	
	public void widgetSelected(SelectionEvent event) {
		
		shell.dispose();
		//System.exit(0);	
	}
}

class DrawCloseMenu implements MenuListener {
	
	JdbcLdapBrowserApp app;
	
	public DrawCloseMenu(JdbcLdapBrowserApp app) {
		this.app = app;
	}
	
	public void menuHidden(MenuEvent e) {
		
	}
	
	public void menuShown(MenuEvent e) {
		
//		first dispose of current entries
			  Menu closeMenu = (Menu) e.widget;
			  MenuItem mis[] = closeMenu.getItems();
			  for (int i=0,m=mis.length;i<m;i++) {
				  mis[i].dispose();
			  }
		
			  Iterator browsers = app.browsers.keySet().iterator();
		
			  while (browsers.hasNext()) {
				  MenuItem mi = new MenuItem(closeMenu,SWT.PUSH);
				  mi.setText((String) browsers.next());
				  mi.addSelectionListener(
				  		new SelectionAdapter() {
							public void widgetSelected(SelectionEvent event) {
									app.removeBrowser(((MenuItem)event.widget).getText());							
								}
				  		}
				  
				  		
				  );
			  }
	}
}

class CloseAllPressed extends SelectionAdapter {
	JdbcLdapBrowserApp app;
	
	public CloseAllPressed(JdbcLdapBrowserApp app) {
		this.app = app;
	}
	
	public void widgetSelected(SelectionEvent event) {
		Object[] browsers = app.browsers.keySet().toArray();
		
		for (int i=0,m=browsers.length;i<m;i++) {
			app.removeBrowser((String) browsers[i]);
		} 
	
	}
}

class CloseDropDown extends SelectionAdapter {
	JdbcLdapBrowserApp app;
	Menu dropDown;
	
	public CloseDropDown(JdbcLdapBrowserApp app) {
		this.app = app;
		dropDown = null;
	}
	
	public void widgetSelected(SelectionEvent event) {
		//System.out.println("in widget selected");
		if (event.detail == SWT.ARROW) {
			ToolItem closeItem = (ToolItem) event.widget;
			ToolBar toolBar = closeItem.getParent();
			dropDown = new Menu(toolBar);
			dropDown.addMenuListener(new DrawCloseMenu(app));
			
			Point pt = toolBar.toDisplay(new Point(event.x, event.y));
			dropDown.setLocation (pt.x, pt.y);
			dropDown.setVisible (true);
			Display display = toolBar.getDisplay ();
			while (dropDown != null && dropDown.isVisible ()) {
				if (!display.readAndDispatch ()) display.sleep ();
			}
			if (dropDown != null) {
				dropDown.dispose ();
				dropDown = null;
			}
		}
		else {
			CTabFolder tabs = app.tabs;
			CTabItem tab = tabs.getSelection();
			if (tab != null) {
				String name = tab.getText();
				app.removeBrowser(name);
			}
		}
		
		
	}
	
}

class LoadRebind extends SelectionAdapter {
	JdbcLdapBrowserApp app;
	
	public LoadRebind (JdbcLdapBrowserApp app) {
		this.app = app;
	}
	
	public void widgetSelected(SelectionEvent event) {
		CTabItem tab = app.tabs.getSelection();
		if (tab == null) {
			MessageDialog.openError(app.shell,"Error","No Browsers Oppened");
			return;
		}
		
		JdbcLdapBrowser browser = ((Browser) app.browsers.get(tab.getText())).browser;
		
		if (browser.isDB) {
			MessageDialog.openError(app.shell,"Can not Rebinf","Rebind can only be performed on directory services");
			return;
		}
		
		try {
			Rebind rebind = new Rebind((JndiLdapConnection) browser.getConnection(),(ConnectionStore) browser.reffedCons.get(0),app.display);
		} catch (SQLException e) {
			
			e.printStackTrace();
			MessageDialog.openError(app.shell,"Error",e.toString());
		}
	}
	
}

class ToLdif extends SelectionAdapter {
	JdbcLdapBrowserApp app;
	
	public ToLdif (JdbcLdapBrowserApp app) {
		this.app = app;
	}
	
	public void widgetSelected(SelectionEvent event) {
		if (app.tabs.getSelection() == null) {
			MessageDialog.openError(app.shell,"Error","No Browsers Oppened");
			return;
		}
		
		JdbcLdapBrowser browser = ((Browser) app.browsers.get(app.tabs.getSelection().getText())).browser;
		
		if (browser.isDB) {
			MessageDialog.openError(app.shell,"Can not generate LDIF","LDIF can only be generated by directory services");
			return;
		}
		
		String SQL = browser.SQL.getText();
		
		if (SQL == null || ! SQL.toLowerCase().startsWith("select")) {
			MessageDialog.openError(app.shell,"Error","No Query Entered");
			return;
		}
		
		FileDialog fd = new FileDialog(app.shell,SWT.SAVE);
		fd.setFilterExtensions(new String[] {"ldif"});
		fd.setText("Save LDIF To");
		fd.open();
		
		String saveTo = fd.getFileName();
		try {
			JndiLdapConnection con = (JndiLdapConnection) browser.getConnection();
			con.setExpandRow(false);
			con.setConcatAtts(true);
			SQL = browser.insertScope(SQL);
			ResultSet rs = con.createStatement().executeQuery(SQL);
			LDIF ldif = new LDIF(rs,"DN",false);
			con.setExpandRow(true);
			con.setConcatAtts(false);
			//System.out.println(fd.getFilterPath() + "/" + fd.getFileName());
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(fd.getFilterPath() + "/" + fd.getFileName()))));
			pw.println(ldif.toString());
			pw.flush();
			pw.close();
		}
		catch (Exception e) {
			MessageDialog.openError(app.shell,"Query Could Not Be Executed",e.toString());
		}
	}
	
}

class ToCSV extends SelectionAdapter {
	JdbcLdapBrowserApp app;
	
	public ToCSV (JdbcLdapBrowserApp app) {
		this.app = app;
	}
	
	public void widgetSelected(SelectionEvent event) {
		if (app.tabs.getSelection() == null) {
			MessageDialog.openError(app.shell,"Error","No Browsers Oppened");
			return;
		}
		
		JdbcLdapBrowser browser = ((Browser) app.browsers.get(app.tabs.getSelection().getText())).browser;
		
		String SQL = browser.SQL.getText().replace('\n',' ');
		
		if (SQL == null || ! SQL.toLowerCase().startsWith("select")) {
			MessageDialog.openError(app.shell,"Error","No Query Entered");
			return;
		}
		
		FileDialog fd = new FileDialog(app.shell,SWT.SAVE);
		fd.setFilterExtensions(new String[] {"csv"});
		fd.setText("Save CSV To");
		fd.open();
		
		String saveTo = fd.getFileName();
		try {
			Connection con =  browser.getConnection();
			if (con instanceof JndiLdapConnection) {
				((JndiLdapConnection) con).setExpandRow(false);
				((JndiLdapConnection)con).setConcatAtts(true);
				SQL = browser.insertScope(SQL);
				((JndiLdapConnection)con).setExpandRow(true);
				((JndiLdapConnection)con).setConcatAtts(false);
			}
			ResultSet rs = con.createStatement().executeQuery(SQL);
			
			
			//System.out.println(fd.getFilterPath() + "/" + fd.getFileName());
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(fd.getFilterPath() + "/" + fd.getFileName()))));
		
			ResultSetMetaData md = rs.getMetaData();
			
			for (int i=1,m=md.getColumnCount();i<=m;i++) {
				pw.print("\"" + md.getColumnName(i) + "\"");
				if (i<m) {
					pw.print(",");
				}
			}
			
			pw.println();
			
			while (rs.next()) {
				for (int i=1,m=md.getColumnCount();i<=m;i++) {
					pw.print("\"" + rs.getString(md.getColumnName(i)) + "\"");
					if (i<m) {
						pw.print(",");
					}
				}
				
				pw.println();
			}
			
			pw.flush();
			pw.close();
		}
		catch (Exception e) {
			MessageDialog.openError(app.shell,"Query Could Not Be Executed",e.toString());
		}
	}
	
}

class CopyName extends SelectionAdapter {
	JdbcLdapBrowserApp app;
	
	public CopyName(JdbcLdapBrowserApp app) {
		this.app = app;
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		CTabItem tab = app.tabs.getSelection();
		if (app.tabs.getSelection() == null) {
			MessageDialog.openError(app.shell,"Error","No Browsers Oppened");
			return;
		}
		
		JdbcLdapBrowser browser = ((Browser) app.browsers.get(tab.getText())).browser;
		
		browser.copyName();
		
	}
	
	
}

class OpenPreferences extends SelectionAdapter {
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		new BrowserPrefs(JdbcLdapBrowserApp.app.display);
	}
}
