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
 * Delete.java
 *
 * Created on March 13, 2002, 5:50 PM
 */

package com.octetstring.jdbcLdap.jndi;


import com.octetstring.jdbcLdap.sql.statements.*;
import com.octetstring.jdbcLdap.sql.*;
import java.sql.*;
import com.novell.ldap.*;
/**
 *Deletes an entry
 *@author Marc Boorshtein, OctetString
 */
public class Delete {
	RetrieveResults res = new RetrieveResults();


	
	public int doDeleteJldap(JdbcLdapDelete del) throws SQLException {
		LDAPConnection con = del.getConnection();
		
		StringBuffer buf = new StringBuffer();
		SqlStore store = del.getSqlStore();
		int count = 0;
		//System.out.println("from : " + store.getFrom());
		if (store.getSimple()) {
			try {
				con.delete(JndiLdapConnection.getRealBase(del));
			}
			catch (LDAPException ne) {
				throw new SQLNamingException(ne);
			}
			
			return 1;
		}
		else {
			try {
				
				LDAPSearchResults enum = res.searchUpInsJldap(del);
				while (enum.hasMore()) {
					LDAPEntry entry = enum.next(); 
					con.delete(entry.getDN());
					count++;
				}
				
				
				
				return count;
			}
			catch (LDAPException ne) {
				throw new SQLNamingException(ne);
			}
		}
	}
}
