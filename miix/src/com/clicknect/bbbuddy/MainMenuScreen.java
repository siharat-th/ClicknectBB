package com.clicknect.bbbuddy;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.decor.Background;
import net.rim.device.api.ui.decor.BackgroundFactory;

import com.jimmysoftware.device.api.command.Command;
import com.jimmysoftware.device.api.command.CommandHandler;
import com.jimmysoftware.device.api.command.ReadOnlyCommandMetadata;
import com.jimmysoftware.ui.ActionScreen;
import com.jimmysoftware.ui.BitmapButtonField;

public class MainMenuScreen extends ActionScreen implements Runnable{
	public static final String ACTION_APP_STORE = "open app delivery";
	public static final String ACTION_CONTENT_STORE = "open e-plaza";
	
	Bitmap applicationBitmap, contentBitmap;
	Bitmap applicationBitmapHover, contentBitmapHover;
	BitmapButtonField appButton;
	BitmapButtonField contentButton;
	public MainMenuScreen(){
		super(false);
		Bitmap bitmap = Bitmap.getBitmapResource("background.png");
		getMainManager().setBackground(BackgroundFactory.createBitmapBackground(bitmap, Background.POSITION_X_LEFT, Background.POSITION_Y_TOP, Background.REPEAT_SCALE_TO_FIT));
	
		applicationBitmap 		= Bitmap.getBitmapResource("app.png");
		contentBitmap 			= Bitmap.getBitmapResource("content.png");
		applicationBitmapHover 	= Bitmap.getBitmapResource("app_hover.png");
		contentBitmapHover 		= Bitmap.getBitmapResource("content_hover.png");
		
		appButton = new BitmapButtonField(applicationBitmap, applicationBitmapHover);
		contentButton = new BitmapButtonField(contentBitmap, contentBitmapHover);
		
		appButton.setCommand(new Command(new AppDeliveryCommandHandler()));
		contentButton.setCommand(new Command(new ContentCommandHandler()));
		
		MenuFieldManager hfm = new MenuFieldManager();
		hfm.add(appButton);
		hfm.add(contentButton);
		
		add(hfm);
		
		Thread t = new Thread(this);
		t.start();
	}
	
	int height = (int)(Font.getDefault().getHeight() * 0.7f);
	Font fontDefault = Font.getDefault().derive(Font.PLAIN, height);
	Font fontBold = Font.getDefault().derive(Font.BOLD);
	Font fontBigBold = Font.getDefault().derive(Font.BOLD, (int)(Font.getDefault().getHeight()*1.25f));
	public void paint(Graphics g){
		if(alpha>255)alpha=255;
		g.setGlobalAlpha(alpha);
		super.paint(g);
		
		
		g.setFont(fontBold);
		g.setColor(Color.YELLOW);
		g.drawText("e-Buddy", 
				Display.getWidth()-5-fontBold.getAdvance("e-Buddy"),
				5, 
				DrawStyle.LEFT);
		g.setFont(fontDefault);
		g.setColor(0xbbbbbb);
		g.drawText("Copyright 2011 Click Connect Co., Ltd.", Display.getWidth()-fontDefault.getAdvance("Copyright 2011 Click Connect Co., Ltd.")-5,
				fontBold.getHeight()+5);
		
		g.setFont(fontBigBold);
		g.setColor(0xffffff);
		if(appButton.isFocus()){
			int x = (Display.getWidth()-fontBigBold.getAdvance("Application zone"))/2;
			int y = Display.getHeight()-fontBigBold.getHeight()*2;
			g.drawText("Application zone", x, y);
		}
		else{
			int x = (Display.getWidth()-fontBigBold.getAdvance("Content zone"))/2;
			int y = Display.getHeight()-fontBigBold.getHeight()*2;
			g.drawText("Content zone", x, y);
		}
	}
	
	class AppDeliveryCommandHandler extends CommandHandler{
		public void execute(ReadOnlyCommandMetadata metadata, Object context) {
			fireAction(ACTION_APP_STORE);
		}
	}
	
	class ContentCommandHandler extends CommandHandler{
		public void execute(ReadOnlyCommandMetadata metadata, Object context) {
			fireAction(ACTION_CONTENT_STORE);
		}	
	}
	
	class MenuFieldManager extends Manager{
		
		public MenuFieldManager(){
			super(Field.USE_ALL_WIDTH|Field.USE_ALL_HEIGHT);
		}

		protected void sublayout(int width, int height) {
			width = Math.min(width, getPreferredWidth());
			height = Math.min(height, getPreferredHeight());
			
			int count = this.getFieldCount();
			if(count>=2){
				int w = 128;
				int h = 128;
				int hPadding, vPadding;
				
				if(isLandscape()){
					int horizontalSpace = Display.getWidth() - w*2;
					hPadding = horizontalSpace /4;
					int verticalSpace = Display.getHeight() - h;
					vPadding = verticalSpace/2;
					
					setPositionChild(getField(0), hPadding, vPadding);
					setPositionChild(getField(1), hPadding*3+128, vPadding);
				}
				else{
					int horizontalSpace = Display.getWidth() - w;
					hPadding = horizontalSpace /2;
					int verticalSpace = Display.getHeight() - h*2;
					vPadding = verticalSpace/4;
					
					setPositionChild(getField(0), hPadding, vPadding);
					setPositionChild(getField(1), hPadding, vPadding*3+128);
				}
				
				for(int i=0; i<count; i++){
					layoutChild(getField(i), width, height);
				}
			}
			
			setExtent(width, height);
		}
		
		public boolean isLandscape(){
			return Display.getWidth() > Display.getHeight();
		}
		
		public int getPreferredWidth(){
			return Display.getWidth();
		}
		
		public int getPreferredHeight(){
			return Display.getHeight();
		}
	}

	int alpha = 8;
	public void run() {
		while(alpha<255){
			alpha+=16;
			UiApplication.getUiApplication().invokeLater(new Runnable(){

				public void run() {
					invalidate();
				}
				
			});
			synchronized(this){
				try {
					wait(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
