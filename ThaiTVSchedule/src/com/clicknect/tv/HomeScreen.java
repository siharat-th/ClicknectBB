package com.clicknect.tv;

import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.FlowFieldManager;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.Background;
import net.rim.device.api.ui.decor.BackgroundFactory;

import com.clicknect.tv.TVApi.XMLNode;
import com.clicknect.tv.TVApi.ChannelData;
import com.clicknect.tv.TVApi.ProgramData;
import com.clicknect.util.MsgBox;
import com.clicknect.util.ResourceManager;
import com.jimmysoftware.ui.Action;
import com.jimmysoftware.ui.ActionScreen;
import com.jimmysoftware.ui.BitmapButtonField;
import com.jimmysoftware.ui.PaneTabManager;

public class HomeScreen extends ActionScreen implements FieldChangeListener {
	public static final String TAB_CHANNEL = "tab channel";
	public static final String TAB_NOW_SHOWING = "tab now showing";
	public static final String TAB_FAVORITE = "tab favorite";
	public static final String TAB_SEARCH = "tab search";
	
	private static final String ACTION_TAB[] = {TAB_CHANNEL, TAB_NOW_SHOWING, TAB_FAVORITE, TAB_SEARCH};
	
	private PaneTabManager pane;
	private Bitmap headers[];
	private Vector xmlNodes;
	
	
	
	public HomeScreen(Vector nodes){
		super(false, Manager.NO_VERTICAL_SCROLL|Manager.USE_ALL_HEIGHT|Manager.USE_ALL_HEIGHT);
		xmlNodes = nodes;
		
		Bitmap bg = ResourceManager.getBitmapResource("bg.png");
		Background background = BackgroundFactory.createBitmapBackground(bg,
				Background.POSITION_X_LEFT,
				Background.POSITION_Y_TOP,
				Background.REPEAT_SCALE_TO_FIT);
		getMainManager().setBackground(background);
		
		// tab buttons
		String normal[] = {"bb1.png","bb2.png", "bb3.png", "bb4.png"};
		String hover[]  = {"bb1-1.png","bb2-2.png", "bb3-3.png", "bb4-4.png"};
		BitmapButtonField buttons[] = new BitmapButtonField[4];
		for(int i=0; i<4; i++){
			Bitmap bmpNormal = ResourceManager.getBitmapResource(normal[i]);
			Bitmap bmpHover = ResourceManager.getBitmapResource(hover[i]);
			buttons[i] = new BitmapButtonField(bmpNormal, bmpHover);
		}
		//tab background
		Bitmap tabbg = ResourceManager.getBitmapResource("tab-bb.png");
		Background tabBackground = BackgroundFactory.createBitmapBackground(tabbg,
				Background.POSITION_X_LEFT,
				Background.POSITION_Y_TOP,
				Background.REPEAT_HORIZONTAL);
		
		// header
		headers = new Bitmap[4];
		String filename[] = {"channel.png", "now-showing.png", "my-favorite.png", "search.png"};
		for(int i=0; i<4; i++){
			headers[i] = ResourceManager.getBitmapResource(filename[i]);
		}
		
		// bodys
		Manager bodys[] = new Manager[4];
		for(int i=0; i<4; i++){
			VerticalFieldManager vfm = new VerticalFieldManager(Manager.USE_ALL_WIDTH){
				protected void sublayout(int width, int height){
					width = Math.min(width, getPreferredWidth());
					height = Math.min(height, getPreferredHeight());
					super.sublayout(width, height);
				    setExtent(width, height);
				}
				
				public int getPreferredHeight(){
					if(Display.getWidth()<=320)
						return Display.getHeight() - 59;
					return Display.getHeight() - 65;
				}
				
				public int getPreferredWidth(){
					return Display.getWidth();
				}
			};
			//vfm.add(new RichTextField("This is bodys: "+i));
			vfm.add(createPane(i));
			bodys[i] = vfm;
		}
		
		pane = new PaneTabManager(buttons, bodys, tabBackground, PaneTabManager.TAB_ON_BOTTOM);
		pane.setTabChangeListener(this);
		add(pane.getView());
		
//		addMenuItem(buttons[0].createMenuItem("Home", 0x1000, 0));
//		addMenuItem(buttons[1].createMenuItem("Now Showing", 0x1000, 1));
//		addMenuItem(buttons[2].createMenuItem("Favorite", 0x1000, 2));
//		addMenuItem(buttons[3].createMenuItem("Search", 0x1000, 3));
	}
	
	public PaneTabManager getPane(){
		return pane;
	}
	
	public boolean onSavePrompt(){
		return true;
	}
	
	public boolean onClose(){
		int choice = Dialog.ask(Dialog.D_YES_NO, "Exit ThaiTVSchedule");
		if(choice==Dialog.YES)
			return super.onClose();
		return false;
	}
	
	public Object paneViews[] = new Object[4];
	
	public Manager createPane(final int index){
		VerticalFieldManager vfm = new VerticalFieldManager();
		BitmapField bmf = new BitmapField(ResourceManager.getBitmapResource("tab-top.png")){
			public void paint(Graphics g){
				super.paint(g);
				Bitmap header = headers[index];
				int w = header.getWidth();
				int h = header.getHeight();
				int bx = 128;
				int by = 26;
				if(Display.getWidth()<=320){
					bx = 90;
					by = 14;
				}
				int x = bx + ((Display.getWidth()-bx)-w)/2;
				
				g.drawBitmap(x, by, w, h, header, 0, 0);
			}
			
			public boolean isFocusable(){
				return false;
			}
		};
		vfm.add(bmf);
		if(index==0){ // channel
			ChannelView channel = new ChannelView(this, xmlNodes);
			vfm.add(channel.manager);
			paneViews[0] = channel;
		}
		else if(index==1){ // now showing
			NowShowingView now = new NowShowingView(this);
			vfm.add(now.getManager());
			paneViews[1] = now;
		}
		else if(index==2){ // favorite
			FavoriteView favorite = new FavoriteView(this);
			vfm.add(favorite.getManager());
			paneViews[2] = favorite;		
		}
		else if(index==3){ // search
			SearchView search = new SearchView(this);
			vfm.add(search.getManager());
			paneViews[3] = search;	
		}
		
		return vfm;
	}

	public void fieldChanged(Field field, int context) {
		if(context<3){
			String action = ACTION_TAB[context];
			fireAction(action, paneViews[context]);
		}
		else{
			SearchView search = (SearchView)paneViews[context];
			search.autoFocus();
		}
	}

	public void fireActionSearch() {
		int context = 3;
		String action = ACTION_TAB[context];
		fireAction(action, paneViews[context]);
	}
}
