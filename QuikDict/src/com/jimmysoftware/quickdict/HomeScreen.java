package com.jimmysoftware.quickdict;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.decor.Background;
import net.rim.device.api.ui.decor.BackgroundFactory;

import com.jimmysoftware.ui.ActionScreen;

public class HomeScreen extends ActionScreen {
	public static final String ACTION_ENTER = "on enter";
	public static final String ACTION_CLOSE = "on close";
	
	public HomeScreen(){
		super(false);
		int width = Display.getWidth();
		int height = Display.getHeight();
		
		if(width<320) width=320;
		if(height<240) height=240;
		if(width>640) width=640;
		if(height>480) height=480;
		
		String filename = "title_" + width + "_" + height + ".png";
		String defaultFilename = "title_480_360.png";
		
		Bitmap background = Bitmap.getBitmapResource(filename);
		if(background==null)
			background = Bitmap.getBitmapResource(defaultFilename);
		getMainManager().setBackground(BackgroundFactory.createBitmapBackground(
				background,
				Background.POSITION_X_CENTER,
				Background.POSITION_Y_TOP,
				Background.REPEAT_NONE)
				);
	}
	
//	public boolean navigationClick(int status, int time){
//		fireAction(ACTION_ENTER);
//		return true;
//	}
}
