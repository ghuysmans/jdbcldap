/* **************************************************************************
 *
 * Copyright (C) 2002-2004 Octet String, Inc. All Rights Reserved.
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
 * JdbcLdapPreparedStatement.java
 *
 * Created on March 19, 2002, 11:48 PM
 */

package com.octetstring.jdbcLdap.sql;
import java.sql.*;
import com.octetstring.jdbcLdap.jndi.*;
import com.octetstring.jdbcLdap.jndi.UnpackResults;
import com.octetstring.jdbcLdap.sql.statements.*;
import com.octetstring.jdbcLdap.sql.*;
import javax.naming.*;
import java.io.*;
/**
 *Parses and manages a prepared statement to the directory
 *@author Marc Boorshtein, OctetString
 * @version
 */
public class JdbcLdapPreparedStatement extends JdbcLdapStatement implements java.sql.PreparedStatement {
    
    
	
	
    
    
    /** Creates new JdbcLdapPreparedStatement */
    public JdbcLdapPreparedStatement(String sql, JndiLdapConnection con) throws SQLException {
        super(con);
        loadSQL(sql);
        
    }
    
    /**
     *Reloads a sql statement based on a sql store
     *@param store the sql store to re-load
     */
    void loadSQL(SqlStore store) throws SQLException {
        loadSQL(store.getSQL());
    }
    
    /**
     *Used to set the caorrect parameter
     *@param pos Position where to insert plus one
     *@param val Value to insert
     */
    void setVal(int pos, String val) throws SQLException {
        
        
        this.stmt.setValue(pos-1,val);
    }

	
    
    public void setUnicodeStream(int param, java.io.InputStream inputStream, int param2) throws java.sql.SQLException {
        char[] c = new char[param2];
        try {
            
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            in.read(c,0,param2);
            
            in.close();
        }
        catch (IOException e) {
            throw new SQLException(e.toString());
        }
        setVal(param,String.valueOf(c));
    }
    
    public void setTime(int param, java.sql.Time time) throws java.sql.SQLException {
        setVal(param,time.toString());
    }
    
    public void setBigDecimal(int param, java.math.BigDecimal bigDecimal) throws java.sql.SQLException {
        setVal(param,bigDecimal.toString());
    }
    
    public boolean execute() throws java.sql.SQLException {
        if (stmt.isUpdate()) {
            executeUpdate();
            return false;
        }
        else {
            executeQuery();
            return true;
        }
    }
    
    public void setURL(int param, java.net.URL uRL) throws java.sql.SQLException {
        setVal(param,uRL.toString());
    }
    
    public void setAsciiStream(int param, java.io.InputStream inputStream, int param2) throws java.sql.SQLException {
        char[] c = new char[param2];
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            
            in.read(c,0,param2);
            
            in.close();
        }
        catch (IOException e) {
            throw new SQLException(e.toString());
        }
        setVal(param,String.valueOf(c));
    }
    
    public void setByte(int param, byte param1) throws java.sql.SQLException {
        setVal(param,Byte.toString(param1));
    }
    
    public void setDouble(int param, double param1) throws java.sql.SQLException {
        setVal(param,Double.toString(param1));
    }
    
    public void setLong(int param, long param1) throws java.sql.SQLException {
        setVal(param,Long.toString(param1));
    }
    
    public void setDate(int param, java.sql.Date date) throws java.sql.SQLException {
        setVal(param,date.toString());
    }
    
    public void setBinaryStream(int param, java.io.InputStream inputStream, int param2) throws java.sql.SQLException {
        char[] c = new char[param2];
        try {
            
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            in.read(c,0,param2);
            
            in.close();
        }
        catch (IOException e) {
            throw new SQLException(e.toString());
        }
        setVal(param,String.valueOf(c));
    }
    
    public java.sql.ParameterMetaData getParameterMetaData() throws java.sql.SQLException {
        return null;
    }
    
    public void setTime(int param, java.sql.Time time, java.util.Calendar calendar) throws java.sql.SQLException {
        setVal(param,time.toString());
    }
    
    public void setBlob(int param, java.sql.Blob blob) throws java.sql.SQLException {
    }
    
    public java.sql.ResultSet executeQuery() throws java.sql.SQLException {
        res.unpack((NamingEnumeration) stmt.executeQuery(),stmt.getRetrieveDN(),this.stmt.getSqlStore().getFrom(),con.getBaseDN());
        
        this.rs = new LdapResultSet(con,this,res.getRows(),res.getFieldNames(),((JdbcLdapSelect) stmt).getBaseContext(),res.getFieldTypes());
        return rs;
    }
    
    public void setCharacterStream(int param, java.io.Reader reader, int param2) throws java.sql.SQLException {
        char[] c = new char[param2];
        try {
            
            BufferedReader in = new BufferedReader(reader);
            in.read(c,0,param2);
            
            in.close();
        }
        catch (IOException e) {
            throw new SQLException(e.toString());
        }
        setVal(param,String.valueOf(c));
    }
    
    public java.sql.ResultSetMetaData getMetaData() throws java.sql.SQLException {
        return rs.getMetaData();
    }
    
    public void setTimestamp(int param, java.sql.Timestamp timestamp, java.util.Calendar calendar) throws java.sql.SQLException {
        setVal(param,timestamp.toString());
    }
    
    public void setObject(int param, java.lang.Object obj, int param2, int param3) throws java.sql.SQLException {
        setVal(param,obj.toString());
    }
    
    public void setObject(int param, java.lang.Object obj, int param2) throws java.sql.SQLException {
        setVal(param,obj.toString());
    }
    
    public void setObject(int param, java.lang.Object obj) throws java.sql.SQLException {
        setVal(param,obj.toString());
    }
    
    public void setRef(int param, java.sql.Ref ref) throws java.sql.SQLException {
    }
    
    public void setArray(int param, java.sql.Array array) throws java.sql.SQLException {
    }
    
    public void setTimestamp(int param, java.sql.Timestamp timestamp) throws java.sql.SQLException {
        setVal(param,timestamp.toString());
    }
    
    public void setInt(int param, int param1) throws java.sql.SQLException {
        setVal(param,Integer.toString(param1));
    }
    
    public void setBytes(int param, byte[] values) throws java.sql.SQLException {
        this.setCharacterStream(param,new InputStreamReader(new ByteArrayInputStream(values)),values.length);
    }
    
    public void setShort(int param, short param1) throws java.sql.SQLException {
        setVal(param,Short.toString(param1));
    }
    
    public void setFloat(int param, float param1) throws java.sql.SQLException {
        setVal(param,Float.toString(param1));
    }
    
    public void setBoolean(int param, boolean param1) throws java.sql.SQLException {
        setVal(param,Boolean.toString(param1));
    }
    
    public void setDate(int param, java.sql.Date date, java.util.Calendar calendar) throws java.sql.SQLException {
        setVal(param,date.toString());
    }
    
    public int executeUpdate() throws java.sql.SQLException {
        return ((Integer) stmt.executeUpdate()).intValue();
    }
    
    public void setString(int param, java.lang.String str) throws java.sql.SQLException {
        setVal(param,str);
    }
    
    public void setClob(int param, java.sql.Clob clob) throws java.sql.SQLException {
    }
    
    public void addBatch() throws java.sql.SQLException {
        statements.add(stmt);
        loadSQL(stmt.getSqlStore());
    }
    
    public void clearParameters() throws java.sql.SQLException {
    }
    
    public void setNull(int param, int param1) throws java.sql.SQLException {
        setVal(param,null);
    }
    
    public void setNull(int param, int param1, java.lang.String str) throws java.sql.SQLException {
        setVal(param,null);
    }
    
}
