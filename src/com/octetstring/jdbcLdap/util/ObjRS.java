/*
 * Created on Feb 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.octetstring.jdbcLdap.util;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

import java.util.*;
import java.io.*;
import java.lang.reflect.*;
/**
 * @author mlb
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public  class ObjRS implements ResultSet, Serializable {
	public final transient static int IT_ITERATOR=0;
	public final transient static int IT_ARRAY=1;
	public final transient static int IT_SINGLE=2;

	public final transient static int EXT_REF=0;
	public final transient static int EXT_HASH=1;
	public final transient static int EXT_ARRAY=2;



	HashMap nameMap;
	Collection c;
	Iterator it;
	HashMap props;
	boolean isFirst;
	int itType;
	int extType;

	Object[] metaData;
	Object o;
	Object[] lines;

	String[][] table;
	String [] fieldNames;
	int curr;

	boolean wasNull;

	private void refGetFields(Object o) {
		LinkedList l = new LinkedList();
		nameMap = new HashMap();
		String name;

		Method[] meths = o.getClass().getMethods();
		for (int i=0,m=meths.length; i<m; i++) {
                	name = meths[i].getName();
			if (name.substring(0,3).equals("get") && meths[i].getParameterTypes().length == 0 && ! name.equals("getClass")) {
				name = name.substring(3);
				nameMap.put(name.toLowerCase(),meths[i]);
				l.add(name);
			}
		}

		this.fieldNames = new String[l.size()];

		l.toArray(this.fieldNames);


	}

	private void hashGetFields(Object o) {

		LinkedList l = new LinkedList();

		Iterator it = ((Map) o).keySet().iterator();
		while (it.hasNext()) {
			l.add(it.next().toString().toLowerCase());
		}

		this.fieldNames = new String[l.size()];
		l.toArray(this.fieldNames);
  	}


	/**
	 * Constructor for BaseRS.
	 */
	public ObjRS(Collection c) {
                if (c == null) {
		 	this.wasNull = true;
			fieldNames = new String[0];
			return;
		}
		wasNull = false;
		this.c = c;
		this.it = c.iterator();
		this.isFirst = true;
		this.itType = IT_ITERATOR;

		if (it.hasNext()) {
  			o = it.next();
			if (o instanceof java.util.Map) {
				this.extType = EXT_HASH;
				hashGetFields(o);
			}
			else {
				this.extType = EXT_REF;
				refGetFields(o);
			}
		}
	}

	public ObjRS(Object[] ar) {
		if (ar == null) {
		 	this.wasNull = true;
			fieldNames = new String[0];
			return;
		}
		wasNull = false;
		this.lines = ar;
		this.itType = IT_ARRAY;
		this.isFirst = true;

		o = lines[0];
		if (o instanceof java.util.Map) {
			this.extType = EXT_HASH;
			hashGetFields(o);
		}
		else {
		 	this.extType = EXT_REF;
			refGetFields(o);
		}

		curr = 0;
	}

	public ObjRS(String[][] ar,String[] fieldNames) {
 		this.table = ar;
		this.itType = IT_ARRAY;
		this.extType = EXT_ARRAY;
		this.isFirst = true;
		this.fieldNames = fieldNames;

		nameMap = new HashMap();
		for (int i=0,m=fieldNames.length; i<m; i++) {
  			nameMap.put(fieldNames[i],new Integer(i));
		}

		curr = 0;
	}

	public ObjRS(Object o) {
		if (o == null) {
		 	this.wasNull = true;
			fieldNames = new String[0];
			return;
		}
		wasNull = false;
		this.o = o;
		if (o instanceof java.util.Map) {
                	this.extType = EXT_HASH;
			hashGetFields(o);
		}
		else {
		 	this.extType = EXT_REF;
			refGetFields(o);
		}
		this.isFirst = true;
		this.itType = IT_SINGLE;
	}



	/**
	 * @see java.sql.ResultSet#next()
	 */
	public boolean next() throws SQLException {
		if (this.wasNull) {
			return false;
		}
		switch (itType) {
			case IT_ITERATOR : if (isFirst) {
   					   	isFirst = false;
						if (o == null) {
							return false;
						}
						else {
						  	return true;
						}

					   }
					   else {
					    	if (it.hasNext()) {
						 	o = it.next();
							return true;
						}
						else {
      							return false;
						}
					   }

			case IT_ARRAY :    if (lines != null) {
						if (isFirst) {
							isFirst = false;
							if (lines != null) {
								return lines.length != 0;
							}
							else {
							 	return false;
							}
						}
						else {
							if (curr < lines.length-1) {
								curr++;
								o = lines[curr];
								return true;
							}
							else {
								return false;
							}
						}
					   }
					   else {
					    	if (isFirst) {
							isFirst = false;
							if (table != null) {
								return table.length != 0;
							}
							else {
								return false;
							}
						}
						else {
							if (curr < table.length - 1) {
								curr++;
								return true;
							}
							else {
								return false;
							}
						}
					   }

			case IT_SINGLE :   if (isFirst) {
						this.isFirst = false;
						return true;
					   }
					   else {
					    	return false;
					   }


		}

		return false;
	}

	/**
	 * @see java.sql.ResultSet#getString(String)
	 */
	public String getString(String columnName) throws SQLException {
		if (this.wasNull) {
			return "";
		}

		
		switch (extType) {
         		case EXT_REF :  try {
						//System.out.println(nameMap.get(columnName));
						//System.out.println(o);
      						//System.out.println(((Method) nameMap.get(columnName)).invoke(o,new Object[] {}));
						Object tmp = ((Method) nameMap.get(columnName)).invoke(o,new Object[] {});
						if (tmp != null) {
						 	return tmp.toString();
						}
						else {
						 	return "";
						}
						//return ((Method) nameMap.get(columnName)).invoke(o,new Object[] {}).toString();
					}
					catch (Exception e) {

					 	throw new SQLException(e.toString());
					}
			case EXT_HASH : 
				
				
				
				Object val = ((Map) o).get(columnName);
				if (val == null) {
					return null;
				} else {
					return val.toString();
				}
				
			case EXT_ARRAY : return table[curr][((Integer) nameMap.get(columnName)).intValue()];

		}
		return "";
	}

	/**
	 * @see java.sql.ResultSet#close()
	 */
	public void close() throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#wasNull()
	 */
	public boolean wasNull() throws SQLException {
		return false;
	}

	/**
	 * @see java.sql.ResultSet#getString(int)
	 */
	public String getString(int columnIndex) throws SQLException {
		return "";
	}

	/**
	 * @see java.sql.ResultSet#getBoolean(int)
	 */
	public boolean getBoolean(int columnIndex) throws SQLException {
		return false;
	}

	/**
	 * @see java.sql.ResultSet#getByte(int)
	 */
	public byte getByte(int columnIndex) throws SQLException {
		return 0;
	}

	/**
	 * @see java.sql.ResultSet#getShort(int)
	 */
	public short getShort(int columnIndex) throws SQLException {
		return 0;
	}

	/**
	 * @see java.sql.ResultSet#getInt(int)
	 */
	public int getInt(int columnIndex) throws SQLException {
		return 0;
	}

	/**
	 * @see java.sql.ResultSet#getLong(int)
	 */
	public long getLong(int columnIndex) throws SQLException {
		return 0;
	}

	/**
	 * @see java.sql.ResultSet#getFloat(int)
	 */
	public float getFloat(int columnIndex) throws SQLException {
		return 0;
	}

	/**
	 * @see java.sql.ResultSet#getDouble(int)
	 */
	public double getDouble(int columnIndex) throws SQLException {
		return 0;
	}

	/**
	 * @see java.sql.ResultSet#getBigDecimal(int, int)
	 * @deprecated
	 */
	public BigDecimal getBigDecimal(int columnIndex, int scale)
		throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getBytes(int)
	 */
	public byte[] getBytes(int columnIndex) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getDate(int)
	 */
	public Date getDate(int columnIndex) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getTime(int)
	 */
	public Time getTime(int columnIndex) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getTimestamp(int)
	 */
	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getAsciiStream(int)
	 */
	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getUnicodeStream(int)
	 * @deprecated
	 */
	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getBinaryStream(int)
	 */
	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		return null;
	}


	/**
	 * @see java.sql.ResultSet#getBoolean(String)
	 */
	public boolean getBoolean(String columnName) throws SQLException {
		return false;
	}

	/**
	 * @see java.sql.ResultSet#getByte(String)
	 */
	public byte getByte(String columnName) throws SQLException {
		return 0;
	}

	/**
	 * @see java.sql.ResultSet#getShort(String)
	 */
	public short getShort(String columnName) throws SQLException {
		return 0;
	}

	/**
	 * @see java.sql.ResultSet#getInt(String)
	 */
	public int getInt(String columnName) throws SQLException {
		return 0;
	}

	/**
	 * @see java.sql.ResultSet#getLong(String)
	 */
	public long getLong(String columnName) throws SQLException {
		return 0;
	}

	/**
	 * @see java.sql.ResultSet#getFloat(String)
	 */
	public float getFloat(String columnName) throws SQLException {
		return 0;
	}

	/**
	 * @see java.sql.ResultSet#getDouble(String)
	 */
	public double getDouble(String columnName) throws SQLException {
		return 0;
	}

	/**
	 * @see java.sql.ResultSet#getBigDecimal(String, int)
	 * @deprecated
	 */
	public BigDecimal getBigDecimal(String columnName, int scale)
		throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getBytes(String)
	 */
	public byte[] getBytes(String columnName) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getDate(String)
	 */
	public Date getDate(String columnName) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getTime(String)
	 */
	public Time getTime(String columnName) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getTimestamp(String)
	 */
	public Timestamp getTimestamp(String columnName) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getAsciiStream(String)
	 */
	public InputStream getAsciiStream(String columnName) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getUnicodeStream(String)
	 * @deprecated
	 */
	public InputStream getUnicodeStream(String columnName)
		throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getBinaryStream(String)
	 */
	public InputStream getBinaryStream(String columnName) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getWarnings()
	 */
	public SQLWarning getWarnings() throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#clearWarnings()
	 */
	public void clearWarnings() throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#getCursorName()
	 */
	public String getCursorName() throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getMetaData()
	 */
	public ResultSetMetaData getMetaData() throws SQLException {
		return new ObjRsMetaData(fieldNames);
	}

	/**
	 * @see java.sql.ResultSet#getObject(int)
	 */
	public Object getObject(int columnIndex) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getObject(String)
	 */
	public Object getObject(String columnName) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#findColumn(String)
	 */
	public int findColumn(String columnName) throws SQLException {
		return 0;
	}

	/**
	 * @see java.sql.ResultSet#getCharacterStream(int)
	 */
	public Reader getCharacterStream(int columnIndex) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getCharacterStream(String)
	 */
	public Reader getCharacterStream(String columnName) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getBigDecimal(int)
	 */
	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getBigDecimal(String)
	 */
	public BigDecimal getBigDecimal(String columnName) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#isBeforeFirst()
	 */
	public boolean isBeforeFirst() throws SQLException {
		return false;
	}

	/**
	 * @see java.sql.ResultSet#isAfterLast()
	 */
	public boolean isAfterLast() throws SQLException {
		return false;
	}

	/**
	 * @see java.sql.ResultSet#isFirst()
	 */
	public boolean isFirst() throws SQLException {
		return false;
	}

	/**
	 * @see java.sql.ResultSet#isLast()
	 */
	public boolean isLast() throws SQLException {
		return false;
	}

	/**
	 * @see java.sql.ResultSet#beforeFirst()
	 */
	public void beforeFirst() throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#afterLast()
	 */
	public void afterLast() throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#first()
	 */
	public boolean first() throws SQLException {
		return false;
	}

	/**
	 * @see java.sql.ResultSet#last()
	 */
	public boolean last() throws SQLException {
		return false;
	}

	/**
	 * @see java.sql.ResultSet#getRow()
	 */
	public int getRow() throws SQLException {
		return 0;
	}

	/**
	 * @see java.sql.ResultSet#absolute(int)
	 */
	public boolean absolute(int row) throws SQLException {
		return false;
	}

	/**
	 * @see java.sql.ResultSet#relative(int)
	 */
	public boolean relative(int rows) throws SQLException {
		return false;
	}

	/**
	 * @see java.sql.ResultSet#previous()
	 */
	public boolean previous() throws SQLException {
		return false;
	}

	/**
	 * @see java.sql.ResultSet#setFetchDirection(int)
	 */
	public void setFetchDirection(int direction) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#getFetchDirection()
	 */
	public int getFetchDirection() throws SQLException {
		return 0;
	}

	/**
	 * @see java.sql.ResultSet#setFetchSize(int)
	 */
	public void setFetchSize(int rows) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#getFetchSize()
	 */
	public int getFetchSize() throws SQLException {
		return 0;
	}

	/**
	 * @see java.sql.ResultSet#getType()
	 */
	public int getType() throws SQLException {
		return 0;
	}

	/**
	 * @see java.sql.ResultSet#getConcurrency()
	 */
	public int getConcurrency() throws SQLException {
		return 0;
	}

	/**
	 * @see java.sql.ResultSet#rowUpdated()
	 */
	public boolean rowUpdated() throws SQLException {
		return false;
	}

	/**
	 * @see java.sql.ResultSet#rowInserted()
	 */
	public boolean rowInserted() throws SQLException {
		return false;
	}

	/**
	 * @see java.sql.ResultSet#rowDeleted()
	 */
	public boolean rowDeleted() throws SQLException {
		return false;
	}

	/**
	 * @see java.sql.ResultSet#updateNull(int)
	 */
	public void updateNull(int columnIndex) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateBoolean(int, boolean)
	 */
	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateByte(int, byte)
	 */
	public void updateByte(int columnIndex, byte x) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateShort(int, short)
	 */
	public void updateShort(int columnIndex, short x) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateInt(int, int)
	 */
	public void updateInt(int columnIndex, int x) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateLong(int, long)
	 */
	public void updateLong(int columnIndex, long x) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateFloat(int, float)
	 */
	public void updateFloat(int columnIndex, float x) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateDouble(int, double)
	 */
	public void updateDouble(int columnIndex, double x) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateBigDecimal(int, BigDecimal)
	 */
	public void updateBigDecimal(int columnIndex, BigDecimal x)
		throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateString(int, String)
	 */
	public void updateString(int columnIndex, String x) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateBytes(int, byte[])
	 */
	public void updateBytes(int columnIndex, byte[] x) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateDate(int, Date)
	 */
	public void updateDate(int columnIndex, Date x) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateTime(int, Time)
	 */
	public void updateTime(int columnIndex, Time x) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateTimestamp(int, Timestamp)
	 */
	public void updateTimestamp(int columnIndex, Timestamp x)
		throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateAsciiStream(int, InputStream, int)
	 */
	public void updateAsciiStream(int columnIndex, InputStream x, int length)
		throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateBinaryStream(int, InputStream, int)
	 */
	public void updateBinaryStream(int columnIndex, InputStream x, int length)
		throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateCharacterStream(int, Reader, int)
	 */
	public void updateCharacterStream(int columnIndex, Reader x, int length)
		throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateObject(int, Object, int)
	 */
	public void updateObject(int columnIndex, Object x, int scale)
		throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateObject(int, Object)
	 */
	public void updateObject(int columnIndex, Object x) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateNull(String)
	 */
	public void updateNull(String columnName) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateBoolean(String, boolean)
	 */
	public void updateBoolean(String columnName, boolean x)
		throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateByte(String, byte)
	 */
	public void updateByte(String columnName, byte x) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateShort(String, short)
	 */
	public void updateShort(String columnName, short x) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateInt(String, int)
	 */
	public void updateInt(String columnName, int x) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateLong(String, long)
	 */
	public void updateLong(String columnName, long x) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateFloat(String, float)
	 */
	public void updateFloat(String columnName, float x) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateDouble(String, double)
	 */
	public void updateDouble(String columnName, double x) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateBigDecimal(String, BigDecimal)
	 */
	public void updateBigDecimal(String columnName, BigDecimal x)
		throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateString(String, String)
	 */
	public void updateString(String columnName, String x) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateBytes(String, byte[])
	 */
	public void updateBytes(String columnName, byte[] x) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateDate(String, Date)
	 */
	public void updateDate(String columnName, Date x) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateTime(String, Time)
	 */
	public void updateTime(String columnName, Time x) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateTimestamp(String, Timestamp)
	 */
	public void updateTimestamp(String columnName, Timestamp x)
		throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateAsciiStream(String, InputStream, int)
	 */
	public void updateAsciiStream(String columnName, InputStream x, int length)
		throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateBinaryStream(String, InputStream, int)
	 */
	public void updateBinaryStream(
		String columnName,
		InputStream x,
		int length)
		throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateCharacterStream(String, Reader, int)
	 */
	public void updateCharacterStream(
		String columnName,
		Reader reader,
		int length)
		throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateObject(String, Object, int)
	 */
	public void updateObject(String columnName, Object x, int scale)
		throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateObject(String, Object)
	 */
	public void updateObject(String columnName, Object x) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#insertRow()
	 */
	public void insertRow() throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateRow()
	 */
	public void updateRow() throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#deleteRow()
	 */
	public void deleteRow() throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#refreshRow()
	 */
	public void refreshRow() throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#cancelRowUpdates()
	 */
	public void cancelRowUpdates() throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#moveToInsertRow()
	 */
	public void moveToInsertRow() throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#moveToCurrentRow()
	 */
	public void moveToCurrentRow() throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#getStatement()
	 */
	public Statement getStatement() throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getObject(int, Map)
	 */
	public Object getObject(int i, Map map) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getRef(int)
	 */
	public Ref getRef(int i) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getBlob(int)
	 */
	public Blob getBlob(int i) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getClob(int)
	 */
	public Clob getClob(int i) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getArray(int)
	 */
	public Array getArray(int i) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getObject(String, Map)
	 */
	public Object getObject(String colName, Map map) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getRef(String)
	 */
	public Ref getRef(String colName) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getBlob(String)
	 */
	public Blob getBlob(String colName) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getClob(String)
	 */
	public Clob getClob(String colName) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getArray(String)
	 */
	public Array getArray(String colName) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getDate(int, Calendar)
	 */
	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getDate(String, Calendar)
	 */
	public Date getDate(String columnName, Calendar cal) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getTime(int, Calendar)
	 */
	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getTime(String, Calendar)
	 */
	public Time getTime(String columnName, Calendar cal) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getTimestamp(int, Calendar)
	 */
	public Timestamp getTimestamp(int columnIndex, Calendar cal)
		throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getTimestamp(String, Calendar)
	 */
	public Timestamp getTimestamp(String columnName, Calendar cal)
		throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getURL(int)
	 */
	public URL getURL(int columnIndex) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#getURL(String)
	 */
	public URL getURL(String columnName) throws SQLException {
		return null;
	}

	/**
	 * @see java.sql.ResultSet#updateRef(int, Ref)
	 */
	public void updateRef(int columnIndex, Ref x) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateRef(String, Ref)
	 */
	public void updateRef(String columnName, Ref x) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateBlob(int, Blob)
	 */
	public void updateBlob(int columnIndex, Blob x) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateBlob(String, Blob)
	 */
	public void updateBlob(String columnName, Blob x) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateClob(int, Clob)
	 */
	public void updateClob(int columnIndex, Clob x) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateClob(String, Clob)
	 */
	public void updateClob(String columnName, Clob x) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateArray(int, Array)
	 */
	public void updateArray(int columnIndex, Array x) throws SQLException {
	}

	/**
	 * @see java.sql.ResultSet#updateArray(String, Array)
	 */
	public void updateArray(String columnName, Array x) throws SQLException {
	}

}
