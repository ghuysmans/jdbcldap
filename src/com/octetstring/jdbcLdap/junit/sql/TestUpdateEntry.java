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

package com.octetstring.jdbcLdap.junit.sql;

import junit.framework.TestCase;
import junit.framework.*;

import com.novell.ldap.LDAPException;
import com.octetstring.jdbcLdap.sql.statements.JdbcLdapSelect;
import com.octetstring.jdbcLdap.jndi.*;
import com.octetstring.jdbcLdap.sql.*;
import com.octetstring.jdbcLdap.sql.statements.*;
import com.octetstring.jdbcLdap.util.*;
import java.sql.*;
import javax.naming.directory.*;
/**
 * @author mlb
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TestUpdateEntry extends TestCase {
	JndiLdapConnection con;
		boolean doInsert;
		boolean doDelete;
		boolean doDeleteMulti;
    
		/** Creates a new instance of TestUpdate */
		public TestUpdateEntry(String name) {
			super(name);
		}
    
		public void tearDown() throws java.lang.Exception {
			//System.out.println("in tear down");
			if (doDelete) {
				//System.out.println("deleteing");
				try {
					con.getConnection().delete("cn=\"Marc Boorshtein, OctetString\",ou=Product Development," + con.getBaseContext());
				}
				catch (LDAPException e) {
					
				}
				
				try {
					con.getConnection().delete("cn=Marc Boorshtein\\, OctetString,ou=Product Development," + con.getBaseContext());
				}
				catch (LDAPException e) {
					//if (care) throw e;
				}
				con.getConnection().delete("cn=Marc Boorsh,ou=Product Development,dc=idrs,dc=com");
				con.getConnection().delete("cn=Steve Boorsh,ou=Product Development,dc=idrs,dc=com");
				con.getConnection().delete("cn=Sherry Boorsh,ou=Product Development,dc=idrs,dc=com");
			}
        
			con.close();
		}
    
		public void setUp() throws java.lang.Exception {
			Class.forName("com.octetstring.jdbcLdap.sql.JdbcLdapDriver");
			con  = (JndiLdapConnection) DriverManager.getConnection(System.getProperty("ldapConnString") + "?SEARCH_SCOPE:=subTreeScope",System.getProperty("ldapUser"),System.getProperty("ldapPass"));
			Statement stmt = con.createStatement();
        
            stmt.executeUpdate("INSERT INTO cn=\"Marc Boorshtein, OctetString\",ou=Product Development (objectClass,objectClass,objectClass,sn,cn,title) VALUES (top,person,organizationalPerson,Boorshtein,\"Marc Boorshtein, OctetString\",test-single)");
			stmt.executeUpdate("INSERT INTO cn=Marc Boorsh,ou=Product Development (objectClass,objectClass,objectClass,sn,cn,title) VALUES (top,person,organizationalPerson,Boorsh,Marc Boorsh,test1-multi)");
			stmt.executeUpdate("INSERT INTO cn=Steve Boorsh,ou=Product Development (objectClass,objectClass,objectClass,sn,cn,title) VALUES (top,person,organizationalPerson,Boorsh,Steve Boorsh,test2-multi)");
			stmt.executeUpdate("INSERT INTO cn=Sherry Boorsh,ou=Product Development (objectClass,objectClass,objectClass,sn,cn,title) VALUES (top,person,organizationalPerson,Boorsh,Sherry Boorsh,test3-multi)");
		}
		
		public void testUpdateEntry() throws Exception {
			doDelete = true;
			doDeleteMulti = true;
			doInsert = true;
			PreparedStatement ps = con.prepareStatement("UPDATE ENTRY cn=Marc Boorsh,ou=Product Development DO DELETE SET title DO ADD SET l=Schaumburg,title=Engineer");
			int result = ps.executeUpdate();
			if (result != 1) fail("Results Are Wrong : " + result);
			
			ps = con.prepareStatement("SELECT DN,l,title FROM cn=Marc Boorsh,ou=Product Development");
			ResultSet rs = ps.executeQuery();
			rs.next();
			assertTrue("l wrong " + rs.getString("l"),"Schaumburg".equals(rs.getString("l")));
			assertTrue("title wrong " + rs.getString("title"),"Engineer".equals(rs.getString("title")));
			
			
		}
		
	public void testUpdateEntryMulti() throws Exception {
				doDelete = true;
				doDeleteMulti = true;
				doInsert = true;
				PreparedStatement ps = con.prepareStatement("UPDATE ENTRY ou=Product Development DO DELETE SET title DO ADD SET l=Schaumburg,title=Engineer WHERE sn=Boorsh");
				int result = ps.executeUpdate();
				if (result != 3) fail("Results Are Wrong : " + result);
			
				ps = con.prepareStatement("SELECT DN,l,title FROM ou=Product Development WHERE sn=Boorsh");
				ResultSet rs = ps.executeQuery();
				
				String ldif = "";
				ldif += "dn: cn=Marc Boorsh,ou=Product Development,dc=idrs,dc=com\n";
				ldif += "l: Schaumburg\n";
				ldif += "title: Engineer\n\n";
				ldif += "dn: cn=Steve Boorsh,ou=Product Development,dc=idrs,dc=com\n";
				ldif += "l: Schaumburg\n";
				ldif += "title: Engineer\n\n";
				ldif += "dn: cn=Sherry Boorsh,ou=Product Development,dc=idrs,dc=com\n";
				ldif += "l: Schaumburg\n";
				ldif += "title: Engineer";
				
		LDIF expLdif = new LDIF(ldif,true);
				//System.out.println("expLdif :  " + expLdif);
		
				LDIF found = new LDIF(rs,"DN",false);
				//System.out.println("found :  " + expLdif);
				LDIF dif = new LDIF();
				if (! found.compareLdif(expLdif,dif)) {
					fail("Results Don't Match : \nexpected:\n" + ldif + "\nfound:\n" + found.toString());
				}
			
			}
	
	public void testUpdateEntryMultiParamVal() throws Exception {
				doDelete = true;
				doDeleteMulti = true;
				doInsert = true;
				PreparedStatement ps = con.prepareStatement("UPDATE ENTRY ou=Product Development DO DELETE SET title DO ADD SET l=?,title=Engineer WHERE sn=Boorsh");
				ps.setString(1,"Schaumburg");
				int result = ps.executeUpdate();
				if (result != 3) fail("Results Are Wrong : " + result);
			
				ps = con.prepareStatement("SELECT DN,l,title FROM ou=Product Development WHERE sn=Boorsh");
				ResultSet rs = ps.executeQuery();
				
				String ldif = "";
				ldif += "dn: cn=Marc Boorsh,ou=Product Development,dc=idrs,dc=com\n";
				ldif += "l: Schaumburg\n";
				ldif += "title: Engineer\n\n";
				ldif += "dn: cn=Steve Boorsh,ou=Product Development,dc=idrs,dc=com\n";
				ldif += "l: Schaumburg\n";
				ldif += "title: Engineer\n\n";
				ldif += "dn: cn=Sherry Boorsh,ou=Product Development,dc=idrs,dc=com\n";
				ldif += "l: Schaumburg\n";
				ldif += "title: Engineer";
				
		LDIF expLdif = new LDIF(ldif,true);
				//System.out.println("expLdif :  " + expLdif);
		
				LDIF found = new LDIF(rs,"DN",false);
				//System.out.println("found :  " + expLdif);
				LDIF dif = new LDIF();
				if (! found.compareLdif(expLdif,dif)) {
					fail("Results Don't Match : \nexpected:\n" + ldif + "\nfound:\n" + found.toString());
				}
			
			}
	
	public void testUpdateEntryMultiParamArg() throws Exception {
				doDelete = true;
				doDeleteMulti = true;
				doInsert = true;
				
				
				
				PreparedStatement ps = con.prepareStatement("UPDATE ENTRY ou=Product Development DO DELETE SET title DO ADD SET l=Schaumburg,title=Engineer WHERE sn=?");
				ps.setString(1,"Boorsh");
				int result = ps.executeUpdate();
				if (result != 3) fail("Results Are Wrong : " + result);
			
				ps = con.prepareStatement("SELECT DN,l,title,description FROM ou=Product Development WHERE sn=Boorsh");
				ResultSet rs = ps.executeQuery();
				
				String ldif = "";
				ldif += "dn: cn=Marc Boorsh,ou=Product Development,dc=idrs,dc=com\n";
				ldif += "l: Schaumburg\n";
				ldif += "title: Engineer\n\n";
				ldif += "dn: cn=Steve Boorsh,ou=Product Development,dc=idrs,dc=com\n";
				ldif += "l: Schaumburg\n";
				ldif += "title: Engineer\n\n";
				ldif += "dn: cn=Sherry Boorsh,ou=Product Development,dc=idrs,dc=com\n";
				ldif += "l: Schaumburg\n";
				ldif += "title: Engineer";
				
		LDIF expLdif = new LDIF(ldif,true);
				//System.out.println("expLdif :  " + expLdif);
		
				LDIF found = new LDIF(rs,"DN",false);
				//System.out.println("found :  " + expLdif);
				LDIF dif = new LDIF();
				if (! found.compareLdif(expLdif,dif)) {
					fail("Results Don't Match : \nexpected:\n" + ldif + "\nfound:\n" + found.toString());
				}
			
			}
	
	public void testUpdateEntryMultiParamCmplx() throws Exception {
					doDelete = true;
					doDeleteMulti = true;
					doInsert = true;
				
				
				
					PreparedStatement ps = con.prepareStatement("UPDATE ENTRY ou=Product Development DO DELETE SET title DO ADD SET l=?,title=Engineer DO ADD SET description=?  WHERE sn=?");
					ps.setString(1,"Schaumburg");
					ps.setString(2,"Boston");
					ps.setString(3,"Boorsh");
					int result = ps.executeUpdate();
					if (result != 3) fail("Results Are Wrong : " + result);
			
					ps = con.prepareStatement("SELECT DN,l,description,title FROM ou=Product Development WHERE sn=Boorsh");
					ResultSet rs = ps.executeQuery();
				
					String ldif = "";
					ldif += "dn: cn=Marc Boorsh,ou=Product Development,dc=idrs,dc=com\n";
					ldif += "l: Schaumburg\n";
					ldif += "description: Boston\n";
					ldif += "title: Engineer\n\n";
					ldif += "dn: cn=Steve Boorsh,ou=Product Development,dc=idrs,dc=com\n";
					ldif += "l: Schaumburg\n";
					ldif += "description: Boston\n";
					ldif += "title: Engineer\n\n";
					ldif += "dn: cn=Sherry Boorsh,ou=Product Development,dc=idrs,dc=com\n";
					ldif += "l: Schaumburg\n";
					ldif += "description: Boston\n";
					ldif += "title: Engineer";
				
			LDIF expLdif = new LDIF(ldif,true);
					//System.out.println("expLdif :  " + expLdif);
		
					LDIF found = new LDIF(rs,"DN",false);
					//System.out.println("found :  " + expLdif);
					LDIF dif = new LDIF();
					
					String fndLdif = found.toString();
					
					if (! found.compareLdif(expLdif,dif)) {
						fail("Results Don't Match : \nexpected:\n" + ldif + "\nfound:\n" + fndLdif);
					}
			
				}
}
