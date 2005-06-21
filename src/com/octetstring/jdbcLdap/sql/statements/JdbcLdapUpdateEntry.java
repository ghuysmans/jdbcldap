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
package com.octetstring.jdbcLdap.sql.statements;

import java.sql.SQLException;

import com.octetstring.jdbcLdap.jndi.JndiLdapConnection;
import com.octetstring.jdbcLdap.sql.SqlStore;
import com.octetstring.jdbcLdap.util.*;

import java.util.*;

import com.octetstring.jdbcLdap.jndi.*;

/**
 * @author mlb
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class JdbcLdapUpdateEntry
	extends JdbcLdapSqlAbs
	implements JdbcLdapSql {
		
	/** Signifies an update entry */
	public static final String UPDATE_ENTRY="update entry";
		
	/** Delete an attribute */
	public static final String DELETE = "delete";

	/** Add an attribute */
	public static final String ADD = "add";
	
	/** replace an attribute */
	public static final String REPLACE = "replace";
	
	/** determine conditions */
	public static final String WHERE = " where ";
	
	/** determines our cmd */
	public static final String DO = " do ";
	
	/** attributes */
	public static final String SET = " set ";
	
	/** question mark */
	public static final String QMARK = "?";
	
	/** A semicolon */
	public static final String SEMI_COLON = ";";
	
	/** the command */
	String cmd;
	
	
	
	/** list of attribs, either as <code>String</code> object or <code>Pair</code> objects */
	LinkedList cmds;
	
	
	
	
	
	
	
	/** number of arguments */
	int numArgs;
	
	/** SET vals */
	String[] argVals;
	
	/** the sql store */
	SqlStore sqlStore;
	
	/** List of offests */
	ArrayList offset;
	
	
	
	
	LinkedList attribs;
	/* (non-Javadoc)
	 * @see com.octetstring.jdbcLdap.sql.statements.JdbcLdapSql#init(com.octetstring.jdbcLdap.jndi.JndiLdapConnection, java.lang.String)
	 */
	public void init(JndiLdapConnection con, String SQL) throws SQLException {
		//first we will determine the dn
		String attribName, attribValue,attr;
		int paramcount = 0;
		int whereIndex;
		this.cmds = new LinkedList();
		String dn,cmd, attribs;
		ArrayList attribList;
		boolean hasWhere = true;
		int begin,end;
		StringTokenizer toker;
		String lsql = SQL.toLowerCase();
		begin = lsql.indexOf(UPDATE_ENTRY) + UPDATE_ENTRY.length();
		end = lsql.indexOf(DO);
		dn = SQL.substring(begin,end).trim();
		
		if (con.getTableDefs().containsKey(dn)) {
        	dn = ((TableDef) con.getTableDefs().get(dn)).getScopeBase();
        }
		
		
		where = null;
		//Need to parse out the scope
		int semi = dn.indexOf(SEMI_COLON); 
		
		if (semi != -1) {
			String sscope = dn.substring(0,semi);
			dn = dn.substring(semi + 1);
			Integer iscope = (Integer) scopes.get(sscope);
			if (iscope != null) {
				this.scope = iscope.intValue();
			}
			else {
				iscope = (Integer) scopes.get(con.getScope());
				if (iscope == null) {
					throw new SQLException("Invalid search scope : " + con.getScope());
				}
				this.scope = iscope.intValue();
			}
		}
		else {
			Integer iscope = (Integer) scopes.get(con.getScope());
			if (iscope == null) {
				throw new SQLException("Invalid search scope : " + con.getScope());
			}
			this.scope = iscope.intValue();//((Integer) scopes.get(con.getScope())).intValue();
		}
		
		
		
		//System.out.println("dn : " + dn);
		
		//want to retrieve all updates
		begin = end;
		end = lsql.indexOf(WHERE);
		if (end == -1) {
			hasWhere = false;
			end = lsql.length();
		}
		
		whereIndex = end;
		
		String cmdSQL = SQL.substring(begin,end);
		String lCmdSql = cmdSQL.toLowerCase();
		
		boolean ok = true;
		begin = 0;
		offset = new ArrayList();
		int params = 0;
		while (ok) {
		  //determine the command
		  //System.out.println("lCmdSql : " + lCmdSql);
		  //System.out.println("begin : " + begin);
		  begin = lCmdSql.indexOf(DO,begin) + DO.length();
		  
		  end = lCmdSql.indexOf(SET,begin);
		
		  cmd = cmdSQL.substring(begin,end).trim();
		  //System.out.println("cmd : " + cmd);
		  //get the attrib list/attrib-value pairs
		  begin = lCmdSql.indexOf(SET,end) + SET.length();
		  
		  
		  end = lCmdSql.indexOf(DO,begin);
		  if (end == -1) {
		  	ok = false;
		  	end = lCmdSql.length();
		  }
		  
		
		  attribs = cmdSQL.substring(begin,end);
		  //System.out.println("attribs : " + attribs );
		  
		  if (ok) {
		  	begin  = end;
		  }
		  
//		  retrieve attribs
			
			attribList = new ArrayList(5);
			this.cmds.add(new UpdateSet(cmd,attribList));
			if (cmd.equalsIgnoreCase(ADD) || cmd.equalsIgnoreCase(REPLACE)) {
		
				
				LinkedList attribsExploded = explodeDN(attribs);
				//toker = new StringTokenizer(attribs,",",false);
				Iterator itoker = attribsExploded.iterator();
				while (itoker.hasNext()) {
					
					attr = (String) itoker.next();
					
					attribName = attr.substring(0,attr.indexOf("="));
					attribValue = attr.substring(attr.indexOf("=") + 1);
			
					if (attribValue.charAt(0) == '"' || attribValue.charAt(0) == '\'') {
						attribValue = attribValue.substring(1,attribValue.length()-1);
					}
					
					if (attribValue.trim().equals("?")) {
						offset.add(new Integer(params++));
					}
			
					attribList.add(new Pair(attribName.trim(),attribValue));
					
				}
			}
			else {
		
				//this.attribs = new LinkedList();
	
				toker = new StringTokenizer(attribs,",",false);
				while (toker.hasMoreTokens()) {
					attr = toker.nextToken();
					if (attr.indexOf('=') != -1) {
						attribName = attr.substring(0,attr.indexOf("="));
						attribValue = attr.substring(attr.indexOf("=") + 1);
						attribList.add(new Pair(attribName.trim(),attribValue.trim()));
					} else {
						attribList.add(attr.trim());
					}
				}
			}	
		}
		
		
				
		//if there is a where, get it
		if (hasWhere) {
			begin = whereIndex + WHERE.length();
			where = con.nativeSQL(sqlArgsToLdap(SQL.substring(begin).trim()));
		}
		
		//System.out.println("where : " + where);
		
		sqlStore = new SqlStore(SQL);
		sqlStore.setDistinguishedName(dn);
		
		
		
		this.numArgs = offset.size();
		
		this.argVals = new String[numArgs];
		this.from = dn;
		sqlStore.setScope(scope);
		
		sqlStore.setCommand(this.cmd);
		sqlStore.setArgs(this.numArgs);
		sqlStore.setAttribs(this.attribs);
		
		sqlStore.setCmds(this.cmds);
		sqlStore.setOffsetList(offset);
		if (where == null || where.trim().length() == 0) {
					where = "(objectClass=*)";
				}
			//System.out.println("where : " + where);
		sqlStore.setWhere(where);
		this.con = con;

	}

	/* (non-Javadoc)
	 * @see com.octetstring.jdbcLdap.sql.statements.JdbcLdapSql#init(com.octetstring.jdbcLdap.jndi.JndiLdapConnection, java.lang.String, com.octetstring.jdbcLdap.sql.SqlStore)
	 */
	public void init(JndiLdapConnection con, String SQL, SqlStore sqlStore)
		throws SQLException {
		this.con = con;
		this.sqlStore = sqlStore;
		
		this.numArgs = sqlStore.getArgs();
		this.attribs = sqlStore.getAttribs();
		
		this.cmd = sqlStore.getCommand();
		this.cmds = sqlStore.getCmds();
		this.argVals = new String[numArgs];
		this.offset = sqlStore.getOffsetList();
		this.argVals = new String[offset.size()];
		this.where = sqlStore.getWhere();
		this.scope = sqlStore.getScope();
	}

	/* (non-Javadoc)
	 * @see com.octetstring.jdbcLdap.sql.statements.JdbcLdapSql#executeQuery()
	 */
	public Object executeQuery() throws SQLException {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see com.octetstring.jdbcLdap.sql.statements.JdbcLdapSql#executeUpdate()
	 */
	public Object executeUpdate() throws SQLException {
		UpdateEntry ue = new UpdateEntry();
		
		return new Integer(ue.doUpdateEntryJldap(this));
	}

	/* (non-Javadoc)
	 * @see com.octetstring.jdbcLdap.sql.statements.JdbcLdapSql#setValue(int, java.lang.String)
	 */
	public void setValue(int pos, String value) throws SQLException {
		if (pos < argVals.length) { 
			this.argVals[pos] = value;
		}
		else {
			this.args[pos - argVals.length] = value;
		}

	}

	/* (non-Javadoc)
	 * @see com.octetstring.jdbcLdap.sql.statements.JdbcLdapSql#getSqlStore()
	 */
	public SqlStore getSqlStore() {
		return this.sqlStore;
	}

	/* (non-Javadoc)
	 * @see com.octetstring.jdbcLdap.sql.statements.JdbcLdapSql#getRetrieveDN()
	 */
	public boolean getRetrieveDN() {
		
		return false;
	}

	/* (non-Javadoc)
	 * @see com.octetstring.jdbcLdap.sql.statements.JdbcLdapSql#isUpdate()
	 */
	public boolean isUpdate() {
		
		return true;
	}

	/**
	 * @return
	 */
	public String[] getArgVals() {
		return argVals;
	}

	/**
	 * @return
	 */
	public LinkedList getAttribs() {
		return attribs;
	}

	/**
	 * @return
	 */
	public String getCmd() {
		return cmd;
	}

	

	/**
	 * @return
	 */
	public int getNumArgs() {
		return numArgs;
	}

	
	/**
	 * @return
	 */
	public LinkedList getCmds() {
		return cmds;
	}

	/**
	 * @param list
	 */
	public void setCmds(LinkedList list) {
		cmds = list;
	}

}
