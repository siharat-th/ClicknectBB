package com.clicknect.bbbuddy;

import java.util.Vector;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.TouchEvent;
import net.rim.device.api.ui.TouchGesture;
import net.rim.device.api.ui.Touchscreen;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.Background;
import net.rim.device.api.ui.decor.BackgroundFactory;
import net.rim.device.api.ui.decor.Border;
import net.rim.device.api.ui.decor.BorderFactory;
import net.rim.device.api.ui.extension.component.PictureScrollField;
import net.rim.device.api.ui.extension.component.PictureScrollField.HighlightStyle;
import net.rim.device.api.ui.extension.component.PictureScrollField.ScrollEntry;

import com.jimmysoftware.ui.ActionScreen;

public class FeatureScreen extends ActionScreen implements FieldChangeListener {
	public static final String ACTION_CLOSE = "close feature screen"; 
	public static final String ACTION_ENTER = "open detail screen";
	private Vector listInfos;
	int index;
	int maxIndex;
//	MyBitmapField bitmapField;
	MyTextField description;
	MyTextField title;
	
	PictureScrollField pictureScrollField;
	
	public FeatureScreen(final Vector listInfos){
		super(Manager.NO_VERTICAL_SCROLL);
		Bitmap bitmap = Bitmap.getBitmapResource("background.png");
		getMainManager().setBackground(BackgroundFactory.createBitmapBackground(bitmap, Background.POSITION_X_LEFT, Background.POSITION_Y_TOP, Background.REPEAT_SCALE_TO_FIT));
		
		VerticalFieldManager vfm = new VerticalFieldManager(Manager.VERTICAL_SCROLL);
		//vfm.setMargin(0, 5, 5, 5);
		this.listInfos = listInfos;
		ListInfo listInfo = (ListInfo)listInfos.elementAt(0);
		pictureScrollField = new PictureScrollField(listInfo.img.getWidth(), listInfo.img.getHeight()){
			public boolean navigationClick(int status, int time){
				ListInfo listInfo = (ListInfo)listInfos.elementAt(getCurrentImageIndex());
				fireAction(ACTION_ENTER, listInfo);
				return true;
				//return super.navigationClick(status, time);
			}
		};
		pictureScrollField.setBackground(BackgroundFactory.createSolidTransparentBackground(0, 0));
		ScrollEntry entries[] = new ScrollEntry[listInfos.size()];
		for(int i=0; i<entries.length; i++){
			ListInfo l = (ListInfo)listInfos.elementAt(i);
			entries[i] = new ScrollEntry(l.img, l.name, null);
		}
		pictureScrollField.setData(entries, 0);
		pictureScrollField.setMargin(5, 0, 0, 0);
		pictureScrollField.setHighlightStyle(HighlightStyle.SHRINK_LENS);
        pictureScrollField.setBackground(BackgroundFactory.createSolidTransparentBackground(0, 0));
        //pictureScrollField.setLensShrink(0.5f);
        pictureScrollField.setCenteredLens(true);
        pictureScrollField.setLabelsVisible(false);
        pictureScrollField.setChangeListener(this);
		vfm.add(pictureScrollField);
		
//		bitmapField = new MyBitmapField();
//		bitmapField.setBitmap(listInfo.img);
		//bitmapField.setMargin(2, 0, 0, 0);
//		vfm.add(bitmapField);
		
		VerticalFieldManager descVfm = new VerticalFieldManager(Manager.VERTICAL_SCROLL);
		descVfm.setMargin(5, 5, 5, 5);
		descVfm.setPadding(2, 2, 2, 2);
		descVfm.setBorder(BorderFactory.createRoundedBorder(new XYEdges(5, 5, 5, 5), 0x555555, Border.STYLE_SOLID));
		
		title = new MyTextField();
		title.setText(listInfo.name);
		title.setColor(0xffffff);
		title.setFont(HomeScreenAppCenter.fontNormalBold);
		descVfm.add(title);
		
		description = new MyTextField();
		description.setText(listInfo.desc);
		description.setColor(0xffffff);
		descVfm.add(description);
		vfm.add(descVfm);
		
		
		index = 0;
		maxIndex = listInfos.size();
		setTitle(" Featured Items "+(index+1)+"/"+maxIndex);
		add(vfm);
	}
	
	public boolean onClose(){
		fireAction(ACTION_CLOSE);
		return false;
	}
	
//	protected boolean touchEvent(TouchEvent message){
////		int x = message.getX(1);
////		int y = message.getY(1);
////        if(message.getEvent() == TouchEvent.CLICK)
////        {
////            if(pictureScrollField.isFocus()){ 
////            	ListInfo listInfo = (ListInfo)listInfos.elementAt(pictureScrollField.getCurrentImageIndex());
////				fireAction(ACTION_ENTER, listInfo);     
////            }      
////            return true;
////        }
////        return super.touchEvent(message);
//		
//		ListInfo listInfo = (ListInfo)listInfos.elementAt(pictureScrollField.getCurrentImageIndex());
//		fireAction(ACTION_ENTER, listInfo); 
//		return true;
//    }
	
//	protected boolean navigationMovement(int dx, int dy, int status, int time){
//		if(bitmapField.isFocus()){
//			if(dx<0){
//				decIndex();
//				return true;
//			}
//			else if(dx>0){
//				incIndex();
//				return true;
//			}
//		}
//		return super.navigationMovement(dx, dy, status, time);
//	}

//	public void incIndex() {
//		index++;
//		if(index>maxIndex-1) index=0;
//		setTitle("Featured Items "+(index+1)+"/"+maxIndex);
//		ListInfo listInfo = (ListInfo)listInfos.elementAt(index);
//		bitmapField.setBitmap(listInfo.img);
//		title.setText(listInfo.name);
//		description.setText(listInfo.desc);
//	}
//
//	public void decIndex() {
//		index--;
//		if(index<0) index = maxIndex-1;
//		setTitle("Featured Items "+(index+1)+"/"+maxIndex);
//		ListInfo listInfo = (ListInfo)listInfos.elementAt(index);
//		bitmapField.setBitmap(listInfo.img);
//		title.setText(listInfo.name);
//		description.setText(listInfo.desc);
//	}
	
//	class MyBitmapField extends BitmapField{
//		public int getPreferredWidth(){
//			return Display.getWidth();
//		}
//		
//		public int getPreferredHeight(){
//			return getBitmapHeight()+4;
//		}
//		
//		public void layout(int width, int height){
//			int w = Math.min(width, getPreferredWidth());
//			int h = Math.min(height, getPreferredHeight());
//			setExtent(w, h);
//		}
//		
//		public void drawFocus(Graphics g, boolean on){
//		}
//		
//		public void paint(Graphics g){
//			int w = getWidth();
//			int h = getHeight();
//			Bitmap bmp = getBitmap();
//			
//			int x = (w-bmp.getWidth())/2;
//			int y = getTop() + 2;
//			if(isFocus()){
//				g.setGlobalAlpha(255);
//			}
//			else{
//				g.setGlobalAlpha(128);
//			}
//			g.drawBitmap(x, y, 256, 256, bmp, 0, 0);
//			g.setColor(0xffffff);
//			g.drawText("<", 0, h/2);
//			g.drawText(">", w-g.getFont().getAdvance(">"), h/2);
//			
////			if(isFocus()){
////				g.setStrokeWidth(2);
////				g.drawRect(x, y, 256, 256);
////			}
//			
//			g.setGlobalAlpha(255);
//		}
//		
//		public boolean isFocusable(){
//			return true;
//		}
//		
//		protected boolean touchEvent(TouchEvent message){
//			if(message.getEvent()==TouchEvent.GESTURE){
//				TouchGesture gesture = message.getGesture();
//				if(gesture.getEvent()==TouchGesture.SWIPE){
//					int dir = gesture.getSwipeDirection();
//					if(dir==TouchGesture.SWIPE_WEST){
//						decIndex();
//						return true;
//					}
//					else if(dir==TouchGesture.SWIPE_EAST){
//						incIndex();
//						return true;
//					}
//				}
//			}
//			
//			return false;
//		}
//		
//		public boolean navigationClick(int status, int time){
//			ListInfo listInfo = (ListInfo)listInfos.elementAt(index);
//			fireAction(ACTION_ENTER, listInfo);
//			return true;
//		}
//	}
	
	class MyTextField extends RichTextField{
		int color;
		public void setColor(int color){
			this.color = color;
		}
		
		public void paint(Graphics g){
			g.setColor(color);
			super.paint(g);
		}
	}

	public void fieldChanged(Field f, int context) {
		if(f==pictureScrollField){
			int index = pictureScrollField.getCurrentImageIndex();
			ListInfo l = (ListInfo)listInfos.elementAt(index);
			
			setTitle(" Featured Items "+(index+1)+"/"+maxIndex);
			title.setText(l.name);
			description.setText(l.desc);
		}
	}
}
