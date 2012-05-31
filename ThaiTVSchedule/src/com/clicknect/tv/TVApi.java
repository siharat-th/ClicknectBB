package com.clicknect.tv;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.HttpConnection;

import net.rim.device.api.i18n.DateFormat;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.io.http.HttpDateParser;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.xml.parsers.DocumentBuilder;
import net.rim.device.api.xml.parsers.DocumentBuilderFactory;
import net.rim.device.api.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.clicknect.util.ResourceManager;
import com.jimmysoftware.network.HttpClient;
import com.jimmysoftware.network.HttpConnectionFactory;

public class TVApi {
	public static final Bitmap BMP_LOGO_CH7_ID42 = ResourceManager.getBitmapResource("7.png");
	public static final Bitmap BMP_LOGO_ModernNineTV_ID45 = ResourceManager.getBitmapResource("9.png");
	public static final Bitmap BMP_LOGO_NBTTV_ID46 = ResourceManager.getBitmapResource("11.png");
	public static final Bitmap BMP_LOGO_THAITPBS_ID44 = ResourceManager.getBitmapResource("6PBS.png");
	public static final Bitmap BMP_LOGO_ThaiTV3_ID41 = ResourceManager.getBitmapResource("3.png");
	public static final Bitmap BMP_LOGO_TV5_ID43 = ResourceManager.getBitmapResource("5.png");
	
	public static final Bitmap BMP_LOGO_HOVER_CH7_ID42 = ResourceManager.getBitmapResource("7-1.png");
	public static final Bitmap BMP_LOGO_HOVER_ModernNineTV_ID45 = ResourceManager.getBitmapResource("9-1.png");
	public static final Bitmap BMP_LOGO_HOVER_NBTTV_ID46 = ResourceManager.getBitmapResource("11-1.png");
	public static final Bitmap BMP_LOGO_HOVER_THAITPBS_ID44 = ResourceManager.getBitmapResource("6PBS-1.png");
	public static final Bitmap BMP_LOGO_HOVER_ThaiTV3_ID41 = ResourceManager.getBitmapResource("3-1.png");
	public static final Bitmap BMP_LOGO_HOVER_TV5_ID43 = ResourceManager.getBitmapResource("5-1.png");
	
	public static final Bitmap BMP_THUMB_CH7_ID42 = ResourceManager.getBitmapResource("7-sm.png");
	public static final Bitmap BMP_THUMB_ModernNineTV_ID45 = ResourceManager.getBitmapResource("9-sm.png");
	public static final Bitmap BMP_THUMB_NBTTV_ID46 = ResourceManager.getBitmapResource("11-sm.png");
	public static final Bitmap BMP_THUMB_THAITPBS_ID44 = ResourceManager.getBitmapResource("6PBS-sm.png");
	public static final Bitmap BMP_THUMB_ThaiTV3_ID41 = ResourceManager.getBitmapResource("3-sm.png");
	public static final Bitmap BMP_THUMB_TV5_ID43 = ResourceManager.getBitmapResource("5-sm.png");
	
	public static final String[] CHANNEL_ID_ORDER = {"41", "43", "42", "45", "46", "44"};
	
	private static final String SERVICE_LIST_CHANNEL = "http://tv.clicknect.com/api/list_channel.php";
	private static final String SERVICE_LIST_PROGRAM = "http://tv.clicknect.com/api/list_program.php?";
	private static final String SERVICE_LIST_NOWSHOWING = "http://tv.clicknect.com/api/list_nowshow.php?";
	private static final String SERVICE_LIST_SEARCH = "http://tv.clicknect.com/api/list_search.php?";
	private static final String SERVICE_LIST_FAVORITE = "http://tv.clicknect.com/api/list_favorite.php?";
	
	private HttpConnectionFactory connFactory;
	private HttpClient httpClient;
	
	public TVApi(){
		connFactory = new HttpConnectionFactory();
		httpClient = new HttpClient(connFactory);
	}
	
	public static Bitmap getChannelLogo(String id){
		int uid = Integer.parseInt(id);
		if(uid==42) return BMP_LOGO_CH7_ID42;
		if(uid==45) return BMP_LOGO_ModernNineTV_ID45;
		if(uid==46) return BMP_LOGO_NBTTV_ID46;
		if(uid==44) return BMP_LOGO_THAITPBS_ID44;
		if(uid==41) return BMP_LOGO_ThaiTV3_ID41;
		if(uid==43) return BMP_LOGO_TV5_ID43;
		return null;
	}
	
	public static Bitmap getChannelLogoHover(String id){
		int uid = Integer.parseInt(id);
		if(uid==42) return BMP_LOGO_HOVER_CH7_ID42;
		if(uid==45) return BMP_LOGO_HOVER_ModernNineTV_ID45;
		if(uid==46) return BMP_LOGO_HOVER_NBTTV_ID46;
		if(uid==44) return BMP_LOGO_HOVER_THAITPBS_ID44;
		if(uid==41) return BMP_LOGO_HOVER_ThaiTV3_ID41;
		if(uid==43) return BMP_LOGO_HOVER_TV5_ID43;
		return null;
	}
	
	public static Bitmap getChannelThumbnail(String id){
		int uid = Integer.parseInt(id);
		if(uid==42) return BMP_THUMB_CH7_ID42;
		if(uid==45) return BMP_THUMB_ModernNineTV_ID45;
		if(uid==46) return BMP_THUMB_NBTTV_ID46;
		if(uid==44) return BMP_THUMB_THAITPBS_ID44;
		if(uid==41) return BMP_THUMB_ThaiTV3_ID41;
		if(uid==43) return BMP_THUMB_TV5_ID43;
		return null;
	}
	
	public static String getChannelName(String channelId){
		int uid = Integer.parseInt(channelId);
		if(uid==42) return "CH7";
		if(uid==45) return "ModernNineTV";
		if(uid==46) return "NBTTV";
		if(uid==44) return "THAITPBS";
		if(uid==41) return "ThaiTV3";
		if(uid==43) return "TV5";
		return "";
	}
	
	public InputStream getServiceListChannel() throws IOException{
		return getService(SERVICE_LIST_CHANNEL);
	}
	
	public InputStream getTodaySchedule(String channelId) throws IOException{
		Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ssz");
		String trim = sdf.format(new Date(d.getTime()));
		return getService(SERVICE_LIST_PROGRAM+"ch_id="+channelId+"&date="+trim);
	}
	
	public InputStream getAllDaySchedule(String channelId, Date currentDate) throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ssz");
		String trim = sdf.format(new Date(currentDate.getTime()));
		return getService(SERVICE_LIST_PROGRAM+"ch_id="+channelId+"&date="+trim);
	}
	
	public InputStream getNowShowing() throws IOException{
		Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ssz");
		String tim = sdf.format(new Date(d.getTime()));
		return getService(SERVICE_LIST_NOWSHOWING+"date="+tim);
	}
	
	public InputStream getSearch(String key) throws IOException{
		String url = SERVICE_LIST_SEARCH+"key="+key;
		String enc = new String(url.getBytes("utf-8"));
		return getService(enc);
	}
	
	public InputStream getFavorite() throws IOException{
		Hashtable hash = FavoritePersistent.getData();
		Enumeration enum = hash.keys();
		StringBuffer buff = new StringBuffer();
		while(enum.hasMoreElements()){
			String pid = (String)enum.nextElement();
			buff.append(pid+",");
		}
		String strId = buff.toString();
		return getService(SERVICE_LIST_FAVORITE+"id="+strId);
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
	
	public Bitmap createBitmapViaHttp(String url) {
		StringBuffer response = null;
		try {
			response = httpClient.doGet(url);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Bitmap bitmap = null;
		if(response!=null){
			byte[] data = response.toString().getBytes();
			if (data.length > 0) {
				EncodedImage image = EncodedImage.createEncodedImage(data, 0, data.length);
				bitmap = Bitmap.createBitmapFromPNG(image.getData(), image.getOffset(), image.getLength());
			}
		}
		return bitmap;
	}
	
	public static String dateToTime(String date){
		long parse = HttpDateParser.parse(date);
		Date d = new Date(parse);
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		String trim = sdf.format(new Date(d.getTime()));
		return trim;
	}
	
	public static String getDay(String date){
		long parse = HttpDateParser.parse(date);
		Date d = new Date(parse);
		SimpleDateFormat sdf = new SimpleDateFormat("E");
		String trim = sdf.format(new Date(d.getTime()));
		return trim;
	}
	
	public static String timeMillisTodate(long time){
		SimpleDateFormat sdf = new SimpleDateFormat("E dd/MM/yyyy");
		String trim = sdf.format(new Date(time));
		return trim;
	}
	
	public class XMLNode{
		private String node, element;
		public XMLNode(String node, String element){
			this.node = node;
			this.element = element;
		}
		
		public String getNode(){return node;}
		public String getElement(){return element;}
		
		public String toString(){
			return node+"="+element;
		}
	}
	
	public class ChannelData{
		public String id;
		public String name;
		public Bitmap logo, logoHover, thumbnail;
	}
	
	public class ProgramData{
		public String id;
		public String title;
		public String beginDateTime, beginTime;
		public String day;
		public String endDateTime, endTime;
		public String channelId;
		public boolean isFavorite;
	}
}
