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
 * JdbcLdapStatement.java
 *
 * Created on March 13, 2002, 12:33 PM
 */

package com.octetstring.jdbcLdap.sql;

import java.sql.*;
import java.util.*;

import com.novell.ldap.LDAPMessageQueue;
import com.novell.ldap.LDAPSearchResults;
import com.octetstring.jdbcLdap.jndi.*;
import com.octetstring.jdbcLdap.sql.statements.*;
import com.octetstring.jdbcLdap.sql.*;
import javax.naming.*;

/**
 * Implements the Statement interface
 *@author Marc Boorshtein, OctetString
 */
public class JdbcLdapStatement implements java.sql.Statement {

	

	/** Represents a SELECT statement */
	static final String SELECT = "select";

	/** Represents the length of select */
	static final int SELECT_LEN = 6;

	/** Represets an INSERT statement */
	static final String INSERT = "insert";

	/** Represents the length of an insert */
	static final int INSERT_LEN = 6;

	/** Represets an DELETE statement */
	static final String DELETE = "delete";

	/** Represents the length of an insert */
	static final int DELETE_LEN = 6;

	/** Represets an UPDATE statement */
	static final String UPDATE = "update";

	/** Represents the length of an insert */
	static final int UPDATE_LEN = 6;
	
	/** Represents an UPDATE ENTRY statement */
	static final String UPDATE_ENTRY = "update entry";
	
	/** Represents the length of the UPDATE ENTRY statement */
	static final int UPDATE_ENTRY_LEN = 12;

	/** Stores batch of statements */
	LinkedList statements;

	/** Used to unpack results */
	UnpackResults res;

	/** Current ResultSet */
	LdapResultSet rs;

	/** Current Statement */
	JdbcLdapSql stmt;

	/** THe LDAP connection */
	JndiLdapConnection con;

	/** Max Results */
	int maxResults;

	/** Maximum Timeout time in MilliSeconds */
	int timeOut;

	/** array of returned results */
	Object[] results;

	/** Used to initialize a statement
	 *@param sql SQL to parse
	 */
	void loadSQL(String sql) throws SQLException {
		String sqll = sql.toLowerCase().trim();
		SqlStore sqlStore = con.getCache(sql);
		//System.out.println(" sqll : " + sqll.substring(0, UPDATE_ENTRY_LEN) );
		if (sqll.substring(0, SELECT_LEN).equals(SELECT)) {
			this.stmt = new JdbcLdapSelect();
		} else if (sqll.substring(0, INSERT_LEN).equals(INSERT)) {
			this.stmt = new JdbcLdapInsert();
		} else if (sqll.substring(0, UPDATE_ENTRY_LEN).equals(UPDATE_ENTRY)) {
			this.stmt = new JdbcLdapUpdateEntry();
		}
		 else if (sqll.substring(0, DELETE_LEN).equals(DELETE)) {
			this.stmt = new JdbcLdapDelete();
		} else if (sqll.substring(0, UPDATE_LEN).equals(UPDATE)) {
			this.stmt = new JdbcLdapUpdate();
		} else {
			throw new SQLException("Opperation not suported");
		}

		if (sqlStore == null) {
			stmt.init(con, sql);

			if (con.cacheStatements())
				con.cacheStatement(sql, stmt.getSqlStore());
		} else {
			stmt.init(con, sql, sqlStore);
		}

	}

	

	/** Creates new JdbcLdapStatement */
	public JdbcLdapStatement(JndiLdapConnection con) {
		this.con = con;
		this.maxResults = -1;
		this.timeOut = -1;
		this.res = new UnpackResults(con);
		this.statements = new LinkedList();
	}

	public void addBatch(java.lang.String str) throws java.sql.SQLException {
		loadSQL(str);
		if (!stmt.isUpdate())
			throw new SQLException(str + " is not an update");
		statements.add(stmt);
	}

	public void cancel() throws java.sql.SQLException {
		throw new SQLException("not implemented");
	}

	public void clearBatch() throws java.sql.SQLException {
		statements.clear();
	}

	public void clearWarnings() throws java.sql.SQLException {
	}

	public void close() throws java.sql.SQLException {
	}

	public boolean execute(java.lang.String str) throws java.sql.SQLException {
		executeQuery(str);
		return true;
	}

	public boolean execute(java.lang.String str, int param)
		throws java.sql.SQLException {
		executeQuery(str);
		return true;
	}

	public boolean execute(java.lang.String str, int[] values)
		throws java.sql.SQLException {
		executeQuery(str);
		return true;
	}

	public boolean execute(java.lang.String str, java.lang.String[] str1)
		throws java.sql.SQLException {
		executeQuery(str);
		return true;
	}

	public int[] executeBatch() throws java.sql.SQLException {
		int[] batch = new int[statements.size()];

		Iterator it = statements.iterator();
		int i = 0;
		while (it.hasNext()) {
			stmt = (JdbcLdapSql) it.next();
			batch[i] = ((Integer) stmt.executeUpdate()).intValue();
			i++;
		}

		return batch;
	}

	public java.sql.ResultSet executeQuery(java.lang.String str)
		throws java.sql.SQLException {
		this.loadSQL(str);
		if (this.con.isDSML()) {
			res.unpackJldap(
					(LDAPSearchResults) stmt.executeQuery(),
					stmt.getRetrieveDN(),
					stmt.getSqlStore().getFrom(),
					con.getBaseDN());
		} else {
			res.unpackJldap(
				(LDAPMessageQueue) stmt.executeQuery(),
				stmt.getRetrieveDN(),
				stmt.getSqlStore().getFrom(),
				con.getBaseDN());
		}


		
		this.rs =
			new LdapResultSet(
				con,
				this,
				res,
				((JdbcLdapSelect) stmt).getBaseContext());
		return rs;

	}

	public int executeUpdate(java.lang.String str)
		throws java.sql.SQLException {
		loadSQL(str);

		return ((Integer) stmt.executeUpdate()).intValue();

	}

	public int executeUpdate(java.lang.String str, int param)
		throws java.sql.SQLException {
		return executeUpdate(str);

	}

	public int executeUpdate(java.lang.String str, int[] values)
		throws java.sql.SQLException {
		return executeUpdate(str);
	}

	public int executeUpdate(java.lang.String str, java.lang.String[] str1)
		throws java.sql.SQLException {
		return executeUpdate(str);
	}

	public java.sql.Connection getConnection() throws java.sql.SQLException {
		return this.con;
	}

	public int getFetchDirection() throws java.sql.SQLException {
		return -1;
	}

	public int getFetchSize() throws java.sql.SQLException {
		return -1;
	}

	public java.sql.ResultSet getGeneratedKeys() throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public int getMaxFieldSize() throws java.sql.SQLException {
		return -1;
	}

	public int getMaxRows() throws java.sql.SQLException {
		return this.maxResults;
	}

	public boolean getMoreResults() throws java.sql.SQLException {
		return true;
	}

	public boolean getMoreResults(int param) throws java.sql.SQLException {
		return true;
	}

	public int getQueryTimeout() throws java.sql.SQLException {
		return this.timeOut;
	}

	public java.sql.ResultSet getResultSet() throws java.sql.SQLException {
		return this.rs;
	}

	public int getResultSetConcurrency() throws java.sql.SQLException {
		return -1;
	}

	public int getResultSetHoldability() throws java.sql.SQLException {
		return -1;
	}

	public int getResultSetType() throws java.sql.SQLException {
		return -1;
	}

	public int getUpdateCount() throws java.sql.SQLException {
		return -1;
	}

	public java.sql.SQLWarning getWarnings() throws java.sql.SQLException {
		return null;
	}

	public void setCursorName(java.lang.String str)
		throws java.sql.SQLException {
	}

	public void setEscapeProcessing(boolean param)
		throws java.sql.SQLException {
	}

	public void setFetchDirection(int param) throws java.sql.SQLException {
	}

	public void setFetchSize(int param) throws java.sql.SQLException {
		this.maxResults = param;
	}

	public void setMaxFieldSize(int param) throws java.sql.SQLException {
	}

	public void setMaxRows(int param) throws java.sql.SQLException {
		this.maxResults = param;
	}

	public void setQueryTimeout(int param) throws java.sql.SQLException {
		this.timeOut = param;
	}

}
