/* **************************************************************************
 *
 * Copyright (C) 2002-2004 Octet String, Inc. All Rights Reserved.
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
package com.octetstring.jdbcLdap.jndi;

import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;
import java.sql.*;
import com.octetstring.jdbcLdap.sql.statements.*;
import com.octetstring.jdbcLdap.util.*;
/**
 * @author mlb
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class UpdateEntry {
	RetrieveResults res = new RetrieveResults();
	
	public int doUpdateEntry(JdbcLdapUpdateEntry stmt) throws SQLException {
		int argPos = 0;
		StringBuffer dn = new StringBuffer();
		//first we need to construct the dn
		/*String[] dnlist = stmt.getDnlist();
		for (int i=0,m=dnlist.length;i<m;i++) {
			dn.append(dnlist[i]);
			//System.out.println("i,m :" + i + "," + m);
			if (i < m-1) {
				dn.append(stmt.getArgVals()[i]);
			}
		}*/
		
		
		
		Iterator icmds = stmt.getCmds().iterator();
		UpdateSet us;
		LinkedList mods = new LinkedList();
		int paramnum = 0;
		while (icmds.hasNext()) {
			us = (UpdateSet) icmds.next();
		
		
			int modtype;
			
			if (us.getCmd().equalsIgnoreCase(JdbcLdapUpdateEntry.ADD)) {
				modtype = DirContext.ADD_ATTRIBUTE;
			}
			else if (us.getCmd().equalsIgnoreCase(JdbcLdapUpdateEntry.DELETE)) {
				modtype = DirContext.REMOVE_ATTRIBUTE;
			}
			else {
				modtype = DirContext.REPLACE_ATTRIBUTE;
			}
			
			
			
			//ModificationItem[] mods = new ModificationItem[stmt.getAttribs().size()];
			Pair p;
			String val,name;
			Iterator it = us.getAttribs().iterator();
			int i = 0;
			ArrayList al = new ArrayList();
			while (it.hasNext()) {
				if (modtype == DirContext.ADD_ATTRIBUTE || modtype == DirContext.REPLACE_ATTRIBUTE) {
					p = (Pair) it.next();
					name = p.getName();
					
					if (p.getValue().equals("?")) {
						//System.out.println("paramnum : " + paramnum);
						//System.out.println("val : " + stmt.getArgVals()[paramnum]);
						val = stmt.getArgVals()[paramnum];
						paramnum++;
						//i++;
					}
					else {
						val = p.getValue();
					}
					
					
					//System.out.println("moditem : " + modtype + ", " + name + "=" + val);
					mods.add(new ModificationItem(modtype,new BasicAttribute(name,val)));
	 			}
	 			else {
	 				Object o = it.next();
	 				if (o instanceof String) {
		 				name = (String) o;
						//System.out.println("moditem : " + modtype + ", " + name);
		 				mods.add(new ModificationItem(modtype,new BasicAttribute(name)));
	 				} else {
	 					p = (Pair) o;
						name = p.getName();
						
						if (p.getValue().equals("?")) {
							//System.out.println("paramnum : " + paramnum);
							//System.out.println("val : " + stmt.getArgVals()[paramnum]);
							val = stmt.getArgVals()[paramnum];
							paramnum++;
							//i++;
						}
						else {
							val = p.getValue();
						}
						
						
						//System.out.println("moditem : " + modtype + ", " + name + "=" + val);
						mods.add(new ModificationItem(modtype,new BasicAttribute(name,val)));
	 				}
	 			}
	 			i++;
			}
		}
		
		Object[] toCopy = mods.toArray();
		ModificationItem[] doMods = new ModificationItem[toCopy.length];
		System.arraycopy(toCopy,0,doMods,0,doMods.length);
		SearchResult seres;
		StringBuffer buf = new StringBuffer();
		String name;
		try {
			int count = 0;
			
			if (stmt.getSearchScope() == SearchControls.OBJECT_SCOPE && stmt.getFilterWithParams().equalsIgnoreCase("(objectClass=*)")) {
				stmt.getContext().modifyAttributes(stmt.getSqlStore().getDistinguishedName(),doMods);
				count++;
			} else {
			
				NamingEnumeration enum = res.searchUpIns(stmt);
				while (enum.hasMore()) {
								seres = (SearchResult) enum.next();
								buf.setLength(0);
								String entryName = seres.getName();
	                
								if (seres.getName().trim().length() > 0) {
	                
									name = buf.append(entryName).append(',').append(stmt.getSqlStore().getDistinguishedName()).toString();
								}
								else {
									name = stmt.getSqlStore().getDistinguishedName();
								}
	                
								//System.out.println("name : " + name);
	                
	                
								stmt.getContext().modifyAttributes(name,doMods);
								count++;
								//System.out.println("count : " + count);
							}
	            
							enum.close();
			}
						//System.out.println("final count : " + count);
						return count;
			//stmt.getContext().modifyAttributes(dn.toString(),doMods);
		} catch (NamingException ne) {
			throw new SQLNamingException(ne);
		}
	}

}
