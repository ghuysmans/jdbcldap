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

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * @author Marc Boorshtein
 *
 */
public class ModifyEntry {
	Shell shell;
	String base;
	String sql;
	
	public ModifyEntry(Display display,String base) {
		shell = new Shell(display);
		this.base = base;
		
		shell.setLayout(new FillLayout());
		Composite top = new Composite(shell,SWT.NONE);
		
		
		this.sql = "";
		shell.setText("Modify " + base);
		shell.setImage(JdbcLdapBrowserApp.add);
		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		top.setLayout(gl);
		
		Label l = new Label(top,SWT.NONE);
		l.setText("Modify Entry : " + base);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = GridData.BEGINNING;
		l.setLayoutData(gd);
		
		l = new Label(top,SWT.NONE);
		l.setText("Change Type : ");
		gd = new GridData();
		gd.horizontalSpan = 1;
		gd.horizontalAlignment = GridData.BEGINNING;
		l.setLayoutData(gd);
		
		Combo change = new Combo(top,SWT.DROP_DOWN  | SWT.READ_ONLY);
		change.add("Add");
		change.add("Replace");
		change.add("Delete");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.grabExcessHorizontalSpace = true;
		change.setLayoutData(gd);
		
		l = new Label(top,SWT.NONE);
		l.setText("Change Attribute : ");
		gd = new GridData();
		gd.horizontalSpan = 1;
		gd.horizontalAlignment = GridData.BEGINNING;
		l.setLayoutData(gd);
		
		Text attrib = new Text(top,SWT.BORDER);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.grabExcessHorizontalSpace = true;
		attrib.setLayoutData(gd);
		
		l = new Label(top,SWT.NONE);
		l.setText("Attribute Value : ");
		gd = new GridData();
		gd.horizontalSpan = 1;
		gd.horizontalAlignment = GridData.BEGINNING;
		l.setLayoutData(gd);
		
		Text val = new Text(top,SWT.BORDER);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.grabExcessHorizontalSpace = true;
		val.setLayoutData(gd);
		
		Button add = new Button(top,SWT.PUSH);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		add.setImage(JdbcLdapBrowserApp.open);
		JdbcLdapBrowserApp.open.setBackground(add.getBackground());
		
		Table changes = new Table(top,SWT.FULL_SELECTION);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		changes.setLayoutData(gd);
		
		changes.setHeaderVisible(true);
		
		TableColumn tc = new TableColumn(changes,SWT.LEFT);
		tc.setText("Change Type");
		tc.setWidth(100);
		
		tc = new TableColumn(changes,SWT.LEFT);
		tc.setText("Change Attribute");
		tc.setWidth(200);
		
		tc = new TableColumn(changes,SWT.LEFT);
		tc.setText("Attribute Value");
		tc.setWidth(400);
		
		Button rem = new Button(top,SWT.PUSH);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		rem.setLayoutData(gd);
		rem.setImage(JdbcLdapBrowserApp.close);
		JdbcLdapBrowserApp.close.setBackground(rem.getBackground());
		
		add.addSelectionListener(new AddChange(change,attrib,val,changes));
		rem.addSelectionListener(new RemoveChange(changes));
		
		Button OK = new Button(top,SWT.PUSH);
		OK.setText("Generate SQL");
		OK.addSelectionListener(new ModOKPressed(changes,base,this));
		shell.setDefaultButton(OK);
		
		Button cancel = new Button(top,SWT.PUSH);
		cancel.setText("Cancel");
		cancel.addSelectionListener(new ModCancelPressed(this));
		
		shell.open();
		
        //shell.addControlListener(new ResizeEvent(w));

        while (!shell.isDisposed()) {
                if (!display.readAndDispatch())
                        display.sleep();
        }
	}
}

class AddChange implements SelectionListener {

	Combo change;
	Text attrib;
	Text val;
	Table changes;
	
	public AddChange(Combo change,Text attrib,Text val,Table changes) {
		this.change = change;
		this.attrib = attrib;
		this.val = val;
		this.changes = changes;
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		TableItem ti = new TableItem(changes,SWT.LEFT);
		ti.setText(0,change.getText());
		ti.setText(1,attrib.getText());
		ti.setText(2,val.getText());
		changes.redraw();
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}

class RemoveChange implements SelectionListener {

	Table changes;
	
	public RemoveChange(Table changes) {
		this.changes = changes;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		
		
		int selected = changes.getSelectionIndex();
		if (selected != -1) {
			changes.remove(selected);
			
			changes.redraw();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}


class ModOKPressed implements SelectionListener {

	Table changes;
	String entry;
	ModifyEntry mod;
	
	public ModOKPressed(Table changes,String entry,ModifyEntry mod) {
		this.changes = changes;
		this.entry = entry;
		this.mod = mod;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		ArrayList adds = new ArrayList(),
				 reps = new ArrayList(),
				 dels = new ArrayList();
		
		for (int i=0,m=changes.getItemCount();i<m;i++) {
			TableItem ti = changes.getItem(i);
			Pair p = new Pair();
			p.name = ti.getText(1);
			p.value = ti.getText(2);
			
			if (ti.getText(0).equalsIgnoreCase("add")) {
				adds.add(p);
			} else if (ti.getText(0).equalsIgnoreCase("delete")) {
				dels.add(p);
			} else {
				reps.add(p);
			}
		}
		
		String sql = "UPDATE ENTRY " + this.entry;
		if (adds.size() != 0) {
			Iterator it = adds.iterator();
			sql += " DO ADD SET ";
			while (it.hasNext()) {
				Pair p = (Pair) it.next();
				sql += p.name + "='" + p.value + "' ";
				if (it.hasNext()) {
					sql += ", ";
				}
			}
		}
		
		if (reps.size() != 0) {
			Iterator it = reps.iterator();
			sql += " DO REPLACE SET ";
			while (it.hasNext()) {
				Pair p = (Pair) it.next();
				sql += p.name + "='" + p.value + "' ";
				if (it.hasNext()) {
					sql += ", ";
				}
			}
		}
		
		if (dels.size() != 0) {
			Iterator it = reps.iterator();
			sql += " DO REPLACE SET ";
			while (it.hasNext()) {
				Pair p = (Pair) it.next();
				if (p.value.trim().length() == 0) {
					sql += p.name + " ";
				} else {
					sql += p.name + "='" + p.value + "' ";
				}
				
				if (it.hasNext()) {
					sql += ", ";
				}
			}
		}
		
		mod.sql = sql;
		
		mod.shell.close();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}

class ModCancelPressed implements SelectionListener {

	ModifyEntry mod;
	
	public ModCancelPressed(ModifyEntry mod) {
		this.mod = mod;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		mod.sql = "";
		mod.shell.close();
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}