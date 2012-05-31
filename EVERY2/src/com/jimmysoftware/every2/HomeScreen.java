package com.jimmysoftware.every2;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.HttpConnection;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationProvider;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.rim.device.api.gps.BlackBerryCriteria;
import net.rim.device.api.gps.BlackBerryLocation;
import net.rim.device.api.gps.BlackBerryLocationProvider;
import net.rim.device.api.gps.GPSInfo;
import net.rim.device.api.i18n.DateFormat;
import net.rim.device.api.i18n.Locale;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.io.http.HttpDateParser;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.CoverageInfo;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngine;
import net.rim.device.api.ui.component.DateField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.GaugeField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.DialogFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.xml.parsers.DocumentBuilder;
import net.rim.device.api.xml.parsers.DocumentBuilderFactory;

import com.jimmysoftware.network.HttpClient;
import com.jimmysoftware.network.HttpConnectionFactory;
import com.jimmysoftware.ui.ActionScreen;

public class HomeScreen extends ActionScreen implements ListFieldCallback{
	public static final String ACTION_ENTER = "webview";
	public static final String ACTION_CHANGE_VIEW = "landscape view";
	public static final String ACTION_RECOMMENED = "recommened application";
	
	private HttpConnectionFactory connFactory;
	private HttpClient httpClient;
	
	private PopupScreen popupActivity = null;
	private GaugeField gaugeField = null;
	private ListField listField;
	
	private Vector listFieldInfos = null;
	public Vector landscapeObjects = null;
	private FeedThread feedThread = null;
	
	private double latitude, longitude, altitude;
	private int progress;
	
	public HomeScreen(HttpConnectionFactory connFactory){
		this.connFactory = connFactory;
		httpClient = new HttpClient(this.connFactory);
		
		listFieldInfos = new Vector();
		landscapeObjects = new Vector();
		
		listField = new ListField(0, Field.FOCUSABLE){
			public void paint(Graphics g){
				if(listFieldInfos.size()<=0) return;
				super.paint(g);
			}
			
			public boolean trackwheelClick(int status, int time){
				onEnter();
				return true;
			}
		};
		listField.setCallback(this);
		listField.setRowHeight(Configuration.HEAD_NEWS_LIST_HEIGHT);
		listField.setSize(0);
		add(listField);
		
		MenuItem refreshMenuItem = new MenuItem("Refresh", 101, 1){
			public void run() {
				try{
					feed();
				}
				catch(IllegalStateException e){
					e.printStackTrace();
					Dialog.alert("Error : "+e.toString());
				}
			}
		};
		this.addMenuItem(refreshMenuItem);
		
		MenuItem recommenedMenuItem = new MenuItem("Recommened", 101, 2){
			public void run() {
				fireAction(ACTION_RECOMMENED);
			}
		};
		this.addMenuItem(recommenedMenuItem);
		
		
		popupActivity = new PopupScreen(new VerticalFieldManager()){
			final String CANCEL_FETCHING = "Cancle fetching?";
			final String INFORM = "Fecthing complete, background process is not require.";
			final String choices[] = {"Yes(Exit program)", "Fecthing in background", "Cancel"};
			final int values[] = {Dialog.YES, Dialog.SAVE, Dialog.CANCEL};
			final int defaultChoice = Dialog.CANCEL;
			final int selected[] = new int[1];
			
			protected boolean keyChar(char c, int status, int time){
				if(c==Keypad.KEY_ESCAPE){
					//onClose();
					//close();
					//Dialog.inform("Close application between feching the application will running in background. When feching is finished application will become foreground by automatically");
					//Status.show("Close application between feching the application will running in background. When finish feching application will become foreground by automatically");
					//UiApplication.getUiApplication().requestBackground();
					
					selected[0] = Dialog.ask(CANCEL_FETCHING, choices, values, defaultChoice);
					if(selected[0]==Dialog.YES){
						feedThread.stop();
						System.exit(1);
					}
					else if(selected[0]==Dialog.SAVE){
						if(feedThread.isRunning())
							UiApplication.getUiApplication().requestBackground();
						else
							Dialog.inform(INFORM);
					}
					return true;
				}
				return super.keyChar(c, status, time);
			}
		};

        gaugeField = new GaugeField(null, 0, 100, 0, GaugeField.PERCENT);
        popupActivity.add(new LabelField("Fetching..."));
        popupActivity.add(gaugeField);
		
		//feed();
	}
	
	public void feed(){
		if(CoverageInfo.isOutOfCoverage()){
			UiApplication.getUiApplication().invokeLater(new Runnable(){

				public void run() {
					Dialog.alert("กรุณาเชื่อมต่ออินเทอร์เน็ต เพื่อเข้าใช้งาน");
				}
				
			});
		}
		else if(feedThread==null || !feedThread.isRunning()){
			gaugeField.setValue(0);
			UiApplication.getUiApplication().pushScreen(popupActivity);
			
			feedThread = new FeedThread();
			feedThread.startFeed();
		}
	}
	
	protected void sublayout(int width, int height)
	{
		if(feedThread!=null && !feedThread.isRunning()){
		   if(UiApplication.getUiApplication().isForeground() &&
				   Display.getOrientation()== Display.ORIENTATION_LANDSCAPE)
		   {
			   fireAction(ACTION_CHANGE_VIEW);
		   }
		}
		super.sublayout(width, height);
	}
	
	private void onEnter(){
		int index = listField.getSelectedIndex();
		ListFieldInfo listInfo = (ListFieldInfo)listFieldInfos.elementAt(index);
		this.fireAction(ACTION_ENTER, listInfo);
	}
	
	public boolean onClose(){
		int selected = Dialog.ask(Dialog.D_YES_NO, "Exit EVERY?");
		if(selected==Dialog.YES)
			return super.onClose();
		return false;
	}
	
	private class FeedThread extends Thread{
		public boolean running = false;
		public void startFeed(){
			latitude = longitude = altitude =Double.NaN;
			running = true;
			start();
		}
		public void run(){
			// obtain Location
			int locationMode;
			if(DeviceInfo.isSimulator()){
				locationMode = GPSInfo.GPS_MODE_AUTONOMOUS;
			}
			else{
				locationMode = GPSInfo.GPS_MODE_CELLSITE;
			}
			BlackBerryCriteria myBlackBerryCriteria = new BlackBerryCriteria(locationMode);
    		try {
				BlackBerryLocationProvider myBlackBerryProvider = (BlackBerryLocationProvider)LocationProvider.getInstance(myBlackBerryCriteria);
				BlackBerryLocation myBlackBerryLoc = (BlackBerryLocation) myBlackBerryProvider.getLocation(500);
				latitude = myBlackBerryLoc.getQualifiedCoordinates().getLatitude();
				longitude = myBlackBerryLoc.getQualifiedCoordinates().getLongitude();
				altitude = myBlackBerryLoc.getQualifiedCoordinates().getAltitude();

    		} catch (LocationException e) {
				e.printStackTrace();
			}
    		catch(InterruptedException e){
    			e.printStackTrace();
    		}
//    		finally{
//    			if(latitude!=Double.NaN && longitude!=Double.NaN){
//    				UiApplication.getUiApplication().invokeLater(new Runnable(){
//
//						public void run() {
//							HomeScreen.this.add("Raw data");
//							HomeScreen.this.add("latitude="+latitude);
//							HomeScreen.this.add("longitude="+longitude);
//							HomeScreen.this.add("altitude="+altitude);
//							HomeScreen.this.add("");
//							
//							HomeScreen.this.add("Encrypt data");
//							HomeScreen.this.add("latitude="+  LocationEncryption.encrypt(latitude));
//							HomeScreen.this.add("longitude="+ LocationEncryption.encrypt(longitude));
//							HomeScreen.this.add("altitude="+  LocationEncryption.encrypt(altitude));
//							HomeScreen.this.add("");
//							
//							HomeScreen.this.add("Decrypt data");
//							HomeScreen.this.add("latitude="+  LocationEncryption.decrypt(LocationEncryption.encrypt(latitude)));
//							HomeScreen.this.add("longitude="+ LocationEncryption.decrypt(LocationEncryption.encrypt(longitude)));
//							HomeScreen.this.add("altitude="+  LocationEncryption.decrypt(LocationEncryption.encrypt(altitude)));
//							HomeScreen.this.add("");
//						}
//    					
//    				});
//    			}
//    		}
    		
    		// feed xml
    		if(listFieldInfos!=null){
    			listFieldInfos.removeAllElements();
    			//listField.setSize(0);
    		}
    		if(landscapeObjects!=null){
    			landscapeObjects.removeAllElements();
    		}
    		
			
			HttpConnection connection = null;
			InputStream inputStream = null;
			Document doc;
			Vector nodeInfos = new Vector();
			try {
				
				connection = connFactory.getHttpConnection(Configuration.FEED_API_URL, null, Configuration.FEED_API_PARAM.getBytes());
				
				if(connection.getResponseCode()==HttpConnection.HTTP_OK) {
					System.out.println("HTTP_OK");
					inputStream = connection.openInputStream();
					
					DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory. newInstance(); 
					DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
					docBuilder.isValidating();
					doc = docBuilder.parse(inputStream);
					doc.getDocumentElement ().normalize ();
					NodeList list=doc.getElementsByTagName("*");

					for (int i=0;i<list.getLength();i++){
						Node value=list.item(i).getChildNodes().item(0);
						String _node=list.item(i).getNodeName();
						String _element = null;
						if(value==null){
							_element=null;
						}
						else{
							_element=value.getNodeValue();
						}
						nodeInfos.addElement(new NodeInfo(_node, _element));
					}//end for
				}
			}
			catch(Exception ex){
				Dialog.alert("Connection Error please check your internet connection");
				stop();
			}
			finally{
				if(inputStream!=null)
					try{inputStream.close();}catch(IOException e){e.printStackTrace();}
			}
			int length = nodeInfos.size();
			progress = 0;
			Enumeration _enum = nodeInfos.elements();
			while(_enum.hasMoreElements()){
				NodeInfo info = (NodeInfo)_enum.nextElement();
				progress++;
				if(info.name.equalsIgnoreCase("item")){
					int count = 0;
					// read only 7 elements name [<type>,<title>,<link>,<pubDate>,<guid>,<pic>,<thb>]
					String type="",title="", url="", date="", guid="", pic="", thb="";
					while(count<7 && _enum.hasMoreElements()){
						info = (NodeInfo) _enum.nextElement();
						if(info.name.equalsIgnoreCase("type")){
							type = info.value;
							count++;
						}
						else if(info.name.equalsIgnoreCase("title")){
							title = info.value;
							count++;
						}
						else if(info.name.equalsIgnoreCase("link")){
							url = info.value;
							count++;
						}
						else if(info.name.equalsIgnoreCase("pubDate")){
							date = info.value;
							DateFormat sdf = new SimpleDateFormat("d MMM yyyy : HH.ss", Locale.getDefaultForSystem());
							long parsed = HttpDateParser.parse(date);
							DateField df = new DateField("", parsed, sdf);
							date = df.toString();
							count++;
						}
						else if(info.name.equalsIgnoreCase("guid")){
							guid = info.value;
							count++;
						}
						else if(info.name.equalsIgnoreCase("pic")){
							pic = info.value;
							count++;
						}
						else if(info.name.equalsIgnoreCase("thb")){
							thb = info.value;
							count++;
						}
						progress++;
					}
					
					if(type.equalsIgnoreCase("news")){
						// Create thumpnail bitmap via http.
						Bitmap bitmap = createBitmapViaHttp(thb);
						// Create List item.
						ListFieldInfo f = new ListFieldInfo(bitmap, title, date, url, guid);
						synchronized(listFieldInfos){
							listFieldInfos.addElement(f);
							UiApplication.getUiApplication().invokeLater(new Runnable(){
								public void run() {
									listField.setSize(listFieldInfos.size());
								}
							});
						}
						
						LandscapeBitmapThread bitmapThread = new LandscapeBitmapThread(pic, title, date, url);
						bitmapThread.start();
					}
				}
				gaugeField.setValue(100*progress/length);
				//UiApplication.getUiApplication().repaint();
			}
			
			
			
			if(running)
				gaugeField.setValue(100);
			// feed done.
			running = false;
			
			UiApplication.getUiApplication().invokeLater(new Runnable(){
				public void run() {
					if(popupActivity.isDisplayed()){
						popupActivity.close();	
					}
					if(Display.getOrientation()==Display.ORIENTATION_LANDSCAPE){
						fireAction(ACTION_CHANGE_VIEW, null, true);
					}
				}
			});
			
			
			
			if(!UiApplication.getUiApplication().isForeground()){
				UiApplication.getUiApplication().requestForeground();
			}
		}
		
		private void stop(){
			running = false;
		}
		
		public boolean isRunning(){
			return running;
		}
		
		
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
				bitmap = Bitmap.createBitmapFromBytes(data, 0, data.length, 1);
			}
		}
		return bitmap;
	}
	
	private Vector wrap (String text, int width) {
		Vector result = new Vector ();
		if (text ==null)
		   return result;
	 
		boolean hasMore = true;
	 
		// The current index of the cursor
		int current = 0;
	 
		// The next line break index
		int lineBreak = -1;
	 
		// The space after line break
		int nextSpace = -1;
	 
		while (hasMore) 
		{
		   //Find the line break
		   while (true) 
		   {
			   lineBreak = nextSpace;
			   if (lineBreak == text.length() - 1) 
			   {
				   // We have reached the last line
				   hasMore = false;
				   break;
			   } 
			   else 
			   {
				   nextSpace = text.indexOf(' ', lineBreak+1);
				   if (nextSpace == -1)
					  nextSpace = text.length() -1;
				   int linewidth = this.getFont().getAdvance(text,current, nextSpace-current);
				   // If too long, break out of the find loop
				   if (linewidth > width) 
					  break;
			   }
		  }
		  String line = text.substring(current, lineBreak + 1);
		  result.addElement(line);
		  current = lineBreak + 1;
		}
		return result;
	}

	public void drawListRow(ListField listField, Graphics g, int index, int y, int width) {
		if(listFieldInfos==null) return;
		
		//System.out.println(">>>>>>>>>>HomeScreen.drawListRow()");
		
		if(listFieldInfos.size()>index){
			ListFieldInfo info = (ListFieldInfo)listFieldInfos.elementAt(index);
			if(info!=null){
				int padding = (Configuration.HEAD_NEWS_LIST_HEIGHT - Configuration.HEAD_NEWS_THUMBNAIL_HEIGHT)>>1;
				if(info.bitmap!=null){
					g.drawBitmap(padding,
							y+padding,
							Configuration.HEAD_NEWS_THUMBNAIL_WIDTH,
							Configuration.HEAD_NEWS_THUMBNAIL_HEIGHT,
							info.bitmap, 0, 0);
					
					g.setColor(0);
					g.setFont(Configuration.BIG_FONT);
					int hx = Configuration.HEAD_NEWS_THUMBNAIL_WIDTH + (padding<<1);
					int hy = y+padding;
					int textwidth = width - Configuration.HEAD_NEWS_THUMBNAIL_HEIGHT - (padding*3);
					Vector lines = wrap(info.header.trim(), textwidth);
					for (int i = 0; i < lines.size()&&i<2; i++) 
					{
					      int liney = hy + (i * this.getFont().getHeight());
					      g.drawText((String)lines.elementAt(i), hx ,liney,DrawStyle.TOP | DrawStyle.LEFT | DrawStyle.ELLIPSIS, textwidth);
					}
					
					g.setColor(0x763b19);
					g.setFont(Configuration.MEDIUM_FONT);
					g.drawText(info.date, hx, y+Configuration.HEAD_NEWS_LIST_HEIGHT-padding, DrawStyle.LEFT|DrawStyle.BOTTOM);
					
					g.setColor(0x5c5c5c);
					g.drawLine(0, y+Configuration.HEAD_NEWS_LIST_HEIGHT-1, width, y+Configuration.HEAD_NEWS_LIST_HEIGHT-1);
				}
			}
		}
	}

	public Object get(ListField listField, int index) {
		if(listFieldInfos.size()>index)
			return listFieldInfos.elementAt(index);
		return null;
	}

	public int getPreferredWidth(ListField listField) {
		return Display.getWidth();
	}

	public int indexOfList(ListField listField, String prefix, int start) {
		if(start>=listFieldInfos.size()) return -1;
		return listFieldInfos.indexOf(prefix, start);
	}
	
	public class ListFieldInfo{
		public Bitmap bitmap = null;
		public String header, date, url;
		public String guid;
		public ListFieldInfo(Bitmap bitmap, String header, String date, String url, String guid){
			this.bitmap = bitmap;
			this.header = header;
			this.date = date;
			this.url = url;
			this.guid = guid;
		}
	}
	
	public class NodeInfo{
		public String name, value;
		public NodeInfo(String name, String value){
			this.name = name;
			this.value = value;
		}
	}
	
	private class LandscapeBitmapThread extends Thread{
		String bitmapURL, title, date, url;
		LandscapeObject landscapeObject;
		
		public LandscapeBitmapThread(String bitmapURL, String title, String date, String url){
			this.bitmapURL = bitmapURL;
			this.title = title;
			this.date = date;
			this.url = url;
			
			Bitmap landscapeBitmap = new Bitmap(0, 0);
			landscapeObject = new LandscapeObject(landscapeBitmap, title, date, url);
			landscapeObjects.addElement(landscapeObject);
		}
		
		private Bitmap createBitmapViaHttp(String url) {
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
					bitmap = Bitmap.createBitmapFromBytes(data, 0, data.length, 1);
				}
			}
			return bitmap;
		}
		
		public void run(){
			landscapeObject.bitmap = createBitmapViaHttp(bitmapURL);
		}
	}
}

class LandscapeObject{
	public Bitmap bitmap;
	public String header;
	public String date;
	public String url;
	public LandscapeObject(Bitmap bitmap, String header, String date, String url){
		this.bitmap = bitmap;
		this.header = header;
		this.date = date;
		this.url = url;
	}
}