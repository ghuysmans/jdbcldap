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
 * TestInsert.java
 *
 * Created on March 13, 2002, 4:22 PM
 */

package com.octetstring.jdbcLdap.junit.sql;

import junit.framework.*;
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
public class TestInsert extends junit.framework.TestCase {
	JndiLdapConnection con;
	boolean doDelete;
	public TestInsert(String name) {
		super(name);
	}
	
	protected void tearDown() throws java.lang.Exception {
		if (doDelete) {
			con.getContext().destroySubcontext("cn=Marc Boorshtein, OctetString,ou=Product Development");
		}
        	con.close();
    	}
    
    	protected void setUp() throws java.lang.Exception {
        	Class.forName("com.octetstring.jdbcLdap.sql.JdbcLdapDriver");
        	con  = (JndiLdapConnection) DriverManager.getConnection(System.getProperty("ldapConnString") + "?SEARCH_SCOPE:=subTreeScope","cn=Admin","manager");
    	}
    
	public void testParseStatement() throws Exception {
		doDelete = false;
		String stmt = "INSERT INTO uid=9999,cn=Marc,sn=Boorshtein,ou=Software,dc=idrs,dc=com (objectClass,objectClass,uid,cn,sn) VALUES(organizationalPerson,Person,?,?,?)";
		String[] fields = {"objectClass","objectClass","uid","cn","sn"};
		String[] vals = {"organizationalPerson","Person","?","?","?"};
		int[] offset = {2,3,4,0,0};
		JdbcLdapInsert ins = new JdbcLdapInsert();
		ins.init(null,stmt);
		SqlStore store = ins.getSqlStore();
		if (! store.getDistinguishedName().equals("uid=9999,cn=Marc,sn=Boorshtein,ou=Software,dc=idrs,dc=com")) {
			fail("-" + store.getDistinguishedName() + "-not correct");
			return;
		}
		
		if ( store.getFields().length != fields.length) fail("No Fields");
		
		for (int i=0;i<store.getFields().length;i++) {
			if (! store.getFields()[i].equals(fields[i])) {
				fail(store.getFields()[i] + " not "	+ fields[i]);
			}
			return;
		}
		
		if ( store.getInsertFields().length != vals.length) fail("No vals");
		
		for (int i=0;i<store.getInsertFields().length;i++) {
			if (! store.getInsertFields()[i].equals(vals[i])) {
				fail(store.getInsertFields()[i] + " not "	+ vals[i]);
			}
			return;
		}
		
		if (store.getFieldOffset().length != offset.length) fail("No Offset");
		
		for (int i=0;i<store.getFieldOffset().length;i++) {
			if (store.getFieldOffset()[i] != offset[i]) {
				fail(Integer.toString(store.getFieldOffset()[i]) + " not "	+ Integer.toString(offset[i]));
			}
			return;
		}
		
		assertTrue(true);
		
	}
	
	public void testInsertDirect() throws Exception {
		doDelete = true;
		String SQL = "INSERT INTO cn=Marc Boorshtein, OctetString,ou=Product Development (objectClass,objectClass,objectClass,sn,cn) VALUES (top,person,organizationalPerson,?,?)";
		JdbcLdapInsert ins = new JdbcLdapInsert();
		ins.init(con,SQL);
		ins.setValue(1,"Marc Boorshtein, OctetString");
		ins.setValue(0,"Marc");
		Insert jndiIns = new Insert();
		jndiIns.doInsert(ins);
		
		PreparedStatement ps = con.prepareStatement("SELECT DN,sn,cn FROM ou=Product Development WHERE cn=Marc Boorshtein, OctetString");
		//PreparedStatement ps = con.prepareStatement("SELECT DN,sn,cn FROM Development WHERE cn=Marc Boorshtein, OctetString");
		ResultSet rs = ps.executeQuery();
		rs.next();
		
		if (! rs.getString("cn").equals("Marc Boorshtein, OctetString")) {
			fail("bad cn : " + rs.getString("cn"));
		}
		System.out.println("DN : " + rs.getString("DN"));
		if (! rs.getString("DN").equals("cn=Marc Boorshtein, OctetString,ou=Product Development,dc=idrs,dc=com")) {
			fail("bad dn : " + rs.getString("DN"));
		}
	}
	
	public void testInsertStatement() throws Exception {
		doDelete = true;
		String SQL = "INSERT INTO cn=Marc Boorshtein, OctetString,ou=Product Development (objectClass,objectClass,objectClass,sn,cn) VALUES (top,person,organizationalPerson,Boorshtein,Marc Boorshtein, OctetString)";
		Statement stmt = con.createStatement();
		int res = stmt.executeUpdate(SQL);
		if (res < 1) {
			fail("no rows updated");
		}
		
		PreparedStatement ps = con.prepareStatement("SELECT DN,sn,cn FROM ou=Product Development WHERE cn=Marc Boorshtein, OctetString");
		ResultSet rs = ps.executeQuery();
		rs.next();
		
		if (! rs.getString("cn").equals("Marc Boorshtein, OctetString")) {
			fail("bad cn : " + rs.getString("cn"));
		}
		System.out.println("DN : " + rs.getString("DN"));
		if (! rs.getString("DN").equals("cn=Marc Boorshtein, OctetString,ou=Product Development,dc=idrs,dc=com")) {
			fail("bad dn : " + rs.getString("DN"));
		}
	}
	
	public void testInsertPreparedStatement() throws Exception {
		doDelete = true;
		String SQL = "INSERT INTO cn=Marc Boorshtein, OctetString,ou=Product Development (objectClass,objectClass,objectClass,sn,cn) VALUES (top,person,organizationalPerson,?,?)";
		PreparedStatement stmt = con.prepareStatement(SQL);
		stmt.setString(1,"Marc");
		stmt.setString(2,"Marc Boorshtein, OctetString");
		int res = stmt.executeUpdate();
		/*
		String SQL = "INSERT INTO cn=Marc Boorshtein, OctetString,ou=Product Development (objectClass,objectClass,objectClass,sn,cn) VALUES (top,person,organizationalPerson,Boorshtein,Marc Boorshtein, OctetString)";
		Statement stmt = con.createStatement();
		int res = stmt.executeUpdate(SQL);*/
		
		if (res < 1) {
			fail("no rows updated");
		}
		
		PreparedStatement ps = con.prepareStatement("SELECT DN,sn,cn FROM ou=Product Development WHERE cn=Marc Boorshtein, OctetString");
		ResultSet rs = ps.executeQuery();
		rs.next();
		
		if (! rs.getString("cn").equals("Marc Boorshtein, OctetString")) {
			fail("bad cn : " + rs.getString("cn"));
		}
		System.out.println("DN : " + rs.getString("DN"));
		if (! rs.getString("DN").equals("cn=Marc Boorshtein, OctetString,ou=Product Development,dc=idrs,dc=com")) {
			fail("bad dn : " + rs.getString("DN"));
		}
	}
        
        public void testInsertPreparedStatementParamInInto() throws Exception {
		doDelete = true;
		String SQL = "INSERT INTO cn,ou=Product Development (objectClass,objectClass,objectClass,sn,cn) VALUES (top,person,organizationalPerson,?,?)";
		PreparedStatement stmt = con.prepareStatement(SQL);
		stmt.setString(1,"Marc");
		stmt.setString(2,"Marc Boorshtein, OctetString");
		int res = stmt.executeUpdate();
		/*
		String SQL = "INSERT INTO cn=Marc Boorshtein, OctetString,ou=Product Development (objectClass,objectClass,objectClass,sn,cn) VALUES (top,person,organizationalPerson,Boorshtein,Marc Boorshtein, OctetString)";
		Statement stmt = con.createStatement();
		int res = stmt.executeUpdate(SQL);*/
		
		if (res < 1) {
			fail("no rows updated");
		}
		
		PreparedStatement ps = con.prepareStatement("SELECT DN,sn,cn FROM ou=Product Development WHERE cn=Marc Boorshtein, OctetString");
		ResultSet rs = ps.executeQuery();
		rs.next();
		
		if (! rs.getString("cn").equals("Marc Boorshtein, OctetString")) {
			fail("bad cn : " + rs.getString("cn"));
		}
		System.out.println("DN : " + rs.getString("DN"));
		if (! rs.getString("DN").equals("cn=Marc Boorshtein, OctetString,ou=Product Development,dc=idrs,dc=com")) {
			fail("bad dn : " + rs.getString("DN"));
		}
	}
}
