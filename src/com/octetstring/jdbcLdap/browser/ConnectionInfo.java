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
import java.util.Iterator;

import org.eclipse.jface.dialogs.MessageDialog;

/**
 * @author Marc Boorshtein
 *
 */
public class ConnectionInfo {
	Text server,port,user,password,pass2;
	
	Button fetchDns,followReferrals,dsml,ssl,spml,ldap;
	Combo base;
	Combo name;
	
	Text extraURL;
	
	String sserver,sport,suser,spassword,sbase,sname;
	
	Shell shell;
	
	boolean doOpen,bfollowReferrals, bIsDSML,bSsl,bspml,bjdbc;
	
	ConfigStore configs;

	protected Button jdbc;

	protected Label serverlabel;

	protected Label serverport;

	public String textraURL;
	
	
	public void showWindow(Display display) {
		try {
			configs = new ConfigStore();
		} catch (Exception e) {
			e.printStackTrace(System.out);
			MessageDialog.openError(shell,"Error Occurred While Loading Configs","The error : " + e.toString());
		}
		//System.out.println("before create");	
			
			//System.out.println("before draw");
			//w.setBlockOnOpen(true);
			//w.open();
			//Display display = new Display();

			this.shell = new Shell(display);
			shell.setText("Enter Connection Information");			shell.setImage(JdbcLdapBrowserApp.open);
			createContents(shell);
			shell.pack();
			shell.open();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) display.sleep();
			}
	}
	
	
	protected void loadNames() {
		if (this.configs == null) return;
		this.name.removeAll();
		
		Iterator it = configs.getConfigLabels().iterator();
		while (it.hasNext()) {
			this.name.add(it.next().toString());
		}
	}
	
	private void createContents(Shell parent) {
		GridLayout gl = new GridLayout();
		gl.numColumns = 4;
		gl.makeColumnsEqualWidth = true;
		parent.setLayout(gl);
		GridData gr;
		
		Label l = new Label(parent,SWT.NONE);
		l.setText("Name ");
		gr = new GridData();
		gr.horizontalSpan = 1;
		gr.horizontalAlignment = GridData.FILL;
		

		name = new Combo(parent,SWT.BORDER);
		gr = new GridData();
		gr.horizontalSpan=3;
		gr.horizontalAlignment = GridData.FILL;
		gr.grabExcessHorizontalSpace = true;
		name.setLayoutData(gr);
		
		if (System.getProperty("os.name").equals("Mac OS X")) {
			
			name.addPaintListener(new LoadCfg(this));
		} else {
			name.addSelectionListener(new LoadCfg(this));
		}

		loadNames();
		
		
		
		
		
		
		
		Button addCfg = new Button(parent,SWT.PUSH);
		addCfg.setText(" Save ");
		//parent.setDefaultButton(addCfg);
		gr = new GridData();
		gr.horizontalSpan = 2;
		gr.horizontalAlignment = GridData.END;
		addCfg.setLayoutData(gr);
		addCfg.addSelectionListener(new ManageConfig(true,this));
		
		Button delCfg = new Button(parent,SWT.PUSH);
		delCfg.setText(" Delete ");
		
		gr = new GridData();
		gr.horizontalSpan = 2;
		gr.horizontalAlignment = GridData.BEGINNING;
		delCfg.setLayoutData(gr);
		delCfg.addSelectionListener(new ManageConfig(false,this));
		
		
		
		serverlabel = new Label(parent,SWT.NONE);
		serverlabel.setText("Server              ");
		gr = new GridData();
		gr.horizontalSpan = 1;
		gr.horizontalAlignment = GridData.FILL;
		
		server = new Text(parent,SWT.BORDER);
		gr = new GridData();
		gr.horizontalSpan=3;
		gr.horizontalAlignment = GridData.FILL;
		gr.grabExcessHorizontalSpace = true;
		server.setLayoutData(gr);
		
		serverport = new Label(parent,SWT.NONE);
				serverport.setText("Port          ");
				gr = new GridData();
				gr.horizontalSpan = 1;
				gr.horizontalAlignment = GridData.FILL;
		
		port = new Text(parent,SWT.BORDER);
				gr = new GridData();
				gr.horizontalSpan=3;
				gr.horizontalAlignment = GridData.FILL;
				gr.grabExcessHorizontalSpace = true;
				port.setLayoutData(gr);
				
		l = new Label(parent,SWT.NONE);
										l.setText("Base (Leave blank for all)");
										gr = new GridData();
										gr.horizontalSpan = 1;
										gr.horizontalAlignment = GridData.FILL;
								
				base = new Combo(parent,SWT.DROP_DOWN);
										gr = new GridData();
										gr.horizontalSpan=3;
										gr.horizontalAlignment = GridData.FILL;
										gr.grabExcessHorizontalSpace = true;
										base.setLayoutData(gr);
										
				fetchDns = new Button(parent,SWT.PUSH);
				fetchDns.setText("Retrieve Bases");
		gr = new GridData();
						gr.horizontalSpan=4;
						gr.horizontalAlignment = GridData.FILL;
						gr.grabExcessHorizontalSpace = true;
						fetchDns.setLayoutData(gr);
						
				
		l = new Label(parent,SWT.NONE);
						l.setText("Username ");
						gr = new GridData();
						gr.horizontalSpan = 1;
						gr.horizontalAlignment = GridData.FILL;
						
		user = new Text(parent,SWT.BORDER);
						gr = new GridData();
						gr.horizontalSpan=3;
						gr.horizontalAlignment = GridData.FILL;
						gr.grabExcessHorizontalSpace = true;
						user.setLayoutData(gr);
						
		
						
		l = new Label(parent,SWT.NONE);
		l.setText("Password ");
		gr = new GridData();
		gr.horizontalSpan = 1;
		gr.horizontalAlignment = GridData.FILL;
								
		password = new Text(parent,SWT.BORDER);
		gr = new GridData();
		gr.horizontalSpan=3;
		gr.horizontalAlignment = GridData.FILL;
		gr.grabExcessHorizontalSpace = true;
		password.setLayoutData(gr);
		password.setEchoChar('*');
		
		
		
		l = new Label(parent,SWT.NONE);
		l.setText("Password Confirm");
		gr = new GridData();
		gr.horizontalSpan = 1;
		gr.horizontalAlignment = GridData.FILL;
								
		pass2 = new Text(parent,SWT.BORDER);
		gr = new GridData();
		gr.horizontalSpan=3;
		gr.horizontalAlignment = GridData.FILL;
		gr.grabExcessHorizontalSpace = true;
		pass2.setLayoutData(gr);
		pass2.setEchoChar('*');
		
		l = new Label(parent,SWT.NONE);
		l.setText("Extra URL Options");
		gr = new GridData();
		gr.horizontalSpan = 1;
		gr.horizontalAlignment = GridData.FILL;
								
		this.extraURL = new Text(parent,SWT.BORDER);
		gr = new GridData();
		gr.horizontalSpan=3;
		gr.horizontalAlignment = GridData.FILL;
		gr.grabExcessHorizontalSpace = true;
		extraURL.setLayoutData(gr);
		
		

		ssl = new Button(parent,SWT.CHECK);
		ssl.setText("Use SSL/TLS");
		gr = new GridData();
		gr.horizontalSpan = 4;
		gr.horizontalAlignment = GridData.CENTER;
		ssl.setLayoutData(gr);
		ssl.setVisible(true);
		
		
		l = new Label(parent,SWT.NONE);
		l.setText("Connection Type :");
		
		ldap = new Button(parent,SWT.RADIO);
		ldap.setText("LDAPv3");
		ldap.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent arg0) {
				ConnectionInfo.this.serverlabel.setText("Server ");
				ConnectionInfo.this.serverport.setText("Port ");
				ConnectionInfo.this.base.setEnabled(true);
				ConnectionInfo.this.fetchDns.setEnabled(true);
				ConnectionInfo.this.ssl.setEnabled(true);
			}

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}});
		
		dsml = new Button(parent,SWT.RADIO);
		dsml.setText("DSMLv2");
		//gr = new GridData();
		//gr.horizontalSpan = 2;
		//gr.horizontalAlignment = GridData.BEGINNING;
		//dsml.setLayoutData(gr);
		dsml.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent arg0) {
				if (dsml.getSelection()) {
					try {
						Class.forName("com.novell.ldap.DSMLConnection");
					} catch (Throwable e) {
						MessageDialog.openError(shell,"DSMLv2 Not Available","This version of the SQL Directory Browser was not compiled with DSMLv2 support");
						return;
					}
				}
				
				ConnectionInfo.this.serverlabel.setText("Server ");
				ConnectionInfo.this.serverport.setText("Port ");
				port.setEnabled(! dsml.getSelection() && ! spml.getSelection());
				ssl.setEnabled(! dsml.getSelection() && ! spml.getSelection());
				
			}

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}});
			
		spml = new Button(parent,SWT.RADIO);
		spml.setText("SPML");
		spml.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent arg0) {
				if (spml.getSelection()) {
					try {
						Class.forName("com.novell.ldap.SPMLConnection");
					} catch (Throwable e) {
						MessageDialog.openError(shell,"SPML Not Available","This version of the SQL Directory Browser was not compiled with SPMLv2 support");
						return;
					}
				}
				port.setEnabled(! spml.getSelection()&& ! dsml.getSelection());
				ssl.setEnabled(! spml.getSelection()&& ! dsml.getSelection());
				if (spml.getSelection()) {
					base.setText("ou=Users,dc=spml,dc=com");
				}
				base.setEnabled(! spml.getSelection());
				ConnectionInfo.this.fetchDns.setEnabled(! spml.getSelection());
				ConnectionInfo.this.serverlabel.setText("Server ");
				ConnectionInfo.this.serverport.setText("Port ");
			}

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}});
		
		jdbc = new Button(parent,SWT.RADIO);
		jdbc.setText("JDBC Connection");
		gr = new GridData();
		gr.horizontalSpan = 6;
		gr.horizontalAlignment = GridData.CENTER;
		jdbc.setLayoutData(gr);
		jdbc.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent arg0) {
				ConnectionInfo.this.serverlabel.setText("JDBC Driver ");
				ConnectionInfo.this.serverport.setText("JDBC URL ");
				
				ConnectionInfo.this.base.setEnabled(false);
				ConnectionInfo.this.fetchDns.setEnabled(false);
				ConnectionInfo.this.ssl.setEnabled(false);
				
			}

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}});
		
		followReferrals = new Button(parent,SWT.CHECK);
		followReferrals.setText("Follow Referrals");
		gr = new GridData();
		gr.horizontalSpan = 4;
		gr.horizontalAlignment = GridData.END;
		followReferrals.setLayoutData(gr);
		followReferrals.setVisible(false);
		
		
		
		
		followReferrals = new Button(parent,SWT.CHECK);
		followReferrals.setText("Follow Referrals");
		gr = new GridData();
		gr.horizontalSpan = 6;
		gr.horizontalAlignment = GridData.END;
		followReferrals.setLayoutData(gr);
		followReferrals.setVisible(false);
					
		fetchDns.addSelectionListener(new FetchBasePressed(shell,server,port,user,password,base,dsml,ssl,spml,extraURL));
		Button ok = new Button(parent,SWT.PUSH);
		ok.setText(" OK ");
		parent.setDefaultButton(ok);
		gr = new GridData();
		gr.horizontalSpan = 2;
		gr.horizontalAlignment = GridData.END;
		ok.setLayoutData(gr);
		ok.addSelectionListener(new OkPressed(this));
		
		Button quit = new Button(parent,SWT.PUSH);
				quit.setText(" QUIT ");
			
				gr = new GridData();
				gr.horizontalSpan = 2;
				gr.horizontalAlignment = GridData.BEGINNING;
				quit.setLayoutData(gr);
				quit.addSelectionListener(new QuitPressed(shell,this));
		
				
		password.addKeyListener(new CheckPasswords(ok,addCfg,password,pass2));
		pass2.addKeyListener(new CheckPasswords(ok,addCfg,password,pass2));
	}
}


class OkPressed extends SelectionAdapter {
	ConnectionInfo con;
	public OkPressed(ConnectionInfo con) {
		this.con = con;
	}
	
	public void widgetSelected(SelectionEvent event) {
		con.sserver = con.server.getText();
		con.sport = con.port.getText();
		con.sbase = con.base.getText();
		con.suser = con.user.getText();
		con.spassword = con.password.getText();
		con.sname = con.name.getText();
		con.doOpen = true;
		con.bfollowReferrals = con.followReferrals.getSelection();
		con.bIsDSML = con.dsml.getSelection();
		con.bSsl = con.ssl.getSelection();
		con.bspml = con.spml.getSelection();
	
		con.bjdbc = con.jdbc.getSelection();
		con.textraURL = con.extraURL.getText();
		con.shell.dispose();	
	}
}

class QuitPressed extends SelectionAdapter {
	Shell shell;
	ConnectionInfo con;
	public QuitPressed(Shell shell,ConnectionInfo con) {
		this.shell = shell;
		this.con = con;
	}
	
	public void widgetSelected(SelectionEvent event) {
		con.doOpen = false;
		shell.dispose();
		//System.exit(0);	
	}
}

class FetchBasePressed extends SelectionAdapter {
	Shell shell;
	Text server,user,port,pass,extraURL;
	Button dsml,spml;
	Combo base;
	private Button ssl;
	public FetchBasePressed(Shell shell,Text server,Text port,Text user,Text pass, Combo base,Button dsml,Button ssl,Button spml,Text extraURL) {
		this.shell = shell;
		this.server = server;
		this.port = port;
		this.user = user;
		this.pass = pass;
		this.base = base;
		this.dsml = dsml;
		this.spml = spml;
		this.ssl = ssl;
		this.extraURL  = extraURL;
	}

	public void widgetSelected(SelectionEvent event) {
		try {
			Connection con = JdbcLdapBrowser.createConnection(server.getText(),port.getText(),"",user.getText(),pass.getText(),false,dsml.getSelection(),ssl.getSelection(),spml.getSelection(),extraURL.getText());
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery("SELECT namingContexts FROM objectScope;");
			base.removeAll();
			String first = null;
			while (rs.next()) {
				if (first == null) {
					first = rs.getString(1);
				}
				base.add(rs.getString(1));
			}
			
			base.setText(first);
		}
		catch (Exception sql) {
			sql.printStackTrace();
			MessageDialog.openError(shell,"Error Occurred In Fetching Base",sql.toString());
		}
	}
}

class ManageConfig extends SelectionAdapter {
	ConnectionInfo cons;
	boolean add;
	
	public ManageConfig(boolean add,ConnectionInfo cons) {
		this.cons = cons;
		this.add = add;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		//System.out.println(e.widget.getClass().getName());
		if (e.widget instanceof Combo) {
			ConnectionStore cs = cons.configs.getConnectionInfo(cons.name.getText());
			if (cs == null) return;
			cons.server.setText(cs.server != null ? cs.server : "");
			cons.port.setText(cs.port != null ? cs.port : "");
			cons.base.setText(cs.base != null ? cs.base : "");
			cons.user.setText(cs.user != null ? cs.user : "");
			cons.password.setText(cs.pass != null ? cs.pass : "");
			cons.dsml.setSelection(cs.isDsml);
			cons.followReferrals.setSelection(cs.followReferrals);
			
			
			cons.ssl.setSelection(cs.isSSL);
			cons.spml.setSelection(cs.isSpml);
			cons.ldap.setSelection(! (cs.isSpml || cs.isDsml || cs.isJDBC));
			
			if (cons.jdbc.getSelection()) {
				cons.serverlabel.setText("JDBC Driver ");
				cons.serverlabel.setText("JDBC URL ");
			}
			
			cons.extraURL.setText(cs.extraUrl);
		}
		else {
			String name = null;
			if (add) {
				try {
					//System.out.println("base : " + cons.base.getText());
					name = cons.name.getText();
					cons.configs.saveConfig(cons.name.getText(),cons.user.getText(),cons.password.getText(),cons.server.getText(),cons.port.getText(),cons.base.getText(),cons.dsml.getSelection(),cons.followReferrals.getSelection(),cons.ssl.getSelection(),cons.spml.getSelection(),cons.jdbc.getSelection(),cons.extraURL.getText());
				} catch (Exception e1) {
					e1.printStackTrace();
					MessageDialog.openError(cons.shell,"Error Occurred When Loading Config",e1.toString());
				}
				
			}
			else {
				try {
					cons.configs.deleteConfig(cons.name.getText());
				}
				catch (Exception e1) {
					e1.printStackTrace();
					MessageDialog.openError(cons.shell,"Error Occurred When Deleting Config",e1.toString());
				}
			}
			
			cons.loadNames();
			
			if (name != null) {
				cons.name.setText(name);
			}
		}
	}


}

class LoadCfg implements PaintListener,SelectionListener,ModifyListener {
	ConnectionInfo cons;
	String current = "";
	
	public LoadCfg(ConnectionInfo cons) {
		this.cons = cons;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
	 */
	public void paintControl(PaintEvent e) {
		loadConfig();
	}
	/**
	 * 
	 */
	private void loadConfig() {
		String curr = cons.name.getText();

		if (! curr.equals(current)) {
			
			
			this.current = curr;
			ConnectionStore cs = cons.configs.getConnectionInfo(this.current);
			if (cs == null) return;
			cons.server.setText(cs.server != null ? cs.server : "");
			cons.port.setText(cs.port != null ? cs.port : "");
			cons.base.setText(cs.base!= null ? cs.base : "");
			cons.user.setText(cs.user != null ? cs.user : "");
			cons.password.setText(cs.pass != null ? cs.pass : "");
			cons.pass2.setText(cs.pass != null ? cs.pass : "");
			cons.dsml.setSelection(cs.isDsml);
			cons.followReferrals.setSelection(cs.followReferrals);
			cons.ssl.setSelection(cs.isSSL);
			
			cons.spml.setSelection(cs.isSpml);
			cons.ldap.setSelection(! (cs.isSpml || cs.isDsml || cs.isJDBC));
			
			cons.jdbc.setSelection(cs.isJDBC);
			
			cons.port.setEnabled(! cons.dsml.getSelection() && ! cons.spml.getSelection());
			cons.ssl.setEnabled(! cons.dsml.getSelection() && ! cons.spml.getSelection() && ! cons.jdbc.getSelection());
			
			if (cons.spml.getSelection() || cons.jdbc.getSelection()) {
				cons.base.setEnabled(false);
				cons.fetchDns.setSelection(false);
			} 
			
			if (cons.jdbc.getSelection()) {
				cons.serverlabel.setText("JDBC Driver ");
				cons.serverlabel.setText("JDBC URL ");
			}
				
			cons.extraURL.setText(cs.extraUrl);
			
			Control[]  ctls = cons.shell.getChildren();
			for (int i=0,m=ctls.length;i<m;i++) {
				ctls[i].redraw();
			}
			
			cons.shell.redraw();
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent arg0) {
		this.loadConfig();
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
	 */
	public void modifyText(ModifyEvent arg0) {
		this.loadConfig();
		
	}
	
	
}

class CheckPasswords implements KeyListener {

	Button save, ok;
	Text pass1,pass2;
	
	public CheckPasswords(Button save, Button ok, Text pass1, Text pass2) {
		this.save = save;
		this.ok = ok;
		this.pass1 = pass1;
		this.pass2 = pass2;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
	 */
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
	 */
	public void keyReleased(KeyEvent arg0) {
		save.setEnabled(pass1.getText().equals(pass2.getText()));
		ok.setEnabled(pass1.getText().equals(pass2.getText()));
		
	}
	
}
