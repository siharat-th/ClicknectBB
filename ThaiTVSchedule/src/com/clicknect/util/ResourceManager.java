package com.clicknect.util;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;

public class ResourceManager {
	private static int width;
	private static int height;
	static{
		width = Display.getWidth();
		height = Display.getHeight();
		
		if(width<320) width=320;
		if(height<240) height=240;
		if(width>640) width=640;
		if(height>480) height=480;
		
		int min = Math.min(width, height);
		int max = Math.max(width, height);
		width = max;
		height = min;
	}
	
	public static String getResourcePath(String filename){
		if(width<=320) return "_320_240/"+filename;
		return "_xxx_xxx/"+filename;
	}
	
	public static Bitmap getBitmapResource(String filename){
		String path = ResourceManager.getResourcePath(filename);
		return Bitmap.getBitmapResource(path);
	}
}
