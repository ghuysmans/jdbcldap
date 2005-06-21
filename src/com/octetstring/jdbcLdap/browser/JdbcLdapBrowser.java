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
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.dnd.*;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.*;

import org.eclipse.jface.dialogs.MessageDialog;
import java.util.*;
import java.util.prefs.Preferences;

import com.novell.ldap.LDAPUrl;
import com.novell.ldap.util.Base64;
import com.octetstring.jdbcLdap.jndi.*;


/**
 * @author Marc Boorshtein
 *
 */

public class JdbcLdapBrowser {
	CTabFolder tabs;
	Font tabFont;
	JndiLdapConnection con;
	String url,server,port,base,user,pass;
	boolean followReferrals,isDsml;
	String lastSQL;
	ArrayList reffedCons;
	Text SQL;
	
	ResultLoader results;
	Clipboard clipboard;
	TreeViewer tv;
	String baseDN;
	private boolean isSSL;
	protected ExecuteSQLPressed executeSQL;
	protected Button executeButton;
	boolean isSpml;
	URLClassLoader driverLoader;
	Connection jdbcCon;
	List history;
	
	boolean isDB;
	private String jdbcDriver;
	String jdbcUrl;
	String name;
	private String extraUrl;
	
	
	
	
	public JdbcLdapBrowser(
		String server,
		String port,
		String base,
		String user,
		String pass,
		boolean followRefferals,boolean isDsml,boolean isSSL,boolean isSpml,String extraUrl)
		throws Exception {
		this.clipboard = new Clipboard(Display.getCurrent());
		ConnectionStore conStore = new ConnectionStore();
		conStore.server = server;
		conStore.port = port;
		conStore.base = base;
		conStore.user = user;
		conStore.pass = pass;
		conStore.followReferrals = followRefferals;
		conStore.isDsml = isDsml;
		conStore.isSSL = isSSL;
		conStore.isSpml = isSpml;
		
		this.base = base;
		this.user = user;
		this.pass = pass;
		this.isDsml = isDsml;
		this.isSSL = isSSL;
		this.isSpml = isSpml;
		this.extraUrl = extraUrl;
		con = createConnection(server, port, base, user, pass,followRefferals,isDsml,isSSL,isSpml,extraUrl);
		this.baseDN = conStore.base;
		if (! isDsml) {
			url = "jdbc:ldap://"
			+ server
			+ ":"
			+ port
			+ "/"
			+ base
			+ "?EXP_ROWS:=true&secure:=" + (isSSL ? "true" : "false");
		}
		else {
			url = "jdbc:dsml:" + server + "?EXP_ROWS:=true";
		}
		conStore.con = con;
		this.reffedCons = new ArrayList();
		this.reffedCons.add(conStore);
		results = new ResultLoader(this);
		
		lastSQL = null;
		
		this.isDB = false;
		
		
		
	}
	
	public JdbcLdapBrowser(String driver,String url,String user,String pass) {
		this.isDB = true;
		
		this.jdbcDriver = driver;
		this.jdbcUrl = url;
		this.user = user;
		this.pass = pass;
		this.clipboard = new Clipboard(Display.getCurrent());
		File dir = new File(JdbcLdapBrowserApp.app.driversDir);
		if (dir.isDirectory() && dir.exists()) {
			File[] children = dir.listFiles();
			URL[] urls = new URL[children.length];
			
			for (int i=0;i<children.length;i++) {
				try {
					urls[i] = children[i].toURL();
				} catch (MalformedURLException e2) {
					MessageDialog.openError(Display.getCurrent().getActiveShell(),"Could not load drivers",e2.toString());
				}
			}
			
			this.driverLoader = new URLClassLoader(urls);
			
		} else {
			this.driverLoader = new URLClassLoader(new URL[0]);
		}
		
		try {
			this.jdbcCon = getJdbcConnection();
		} catch (Exception e) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),"Could not load JDBC Driver",e.toString());
		}
		
		try {
			results = new ResultLoader(this);
		} catch (SQLException e1) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),"Could not create result loader",e1.toString());
		}
	}
	
	/**
	 * @return
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 */
	private Connection getJdbcConnection() throws SQLException, SecurityException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		
		
		Driver driverImpl = (Driver) this.driverLoader.loadClass(this.jdbcDriver).newInstance();
		
		Properties props = new Properties();
		props.put("user",this.user);
		props.put("password",this.pass);
		
		return driverImpl.connect(this.jdbcUrl,props);
		
		/*Class.forName(this.jdbcDriver).newInstance();
		
		
		return DriverManager.getConnection(this.jdbcUrl,this.user,this.pass);*/
		
	}

	public Connection getConnection() throws SQLException {
		if (! this.isDB) {
			return getConnection(0);
		} else {
			return this.jdbcCon;
		}
	}
	
	public JndiLdapConnection getConnection(int id) throws SQLException {
		ConnectionStore conStore;
		JndiLdapConnection con;
		if (id < this.reffedCons.size()) {
			conStore = (ConnectionStore) this.reffedCons.get(id);
			con = conStore.con;
		}
		else {
			return null;
		}
		
		
		boolean isopen = false;
		try {
			isopen = ! con.isClosed();
		}
		catch (SQLException e) {
			isopen = false;
		}
		
		try {
			
			//System.out.println("Connection Open? : " + isopen);
			if (! isopen) {
				if (MessageDialog.openQuestion(tabs.getShell(),"Reconnect?","Connection Broken, Reconnect?")) {
					con = JdbcLdapBrowser.createConnection(conStore.server,conStore.port,conStore.base,conStore.user,conStore.pass,conStore.followReferrals,conStore.isDsml,conStore.isSSL,conStore.isSpml,conStore.extraUrl);
					conStore.con = con;
					con.setMaxSizeLimit(JdbcLdapBrowserApp.app.sizeLimit);
					con.setMaxTimeLimit(JdbcLdapBrowserApp.app.timeLimit);
					return con;
				}
				else {
					throw new SQLException("No Connection");
				}
			}
			else {
				con.setMaxSizeLimit(JdbcLdapBrowserApp.app.sizeLimit);
				con.setMaxTimeLimit(JdbcLdapBrowserApp.app.timeLimit);
				return con;
			}
		}
		catch (SQLException e) {
			if(MessageDialog.openQuestion(tabs.getShell(),"Reconnect?",e.toString() + " - Connection Broken, Reconnect?")) {
				
					con = JdbcLdapBrowser.createConnection(conStore.server,conStore.port,conStore.base,conStore.user,conStore.pass,conStore.followReferrals,conStore.isDsml,conStore.isSSL,isSpml,conStore.extraUrl);
					conStore.con = con;
					con.setMaxSizeLimit(JdbcLdapBrowserApp.app.sizeLimit);
					con.setMaxTimeLimit(JdbcLdapBrowserApp.app.timeLimit);
				return con;
			}
			else {
				throw new SQLException("No Conection - " + e.toString());
			}
		
			
		}
	}
	
	public int createRefConnection(String url) throws SQLException {
		ConnectionStore conStore = new ConnectionStore();
		LDAPUrl ldapUrl = null;
		try {
			ldapUrl = new LDAPUrl(url);
		} catch (MalformedURLException e) {
			throw new SQLException(e.toString());
		}
		
		
		
		conStore.server = ldapUrl.getHost(); 
		conStore.port = Integer.toString(ldapUrl.getPort());
		conStore.base = ldapUrl.getDN();
		conStore.pass = this.pass;
		conStore.user = this.user;
		conStore.followReferrals = true;
		conStore.con = createConnection(conStore.server,conStore.port,conStore.base,conStore.user,conStore.pass,conStore.followReferrals,conStore.isDsml,conStore.isSSL,isSpml,conStore.extraUrl);
		this.reffedCons.add(conStore);
		return this.reffedCons.size()-1;
	}

	public static JndiLdapConnection createConnection(String server, String port, String base, String user, String pass,boolean followReferrals,boolean isDsml,boolean isSSL,boolean isSpml,String extraUrl) throws  SQLException {
		//super(null);
		try {
			Class.forName("com.octetstring.jdbcLdap.sql.JdbcLdapDriver");
		}
		catch (ClassNotFoundException e) {
			throw new SQLException(e.toString());
		}
		JndiLdapConnection con; 
		
		if (isDsml) {
			
			con =
				(JndiLdapConnection) DriverManager.getConnection(
						"jdbc:dsml://"
						+ server
						+ "?EXP_ROWS:=true&secure:=" + (isSSL ? "true" : "false") + (extraUrl.trim().length() != 0 ? "&" + extraUrl : "") ,
						user,
						pass);
		} else if (isSpml) {
			
			con =
				(JndiLdapConnection) DriverManager.getConnection(
						"jdbc:spml://"
						+ server
						+ "?EXP_ROWS:=true&secure:=" + (isSSL ? "true" : "false") + (extraUrl.trim().length() != 0 ? "&" + extraUrl : ""),
						user,
						pass);
		}
		else {
			
		
		con =
			(JndiLdapConnection) DriverManager.getConnection(
				"jdbc:ldap://"
					+ server
					+ ":"
					+ port
					+ "/"
					+ ""
					+ "?EXP_ROWS:=true&PRE_FETCH:=false&secure:=" + (isSSL ? "true" : "false") + (extraUrl.trim().length() != 0 ? "&" + extraUrl : ""),
				user,
				pass);
		}
		
		return con;
	}

	

	protected void createContents(Composite parent) {
		if (! isDB) {
			createLDAPContents(parent);
		} else {
			createDBContents(parent);
		}
	}
	
	/**
	 * @param parent
	 */
	private void createDBContents(Composite parent) {
		//try {

			

			GridLayout gr = new GridLayout();

			gr.numColumns = 1;
			gr.makeColumnsEqualWidth = false;

			parent.setLayout(gr);

			tabs = new CTabFolder(parent, SWT.TOP | SWT.BORDER);
			tabs.setSimple(false);
			tabs.setSelectionForeground(parent.getDisplay().getSystemColor(SWT.COLOR_TITLE_FOREGROUND));
			/*tabs.setSelectionBackground(new Color[]{parent.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND), 
				parent.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT),
				parent.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT), 
				parent.getDisplay().getSystemColor(SWT.COLOR_WHITE)},
				new int[] {75, 75, 75},true);*/
			tabs.setSelectionBackground(parent.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND));
			FontData fd = new FontData();
					fd.setName(tabs.getFont().getFontData()[0].getName());
					fd.setHeight(tabs.getFont().getFontData()[0].getHeight());
					fd.setStyle(SWT.BOLD);
		
					tabFont = new Font(parent.getDisplay(),fd);
					tabs.setFont(tabFont);
			GridLayout gl = new GridLayout();
			gl.numColumns = 1;

			//tabs.setLayout(new FillLayout());
			GridData gd = new GridData();
			gd.grabExcessVerticalSpace = true;
			//tabs.setLayoutData(gd);
			gd.grabExcessHorizontalSpace = true;
			gd.verticalAlignment = GridData.FILL;
			gd.horizontalAlignment = GridData.FILL;
			tabs.setLayoutData(gd);

			CTabItem ti = new CTabItem(tabs, SWT.NONE);
			ti.setText("Database Meta-Data Browser");

			SashForm sash_form = new SashForm(tabs, SWT.HORIZONTAL | SWT.NULL);
			/*gd  = new GridData();
			gd.horizontalAlignment = GridData.FILL;
			gd.verticalAlignment = GridData.FILL;
			gd.grabExcessHorizontalSpace = true;
			gd.grabExcessVerticalSpace = true;
			sash_form.setLayoutData(gd);*/

			ti.setControl(sash_form);

			tabs.setSelection(0);

			tv = new TreeViewer(sash_form);
			tv.getTree().addKeyListener(new GetKey(this));
			
			tv.setContentProvider(new JdbcTree());
			
			//tv.setInput(new TreeObject(this.baseDN, null, this.baseDN));

			tv.setInput(this);
			
			final TableViewer tbv = new TableViewer(sash_form, SWT.BORDER | SWT.FULL_SELECTION);
			
			//tbv.setContentProvider(new AttributesList(this));
			//tbv.setLabelProvider(new AttributeLabel());

			TableColumn column = new TableColumn(tbv.getTable(), SWT.LEFT);
			column.setText("Name");
			column.setWidth(75);
			column = new TableColumn(tbv.getTable(), SWT.LEFT);
			column.setText("Type");
			column.setWidth(200);
			
			column = new TableColumn(tbv.getTable(), SWT.LEFT);
			column.setText("Length");
			column.setWidth(200);
			
			column = new TableColumn(tbv.getTable(), SWT.LEFT);
			column.setText("Description");
			column.setWidth(200);
			tbv.getTable().setHeaderVisible(true);
			tv.addSelectionChangedListener(new ISelectionChangedListener() {
				
				
				public void selectionChanged(SelectionChangedEvent event) {
					
					IStructuredSelection selection =
						(IStructuredSelection) event.getSelection();

					Object selectedNode = selection.getFirstElement();
					tbv.setInput(selectedNode);
				}
			});
			
			ShowPopup pop = new ShowPopup(this);
			tv.getTree().addMouseListener(pop);
			tv.getTree().addKeyListener(pop);

			//Composite c = new Composite(tabs,SWT.NONE);
			//c.setLayout(new FillLayout());
			Table tbvDB = new Table(tabs, SWT.BORDER | SWT.FULL_SELECTION);
			//		gd  = new GridData();
			//				gd.horizontalAlignment = GridData.FILL;
			//				gd.verticalAlignment = GridData.FILL;
			//				gd.grabExcessHorizontalSpace = true;
			//				gd.grabExcessVerticalSpace = true;
			//		  tbvDB.setLayoutData(gd);
			tbvDB.setLinesVisible(true);
			tbvDB.setHeaderVisible(true);
			//DbLabelProvider dbLbl = new DbLabelProvider();
			
			JdbcMetaDataView view = new JdbcMetaDataView();
			tbv.setContentProvider(view);
			tbv.setLabelProvider(view);
			

			

			ti = new CTabItem(tabs, SWT.NONE);
			ti.setControl(tbvDB);
			ti.setText("Results View");

			//GridData gd;

			Composite c = new Composite(parent, SWT.NONE);
			gd = new GridData();
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = GridData.FILL;
			c.setLayoutData(gd);

			gr = new GridLayout();
			gr.numColumns = 4;
			gr.makeColumnsEqualWidth = false;
			c.setLayout(gr);

			Label l = new Label(c, SWT.NONE);
			l.setText("SQL : ");
			gd = new GridData();
			gd.horizontalAlignment = GridData.BEGINNING;
			l.setLayoutData(gd);

			SQL = new Text(c, SWT.BORDER | SWT.MULTI | SWT.WRAP);
		
			gd = new GridData();
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = GridData.FILL;
			gd.verticalAlignment = GridData.FILL;
			gd.verticalSpan = 15;
			SQL.setLayoutData(gd);

			executeButton = new Button(c, SWT.PUSH);
			executeButton.setText(" Execute ");
			gd = new GridData();
			gd.horizontalAlignment = GridData.END;
			executeSQL = new ExecuteSQLPressed(tabs, results, SQL, tv,this);
			executeButton.addSelectionListener(executeSQL);
			
			Button b = new Button(c, SWT.PUSH);
			b.setText(" Clear Browser ");
			gd = new GridData();
			gd.horizontalAlignment = GridData.BEGINNING;
			b.setLayoutData(gd);
			b.addSelectionListener(executeSQL);

			l = new Label(c,SWT.NONE);
			
			history = new List(c,SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
			gd = new GridData();
			gd.horizontalSpan = 2;
			gd.horizontalAlignment = GridData.FILL;
			gd.verticalAlignment = GridData.FILL;
			gd.verticalSpan = 14;
			gd.widthHint = 200;
			history.setLayoutData(gd);
			history.addSelectionListener(new SelectionListener(){

				public void widgetSelected(SelectionEvent arg0) {
					JdbcLdapBrowser.this.SQL.setText(JdbcLdapBrowser.this.history.getSelection()[0]);
					
				}

				public void widgetDefaultSelected(SelectionEvent arg0) {
					// TODO Auto-generated method stub
					
				}});
			
			
			this.loadHistory();
		//} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();

		//}
	}

	/**
	 * @param parent
	 */
	private void createLDAPContents(Composite parent) {
		try {

			

			GridLayout gr = new GridLayout();

			gr.numColumns = 1;
			gr.makeColumnsEqualWidth = false;

			parent.setLayout(gr);

			tabs = new CTabFolder(parent, SWT.TOP | SWT.BORDER);
			tabs.setSimple(false);
			tabs.setSelectionForeground(parent.getDisplay().getSystemColor(SWT.COLOR_TITLE_FOREGROUND));
			/*tabs.setSelectionBackground(new Color[]{parent.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND), 
				parent.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT),
				parent.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT), 
				parent.getDisplay().getSystemColor(SWT.COLOR_WHITE)},
				new int[] {75, 75, 75},true);*/
			tabs.setSelectionBackground(parent.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND));
			FontData fd = new FontData();
					fd.setName(tabs.getFont().getFontData()[0].getName());
					fd.setHeight(tabs.getFont().getFontData()[0].getHeight());
					fd.setStyle(SWT.BOLD);
		
					tabFont = new Font(parent.getDisplay(),fd);
					tabs.setFont(tabFont);
			GridLayout gl = new GridLayout();
			gl.numColumns = 1;

			//tabs.setLayout(new FillLayout());
			GridData gd = new GridData();
			gd.grabExcessVerticalSpace = true;
			//tabs.setLayoutData(gd);
			gd.grabExcessHorizontalSpace = true;
			gd.verticalAlignment = GridData.FILL;
			gd.horizontalAlignment = GridData.FILL;
			tabs.setLayoutData(gd);

			CTabItem ti = new CTabItem(tabs, SWT.NONE);
			ti.setText("Tree View");

			SashForm sash_form = new SashForm(tabs, SWT.HORIZONTAL | SWT.NULL);
			/*gd  = new GridData();
			gd.horizontalAlignment = GridData.FILL;
			gd.verticalAlignment = GridData.FILL;
			gd.grabExcessHorizontalSpace = true;
			gd.grabExcessVerticalSpace = true;
			sash_form.setLayoutData(gd);*/

			ti.setControl(sash_form);

			tabs.setSelection(0);

			tv = new TreeViewer(sash_form);
			tv.getTree().addKeyListener(new GetKey(this));
			
			tv.setContentProvider(new DirTree(this));
			tv.setInput(new TreeObject(this.baseDN, null, this.baseDN));

			final TableViewer tbv = new TableViewer(sash_form, SWT.BORDER | SWT.FULL_SELECTION);
			
			tbv.setContentProvider(new AttributesList(this));
			tbv.setLabelProvider(new AttributeLabel());

			TableColumn column = new TableColumn(tbv.getTable(), SWT.LEFT);
			column.setText("Name");
			column.setWidth(75);
			column = new TableColumn(tbv.getTable(), SWT.LEFT);
			column.setText("Value");
			column.setWidth(200);
			tbv.getTable().setHeaderVisible(true);
			tv.addSelectionChangedListener(new ISelectionChangedListener() {
				
				
				public void selectionChanged(SelectionChangedEvent event) {
					
					IStructuredSelection selection =
						(IStructuredSelection) event.getSelection();

					Object selectedNode = selection.getFirstElement();
					tbv.setInput(selectedNode);
				}
			});
			
			ShowPopup pop = new ShowPopup(this);
			tv.getTree().addMouseListener(pop);
			tv.getTree().addKeyListener(pop);

			//Composite c = new Composite(tabs,SWT.NONE);
			//c.setLayout(new FillLayout());
			Table tbvDB = new Table(tabs, SWT.BORDER | SWT.FULL_SELECTION);
			//		gd  = new GridData();
			//				gd.horizontalAlignment = GridData.FILL;
			//				gd.verticalAlignment = GridData.FILL;
			//				gd.grabExcessHorizontalSpace = true;
			//				gd.grabExcessVerticalSpace = true;
			//		  tbvDB.setLayoutData(gd);
			tbvDB.setLinesVisible(true);
			tbvDB.setHeaderVisible(true);
			DbLabelProvider dbLbl = new DbLabelProvider();
			//tbvDB.setLabelProvider(dbLbl);
			//tbvDB.setContentProvider(new DbTable());

			

			ti = new CTabItem(tabs, SWT.NONE);
			ti.setControl(tbvDB);
			ti.setText("Table View");

			//GridData gd;

			Composite c = new Composite(parent, SWT.NONE);
			gd = new GridData();
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = GridData.FILL;
			c.setLayoutData(gd);

			gr = new GridLayout();
			gr.numColumns = 4;
			gr.makeColumnsEqualWidth = false;
			c.setLayout(gr);

			Label l = new Label(c, SWT.NONE);
			l.setText("SQL : ");
			gd = new GridData();
			gd.horizontalAlignment = GridData.BEGINNING;
			l.setLayoutData(gd);

			SQL = new Text(c, SWT.BORDER | SWT.MULTI | SWT.WRAP);
			
			gd = new GridData();
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = GridData.FILL;
			gd.verticalAlignment = GridData.FILL;
			gd.verticalSpan = 15;
			SQL.setLayoutData(gd);

			
			
			
			
			executeButton = new Button(c, SWT.PUSH);
			executeButton.setText(" Execute ");
			gd = new GridData();
			gd.horizontalAlignment = GridData.END;
			executeSQL = new ExecuteSQLPressed(tabs, results, SQL, tv,this);
			executeButton.addSelectionListener(executeSQL);
			Button b = new Button(c, SWT.PUSH);
			b.setText(" Clear Browser ");
			gd = new GridData();
			gd.horizontalAlignment = GridData.BEGINNING;
			b.setLayoutData(gd);
			b.addSelectionListener(executeSQL);

			l = new Label(c,SWT.NONE);
			
			history = new List(c,SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
			gd = new GridData();
			gd.horizontalSpan = 2;
			gd.horizontalAlignment = GridData.FILL;
			gd.verticalAlignment = GridData.FILL;
			gd.verticalSpan = 14;
			gd.widthHint = 200;
			history.setLayoutData(gd);
			history.addSelectionListener(new SelectionListener(){

				public void widgetSelected(SelectionEvent arg0) {
					JdbcLdapBrowser.this.SQL.setText(JdbcLdapBrowser.this.history.getSelection()[0]);
					
				}

				public void widgetDefaultSelected(SelectionEvent arg0) {
					// TODO Auto-generated method stub
					
				}});
			
			
			this.loadHistory();
			
			c = new Composite(parent, SWT.NONE);
			gl = new GridLayout();
			gl.numColumns = 4;
			gl.makeColumnsEqualWidth = true;
			c.setLayout(gl);
			gd = new GridData();
			gd.horizontalAlignment = GridData.CENTER;
			c.setLayoutData(gd);
			Button baseScope = new Button(c, SWT.RADIO);
			baseScope.setText("Base Scope");
			baseScope.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					results.scope = "objectScope";
				}
			});

			Button oneLevelScope = new Button(c, SWT.RADIO);
			oneLevelScope.setText("One Level Scope");
			oneLevelScope.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					results.scope = "oneLevelScope";
				}
			});

			Button subTreeScope = new Button(c, SWT.RADIO);
			subTreeScope.setText("Sub Tree Scope");
			subTreeScope.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					results.scope = "subTreeScope";
				}
			});

			Button inSqlScope = new Button(c, SWT.RADIO);
			inSqlScope.setSelection(true);
			inSqlScope.setText("Scope In SQL");
			inSqlScope.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					results.scope = "";
				}
			});

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		//return null;
	}

	public void setLastSQL(String sql) {
		this.lastSQL = sql;
	}
	
	public String getLastSQL() {
		return this.lastSQL;
	}
	
	public  String insertScope(String SQL) {
			if (results.scope.trim().length() != 0) {
				String lsql = SQL.toLowerCase();
				int begin;
				if (lsql.startsWith("select") || lsql.startsWith("delete")) {
			
				
					begin = lsql.indexOf("from") + 5;
				
				}
				else {
					if (lsql.startsWith("update entry")) {
						begin = lsql.indexOf("update entry") + 13 ;
					
					}
					else {
						begin = lsql.indexOf("update") + 7;
					}
				}
			
				if (begin - 1 < SQL.length()) {
					SQL =
						SQL.substring(0, begin)
							+ results.scope
							+ ";"
							+ SQL.substring(begin);
				}
				else {
					SQL += " " + results.scope + ";";
				}
			
			
			}
			return SQL;
		}
	
	public void copyName() {
		TreeItem[] items = this.tv.getTree().getSelection();
		
		Object obj = items[0].getData();
		TextTransfer text_transfer = TextTransfer.getInstance();
		if (obj instanceof TreeObject) {
			TreeObject to = (TreeObject) items[0].getData();
			
			
			clipboard.setContents(new Object[] {to.getBase()},new Transfer[] {text_transfer});
		} else {
			JdbcTreeObject to = (JdbcTreeObject) items[0].getData();
			
			String toCopy = null;
			if (to.parentName == null) {
				toCopy = to.toString();
			} else {
				toCopy = to.parentName + "." + to.toString();
			}
		
			
			
			clipboard.setContents(new Object[] { toCopy},new Transfer[] {text_transfer});
		}
		
		
		
		
		
	}
	
	public void executeSQL() {
		
		this.executeSQL.executeCMD(this.executeButton.getText());
		
	}

	/**
	 * @param sname
	 */
	public void setName(String sname) {
		this.name = sname;
		
	}
	
	public void loadHistory() {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		String hist = prefs.get("JDBC_LDAP_HISTORY_" + name,"");
		
		StringTokenizer toker = new StringTokenizer(hist,"|");
		
		while (toker.hasMoreTokens()) {
			history.add(new String(Base64.decode(toker.nextToken())));
		}
	}
}

class ExecuteSQLPressed extends SelectionAdapter {

	CTabFolder tabs;
	ResultLoader results;
	Text sql;
	TreeViewer tv;
	String scope;
	JdbcLdapBrowser browser;
	public ExecuteSQLPressed(
		CTabFolder tabs,
		ResultLoader results,
		Text sql,
		TreeViewer tv,
		JdbcLdapBrowser browser) {
		this.tabs = tabs;
		this.tv = tv;
		this.results = results;
		this.sql = sql;
		this.browser = browser;

	}

	public void widgetSelected(SelectionEvent event) {

		String buttonname = ((Button) event.widget).getText();
		executeCMD(buttonname);
	}

	/**
	 * @param buttonname
	 */
	public void executeCMD(String buttonname) {
		tabs.getItem(tabs.getItemCount() - 1).dispose();

		Table tbvDB = new Table(tabs, SWT.BORDER | SWT.FULL_SELECTION);
		//		GridData gd  = new GridData();
		//				gd.horizontalAlignment = GridData.FILL;
		//				gd.verticalAlignment = GridData.FILL;
		//				gd.grabExcessHorizontalSpace = true;
		//				gd.grabExcessVerticalSpace = true;
		//				tbvDB.setLayoutData(gd);
		//results.table = tbvDB;
		tbvDB.setLinesVisible(true);
		tbvDB.setHeaderVisible(true);
		//DbLabelProvider dbLbl = new DbLabelProvider();
		//tbvDB.setLabelProvider(dbLbl);

		CTabItem ti = new CTabItem(tabs, SWT.NONE);
		ti.setControl(tbvDB);
		ti.setText("Table View");
		if (buttonname.trim().equalsIgnoreCase("execute")) {

			String orgSQL = sql.getText();
			String SQL = sql.getText().replace('\n',' ');
			System.out.println(SQL);
			int index = this.browser.history.indexOf(orgSQL);
			if (this.browser.history.indexOf(orgSQL) >= 0) {
				this.browser.history.remove(index);
				 
			}
			this.browser.history.add(orgSQL,0);
			
			if (this.browser.history.getItemCount() >= JdbcLdapBrowserApp.app.historyLimit) {
				this.browser.history.remove(JdbcLdapBrowserApp.app.historyLimit,browser.history.getItemCount() - 1);
			}
			
			storeHistory();
			if (SQL.toLowerCase().startsWith("select")) {
			
			
				SQL = addScopeAndDN(SQL);
	
				try {
					results.loadResults(SQL, tbvDB, tv);
					this.browser.setLastSQL(SQL);
					
					
					
				}
				catch (SQLException e) {
					MessageDialog.openError(this.tabs.getShell(),"Error",e.toString());
					return;
				}
				//tbvDB.setInput(results.getTableView());
				TableColumn[] tcs = tbvDB.getColumns();
				for (int i = 0, m = tcs.length; i < m; i++) {
					tcs[i].pack();
				}
				tabs.setSelection(ti);
			}
			else {
				if (SQL.toLowerCase().startsWith("update") && ! browser.isDB) {
					SQL = browser.insertScope(SQL);
				}
				
				try {
					int result = this.results.executeUpdate(SQL);
					
					MessageDialog.openInformation(this.tabs.getShell(),"SQL Update","Records Effected : " + result);
				}
				catch (SQLException sql) {
					MessageDialog.openError(this.tabs.getShell(),"Error Occurred",sql.toString());
				}
				
			}
		}
		else {
			
				browser.setLastSQL(null);
				if (! browser.isDB) {
					tv.setInput(new TreeObject(results.browser.baseDN, null, results.browser.baseDN));
				} else {
					tv.setInput(browser);
				}
			
		}
	}

	/**
	 * 
	 */
	private void storeHistory() {
		StringBuffer buff = new StringBuffer();
		String[] hist = browser.history.getItems();
		for (int i=0,m=hist.length;i<m;i++) {
			buff.append(Base64.encode(hist[i]));
			if (i + 1 < m) {
				buff.append('|');
			}
		}
		
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		prefs.put("JDBC_LDAP_HISTORY_" + browser.name,buff.toString());
	}

	/**
	 * @param SQL
	 * @return
	 */
	private String addScopeAndDN(String SQL) {
		int index;
		if (! browser.isDB) {
			SQL = browser.insertScope(SQL);
		
		
			String lsql = SQL.toLowerCase();
			index = lsql.indexOf(" from");
			String attrs = lsql.substring(6,index);
			StringTokenizer toker = new StringTokenizer(attrs);
			boolean hasdn = false;
			while (toker.hasMoreTokens()) {
				String token = toker.nextToken();
				hasdn = hasdn || token.equals("dn") || token.equals("*"); 
			}
			
			if (! hasdn) {
				SQL = "SELECT DN," + SQL.substring(6);
			}
		}
		//System.out.println("SQL : " + SQL);
		return SQL;
	}

	

}

class ResizeEvent extends ControlAdapter {
	JdbcLdapBrowser browser;
	public ResizeEvent(JdbcLdapBrowser browser) {
		this.browser = browser;
	}
	public void controlResized(ControlEvent e) {
		if (browser.tabs != null && !browser.tabs.isDisposed()) {
			//System.out.println("in here");
			//browser.tabs.redraw();
			browser.tabs.getSelection().getControl().forceFocus();
		}
	}
}

class GetKey implements KeyListener {
	JdbcLdapBrowser browser;
	
	public GetKey(JdbcLdapBrowser browser) {
		this.browser = browser;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {
		if (e.stateMask == SWT.CONTROL && e.keyCode == 99) {
			browser.copyName();
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

}

class ShowPopup implements MouseListener, KeyListener {
	JdbcLdapBrowser browser;
	
	long keyCode = -1;
	
	
	public ShowPopup(JdbcLdapBrowser browser) {
		this.browser = browser;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseDoubleClick(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseDown(MouseEvent e) {
		
		if (e.button == 3 || (e.button == 1 && this.keyCode == 262144 )) {
			if (e.getSource() instanceof Tree) {
				Tree tree = (Tree) e.getSource();
				Menu popup = new Menu(tree);
				Point pt = tree.toDisplay(new Point(e.x, e.y));
				popup.addMenuListener(new DrawTreeMenu(browser));
				popup.setLocation (pt.x, pt.y);
				popup.setVisible (true);
				Display display = tree.getDisplay ();
				while (popup != null && popup.isVisible ()) {
					if (!display.readAndDispatch ()) display.sleep ();
				}
				if (popup != null) {
					popup.dispose ();
					popup = null;
				}
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseUp(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
		this.keyCode = e.keyCode;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		this.keyCode = -1;
	}
	
}

class DrawTreeMenu implements MenuListener {
	
	JdbcLdapBrowser browser;
	
	public DrawTreeMenu(JdbcLdapBrowser browser) {
		this.browser = browser;
	}
	
	public void menuHidden(MenuEvent e) {
		
	}
	
	public void menuShown(MenuEvent e) {
		
//		first dispose of current entries
			  Menu popup = (Menu) e.widget;
			  MenuItem mis[] = popup.getItems();
			  for (int i=0,m=mis.length;i<m;i++) {
				  mis[i].dispose();
			  }
		
			  
			 JdbcLdapBrowserApp.drawEditMenu(popup,true);
			  
			  /*Iterator browsers = app.browsers.keySet().iterator();
		
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
			  }*/
	}

	
}
