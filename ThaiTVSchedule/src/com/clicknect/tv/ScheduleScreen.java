package com.clicknect.tv;

import java.util.Date;
import java.util.Vector;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.Background;
import net.rim.device.api.ui.decor.BackgroundFactory;
import net.rim.device.api.util.DateTimeUtilities;

import com.clicknect.tv.TVApi.ProgramData;
import com.clicknect.util.ResourceManager;
import com.jimmysoftware.device.api.command.Command;
import com.jimmysoftware.device.api.command.CommandHandler;
import com.jimmysoftware.device.api.command.ReadOnlyCommandMetadata;
import com.jimmysoftware.ui.ActionScreen;
import com.jimmysoftware.ui.BitmapButtonField;

public class ScheduleScreen extends ActionScreen implements ListFieldCallback {
	public static final String ACTION_CLOSE = "close me please";
	public static final String ACTION_CHANGE_DATE = "change date please update schedule";
	
	public static Bitmap LIST_BACKGROUND 	= ResourceManager.getBitmapResource("list-bar.png");
	public static Bitmap LIST_FAVORITE_ON 	= ResourceManager.getBitmapResource("fav1.png");
	public static Bitmap LIST_FAVORITE_OFF 	= ResourceManager.getBitmapResource("fav.png");
	
	// Bitmap Button set
	public static Bitmap BMP_BUTTON_LEFT 		= ResourceManager.getBitmapResource("ar1.png");
	public static Bitmap BMP_BUTTON_LEFT_HOVER 	= ResourceManager.getBitmapResource("ar1-1.png");
	public static Bitmap BMP_BUTTON_RIGHT 		= ResourceManager.getBitmapResource("ar3.png");
	public static Bitmap BMP_BUTTON_RIGHT_HOVER = ResourceManager.getBitmapResource("ar3-1.png");
	public static Bitmap BMP_BUTTON_TODAY 		= ResourceManager.getBitmapResource("ar2.png");
	public static Bitmap BMP_BUTTON_TODAY_HOVER = ResourceManager.getBitmapResource("ar2-1.png");
	
	public static Bitmap BMP_TODAY_SCHEDULE = ResourceManager.getBitmapResource("todayschedule.png"); 
	
	private ListField listField;
	//private DateField dateField;
	public static  int LIST_HEIGHT = LIST_BACKGROUND.getHeight();
	Vector listData = new Vector();
	
	private Date todayDate, currentDate, requestDate;
	private LabelField dateLabel;
	private int menubarIndex;
	public ScheduleScreen(){
		super(false, Manager.NO_VERTICAL_SCROLL|Manager.USE_ALL_HEIGHT);
		
		VerticalFieldManager _root = new VerticalFieldManager(Manager.USE_ALL_WIDTH){
			
			protected void sublayout(int width, int height){
				width = Display.getWidth();//Math.min(width, getPreferredWidth());
				height = Display.getHeight();//Math.min(height, getPreferredHeight());
				super.sublayout(width, height);
			    setExtent(width, height);
			}
			
			public int getPreferredHeight(){
				return Display.getHeight();
			}
			
			public int getPreferredWidth(){
				return Display.getWidth();
			}
			
//			public void paint(Graphics g){
//				super.paint(g);
//				if(ThaiTVSchedule.isPopupDisplay()){
//					int oldColor = g.getColor();
//					try{
//						g.setColor(0);
//						g.setGlobalAlpha(64);
//						g.fillRect(0, 0, Display.getWidth(), Display.getHeight());
//					}
//					finally{
//						g.setColor(oldColor);
//						g.setGlobalAlpha(255);
//					}
//				}
//			}
		};
		// background
		Bitmap bg = ResourceManager.getBitmapResource("bg.png");
		Background background = BackgroundFactory.createBitmapBackground(bg,
				Background.POSITION_X_LEFT,
				Background.POSITION_Y_TOP,
				Background.REPEAT_SCALE_TO_FIT);
		getMainManager().setBackground(background);
		
		// header
		BitmapField bmf = new BitmapField(ResourceManager.getBitmapResource("tab-top.png")){
			public void paint(Graphics g){
				super.paint(g);
				Bitmap header = BMP_TODAY_SCHEDULE;
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
		_root.add(bmf);
		
		//Date
		todayDate = new Date();
		currentDate = new Date(todayDate.getTime());
		requestDate = new Date(todayDate.getTime());
		dateLabel = new LabelField(TVApi.timeMillisTodate(todayDate.getTime()), Field.NON_FOCUSABLE){
			public void paint(Graphics g){
				g.setColor(0);
				super.paint(g);
			}
		};
		
		// menu bar button
		BitmapButtonField leftButon = new BitmapButtonField(BMP_BUTTON_LEFT, BMP_BUTTON_LEFT_HOVER);
		BitmapButtonField rightButon = new BitmapButtonField(BMP_BUTTON_RIGHT, BMP_BUTTON_RIGHT_HOVER);
		BitmapButtonField todayButon = new BitmapButtonField(BMP_BUTTON_TODAY, BMP_BUTTON_TODAY_HOVER);
		leftButon.setCommand(new Command(new ButtonCommandHandler(-1)));
		rightButon.setCommand(new Command(new ButtonCommandHandler(1)));
		todayButon.setCommand(new Command(new ButtonCommandHandler(0)));
		
		// menu bar
		HorizontalFieldManager hfm = new HorizontalFieldManager(HorizontalFieldManager.NO_VERTICAL_SCROLL|Field.FOCUSABLE){
			int padding = 5;
			protected void sublayout(int width, int height){
				
				width = Math.min(width, getPreferredWidth());
				height = Math.min(height, getPreferredHeight());
				int numberOfFields = getFieldCount();
				if(numberOfFields!=4){
					super.sublayout(width, height);
					return;
				}

			    for (int i = 0;i < numberOfFields;i++) {
			    	Field field = getField(i);
			    	int y = getPreferredHeight()/2;
			    	if(i==0){
			    		int h = field.getFont().getHeight();
			    		setPositionChild(field, padding, y-h/2);
				    	layoutChild(field, width, height);
			    	}
			    	else{
			    		int dec_m[] = {0, 185, 155, 30}; 
			    		int dec_s[] = {0, 120, 100, 20};
			    		int dec[];
			    		if(Display.getWidth()<=320)
			    			dec = dec_s;
			    		else
			    			dec = dec_m;
			    		int h = BMP_BUTTON_LEFT.getHeight();
			    		int x = getPreferredWidth() - dec[i];
			    		setPositionChild(field, x, y-h/2);
				    	layoutChild(field, width, height);
			    	}
			    }
			    setExtent(width, height);
			}
			
			public int getPreferredWidth(){
				return Display.getWidth();
			}
			
			public int getPreferredHeight(){
				return 40;
			}
			
			public boolean navigationMovement(int dx, int dy, int status, int time){
				if(dy>0){
					listField.setFocus();
					return true; 
				}
				return super.navigationMovement(dx, dy, status, time);
			}
			
			protected void onFocus(int direction){
				super.onFocus(direction);
				if(listField!=null)listField.invalidate();
			}
		};
		hfm.add(dateLabel);
		hfm.add(leftButon);
		hfm.add(todayButon);
		hfm.add(rightButon);
		
		_root.add(hfm);
		
		VerticalFieldManager vfm = new VerticalFieldManager(Manager.VERTICAL_SCROLL|Manager.VERTICAL_SCROLLBAR|Manager.USE_ALL_HEIGHT){
			protected void sublayout(int width, int height){
				width = Math.min(width, getPreferredWidth());
				height = Math.min(height, getPreferredHeight());
				super.sublayout(width, height);
			    setExtent(width, height);
			}
			
			public int getPreferredHeight(){
				return Display.getHeight();
			}
			
			public int getPreferredWidth(){
				return Display.getWidth();
			}
		};
		listField = new ListField(){
			public boolean navigationClick(int status, int time){
				if(listData.size()<=0)
					return super.navigationClick(status, time);
				int index = getSelectedIndex();
				ProgramData data = (ProgramData)ScheduleScreen.this.listData.elementAt(index);
				data.isFavorite = !data.isFavorite;
				if(data.isFavorite){
					FavoritePersistent.addFavorite(data.id);
					PIMEvent.addEvent(data);
				}
				else{
					FavoritePersistent.removeFavorite(data.id);
					PIMEvent.removeEvent(data);
				}
				invalidate(); 
				return true;
			}
			
			public void paint(Graphics g){
				int ty = getContentTop()+ LIST_HEIGHT/2 - g.getFont().getHeight()/2;
				if(ThaiTVSchedule.isFetching()){
					ThaiTVSchedule.paintFetching(g, ty);
				}
				else if(this.getSize()<1){
					g.setColor(0);
					int x = (getWidth() - g.getFont().getAdvance("No data"))/2;
					g.drawText("No data", x, ty);
				}
				else{
					super.paint(g);
				}
			}
			
			public boolean isFocusable(){
				return !ThaiTVSchedule.isFetching();
			}
			
//			protected void onUnfocus(){
//				super.onUnfocus();
//				invalidate();
//			}
		};
		listField.setCallback(this);
		listField.setRowHeight(ScheduleScreen.LIST_HEIGHT);
		listField.setSize(0);
		listField.setFont(Font.getDefault().derive(Font.PLAIN, 20));
		vfm.add(listField);
		_root.add(vfm);
		
		//todayButon.setFocus();
		menubarIndex = 2;
		add(_root);
	}
	
	protected void sublayout(int width, int height){
//		setExtent(Display.getWidth(), Display.getHeight());
		super.sublayout(Display.getWidth(), Display.getHeight());
		setExtent(Display.getWidth(), Display.getHeight());
	}
	
//	public void paint(Graphics g){
//		super.paint(g);
//		if(listField.getSize()>0 && ThaiTVSchedule.isPopupDisplay()){
//			int oldColor = g.getColor();
//			try{
//				g.setColor(0);
//				g.setGlobalAlpha(64);
//				g.fillRect(0, 0, Display.getWidth(), Display.getHeight());
//			}
//			finally{
//				g.setColor(oldColor);
//				g.setGlobalAlpha(255);
//			}
//		}
//	}
	
//	public void setData(Vector listData){
////		if(listField.getSize()==0){
////			listField.setFocus();
////		}
//		this.listData = listData;
//		listField.setSize(listData.size());
////		listField.setFocus();
//		listField.invalidate();
//	}
	
	public void setData(Vector listData){
		this.listData = listData;
		listField.setSize(listData.size());
		//listField.setFocus();
	}
	
	public void drawListRow(ListField listField, Graphics g, int index, int y, int width) {
		ProgramData data = (ProgramData)listData.elementAt(index);
		
		if(!listField.isFocus() || index!=listField.getSelectedIndex()){
			for(int x=0; x<Display.getWidth(); x+=LIST_BACKGROUND.getWidth())
				g.drawBitmap(x, y, LIST_BACKGROUND.getWidth(), LIST_BACKGROUND.getHeight(), LIST_BACKGROUND, 0, 0);
		}
		
		Bitmap thumb = TVApi.getChannelThumbnail(data.channelId);
		if(thumb!=null){
			int padding = (LIST_HEIGHT-thumb.getHeight())/2;
			int ty = y+padding;
			int tx = padding;
			g.drawBitmap(tx, ty, thumb.getWidth(), thumb.getHeight(), thumb, 0, 0);
			g.setColor(0);
			g.drawText(data.beginTime+" - "+data.endTime, tx+thumb.getWidth()+tx, ty);
			Bitmap favorite = data.isFavorite ? LIST_FAVORITE_ON : LIST_FAVORITE_OFF;
			int titleMaxWidth = width - thumb.getWidth() - favorite.getWidth() - (padding*4);
			g.drawText(data.title, tx+thumb.getWidth()+tx, ty+thumb.getHeight(), DrawStyle.BASELINE|DrawStyle.ELLIPSIS, titleMaxWidth);
			
			
			g.drawBitmap(width-padding-favorite.getWidth(), y+(LIST_HEIGHT-favorite.getHeight())/2,
					favorite.getWidth(), favorite.getHeight(), favorite, 0, 0);
		}
	}
	public Object get(ListField listField, int index) {
		return listData.elementAt(index);
	}
	public int getPreferredWidth(ListField listField) {
		return Display.getWidth();
	}
	public int indexOfList(ListField listField, String prefix, int start) {
		return listData.indexOf(prefix, start);
	}
	
	public boolean onSavePrompt(){
		return true;
	}
	
	public boolean onClose(){
		ThaiTVSchedule.stopFetch();
		fireAction(ACTION_CLOSE);
		return false;
	}
	
	public void setDateLabel(final String strDate){
		UiApplication.getUiApplication().invokeLater(new Runnable(){

			public void run() {
				currentDate.setTime(requestDate.getTime());
				dateLabel.setText(strDate);
			}
			
		});
	}
	
	private class ButtonCommandHandler extends CommandHandler{
		private int flag;
		public ButtonCommandHandler(int flag){
			this.flag = flag;
		}
		public void execute(ReadOnlyCommandMetadata metadata, Object context) {
			int currentIndex = 0;
			if(flag==0){
				requestDate.setTime(todayDate.getTime());
				currentIndex = 2;
			}
			else if(flag==-1){
				long time = currentDate.getTime();
				time-=DateTimeUtilities.ONEDAY;
				requestDate.setTime(time);
				currentIndex = 1;
			}
			else if(flag==1){
				long time = currentDate.getTime();
				time+=DateTimeUtilities.ONEDAY;
				requestDate.setTime(time);
				currentIndex = 3;
			}
			else{return;}
			
			if(currentIndex==2 && menubarIndex==2){
				return;
			}
			menubarIndex = currentIndex;
			//String strDate = TVApi.timeMillisTodate(currentDate.getTime());
			//dateLabel.setText(strDate);
			fireAction(ACTION_CHANGE_DATE);
		}
		
	}

	public Date getCurrentDate() {
		return currentDate;
	}
	
	public Date getRequestDate() {
		return requestDate;
	}
}
