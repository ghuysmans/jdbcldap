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
 * JdbcLdapSql.java
 *
 * Created on March 13, 2002, 12:56 PM
 */

package com.octetstring.jdbcLdap.sql.statements;

import com.novell.ldap.LDAPConnection;
import com.octetstring.jdbcLdap.jndi.*;
import com.octetstring.jdbcLdap.sql.*;
import java.sql.*;
import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;

/**
 *This interface is used to wrap the internals of a statement
 *@author Marc Boorshtein, OctetString
 */
public abstract class JdbcLdapSqlAbs implements JdbcLdapSql {
    /** Object scope value */
    static final int OBJECT_SCOPE = SearchControls.OBJECT_SCOPE;
    
    /** One level scope value */
    static final int ONELEVEL_SCOPE = SearchControls.ONELEVEL_SCOPE;
    
    /** Sub tree scope value */
    static final int SUBTREE_SCOPE = SearchControls.SUBTREE_SCOPE;
    
    /** Stores the string reps of the various serach scopes */
    HashMap scopes;
    
    /** Where clasuse, in ldap form */
    String where;
    
    /** Array of arguments */
    Object[] args;
    
    /** Connection to LDAP server */
    JndiLdapConnection con;
    
    /** From clause, aka the base context */
    String from;
    
    /** Query Timeout */
    int queryTimeOut;
    
    /** Search Scope */
    int scope;
    
    /** Creates new JdbcLdapSql using a connection and a SQL Statement*/
    public abstract void init(JndiLdapConnection con, String SQL) throws SQLException;
    
    /** Creates new JdbcLdapSql using a connection, a SQL Statement and a cached SqlStore*/
    public  abstract void init(JndiLdapConnection con, String SQL,SqlStore sqlStore) throws SQLException;
    
    /** Executes the current statement and returns the results */
    public  abstract Object executeQuery() throws SQLException;
    public  abstract Object executeUpdate() throws SQLException;
    
    /**
     *Sets the value at position
     *@param pos Position to set
     *@param val Value to set
     */
    public abstract void setValue(int pos,String value) throws SQLException;
    
    /**
     *Used to retrieve the SqlStore for caching
     *@return The statments SqlStore object
     */
    public  abstract  SqlStore getSqlStore();
    
    /**
     *Retrieves if the DN is being returned
     */
    public abstract  boolean getRetrieveDN();
    
    public JdbcLdapSqlAbs() {
        scopes = new HashMap();
        
        scopes.put(JndiLdapConnection.OBJECT_SCOPE,new Integer(OBJECT_SCOPE));
        scopes.put(JndiLdapConnection.ONELEVEL_SCOPE,new Integer(ONELEVEL_SCOPE));
        scopes.put(JndiLdapConnection.SUBTREE_SCOPE,new Integer(SUBTREE_SCOPE));
    }
    
    /**
     *Replaces all '?' instances with a {i} for every ith ?
     *@param where The where clause
     *@return New where clause
     */
    String sqlArgsToLdap(String where) {
        
        StringBuffer buff = new StringBuffer(where);
        StringBuffer arg = new StringBuffer();
        int curr,num=0;
        
        curr = where.indexOf("?");
        
        if (curr == -1) {
            
            args = new Object[0];
            return where;
        }
        
        while (curr != -1) {
            if (buff.charAt(curr - 1) != '\\') {
                arg.setLength(0);
                buff.replace(curr,curr+1, arg.append("{").append(num).append("}").toString());
                num++;
            }
            else {   
                buff.deleteCharAt(curr-1);
                curr++;
            }
            curr = buff.toString().indexOf('?',curr+1);
        }        
        args = new Object[num];
        return buff.toString();
    }
    
    /**
     *Returns the serach filter with all arguments added
     */
    public String getFilterWithParams() {
        if (this.where == null) return null;
        
        if (args == null) args = new Object[0];
        int i,m,begin,end;
        m = args.length;
        StringBuffer buf = new StringBuffer(this.where);
        StringBuffer search = new StringBuffer();
        
        for (i = 0;i < m ;i++) {
            search.setLength(0);
            search.append("{").append(i).append("}");
            begin = buf.toString().indexOf(search.toString());
            
            if (begin != -1) {
                
                end = begin + search.length();
                buf.replace(begin,end,args[i].toString());
                
            }
            
            
        }
        
        return buf.toString();
    }
    
    /**
     *Retrieves the context used to talk to the LDAP server
     */
    
    public DirContext getContext() {
        return con.getContext();
    }
    
    public LDAPConnection getConnection() {
    	return con.getConnection();
    }
    
    /**
     *Returns the base context for the search
     */
    public String getBaseContext() {
        return this.from;
    }
    
    /**
     *Sets the query time out in milliseconds
     *@param time Time out period
     */
    public void setTimeOut(int time) {
        this.queryTimeOut = time;
    }
    
    /**
     *Returns query time out in milliseconds
     *@return Time out period
     */
    public int getTimeOut() {
        return this.queryTimeOut;
    }
    
    /**
     *Retrieves the search scope
     */
    public int getSearchScope() {
        return scope;
    }
    
	protected LinkedList explodeDN(String dn) {
				LinkedList rdnComponents = new LinkedList();

				String dnstr = dn.toString();

				boolean inquotes = false;
				boolean escaped = false;

				int currentStart = 0;
				char current = 0;
				for (int i = 0; i < dnstr.length(); i++) {
					current = dnstr.charAt(i);
					if (current == '\\') {
						escaped = !escaped;
					} else if ((current == '\"' || current == '\'') && !escaped) {
						inquotes = !inquotes;
					} else if (
						(current == ',' || current == ';') && !escaped && !inquotes) {
						String currdn = dnstr.substring(currentStart, i).trim();
						if (currdn.endsWith("\\") && dnstr.charAt(i-1) == ' ') {
							currdn = currdn + " ";
						}
						if (currdn.length() > 0) {
							rdnComponents.add(currdn);
							//System.err.println("Component: '" + currdn + "'");
						}
						currentStart = i + 1;
					} else {
						escaped = false;
					}
				}

				if (dnstr.length() > currentStart) {
					String currdn =
						dnstr.substring(currentStart, dnstr.length()).trim();
					if (currdn.length() > 0) {
						rdnComponents.add(currdn);
					}
				}
				return rdnComponents;
			}
    
    public String getConnectionBase() {
    	return con.getBaseContext();
    }
    
    
    public JndiLdapConnection getJDBCConnection () {
    		return this.con;
    }
}
