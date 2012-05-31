package com.clicknect.webapi;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.HttpConnection;

import com.jimmysoftware.network.HttpConnectionFactory;

import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Status;

public class Activate {
	static final String UID = Integer.toHexString(DeviceInfo.getDeviceId());
	static final String SERVICE_URL = "http://bbcenter.clicknect.com/services/activate/?";
	static final String DEFAULT_PACKAGE = "com.clicknect.bbbuddy.$";
	String packageName;
	String serviceURL;
	public Activate(){
		ApplicationDescriptor appDesc = ApplicationDescriptor.currentApplicationDescriptor();
		//appDesc.getLocalizedName() ThaiNews
		//appDesc.getModuleName() ThaiNews
		packageName = DEFAULT_PACKAGE + appDesc.getLocalizedName();
		serviceURL = SERVICE_URL + "c=" + UID +";" + packageName;
	}
	
	public void request(){
		ActivateThread thread = new ActivateThread();
		thread.start();
	}
	
	public void showStatus(final String msg){
		UiApplication.getUiApplication().invokeLater(new Runnable(){

			public void run() {
				Status.show(msg);
			}
			
		});
	}
	
	class ActivateThread extends Thread{
		public void run(){
			HttpConnectionFactory connFact = new HttpConnectionFactory();
			HttpConnection connection = null;
			InputStream inputStream = null;
			
			try {
				connection = connFact.getHttpConnection(serviceURL);
				if(DeviceInfo.isSimulator()){
					if(connection!=null){
						int responseCode = connection.getResponseCode();
						if(responseCode==200){
							inputStream = connection.openInputStream();
							byte[] response = IOUtilities.streamToBytes(inputStream);
							String msg = new String(response);
							showStatus(serviceURL+" : "+msg);
						}
					}
				}
			} 
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				showStatus(e.toString());
			}
			finally{
				if(inputStream!=null) try{inputStream.close();}catch(IOException e){e.printStackTrace();}
				if(connection!=null) try{connection.close();}catch(IOException e){e.printStackTrace();}
			}
		}
	}
}
