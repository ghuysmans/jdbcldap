/*
 * Created on Feb 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.octetstring.jdbcLdap.util;

/**
This class is used as a basis for not database data inside of RML pages.  Implementation of this class can be used
natively inside of an RML page.  Javadoc APIs won't apear here and can be found in the 
<a href="http://java.sun.com/j2se/1.3/docs/api/java/sql/package-summary.html">JDBC javadocs</a>
*/



import java.sql.*;
import java.util.*;

public class RSMetaData implements java.sql.ResultSetMetaData
{


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
  return 0;
}

/**
 * Is the column automatically numbered (and thus read-only)
 *
 * MySQL Auto-increment columns are not read only,
 * so to conform to the spec, this method returns false.
 *
 * @param column the first column is 1, the second is 2...
 * @return true if so
 * @exception java.sql.SQLException if a database access error occurs
 * 
 * @author Mark Matthews <mmatthew@worldserver.com>
 */

public boolean isAutoIncrement(int column) throws java.sql.SQLException
{
  return false;
}

/**
 * Does a column's case matter? ASSUMPTION: Any field that is
 * not obviously case insensitive is assumed to be case sensitive
 *
 * @param column the first column is 1, the second is 2...
 * @return true if so
 * @exception java.sql.SQLException if a database access error occurs
 * 
 * @author Mark Matthews <mmatthew@worldserver.com>
 */

public boolean isCaseSensitive(int column) throws java.sql.SQLException
{
  return false;
  
}

/**
 * Can the column be used in a WHERE clause?  Basically for
 * this, I split the functions into two types: recognised
 * types (which are always useable), and OTHER types (which
 * may or may not be useable).  The OTHER types, for now, I
 * will assume they are useable.  We should really query the
 * catalog to see if they are useable.
 *
 * @param column the first column is 1, the second is 2...
 * @return true if they can be used in a WHERE clause
 * @exception java.sql.SQLException if a database access error occurs
 * 
 * @author Mark Matthews <mmatthew@worldserver.com>
 */

public boolean isSearchable(int column) throws java.sql.SQLException
{
  return true;
}

/**
 * Is the column a cash value?
 *
 * @param column the first column is 1, the second is 2...
 * @return true if its a cash column
 * @exception java.sql.SQLException if a database access error occurs
 * 
 * @author Mark Matthews <mmatthew@worldserver.com>
 */

public boolean isCurrency(int column) throws java.sql.SQLException
{
  return false;
}

/**
 * Can you put a NULL in this column?  
 *
 * @param column the first column is 1, the second is 2...
 * @return one of the columnNullable values
 * @exception java.sql.SQLException if a database access error occurs
 * 
 * @author Mark Matthews <mmatthew@worldserver.com>
 */

public int isNullable(int column) throws java.sql.SQLException
{
  return 0;
}

/**
 * Is the column a signed number?
 *
 * @param column the first column is 1, the second is 2...
 * @return true if so
 * @exception java.sql.SQLException if a database access error occurs
 * 
 * @author Mark Matthews <mmatthew@worldserver.com>
 */

public boolean isSigned(int column) throws java.sql.SQLException
{
  return false;
}

/**
 * What is the column's normal maximum width in characters?
 *
 * @param column the first column is 1, the second is 2, etc.
 * @return the maximum width
 * @exception java.sql.SQLException if a database access error occurs
 * 
 * @author Mark Matthews <mmatthew@worldserver.com>
 */

public int getColumnDisplaySize(int column) throws java.sql.SQLException
{
  return 0;
}

/**
 * What is the suggested column title for use in printouts and
 * displays?
 *
 * @param column the first column is 1, the second is 2, etc.
 * @return the column label
 * @exception java.sql.SQLException if a database access error occurs
 * 
 * @author Mark Matthews <mmatthew@worldserver.com>
 */

public String getColumnLabel(int column) throws java.sql.SQLException
{
  return "";
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
  return "";
}

/**
 * What is a column's table's schema?  This relies on us knowing
 * the table name.
 *
 * The JDBC specification allows us to return "" if this is not
 * applicable.
 *
 * @param column the first column is 1, the second is 2...
 * @return the Schema
 * @exception java.sql.SQLException if a database access error occurs
 * 
 * @author Mark Matthews <mmatthew@worldserver.com>
 */

public String getSchemaName(int column) throws java.sql.SQLException
{
  return "";
}

/**
 * What is a column's number of decimal digits.
 *
 * @param column the first column is 1, the second is 2...
 * @return the precision
 * @exception java.sql.SQLException if a database access error occurs
 * 
 * @author James Klicman <james@klicman.com>
 */

public int getPrecision(int column) throws java.sql.SQLException
{
	  return 0;
}

/**
 * What is a column's number of digits to the right of the
 * decimal point?
 *
 * @param column the first column is 1, the second is 2...
 * @return the scale
 * @exception java.sql.SQLException if a database access error occurs
 * 
 * @author James Klicman <james@klicman.com>
 */

public int getScale(int column) throws java.sql.SQLException
{
	return 0;
}

/**
 * What's a column's table's catalog name?
 *
 * @param column the first column is 1, the second is 2...
 * @return catalog name, or "" if not applicable
 * @exception java.sql.SQLException if a database access error occurs
 * 
 * @author Mark Matthews <mmatthew@worldserver.com>
 */

public String getCatalogName(int column) throws java.sql.SQLException
{
  

	  return "";
}

/**
 * Whats a column's table's name?  
 *
 * @param column the first column is 1, the second is 2...
 * @return column name, or "" if not applicable
 * @exception java.sql.SQLException if a database access error occurs
 * 
 * @author Mark Matthews <mmatthew@worldserver.com>
 */

public String getTableName(int column) throws java.sql.SQLException
{
  return "";
}

/**
 * What is a column's SQL Type? (java.sql.Type int)
 *
 * @param column the first column is 1, the second is 2, etc.
 * @return the java.sql.Type value
 * @exception java.sql.SQLException if a database access error occurs
 * @see java.sql.Types
 * 
 * @author Mark Matthews <mmatthew@worldserver.com>
 */

public int getColumnType(int column) throws java.sql.SQLException
{
  return 0;
}

/**
 * Whats is the column's data source specific type name?
 *
 * @param column the first column is 1, the second is 2, etc.
 * @return the type name
 * @exception java.sql.SQLException if a database access error occurs
 *   
 * @author Mark Matthews <mmatthew@worldserver.com>
 */

public String getColumnTypeName(int column) throws java.sql.SQLException
{
  return "String";
}
 
/**
 * Is the column definitely not writable?
 *
 * @param column the first column is 1, the second is 2, etc.
 * @return true if so
 * @exception java.sql.SQLException if a database access error occurs
 * 
 * @author Mark Matthews <mmatthew@worldserver.com>
 */

public boolean isReadOnly(int column) throws java.sql.SQLException
{
  return false;
}

/**
 * Is it possible for a write on the column to succeed?
 *
 * @param column the first column is 1, the second is 2, etc.
 * @return true if so
 * @exception java.sql.SQLException if a database access error occurs
 * 
 * @author Mark Matthews <mmatthew@worldserver.com>
 */

public boolean isWritable(int column) throws java.sql.SQLException
{
  return false;
}

/**
 * Will a write on this column definately succeed?
 *
 * @param column the first column is 1, the second is 2, etc..
 * @return true if so
 * @exception java.sql.SQLException if a database access error occurs
 * 
 * @author Mark Matthews <mmatthew@worldserver.com>
 */

public boolean isDefinitelyWritable(int column) throws java.sql.SQLException
{
  return false;
}

public String getColumnClassName(int x) throws java.sql.SQLException {
	return "";
}

@Override
public <T> T unwrap(Class<T> iface) throws SQLException {
  // TODO Auto-generated method stub
  return null;
}

@Override
public boolean isWrapperFor(Class<?> iface) throws SQLException {
  // TODO Auto-generated method stub
  return false;
}

// *********************************************************************
//
//                END OF PUBLIC INTERFACE
//
// *********************************************************************



}
