/* **************************************************************************
 *
 * Copyright (C) 2002-2004 Octet String, Inc. All Rights Reserved.
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
 * JndiLdapConnection.java
 *
 * Created on March 10, 2002, 3:22 PM
 */

package com.octetstring.jdbcLdap.jndi;

import java.sql.*;
import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.*;
import java.util.*;
import com.octetstring.jdbcLdap.sql.*;
import com.octetstring.jdbcLdap.sql.statements.JdbcLdapSql;

/**
 * Wraps a Jndi Connection to an LDAP server
 *@author Marc Boorshtein, OctetString
 */
public class JndiLdapConnection implements java.sql.Connection {
	public final static String LDAP_COMMA = "\\,";
		public final static String LDAP_EQUALS = "\\=";
		public final static String LDAP_PLUS = "\\+";
		public final static String LDAP_LESS = "\\<";
		public final static String LDAP_GREATER = "\\>";
		public final static String LDAP_SEMI_COLON = "\\;";
   
    /** Object scope */
    public static final String OBJECT_SCOPE = "objectScope";
    
    /** One level scope */
    public static final String ONELEVEL_SCOPE = "oneLevelScope";
    
    /** Subtree scope */
    public static final String SUBTREE_SCOPE = "subTreeScope";
    
    /** Determines if Multiple value attributes are concattinated into one cell surrounded by [], ie
     *  [val1][val2][val3].  Must be true or false */
    public static final String CONCAT_ATTS = "CONCAT_ATTS";
    
    
    /** Determines if multiple value attributes will cause rows to be expanded for each value of the attribute */
    public static final String EXP_ROWS = "EXP_ROWS";
    
    /** Determines what type of authentication will be used, none, simple, sasl */
    public static final String AUTHENTICATION_TYPE = javax.naming.Context.SECURITY_AUTHENTICATION;
    
    /** Used to specify no authentication */
    public static final String NO_AUTHENTICATION = "none";
    
    /** Used for simple authentication */
    public static final String SIMPLE_AUTHENTICATION = "simple";
    
    /** OPTIONAL - Determines search scope.  If not specified, then it must be supplied with each statement passed in */
    public static final String SEARCH_SCOPE = "SEARCH_SCOPE";
    
    /** OPTIONAL - States if statements should be cached.  Default is false */
    public static final String CACHE_STATEMENTS = "CACHE_STATEMENTS";
    
    /** The JNDI factory for connecting */
    public static final String JNDI_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
    
    /** The JNDI DSML factory*/
    public static final String JNDI_DSML_FACTORY = "com.sun.jndi.dsmlv2.soap.DsmlSoapCtxFactory";
    
    /** OPTIONAL - States if transaction calls should be ignored, default to false */
    public static final String IGNORE_TRANSACTIONS = "IGNORE_TRANSACTIONS";
  
    
    
    /** user property */
    static final String USER  = "user";
    
    /** password property */
    static final String PASSWORD = "password";
    
    /** secure connection property */
    static final String SECURE = "secure";
    
    /** number of characters to eliminate 'jdbc:' from the url */
    static final int ELIM_JDBC = 5;
    
    /** number of characters to eliminate 'jdbc:dsml: from the url */
    static final int ELIM_JDBC_DSML = 12;
    
    /** Contains all cached statements */
    HashMap statements;
    
    /** LDAP connection */
    DirContext con;
    
    /**Stores properties for initialization */
    Hashtable env;
    
    /** Determinies if statements are cached */
    boolean cacheStatements;
    
    /** Determines if rows are expanded */
    boolean expandRow;
    
    /** The default search scope */
    String scope;
    
    /** The Base DN for the conneciton */
    String baseDN;
    
    /** The converter from SQL to LDAP */
    SqlToLdap sql2ldap;
    
    /** Determines if attributes are concattintated */
    boolean concatAtts;
    
    /** temporary string buffer */
    StringBuffer tmpBuff;
    
    /** determines if the connection is dsmlv2 */
    boolean isDsml;
    
    /** determines if a transaction should be ignored */
    boolean ignoreTransactions;
    
    /*
     *Public methods not part of java.sql.Connection
     */
    
    
	
    
    
    /**
    Caches a statement
    @param statement The statement to cache
    */
    
    public void cacheStatement(String sql,SqlStore stmt) {
	if (this.cacheStatements()) {
		statements.put(sql,stmt);
	}
    }
    
    /**
    Returns a cached statement
    */
    public SqlStore getCache(String sql) {
	    return (SqlStore) statements.get(sql);
    }
    
    /**
     *Alows for setting of CONCAT_ATTS
     *@param concat true if concatination is to occurr
     */
    public void setConcatAtts(boolean concat) {
        this.concatAtts = concat;
    }
    
    /**
     *Retrieves if methods are to be concatenated
     *@return True if concatination is to take place
     */
    public boolean getConcatAtts() {
        return this.concatAtts;
    }
    
    /**
     *Returns the context used to connect
     */
    public DirContext getContext() {
        return this.con;
    }
    
    /**
     *Returns true if statements are cached (Set by the <b>CACHE_STATEMENTS</b> property)
     *@return true if statements are cached
     */
    public boolean cacheStatements() {
        return this.cacheStatements;
    }
    
    /**
     *Returns the search scope if set globaly for this connection
     *@return Search Scope (null if not set)
     */
    public String getSearchScope() {
        return this.scope;
    }
    
    public String getBaseDN() {
	    return this.baseDN;
    }
    
    
    /**
     *Initializes the Connection with a set of properties
     *@param url URL of the server in the form ldap://server:port/base dn
     *@param props Properties correspinding to Conneciton Constants
     */
    public JndiLdapConnection(String url, Properties props) throws SQLException {
        statements = new HashMap();
        sql2ldap = new SqlToLdap();
        env = new Hashtable();
        this.concatAtts = false;
        boolean authFound = false;
        this.tmpBuff = new StringBuffer();
        this.ignoreTransactions = false;
        isDsml = url.startsWith(JdbcLdapDriver.DSML_URL_ID);
        
        Enumeration en = props.propertyNames();
//        System.out.println("JndiLdapConnection.<init>: url="+url);
        baseDN = url.substring(url.lastIndexOf('/') + 1);
//       	System.out.println("JndiLdapConnection.<init>: baseDN="+baseDN); 
        String prop;
        String user=null, pass=null ;
        env.put(Context.INITIAL_CONTEXT_FACTORY,isDsml ? JNDI_DSML_FACTORY : JNDI_FACTORY);
		//env.put(Context.INITIAL_CONTEXT_FACTORY,JNDI_DSML_FACTORY);
        if (isDsml) {
        	env.put(Context.PROVIDER_URL,url.substring(ELIM_JDBC_DSML));
        }
        else {
        	env.put(Context.PROVIDER_URL,url.substring(ELIM_JDBC));
        }
        
        while (en.hasMoreElements()) {
            prop = (String) en.nextElement();
            
            if (prop.equalsIgnoreCase(USER)) {
                user = props.getProperty(prop);
            }
            else if (prop.equalsIgnoreCase(PASSWORD)) {
                pass = props.getProperty(prop);
            }
            else if (prop.equalsIgnoreCase(CACHE_STATEMENTS))     {
                cacheStatements = props.getProperty(prop).equalsIgnoreCase("true");
            }
            else if (prop.equalsIgnoreCase(EXP_ROWS)) {
            	this.expandRow = props.getProperty(prop).equalsIgnoreCase("true");
            }
            else if (prop.equalsIgnoreCase(SEARCH_SCOPE)) {
                scope = props.getProperty(prop);
            }
            else if (prop.equalsIgnoreCase(AUTHENTICATION_TYPE)) {
                
                env.put(Context.SECURITY_AUTHENTICATION,props.getProperty(prop));
            }
            else if (prop.equalsIgnoreCase(CONCAT_ATTS))     {
                this.concatAtts = props.getProperty(prop).equalsIgnoreCase("true");
            }
            else if (prop.equalsIgnoreCase(SECURE)) {
            	if (props.getProperty(prop).equalsIgnoreCase("true")) {
        			env.put(Context.SECURITY_PROTOCOL, "ssl");
            	}
            } else if (prop.equalsIgnoreCase(IGNORE_TRANSACTIONS)) {
            		this.ignoreTransactions = props.getProperty(prop).equalsIgnoreCase("true");
            }
            else {
                env.put(prop,props.getProperty(prop));
            }
            
        }
        
        if (env.get(Context.SECURITY_AUTHENTICATION) == null || !((String) env.get(Context.SECURITY_AUTHENTICATION)).equalsIgnoreCase(NO_AUTHENTICATION)) {
            
            if (user != null && pass != null) {
                env.put(Context.SECURITY_PRINCIPAL,user);
                env.put(Context.SECURITY_CREDENTIALS,pass);
            
                if (env.get(Context.SECURITY_AUTHENTICATION) == null) {
                    env.put(Context.SECURITY_AUTHENTICATION,SIMPLE_AUTHENTICATION);
                }
            }
            else {
                env.put(Context.SECURITY_AUTHENTICATION,NO_AUTHENTICATION);
            }
        }
             
       
        
        try {
            con = new InitialDirContext(env);
            //System.out.println("JndiLdapConnection.<init>: called InitialDirContext");
           
            
        }
        catch (NamingException e) {
         throw new SQLNamingException(e);
        }
        
        
        
        
        if (this.isClosed()) {
            throw new SQLException("Connection Failed");
        }
        
    }

    public void setTransactionIsolation(int param) throws java.sql.SQLException {
        	if (! this.ignoreTransactions) {
        		throw new SQLException("LDAP Does Not Support Transactions");
        	}
    }
    
    public boolean getAutoCommit() throws java.sql.SQLException {
        return false;
    }
    
    public void close() throws java.sql.SQLException {
        try {
            con.close();
        }
        catch (NamingException e) {
            throw new SQLNamingException(e);
        }
    }
    
    public void commit() throws java.sql.SQLException {
    		if (! this.ignoreTransactions) {
    			throw new SQLException("LDAP Does Not Support Transactions");
    		}
    }
    
    public boolean isClosed() throws java.sql.SQLException {
        try {
            return con == null || con.lookup("") == null;
        }
        catch (NamingException e) {
            //e.printStackTrace(System.out);
            return false;
        }
        
        
        
    }
    
	/**
			 * escaping the following characters per rfc2253 -- "," / "=" / "+" / "<" /  ">" / "#" / ";"
			 * @param a variable passed in
			 * @return a cleaned LDAP parameter
			 */
		public String ldapClean(String val) {
			
			char c;
			tmpBuff.setLength(0);
			for (int i=0,m= val.length(); i<m; i++) {
				c = val.charAt(i);
				
				switch (c) {
					case ',':  tmpBuff.append("\\,"); break;
					case '=':	tmpBuff.append("\\="); break;
					case '+':  tmpBuff.append("\\+"); break;
					case '<':  tmpBuff.append("\\<"); break;
					case '>':  tmpBuff.append("\\>"); break;
					case '#':  tmpBuff.append("\\#"); break;
					case ';': tmpBuff.append("\\;"); break; /*this.tmpBuff.setLength(0);
								 tmpBuff .append('"').append(val).append('"');
								System.out.println("tmpbuf : " + tmpBuff.toString());
								 return tmpBuff.toString();*/
					default : tmpBuff.append(c);
				}
			}
			
			
			
			return tmpBuff.toString();
		}
    
    public void setCatalog(java.lang.String str) throws java.sql.SQLException {
    }
    
    public java.sql.Savepoint setSavepoint() throws java.sql.SQLException {
        return null;
    }
    
    public boolean isReadOnly() throws java.sql.SQLException {
        return false;
    }
    
    public void setHoldability(int param) throws java.sql.SQLException {
        
    }
    
    public void rollback() throws java.sql.SQLException {
        throw new SQLException("LDAP Does Not Support Transactions");
    }
    
    public java.lang.String getCatalog() throws java.sql.SQLException {
        return baseDN;
    }
    
    public java.sql.PreparedStatement prepareStatement(java.lang.String str, int param) throws java.sql.SQLException {
        return new JdbcLdapPreparedStatement(str,this);
    }
    
    public java.sql.PreparedStatement prepareStatement(java.lang.String str, int param, int param2, int param3) throws java.sql.SQLException {
        return new JdbcLdapPreparedStatement(str,this);
    }
    
    public java.sql.PreparedStatement prepareStatement(java.lang.String str, int param, int param2) throws java.sql.SQLException {
        return new JdbcLdapPreparedStatement(str,this);
    }
    
    public void setAutoCommit(boolean param) throws java.sql.SQLException {
    		if (! this.ignoreTransactions) {
    			throw new SQLException("LDAP Does Not Support Transactions");
    		}
    }
    
    public java.sql.CallableStatement prepareCall(java.lang.String str) throws java.sql.SQLException {
        return null;
    }
    
    public java.sql.PreparedStatement prepareStatement(java.lang.String str, int[] values) throws java.sql.SQLException {
        return new JdbcLdapPreparedStatement(str,this);
    }
    
    public void setTypeMap(java.util.Map map) throws java.sql.SQLException {
    }
    
    public int getHoldability() throws java.sql.SQLException {
        return -1;
    }
    
    public java.sql.Savepoint setSavepoint(java.lang.String str) throws java.sql.SQLException {
        return null;
    }
    
    public java.sql.Statement createStatement(int param, int param1, int param2) throws java.sql.SQLException {
        return new JdbcLdapStatement(this);
    }
    
    public java.sql.Statement createStatement(int param, int param1) throws java.sql.SQLException {
        return new JdbcLdapStatement(this);
    }
    
    public java.sql.Statement createStatement() throws java.sql.SQLException {
        return new JdbcLdapStatement(this);
    }
    
    /**
     *Takes an SQL evaluation condition and translates it to LDAP form, ie:<br>
     *Field1=1 AND Field2=2 OR Field3=3 <br>
     *Becomes...<br>
     *|(&((Field1=1)(Field2=2))(Field3=3))
     */
    public java.lang.String nativeSQL(java.lang.String str) throws java.sql.SQLException {
        return sql2ldap.convertToLdap(str);
    }
    
    public java.sql.CallableStatement prepareCall(java.lang.String str, int param, int param2) throws java.sql.SQLException {
        throw new SQLException("LDAP Does Not Support Stored Procedures");
    }
    
    
    public java.sql.CallableStatement prepareCall(java.lang.String str, int param, int param2, int param3) throws java.sql.SQLException {
        throw new SQLException("LDAP Does Not Support Stored Procedures");
    }
    
    public java.sql.SQLWarning getWarnings() throws java.sql.SQLException {
        return null;
    }
    
    public java.sql.PreparedStatement prepareStatement(java.lang.String str) throws java.sql.SQLException {
        return new JdbcLdapPreparedStatement(str,this);
    }
    
    public void setReadOnly(boolean param) throws java.sql.SQLException {
    }
    
    public java.util.Map getTypeMap() throws java.sql.SQLException {
        return null;
    }
    
    public void releaseSavepoint(java.sql.Savepoint savepoint) throws java.sql.SQLException {
        
    }
    
    public java.sql.PreparedStatement prepareStatement(java.lang.String str, java.lang.String[] str1) throws java.sql.SQLException {
        return new JdbcLdapPreparedStatement(str,this);
    }
    
    public int getTransactionIsolation() throws java.sql.SQLException {
        return -1;
    }
    
    public java.sql.DatabaseMetaData getMetaData() throws java.sql.SQLException {
        return null;
    }
    
    public void clearWarnings() throws java.sql.SQLException {
    }
    
    public void rollback(java.sql.Savepoint savepoint) throws java.sql.SQLException {
    		if (! this.ignoreTransactions) {
    			throw new SQLException("LDAP Does Not Support Transactions");
    		}
    }
    
	/**
	 * @return trie if expanding rows
	 */
	public boolean isExpandRow() {
		return expandRow;
	}

	/**
	 * @param b sets if expanding rows
	 */
	public void setExpandRow(boolean b) {
		expandRow = b;
	}

	/**
	 * @return
	 */
	public String getScope() {
		return scope;
	}

	/**
	 * @param string
	 */
	public void setScope(String string) {
		scope = string;
	}

}
