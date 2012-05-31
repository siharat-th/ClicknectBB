package com.clicknect.tv;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.xml.sax.SAXException;

import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.applicationcontrol.ApplicationPermissionsManager;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Device;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.TransitionContext;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngineInstance;
import net.rim.device.api.ui.XYRect;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.BackgroundFactory;
import net.rim.device.api.xml.parsers.ParserConfigurationException;

import com.clicknect.tv.TVApi.ProgramData;
import com.clicknect.tv.TVApi.XMLNode;
import com.clicknect.util.MsgBox;
import com.clicknect.util.ResourceManager;
import com.clicknect.webapi.Activate;
import com.jimmysoftware.device.api.command.Command;
import com.jimmysoftware.device.api.command.CommandHandler;
import com.jimmysoftware.device.api.command.ReadOnlyCommandMetadata;
import com.jimmysoftware.ui.Action;
import com.jimmysoftware.ui.CommandButtonField;
import com.jimmysoftware.ui.CustomPopupScreen;
import com.jimmysoftware.ui.HyperlinkButtonField;
import com.jimmysoftware.ui.JSApplication;

public class ThaiTVSchedule extends JSApplication {
	private SplashScreen splashScreen;
	private HomeScreen homeScreen;
	private ScheduleScreen scheduleScreen;
	public static TVApi tvapi = new TVApi();
	
	public ThaiTVSchedule(){
		super(false);
	}
	
	private String channelId;
	private static CustomPopupScreen popupLoading;
	protected void doAction(Action event) {
		if(event.getSource()==splashScreen){
			if(event.getAction()==SplashScreen.ACTION_ENTER){
				Vector nodes = (Vector)event.getData();
				homeScreen = new HomeScreen(nodes);
				homeScreen.addActionListener(this);
				
//				TransitionContext transition = new TransitionContext(TransitionContext.TRANSITION_FADE);
//		        transition.setIntAttribute(TransitionContext.ATTR_DURATION, 500);
//		        transition.setIntAttribute(TransitionContext.ATTR_KIND, TransitionContext.KIND_OUT);
//		        UiEngineInstance engine = Ui.getUiEngineInstance();
//		        engine.setTransition(splashScreen, homeScreen, UiEngineInstance.TRIGGER_POP, transition);
//		        popScreen(splashScreen);
		        
//		        TransitionContext transition = new TransitionContext(TransitionContext.TRANSITION_FADE);
//		        transition.setIntAttribute(TransitionContext.ATTR_DURATION, 500);
//		        transition.setIntAttribute(TransitionContext.ATTR_KIND, TransitionContext.KIND_IN);
//		        transition.setIntAttribute(TransitionContext.ATTR_STYLE, TransitionContext.STYLE_OVER);
//		        UiEngineInstance engine = Ui.getUiEngineInstance();
//		        engine.setTransition(null, homeScreen, UiEngineInstance.TRIGGER_PUSH, transition);
				pushScreen(homeScreen);
				popScreen(splashScreen);
			}
		}
		else if(event.getSource()==homeScreen){
			if(event.getAction()==ChannelView.ACTION_ENTER){
				startFetch();
				channelId=(String)event.getData();
				scheduleScreen = new ScheduleScreen();
				scheduleScreen.addActionListener(this);
				
				Thread t= new Thread(new Runnable(){

					public void run() {
						InputStream is = null;
						try {
							is = tvapi.getTodaySchedule(channelId);
							if(is!=null){
								final Vector programData = createProgramData(is);
								UiApplication.getUiApplication().invokeLater(new Runnable(){

									public void run() {
										//if(ThaiTVSchedule.isPopupDisplay())
										//if(ThaiTVSchedule.isFetching())
											scheduleScreen.setData(programData);
									}
								});
								
							}
						}
						catch (Exception e) {
							e.printStackTrace();
							MsgBox.alert("Connection Error, Please check your internet connection.");
						}
						finally{
							stopFetch();
							if(is!=null)try{is.close();}catch(IOException e){e.printStackTrace();}
							
							//safeClosePopup();
						}
					}
					
				});
				t.start();
				
				TransitionContext transition = new TransitionContext(TransitionContext.TRANSITION_SLIDE);
				transition.setIntAttribute(TransitionContext.ATTR_DURATION, 250);
				transition.setIntAttribute(TransitionContext.ATTR_STYLE, TransitionContext.STYLE_PUSH);
				transition.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_LEFT);
				UiEngineInstance engine = Ui.getUiEngineInstance();
				engine.setTransition(null, scheduleScreen, UiEngineInstance.TRIGGER_PUSH, transition);
				
				pushScreen(scheduleScreen);
//				UiApplication.getUiApplication().invokeLater(new Runnable(){
//
//					public void run() {
//						//pushScreen(popupLoading);
//						//showPopup();
//						startFetch();
//					}
//					
//				}, 250, false);
			}
			else if(event.getAction()==HomeScreen.TAB_NOW_SHOWING){
				//pushScreen(popupLoading);
				//showPopup();
				startFetch();
				final NowShowingView now = (NowShowingView)event.getData();
				Thread t = new Thread(new Runnable(){

					public void run() {
						InputStream is = null;
						try {
							is = tvapi.getNowShowing();
							if(is!=null){
								final Vector programData = createProgramData(is);
								UiApplication.getUiApplication().invokeLater(new Runnable(){

									public void run() {
										//if(ThaiTVSchedule.isPopupDisplay())
										//if(ThaiTVSchedule.isFetching())
											now.setData(programData);
									}
								});
							}
						} 
						catch (Exception e) {
							e.printStackTrace();
							MsgBox.alert("Connection Error, Please check your internet connection.");
						}
						finally{
							stopFetch();
							if(is!=null)try{is.close();}catch(IOException e){e.printStackTrace();}
							//safeClosePopup();
						}
					}
					
				});
				t.start();
			}
			else if(event.getAction()==HomeScreen.TAB_FAVORITE){
				//pushScreen(popupLoading);
				//showPopup();
				startFetch();
				final FavoriteView favorite = (FavoriteView)event.getData();
				Thread t = new Thread(new Runnable(){

					public void run() {
						InputStream is = null;
						try {
							is = tvapi.getFavorite();
							if(is!=null){
								final Vector programData = createProgramData(is);
								UiApplication.getUiApplication().invokeLater(new Runnable(){

									public void run() {
										//if(ThaiTVSchedule.isPopupDisplay())
										//if(ThaiTVSchedule.isFetching())
											favorite.setData(programData);
									}
								});
							}
						} 
						catch (Exception e) {
							e.printStackTrace();
							MsgBox.alert("Connection Error, Please check your internet connection.");
						}
						finally{
							stopFetch();
							if(is!=null)try{is.close();}catch(IOException e){e.printStackTrace();}
							//safeClosePopup();
						}
					}
					
				});
				t.start();
			}
			else if(event.getAction()==HomeScreen.TAB_SEARCH){
				//pushScreen(popupLoading);
				//showPopup();
				startFetch();
				final SearchView search = (SearchView)event.getData();
				Thread t = new Thread(new Runnable(){

					public void run() {
						InputStream is = null;
						try {
							String key = search.getKeyword();
							is = tvapi.getSearch(key);
							if(is!=null){
								final Vector programData = createProgramData(is);
								UiApplication.getUiApplication().invokeLater(new Runnable(){

									public void run() {
										//if(ThaiTVSchedule.isPopupDisplay())
										//if(ThaiTVSchedule.isFetching())
											search.setData(programData);
									}
								});
							}
						} 
						catch (Exception e) {
							e.printStackTrace();
							MsgBox.alert("Connection Error, Please check your internet connection.");
						}
						finally{
							stopFetch();
							if(is!=null)try{is.close();}catch(IOException e){e.printStackTrace();}
							//safeClosePopup();
						}
					}
					
				});
				t.start();
			}
		}
		else if(event.getSource()==scheduleScreen){
			if(event.getAction()==ScheduleScreen.ACTION_CLOSE){
				TransitionContext transition = new TransitionContext(TransitionContext.TRANSITION_SLIDE);
				transition.setIntAttribute(TransitionContext.ATTR_DURATION, 250);
				transition.setIntAttribute(TransitionContext.ATTR_STYLE, TransitionContext.STYLE_PUSH);
				UiEngineInstance engine = Ui.getUiEngineInstance();
				transition.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_RIGHT);
				engine.setTransition(scheduleScreen, null, UiEngineInstance.TRIGGER_POP, transition);
				popScreen(scheduleScreen);
			}
			else if(event.getAction()==ScheduleScreen.ACTION_CHANGE_DATE){				
				//pushScreen(popupLoading);
				//showPopup();
				startFetch();
				//scheduleScreen.invalidate();
				Thread t = new Thread(new Runnable(){

					public void run() {
						Date requestDate = scheduleScreen.getRequestDate();
						final String strDate = TVApi.timeMillisTodate(requestDate.getTime());
						InputStream is = null;
						try {
							is = tvapi.getAllDaySchedule(channelId, requestDate);
							if(is!=null){
								final Vector programData = createProgramData(is);
								UiApplication.getUiApplication().invokeLater(new Runnable(){

									public void run() {
										//if(ThaiTVSchedule.isPopupDisplay()){
										
										//if(ThaiTVSchedule.isFetching()){
											scheduleScreen.setData(programData);
											scheduleScreen.setDateLabel(strDate);
										//}
										//}
									}
								});
								
							}
						}
						catch (Exception e) {
							e.printStackTrace();
							//Dialog.alert("Connection Error, Please check your internet connection");
							MsgBox.alert("Connection Error, Please check your internet connection "+e.toString());
						}
						finally{
							//safeClosePopup();
							stopFetch();
							if(is!=null)try{is.close();}catch(IOException e){e.printStackTrace();}
						}
					}
					
				});
				t.start();
			}
		}
	}

	protected void onVerifyDone() {
		//final Font fontSmall = Display.getHeight() >= 480 ? Font.getDefault() : Font.getDefault().derive(Font.PLAIN, 16);
		VerticalFieldManager vfm = new VerticalFieldManager();
		HorizontalFieldManager hfm = new HorizontalFieldManager();
		LabelField connecting = new LabelField("Thai TV Schedule", Manager.USE_ALL_WIDTH){
			public void paint(Graphics g){
				g.setColor(0x424441);
				super.paint(g);
			}
		};
		connecting.setPadding(4, 4, 4, 4);
		connecting.setBackground(BackgroundFactory.createSolidBackground(0xffffff));
		//connecting.setFont(fontSmall.derive(Font.BOLD));
		vfm.add(connecting);
		vfm.add(hfm);
		popupLoading = new CustomPopupScreen(vfm);
		//popupLoading.setPadding(10, 10, 10, 10);
		//Bitmap icon = ResourceManager.getBitmapResource("icon.png");
//		popupLoading.add(new BitmapField(Bitmap.getPredefinedBitmap(Bitmap.HOURGLASS)));
		//hfm.setPadding(4, 4, 4, 4);
		//hfm.add(new BitmapField(icon));
		
		LabelField fetch = new LabelField("   Fecthing...", Field.USE_ALL_WIDTH){
			public void paint(Graphics g){
				ThaiTVSchedule.paintFetching2(g, 0);
			}
		};
		fetch.setMargin(10, 0, 0, 0);
		//fetch.setFont(fontSmall);
		vfm.add(fetch);
		
		CommandButtonField button = new CommandButtonField("Cancel", Manager.USE_ALL_WIDTH|Field.FIELD_RIGHT){
//			public void layout(int width, int height){
//				width = Math.min(width, fontSmall.getAdvance("Cancel"));
//				height = Math.min(height, fontSmall.getHeight());
//				setExtent(width, height);
//				super.layout(width, height);
//			}
		}; 
		button.setCommand(new Command(new CommandHandler(){

			public void execute(ReadOnlyCommandMetadata metadata, Object context) {
				safeClosePopup();
			}
			
		}));
		button.setMargin(4, 4, 4, 4);
		//button.setFont(fontSmall);
		vfm.add(button);
		
		splashScreen = new SplashScreen();
		splashScreen.addActionListener(this);
		
		pushScreen(splashScreen);
		
		Activate atv = new Activate();
		atv.request();
	}

	protected void checkPermissions() {
		ApplicationPermissionsManager apm = ApplicationPermissionsManager.getInstance();
		ApplicationPermissions original = apm.getApplicationPermissions();

		if (
				//(original.getPermission(ApplicationPermissions.PERMISSION_APPLICATION_MANAGEMENT)==ApplicationPermissions.VALUE_ALLOW)
				(original.getPermission(ApplicationPermissions.PERMISSION_AUTHENTICATOR_API)==ApplicationPermissions.VALUE_ALLOW)
				//&&(original.getPermission(ApplicationPermissions.PERMISSION_BLUETOOTH)==ApplicationPermissions.VALUE_ALLOW)
				//&&(original.getPermission(ApplicationPermissions.PERMISSION_BROWSER_FILTER)==ApplicationPermissions.VALUE_ALLOW)
				//&&(original.getPermission(ApplicationPermissions.PERMISSION_CROSS_APPLICATION_COMMUNICATION)==ApplicationPermissions.VALUE_ALLOW)
				//&&(original.getPermission(ApplicationPermissions.PERMISSION_DEVICE_SETTINGS)==ApplicationPermissions.VALUE_ALLOW)
				//&&(original.getPermission(ApplicationPermissions.PERMISSION_DISPLAY_LOCKED)==ApplicationPermissions.VALUE_ALLOW)
				//&&(original.getPermission(ApplicationPermissions.PERMISSION_EMAIL)==ApplicationPermissions.VALUE_ALLOW)
				//&&(original.getPermission(ApplicationPermissions.PERMISSION_FILE_API)==ApplicationPermissions.VALUE_ALLOW)
				//&&(original.getPermission(ApplicationPermissions.PERMISSION_IDLE_TIMER)==ApplicationPermissions.VALUE_ALLOW)
				&&(original.getPermission(ApplicationPermissions.PERMISSION_INPUT_SIMULATION)==ApplicationPermissions.VALUE_ALLOW)
				&&(original.getPermission(ApplicationPermissions.PERMISSION_INTERNET)==ApplicationPermissions.VALUE_ALLOW)
				//&&(original.getPermission(ApplicationPermissions.PERMISSION_LOCATION_DATA)==ApplicationPermissions.VALUE_ALLOW)
				//&&(original.getPermission(ApplicationPermissions.PERMISSION_MEDIA)==ApplicationPermissions.VALUE_ALLOW)
				&&(original.getPermission(ApplicationPermissions.PERMISSION_ORGANIZER_DATA)==ApplicationPermissions.VALUE_ALLOW)
				//&&(original.getPermission(ApplicationPermissions.PERMISSION_PHONE)==ApplicationPermissions.VALUE_ALLOW)
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
		//permRequest.addPermission(ApplicationPermissions.PERMISSION_APPLICATION_MANAGEMENT);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_AUTHENTICATOR_API);
		//permRequest.addPermission(ApplicationPermissions.PERMISSION_BLUETOOTH);
		//permRequest.addPermission(ApplicationPermissions.PERMISSION_BROWSER_FILTER);
		//permRequest.addPermission(ApplicationPermissions.PERMISSION_CROSS_APPLICATION_COMMUNICATION);
		//permRequest.addPermission(ApplicationPermissions.PERMISSION_DEVICE_SETTINGS);
		//permRequest.addPermission(ApplicationPermissions.PERMISSION_DISPLAY_LOCKED);
		//permRequest.addPermission(ApplicationPermissions.PERMISSION_EMAIL);
		//permRequest.addPermission(ApplicationPermissions.PERMISSION_FILE_API);
		//permRequest.addPermission(ApplicationPermissions.PERMISSION_IDLE_TIMER);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_INPUT_SIMULATION);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_INTERNET);
		//permRequest.addPermission(ApplicationPermissions.PERMISSION_LOCATION_DATA);
		//permRequest.addPermission(ApplicationPermissions.PERMISSION_MEDIA);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_ORGANIZER_DATA);
		//permRequest.addPermission(ApplicationPermissions.PERMISSION_PHONE);
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
	
	private Vector createProgramData(InputStream is) throws ParserConfigurationException, SAXException, IOException {
		Vector nodes = tvapi.parseXMLAsVector(is);
		Vector programData = new Vector();
		Enumeration enum = nodes.elements();
		while(enum.hasMoreElements()){
			XMLNode node = (XMLNode)enum.nextElement();
			if(node.getNode().equalsIgnoreCase("row")){
				XMLNode id = (XMLNode)enum.nextElement();
				XMLNode titleth = (XMLNode)enum.nextElement();
				XMLNode titleen = (XMLNode)enum.nextElement();
				XMLNode desc = (XMLNode)enum.nextElement();
				XMLNode start = (XMLNode)enum.nextElement();
				XMLNode stop = (XMLNode)enum.nextElement();
				XMLNode channelid = (XMLNode)enum.nextElement();
				XMLNode channelname = (XMLNode)enum.nextElement();
				ProgramData data = tvapi.new ProgramData();
				data.id = id.getElement();
				if(data.id==null) break;
				data.title = titleth.getElement();
				data.beginDateTime = start.getElement();
				data.beginTime = TVApi.dateToTime(start.getElement());
				data.endDateTime = stop.getElement();
				data.endTime = TVApi.dateToTime(stop.getElement());
				data.channelId = channelid.getElement();
				data.isFavorite = FavoritePersistent.hasFavorite(data.id);
				data.day = TVApi.getDay(data.beginDateTime);
				programData.addElement(data);
			}
		}
		return programData;
	}
	
	public static boolean popup = false;
	public static boolean fetching = false;
	
	public static void startFetch(){
		fetching=true;
	}
	
	public static void stopFetch(){
		fetching=false;
	}
	
	public static boolean isFetching(){
		return fetching;
	}
	
	public static void showPopup(){
		TransitionContext transition = new TransitionContext(TransitionContext.TRANSITION_ZOOM);
		transition.setIntAttribute(TransitionContext.ATTR_DURATION, 100);
		transition.setIntAttribute(TransitionContext.ATTR_STYLE, TransitionContext.STYLE_OVER);
//		transition.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_LEFT);
		transition.setIntAttribute(TransitionContext.ATTR_KIND, TransitionContext.KIND_IN);
		UiEngineInstance engine = Ui.getUiEngineInstance();
		engine.setTransition(null, popupLoading, UiEngineInstance.TRIGGER_PUSH, transition);
		UiApplication.getUiApplication().pushScreen(popupLoading);
		popup = true;
	}
	
	public static boolean isPopupDisplay(){
		return popup;
	}
	
	public static void safeClosePopup() {
		if(popupLoading.isDisplayed()){
			UiApplication.getUiApplication().invokeAndWait(new Runnable(){

				public void run() {
					popupLoading.close();
					popup=false;
					UiApplication.getUiApplication().getActiveScreen().invalidate();
				}
				
			});
			
		}
	}

	public static void main(String args[]){
		ThaiTVSchedule app = new ThaiTVSchedule();
		app.enterEventDispatcher();
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
	
	public static void paintFetching(Graphics g, int translateY) {
		int h = g.getFont().getHeight(Ui.UNITS_px)/2;
		int w = h/2;
		int y = g.getFont().getHeight(Ui.UNITS_px)/5;
		int hh = h/2;
		int yy = y+hh/2;
		int max_possible_width = h*3 + g.getFont().getAdvance("Fetching...");
		int tx = (Display.getWidth()-max_possible_width)/2;
		int ty = translateY;
		g.translate(tx, ty);
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
		g.translate(-tx, -ty);
	}
	
	public static void paintFetching2(Graphics g, int translateY) {
		int h = g.getFont().getHeight(Ui.UNITS_px)/2;
		int w = h/2;
		int y = g.getFont().getHeight(Ui.UNITS_px)/5;
		int hh = h/2;
		int yy = y+hh/2;
		int max_possible_width = h*3 + g.getFont().getAdvance("Fetching...");
		XYRect r = g.getClippingRect();
		int tx = (r.width-max_possible_width)/2;
		int ty = translateY;
		g.translate(tx, ty);
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
		g.setColor(0xffffff);
		g.drawText("Fetching...", allWidth, 0);
		g.translate(-tx, -ty);
	}
}
