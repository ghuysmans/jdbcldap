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
 * TestSQL.java
 *
 * Created on May 23, 2002, 12:07 PM
 */
import com.octetstring.jdbcLdap.sql.statements.JdbcLdapSelect;
import com.octetstring.jdbcLdap.jndi.*;
import com.octetstring.jdbcLdap.sql.*;
import com.octetstring.jdbcLdap.sql.statements.*;
import java.sql.*;
import javax.naming.directory.*;
/**
 *
 * @author  pjh, OctetString, Inc (c)2002
 */
public class TestSQL {

	/** Creates a new instance of Test */
	public static void main(String[] args) throws Exception {
		System.out.println("Welcome to the JDBC LDAP Demo Application.");
		//JndiLdapConnection con;
		boolean doInsert;
		boolean doDelete;
		boolean doDeleteMulti;

		Class.forName("com.octetstring.jdbcLdap.sql.JdbcLdapDriver");

		// ldapConnString should be of the form...
		//  jdbc:ldap://host[:port]/base dn
		String ldapConnectString =
			"jdbc:ldap://127.0.0.1:389/o=acme.com?SEARCH_SCOPE:=subTreeScope";
		java.sql.Connection con;

		con =
			DriverManager.getConnection(
				ldapConnectString,
				"cn=Admin",
				"manager");
		System.out.println("Connection established");

		try {
			System.out.println("Attempting to insert test records...");
			Statement stmt = con.createStatement();
			int count;
			count =
				stmt.executeUpdate(
					"INSERT INTO ou=Product Development (objectClass,objectClass,ou) VALUES (top,organizationalunit,Product Development)");
			count
				+= stmt.executeUpdate(
					"INSERT INTO cn=Marc Boorshtein, OctetString,ou=Product Development (objectClass,objectClass,objectClass,sn,cn) VALUES (top,person,organizationalPerson,Boorshtein,Marc Boorshtein, OctetString)");
			count
				+= stmt.executeUpdate(
					"INSERT INTO cn=Phil Hunt,ou=Product Development (objectClass,objectClass,objectClass,sn,cn) VALUES (top,person,organizationalPerson,Hunt,Phil Hunt)");
			count
				+= stmt.executeUpdate(
					"INSERT INTO cn=Steve Boorsh,ou=Product Development (objectClass,objectClass,objectClass,sn,cn) VALUES (top,person,organizationalPerson,Boorsh,Steve Boorsh)");
			count
				+= stmt.executeUpdate(
					"INSERT INTO cn=Sherry Boorsh,ou=Product Development (objectClass,objectClass,objectClass,sn,cn) VALUES (top,person,organizationalPerson,Boorsh,Sherry Boorsh)");
			if (count < 5) {
				System.out.println("Test Insert failed.");
			} else {
				System.out.println("Test Insert succeeded.");
			}
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			System.out.println("Attempting to delete a record...");
			String SQL = "DELETE FROM ou=Product Development WHERE sn=?";
			PreparedStatement ps = con.prepareStatement(SQL);
			ps.setString(1, "boorshtein");
			ps.execute();

			System.out.println("Test single deletion successful.");
		} catch (Exception e) {
			System.out.println("Test single deletion failed.");
			e.printStackTrace();
			return;
		}

		try {
			System.out.println("Test query by sn...");
			Statement stmt = con.createStatement();
			ResultSet rs =
				stmt.executeQuery(
					"SELECT * FROM ou=Product Development WHERE sn=Boorsh");
			System.out.println("Query result rows = " + rs.getFetchSize());
			boolean valuesPrinted = false;
			while (rs.next()) {
				valuesPrinted = true;
				// Note, but, getString is case sensitive.  
				// DN must be uppercase, rest are lowercase
				System.out.println(
					"DN="
						+ rs.getString("DN")
						+ ", sn="
						+ rs.getString("sn")
						+ ", cn="
						+ rs.getString("cn"));
			}
			if (!valuesPrinted)
				System.out.println("Query returned no results.");
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			System.out.println("Attempting to delete remaining records...");
			String SQL =
				"DELETE FROM ou=Product Development WHERE objectclass=*";
			Statement del = con.createStatement();

			int res = del.executeUpdate(SQL);
			System.out.println("Result=" + res);
			if (res < 3) {
				System.out.println(
					"Test deletion of remaining entries failed.");
				return;
			} else {
				System.out.println(
					"Test deletion of remaining entries successful.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		con.close();

		System.out.println("Test run finished.");
	}

}
