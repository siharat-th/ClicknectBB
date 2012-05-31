package com.jimmysoftware.quickdict;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import com.clicknect.webapi.Activate;
import com.googlecode.toolkits.stardict.StarDict;
import com.jimmysoftware.quickdict.WebDownload.DownloadListener;
import com.jimmysoftware.ui.Action;
import com.jimmysoftware.ui.ActionListener;
import com.jimmysoftware.ui.PINContactScreen;
import com.jimmysoftware.ui.RegisterScreen;

import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.applicationcontrol.ApplicationPermissionsManager;
import net.rim.device.api.io.File;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.ApplicationManagerException;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.GaugeField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

public class QuickDict extends UiApplication implements ActionListener, DownloadListener{
	//private RegisterScreen registerScreen;
	private HomeScreen homeScreen;
	private WordScreen wordScreen;
	private ExplanationScreen explanationScreen;
	//private PINContactScreen contactScreen;
	private StarDict starDict;
	private DownloadThread downloadThread=null;
	/**
	 * @param args
	 */
	public static void main(String[] args) {  
		// TODO Auto-generated method stub
		QuickDict app = new QuickDict();
		app.enterEventDispatcher();
	}
	
	private void checkPermissions() {
		// TODO Auto-generated method stub
		ApplicationPermissionsManager apm = ApplicationPermissionsManager.getInstance();
		ApplicationPermissions original = apm.getApplicationPermissions();

		if ((original.getPermission(ApplicationPermissions.PERMISSION_INPUT_SIMULATION) == ApplicationPermissions.VALUE_ALLOW)
				//&& (original.getPermission(ApplicationPermissions.PERMISSION_DEVICE_SETTINGS) == ApplicationPermissions.VALUE_ALLOW)
				//&& (original.getPermission(ApplicationPermissions.PERMISSION_CROSS_APPLICATION_COMMUNICATION) == ApplicationPermissions.VALUE_ALLOW)
				&& (original.getPermission(ApplicationPermissions.PERMISSION_INTERNET) == ApplicationPermissions.VALUE_ALLOW)
				&& (original.getPermission(ApplicationPermissions.PERMISSION_SERVER_NETWORK) == ApplicationPermissions.VALUE_ALLOW)
				//&& (original.getPermission(ApplicationPermissions.PERMISSION_EMAIL) == ApplicationPermissions.VALUE_ALLOW)
				&& (original.getPermission(ApplicationPermissions.PERMISSION_BROWSER_FILTER) == ApplicationPermissions.VALUE_ALLOW) 
				&&(original.getPermission(ApplicationPermissions.PERMISSION_FILE_API)==ApplicationPermissions.VALUE_ALLOW)
				//&&(original.getPermission(ApplicationPermissions.PERMISSION_SECURITY_DATA)==ApplicationPermissions.VALUE_ALLOW)
				//&&(original.getPermission(ApplicationPermissions.PERMISSION_ORGANIZER_DATA)==ApplicationPermissions.VALUE_ALLOW)
				&&(original.getPermission(ApplicationPermissions.PERMISSION_WIFI)==ApplicationPermissions.VALUE_ALLOW)
		) {
			return;
		}

		ApplicationPermissions permRequest = new ApplicationPermissions();
		permRequest.addPermission(ApplicationPermissions.PERMISSION_INPUT_SIMULATION);
		//permRequest.addPermission(ApplicationPermissions.PERMISSION_DEVICE_SETTINGS);
		//permRequest.addPermission(ApplicationPermissions.PERMISSION_CROSS_APPLICATION_COMMUNICATION);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_INTERNET);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_SERVER_NETWORK);
		//permRequest.addPermission(ApplicationPermissions.PERMISSION_EMAIL);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_BROWSER_FILTER);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_FILE_API);
		//permRequest.addPermission(ApplicationPermissions.PERMISSION_SECURITY_DATA);
		//permRequest.addPermission(ApplicationPermissions.PERMISSION_ORGANIZER_DATA);
		//permRequest.addPermission(ApplicationPermissions.PERMISSION_MEDIA);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_WIFI);
		

		boolean acceptance = ApplicationPermissionsManager.getInstance().invokePermissionsRequest(permRequest);

		if (acceptance) {
			// User has accepted all of the permissions.
			return;
		} else {
		}
	}
	
	public QuickDict(){
		checkPermissions();
		homeScreen = new HomeScreen();
		homeScreen.addActionListener(this);
		
		
		// for app world
//		pushScreen(homeScreen);
//		Thread t = new StarDictThread();
//		t.start();
		
//		pushScreen(homeScreen);
//		
//		invokeLater(new Runnable(){
//			public void run() {
//				starDict = new StarDict();
//				if(starDict.exist()){
//					wordScreen = new WordScreen(starDict);
//					explanationScreen = new ExplanationScreen(starDict);
//					wordScreen.addActionListener(QuickDict.this);
//					explanationScreen.addActionListener(QuickDict.this);
//				}
//				homeScreen.fireAction(HomeScreen.ACTION_ENTER);
//			}
//		});
		
		pushScreen(homeScreen);
		starDict = new StarDict();
		if(starDict.exist()){
			wordScreen = new WordScreen(starDict);
			explanationScreen = new ExplanationScreen(starDict);
			wordScreen.addActionListener(QuickDict.this);
			explanationScreen.addActionListener(QuickDict.this);
		}
		homeScreen.fireAction(HomeScreen.ACTION_ENTER);
		
		Activate atv = new Activate();
		atv.request();
	}
	
	public class StarDictThread extends Thread{
		public void run(){
			starDict = new StarDict();
			if(starDict.exist()){
				wordScreen = new WordScreen(starDict);
				explanationScreen = new ExplanationScreen(starDict);
				wordScreen.addActionListener(QuickDict.this);
				explanationScreen.addActionListener(QuickDict.this);
			}
			
			
			synchronized(UiApplication.getEventLock()){
				homeScreen.fireAction(HomeScreen.ACTION_ENTER);
			}
			
		}
	}
	
	public boolean hasSDCard(){
		boolean _hasSDCard = false;
		String des = StarDict.DICT_FOLDER;
		FileConnection fconn=null;
		try {
			fconn = (FileConnection)Connector.open(des, Connector.READ_WRITE);
			if(!fconn.exists()){
				fconn.mkdir();
			}
			_hasSDCard = true;
		}
		catch (IOException e) {
			e.printStackTrace();
			_hasSDCard = false;
		}
		finally{
			if(fconn!=null && fconn.isOpen())
				try{fconn.close();}catch(IOException ex){ex.printStackTrace();}
		}
		return _hasSDCard;
	}
	
	//private final String CLICKCONNECT_FOLDER = "file:///store/home/user/clickconnect/";
	private final String SOURCE_FOLDER = StarDict.DICT_FOLDER;
	private final int LEXITRON_FILESIZE_IFO = StarDict.LEXITRON_FILESIZE_IFO;
	private final int LEXITRON_FILESIZE_YAIDX = StarDict.LEXITRON_FILESIZE_YAIDX;
	private final int LEXITRON_FILESIZE_IDX = StarDict.LEXITRON_FILESIZE_IDX;
	private final int LEXITRON_FILE_SIZE_DZ = 5200374;
	
	public void doDownload(String extension){
		String fullpath = SOURCE_FOLDER + "lexitron" + extension;
		String url = "http://www.mozeal.com/dict/lexitron"+extension;
		
		// check 3 split file
		if(extension.equalsIgnoreCase(".dict.dz")){
			boolean b = isExist(SOURCE_FOLDER + "lexitron.dict.p0.gz");
			b|=isExist(SOURCE_FOLDER + "lexitron.dict.p1.gz");
			b|=isExist(SOURCE_FOLDER + "lexitron.dict.p2.gz");
			
			if(b){
				byte p0[] = readFile(SOURCE_FOLDER + "lexitron.dict.p0.gz");
				byte p1[] = readFile(SOURCE_FOLDER + "lexitron.dict.p1.gz");
				byte p2[] = readFile(SOURCE_FOLDER + "lexitron.dict.p2.gz");
				int totalSizeInbyte = p0.length + p1.length + p2.length;
				byte dzdata[] = new byte[totalSizeInbyte];
				System.arraycopy(p0, 0, dzdata, 0, p0.length);
				System.arraycopy(p1, 0, dzdata, p0.length, p1.length);
				System.arraycopy(p2, 0, dzdata, p0.length+p1.length, p2.length);
				
				writeFile(StarDict.DICT_LEXITRON+".dict.gz", dzdata);
			}
		}
		
		boolean fromDeviceStorage = false;
		if(isExist(fullpath)){
			if(isValidFilesize(extension)){
				url = fullpath;
				fromDeviceStorage = true;
			}
		}
		
		if(extension.equalsIgnoreCase(".dict.dz")){
			boolean fromMediaCard = isExist(StarDict.DICT_LEXITRON+".dict.gz");
			if(fromMediaCard){
				url = StarDict.DICT_LEXITRON+".dict.gz";
				fromDeviceStorage = true;
			}
		}
		
		String des = StarDict.DICT_LEXITRON+extension;
		FileConnection fconn = null;
		try {
			fconn = (FileConnection)Connector.open(des, Connector.READ_WRITE);
			if(!fconn.exists()){
				fconn.create();
			}
			else{
				fconn.truncate(0);
			}
			
			OutputStream os = fconn.openOutputStream();
			WebDownload web = new WebDownload(url, fromDeviceStorage);
			web.setDownloadLsitener(this);
			
			if(fconn!=null && fconn.isOpen()){
				try{fconn.close();}catch(IOException e){e.printStackTrace();}
			}
			
			if(extension.endsWith(".dz")){
				web.doGetGZIP(os);
			}
			else{
				web.doGet(os);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
//			String path = "Media Card/dict/lexitron"+extension;
//			UiApplication.getUiApplication().invokeLater(new Runnable(){
//
//				public void run() {
//					Dialog.alert("Data file is conflict. Please close QuickDict, then remove "+ path +" in your Media Card and try download again.");
//				}
//				
//			});
//			Dialog.alert("Data file is conflict. Please close QuickDict, then remove "+ path +" in your Media Card and try download again.");
			//Dialog.alert("Data file is conflict. Please try download again.");
//			Dialog.alert("Error :"+e.toString());
			showErrorDialog("Error :"+e.toString());
		}
		finally{
			if(fconn!=null && fconn.isOpen()){
				try{fconn.close();}catch(IOException e){e.printStackTrace();}
			}
		}
	}
	
	public static void writeFile(String filename, byte[] data){
		FileConnection fconn = null;
		OutputStream os = null;
		try {
			fconn = (FileConnection) Connector.open(filename, Connector.READ_WRITE);
			if(!fconn.exists()){
				fconn.create();
			}
			os = fconn.openOutputStream();
			os.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			if(os!=null)
				try{os.close();}catch(IOException e){e.printStackTrace();}
			if(fconn!=null&&fconn.isOpen())
				try{fconn.close();}catch(IOException e){e.printStackTrace();}
		}
	}
	
	public static byte[] readFile(String filename){
		FileConnection fconn = null;
		InputStream is = null;
		byte b[] = null;
		try {
			fconn = (FileConnection) Connector.open(filename, Connector.READ_WRITE);
			if(fconn.exists()){
				is = fconn.openInputStream();
				b = new byte[(int) fconn.fileSize()];
				is.read(b);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			if(is!=null)
				try{is.close();}catch(IOException e){e.printStackTrace();}
			if(fconn!=null&&fconn.isOpen())
				try{fconn.close();}catch(IOException e){e.printStackTrace();}
		}
		return b;
	}
	
	public boolean isValidFilesize(String ext){
		if(ext.equalsIgnoreCase(".ifo")){
			if(getFileSize(ext)>=LEXITRON_FILESIZE_IFO)
				return true;
		}
		else if(ext.equalsIgnoreCase(".yaidx")){
			if(getFileSize(ext)>=LEXITRON_FILESIZE_YAIDX)
				return true;
		}
		else if(ext.equalsIgnoreCase(".idx")){
			if(getFileSize(ext)>=LEXITRON_FILESIZE_IDX)
				return true;
		}
		else if(ext.equalsIgnoreCase(".dict.dz")){
			if(getFileSize(ext)>=LEXITRON_FILE_SIZE_DZ)
				return true;
		}
		
		return false;
	}
	
	public long getFileSize(String ext){
		String fullpath = SOURCE_FOLDER + "lexitron" + ext;
		FileConnection fconn = null;
		long fileSize = 0;
		try{
			fconn = (FileConnection)Connector.open(fullpath);
			fileSize = fconn.fileSize();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			if(fconn!=null&&fconn.isOpen()) try{fconn.close();}catch(Exception e){e.printStackTrace();}
		}
		return fileSize;
	}
	
	public static boolean isExist(String fullpath){
		FileConnection fconn = null;
		try{
			fconn = (FileConnection)Connector.open(fullpath);
			return fconn.exists();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			if(fconn!=null&&fconn.isOpen()) try{fconn.close();}catch(Exception e){e.printStackTrace();}
		}
		return false;
	}
	
	private PopupScreen downloadPopup;
	private LabelField downloadStatus;
	private GaugeField downloadProgress;
	private int downloadFileCount = 0;
	private int totalDownloadFileCount = 0;
	
	public void onDownload(final int numBytes) {
		// TODO Auto-generated method stub
//		UiApplication.getUiApplication().invokeLater(new Runnable(){
//
//			public void run() {
//				// TODO Auto-generated method stub
//				int count = Math.min(downloadFileCount+1, totalDownloadFileCount);
//				if(numBytes<1024)
//					downloadStatus.setText("download ("+count+"/"+totalDownloadFileCount+")  "+numBytes+" bytes.");
//				else
//					downloadStatus.setText("download ("+count+"/"+totalDownloadFileCount+")  "+(numBytes/1024)+" KB.");
//				downloadProgress.setValue(numBytes);
//			}
//			
//		});
		
		synchronized(UiApplication.getEventLock()){
			if(numBytes<1024)
				downloadStatus.setText("download ("+(downloadFileCount+1)+"/"+totalDownloadFileCount+")  "+numBytes+" bytes.");
			else
				downloadStatus.setText("download ("+(downloadFileCount+1)+"/"+totalDownloadFileCount+")  "+(numBytes/1024)+" KB.");
			downloadProgress.setValue(numBytes);
		}
//		
//		if(numBytes>=WebDownload.BYTE_SHRINK)repaint();
		
//		UiApplication.getUiApplication().invokeLater(new Runnable(){
//
//			public void run() {
//				if(numBytes<1024)
//					downloadStatus.setText("download ("+(downloadFileCount+1)+"/"+totalDownloadFileCount+")  "+numBytes+" bytes.");
//				else
//					downloadStatus.setText("download ("+(downloadFileCount+1)+"/"+totalDownloadFileCount+")  "+(numBytes/1024)+" KB.");
//				downloadProgress.setValue(numBytes);
//			}
//			
//		});
		
		
	}

	public void onDownloadComplete(int totalBytes, String url, boolean cancelled) {
		// TODO Auto-generated method stub
		if(!cancelled)
			downloadFileCount++;
	}
	
//	public void deleteFile(String fullpath){
//		FileConnection fconn = null;
//		try{
//			fconn = (FileConnection)Connector.open(fullpath, Connector.READ);
//			if(fconn!=null && fconn.isOpen()){
//				fconn.close();
//				fconn = (FileConnection)Connector.open(fullpath, Connector.WRITE);
//			}
//			if(fconn!=null){
//				fconn.truncate(0);
//				fconn.delete();
//			}
//		}
//		catch(Exception e){
//			e.printStackTrace();
//		}
//		finally{
//			if(fconn!=null&&fconn.isOpen()) try{fconn.close();}catch(Exception e){e.printStackTrace();}
//		}
//	}
	
	public boolean needDownload = false;
	public boolean checkingSDCard = false;
	public void onAction(Action event) {
//		if(event.getSource()==registerScreen){
//			if(event.getAction()==RegisterScreen.REGISTER_DONE || event.getAction()==RegisterScreen.REGISTER_ALREADY_DONE){
//				if(event.getAction()==RegisterScreen.REGISTER_DONE){
//					String msg = (String)event.getData();
//					Status.show(msg);
//				}
//				popScreen(registerScreen);
//				pushScreen(homeScreen);
//				starDict = new StarDict();
//				if(starDict.exist()){
//					wordScreen = new WordScreen(starDict);
//					explanationScreen = new ExplanationScreen(starDict);
//					wordScreen.addActionListener(QuickDict.this);
//					explanationScreen.addActionListener(QuickDict.this);
//				}
//				homeScreen.fireAction(HomeScreen.ACTION_ENTER);
//			}
//		}
//		else if(event.getSource()==homeScreen){
		if(event.getSource()==homeScreen){
			if(event.getAction()==HomeScreen.ACTION_ENTER){				
				if(starDict.exist()){
					wordScreen = new WordScreen(starDict);
					explanationScreen = new ExplanationScreen(starDict);
					wordScreen.addActionListener(this);
					explanationScreen.addActionListener(this);
					pushScreen(wordScreen);
				}
				else{
//					int select = Dialog.ask(Dialog.D_YES_NO, "Download dictionary file?", 0);
//					if(select==Dialog.YES){
//						if(!hasSDCard()){
//							Dialog.alert("An Media Card is required to store dictionary files.");
//						}
//						else{					
//							popupDownloading();
//							WebDownloadRunnable t = new WebDownloadRunnable();
//							//setAcceptEvents(false);
//							invokeAndWait(t);
//							//setAcceptEvents(true);
//							downloadFinish();
//						}
//					}
					
//					UiApplication.getUiApplication().invokeLater(new Runnable(){
//						public void run() {
//							int select = Dialog.ask(Dialog.D_YES_NO, "Download dictionary file?", 0);
//							if(select==Dialog.YES){
//								if(!hasSDCard()){
//									Dialog.alert("An Media Card is required to store dictionary files.");
//								}
//								else{					
//									needDownload = true;
//								}
//							}
//							checkingSDCard = true;
//						}
//					});
					
					checkingSDCard = hasSDCard();
					if(!checkingSDCard){
//						UiApplication.getUiApplication().invokeLater(new Runnable(){
//
//							public void run() {
//								Dialog.alert("An Media Card is required to store dictionary files.");
//							}
//							
//						});
						showErrorDialog("An Media Card is required to store dictionary files.");
					}
					else{
						
						if(!starDict.dbExist()){
							UiApplication.getUiApplication().invokeLater(new Runnable(){
								public void run() {
									Status.show("QuikDict will preparing database", 1000);
									try {
										ApplicationManager.getApplicationManager().launchApplication("Lexitron");
									} catch (ApplicationManagerException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									finally{
										System.exit(0);
									}
								}
								
							});
						}
						else if(!isDZExist()){
							UiApplication.getUiApplication().invokeLater(new Runnable(){

								public void run() {
									Status.show("QuikDict will extracting database", 1000);
									try {
										ApplicationManager.getApplicationManager().launchApplication("LexitronDZ");
									} catch (ApplicationManagerException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									finally{
										System.exit(0);
									}
								}
								
							});
			
						}
						
						popupDownloading();
						
						UiApplication.getUiApplication().invokeLater(new Runnable(){

							public void run() {
								downloadThread = new DownloadThread();
								downloadThread.startDownload();
							}
							
						}, 4000, false);
						
					}
				}
			}
			else if(event.getAction()==HomeScreen.ACTION_CLOSE){
				try {
					starDict.close();
				} catch (Exception e) {
					e.printStackTrace(); 
				}
			}
			
		}// end if event.getSouce()==homeScreen
		
		else if(event.getSource()==wordScreen){
			if(event.getAction()==WordScreen.ACTION_ENTER){
				String word = (String)event.getData();
				explanationScreen = new ExplanationScreen(starDict);
				explanationScreen.addActionListener(this);
				explanationScreen.showExplaination(word);
				pushScreen(explanationScreen);
			}
//			else if(event.getAction()==WordScreen.ACTION_RECOMMENED){
//				pushScreen(contactScreen);
//			}
		}// end if event.getSource()==wordScreen
		else if(event.getSource()==explanationScreen){
			if(event.getAction()==ExplanationScreen.ACTION_CLOSE){
				popScreen(explanationScreen);
			}
		}

	}
	
	final String FILE_NAMES[] = {"lexitron.dict.p0.gz", "lexitron.dict.p1.gz", "lexitron.dict.p2.gz"};
    public static final String DEFAULT_PATH = System.getProperty("fileconn.dir.memorycard") + "dict/";
	public boolean isFileExist(String path){
    	boolean bExist = false;
    	FileConnection fconn = null;
    	try {
			fconn = (FileConnection) Connector.open(path, Connector.READ_WRITE);
			if(fconn.exists()){
				long filesize = fconn.fileSize();
				if(filesize>0)
					bExist=true;
			}
			fconn.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(fconn!=null&&fconn.isOpen())try{fconn.close();}catch(IOException e){e.printStackTrace();}
		}
    	return bExist;
    }
	
	public boolean isDZExist(){
		for(int i=0; i<FILE_NAMES.length; i++){
			String path = DEFAULT_PATH + FILE_NAMES[i];
			boolean b = isFileExist(path);
			if(!b) return false;
		}
		
		return true;
	}
	
	public void popupDownloading(){
//		UiApplication.getUiApplication().invokeLater(new Runnable(){
//
//			public void run() {
//				Status.show("Downloading files to the Media Card (required once). This may take some time depending on your connection. Please wait and do not cancel!", 3000);
//				downloadPopup = new PopupScreen(new VerticalFieldManager());
//				downloadPopup.add(new LabelField("Downloading..."));
//				downloadPopup.add(new SeparatorField());
//				downloadStatus = new LabelField("Downloading in progress...");
//				downloadProgress = new GaugeField();
//				downloadPopup.add(downloadStatus);
//				downloadPopup.add(new LabelField());
//				downloadPopup.add(downloadProgress);
//				pushScreen(downloadPopup);
//			}
//			
//		});
		
		downloadPopup = new PopupScreen(new VerticalFieldManager()){
			final String choices[] = {"Yes(Exit program)", "Download in background", "Cancel"};
			final int values[] = {Dialog.YES, Dialog.SAVE, Dialog.CANCEL};
			final int defaultChoice = Dialog.CANCEL;
			final int selected[] = new int[1];
			public boolean keyChar(char c, int status, int time){
				if(c==Keypad.KEY_ESCAPE){
					
					selected[0] = Dialog.ask("Cancel downloading?", choices, values, defaultChoice);
					if(selected[0]==Dialog.YES){
						if(downloadThread!=null)downloadThread.stopDownload();
						System.exit(1);
					}
					else if(selected[0]==Dialog.SAVE){
						if(downloadThread!=null){
							if(downloadThread.isRunning())
								UiApplication.getUiApplication().requestBackground();
							else
								showInformationDialog("Download complete, background process is not require.");
						}
					}
					return true;
				}
				return super.keyChar(c, status, time);
			}
		};
		downloadPopup.add(new LabelField("Downloading..."));
		downloadPopup.add(new SeparatorField());
		downloadStatus = new LabelField("Downloading in progress...");
		downloadProgress = new GaugeField();
		downloadPopup.add(downloadStatus);
		downloadPopup.add(new LabelField());
		downloadPopup.add(downloadProgress);
		
		//Status.show("Downloading files to the Media Card (required once). This may take some time depending on your connection. Please wait and do not cancel!", 3000);
		showStatusDialog("Downloading & Extracting files to the Media Card (required once). This may take some time depending on your connection. Please wait and do not cancel!");
		//pushScreen(downloadPopup);
		UiApplication.getUiApplication().invokeLater(new Runnable(){

			public void run() {
				pushScreen(downloadPopup);
			}
			
		},3000, false);
	}
	
	public void showStatusDialog(final String message){
		UiApplication.getUiApplication().invokeLater(new Runnable(){
			public void run() {
				Status.show(message, 3000);
			}
		});
	}
	
	public void showErrorDialog(final String message){
		UiApplication.getUiApplication().invokeLater(new Runnable(){
			public void run() {
				Dialog.alert(message);
			}
		});
	}
	
	public void showInformationDialog(final String message){
		UiApplication.getUiApplication().invokeLater(new Runnable(){
			public void run() {
				Dialog.inform(message);
			}
		});
	}
	
	public class DownloadThread extends Thread{
		public boolean isRunning = false;
		
		public void startDownload(){
			isRunning = true;
			start();
		}
		
		public boolean isRunning() {
			return isRunning;
		}

		public void stopDownload(){
			isRunning = false;
		}
		
		public void run(){
			processWebDownload();
			stopDownload();
			downloadFinish();
			if(!UiApplication.getUiApplication().isForeground()){
				UiApplication.getUiApplication().requestForeground();
			}
		}
	}
	
	public void processWebDownload(){
		downloadFileCount = 0;
		totalDownloadFileCount = 0;
		
		Vector extToDownload = starDict.getFileExtensionDownloadNecessary();
		totalDownloadFileCount = extToDownload.size();
		try {
			starDict.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(int i=0;i<totalDownloadFileCount; i++){
			String ext = (String)extToDownload.elementAt(i);
			final int filesize = StarDict.getFileSize(ext);
			
//			downloadProgress.reset("", 0, filesize, 0);
			
			UiApplication.getUiApplication().invokeLater(new Runnable() {

				public void run() {
					downloadProgress.reset("", 0, filesize, 0);
				}

			});
			
			doDownload(ext);
		}
	}
	
	public void downloadFinish(){
		UiApplication.getUiApplication().invokeLater(new Runnable(){

			public void run() {
				downloadPopup.close();
				
				starDict = new StarDict();
				if(starDict.exist()){
					Status.show("Download complete.");
					wordScreen = new WordScreen(starDict);
					explanationScreen = new ExplanationScreen(starDict);
					wordScreen.addActionListener(QuickDict.this);
					explanationScreen.addActionListener(QuickDict.this);
					pushScreen(wordScreen);
				}
				else{
					Status.show("Download not complete.Please try again.");
				}
			}
			
		});
		
//		UiApplication.getUiApplication().invokeLater(new Runnable(){
//
//			public void run() {
//				if(downloadPopup.isDisplayed())
//					downloadPopup.close();
//			}
//			
//		});
		
		
		
//		starDict = new StarDict();
//		if(starDict.exist()){
//			//Status.show("Download complete.");
//			showStatusDialog("Download complete.");
//			wordScreen = new WordScreen(starDict);
//			explanationScreen = new ExplanationScreen(starDict);
//			wordScreen.addActionListener(QuickDict.this);
//			explanationScreen.addActionListener(QuickDict.this);
//			pushScreen(wordScreen);
//		}
//		else{
//			showStatusDialog("Download not complete yet.Please try again later.");
//		}
	}
	
	private class WebDownloadRunnable implements Runnable{	
		public void run(){
			//popupDownloading();
			processWebDownload();
			//downloadFinish();
		}
	}
}
