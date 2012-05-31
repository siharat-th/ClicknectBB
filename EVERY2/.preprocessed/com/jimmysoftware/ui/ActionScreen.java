
package com.jimmysoftware.ui;

import java.util.Enumeration;
import java.util.Vector;

import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.BackgroundFactory;

public class ActionScreen extends MainScreen {
	protected Vector actionListeners = new Vector();
	private VerticalFieldManager _manager;

	public ActionScreen(){
		this(true);
	}
	
	public ActionScreen(boolean standardTitleBar){
		this(standardTitleBar, 0);
	}
	
	public ActionScreen(long style){
		this(true, style);
	}
	
	public boolean openBackdoorAltQ(){return false;}
	
	public boolean openBackdoorAltA(){return false;}
	
	private final boolean openBackdoorAltM(){
		Runtime r = Runtime.getRuntime();
		long _total = r.totalMemory();
		long _free = r.freeMemory();
		int selected = Dialog.ask(Dialog.D_YES_NO, 
				"Total memory : "+_total+"\n"+
				"Free memory : "+_free+"\n"+
				"\nClean up memory?");
		if(selected==Dialog.YES){
			r.gc();
			_total = r.totalMemory();
			_free = r.freeMemory();
			Status.show(
					"Clean up done!\n"+
					"Total memory : "+_total+"\n"+
					"Free memory : "+_free+"\n");
		}
		
		return true;
	}
	















	
	public ActionScreen(boolean standardTitleBar, long style){
		super(style);
		if(standardTitleBar){



			

			setTitle(UiFactory.APPLICATION_NAME);

			
			_manager = (VerticalFieldManager)getMainManager();
			_manager.setBackground(BackgroundFactory.createSolidBackground(UiFactory.getDefaultBackgroundColor()));
			//add(new SeparatorField());
		}
	}
	
	public void addActionListener(ActionListener actionListener) {
		if (actionListener != null) {
			actionListeners.addElement(actionListener);
		}
	}

	public void fireAction(String action) {
		fireAction(action, null);
	}

	public void fireAction(String action, Object data) {
//		if(this.isDisplayed() && this.isFocus()){
//			Enumeration listenersEnum = actionListeners.elements();
//			while (listenersEnum.hasMoreElements()) {
//				((ActionListener) listenersEnum.nextElement()).onAction(new Action(this, action, data));
//			}
//		}
		fireAction(action, data, false);
	}
	
	public void fireAction(String action, Object data, boolean fireWhenNotFocus) {
		if(fireWhenNotFocus || (this.isDisplayed() && this.isFocus())){
			Enumeration listenersEnum = actionListeners.elements();
			while (listenersEnum.hasMoreElements()) {
				((ActionListener) listenersEnum.nextElement()).onAction(new Action(this, action, data));
			}
		}
	}
	
	public void add(String label){
		add(new LabelField(label));
	}
	
	public void addRTF(String label){
		add(new RichTextField(label));
	}
	
	public void addSeparator(){
		add(new SeparatorField());
	}
}
