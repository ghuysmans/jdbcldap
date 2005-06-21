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
 * TestResultSet.java
 *
 * Created on March 16, 2002, 3:22 PM
 */

package com.octetstring.jdbcLdap.junit.sql;

import junit.framework.*;

import com.novell.ldap.LDAPMessageQueue;
import com.novell.ldap.LDAPSearchResults;
import com.octetstring.jdbcLdap.sql.statements.JdbcLdapSelect;
import com.octetstring.jdbcLdap.jndi.*;
import com.octetstring.jdbcLdap.sql.*;
import java.sql.*;
import javax.naming.directory.*;
import javax.naming.*;
import java.io.*;
import java.util.*;

/**
 *Tests the creation of a ResultSet
 *@author Marc Boorshtein, OctetString
 */
public class TestResultSet extends junit.framework.TestCase {
    JndiLdapConnection con;
    
    public TestResultSet(String name) {
        super(name);
    }
    
    protected void tearDown() throws java.lang.Exception {
        con.close();
    }
    
    protected void setUp() throws java.lang.Exception {
        Class.forName("com.octetstring.jdbcLdap.sql.JdbcLdapDriver");
		con  = (JndiLdapConnection) DriverManager.getConnection(System.getProperty("ldapConnString") + "?SEARCH_SCOPE:=subTreeScope",System.getProperty("ldapUser"),System.getProperty("ldapPass"));
    }
    
    /**
     *Tests comparing a ResultSet agains a NamingEnumeration
     */
    public void testGetResultSet() throws Exception {
        String sql = "SELECT sn,ou,seeAlso FROM  WHERE ou=Peons AND cn=A*";
        
		        
        JdbcLdapSelect sel = new JdbcLdapSelect();
        sel.init(con,sql);
        String field;
        
        
        LDAPMessageQueue enum = (LDAPMessageQueue) sel.executeQuery();
        
        UnpackResults pack = new UnpackResults(con);
        pack.unpackJldap(enum,sel.getRetrieveDN(),sel.getSqlStore().getFrom(),con.getBaseDN(),sel.getSqlStore().getRevFieldMap());
        
        LdapResultSet rs = new LdapResultSet(con,null,pack,sel.getBaseContext());
        
        
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
        
        if (! this.tstResultSetByName(fieldsExp,rowsExp,rs)) {
            fail("Compare by Name Failed");
        }
        
        rs.beforeFirst();
        
        if (! this.tstResultByMetaData(rowsExp,rs)) {
            fail("Compare by Metadata Failed");
        }
        
        assertTrue(true);
        
    }
    
    /**
     *Test all implemented getXXX Methods
     */
    public void testGetMethods() throws Exception {
        String sql = "SELECT sn,ou,seeAlso,l,description,title FROM  WHERE ou=Peons AND cn=Aggy Zimmermann";
        JdbcLdapSelect sel = new JdbcLdapSelect();
        sel.init(con,sql);
        String field;
        StringBuffer buf = new StringBuffer();
        
        LDAPMessageQueue enum = (LDAPMessageQueue) sel.executeQuery();
        
        UnpackResults pack = new UnpackResults(con);
        pack.unpackJldap(enum,sel.getRetrieveDN(),sel.getSqlStore().getFrom(),con.getBaseDN(),sel.getSqlStore().getRevFieldMap());
        
        LdapResultSet rs = new LdapResultSet(con,null,pack,sel.getBaseContext());
        
        LinkedList fieldsExp = new LinkedList();
        
        String ssn = "Zimmermann",
               sou = "Peons",
               sseeAlso = "cn=Aggy",
               sl = "100",
               sdescription = "true",
               stitle_0 = "1981-3-20",
               stitle_1 = "05:05:00",
               stitle_2 = "1981-3-20 05:05:00";
        
        assertTrue("No results",rs.next());
        
        try {
            assertTrue("String Values Don't Match",rs.getString("sn").equals(ssn));
        }
        catch (Exception e) {
            fail("String : " + e.toString());
            return;
        }
        
        try {
            assertTrue("Int Values Don't Match",rs.getInt("l") == Integer.parseInt(sl));
        }
        catch (Exception e) {
            fail("Int : " + e.toString());
            return;
        }
        
        try {
            assertTrue("Long Values Don't Match",rs.getLong("l") == Long.parseLong(sl));
        }
        catch (Exception e) {
            fail("Long : " + e.toString());
            return;
        }
        
        try {
            assertTrue("Doubel Values Don't Match",rs.getDouble("l") == Double.parseDouble(sl));
        }
        catch (Exception e) {
            fail("Double : " + e.toString());
            return;
        }
        
        try {
            assertTrue("Float Values Don't Match",rs.getFloat("l") == Float.parseFloat(sl));
        }
        catch (Exception e) {
            fail("Float : " + e.toString());
            return;
        }
        
        try {
            assertTrue("Boolean Values Don't Match",rs.getBoolean(sdescription) == Boolean.getBoolean(sdescription));
        }
        catch (Exception e) {
            fail("Boolean : " + e.toString());
            return;
        }
        
        try {
            assertTrue("Short Values Don't Match",rs.getShort("l") == Short.parseShort(sl));
        }
        catch (Exception e) {
            fail("Short : " + e.toString());
            return;
        }
        
        try {
            System.out.println("Title : " + rs.getString("title_0"));
            assertTrue("Date Values Don't Match",rs.getDate("title_0").equals(java.sql.Date.valueOf(stitle_0) ));
        }
        catch (Exception e) {
             fail("Title_0 : " + e.toString() + "\n\n" + rs.getString("title_0") );
            return;
        }
        
        try {
            assertTrue("Time Values Don't Match",rs.getTime("title_1").equals(java.sql.Time.valueOf(stitle_1) ));
        }
        catch (Exception e) {
             fail("Title_1 : " + e.toString() + "\n\n" + rs.getString("title_1") );
            return;
        }
        
        try {
            assertTrue("Timestamp Values Don't Match",rs.getTimestamp("title_2").equals(java.sql.Timestamp.valueOf(stitle_2) ));
        }
        catch (Exception e) {
             fail("Title_2 : " + e.toString() + "\n\n" + rs.getString("title_2") );
            return;
        }
        
        try {
            buf.setLength(0);
            BufferedReader in = new BufferedReader(new InputStreamReader(rs.getAsciiStream("ou")));
            
            
            assertTrue("Asscii Stream Values Don't Match",in.readLine().equals(sou) );
        }
        catch (Exception e) {
             fail("AscciStream : " + e.toString() + "\n\n" + rs.getString("ou") );
            return;
        }
        
        try {
            buf.setLength(0);
            BufferedReader in = new BufferedReader(rs.getCharacterStream("ou"));
            
            
            assertTrue("Character Stream Values Don't Match",in.readLine().equals(sou) );
        }
        catch (Exception e) {
             fail("CharacterStream : " + e.toString() + "\n\n" + rs.getString("ou") );
            return;
        }
        
        assertTrue(true);
        
        
        
    }
    
    
    /**
     *Compares ResultSet amd a manually built table by passed in names
     *@param fields Field names to check
     *@param test The Expected data
     *@param rs The generated ResultSet
     *@return True if test data matches ResultSet
     */
    boolean tstResultSetByName(LinkedList fields, LinkedList test, LdapResultSet rs) throws Exception {
        Iterator itFields;
        Iterator itRows = test.iterator();
        HashMap row;
        String field;
        
        while (rs.next()) {
            row = (HashMap) itRows.next();
            itFields = fields.iterator();
            
            while (itFields.hasNext()) {
                field = (String) itFields.next();
                System.out.println("field : " + field);
                System.out.println("equal? " + row.get(field) + "==" + rs.getString(field));
                if (! row.get(field).equals(rs.getString(field))) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    
    /**
     *Compares ResultSet amd a manually built table by generated ResultSetMetaData
     *@param test The Expected data
     *@param rs The generated ResultSet
     *@return True if test data matches ResultSet
     */
    boolean tstResultByMetaData(LinkedList test, LdapResultSet rs) throws Exception {
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
                    System.out.println("TEST FAILED : " + vals[i-1] + "; " + rsmd.getColumnName(i) + "; " + rs.getString(rsmd.getColumnName(i)));
                	return false;
                }
            }
            
        }
        
        return true;
    }
    

}
