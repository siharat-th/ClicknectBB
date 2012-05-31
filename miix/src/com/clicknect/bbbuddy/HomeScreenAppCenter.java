package com.clicknect.bbbuddy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.io.LineReader;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.ApplicationManagerException;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.Background;
import net.rim.device.api.ui.decor.BackgroundFactory;

import com.jimmysoftware.network.HttpClient;
import com.jimmysoftware.network.HttpConnectionFactory;
import com.jimmysoftware.ui.ActionScreen;

public class HomeScreenAppCenter extends ActionScreen implements ListFieldCallback {
	public static final String ACTION_ENTER = "open detail screen";
	public static final String ACTION_NEW_FEED = "open store page";
	private static final String DEFAULT_PATH = "file:///store/home/user/miix/";
	
	public static Font fontNormal;
	public static Font fontNormalBold;
	//public static Font fontBigBold;
	public static Font fontSmall;
	VerticalFieldManager vfm;
	ListField listField;
	Vector listInfos;
	int listHeight;
	Bitmap bmpUpdateNotify, bmpInstall, bmpUpdate, bmpRun, bmpBBbuddy, bmpDoodle;
	
	
	public HomeScreenAppCenter(){
		super(true, Manager.NO_VERTICAL_SCROLL);
		
		bmpUpdateNotify = Bitmap.getBitmapResource("upgrade_available.png");
		bmpInstall = Bitmap.getBitmapResource("install.png");
		bmpUpdate = Bitmap.getBitmapResource("update.png");
		bmpRun = Bitmap.getBitmapResource("run.png");
		bmpBBbuddy = Bitmap.getBitmapResource("bbbuddy.png");
		bmpDoodle = Bitmap.getBitmapResource("doodle.png");
		
		int h = Font.getDefault().getHeight();
		fontNormal = Font.getDefault().derive(Font.PLAIN, h);
		fontNormalBold = Font.getDefault().derive(Font.BOLD, h);
		//fontBigBold = Font.getDefault().derive(Font.BOLD, (int)(h*1.25f));
		int hSmall = (int)(h*0.7f);
		if(hSmall<12) hSmall=12;
		fontSmall = Font.getDefault().derive(Font.PLAIN, hSmall);
		setFont(fontNormal);
		
		Bitmap bitmap = Bitmap.getBitmapResource("background.png");
		getMainManager().setBackground(BackgroundFactory.createBitmapBackground(bitmap, Background.POSITION_X_LEFT, Background.POSITION_Y_TOP, Background.REPEAT_SCALE_TO_FIT));
	
		vfm = new VerticalFieldManager(Manager.VERTICAL_SCROLL);
		add(vfm);
		
		listInfos = BBbuddy.getListInfos();
		listField = new ListField(){
			public boolean navigationClick(int status, int time){
				ListInfo listInfo = (ListInfo)listInfos.elementAt(getSelectedIndex());
				if(getSelectedIndex()==0){
					if(listInfo.hasUpdate())
						fireAction(ACTION_ENTER, listInfo);
					else{
						// online buddy
						fireAction(ACTION_NEW_FEED, listInfos);
					}
				}
				else if(listInfo.runWhenClick()){
					String moduleName = CodeModuleManager.getModuleName(listInfo.handle);
					try {
						ApplicationManager.getApplicationManager().launchApplication(moduleName);
					} catch (ApplicationManagerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else
					fireAction(ACTION_ENTER, listInfo);
				return true;
			}
		};
		listField.setCallback(this);
		
		int maxPossibleHeight = h+hSmall+10+bmpInstall.getHeight();
		listHeight = maxPossibleHeight > 70 ? maxPossibleHeight : 70;
		listField.setRowHeight(listHeight);
		vfm.add(listField);
		
//		ButtonField updateButton = new ButtonField("Checking Update", Manager.USE_ALL_WIDTH|Field.FIELD_HCENTER){
//			public boolean navigationClick(int status, int time){
//				readFromWeb();
//				return true;
//			}
//		};
//		
//		updateButton.setMargin(5, 0, 0, 0);
//		vfm.add(updateButton);
		
		createFolder("");
		createFolder("thumb/");
		createFolder("img/");
		loadList();
	}
	
	public void loadList(){
		listInfos.removeAllElements();
		byte[] feedData = readFile("feed.txt");
		InputStream stream = null;
		try {
			stream = new ByteArrayInputStream(feedData);
			LineReader lineReader = new LineReader(stream);
			byte b[] = null;
			b = lineReader.readLine();
			int idx = 0;
			while(b!=null){
				String line = new String(b);
				if(line.startsWith("feedversion")){
					int index = line.indexOf('=');
					String feedversion = line.substring(index+1);
				}
				else if(line.startsWith("appname")){
					int index = line.indexOf('=');
					String appname = line.substring(index+1).trim();
					
					line = new String(lineReader.readLine());
					index = line.indexOf('=');
					String modulename = line.substring(index+1).trim();
					
					line = new String(lineReader.readLine());
					index = line.indexOf('=');
					String version = line.substring(index+1).trim();
					
					line = new String(lineReader.readLine(), "utf-8");
					index = line.indexOf('=');
					String sdesc = line.substring(index+1);
					
					line = new String(lineReader.readLine());
					index = line.indexOf('=');
					String icon = line.substring(index+1).trim();
					
					line = new String(lineReader.readLine());
					index = line.indexOf('=');
					String img = line.substring(index+1).trim();
					
					line = new String(lineReader.readLine());
					index = line.indexOf('=');
					String url = line.substring(index+1).trim();
					
					ListInfo listInfo = new ListInfo();
					listInfo.name = appname;
					listInfo.moduleName = modulename;
					listInfo.icon = loadBitmap(icon);
					if(listInfo.icon==null){
						loadBitmapViaHttp(listInfo.icon,icon);
					}
					listInfo.imgUrl = img;
					listInfo.url = url;
					listInfo.s_desc = sdesc;
					listInfo.updateversion = version;
					
					int handle = CodeModuleManager.getModuleHandle(modulename);
					listInfo.isInstalled = handle!=0;
					listInfo.handle = handle;
					if(handle!=0){
						listInfo.moduleCodeSize = CodeModuleManager.getModuleCodeSize(handle);
						listInfo.vendor = CodeModuleManager.getModuleVendor(handle);
						listInfo.version = CodeModuleManager.getModuleVersion(handle);
					}
					
					String descFilename = modulename+".txt";
					byte data[] = readFile(descFilename);
					listInfo.desc = new String(data, "utf-8");
					
					listInfos.addElement(listInfo);
					idx++;
				}
				b = lineReader.readLine();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		finally{
			try{if(stream!=null) stream.close();}catch(IOException e){};
		}
		int _size = listInfos.size();
		listField.setSize(_size);
	}
	
	private void loadBitmapViaHttp(final Bitmap icon,final String url) {
		Thread t = new Thread(new Runnable(){

			public void run() {
				HttpConnectionFactory conn = new HttpConnectionFactory();
				HttpClient client = new HttpClient(conn);
				try {
					StringBuffer buff = client.doGet(url);
					byte data[] = buff.toString().getBytes();
					if(data!=null){
						Bitmap bmp = Bitmap.createBitmapFromPNG(data, 0, data.length);
						icon.setRGB565(data, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
		t.start();
	}

	private void showStatus(final String message, final int time) {
		UiApplication.getUiApplication().invokeLater(new Runnable(){

			public void run() {
				Status.show(message, time);
			}
			
		});
	}
	
	private void createFolder(String folder){
		String fullpath = DEFAULT_PATH + folder;
		FileConnection fc = null;
		try {
			fc = (FileConnection)Connector.open(fullpath, Connector.READ_WRITE);
			if(!fc.exists()){
				fc.mkdir();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			if(fc!=null) try{fc.close();}catch(IOException e){e.printStackTrace();}
		}
	}
	
	private void writeFile(byte data[], String filename){
		String fullpath = DEFAULT_PATH+filename;
		FileConnection fc = null;
		OutputStream os = null;
		try {
			fc = (FileConnection)Connector.open(fullpath, Connector.READ_WRITE);
			if(!fc.exists()){
				fc.create();
			}
			else{
				// file exists replace all from beginning of file.
				fc.truncate(0);
			}
				
		    // write data to file
			os = fc.openOutputStream();
			os.write(data);
			os.flush();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			if(os!=null) try{os.close();}catch(IOException e){e.printStackTrace();}
			if(fc!=null) try{fc.close();}catch(IOException e){e.printStackTrace();}
		}
	}
	
	private byte[] readFile(String filename){
		// (1)read from storage
		String fullpath = DEFAULT_PATH+filename;
		FileConnection fc = null;
		InputStream is = null;
		byte data[] = null;
		try {
			fc = (FileConnection)Connector.open(fullpath, Connector.READ);
			is = fc.openInputStream();
			data = IOUtilities.streamToBytes(is);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			if(is!=null) try{is.close();}catch(IOException e){e.printStackTrace();}
			if(fc!=null && fc.isOpen()) try{fc.close();}catch(IOException e){e.printStackTrace();}
		}
		
		// (2)read from bundle
		if(data==null){
			is = null;
			try {
				is = getClass().getResourceAsStream("/"+filename);
				data = IOUtilities.streamToBytes(is);
				if(data!=null){
					writeFile(data, filename);
				}
			}catch(IOException e){
				e.printStackTrace();
			}
			finally{
				if(is!=null) try{is.close();}catch(IOException e){e.printStackTrace();}
			}
		}
		
		return data;
	}
	
	public void readFromWeb(){
		
	}
	
	protected void onFocusNotify(boolean focus){
		if(focus){
			UiApplication.getUiApplication().invokeLater(new Runnable(){

				public void run() {
					synchronized(listInfos){
						//listInfos = BBbuddy.getListInfos();
						for(int i=0; i<listInfos.size(); i++){
							ListInfo listInfo = (ListInfo)listInfos.elementAt(i);
							listInfo.handle = CodeModuleManager.getModuleHandle(listInfo.moduleName);
							if(listInfo.handle!=0){
								listInfo.isInstalled = true;
								listInfo.moduleCodeSize = CodeModuleManager.getModuleCodeSize(listInfo.handle);
								listInfo.vendor = CodeModuleManager.getModuleVendor(listInfo.handle);
								listInfo.version = CodeModuleManager.getModuleVersion(listInfo.handle);
							}
						}
						listField.setSize(listInfos.size());
					}
				}
				
			});
//			listField.setSize(0);
//			synchronized(listInfos){
//				listInfos = BBbuddy.getListInfos();
//				for(int i=0; i<listInfos.size(); i++){
//					ListInfo listInfo = (ListInfo)listInfos.elementAt(i);
//					listInfo.handle = CodeModuleManager.getModuleHandle(listInfo.moduleName);
//					if(listInfo.handle!=0){
//						listInfo.isInstalled = true;
//						listInfo.moduleCodeSize = CodeModuleManager.getModuleCodeSize(listInfo.handle);
//						listInfo.vendor = CodeModuleManager.getModuleVendor(listInfo.handle);
//						listInfo.version = CodeModuleManager.getModuleVersion(listInfo.handle);
//					}
//				}
//				listField.setSize(listInfos.size());
//			}
		}
	}
	
	private Bitmap loadBitmap(String filename){
		Bitmap bmp = null;
		byte data[] = readFile(filename);
		if(data!=null){
			bmp = Bitmap.createBitmapFromPNG(data, 0, data.length);
		}
		
		return bmp;
	}

	public void drawListRow(ListField listField, Graphics g, int index, int y, int width) {
		// TODO Auto-generated method stub
		ListInfo listInfo = (ListInfo)listInfos.elementAt(index);
		if(index==0){
			if(listField.getSelectedIndex()!=index){
				g.setColor(0xffffff);
				g.fillRect(0, y, width, listHeight);
				g.drawBitmap(0, y, width, listHeight, bmpDoodle, 0, 0);
			}
			int w = bmpBBbuddy.getWidth();
			int h = bmpBBbuddy.getHeight();
			g.drawBitmap(5, y+5, w, h, bmpBBbuddy, 0, 0);
			if(listInfo.hasUpdate()){
				g.drawBitmap(5, y+5, 60, 60, bmpUpdateNotify, 0, 0);
			}
			g.setColor(0x555555);
			int aY = y+listHeight-1;
			g.drawLine(0, aY, width, aY);
		}
		else{
			if(listInfo.icon!=null)
				g.drawBitmap(5, y+5, 60, 60, listInfo.icon, 0, 0);
			if(listInfo.hasUpdate()){
				g.drawBitmap(5, y+5, 60, 60, bmpUpdateNotify, 0, 0);
			}
			g.setColor(0xffffff);
			g.setFont(fontNormalBold);
			int aX = 70;
			int aY = y+5;
			g.drawText(listInfo.name, aX, aY, DrawStyle.LEADING);
			
			aY+=fontNormalBold.getHeight();
			g.setFont(fontSmall);
			int selectIndex = listField.getSelectedIndex();
			if(index==selectIndex){
				g.setColor(0xffffff);
			}
			else{
				g.setColor(0x999999);
//				g.setColor(0x222222);
//				g.drawText(listInfo.s_desc, aX+1, aY+1, DrawStyle.ELLIPSIS);
//				g.setColor(0x999999);
//				g.setColor(0xbbbbbb);
			}
			//String installStatus = listInfo.isInstalled ? "Installed" : "Not install";
			g.drawText(listInfo.s_desc, aX, aY, DrawStyle.ELLIPSIS);
	//		if(listInfo.isInstalled){
	//			int size_in_kb = listInfo.moduleCodeSize/1024;
	//			String size;
	//			if(size_in_kb>=1000.0f){
	//				float size_in_mb = size_in_kb/1024.0f;
	//				size = size_in_mb+"";
	//				int idx = size.indexOf('.');
	//				size = size.substring(0, idx+2) + " MB";
	//			}
	//			else{
	//				size = size_in_kb + " KB";
	//			}
	//			int axX = width - 5 - fontNormal.getAdvance(size);
	//			g.drawText(size, axX, aY);
	//		}
			
	//		if(listInfo.hasUpdate){
	//			aY=y+listHeight-5-fontNormal.getHeight();
	//			g.setColor(0xffda00);
	//			g.setFont(fontNormal);
	//			g.drawText("Upgrade Available", aX, aY);
	//		}
			
			Bitmap button;
			if(listInfo.isInstalled){
				if(listInfo.hasUpdate()){
					button = bmpUpdate;
				}
				else{
					button = bmpRun;
				}
			}
			else{
				button = bmpInstall;
			}
			
			int bx = Display.getWidth() - 5 - button.getWidth();
			int by = y + listHeight - 5 - button.getHeight();
			g.drawBitmap(bx, by, button.getWidth(), button.getHeight(), button, 0, 0);
			
			g.setColor(0x555555);
			aY = y+listHeight-1;
			g.drawLine(0, aY, width, aY);
		}
	}

	public Object get(ListField listField, int index) {
		return listInfos.elementAt(index);
	}

	public int getPreferredWidth(ListField listField) {
		return Display.getWidth();
	}

	public int indexOfList(ListField listField, String prefix, int start) {
		return listInfos.indexOf(prefix, start);
	}
}
