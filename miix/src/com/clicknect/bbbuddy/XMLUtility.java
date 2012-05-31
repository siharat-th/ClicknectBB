package com.clicknect.bbbuddy;

import java.io.InputStream;
import java.util.Vector;

import net.rim.device.api.xml.parsers.DocumentBuilder;
import net.rim.device.api.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLUtility {

	public static Vector parseXML(InputStream is) throws Exception {
		Vector data = new Vector();
		Document doc;
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory. newInstance(); 
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		docBuilder.isValidating();
		doc = docBuilder.parse(is);
		doc.getDocumentElement ().normalize ();
		NodeList lists=doc.getElementsByTagName("*");
		for(int i=0; i<lists.getLength(); i++){
			Node value=lists.item(i).getChildNodes().item(0);
			String node=lists.item(i).getNodeName();
			String element = null;
			if(value==null){
				element=null;
			}
			else{
				element=value.getNodeValue();
			}
			data.addElement(new XMLNode(node, element));
		}
		
		return data;
	}

}
