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

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import java.util.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.*;

/**
 * @author Marc Boorshtein
 *
 */
public class AddEntry {
	Shell shell;
	Text rdnAttrib;
	Text rdnVal;
	ArrayList list;
	String base;
	String sql;
	
	
	public AddEntry(Display display,String base) {
		shell = new Shell(display);
		this.base = base;
		
		this.sql = "";
		shell.setText("Add New Entry");
		shell.setImage(JdbcLdapBrowserApp.add);
		GridLayout gl = new GridLayout();
		gl.numColumns=6;
		shell.setLayout(gl);
		Label l = new Label(shell,SWT.NONE);
		l.setText("Name: ");
		
		rdnAttrib = new Text(shell,SWT.BORDER);
		GridData gd = new GridData();
		//gd.grabExcessHorizontalSpace = false;
		gd.horizontalSpan = 1;
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		rdnAttrib.setLayoutData(gd);
		
		
		
		l=new Label(shell,SWT.NONE);
		l.setText("=");
		
		
		rdnVal = new Text(shell,SWT.BORDER);
		gd = new GridData();
		gd.grabExcessHorizontalSpace=true;
		gd.horizontalAlignment = GridData.FILL;
		gd.horizontalSpan=2;
		rdnVal.setLayoutData(gd);
		l=new Label(shell,SWT.NONE);
		l.setText("," + base);
		
		Table entry = new Table(shell,SWT.FULL_SELECTION);
		gd = new GridData();
		gd.grabExcessHorizontalSpace=true;
		gd.grabExcessVerticalSpace=true;
		gd.horizontalSpan=6;
		gd.horizontalAlignment=GridData.FILL;
		gd.verticalAlignment=GridData.FILL;
		entry.setLayoutData(gd);
		entry.setLinesVisible(true);
		
		TableViewer entryViewer = new TableViewer(entry);
		entryViewer.setColumnProperties(new String[]{"Attribute Name","Attribute Value"});
		CellEditor[] editors = new CellEditor[] {new TextCellEditor(entry),new TextCellEditor(entry)};
		entryViewer.setCellEditors(editors);
		entryViewer.setContentProvider(new EntryTableContent());
		entryViewer.setLabelProvider(new EntryTableLabel());
		entryViewer.setCellModifier(new EntryModify(entryViewer));
		
		TableColumn entryName = new TableColumn(entry,SWT.LEFT);
		entryName.setText("Attribute Name");
		entryName.setWidth(200);
		
		TableColumn entryVal = new TableColumn(entry,SWT.LEFT);
		entryVal.setText("Attribute Value");
		entryVal.setWidth(200);
		
		
		
		entry.setHeaderVisible(true);
		/*entry.showColumn(entryName);
		entry.showColumn(entryVal);
		
		TableItem tbli = new TableItem(entry,SWT.NONE);
		tbli.setText(0,"cn");
		tbli.setText(1,"Marc boorshtein");
		*/
		
		list = new ArrayList();
//		Pair p = new Pair();
//		p.name = "objectClass";
//		p.value = "top";
//		list.add(p);
//		p = new Pair();
//		p.name = "objectClass";
//		p.value = "person";
//		list.add(p);
//		p = new Pair();
//		p.name = "cn";
//		p.value = "Marc Boorshtein";
//		list.add(p);
//		p = new Pair();
//		p.name = "sn";
//		p.value = "Boorshtein";
//		list.add(p);
		
		entryViewer.setInput(list);
		
		
		Button addAttrib = new Button(shell,SWT.PUSH);
		addAttrib.setImage(JdbcLdapBrowserApp.open);
		JdbcLdapBrowserApp.open.setBackground(addAttrib.getBackground());
		addAttrib.addSelectionListener(new AddAttrib(entryViewer,list));
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		addAttrib.setLayoutData(gd);
		
		Button removeAttrib = new Button(shell,SWT.PUSH);
		removeAttrib.setImage(JdbcLdapBrowserApp.close);
		JdbcLdapBrowserApp.close.setBackground(removeAttrib.getBackground());
		removeAttrib.addSelectionListener(new RemoveAttrib(entryViewer,list));
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		removeAttrib.setLayoutData(gd);
		
		l = new Label(shell,SWT.NONE);
		gd = new GridData();
		gd.horizontalSpan=4;
		l.setLayoutData(gd);
		
		Composite buttons = new Composite(shell,SWT.NONE);
		gl = new GridLayout();
		gl.numColumns = 2;
		buttons.setLayout(gl);
		gd = new GridData();
		gd.horizontalAlignment = GridData.CENTER;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 6;
		buttons.setLayoutData(gd);
		
		
		Button ok = new Button(buttons,SWT.PUSH);
		ok.setText("Generate SQL");
		ok.addSelectionListener(new GenerateSQL(this));
		shell.setDefaultButton(ok);
		
		Button cancel = new Button(buttons,SWT.PUSH);
		cancel.setText("Cancel");
		cancel.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent event) {
							shell.close();	
						}
			  		});
		
		shell.open();
		
        //shell.addControlListener(new ResizeEvent(w));

        while (!shell.isDisposed()) {
                if (!display.readAndDispatch())
                        display.sleep();
        }
	}
	
	public static void main(String[] args) throws Exception {
		new AddEntry(new Display(),"");
	}
}

class EntryTableLabel implements ITableLabelProvider {
	public String getColumnText(Object obj, int i)
	  {

		switch (i) {
			case 0 : return ((Pair) obj).name;
			case 1 : return ((Pair) obj).value;
			default : return "none";
		}
	  }

	  public void addListener(ILabelProviderListener ilabelproviderlistener)
	  {
	  }

	  public void dispose()
	  {
	  }

	  public boolean isLabelProperty(Object obj, String s)
	  {
	    return false;
	  }

	  public void removeListener(ILabelProviderListener ilabelproviderlistener)
	  {
	  }
	  
	  public Image getColumnImage(Object arg0, int arg1)
	  {
	    return null;
	  }
}

class EntryTableContent implements IStructuredContentProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object arg0) {
		
	java.util.List vals = (java.util.List) arg0;
	
	
		
		
		return vals.toArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		// TODO Auto-generated method stub
		
	}
	
}

class EntryModify implements org.eclipse.jface.viewers.ICellModifier {

	TableViewer tbl;
	
	public EntryModify(TableViewer tbl) {
		this.tbl = tbl;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
	 */
	public boolean canModify(Object arg0, String arg1) {
		// TODO Auto-generated method stub
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
	 */
	public Object getValue(Object element, String property) {
		if (property.equals("Attribute Name")) {
			return ((Pair) element).name;
		} else if (property.equals("Attribute Value")) {
			return ((Pair) element).value;
		}  
		
		return "";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
	 */
	public void modify(Object element, String property, Object value) {
		TableItem item = (TableItem) element;
		Pair p = (Pair) item.getData();
		
		if (property.equals("Attribute Name")) {
			p.name = value.toString();
		} else if (property.equals("Attribute Value")) {
			p.value = value.toString();
		}
		
		tbl.refresh();
		
	}
	
}

class AddAttrib extends SelectionAdapter {
	TableViewer entry;
	java.util.List vals;
	
	public AddAttrib(TableViewer entry, java.util.List list) {
		this.entry = entry;
		this.vals = list;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent arg0) {
		Pair p = new Pair();
		p.name = "Attribute Name";
		p.value = "Attribute Value";
		vals.add(p);
		entry.refresh();
	}
}

class RemoveAttrib extends SelectionAdapter {
	TableViewer entry;
	java.util.List vals;
	
	public RemoveAttrib(TableViewer entry, java.util.List list) {
		this.entry = entry;
		this.vals = list;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent select) {
		int index = entry.getTable().getSelectionIndex();
		if (index >= 0) {
			vals.remove(index);
			entry.refresh();
		}
	}
}

class GenerateSQL extends SelectionAdapter {
	AddEntry addEntry;
	
	
	public GenerateSQL(AddEntry addEntry) {
		this.addEntry = addEntry;		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent select) {
		StringBuffer SQL = new StringBuffer();
		SQL.append("INSERT INTO ").append(addEntry.rdnAttrib.getText()).append('=').append(addEntry.rdnVal.getText()).append(',').append(addEntry.base).append(" (");
		
		Iterator it = addEntry.list.iterator();
		while (it.hasNext()) {
			SQL.append(((Pair) it.next()).name);
			if (it.hasNext()) SQL.append(',');
		}
		
		SQL.append(") VALUES (");
		it = addEntry.list.iterator();
		while (it.hasNext()) {
			String val = ((Pair) it.next()).value; 
			if (val.indexOf(',') != -1) {
				SQL.append('\'').append(val).append('\'');
			} else {
				SQL.append(val);
			}
			
			
			if (it.hasNext()) SQL.append(',');
		}
		
		SQL.append(')');
	
		addEntry.sql = SQL.toString();
		
		//System.out.println("SQL : " + SQL);
		
		addEntry.shell.close();
	}
}

class Pair {
	String name;
	String value;
	
	public String toString() {
		return name + "=" + value;
	}
}
