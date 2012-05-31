package com.clicknect.tv;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.CoverageInfo;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.decor.Background;
import net.rim.device.api.ui.decor.BackgroundFactory;

import com.clicknect.util.MsgBox;
import com.clicknect.util.ResourceManager;
import com.jimmysoftware.ui.ActionScreen;

public class SplashScreen extends ActionScreen {
	public static final String ACTION_ENTER = "request homescreen";
	private TVApi tvApi = ThaiTVSchedule.tvapi;
	public SplashScreen(){
		super(false);
		String path;
		if(isPortrait())
			path = ResourceManager.getResourcePath("cover_portrait.png");
		else
			path = ResourceManager.getResourcePath("cover.png");
		Bitmap bmpCover = Bitmap.getBitmapResource(path);
		getMainManager().setBackground(BackgroundFactory.createBitmapBackground(bmpCover, Background.POSITION_X_CENTER, Background.POSITION_Y_CENTER, Background.REPEAT_SCALE_TO_FIT));
		
	}
	
	private boolean isPortrait(){
		return Display.getWidth() < Display.getHeight();
	}
	
	public void paint(Graphics g){
		super.paint(g);
		if(ThaiTVSchedule.isPopupDisplay()){
			int oldColor = g.getColor();
			try{
				g.setColor(0);
				g.setGlobalAlpha(64);
				g.fillRect(0, 0, getWidth(), getHeight());
			}
			finally{
				g.setColor(oldColor);
				g.setGlobalAlpha(255);
			}
		}
	}
	
	protected void onUiEngineAttached(boolean attached){
		if(attached){
			Thread t = new FetchThread();
			t.start();
			
			UiApplication.getUiApplication().invokeLater(new Runnable(){

				public void run() {
					if(CoverageInfo.isOutOfCoverage()){
						Dialog.alert("กรุณาเชื่อมต่ออินเทอร์เน็ต เพื่อเข้าใช้งาน");
					}
					else if(SplashScreen.this.isDisplayed() && fetch){
						ThaiTVSchedule.showPopup();
						SplashScreen.this.invalidate();
					}
				}
				
			}, 500, false);
		}
		else{
			ThaiTVSchedule.safeClosePopup();
		}
	}
	
	public static boolean fetch = false;
	class FetchThread extends Thread{
		public void run() {
			fetch = true;
			InputStream is = null;
			try {
				is = tvApi.getServiceListChannel();
				if(is!=null){
					final Vector nodes = tvApi.parseXMLAsVector(is);
					
//					synchronized(UiApplication.getEventLock()){
//						fireAction(ACTION_ENTER, nodes);
//					}
					
					UiApplication.getUiApplication().invokeLater(new Runnable(){

						public void run() {
							fireAction(ACTION_ENTER, nodes);
						}
						
					});
				}
			} 
			catch (Exception e) {
				fetch = false;
				e.printStackTrace();
				//MsgBox.alert("Connection Error! Please check your internet connection.");
			}
			finally{
				fetch = false;
				if(is!=null) try{is.close();}catch(IOException e){e.printStackTrace();}
			}
		}
	}
}
