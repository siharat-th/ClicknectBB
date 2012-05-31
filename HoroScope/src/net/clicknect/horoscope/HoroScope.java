package net.clicknect.horoscope;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.clicknect.horoscope.OnepostApi.ServiceInfo;
import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.applicationcontrol.ApplicationPermissionsManager;
import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldConfig;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.TransitionContext;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngineInstance;
import net.rim.device.api.ui.container.MainScreen;

import com.clicknect.webapi.Activate;
import com.jimmysoftware.network.HttpClient;
import com.jimmysoftware.network.HttpConnectionFactory;
import com.jimmysoftware.ui.Action;
import com.jimmysoftware.ui.EventThreadDialog;
import com.jimmysoftware.ui.JSApplication;
import com.jimmysoftware.ui.PINContactScreen;
import com.jimmysoftware.ui.UiFactory;

public class HoroScope extends JSApplication{
	private HttpConnectionFactory connFactory;
	private HttpClient httpClient;
	
    private SplashScreen splashScreen;
    private HomeScreen homeScreen;
    private PredictionScreen predictionScreen;
    private PINContactScreen contactScreen;
    
	public HoroScope(){
		super(false);
	}
	
    public static void main(String[] args){
    	UiFactory.setApplicationName("HOROSCOPE");
    	new HoroScope().enterEventDispatcher();
    }

	protected void onVerifyDone() {
		connFactory = new HttpConnectionFactory();
		httpClient = new HttpClient(connFactory);
		
		splashScreen = new SplashScreen(connFactory);
		splashScreen.addActionListener(this);
		
		homeScreen = new HomeScreen();
		homeScreen.addActionListener(this);
		
		contactScreen = new PINContactScreen();
		contactScreen.addActionListener(this);
		
		splashScreen.startFetch();
		pushScreen(splashScreen);
		
		Activate atv = new Activate();
		atv.request();
	}

	protected void checkPermissions() {
		ApplicationPermissionsManager apm = ApplicationPermissionsManager.getInstance();
		ApplicationPermissions original = apm.getApplicationPermissions();

		if (
				  (original.getPermission(ApplicationPermissions.PERMISSION_APPLICATION_MANAGEMENT)==ApplicationPermissions.VALUE_ALLOW)
				&&(original.getPermission(ApplicationPermissions.PERMISSION_AUTHENTICATOR_API)==ApplicationPermissions.VALUE_ALLOW)
				&&(original.getPermission(ApplicationPermissions.PERMISSION_BROWSER_FILTER)==ApplicationPermissions.VALUE_ALLOW)
				&&(original.getPermission(ApplicationPermissions.PERMISSION_CROSS_APPLICATION_COMMUNICATION)==ApplicationPermissions.VALUE_ALLOW)
				&&(original.getPermission(ApplicationPermissions.PERMISSION_INPUT_SIMULATION)==ApplicationPermissions.VALUE_ALLOW)
				&&(original.getPermission(ApplicationPermissions.PERMISSION_INTERNET)==ApplicationPermissions.VALUE_ALLOW)
				&&(original.getPermission(ApplicationPermissions.PERMISSION_SECURITY_DATA)==ApplicationPermissions.VALUE_ALLOW)
				&&(original.getPermission(ApplicationPermissions.PERMISSION_FILE_API)==ApplicationPermissions.VALUE_ALLOW)
				&&(original.getPermission(ApplicationPermissions.PERMISSION_ORGANIZER_DATA)==ApplicationPermissions.VALUE_ALLOW)
				&&(original.getPermission(ApplicationPermissions.PERMISSION_SERVER_NETWORK)==ApplicationPermissions.VALUE_ALLOW)
				&&(original.getPermission(ApplicationPermissions.PERMISSION_WIFI)==ApplicationPermissions.VALUE_ALLOW)
			)
		{
			return;
		}

		ApplicationPermissions permRequest = new ApplicationPermissions();
		permRequest.addPermission(ApplicationPermissions.PERMISSION_APPLICATION_MANAGEMENT);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_AUTHENTICATOR_API);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_BROWSER_FILTER);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_CROSS_APPLICATION_COMMUNICATION);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_INPUT_SIMULATION);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_INTERNET);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_SECURITY_DATA);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_FILE_API);
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
	
	protected void doAction(Action event) {
		if(event.getSource()==splashScreen){
			if(event.getAction()==SplashScreen.ACTION_FETCH_DONE){
				Vector horoData = (Vector)event.getData();
				homeScreen.setHoroData(horoData);
				
				TransitionContext transition = new TransitionContext(TransitionContext.TRANSITION_FADE);
		        transition.setIntAttribute(TransitionContext.ATTR_DURATION, 500);
		        transition.setIntAttribute(TransitionContext.ATTR_KIND, TransitionContext.KIND_IN);
		        UiEngineInstance engine = Ui.getUiEngineInstance();
		        engine.setTransition(null, homeScreen, UiEngineInstance.TRIGGER_PUSH, transition);
		        engine.setTransition(splashScreen, null, UiEngineInstance.TRIGGER_POP, transition);
		        
				pushScreen(homeScreen);
				popScreen(splashScreen);
			}
		}
		else if(event.getSource()==homeScreen){
			if(event.getAction()==HomeScreen.ACTION_PREDICTION){
				ZodiacData data = (ZodiacData)event.getData();
				predictionScreen = new PredictionScreen(connFactory, homeScreen.getStringDate(), data);
				predictionScreen.addActionListener(this);
				
				TransitionContext transition = new TransitionContext(TransitionContext.TRANSITION_SLIDE);
		        transition.setIntAttribute(TransitionContext.ATTR_DURATION, 250);
		        transition.setIntAttribute(TransitionContext.ATTR_STYLE, TransitionContext.STYLE_PUSH);
		        transition.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_LEFT);
		        UiEngineInstance engine = Ui.getUiEngineInstance();
		        engine.setTransition(null, predictionScreen, UiEngineInstance.TRIGGER_PUSH, transition);
		        
				pushScreen(predictionScreen);
			}
			else if(event.getAction()==HomeScreen.ACTION_RECOMMENED){
				pushScreen(contactScreen);
			}
		}
		else if(event.getSource()==predictionScreen){
			if(event.getAction()==PredictionScreen.ACTION_ON_CLOSE){
				TransitionContext transition = new TransitionContext(TransitionContext.TRANSITION_SLIDE);
		        transition.setIntAttribute(TransitionContext.ATTR_DURATION, 250);
		        transition.setIntAttribute(TransitionContext.ATTR_STYLE, TransitionContext.STYLE_PUSH);
		        transition.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_RIGHT);
		        UiEngineInstance engine = Ui.getUiEngineInstance();
		        engine.setTransition(predictionScreen, null, UiEngineInstance.TRIGGER_POP, transition);
		        
		        popScreen(predictionScreen);
			}
			else if(event.getAction()==PredictionScreen.ACTION_REGISTER){
				BrowserFieldConfig config = new BrowserFieldConfig();
				config.setProperty(BrowserFieldConfig.NAVIGATION_MODE, BrowserFieldConfig.NAVIGATION_MODE_POINTER);
				final BrowserField browserField = new BrowserField(config);
				
				MainScreen registerScreen = new MainScreen(){
					public boolean keyChar(char c, int status, int time){
						if(c==Keypad.KEY_ESCAPE){
							TransitionContext transition = new TransitionContext(TransitionContext.TRANSITION_SLIDE);
					        transition.setIntAttribute(TransitionContext.ATTR_DURATION, 250);
					        transition.setIntAttribute(TransitionContext.ATTR_STYLE, TransitionContext.STYLE_PUSH);
					        transition.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_RIGHT);
					        UiEngineInstance engine = Ui.getUiEngineInstance();
					        engine.setTransition(this, null, UiEngineInstance.TRIGGER_POP, transition);
					        
					        if(OnepostApi.PIN.equalsIgnoreCase("22c4faa7"))
					        	EventThreadDialog.informDialog(browserField.getDocumentUrl());
					        
					        popScreen(this);
					   
					        return true;
						}
						return super.keyChar(c, status, time);
					}
				};
				registerScreen.setTitle("Service Require Register");
				
				
				registerScreen.add(browserField);
				
				TransitionContext transition = new TransitionContext(TransitionContext.TRANSITION_SLIDE);
		        transition.setIntAttribute(TransitionContext.ATTR_DURATION, 250);
		        transition.setIntAttribute(TransitionContext.ATTR_STYLE, TransitionContext.STYLE_PUSH);
		        transition.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_LEFT);
		        UiEngineInstance engine = Ui.getUiEngineInstance();
		        engine.setTransition(null, registerScreen, UiEngineInstance.TRIGGER_PUSH, transition);
		        
				pushScreen(registerScreen);
				
				ServiceInfo serviceInfo = (ServiceInfo)event.getData();
				final String serviceName = serviceInfo.getServiceName();
				final OnepostApi onepostApi = ((PredictionScreen)event.getSource()).onepostApi;
				UiApplication.getUiApplication().invokeLater(new Runnable(){

					public void run() {
						String postData = onepostApi.getRegisterServicePostData(serviceName);
						Hashtable header = new Hashtable();
						header.put("Content-Length", "" + postData.length());
						header.put("Content-Type", "application/x-www-form-urlencoded");
						browserField.requestContent(OnepostApi.REGISTER_SERVICE_URL,
								postData.getBytes(),
								header);
					}
					
				}, 250, false);
			}
//			else if(event.getAction()==PredictionScreen.ACTION_SEND){
//				PredictionScreen prediction = (PredictionScreen)event.getSource();
//				ServiceInfo myServiceinfo = (ServiceInfo)event.getData();
//				String secureCode = myServiceinfo.getSecureCode();
//				String serviceName = myServiceinfo.getServiceName();
//				StringBuffer buff = new StringBuffer();
//				buff.append("ดูดวงประจำวันที่ "+ prediction.strDate +"\n");
//				buff.append(prediction.data.title+"\n");
//				//buff.append(prediction.data.description);
//				String textMessage = buff.toString();
//				
//				String postData;
//				InputStream is = null;
//				try {
//					postData = prediction.onepostApi.getSendServicePostDataAsString(secureCode, serviceName, textMessage);
//					is = prediction.onepostApi.getOnepostSendServiceResponse(postData);
//					Vector xmlNodes = ((PredictionScreen)event.getSource()).onepostApi.parseXMLAsVector(is);
//					
//					Enumeration _enum = xmlNodes.elements();
//					boolean success = false;
//					String message = "Message could not be sent.";
//					while(_enum.hasMoreElements()){
//						XMLNode xml = (XMLNode)_enum.nextElement();
//						if(xml.getNode().equalsIgnoreCase("R")){
//							if(xml.getElement().equalsIgnoreCase("Y"))
//								success = true;
//						}
//						else if(xml.getNode().equalsIgnoreCase("E")){
//							message = xml.getElement();
//						}
//					}
//					
//					if(success){
//						EventThreadDialog.statusDialog(message);
//					}
//					else{
//						EventThreadDialog.errorDialog(message);
//					}
//					
//				}
//				catch (Exception e) {
//					e.printStackTrace();
//					EventThreadDialog.informDialog("Send error "+e.toString());
//				}
//				finally{
//					if(is!=null) try{is.close();}catch(IOException e){e.printStackTrace();}
//				}
//			}
			else if(event.getAction()==PredictionScreen.ACTION_SEND){
				PredictionScreen prediction = (PredictionScreen)event.getSource();
				ServiceInfo myServiceinfo = (ServiceInfo)event.getData();
				String secureCode = myServiceinfo.getSecureCode();
				String serviceName = myServiceinfo.getServiceName();
				StringBuffer buff = new StringBuffer();
				//buff.append("ดูดวงประจำวันที่ "+ prediction.strDate +"\n");
				buff.append(prediction.data.title+"\n");
				buff.append(prediction.data.description);
				String textMessage = buff.toString();
				
				Hashtable postData;
				ByteArrayInputStream is = null;
					
				try {
					postData = prediction.onepostApi.getSendServicePostDataAsHashTable(secureCode, null, serviceName, textMessage);
					StringBuffer response = httpClient.doPost( OnepostApi.SEND_SERVICE_URL, postData);
					byte data[] = response.toString().getBytes();
					is = new ByteArrayInputStream(data);
					Vector xmlNodes = prediction.onepostApi.parseXMLAsVector(is);
					Enumeration _enum = xmlNodes.elements();
					boolean success = false;
					String message="";// = "Message has been sent.";
					String error="";// = "Message could not be send";
					while (_enum.hasMoreElements()) {
						XMLNode xml = (XMLNode) _enum.nextElement();
						if (xml.getNode().equalsIgnoreCase("R")) {
							if (xml.getElement().equalsIgnoreCase("Y"))
								success = true;
						}
						else if (xml.getNode().equalsIgnoreCase("M")) {
							message = xml.getElement();
						}
						else if (xml.getNode().equalsIgnoreCase("E")) {
							error = "Message could not be send " + xml.getElement();
						}
					}

					if (success) {
						EventThreadDialog.statusDialog(message);
					} 
					else {
						EventThreadDialog.errorDialog(error);
					}
					
				} 
				catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} 
				catch (Exception e) {
					e.printStackTrace();
				} 
				finally {
					if (is != null){
						try {
							is.close();
						} 
						catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
}
