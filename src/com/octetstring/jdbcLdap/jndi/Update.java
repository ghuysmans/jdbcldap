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
 * Update.java
 *
 * Created on May 24, 2002, 12:56 PM
 */

package com.octetstring.jdbcLdap.jndi;
import javax.naming.*;
import javax.naming.directory.*;
import com.octetstring.jdbcLdap.sql.statements.*;
import com.octetstring.jdbcLdap.sql.*;
import java.sql.*;
/**
 *Contains logic for updating records in the directory
 *@author Marc Boorshtein, OctetString
 */
public class Update {
    RetrieveResults res = new RetrieveResults();
    SearchResult seres;
    
    public int doUpdate(JdbcLdapUpdate update) throws SQLException {
        DirContext con = update.getCon().getContext();
        StringBuffer buf = new StringBuffer();
        SqlStore store = update.getSqlStore();
        int count = 0;
        ModificationItem[] mods;
        String[] fields,vals;
        //build ModificationItem array
        mods = new ModificationItem[store.getFields().length];
        fields = store.getFields();
        vals = update.getVals();
        
        for (int i=0,m=mods.length;i<m;i++) {
            mods[i] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,new BasicAttribute(fields[i],vals[i]));
        }
        
        try {
            
            NamingEnumeration enum = res.searchUpIns(update);
            while (enum.hasMore()) {
                seres = (SearchResult) enum.next();
                buf.setLength(0);
                
                con.modifyAttributes(buf.append(seres.getName()).append(',').append(store.getDistinguishedName()).toString(),mods);
                count++;
                //System.out.println("count : " + count);
            }
            
            enum.close();
            //System.out.println("final count : " + count);
            return count;
        }
        catch (NamingException ne) {
            throw new SQLNamingException(ne);
        }
    }
    
}
