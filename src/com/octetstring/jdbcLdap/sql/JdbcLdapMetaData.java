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
 * JdbcLdapMetaData.java
 *
 * Created on March 15, 2002, 4:07 PM
 */

package com.octetstring.jdbcLdap.sql;

import java.sql.*;
import java.util.*;
import com.octetstring.jdbcLdap.jndi.*;

/**
 *Holds the metadata of the table
 *@author Marc Boorshtein, OctetString
 */
public class JdbcLdapMetaData implements java.sql.ResultSetMetaData {

    
    
    /** BaseDN used for search */
    String baseDN;
    
    

	private UnpackResults unpack;
    
    /** Creates new JdbcsResultSelect */
    public JdbcLdapMetaData(UnpackResults unpack, String baseDN) {
        this.unpack = unpack;
    }

    
    
    public java.lang.String getCatalogName(int param) throws java.sql.SQLException {
        return ((String) unpack.getFieldNames().get(param));
    }
    
    public java.lang.String getColumnClassName(int param) throws java.sql.SQLException {
        //TODO:Implement stronger detection
        return "java.lang.String";
    }
    
    public int getColumnCount() throws java.sql.SQLException {
        return (unpack.getFieldNames().size());
    }
    
    public int getColumnDisplaySize(int param) throws java.sql.SQLException {
        return -1;
    }
    
    public java.lang.String getColumnLabel(int param) throws java.sql.SQLException {
        return getCatalogName(param-1);
    }
    
    public java.lang.String getColumnName(int param) throws java.sql.SQLException {
        return getCatalogName(param -1);
    }
    
    public int getColumnType(int param) throws java.sql.SQLException {
        return ((Integer) unpack.getFieldTypes().get(param-1)).intValue();
    }
    
    
    public java.lang.String getColumnTypeName(int param) throws java.sql.SQLException {
        int coltype = ((Integer) unpack.getFieldTypes().get(param-1)).intValue();
        switch (coltype) {
            case Types.VARCHAR : return "VARCHAR";
            case Types.DOUBLE : return "DOUBLE";
            case Types.INTEGER : return "INTEGER";
            case Types.DATE : return "DATE";
            case Types.TIME : return "TIME";
            case Types.TIMESTAMP : return "TIMESTAMP";
            default :throw new SQLException("Illegal Type");
        }
    }
    
    
    public int getPrecision(int param) throws java.sql.SQLException {
        return 0;
    }
    
    public int getScale(int param) throws java.sql.SQLException {
        return 0;
    }
    
    public java.lang.String getSchemaName(int param) throws java.sql.SQLException {
        return "";
    }
    
    public java.lang.String getTableName(int param) throws java.sql.SQLException {
        return baseDN;
    }
    
    public boolean isAutoIncrement(int param) throws java.sql.SQLException {
        return false;
    }
    
    public boolean isCaseSensitive(int param) throws java.sql.SQLException {
        return false;
    }
    
    public boolean isCurrency(int param) throws java.sql.SQLException {
        return false;
    }
    
    public boolean isDefinitelyWritable(int param) throws java.sql.SQLException {
        return false;
    }
    
    public int isNullable(int param) throws java.sql.SQLException {
        return 0;
    }
    
    public boolean isReadOnly(int param) throws java.sql.SQLException {
        return true;
    }
    
    public boolean isSearchable(int param) throws java.sql.SQLException {
        return false;
    }
    
    public boolean isSigned(int param) throws java.sql.SQLException {
        return false;
    }
    
    public boolean isWritable(int param) throws java.sql.SQLException {
        return false;
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
    
}
