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




import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author Marc Boorshtein
 *
 */
public class SearchDialog {
	Shell shell;
	String sql;
	String base;
	
	public SearchDialog(Display display,String base) {
		this.base = base;
		this.sql = "";
		this.shell = new Shell(display);
		shell.setText("SQL Search Dialog");
		shell.setImage(JdbcLdapBrowserApp.search);
		shell.setLayout(new FillLayout());
		
		Composite top = new Composite(shell,SWT.NONE);
		
		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		//gl.makeColumnsEqualWidth = true;
		top.setLayout(gl);
		
		Label l = new Label(top,SWT.NONE);
		l.setText("Base : ");
		
		Text baseText = new Text(top,SWT.BORDER);
		baseText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		baseText.setText(base);
		
		l = new Label(top,SWT.NONE);
		l.setText("Scope : ");
		
		GridData gd;
		
		Button baseScope = new Button(top,SWT.RADIO);
		baseScope.setText("Base");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.grabExcessHorizontalSpace = true;
		baseScope.setLayoutData(gd);
		
		l = new Label(top,SWT.NONE);
		l.setText("");
		
		Button oneScope = new Button(top,SWT.RADIO);
		oneScope.setText("One Level");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.grabExcessHorizontalSpace = true;
		oneScope.setLayoutData(gd);
		
		l = new Label(top,SWT.NONE);
		l.setText("");
		
		Button subScope = new Button(top,SWT.RADIO);
		subScope.setText("Subtree");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.grabExcessHorizontalSpace = true;
		subScope.setLayoutData(gd);
		
		
		l = new Label(top,SWT.NONE);
		l.setText("Attributes : ");
		
		Text attribs = new Text(top,SWT.BORDER);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.grabExcessHorizontalSpace = true;
		attribs.setLayoutData(gd);
		
		l = new Label(top,SWT.NONE);
		l.setText("LDAP Filter : ");
		
		Text filter = new Text(top,SWT.BORDER);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.grabExcessHorizontalSpace = true;
		filter.setLayoutData(gd);
		
		
		baseScope.setSelection(true);
		
		Composite buttons = new Composite(top,SWT.NONE);
		
		buttons.setLayout(new RowLayout());
		
		Button ok = new Button(buttons,SWT.PUSH);
		ok.setText("Generate SQL");
		ok.addSelectionListener(new SearchOKPressed(baseText,attribs,baseScope,oneScope,subScope,this,filter));
		shell.setDefaultButton(ok);
		
		Button cancel = new Button(buttons,SWT.PUSH);
		cancel.setText("Cancel");
		cancel.addSelectionListener(new SearchCancelPressed(shell));
		
		shell.pack();
		Rectangle bounds = shell.getBounds();
		bounds.width = 600;
		shell.setBounds(bounds);
		
		shell.open();
		
		 while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                    display.sleep();
		 }
	}
	
	
}

class SearchOKPressed implements SelectionListener {
	Text base;
	Text attribs;
	Text filter;
	Button baseScope,
	       oneScope,
		   subScope;
	
	SearchDialog search;
	
	public SearchOKPressed(Text base,Text attribs,Button baseScope,Button oneScope,Button subScope,SearchDialog search,Text filter) {
		this.base = base;
		this.attribs = attribs;
		this.baseScope = baseScope;
		this.oneScope = oneScope;
		this.subScope = subScope;
		this.search = search;
		this.filter = filter;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		String SQL = "SELECT ";
		String attribs = this.attribs.getText();
		if (attribs.trim().length() == 0 || attribs.indexOf('*') != -1) {
			attribs = "*";
		} else {
			attribs += ",DN";
			
		}
		
		SQL += " " + attribs + " FROM ";
		
		if (baseScope.getSelection()) {
			SQL += "objectScope;";
		} else if (oneScope.getSelection()) {
			SQL += "oneLevelScope;";
		} else {
			SQL += "subTreeScope;";
		}
		
		try {
			SQL += base.getText() + " " + this.filterToWhere(filter.getText());
		} catch (IndexOutOfBoundsException err) {
			err.printStackTrace();
			MessageDialog.openError(search.shell,"Invalid Filter","Bad Search Filter");
			return;
		}
		
		this.search.sql = SQL;
		
		search.shell.close();
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	String filterToWhere(String filter) {
		if (filter.trim().length() == 0 || filter.trim().equalsIgnoreCase("(objectClass=*)") || filter.trim().equalsIgnoreCase("objectClass=*")) {
			return "";
		}
		
		if (filter.charAt(0) == '(') {
			filter = filter.substring(1,filter.length() - 1);
		}
		
		
		String where = parseFilter(filter);
		if (where.charAt(0) == '(') {
			where = where.substring(1,where.length() - 1);
		}
		return " WHERE " + where;
		
		 
	}
	
	String parseFilter(String filter) {
		
		String sql = "(";
		for (int i=0,m=filter.length();i<m;i++) {
			char c = filter.charAt(i);
			
			switch (c) {
				case ')' :
				case ' ' : continue;
				case '&' :
					String and;
					if (filter.charAt(i + 1) == '(' && filter.charAt(i + 2) == '(') {
						and = stripParen(filter,i+1);
					} else {
						and = filter.substring(1);
					}
					sql += constructOp(and,true);
					return sql + ")";
				case '|' :
					String or;
					if (filter.charAt(i + 1) == '(' && filter.charAt(i + 2) == '(') {
						or = stripParen(filter,i+1);
					} else {
						or = filter.substring(1);
					}
					sql += constructOp(or,false);
					return sql + ")";
				case '!' :
					String not = stripParen(filter,i+1);
					sql += "NOT " + parseFilter(not);
					i += not.length();
					
					return sql + ")";
				default :
					//System.out.println("filter : " + filter);
					
					String tmpFilter = filter;
					boolean isPresent = false;
					if (tmpFilter.indexOf('*') != -1) {
						int loc = tmpFilter.indexOf('*');
						int eq = tmpFilter.indexOf('=');
						
						
						
						isPresent = loc == eq + 1 && loc == tmpFilter.length() - 1;
						
						
						if (! isPresent) {
							tmpFilter = tmpFilter.replaceAll("%","\\%");
							tmpFilter = tmpFilter.replace('*','%');
							tmpFilter = tmpFilter.replaceAll("[=]"," LIKE '");
						} else {
							tmpFilter = tmpFilter.substring(0,tmpFilter.indexOf('=')) + " IS NOT NULL ";
						}
					}
				
					if (! isPresent) {
						tmpFilter = tmpFilter.replaceAll("=","='") + "'";
					}
					
					filter = tmpFilter;
					
					
					return filter;
			}
		}
		
		return "";
		
	}
	
	String stripParen(String filter,int start) {
		
		
		
		int count = 1;
		int lookfor;
		
			start = start + 1;
			count = 1;
		
		
		
		
		int index = start;
		while (count != 0) {
			
			char c = filter.charAt(index);
			if (c == '(') {
				count++;
			} if (c == ')') {
				count--;
			}
			
			index++;
		} 
		
		index -= 1;
		
		
		
		return filter.substring(start,index);
	}
	
	String constructOp(String filter,boolean and) {
		
		boolean first = true;
		String fsql = "";
		for (int i=0,m=filter.length();i<m;i++) {
			char c = filter.charAt(i);
			if (c == '(') {
				String part = stripParen(filter,i);
				String sql = parseFilter(part);
				i += part.length();
				
				if (first) {
					first = false;
				} else {
					fsql += (and ? " AND " : " OR ");
				}
				
				fsql += sql;
			}
		}
		
		return fsql;
	}
	
	
	
}

class SearchCancelPressed implements SelectionListener {

	Shell shell;
	
	public SearchCancelPressed(Shell shell) {
		this.shell = shell;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		this.shell.close();
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}
