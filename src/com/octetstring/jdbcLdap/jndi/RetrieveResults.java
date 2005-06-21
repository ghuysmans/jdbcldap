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

/*
 * RetrieveResults.java
 *
 * Created on March 13, 2002, 5:50 PM
 */

package com.octetstring.jdbcLdap.jndi;


import com.octetstring.jdbcLdap.sql.statements.*;
import java.sql.*;
import java.util.*;

import com.novell.ldap.*;
import com.novell.ldap.controls.LDAPSortControl;
import com.novell.ldap.controls.LDAPSortKey;
/**
 *Retrieves the results from a qeury
 *@author Marc Boorshtein, OctetString
 */
public class RetrieveResults {
    
    /** Creates new RetrieveResults */
    public RetrieveResults() {
    }
    
    public Object searchJldap(JdbcLdapSelect select) throws SQLException {
    	try {
			LDAPConnection con = select.getConnection();
			String[] fields = select.getSearchAttributes();
			fields = fields != null ? fields : new String[0];
			
			
			String[] searchAttribs;
			
			if (fields.length == 1 && fields[0].equalsIgnoreCase("dn")) {
				searchAttribs = new String[] {"1.1"};	
				
			}
			else {
				searchAttribs = fields;
			}
			
			/*System.out.println("attribs");
			for (int i=0,m=searchAttribs.length;i<m;i++) {
				System.out.println("attrib : " + searchAttribs[i]);
			}*/
			
			
			
			String useBase = JndiLdapConnection.getRealBase(select);
			//System.out.println("useBase : " + useBase);
			String filter = select.getFilterWithParams();
			//System.out.println("filter : " + filter);
			
			LDAPSearchConstraints constraints = null;
			
			if (select.getJDBCConnection().getMaxSizeLimit() >= 0) {
				constraints = con.getSearchConstraints();
				constraints.setMaxResults(select.getJDBCConnection().getMaxSizeLimit());
			}
			
			if (select.getJDBCConnection().getMaxTimeLimit() >= 0) {
				if (constraints == null) {
					constraints = con.getSearchConstraints();
				}
				
				constraints.setTimeLimit(select.getJDBCConnection().getMaxTimeLimit());
			}
			
			
			
			LDAPSortKey[] keys = null;
			
			if (select.getSqlStore().getOrderby() != null) {
				keys = new LDAPSortKey[select.getSqlStore().getOrderby().length];
				for (int i=0,m=keys.length;i<m;i++) {
					keys[i] = new LDAPSortKey(this.getFieldName(select.getSqlStore().getOrderby()[i],select.getSqlStore().getFieldMap()));
				}
			}
			
			
			
			if (select.getJDBCConnection().isDSML() || select.getJDBCConnection().isSPML()) {
				return con.search(useBase,select.getSearchScope(),filter,searchAttribs,false,constraints);
			} else {
				
				if (keys != null) {
					constraints.setControls(new LDAPControl[] {new LDAPSortControl(keys, true)});
				} 
				
				return con.search(useBase,select.getSearchScope(),filter,searchAttribs,false,null,constraints);
			}
		} catch (LDAPException e) {
			throw new SQLNamingException(e);
		}
    	
    }
    
    
    
    
    public LDAPSearchResults searchUpInsJldap(JdbcLdapSqlAbs sql) throws SQLException {
    	try {
    		LDAPConnection con = sql.getConnection();
    		
    		
    		String useBase = JndiLdapConnection.getRealBase(sql);
    		
    		String filter = sql.getFilterWithParams();
    		
    		LDAPSearchConstraints constraints = null;
			
			if (sql.getJDBCConnection().getMaxSizeLimit() >= 0) {
				constraints = con.getSearchConstraints();
				constraints.setMaxResults(sql.getJDBCConnection().getMaxSizeLimit());
			}
			
			if (sql.getJDBCConnection().getMaxTimeLimit() >= 0) {
				if (constraints == null) {
					constraints = con.getSearchConstraints();
				}
				
				constraints.setTimeLimit(sql.getJDBCConnection().getMaxTimeLimit());
			}
    		
//	    	System.out.println("sql.getBaseContext() " + sql.getBaseContext());
//	    	System.out.println("where : " + filter);
//	    	System.out.println("scope  : " + sql.getSearchScope());
    		return con.search(useBase,sql.getSearchScope(),filter,new String[] {"1.1"},false,constraints);
    	}
    	catch (LDAPException e) {
    		throw new SQLNamingException(e);
    	}
    }

    private String getFieldName(String name,HashMap revMap) {
		
		if (revMap != null) {
			String nname = (String) revMap.get(name);
			if (nname != null) {
				return nname;
			}
		}
		
		return name;
	}
    
}
