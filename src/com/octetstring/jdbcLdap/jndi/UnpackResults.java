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
 * UnpackResults.java
 *
 * Created on March 14, 2002, 10:06 AM
 */

package com.octetstring.jdbcLdap.jndi;

import javax.naming.*;
import javax.naming.directory.*;
import com.octetstring.jdbcLdap.jndi.JndiLdapConnection;
import java.util.*;
import java.sql.*;

/**
 *Takes a JNDI Naming Enumeration and places it into a LinkedList of
 *HasMap's for processing
 *@author Marc Boorshtein, OctetString
 */
public class UnpackResults {
    /** DN attribute name */
    static final String DN_ATT = "DN";
    
    /** The Connection to the LDAP server */
    JndiLdapConnection con;
    
    /** List of Field Names */
    HashMap names;
    
    /** List of rows */
    LinkedList rows;
    
    
    
    
    /** Creates new UnpackResults */
    public UnpackResults(JndiLdapConnection con) {
        this.con = con;
        names = new HashMap();
        rows = new LinkedList();
    }
    
    /** Returns the field names of the result */
    public LinkedList getFieldNames() {
        LinkedList fields = new LinkedList();
        Iterator it = names.keySet().iterator();
        FieldStore f;
        int i;
        StringBuffer buf = new StringBuffer();
        while (it.hasNext()) {
            f = (FieldStore) names.get(it.next());
            if (f.numVals > 0) {
                for (i=0;i<f.numVals;i++) {
                    buf.setLength(0);
                    fields.add(buf.append(f.name).append('_').append(i).toString());
                }
            }
            else {
                fields.add(f.name);
            }
        }
        
        
        return fields;
    }
    
    /** Returns the types for the query */
    public int[] getFieldTypes() {
        LinkedList fields = new LinkedList();
        Iterator it = names.keySet().iterator();
        FieldStore f;
        int i;
        StringBuffer buf = new StringBuffer();
        int count=0;
        while (it.hasNext()) {
            f = (FieldStore) names.get(it.next());
            if (f.numVals > 0) {
                for (i=0;i<f.numVals;i++) {
                    buf.setLength(0);
                    fields.add(new Integer(f.getType()));
                    count++;
                }
            }
            else {
                fields.add(new Integer(f.getType()));
                count++;
            }
        }
        
        int[] types = new int[count];
        for (i=0;i<count;i++) {
            types[i] = ((Integer) fields.get(i)).intValue();
        }
        
        return types;
    }
    
    /** Returns the results of the search */
    public LinkedList getRows() {
        return rows;
    }
    
    public void unpack(NamingEnumeration results,boolean dn,String fromContext, String baseContext) throws SQLException {
        try {
            SearchResult res;
            Attributes atts;
            NamingEnumeration enumAtts;
            Enumeration vals;
            Attribute att;
            String attrid;
            String val;
	    //String dn;
            FieldStore field;
            StringBuffer buff = new StringBuffer();
	    String base;
            names.clear();
            rows.clear();
            int currNumVals;
            HashMap row;
            
	    buff.setLength(0);
	    if (fromContext != null && fromContext.length() != 0) buff.append(',').append(fromContext);
	    if (baseContext != null && baseContext.length() != 0) buff.append(',').append(baseContext);
	    
	    base = buff.toString();
	    
            while (results.hasMore()) {
                res = (SearchResult) results.next();
                row = new HashMap();
                atts = (Attributes) res.getAttributes();
                enumAtts = atts.getAll();
               
                if (dn) {
                    field = (FieldStore) names.get(DN_ATT);
                    if (field == null) {
                        field = new FieldStore();
                        field.name = DN_ATT;
                        names.put(field.name,field);
                    }
                    buff.setLength(0);
		    buff.append(res.getName()).append(base);
		//    System.out.println("Unpack: dn="+buff);
		    
                    row.put(DN_ATT,buff.toString());
                    
                }
                
                while (enumAtts.hasMore()) {
                    att = (Attribute) enumAtts.next();
                    attrid = att.getID();
      //              System.out.println("Unpack: att="+att+", attrid="+attrid);
                    
                    
                    field = (FieldStore) names.get(attrid);
                    if (field == null) {
                        field = new FieldStore();
                        field.name = attrid;
                        names.put(field.name,field);
                    }
                    
                    if (att.size() > 1) {
                        
                        if (con.getConcatAtts()) {
                            buff.setLength(0);
                            field.numVals = 0;
                            
                            for (vals = att.getAll();vals.hasMoreElements();) {
                                val = vals.nextElement().toString();
                                field.determineType(val);
                                buff.append('[').append(val).append(']');
                            }
                            
                            row.put(field.name,buff.toString());
                        }
                        else {
                            currNumVals = 0;
                            
                            for (vals = att.getAll();vals.hasMoreElements();) {
                                buff.setLength(0);
                                val = vals.nextElement().toString();
                                field.determineType(val);
                                row.put(buff.append(field.name).append('_').append(currNumVals).toString(),val);
                                currNumVals++;
                            }
                            
                            field.numVals = (currNumVals > field.numVals) ? currNumVals : field.numVals;
                        }
                    }
                    else {
                        val = att.get(0).toString();
                        field.determineType(val);
                        row.put(field.name,val);
                    }
                    
                }
                
                enumAtts.close();
                
                
                
                rows.add(row);
                
                
            }
            results.close();
        }
        catch (NamingException e) {
            throw new SQLNamingException(e);
        }
    }
    
}


