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
 * TestDriver.java
 *
 * Created on March 9, 2002, 5:03 PM
 */

package com.octetstring.jdbcLdap.junit.sql;

import junit.framework.*;
import java.sql.*;
import java.util.*;
import com.octetstring.jdbcLdap.jndi.JndiLdapConnection;
/**
 *Tests The loading of drivers and the creation of connections
 *@author Marc Boorshtein, OctetString
 */
public class TestDriver extends junit.framework.TestCase {

   
    
    public TestDriver(final String name) {
        super(name);
        
    }
    
   
    
    
   /**
    *Test loading the drivers
    */ 
    public void testLoadDriver() {
        try {
            Class.forName("com.octetstring.jdbcLdap.sql.JdbcLdapDriver").newInstance();
        }
        
        catch (Exception eee) {
            eee.printStackTrace(System.out);
            fail("error" + eee);
        }
        
        this.assertTrue(true);
    }
    
    /**
     *Test generating connections
     */
    public void testGenerateConnection() {
        try {
            Class.forName("com.octetstring.jdbcLdap.sql.JdbcLdapDriver").newInstance();
            
            Connection con = DriverManager.getConnection(System.getProperty("ldapConnString") ,System.getProperty("ldapUser"),System.getProperty("ldapPass"));
        }
        catch (ClassNotFoundException ee) {
         ee.printStackTrace(System.out);
         fail("Not able to load driver");
        }
        
        catch (SQLException e) {
            e.printStackTrace(System.out);
            fail("Driver not detected by url");
        }
        catch (Exception eee) {
            eee.printStackTrace(System.out);
            fail("error" + eee);
        }
        
        
        
        
        this.assertTrue(true);
        
    }
    
    public void testSetSizeLimit() {
    	Connection con = null;
    	try {
            Class.forName("com.octetstring.jdbcLdap.sql.JdbcLdapDriver").newInstance();
            
            String url = System.getProperty("ldapConnString");
            if (url.indexOf("?") == -1) {
            		url += "?SIZE_LIMIT:=1";
            } else {
            		url += "&SIZE_LIMIT:=1";
            }
            
            System.out.println("URL : " + url);
            
            con = DriverManager.getConnection(url ,System.getProperty("ldapUser"),System.getProperty("ldapPass"));
        }
        catch (ClassNotFoundException ee) {
         ee.printStackTrace(System.out);
         fail("Not able to load driver");
        }
        
        catch (SQLException e) {
            e.printStackTrace(System.out);
            fail("Driver not detected by url");
        }
        catch (Exception eee) {
            eee.printStackTrace(System.out);
            fail("error" + eee);
        }
        
        
        try {
			ResultSet rs = con.createStatement().executeQuery("SELECT DN FROM oneLevelScope;");
			while (rs.next()) {
				System.out.println(rs.getString("DN"));
			}
		} catch (SQLException e1) {
			this.assertTrue(true);
			return;
		}
        
		this.assertTrue(false);
    }
    
    public void testSetTimeLimit() {
    	Connection con = null;
    	try {
            Class.forName("com.octetstring.jdbcLdap.sql.JdbcLdapDriver").newInstance();
            
            String url = System.getProperty("ldapConnString");
            if (url.indexOf("?") == -1) {
            		url += "?TIME_LIMIT:=1";
            } else {
            		url += "&TIME_LIMIT:=1";
            }
            
            System.out.println("URL : " + url);
            
            con = DriverManager.getConnection(url ,System.getProperty("ldapUser"),System.getProperty("ldapPass"));
        }
        catch (ClassNotFoundException ee) {
         ee.printStackTrace(System.out);
         fail("Not able to load driver");
        }
        
        catch (SQLException e) {
        	System.out.println("error code : " + e.getErrorCode());
         if (e.getErrorCode() == 85) {
         	this.assertTrue(true);
         	return;
         }
        	e.printStackTrace(System.out);
            fail("Driver not detected by url");
        }
        catch (Exception eee) {
            eee.printStackTrace(System.out);
            fail("error" + eee);
        }
        
        
        try {
			ResultSet rs = con.createStatement().executeQuery("SELECT DN FROM oneLevelScope;");
			while (rs.next()) {
				System.out.println(rs.getString("DN"));
			}
		} catch (SQLException e1) {
			this.assertTrue(true);
			return;
		}
        
		this.assertTrue(false);
    }
    
    
    public void testGenerateTLSConnection() {
        try {
            Class.forName("com.octetstring.jdbcLdap.sql.JdbcLdapDriver").newInstance();
            
            Connection con = DriverManager.getConnection(System.getProperty("ldapConnStringTLS") ,System.getProperty("ldapUser"),System.getProperty("ldapPass"));
        }
        catch (ClassNotFoundException ee) {
         ee.printStackTrace(System.out);
         fail("Not able to load driver");
        }
        
        catch (SQLException e) {
            e.printStackTrace(System.out);
            fail("Driver not detected by url");
        }
        catch (Exception eee) {
            eee.printStackTrace(System.out);
            fail("error" + eee);
        }
        
        this.assertTrue(true);
        
    }
    
    /**
     *Tests generating a connection using URL properties
     */
    public void testGenerateConnectionUrlProps() {
        try {
            Class.forName("com.octetstring.jdbcLdap.sql.JdbcLdapDriver").newInstance();
            
            Connection con = DriverManager.getConnection(System.getProperty("ldapConnString") + "?java.naming.authentication:=simple&user:=" + System.getProperty("ldapUser") + "&password:=" + System.getProperty("ldapPass"));
        }
        catch (ClassNotFoundException ee) {
         ee.printStackTrace(System.out);
         fail("Not able to load driver");
        }
        
        catch (SQLException e) {
            e.printStackTrace(System.out);
            fail("Driver not detected by url");
        }
        catch (Exception eee) {
            eee.printStackTrace(System.out);
            fail("error" + eee);
        }
        
        this.assertTrue(true);
    }
    
    /**
     *Attempts an anonymouse connection
     */
    public void testGenerateConnectionAnon() {
        try {
            Class.forName("com.octetstring.jdbcLdap.sql.JdbcLdapDriver").newInstance();
            
            Connection con = DriverManager.getConnection(System.getProperty("ldapConnString") + "?java.naming.authentication:=none");
        }
        catch (ClassNotFoundException ee) {
         ee.printStackTrace(System.out);
         fail("Not able to load driver");
        }
        
        catch (SQLException e) {
            e.printStackTrace(System.out);
            fail("Driver not detected by url");
        }
        catch (Exception eee) {
            eee.printStackTrace(System.out);
            fail("error" + eee);
        }
        
        this.assertTrue(true);
    }
    
    /**
     *Attempted an anonymouse connection with no connection type specified
     */
    public void testGenerateConnectionAnonNoTypeSpeced() {
        try {
            Class.forName("com.octetstring.jdbcLdap.sql.JdbcLdapDriver").newInstance();
            
            Connection con = DriverManager.getConnection(System.getProperty("ldapConnString"));
        }
        catch (ClassNotFoundException ee) {
         ee.printStackTrace(System.out);
         fail("Not able to load driver");
        }
        
        catch (SQLException e) {
            e.printStackTrace(System.out);
            fail("Driver not detected by url");
        }
        catch (Exception eee) {
            eee.printStackTrace(System.out);
            fail("error" + eee);
        }
        
        this.assertTrue(true);
    }
    
    /**
     *Tests connecting with a bad user
     */
    public void testGenerateConnectionBadUser() {
        try {
            Class.forName("com.octetstring.jdbcLdap.sql.JdbcLdapDriver").newInstance();
            
            Connection con = DriverManager.getConnection(System.getProperty("ldapConnString") + "?user:=cn=Marc&password:=manager");
        }
        catch (ClassNotFoundException ee) {
         
         fail("Not able to load driver");
        }
        
        catch (SQLException e) {
            this.assertTrue(true);
            return;
        }
        catch (Exception eee) {
         
            fail("error" + eee);
        }
        
        this.fail("Connection Succeeded");
    }
   
    /**
     *Tests setting the scope
     */
    public void testSetScope() throws Exception {
        Connection con=null;
        try {
            Class.forName("com.octetstring.jdbcLdap.sql.JdbcLdapDriver").newInstance();
            
            con = DriverManager.getConnection(System.getProperty("ldapConnString") + "?user:=" + System.getProperty("ldapUser") + "&password:=" + System.getProperty("ldapPass") + "&SEARCH_SCOPE:=OBJECT_SCOPE");
        }
        catch (ClassNotFoundException ee) {
         
         fail("Not able to load driver");
        }
        
        catch (SQLException e) {
            fail("Not able to connect");
            
        }
        catch (Exception eee) {
         
            fail("error" + eee);
        }
        
        this.assertEquals(((JndiLdapConnection) con).getSearchScope(),"OBJECT_SCOPE");
    }
    
    /**
     *Tests setting concat atts
     */
    public void testSetConcatAtts() throws Exception {
        Connection con=null;
        try {
            Class.forName("com.octetstring.jdbcLdap.sql.JdbcLdapDriver").newInstance();
            
            con = DriverManager.getConnection(System.getProperty("ldapConnString") + "?user:=" + System.getProperty("ldapUser") + "&password:=" + System.getProperty("ldapPass") + "&CONCAT_ATTS:=true");
        }
        catch (ClassNotFoundException ee) {
         
         fail("Not able to load driver");
        }
        
        catch (SQLException e) {
            fail("Not able to connect");
            
        }
        catch (Exception eee) {
         
            fail("error" + eee);
        }
        
        this.assertTrue("Proprty not recognized",((JndiLdapConnection) con).getConcatAtts());
    }
    
    /**
     *Tests set cache
     */
    public void testSetCache() throws Exception {
        Connection con=null;
     try {
            Class.forName("com.octetstring.jdbcLdap.sql.JdbcLdapDriver").newInstance();
            
            con = DriverManager.getConnection(System.getProperty("ldapConnString") + "?user:=" + System.getProperty("ldapUser") + "&password:=" + System.getProperty("ldapPass") + "&CACHE_STATEMENTS:=true");
        }
        catch (ClassNotFoundException ee) {
         
         fail("Not able to load driver");
        }
        
        catch (SQLException e) {
            fail("Not able to connect");
            
        }
        catch (Exception eee) {
         
            fail("error" + eee);
        }
        
        this.assertTrue(((JndiLdapConnection) con).cacheStatements());
    }
    
    protected void setUp() throws java.lang.Exception {
        
    }
    
    protected void tearDown() throws java.lang.Exception {
    }
    
    
    
}
