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
 * JdbcLdapDelete.java
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
public class JdbcLdapDelete
	extends com.octetstring.jdbcLdap.sql.statements.JdbcLdapSqlAbs
	implements com.octetstring.jdbcLdap.sql.statements.JdbcLdapSql {
	/** Determines end of FROM */
	static final String FROM = " from ";

	/** Determines there's a WHERE clause */
	static final String WHERE = " where ";

	/** Determines end of FROM */
	static final String DELETE = "delete ";

	/** Store SQL information */
	SqlStore store;

	/** the SQL */
	String SQL;

	/** Is Simple Delete? */
	boolean simple;

	/** performs deletion */
	Delete del;

	public JdbcLdapDelete() {
		super();
		del = new Delete();

	}

	/** Creates new JdbcLdapSql using a connection and a SQL Statement*/
	public void init(JndiLdapConnection con, String SQL) throws SQLException {
		//System.out.println("sql " + SQL);
		this.con = con;
		int begin, end;
		String tmpSQL = SQL.toLowerCase();
		this.SQL = SQL;
		Integer iscope;
		begin = tmpSQL.indexOf(FROM) + FROM.length();
		String sscope;
		LinkedList ltok;
		Iterator it;
		
		//is this simply delete one context or spcific entries
		if (tmpSQL.indexOf(WHERE) == -1) {
			//simple context
			from = SQL.substring(begin);
			from = from.trim();
			simple = true;
		} else {
			//retrieve base context
			from = SQL.substring(begin, tmpSQL.indexOf(WHERE));
			from = from.trim();
			//determine search scope
			begin = tmpSQL.indexOf(DELETE) + DELETE.length() - 1;
			end = tmpSQL.indexOf(FROM);

			sscope = SQL.substring(begin, end).trim();

			if (sscope.trim().length() == 0) {
				iscope = (Integer) scopes.get(con.getSearchScope());
			} else {
				iscope = (Integer) scopes.get(sscope.trim());
			}

			if (iscope == null) {
				throw new SQLException("Unrecognized Search Scope");
			}
			scope = iscope.intValue();

			begin = tmpSQL.indexOf(WHERE) + WHERE.length();
			where = SQL.substring(begin).trim();
			
			
			
			where = con.nativeSQL(sqlArgsToLdap(  where ));

			simple = false;
		}

		store = new SqlStore(SQL);
		//System.out.println("from : " + from);
		store.setFrom(from);
		store.setSimple(simple);
		store.setScope(this.scope);
		store.setWhere(where);

	}

	/** Creates new JdbcLdapSql using a connection, a SQL Statement and a cached SqlStore*/
	public void init(JndiLdapConnection con, String SQL, SqlStore sqlStore)
		throws SQLException {

		this.con = con;
		this.SQL = SQL;
		this.store = store;
		this.from = store.getFrom();
		this.simple = store.getSimple();
		args = new Object[store.getArgs()];
		this.where = store.getWhere();
	}

	/** Executes the current statement and returns the results */
	public Object executeQuery() throws SQLException {
		return null;
	}
	public Object executeUpdate() throws SQLException {
		return new Integer(del.doDeleteJldap(this));
	}

	/**
	 *Sets the value at position
	 *@param pos Position to set
	 *@param val Value to set
	 */
	public void setValue(int pos, String value) throws SQLException {
		this.args[pos] = value;
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
	 *Retrieves Connection
	 */
	public JndiLdapConnection getCon() {
		return con;
	}

	/**
	 *Retrieves WHERE statement
	 */
	public String getWhere() {
		return where;
	}

	/**
	 * Determines if statement is update or select
	 * @return true if is an update
	 */
	public boolean isUpdate() {
		return true;
	}

}
