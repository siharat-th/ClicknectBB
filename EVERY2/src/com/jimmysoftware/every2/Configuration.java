package com.jimmysoftware.every2;

import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Font;

public class Configuration {
	// Device
	public static final int DEVICE_PORTRAIT_WIDTH = getDevicePortraitWidth();
	public static final int DEVICE_PORTRAIT_HEIGHT = getDevicePortraitWidth();
	public static final int DEVICE_UDID = DeviceInfo.getDeviceId();
	public static final String DEVICE_PIN = Integer.toHexString(DEVICE_UDID);
	
	// HeadNews
	public static int HEAD_NEWS_THUMBNAIL_WIDTH = 76;
	public static int HEAD_NEWS_THUMBNAIL_HEIGHT = 76;
	public static int HEAD_NEWS_LIST_HEIGHT = 90;
	
	// Font
	public static final Font LARGE_FONT = Font.getDefault().derive(Font.BOLD|Font.ANTIALIAS_STANDARD, 20);
	public static final Font BIG_FONT = Font.getDefault().derive(Font.BOLD|Font.ANTIALIAS_STANDARD, 16);
	public static final Font MEDIUM_FONT = Font.getDefault().derive(Font.ANTIALIAS_STANDARD, 12);
	public static final Font SMALL_FONT = Font.getDefault().derive(Font.ANTIALIAS_STANDARD, 10);
	
	// API
	public static final String FEED_API_URL = "http://news.clicknect.com/api/list_posts.php";//
	public static final String FEED_API_PARAM = "disp=xml&cid=54&uid=1&limit=10&page=1&im="+DEVICE_PIN;//disp=xml&limit=10";
	
	public static final long  MILLS_PER_TICK = 1000/20; // 20 fps.
	
	// Utility Method
	public static int getDevicePortraitWidth(){
		int orient = Display.getOrientation();
		if(orient==Display.ORIENTATION_LANDSCAPE){
			return Display.getHeight();
		}
		return Display.getWidth();
	}
	
	public static int getDevicePortraitHeight(){
		int orient = Display.getOrientation();
		if(orient==Display.ORIENTATION_LANDSCAPE){
			return Display.getWidth();
		}
		return Display.getHeight();
	}
}
