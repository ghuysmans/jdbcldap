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
package com.octetstring.jdbcLdap.util;

import java.util.*;
import java.sql.*;
import java.io.*;

/**
 * @author mlb
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class LDIF {

	/** The DN */
	public static final String DN = "dn";

	/** The seperator of the attribs and val */
	public static final String SEP = ": ";
	/** The seperator of the attribs and binary val */
	public static final String BIN_SEP = ":: ";

	/** Stores The ldif */
	LinkedList ldif;

	/** Debug mode */
	boolean debug;

	public LDIF() {
		this.ldif = new LinkedList();
	}

	/**
	 * Loads LDIF from a string
	 * @param ldif The ldif to load
	 */
	public LDIF(String ldif, boolean debug) throws Exception {
		this.debug = debug;
		StringReader r = new StringReader(ldif);
		BufferedReader in = new BufferedReader(r);
		String attr=null, val=null;

		String line;

		this.ldif = new LinkedList();

		HashMap entry = null;
		LinkedList attrib;

		String sep;

		
		while ((line = in.readLine()) != null) {
			//don't do anything if there is a blank line
			if (line.trim().length() != 0) {
				//split the line 

				try {
					attr = line.substring(0, line.indexOf(SEP)).trim();
					val = line.substring(line.indexOf(SEP) + SEP.length());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace(System.out);
					System.out.println("Error on line : " + line);
					System.exit(1);
				}

				//if this is a dn, we need to create a new entry
				if (attr.equalsIgnoreCase(DN)) {
					entry = new HashMap();
					this.ldif.add(new Entry(val, entry));

				} else {
					attrib = (LinkedList) entry.get(attr);

					//if it doesn't exist, add it
					if (attrib == null) {
						attrib = new LinkedList();
						entry.put(attr, attrib);
					}

					attrib.add(val);
				}

			}
		}
	}

	/**
	 * Loads and ldif from a result set.  each row is an entry
	 * @param rs the ResultSet
	 */
	public LDIF(ResultSet rs, String dnField, boolean debug) throws Exception {
		this.debug = debug;
		String dn;
		String attr, val;
		StringTokenizer tok;

		this.ldif = new LinkedList();

		HashMap entry = null;
		LinkedList attrib;

		//each row is an entry
		while (rs.next()) {
			//create an entry
			entry = new HashMap();

			dn = rs.getString(dnField);
			ldif.add(new Entry(dn, entry));

			ResultSetMetaData rsmd = rs.getMetaData();
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				//get the attribute and value
				attr = rsmd.getColumnName(i);

				//don't want to check the dn field
				val = rs.getString(attr);
				if (!attr.equals(dnField) && val.trim().length() != 0) {
					

					//create the attribute
					attrib = new LinkedList();
					entry.put(attr, attrib);

					//if the entry is a concatinated field, primarily for the jdbcLdapDrivers
					if (val.charAt(0) == '['
						&& val.charAt(val.length() - 1) == ']') {
						tok =
							new StringTokenizer(
								val.substring(1, val.length() - 1),
								"][",
								false);
						while (tok.hasMoreTokens()) {
							attrib.add(tok.nextToken());
						}

					} else {
						attrib.add(val);
					}
				}
			}
		}
	}

	public boolean compareLdif(LDIF o, LDIF diffLdif) {
		if (!(o instanceof LDIF)) {
			return false;
		}

		LinkedList ldif1, ldif2, diff;
		String tmpval;
		ldif1 = (LinkedList) ldif.clone();
		ldif2 = (LinkedList) ((LDIF) o).ldif.clone();
		diff = new LinkedList();

		boolean match = true;
		boolean equals = true;

		if (debug) {

			System.out.println("ldif 1: " + ldif1.size());
			System.out.println("ldif 2: " + ldif2.size());
		}

		Entry ent, ent2 = null;
		String dn = null;
		Iterator entries2 = null;
		Iterator entries = ldif1.iterator();
		boolean found = false;
		LinkedList ts1, ts2;
		Iterator attribsKeys;
		Iterator tsi, it,itat;
		HashMap attribs, attribs2;
		String val;
		String attrib1, attrib2, val2;
		while (entries.hasNext()) {
			ent = (Entry) entries.next();
			dn = ent.getDn();
			//System.out.println("In DN :" + dn);
			entries2 = ldif2.iterator();
			found = false;
			match = true;
			while (entries2.hasNext()) {
				ent2 = (Entry) entries2.next();

				if (ent2.getDn().equalsIgnoreCase(dn)) {
					found = true;
					//System.out.println("removing: " + ent2.getDn() + "--" + ldif2.size());
					ldif2.remove(ent2);
					//System.out.println("still has : " + ldif2.contains(ent2) + " -- " + ldif2.size());

					break;
				}
			}

			if (!found) {
				System.out.println("DN " + dn + " not found");
				match = false;
			}

			attribs = ent.getAtts();
			attribs2 = null;
			if (found) attribs2 = (HashMap) ent2.getAtts().clone();

			attribsKeys = attribs.keySet().iterator();
			while (found && attribsKeys.hasNext()) {
				attrib1 = (String) attribsKeys.next();
				if (debug)
					System.out.println("Looking for : " + attrib1);
				
				
				ts1 = (LinkedList) attribs.get(attrib1);
				itat = attribs2.keySet().iterator();
				ts2 = null;
				
				while (itat.hasNext()) {
						tmpval = (String) itat.next();
						if (tmpval.equalsIgnoreCase(attrib1)) {
							ts2 = ((LinkedList) attribs2.remove(tmpval));
							ts2 = (LinkedList) ts2.clone();
							break;		
						}
				}
				
				if (ts2 == null) {
					match = false;
					System.out.println("FAILED : " + dn);
					break;
				}
				

				tsi = ts1.iterator();
				while (tsi.hasNext()) {
					val = (String) tsi.next();
					if (debug) {

						System.out.println("\tAttib : " + attrib1);
						System.out.println("\tVal : " + val);
						
						

					}
					found = false;
					it = ts2.iterator();
					while (it.hasNext()) {
						val2 = (String) it.next();
						if (val2.equalsIgnoreCase(val)) {
							found = true;
							it.remove();
							tsi.remove();
							break;
						}
					}
					if (debug) System.out.println("\tContains : " + found);
					if (!found) {
						System.out.println("FAILED : " + dn);
					
						match = false;
					}
				}

				if (debug) {
					System.out.println("ts2.size() : " + ts2.size());
					System.out.println("ts1.size() : " + ts1.size());
				}
				if (ts2.size() != 0) {
					System.out.println("FAILED : " + dn);
					match = false;
				}

			}

			if (debug)
				System.out.println("attribs2.size() : " + (attribs2 != null ? Integer.toString(attribs2.size()) : "null"));
			if (attribs2 == null || attribs2.size() != 0) {
				System.out.println("FAILED : " + dn);
				match = false;
			}

			if (!match) {
				System.out.println("FAILED : " + dn);
				equals = false;
				diff.add(ent);
			}

		}

		if (debug)
			System.out.println("ldif2.size() : " + ldif2.size());
		if (ldif2.size() != 0) {
			entries2 = ldif2.iterator();
			while (entries2.hasNext()) {
				ent2 = (Entry) entries2.next();
				System.out.println("FAILED : " + (dn != null ? dn : ""));
				diff.add(ent2);
			}
			equals = false;
		}

		if (!equals)
			diffLdif.ldif = diff;

		return equals;
		//return ((LDIF) o).ldif.equals(this.ldif);

	}

	public String toString() {
		StringBuffer ldif = new StringBuffer();

		String dn, attrib;

		Iterator entries = this.ldif.iterator();
		HashMap entry;

		Iterator atts;
		Iterator vals;

		LinkedList ts;

		Entry ent;

		while (entries.hasNext()) {
			ent = (Entry) entries.next();
			dn = ent.getDn();
			ldif.append("dn: ").append(dn).append('\n');
			entry = ent.getAtts();

			atts = entry.keySet().iterator();
			while (atts.hasNext()) {
				attrib = (String) atts.next();

				ts = (LinkedList) entry.get(attrib);
				vals = ts.iterator();
				while (vals.hasNext()) {

					ldif.append(attrib).append(SEP).append(vals.next()).append(
						'\n');
				}
			}

			ldif.append('\n');
		}

		return ldif.toString();
	}
}

class Compare implements Comparator {
	public int compare(Object v1, Object v2) {
		return ((String) v1).compareTo((String)v2);
	}
}

class Entry {
	String dn;
	HashMap atts;

	public Entry(String dn, HashMap atts) {
		this.dn = dn;
		this.atts = atts;

	}

	/**
	 * @return
	 */
	public HashMap getAtts() {
		return atts;
	}

	/**
	 * @return
	 */
	public String getDn() {
		return dn;
	}

	public boolean equals(Object o) {
		Entry e = (Entry) o;
		return e.getDn().equalsIgnoreCase(this.dn);
	}

}
