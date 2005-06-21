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
package com.octetstring.jdbcLdap.browser;
import java.net.MalformedURLException;
import java.util.*;

import com.novell.ldap.LDAPUrl;
import com.novell.ldap.util.DN;
import com.octetstring.jdbcLdap.jndi.*;
import java.util.regex.*;

/**
 * @author Marc Boorshtein
 *
 */
public class TreeObject {
		public static final Pattern refPat = Pattern.compile("^cn=Referral\\[(.*)\\].*$",Pattern.CASE_INSENSITIVE);
	
	
		TreeObject parent;
		String name;
		String displayName;
		String base;
		String url;
		HashMap attributes;
		HashMap children;
		boolean getSQL;
		boolean isRef;
		int conId;
		
		JndiLdapConnection con;
		
		public TreeObject(String name, TreeObject parent,String topBase,boolean getSQL,HashMap children,HashMap attributes) {
			this(name,parent,topBase,getSQL);
			this.children = children;
			this.attributes = attributes;
			conId = 0;
			
		}
		
		public TreeObject(String name,TreeObject parent,String topBase,boolean getSQL) {
			this(name,parent,topBase);
			this.getSQL = getSQL;
			this.children = new HashMap();
			this.attributes = new HashMap();
			conId = 0;
		}
		
		public TreeObject(String name, TreeObject parent,String topBase) {
			this.getSQL = true;
			if (parent == null || parent.getName().equalsIgnoreCase("RootDSE"))  {
				if (name.trim().length() == 0) {
					this.base = name;
					this.name = "RootDSE";
				} else {
					this.base = name;
					this.name = name;
				}
				
			}
			else {
					//System.out.println("passed in name : " + name);
					if (name.indexOf(',') == -1) {
						name = "RootDSE";
						base = " ";
					} else {
						this.name = new DN(name).explodeDN(false)[0];//  name.substring(0,name.indexOf(','));
						
						if (name.toLowerCase().endsWith(parent.getBase().toLowerCase()) || parent.getName().toLowerCase().equalsIgnoreCase("RootDSE")) {
							this.base = name;
						}
						else {
							if (name.toLowerCase().lastIndexOf(topBase.toLowerCase()) == -1) {
								this.base = name;
							} else {
								this.base = name.substring(name.indexOf(',') + 1, name.lastIndexOf(topBase));
							}
						}
					}
				
			}
			
			Matcher m = refPat.matcher(name);
			
			this.isRef = m.matches();
			if (this.isRef && this.getSQL) {
				this.url = m.group(1);
				this.displayName = name;
			
				try {
					LDAPUrl  ldapUrl = new LDAPUrl(this.url);
					this.name = ldapUrl.getDN();
					this.base = name.substring(name.lastIndexOf(']') + 2);
					if (this.base.indexOf('?') != -1) base = base.substring(0,base.indexOf('?'));
					//System.out.println("name : " + this.name);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//this.name = this.name.substring(this.name.lastIndexOf('/') + 1);
				//this.displayName = this.name + " [" + this.url.substring(this.url.indexOf("//") + 2,this.url.lastIndexOf("/")) + "]";
				
				//this.con = this.parent.getRefCon();
			} else {
				this.displayName = this.name;
			}
			
			this.parent = parent;
			conId = 0;
		}
	
		public TreeObject getParent() {
			return this.parent;
		}
	
		public String toString() {
			
				return displayName;
			
		}
		
		public String getBase() {
			return this.base;
		}
		
		public TreeObject getChild(String name) {
			return (TreeObject) this.children.get(name);
		}
		
		public void addChild(String name,TreeObject to) {
			this.children.put(name,to);
		}
		
		public boolean isRef() {
			return this.isRef;
		}
		
		public JndiLdapConnection getRefCon() {
			return this.con;
		}
		
		public void setRefConnection(JndiLdapConnection con) {
			this.con = con;
		}
		
		public String getDisplayName() {
			return this.displayName;
		}
		
		public String getRefUrl() {
			return this.url;
		}
		
		public String getName() {
			return this.name;
		}
		
		public void setConId(int id) {
			this.conId = id;
		}
		
		public int getConId() {
			return this.conId;
		}
}
