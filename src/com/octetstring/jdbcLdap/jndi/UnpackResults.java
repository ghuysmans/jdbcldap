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
 * UnpackResults.java
 *
 * Created on March 14, 2002, 10:06 AM
 */

package com.octetstring.jdbcLdap.jndi;

import javax.naming.*;
import javax.naming.directory.*;
import com.octetstring.jdbcLdap.jndi.JndiLdapConnection;
import java.util.*;
import java.sql.*;

/**
 *Takes a JNDI Naming Enumeration and places it into a LinkedList of
 *HasMap's for processing
 *@author Marc Boorshtein, OctetString
 */
public class UnpackResults {
	static final String HEX_COMMA="\\2C";
	static final String HEX_PLUS="\\2B";
	static final String HEX_DBL_QUOTE="\\22";
	static final String HEX_BACK_SLASH="\\5C";
	static final String HEX_LESS="\\3C";
	static final String HEX_MORE="\\3E";
	static final String HEX_SEMI_COLON="\\3B";
	static final HashMap HEX_TO_STRING;
	
	
	
	/** DN attribute name */
	static final String DN_ATT = "DN";

	/** The Connection to the LDAP server */
	JndiLdapConnection con;

	/** List of Field Names */
	HashMap names;

	/** List of rows */
	LinkedList rows;

	static {
		HEX_TO_STRING = new HashMap();
		HEX_TO_STRING.put(HEX_COMMA,"\\,");
		HEX_TO_STRING.put(HEX_PLUS,"\\+");
		HEX_TO_STRING.put(HEX_DBL_QUOTE,"\\\"");
		HEX_TO_STRING.put(HEX_BACK_SLASH,"\\\\");
		HEX_TO_STRING.put(HEX_LESS,"\\<");
		HEX_TO_STRING.put(HEX_MORE,"\\>");
		HEX_TO_STRING.put(HEX_SEMI_COLON,"\\;");
		
	}
	
	/** Creates new UnpackResults */
	public UnpackResults(JndiLdapConnection con) {
		this.con = con;
		names = new HashMap();
		rows = new LinkedList();
	}

	/** Returns the field names of the result */
	public LinkedList getFieldNames() {
		LinkedList fields = new LinkedList();
		Iterator it = names.keySet().iterator();
		FieldStore f;
		int i;
		StringBuffer buf = new StringBuffer();
		while (it.hasNext()) {
			f = (FieldStore) names.get(it.next());
			if (f.numVals > 0) {
				for (i = 0; i < f.numVals; i++) {
					buf.setLength(0);
					fields.add(
						buf.append(f.name).append('_').append(i).toString());
				}
			} else {
				fields.add(f.name);
			}
		}

		return fields;
	}

	/** Returns the types for the query */
	public int[] getFieldTypes() {
		LinkedList fields = new LinkedList();
		Iterator it = names.keySet().iterator();
		FieldStore f;
		int i;
		StringBuffer buf = new StringBuffer();
		int count = 0;
		while (it.hasNext()) {
			f = (FieldStore) names.get(it.next());
			if (f.numVals > 0) {
				for (i = 0; i < f.numVals; i++) {
					buf.setLength(0);
					fields.add(new Integer(f.getType()));
					count++;
				}
			} else {
				fields.add(new Integer(f.getType()));
				count++;
			}
		}

		int[] types = new int[count];
		for (i = 0; i < count; i++) {
			types[i] = ((Integer) fields.get(i)).intValue();
		}

		return types;
	}

	/** Returns the results of the search */
	public LinkedList getRows() {
		return rows;
	}

	public  void unpack(
		NamingEnumeration results,
		boolean dn,
		String fromContext,
		String baseContext)
		throws SQLException {
		try {
			LinkedList tmprows;
			LinkedList expRows = null;
			SearchResult res;
			Attributes atts;
			NamingEnumeration enumAtts;
			Enumeration vals;
			Attribute att;
			String attrid;
			String val;
			//String dn;
			FieldStore field;
			StringBuffer buff = new StringBuffer();
			String base;
			names.clear();
			rows.clear();
			int currNumVals;
			HashMap row;

			Iterator it;

			buff.setLength(0);
			if (fromContext != null && fromContext.length() != 0)
				buff.append(',').append(fromContext);
			if (baseContext != null && baseContext.length() != 0)
				buff.append(',').append(baseContext);

			base = buff.toString();

			while (results.hasMore()) {
				res = (SearchResult) results.next();
				row = new HashMap();
				if (con.isExpandRow()) {
					expRows = new LinkedList();
					expRows.add(row);
				}
				atts = (Attributes) res.getAttributes();
				enumAtts = atts.getAll();

				if (dn) {
					field = (FieldStore) names.get(DN_ATT);
					if (field == null) {
						field = new FieldStore();
						field.name = DN_ATT;
						names.put(field.name, field);
					}
					buff.setLength(0);
					//TODO, need to be able to handle unicode strings....
					/*try {
						System.out.println("in unpack dn : " + cleanDn(res.getName()));
					}
					catch (Exception e) {
						e.printStackTrace();
					}*/
					
					buff.append(cleanDn(res.getName())).append(base);
					if (buff.length() > 0 && buff.charAt(0) == ',')
						buff.deleteCharAt(0);
					// System.out.println("Unpack: dn="+buff);

					row.put(DN_ATT, buff.toString());

				}

				while (enumAtts.hasMore()) {
					att = (Attribute) enumAtts.next();
					if (att.size() == 0) continue;
					
					attrid = att.getID();
					//              System.out.println("Unpack: att="+att+", attrid="+attrid);

					field = (FieldStore) names.get(attrid);
					if (field == null) {
						field = new FieldStore();
						field.name = attrid;
						names.put(field.name, field);
					}

					if (att.size() > 1) {

						if (con.getConcatAtts()) {
							buff.setLength(0);
							field.numVals = 0;
							
							for (vals = att.getAll();
								vals.hasMoreElements();
								) {
								val = vals.nextElement().toString();
								field.determineType(val);
								buff.append('[').append(val).append(']');
							}

							row.put(field.name, buff.toString());
						} 
						else if (con.isExpandRow()){
							tmprows =new LinkedList();
							for (vals = att.getAll();vals.hasMoreElements();) {
								
								val = vals.nextElement().toString();
								field.determineType(val);
								it = expRows.iterator();
								
								while (it.hasNext()) {
									row = (HashMap) it.next();
									row = (HashMap) row.clone();
									
									row.put(field.name,val);
									tmprows.add(row);
								}
								
								
							}
							expRows = tmprows;
						}
						else {
							currNumVals = 0;

							for (vals = att.getAll();
								vals.hasMoreElements();
								) {
								buff.setLength(0);
								val = vals.nextElement().toString();
								field.determineType(val);
								row.put(
									buff
										.append(field.name)
										.append('_')
										.append(currNumVals)
										.toString(),
									val);
								currNumVals++;
							}

							field.numVals =
								(currNumVals > field.numVals)
									? currNumVals
									: field.numVals;
						}
					} 
					else if (con.isExpandRow()) {
						val = att.get(0).toString();
						it = expRows.iterator();
						while (it.hasNext()) {
							field.determineType(val);
							row = (HashMap) it.next();
							row.put(field.name, val);
						}
					}
					else {
						val = att.get(0).toString();
						field.determineType(val);
						row.put(field.name, val);
					}

				}

				enumAtts.close();

				if (con.isExpandRow() ) {
					rows.addAll(expRows);	
				}
				else {
					rows.add(row);
				}

			}
			results.close();
		} catch (NamingException e) {
			throw new SQLNamingException(e);
		}
	}
	
	public String cleanDn(String dn) {
		StringBuffer buf = new StringBuffer(dn);
		int begin,end;
		begin = buf.indexOf("\\");
		String val;
		while (begin != -1) {
			val = (String) UnpackResults.HEX_TO_STRING.get(buf.substring(begin,begin+3));
			if (val != null) {
				buf.replace(begin,begin+3,val);
			}
			begin = begin = buf.indexOf("\\",begin + 1);
		}
		
		return buf.toString();
	}
	
	

}
