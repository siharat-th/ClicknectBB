package com.clicknect.util;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Status;

public class MsgBox {
	public static void show(final String msg, final int time){
		UiApplication.getUiApplication().invokeLater(new Runnable(){
			public void run() {
				Status.show(msg, time);
			}
		});
	}
	
	public static void alert(final String msg){
		UiApplication.getUiApplication().invokeLater(new Runnable(){
			public void run() {
				Dialog.alert(msg);
			}
		});
	}
	
	public static void inform(final String msg){
		UiApplication.getUiApplication().invokeLater(new Runnable(){
			public void run() {
				Dialog.inform(msg);
			}
		});
	}
}
