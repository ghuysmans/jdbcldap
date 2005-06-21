/*
 * Created on Feb 19, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.octetstring.jdbcLdap.util;

import java.util.HashSet;
import java.util.Set;

/**
 * @author mboorshtei002
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AddPattern {

	String addPattern;
	Set notToAdd;
	String defaultOC;
	
	public AddPattern(String addPattern,Set notToAdd,String defaultOC) {
		this.addPattern = addPattern;
		this.defaultOC = defaultOC;
		this.notToAdd = notToAdd;
	}
	
	
	/**
	 * @return Returns the addPattern.
	 */
	public String getAddPattern() {
		return addPattern;
	}
	/**
	 * @return Returns the defaultOC.
	 */
	public String getDefaultOC() {
		return defaultOC;
	}
	/**
	 * @return Returns the notToAdd.
	 */
	public Set getNotToAdd() {
		return notToAdd;
	}
}
