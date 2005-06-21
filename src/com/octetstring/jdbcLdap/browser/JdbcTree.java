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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class JdbcTree implements ITreeContentProvider,ILabelProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object arg0) {
		
		try {
			JdbcTreeObject treeObj = (JdbcTreeObject) arg0;
			switch (treeObj.getType()) {
				case JdbcTreeObject.ROOT :
					Object[] ret = this.getSchemas(treeObj);
					if (ret == null) {
						return this.getTables(null,treeObj);
					} else {
						return ret;
					}
				case JdbcTreeObject.SCHEMA :
					return this.getTables(treeObj.getName(),treeObj);
					
				case JdbcTreeObject.TABLE : return new Object[0];
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Object[0];
		}
		
		return new Object[0];
	}
	
	private Object[] getSchemas(JdbcTreeObject treeObj) throws SQLException {
		Connection con = treeObj.getBrowser().jdbcCon;
		ResultSet rs = con.getMetaData().getSchemas();
		ArrayList schems = new ArrayList();
		if (rs == null || ! rs.next()) {
			return null;
		} else {
			do {
				String name = rs.getString("TABLE_SCHEM");
				schems.add(new JdbcTreeObject(JdbcTreeObject.SCHEMA,name,treeObj.getBrowser()));
			} while (rs.next());
			
			return schems.toArray();
		}
	}
	
	private Object[] getTables(String schema,JdbcTreeObject treeObj) throws SQLException {
		Connection con = treeObj.getBrowser().jdbcCon;
		ResultSet tables = con.getMetaData().getTables(null,schema,"%",null);
		ArrayList tbls = new ArrayList();
		while (tables.next()) {
			tbls.add(new JdbcTreeObject(JdbcTreeObject.TABLE,tables.getString("TABLE_NAME"),treeObj.getBrowser(),schema));
		}
		
		return tbls.toArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object arg0) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object val) {
		if (val instanceof JdbcLdapBrowser) {
			
			JdbcLdapBrowser browser = (JdbcLdapBrowser) val;
			String url = browser.jdbcUrl;
			
			url = url.substring(url.lastIndexOf("/") + 1);
			if (url.indexOf('?') != -1) {
				url = url.substring(0,url.indexOf('?'));
			}
			
			return new Object[] {new JdbcTreeObject(JdbcTreeObject.ROOT,url,browser)};
			
			
			
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object arg0) {
		if (arg0 instanceof JdbcTreeObject) {
			return ((JdbcTreeObject) arg0).getName();
		}
		return "?";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object arg0, String arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener arg0) {
		// TODO Auto-generated method stub
		
	}

	
}
