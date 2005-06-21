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

import java.lang.Boolean;
import java.lang.String;
import java.util.*;
import java.util.Hashtable;

import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.*;
import org.w3c.dom.*;
import java.io.*;

/**
 * @author Marc Boorshtein
 *
 */
public class ConfigStore {
	Element root;
	String prefsFile;
	Document doc;
	TreeMap configs;
	
	public ConfigStore() throws Exception {
		String home = System.getProperty("user.home");
		prefsFile = home + "/.jdbcLdapBrowserCfg";
		this.configs = new TreeMap();
		File f = new File(prefsFile);
		if (f.exists()) {
			parseConfigs();
		}
		else {
			createConfig();
		}
	}

	public void saveConfig(String label,String user, String pass, String host, String port,String base,boolean isDSML,boolean followReferrals, boolean isSSL, boolean isSpml, boolean isJdbc, String extraUrl) throws Exception {
		ConnectionStore cs = new ConnectionStore();
		Element svr = null;
		NodeList nl = doc.getDocumentElement().getElementsByTagName("server");
		for (int i=0,m=nl.getLength();i<m;i++) {
			if (((Element) nl.item(i)).getAttribute("name").equalsIgnoreCase(label)) {
				svr = (Element) nl.item(i);
				break;
			}
			
		}
		
		
		if (svr == null) {
			svr = doc.createElement("server");
			root.appendChild(svr);
		}
		
		svr.setAttribute("name",label);
		
		cs.base = base;
		svr.setAttribute("base",base);
		
		cs.followReferrals = followReferrals;
		svr.setAttribute("followReferrals",Boolean.toString(followReferrals));
		
		cs.isDsml = isDSML;
		svr.setAttribute("isDSML",Boolean.toString(isDSML));
		
		cs.isSpml = isSpml;
		svr.setAttribute("isSPML",Boolean.toString(isSpml));
		
		cs.isSSL = isSSL;
		svr.setAttribute("isSSL",Boolean.toString(isSSL));
		
		cs.pass = pass;
		svr.setAttribute("pass",pass);
		
		cs.port = port;
		svr.setAttribute("port",port);
		
		cs.server = host;
		svr.setAttribute("server",host);
		
		cs.user = user;
		svr.setAttribute("user",user);
		
		cs.isJDBC = isJdbc;
		svr.setAttribute("isJDBC",Boolean.toString(isJdbc));
		
		cs.extraUrl = extraUrl;
		svr.setAttribute("extraUrl",cs.extraUrl);
		this.configs.put(label,cs);
		this.storeConfigs();
		
	}
	
	/**
	 * 
	 */
	private void parseConfigs() throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = factory.newDocumentBuilder();
		doc = db.parse(prefsFile);
		this.root = doc.getDocumentElement();
		NodeList nl = doc.getDocumentElement().getElementsByTagName("server");
		for (int i=0,m=nl.getLength();i<m;i++) {
			ConnectionStore cs = new ConnectionStore();
			Element svr = (Element) nl.item(i);
			String label = svr.getAttribute("name");
			
			String tmp = svr.getAttribute("isDSML");
			cs.isDsml = tmp.equals("1") || tmp.equalsIgnoreCase("true");
			
			tmp = svr.getAttribute("followReferrals");
			cs.followReferrals = tmp.equals("1") || tmp.equalsIgnoreCase("true");
			
			tmp = svr.getAttribute("isSSL");
			cs.isSSL = tmp.equals("1") || tmp.equalsIgnoreCase("true");
			
			tmp = svr.getAttribute("isJDBC");
			cs.isJDBC = tmp.equals("1") || tmp.equalsIgnoreCase("true");
			
			cs.pass = svr.getAttribute("pass");
			cs.port = svr.getAttribute("port");
			cs.server = svr.getAttribute("server");
			cs.user = svr.getAttribute("user");
			cs.base = svr.getAttribute("base");
			cs.extraUrl = svr.getAttribute("extraUrl");
			this.configs.put(label,cs);
		}
	}

	/**
	 * 
	 */
	private void createConfig() throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			
			e.printStackTrace();
			return;
		}
		doc = builder.newDocument();
		
		this.root = doc.createElement("servers");
		doc.appendChild(root);
		this.storeConfigs();
	}
	
	private void storeConfigs() throws Exception {
		try {
			Source xmlSource = new DOMSource(doc);
			Result res = new StreamResult(new FileOutputStream(this.prefsFile));
			TransformerFactory transf = TransformerFactory.newInstance();
			Transformer trans = transf.newTransformer();
			//trans.setOutputProperty("indent","yes");
			//trans.setOutputProperty(OutputKeys.ENCODING,"ISO-8859-1");

			trans.setOutputProperty(OutputKeys.INDENT,"yes");
			//trans.setOutputProperty(OutputKeys.STANDALONE,"yes");
			trans.setOutputProperty(OutputKeys.METHOD,"xml");
			trans.transform(xmlSource,res);
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
			throw new Exception("Could Not Find File : " + this.prefsFile,e);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
			throw new Exception("Could not store configuration",e);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new Exception("Could not store configuration",e);
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
			throw new Exception("Could not store configuration",e);
		} catch (TransformerException e) {
			e.printStackTrace();
			throw new Exception("Could not store configuration",e);
		}
	}
	
	public void deleteConfig(String label) throws Exception {
		this.configs.remove(label);
		Element svr = null;
		NodeList nl = doc.getDocumentElement().getElementsByTagName("server");
		for (int i=0,m=nl.getLength();i<m;i++) {
			if (((Element) nl.item(i)).getAttribute("name").equalsIgnoreCase(label)) {
				svr = (Element) nl.item(i);
				root.removeChild(svr);
			}
			
		}
		
		this.storeConfigs();
		
	}
	
	public Set getConfigLabels() {
		return this.configs.keySet();
	}
	
	public ConnectionStore getConnectionInfo(String name) {
		return (ConnectionStore) this.configs.get(name);
	}
}
