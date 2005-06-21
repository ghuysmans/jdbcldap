/*
 * Created on Feb 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.octetstring.jdbcLdap.util;

import java.lang.reflect.Field;
import java.sql.DatabaseMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import com.novell.ldap.LDAPAttributeSchema;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPObjectClassSchema;
import com.novell.ldap.LDAPSchema;

/**
 * @author mboorshtei002
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TableDef {

	String dn;
	String scope;
	String combined;
	
	ArrayList metadata;
	HashMap attrMetaData;
	private String name;
	
	private static final String synbase = "1.3.6.1.4.1.1466.115.121.1.";
    private static final String adSynBase = "1.2.840.113556.1.4.90";
    
    
    
    
    static HashMap syntaxToSQL;
	private HashMap addPatterns; 
    
    
    
    static {
    	syntaxToSQL = new HashMap();
    	syntaxToSQL.put("name","VARCHAR");
        syntaxToSQL.put(synbase + "3", "VARCHAR"); 
        syntaxToSQL.put(synbase + "5", "BINARY"); 
        syntaxToSQL.put(synbase + "6", "VARCHAR"); 
        syntaxToSQL.put(synbase + "7", "VARCHAR"); 
        syntaxToSQL.put(synbase + "8", "BINARY"); 
        syntaxToSQL.put(synbase + "9", "BINARY"); 
        syntaxToSQL.put(synbase + "10", "BINARY"); 
        syntaxToSQL.put(synbase + "11", "VARCHAR"); 
        syntaxToSQL.put(synbase + "12", "VARCHAR"); 
        syntaxToSQL.put(synbase + "14", "VARCHAR"); 
        syntaxToSQL.put(synbase + "15", "VARCHAR"); 
        syntaxToSQL.put(synbase + "16", "VARCHAR"); 
        syntaxToSQL.put(synbase + "17", "VARCHAR"); 
        syntaxToSQL.put(synbase + "21", "VARCHAR"); 
        syntaxToSQL.put(synbase + "22", "VARCHAR"); 
        syntaxToSQL.put(synbase + "23", "BINARY"); 
        syntaxToSQL.put(synbase + "24", "VARCHAR"); 
        syntaxToSQL.put(synbase + "25", "VARCHAR"); 
        syntaxToSQL.put(synbase + "26", "VARCHAR"); 
        syntaxToSQL.put(synbase + "27", "INTEGER"); 
        syntaxToSQL.put(synbase + "28", "BINARY"); 
        syntaxToSQL.put(synbase + "30", "VARCHAR"); 
        syntaxToSQL.put(synbase + "31", "VARCHAR"); 
        syntaxToSQL.put(synbase + "33", "VARCHAR"); 
        syntaxToSQL.put(synbase + "34", "VARCHAR"); 
        syntaxToSQL.put(synbase + "35", "VARCHAR"); 
        syntaxToSQL.put(synbase + "36", "INTEGER"); 
        syntaxToSQL.put(synbase + "37", "VARCHAR"); 
        syntaxToSQL.put(synbase + "38", "VARCHAR"); 
        syntaxToSQL.put(synbase + "39", "VARCHAR"); 
        syntaxToSQL.put(synbase + "40", "BINARY");
        
        syntaxToSQL.put(synbase + "41", "VARCHAR"); 
        syntaxToSQL.put(synbase + "43", "VARCHAR"); 
        syntaxToSQL.put(synbase + "44", "VARCHAR"); 
        syntaxToSQL.put(synbase + "50", "VARCHAR"); 
        syntaxToSQL.put(synbase + "51", "VARCHAR"); 
        syntaxToSQL.put(synbase + "53", "VARCHAR"); 
        syntaxToSQL.put(synbase + "54", "VARCHAR"); 
        
//      Active Directory Syntaxs
        syntaxToSQL.put(adSynBase + "3","BINARY");
        syntaxToSQL.put(adSynBase + "5","VARCHAR");
        syntaxToSQL.put(adSynBase + "6","INTEGER");
        syntaxToSQL.put(adSynBase + "7","BINARY");
    }
	
	public TableDef(String name,String dn,String scope,String[] objectClasses,LDAPConnection ldapcon, HashMap addPatternMap) throws LDAPException {
		this.name = name;
		this.dn = dn;
		this.scope = scope;
		this.combined = scope + ";" + dn;
		this.attrMetaData = new HashMap();
		LDAPSchema schema = ldapcon.fetchSchema(ldapcon.getSchemaDN());
		
		
		
		HashSet proced = new HashSet();
		HashSet procedAttribs = new HashSet();
		
		proced.add("top");
		
		int index = 0;
		
		this.metadata = new ArrayList();
		
		for (int i=0,m=objectClasses.length;i<m;i++) {
			String oc = objectClasses[i];
			if (! proced.contains(oc)) {
				LDAPObjectClassSchema ocSchema = schema.getObjectClassSchema(oc);
				
				String[] sups;
				String sup;
				index = extractObjectClass(schema, proced, procedAttribs, index, oc, ocSchema);
			}
		}
		
		this.addPatterns = addPatternMap;
	}
	
	/**
	 * @param schema
	 * @param proced
	 * @param procedAttribs
	 * @param index
	 * @param oc
	 * @param ocSchema
	 * @return
	 */
	private int extractObjectClass(LDAPSchema schema, HashSet proced, HashSet procedAttribs, int index, String oc, LDAPObjectClassSchema ocSchema) {
		
		String[] sups = ocSchema.getSuperiors();
		
		for (int j=0,n=sups.length;j<n;j++) {
			String sup = sups[j];
			if (! proced.contains(sup)) {
				index = this.extractObjectClass(schema,proced,procedAttribs,index,sup,schema.getObjectClassSchema(sup)); 	
				proced.add(sup);
			}
		}
		
		index = this.addObjectClass(oc,this.metadata,schema,index,procedAttribs);
		
		proced.add(oc);
		
		return index;
	}

	private int addObjectClass(String oc,ArrayList table,LDAPSchema schema,int index,Set procedAttribs) {
		
		
		LDAPObjectClassSchema ocSchema = schema.getObjectClassSchema(oc);
		
		String[] attribs = ocSchema.getOptionalAttributes();
		
		if (attribs != null) {
			for (int i=0,m=attribs.length;i<m;i++) {
				LinkedHashMap row = new LinkedHashMap();
				LDAPAttributeSchema attribSchema = (LDAPAttributeSchema) schema.getAttributeSchema(attribs[i]);
				
				String name = attribSchema.getNames()[0];
				
				if (attribSchema == null || procedAttribs.contains(attribSchema.getNames()[0])) {
					continue;
				}
				
				row.put("TABLE_CAT",null);
				row.put("TABLE_SCHEM",null);
				row.put("TABLE_NAME",this.name);
				row.put("COLUMN_NAME",attribSchema.getNames()[0]);
				row.put("DATA_TYPE",new Integer(this.getType(attribSchema.getSyntaxString())));
				row.put("TYPE_NAME",this.getTypeName(attribSchema.getSyntaxString()));
				row.put("COLUMN_SIZE",new Integer(255));
				row.put("BUFFER_LENGTH",new Integer(0));
				row.put("DECIMAL_DIGITS",new Integer(10));
				row.put("NUM_PREC_RADIX", new Integer(10));
				row.put("NULLABLE","columnNullable");
				row.put("REMARKS",attribSchema.getDescription());
				row.put("COLUMN_DEF",null);
				row.put("SQL_DATA_TYPE",new Integer(0));
				row.put("SQL_DATETIME_SUB",new Integer(0));
				row.put("CHAR_OCTET_LENGTH",new Integer(255));
				row.put("ORDINAL_POSITION",new Integer(++index));
				row.put("IS_NULLABLE","YES");
				row.put("SCOPE_CATALOG",null);
				row.put("SCOPE_TABLE",null);
				row.put("SCOPE_DATA_TYPE",null);
				
				table.add(row);
				this.attrMetaData.put(attribSchema.getNames()[0],row);
				procedAttribs.add(attribSchema.getNames()[0]);
			}
		}
		
		attribs = ocSchema.getRequiredAttributes();
		
		if (attribs != null) {
			for (int i=0,m=attribs.length;i<m;i++) {
				LinkedHashMap row = new LinkedHashMap();
				LDAPAttributeSchema attribSchema = (LDAPAttributeSchema) schema.getAttributeSchema(attribs[i]);
				String name = attribSchema.getNames()[0];
				
				if (attribSchema == null  || procedAttribs.contains(attribSchema.getNames()[0])) {
					continue;
				}
				
				
				row.put("TABLE_CAT",null);
				row.put("TABLE_SCHEM",null);
				row.put("TABLE_NAME",this.name);
				row.put("COLUMN_NAME",attribSchema.getNames()[0]);
				row.put("DATA_TYPE",new Integer(this.getType(attribSchema.getSyntaxString())));
				row.put("TYPE_NAME",this.getTypeName(attribSchema.getSyntaxString()));
				row.put("COLUMN_SIZE",new Integer(255));
				row.put("BUFFER_LENGTH",new Integer(0));
				row.put("DECIMAL_DIGITS",new Integer(10));
				row.put("NUM_PREC_RADIX", new Integer(10));
				row.put("NULLABLE","columnNoNulls ");
				row.put("REMARKS",attribSchema.getDescription());
				row.put("COLUMN_DEF",null);
				row.put("SQL_DATA_TYPE",new Integer(0));
				row.put("SQL_DATETIME_SUB",new Integer(0));
				row.put("CHAR_OCTET_LENGTH",new Integer(255));
				row.put("ORDINAL_POSITION",new Integer(++index));
				row.put("IS_NULLABLE","NO");
				row.put("SCOPE_CATALOG",null);
				row.put("SCOPE_TABLE",null);
				row.put("SCOPE_DATA_TYPE",null);
				
				this.attrMetaData.put(attribSchema.getNames()[0],row);
				table.add(row);
				procedAttribs.add(attribSchema.getNames()[0]);
			}
		}
		
		return index;
	}

	/**
	 * @param syntaxString
	 * @return
	 */
	private String getTypeName(String syntaxString) {
		int index = syntaxString.indexOf('{');
		if (index != -1) {
			syntaxString = syntaxString.substring(0,index);
		}
		return (String) TableDef.syntaxToSQL.get(syntaxString);
	}

	/**
	 * @param syntaxString
	 * @return
	 */
	private int getType(String syntaxString) {
		Field[] fields = Types.class.getFields();
		
		String name = this.getTypeName(syntaxString);
		
		if (name == null) {
			return -1;
		}
		
		for (int i=0,m=fields.length;i<m;i++) {
			if (fields[i].getName().equals(name)) {
				try {
					return fields[i].getInt(null);
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return -1;
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return -1;
				}
			}
		}
		
		return -1;
	}

	/**
	 * @return
	 */
	public ArrayList getTable() {
		return this.metadata;
	}

	/**
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return
	 */
	public String getScopeBase() {
		return this.combined;
	}

	/**
	 * @return
	 */
	public HashMap getAddPatterns() {
		return this.addPatterns;
	}

	/**
	 * @return
	 */
	public String getBase() {
		return this.dn;
	}
	
	
}
