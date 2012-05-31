//#preprocess
package com.jimmysoftware.ui;

//#ifdef BlackBerrySDK6.0.0
import net.rim.device.api.command.Command;
//#endif

//#ifdef BlackBerrySDK5.0.0
import com.jimmysoftware.device.api.command.Command;
//#endif

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.TouchEvent;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.decor.BackgroundFactory;

public class BitmapButtonField extends BitmapField {
	Bitmap hover;
	Bitmap old;
	private Command command;
	private int x, y, w, h;

	public BitmapButtonField(Bitmap source, Bitmap hover) {
		this(source, hover, Field.FOCUSABLE);
	}
	
	public BitmapButtonField(Bitmap source, Bitmap hover, long style) {
		super(source, style);
		//super(source);
		this.old = source;
		this.hover = hover;
		this.w = source.getWidth();
		this.h = source.getHeight();
		//setBackground(BackgroundFactory.createSolidTransparentBackground(Color.WHITE, 0));
		//setPosition(x, y);
	}
	
	public BitmapButtonField(Bitmap source, Bitmap hover, int x, int y) {
		super(source, Field.FOCUSABLE);
		this.old = source;
		this.hover = hover;
		this.w = source.getWidth();
		this.h = source.getHeight();
		this.x = x;
		this.y = y;
	}
	
	public int getPreferredWidth(){
		return w;
	}
	
	public int getPreferredHeight(){
		return h;
	}

	protected void onFocus(int direction) {
		setBitmap(hover);
		super.onFocus(direction);
	}
	
	public void unSelected(){
		onUnfocus();
	}

	protected void onUnfocus() {
		setBitmap(old);
		super.onUnfocus();
	}

	public void setCommand(Command command) {
		this.command = command;
	}
	
	public void drawFocus(Graphics g, boolean on){
	}
	
	public void paint(Graphics g){
		g.drawBitmap(x, y, w, h, getBitmap(), 0, 0);
	}
	
	public boolean executeCommand(){
		if(command==null) return false;
		if (command.canExecute(this)) {
			setBitmap(hover);
			command.execute(this);
			return true;
		}
		return false;
	}
	
	public boolean touchEvent(TouchEvent message){
		int x = message.getX(1);
        int y = message.getY(1);        
                                          
        int eventCode = message.getEvent();   
        if(eventCode==TouchEvent.UP){
        	if(isPointIn(x, y)){
        		if(command==null) return false;
        		if (command.canExecute(this)) {
        			command.execute(this);
        			return true;
        		}
        	}
        }
        else if(eventCode==TouchEvent.DOWN || eventCode==TouchEvent.MOVE){
        	if(isPointIn(x, y)){
        		setBitmap(hover);
        		return true;
        	}
        }
        else{
        	setBitmap(old);
        	return true;
        }
        
        
		return false;
	}
	
	private boolean isPointIn(int x, int y){
		if(x<this.x) return false;
		if(x>this.x+w) return false;
		if(y<this.y) return false;
		if(y>this.y+h) return false;
		return true;
	}

	
	protected boolean navigationClick(int status, int time) {
		if(command==null) return false;
		if (command.canExecute(this)) {
			command.execute(this);
			return true;
		}
		return false;
	}
	
}

