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
 * JdbcLdapInsert.java
 *
 * Created on March 13, 2002, 1:07 PM
 */

package com.octetstring.jdbcLdap.sql.statements;

import com.octetstring.jdbcLdap.jndi.*;
import com.octetstring.jdbcLdap.sql.*;
import java.sql.*;
import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;

/**
 *Stores the information needed to process a SELECT statement
 *@author Marc Boorshtein, OctetString
 */
public class JdbcLdapInsert extends com.octetstring.jdbcLdap.sql.statements.JdbcLdapSqlAbs implements com.octetstring.jdbcLdap.sql.statements.JdbcLdapSql {
    
    /** Insertion Identifier */
    static final String INSERT_INTO = "insert into"; 
    
    /** Left Parenthasys */
    static final char LPAR = '(';
    
    /** Right Parenthasys */
    static final char RPAR = ')';
    
    /** Question Mark */
    static final String QMARK = "?";
    
    /** Comma */
    static final String COMMA = ",";
    
    /** Equals */
    static final String EQUALS = "=";
    
    /** DN of insertion */
    String dn;
    
    /** Contains fields to be inserted */
    String[] fields;
    
    /** dn field array */
    String[] dnFields;
    
    /** Contains map of fields */
    HashMap fieldsMap;
    
    /** Connection to LDAP server */
    JndiLdapConnection con;
    
    /** SQL Statement being used */
    String sql;
    
    /** Stores the SQL's parts */
    SqlStore store;
    
    /** Contains values of insertion */
    String[] vals;
    
    /** Contains offset information */
    int[] offset;
    
    /** Insertion modules */
    Insert insert;
    
    public JdbcLdapInsert() {
	    super();
	    insert = new Insert();
    }
    
   /** Creates new JdbcLdapSql using a connection and a SQL Statement*/
    public void init(JndiLdapConnection con, String SQL) throws SQLException {
	    
	    String tmp;
	    String tmpSQL = SQL.toLowerCase();
	    int begin,end;
	    StringTokenizer tok;
	    String val;
	    int i,j;
	    
	    
	    this.con = con;
	    
	    //retrieve DN
	    begin = tmpSQL.indexOf(INSERT_INTO);
	    begin += INSERT_INTO.length();
	    end = tmpSQL.indexOf(LPAR);
	    
	    tmp = SQL.substring(begin,end);
	    this.dn = tmp.trim();
            tok = new StringTokenizer(this.dn,COMMA);
            this.dnFields = new String[tok.countTokens()];
            for (i=0;tok.hasMoreTokens();i++) {
                dnFields[i] = tok.nextToken();
            }
            
            fieldsMap = new HashMap();
	    
	    //retrieve fields to insert
	    begin = end + 1;
	    end = tmpSQL.indexOf(RPAR,begin);
	    tmp = SQL.substring(begin,end);
	    
	    tok = new StringTokenizer(tmp,",",false);
	    fields = new String[tok.countTokens()];
	    for (i=0;tok.hasMoreTokens();i++) {
		    fields[i] = tok.nextToken();
	    }
	    
	    
	    //retrieves the field values and builds offset
	    
	    begin = end + 1;
	    begin = tmpSQL.indexOf(LPAR,begin) + 1;
	    end = tmpSQL.indexOf(RPAR,begin);
	    
	    tmp = SQL.substring(begin,end);
	    tok = new StringTokenizer(tmp,",",false);
	    
	    vals = new String[tok.countTokens()];
	    offset = new int[tok.countTokens()];
	    for (i=0,j=0;tok.hasMoreTokens();i++) {
		    vals[i] = tok.nextToken();
		    if (vals[i].equals(QMARK)) {
			offset[j++] = i;
		    }
                    else {
                        fieldsMap.put(fields[i],vals[i]);
                    }
	    }
	    
	    //store it in the SQL Store
	    store = new SqlStore(SQL);
	    store.setFields(fields);
	    store.setDistinguishedName(dn);
	    store.setArgs(vals.length);
	    store.setInsertFields(vals);
	    store.setFieldOffset(offset);
            store.setDnFields(this.dnFields);
            store.setFieldsMap(fieldsMap);
    }
    
    /** Creates new JdbcLdapSql using a connection, a SQL Statement and a cached SqlStore*/
    public void init(JndiLdapConnection con, String SQL,SqlStore sqlStore) throws SQLException {
	    this.con = con;
	    this.sql = SQL;
	    this.store = sqlStore;
	    fields = sqlStore.getFields();
	    dn = store.getDistinguishedName();
	    vals = new String[sqlStore.getArgs()];
	    System.arraycopy(sqlStore.getInsertFields(),0,vals,0,vals.length);
	    offset = sqlStore.getFieldOffset();
            this.dnFields = sqlStore.getDnFields();
            this.fieldsMap = new HashMap();
            fieldsMap.putAll(store.getFieldsMap());
	    
    }
    
    /** Executes the current statement and returns the results */
    public Object executeQuery() throws SQLException {
	    return null;
    }
    
    public Object executeUpdate() throws SQLException {
	    insert.doInsert(this);
	    return new Integer(1);
    }
    
    /**
     *Sets the value at position
     *@param pos Position to set
     *@param val Value to set
     */
    public void setValue(int pos,String value) throws SQLException {
	if (pos < 0 || pos > vals.length) throw new SQLException(Integer.toString(pos) + " out of bounds");
	fieldsMap.put(fields[offset[pos]], value);
    }

    /**
     *Used to retrieve the SqlStore for caching
     *@return The statments SqlStore object
     */
    public SqlStore getSqlStore() {
	    return store;
    }
    
    /**
     *Retrieves if the DN is being returned
     */
    public boolean getRetrieveDN() {
	    return false;
    }
    
    /**
     *Retrieves the context used to talk to the LDAP server
     */
    
    public DirContext getContext() {
        return con.getContext();
    }
    
    /**
     *Retrieves values to be inserted
     *@return array of values to insert
     */
     public String[] getVals() {
	     return vals;
     }
     
     /**
     * Determines if statement is update or select
     * @return true if is an update
     */
    public boolean isUpdate() {
        return true;
    }    
    
    /**
     *Retrieves the complete dn
     *@return a nuilt dn based on passed in parameters
     */
    public String getDistinguishedName() {
        StringBuffer fdn = new StringBuffer();
        
        for (int i=0;i<this.dnFields.length;i++) {
            if (dnFields[i].indexOf('=') != -1) {
                fdn.append(dnFields[i]).append(COMMA);
            }
            else {
                fdn.append(dnFields[i]).append('=').append(fieldsMap.get(dnFields[i])).append(COMMA);
            }
        }
        String finalDN = fdn.toString();
        return finalDN.toString().substring(0,finalDN.length()-1);
    }
    
   
     
}
