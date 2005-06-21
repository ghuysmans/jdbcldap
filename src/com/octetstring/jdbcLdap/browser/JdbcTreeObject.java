/* **************************************************************************
*
* Copyright (C) 2005 Marc Boorshtein. All Rights Reserved.
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

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class JdbcTreeObject {

	public static final int ROOT = 0;
	
	public static final int SCHEMA = 1;
	
	public static final int TABLE = 2;
	
	int type;
	
	String name;
	
	String parentName;

	private JdbcLdapBrowser browser;
	
	public JdbcTreeObject(int type,String name,JdbcLdapBrowser browser) {
		this.type = type;
		this.name = name;
		this.browser = browser;
	}
	
	public JdbcTreeObject(int type,String name,JdbcLdapBrowser browser,String parent) {
		this(type,name,browser);
		this.parentName = parent;
	}
	
	public String toString() {
		return this.name;
	}
	
	
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}
	/**
	 * @param type The type to set.
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @return
	 */
	public JdbcLdapBrowser getBrowser() {
		return this.browser;
	}
	/**
	 * @return Returns the parentName.
	 */
	public String getParentName() {
		return parentName;
	}
	/**
	 * @param parentName The parentName to set.
	 */
	public void setParentName(String parentName) {
		this.parentName = parentName;
	}
}
