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
 * JdbcLdapDriver.java
 *
 * Created on March 9, 2002, 4:49 PM
 */

package com.octetstring.jdbcLdap.sql;

import java.sql.*;
import com.octetstring.jdbcLdap.jndi.JndiLdapConnection;
import java.util.*;
import java.util.logging.Logger;

/**
 * Establishes a JNDI connection to an LDAP store.  URLs are in the form :<br>
 *<b>jdbc:ldap://host[:port]/base dn[?property1:=value1&property2:=value2&property<i>n</i>:=value<i>n</i>]</b><br
 *for any properties the driver accepts.  <b>NOTE</b>: Any extra properties required by jndi's factory may be passed as a property
 *@author Marc Boorshtein, OctetString
 */
public class JdbcLdapDriver implements java.sql.Driver {
    /** Identifies the URL prefix */
    public static final String URL_ID = "jdbc:ldap";
    
    /** Identifies the URL prefix for DSMLv2 Connections */
    public static final String DSML_URL_ID = "jdbc:dsml";
    
    /** Identifies the URL prefix for SPML Connections */
    public static final String SPML_URL_ID = "jdbc:spml";
    
    /**Major Version of driver */
    public static final int MAJOR_VERSION = 0;
    
    /**Minor Version of driver */
    public static final int MINOR_VERSION = 99;
    
    /**Is JDBC Type IV Driver? */
    public static final boolean JDBC_IV = false;
    public static final String PARAM_DELIM = ":="; 
    
    static {
       try {
           
        DriverManager.registerDriver(new com.octetstring.jdbcLdap.sql.JdbcLdapDriver());
       }
       catch (SQLException e) {
           
        e.printStackTrace(System.out);
       }
        
    }
    
    /** Creates new JdbcLdapDriver */
    public JdbcLdapDriver() throws SQLException {
        DriverManager.registerDriver(this);
    }
    
    
    /**
     *Accepts URLs in the form ldap://host:port/basedn
     */
    public boolean acceptsURL(java.lang.String str) throws java.sql.SQLException {
        return str.substring(0,9).equalsIgnoreCase(URL_ID) || str.substring(0,9).equalsIgnoreCase(DSML_URL_ID) || str.substring(0,9).equalsIgnoreCase(SPML_URL_ID);
    }
    
    public java.sql.Connection connect(java.lang.String str, java.util.Properties properties) throws java.sql.SQLException {
	    	if (!acceptsURL(str))
	    	{
	    		return null;
	    	}
    		String props;
        StringTokenizer toker;
        String prop, val,token;
        int seperator = str.indexOf("?");
        if (seperator != -1) {
            props = str.substring(seperator + 1);
            
            toker = new StringTokenizer(props,"&",false);
            while (toker.hasMoreTokens()) {
                token = toker.nextToken();
                prop = token.substring(0,token.indexOf(PARAM_DELIM));
                val = token.substring(token.indexOf(PARAM_DELIM) + PARAM_DELIM.length());
                properties.setProperty(prop,val);
                
            }
            
            return new JndiLdapConnection(str.substring(0,seperator),properties);
            
        }
        else {
            return new JndiLdapConnection(str,properties);
        }
    }
    
    public int getMajorVersion() {
        return MAJOR_VERSION;
    }
    
    public int getMinorVersion() {
        return MINOR_VERSION;
    }
    
    public java.sql.DriverPropertyInfo[] getPropertyInfo(java.lang.String str, java.util.Properties properties) throws java.sql.SQLException {
        DriverPropertyInfo[] props = new DriverPropertyInfo[5];
        props[0] = new DriverPropertyInfo("user","Security Principal");
        props[1] = new DriverPropertyInfo("password","Security Credentials");
        props[2] = new DriverPropertyInfo("java.naming.security.authentication","Authentication type - simple, none or SASL type");
        props[3] = new DriverPropertyInfo("SEARCH_SCOPE","The Search scope");
        props[4] = new DriverPropertyInfo("CACHE_STATEMENT","true or false, wether or not statements should be cached");
        
        return props;
    }
    
    public boolean jdbcCompliant() {
        return JDBC_IV;
    }


    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        // TODO Auto-generated method stub
        return null;
    }
    
}
