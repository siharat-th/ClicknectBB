package com.clicknect.bbbuddy;

import net.rim.device.api.system.Bitmap;

public class ListInfo{
	public String name, url, s_desc, desc, size, version, vendor, updateversion, imgUrl;
	public Bitmap icon, img;
	public boolean isInstalled;
	//public boolean hasUpdate;
	public int handle, moduleCodeSize;
	public String moduleName;
	
	public ListInfo(){
		version = updateversion = "1.0.0";
	}
	
	public boolean hasUpdate(){
		return updateversion.compareTo(version) > 0;
	}
	
	public boolean runWhenClick(){
		return isInstalled&&!hasUpdate();
	}
}
