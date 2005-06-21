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
 * TestSqlToLdap.java
 *
 * Created on March 11, 2002, 9:35 AM
 */

package com.octetstring.jdbcLdap.junit.sql;

import java.sql.SQLException;
import java.util.*;
import junit.framework.*;
import com.octetstring.jdbcLdap.sql.SqlToLdap;
/**
 *Tests the conversion from SQL style to LDAP style filters
 *@author Marc Boorshtein, OctetString
 */
public class TestSqlToLdap extends junit.framework.TestCase {
    String testName;
    /** Creates new TestSqlToLdap */
    public TestSqlToLdap(final String name) {
        super(name);    
    }
    
    
    
    protected void setUp() throws java.lang.Exception {
    }
    
    protected void tearDown() throws java.lang.Exception {
    }
    
    /**
     *Tests parsing a large SQL statement : F1=1 AND F2=2 OR (f3=3 OR f4=4) AND NOT f5=5
     */
    public void testParseLong() {
        int i;
        String expr = "F1=1 AND F2=2 OR (f3=3 OR f4=4) AND NOT f5=5";
        LinkedList list = new LinkedList();
        list.add("F1=1");
        list.add("AND");
        list.add("F2=2");
        list.add("OR");
        list.add("(");
        list.add("f3=3");
        list.add("OR");
        list.add("f4=4");
        list.add(")");
        list.add("AND");
        list.add("NOT");
        list.add("f5=5");
        
        SqlToLdap trans = new SqlToLdap();
        LinkedList comp = trans.inOrder(expr,null);
        
        boolean match = true;
        for (i=0;i<list.size();i++) {
            match = list.get(i).equals(comp.get(i));
            if (! match) break;
        }
        
        
            String result = "";
            for (i=0;i<list.size();i++) {
                result += list.get(i).toString() + " --- " + comp.get(i).toString() + "\n";
            }
            
            if (! match) {
            
                fail("Parseing Failed : \n" + result);
            }
            else {
                assertTrue("PArsing Succeeded : \n" + result,true);
            }
        
        
    }
    
    /**
     *Tests parsing an AND statement F1=1 AND F2=2
     */
    public void testParseAND() {
        int i;
        String expr = "F1=1 AND F2=2";
        LinkedList list = new LinkedList();
        list.add("F1=1");
        list.add("AND");
        list.add("F2=2");
        
        
        SqlToLdap trans = new SqlToLdap();
        LinkedList comp = trans.inOrder(expr,null);
        
        boolean match = true;
        for (i=0;i<list.size();i++) {
            match = list.get(i).equals(comp.get(i));
            if (! match) break;
        }
        
        
            String result = "";
            for (i=0;i<list.size();i++) {
                result += list.get(i).toString() + " --- " + comp.get(i).toString() + "\n";
            }
            
            if (! match) {
            
                fail("Parseing Failed : \n" + result);
            }
            else {
                assertTrue("Parsing Succeeded : \n" + result,true);
            }
        
        
    }
    
    /**
     *Tests parsing an OR statement F2=2 OR f3=3
     */
    public void testParseOR() {
        int i;
        String expr = "F2=2 OR f3=3";
        LinkedList list = new LinkedList();
        list.add("F2=2");
        
        list.add("OR");
        list.add("f3=3");
        
        
        SqlToLdap trans = new SqlToLdap();
        LinkedList comp = trans.inOrder(expr,null);
        
        boolean match = true;
        for (i=0;i<list.size();i++) {
            match = list.get(i).equals(comp.get(i));
            if (! match) break;
        }
        
        
            String result = "";
            for (i=0;i<list.size();i++) {
                result += list.get(i).toString() + " --- " + comp.get(i).toString() + "\n";
            }
            
            if (! match) {
            
                fail("Parseing Failed : \n" + result);
            }
            else {
                assertTrue("Parsing Succeeded : \n" + result,true);
            }
        
        
    }
    
    /**
     *Tests a NOT expression NOT f5=5
     */
    public void testParseNOT() {
        
        int i;
        String expr = "NOT f5=5";
        LinkedList list = new LinkedList();
        
        list.add("NOT");
        list.add("f5=5");
        
        SqlToLdap trans = new SqlToLdap();
        LinkedList comp = trans.inOrder(expr,null);
        
        boolean match = true;
        for (i=0;i<list.size();i++) {
            match = list.get(i).equals(comp.get(i));
            if (! match) break;
        }
        
        
            String result = "";
            for (i=0;i<list.size();i++) {
                result += list.get(i).toString() + " --- " + comp.get(i).toString() + "\n";
            }
            
            if (! match) {
            
                fail("Parseing Failed : \n" + result);
            }
            else {
                assertTrue("PArsing Succeeded : \n" + result,true);
            }
        
        
    }
    
    /**
     *Tests the parsing of Parenthasys (((((F1=1)))))
     */
    public void testParsePars() {
        int i;
        String expr = "(((((F1=1)))))";
        LinkedList list = new LinkedList();
        list.add("(");
        list.add("(");
        list.add("(");
        list.add("(");
        list.add("(");
        list.add("F1=1");
        list.add(")");
        list.add(")");
        list.add(")");
        list.add(")");
        list.add(")");
        
        SqlToLdap trans = new SqlToLdap();
        LinkedList comp = trans.inOrder(expr,null);
        
        boolean match = true;
        for (i=0;i<list.size();i++) {
            match = list.get(i).equals(comp.get(i));
            if (! match) break;
        }
        
        
            String result = "";
            for (i=0;i<list.size();i++) {
                result += list.get(i).toString() + " --- " + comp.get(i).toString() + "\n";
            }
            
            if (! match) {
            
                fail("Parseing Failed : \n" + result);
            }
            else {
                assertTrue("Parsing Succeeded : \n" + result,true);
            }
        
        
        
    }
    
    /**
     *Tests a conversion of NOT ou=Peons AND (ou=Accounting OR (ou=Payroll AND NOT ou=Planning)) into (&(!(ou=Peons))(|(ou=Accounting)(&(ou=Payroll)(!(ou=Planning)))))
     */
    public void testSQLtoLDAP() throws SQLException{
        String sql = "NOT ou=Peons AND (ou=Accounting OR (ou=Payroll AND NOT ou=Planning))";
        String ldapExp = "(&(!(ou=Peons))(|(ou=Accounting)(&(ou=Payroll)(!(ou=Planning)))))";
        SqlToLdap trans = new SqlToLdap();
        
        
        
        
        String ldap = trans.convertToLdap(sql,null);
        /*
        System.out.println("sql : " + sql);
        System.out.println("ldap     : " + ldap);
        System.out.println("ldap exp : " + ldapExp);
        */
        this.assertTrue("Statements are equal",ldap.equalsIgnoreCase(ldapExp));
    }
    

}
