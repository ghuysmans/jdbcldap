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
 * TestUnpack.java
 *
 * Created on March 14, 2002, 11:03 AM
 */

package com.octetstring.jdbcLdap.junit.sql;
import junit.framework.*;

import com.novell.ldap.LDAPMessageQueue;
import com.novell.ldap.LDAPSearchResults;
import com.octetstring.jdbcLdap.sql.statements.JdbcLdapSelect;
import com.octetstring.jdbcLdap.jndi.*;
import com.octetstring.jdbcLdap.sql.*;
import com.octetstring.jdbcLdap.util.*;
import java.sql.*;
import javax.naming.directory.*;
import javax.naming.*;

import java.util.*;
/**
 *Tests unpacking of data from a NamingEnumeration 
 *@author Marc Boorshtein, OctetString
 */
public class TestUnpack extends junit.framework.TestCase {
    
    JndiLdapConnection con;
    
    /** Creates new TestUnpack */
    public TestUnpack(String name) {
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
     *Test SQL:SELECT sn,ou,seeAlso FROM  WHERE ou=Peons AND cn=A*
     */
    public void testUnpackingResults() throws Exception {
        String sql = "SELECT sn,ou,seeAlso FROM  WHERE ou=Peons AND cn=A*";
        JdbcLdapSelect sel = new JdbcLdapSelect();
        sel.init(con,sql);
        String field;
        
        
        
        LDAPMessageQueue enum = (LDAPMessageQueue) sel.executeQuery();
        
        UnpackResults pack = new UnpackResults(con);
        pack.unpackJldap(enum,sel.getRetrieveDN(),sel.getSqlStore().getFrom(),con.getBaseDN(),null);
        
        ArrayList fieldsExp = new ArrayList();
        
        fieldsExp.add("sn");
        fieldsExp.add("ou");
        fieldsExp.add("seeAlso");
        
        /*
        field = new FieldStore("sn",0);
        fieldsExp.put("sn",field);
        field = new FieldStore("ou",0);
        fieldsExp.put("ou",field);
        field = new FieldStore("seeAlso",0);
        fieldsExp.put("seeAlso",field);
        */
        
        ArrayList fields = pack.getFieldNames();
        
        Iterator it = fields.iterator();
        
        while (it.hasNext()) {
            field = (String) it.next();
            if (! fieldsExp.contains(field) ) {
                fail("Incorrect fields returned : " + field);
            }
        }
        
        
        
        
        
		String cmpLdif; 
		cmpLdif = "dn: cn=Audi Hsiang,ou=Peons,dc=idrs,dc=com\n";
		cmpLdif += "sn: Hsiang\n";
		cmpLdif += "ou: Peons\n";
		cmpLdif += "seeAlso: cn=Audi\n\n";
        
		cmpLdif = "dn: cn=Agnella Security,ou=Peons,dc=idrs,dc=com\n";
		cmpLdif += "sn: Security\n";
		cmpLdif += "ou: Peons\n";
		cmpLdif += "seeAlso: cn=Agnella\n\n";
		
		cmpLdif = "dn: cn=Aggy Zimmermann,ou=Peons,dc=idrs,dc=com\n";
		cmpLdif += "sn: Zimmermann\n";
		cmpLdif += "ou: Peons\n";
		cmpLdif += "seeAlso: cn=Aggy\n\n";

		cmpLdif = "dn: cn=Ailina Dept,ou=Peons,dc=idrs,dc=com\n";
		cmpLdif += "sn: Dept\n";
		cmpLdif += "ou: Peons\n";
		cmpLdif += "seeAlso: cn=Ailina\n\n";
		
		cmpLdif = "dn: cn=Amir Poorman,ou=Peons,dc=idrs,dc=com\n";
		cmpLdif += "sn: Poorman\n";
		cmpLdif += "ou: Peons\n";
		cmpLdif += "seeAlso: cn=Amir";
		
		
        ArrayList rowsExp = new ArrayList();
        HashMap row ;
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
		
        
        
        
        
        
        ArrayList rows = pack.getRows();
        
		
		
		
        assertTrue("Tables Don't Match\n\n" + this.formTable(rowsExp) + "\n\n" + this.formTable(rows),compareTables(fieldsExp,rowsExp,rows));
        
        
    }
    
    
    
    public void testUnpackResultsMultiValExpRows() throws Exception {
		((JndiLdapConnection) con).setConcatAtts(false);
		((JndiLdapConnection) con).setExpandRow(true);
		String sql = "SELECT objectClass,sn,ou,seeAlso FROM  WHERE ou=Peons AND sn=Zimmermann";
				JdbcLdapSelect sel = new JdbcLdapSelect();
				sel.init(con,sql);
				String field;
        
        
				LDAPMessageQueue enum = (LDAPMessageQueue) sel.executeQuery();
        
				System.out.println(enum);
				
				UnpackResults pack = new UnpackResults(con);
				pack.unpackJldap(enum,sel.getRetrieveDN(),sel.getSqlStore().getFrom(),con.getBaseDN(),null);
        
        
				ArrayList fieldsExp = new ArrayList();
        
				fieldsExp.add("objectClass");
				fieldsExp.add("sn");
				fieldsExp.add("ou");
				fieldsExp.add("seeAlso");
        
				/*
				HashMap fieldsExp = new HashMap();
				FieldStore field;
				field = new FieldStore("objectClass",0);
				fieldsExp.put("objectClass",field);
				field = new FieldStore("sn",0);
				fieldsExp.put("sn",field);
				field = new FieldStore("ou",0);
				fieldsExp.put("ou",field);
				field = new FieldStore("seeAlso",0);
				fieldsExp.put("seeAlso",field);
				*/
        
				ArrayList fields = pack.getFieldNames();
        
				Iterator it = fields.iterator();
        
				while (it.hasNext()) {
					field = (String) it.next();
					if (! fieldsExp.contains(field)) {
						fail("Incorrect fields returned : " + field);
					}
				}
        
		
        
				ArrayList rowsExp = new ArrayList();
				HashMap row = new HashMap();
				row.put("objectClass","top");
				row.put("sn","Zimmermann");
				row.put("ou","Peons");
				row.put("seeAlso","cn=Aggy");
				rowsExp.add(row);
		row = new HashMap();
						row.put("objectClass","person");
						row.put("sn","Zimmermann");
						row.put("ou","Peons");
						row.put("seeAlso","cn=Aggy");
						rowsExp.add(row);
		row = new HashMap();
						row.put("objectClass","organizationalPerson");
						row.put("sn","Zimmermann");
						row.put("ou","Peons");
						row.put("seeAlso","cn=Aggy");
						rowsExp.add(row);
						
		ArrayList rows = pack.getRows();
        
				assertTrue("Tables Don't Match\n\n" + this.formTable(rowsExp) + "\n\n" + this.formTable(rows),compareTables(fieldsExp,rowsExp,rows));
    }
    
    /**
     *Tests unpack results with multi-value attributes with concatenating on
     */
    public void testUnpackingResultsMultIValueConcat() throws Exception {
        ((JndiLdapConnection) con).setConcatAtts(true);
        String sql = "SELECT objectClass,sn,ou,seeAlso FROM  WHERE ou=Peons AND cn=A*";
        JdbcLdapSelect sel = new JdbcLdapSelect();
        sel.init(con,sql);
        String field;
        
        
        LDAPMessageQueue enum = (LDAPMessageQueue) sel.executeQuery();
        
        UnpackResults pack = new UnpackResults(con);
        pack.unpackJldap(enum,sel.getRetrieveDN(),sel.getSqlStore().getFrom(),con.getBaseDN(),null);
        
        
        ArrayList fieldsExp = new ArrayList();
        
        fieldsExp.add("objectClass");
        fieldsExp.add("sn");
        fieldsExp.add("ou");
        fieldsExp.add("seeAlso");
        
        /*
        HashMap fieldsExp = new HashMap();
        FieldStore field;
        field = new FieldStore("objectClass",0);
        fieldsExp.put("objectClass",field);
        field = new FieldStore("sn",0);
        fieldsExp.put("sn",field);
        field = new FieldStore("ou",0);
        fieldsExp.put("ou",field);
        field = new FieldStore("seeAlso",0);
        fieldsExp.put("seeAlso",field);
        */
        
        ArrayList fields = pack.getFieldNames();
        
        Iterator it = fields.iterator();
        
        while (it.hasNext()) {
            field = (String) it.next();
            if (! fieldsExp.contains(field)) {
                fail("Incorrect fields returned : " + field);
            }
        }
        
        ArrayList rowsExp = new ArrayList();
        HashMap row;
        
        row = new HashMap();
		row.put("objectClass","[top][person][organizationalPerson]");
		row.put("sn","Dept");
		row.put("ou","Peons");
		row.put("seeAlso","cn=Ailina");
		rowsExp.add(row);        
		row = new HashMap();
		row.put("objectClass","[top][person][organizationalPerson]");
		row.put("sn","Poorman");
		row.put("ou","Peons");
		row.put("seeAlso","cn=Amir");
		rowsExp.add(row);
        
        
        row = new HashMap();
        row.put("objectClass","[top][person][organizationalPerson]");
        row.put("sn","Zimmermann");
        row.put("ou","Peons");
        row.put("seeAlso","cn=Aggy");
        rowsExp.add(row);
        row = new HashMap();
        row.put("objectClass","[top][person][organizationalPerson]");
        row.put("sn","Security");
        row.put("ou","Peons");
        row.put("seeAlso","cn=Agnella");
        rowsExp.add(row);
        

        row = new HashMap();
        row.put("objectClass","[top][person][inetOrgPerson]");
        row.put("sn","Hsiang");
        row.put("ou","Peons");
        row.put("seeAlso","cn=Audi");
        rowsExp.add(row);
        
        ArrayList rows = pack.getRows();
        
        assertTrue("Tables Don't Match\n\n" + this.formTable(rowsExp) + "\n\n" + this.formTable(rows),compareTables(fieldsExp,rowsExp,rows));
        
        
    }
    
    /**
     *Tests unpack results with multi-value attributes with concatenating off
     */
    public void testUnpackingResultsMultIValueNoConcat() throws Exception {
        con.setConcatAtts(false);
        String sql = "SELECT objectClass,sn,ou,seeAlso FROM  WHERE ou=Peons AND cn=A*";
        JdbcLdapSelect sel = new JdbcLdapSelect();
        sel.init(con,sql);
        String field;
        
        
        LDAPMessageQueue enum = (LDAPMessageQueue) sel.executeQuery();
        
        UnpackResults pack = new UnpackResults(con);
        pack.unpackJldap(enum,sel.getRetrieveDN(),sel.getSqlStore().getFrom(),con.getBaseDN(),null);
        
        ArrayList fieldsExp = new ArrayList();
        
        
        fieldsExp.add("objectClass_0");
        fieldsExp.add("objectClass_1");
        fieldsExp.add("objectClass_2");
        fieldsExp.add("sn");
        fieldsExp.add("ou");
        fieldsExp.add("seeAlso");
        
        
        
        
        
        //HashMap fieldsExp = new HashMap();
        
        
        
        /*
        field = new FieldStore("objectClass",3);
        fieldsExp.put("objectClass",field);
        field = new FieldStore("sn",0);
        fieldsExp.put("sn",field);
        field = new FieldStore("ou",0);
        fieldsExp.put("ou",field);
        field = new FieldStore("seeAlso",0);
        fieldsExp.put("seeAlso",field);
        */
        
        ArrayList fields = pack.getFieldNames();
        
        Iterator it = fields.iterator();
        
        while (it.hasNext()) {
            field = (String) it.next();
            if (! fieldsExp.contains(field)) {
                fail("Incorrect fields returned : " + field + "; " + fields);
            }
        }
        
        ArrayList rowsExp = new ArrayList();
        HashMap row;
        
		row = new HashMap();
		row.put("objectClass_0","top");
		row.put("objectClass_1","person");
		row.put("objectClass_2","organizationalPerson");
		row.put("sn","Dept");
		row.put("ou","Peons");
		row.put("seeAlso","cn=Ailina");
		rowsExp.add(row);
		row = new HashMap();
		row.put("objectClass_0","top");
		row.put("objectClass_1","person");
		row.put("objectClass_2","organizationalPerson");
		row.put("sn","Poorman");
		row.put("ou","Peons");
		row.put("seeAlso","cn=Amir");
		rowsExp.add(row);
		row = new HashMap();
				row.put("objectClass_0","top");
				row.put("objectClass_1","person");
				row.put("objectClass_2","organizationalPerson");
				row.put("sn","Zimmermann");
				row.put("ou","Peons");
				row.put("seeAlso","cn=Aggy");
				rowsExp.add(row);
		row = new HashMap();
		row.put("objectClass_0","top");
		row.put("objectClass_1","person");
		row.put("objectClass_2","organizationalPerson");
		row.put("sn","Security");
		row.put("ou","Peons");
		row.put("seeAlso","cn=Agnella");
		rowsExp.add(row);
        
        row = new HashMap();



        row = new HashMap();
        row.put("objectClass_0","top");
        row.put("objectClass_1","person");
        row.put("objectClass_2","inetOrgPerson");
        row.put("sn","Hsiang");
        row.put("ou","Peons");
        row.put("seeAlso","cn=Audi");
        rowsExp.add(row);
        
        ArrayList rows = pack.getRows();
        
        assertTrue("Tables Don't Match\n\n" + this.formTable(rowsExp) + "\n\n" + this.formTable(rows),compareTables(fieldsExp,rowsExp,rows));
        
        
    }
    
    /**
     *Tests unpack results with arguments
     */
    public void testUnpackingResultsParams() throws Exception {
        String sql = "SELECT sn,ou,seeAlso FROM  WHERE ou=? AND cn=?";
        JdbcLdapSelect sel = new JdbcLdapSelect();
        sel.init(con,sql);
        String field;
        
        
        sel.getArgs()[0] = "Peons";
        sel.getArgs()[1] = "A*";
        
        LDAPMessageQueue enum = (LDAPMessageQueue) sel.executeQuery();
        //if (! enum.hasMore()) System.out.println("no results");
        
        UnpackResults pack = new UnpackResults(con);
        pack.unpackJldap(enum,sel.getRetrieveDN(),sel.getSqlStore().getFrom(),con.getBaseDN(),null);
        
        ArrayList fieldsExp = new ArrayList();
        
        
        fieldsExp.add("sn");
        fieldsExp.add("ou");
        fieldsExp.add("seeAlso");
        
        /*
        field = new FieldStore("sn",0);
        fieldsExp.put("sn",field);
        field = new FieldStore("ou",0);
        fieldsExp.put("ou",field);
        field = new FieldStore("seeAlso",0);
        fieldsExp.put("seeAlso",field);
        */
        
        ArrayList fields = pack.getFieldNames();
        
        Iterator it = fields.iterator();
        
        while (it.hasNext()) {
            field = (String) it.next();
            if (! fieldsExp.contains(field)) {
                fail("Incorrect fields returned : " + field);
            }
        }
        
        ArrayList rowsExp = new ArrayList();
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
        
        ArrayList rows = pack.getRows();
        
        assertTrue("Tables Don't Match\n\n" + this.formTable(rowsExp) + "\n\n" + this.formTable(rows),compareTables(fieldsExp,rowsExp,rows));
        
        
    }
    
    /**
     *Compares 2 tables
     *@param f1 List of fields
     *@param t1 Table 1
     *@param t2 Table 2
     *@return True if tables content is equal
     */
    boolean compareTables(ArrayList f1,ArrayList t1, ArrayList t2) {
        int i,j;
        HashMap r1,r2;
        Iterator it;
        String field;
        String fs;
        for (i=0;i<t1.size();i++) {
            r1 = (HashMap) t1.get(i);
            r2 = (HashMap) t2.get(i);
            
            it = f1.iterator();
            
            while (it.hasNext()) {
                fs = (String) it.next();
                
                
                    field = fs;
                    if (! r1.get(field).equals(r2.get(field))) {
                        System.out.println(i);
                        System.out.println(r1.get(field) + " != " + r2.get(field));
                        return false;
                    }
                
            }
            
        }
        
        return true;
    }
    
    /**
     *Creates a string representation of a table
     *@param t Table to create
     */
    String formTable(ArrayList t) {
        StringBuffer buff = new StringBuffer();
        HashMap r1;
        String field;
        int i;
        Iterator it;
        try {
            it = ((HashMap) t.get(0)).keySet().iterator();
        }
        catch (Exception e) {
            return "";
        }
        
        while (it.hasNext()) {
            buff.append(it.next().toString()).append("\t");
        }
        
        buff.append('\n');
        
        for (i=0;i<t.size();i++) {
            r1 = (HashMap) t.get(i);
            
            
            it = r1.keySet().iterator();
            
            while (it.hasNext()) {
                field = (String) it.next();
                buff.append(r1.get(field).toString()).append("\t");
            }
            
            buff.append('\n');
            
        }
        
        return buff.toString();
        
    }
    
    
    
}
