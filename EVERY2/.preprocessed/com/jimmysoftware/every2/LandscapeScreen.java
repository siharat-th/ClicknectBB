
package com.jimmysoftware.every2;







import com.jimmysoftware.device.api.command.*;


import java.util.Vector;
import net.rim.device.api.i18n.DateFormat;
import net.rim.device.api.i18n.Locale;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.io.http.HttpDateParser;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.TouchEvent;
import net.rim.device.api.ui.TouchGesture;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.DateField;
import com.jimmysoftware.ui.ActionScreen;
import com.jimmysoftware.ui.BitmapButtonField;
import com.jimmysoftware.ui.UiFactory;

public class LandscapeScreen extends ActionScreen {
	public static final String ACTION_CHANGE_VIEW = "portrait view";
	public static final String ACTION_ENTER = "landscape webview";
	public static final String ACTION_CLOSE = "close landscape";
	
	private Vector landscapeObjects = null;
	private float deltaTime = 0.0f;
	public  float globalAlpha = 0.0f;
	private float bitmapCurrentScale;
	private RepaintThread repaintThread = null;
	private int textPositionMinX;
	private float textPositionX = 10.0f;
	private float textSpeed;
	private float timeSinceStart = 0.0f;
	private int currentObjectIndex = 0;
	private Bitmap currentBitmap = null;
	private Bitmap currentScaleBitmap = null;
	private String currentHeader;
	private String currentDate;
	private String currentURL;
	private Font headerFont = Configuration.LARGE_FONT;
	private Font dateFont = Configuration.MEDIUM_FONT;
	private Bitmap bgHeaderBitmap = UiFactory.bgHeaderBitmap;
	private Bitmap bgGradientBitmap = UiFactory.bgGradientBitmap;
	private Bitmap moreButtonBitmap = UiFactory.moreButtonBitmap;
	
	private BitmapButtonField moreButton, leftButton, rightButton;
	
	public LandscapeScreen(Vector landscapeObjects){
		int sw = Math.max(Display.getWidth(), Display.getHeight());
		int sh = Math.min(Display.getWidth(), Display.getHeight());
		moreButton = new BitmapButtonField(
				UiFactory.moreButtonBitmap,
				UiFactory.moreHoverButtonBitmap,
				sw-moreButtonBitmap.getWidth(), 
				sh-moreButtonBitmap.getHeight()-10);
		moreButton.setCommand(new Command(new MoreButtonCommandHandler()));
		
		leftButton = new BitmapButtonField(
				UiFactory.leftButtonBitmap,
				UiFactory.leftHoverButtonBitmap,
				0, 
				(sh-UiFactory.leftButtonBitmap.getHeight())/2);
		leftButton.setCommand(new Command(new LeftButtonCommandHandler()));
		
		rightButton = new BitmapButtonField(
				UiFactory.rightButtonBitmap,
				UiFactory.rightHoverButtonBitmap,
				sw-UiFactory.rightButtonBitmap.getWidth(), 
				(sh-UiFactory.rightButtonBitmap.getHeight())/2);
		rightButton.setCommand(new Command(new RightButtonCommandHandler()));
		
		add(leftButton);
		add(rightButton);
		add(moreButton);
		
		setContent(landscapeObjects);
	}
	
	public void stopThread(){
		if(repaintThread!=null){
			repaintThread.stopPainting();
		}
	}
	
	protected void onObscured(){
		stopThread();
		UiApplication.getUiApplication().popScreen(this);
	}
	
//	protected void onExposed(){
//		stopThread();
//		repaintThread = new RepaintThread();
//		repaintThread.startPainting();
//	}
	
	public void setContent(Vector landscapeObjects){
		this.landscapeObjects = landscapeObjects;
		
		if(landscapeObjects!=null && landscapeObjects.size()>0){
			LandscapeObject obj = (LandscapeObject)landscapeObjects.elementAt(0);
			if(obj!=null){
				currentBitmap = obj.bitmap;
				currentHeader = obj.header;
				currentDate = obj.date;
				currentURL = obj.url;
			}
		}
		textPositionX = 10.0f;
		textPositionMinX = 0 - headerFont.getAdvance(currentHeader, 0, currentHeader.length());
		textSpeed = headerFont.getAdvance(currentHeader, 0, currentHeader.length()) / 5.0f;
		timeSinceStart = 0.0f;
		globalAlpha = 0.0f;
		findBitmapRatio();
		
		stopThread();
		repaintThread = new RepaintThread();
		repaintThread.startPainting();
	}
	
	private void findBitmapRatio(){
		if(currentBitmap!=null){
			float sx = (float)Display.getWidth() / currentBitmap.getWidth();
			float sy = (float)Display.getHeight() / currentBitmap.getHeight();
			bitmapCurrentScale = sx>sy? sx: sy;
			
			int nsx = (int)(currentBitmap.getWidth()* bitmapCurrentScale);
			int nsy = (int)(currentBitmap.getHeight()* bitmapCurrentScale);
			currentScaleBitmap = new Bitmap(nsx, nsy);
			currentBitmap.scaleInto(currentScaleBitmap, Bitmap.FILTER_BILINEAR);
		}
	}

	
	protected void paint(Graphics g){
		if(landscapeObjects==null || landscapeObjects.size()<=0) return;
		if(!UiApplication.getUiApplication().isForeground()) return;
		//System.out.println(">>>>>>>>>>LandscapeScree.paint()");
		
		g.setGlobalAlpha((int)globalAlpha);
		
		g.setColor(0);
		g.fillRect(0, 0, Display.getWidth(), Display.getHeight());
		if(currentScaleBitmap!=null){
			int w = currentScaleBitmap.getWidth();
			int h = currentScaleBitmap.getHeight();
			int cx = (Display.getWidth()-w)/2;
			int cy = (Display.getHeight()-h)/2;
			g.drawBitmap(cx, cy, w, h, currentScaleBitmap, 0, 0);
		}
		//g.setGlobalAlpha((int)globalAlpha);
		//g.drawBitmap(0, 0, Display.getWidth(), Display.getHeight(), bgGradientBitmap, 0, 0);
		g.drawBitmap(0, Display.getHeight()-bgHeaderBitmap.getHeight()-10, Display.getWidth(), bgHeaderBitmap.getHeight(), bgHeaderBitmap, 0, 0);
		//g.drawBitmap(Display.getWidth()-moreButtonBitmap.getWidth(), Display.getHeight()-moreButtonBitmap.getHeight()-10,
		//		moreButtonBitmap.getWidth(), moreButtonBitmap.getHeight(), moreButtonBitmap, 0, 0);
		
		int hx = (int)textPositionX;
		int hy = Display.getHeight()-bgHeaderBitmap.getHeight();
		g.setFont(headerFont);
		g.setColor(0x00ffffff);
		g.drawText(currentHeader, hx, hy);
		g.setFont(dateFont);
		g.setColor(0xAAAAAA);
		g.drawText(currentDate, 10, Display.getHeight()-20-dateFont.getHeight());
		
		
		leftButton.paint(g);
		rightButton.paint(g);
		moreButton.paint(g);
	}
	
	
	protected void sublayout(int width, int height){
		if(Display.getOrientation()==Display.ORIENTATION_PORTRAIT){
			fireAction(ACTION_CHANGE_VIEW);
		}
		super.sublayout(width, height);
	}
	
	
	public boolean onClose(){
		//repaintThread.stopPainting();
		fireAction(ACTION_CLOSE);
		return false;
	}
	
//	protected boolean navigationMovement(int dx, int dy, int status, int time){
//		if(status==KeypadListener.STATUS_TRACKWHEEL){
//			if(dx<0){
//				rightButton.unSelected();
//				moreButton.unSelected();
//				leftButton.executeCommand();
//				return true;
//			}
//			else if(dx>0){
//				leftButton.unSelected();
//				moreButton.unSelected();
//				rightButton.executeCommand();
//				return true;
//			}
//		}
//		return super.navigationMovement(dx, dy, status, time);
//	}
	
	protected boolean touchEvent(TouchEvent message)
    {
        // Retrieve the new x and y touch positions.  
		int eventCode = message.getEvent();
		if(eventCode==TouchEvent.GESTURE){
			TouchGesture gesture = message.getGesture();
			int dir = gesture.getSwipeDirection();
			if(dir==TouchGesture.SWIPE_EAST){
				boolean b = rightButton.executeCommand();
				if(b){
					leftButton.unSelected();
					moreButton.unSelected();
				}
				return true;
			}
			else if(dir==TouchGesture.SWIPE_WEST){
				boolean b = leftButton.executeCommand();
				if(b){
					rightButton.unSelected();
					moreButton.unSelected();
				}
				return true;
			}
		}
		
        leftButton.touchEvent(message);
        rightButton.touchEvent(message);
        moreButton.touchEvent(message);
        return true;
    }
	
	/*
	public boolean isPointInRect(int px, int py, int rx, int ry, int rw, int rh){
		if(px<rx) return false;
		if(px>rx+rw) return false;
		if(py<ry) return false;
		if(py>ry+rh) return false;
		return true;
	}
	*/
	
	private class RepaintThread extends Thread{
		private boolean runThread = false;
		
		public void startPainting(){
			runThread = true;
			start();
		}
		
		public void stopPainting(){
			runThread = false;
		}
		
		public void run(){
			while(runThread){
				if(UiApplication.getUiApplication().getActiveScreen()==LandscapeScreen.this){
					System.out.println(">>>>>>>>>>RepainThread.run()in whileloop"+this);
					if(UiApplication.getUiApplication().isForeground() && isDisplayed()){
						System.out.println(">>>>>>>>>>RepainThread.run()in ifForeground"+this);
						
						long startTime = System.currentTimeMillis();
						invalidate();
						long finishTime = System.currentTimeMillis();
						long elapseTime = finishTime - startTime;
						//if(bitmapCurrentScale<bitmapMaxScale){
						//	int sx = (int)(currentBitmap.getWidth()* bitmapCurrentScale);
						//	int sy = (int)(currentBitmap.getHeight()* bitmapCurrentScale);
						//	int w = currentScaleBitmap.getWidth();
						//	int h = currentScaleBitmap.getHeight();
						//	if(sx>w && sy>h){
						//		currentScaleBitmap = null;
						//		currentScaleBitmap = new Bitmap(sx, sy);
						//		currentBitmap.scaleInto(currentScaleBitmap, Bitmap.FILTER_BILINEAR);
						//	}
						//}
						if(elapseTime<Configuration.MILLS_PER_TICK){
							try {
								sleep(Configuration.MILLS_PER_TICK-elapseTime);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						deltaTime = (System.currentTimeMillis() - startTime) / 1000.0f;
						timeSinceStart+=deltaTime;
						globalAlpha+=255*deltaTime*2;
						if(globalAlpha>255.0f)
							globalAlpha = 255.0f;
						
						// update data
						//bitmapCurrentScale+=Configuration.BITMAP_MAX_ZOOM_SPEED * deltaTime;
						if(textPositionMinX<-Display.getWidth())
						textPositionX-= textSpeed * deltaTime;
						
						if(timeSinceStart>=5.0f){
							currentObjectIndex++;
							if(currentObjectIndex>=landscapeObjects.size())
								currentObjectIndex = 0;
							LandscapeObject obj = (LandscapeObject)landscapeObjects.elementAt(currentObjectIndex);
							if(obj!=null){
								currentBitmap = obj.bitmap;
								currentHeader = obj.header;
								currentDate = obj.date;
								currentURL = obj.url;
							}
							textPositionX = 10.0f;
							textPositionMinX = 0 - headerFont.getAdvance(currentHeader, 0, currentHeader.length());
							textSpeed = headerFont.getAdvance(currentHeader, 0, currentHeader.length()) / 5.0f;
							//currentText = obj.text;
							//textPositionX = Configuration.DEVICE_PORTRAIT_HEIGHT;
							//textPositionMinX = 0 - textRunningFont.getAdvance(currentText, 0, currentText.length());
							findBitmapRatio();
							timeSinceStart = 0.0f;
						}
					}
				} // end if
			} //end while
		} // end run methode
	} // end class
	
	private class MoreButtonCommandHandler extends CommandHandler{
	     public void execute(ReadOnlyCommandMetadata metadata, Object context)
	     {
	    	 fireAction(ACTION_ENTER, currentURL);
	     }        
	}
	
	private class LeftButtonCommandHandler extends CommandHandler{
	     public void execute(ReadOnlyCommandMetadata metadata, Object context)
	     {
	    	 currentObjectIndex--;
				if(currentObjectIndex<0)
					currentObjectIndex = landscapeObjects.size()-1;
				LandscapeObject obj = (LandscapeObject)landscapeObjects.elementAt(currentObjectIndex);
				currentBitmap = obj.bitmap;
				currentHeader = obj.header;
				currentDate = obj.date;
				currentURL = obj.url;
				textPositionX = 10.0f;
				textPositionMinX = 0 - headerFont.getAdvance(currentHeader, 0, currentHeader.length());
				textSpeed = headerFont.getAdvance(currentHeader, 0, currentHeader.length()) / 5.0f;
				//currentText = obj.text;
				//textPositionX = Configuration.DEVICE_PORTRAIT_HEIGHT;
				//textPositionMinX = 0 - textRunningFont.getAdvance(currentText, 0, currentText.length());
				findBitmapRatio();
				timeSinceStart = 0.0f;
	     }        
	}
	
	private class RightButtonCommandHandler extends CommandHandler{
	     public void execute(ReadOnlyCommandMetadata metadata, Object context)
	     {
	    	 	currentObjectIndex++;
				if(currentObjectIndex>=landscapeObjects.size())
					currentObjectIndex = 0;
				LandscapeObject obj = (LandscapeObject)landscapeObjects.elementAt(currentObjectIndex);
				currentBitmap = obj.bitmap;
				currentHeader = obj.header;
				currentDate = obj.date;
				currentURL = obj.url;
				textPositionX = 10.0f;
				textPositionMinX = 0 - headerFont.getAdvance(currentHeader, 0, currentHeader.length());
				textSpeed = headerFont.getAdvance(currentHeader, 0, currentHeader.length()) / 5.0f;
				//currentText = obj.text;
				//textPositionX = Configuration.DEVICE_PORTRAIT_HEIGHT;
				//textPositionMinX = 0 - textRunningFont.getAdvance(currentText, 0, currentText.length());
				findBitmapRatio();
				timeSinceStart = 0.0f;
	     }        
	}
}
