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




import java.io.UnsupportedEncodingException;
import java.util.Iterator;

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

import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchRequest;
import com.novell.ldap.rfc2251.RfcFilter;

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
	
	private static String byteString(byte[] value) {
        String toReturn = null;
        if (com.novell.ldap.util.Base64.isValidUTF8(value, true)) {
            try {
                toReturn = new String(value, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(
                        "Default JVM does not support UTF-8 encoding" + e);
            }
        } else {
            StringBuffer binary = new StringBuffer();
            for (int i=0; i<value.length; i++){
                //TODO repair binary output
                //Every octet needs to be escaped
                if (value[i] >=0) {
                    //one character hex string
                    binary.append("\\0");
                    binary.append(Integer.toHexString(value[i]));
                } else {
                    //negative (eight character) hex string
                    binary.append("\\"+
                            Integer.toHexString(value[i]).substring(6));
                }
            }
            toReturn = binary.toString();
        }
        return toReturn;
    }
	
	private void stringFilter(Iterator itr, StringBuffer filter) {
        int op=-1;
        //filter.append('(');
        String comp = null;
        
        boolean isFirst = true;
        
        while (itr.hasNext()){
            Object filterpart = itr.next();
            if (filterpart instanceof Integer){
                op = ((Integer)filterpart).intValue();
                switch (op){
                    case LDAPSearchRequest.AND:
                        comp = " AND ";
                        break;
                    case LDAPSearchRequest.OR:
                        comp = " OR ";
                        break;
                    case LDAPSearchRequest.NOT:
                        filter.append(" NOT ");
                        break;
                    case LDAPSearchRequest.EQUALITY_MATCH:{
                        filter.append((String)itr.next());
                        filter.append("='");
                        byte[] value = (byte[])itr.next();
                        filter.append(byteString(value)).append('\'');
                        
                        if (comp != null && itr.hasNext()) {
                        	filter.append(comp);
                        }
                        
                        break;
                    }
                    case LDAPSearchRequest.GREATER_OR_EQUAL:{
                        filter.append((String)itr.next());
                        filter.append(">=");
                        byte[] value = (byte[])itr.next();
                        filter.append(byteString(value));
                        
                        if (comp != null && itr.hasNext()) {
                        	filter.append(comp);
                        }
                        
                        break;
                    }
                    case LDAPSearchRequest.LESS_OR_EQUAL:{
                        filter.append((String)itr.next());
                        filter.append("<=");
                        byte[] value = (byte[])itr.next();
                        filter.append(byteString(value));
                        
                        if (comp != null && itr.hasNext()) {
                        	filter.append(comp);
                        }
                        
                        break;
                    }
                    case LDAPSearchRequest.PRESENT:
                        filter.append((String)itr.next());
                        filter.append(" IS NOT NULL ");
                        
                        if (comp != null && itr.hasNext()) {
                        	filter.append(comp);
                        }
                        
                        break;
                    case LDAPSearchRequest.APPROX_MATCH:
                        filter.append((String)itr.next());
                        filter.append("~=");
                        byte[] value = (byte[])itr.next();
                        filter.append(byteString(value));
                        
                        if (comp != null && itr.hasNext()) {
                        	filter.append(comp);
                        }
                        
                        break;
                    case LDAPSearchRequest.EXTENSIBLE_MATCH:
                        String oid = (String)itr.next();

                        filter.append((String)itr.next());
                        filter.append(':');
                        filter.append(oid);
                        filter.append(":=");
                        filter.append((String)itr.next());
                        
                        if (comp != null && itr.hasNext()) {
                        	filter.append(comp);
                        }
                        
                        break;
                    case LDAPSearchRequest.SUBSTRINGS:{
                        filter.append((String)itr.next());
                        filter.append(" LIKE '");
                        boolean noStarLast = false;
                        while (itr.hasNext()){
                            op = ((Integer)itr.next()).intValue();
                            switch(op){
                                case LDAPSearchRequest.INITIAL:
                                    filter.append((String)itr.next());
                                    filter.append('%');
                                    noStarLast = false;
                                    break;
                                case LDAPSearchRequest.ANY:
                                    if( noStarLast)
                                        filter.append('%');
                                    filter.append((String)itr.next());
                                    filter.append('%');
                                    noStarLast = false;
                                    break;
                                case LDAPSearchRequest.FINAL:
                                    if( noStarLast)
                                        filter.append('%');
                                    
                                    filter.append((String)itr.next());
                                    break;
                            }
                            
                            filter.append('\'');
                            
                            if (comp != null && itr.hasNext() ) {
                            	if (isFirst) {
                            		isFirst = false;
                            	} else {
                            		filter.append(comp);
                            	}
                            }
                        }
                        break;
                    }
                }
            } else if (filterpart instanceof Iterator){
                stringFilter((Iterator)filterpart, filter);
            }
            
            if (comp != null && itr.hasNext()) {
            	if (isFirst) {
            		isFirst = false;
            		filter.append('(');
            	} else {
            		filter.append(comp);
            	}
            }
        }
        
        
        
        if (comp != null) {
        	filter.append(')');
        }
    }
	
	String filterToWhere(String filter) {
		if (filter.trim().length() == 0 || filter.trim().equalsIgnoreCase("(objectClass=*)") || filter.trim().equalsIgnoreCase("objectClass=*")) {
			return "";
		}
		
		
		String where = "";
		try {
			RfcFilter rfcFilter = new RfcFilter(filter.trim());
			StringBuffer buff = new StringBuffer();
			
			this.stringFilter(rfcFilter.getFilterIterator(),buff);
			
			where = buff.toString();
			
		} catch (LDAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*
		if (filter.charAt(0) == '(') {
			filter = filter.substring(1,filter.length() - 1);
		}
		
		
		String where = parseFilter(filter);
		if (where.charAt(0) == '(') {
			where = where.substring(1,where.length() - 1);
		}*/
		return " WHERE " + where;
		
		 
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
