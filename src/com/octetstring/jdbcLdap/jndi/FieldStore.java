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
 * FieldStore.java
 *
 * Created on March 14, 2002, 10:20 AM
 */

package com.octetstring.jdbcLdap.jndi;

import java.sql.Types;
/**
 *Stores Meta information about a field
 *@author Marc Boorshtein, OctetString
 */

public class FieldStore {
    static final String DECIMAL = ".";
    static final String DASH = "-";
    static final String COLON = ":";
    String name;
    int numVals;
    int type;
    boolean determined;
    
    /** Creates new FieldStore */
    public FieldStore() {
        numVals = 0;
        determined = false;
    }
    
    public FieldStore(String name, int vals) {
        this.name = name;
        this.numVals = vals;
        determined = false;
    }
    
    /**
     *Returns the name of the field
     */
    public String getName() {
        return name;
    }
    
    /**
     *Returns the number of values for this field
     */
    public int getNumVals() {
        return numVals;
    }
    
    /**
     *Retrieves data type
     *@return SQL type
     */
    public int getType() {
        if (determined) {
            return type;
        }
        else {
            return java.sql.Types.VARCHAR;
        }
    }
    
    /**
     *Determines the type of data if it has not been determined
     *@param val Value to try and determine
     */
    public void determineType(String val) {
        long ltmp;
        int itmp;
        int pos1,pos2;
        
        
        //if it is already determined or it is empty, skip
        if (determined || (val == null || val.length() == 0)) return;
        
        //firt dtermine if first char is numeric
        if (Character.isDigit(val.charAt(0))) {
            
            mightNumeric(val);
        }
        else {
            //not numeric
            type = Types.VARCHAR;
            determined = true;
        }
        
        
    }
    
    protected void mightNumeric(String val) {
            //MIGHT be numeric OR a date/time
            int pos1 = val.indexOf(DASH);
            int pos2 = val.indexOf(COLON);
            long ltmp;
            
            
            //is this a date or time?
            if (pos1 != -1 || pos2 != -1) {
                mightDateTime(val,pos1,pos2);
            }
            //first see if there is a "."
            else if(val.indexOf(DECIMAL) != -1) {
                //try making it into a double
                try {
                    Double.valueOf(val);
                    type = Types.DOUBLE;
                    determined = true;
                }
                catch (Exception e) {
                    //not a decimal, must be a string
                    type = Types.VARCHAR;
                    determined = true;
                }
            }
            else {
                //might be an integer
                try {
                    //we'll try to make a long
                    ltmp = Long.parseLong(val);
                    type = Types.INTEGER;
                    determined = true;
                }
                catch (Exception e) {
                    //string
                    type = Types.VARCHAR;
                    determined = true;
                }
            }
    }
    
    protected void mightDateTime(String val,int pos1, int pos2) {
        //lets see if its a timestamp or a datetime
        
        
            if (pos1 != -1 && pos2 != -1 && pos1 < pos2) {
                //could be a date time
                try {
                    java.sql.Timestamp.valueOf(val);
                    type = Types.TIMESTAMP;
                    determined = true;
                }
                catch (IllegalArgumentException e) {
                    //not a decimal, must be a string
                    type = Types.VARCHAR;
                    determined = true;
                }
            }
            else {
                //now we need to determine if it might be a date or a time
                if (pos1 != -1 || pos2 != -1) {
                    if (pos1 == -1) {
                        //might be a time
                        try {
                            java.sql.Time.valueOf(val);
                            type = Types.TIME;
                            determined = true;
                        }
                        catch (IllegalArgumentException e) {
                            //not a decimal, must be a string
                            type = Types.VARCHAR;
                            determined = true;
                        }
                    }
                    else {
                        //might be a date
                        try {
                            java.sql.Date.valueOf(val);
                            type = Types.DATE;
                            determined = true;
                        }
                        catch (IllegalArgumentException e) {
                            //not a decimal, must be a string
                            type = Types.VARCHAR;
                            determined = true;
                        }
                    }
                }
                else {
                    type = Types.VARCHAR;
                    determined = true;
                }
                
            }
    }

}