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
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;
import org.eclipse.jface.viewers.*;

import java.sql.*;

import com.novell.ldap.LDAPDN;
import com.novell.ldap.util.DN;
import com.novell.ldap.util.RDN;
import com.octetstring.jdbcLdap.jndi.*;

import java.util.*;
import org.eclipse.jface.dialogs.MessageDialog;


/**
 * @author Marc Boorshtein
 *
 */
public class ResultLoader {
	JdbcLdapBrowser browser;
	String scope;
	
	
	LinkedList rows;
	public ResultLoader(JdbcLdapBrowser browser) throws SQLException  {
		this.browser = browser;
		scope = "";
			
	}
	
	
	
	private void addToTree(HashMap entrys,TreeObject root,ResultSet rs) throws SQLException {
		String name = rs.getString("DN");
		TreeObject to = (TreeObject) entrys.get(name.toLowerCase());
		if (to == null) {
			String nname;
			if (root.getName().equalsIgnoreCase("RootDSE")) {
				nname = name;
			} else if (name.lastIndexOf(root.toString()) == -1) {
				nname = "cn=Unknown";
			}  else {
				nname = name.substring(0,name.lastIndexOf(root.toString()));
				if (nname.endsWith(",")) {
					nname = nname.substring(0,nname.length() - 1);
				}
			}
			
			
			DN dn = new DN(nname);
			Iterator it = dn.getRDNs().iterator();
			
			String[] dnparts = dn.explodeDN(false);
			
			
			//try to find child
			TreeObject tmp = root;
			TreeObject parent = null;
			for (int i=dnparts.length-1;i>0;i--) {
				parent = tmp;
				//System.out.println("i : " + i + ", len : " + dnparts.length);
				///System.out.println("dn part : " + dnparts[i]);
				tmp = tmp.getChild(dnparts[i]);
				if (tmp == null) {
					String fname = "";
					for (int j=i,m=dnparts.length;j<m;j++) {
						fname +=  LDAPDN.escapeRDN(dnparts[j]) + ",";
					}
					
					if (! root.getName().equalsIgnoreCase("RootDSE")) {
						fname += root.toString();
					} else {
						fname = fname.substring(0,fname.length() - 1);
					}
					
					tmp = new TreeObject(fname,parent,root.getBase(),false);
					parent.addChild(dnparts[i],tmp);
				}
			}
			
			to = new TreeObject(name,tmp,root.getBase(),false);
			tmp.addChild(dnparts[0],to);
			entrys.put(name.toLowerCase(),to);
			
		}
		
		HashMap attribs = to.attributes;
		ResultSetMetaData rsmd = rs.getMetaData();
		String attribname,val;
		LinkedList attribVals;
		for (int i=1,m=rsmd.getColumnCount();i<=m;i++) {
							attribname = rsmd.getColumnName(i);
							val = rs.getString(attribname);
					
							if (attribs.containsKey(attribname)) {
								attribVals = (LinkedList) attribs.get(attribname);
								if (! attribVals.contains(val)) {
									attribVals.add(val);
								}
							}
							else {
								attribVals = new LinkedList();
								attribVals.add(val);
								attribs.put(attribname,attribVals);
							}
					
							//vals.add(name + "=" + val);
						}
		
	}
	
	public void loadResults(String sql,Table table, TreeViewer tv) throws SQLException {
		Connection con =  this.browser.getConnection();
		Statement stmt = this.browser.getConnection().createStatement();
		TableColumn tc;
				rows = new LinkedList();
				HashMap row;
				ResultSetMetaData rsmd;
				HashMap entries = new HashMap();
				TreeObject root = null;
				if (! browser.isDB) {
					root = new TreeObject(browser.baseDN,null,browser.baseDN,false);
					entries.put(browser.baseDN,root);
				}
				
				//System.out.println("columns cleared");
				try {
					ResultSet rs = stmt.executeQuery(sql);
					/*rsmd = rs.getMetaData();
					//this.label.setMetaData(rsmd);
					System.out.println("cols " + rsmd.getColumnCount());
					for (int i=1,m=rsmd.getColumnCount();i<=m;i++) {
						System.out.println("i : " + i);
						
							System.out.println("creating columns");
							tc = new TableColumn(table,SWT.LEFT);
											tc.setText(rsmd.getColumnName(i));
											tc.setWidth(50);	
							System.out.println("done creating column");
						
				
					}*/
			
			
					//System.out.println("creating table");
					int maxCols = 1;
					
					int start;
					
					if (! browser.isDB) {
						tc = new TableColumn(table,SWT.LEFT);
						tc.setText("DN");
						tc.setWidth(50);
						start = 2;
					} else {
						start = 1;
						maxCols = 0;
					}
					
					while (rs.next()) {
						row = new HashMap();
						rows.add(row);
						TableItem tbli = null;
						
						
						
						if (! browser.isDB) {
							this.addToTree(entries,root,rs);
							
						}
						tbli = new TableItem(table,SWT.NONE);
						rsmd = rs.getMetaData();
						String value = null;
						if (! browser.isDB) {
							value = rs.getString("DN");
							value = value != null ? value : "null";
							
							tbli.setText(0,value);
						}
						for (int i=start,m=rsmd.getColumnCount();i<=m;i++) {
							//table.setT
							
							if (rsmd.getColumnName(i).equalsIgnoreCase("DN")) {
								continue;
							}
							
							if (i > maxCols) {
								tc = new TableColumn(table,SWT.LEFT);
								tc.setText(rsmd.getColumnName(i));
								tc.setWidth(50);	
								maxCols = i;
							}
							
							
							
							value = rs.getString(rsmd.getColumnName(i));
							value = value != null ? value : "null";
							
							tbli.setText(i-1,value);
						}
					}
					//System.out.println("finised creation");
					if (! browser.isDB) {
						tv.setInput(root);
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					MessageDialog.openError(table.getShell(),"Error Occurred",e.toString());
					//new MessageDialog(table.getShell(),"Error Occurred",null,e.toString(),MessageDialog.ERROR,new String[]{"OK"},0).create();
					
					e.printStackTrace();
					
				}
		tv.expandAll();
		
			//this.rows = rows.toArray();
		
				// TODO Auto-generated method stub
				
	}
	
	public LinkedList getTableView() {
		return this.rows;
	}
	
	public TreeObject getTreeView() {
		return null;
	}
	
	public int executeUpdate(String sql) throws SQLException {
		return browser.getConnection().createStatement().executeUpdate(sql);
	}
}
