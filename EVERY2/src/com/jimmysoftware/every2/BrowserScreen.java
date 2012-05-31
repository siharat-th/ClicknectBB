package com.jimmysoftware.every2;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldConfig;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.Display;

import com.jimmysoftware.ui.ActionScreen;

public class BrowserScreen extends ActionScreen {
	public static final String ON_PORTRAIT_CLOSE = "portrait close";
	public static final String ON_LANDSCAPE_CLOSE = "landscape close";
	public String url;
	
	public BrowserScreen(String url){
		this.url = url;
	}
	
	public void requestBrowserFieldContent(){
		BrowserFieldConfig browserFieldConfig = new BrowserFieldConfig();
		browserFieldConfig.setProperty(BrowserFieldConfig.NAVIGATION_MODE, BrowserFieldConfig.NAVIGATION_MODE_POINTER);
		
		BrowserField browserField = new BrowserField(browserFieldConfig);
		add( browserField );
		browserField.requestContent( url);

		//com.jimmysoftware.globalevent = 0x282ae2cf37276f1L
		ApplicationManager.getApplicationManager().postGlobalEvent(0x282ae2cf37276f1L, 0, 0, "Every", url);
	}
	
	protected void onUiEngineAttached(boolean attached){
		if(attached){
			requestBrowserFieldContent();
		}
	}
	
	public boolean onClose(){
		if(Display.getOrientation()==Display.ORIENTATION_LANDSCAPE){
			fireAction(ON_LANDSCAPE_CLOSE);
			return false;
		}
		else if(Display.getOrientation()==Display.ORIENTATION_PORTRAIT){
			fireAction(ON_PORTRAIT_CLOSE);
			return false;
		}
		
		return super.onClose();
//		fireAction(ON_PORTRAIT_CLOSE);
//		return false;
	}
}
