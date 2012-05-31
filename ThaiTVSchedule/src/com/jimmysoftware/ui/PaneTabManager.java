package com.jimmysoftware.ui;

import com.clicknect.tv.ThaiTVSchedule;
import com.clicknect.util.MsgBox;
import com.jimmysoftware.device.api.command.Command;
import com.jimmysoftware.device.api.command.CommandHandler;
import com.jimmysoftware.device.api.command.ReadOnlyCommandMetadata;

import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.Background;
import net.rim.device.api.ui.decor.BackgroundFactory;
import net.rim.device.api.ui.extension.container.EyelidFieldManager;

public class PaneTabManager {
	public final static int TAB_ON_TOP = 0xf0;
	public final static int TAB_ON_BOTTOM = 0xf1;
	private EyelidFieldManager efm;
	private Manager currentBody;
	private Manager[] bodys;
	private Manager bodyWrapper;
	private HorizontalFieldManager hfm;
	private FieldChangeListener listener;
	
	public PaneTabManager(BitmapButtonField[] tabButtons, Manager[] bodys, Background tabBackground, int style){
		efm = new EyelidFieldManager();
		efm.setEyelidDisplayTime(0);
		efm.setBottomBackground(BackgroundFactory.createSolidTransparentBackground(0, 0));
		
		// set body
		this.bodys = bodys;
		currentBody = bodys[0];
		bodyWrapper = new VerticalFieldManager(Manager.USE_ALL_WIDTH);
		bodyWrapper.add(bodys[0]);
		efm.add(bodyWrapper, 0, 0);
		
		// make tab
		hfm = new HorizontalFieldManager(Manager.USE_ALL_WIDTH|Manager.FIELD_HCENTER){
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
			    	setPositionChild(field, x-w/2, y-h/2); //set the position for the field
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
				if(Display.getWidth()<=320)
					return 59;
				return 65;
			}
			
			public boolean navigationMovement(int dx, int dy, int status, int time){
				if(dy<0){
					int fieldCount = currentBody.getFieldCount();
					Manager m = (Manager)currentBody.getField(fieldCount-1);
					Manager m2 = (Manager)m.getField(m.getFieldCount()-1);
					Field lastField = m2.getField(m2.getFieldCount()-1);
					if(lastField!=null)
						lastField.setFocus();
					return true;
				}
				return super.navigationMovement(dx, dy, status, time);
			}
		};
		hfm.setBackground(tabBackground);
		for(int i=0; i<tabButtons.length; i++){
			tabButtons[i].setCommand(new Command(new TabCommandHandler(i)));
			hfm.add(tabButtons[i]);
		}
		if(style==TAB_ON_BOTTOM){
			efm.addBottom(hfm);
		}
		else{
			efm.addTop(hfm);
		}
	}
	
	private boolean setBody(Manager newBody) {
		if(newBody!=currentBody){
			ThaiTVSchedule.stopFetch();
			Field oldBody = currentBody;
			bodyWrapper.replace(oldBody, newBody);
			currentBody = newBody;
			return true;
		}
		return false;
	}

	public Field getView(){
		return efm;
	}
	
	public void setTabChangeListener(FieldChangeListener listener){
		this.listener = listener;
	}
	
	class TabCommandHandler extends CommandHandler{
		private int index;
		public TabCommandHandler(int index){
			this.index = index;
		}
		public void execute(ReadOnlyCommandMetadata metadata, Object context) {
			UiApplication.getUiApplication().invokeLater(new Runnable(){

				public void run() {
					Manager newBody = bodys[index];
					boolean b = setBody(newBody);
					if(b && listener!=null)
						listener.fieldChanged(newBody, index);
				}
				
			});
			
		}
		
	}
}
