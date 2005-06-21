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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import java.util.*;

import java.sql.*;
import org.eclipse.jface.dialogs.MessageDialog;
import com.octetstring.jdbcLdap.jndi.*;

/**
 * @author Marc Boorshtein
 *
 */
public class DirTree implements ITreeContentProvider {
	JdbcLdapBrowser browser;
	Statement stmt;
	
	public DirTree(JdbcLdapBrowser browser) throws SQLException {
		this.browser = browser;
			
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object obj) {
		TreeObject to = (TreeObject) obj;
		
		ResultSet namingContexts = null;
		
		if (! to.getSQL) {
			return to.children.values().toArray();
		}
		
		JndiLdapConnection con = null;
		try {
			con = (JndiLdapConnection) browser.getConnection();
		}
		catch (SQLException e) {
			MessageDialog.openError(browser.tabs.getShell(),"Error",e.toString());
			return new Object[0];
		}
		
		String sbase;
		String sql;
		
		//System.out.println("to.toString()" + to.toString());
		int conid = to.getConId();
		
		if (to.isRef()) {
			try {
				if (to.conId == 0) {
					to.setConId(browser.createRefConnection(to.getRefUrl()));
				}
			
			
				con = browser.getConnection(to.getConId());
				sql = "SELECT dn FROM oneLevelScope;" + to.getName();
				
			}
			catch (Exception e) {
				MessageDialog.openError(browser.tabs.getShell(),"Error",e.toString());
				return new Object[0];
			}
		}
		else {
			try {
				con = browser.getConnection(to.getConId());
				String base = to.getBase();
				if (base.equalsIgnoreCase("RootDSE")) {
					sql = "SELECT namingContexts FROM objectScope; ";
					namingContexts = con.createStatement().executeQuery(sql);
				} else {
				
					sql = "SELECT dn FROM oneLevelScope;" + to.getBase();
					
					
				}
				/*if (to.toString().equalsIgnoreCase(con.getBaseDN())) {	
					sql = "SELECT dn FROM oneLevelScope;";
				}
				else {
					
					
					if (to.getBase().endsWith(con.getBaseContext())) {
						sql = "SELECT dn FROM oneLevelScope;" + to.getBase().substring(0,to.getBase().lastIndexOf(con.getBaseContext())-1);
						
					}
					else if (to.getConId() != 0) {
						sql = "SELECT dn FROM oneLevelScope;" + to.getBase();// + "," + to.getBase();
					}
					else {
						sql = "SELECT dn FROM oneLevelScope;" + to.getName();// + "," + to.getBase(); 
					}
					
				}*/
			}
			catch (Exception e) {
				MessageDialog.openError(browser.tabs.getShell(),"Error",e.toString());
				return new Object[0];
			}
		}
		
		
		
		
		sql = sql.trim();
		if (sql.length() > 0 && sql.charAt(sql.length()-1) == ',') sql = sql.substring(0,sql.lastIndexOf(','));
		
		LinkedList children = new LinkedList();
		
		 //=  + obj.toString() + "," + sbase;
		//System.out.println("sql : " + sql);
		try {
			//System.out.println("beginning sql");
			//System.out.println("to.getConId: " + to.getConId());
			ResultSet rs = null;
			String dnAttrib = "DN";
			
			if (namingContexts == null) {
				rs = con.createStatement().executeQuery(sql);
			} else {
				rs = namingContexts;
				dnAttrib = "namingContexts";
			}
			
			
			//System.out.println("retrieving sql");
			
			while (rs.next()) {
				TreeObject nto = new TreeObject(rs.getString(1),to,con.getBaseDN());
				nto.setConId(to.getConId());
				children.add(nto);
			}
		} catch (SQLException e) {
			MessageDialog.openError(browser.tabs.getShell(),"Error",e.toString());
			e.printStackTrace();
			//return new Object[0];
			
		}
		
		return children.toArray();
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object base) {
		TreeObject to = (TreeObject) base;
		return to.getParent();
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object arg0) {
		// TODO Auto-generated method stub
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object arg0) {
		TreeObject to = (TreeObject) arg0;
		//System.out.println("arg0 : " + arg0);
		//return this.getChildren(arg0);
		//return new Object[] {new TreeObject(to.toString(),null,to.getBase())};
		//return new Object[] {arg0};
		return new Object[] {new TreeObject(to.toString(),null,to.getBase(),to.getSQL,to.children,to.attributes)};
		
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


