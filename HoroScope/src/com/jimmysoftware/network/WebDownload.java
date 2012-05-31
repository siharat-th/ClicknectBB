package com.jimmysoftware.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.HttpConnection;

import net.rim.device.api.compress.GZIPInputStream;


public class WebDownload 
{
	public static final String USER_AGENT = "NSPlayer/8.0.0.4477";
	public static final int BYTE_SHRINK = 1024;
    
	private HttpConnectionFactory connFactory;
	private DownloadListener listener;
	
	private String url;
	
    static public interface DownloadListener {
    	public void onDownload(final int numBytes); 
    	public void onDownloadComplete(int totalBytes, String url, boolean cancelled);
    }
    
	public WebDownload(String url){
		this.url = url;
		connFactory = new HttpConnectionFactory();
	}
	
	public void setDownloadLsitener(DownloadListener l) {
		listener = l;
	}

    public String doGet (OutputStream os) throws Exception{
    	
    	HttpConnection conn = (HttpConnection)connFactory.getHttpConnection(url);
    	//conn.setRequestProperty("User-Agent", USER_AGENT);
    	InputStream is=null;
		try {
			is = conn.openInputStream();
			int c;
	 	    byte[] buf = new byte[BYTE_SHRINK];
	 	    int numBytes = 0;
			while ((c = is.read(buf)) != -1) {
				numBytes += c;
				os.write(buf, 0, c);

				if (listener != null)
					listener.onDownload(numBytes);
			}

			// send totals
			if (listener != null)
				listener.onDownloadComplete(numBytes, url, false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(is!=null)is.close();
			if(os!=null)os.close();
			if(conn!=null)conn.close();
		}
		return os.toString();
    }
    
    public String doGetGZIP(OutputStream os) throws IOException{
    	
    	HttpConnection conn = (HttpConnection)connFactory.getHttpConnection(url);
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

				if (listener != null)
					listener.onDownload(numBytes);
			}
			
			// send totals
			if (listener != null)
				listener.onDownloadComplete(numBytes, url, false);
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
