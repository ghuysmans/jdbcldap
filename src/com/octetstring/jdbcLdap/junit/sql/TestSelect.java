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
 * TestSelect.java
 *
 * Created on March 13, 2002, 4:22 PM
 */

package com.octetstring.jdbcLdap.junit.sql;

import junit.framework.*;
import com.octetstring.jdbcLdap.sql.statements.JdbcLdapSelect;
import com.octetstring.jdbcLdap.jndi.JndiLdapConnection;
import com.octetstring.jdbcLdap.sql.*;
import java.sql.*;
import javax.naming.directory.*;

/**
 *Tests the parsing of a SELECT statement
 *@author Marc Boorshtein, OctetString
 */
public class TestSelect extends junit.framework.TestCase {
    String name;
    JndiLdapConnection con;
    
    /** Creates new TestSelect */
    public TestSelect(String name) {
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
     *Parses SELECT cn,sn,ou FROM dc=idrs,dc=com WHERE ou=Payroll OR ou=Peons
     */
    public void testSQLSimple() throws SQLException  {
        String sql = "SELECT cn,sn,ou FROM dc=idrs,dc=com WHERE ou=Payroll OR ou=Peons";
        JdbcLdapSelect sel = new JdbcLdapSelect();
        sel.init(con,sql);
        int i;
        
        String[] fields = sel.getSearchAttributes();
             
        if (! fields[0].equalsIgnoreCase("cn")) {
            fail("Fields don't match");
            return;
        }
        if (! fields[1].equalsIgnoreCase("sn")) {
            fail("Fields don't match");
            return;
        }
        if (! fields[2].equalsIgnoreCase("ou")) {
            fail("Fields don't match");
            return;
        }
        
        if (! sel.getBaseContext().equalsIgnoreCase("dc=idrs,dc=com")) {
            fail("FROM not correct");
            return;
        }
        
        if (! sel.getSearchString().equalsIgnoreCase("(|(ou=Payroll)(ou=Peons))")) {
            fail("WHERE not correct\n(|(ou=Payroll)(ou=Peons)) !=" + sel.getSearchString());
            return;
        }
        
        if (sel.getSearchScope() != SearchControls.SUBTREE_SCOPE) {
            fail("Scope's not equal");
            return;
        }
        
        assertTrue(true);
        
        
        
    }
    
    /**
     *Parses:SELECT cn,sn,ou FROM dc=idrs,dc=com WHERE ou=? OR ou=?
     */
    public void testSQLWithArgs() throws SQLException  {
        String sql = "SELECT cn,sn,ou FROM dc=idrs,dc=com WHERE ou=? OR ou=?";
        JdbcLdapSelect sel = new JdbcLdapSelect();
        sel.init(con,sql);
        int i;
        
        String[] fields = sel.getSearchAttributes();
        
        if (! fields[0].equalsIgnoreCase("cn")) {
            fail("Fields don't match");
            return;
        }
        if (! fields[1].equalsIgnoreCase("sn")) {
            fail("Fields don't match");
            return;
        }
        if (! fields[2].equalsIgnoreCase("ou")) {
            fail("Fields don't match");
            return;
        }
        
        if (! sel.getBaseContext().equalsIgnoreCase("dc=idrs,dc=com")) {
            fail("FROM not correct");
            return;
        }
        
        if (! sel.getSearchString().equalsIgnoreCase("(|(ou={0})(ou={1}))")) {
            fail("WHERE not correct\n(|(ou={0})(ou={1})) !=" + sel.getSearchString());
            return;
        }
        
        sel.getArgs()[0] = "Peons";
        sel.getArgs()[1] = "Payroll";
        
        if (! sel.getFilterWithParams().equalsIgnoreCase("(|(ou=Peons)(ou=Payroll))")) {
            fail("WHERE not correct\n(|(ou=Peons)(ou=Payroll)) !=" + sel.getFilterWithParams());
            return;
        }
        
        if (sel.getSearchScope() != SearchControls.SUBTREE_SCOPE) {
            fail("Scope's not equal");
            return;
        }
        
        assertTrue(true);
        
        
        
    }
    
    /**
     *Parses:SELECT cn,sn,ou FROM dc=idrs,dc=com WHERE ou=\\? OR ou=?
     */
    public void testSQLWithArgsEscape() throws SQLException  {
        String sql = "SELECT cn,sn,ou FROM dc=idrs,dc=com WHERE ou=\\? OR ou=?";
        JdbcLdapSelect sel = new JdbcLdapSelect();
        sel.init(con,sql);
        int i;
        
        String[] fields = sel.getSearchAttributes();
        
        if (! fields[0].equalsIgnoreCase("cn")) {
            fail("Fields don't match");
            return;
        }
        if (! fields[1].equalsIgnoreCase("sn")) {
            fail("Fields don't match");
            return;
        }
        if (! fields[2].equalsIgnoreCase("ou")) {
            fail("Fields don't match");
            return;
        }
        
        if (! sel.getBaseContext().equalsIgnoreCase("dc=idrs,dc=com")) {
            fail("FROM not correct");
            return;
        }
        
        if (! sel.getSearchString().equalsIgnoreCase("(|(ou=?)(ou={0}))")) {
            fail("WHERE not correct\n(|(ou=?)(ou={0})) !=" + sel.getSearchString());
            return;
        }
        
        if (sel.getSearchScope() != SearchControls.SUBTREE_SCOPE) {
            fail("Scope's not equal");
            return;
        }
        
        assertTrue(true);
        
        
        
    }
    
    /**
     *Parses:SELECT * FROM dc=idrs,dc=com WHERE ou=Payroll OR ou=Peons
     */
    public void testSQLAllAtts() throws SQLException  {
        String sql = "SELECT * FROM dc=idrs,dc=com WHERE ou=Payroll OR ou=Peons";
        JdbcLdapSelect sel = new JdbcLdapSelect();
        sel.init(con,sql);
        int i;
        
        String[] fields = sel.getSearchAttributes();
        
        
        if (fields.length != 0) {
            fail("Fields don't match");
            return;
        }
        
        if (! sel.getBaseContext().equalsIgnoreCase("dc=idrs,dc=com")) {
            fail("FROM not correct");
            return;
        }
        
        if (! sel.getSearchString().equalsIgnoreCase("(|(ou=Payroll)(ou=Peons))")) {
            fail("WHERE not correct\n(|(ou=Payroll)(ou=Peons)) !=" + sel.getSearchString());
            return;
        }
        
        if (sel.getSearchScope() != SearchControls.SUBTREE_SCOPE) {
            fail("Scope's not equal");
            return;
        }
        
        assertTrue(true);
        
        
        
    }
    
    
    /**
     *Parses:SELECT cn,sn,ou FROM objectScope;dc=idrs,dc=com WHERE ou=Payroll OR ou=Peons
     */
    public void testSQLSetScope() throws SQLException  {
        String sql = "SELECT cn,sn,ou FROM objectScope;dc=idrs,dc=com WHERE ou=Payroll OR ou=Peons";
        JdbcLdapSelect sel = new JdbcLdapSelect();
        sel.init(con,sql);
        int i;
        
        String[] fields = sel.getSearchAttributes();
        
        if (! fields[0].equalsIgnoreCase("cn")) {
            fail("Fields don't match");
            return;
        }
        if (! fields[1].equalsIgnoreCase("sn")) {
            fail("Fields don't match");
            return;
        }
        if (! fields[2].equalsIgnoreCase("ou")) {
            fail("Fields don't match");
            return;
        }
        
        if (! sel.getBaseContext().equalsIgnoreCase("dc=idrs,dc=com")) {
            fail("FROM not correct");
            return;
        }
        
        if (! sel.getSearchString().equalsIgnoreCase("(|(ou=Payroll)(ou=Peons))")) {
            fail("WHERE not correct\n(|(ou=Payroll)(ou=Peons)) !=" + sel.getSearchString());
            return;
        }
        
        if (sel.getSearchScope() != SearchControls.OBJECT_SCOPE) {
            fail("Scope's not equal");
            return;
        }
        
        assertTrue(true);
        
        
        
    }
    
    /**
     *Parses:SELECT cn,sn,ou FROM objectScope;dc=idrs,dc=com
     */
    public void testSQLNoWhere() throws SQLException  {
        String sql = "SELECT cn,sn,ou FROM objectScope;dc=idrs,dc=com";
        JdbcLdapSelect sel = new JdbcLdapSelect();
        sel.init(con,sql);
        int i;
        
        String[] fields = sel.getSearchAttributes();
        
        if (! fields[0].equalsIgnoreCase("cn")) {
            fail("Fields don't match");
            return;
        }
        if (! fields[1].equalsIgnoreCase("sn")) {
            fail("Fields don't match");
            return;
        }
        if (! fields[2].equalsIgnoreCase("ou")) {
            fail("Fields don't match");
            return;
        }
        
        if (! sel.getBaseContext().equalsIgnoreCase("dc=idrs,dc=com")) {
            fail("FROM not correct");
            return;
        }
        
        if (! sel.getSearchString().equals("(objectClass=*)")) {
            fail("WHERE not correct\n" + sel.getSearchString());
            return;
        }
        
        if (sel.getSearchScope() != SearchControls.OBJECT_SCOPE) {
            fail("Scope's not equal");
            return;
        }
        
        assertTrue(true);
        
        
        
    }
    
    /**
     *Parses:SELECT cn,sn,ou FROM objectScop;dc=idrs,dc=com
     */
    public void testSQLBadScope() throws SQLException  {
        String sql = "SELECT cn,sn,ou FROM objectScop;dc=idrs,dc=com";
        
        try {
            JdbcLdapSelect sel = new JdbcLdapSelect();
            sel.init(con,sql);
        }
        catch (SQLException e) {
            assertTrue(true);
            return;
        }
        
        fail("Bad scope not recognized");
        
        
        
        
    }
    
    /**
     *Parses:SELECT * FROM dc=idrs,dc=com WHERE ou=Payroll OR ou=Peons
     */
    public void testSQLWhereSpace() throws SQLException  {
        String sql = "SELECT * FROM dc=idrs,dc=com WHERE cn=Marge Chima";
        JdbcLdapSelect sel = new JdbcLdapSelect();
        sel.init(con,sql);
        int i;
        
        String[] fields = sel.getSearchAttributes();
        
        
        if (fields.length != 0) {
            fail("Fields don't match");
            return;
        }
        
        if (! sel.getBaseContext().equalsIgnoreCase("dc=idrs,dc=com")) {
            fail("FROM not correct");
            return;
        }
        
        if (! sel.getSearchString().equalsIgnoreCase("(cn=Marge Chima)")) {
            fail("WHERE not correct\n(cn=Marge Chima) !=" + sel.getSearchString());
            return;
        }
        
        if (sel.getSearchScope() != SearchControls.SUBTREE_SCOPE) {
            fail("Scope's not equal");
            return;
        }
        
        assertTrue(true);
        
        
        
    }
    
    public void testSQLWhereSpaceQuotes() throws SQLException  {
        String sql = "SELECT * FROM dc=idrs,dc=com WHERE cn='Marge Chima'";
        JdbcLdapSelect sel = new JdbcLdapSelect();
        sel.init(con,sql);
        int i;
        
        String[] fields = sel.getSearchAttributes();
        
        
        if (fields.length != 0) {
            fail("Fields don't match");
            return;
        }
        
        if (! sel.getBaseContext().equalsIgnoreCase("dc=idrs,dc=com")) {
            fail("FROM not correct");
            return;
        }
        
        if (! sel.getSearchString().equalsIgnoreCase("(cn=Marge Chima)")) {
            fail("WHERE not correct\n(cn=Marge Chima) !=" + sel.getSearchString());
            return;
        }
        
        if (sel.getSearchScope() != SearchControls.SUBTREE_SCOPE) {
            fail("Scope's not equal");
            return;
        }
        
        assertTrue(true);
        
        
        
    }
    
    public void testSQLWhereSpaceQuotesWhiteSpace() throws SQLException  {
        String sql = "SELECT * FROM dc=idrs,dc=com WHERE cn = 'Marge Chima'";
        JdbcLdapSelect sel = new JdbcLdapSelect();
        sel.init(con,sql);
        int i;
        
        String[] fields = sel.getSearchAttributes();
        
        
        if (fields.length != 0) {
            fail("Fields don't match");
            return;
        }
        
        if (! sel.getBaseContext().equalsIgnoreCase("dc=idrs,dc=com")) {
            fail("FROM not correct");
            return;
        }
        
        if (! sel.getSearchString().equalsIgnoreCase("(cn =Marge Chima)")) {
            fail("WHERE not correct\n(cn =Marge Chima) !=" + sel.getSearchString());
            return;
        }
        
        if (sel.getSearchScope() != SearchControls.SUBTREE_SCOPE) {
            fail("Scope's not equal");
            return;
        }
        
        assertTrue(true);
        
        
        
    }
    
    public void testSQLWhereLIKE() throws SQLException  {
        String sql = "SELECT * FROM dc=idrs,dc=com WHERE cn LIKE '\\%Ma%\\%'";
        JdbcLdapSelect sel = new JdbcLdapSelect();
        sel.init(con,sql);
        int i;
        
        String[] fields = sel.getSearchAttributes();
        
        
        if (fields.length != 0) {
            fail("Fields don't match");
            return;
        }
        
        if (! sel.getBaseContext().equalsIgnoreCase("dc=idrs,dc=com")) {
            fail("FROM not correct");
            return;
        }
        
        if (! sel.getSearchString().equalsIgnoreCase("(cn=%Ma*%)")) {
            fail("WHERE not correct\n(cn=%Ma*%) !=" + sel.getSearchString());
            return;
        }
        
        if (sel.getSearchScope() != SearchControls.SUBTREE_SCOPE) {
            fail("Scope's not equal");
            return;
        }
        
        assertTrue(true);
        
        
        
    }
    
    
    public void testSQLWhereLIKEOC() throws SQLException  {
    		System.err.println("---------------------------");
        //String sql = "SELECT * FROM dc=idrs,dc=com WHERE objectClass='*' AND cn LIKE '\\%Ma%\\%'";
    	String sql = "SELECT * FROM dc=idrs,dc=com WHERE cn LIKE '\\%Ma%\\%' AND objectClass='*'";
        JdbcLdapSelect sel = new JdbcLdapSelect();
        sel.init(con,sql);
        int i;
        
        String[] fields = sel.getSearchAttributes();
        
        
        if (fields.length != 0) {
            fail("Fields don't match");
            return;
        }
        
        if (! sel.getBaseContext().equalsIgnoreCase("dc=idrs,dc=com")) {
            fail("FROM not correct");
            return;
        }
        
        if (! sel.getSearchString().equalsIgnoreCase("(&(cn=%Ma*%)(objectClass=*))")) {
            fail("WHERE not correct\n(&(cn=%Ma*%)(objectClass=*)) !=" + sel.getSearchString());
            return;
        }
        
        if (sel.getSearchScope() != SearchControls.SUBTREE_SCOPE) {
            fail("Scope's not equal");
            return;
        }
        
        assertTrue(true);
        
        
        
    }
    
    public void testSQLWhereLIKEPresence() throws SQLException  {
		System.err.println("---------------------------");
    //String sql = "SELECT * FROM dc=idrs,dc=com WHERE objectClass='*' AND cn LIKE '\\%Ma%\\%'";
	String sql = "SELECT * FROM dc=idrs,dc=com WHERE objectClass IS NOT NULL AND cn LIKE '\\%Ma%\\%'";
    JdbcLdapSelect sel = new JdbcLdapSelect();
    sel.init(con,sql);
    int i;
    
    String[] fields = sel.getSearchAttributes();
    
    
    if (fields.length != 0) {
        fail("Fields don't match");
        return;
    }
    
    if (! sel.getBaseContext().equalsIgnoreCase("dc=idrs,dc=com")) {
        fail("FROM not correct");
        return;
    }
    
    if (! sel.getSearchString().equalsIgnoreCase("(&(objectClass=*)(cn=%Ma*%))")) {
        fail("WHERE not correct\n(&(objectClass=*)(cn=%Ma*%)) !=" + sel.getSearchString());
        return;
    }
    
    if (sel.getSearchScope() != SearchControls.SUBTREE_SCOPE) {
        fail("Scope's not equal");
        return;
    }
    
    assertTrue(true);
    
    
    
}
    
    public void testSQLWhereLIKENOTPresence() throws SQLException  {
		System.err.println("---------------------------");
    //String sql = "SELECT * FROM dc=idrs,dc=com WHERE objectClass='*' AND cn LIKE '\\%Ma%\\%'";
	String sql = "SELECT * FROM dc=idrs,dc=com WHERE objectClass IS NULL AND cn LIKE '\\%Ma%\\%'";
    JdbcLdapSelect sel = new JdbcLdapSelect();
    sel.init(con,sql);
    int i;
    
    String[] fields = sel.getSearchAttributes();
    
    
    if (fields.length != 0) {
        fail("Fields don't match");
        return;
    }
    
    if (! sel.getBaseContext().equalsIgnoreCase("dc=idrs,dc=com")) {
        fail("FROM not correct");
        return;
    }
    
    if (! sel.getSearchString().equalsIgnoreCase("(&(!(objectClass=*))(cn=%Ma*%))")) {
        fail("WHERE not correct\n(&(!(objectClass=*))(cn=%Ma*%)) !=" + sel.getSearchString());
        return;
    }
    
    if (sel.getSearchScope() != SearchControls.SUBTREE_SCOPE) {
        fail("Scope's not equal");
        return;
    }
    
    assertTrue(true);
    
    
    
}
    
    public void testSQLWhereLIKEPresenceMixed() throws SQLException  {
		System.err.println("---------------------------");
    //String sql = "SELECT * FROM dc=idrs,dc=com WHERE objectClass='*' AND cn LIKE '\\%Ma%\\%'";
	String sql = "SELECT * FROM dc=idrs,dc=com WHERE  (cn LIKE 'Clayton*' AND cn LIKE '*Donley') AND (objectClass IS NOT NULL AND cn IS NOt NULL)";
    JdbcLdapSelect sel = new JdbcLdapSelect();
    sel.init(con,sql);
    int i;
    
    String[] fields = sel.getSearchAttributes();
    
    
    if (fields.length != 0) {
        fail("Fields don't match");
        return;
    }
    
    if (! sel.getBaseContext().equalsIgnoreCase("dc=idrs,dc=com")) {
        fail("FROM not correct");
        return;
    }
    
    if (! sel.getSearchString().equalsIgnoreCase("(&(&(cn=Clayton*)(cn=*Donley))(&(objectClass=*)(cn=*)))")) {
        fail("WHERE not correct\n(&(&(cn=Clayton*)(cn=*Donley))(&(objectClass=*)(cn=*))) !=" + sel.getSearchString());
        return;
    }
    
    if (sel.getSearchScope() != SearchControls.SUBTREE_SCOPE) {
        fail("Scope's not equal");
        return;
    }
    
    assertTrue(true);
    
    
    
}
    
}
