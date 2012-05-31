package com.jimmysoftware.ui;
import com.jimmysoftware.device.api.command.Command;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.BitmapField;

public class BitmapButtonField extends BitmapField {
	protected Bitmap hover;
	protected Bitmap old;
	private Command command;
	private int w, h;

	public BitmapButtonField(Bitmap source, Bitmap hover) {
		this(source, hover, Field.FOCUSABLE);
	}
	
	public BitmapButtonField(Bitmap source, Bitmap hover, long style) {
		super(source, style);
		this.old = source;
		this.hover = hover;
		this.w = source.getWidth();
		this.h = source.getHeight();
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
	
	public MenuItem createMenuItem(String text, int ordinal, int priority){
		MenuItem menuItem = new MenuItem(text, ordinal, priority){

			public void run() {
				executeCommand();
			}
			
		};
		return menuItem;
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
	
	protected boolean navigationClick(int status, int time) {
		if(command==null) return false;
		if (command.canExecute(this)) {
			command.execute(this);
			return true;
		}
		return false;
	}
	
}

