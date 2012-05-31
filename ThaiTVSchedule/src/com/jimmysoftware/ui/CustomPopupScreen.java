package com.jimmysoftware.ui;

import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.decor.BackgroundFactory;
import net.rim.device.api.ui.decor.Border;
import net.rim.device.api.ui.decor.BorderFactory;

public class CustomPopupScreen extends PopupScreen {
	private int BORDER_COLOR = 0xffffff;
	private int BACKGROUND_COLOR = 0x404040;
	private int BACKGROUND_ALPHA = 255;
	private int STROKE_COLOR = 0;
	private int STROKE_ALPHA = 64;
	
	public CustomPopupScreen(Manager manager){
		super(manager);
		setBackground(BackgroundFactory.createSolidTransparentBackground(0x424441, 0));
		setBorder(BorderFactory.createRoundedBorder(new XYEdges(0,0,0,0), 0, Border.STYLE_TRANSPARENT));
		//setPadding(10, 3, 3, 3);
	}
	
	public void setColor(int borderColor, int backgroundColor, int backgroundAlpha, int strokeColor, int strokeAlpha){
		setBorderColor(borderColor);
		setBackgroundColor(backgroundColor, backgroundAlpha);
		setStrokeColor(strokeColor, strokeAlpha);
	}
	
	public void setBorderColor(int borderColor){
		BORDER_COLOR = borderColor;
	}
	
	public void setBackgroundColor(int backgroundColor, int backgroundAlpha){
		BACKGROUND_COLOR = backgroundColor;
		BACKGROUND_ALPHA = backgroundAlpha;
	}
	
	public void setStrokeColor(int strokeColor, int strokeAlpha){
		STROKE_COLOR = strokeColor;
		STROKE_ALPHA = strokeAlpha;
	}
	
	public void paintBackground(Graphics g){
		super.paintBackground(g);
		int oldColor = g.getColor();
		int w = getWidth();
		int h = getHeight();
		
		g.setColor(STROKE_COLOR);
		g.setGlobalAlpha(STROKE_ALPHA);
		g.fillRoundRect(0, 0, w, h, 10, 10);
		
		g.setColor(BORDER_COLOR);
		g.setGlobalAlpha(255);
		g.fillRoundRect(1, 1, w-2, h-2, 10, 10);
		
		g.setColor(STROKE_COLOR);
		g.setGlobalAlpha(STROKE_ALPHA);
		g.fillRoundRect(2, 2, w-4, h-4, 10, 10);
		
		g.setColor(BACKGROUND_COLOR);
		g.setGlobalAlpha(BACKGROUND_ALPHA);
		g.fillRoundRect(3, 3, w-6, h-6, 10, 10);
		
		g.setColor(oldColor);
		g.setGlobalAlpha(255);
	}
}
