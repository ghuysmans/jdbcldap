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
 * JdbcLdapSelect.java
 *
 * Created on March 13, 2002, 1:07 PM
 */

package com.octetstring.jdbcLdap.sql.statements;

import com.octetstring.jdbcLdap.jndi.*;
import com.octetstring.jdbcLdap.sql.*;
import com.octetstring.jdbcLdap.util.TableDef;

import java.io.StreamTokenizer;
import java.sql.*;
import java.util.*;

import javax.naming.*;
import javax.naming.directory.*;

/**
 *Stores the information needed to process a SELECT statement
 *@author Marc Boorshtein, OctetString
 */
public class JdbcLdapSelect extends com.octetstring.jdbcLdap.sql.statements.JdbcLdapSqlAbs implements com.octetstring.jdbcLdap.sql.statements.JdbcLdapSql {
    
    
    /** The default search filter when no filter (or WHERE clause) is specified */
    static final String DEFAULT_SEARCH_FILTER = "(objectClass=*)";
    
    
    /** Search Scope Determinate */
    public final static String FROM_SCOPE = ";";
    
    /** SELECT constanct */
    static final String SELECT = "select";
    
    /** Select length */
    static final int SELECT_SIZE = 6;
    
    /** From constant */
    static final String FROM = "from";
    
    /** From length */
    static final int FROM_SIZE = 4;
    
    /** Where constant */
    static final String WHERE = "where";
    
    /** Where length */
    static final int WHERE_SIZE = 5;
    
    /** Wildcard Character */
    static final String WILDCARD = "*";
    
    /** DN filed name */
    static final String DN_FIELD = "DN";
    
    /**Search Bot */
    RetrieveResults search;
    
    
    
    
    
    /** SQL Statement being used */
    String sql;
    
    
    
    
    
    
    
    /** Fields to return **/
    String[] fields;
    
    /** Maximum Rows to return */
    int maxRows;
    
    
    
    
    
    /** Stores the SQL's parts */
    SqlStore sqlStore;
    
    /** Determines if the DN was retrieved */
    boolean retreiveDN;
    
    String[] sortBy;


	private HashMap fieldMap;
	
	private HashMap revFieldMap;
    
    /** Creates new JdbcLdapSelect */
    public JdbcLdapSelect() {
        super();
        
        search = new RetrieveResults();
        
        maxRows = -1;
        queryTimeOut = -1;
    }

    /** Executes the current statement and returns the results  */
    public Object executeQuery() throws SQLException {
        return search.searchJldap(this);
    }
    
     /** Creates new JdbcLdapSql using a connection, a SQL Statement and a cached SqlStore*/
    public void init(JndiLdapConnection con, String SQL,SqlStore sqlStore) throws SQLException {
        this.con = con;
        this.sql = sql;
        this.sqlStore = sqlStore;
        this.where = sqlStore.getWhere();
        this.from = sqlStore.getFrom();
        this.fields = sqlStore.getFields();
        this.retreiveDN = sqlStore.getDN();
        this.args = new Object[sqlStore.getArgs()];
        this.scope = sqlStore.getScope();
        this.sortBy = sqlStore.getOrderby();
        this.fieldMap = sqlStore.getFieldMap();
        this.revFieldMap = sqlStore.getRevFieldMap();
    }
    
    /** Creates new JdbcLdapSql using a connection and a SQL Statement */
    public void init(JndiLdapConnection con, String SQL) throws SQLException {
        this.con = con;
        this.retreiveDN = false;
        String fields;
        SQL = SQL.trim();
        String sql = SQL.toLowerCase();
        int begin,end;
        Integer scope;
        boolean whereFound = false;
        
        
        
        //First, break the statement up into it's parts
        
        //Determine the attributes to return
        begin = sql.indexOf(SELECT) + SELECT_SIZE;
        end = sql.indexOf(FROM);
        
        fields = SQL.substring(begin,end);
        fields = fields.trim();
        
        if (! fields.equalsIgnoreCase(WILDCARD)) {
            StringTokenizer toker = new StringTokenizer(fields,",");
            this.fields = new String[toker.countTokens()];
            int i = 0;
            while (toker.hasMoreTokens()) {
                this.fields[i] = toker.nextToken().trim();
                
                String fieldLcase = this.fields[i].toLowerCase();
                int beginas = fieldLcase.indexOf(" as ");
                if (beginas != -1) {
                	String fieldName = this.fields[i].substring(0,beginas).trim();
                	String asName = this.fields[i].substring(beginas + 4).trim();
                	this.fields[i] = fieldName;
                	if (this.fieldMap == null) {
                		this.fieldMap = new HashMap();
                		this.revFieldMap = new HashMap();
                	}
                	this.fieldMap.put(asName,fieldName);
                	this.revFieldMap.put(fieldName,asName);
                }
                
                if (this.fields[i].equalsIgnoreCase(DN_FIELD)) this.retreiveDN = true;
                i++;
            }
        }
        else {
            this.fields = new String[0];
            this.retreiveDN = true;
        }
        
        //Figure out the base context
        begin = sql.indexOf(FROM,end) + FROM_SIZE;
        end = sql.indexOf(WHERE,begin);
        
        //If there is no where clause
        if (end != -1) {
            this.from = SQL.substring(begin,end).trim();
            whereFound = true;
        }
        else {
        	end = sql.indexOf(" order by ",begin);
            if (end != -1) {
            	this.from = SQL.substring(begin,end).trim();
            	procOrderBy(SQL, end);
            } else {
            	this.from = SQL.substring(begin).trim();
            }
        	
        }
        
        //determine if we are working with a table
        if (con.getTableDefs().containsKey(from.trim())) {
        	//this is a table defenition
        	TableDef table = (TableDef) con.getTableDefs().get(from.trim());
        	from = table.getScopeBase();
        }
        
        //determine if the search scope is specified, other wise retrieve it from the connection
        end = from.indexOf(FROM_SCOPE);
        if (end == -1) {
            scope = (Integer) scopes.get(con.getSearchScope());
        }
        else {
            scope = (Integer) scopes.get(from.substring(0,end).trim());
            this.from = this.from.substring(end+1).trim();
        }
        
        if (scope == null) {
            throw new SQLException("Scope not recognized");
        }
        else {
            this.scope = scope.intValue();
        }
        
        //Where portion
        
        
        if (whereFound) {
            begin = sql.indexOf(WHERE,end) + WHERE_SIZE;
            end = sql.indexOf(" order by ",begin);
            if (end != -1) {
            	this.where = con.nativeSQL(sqlArgsToLdap(SQL.substring(begin,end).trim()),this.fieldMap);
            	procOrderBy(SQL, end);
            } else {
            	this.where = con.nativeSQL(sqlArgsToLdap(SQL.substring(begin).trim()),this.fieldMap);
            }
        }
        else {
            this.where = DEFAULT_SEARCH_FILTER;
        }      
        
        System.out.println("Sort by : " + this.sortBy );
        
        sqlStore = new SqlStore(SQL);
        sqlStore.setOrderby(this.sortBy);
        sqlStore.setWhere(where);
        sqlStore.setFrom(from);
        sqlStore.setFields(this.fields);
        sqlStore.setDN(this.retreiveDN);
        sqlStore.setScope(this.scope);
        sqlStore.setFieldMap(this.fieldMap);
        sqlStore.setRevFieldMap(this.revFieldMap);
        sqlStore.setArgs(args != null ? this.args.length : 0);
    }
    
    
    
    
    /**
	 * @param SQL
	 * @param end
	 */
	private void procOrderBy(String SQL, int end) {
		String order = SQL.substring(end + 10).trim();
		StringTokenizer toker = new StringTokenizer(order,",");
		this.sortBy = new String[toker.countTokens()];
		for (int i=0,m=sortBy.length;i<m;i++) {
			this.sortBy[i] = toker.nextToken();
		}
	}

	/**
     *Returns the LDAP Search String
     */
    public String getSearchString() {
        return this.where;
    }
    
    /**
     *Returns the attributes returned by this search
     */
    public String[] getSearchAttributes() {
        return this.fields;
    }
    
    
    
    
    
    /**
     *Sets the maximum records retrieved
     *@param rec Number of records
     */
    public void setMaxRecords(int rec) {
        this.maxRows = rec;
    }
    
    /**
     *Retrieves the maximum number of records
     *@return Number of records
     */
    public int getMaxRecords() {
        return this.maxRows;
    }
    
    
    
    
    
    /**
     *Retrieves the Object[] containing the arguments
     */
    public Object[] getArgs() {
        return this.args;
    }
    
    public Object executeUpdate() throws SQLException {
        return null;
    }
    
    /**
     * Sets the value at position
     * @param pos Position to set
     * @param val Value to set
     */
    public void setValue(int pos, String value) throws SQLException {
	if (pos < 0 || pos > args.length) throw new SQLException(Integer.toString(pos) + " out of bounds");
        this.args[pos] = value;
    }    
    
    /**
     *Used to retrieve the SqlStore for caching
     *@return The statments SqlStore object
     */
    public SqlStore getSqlStore() {
        return this.sqlStore;
    }
    
    /**
     * Retrieves if the DN is being returned
     */
    public boolean getRetrieveDN() {
        return this.retreiveDN;
    }    
    
    /**
     * Determines if statement is update or select
     * @return true if is an update
     */
    public boolean isUpdate() {
        return false;
    }    
    
}
