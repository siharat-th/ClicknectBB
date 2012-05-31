package net.clicknect.horoscope;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.HttpConnection;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jimmysoftware.network.HttpConnectionFactory;

import net.rim.blackberry.api.browser.URLEncodedPostData;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.xml.parsers.DocumentBuilder;
import net.rim.device.api.xml.parsers.DocumentBuilderFactory;
import net.rim.device.api.xml.parsers.ParserConfigurationException;

public class OnepostApi {
	public static final String PIN = Integer.toHexString(DeviceInfo.getDeviceId());
	public static final String GET_SERVICE_AVAILABLE = "http://onepost.clicknect.com/service/getservices.php?rttype=xml&im="+PIN;
	public static final String REGISTER_SERVICE_URL = "http://onepost.clicknect.com/service/register.php";
	public static final String SEND_SERVICE_URL = "http://onepost.clicknect.com/service/send.php";
	// onepost tag
	public static final String TAG_SERVICE_ID = "sid";
	public static final String TAG_SECURE_CODE = "sc";
	public static final String TAG_SERVICE_ICON = "icon";
	private static final String TAG_ITEM = "item";
	
	private HttpConnectionFactory connFactory;
	
	public OnepostApi(HttpConnectionFactory connFactory){
		this.connFactory = connFactory;
	}
	
	private InputStream getService(String url) throws IOException{
		HttpConnection connection = null;
		InputStream inputStream = null;
		
		connection = connFactory.getHttpConnection(url);
		
		if(connection.getResponseCode()==HttpConnection.HTTP_OK){
			inputStream = connection.openInputStream();
		}
		return inputStream;
	}
	
	public InputStream getOnepostServiceAvailable() throws IOException{
		return getService(GET_SERVICE_AVAILABLE);
	}
	
	public InputStream getOnepostSendServiceResponse(String postData) throws IOException{
		String urlPostData = SEND_SERVICE_URL + postData;
		String urlEncodePostData = new String(urlPostData.getBytes("utf-8"));
		return getService(urlEncodePostData);
	}
	
	public Vector parseXMLAsVector(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {		
		Vector data = new Vector();
		Document doc;
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory. newInstance(); 
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		docBuilder.isValidating();
		doc = docBuilder.parse(inputStream);
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
	
	public Vector getOnepostServiceInfos(Vector xmlNodes){
		Vector serviceInfos = new Vector();
		
		Enumeration _enum = xmlNodes.elements();
		while(_enum.hasMoreElements()){
			XMLNode xml = (XMLNode)_enum.nextElement();
			if(xml.node.equalsIgnoreCase(TAG_ITEM)){
				int numElement = 0;
				String serviceID="", secureCode="", serviceIcon="";
				while(numElement<ServiceInfo.NUM_ELEMENTS && _enum.hasMoreElements()){
					xml = (XMLNode)_enum.nextElement();
					if(xml.node.equalsIgnoreCase(TAG_SERVICE_ID)){
						serviceID = xml.element;
						numElement++;
					}
					else if(xml.node.equalsIgnoreCase(TAG_SECURE_CODE)){
						secureCode = xml.element;
						numElement++;
					}
					else if(xml.node.equalsIgnoreCase(TAG_SERVICE_ICON)){
						serviceIcon = xml.element;
						numElement++;
					}
				}
				serviceInfos.addElement(new ServiceInfo(serviceID, secureCode, serviceIcon));
			}
		}
		
		return serviceInfos;
	}
	
	public String getRegisterServicePostData(String serviceName){
		String prefix = "rt=dialog&im="+ PIN +"&service=" ;
		return prefix + serviceName;
	}
	
	public String getSendServicePostDataAsString(String secureCode, String serviceName, String textMessage) throws UnsupportedEncodingException{		
		//return "?rttype=xml&sc="+secureCode+"&img="+"&service="+serviceName+"&msg="+textMessage;
		if(serviceName.equalsIgnoreCase("twitter") && textMessage.length()>100)
			textMessage = textMessage.substring(0, 100);
		return "?rttype=xml&sc="+secureCode+"&service="+serviceName+"&msg="+textMessage;
	}
	
	public Hashtable getSendServicePostDataAsHashTable(String secureCode, byte[] jpgData, String serviceName, String textMessage) throws UnsupportedEncodingException{
		Hashtable hashTable = new Hashtable();
		hashTable.put("rttype", "xml");
		hashTable.put("sc", secureCode);
		
		if(jpgData!=null)
			hashTable.put("img", new String(jpgData));
		
		hashTable.put("service", serviceName);
		
		// trim to 140 character for twitter
		if(serviceName.equalsIgnoreCase("twitter") && textMessage.length()>137)
			textMessage = textMessage.substring(0, 137)+"...";
		else{
			textMessage = textMessage + "\n\nSending via Horoscope Application for BlackBerry Develop by Clicknect Co., Ltd.";
		}
		hashTable.put("msg", textMessage);
		
		return hashTable;
	}
	
	public byte[] getSendServicePostDataAsByteAray(String secureCode, byte[] jpgData, String serviceName, String textMessage) throws UnsupportedEncodingException{
		Hashtable data = getSendServicePostDataAsHashTable(secureCode, jpgData, serviceName, textMessage);
		URLEncodedPostData encoder = new URLEncodedPostData("UTF-8", false);
		Enumeration keysEnum = data.keys();

		while (keysEnum.hasMoreElements()) {
			String key = (String) keysEnum.nextElement();
			String val = null;
			if(key.equalsIgnoreCase("img")){
				byte raw[] = (byte[])data.get(key);
				val = new String(raw);
			}
			else{
				val = (String)data.get(key);
			}
			encoder.append(key, val);
		}
		return encoder.getBytes();
	}
	
	public class ServiceInfo{
		public static final int NUM_ELEMENTS = 3;
		private String sid, sc, icon;
		public ServiceInfo(String sid, String sc, String icon){
			this.sid = sid;
			this.sc = sc;
			this.icon = icon;
		}
		
		public boolean isRegistered(){
			if(sc==null) return false;
			sc.trim();
			return sc.length()>0;
		}
		
		public String getServiceName(){ return sid;}
		public String getSecureCode(){ return sc;}
		public String getIconURL(){ return icon;}
		
		public String toString(){
			return "sid="+sid+",sc="+sc+",icon="+icon;
		}
	}
}
