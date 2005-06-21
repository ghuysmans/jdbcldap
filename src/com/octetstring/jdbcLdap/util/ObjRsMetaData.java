/*
 * Created on Feb 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.octetstring.jdbcLdap.util;

import java.sql.*;

import java.io.*;

public class ObjRsMetaData extends RSMetaData implements Serializable {
	String[] fieldNames;

	public ObjRsMetaData (String[] fieldNames) {
	 	this.fieldNames = fieldNames;
	}

	/**
   * Whats the number of columns in the ResultSet?
   *
   * @return the number
   * @exception java.sql.SQLException if a database access error occurs
   *
   * @author Mark Matthews <mmatthew@worldserver.com>
   */

  public int getColumnCount() throws java.sql.SQLException
  {
    return fieldNames != null ? fieldNames.length : 0;
  }

  /**
   * What's a column's name?
   *
   * @param column the first column is 1, the second is 2, etc.
   * @return the column name
   * @exception java.sql.SQLException if a databvase access error occurs
   *
   * @author Mark Matthews <mmatthew@worldserver.com>
   */

  public String getColumnName(int column) throws java.sql.SQLException
  {
    return fieldNames[column-1];
  }

}
