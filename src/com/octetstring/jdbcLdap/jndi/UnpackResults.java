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
 * UnpackResults.java
 *
 * Created on March 14, 2002, 10:06 AM
 */

package com.octetstring.jdbcLdap.jndi;

import javax.naming.*;
import javax.naming.directory.*;
import com.octetstring.jdbcLdap.jndi.JndiLdapConnection;
import java.util.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.*;
import com.novell.ldap.*;
import com.novell.ldap.util.*;
import com.novell.ldap.util.Base64;

/**
 *Takes a JNDI Naming Enumeration and places it into a ArrayList of
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
	ArrayList rows;
	LDAPMessageQueue queue;
	protected boolean dn;
	protected String fromContext;
	protected StringBuffer buff;
	protected LDAPEntry entry;
	
	ArrayList fieldNames;
	ArrayList fieldTypes;
	
	
	private boolean hasMoreEntries;
	private LDAPSearchResults searchResults;
	private HashMap revMap;

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
		rows = new ArrayList();
	}

	/** Returns the field names of the result */
	public ArrayList getFieldNames() {
		/*ArrayList fields = new ArrayList();
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

		return fields;*/
		return this.fieldNames;
	}

	/** Returns the types for the query */
	public ArrayList getFieldTypes() {
		/*ArrayList fields = new ArrayList();
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

		return types;*/
		return this.fieldTypes;
	}

	/** Returns the results of the search */
	public ArrayList getRows() {
		return rows;
	}

	public void unpackJldap(LDAPSearchResults res,boolean dn,String fromContext,String baseContext,HashMap revMap) throws SQLException {
		ArrayList tmprows;
		ArrayList expRows = null;
		
		
		this.queue = null;
		this.searchResults = res;
		
		this.revMap = revMap;
		
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
		String[] svals;
		LDAPEntry entry = null;
		Iterator it;
		byte[][] byteVals;
		
		buff.setLength(0);
		if (fromContext != null && fromContext.length() != 0)
			buff.append(',').append(fromContext);
		if (baseContext != null && baseContext.length() != 0)
			buff.append(',').append(baseContext);

		base = buff.toString();
		
		this.dn = dn;
		this.fromContext = fromContext;
		this.buff = buff;
		this.entry = entry;
		
		
		this.fieldNames = new ArrayList();
		
		this.fieldTypes = new ArrayList();
		
		//this.results = new ResultListener(this,this.currentThread,queue);
		this.hasMoreEntries = true;
		if (con.isPreFetch()) {
			int i=0;
			while (this.moveNext(i++));
		}
		
		
	}
	
	
	public void unpackJldap(LDAPMessageQueue queue,boolean dn,String fromContext,String baseContext,HashMap revMap) throws SQLException {
		ArrayList tmprows;
		ArrayList expRows = null;
		SearchResult res;
		
		this.revMap = revMap;
		
		this.queue = queue;
		this.searchResults = null;
		
		
		
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
		String[] svals;
		LDAPEntry entry = null;
		Iterator it;
		byte[][] byteVals;
		
		buff.setLength(0);
		if (fromContext != null && fromContext.length() != 0)
			buff.append(',').append(fromContext);
		if (baseContext != null && baseContext.length() != 0)
			buff.append(',').append(baseContext);

		base = buff.toString();
		
		this.dn = dn;
		this.fromContext = fromContext;
		this.buff = buff;
		this.entry = entry;
		
		
		this.fieldNames = new ArrayList();
		
		this.fieldTypes = new ArrayList();
		
		//this.results = new ResultListener(this,this.currentThread,queue);
		this.hasMoreEntries = true;
		if (con.isPreFetch()) {
			int i=0;
			while (this.moveNext(i++));
		}
		
		
	}
	
	
	/**
	 * @param results
	 * @param dn
	 * @param fromContext
	 * @param buff
	 * @param entry
	 * @return
	 * @throws SQLNamingException
	 */
	protected LDAPEntry extractEntry(boolean dn, String fromContext, StringBuffer buff, LDAPEntry entry) throws SQLNamingException {
		ArrayList tmprows;
		ArrayList expRows = null;
		String val;
		FieldStore field;
		int currNumVals;
		HashMap row;
		String[] svals;
		Iterator it;
		byte[][] byteVals;
		
		
		
		
		//System.out.println("entry : " + entry);
		
		LDAPAttributeSet atts = entry.getAttributeSet();
		
		row = new HashMap();
		if (con.isExpandRow()) {
			expRows = new ArrayList();
			expRows.add(row);
		}
		
		if (dn) {
			field = (FieldStore) names.get(DN_ATT);
			if (field == null) {
				field = new FieldStore();
				field.name = DN_ATT;
				names.put(field.name, field);
				this.fieldNames.add(DN_ATT);
				this.fieldTypes.add(new Integer(field.type));
			}
			buff.setLength(0);
			//TODO, need to be able to handle unicode strings....
			/*try {
			 System.out.println("in unpack dn : " + cleanDn(res.getName()));
			 }
			 catch (Exception e) {
			 e.printStackTrace();
			 }*/
			
			
			// System.out.println("Unpack: dn="+buff);

			
			
			row.put(DN_ATT, LDAPDN.normalize(entry.getDN()));
		}
		
		//TODO figure out what the hell is going on here
		//it = atts.getAttributeNames().iterator();
		Object[] attribArray = atts.toArray();
		for (int j=0,n=attribArray.length;j<n;j++) {

			//String attribName = (String) it.next();
			//System.out.println(atts);
			//System.out.println("attribname : " + attribName);
			LDAPAttribute attrib = (LDAPAttribute) attribArray[j]; //atts.getAttribute(attribName);
			//System.out.println("working with : " + attrib);
			field = (FieldStore) names.get(this.getFieldName(attrib.getName()));
			boolean existed = true;
			if (field == null) {
				field = new FieldStore();
				field.name = this.getFieldName(attrib.getName());
				names.put(field.name, field);
				existed = false;
			}
			
			byte[] bval = attrib.getByteValue();
			if (bval == null) {
				bval = new byte[0];
			}
			if (Base64.isLDIFSafe(bval)) {
				svals = attrib.getStringValueArray();
			} else {
				byteVals = attrib.getByteValueArray();
				svals  = new String[byteVals.length];
				for (int i=0,m=byteVals.length;i<m;i++) {
					svals[i] = Base64.encode(byteVals[i]);
				}
				
			}
			
			if (svals.length <= 1) {
				if (con.isExpandRow()) {
					val = (svals.length != 0) ? svals[0] : "";
					it = expRows.iterator();
					while (it.hasNext()) {
						field.determineType(val);
						row = (HashMap) it.next();
						row.put(field.name, val);
					}
					
					if (! existed) {
						this.fieldNames.add(field.name);
						this.fieldTypes.add(new Integer(field.type));
					}
				}
				else {
					val = svals[0];
					field.determineType(val);
					row.put(field.name, val);
					if (! existed) {
						this.fieldNames.add(field.name);
						this.fieldTypes.add(new Integer(field.type));
					}
				}
			}
			else {
				if (con.getConcatAtts()) {
					buff.setLength(0);
					field.numVals = 0;
					
					for (int i=0,m=svals.length;i<m;i++) {
						val = svals[i];
						field.determineType(val);
						buff.append('[').append(val).append(']');
					}

					row.put(field.name, buff.toString());
					if (! existed) {
						this.fieldNames.add(field.name);
						this.fieldTypes.add(new Integer(field.type));
					}
				}
				else if (con.isExpandRow()){

					tmprows =new ArrayList();
					
					for (int i=0,m=svals.length;i<m;i++) {
						
						val = svals[i];
						field.determineType(val);
						it = expRows.iterator();
						
						while (it.hasNext()) {
							row = (HashMap) it.next();
							row = (HashMap) row.clone();
							
							row.put(field.name,val);
							tmprows.add(row);
						}
						
						
					}
					
					if (! existed) {
						this.fieldNames.add(field.name);
						this.fieldTypes.add(new Integer(field.type));
					}
					
					expRows = tmprows;
				}
				else {
					currNumVals = 0;
					int low = field.numVals;
					for (int i=0,m=svals.length;i<m;i++) {
						buff.setLength(0);
						val = svals[i];
						field.determineType(val);
						row.put(
								buff
								.append(field.name)
								.append('_')
								.append(currNumVals)
								.toString(),
								val);
						currNumVals++;
						
						String fieldName = field.name + "_" + Integer.toString(currNumVals - 1);
						
						if (currNumVals >= low && ! this.fieldNames.contains(fieldName)) {
							this.fieldNames.add(fieldName);
							this.fieldTypes.add(new Integer(field.type));
						}
						
						
					}

					field.numVals =
						(currNumVals > field.numVals)
					? currNumVals
					: field.numVals;
				}
			}	
		}
		
		if (con.isExpandRow() ) {
			rows.addAll(expRows);	
		}
		else {
			rows.add(row);
		}
		return entry;
	}

	/**
	 * @param results
	 * @param fromContext
	 * @param entry
	 * @return
	 * @throws SQLNamingException
	 */
	private LDAPEntry getEntry(LDAPSearchResults results, String fromContext, LDAPEntry entry) throws SQLNamingException {
		try {
			entry = results.next();
		} 
		catch (LDAPReferralException ref) {
			//for now, we will simply create an entry based on the referral
			
			String refName = "cn=Referral[" + ref.getReferrals()[0] + "]";
			if (entry == null) {
				
				if (con.baseDN != null && con.baseDN.trim().length() >= 0) {
					refName += "," + fromContext;
				}
			}
			else {
				String[] parts = LDAPDN.explodeDN(entry.getDN(),false);
				for (int i=1,m=parts.length;i<m;i++) {
					refName += "," + parts[i];
				}
			}
			LDAPAttribute attrib = new LDAPAttribute("ref");
			String[] refUrls = ref.getReferrals();
			for (int i=0,m=refUrls.length;i<m;i++) {
				
					attrib.addValue(refUrls[i]);
				
			}
			
			LDAPAttributeSet attribs = new LDAPAttributeSet();
			attribs.add(attrib);
			
			entry = new LDAPEntry(refName,attribs);
			
			
		}
		catch (LDAPException e) {
			throw new SQLNamingException(e);
		}
		return entry;
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
	
	/**
	 * Used to iterate through the result set
	 * @param index Index of current row
	 * @return
	 * @throws SQLNamingException
	 */
	public boolean moveNext(int index) throws SQLNamingException {
		
		if (index >= this.rows.size()) {
			if (this.hasMoreEntries) {
				getNextEntry();
			
				return this.hasMoreEntries;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	/**
	 * @throws SQLNamingException
	 */
	protected void getNextEntry() throws SQLNamingException {
		if (this.queue != null) {
			getNextQueue();
		} else {
			getNextResults();
		}
	}

	/**
	 * @throws SQLNamingException
	 */
	private void getNextQueue() throws SQLNamingException {
		LDAPMessage message;
		try {
			message = queue.getResponse();
		} catch (LDAPException e) {
			throw new SQLNamingException(e);
		}
		if (message instanceof LDAPSearchResult) {
			entry =  ((LDAPSearchResult) message).getEntry();
			extractEntry(dn, fromContext, buff,entry);
			
		} else if (message instanceof LDAPSearchResultReference) {
			LDAPSearchResultReference ref = (LDAPSearchResultReference) message;
//			for now, we will simply create an entry based on the referral
			
			String refName = "cn=Referral[" + ref.getReferrals()[0] + "]";
			if (entry == null) {
				
				if (con.baseDN != null && con.baseDN.trim().length() >= 0) {
					refName += "," + fromContext;
				}
			}
			else {
				String[] parts = LDAPDN.explodeDN(entry.getDN(),false);
				for (int i=1,m=parts.length;i<m;i++) {
					refName += "," + parts[i];
				}
			}
			LDAPAttribute attrib = new LDAPAttribute("ref");
			String[] refUrls = ref.getReferrals();
			for (int i=0,m=refUrls.length;i<m;i++) {
				
					attrib.addValue(refUrls[i]);
				
			}
			
			LDAPAttributeSet attribs = new LDAPAttributeSet();
			attribs.add(attrib);
			
			entry = new LDAPEntry(refName,attribs);
			extractEntry(dn,fromContext, buff, entry);
		} else  {
			//System.out.println("Message : " + message.getClass().getName());
			LDAPResponse resp = (LDAPResponse) message;
			if (resp.getResultCode() == LDAPException.SUCCESS) {
				this.hasMoreEntries = false;
				
			} else {
				throw new SQLNamingException(new LDAPException(resp.getErrorMessage(),resp.getResultCode(),resp.getErrorMessage(),resp.getMatchedDN()));
			}
		}
	}
	
	/**
	 * @throws SQLNamingException
	 */
	private void getNextResults() throws SQLNamingException {
		LDAPMessage message;
		
		if (! this.searchResults.hasMore()) {
			this.hasMoreEntries = false;
			return;
		}
		
		try {
			entry =  this.searchResults.next();
			
			if (this.con.isSPML()) {
				String name = entry.getDN();
				entry = new LDAPEntry(name + ",ou=Users," + con.getBaseContext(),entry.getAttributeSet());
			}
			
			extractEntry(dn, fromContext, buff,entry);
			
		} catch (LDAPReferralException ref) {
			//for now, we will simply create an entry based on the referral
			
			String refName = "cn=Referral[" + ref.getReferrals()[0] + "]";
			if (entry == null) {
				
				if (con.baseDN != null && con.baseDN.trim().length() >= 0) {
					refName += "," + fromContext;
				}
			}
			else {
				String[] parts = LDAPDN.explodeDN(entry.getDN(),false);
				for (int i=1,m=parts.length;i<m;i++) {
					refName += "," + parts[i];
				}
			}
			LDAPAttribute attrib = new LDAPAttribute("ref");
			String[] refUrls = ref.getReferrals();
			for (int i=0,m=refUrls.length;i<m;i++) {
				
					attrib.addValue(refUrls[i]);
				
			}
			
			LDAPAttributeSet attribs = new LDAPAttributeSet();
			attribs.add(attrib);
			
			
			entry = new LDAPEntry(refName,attribs);
			extractEntry(dn,fromContext, buff, entry);
		} catch (LDAPException ldape)  {
			throw new SQLNamingException(ldape);
		}
	}
	
	private String getFieldName(String name) {
		
		if (this.revMap != null) {
			String nname = (String) this.revMap.get(name);
			if (nname != null) {
				return nname;
			}
		}
		
		return name;
	}

}

