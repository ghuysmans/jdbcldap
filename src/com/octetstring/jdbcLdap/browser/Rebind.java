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

import com.novell.ldap.*;
import com.octetstring.jdbcLdap.jndi.*;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
/**
 * @author Marc Boorshtein
 *
 */
public class Rebind {
	JndiLdapConnection con;
	ConnectionStore conInfo;
	
	Text user,pass,pass2;
	
	Display display;
	
	Shell parent;
	
	public Rebind(JndiLdapConnection con, ConnectionStore conInfo,Display display) {
		this.con = con;
		this.conInfo = conInfo;
		this.display = display;
		
		drawWindow();
		
		parent.setText("Rebind To Server");
		parent.setImage(JdbcLdapBrowserApp.rebind);
		parent.pack();
		parent.open();
		while (!parent.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
	}
	
	protected void drawWindow() {
		GridLayout gl = new GridLayout();
		gl.numColumns = 4;
		gl.makeColumnsEqualWidth = true;
		parent = new Shell(display);
		parent.setLayout(gl);
		
		GridData gr;
		
		Label l = new Label(parent,SWT.NONE);
		l.setText("Current Credentials : ");
		gr = new GridData();
		gr.horizontalSpan = 1;
		l.setLayoutData(gr);
		
		l = new Label(parent,SWT.NONE);
		l.setText(conInfo.user);
		gr = new GridData();
		gr.horizontalSpan = 3;
		gr.horizontalAlignment = GridData.FILL;
		gr.grabExcessHorizontalSpace = true;
		l.setLayoutData(gr);
		
		l = new Label(parent,SWT.NONE);
		l.setText("New Credentials : ");
		gr = new GridData();
		gr.horizontalSpan = 1;
		l.setLayoutData(gr);
		
		user = new Text(parent,SWT.BORDER);
		gr = new GridData();
		gr.horizontalSpan = 3;
		gr.horizontalAlignment = GridData.FILL;
		gr.grabExcessHorizontalSpace = true;
		user.setLayoutData(gr);
		
		
		l = new Label(parent,SWT.NONE);
		l.setText("Password : ");
		gr = new GridData();
		gr.horizontalSpan = 1;
		l.setLayoutData(gr);
		
		pass = new Text(parent,SWT.BORDER);
		gr = new GridData();
		gr.horizontalSpan = 3;
		gr.grabExcessHorizontalSpace = true;
		gr.horizontalAlignment = GridData.FILL;
		pass.setLayoutData(gr);
		pass.setEchoChar('*');
		
		
		l = new Label(parent,SWT.NONE);
		l.setText("Confirm Password : ");
		gr = new GridData();
		gr.horizontalSpan = 1;
		l.setLayoutData(gr);
		
		pass2 = new Text(parent,SWT.BORDER);
		gr = new GridData();
		gr.horizontalSpan = 3;
		gr.grabExcessHorizontalSpace = true;
		gr.horizontalAlignment = GridData.FILL;
		pass2.setLayoutData(gr);
		pass2.setEchoChar('*');
		
		Button ok = new Button(parent,SWT.PUSH);
		ok.setText(" OK ");
		gr = new GridData();
		gr.horizontalSpan  = 2;
		gr.horizontalAlignment = GridData.END;
		ok.setLayoutData(gr);
		ok.addSelectionListener(new DoRebind(this));
		parent.setDefaultButton(ok);
		
		Button cancel = new Button(parent,SWT.PUSH);
		cancel.setText(" Cancel ");
		gr = new GridData();
		gr.horizontalSpan  = 2;
		cancel.setLayoutData(gr);
		cancel.addSelectionListener(new QuitRebind(this));
		
		
		pass.addKeyListener(new CheckPasswordsRebind(ok,pass,pass2));
		pass2.addKeyListener(new CheckPasswordsRebind(ok,pass,pass2));
	}
}

class DoRebind extends SelectionAdapter {
	
	Rebind rebind;
	
	public DoRebind(Rebind rebind) {
		this.rebind = rebind;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		try {
			rebind.con.getConnection().bind(3,rebind.user.getText(),rebind.pass.getText());
			rebind.conInfo.user = rebind.user.getText();
			rebind.conInfo.pass = rebind.pass.getText();
			
			rebind.parent.dispose();
		} catch (LDAPException e1) {
			MessageDialog.openError(rebind.user.getShell(),"Error Occurred",e1.toString());
		}
	}

}

class QuitRebind extends SelectionAdapter {
	
	Rebind rebind;
	
	public QuitRebind(Rebind rebind) {
		this.rebind = rebind;
	}

	public void widgetSelected(SelectionEvent e) {
		rebind.parent.dispose();
	}
}

class CheckPasswordsRebind implements KeyListener {

	Button save, ok;
	Text pass1,pass2;
	
	public CheckPasswordsRebind(Button ok, Text pass1, Text pass2) {
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
		
		ok.setEnabled(pass1.getText().equals(pass2.getText()));
		
	}
	
}