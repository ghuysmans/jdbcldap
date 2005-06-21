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

package com.octetstring.jdbcLdap.sql;

import java.util.*;
/**
 *This  class stores the basic information shared by SQL Statements
 *@author Marc Boorshtein, OctetString
 */
public class SqlStore {

    /** SQL Statement being used */
    String sql;
    
    /** Where clasuse, in ldap form */
    String where;
    
    /** From clause, aka the base context */
    String from;
    
    /** Determines if the statement is simple */
    boolean simple;
    
    /** Fields to return **/
    String[] fields;
    
    /** Determines if DN is in the field list */
    boolean getDN;
    
    /** Determines the search scope */
    int scope;
    
    /** Determines howmany arguments there are */
    int numArgs;
    
    /** Contains data to update/insert */
    String[] insertFields;
    
    /** Distinguished Name */
    String dn;
    
    /** Contains field offset information */
    int fieldOffset[];
    
    /** argumnet in border of SET and WHERE */
    int border;
    
    /** List of field, value pairs */
    LinkedList fieldsMap;
    
    /** Contains fields of a DN */
    String[] dnfields;
    
    /** Stores a modify command */
    String command;
    
    /** stores the arguments to be handled */
    LinkedList attribs;
    
    
    
    
    
	/** stores modify commands */
	LinkedList cmds;
   
	/** Stores a list of offsets */
	ArrayList offsetList;
    
    String[] orderby;
    
    HashMap fieldMap;
    
    HashMap revFieldMap;

	private Set dontAdd;

	private String defOC;
	
	
	/**
	 * @return Returns the orderby.
	 */
	public String[] getOrderby() {
		return orderby;
	}
	/**
	 * @param orderby The orderby to set.
	 */
	public void setOrderby(String[] orderby) {
		this.orderby = orderby;
	}
    /**
     *Creates a new SqlStore
     *@param sql SQL statement being used
     */
    public SqlStore(String sql) {
        this.sql = sql;
    }
    
    
    /**
     *Sets the WHERE clause of a SQL statement
     *@param where WHERE clasuse
     */
    public void setWhere(String where) {
        this.where = where;
    }
    
    /**
     *Retrieves the WHERE clause of the SQL statement
     *@return WHERE clause of SQL
     */
     public String getWhere() {
        return this.where;
     }
     
     /**
     *Sets the FROM clause of a SQL statement
     *@param from FROM clasuse
     */
    public void setFrom(String from) {
        this.from = from;
    }
    
    /**
     *Retrieves the FROM clause of the SQL statement
     *@return FROM clause of SQL
     */
     public String getFrom() {
        return this.from;
     }
     
     /**
     *Sets the Fields used in the SQL statement
     *@param fields Fields retrieved
     */
    public void setFields(String[] fields) {
        this.fields = fields;
    }
    
    /**
     *Retrieves the Fields usede by the SQL statement
     *@return Fields of the SQL
     */
     public String[] getFields() {
        return this.fields;
     }
     
     /**
     *Sets if the DN is being retrieved
     *@param dn true if the DN is a field
     */
    public void setDN(boolean dn) {
        this.getDN = dn;
    }
    
    /**
     *Gets if the DN is being retrieved
     *@return true if the DN is a field
     */
     public boolean getDN() {
        return this.getDN;
     }
     
     /**
     *Sets the DN
     *@param dn the DN
     */
    public void setDistinguishedName(String dn) {
        this.dn = dn;
    }
    
    /**
     *Gets the DN
     *@return the dn
     */
     public String getDistinguishedName() {
        return dn;
     }
     
     /**
     *Sets the scope of a search
     *@param scope Scope of search
     */
    public void setScope(int scope) {
        this.scope = scope;
    }
    
    /**
     *Gets the scope of a search
     *@return Scope of search
     */
     public int getScope() {
        return this.scope;
     }
     
     /**
     *Sets the number of arguments in a SQL statement
     *@param args Number of arguments
     */
    public void setArgs(int args) {
        this.numArgs = args;
    }
    
    /**
     *Gets the number of arguments in a SQL statement
     *@return Number of arguments
     */
     public int getArgs() {
        return this.numArgs;
     }
     
     /**
     *Sets the data in the fields to be written
     *@param fields Array of data to be written, '?' indicates a parameter
     */
     public void setInsertFields(String[] fields) {
	     this.insertFields = fields;
     }
     
     /**
     *Retrieves the data in the fields to be written
     *@return Array of data to be written, '?' indicates a parameter
     */
     public String[] getInsertFields() {
	     return this.insertFields;
     }
     
     /**
     *Retrieves the offset for fields
     *@return offset array
     */
     public int[] getFieldOffset() {
	    return this.fieldOffset;
     }
     
     /**
     *Sets the offset for fields
     *@param offest offset array
     */
     public void setFieldOffset(int[] offset) {
	     this.fieldOffset = offset;
     }
     
     /**
      *Sets if statement is simple
      *@param simple Is it simple?
      */
     public void setSimple(boolean simple) {
	this.simple = simple;     
     }
     
     /**
      *Gets if statement is simple
      *@return simple Is it simple?
      */
     public boolean getSimple() {
	return simple;     
     }
     
     /**
      *Sets the border between SET and WHERE
      *@param border last number for arguments
      */
     public void setBorder(int border) {
        this.border = border;
     }
     
     /**
      *Gets the border between SET and WHERE
      *@return last number for arguments
      */
     public int getBorder() {
        return border;
     }
     
     /**
      *Returns the sql string
      *@return SQL string
      */
     public String getSQL() {
         return this.sql;
     }
     
     /**
      *Sets the fields Map
      *@param fieldsMap The Map
      */
     public void setFieldsMap(LinkedList fieldsMap) {
         this.fieldsMap = fieldsMap;
     }
     
     /**
      *Returns the field,value map
      *@return map
      */
     public LinkedList getFieldsMap() {
         return this.fieldsMap;
     }
     
     /**
      *Sets the fields of the dn
      *@param fieldsDN Array of field names
      */
     public void setDnFields(String[] dnFields) {
         this.dnfields = dnFields;
     }
     
     /**
      *Retrieves the fields of the dn
      *@return fields in the domain
      */
     public String[] getDnFields() {
         return this.dnfields;
     }
     
	/**
	 * Retrieves a modify's command
	 * @return The command
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * Sets a modify's command
	 * @param cmd The command
	 */
	public void setCommand(String cmd) {
		command = cmd;
	}

	/**
	 * @return
	 */
	public LinkedList getAttribs() {
		return attribs;
	}

	

	

	

	/**
	 * @param list
	 */
	public void setAttribs(LinkedList list) {
		attribs = list;
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

	/**
	 * @return Returns the offsetList.
	 */
	public ArrayList getOffsetList() {
		return offsetList;
	}

	/**
	 * @param offsetList The offsetList to set.
	 */
	public void setOffsetList(ArrayList offsetList) {
		this.offsetList = offsetList;
	}

	/**
	 * @return Returns the fieldStore.
	 */
	public HashMap getFieldMap() {
		return fieldMap;
	}
	/**
	 * @param fieldStore The fieldStore to set.
	 */
	public void setFieldMap(HashMap fieldMap) {
		this.fieldMap = fieldMap;
	}
	/**
	 * @return Returns the revFieldMap.
	 */
	public HashMap getRevFieldMap() {
		return revFieldMap;
	}
	/**
	 * @param revFieldMap The revFieldMap to set.
	 */
	public void setRevFieldMap(HashMap revFieldMap) {
		this.revFieldMap = revFieldMap;
	}
	/**
	 * @param set
	 */
	public void setDontAdd(Set set) {
		this.dontAdd = set;
		
	}
	/**
	 * @return
	 */
	public Set getDontAdd() {
		return this.dontAdd;
	}
	/**
	 * @param string
	 */
	public void setDefaultOC(String string) {
		this.defOC = string;
		
	}
	/**
	 * @return
	 */
	public String getDefOC() {
		return this.defOC;
	}
}
