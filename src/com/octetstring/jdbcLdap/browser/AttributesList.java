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

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import java.sql.*;
import java.util.*;

import com.octetstring.jdbcLdap.jndi.*;
/**
 * @author Marc Boorshtein
 *
 */
public class AttributesList implements IStructuredContentProvider {
	JdbcLdapBrowser browser;
	public AttributesList(JdbcLdapBrowser browser) throws SQLException {
		this.browser = browser;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object arg0) {
		TreeObject to = (TreeObject) arg0;
		HashMap attribs = new HashMap();
		String name,val;
		LinkedList vals = new LinkedList();
		if (to.getSQL) {
		
			//System.out.println("name : " + to.getName());
			//System.out.println("base : " + to.getBase());
			//System.out.println("toString : " + to.toString());
			//System.out.println("base context" + con.getBaseContext());
		
			String sql;
			
			String base = to.getBase();
			if (base.equalsIgnoreCase("RootDSE")) {
				base = " ";
			}
			
			if (! browser.isSpml) {
				sql = "SELECT * FROM objectScope;" + base;
			} else {
				sql = "SELECT view FROM objectScope;" + base;
			}
			
			
			
			
			
//			if (to.toString().equalsIgnoreCase(to.getBase())) {
//				sql = "SELECT * FROM objectScope;";
//			}
//			else {
//				if (to.isRef()) {
//					sql = "SELECT * FROM objectScope;" + to.getName();
//				}
//				else if (to.getConId() != 0) {
//					sql = "SELECT * FROM objectScope;" + to.getBase();
//				}
//				else {
//					sql = "SELECT * FROM objectScope;" + to.toString();// + "," + to.getBase(); 
//				}
//				
//			}
			
			
			//sql = "SELECT * FROM objectScope;" + (to.isRef() ? to.getName() : );
			
			sql = sql.trim();
			if (sql.length() > 0 && sql.charAt(sql.length()-1) == ',') sql = sql.substring(0,sql.lastIndexOf(','));
			
			
			LinkedList attribVals;
			
			
			try {
				ResultSet rs;
				if (to.isRef()) {
					if (to.getConId() == 0) {
						to.setConId(browser.createRefConnection(to.getRefUrl()));
					}
					
					rs = browser.getConnection(to.getConId()).createStatement().executeQuery("SELECT * FROM objectScope;");
				}
				else {
					rs = browser.getConnection(to.getConId()).createStatement().executeQuery(sql);
				}
				
				
				ResultSetMetaData rsmd = rs.getMetaData();
				
				
				while (rs.next()) {
					
					for (int i=1,m=rsmd.getColumnCount();i<=m;i++) {
						name = rsmd.getColumnName(i);
						val = rs.getString(name);
						
						if (attribs.containsKey(name)) {
							attribVals = (LinkedList) attribs.get(name);
							if (! attribVals.contains(val)) {
								attribVals.add(val);
							}
						}
						else {
							attribVals = new LinkedList();
							attribVals.add(val);
							attribs.put(name,attribVals);
						}
						
						//vals.add(name + "=" + val);
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			attribs = to.attributes;
		}
		Iterator itNames = attribs.keySet().iterator();
		Iterator itVals;
		while (itNames.hasNext()) {
			name = (String) itNames.next();
			itVals = ((LinkedList) attribs.get(name)).iterator();
			while (itVals.hasNext()) {
				vals.add(new Attribute(name,(String) itVals.next()));
			}
		}
		
		return vals.toArray();
		
		// TODO Auto-generated method stub
		//return null;
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
