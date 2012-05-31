package net.clicknect.horoscope;
import java.util.Date;
import java.util.Vector;


import net.rim.device.api.i18n.Locale;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.XYRect;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.NullField;
import net.rim.device.api.ui.container.AbsoluteFieldManager;
import net.rim.device.api.ui.container.FlowFieldManager;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.Background;
import net.rim.device.api.ui.decor.BackgroundFactory;

import com.jimmysoftware.device.api.command.Command;
import com.jimmysoftware.device.api.command.CommandHandler;
import com.jimmysoftware.device.api.command.ReadOnlyCommandMetadata;
import com.jimmysoftware.ui.ActionScreen;
import com.jimmysoftware.ui.BitmapButtonField;

public class HomeScreen extends ActionScreen{
	public static final String ACTION_PREDICTION = "let the fortuneteller to foretell";
	public static final String ACTION_RECOMMENED = "recommened application";
	
	private Vector zodiacData;
	private Background bg;
	private FlowFieldManager ffm;
	private Bitmap bmpRasri;
	private String strDate;
	public final static Font DATE_FONT = Font.getDefault().derive(Font.PLAIN, 16);
	
	public HomeScreen(){
		super(false, VerticalFieldManager.NO_VERTICAL_SCROLL);
		bg = BackgroundFactory.createBitmapBackground(Bitmap.getBitmapResource("background.png"),
				Background.POSITION_X_CENTER, Background.POSITION_Y_TOP, Background.REPEAT_SCALE_TO_FIT);
		
		getMainManager().setBackground(bg);
		//afm = new AbsoluteFieldManager();
		//afm.setBackground(bg);
		bmpRasri = Bitmap.getBitmapResource("rasri.png");
		if(Display.getHeight()<=240){
			Bitmap scale = new Bitmap( 320, 32);
			bmpRasri.scaleInto(scale, Bitmap.FILTER_BILINEAR);
			bmpRasri = null;
			bmpRasri = scale;
		}
		
		ffm = new FlowFieldManager(FlowFieldManager.VERTICAL_SCROLLBAR|FlowFieldManager.VERTICAL_SCROLL|FlowFieldManager.FIELD_HCENTER);
		
		//int order[] = {10, 11, 12, 1, 2, 3, 4, 5, 6, 7, 8, 9};
//		Bitmap bmpScale=null;
//		if(Display.getHeight()<=240){
//			bmpScale = new Bitmap(76, 76);
//		}
		for(int i=1; i<=12; i++){
			String prefix = i<10? "P2-z0" : "P2-z";
			String filename = prefix + i + ".png";
			String hover = prefix + i + "-over.png";
			
			Bitmap bmp = Bitmap.getBitmapResource(filename);
			Bitmap bmpHover = Bitmap.getBitmapResource(hover);
			
			if(Display.getHeight()<=240){
				Bitmap bmpScale = new Bitmap(76, 76);
				bmpScale.createAlpha(Bitmap.ALPHA_BITDEPTH_MONO);
				bmp.scaleInto(bmpScale, Bitmap.FILTER_BILINEAR);
				bmp=null; bmp=bmpScale;
				
				bmpScale = new Bitmap(76, 76);
				bmpScale.createAlpha(Bitmap.ALPHA_BITDEPTH_MONO);
				bmpHover.scaleInto(bmpScale, Bitmap.FILTER_BILINEAR);
				bmpHover=null; bmpHover=bmpScale;
			}
			
			BitmapButtonField bbf = new BitmapButtonField(bmp, bmpHover);
			
			if(Display.getHeight()<=240){ // Hardcode for device with resolution 360*240 such as Curve8520
				bbf.setPadding(2, 2, 2, 2);
			}
			else{
				bbf.setPadding(9, 9, 9, 9);
			}
			
			//int zodiac_id = 10+(i-1);
			bbf.setCommand(new Command(new PredictionCommandHandler( i)));
			ffm.add(bbf);
		}
		
		NullField nullField = new NullField(Field.NON_FOCUSABLE){
			public void layout(int width, int height){
				width = Math.min(width, getPreferredWidth());
				height = Math.min(height, getPreferredHeight());
				super.layout(width, height);
				setExtent(width, height);
			}
			
			public int getPreferredWidth(){
				return Display.getWidth();
			}
			
			public int getPreferredHeight(){
				return bmpRasri.getHeight()+20;
			}
		};
		//add("");
		//add("");
		//add("");
		add(nullField);
		add(ffm);
		
		long time = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.get(Locale.LOCALE_th));
		strDate = sdf.format(new Date(time));
		
		sdf = new SimpleDateFormat("yy", Locale.get(Locale.LOCALE_th));
		String strYear= sdf.format(new Date(time));
		int year_th = Integer.parseInt(strYear);
		if(year_th<54) year_th+=43;
		strDate = strDate + " " + year_th;
		
		MenuItem recommenedMenuItem = new MenuItem("Recommened", 101, 2){
			public void run() {
				fireAction(ACTION_RECOMMENED);
			}
		};
		this.addMenuItem(recommenedMenuItem);
	}
	
	public void paint(Graphics g){
		super.paint(g);
		int w = bmpRasri.getWidth();
		int h = bmpRasri.getHeight();
		int x = Display.getWidth()-w;
		int y = 20;
		g.drawBitmap(x, y, w, h, bmpRasri, 0, 0);
		
		g.setFont(DATE_FONT);
		int strWidth = g.getFont().getAdvance(strDate);
		int strHeight = g.getFont().getHeight();
		x = Display.getWidth()-strWidth-3;
		y = y + h/2 - strHeight/2;
		g.setColor(0x991007);
		g.drawText(strDate, x, y);
	}
	
	public void setHoroData(Vector zodiacData){
		this.zodiacData = zodiacData;
//		int size = zodiacData.size();
//		for(int i=0; i<size; i++){
//			ZodiacData data = (ZodiacData)zodiacData.elementAt(i);
//			addRTF(data.toString());
//		}
	}
	
	public String getStringDate(){
		return strDate;
	}
	
	public boolean keyChar(char c, int status, int time){
		if(c==Keypad.KEY_ESCAPE){
			int selected = Dialog.ask(Dialog.D_YES_NO, "Exit Application?");
			if(selected==Dialog.YES){
				System.exit(1);
			}
			return true;
		}
		
		return super.keyChar(c, status, time);
	}
	
	private class PredictionCommandHandler extends CommandHandler{
		int index;
		public PredictionCommandHandler(int index){
			this.index = index;
		}
		
		public void execute(ReadOnlyCommandMetadata metadata, Object context) {
			int size = zodiacData.size();
			for(int i=0; i<size; i++){
				ZodiacData data = (ZodiacData)zodiacData.elementAt(i);
				if(data.id.compareTo(String.valueOf(index))==0){
					fireAction(ACTION_PREDICTION, zodiacData.elementAt(i));
				}
			}
//			Dialog.alert("index="+index);
		}
		
	}
}
