package com.clicknect.bbbuddy;
import java.io.IOException;
import java.io.InputStream;

import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.system.CodeModuleManager;

public class Util {
	public static final int _64K = 65535;
	private static Util instance = null;
	private Util(){}
	
	public static Util getInstance(){
		if(instance==null){
			instance = new Util();
		}
		return instance;
	}
	
	public boolean installCodeModule(String bundleFilename, int numSiblings){ // do not append .cod to bundleFilename 
		for(int i=0; i<numSiblings; i++){
			String filename = i==0 ? bundleFilename+".cod" : bundleFilename + "-" + i + ".cod";
			byte[] data = getResourceAsByteArray(filename);
			int length = data.length;
			if(length>_64K){
				int handle = CodeModuleManager.createNewModule(length, data, _64K);
				CodeModuleManager.writeNewModule(handle, _64K, data, _64K, length-_64K);
				int ret = CodeModuleManager.saveNewModule(handle);
				if(ret!=CodeModuleManager.CMM_OK)
				return false;
			}
			else{
				int handle = CodeModuleManager.createNewModule(length, data, length);
				int ret = CodeModuleManager.saveNewModule(handle);
				if(ret!=CodeModuleManager.CMM_OK)
				return false;
			}
		}
		return true;
	}
	
	public boolean isModuleInstalled(String moduleName){
		int handle = CodeModuleManager.getModuleHandle(moduleName);
		return handle!=0;
	}
	
	public byte[] getResourceAsByteArray(String resFilename){
		try {
			InputStream is = getClass().getResourceAsStream(resFilename);
			byte data[] = IOUtilities.streamToBytes(is);
			is.close();
			return data;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
