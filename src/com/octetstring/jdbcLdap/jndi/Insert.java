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
 * Insert.java
 *
 * Created on March 13, 2002, 5:50 PM
 */

package com.octetstring.jdbcLdap.jndi;

import javax.naming.*;
import java.util.*;
import javax.naming.directory.*;
import com.octetstring.jdbcLdap.sql.statements.JdbcLdapInsert;
import com.octetstring.jdbcLdap.sql.*;
import java.sql.*;
/**
 *Inserts a new entry
 *@author Marc Boorshtein, OctetString
 */
public class Insert {
	/**
	*Performs an insert based on an insert statement
	*/
	
	public void doInsert(JdbcLdapInsert insert) throws SQLException {
		DirContext con = insert.getContext();
		Attributes atts = new BasicAttributes();
		SqlStore store = insert.getSqlStore();
		String[] fields = store.getFields();
		String[] vals = insert.getVals();
                HashMap fieldsMap = store.getFieldsMap();
		Iterator it;
                String field;
                
		//take all attributes and add it to addition list
		try {
                        it = fieldsMap.keySet().iterator();
			while (it.hasNext()) {
                                field = (String) it.next();
				atts.put(new BasicAttribute(field,fieldsMap.get(field)));
			}
			con.createSubcontext(insert.getDistinguishedName(),atts);
		}
		catch (NamingException ne) {
			throw new SQLNamingException(ne); 
		}
	}
}
