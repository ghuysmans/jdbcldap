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
 * Update.java
 *
 * Created on May 24, 2002, 12:56 PM
 */

package com.octetstring.jdbcLdap.jndi;

import com.octetstring.jdbcLdap.sql.statements.*;
import com.octetstring.jdbcLdap.sql.*;
import java.sql.*;
import com.novell.ldap.*;
/**
 *Contains logic for updating records in the directory
 *@author Marc Boorshtein, OctetString
 */
public class Update {
	RetrieveResults res = new RetrieveResults();
	
	
	
	
	public int doUpdateJldap(JdbcLdapUpdate update) throws SQLException {
		LDAPConnection con = update.getConnection();
		LDAPEntry seres;
		StringBuffer buf = new StringBuffer();
		SqlStore store = update.getSqlStore();
		int count = 0;
		LDAPModification[] mods;
		String[] fields,vals;
		//build ModificationItem array
		mods = new LDAPModification[store.getFields().length];
		fields = store.getFields();
		vals = update.getVals();
		String name;
		for (int i=0,m=mods.length;i<m;i++) {
			mods[i] = new LDAPModification(LDAPModification.REPLACE,new LDAPAttribute(fields[i],vals[i]));
		}
		
		try {
			if (update.getSearchScope() != 0) {
				LDAPSearchResults enum = res.searchUpInsJldap(update);
				//System.out.println("enum.hasMore : " + enum.hasMore());
				while (enum.hasMore()) {
					seres =  enum.next();
					buf.setLength(0);
					
					name = seres.getDN();
					
					//System.out.println("name : " + name);
					
					
					con.modify(name,mods);
					count++;
					//System.out.println("count : " + count);
				}
			} else {
				con.modify(update.getBaseContext(),mods);
				count++;
			}
			
			
			//System.out.println("final count : " + count);
			return count;
		}
		catch (LDAPException ne) {
			throw new SQLNamingException(ne);
		}
	}
	
}
