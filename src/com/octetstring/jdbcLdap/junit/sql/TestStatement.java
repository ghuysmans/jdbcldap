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
 * TestStatement.java
 *
 * Created on March 19, 2002, 7:51 PM
 */

package com.octetstring.jdbcLdap.junit.sql;
import junit.framework.*;
import com.octetstring.jdbcLdap.sql.statements.JdbcLdapSelect;
import com.octetstring.jdbcLdap.jndi.*;
import com.octetstring.jdbcLdap.sql.*;
import java.sql.*;
import javax.naming.directory.*;
import javax.naming.*;
import java.io.*;
import java.util.*;
/**
 *Tests the use of Statements and PreparedStatement interfaces
 */
public class TestStatement extends junit.framework.TestCase {
    
    JndiLdapConnection con;
    protected void tearDown() throws java.lang.Exception {
        con.close();
    }
    
    protected void setUp() throws java.lang.Exception {
        Class.forName("com.octetstring.jdbcLdap.sql.JdbcLdapDriver");
		con  = (JndiLdapConnection) DriverManager.getConnection(System.getProperty("ldapConnString") + "?SEARCH_SCOPE:=subTreeScope",System.getProperty("ldapUser"),System.getProperty("ldapPass"));
    }
    
    /**
     *Uses the java.sql.Statement interface to use SQL on LDAP
     */
    public void testGetResultSetFromStatement() throws Exception {
        String sql = "SELECT sn,ou,seeAlso FROM  WHERE ou=Peons AND cn=A*";
        
        java.sql.Statement stmt = new JdbcLdapStatement(con);
        
        ResultSet rs = stmt.executeQuery(sql);
        
        String field;
        
        LinkedList fieldsExp = new LinkedList();
        
        fieldsExp.add("sn");
        fieldsExp.add("ou");
        fieldsExp.add("seeAlso");
        
        LinkedList rowsExp = new LinkedList();
        HashMap row;
		row = new HashMap();
				row.put("sn","Dept");
				row.put("ou","Peons");
				row.put("seeAlso","cn=Ailina");
				rowsExp.add(row);
		row = new HashMap();
				row.put("sn","Poorman");
				row.put("ou","Peons");
				row.put("seeAlso","cn=Amir");
				rowsExp.add(row);
        row = new HashMap();
        row.put("sn","Zimmermann");
        row.put("ou","Peons");
        row.put("seeAlso","cn=Aggy");
        rowsExp.add(row);
        row = new HashMap();
        row.put("sn","Security");
        row.put("ou","Peons");
        row.put("seeAlso","cn=Agnella");
        rowsExp.add(row);
        
        
        row = new HashMap();
        row.put("sn","Hsiang");
        row.put("ou","Peons");
        row.put("seeAlso","cn=Audi");
        rowsExp.add(row);
        
        
        
        
        
        if (! this.tstResultByMetaData(rowsExp,rs)) {
            fail("Compare by Metadata Failed");
        }
        
        assertTrue(true);
        
    }
    
    /**
     *Uses the java.sql.Connection.createStatement(sql) to create a statement
     */
    public void testGetResultSetFromCreateStatement() throws Exception {
        String sql = "SELECT sn,ou,seeAlso FROM  WHERE ou=Peons AND cn=A*";
        
        java.sql.Statement stmt = con.createStatement();
        
        ResultSet rs = stmt.executeQuery(sql);
        
        String field;
        
        LinkedList fieldsExp = new LinkedList();
        
        fieldsExp.add("sn");
        fieldsExp.add("ou");
        fieldsExp.add("seeAlso");
        
        LinkedList rowsExp = new LinkedList();
        HashMap row;
		row = new HashMap();
				row.put("sn","Dept");
				row.put("ou","Peons");
				row.put("seeAlso","cn=Ailina");
				rowsExp.add(row);
		row = new HashMap();
				row.put("sn","Poorman");
				row.put("ou","Peons");
				row.put("seeAlso","cn=Amir");
				rowsExp.add(row);
        row = new HashMap();
        row.put("sn","Zimmermann");
        row.put("ou","Peons");
        row.put("seeAlso","cn=Aggy");
        rowsExp.add(row);
        row = new HashMap();
        row.put("sn","Security");
        row.put("ou","Peons");
        row.put("seeAlso","cn=Agnella");
        rowsExp.add(row);
        
        
        row = new HashMap();
        row.put("sn","Hsiang");
        row.put("ou","Peons");
        row.put("seeAlso","cn=Audi");
        rowsExp.add(row);
        
        
        
        
        
        if (! this.tstResultByMetaData(rowsExp,rs)) {
            fail("Compare by Metadata Failed");
        }
        
        assertTrue(true);
        
    }
    
    public void testType() throws Exception {
        String sql = "SELECT * FROM ou=Peons WHERE cn=Marc Boorshtein, OctetString";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        rs.next();
        ResultSetMetaData md = rs.getMetaData();
        HashMap ft = new HashMap();
        ft.put("telephoneNumber","INTEGER");
        ft.put("description","VARCHAR");
        ft.put("l","DATE");
        ft.put("seeAlso","DOUBLE");
        ft.put("postalAddress","TIMESTAMP");
        
        
        
        int m=md.getColumnCount();
        
        for (int i=1;i<=m;i++) {
            
            if (ft.get(md.getColumnLabel(i)) != null) {
            
                if (! ft.get(md.getColumnLabel(i)).equals(md.getColumnTypeName(i))) {
                    fail("Incorrect type for " + md.getColumnLabel(i) + " " + ft.get(md.getColumnLabel(i)) + " != " + md.getColumnTypeName(i));
                }
            }
        }
    }
    
    /**
     *Tests using a PreparedStatement
     */
    public void testPreparedStatement() throws Exception {
        String sql = "SELECT sn,ou,seeAlso FROM  WHERE ou=? AND cn=?";
        
        PreparedStatement ps = con.prepareStatement(sql);
        
        if (ps == null) System.out.println("ps is null");
        
        ps.setString(1,"Peons");
        ps.setString(2,"A*");
        
        ResultSet rs = ps.executeQuery();
        
        LinkedList fieldsExp = new LinkedList();
        
        
        fieldsExp.add("sn");
        fieldsExp.add("ou");
        fieldsExp.add("seeAlso");
        
        
        
        LinkedList rowsExp = new LinkedList();
        HashMap row;
		row = new HashMap();
				row.put("sn","Dept");
				row.put("ou","Peons");
				row.put("seeAlso","cn=Ailina");
				rowsExp.add(row);
				
		row = new HashMap();
				row.put("sn","Poorman");
				row.put("ou","Peons");
				row.put("seeAlso","cn=Amir");
				rowsExp.add(row);
				
        row = new HashMap();
        row.put("sn","Zimmermann");
        row.put("ou","Peons");
        row.put("seeAlso","cn=Aggy");
        rowsExp.add(row);
        row = new HashMap();
        row.put("sn","Security");
        row.put("ou","Peons");
        row.put("seeAlso","cn=Agnella");
        rowsExp.add(row);
        
        
        row = new HashMap();
        row.put("sn","Hsiang");
        row.put("ou","Peons");
        row.put("seeAlso","cn=Audi");
        rowsExp.add(row);
        
        
        
        assertTrue("table don't match",this.tstResultByMetaData(rowsExp,rs));
        
        
    }
    
    /**
     *Test a currently invalid SQL directive
     */
    public void testTryNonSQL() {
        try {
            java.sql.Statement stmt = new JdbcLdapStatement(con);
            stmt.executeQuery("DUBU dc=idrs,dc=com (cn,dn) VALUES (Marc,Marc Boorshtein, OctetString)");
        }
        catch (SQLException e) {
            assertTrue(true);
            return;
        }
        
        fail("Didn't fail on INSERT");
    }
    
    /** Creates new TestStatement */
    public TestStatement(String name) {
        super(name);
    }
    
    /**
     *Compares ResultSet amd a manually built table by generated ResultSetMetaData
     *@param test The Expected data
     *@param rs The generated ResultSet
     *@return True if test data matches ResultSet
     */
    boolean tstResultByMetaData(LinkedList test, java.sql.ResultSet rs) throws Exception {
        ResultSetMetaData rsmd = rs.getMetaData();
        int i;
        Iterator itRows = test.iterator();
        HashMap row;
        String field;
        Object[] vals;
        
        
        while (rs.next()) {
            row = (HashMap) itRows.next();
            
            vals = row.values().toArray();
            for (i=1;i<=rsmd.getColumnCount() ;i++) {
                
                if (! row.get(rsmd.getColumnName(i)).equals(rs.getString(rsmd.getColumnName(i)))) {
                    return false;
                }
            }
            
        }
        
        return true;
    }
    
    public void testBatchStatement() throws Exception {
        Statement stmt = con.createStatement();
        stmt.addBatch("INSERT INTO cn=Marc Boorsh,ou=Product Development (objectClass,objectClass,objectClass,sn,cn,title) VALUES (top,person,organizationalPerson,Boorsh,Marc Boorsh,test1-multi)");
        stmt.addBatch("INSERT INTO cn=Steve Boorsh,ou=Product Development (objectClass,objectClass,objectClass,sn,cn,title) VALUES (top,person,organizationalPerson,Boorsh,Steve Boorsh,test1-multi)");
        stmt.addBatch("INSERT INTO cn=Sherry Boorsh,ou=Product Development (objectClass,objectClass,objectClass,sn,cn,title) VALUES (top,person,organizationalPerson,Boorsh,Sherry Boorsh,test1-multi)");
        stmt.addBatch("DELETE FROM ou=Product Development WHERE sn=Boorsh");
        
        int[] res = stmt.executeBatch();
        if (res[0] != 1) fail("Incorrect result 0 " + res[0]);
        if (res[1] != 1) fail("Incorrect result 1 " + res[1]);
        if (res[2] != 1) fail("Incorrect result 2 " + res[2]);
        if (res[3] != 3) fail("Incorrect result 3 " + res[3]);
    }
    
    public void testBatchPreparedStatement() throws Exception {
        Statement stmt = con.createStatement();
        stmt.addBatch("INSERT INTO cn=Marc Boorsh,ou=Product Development (objectClass,objectClass,objectClass,sn,cn,title) VALUES (top,person,organizationalPerson,Boorsh,Marc Boorsh,test1-multi)");
        stmt.addBatch("INSERT INTO cn=Steve Boorsh,ou=Product Development (objectClass,objectClass,objectClass,sn,cn,title) VALUES (top,person,organizationalPerson,Boorsh,Steve Boorsh,test1-multi)");
        stmt.addBatch("INSERT INTO cn=Sherry Boorsh,ou=Product Development (objectClass,objectClass,objectClass,sn,cn,title) VALUES (top,person,organizationalPerson,Boorsh,Sherry Boorsh,test1-multi)");
        stmt.executeBatch();
        
        PreparedStatement ps = con.prepareStatement("UPDATE ou=Product Development SET sn=? WHERE cn=?");
        ps.setString(1,"Boorsh1");
        ps.setString(2,"Marc Boorsh");
        ps.addBatch();
        ps.setString(1,"Boorsh2");
        ps.setString(2,"Steve Boorsh");
        ps.addBatch();
        ps.setString(1,"Boorsh3");
        ps.setString(2,"Sherry Boorsh");
        ps.addBatch();
        
        ps.executeBatch();
        
        ResultSet rs = stmt.executeQuery("SELECT sn FROM ou=Product Development WHERE cn=Marc Boorsh");
        rs.next();
        if (! rs.getString("sn").equals("Boorsh1")) {
            fail("invalid sn : " + rs.getString("sn"));
            return;
        }
        
        rs = stmt.executeQuery("SELECT sn FROM ou=Product Development WHERE cn=Steve Boorsh");
        rs.next();
        if (! rs.getString("sn").equals("Boorsh2")) {
            fail("invalid sn : " + rs.getString("sn"));
            return;
        }
        
        rs = stmt.executeQuery("SELECT sn FROM ou=Product Development WHERE cn=Sherry Boorsh");
        rs.next();
        if (! rs.getString("sn").equals("Boorsh3")) {
            fail("invalid sn : " + rs.getString("sn"));
            return;
        }
        
        stmt.executeUpdate("DELETE FROM ou=Product Development WHERE sn=Boorsh*");
        
    }

}
