package com.jimmysoftware.ui;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Status;

public abstract class JSApplication extends UiApplication implements ActionListener {
	private RegisterScreen registerScreen;
	
	public JSApplication(boolean needVerify){
		checkPermissions();
		if(needVerify){
			verifyApp();
		}
		else{
			onVerifyDone();
		}
	}
	
	protected void verifyApp(){
		registerScreen = new RegisterScreen();
		registerScreen.addActionListener(this);
		
		if(!registerScreen.isAlreadyRegistry()){
			pushScreen(registerScreen);
			invokeLater(new Runnable(){

				public void run() {
					// TODO Auto-generated method stub
					registerScreen.checkRegister();
				}
				
			});
		}
		else{
			invokeLater(new Runnable(){
				public void run() {
					onVerifyDone();
				}
			});
		}
	}
	
	public void onAction(Action event) {
		if(event.getSource()==registerScreen){
			if(event.getAction()==RegisterScreen.REGISTER_DONE || event.getAction()==RegisterScreen.REGISTER_ALREADY_DONE){
				if(event.getAction()==RegisterScreen.REGISTER_DONE){
					String msg = (String)event.getData();
					Status.show(msg);
				}
				popScreen(registerScreen);
				onVerifyDone();
			}
		}
		else{
			doAction(event);
		}
	}
	
	
	protected abstract void doAction(Action event);
	protected abstract void onVerifyDone();
	protected abstract void checkPermissions();
}
