//#preprocess
package com.jimmysoftware.ui;

//#ifdef BlackBerrySDK6.0.0
import net.rim.device.api.ui.component.StandardTitleBar;
import net.rim.device.api.ui.component.TitleBar;
//#endif
import net.rim.device.api.ui.decor.BackgroundFactory;
import net.rim.device.api.ui.Color;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;

public class UiFactory {
	public static String APPLICATION_NAME;
	
	public static Bitmap bgHeaderBitmap = EncodedImage.getEncodedImageResource("bg_header_landscape.png").getBitmap();
	public static Bitmap bgGradientBitmap = EncodedImage.getEncodedImageResource("lanscape_gradient.png").getBitmap();
	public static Bitmap moreButtonBitmap = EncodedImage.getEncodedImageResource("more_button.png").getBitmap();
	public static Bitmap moreHoverButtonBitmap = EncodedImage.getEncodedImageResource("more_button_hover.png").getBitmap();
	public static Bitmap leftButtonBitmap = EncodedImage.getEncodedImageResource("l_button.png").getBitmap();
	public static Bitmap leftHoverButtonBitmap = EncodedImage.getEncodedImageResource("l_button_hover.png").getBitmap();
	public static Bitmap rightButtonBitmap = EncodedImage.getEncodedImageResource("r_button.png").getBitmap();
	public static Bitmap rightHoverButtonBitmap = EncodedImage.getEncodedImageResource("r_button_hover.png").getBitmap();
	
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
		return 0xb5aead;
	}
}
