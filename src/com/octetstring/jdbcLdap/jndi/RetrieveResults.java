/* **************************************************************************
 *
 * Copyright (C) 2002 Octet String, Inc. All Rights Reserved.
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
 * RetrieveResults.java
 *
 * Created on March 13, 2002, 5:50 PM
 */

package com.octetstring.jdbcLdap.jndi;

import javax.naming.*;
import javax.naming.directory.*;
import com.octetstring.jdbcLdap.sql.statements.*;
import java.sql.*;
import java.util.*;
/**
 *Retrieves the results from a qeury
 *@author Marc Boorshtein, OctetString
 */
public class RetrieveResults {
    
    /** Creates new RetrieveResults */
    public RetrieveResults() {
    }
    
    public NamingEnumeration search(JdbcLdapSelect select) throws SQLException {
        try {
            DirContext con = select.getContext();
            SearchControls ctls = new SearchControls();
            int num;
            
           //System.out.println("select.getSearchAttributes().length : " + select.getSearchAttributes().length);
            
            String[] fields = select.getSearchAttributes();
            fields = fields != null ? fields : new String[0];
            
            
			String[] searchAttribs;
			
            if (fields.length == 1 && fields[0].equalsIgnoreCase("dn")) {
            	searchAttribs = new String[] {"1.1"};	
            	
            }
            else {
				ArrayList ar = new ArrayList();


				for (int i=0, m=fields.length;i<m;i++) {
					if (! fields[i].equalsIgnoreCase("dn")) {
						ar.add(fields[i]);
					}
				}

				searchAttribs = new String[ar.size()];

				System.arraycopy(ar.toArray(),0,searchAttribs,0,ar.size());	
            }
            
            
            
             
            
            
            ctls.setReturningAttributes(searchAttribs.length != 0 ? searchAttribs : null);
            ctls.setSearchScope(select.getSearchScope());
            
            num = select.getMaxRecords();
            if (num != -1) ctls.setCountLimit(num);
            
            num = select.getTimeOut();
            if (num != -1) ctls.setTimeLimit(num);
            
            String filter = select.getFilterWithParams();
            
            return con.search(select.getBaseContext(), filter ,ctls);
        }
        catch (NamingException e) {
            throw new SQLNamingException(e);
        }
    }
    
    public NamingEnumeration searchUpIns(JdbcLdapSqlAbs sql) throws SQLException {
        try {
            DirContext con = sql.getContext();
            SearchControls ctls = new SearchControls();
            int num;
            
            ctls.setSearchScope(sql.getSearchScope());
            
            num = sql.getTimeOut();
            if (num != -1) ctls.setTimeLimit(num);
            
            String filter = sql.getFilterWithParams();
            
	    
            return con.search(sql.getBaseContext(), filter ,ctls);
        }
        catch (NamingException e) {
            throw new SQLNamingException(e);
        }
    }
    
}
