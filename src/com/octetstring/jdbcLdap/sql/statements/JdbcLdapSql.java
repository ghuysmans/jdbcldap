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
 * JdbcLdapSql.java
 *
 * Created on March 13, 2002, 12:56 PM
 */

package com.octetstring.jdbcLdap.sql.statements;

import java.sql.*;
import com.octetstring.jdbcLdap.sql.*;
import com.octetstring.jdbcLdap.jndi.*;
/**
 *This interface is used to wrap the internals of a statement
 *@author Marc Boorshtein, OctetString
 */
public interface JdbcLdapSql {
    
    
    /** Creates new JdbcLdapSql using a connection and a SQL Statement*/
    public void init(JndiLdapConnection con, String SQL) throws SQLException;
    
    /** Creates new JdbcLdapSql using a connection, a SQL Statement and a cached SqlStore*/
    public void init(JndiLdapConnection con, String SQL,SqlStore sqlStore) throws SQLException;
    
    /** Executes the current statement and returns the results */
    public Object executeQuery() throws SQLException;
    public Object executeUpdate() throws SQLException;
    
    /**
     *Sets the value at position
     *@param pos Position to set
     *@param val Value to set
     */
    public void setValue(int pos,String value) throws SQLException;

    /**
     *Used to retrieve the SqlStore for caching
     *@return The statments SqlStore object
     */
    public  SqlStore getSqlStore();
    
    /**
     *Retrieves if the DN is being returned
     */
    public  boolean getRetrieveDN();
    
    
    
    /**
     *Returns the serach filter with all arguments added
     */
    public String getFilterWithParams() ;
    
    /**
     *Determines if statement is update or select
     *@return true if is an update
     */
    public boolean isUpdate();
    
}
