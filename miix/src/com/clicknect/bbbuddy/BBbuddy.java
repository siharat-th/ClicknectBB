package com.clicknect.bbbuddy;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.applicationcontrol.ApplicationPermissionsManager;
import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.io.LineReader;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.ApplicationManagerException;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.CoverageInfo;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.TransitionContext;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngineInstance;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.XYRect;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.Border;
import net.rim.device.api.ui.decor.BorderFactory;
import net.rim.device.api.ui.picker.FilePicker.Listener;

import com.clicknect.webapi.Activate;
import com.jimmysoftware.network.HttpClient;
import com.jimmysoftware.network.HttpConnectionFactory;
import com.jimmysoftware.ui.Action;
import com.jimmysoftware.ui.JSApplication;
import com.jimmysoftware.ui.PINContactScreen;
import com.jimmysoftware.ui.UiFactory;

public class BBbuddy extends JSApplication{
	public static final String FEED_BUNDLE_VERSION = "11.10.11.00";
	public static final String BASE_URL = "http://bbcenter.clicknect.com/miix/";
	public static final String APP_FEED_URL = BASE_URL + "feed.txt";
	private static final String DEFAULT_PATH = "file:///store/home/user/miix/";
	//public static boolean USE_BUNDLE_RESOURCE = true;
	
//	private MainMenuScreen mainMenuScreen;
	public HomeScreenAppCenter homeScreen;
	private FeatureScreen featureScreen;
	private DetailScreenAppCenter detailScreen;
	
	private PopupScreen popup = null;
	private HttpConnectionFactory connFactory;
	private FeedThread feedThread = null;
	
	private static BBbuddy instance=null;
	public static BBbuddy getInstance(){
		return instance;
	}
	
	private static Vector listInfos = new Vector();
	
	public BBbuddy(){
		super(false);
		connFactory = new HttpConnectionFactory();
	}
	
    public static void main(String[] args){
    	UiFactory.setApplicationName(" miix");
    	instance = new BBbuddy();
    	instance.enterEventDispatcher();
    }

	protected void checkPermissions() {
		ApplicationPermissionsManager apm = ApplicationPermissionsManager.getInstance();
		ApplicationPermissions original = apm.getApplicationPermissions();

		if (
				  (original.getPermission(ApplicationPermissions.PERMISSION_APPLICATION_MANAGEMENT)==ApplicationPermissions.VALUE_ALLOW)
				&&(original.getPermission(ApplicationPermissions.PERMISSION_AUTHENTICATOR_API)==ApplicationPermissions.VALUE_ALLOW)
				//&&(original.getPermission(ApplicationPermissions.PERMISSION_BLUETOOTH)==ApplicationPermissions.VALUE_ALLOW)
				&&(original.getPermission(ApplicationPermissions.PERMISSION_BROWSER_FILTER)==ApplicationPermissions.VALUE_ALLOW)
				//&&(original.getPermission(ApplicationPermissions.PERMISSION_CROSS_APPLICATION_COMMUNICATION)==ApplicationPermissions.VALUE_ALLOW)
				&&(original.getPermission(ApplicationPermissions.PERMISSION_DEVICE_SETTINGS)==ApplicationPermissions.VALUE_ALLOW)
				//&&(original.getPermission(ApplicationPermissions.PERMISSION_DISPLAY_LOCKED)==ApplicationPermissions.VALUE_ALLOW)
				//&&(original.getPermission(ApplicationPermissions.PERMISSION_EMAIL)==ApplicationPermissions.VALUE_ALLOW)
				&&(original.getPermission(ApplicationPermissions.PERMISSION_FILE_API)==ApplicationPermissions.VALUE_ALLOW)
				//&&(original.getPermission(ApplicationPermissions.PERMISSION_IDLE_TIMER)==ApplicationPermissions.VALUE_ALLOW)
				&&(original.getPermission(ApplicationPermissions.PERMISSION_INPUT_SIMULATION)==ApplicationPermissions.VALUE_ALLOW)
				&&(original.getPermission(ApplicationPermissions.PERMISSION_INTERNET)==ApplicationPermissions.VALUE_ALLOW)
				//&&(original.getPermission(ApplicationPermissions.PERMISSION_LOCATION_DATA)==ApplicationPermissions.VALUE_ALLOW)
				//&&(original.getPermission(ApplicationPermissions.PERMISSION_MEDIA)==ApplicationPermissions.VALUE_ALLOW)
				//&&(original.getPermission(ApplicationPermissions.PERMISSION_ORGANIZER_DATA)==ApplicationPermissions.VALUE_ALLOW)
				&&(original.getPermission(ApplicationPermissions.PERMISSION_PHONE)==ApplicationPermissions.VALUE_ALLOW)
				//&&(original.getPermission(ApplicationPermissions.PERMISSION_RECORDING)==ApplicationPermissions.VALUE_ALLOW)
				//&&(original.getPermission(ApplicationPermissions.PERMISSION_SECURITY_DATA)==ApplicationPermissions.VALUE_ALLOW)
				//&&(original.getPermission(ApplicationPermissions.PERMISSION_SERVER_NETWORK)==ApplicationPermissions.VALUE_ALLOW)
				//&&(original.getPermission(ApplicationPermissions.PERMISSION_THEMES)==ApplicationPermissions.VALUE_ALLOW)
				//&&(original.getPermission(ApplicationPermissions.PERMISSION_USB)==ApplicationPermissions.VALUE_ALLOW)
				&&(original.getPermission(ApplicationPermissions.PERMISSION_WIFI)==ApplicationPermissions.VALUE_ALLOW)
			)
		{
			return;
		}

		ApplicationPermissions permRequest = new ApplicationPermissions();
		permRequest.addPermission(ApplicationPermissions.PERMISSION_APPLICATION_MANAGEMENT);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_AUTHENTICATOR_API);
		//permRequest.addPermission(ApplicationPermissions.PERMISSION_BLUETOOTH);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_BROWSER_FILTER);
		//permRequest.addPermission(ApplicationPermissions.PERMISSION_CROSS_APPLICATION_COMMUNICATION);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_DEVICE_SETTINGS);
		//permRequest.addPermission(ApplicationPermissions.PERMISSION_DISPLAY_LOCKED);
		//permRequest.addPermission(ApplicationPermissions.PERMISSION_EMAIL);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_FILE_API);
		//permRequest.addPermission(ApplicationPermissions.PERMISSION_IDLE_TIMER);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_INPUT_SIMULATION);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_INTERNET);
		//permRequest.addPermission(ApplicationPermissions.PERMISSION_LOCATION_DATA);
		//permRequest.addPermission(ApplicationPermissions.PERMISSION_MEDIA);
		//permRequest.addPermission(ApplicationPermissions.PERMISSION_ORGANIZER_DATA);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_PHONE);
		//permRequest.addPermission(ApplicationPermissions.PERMISSION_RECORDING);
		//permRequest.addPermission(ApplicationPermissions.PERMISSION_SECURITY_DATA);
		//permRequest.addPermission(ApplicationPermissions.PERMISSION_SERVER_NETWORK);
		//permRequest.addPermission(ApplicationPermissions.PERMISSION_THEMES);
		//permRequest.addPermission(ApplicationPermissions.PERMISSION_USB);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_WIFI);

		boolean acceptance = ApplicationPermissionsManager.getInstance().invokePermissionsRequest(permRequest);

		if (acceptance) {
			// User has accepted all of the permissions.
			return;
		} else {
		}
	}

	protected void doAction(Action event) {
//		if(event.getSource()==homeScreen){
//			if(event.getAction()==HomeScreenAppCenter.ACTION_ENTER){
//				ListInfo listInfo = (ListInfo)event.getData();
//				detailScreen = new DetailScreenAppCenter(listInfo);
//				detailScreen.addActionListener(this);
//				
//				// Transition
//				TransitionContext transition = new TransitionContext(TransitionContext.TRANSITION_SLIDE);
//				transition.setIntAttribute(TransitionContext.ATTR_DURATION, 250);
//				transition.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_LEFT);
//				transition.setIntAttribute(TransitionContext.ATTR_STYLE, TransitionContext.STYLE_PUSH);
//				UiEngineInstance engine = Ui.getUiEngineInstance();
//				engine.setTransition(homeScreen, detailScreen, UiEngineInstance.TRIGGER_PUSH, transition);
//				pushScreen(detailScreen);
//			}
////			else if(event.getAction()==HomeScreen.ACTION_RECOMMENED){
////				pushScreen(contactScreen);
////			}
//		}
//		else if(event.getSource()==detailScreen){
//			if(event.getAction()==DetailScreenAppCenter.ACTION_CLOSE){
//				// Transition
//				TransitionContext transition = new TransitionContext(TransitionContext.TRANSITION_SLIDE);
//				transition.setIntAttribute(TransitionContext.ATTR_DURATION, 250);
//				transition.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_RIGHT);
//				transition.setIntAttribute(TransitionContext.ATTR_STYLE, TransitionContext.STYLE_PUSH);
//				UiEngineInstance engine = Ui.getUiEngineInstance();
//				engine.setTransition(detailScreen, homeScreen, UiEngineInstance.TRIGGER_POP, transition);
//				popScreen(detailScreen);
//			}
//		}
////		if(event.getSource()==mainMenuScreen){
////			if(event.getAction()==MainMenuScreen.ACTION_APP_STORE){
////				TransitionContext transition = new TransitionContext(TransitionContext.TRANSITION_FADE);
////				transition.setIntAttribute(TransitionContext.ATTR_DURATION, 500);
////				transition.setIntAttribute(TransitionContext.ATTR_KIND, TransitionContext.KIND_IN);
////				UiEngineInstance engine = Ui.getUiEngineInstance();
////				engine.setTransition(homeScreen, null, UiEngineInstance.TRIGGER_PUSH, transition);
////				
////				homeScreen = new HomeScreenAppCenter();
////				homeScreen.addActionListener(this);
////				pushScreen(homeScreen);
////			}
//////			else if(event.getAction()==MainMenuScreen.ACTION_CONTENT_STORE){
//////				if(Util.getInstance().isModuleInstalled("e_Plaza")){
//////					String codeModuleName = "e_Plaza";
//////					try {
//////						ApplicationManager.getApplicationManager().launchApplication(codeModuleName);
//////					} catch (ApplicationManagerException e) {
//////						// TODO Auto-generated catch block
//////						e.printStackTrace();
//////					}
//////				}
//////				else{
//////					Util.getInstance().installCodeModule("/e_Plaza/e_Plaza", 87);
//////				}
//////			}
////		}
		if(event.getSource()==homeScreen){
			if(event.getAction()==HomeScreenAppCenter.ACTION_ENTER){
				ListInfo listInfo = (ListInfo)event.getData();
				detailScreen = new DetailScreenAppCenter(listInfo);
				detailScreen.addActionListener(this);
				
				// Transition
				TransitionContext transition = new TransitionContext(TransitionContext.TRANSITION_SLIDE);
				transition.setIntAttribute(TransitionContext.ATTR_DURATION, 250);
				transition.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_LEFT);
				transition.setIntAttribute(TransitionContext.ATTR_STYLE, TransitionContext.STYLE_PUSH);
				UiEngineInstance engine = Ui.getUiEngineInstance();
				engine.setTransition(homeScreen, detailScreen, UiEngineInstance.TRIGGER_PUSH, transition);
				pushScreen(detailScreen);
			}
			else if(event.getAction()==HomeScreenAppCenter.ACTION_NEW_FEED){
				if(CoverageInfo.isOutOfCoverage()){
					Dialog.alert("กรุณาเชื่อมต่ออินเทอร์เน็ต เพื่อเข้าใช้งาน");
					return;
				}
				if(popup==null){
					popup = new PopupScreen(new HorizontalFieldManager(Manager.USE_ALL_WIDTH)){
						final String CANCEL_FETCHING = "Cancle fetching?";
						final String INFORM = "Fecthing complete, background process is not require.";
						final String choices[] = {"Yes(Exit program)", "Fecthing in background", "Cancel"};
						final int values[] = {Dialog.YES, Dialog.SAVE, Dialog.CANCEL};
						final int defaultChoice = Dialog.CANCEL;
						final int selected[] = new int[1];
						
						protected boolean keyChar(char c, int status, int time){
							if(c==Keypad.KEY_ESCAPE){
								
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
						
						public void paint(Graphics g){
							g.setColor(0xffffff);
							int w = getWidth();
							int h = getHeight();
							g.fillRect(0, 0, w, h);
							int fontHeight =  g.getFont().getHeight(Ui.UNITS_px);
							g.setColor(0x31659c);
							g.fillRect(0, 0, getWidth(), fontHeight);
							g.setColor(0xffffff);
							g.setFont(g.getFont().derive(Font.BOLD));
							g.drawText(UiFactory.APPLICATION_NAME, 2, 0);
							g.setFont(g.getFont().derive(Font.PLAIN));
							
							int fh = fontHeight/2;
							int max_possible_width = fh*3 + g.getFont().getAdvance("Fetching...");
							int tx = (w-max_possible_width)/2;
							int ty = fontHeight+ ((h-fontHeight)-g.getFont().getHeight())/2;
							paintFetching(g, tx, ty);
						}
					};
					popup.setBorder(BorderFactory.createRoundedBorder(new XYEdges(), Border.STYLE_TRANSPARENT));
					LabelField title = new LabelField("BBbuddy", Field.USE_ALL_WIDTH){
						public void paint(Graphics g){
							g.setColor(0x0000ff);
							g.fillRect(0, 0, getWidth(), getHeight());
							g.setColor(0xffffff);
							super.paint(g);
						}
					};
					popup.add(title);
					popup.add(new BitmapField(Bitmap.getPredefinedBitmap(Bitmap.HOURGLASS)));
					popup.add(new LabelField("Fetching... ,Please wait...", Field.FIELD_VCENTER));
				}
				feed();
			}
//			else if(event.getAction()==HomeScreen.ACTION_RECOMMENED){
//				pushScreen(contactScreen);
//			}
		}
		else if(event.getSource()==detailScreen){
			if(event.getAction()==DetailScreenAppCenter.ACTION_CLOSE){
				// Transition
				TransitionContext transition = new TransitionContext(TransitionContext.TRANSITION_SLIDE);
				transition.setIntAttribute(TransitionContext.ATTR_DURATION, 250);
				transition.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_RIGHT);
				transition.setIntAttribute(TransitionContext.ATTR_STYLE, TransitionContext.STYLE_PUSH);
				UiEngineInstance engine = Ui.getUiEngineInstance();
				engine.setTransition(detailScreen, null, UiEngineInstance.TRIGGER_POP, transition);
				popScreen(detailScreen);
			}
		}
		else if(event.getSource()==featureScreen){
			if(event.getAction()==FeatureScreen.ACTION_CLOSE){
				// Transition
//				homeScreen.loadList();
//				TransitionContext transition = new TransitionContext(TransitionContext.TRANSITION_SLIDE);
//				transition.setIntAttribute(TransitionContext.ATTR_DURATION, 250);
//				transition.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_RIGHT);
//				transition.setIntAttribute(TransitionContext.ATTR_STYLE, TransitionContext.STYLE_PUSH);
//				UiEngineInstance engine = Ui.getUiEngineInstance();
//				engine.setTransition(featureScreen, null, UiEngineInstance.TRIGGER_POP, transition);
				popScreen(featureScreen);
			}
			else if(event.getAction()==FeatureScreen.ACTION_ENTER){
				ListInfo listInfo = (ListInfo)event.getData();
				detailScreen = new DetailScreenAppCenter(listInfo);
				detailScreen.addActionListener(this);
				
				// Transition
				TransitionContext transition = new TransitionContext(TransitionContext.TRANSITION_SLIDE);
				transition.setIntAttribute(TransitionContext.ATTR_DURATION, 250);
				transition.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_LEFT);
				transition.setIntAttribute(TransitionContext.ATTR_STYLE, TransitionContext.STYLE_PUSH);
				UiEngineInstance engine = Ui.getUiEngineInstance();
				engine.setTransition(featureScreen, detailScreen, UiEngineInstance.TRIGGER_PUSH, transition);
				pushScreen(detailScreen);
			}
		}
	}
	
	private static Timer fecthTimer = new Timer();
    private static TimerTask fetchTask;
    private static int frame = 0;
    static{
    	// Timer Task
		fetchTask = new TimerTask() {
	        public void run() {
	        	frame++;
	        	frame=frame%5;
	            Screen screen = UiApplication.getUiApplication().getActiveScreen();
	            if(screen!=null)
	            	screen.invalidate();
	        }
		};
		fecthTimer.scheduleAtFixedRate(fetchTask, 200, 250);
    }
    
	public static void paintFetching(Graphics g, int translateX, int translateY) {
		int h = g.getFont().getHeight(Ui.UNITS_px)/2;
		int w = h/2;
		int y = g.getFont().getHeight(Ui.UNITS_px)/5;
		int hh = h/2;
		int yy = y+hh/2;
		int max_possible_width = h*3 + g.getFont().getAdvance("Fetching...");
		
		g.translate(translateX, translateY);
		g.setColor(0xffffff);
		for(int i=0; i<3; i++){
			int x = i*h;
			g.fillRect(x, yy, w, hh);
		}
		
		if(frame>0){
			int x = (frame-1)*h;
			if(frame>1){
				int xx = x-h;
				g.setColor(0xd0e0f0);
				g.fillRect(xx, yy, w, hh);
				g.setColor(0xb0b0b0);
				g.drawRect(xx-1, yy-1, w+2, hh+2);
			}
			if(frame<4){
				g.setColor(0x499ac5);  //0xd0e5f0
				g.fillRect(x, y, w, h);
				g.setColor(0);
				g.drawRect(x, y, w, h);
			}
		}
		int allWidth = h*3;
		g.setColor(0);
		g.drawText("Fetching...", allWidth, 0);
	}

	protected void onVerifyDone() {
//		boolean installed = Util.getInstance().isModuleInstalled("e_Plaza");
//		if(!installed){
//			 Util.getInstance().installCodeModule("/e_Plaza/e_Plaza", 87);
//		}
		
//		mainMenuScreen = new MainMenuScreen();
//		mainMenuScreen.addActionListener(this);
//		mainMenuScreen.addActionListener(EPlaza.getInstance());
//		pushScreen(mainMenuScreen);
		
		homeScreen = new HomeScreenAppCenter();
		homeScreen.addActionListener(this);
		pushScreen(homeScreen);
		
		
		//if(!DeviceInfo.isSimulator()){
			Activate atv = new Activate();
			atv.request();
		//}
	}
	
	boolean done = false;
	public class FeedThread extends Thread{
		public boolean running = false;
		public void startFeed(){
			running = true;
			start();
		}
		
		public void run(){
			InputStream is = null;
			done = true;
			
			try {
				is = getServiceResponse();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				done=false;
				e1.printStackTrace();
				errorDialog("Cannot connect to server, please check your internet connection and try again later");
				//Dialog.alert("Cannot connect to server, please check your internet connection and try again later");
				stop();
			}
			
			
			if(is!=null){
				Vector listInfos = new Vector();
				try {
					byte[] data = IOUtilities.streamToBytes(is);
					//String msg = new String(data, "utf-8");
					writeFile(data, "feed.txt");
					//informDialog(msg);
					
					InputStream stream = new ByteArrayInputStream(data);
					
					try {
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
								listInfo.icon = loadBitmap(icon);
								listInfo.imgUrl = img;
								listInfo.img = loadBitmap(listInfo.imgUrl);
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
								
								//String descFilename = modulename+".txt";
								//byte descdata[] = readFile(descFilename);
								//listInfo.desc = new String(descdata, "utf-8");
								listInfo.desc = loadDescription(modulename+".txt");
								
								listInfos.addElement(listInfo);
								idx++;
							}
							b = lineReader.readLine();
						}
					} 
					catch(EOFException eof){
						
					}
					catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						errorDialog("Fetch Error, please try again later.(1)"+e1.toString());
						done=false;
					}
					finally{
						try{if(stream!=null) stream.close();}catch(IOException e){};
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					errorDialog("Fetch Error, please try again later."+e.toString());
					//Dialog.alert("Fetch Error, please try again later.");
					stop();
					done = false;
				}
				finally{
					if(done){
						BBbuddy.this.setListInfos(listInfos);
					}
				}
			}
			
			// feed done.
			running = false;
			
			UiApplication.getUiApplication().invokeLater(new Runnable(){
				public void run() {
					if(done){
						featureScreen = new FeatureScreen(BBbuddy.getListInfos());
						featureScreen.addActionListener(BBbuddy.this);
						// Transition
						TransitionContext transition = new TransitionContext(TransitionContext.TRANSITION_SLIDE);
						transition.setIntAttribute(TransitionContext.ATTR_DURATION, 250);
						transition.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_LEFT);
						transition.setIntAttribute(TransitionContext.ATTR_STYLE, TransitionContext.STYLE_PUSH);
						UiEngineInstance engine = Ui.getUiEngineInstance();
						engine.setTransition(homeScreen, featureScreen, UiEngineInstance.TRIGGER_PUSH, transition);
						
						
						UiApplication.getUiApplication().pushScreen(featureScreen);
					}
						
					if(popup.isDisplayed()){
						popup.close();	
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
	
	public void feed(){
		if(feedThread==null || !feedThread.isRunning()){
			UiApplication.getUiApplication().pushScreen(popup);
			
			feedThread = new FeedThread();
			feedThread.startFeed();
		}
	}
	
	public String loadDescription(String filename) {
		String desc= "";
		HttpClient client = new HttpClient(connFactory);
		try {
			StringBuffer buff = client.doGet(BASE_URL+filename);
			byte[] data = buff.toString().getBytes();
			if(data!=null && data.length>0){
				writeFile(data, filename);
				desc = new String(data, "utf-8");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return desc.trim();
	}

	private InputStream getServiceResponse() throws IOException{
		HttpConnection connection = null;
		InputStream inputStream = null;
		
		connection = connFactory.getHttpConnection(APP_FEED_URL);
		
		if(connection.getResponseCode()==HttpConnection.HTTP_OK){
			inputStream = connection.openInputStream();
		}
		return inputStream;
	}
	
	private void informDialog(final String msg){
		UiApplication.getUiApplication().invokeLater(new Runnable(){

			public void run() {
				Dialog.inform(msg);
			}
			
		});
	}
	
	private void errorDialog(final String msg){
		UiApplication.getUiApplication().invokeLater(new Runnable(){

			public void run() {
				Dialog.alert(msg);
			}
			
		});
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
	
	private Bitmap loadBitmap(String filename){
		if(filename.startsWith("img/")){
			if(Display.getWidth()<=320){
				int idx = filename.indexOf('/');
				String prefix = filename.substring(0, idx+1);
				String suffix = filename.substring(idx+1);
				filename = prefix + "ss_" + suffix;
			}
			else if(Display.getWidth()<=480){
				int idx = filename.indexOf('/');
				String prefix = filename.substring(0, idx+1);
				String suffix = filename.substring(idx+1);
				filename = prefix + "s_" + suffix;
			}
		}
		Bitmap bmp = null;
		byte data[] = readFile(filename);
		if(data!=null){
			bmp = Bitmap.createBitmapFromPNG(data, 0, data.length);
		}
		else{
			HttpClient client = new HttpClient(connFactory);
			try {
				StringBuffer buff = client.doGet(BASE_URL+filename);
				data = buff.toString().getBytes();
				if(data!=null && data.length>0){
					writeFile(data, filename);
					bmp = Bitmap.createBitmapFromPNG(data, 0, data.length);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return bmp;
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
	
	public static boolean isFileExist(String fullpath){
		FileConnection fc = null;
		boolean bExist = false;
		try {
			fc = (FileConnection)Connector.open(fullpath, Connector.READ);
			bExist = fc.exists();
		}
		catch (IOException e) {
			e.printStackTrace();
			bExist = false;
		}
		finally{
			if(fc!=null && fc.isOpen()) try{fc.close();}catch(IOException e){e.printStackTrace();}
		}
		return bExist;
	}
	
	public static Vector getListInfos(){
		return listInfos;
	}
	
	private void setListInfos(Vector listInfos){
		synchronized(BBbuddy.listInfos){
			BBbuddy.listInfos = listInfos;
		}
	}
}
