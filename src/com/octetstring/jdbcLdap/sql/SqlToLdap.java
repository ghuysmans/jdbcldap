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
 * SqlToLdap.java
 *
 * Created on March 11, 2002, 8:44 AM
 */

package com.octetstring.jdbcLdap.sql;

import java.sql.SQLException;
import java.util.*;

import com.octetstring.jdbcLdap.jndi.SQLNamingException;

/**
 *
 *Transforms an in-fix SQL query into an LDAP pre-fix notation
 *@author Marc Boorshtein, OctetString 
 */
public class SqlToLdap {
    
    /** SQL Representation of AND */
    public static final String SQL_AND = "AND";
    
    /** SQL Representation or OR */
    public static final String SQL_OR = "OR";
    
    /** SQL Representation of NOT */
    public static final String SQL_NOT = "NOT";
    
    /** SQL Representation of NULL */
    public static final String SQL_NULL = "NULL";
    
    /** SQL Representation of IS */
    public static final String SQL_IS = "IS";
    
    /** Left Parenthasese **/
    public static final char LEFT_PAR = '(';
    
    /** Right Parenthasese **/
    public static final char RIGHT_PAR = ')';
    
    /** String Left Parenthasese **/
    static final String SL_PAR = "(";
    
    /** String Right Parenthasese **/
    static final String SR_PAR = ")";
    
    /** Contains the order of opperations */
    HashMap order;
    
    
    
    
    /** Creates new SqlToLdap */
    public SqlToLdap() {
        order = new HashMap();
        order.put(SQL_NOT,new Integer(Node.TYPE_NOT));
        order.put(SQL_AND,new Integer(Node.TYPE_AND));
        order.put(SQL_OR,new Integer(Node.TYPE_OR));
        order.put(SR_PAR,new Integer(Node.TYPE_RPAR));
    }
    
    /**
     *Determines if the last node is of a greater order then current
     *@param opps Stack of opperations
     *@param curr Current Operation
     *@return true if the last opp is greater then the current one
     */
    boolean lastNodeGreater(Stack opps, String curr) {
        if (opps.isEmpty()) return false;
        
        Node node = (Node) opps.peek();
        
        return node.type > ((Integer) order.get(curr)).intValue();
    }
    
    
    /**
     *Determines if the string is NOT AND OR )
     *@param curr Current element
     */
    boolean isCmd(String curr) {
        return (curr.equalsIgnoreCase(SR_PAR)) || (curr.equalsIgnoreCase(SQL_AND)) || (curr.equalsIgnoreCase(SQL_OR)) || (curr.equalsIgnoreCase(SQL_NOT) );
    }
    
    /**
     *Processes the stacks until an element with a lower order is found
     *@param opps The Opperations stack
     *@param elements The elements stack
     */
    void procStack(Stack opps, Stack elements, int curr) {
        Node l,r,opp,tmp;
        
        
        if (opps.isEmpty()) return;
        
        do {
            
            opp = (Node) opps.pop();
            
            r = (Node) elements.pop();
            if (opp.type != Node.TYPE_NOT && opp.type != Node.TYPE_LPAR) {
                
                l = (Node) elements.pop();
            }
            else {
                
                l=null;
            }
            
            
            
            opp.l = l;
            opp.r = r;
            
            if (opp.type == Node.TYPE_LPAR) {
                elements.push(opp.r);
            }
            else {
                elements.push(opp);
            }
            
        } while (opp.type > curr &&  ! opps.isEmpty());
        
    }
    
    /**
     *Converts a SQL expresion into and LDAP expression
     *@param expr SQL Expresion to convert
     * @throws SQLException
     */
    public String convertToLdap(String expr,HashMap fieldMap) throws SQLException {
       LinkedList list = inOrder(expr,fieldMap);
       
       Stack elements = new Stack();
       Stack opps = new Stack();
       Node tree;
       String curr;
       String currUCase;
       Iterator it = list.iterator();
       while (it.hasNext()) {
            
            curr = (String) it.next();
            
            while (curr.trim().length() == 0 && it.hasNext()) {
                curr = (String) it.next();
                
            }
            
            currUCase = curr.toUpperCase();
            
            if (curr.equalsIgnoreCase(SL_PAR)) {
                tree = new Node();
                tree.type = Node.TYPE_LPAR;
                tree.l = null;
                tree.r = null;
                opps.push(tree);
            }
            else if (isCmd(currUCase)) {
                if (curr.equalsIgnoreCase(SR_PAR)) {
                    procStack(opps,elements,((Integer) order.get(SR_PAR)).intValue());
                }
                else if (lastNodeGreater(opps,currUCase)) {
                    procStack(opps,elements,((Integer) order.get(currUCase)).intValue());
                    tree = new Node();
                    tree.l = null;
                    tree.r = null;
                    tree.type = ((Integer) order.get(currUCase)).intValue();
                    opps.push(tree);
                }
                else {
                    tree = new Node();
                    tree.l = null;
                    tree.r = null;
                    tree.type = ((Integer) order.get(currUCase)).intValue();
                    opps.push(tree);
                }
            } else if (currUCase.equals(SQL_IS)) {
            		String next = (String) it.next();
            		
            		if (next.equalsIgnoreCase(SQL_NULL)) { 
            			tree = (Node) elements.peek();
            			if (fieldMap != null) {
            				
            				String fieldName = tree.val;
            				String newField = (String) fieldMap.get(fieldName);
            				if (newField != null) {
            					tree.val = newField;
            				}
            			} 
            			
            			
            			tree.val = "!(" + tree.val + "=*)";
            		} else if (next.equalsIgnoreCase(SQL_NOT)) {
            			String next2 = (String) it.next();
            			if (! next2.equalsIgnoreCase(SQL_NULL)) {
            				throw new SQLException("Unexpected token near IS");
            			}
            			
            			tree = (Node) elements.peek();
            			
            			if (fieldMap != null) {
            				
            				String fieldName = tree.val;
            				String newField = (String) fieldMap.get(fieldName);
            				if (newField != null) {
            					tree.val = newField;
            				}
            			} 
            			
            			tree.val = tree.val + "=*";
            			
            		} else {
            			throw new SQLException("Unexpected token near IS");
            		}
            }
            else {
                tree = new Node();
                tree.l = null;
                tree.r = null;
                tree.val = curr;
                tree.type = Node.TYPE_ELEMENT;
                elements.push(tree);
            }
       }
       
       procStack(opps,elements,-1);
       
       tree = (Node) elements.pop();
       
       
       
       StringBuffer finalExpr = new StringBuffer();
       tree.traverse(finalExpr);
       
       return finalExpr.toString();
       
    }
    
    /**
     *Parses an expression into a set of in-order nodes
     *@param expr The SQL expression to be parsed
     *@return A LinkedList containing the parsed nodes
     */
    public LinkedList inOrder(String expr,HashMap fieldMap) {
        LinkedList list = new LinkedList();
        StringBuffer buf = new StringBuffer();
        int i;
        char curr;
        char[] tmp = new char[1];
        
        for (i=0;i<expr.length();i++) {
            curr = expr.charAt(i);
            
            //first determine if we are at a ()
            if (curr == LEFT_PAR || curr == RIGHT_PAR) {
                //add buffer to list
                if (buf.length() != 0) {
                    if (! addToList(list,buf,fieldMap)) {
 
                        list.add(transformToFilter(new StringBuffer(buf.toString().trim()),fieldMap));
                        buf.setLength(0);
                    }
                }
                //add parenthasese to list
                tmp[0] = curr;
                list.add(new String(tmp));
            }
            else {
                //if we are at a space, detrmine if we need to add to the list
                if (curr == ' ') {
                    if (! addToList(list,buf,fieldMap))
                        buf.append(curr);
                }
                else {
                    //add to the buffer instead
                    buf.append(curr);
                }
            }
        }
        
        if (buf.length() != 0) {
        	
        		String stmp = transformToFilter(buf,fieldMap);
            list.add(stmp);
        }
        
        return list;
    }
    
    /**
	 * @param buf
	 * @return
	 */
	private String transformToFilter(StringBuffer buf,HashMap fieldMap) {
		String stmp = buf.toString().trim();

		int like = stmp.toLowerCase().indexOf(" like ");
		if (like != -1) {
			buf.setLength(0);
			buf.append(stmp);
			System.out.println("Buff : " + like + ";" + buf.length());
			buf.delete(like, like + 6);
			buf.insert(like,'=');
			stmp = buf.toString();
		}
		
		if (stmp.charAt(0) == '\'') {
			stmp = stmp.substring(1);
		}
		
		int equals = stmp.indexOf('=');
		if (equals != -1) {
			int quote = stmp.indexOf('\'',equals);
			if (quote != -1 && stmp.charAt(quote - 1) != '\\') {
				buf.setLength(0);
				buf.append(stmp);
				buf.delete(equals + 1,quote + 1);
				stmp = buf.toString();
			}
			
			if (fieldMap != null) {
				equals = stmp.indexOf('=');
				String fieldName = stmp.substring(0,equals).trim();
				System.out.println("filedName " + fieldName);
				String newField = (String) fieldMap.get(fieldName);
				System.out.println("newfield " + newField);
				if (newField != null) {
					buf.setLength(0);
					buf.append(newField).append(stmp.substring(equals));
					stmp = buf.toString();
					System.out.println(stmp);
				}
			} 
			
			
			
		}
		
		
		
		int wc = stmp.indexOf('%');
		if (wc != -1) {
			buf.setLength(0);
			buf.append(stmp);
			if (buf.charAt(wc - 1) != '\\') {
				buf.setCharAt(wc,'*');
			} else {
				buf.deleteCharAt(wc - 1);
				wc--;
				
			}
			wc = buf.indexOf("%",wc + 1);
			while (wc != -1) {
				if (buf.charAt(wc - 1) != '\\') {
					buf.setCharAt(wc,'*');
				} else {
					buf.deleteCharAt(wc - 1);
					wc--;
				}
				wc = buf.indexOf("%",wc + 1);
			}
			stmp = buf.toString();
		}
		
		if (stmp.charAt(stmp.length() - 1) == '\'' && stmp.charAt(stmp.length() - 2) != '\\') {
			stmp = stmp.substring(0,stmp.length() - 1);
		}
		return stmp;
	}

	boolean addToList(LinkedList list, StringBuffer buf,HashMap fieldMap) {
        int space = buf.toString().lastIndexOf(' ');
        boolean added = false;
        
        String add;
        
      
        
        if (buf.toString().trim().equalsIgnoreCase(SQL_NOT)) {
            list.add(this.transformToFilter(buf,fieldMap));
            
            buf.setLength(0);
        }
        else if ((buf.substring(space + 1).equalsIgnoreCase(SQL_AND)) || 
            (buf.substring(space + 1).equalsIgnoreCase(SQL_OR)) ||
            (buf.substring(space + 1).equalsIgnoreCase(SQL_NOT)) ||
            (buf.substring(space + 1).equalsIgnoreCase(SQL_IS)) ||  
			(buf.substring(space + 1).equalsIgnoreCase(SQL_IS)) )
        {
            add =buf.substring(0,space).trim();
            if (add.length() != 0) {
            		String tmp  = buf.substring(0,space).trim();
            		
                list.add(this.transformToFilter(new StringBuffer(tmp),fieldMap));
            }
            
            list.add(buf.substring(space + 1).trim());
            
            buf.setLength(0);
            added = true;
        }
        
        return added;
    }

}

class Node {
    public static final int TYPE_LPAR = 0;
    public static final  int TYPE_RPAR = 1;
    public static  final int TYPE_PAR = 2;
    public static  final int TYPE_OR = 3;
    public static  final int TYPE_AND = 4;
    public static  final int TYPE_NOT = 5;
    public static  final int TYPE_ELEMENT = 6;
    
    Node l,r;
    int type;
    String val;
    
    public void traverse(StringBuffer buff) {
       

        switch (type) {
           case TYPE_AND :   buff.append("(&");  break;
           case TYPE_OR :    buff.append("(|"); break;
           case TYPE_NOT :  buff.append("(!");  break;
           case TYPE_ELEMENT : buff.append("(").append(val).append(")"); break;
       }
       
       if (l!=null) l.traverse(buff);
       if (r!=null) r.traverse(buff);
       
       if (type != TYPE_ELEMENT) {
            buff.append(")");
       }
       
       
       
       
    }
    
    public void inOrder() {
        boolean cont = l!=null;
        
        if (cont) l.inOrder();
        
        switch (type) {
           case TYPE_AND :   System.out.print(" AND "); break;
           case TYPE_OR :    System.out.print(" OR "); break;
           case TYPE_NOT :  System.out.print(" NOT "); break;
           case TYPE_ELEMENT : System.out.print(val); break;
       }
       
       if (r!=null) r.inOrder();
        
        
        
    }
}
