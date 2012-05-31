package net.clicknect.horoscope;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.xml.parsers.DocumentBuilder;
import net.rim.device.api.xml.parsers.DocumentBuilderFactory;
import net.rim.device.api.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jimmysoftware.network.HttpConnectionFactory;
import com.jimmysoftware.ui.EventThreadDialog;

public class HoroApi {
	private static final String SERVICE_LIST_HORO = "http://horo.clicknect.com/api/list_horo.php";
	private static final String FILE_FOLDER = "store/home/user/horoscope/";
	private static final String FILE_NAME = "feed.xml";
	// horo tag
	public static final String TAG_ROW = "row";
	public static final String TAG_ID = "id";
	public static final String TAG_TITLE = "title";
	public static final String TAG_DESCRIPTION = "description";
	
	public static final int[] ZODIAC_ID_ORDER = {10, 11, 12, 1, 2, 3, 4, 5, 6, 7, 8, 9};
	public static final Bitmap BMP_ZODIAC_THUMBNAIL[] = new Bitmap[12];
	static{
		for(int i=1; i<=12; i++){
			String prefix = i<10? "ins-z0" : "ins-z";
			String filename = prefix + i + ".png";
			BMP_ZODIAC_THUMBNAIL[i-1] = Bitmap.getBitmapResource(filename);  
			
			if(Display.getHeight()<=240){
				// scale to 60*45(HorizontalRatio) or 52*40(VerticalRatio)
				Bitmap scale = new Bitmap(52, 40);
				scale.createAlpha(Bitmap.ALPHA_BITDEPTH_MONO);
				BMP_ZODIAC_THUMBNAIL[i-1].scaleInto(scale, Bitmap.FILTER_BILINEAR, Bitmap.SCALE_TO_FIT);
				BMP_ZODIAC_THUMBNAIL[i-1] = null;
				BMP_ZODIAC_THUMBNAIL[i-1] = scale;
			}
		}
	}
	public static final String STR_ZODIAC_DATE[] = {
		"เกิดระหว่าง 15 เม.ย.-14 พ.ค.",
		"เกิดระหว่าง 15 พ.ค.-14 มิ.ย.",
		"เกิดระหว่าง 15 มิ.ย.–15 ก.ค.",
		"เกิดระหว่าง 16 ก.ค.–16 ส.ค.",
		"เกิดระหว่าง 17 ส.ค.–15 ก.ย.",
		"เกิดระหว่าง 16 ก.ย.–16 ต.ค.",
		"เกิดระหว่าง 17 ต.ค.–15 พ.ย.",
		"เกิดระหว่าง 16 พ.ย.–15 ธ.ค.",
		"เกิดระหว่าง 16 ธ.ค.–13 ม.ค.",
		"เกิดระหว่าง 14 ม.ค.-12 ก.พ.",
		"เกิดระหว่าง 13 ก.พ.– 13 มี.ค.",
		"เกิดระหว่าง 14 มี.ค.-14 เม.ย.",
	};
	
	private HttpConnectionFactory connFactory;
	
	public HoroApi(HttpConnectionFactory connFactory){
		this.connFactory = connFactory;
	}
	
	
	public static Bitmap getBmpThumbByID(int id){
		return BMP_ZODIAC_THUMBNAIL[id-1];
	}
	
	public static String getZodiacDateByID(int id){
		return STR_ZODIAC_DATE[id-1];
	}
	
	
	public InputStream getServiceListHoro() throws IOException{
		return getService(SERVICE_LIST_HORO);
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

	public InputStream getOfflineServiceListHoro() {
		FileConnection fconnFolder = null;
		FileConnection fconnFile = null;
		OutputStream os = null;
		try {
			fconnFolder = (FileConnection) Connector.open("file:///"+FILE_FOLDER, Connector.READ_WRITE);
			if(!fconnFolder.exists()){
				fconnFolder.mkdir();
			}
			fconnFile = (FileConnection) Connector.open("file:///"+FILE_FOLDER+FILE_NAME, Connector.READ_WRITE);
			if(!fconnFile.exists()){
				// file not exists create dummy file
				fconnFile.create();
				
				// read dummy data
				InputStream is = getClass().getResourceAsStream("/list_horo.php.xml");
				byte data[] = new byte[is.available()];
				is.read(data);
				
			    // write data to file
				os = fconnFile.openOutputStream();
				os.write(data);
				os.flush();
			}
			return fconnFile.openInputStream();
		}
		catch (IOException e) {
			e.printStackTrace();
			EventThreadDialog.errorDialog("Create file error: "+e.toString());
		}
		finally{
			try{
				if(os!=null) os.close();
				if(fconnFolder!=null && fconnFolder.isOpen()) fconnFolder.close();
				if(fconnFile!=null && fconnFile.isOpen()) fconnFile.close();
			}
			catch(Exception ex){
				ex.printStackTrace();
				EventThreadDialog.errorDialog("Close connection file error: "+ex.toString());
			}
		}
		return null;
	}
	
	public void saveZodiacDataToStorage(Vector zodiacData){
		StringBuffer buff = new StringBuffer();
		buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><list>");
		
		int size = zodiacData.size();
		for(int i=0; i<size; i++){
			ZodiacData data = (ZodiacData)zodiacData.elementAt(i);
			buff.append("<row>");
			buff.append("<id>"+ data.id +"</id>");
			buff.append("<title>"+ data.title +"</title>");
			buff.append("<description>"+ data.description +"</description>");
			buff.append("</row>");
		}
		
		buff.append("</list>");
		byte data[]=null;
		try {
			data = buff.toString().getBytes("utf-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if(data!=null){
			boolean success = writeFile(data);
			if(success){
				//statusDialog("Create file successfully.");
			}
		}
	}
	
	private boolean writeFile(byte[] data){
		FileConnection fconnFolder = null;
		FileConnection fconnFile = null;
		OutputStream os = null;
		try {
			fconnFolder = (FileConnection) Connector.open("file:///"+FILE_FOLDER, Connector.READ_WRITE);
			if(!fconnFolder.exists()){
				fconnFolder.mkdir();
			}
			
			fconnFile = (FileConnection) Connector.open("file:///"+FILE_FOLDER+FILE_NAME, Connector.READ_WRITE);
			if(!fconnFile.exists()){
				// file not exists create blank file.
				fconnFile.create();
			}
			else{
				// file exists replace all from beginning of file.
				fconnFile.truncate(0);
			}
				
		    // write data to file
			os = fconnFile.openOutputStream();
			os.write(data);
			os.flush();
			return true;
		}
		catch (IOException e) {
			e.printStackTrace();
			EventThreadDialog.errorDialog("Cannot write database file: "+e.toString());
			return false;
		}
		finally{
			try{
				if(os!=null) os.close();
				if(fconnFolder!=null && fconnFolder.isOpen()) fconnFolder.close();
				if(fconnFile!=null && fconnFile.isOpen()) fconnFile.close();
			}
			catch(Exception ex){
				ex.printStackTrace();
				EventThreadDialog.errorDialog("Close file error: "+ex.toString());
			}
		}
	}	
}


class XMLNode {
	public String node, element;
	
	public XMLNode(String node, String element){
		this.node = node;
		this.element = element;
	}

	public String getNode() {
		return node;
	}

	public String getElement() {
		return element;
	}
	
	public String toString(){
		return node+"="+element;
	}
}

class ZodiacData{
	public String id, title, description;
	
	public String toString(){
		return "id:"+id+"\ntitle:"+title+"\ndesc:"+description+"\n\n";
	}
}
