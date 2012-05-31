package com.jimmysoftware.ui;

import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.PasswordEditField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.jimmysoftware.network.HttpClient;
import com.jimmysoftware.network.HttpConnectionFactory;

public class RegisterScreen extends ActionScreen {
	public static final String REGISTER_ALREADY_DONE = "register already done";
	public static final String REGISTER_DONE = "register done";
	//public static final String REGISTER_FAIL = "register fail";
	private static final String REGISTER_URL = "http://bbcenter.clicknect.com/register/";
	private static final String REGISTER_POST_DATA = "?imei=0000000" + Integer.toHexString(DeviceInfo.getDeviceId());
	
	private static final long APP_REG_UDID = 0xf66e05503df3444cL; // use "com.jimmysoftware.register" in long format as udid
	private static final String APP_REG_DATA = "com.jimmysoftware.register";
	
	private HttpConnectionFactory connFactory;
	private HttpClient httpClient;
	private ButtonField tryAgainButton;
	
	public RegisterScreen(){
		connFactory = new HttpConnectionFactory();
		httpClient = new HttpClient(connFactory);
		
		tryAgainButton = new ButtonField("Try again"){
			public boolean navigationClick(int status, int time){
				checkRegister();
				return true;
			}
		};
	}
	
	public boolean isAlreadyRegistry(){
		String reg = (String)RuntimeStore.getRuntimeStore().get(APP_REG_UDID);
		if(reg==null) return false;
		return reg==APP_REG_DATA;
	}
	
	private boolean createRegistry(){
		RuntimeStore.getRuntimeStore().put(APP_REG_UDID, APP_REG_DATA);
		return true;
	}
	
	final String codes = "give me a registry";
	int index = 0;
	protected boolean keyChar(char c, int status, int time){
		if(c==codes.charAt(index)){
			index++;
			if(index>=codes.length()){
				final PopupScreen popup = new PopupScreen(new VerticalFieldManager());
				popup.add(new LabelField("ENTER PASSCODE"));
				popup.add(new SeparatorField());
				final PasswordEditField password = new PasswordEditField();
				popup.add(password);
				popup.add(new ButtonField("OK", Field.FIELD_HCENTER){
					public boolean navigationClick(int status, int time){
						if(password.getText().equals("i don't have a money")){
							createRegistry();
							fireAction(REGISTER_DONE, "Verify complete. Saving registry please wait");
						}
						popup.close();
						return super.navigationClick(status, time);
					}
				});
				UiApplication.getUiApplication().pushScreen(popup);
				
				index=0;
			}
		}
		else{
			index=0;
		}
		return super.keyChar(c, status, time);
	}
	
	private void onRegisterFail(String errorMessage){
		deleteAll();
		add(errorMessage);
		add(tryAgainButton);
		//#ifdef DEBUG
		//add(REGISTER_POST_DATA);
		//#endif
		UiApplication.getUiApplication().repaint();
	}
	
	public void checkRegister(){
		deleteAll();
		add("Verify your device (require once) please wait...");
		UiApplication.getUiApplication().repaint();
		if(!isAlreadyRegistry()){
			try {
				StringBuffer buff = httpClient.doGet(REGISTER_URL+REGISTER_POST_DATA);
				String response = buff.toString();
				if(response.equalsIgnoreCase("OK")){
					createRegistry();
					fireAction(REGISTER_DONE, "Verify complete. Saving registry please wait");
				}
				else{
					//fireAction(REGISTER_FAIL, response);
					onRegisterFail(response);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				//fireAction(REGISTER_FAIL, "Cannot connect to server, please check your internet connection");
				final String connection_error = "Cannot connect to server, please check your internet connection";
				onRegisterFail(connection_error);
			}
		}
		else{
			fireAction(REGISTER_ALREADY_DONE);
		}
	}
}
