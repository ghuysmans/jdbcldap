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
 * SQLNamingException.java
 *
 * Created on March 10, 2002, 3:35 PM
 */

package com.octetstring.jdbcLdap.jndi;

import java.sql.*;
import javax.naming.*;
import com.novell.ldap.util.*;
import com.novell.ldap.*;

import java.io.FileNotFoundException;
import java.net.*;

/**
 *Translates a JndiException to a SQLException
 *@author Marc Boorshtein, OctetString
 * @version 
 */

public class SQLNamingException extends java.sql.SQLException {
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	
    Exception e;
    
    public String toString() {
    	if (e instanceof LDAPException) {
    		LDAPException le = (LDAPException) e;
    		return le.toString() + " -- " + le.getLDAPErrorMessage();
    	} else {
    		return e.toString();
    	}
	}
    
    /** Creates new SQLNamingExcepton */
    public SQLNamingException(NamingException e) {
        this.e = e;
    }
    
    public SQLNamingException(LDAPException e) {
    	this.e = e;
    }
    
    public SQLNamingException(MalformedURLException e) {
    	this.e = e;
    }

    /**
	 * @param e1
	 */
	public SQLNamingException(Exception e1) {
		
		this.e = e1;
	}

	protected java.lang.Object clone() throws java.lang.CloneNotSupportedException {
        return super.clone();
    }
    
    public boolean equals(java.lang.Object obj) {
        return super.equals(obj);
    }
    
    
    
    
    
    
    
    public java.lang.Throwable fillInStackTrace() {
        return super.fillInStackTrace();
    }
    
    public java.lang.Throwable getCause() {
        return e.getCause();
    }
    
    public java.lang.String getLocalizedMessage() {
        return e.getLocalizedMessage();
    }
    
    public java.lang.String getMessage() {
        return e.getMessage();
    }
    
    public java.lang.StackTraceElement[] getStackTrace() {
        return e.getStackTrace();
    }
    
    public java.lang.Throwable initCause(java.lang.Throwable throwable) {
        return e.initCause(throwable);
    }
    
    public void printStackTrace() {
        e.printStackTrace();
    }
    
    public void printStackTrace(java.io.PrintStream printStream) {
        e.printStackTrace(printStream);
    }
    
    public void printStackTrace(java.io.PrintWriter printWriter) {
        e.printStackTrace(printWriter);
    }
    
    public void setStackTrace(java.lang.StackTraceElement[] stackTraceElement) {
        e.setStackTrace(stackTraceElement);
    }
    
    public int getErrorCode() {
    		if (e instanceof LDAPException) {
    			return ((LDAPException) e).getResultCode();
    		} else {
    			return 0;
    		}
    }
    
    public java.sql.SQLException getNextException() {
        return this;
    }
    
    public java.lang.String getSQLState() {
        return "ERROR";
    }
    
    public void setNextException(java.sql.SQLException sQLException) {
        
    }
    
    public Exception getNamingException() {
    	return this.e;
    }
    
}
