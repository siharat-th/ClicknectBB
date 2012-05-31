package com.clicknect.bbbuddy;

import java.io.IOException;
import java.io.InputStream;

import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.math.Fixed32;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.decor.Background;
import net.rim.device.api.ui.decor.BackgroundFactory;

import com.jimmysoftware.ui.ActionScreen;

public class TitleScreen extends ActionScreen implements Runnable{
	private int _timerID = -1;
	private Application _application;
	BitmapScalable applicationBitmap, featuredBitmap, contentBitmap;
	private boolean _visible;
	private float fScale[] = {0.5f, 1.0f, 0.5f};
	private float fSpeed = 0.06f;
	int selectedIndex;
	
	public TitleScreen(){
		super(false);
		Bitmap bitmap = Bitmap.getBitmapResource("background.png");
		getMainManager().setBackground(BackgroundFactory.createBitmapBackground(bitmap, Background.POSITION_X_LEFT, Background.POSITION_Y_TOP, Background.REPEAT_SCALE_TO_FIT));
		
		applicationBitmap = new BitmapScalable("app.png");
		applicationBitmap.setPosition(Display.getWidth()/2-100, Display.getHeight()/2);
		
		featuredBitmap = new BitmapScalable("like.png");
		featuredBitmap.setPosition(Display.getWidth()/2, Display.getHeight()/2);
		
		contentBitmap = new BitmapScalable("content.png");
		contentBitmap.setPosition(Display.getWidth()/2+101, Display.getHeight()/2);
		
		_application = Application.getApplication();
		selectedIndex = 1;
	}
	
	public void run() {
		if (_visible) {
			invalidate();
		}
	}
	
	public void sublayout(int width, int height){
		applicationBitmap.setPosition(Display.getWidth()/2-100, Display.getHeight()/2);
		featuredBitmap.setPosition(Display.getWidth()/2, Display.getHeight()/2);
		contentBitmap.setPosition(Display.getWidth()/2+101, Display.getHeight()/2);
		
		super.sublayout(width, height);
	}
	
	public boolean navigationMovement(int dx, int dy, int status, int time){
		if(dx<0) selectedIndex--;
		if(dx>0) selectedIndex++;
		if(selectedIndex<0) selectedIndex = 0;
		if(selectedIndex>2) selectedIndex = 2;
		return super.navigationMovement(dx, dy, status, time);
	}
	
	public void paint(Graphics g){
		super.paint(g);
		applicationBitmap.paint(g, fScale[0]);
		featuredBitmap.paint(g, fScale[1]);
		contentBitmap.paint(g, fScale[2]);
		
		if(selectedIndex==0){
			XYEdges rect = applicationBitmap.getRect();
			int x = rect.right + featuredBitmap.getWidth()/2;
			featuredBitmap.setPosition(x, Display.getHeight()/2);
			
			rect = featuredBitmap.getRect();
			x = rect.right + contentBitmap.getWidth()/2;
			contentBitmap.setPosition(x, Display.getHeight()/2);
		}
		else if(selectedIndex==1){
			XYEdges rect = featuredBitmap.getRect();
			int x = rect.left - applicationBitmap.getWidth()/2;
			applicationBitmap.setPosition(x, Display.getHeight()/2);
			
			x = rect.right + contentBitmap.getWidth()/2;
			contentBitmap.setPosition(x, Display.getHeight()/2);
		}
		else if(selectedIndex==2){
			XYEdges rect = contentBitmap.getRect();
			int x = rect.left - featuredBitmap.getWidth()/2;
			featuredBitmap.setPosition(x, Display.getHeight()/2);
			
			rect = featuredBitmap.getRect();
			x = rect.left - applicationBitmap.getWidth() /2;
			applicationBitmap.setPosition(x, Display.getHeight()/2);
		}
		
		for(int i=0; i<fScale.length; i++){
			if(selectedIndex==i) fScale[i]+=fSpeed;
			else fScale[i]-=fSpeed;
			
			if(fScale[i]<0.5f) fScale[i]=0.5f;
			if(fScale[i]>1.0f) fScale[i]=1.0f;
		}
	}
	
	public static final long MILL_PER_TICK = 1000/16;
	protected void onUiEngineAttached(boolean attached){
		if(attached){
			_visible = true;
			if (_timerID == -1) {
				_timerID = _application.invokeLater(this, MILL_PER_TICK, true);
			}
		}
		else{
			_visible = false;
			if (_timerID != -1) {
				_application.cancelInvokeLater(_timerID);
				_timerID = -1;
			}
		}
	}
	
	class BitmapScalable{
		Bitmap original, tmp;
		int posX, posY;
		
		ImageManipulator imgManipulator;
		
		public BitmapScalable(String filename){
			original = Bitmap.getBitmapResource(filename);
			imgManipulator = new ImageManipulator(original);
		}
		
		public void setPosition(int x, int y){
			posX = x;
			posY = y;
		}
		
		public void paint(Graphics g, double scale){
			imgManipulator.setScale(ImageManipulator.toFP(scale));
			imgManipulator.setBackgroundAlpha(0);
			tmp = imgManipulator.transformAndPaintBitmap();
			int w = tmp.getWidth();
			int h = tmp.getHeight();
			int x = posX - w/2;
			int y = posY - h/2;
			g.drawBitmap(x, y, tmp.getWidth(), tmp.getHeight(), tmp, 0, 0);
		}
		
		public XYEdges getRect(){
			int w = tmp.getWidth();
			int h = tmp.getHeight();
			int left = posX - w/2;
			int top = posY - h/2;
			return new XYEdges(top, left+tmp.getWidth(),top+tmp.getHeight(), left);
		}
		
		public int getWidth(){
			return tmp.getWidth();
		}
		
		public int getHeight(){
			return tmp.getHeight();
		}
	}
}
