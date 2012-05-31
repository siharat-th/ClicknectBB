//#preprocess
package com.jimmysoftware.ui;

//#ifdef BlackBerrySDK6.0.0
import net.rim.device.api.ui.component.StandardTitleBar;
import net.rim.device.api.ui.component.TitleBar;
//#endif
import net.rim.device.api.ui.decor.BackgroundFactory;
import net.rim.device.api.ui.Color;

public class UiFactory {
	public static String APPLICATION_NAME;
	
	public static void setApplicationName(final String appName){
		APPLICATION_NAME = appName;
	}
	
	//#ifdef BlackBerrySDK6.0.0
	public static TitleBar createTitleBar(){
		StandardTitleBar titleBar = new StandardTitleBar();
		titleBar.addTitle(APPLICATION_NAME);
		titleBar.addNotifications();
		titleBar.addSignalIndicator();
		return titleBar;
	}
	
	public static TitleBar createTransparentTitleBar(){
		StandardTitleBar titleBar = new StandardTitleBar();
		titleBar.addTitle(APPLICATION_NAME);
		titleBar.addNotifications();
		titleBar.addSignalIndicator();
		titleBar.setBackground(BackgroundFactory.createSolidTransparentBackground(0, 0));
		return titleBar;
	}
	//#endif
	
	public static int getDefaultBackgroundColor(){
		return Color.WHITE;
	}
}
