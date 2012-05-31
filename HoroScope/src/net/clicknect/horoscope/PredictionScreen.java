package net.clicknect.horoscope;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import net.clicknect.horoscope.OnepostApi.ServiceInfo;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.FontFamily;
import net.rim.device.api.ui.FontManager;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.GridFieldManager;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.Background;
import net.rim.device.api.ui.decor.BackgroundFactory;
import net.rim.device.api.ui.decor.BorderFactory;
import net.rim.device.api.ui.extension.container.EyelidFieldManager;

import com.jimmysoftware.device.api.command.Command;
import com.jimmysoftware.device.api.command.CommandHandler;
import com.jimmysoftware.device.api.command.ReadOnlyCommandMetadata;
import com.jimmysoftware.network.HttpConnectionFactory;
import com.jimmysoftware.ui.ActionScreen;
import com.jimmysoftware.ui.BitmapButtonField;
import com.jimmysoftware.ui.EventThreadDialog;

public class PredictionScreen extends ActionScreen {
	public static final String ACTION_ON_CLOSE = "close me please";
	public static final String ACTION_REGISTER = "please register me";
	public static final String ACTION_SEND = "send message";
//	public static Font FONT_CARA, FONT_CARE;
	
	public ZodiacData data;
	
//	static{
//		FontManager.getInstance().load("P_Cara.ttf", "PLE_Cara", FontManager.APPLICATION_FONT);
//			try {
//				FontFamily typeface = FontFamily.forName("PLE_Cara");
//				FONT_CARA = typeface.getFont(Font.BOLD, 32);
//			} catch (ClassNotFoundException e) {
//				e.printStackTrace();
//			}
//		
//		FontManager.getInstance().load("P_Care.ttf", "PLE_Care", FontManager.APPLICATION_FONT);
//			try {
//				FontFamily typeface = FontFamily.forName("PLE_Care");
//				FONT_CARE = typeface.getFont(Font.PLAIN, 18);
//			} catch (ClassNotFoundException e) {
//				e.printStackTrace();
//			}
//		
//	}
	
	private static final Bitmap BMP_TITLE_BG = Bitmap.getBitmapResource("title-bg.png");
	//private static final Bitmap BMP_TAB_BG = Bitmap.getBitmapResource("tab-bg.png");
	private static Bitmap BMP_BG = Bitmap.getBitmapResource("tab1.png");
	static{
		//238*48
		if(Display.getHeight()<=240){
			Bitmap scale = new Bitmap(Display.getWidth(), 48);
			BMP_BG.scaleInto(scale, Bitmap.FILTER_BILINEAR, Bitmap.SCALE_STRETCH);
			BMP_BG = null;
			BMP_BG = scale;
		}
	}
	private static final Bitmap BMP_FACEBOOK = Bitmap.getBitmapResource("icon-facebook.png");
	private static final Bitmap BMP_FACEBOOK_HOVER = Bitmap.getBitmapResource("icon-facebook-over.png");
	private static final Bitmap BMP_TWITTER = Bitmap.getBitmapResource("icon-twitter.png");
	private static final Bitmap BMP_TWITTER_HOVER = Bitmap.getBitmapResource("icon-twitter-over.png");
	private static final Bitmap BMP_HOME = Bitmap.getBitmapResource("icon-home.png");
	private static final Bitmap BMP_HOME_HOVER = Bitmap.getBitmapResource("icon-home-over.png");
	private static final Bitmap BMP_CLOSE = Bitmap.getBitmapResource("icon-close.png");
	private static final Bitmap BMP_CLOSE_HOVER = Bitmap.getBitmapResource("icon-close-over.png");
	
	public String strDate;
	public OnepostApi onepostApi;
	
	public PredictionScreen(HttpConnectionFactory connFactory, final String strDate, ZodiacData data){
		super(false, VerticalFieldManager.NO_VERTICAL_SCROLL);
		this.strDate = strDate;
		this.data = data;
		onepostApi = new OnepostApi(connFactory);
		
		Background bg = BackgroundFactory.createBitmapBackground(Bitmap.getBitmapResource("background.png"),
				Background.POSITION_X_CENTER, Background.POSITION_Y_TOP, Background.REPEAT_SCALE_TO_FIT);
		
		getMainManager().setBackground(bg);
		
		//fcd200
//		LabelField titleField = new LabelField("ดูดวงประจำวันที่ "+strDate, Field.USE_ALL_WIDTH){
//			public void paint(Graphics g){
//				paintBackground(g);
//				
//				g.setGlobalAlpha(128);
//				g.setColor(0x4b4b4b);
//				g.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
//				
//				g.setGlobalAlpha(255);
//				g.setColor(0xffffff);
//				super.paint(g);
//			}
//		};
//		titleField.setBackground(BackgroundFactory.createLinearGradientBackground(
//				0x480607, 0x9c1007, 0x9c1007, 0x480607));
//		
//		titleField.setFont(Font.getDefault().derive(Font.BOLD));
//		titleField.setPadding(4, 4, 4, 4);
		
		BitmapField titleField = new BitmapField(BMP_TITLE_BG, Field.USE_ALL_WIDTH){
			public void paint(Graphics g){
				g.setColor(0xfcd200);
				g.fillRect(0 , 0, getWidth(), getHeight());
				int w = getBitmapWidth();
				int x = Display.getWidth()-w;
				g.drawBitmap(x, 0, getBitmapWidth(), getBitmapHeight(), getBitmap(), 0, 0);
				
				g.setFont(HomeScreen.DATE_FONT);
				int strWidth = g.getFont().getAdvance(strDate);
				int strHeight = g.getFont().getHeight();
				int h = getBitmapHeight();
				x = Display.getWidth()-strWidth-3;
				int y = (h - strHeight)/2;
				g.setColor(0x991007);
				g.drawText(strDate, x, y);
				
				g.setColor(0x9d8c75);
				g.drawLine(0, getHeight()-1, Display.getWidth(), getHeight()-1);
			}
			
			public int getPreferredHeight(){
				return BMP_TITLE_BG.getHeight();
			}
			
			public int getPreferredWidth(){
				return Display.getWidth();
			}
		};

		//setTitle(titleField);
		
		final int index = Integer.parseInt(data.id.trim());
		final Bitmap thumb = HoroApi.getBmpThumbByID(index);
		final String date = HoroApi.getZodiacDateByID(index);
		final int padding = (BMP_BG.getHeight()-thumb.getHeight())/2;//7;
		BitmapField header = new BitmapField(BMP_BG){
			public void paint(Graphics g){
				g.drawBitmap(padding, padding, thumb.getWidth(), thumb.getHeight(), thumb, 0, 0);
				
				int strWidth = g.getFont().getAdvance(date);
				int strHeight = g.getFont().getHeight();
				int x = (thumb.getWidth()+padding)+((Display.getWidth() - padding - thumb.getWidth())-strWidth)/2;
				int y = thumb.getHeight()/2 - strHeight/3;
				g.setColor(0x1386a0);
				g.drawText(date, x, y);
				
				g.setColor(0x9d8c75);
				g.drawLine(0, getHeight()-1, Display.getWidth(), getHeight()-1);
			}
			
			public int getPreferredHeight(){
				return BMP_BG.getHeight();
			}
			
			public int getPreferredWidth(){
				return Display.getWidth();
			}
		};
		header.setBackground(BackgroundFactory.createBitmapBackground(BMP_BG, Background.POSITION_X_LEFT, Background.POSITION_Y_TOP, Background.REPEAT_SCALE_TO_FIT));
		
		VerticalFieldManager body = new VerticalFieldManager(VerticalFieldManager.VERTICAL_SCROLL|VerticalFieldManager.VERTICAL_SCROLLBAR|Field.FOCUSABLE){
			
			protected void sublayout(int width, int height){
				width = Math.min(width, getPreferredWidth());
				height = Math.min(height, getPreferredHeight());
				super.sublayout(width, height);
			    setExtent(width, height);
			}
			
			public int getPreferredHeight(){
				return Display.getHeight() - (BMP_TITLE_BG.getHeight()+BMP_BG.getHeight()+BMP_FACEBOOK.getHeight()+4);
			}
			
			public int getPreferredWidth(){
				return Display.getWidth();
			}
		};
		body.setBackground(BackgroundFactory.createBitmapBackground(BMP_BG, Background.POSITION_X_LEFT, Background.POSITION_Y_TOP, Background.REPEAT_SCALE_TO_FIT));
		RichTextField desc = new RichTextField(data.description, Field.FOCUSABLE);
		desc.setPadding(5, 5, 5, 5);
		body.add(desc);
		
		BitmapButtonField home = new BitmapButtonField(BMP_HOME, BMP_HOME_HOVER);
		BitmapButtonField facebook = new BitmapButtonField(BMP_FACEBOOK, BMP_FACEBOOK_HOVER);
		BitmapButtonField twitter = new BitmapButtonField(BMP_TWITTER, BMP_TWITTER_HOVER);
		BitmapButtonField close = new BitmapButtonField(BMP_CLOSE, BMP_CLOSE_HOVER);
		
		XYEdges edgetwo = new XYEdges(2, 2, 2, 2);
		home.setPadding(edgetwo);
		facebook.setPadding(edgetwo);
		twitter.setPadding(edgetwo);
		close.setPadding(edgetwo);
		
		home.setCommand(new Command(new CommandHandler(){

			public void execute(ReadOnlyCommandMetadata metadata, Object context) {
				fireAction(ACTION_ON_CLOSE);
			}
			
		}));
		facebook.setCommand(new Command(new SocialShareCommandHandler(SocialShareCommandHandler.SERVICE_FACEBOOK)));
		twitter.setCommand(new Command(new SocialShareCommandHandler(SocialShareCommandHandler.SERVICE_TWITTER)));
		close.setCommand(new Command(new CommandHandler(){

			public void execute(ReadOnlyCommandMetadata metadata, Object context) {
				int selected = Dialog.ask(Dialog.D_YES_NO, "Exit Application?");
				if(selected==Dialog.YES)
					System.exit(1);
			}
			
		}));
		
		
		HorizontalFieldManager tabbar = new HorizontalFieldManager(Manager.USE_ALL_WIDTH|Manager.FIELD_HCENTER){
			private int height=0, width=0;
			protected void sublayout(int width, int height){
				width = Math.min(width, getPreferredWidth());
				height = Math.min(height, getPreferredHeight());
				int numberOfFields = getFieldCount();
				if(numberOfFields<=0) return;
				int step = getPreferredWidth()/numberOfFields;
			    int x = step/2;
			    int y = height/2;
			    for (int i = 0;i < numberOfFields;i++) {
			    	Field field = getField(i); //get the field
			    	BitmapButtonField button = (BitmapButtonField)field;
			    	int w = button.getBitmapWidth();
			    	int h = button.getBitmapHeight();
			    	setPositionChild(field, x-w/2, y-h/2-2); //set the position for the field
			    	layoutChild(field, width, height); //lay out the field
			    	x+=step;
			    }
			    setExtent(width, height);
			}
			
			public void add(Field field){
				BitmapButtonField button = (BitmapButtonField)field;
				height = Math.max(height, button.getBitmapHeight()+4);
				width += button.getBitmapWidth() + 4;
				super.add(button);
			}
			
			public int getPreferredWidth(){
				
				return Math.max(Display.getWidth(), width);
			}

			public int getPreferredHeight(){
				return height;
			}
			
			public void paint(Graphics g){
				super.paint(g);
				g.setColor(0x9d8c75);
				g.drawLine(0, 0, Display.getWidth(), 0);
				//g.setColor(0xafafaf);
				//g.drawLine(0, 1, Display.getWidth(), 1);
			}
		};
		tabbar.setBackground(BackgroundFactory.createLinearGradientBackground(
				0x392225, 0x392225, 0x130c0c, 0x130c0c));
		
		tabbar.add(home);
		tabbar.add(facebook);
		tabbar.add(twitter);
		tabbar.add(close);
		
		
		EyelidFieldManager efm = new EyelidFieldManager();
		efm.setEyelidDisplayTime(0);
		
		efm.addTop(titleField);
		efm.add(header, 0, titleField.getBitmapHeight());
		efm.add(body, 0, titleField.getBitmapHeight()+header.getBitmapHeight());
		efm.addBottom(tabbar);
		
		add(efm);
	}
	
//	public void paint(Graphics g){
//		super.paint(g);
//	}
	
//	public void paintBackground(Graphics g){
//		//super.paint(g);
//	}
	
	public boolean onClose(){
		fireAction(ACTION_ON_CLOSE);
		return false;
	}
	
	public static PopupScreen popupRequest = new PopupScreen(new HorizontalFieldManager(Manager.USE_ALL_WIDTH));
	static{
		popupRequest.add(new BitmapField(Bitmap.getPredefinedBitmap(Bitmap.HOURGLASS)));
		popupRequest.add(new LabelField("Requesting...", Field.FIELD_VCENTER));
	}
	
	private class SocialShareCommandHandler extends CommandHandler{
		public static final String SERVICE_FACEBOOK = "facebook";
		public static final String SERVICE_TWITTER = "twitter";
		private String service;
		public SocialShareCommandHandler(String service){
			this.service = service;
		}
		
		public void execute(ReadOnlyCommandMetadata metadata, Object context) {
			int selected = Dialog.ask(Dialog.D_YES_NO, "Share via "+service);
			if(selected==Dialog.NO) return;
			
			UiApplication.getUiApplication().pushScreen(popupRequest);
			Thread t = new Thread(new Runnable(){
				public void run() {
					InputStream is = null;
					try {
						is = onepostApi.getOnepostServiceAvailable();
						Vector nodes = onepostApi.parseXMLAsVector(is);
						Vector serviceInfos = onepostApi.getOnepostServiceInfos(nodes);
						int size = serviceInfos.size();
						ServiceInfo myServiceinfo = null;
						for(int i=0; i<size; i++){
							ServiceInfo info = (ServiceInfo)serviceInfos.elementAt(i);
							if(info.getServiceName().equalsIgnoreCase(service)){
								myServiceinfo = info;
								break;
							}
						}
						if(myServiceinfo!=null){
							if(!myServiceinfo.isRegistered()){
								synchronized(UiApplication.getEventLock()){
									fireAction(ACTION_REGISTER, myServiceinfo);
								}
							}
							else{
								synchronized(UiApplication.getEventLock()){
									fireAction(ACTION_SEND, myServiceinfo);
								}
							}
						}
						else{
							EventThreadDialog.errorDialog("Service unavailable, please try again later.");
						}
					} 
					catch (Exception e) {
						e.printStackTrace();
						EventThreadDialog.errorDialog("Connection error, please check your internet connection."+e.toString());
					}
					finally{
						if(is!=null) 
							try{is.close();}catch(IOException e){e.printStackTrace();}
							closePopupRequest();
					}
				}
			});
			t.start();
		}
	}
	
	public static void closePopupRequest(){
		UiApplication.getUiApplication().invokeLater(new Runnable(){

			public void run() {
				if(popupRequest.isDisplayed())
					popupRequest.close();
			}
			
		});
	}
}
