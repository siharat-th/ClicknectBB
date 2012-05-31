package com.clicknect.tv;

import java.util.Hashtable;

import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;

public class FavoritePersistent {
	//"com.clicknect.tv" = 0xb77f67dde09f77a8L
	public static long DATA_UID = 0xb77f67dde09f77a8L;
	private static PersistentObject persistentObject = PersistentStore.getPersistentObject( DATA_UID );
	private static Hashtable data;
	static{
		data = (Hashtable) persistentObject.getContents(); 
		if(data==null){
			data = new Hashtable();
		}
	}
	
	public static boolean hasFavorite(String key){
		return data.containsKey(key);
	}
	
	public static void addFavorite(String key){
		data.put(key, key);
		persistentObject.setContents(data);
	}
	
	public static void removeFavorite(String key){
		data.remove(key);
		persistentObject.setContents(data);
	}
	
	public static Hashtable getData(){
		return data;
	}
}
