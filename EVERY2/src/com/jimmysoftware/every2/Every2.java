package com.jimmysoftware.every2;

//import net.rim.blackberry.api.bbm.platform.profile.BBMPlatformContact;
import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.applicationcontrol.ApplicationPermissionsManager;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.JPEGEncodedImage;
import net.rim.device.api.ui.TransitionContext;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngineInstance;
import net.rim.device.api.ui.container.MainScreen;

import com.clicknect.webapi.Activate;
import com.jimmysoftware.every2.HomeScreen.ListFieldInfo;
import com.jimmysoftware.network.HttpConnectionFactory;
import com.jimmysoftware.ui.Action;
import com.jimmysoftware.ui.JSApplication;
import com.jimmysoftware.ui.PINContactScreen;
import com.jimmysoftware.ui.UiFactory;

public class Every2 extends JSApplication{// implements BBMBridgeCallback{
	private HttpConnectionFactory connFactory;
	private HomeScreen homeScreen;
	private BrowserScreen browserScreen;
	private LandscapeScreen landscapeScreen = null;
	private PINContactScreen contactScreen;
	
//	private static BBMBridge bbmBridge;
//	
//	public static BBMBridge getBBMBridge(){
//		return bbmBridge;
//	}
	
	public Every2(){
		super(false);
	}
 
    public static void main(String[] args){
    	UiFactory.setApplicationName("EVERY");
    	new Every2().enterEventDispatcher();
    }

	protected void onVerifyDone() {
		connFactory = new HttpConnectionFactory();
		
		homeScreen = new HomeScreen(connFactory);
		homeScreen.addActionListener(this);
		
		contactScreen = new PINContactScreen();
		contactScreen.addActionListener(this);
		
		pushScreen(homeScreen);
		homeScreen.feed();
		
		Activate atv = new Activate();
		atv.request();
		
//		UiApplication.getUiApplication().invokeLater(new Runnable(){
//
//			public void run() {
//				bbmBridge = new BBMBridge(Every2.this);
//				bbmBridge.start();
//			}
//			
//		}, 1000, false);
	}

	protected void checkPermissions() {
		ApplicationPermissionsManager apm = ApplicationPermissionsManager.getInstance();
		ApplicationPermissions original = apm.getApplicationPermissions();

		if (
				(original.getPermission(ApplicationPermissions.PERMISSION_AUTHENTICATOR_API)==ApplicationPermissions.VALUE_ALLOW)
				&&(original.getPermission(ApplicationPermissions.PERMISSION_BROWSER_FILTER)==ApplicationPermissions.VALUE_ALLOW)
				&&(original.getPermission(ApplicationPermissions.PERMISSION_CROSS_APPLICATION_COMMUNICATION)==ApplicationPermissions.VALUE_ALLOW)
				&&(original.getPermission(ApplicationPermissions.PERMISSION_INPUT_SIMULATION)==ApplicationPermissions.VALUE_ALLOW)
				&&(original.getPermission(ApplicationPermissions.PERMISSION_INTERNET)==ApplicationPermissions.VALUE_ALLOW)
				&&(original.getPermission(ApplicationPermissions.PERMISSION_LOCATION_DATA)==ApplicationPermissions.VALUE_ALLOW)
				&&(original.getPermission(ApplicationPermissions.PERMISSION_SECURITY_DATA)==ApplicationPermissions.VALUE_ALLOW)
				&&(original.getPermission(ApplicationPermissions.PERMISSION_ORGANIZER_DATA)==ApplicationPermissions.VALUE_ALLOW)
				&&(original.getPermission(ApplicationPermissions.PERMISSION_SERVER_NETWORK)==ApplicationPermissions.VALUE_ALLOW)
				&&(original.getPermission(ApplicationPermissions.PERMISSION_WIFI)==ApplicationPermissions.VALUE_ALLOW)
			)
		{
			return;
		}

		ApplicationPermissions permRequest = new ApplicationPermissions();
		permRequest.addPermission(ApplicationPermissions.PERMISSION_AUTHENTICATOR_API);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_BROWSER_FILTER);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_CROSS_APPLICATION_COMMUNICATION);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_INPUT_SIMULATION);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_INTERNET);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_LOCATION_DATA);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_SECURITY_DATA);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_ORGANIZER_DATA);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_SERVER_NETWORK);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_WIFI);

		boolean acceptance = ApplicationPermissionsManager.getInstance().invokePermissionsRequest(permRequest);

		if (acceptance) {
			// User has accepted all of the permissions.
			return;
		} else {
		}
	}
	//static int iconID = 1;
	protected void doAction(Action event) {
		System.out.println("Invoke doAction, source="+event.getSource()+", action="+event.getAction());
		if(event.getSource()==homeScreen){
			if (event.getAction().equals(HomeScreen.ACTION_ENTER)) {
				ListFieldInfo f = (ListFieldInfo) event.getData();
				//Dialog.alert("webview "+f.getURL());
				browserScreen = new BrowserScreen(f.url);
				browserScreen.addActionListener(this);
				
				// Transition
				TransitionContext transition = new TransitionContext(TransitionContext.TRANSITION_SLIDE);
				transition.setIntAttribute(TransitionContext.ATTR_DURATION, 250);
				transition.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_LEFT);
				transition.setIntAttribute(TransitionContext.ATTR_STYLE, TransitionContext.STYLE_PUSH);
				UiEngineInstance engine = Ui.getUiEngineInstance();
				engine.setTransition(homeScreen, browserScreen, UiEngineInstance.TRIGGER_PUSH, transition);
				
				pushScreen(browserScreen);
				
				
				EncodedImage icon = JPEGEncodedImage.encode(f.bitmap, 50);
				String text = f.header;
//				bbmBridge.registerProfileBoxIcon(Integer.parseInt(f.guid), icon);
//				bbmBridge.addProfileBoxItem(Integer.parseInt(f.guid), icon, text);
				//iconID++;
			}
			else if(event.getAction().equals(HomeScreen.ACTION_CHANGE_VIEW)){
				HomeScreen home = (HomeScreen)event.getSource();
				if(landscapeScreen==null){
					landscapeScreen = new LandscapeScreen(home.landscapeObjects);
					landscapeScreen.addActionListener(this);
				}
				else{
					landscapeScreen.setContent(home.landscapeObjects);
				}
				// Transition
				TransitionContext transition = new TransitionContext(TransitionContext.TRANSITION_FADE);
				transition.setIntAttribute(TransitionContext.ATTR_DURATION, 250);
				transition.setIntAttribute(TransitionContext.ATTR_KIND, TransitionContext.KIND_IN); 
				transition.setIntAttribute(TransitionContext.ATTR_STYLE, TransitionContext.STYLE_OVER);
				UiEngineInstance engine = Ui.getUiEngineInstance();
				engine.setTransition(landscapeScreen, null, UiEngineInstance.TRIGGER_PUSH, transition);
				
				pushScreen(landscapeScreen);
			}
			else if(event.getAction()==HomeScreen.ACTION_RECOMMENED){
				pushScreen(contactScreen);
			}
		}
		else if(event.getSource() == landscapeScreen){
			if(event.getAction().equals(LandscapeScreen.ACTION_CHANGE_VIEW)){
				popScreen(landscapeScreen);
			}
			else if(event.getAction().equals(LandscapeScreen.ACTION_ENTER)){
				String url = (String) event.getData();
				browserScreen = new BrowserScreen(url);
				browserScreen.addActionListener(this);
				
				// Transition
				TransitionContext transition = new TransitionContext(TransitionContext.TRANSITION_SLIDE);
				transition.setIntAttribute(TransitionContext.ATTR_DURATION, 250);
				transition.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_LEFT);
				transition.setIntAttribute(TransitionContext.ATTR_STYLE, TransitionContext.STYLE_PUSH);
				UiEngineInstance engine = Ui.getUiEngineInstance();
				engine.setTransition(landscapeScreen, null, UiEngineInstance.TRIGGER_POP, transition);
				engine.setTransition(null, browserScreen, UiEngineInstance.TRIGGER_PUSH, transition);
				
				popScreen(landscapeScreen);
				pushScreen(browserScreen);
			}
			else if(event.getAction().equals(LandscapeScreen.ACTION_CLOSE)){
				// Transition
				TransitionContext transition = new TransitionContext(TransitionContext.TRANSITION_FADE);
				transition.setIntAttribute(TransitionContext.ATTR_DURATION, 250);
				transition.setIntAttribute(TransitionContext.ATTR_KIND, TransitionContext.KIND_OUT); 
				transition.setIntAttribute(TransitionContext.ATTR_STYLE, TransitionContext.STYLE_OVER);
				UiEngineInstance engine = Ui.getUiEngineInstance();
				engine.setTransition(landscapeScreen, null, UiEngineInstance.TRIGGER_POP, transition);
				
				popScreen(landscapeScreen);
			}
		}
		else if(event.getSource()==browserScreen){
			if(event.getAction()==BrowserScreen.ON_LANDSCAPE_CLOSE){
				if(landscapeScreen!=null){
					landscapeScreen.setContent(homeScreen.landscapeObjects);
					// Transition
					TransitionContext transition = new TransitionContext(TransitionContext.TRANSITION_SLIDE);
					transition.setIntAttribute(TransitionContext.ATTR_DURATION, 250);
					transition.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_RIGHT);
					transition.setIntAttribute(TransitionContext.ATTR_STYLE, TransitionContext.STYLE_PUSH);
					UiEngineInstance engine = Ui.getUiEngineInstance();
					engine.setTransition(browserScreen, null, UiEngineInstance.TRIGGER_POP, transition);
					engine.setTransition(null, landscapeScreen, UiEngineInstance.TRIGGER_PUSH, transition);
					
					//popScreen(landscapeScreen);
					//pushScreen(browserScreen);
					//pushScreen(landscapeScreen);
					popScreen(browserScreen);
					pushScreen(landscapeScreen);
				}
				else{
					landscapeScreen = new LandscapeScreen(homeScreen.landscapeObjects);
					landscapeScreen.addActionListener(this);
					
					// Transition
					TransitionContext transition = new TransitionContext(TransitionContext.TRANSITION_SLIDE);
					transition.setIntAttribute(TransitionContext.ATTR_DURATION, 250);
					transition.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_RIGHT);
					transition.setIntAttribute(TransitionContext.ATTR_STYLE, TransitionContext.STYLE_PUSH);
					UiEngineInstance engine = Ui.getUiEngineInstance();
					engine.setTransition(browserScreen, null, UiEngineInstance.TRIGGER_POP, transition);
					engine.setTransition(null, landscapeScreen, UiEngineInstance.TRIGGER_PUSH, transition);
					
					popScreen(browserScreen);
					pushScreen(landscapeScreen);
				}
			}
			else if(event.getAction()==BrowserScreen.ON_PORTRAIT_CLOSE){
				// Transition
				TransitionContext transition = new TransitionContext(TransitionContext.TRANSITION_SLIDE);
				transition.setIntAttribute(TransitionContext.ATTR_DURATION, 250);
				transition.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_RIGHT);
				transition.setIntAttribute(TransitionContext.ATTR_STYLE, TransitionContext.STYLE_PUSH);
				UiEngineInstance engine = Ui.getUiEngineInstance();
				engine.setTransition(browserScreen, null, UiEngineInstance.TRIGGER_POP, transition);
				
				//pushScreen(browserScreen);
				popScreen(browserScreen);
			}
		}
	}
	
	public void activate(){
		if(landscapeScreen!=null){
			
			//if(landscapeScreen.isDisplayed() && landscapeScreen.isFocus()){
			if(getActiveScreen() == landscapeScreen){
				if(Display.getOrientation()==Display.ORIENTATION_PORTRAIT)
					popScreen(landscapeScreen);
			}
			//}
		}
		//super.activate();
	}

//	public void onInitialized(boolean success) {
////		bbmBridge.changePersonalMessage("I'm using EVERY ThaiNews \u00A9 2011 Click Connect Co., Ltd.");
//	}
//
//	public void onContactJoined(BBMPlatformContact contact) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	public void onContactLeft(BBMPlatformContact contact) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	public void onJoining(BBMPlatformContact contact) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	public void onMessageReceived(BBMPlatformContact contact, String type,
//			String message) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	public void exitApp() {
//		// TODO Auto-generated method stub
//		
//	}
}
