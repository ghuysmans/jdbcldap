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
 * TestUpdate.java
 *
 * Created on May 24, 2002, 11:07 AM
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
 *Tests the updating of a record
 *@author Marc Boorshtein, OctetString
 */
public class TestUpdate extends junit.framework.TestCase {
    JndiLdapConnection con;
    boolean doInsert;
    boolean doDelete;
    boolean doDeleteMulti;
    
    /** Creates a new instance of TestUpdate */
    public TestUpdate(String name) {
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
        
        
        stmt.executeUpdate("INSERT INTO cn=\"Marc Boorshtein, OctetString\",ou=Product Development (objectClass,objectClass,objectClass,sn,cn,title) VALUES (top,person,organizationalPerson,Boorshtein,\"Marc Boorshtein, OctetString\",test-single)");
        stmt.executeUpdate("INSERT INTO cn=Marc Boorsh,ou=Product Development (objectClass,objectClass,objectClass,sn,cn,title) VALUES (top,person,organizationalPerson,Boorsh,Marc Boorsh,test1-multi)");
        stmt.executeUpdate("INSERT INTO cn=Steve Boorsh,ou=Product Development (objectClass,objectClass,objectClass,sn,cn,title) VALUES (top,person,organizationalPerson,Boorsh,Steve Boorsh,test2-multi)");
        stmt.executeUpdate("INSERT INTO cn=Sherry Boorsh,ou=Product Development (objectClass,objectClass,objectClass,sn,cn,title) VALUES (top,person,organizationalPerson,Boorsh,Sherry Boorsh,test3-multi)");
    }
    
    public void testParse() throws Exception {
        doDelete = true;
        doDeleteMulti = true;
        String sql = "UPDATE ou=Product Development SET title=Title set!, sn=?";
        JdbcLdapUpdate update = new JdbcLdapUpdate();
        update.init(con,sql);
        SqlStore store = update.getSqlStore();
        String[] look;
        
        //test from
        if (! store.getDistinguishedName().equals("ou=Product Development")) {
            fail("FROM not correct : " + store.getDistinguishedName());
            return;
        }
        
        
        //test fields
        look = store.getFields();
        if (! (look[0].equals("title") && look[1].equals("sn"))) {
            fail("improper fields : " + look[0] + " " + look[1]);
            return;
        }
        
        //test insert vals
        look = store.getInsertFields();
        if (! (look[0].equals("Title set!") && look[1].equals("?"))) {
            fail("improper insert fields : " + look[0] + " " + look[1]);
            return;
        }
        
        if (! store.getWhere().equals("(objectClass=*)")) {
            fail("improper where statement : " + store.getWhere());
            return;
        }
        
        assertTrue(true);
    }
    
    public void testParseMulti() throws Exception {
        doDelete = true;
        String sql = "UPDATE ou=Product Development SET title=Title set!, sn=? WHERE cn=?";
        JdbcLdapUpdate update = new JdbcLdapUpdate();
        update.init(con,sql);
        SqlStore store = update.getSqlStore();
        String[] look;
        
        //test from
        if (! store.getDistinguishedName().equals("ou=Product Development")) {
            fail("FROM not correct : " + store.getDistinguishedName());
            return;
        }
        
        
        //test fields
        look = store.getFields();
        if (! (look[0].equals("title") && look[1].equals("sn"))) {
            fail("improper fields : " + look[0] + " " + look[1]);
            return;
        }
        
        //test insert vals
        look = store.getInsertFields();
        if (! (look[0].equals("Title set!") && look[1].equals("?"))) {
            fail("improper insert fields : " + look[0] + " " + look[1]);
            return;
        }
        
        //test where string
        if (! store.getWhere().equals("(cn={0})")) {
            fail("invalid where : " + store.getWhere());
        }
        
        update.setValue(0,"Boorshtein");
        update.setValue(1,"Marc Boorshtein, OctetString");
        String filter = update.getFilterWithParams();
        if (! filter.equals("(cn=Marc Boorshtein, OctetString)")) {
            fail("invalid complete filter : " + filter);
        }
        
        assertTrue(true);
    }
    
    public void testUpdate() throws Exception {
        doDelete = true;
        doDeleteMulti = true;
        
        String SQL = "UPDATE ou=Product Development SET sn=Boorshtein WHERE sn=Boorsh";
        String search = "SELECT sn FROM ou=Product Development WHERE cn=*Boorsh";
        Statement stmt = con.createStatement();
        JdbcLdapUpdate update = new JdbcLdapUpdate();
        update.init(con,SQL);
        int count = ((Integer) update.executeUpdate()).intValue();
        if (count < 3) {
            fail("Error:, count is : " + count);
            return;
        }
        
        ResultSet rs = stmt.executeQuery(search);
        count = 0;
        while (rs.next()) {
            if (! rs.getString("sn").equals("Boorshtein")) {
                fail("sn is wrong : " + rs.getString("sn"));
                return;
            }
            count ++;
        }
        
        if (count < 3) {
            fail("wrong count! : " + count);
            return;
        }
        
        assertTrue(true);
        
    }
    
    public void testUpdateParams() throws Exception {
        doDelete = true;
        doDeleteMulti = true;
        
        String SQL = "UPDATE ou=Product Development SET sn=? WHERE sn=?";
        String search = "SELECT sn FROM ou=Product Development WHERE cn=*Boorsh";
        Statement stmt = con.createStatement();
        JdbcLdapUpdate update = new JdbcLdapUpdate();
        update.init(con,SQL);
        update.setValue(0,"Boorshtein");
        update.setValue(1,"Boorsh");
        int count = ((Integer) update.executeUpdate()).intValue();
        if (count < 3) {
            fail("Error:, count is : " + count);
            return;
        }
        
        ResultSet rs = stmt.executeQuery(search);
        count = 0;
        while (rs.next()) {
            if (! rs.getString("sn").equals("Boorshtein")) {
                fail("sn is wrong : " + rs.getString("sn"));
                return;
            }
            count ++;
        }
        
        if (count < 3) {
            fail("wrong count! : " + count);
            return;
        }
        
        assertTrue(true);
        
    }
    
    public void testUpdateStatement() throws Exception {
        doDelete = true;
        doDeleteMulti = true;
        
        String SQL = "UPDATE ou=Product Development SET sn=Boorshtein WHERE sn=Boorsh";
        String search = "SELECT sn FROM ou=Product Development WHERE cn=*Boorsh";
        Statement stmt = con.createStatement();
        
        
        int count = stmt.executeUpdate(SQL);
        if (count < 3) {
            fail("Error:, count is : " + count);
            return;
        }
        
        ResultSet rs = stmt.executeQuery(search);
        count = 0;
        while (rs.next()) {
            if (! rs.getString("sn").equals("Boorshtein")) {
                fail("sn is wrong : " + rs.getString("sn"));
                return;
            }
            count ++;
        }
        
        if (count < 3) {
            fail("wrong count! : " + count);
            return;
        }
        
        assertTrue(true);
        
    }
    
    public void testUpdatePreparedStatement() throws Exception {
        doDelete = true;
        doDeleteMulti = true;
        
        String SQL = "UPDATE ou=Product Development SET sn=? WHERE sn=?";
        String search = "SELECT sn FROM ou=Product Development WHERE cn=*Boorsh";
        PreparedStatement ps = con.prepareStatement(SQL);
        Statement stmt = con.createStatement();
        
        ps.setString(1,"Boorshtein");
        ps.setString(2,"Boorsh");
        
        int count = ps.executeUpdate();
        if (count < 3) {
            fail("Error:, count is : " + count);
            return;
        }
        
        ResultSet rs = stmt.executeQuery(search);
        count = 0;
        while (rs.next()) {
            if (! rs.getString("sn").equals("Boorshtein")) {
                fail("sn is wrong : " + rs.getString("sn"));
                return;
            }
            count ++;
        }
        
        if (count < 3) {
            fail("wrong count! : " + count);
            return;
        }
        
        assertTrue(true);
        
    }
    
	public void testUpdateQuotePreparedStatement() throws Exception {
			doDelete = true;
			doDeleteMulti = true;
        
			String SQL = "UPDATE ou=Product Development SET sn=\"Boorshtein, Marc\" WHERE sn=?";
			String search = "SELECT sn FROM ou=Product Development WHERE cn=*Boorsh";
			PreparedStatement ps = con.prepareStatement(SQL);
			Statement stmt = con.createStatement();
        
			//ps.setString(1,"Boorshtein");
			ps.setString(1,"Boorsh");
        
			int count = ps.executeUpdate();
			if (count < 3) {
				fail("Error:, count is : " + count);
				return;
			}
        
			ResultSet rs = stmt.executeQuery(search);
			count = 0;
			while (rs.next()) {
				if (! rs.getString("sn").equals("Boorshtein, Marc")) {
					fail("sn is wrong : " + rs.getString("sn"));
					return;
				}
				count ++;
			}
        
			if (count < 3) {
				fail("wrong count! : " + count);
				return;
			}
        
			assertTrue(true);
        
		}
    
}
