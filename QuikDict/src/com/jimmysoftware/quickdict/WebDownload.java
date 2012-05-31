package com.jimmysoftware.quickdict;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.ContentConnection;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.InputConnection;

import net.rim.device.api.compress.GZIPInputStream;

import com.jimmysoftware.network.HttpConnectionFactory;
import javax.microedition.io.file.FileConnection;

public class WebDownload 
{
	public static final String USER_AGENT = "NSPlayer/8.0.0.4477";
	public static final int BYTE_SHRINK = 1024;
    
	private HttpConnectionFactory _connFactory;
	private DownloadListener _listener;
	
	private String _url;
	private boolean _fromDeviceStorage;
	
    static public interface DownloadListener {
    	public void onDownload(final int numBytes); 
    	public void onDownloadComplete(int totalBytes, String url, boolean cancelled);
    }
    
	public WebDownload(String url){
		this(url, false);
	}
	
	public WebDownload(String url, boolean fromDeviceStorage){
		_url = url;
		_fromDeviceStorage = fromDeviceStorage;
		if(!fromDeviceStorage)
			_connFactory = new HttpConnectionFactory();
	}
	
	public void setDownloadLsitener(DownloadListener l) {
		_listener = l;
	}

    public String doGet (OutputStream os) throws Exception{
    	
    	InputConnection conn = null;
    	
    	if(_fromDeviceStorage){
    		conn = (FileConnection)Connector.open(_url, Connector.READ_WRITE);
    	}
    	else{
    		conn = (HttpConnection)_connFactory.getHttpConnection(_url);
    	}

    	InputStream is=null;
		try {
			is = conn.openInputStream();
			int c;
	 	    byte[] buf = new byte[BYTE_SHRINK];
	 	    int numBytes = 0;
			while ((c = is.read(buf)) != -1) {
				numBytes += c;
				os.write(buf, 0, c);

				if (_listener != null)
					_listener.onDownload(numBytes);
			}

			// send totals
			if (_listener != null){
				if(conn!=null){
//					if(_fromDeviceStorage){
//						((FileConnection)conn).delete();
//			    	}
					conn.close();
				}
				_listener.onDownloadComplete(numBytes, _url, false);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(is!=null)is.close();
			if(os!=null)os.close();
		}
		return os.toString();
    }
    
    public String doGetGZIP(OutputStream os) throws IOException{
    	
    	InputConnection conn = null;
    	
    	if(_fromDeviceStorage){
    		conn = (FileConnection)Connector.open(_url, Connector.READ_WRITE);
    	}
    	else{
    		conn = (HttpConnection)_connFactory.getHttpConnection(_url);
    	}
    	//conn.setRequestProperty("User-Agent", USER_AGENT);
    	InputStream is = null;
    	GZIPInputStream zis = null;
		try {
			is = conn.openInputStream();
			zis = new GZIPInputStream(is);
			int c;
	 	    byte[] buf = new byte[BYTE_SHRINK*10];
	 	    int numBytes = 0;
			while ((c = zis.read(buf)) != -1) {
				numBytes += c;
				os.write(buf, 0, c);

				if (_listener != null)
					_listener.onDownload(numBytes);
			}
			
			// send totals
			if (_listener != null){
				if(conn!=null){
					if(_fromDeviceStorage){
						((FileConnection)conn).delete();
			    	}
					conn.close();
				}
				_listener.onDownloadComplete(numBytes, _url, false);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	finally{
    		if(zis!=null)zis.close();
    		if(is!=null) is.close();
    		if(os!=null)os.close();
    		if(conn!=null)conn.close();
    	}
		return os.toString();
    }
}
