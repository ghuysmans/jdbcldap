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
 * JdbcLdapUpdate.java
 *
 * Created on May 23, 2002, 1:24 PM
 */

package com.octetstring.jdbcLdap.sql.statements;
import com.octetstring.jdbcLdap.jndi.*;
import com.octetstring.jdbcLdap.sql.*;
import com.octetstring.jdbcLdap.util.TableDef;

import java.sql.*;
import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;
/**
 *Processes an SQL UPDATE statement
 *@author Marc Boorshtein, OctetString
 */
public class JdbcLdapUpdate extends com.octetstring.jdbcLdap.sql.statements.JdbcLdapSqlAbs implements com.octetstring.jdbcLdap.sql.statements.JdbcLdapSql {
    /** A double quote character */
    static final char QUOTE = '"';
    
    /** The default search filter when no filter (or WHERE clause) is specified */
    static final String DEFAULT_SEARCH_FILTER = "(objectClass=*)";
    
    /** performs update */
    Update update;
    
    /** Question Mark */
    static final String QMARK = "?";
    
    /** Equals */
    static final String EQUALS = "=";
    
    /** UPDATE constant */
    static final String UPDATE = "update";
    
    /** SET constant */
    static final String SET = " set ";
    
    /** WHERE constant */
    static final String WHERE = " where ";
    
    /** Comma constant */
    static final String COMMA = ",";
    
    /** Store SQL information */
    SqlStore store;
    
    /** stores fields */
    String[] fields;
    
    /** Vals */
    String[] vals;
    
    /** offset array */
    int[] offset;
    
    
    /** border value between SET and WHERE */
    int border;
    
    /** Creates a new instance of JdbcLdapUpdate */
    public JdbcLdapUpdate() {
        super();
        update = new Update();
    }
    
    /** Executes the current statement and returns the results  */
    public Object executeQuery() throws SQLException {
        throw new SQLException("UPDATE can not execute a query");
    }
    
    public Object executeUpdate() throws SQLException {
        return new Integer(update.doUpdateJldap(this));
    }
    
    
    
    /**
     * Retrieves if the DN is being returned
     */
    public boolean getRetrieveDN() {
        return false;
    }
    
    /**
     * Used to retrieve the SqlStore for caching
     * @return The statments SqlStore object
     */
    public SqlStore getSqlStore() {
        return store;
    }
    
    /** Creates new JdbcLdapSql using a connection and a SQL Statement */
    public void init(JndiLdapConnection con, String SQL) throws SQLException {
        this.con = con;
        String tmpSQL = SQL.toLowerCase();
        int begin, end,i,j;
        String sscope;
        Integer iscope;
        
        StringTokenizer toker;
        String toekn;
        String field, val;
        String set;
        String token;
        
        //determine scope a base context
        begin = tmpSQL.indexOf(UPDATE) + UPDATE.length();
        end = tmpSQL.indexOf(SET);
        from = SQL.substring(begin,end).trim();
        
        if (con.getTableDefs().containsKey(from)) {
        	from = ((TableDef) con.getTableDefs().get(from)).getScopeBase();
        }
        
        if (from.indexOf(";") != -1) {
            sscope = from.substring(0,from.indexOf(";")).trim(); 
            //System.out.println("sscope : " + sscope);       
            iscope = (Integer) scopes.get(sscope);
            from = from.substring(from.indexOf(";")+1);
            //System.out.println("from : " + from);
        }
        else {
            iscope = (Integer) scopes.get(con.getSearchScope());
        }
		
        if (iscope == null) {
            throw new SQLException("Unrecognized Search Scope");
        }
        
        scope = iscope.intValue();
        //System.out.println("scope : " + scope);
        
        //break up the SET portion
        begin = tmpSQL.indexOf(SET) + SET.length();
        
        end = tmpSQL.indexOf(WHERE);
        if (end == -1) {
            end = tmpSQL.length();
        }
        
        set = SQL.substring(begin,end);
        
        LinkedList itok = explodeDN(set);
        Iterator it;
        
        
        
        
        
        //toker = new StringTokenizer(set,COMMA);
        
        fields = new String[itok.size()];
        vals = new String[itok.size()];
        offset = new int[itok.size()];
        it = itok.iterator();
        for (i=0, j=0;it.hasNext();i++) {
            token = ((String) it.next()).trim();
            
            
            
            fields[i] = token.substring(0,token.indexOf(EQUALS));
            vals[i] = token.substring(token.indexOf(EQUALS) + 1);
            
			//temporary
			if (vals[i].charAt(0) == '"' || vals[i].charAt(0) == '\'') {
				vals[i] = vals[i].substring(1,vals[i].length()-1);
			}
            
            
            if (vals[i].equals(QMARK)) {
				offset[j++] = i;
            }
            else if (vals[i].charAt(0) == QUOTE) {
				
				vals[i] = vals[i].substring(1,vals[i].lastIndexOf(QUOTE));
			}
        }
        
        border = j;
        
        //determine if there is a "where" clause
        if (end == tmpSQL.length()) {
            //no where clause, set where to default
            where = DEFAULT_SEARCH_FILTER;
            border = -1;
        }
        else {
            begin = end + WHERE.length();
            this.where = con.nativeSQL(sqlArgsToLdap(SQL.substring(begin).trim()));
        }
        
        store = new SqlStore(SQL);
        store.setFields(fields);
        store.setDistinguishedName(from);
        store.setArgs(args != null ? this.args.length : 0);
        store.setInsertFields(vals);
        store.setFieldOffset(offset);
        
        store.setWhere(where);
        store.setBorder(border);
        //System.out.println("scope : " + scope);
        store.setScope(scope);
        
    }
    
    /** Creates new JdbcLdapSql using a connection, a SQL Statement and a cached SqlStore */
    public void init(JndiLdapConnection con, String SQL, SqlStore sqlStore) throws SQLException {
        this.con = con;
        store = sqlStore;
        fields = store.getFields();
        from = store.getDistinguishedName();
        offset = store.getFieldOffset();
        where = store.getWhere();
        border = store.getBorder();
        args = new Object[store.getArgs()];
        vals = new String[fields.length];
        scope = store.getScope();
	System.arraycopy(sqlStore.getInsertFields(),0,vals,0,vals.length);
    }
    
    /**
     * Sets the value at position
     * @param pos Position to set
     * @param val Value to set
     */
    public void setValue(int pos, String value) throws SQLException {
        //is the argument for SET or WHERE
        if (pos < border || border==-1) {
            if (pos < 0) throw new SQLException(Integer.toString(pos) + " out of bounds");
            vals[offset[pos]] = value;
        }
        else {
            args[pos - border] = value;
        }
    }
    
    /**
     *Retrieves values to be inserted
     *@return array of values to insert
     */
     public String[] getVals() {
	     return vals;
     }
     
     /**
     *Retrieves Connection
     */
    public JndiLdapConnection getCon() {
	    return con;
    }
    
    /**
     * Determines if statement is update or select
     * @return true if is an update
     */
    public boolean isUpdate() {
        return true;
    }
    
}
