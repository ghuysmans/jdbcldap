/* **************************************************************************
 *
 * Copyright (C) 2002 Octet String, Inc. All Rights Reserved.
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

import javax.naming.*;
import javax.naming.directory.*;
import com.octetstring.jdbcLdap.sql.statements.*;
import com.octetstring.jdbcLdap.sql.*;
import java.sql.*;
/**
 *Deletes an entry
 *@author Marc Boorshtein, OctetString
 */
public class Delete {
	RetrieveResults res = new RetrieveResults();
	SearchResult seres;
	public int doDelete(JdbcLdapDelete del) throws SQLException {
		DirContext con = del.getCon().getContext();
		StringBuffer buf = new StringBuffer();
		SqlStore store = del.getSqlStore();
		int count = 0;
		//System.out.println("from : " + store.getFrom());
		if (store.getSimple()) {
			try {
				con.destroySubcontext(store.getFrom());
			}
			catch (NamingException ne) {
				throw new SQLNamingException(ne);
			}
			
			return 1;
		}
		else {
			try {
				
				NamingEnumeration enum = res.searchUpIns(del);
				while (enum.hasMore()) {
					seres = (SearchResult) enum.next();
					buf.setLength(0);
					con.destroySubcontext(buf.append(seres.getName()).append(',').append(store.getFrom()).toString());
					count++;
				}
			
				enum.close();
			
				return count;
			}
			catch (NamingException ne) {
				throw new SQLNamingException(ne);
			}
		}
	}
}
