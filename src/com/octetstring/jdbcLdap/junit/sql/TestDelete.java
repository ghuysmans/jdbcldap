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
 * TestDelete.java
 *
 * Created on March 13, 2002, 4:22 PM
 */

package com.octetstring.jdbcLdap.junit.sql;

import junit.framework.*;

import com.novell.ldap.LDAPException;
import com.octetstring.jdbcLdap.sql.statements.JdbcLdapSelect;
import com.octetstring.jdbcLdap.jndi.*;
import com.octetstring.jdbcLdap.sql.*;
import com.octetstring.jdbcLdap.sql.statements.*;
import java.sql.*;
import javax.naming.directory.*;

/**
 *Tests the parsing of a SELECT statement
 *@author Marc Boorshtein, OctetString
 */
public class TestDelete extends junit.framework.TestCase {
    JndiLdapConnection con;
    boolean doInsert;
    boolean doDelete;
    boolean doDeleteMulti;
    
    public TestDelete(String name) {
        super(name);
    }
    
    protected void tearDown() throws java.lang.Exception {
        if (doDelete) {
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
        }
        
        if (doDeleteMulti) {
            con.getConnection().delete("cn=Marc Boorsh,ou=Product Development,dc=idrs,dc=com");
            con.getConnection().delete("cn=Steve Boorsh,ou=Product Development,dc=idrs,dc=com");
            con.getConnection().delete("cn=Sherry Boorsh,ou=Product Development,dc=idrs,dc=com");
        }
        con.close();
    }
    
    protected void setUp() throws java.lang.Exception {
        
        
        
        Class.forName("com.octetstring.jdbcLdap.sql.JdbcLdapDriver");
        con  = (JndiLdapConnection) DriverManager.getConnection(System.getProperty("ldapConnString") + "?SEARCH_SCOPE:=subTreeScope",System.getProperty("ldapUser"),System.getProperty("ldapPass"));
        Statement stmt = con.createStatement();
        stmt.executeUpdate("INSERT INTO cn=\"Marc Boorshtein, OctetString\",ou=Product Development (objectClass,objectClass,objectClass,sn,cn) VALUES (top,person,organizationalPerson,Boorshtein,\"Marc Boorshtein, OctetString\")");
        stmt.executeUpdate("INSERT INTO cn=Marc Boorsh,ou=Product Development (objectClass,objectClass,objectClass,sn,cn) VALUES (top,person,organizationalPerson,Boorsh,Marc Boorsh)");
        stmt.executeUpdate("INSERT INTO cn=Steve Boorsh,ou=Product Development (objectClass,objectClass,objectClass,sn,cn) VALUES (top,person,organizationalPerson,Boorsh,Steve Boorsh)");
        stmt.executeUpdate("INSERT INTO cn=Sherry Boorsh,ou=Product Development (objectClass,objectClass,objectClass,sn,cn) VALUES (top,person,organizationalPerson,Boorsh,Sherry Boorsh)");
        
    }
    
    public void testParse() throws Exception {
        doDelete = true;
        doDeleteMulti = true;
        String SQL = "DELETE FROM cn=\"Marc Boorshtein, OctetString\",ou=Product Development";
        JdbcLdapDelete del = new JdbcLdapDelete();
        del.init(con,SQL);
        SqlStore store = del.getSqlStore();
        
        if (! store.getFrom().equals("cn=\"Marc Boorshtein, OctetString\",ou=Product Development")) {
            fail("from incorrect : " + store.getFrom());
        }
        
        if (! store.getSimple()) {
            fail("should be simple");
        }
        
        assertTrue(true);
    }
    
    public void testDeleteDirect() throws Exception {
        doDelete = false;
        doDeleteMulti = true;
        String SQL = "DELETE FROM cn=\"Marc Boorshtein, OctetString\",ou=Product Development";
        JdbcLdapDelete del = new JdbcLdapDelete();
        del.init(con,SQL);
        int res = ((Integer) del.executeUpdate()).intValue();
        
        
        if (res < 1) {
            fail("no result");
        }
        
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT DN FROM ou=Product Development WHERE cn=\"Marc Boorshtein, OctetString\"");
        
        if (rs.next()) {
            fail("not deleted");
        }
        
        assertTrue(true);
    }
    
    public void testDeleteStatement() throws Exception {
        doDelete = false;
        doDeleteMulti = true;
        String SQL = "DELETE FROM cn=\"Marc Boorshtein, OctetString\",ou=Product Development";
        Statement stmt = con.createStatement();
        int res = stmt.executeUpdate(SQL);
        
        if (res < 1) {
            fail("no result");
            doDelete = true;
        }
        
        ResultSet rs = stmt.executeQuery("SELECT DN FROM ou=Product Development WHERE cn=\"Marc Boorshtein, OctetString\"");
        
        if (rs.next()) {
            fail("not deleted");
            doDelete = true;
        }
        
        assertTrue(true);
    }
    
    public void testDeletePreparedStatement() throws Exception {
        doDelete = false;
        doDeleteMulti = true;
        String SQL = "DELETE FROM cn=\"Marc Boorshtein, OctetString\",ou=Product Development";
        PreparedStatement ps = con.prepareStatement(SQL);
        int res = ps.executeUpdate(SQL);
        
        if (res < 1) {
            fail("no result");
            doDelete = true;
        }
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT DN FROM ou=Product Development WHERE cn=Marc Boorshtein, OctetString");
        
        if (rs.next()) {
            fail("not deleted");
            doDelete = true;
        }
        
        assertTrue(true);
    }
    
    public void testDeleteMultiParse() throws Exception {
        doDelete = true;
        doDeleteMulti = true;
        String SQL = "DELETE FROM cn=\"Marc Boorshtein, OctetString\",ou=Product Development WHERE cn=Marc Boorshtein, OctetString AND sn=Boorshtein";
        JdbcLdapDelete del = new JdbcLdapDelete();
        del.init(con,SQL);
        SqlStore store = del.getSqlStore();
        
        if (! store.getFrom().equals("cn=\"Marc Boorshtein, OctetString\",ou=Product Development")) {
            fail("from incorrect : " + store.getFrom());
        }
        
        if (! del.getWhere().equals("(&(cn=Marc Boorshtein, OctetString)(sn=Boorshtein))")) {
            fail("where incorrect : " + store.getWhere());
        }
        if ( store.getSimple()) {
            fail("should not be simple");
        }
        
        assertTrue(true);
    }
    
    public void testDeleteDirectMulti() throws Exception {
        doDelete = true;
        doDeleteMulti = false;
        String SQL = "DELETE FROM ou=Product Development WHERE sn=Boorsh";
        JdbcLdapDelete del = new JdbcLdapDelete();
        del.init(con,SQL);
        int res = ((Integer) del.executeUpdate()).intValue();
        
        
        if (res < 3) {
            fail("not enough results");
        }
        
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT DN FROM ou=Product Development WHERE sn=Boorsh");
        
        if (rs.next()) {
            fail("not deleted");
        }
        
        assertTrue(true);
    }
    
    public void testDeleteStatementMulti() throws Exception {
        doDelete = true;
        doDeleteMulti = false;
        String SQL = "DELETE FROM ou=Product Development WHERE sn=Boorsh";
        Statement stmt = con.createStatement();
        int res = stmt.executeUpdate(SQL);
        
        if (res < 3) {
            fail("no result");
            doDelete = true;
        }
        
        ResultSet rs = stmt.executeQuery("SELECT DN FROM ou=Product Development WHERE sn=Boorsh");
        
        if (rs.next()) {
            fail("not deleted");
            doDelete = true;
        }
        
        assertTrue(true);
    }
    
    public void testDeletePreparedStatementMulti() throws Exception {
        
        doDelete = true;
        doDeleteMulti = false;
        String SQL = "DELETE FROM ou=Product Development WHERE sn=?";
        
        PreparedStatement ps = con.prepareStatement(SQL);
        
        
        
        ps.setString(1,"Boorsh");
        
        
        
        int res = ps.executeUpdate();
        
        if (res < 3) {
            fail("no result");
            doDelete = true;
        }
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT DN FROM ou=Product Development WHERE sn=Boorsh");
        
        if (rs.next()) {
            fail("not deleted");
            doDelete = true;
        }
        
        assertTrue(true);
        
    }
}
