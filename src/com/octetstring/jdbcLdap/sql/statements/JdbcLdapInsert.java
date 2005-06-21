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
 * JdbcLdapInsert.java
 *
 * Created on March 13, 2002, 1:07 PM
 */

package com.octetstring.jdbcLdap.sql.statements;

import com.novell.ldap.LDAPDN;
import com.octetstring.jdbcLdap.jndi.*;
import com.octetstring.jdbcLdap.sql.*;
import java.sql.*;
import java.util.*;

import javax.naming.*;
import javax.naming.directory.*;
import com.octetstring.jdbcLdap.util.*;

/**
 *Stores the information needed to process a SELECT statement
 *@author Marc Boorshtein, OctetString
 */
public class JdbcLdapInsert
	extends com.octetstring.jdbcLdap.sql.statements.JdbcLdapSqlAbs
	implements com.octetstring.jdbcLdap.sql.statements.JdbcLdapSql {

	

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
	
	/** Quote */
		static final char QUOTE = '"';

	/** DN of insertion */
	String dn;

	/** Contains fields to be inserted */
	String[] fields;

	/** dn field array */
	String[] dnFields;

	/** Contains list of fields */
	LinkedList fieldsMap;

	

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

	private Set dontAdd;

	private String defOC;

	public JdbcLdapInsert() {
		super();
		insert = new Insert();
	}

	/** Creates new JdbcLdapSql using a connection and a SQL Statement*/
	public void init(JndiLdapConnection con, String SQL) throws SQLException {
		this.con = con;
		
		LinkedList tmpvals;
		String tmp;
		String tmpSQL = SQL.toLowerCase();
		int begin, end;
		StringTokenizer tok;
		String val;
		int i, j;
		LinkedList ltoks;
		Iterator it;
		this.con = con;

		//retrieve DN
		begin = tmpSQL.indexOf(INSERT_INTO);
		begin += INSERT_INTO.length();
		end = tmpSQL.indexOf(LPAR);

		tmp = SQL.substring(begin, end);
		this.dn = tmp.trim();
		//System.out.println("dn : " + this.dn);
		
		
		
		TableDef def = null;
		if (con != null) {
			def = (TableDef) con.getTableDefs().get(dn);
		}
		if (def == null) {
			ltoks = explodeDN(this.dn);
			parseCtx(ltoks);
		}

		fieldsMap = new LinkedList();

		//retrieve fields to insert
		begin = end + 1;
		end = tmpSQL.indexOf(RPAR, begin);
		tmp = SQL.substring(begin, end);

		tok = new StringTokenizer(tmp, ",", false);
		fields = new String[tok.countTokens()];
		
		HashMap addPattern = null;
		
		if (def != null) {
			addPattern = def.getAddPatterns();
		}
		
		for (i = 0; tok.hasMoreTokens(); i++) {
			fields[i] = tok.nextToken();
			if (addPattern != null) {
				Object o = addPattern.get(fields[i]);
				if (o != null) {
					if (o instanceof HashMap) {
						addPattern = (HashMap) o;
					} else {
						AddPattern pat = (AddPattern) o;
						this.dn = pat.getAddPattern() + "," + def.getBase();
						this.dontAdd = pat.getNotToAdd();
						this.defOC = pat.getDefaultOC();
					}
				}
			}
			//System.out.println("fields : " + fields[i]);
		}

		if (def != null) {
			ltoks = explodeDN(this.dn);
			parseCtx(ltoks);
		}
		
		//retrieves the field values and builds offset

		begin = end + 1;
		begin = tmpSQL.indexOf(LPAR, begin) + 1;
		end = tmpSQL.indexOf(RPAR, begin);

		tmp = SQL.substring(begin, end);
		//tok = new StringTokenizer(tmp,",",false);
		
		ltoks = explodeDN(tmp);
		vals = new String[ltoks.size()];
		offset = new int[ltoks.size()];
		it = ltoks.iterator();
		//System.out.println("begin fields");
		for (i = 0, j = 0; it.hasNext(); i++) {
			vals[i] = (String) it.next();
			
			//temporary
			if (vals[i].charAt(0) == '"' || vals[i].charAt(0) == '\'') {
				vals[i] = vals[i].substring(1,vals[i].length()-1);
			}
			
			//System.out.println(vals[i]);
			
			
			
			if (vals[i].equals(QMARK)) {
				
				offset[j++] = i;
				//System.out.println("j : " + j + " i : " + i + " fields[i] : " + fields[i]);
				
			}
			else if (vals[i].charAt(0) == QUOTE || vals[i].charAt(0) == '\'') {
				vals[i] = vals[i].substring(1,vals[i].length()-2);
				
			}
			 //else {
			fieldsMap.add(new Pair(fields[i],vals[i]));
			 	
				
			//}
		}
		//System.out.println("end fields");

		//store it in the SQL Store
		store = new SqlStore(SQL);
		store.setFields(fields);
		store.setDistinguishedName(dn);
		store.setArgs(vals.length);
		store.setInsertFields(vals);
		store.setFieldOffset(offset);
		store.setDnFields(this.dnFields);
		store.setFieldsMap(fieldsMap);
		store.setDontAdd(this.dontAdd);
		store.setDefaultOC(this.defOC);
	}

	/**
	 * @param ltoks
	 */
	private void parseCtx(LinkedList ltoks) {
		int i;
		Iterator it;
		//dnFields = LDAPDN.explodeDN(this.dn,false);
		//tok = new StringTokenizer(this.dn,COMMA);
		//System.out.println("dnFields length : " +dnFields.length);
		dnFields = new String[ltoks.size()];
		it = ltoks.iterator();
		i=0;
		while (it.hasNext()) {
			dnFields[i] = (String) it.next();
			//if (dnFields[i].indexOf('=') != -1) dnFields[i] = LDAPDN.normalize(dnFields[i]);
			//System.out.println(dnFields[i]);
			i++;
		}
	}

	/** Creates new JdbcLdapSql using a connection, a SQL Statement and a cached SqlStore*/
	public void init(JndiLdapConnection con, String SQL, SqlStore sqlStore)
		throws SQLException {
		this.con = con;
		this.sql = SQL;
		this.store = sqlStore;
		fields = sqlStore.getFields();
		dn = store.getDistinguishedName();
		vals = new String[sqlStore.getArgs()];
		System.arraycopy(sqlStore.getInsertFields(), 0, vals, 0, vals.length);
		offset = sqlStore.getFieldOffset();
		this.dnFields = sqlStore.getDnFields();
		this.fieldsMap = new LinkedList();
		fieldsMap.addAll(store.getFieldsMap());
		this.dontAdd = store.getDontAdd();
		this.defOC = store.getDefOC();
	}

	/** Executes the current statement and returns the results */
	public Object executeQuery() throws SQLException {
		return null;
	}

	public Object executeUpdate() throws SQLException {
		insert.doInsertJldap(this);
		return new Integer(1);
	}

	/**
	 *Sets the value at position
	 *@param pos Position to set
	 *@param val Value to set
	 */
	public void setValue(int pos, String value) throws SQLException {
		
		//if (pos < 0 || pos > fieldsMap.size())
		//	throw new SQLException(Integer.toString(pos) + " out of bounds");
		((Pair) fieldsMap.get(offset[pos])).setValue(value);
		//fieldsMap.put(fields[offset[pos]], value);
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
		
		HashMap track = new HashMap();
		Integer loc;
		int start;
		String val;
		Object[] fields = fieldsMap.toArray();
		
		for (int i = 0; i < this.dnFields.length; i++) {
			if (dnFields[i].indexOf('=') != -1) {
				//System.out.println("in getDistinguishedName : " +dnFields[i]);
				fdn.append(dnFields[i]).append(COMMA);
			} else {
				val = "";
				if (track.containsKey(dnFields[i])) {
					start = ((Integer) track.get(dnFields[i])).intValue() + 1;
				}
				else {
					start = 0;
				}
				
				for (int j=start,m=fields.length;j<m;j++) {
					if (((Pair) fields[j]).getName().equalsIgnoreCase(dnFields[i])) {
						track.put(dnFields[i],new Integer(j));
						val = ((Pair) fields[j]).getValue();
						break;
					}
				}
				
				
				String tmpv = LDAPDN.escapeRDN(dnFields[i] + "=" + val);
				//System.out.println("cleaned : " + tmpv);
				fdn.append(tmpv).append(
					COMMA);
				//TODO This HAS to be fixed
				
			}
		}
		String finalDN = fdn.toString();
		
		return finalDN.toString().substring(0, finalDN.length() - 1);
	}

}
