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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author mlb
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class BrowserPrefs {
	private Shell shell;

	public BrowserPrefs(Display display) {
		shell = new Shell(display);
		
		shell.setText("SQL Directory Browser Preferences");
		
		shell.setLayout(new FillLayout());
		
		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		
		Composite top = new Composite(shell,SWT.NONE);
		
		top.setLayout(gl);
		
		Label l = new Label(top,SWT.NONE);
		l.setText("Maximum Number of Entries : ");
		
		final Text size = new Text(top,SWT.BORDER);
		size.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		size.setText(Integer.toString(JdbcLdapBrowserApp.app.sizeLimit));
		
		l = new Label(top,SWT.NONE);
		l.setText("JDBC Drivers Path : ");
		
		final Text driversPath = new Text(top,SWT.BORDER);
		driversPath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		driversPath.setText(JdbcLdapBrowserApp.app.driversDir);
		
		l = new Label(top,SWT.NONE);
		l.setText("Maximum Time Per Operation : ");
		
		final Text time = new Text(top,SWT.BORDER);
		time.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		time.setText(Integer.toString(JdbcLdapBrowserApp.app.timeLimit));
		
		l = new Label(top,SWT.NONE);
		l.setText("Number of statements to store in history : ");
		
		final Text history = new Text(top,SWT.BORDER);
		history.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		history.setText(Integer.toString(JdbcLdapBrowserApp.app.historyLimit));
		
		l = new Label(top,SWT.NONE);
		l.setText("Auto execute SQL from dialogs : ");
		final Button autoExecute = new Button(top,SWT.CHECK);
		autoExecute.setSelection(JdbcLdapBrowserApp.app.autoExec);
		
		
		
		Button ok = new Button(top,SWT.PUSH);
		ok.setText(" OK ");
		ok.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		ok.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent arg0) {
				JdbcLdapBrowserApp.app.sizeLimit = Integer.parseInt(size.getText());
				JdbcLdapBrowserApp.app.timeLimit = Integer.parseInt(time.getText());
				JdbcLdapBrowserApp.app.autoExec = autoExecute.getSelection();
				JdbcLdapBrowserApp.app.driversDir = driversPath.getText();
				JdbcLdapBrowserApp.app.historyLimit = Integer.parseInt(history.getText());
				JdbcLdapBrowserApp.app.setPrefs();
				shell.close();
				
			}

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}});
		
		Button cancel = new Button(top,SWT.PUSH);
		cancel.setText("Cancel");
		cancel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		cancel.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent arg0) {
				
				shell.close();
				
			}

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}});
		
		
		shell.pack();
		Rectangle bounds = shell.getBounds();
		bounds.width = 400;
		shell.setBounds(bounds);
		shell.open();
		
        

        while (!shell.isDisposed()) {
                if (!display.readAndDispatch())
                        display.sleep();
        }
	}
}
