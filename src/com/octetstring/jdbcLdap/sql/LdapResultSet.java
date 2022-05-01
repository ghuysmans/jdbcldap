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
 * LdapResultSet.java
 *
 * Created on March 14, 2002, 6:17 PM
 */

package com.octetstring.jdbcLdap.sql;

import java.sql.*;
import com.octetstring.jdbcLdap.sql.statements.JdbcLdapSelect;
import com.octetstring.jdbcLdap.jndi.*;

import java.io.*;
import java.util.*;
import java.net.*;
import java.math.BigDecimal;

/**
 *Stores the result from a query
 *@author Marc Boorshtein, OctetString
 */
public class LdapResultSet implements java.sql.ResultSet {

	/** List of field names and number of values **/
	ArrayList fields;

	

	/**Connection to ldap server */
	JndiLdapConnection con;

	/** Current Row */
	HashMap row;



	/** Row pointer */
	int pos;

	/** base DN of query */
	String baseDN;

	/** Base statement */
	JdbcLdapStatement statement;

	/** List of types */
	int[] types;

	/** Determines if the last value was null */
	boolean wasNull;
	
	UnpackResults unpack;

	/** 
	 * Sets <code>row</code> to the current row
	 *@return true if <cod>row</code> is not null
	 * @throws SQLNamingException
	 */
	boolean moveToPos() throws SQLNamingException {
		if (pos >= 0 && unpack.moveNext(pos)) {
			this.row = (HashMap) unpack.getRows().get(this.pos);
			return (row != null);
		} else {
			row = null;
			return false;
		}
	}

	/** Creates new LdapResultSet */
	public LdapResultSet(
		JndiLdapConnection con,
		JdbcLdapStatement statement,
		UnpackResults unpack,
		String baseDN) {
		
		this.unpack = unpack;
		this.con = con;
		
		this.baseDN = baseDN;
		this.statement = statement;
		
		
		

		pos = -1;
	}

	String getByName(String name) throws SQLException {
		if (row == null)
			throw new SQLException("Invalid row position");
		String val = ((String) row.get(name));
		if (val == null) {
			this.wasNull = true;
			return "";
		} else {
			this.wasNull = false;
			return val;
		}
	}

	String getByNum(int id) throws SQLException {
		if (row == null)
			throw new SQLException("Invalid row position");

		String field = (String) this.unpack.getFieldNames().get(id - 1);

		if (field == null)
			throw new SQLException(
				"Field " + Integer.toString(id) + " Does not Exist");

		String val = ((String) row.get(field));
		if (val == null) {
			this.wasNull = true;
			return "";
		} else {
			this.wasNull = false;
			return val;
		}
	}

	public boolean absolute(int param) throws java.sql.SQLException {
		pos = param;
		return (moveToPos());
	}

	public void afterLast() throws java.sql.SQLException {
		pos = this.unpack.getRows().size();
		moveToPos();
	}

	public void beforeFirst() throws java.sql.SQLException {
		pos = -1;
		moveToPos();
	}

	public void cancelRowUpdates() throws java.sql.SQLException {

	}

	public void clearWarnings() throws java.sql.SQLException {
	}

	public void close() throws java.sql.SQLException {

	}

	public void deleteRow() throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public int findColumn(java.lang.String str) throws java.sql.SQLException {
		return this.unpack.getFieldNames().indexOf(str);
	}

	public boolean first() throws java.sql.SQLException {
		pos = 0;
		return moveToPos();

	}

	public java.sql.Array getArray(int param) throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public java.sql.Array getArray(java.lang.String str)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public java.io.InputStream getAsciiStream(int param)
		throws java.sql.SQLException {
		return new StringBufferInputStream(getByNum(param));
	}

	public java.io.InputStream getAsciiStream(java.lang.String str)
		throws java.sql.SQLException {
		return new StringBufferInputStream(getByName(str));
	}

	public java.math.BigDecimal getBigDecimal(int param)
		throws java.sql.SQLException {
		return new BigDecimal(getByNum(param));
	}

	public java.math.BigDecimal getBigDecimal(java.lang.String str)
		throws java.sql.SQLException {
		return new BigDecimal(getByName(str));
	}

	public java.math.BigDecimal getBigDecimal(int param, int param1)
		throws java.sql.SQLException {
		return new BigDecimal(getByNum(param));
	}

	public java.math.BigDecimal getBigDecimal(java.lang.String str, int param)
		throws java.sql.SQLException {
		return new BigDecimal(getByName(str));
	}

	public java.io.InputStream getBinaryStream(int param)
		throws java.sql.SQLException {
		return new ByteArrayInputStream(getByNum(param).getBytes());
	}

	public java.io.InputStream getBinaryStream(java.lang.String str)
		throws java.sql.SQLException {
		return new ByteArrayInputStream(getByName(str).getBytes());
	}

	public java.sql.Blob getBlob(int param) throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public java.sql.Blob getBlob(java.lang.String str)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public boolean getBoolean(int param) throws java.sql.SQLException {
		return Boolean.getBoolean(this.getByNum(param));
	}

	public boolean getBoolean(java.lang.String str)
		throws java.sql.SQLException {
		return Boolean.getBoolean(getByName(str));
	}

	public byte getByte(int param) throws java.sql.SQLException {
		return Byte.parseByte(getByNum(param));
	}

	public byte getByte(java.lang.String str) throws java.sql.SQLException {
		return Byte.parseByte(getByName(str));
	}

	public byte[] getBytes(int param) throws java.sql.SQLException {
		return getByNum(param).getBytes();

	}

	public byte[] getBytes(java.lang.String str) throws java.sql.SQLException {
		return getByName(str).getBytes();
	}

	public java.io.Reader getCharacterStream(int param)
		throws java.sql.SQLException {
		return new StringReader(getByNum(param));
	}

	public java.io.Reader getCharacterStream(java.lang.String str)
		throws java.sql.SQLException {
		return new StringReader(getByName(str));
	}

	public java.sql.Clob getClob(int param) throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public java.sql.Clob getClob(java.lang.String str)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public int getConcurrency() throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public java.lang.String getCursorName() throws java.sql.SQLException {
		return "";
	}

	public java.sql.Date getDate(int param) throws java.sql.SQLException {
		return java.sql.Date.valueOf(getByNum(param));
	}

	public java.sql.Date getDate(java.lang.String str)
		throws java.sql.SQLException {
		return java.sql.Date.valueOf(getByName(str));
	}

	public java.sql.Date getDate(int param, java.util.Calendar calendar)
		throws java.sql.SQLException {
		return getDate(param);

	}

	public java.sql.Date getDate(
		java.lang.String str,
		java.util.Calendar calendar)
		throws java.sql.SQLException {
		return getDate(str);
	}

	public double getDouble(int param) throws java.sql.SQLException {
		return Double.parseDouble(getByNum(param));
	}

	public double getDouble(java.lang.String str)
		throws java.sql.SQLException {
		return Double.parseDouble(getByName(str));
	}

	public int getFetchDirection() throws java.sql.SQLException {
		return 1;
	}

	public int getFetchSize() throws java.sql.SQLException {
		return 1;
	}

	public float getFloat(int param) throws java.sql.SQLException {
		return Float.parseFloat(getByNum(param));
	}

	public float getFloat(java.lang.String str) throws java.sql.SQLException {
		return Float.parseFloat(getByName(str));
	}

	public int getInt(int param) throws java.sql.SQLException {
		String val = getByNum(param);
		return val.length() == 0 ? 0 : Integer.parseInt(val);
	}

	public int getInt(java.lang.String str) throws java.sql.SQLException {
		String val = getByName(str);
		return val.length() == 0 ? 0 : Integer.parseInt(val);
	}

	public long getLong(int param) throws java.sql.SQLException {
		String val = getByNum(param);
		return val.length() == 0 ? 0 : Long.parseLong(val);
	}

	public long getLong(java.lang.String str) throws java.sql.SQLException {
		String val = getByName(str);
		return val.length() == 0 ? 0 : Long.parseLong(val);
	}

	public java.sql.ResultSetMetaData getMetaData()
		throws java.sql.SQLException {
		return new JdbcLdapMetaData(
			this.unpack,
			this.baseDN);
	}

	public java.lang.Object getObject(int param) throws java.sql.SQLException {
		return getByNum(param);
	}

	public java.lang.Object getObject(java.lang.String str)
		throws java.sql.SQLException {
		return getByName(str);
	}

	public java.lang.Object getObject(int param, java.util.Map map)
		throws java.sql.SQLException {
		return getByNum(param);

	}

	public java.lang.Object getObject(java.lang.String str, java.util.Map map)
		throws java.sql.SQLException {
		return getByName(str);
	}

	public java.sql.Ref getRef(int param) throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public java.sql.Ref getRef(java.lang.String str)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public int getRow() throws java.sql.SQLException {
		return pos;
	}

	public short getShort(int param) throws java.sql.SQLException {
		return Short.parseShort(getByNum(param));
	}

	public short getShort(java.lang.String str) throws java.sql.SQLException {
		return Short.parseShort(getByName(str));
	}

	public java.sql.Statement getStatement() throws java.sql.SQLException {
		return this.statement;
	}

	public java.lang.String getString(int param) throws java.sql.SQLException {
		return getByNum(param);
	}

	public java.lang.String getString(java.lang.String str)
		throws java.sql.SQLException {
		return getByName(str);
	}

	public java.sql.Time getTime(int param) throws java.sql.SQLException {
		return java.sql.Time.valueOf(getByNum(param));
	}

	public java.sql.Time getTime(java.lang.String str)
		throws java.sql.SQLException {
		return java.sql.Time.valueOf(getByName(str));
	}

	public java.sql.Time getTime(int param, java.util.Calendar calendar)
		throws java.sql.SQLException {
		return getTime(param);
	}

	public java.sql.Time getTime(
		java.lang.String str,
		java.util.Calendar calendar)
		throws java.sql.SQLException {
		return getTime(str);
	}

	public java.sql.Timestamp getTimestamp(int param)
		throws java.sql.SQLException {
		return java.sql.Timestamp.valueOf(getByNum(param));
	}

	public java.sql.Timestamp getTimestamp(java.lang.String str)
		throws java.sql.SQLException {
		return java.sql.Timestamp.valueOf(getByName(str));
	}

	public java.sql.Timestamp getTimestamp(
		int param,
		java.util.Calendar calendar)
		throws java.sql.SQLException {
		return getTimestamp(param);
	}

	public java.sql.Timestamp getTimestamp(
		java.lang.String str,
		java.util.Calendar calendar)
		throws java.sql.SQLException {
		return getTimestamp(str);
	}

	public int getType() throws java.sql.SQLException {
		return 0;
	}

	public java.net.URL getURL(int param) throws java.sql.SQLException {
		try {
			return new URL(getByNum(param));
		} catch (Exception e) {
			throw new SQLException(e.toString());
		}
	}

	public java.net.URL getURL(java.lang.String str)
		throws java.sql.SQLException {
		try {
			return new URL(getByName(str));
		} catch (Exception e) {
			throw new SQLException(e.toString());
		}
	}

	public java.io.InputStream getUnicodeStream(int param)
		throws java.sql.SQLException {
		return new StringBufferInputStream(getByNum(param));
	}

	public java.io.InputStream getUnicodeStream(java.lang.String str)
		throws java.sql.SQLException {
		return new StringBufferInputStream(getByName(str));
	}

	public java.sql.SQLWarning getWarnings() throws java.sql.SQLException {
		return null;
	}

	public void insertRow() throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public boolean isAfterLast() throws java.sql.SQLException {
		return pos > this.unpack.getRows().size() - 1;
	}

	public boolean isBeforeFirst() throws java.sql.SQLException {
		return pos == -1;
	}

	public boolean isFirst() throws java.sql.SQLException {
		return pos == 0;
	}

	public boolean isLast() throws java.sql.SQLException {
		return pos == this.unpack.getRows().size() - 1;
	}

	public boolean last() throws java.sql.SQLException {
		pos = this.unpack.getRows().size() - 1;
		return true;
	}

	public void moveToCurrentRow() throws java.sql.SQLException {

	}

	public void moveToInsertRow() throws java.sql.SQLException {
		throw new SQLException("Not Implemented");
	}

	public boolean next() throws java.sql.SQLException {
		pos++;
		return moveToPos();
	}

	public boolean previous() throws java.sql.SQLException {
		pos--;
		return moveToPos();
	}

	public void refreshRow() throws java.sql.SQLException {

	}

	public boolean relative(int param) throws java.sql.SQLException {
		pos += param;
		return moveToPos();
	}

	public boolean rowDeleted() throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public boolean rowInserted() throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public boolean rowUpdated() throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void setFetchDirection(int param) throws java.sql.SQLException {

	}

	public void setFetchSize(int param) throws java.sql.SQLException {

	}

	public void updateArray(int param, java.sql.Array array)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateArray(java.lang.String str, java.sql.Array array)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateAsciiStream(
		int param,
		java.io.InputStream inputStream,
		int param2)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateAsciiStream(
		java.lang.String str,
		java.io.InputStream inputStream,
		int param)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateBigDecimal(int param, java.math.BigDecimal bigDecimal)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateBigDecimal(
		java.lang.String str,
		java.math.BigDecimal bigDecimal)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateBinaryStream(
		int param,
		java.io.InputStream inputStream,
		int param2)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateBinaryStream(
		java.lang.String str,
		java.io.InputStream inputStream,
		int param)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateBlob(int param, java.sql.Blob blob)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateBlob(java.lang.String str, java.sql.Blob blob)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateBoolean(int param, boolean param1)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateBoolean(java.lang.String str, boolean param)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateByte(int param, byte param1)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateByte(java.lang.String str, byte param)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateBytes(int param, byte[] values)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateBytes(java.lang.String str, byte[] values)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateCharacterStream(
		int param,
		java.io.Reader reader,
		int param2)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateCharacterStream(
		java.lang.String str,
		java.io.Reader reader,
		int param)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateClob(int param, java.sql.Clob clob)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateClob(java.lang.String str, java.sql.Clob clob)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateDate(int param, java.sql.Date date)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateDate(java.lang.String str, java.sql.Date date)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateDouble(int param, double param1)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateDouble(java.lang.String str, double param)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateFloat(int param, float param1)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateFloat(java.lang.String str, float param)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateInt(int param, int param1) throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateInt(java.lang.String str, int param)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateLong(int param, long param1)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateLong(java.lang.String str, long param)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateNull(int param) throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateNull(java.lang.String str) throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateObject(int param, java.lang.Object obj)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateObject(java.lang.String str, java.lang.Object obj)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateObject(int param, java.lang.Object obj, int param2)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateObject(
		java.lang.String str,
		java.lang.Object obj,
		int param)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateRef(int param, java.sql.Ref ref)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateRef(java.lang.String str, java.sql.Ref ref)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateRow() throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateShort(int param, short param1)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateShort(java.lang.String str, short param)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateString(int param, java.lang.String str)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateString(java.lang.String str, java.lang.String str1)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateTime(int param, java.sql.Time time)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateTime(java.lang.String str, java.sql.Time time)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateTimestamp(int param, java.sql.Timestamp timestamp)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public void updateTimestamp(
		java.lang.String str,
		java.sql.Timestamp timestamp)
		throws java.sql.SQLException {
		throw new SQLException("Not implemented");
	}

	public boolean wasNull() throws java.sql.SQLException {
		return this.wasNull;
	}

}
