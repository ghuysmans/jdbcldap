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
 * TestSelectRetrieve.java
 *
 * Created on March 13, 2002, 10:29 PM
 */

package com.octetstring.jdbcLdap.junit.sql;
import junit.framework.*;
import com.octetstring.jdbcLdap.sql.statements.JdbcLdapSelect;
import com.octetstring.jdbcLdap.jndi.JndiLdapConnection;
import com.octetstring.jdbcLdap.sql.*;
import java.sql.*;
import javax.naming.directory.*;
import javax.naming.*;
import java.io.*;
import java.util.*;
import com.novell.ldap.*;
/**
 *Tests using a JdbcLdapSelect instance to retrieve results from a LDAP server
 *@author Marc Boorshtein, OctetString
 */
public class TestSelectRetrieve extends junit.framework.TestCase {
    JndiLdapConnection con;
    
    /** Creates new TestSelectRetrieve */
    public TestSelectRetrieve(String name) {
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
     *Retrieves info with :SELECT cn,sn,ou FROM  WHERE ou=Payroll OR ou=Peons
     */
    public void testSelectRetrieve() throws Exception {
        String sql = "SELECT cn,sn,ou FROM  WHERE ou=Payroll OR ou=Peons";
        JdbcLdapSelect sel = new JdbcLdapSelect();
        sel.init(con,sql);
        
        LDAPMessageQueue res = (LDAPMessageQueue) sel.executeQuery();        
        LDAPConnection ldap = con.getConnection();
        //SearchControls ctls = new SearchControls();
        
        String[] atts = {"cn","sn","ou"};
        String filter = "(|(ou=Payroll)(ou=Peons))";
        String base = con.getBaseContext();
        
        LDAPMessageQueue ctrl = ldap.search(base,LDAPConnection.SCOPE_SUB,filter,atts,false,null,null);
        
        
        
        
        
        LinkedList l2 = load(ctrl);
        LinkedList l1 = load(res);
        
        
        
        if (l1.size() != l2.size()) fail("Same results not gotten");
        
        assertTrue("Results don't match",this.compareLists(l1,l2));
        
    }
    
    /**
     *Retrieves info with :SELECT cn,sn,ou FROM  WHERE ou=? OR ou=?
     */
    public void testSelectRetrieveArgs() throws Exception {
        String sql = "SELECT cn,sn,ou FROM  WHERE ou=? OR ou=?";
        JdbcLdapSelect sel = new JdbcLdapSelect();
        sel.init(con,sql);
        sel.getArgs()[0] = "Payroll";
        sel.getArgs()[1] = "Peons";
        
        LDAPMessageQueue res = (LDAPMessageQueue) sel.executeQuery();        
        LDAPConnection ldap = con.getConnection();
        
        String[] atts = {"cn","sn","ou"};
        String filter = "(|(ou=Payroll)(ou=Peons))";
        
        
        String base = con.getBaseContext();
        
        LDAPMessageQueue ctrl = ldap.search(base,LDAPConnection.SCOPE_SUB,filter,atts,false,null,null);
        
        LinkedList l2 = load(ctrl);
        LinkedList l1 = load(res);
        
        
        
        if (l1.size() != l2.size()) fail("Same results not gotten");
        
        assertTrue("Results don't match",this.compareLists(l1,l2));
        
    }
    
    /**
     *Retrieves info with :SELECT cn,sn,ou FROM ou=Peons
     */
    public void testSelectRetrieveNoWhere() throws Exception {
        String sql = "SELECT cn,sn,ou FROM ou=Peons";
        JdbcLdapSelect sel = new JdbcLdapSelect();
        sel.init(con,sql);
        
        LDAPMessageQueue res = (LDAPMessageQueue) sel.executeQuery();        
        LDAPConnection ldap = con.getConnection();
        
        
        String[] atts = {"cn","sn","ou"};
        String filter = "(objectClass=*)";
        
        
        String base = "ou=Peons," + con.getBaseContext();
        
        LDAPMessageQueue ctrl = ldap.search(base,LDAPConnection.SCOPE_SUB,filter,atts,false,null,null);
        
        LinkedList l2 = load(ctrl);
        LinkedList l1 = load(res);
        
        
        //System.out.println(l1);
        //System.out.println();
        //System.out.println(l2);
        if (l1.size() != l2.size()) fail("Same results not gotten");
        
        assertTrue("Results don't match",this.compareLists(l1,l2));
        
        
        assertTrue(true);
        
    }
    
    
    LinkedList load(LDAPMessageQueue res) throws Exception {
    	LinkedList list = new LinkedList();
    	LinkedList row;
    	LDAPEntry entry;
    	Attributes atts;
    	NamingEnumeration enum2;
    	Enumeration vals;
    	Attribute att;
    	String attrid;
    	String val;
    	Object obj;
    	String svals[],svals1[];
    	entry = null;
    	boolean hasMore = true;
    	while (hasMore) {
    		row = new LinkedList();
    		LDAPMessage message = res.getResponse();
    		if (message instanceof LDAPSearchResult) {
    			entry = ((LDAPSearchResult) message).getEntry();
    		} else {
    			LDAPResponse resp = (LDAPResponse) message;
    			if (resp.getResultCode() == LDAPException.SUCCESS) {
    				hasMore = false;
    				break;
    			} else {
    				throw new LDAPException(resp.getErrorMessage(),resp.getResultCode(),resp.getErrorMessage(),resp.getMatchedDN());
    			}
    			
    		}
    		
    		//System.out.println(res.getName());
    		LDAPAttributeSet attribs = entry.getAttributeSet();
    		Iterator it = attribs.iterator();
    		while (it.hasNext()) {
    			LDAPAttribute attrib = (LDAPAttribute) it.next();
    			
    			attrid = attrib.getName();
    			//System.out.println(attrid);
    			val = "";
    			svals = attrib.getStringValueArray();
    			for (int i=0,m=svals.length;i<m;i++) {
    				obj = svals[i];
    				
    				val +=  obj.toString();
    			}
    			//System.out.println(val);
    			row.add(attrid + val);
    		}
    		
    		list.add(row);
    		
    	}
    	
    	return list;
    }
    
    /**
     *Loads a NamingEnumeration into a LinkedList
     *@param enum Search Results
     */
    LinkedList load(NamingEnumeration enum) throws Exception {
        LinkedList list = new LinkedList();
        LinkedList row;
        SearchResult res;
        Attributes atts;
        NamingEnumeration enum2;
        Enumeration vals;
        Attribute att;
        String attrid;
        String val;
        Object obj;
        
        while (enum.hasMore()) {
            row = new LinkedList();
            res = (SearchResult) enum.next();
            //System.out.println(res.getName());
            atts = (Attributes) res.getAttributes();
            
            for (enum2 = atts.getAll();enum2.hasMore();) {
                att = (Attribute) enum2.next();
                attrid = att.getID();
                //System.out.println(attrid);
                val = "";
                
                for (vals = att.getAll();vals.hasMoreElements();) {
                    obj = vals.nextElement();
                    
                    val +=  obj.toString();
                }
                //System.out.println(val);
                row.add(attrid + val);
            }
            
            list.add(row);
            
        }
        
        return list;
    }
    
    /**
     *Compares two lists
     *@param l1 List 1
     *@param l2 List 2
     *@return True if lists contain identical data
     */
    boolean compareLists(LinkedList l1, LinkedList l2) {
        int i,j;
        LinkedList ll1, ll2;
        for (i=0;i<l1.size();i++) {
            ll1 = (LinkedList) l1.get(i);
            ll2 = (LinkedList) l2.get(i);
            
            if (ll1.size() != ll2.size()) return false;
            
            for (j=0;j<ll1.size();j++) {
                if (! ll1.get(j).toString().equalsIgnoreCase(ll2.get(j).toString())) return false;
            }
            
        }
        
        return true;
    }
    
    /**
     *Prints the contents of a table
     *@param l LinkedList of data
     */
    String printTable(LinkedList l) {
        int i,j;
        StringBuffer buf = new StringBuffer();
        LinkedList row;
        for (i=0;i< l.size();i++) {
            row = (LinkedList) l.get(i);
            for (j=0;j<row.size();j++) {
              buf.append(row.get(j)).append("\t\t");   
            }
            buf.append("\n");
        }
        return buf.toString();
    }
    
    
    
    
    
}
