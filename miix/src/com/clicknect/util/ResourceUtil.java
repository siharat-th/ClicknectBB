package com.clicknect.util;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.Sensor;

public class ResourceUtil {
	public static final int TYPE_MINI = 0; //320x240
	public static final int TYPE_STANDARD = 1; //480x360
	public static final int TYPE_STANDARD_ORIENTABLE = 2; // 360x480 & 480x360
	public static final int TYPE_STANDARD_PORTRAIT = 3; // 360x400 (Pearl)
	public static final int TYPE_HD = 4; //640x480
	public static int getDeviceType(){
		int w = Display.getWidth();
		int h = Display.getHeight();
		if(w==320&&h==240) return TYPE_MINI;
		if((w==480&&h==360)||(w==360&&h==480)){
			if(Sensor.isSupported(Sensor.FLIP)) return TYPE_STANDARD_ORIENTABLE;
			else return TYPE_STANDARD;
		}
		if(w==640&&h==480) return TYPE_HD;
		
		if(w<=320) return TYPE_MINI;
		if(w<=360) return TYPE_STANDARD_PORTRAIT;
		if(w<=480) return TYPE_STANDARD;
		return TYPE_HD;
	}
	
	public static String getResFolder(){
		int type = getDeviceType();
		if(type==TYPE_MINI) return "320x240/";
		if(type==TYPE_STANDARD) return "480x360/";
		if(type==TYPE_STANDARD_ORIENTABLE) return "480x360/";
		if(type==TYPE_STANDARD_PORTRAIT) return "480x360/";
		return "640x480/";
	}
	
	public static String getResSuffix(){
		int type = getDeviceType();
		if(type==TYPE_MINI) return "_320_240.png";
		if(type==TYPE_STANDARD) return "_480_360.png";
		if(type==TYPE_STANDARD_ORIENTABLE) return "_480_360.png";
		if(type==TYPE_STANDARD_PORTRAIT) return "_480_360.png";
		return "_640_480.png";
	}
	
	public static String getResSuffixWidth(){
		int type = getDeviceType();
		if(type==TYPE_MINI) return "_320_";
		if(type==TYPE_STANDARD) return "_480_";
		if(type==TYPE_STANDARD_ORIENTABLE) return "_480_";
		if(type==TYPE_STANDARD_PORTRAIT) return "_480_";
		return "_640_";
	}
	
	public static Bitmap loadBitmap(String filename){ // filename without ext
		String path = getResFolder() + filename + getResSuffix();
		return Bitmap.getBitmapResource(path);
	}
}
