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
 * Insert.java
 *
 * Created on March 13, 2002, 5:50 PM
 */

package com.octetstring.jdbcLdap.jndi;

import javax.naming.*;
import java.util.*;
import javax.naming.directory.*;

import com.novell.ldap.*;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.octetstring.jdbcLdap.sql.statements.JdbcLdapInsert;
import com.octetstring.jdbcLdap.sql.*;
import java.sql.*;
import com.octetstring.jdbcLdap.util.*;
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
                LinkedList fieldsMap = store.getFieldsMap();
		Iterator it;
                String field;
        Pair p;
		//take all attributes and add it to addition list
		try {
                        it = fieldsMap.iterator();
			while (it.hasNext()) {
               p = (Pair) it.next();
               field = p.getName();
               //System.out.println("adding " + field + "=" + p.getValue());
				if (atts.get(field) == null ) {
				
					atts.put(new BasicAttribute(field,p.getValue()));
				}
				else {
					((BasicAttribute) atts.get(field)).add(p.getValue());
				}
			}
			//System.out.println("in insert DN : " + insert.getDistinguishedName());
			con.createSubcontext(insert.getDistinguishedName(),atts);
		}
		catch (NamingException ne) {
			throw new SQLNamingException(ne); 
		}
	}
	
	public void doInsertJldap(JdbcLdapInsert insert) throws SQLException {
		LDAPConnection con = insert.getConnection();
		Attributes atts = new BasicAttributes();
		SqlStore store = insert.getSqlStore();
		String[] fields = store.getFields();
		String[] vals = insert.getVals();
		LinkedList fieldsMap = store.getFieldsMap();
		Iterator it;
		String field;
		Pair p;
		
		
		
		LDAPEntry entry;
		LDAPAttributeSet attribs = new LDAPAttributeSet();
		
		LDAPAttribute attrib;
		//take all attributes and add it to addition list
		try {
			it = fieldsMap.iterator();
			Set dontAdd = insert.getSqlStore().getDontAdd();
			
			boolean usedOC = false;
			
			while (it.hasNext()) {
				
				p = (Pair) it.next();
				
				
				
				if (dontAdd != null && dontAdd.contains(p.getNameUpperCase())) {
					//this is an attribute that wont be added, most likely because 
					
					continue;
				}
				
				
				
				field = p.getName();
				
				if (field.equalsIgnoreCase("objectClass")) {
					usedOC = true;
				}
				
				attrib = attribs.getAttribute(field);
				
				if (attrib == null) {
					attrib = new LDAPAttribute(field);
					attribs.add(attrib);
				}
				
				
				attrib.addValue(p.getValue());
			}
			
			if (! usedOC && insert.getSqlStore().getDefOC() != null) {
				attrib = new LDAPAttribute("objectClass");
				attrib.addValue(insert.getSqlStore().getDefOC());
				attribs.add(attrib);
			}
			
			//System.err.println("creating :" + JndiLdapConnection.getRealBase(insert));
			con.add(new LDAPEntry(JndiLdapConnection.getRealBase(insert),attribs));
		}
		catch (LDAPException ne) {
			throw new SQLNamingException(ne); 
		}
	}
}
