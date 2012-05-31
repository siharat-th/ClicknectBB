package net.clicknect.horoscope;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.CoverageInfo;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.decor.Background;
import net.rim.device.api.ui.decor.BackgroundFactory;

import com.jimmysoftware.network.HttpConnectionFactory;
import com.jimmysoftware.ui.ActionScreen;

public class SplashScreen extends ActionScreen {
	public static final String ACTION_FETCH_DONE = "Fetch horo done!";
	private static SplashScreen instance;

	private HoroApi horoApi;
	private boolean threadRunning = false;
	
	public SplashScreen(HttpConnectionFactory connFactory){
		super(false);
		horoApi = new HoroApi(connFactory);
		
		Background bg = BackgroundFactory.createBitmapBackground(Bitmap.getBitmapResource("splash.png"),
				Background.POSITION_X_CENTER,
				Background.POSITION_Y_TOP,
				Background.REPEAT_SCALE_TO_FIT);
		getMainManager().setBackground(bg);
		
		instance = this;
	}
	
	final String strFetching = "Now Fetching!, please wait...";
	public void paint(Graphics g){
		super.paint(g);
		if(threadRunning){
			int strWidth = g.getFont().getAdvance(strFetching);
			int x = (Display.getWidth()-strWidth)/2;
			int y = (Display.getHeight()-g.getFont().getHeight())/2;
			
			g.setColor(0x38a7ed);
			g.setGlobalAlpha(96);
			g.fillRect(0, Display.getHeight()/2-18, Display.getWidth(), 36);
			g.setColor(0xffffff);
			g.setGlobalAlpha(255);
			g.fillRect(0, Display.getHeight()/2-20, Display.getWidth(), 2);
			g.fillRect(0, Display.getHeight()/2+18, Display.getWidth(), 2);
			
			g.drawText(strFetching, x, y+1);
			g.setColor(0);
			g.drawText(strFetching, x, y);
		}
	}
	
	public void startFetch() {
		if(CoverageInfo.isOutOfCoverage()){
			UiApplication.getUiApplication().invokeLater(new Runnable(){

				public void run() {
					Dialog.alert("กรุณาเชื่อมต่ออินเทอร์เน็ต เพื่อเข้าใช้งาน");
				}
				
			});
		}
		else{
			threadRunning = true;
			Thread t = new FetchThread();
			t.start();
		}
	}
	
	public boolean keyChar(char c, int status, int time){
		if(c==Keypad.KEY_ESCAPE){
			int selected = Dialog.ask(Dialog.D_YES_NO, "Exit Application?");
			if(selected==Dialog.YES){
				System.exit(1);
			}
			return true;
		}
		
		return super.keyChar(c, status, time);
	}
	
	private class FetchThread extends Thread{
		public void run(){
			Vector zodiacData = new Vector();
			InputStream is_local = null;
			InputStream is = null;
			boolean done = true;
			try{
				// read temp data first
				is_local = horoApi.getOfflineServiceListHoro();
				Vector nodes = horoApi.parseXMLAsVector(is_local);
				Enumeration enum = nodes.elements();
				while(enum.hasMoreElements()){
					XMLNode node = (XMLNode)enum.nextElement();
					if(node.getNode().equalsIgnoreCase("row")){
						XMLNode nodeId = (XMLNode)enum.nextElement();
						XMLNode nodeTitle = (XMLNode)enum.nextElement();
						XMLNode nodeDesc = (XMLNode)enum.nextElement();
						
						ZodiacData data = new ZodiacData(); 
						data.id = nodeId.getElement();
						data.title = nodeTitle.getElement();
						data.description = nodeDesc.getElement();
						zodiacData.addElement(data);
					}
				}
				
				is = horoApi.getServiceListHoro();
				nodes = horoApi.parseXMLAsVector(is);
				enum = nodes.elements();
				while(enum.hasMoreElements()){
					XMLNode node = (XMLNode)enum.nextElement();
					if(node.getNode().equalsIgnoreCase("row")){
						XMLNode nodeId = (XMLNode)enum.nextElement();
						XMLNode nodeTitle = (XMLNode)enum.nextElement();
						XMLNode nodeDesc = (XMLNode)enum.nextElement();
						
						ZodiacData data = new ZodiacData(); 
						data.id = nodeId.getElement();
						data.title = nodeTitle.getElement();
						data.description = nodeDesc.getElement();
						
						// replace with online data
						int size = zodiacData.size();
						for(int i=0; i<size; i++){
							ZodiacData _data = (ZodiacData)zodiacData.elementAt(i);
							if(_data.id.equalsIgnoreCase(data.id)){
								zodiacData.removeElementAt(i);
								zodiacData.insertElementAt(data, i);
								break;
							}
						}
					}
				}
			}
			catch(Exception e){
				done = false;
				e.printStackTrace();
				errorDialog("Fetching error :"+e.toString());
			}
			finally{
				if(is!=null){
					try{is.close();}catch(IOException e){e.printStackTrace();}
				}
				if(is_local!=null){
					try{is_local.close();}catch(IOException e){e.printStackTrace();}
				}
				
				threadRunning = false;
				if(done){
					// save data to storage
					horoApi.saveZodiacDataToStorage(zodiacData);
					onFetchDone(zodiacData);
				}
			}
		}
	}

	public static void errorDialog(final String message){
		UiApplication.getUiApplication().invokeAndWait(new Runnable(){

			public void run() {
				Dialog.alert("Error: "+message);
			}
			
		});
	}
	
	public static void informDialog(final String message){
		UiApplication.getUiApplication().invokeAndWait(new Runnable(){

			public void run() {
				Dialog.inform("Information: "+message);
			}
			
		});
	}
	
	public static void onFetchDone(Vector horoData){
		synchronized(UiApplication.getEventLock()){
			instance.fireAction(ACTION_FETCH_DONE, horoData);
		}
	}
}
