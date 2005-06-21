/*
 * Created on Feb 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.octetstring.jdbcLdap.junit.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.HashMap;

import com.novell.ldap.LDAPException;
import com.octetstring.jdbcLdap.jndi.JndiLdapConnection;
import com.octetstring.jdbcLdap.util.AddPattern;
import com.octetstring.jdbcLdap.util.LDIF;
import com.octetstring.jdbcLdap.util.ObjRS;
import com.octetstring.jdbcLdap.util.TableDef;

/**
 * @author mboorshtei002
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class TestTableDef extends junit.framework.TestCase {

	private JndiLdapConnection con;

	/**
	 * @param arg0
	 */
	public TestTableDef(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	protected void tearDown() throws java.lang.Exception {

		con.close();
	}

	protected void setUp() throws java.lang.Exception {
		Class.forName("com.octetstring.jdbcLdap.sql.JdbcLdapDriver");
		con = (JndiLdapConnection) DriverManager.getConnection(System
				.getProperty("ldapConnString")
				+ "?CONCAT_ATTS:=true&SEARCH_SCOPE:=subTreeScope", System
				.getProperty("ldapUser"), System.getProperty("ldapPass"));
		//care = true;

		/*
		 * try { con.getConnection().delete("cn=\"Marc Boorshtein,
		 * OctetString\",ou=Product Development," + con.getBaseContext()); }
		 * catch (LDAPException e) { //e.printStackTrace(System.out); }
		 */
	}
	
	public void testLoadProps() throws Exception {
		Connection con = con = (JndiLdapConnection) DriverManager.getConnection(System
				.getProperty("ldapConnString")
				+ "?CONCAT_ATTS:=true&SEARCH_SCOPE:=subTreeScope&TABLE_DEF:=C:\\table.prop", System
				.getProperty("ldapUser"), System.getProperty("ldapPass"));
		
		DatabaseMetaData md = con.getMetaData();
		boolean ok = false;
		ResultSet rs = md.getColumns(null,null,"users","cn");
		if (! rs.next() || ! rs.getString("COLUMN_NAME").equals("cn")) {
			fail("Did not load cn attribute");
			return;
		}
		
		rs = md.getColumns(null,null,"users","%");
		while (rs.next()) {
			if (rs.getString("COLUMN_NAME").equals("cn")) {
				ok = true;
				
			}
		}
		
		rs = md.getColumns(null,null,"use%","c%");
		boolean atleastonce = false;
		
		
		
		while (rs.next()) {
			atleastonce = true;
			if (rs.getString("COLUMN_NAME").equals("cn")) {
				ok = true;
				break;
			}
		}
		
		if (! atleastonce) {
			fail("no results");
		}
		
		rs = md.getColumns(null,null,"use%","t%");
		atleastonce = false;
		while (rs.next()) {
			atleastonce = true;
			if (rs.getString("COLUMN_NAME").equals("cn")) {
				fail("Contained cn");
				return;
			}
		}
		
		if (! atleastonce) {
			fail("no results");
		}
		
		if (! ok) {
			fail("did not load attributes");
		}
		
		HashMap addPattern = ((TableDef) ((JndiLdapConnection) con).getTableDefs().get("users")).getAddPatterns();
		
		if (! ((AddPattern) addPattern.get("ou")).getAddPattern().equals("ou")) {
			fail("ou pattern doesn't exist");
		}
		
		if (! ((AddPattern) ((HashMap) addPattern.get("cn")).get("ou")).getAddPattern().equals("cn,ou")) {
			fail("cn,ou pattern doesn't exist");
		}
		
		con.close();
	}
	
	public void testCreateTableDef() throws Exception {
		TableDef tbl = new TableDef("users","dc=idrs,dc=com","subTreeScope",new String[]{"organizationalUnit","inetOrgPerson","domain"},con.getConnection(),new HashMap());
		System.out.println(tbl.getTable());
		ResultSet rs = new ObjRS(tbl.getTable());
		
		boolean isOK = true;
		
		while (rs.next()) {
			String name = rs.getString("COLUMN_NAME");
			if (name != null && name.equals("cn")) {
				if (! rs.getString("TYPE_NAME").equals("VARCHAR")) {
					fail("Improper cn type");
					return;
				}
				
			
			}
			
			if (name != null && name.equals("userPassword")) {
				if (! rs.getString("TYPE_NAME").equals("BINARY")) {
					fail("Improper userPassword type");
					return;
				}
			}
		}
		
		
	}
	
	public void testSelect() throws Exception {
		
		String url = System.getProperty("ldapConnString");
		
		url = url.substring(0,url.lastIndexOf('/') + 1);
		
		Connection con = con = (JndiLdapConnection) DriverManager.getConnection(url
				+ "?CONCAT_ATTS:=true&SEARCH_SCOPE:=subTreeScope&TABLE_DEF:=C:\\table.prop", System
				.getProperty("ldapUser"), System.getProperty("ldapPass"));
		
		ResultSet rs = con.createStatement().executeQuery("SELECT DN FROM users WHERE cn='Marc Boorshtein' AND seeAlso='cn=Marc'");
		
		String ldif = "dn: cn=Marc Boorshtein,ou=Peons,dc=idrs,dc=com";
		
		LDIF fromServer = new LDIF(rs,"DN",false);
		LDIF exp = new LDIF(ldif, false);
		
		if (! fromServer.compareLdif(exp,new LDIF())) {
			fail("Un expected ldif: \n" + fromServer.toString());
		}
		
		con.close();
	}
	
	public void testUpdate() throws Exception {
		
		String url = System.getProperty("ldapConnString");
		
		url = url.substring(0,url.lastIndexOf('/') + 1);
		
		Connection con = con = (JndiLdapConnection) DriverManager.getConnection(url
				+ "?CONCAT_ATTS:=true&SEARCH_SCOPE:=subTreeScope&TABLE_DEF:=C:\\table.prop", System
				.getProperty("ldapUser"), System.getProperty("ldapPass"));
		
		con.createStatement().executeUpdate("UPDATE users SET sn='Boorsht' WHERE cn='Marc Boorshtein' AND seeAlso='cn=Marc'");
		
		
		
		ResultSet rs = con.createStatement().executeQuery("SELECT DN,sn FROM users WHERE cn='Marc Boorshtein' AND seeAlso='cn=Marc'");
		
		String ldif = "dn: cn=Marc Boorshtein,ou=Peons,dc=idrs,dc=com\nsn: Boorsht";
		
		LDIF fromServer = new LDIF(rs,"DN",false);
		LDIF exp = new LDIF(ldif, false);
		
		if (! fromServer.compareLdif(exp,new LDIF())) {
			fail("Un expected ldif: \n" + fromServer.toString());
		}
		
		con.createStatement().executeUpdate("UPDATE users SET sn='Boorshtein' WHERE cn='Marc Boorshtein' AND seeAlso='cn=Marc'");
		
		con.close();
	}
	
	public void testUpdateEntry() throws Exception {
		
		String url = System.getProperty("ldapConnString");
		
		url = url.substring(0,url.lastIndexOf('/') + 1);
		
		Connection con = con = (JndiLdapConnection) DriverManager.getConnection(url
				+ "?CONCAT_ATTS:=true&SEARCH_SCOPE:=subTreeScope&TABLE_DEF:=C:\\table.prop", System
				.getProperty("ldapUser"), System.getProperty("ldapPass"));
		
		con.createStatement().executeUpdate("UPDATE ENTRY users DO ADD SET sn='Boorsht' WHERE cn='Marc Boorshtein' AND seeAlso='cn=Marc'");
		
		
		
		ResultSet rs = con.createStatement().executeQuery("SELECT DN,sn FROM users WHERE cn='Marc Boorshtein' AND seeAlso='cn=Marc'");
		
		String ldif = "dn: cn=Marc Boorshtein,ou=Peons,dc=idrs,dc=com\nsn: Boorshtein\nsn: Boorsht";
		
		LDIF fromServer = new LDIF(rs,"DN",false);
		LDIF exp = new LDIF(ldif, false);
		
		if (! fromServer.compareLdif(exp,new LDIF())) {
			fail("Un expected ldif: \n" + fromServer.toString());
		}
		
		con.createStatement().executeUpdate("UPDATE users SET sn='Boorshtein' WHERE cn='Marc Boorshtein' AND seeAlso='cn=Marc'");
		
		con.close();
	}
	
	public void testInsert() throws Exception {
		
		String url = System.getProperty("ldapConnString");
		
		url = url.substring(0,url.lastIndexOf('/') + 1);
		
		Connection con = con = (JndiLdapConnection) DriverManager.getConnection(url
				+ "?CONCAT_ATTS:=true&SEARCH_SCOPE:=subTreeScope&TABLE_DEF:=C:\\table.prop", System
				.getProperty("ldapUser"), System.getProperty("ldapPass"));
		
		con.createStatement().executeUpdate("INSERT INTO users (objectClass,cn,sn,ou) VALUES (inetOrgPerson,Test User,User,Peons)");
		
		
		
		ResultSet rs = con.createStatement().executeQuery("SELECT DN,sn FROM users WHERE cn='Test User' AND ou='Peons'");
		
		String ldif = "dn: cn=Test User,ou=Peons,dc=idrs,dc=com\nsn: User";
		
		LDIF fromServer = new LDIF(rs,"DN",false);
		LDIF exp = new LDIF(ldif, false);
		
		if (! fromServer.compareLdif(exp,new LDIF())) {
			fail("Un expected ldif: \n" + fromServer.toString());
		}
		
		con.createStatement().executeUpdate("DELETE FROM cn=Test User,ou=Peons,dc=idrs,dc=com");
		
		
		con.createStatement().executeUpdate("INSERT INTO users2 (cn,sn,ou) VALUES (Test User,User,Peons)");
		
		rs = con.createStatement().executeQuery("SELECT DN,sn,ou FROM users WHERE cn='Test User' AND sn='User'");
		
		ldif = "dn: cn=Test User,ou=Peons,dc=idrs,dc=com\nsn: User";
		
		fromServer = new LDIF(rs,"DN",false);
		exp = new LDIF(ldif, false);
		
		if (! fromServer.compareLdif(exp,new LDIF())) {
			fail("Un expected ldif: \n" + fromServer.toString());
		}
		
		con.createStatement().executeUpdate("DELETE FROM cn=Test User,ou=Peons,dc=idrs,dc=com");
		
		con.close();
	}

}