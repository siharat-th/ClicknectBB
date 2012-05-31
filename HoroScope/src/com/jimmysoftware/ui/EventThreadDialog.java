package com.jimmysoftware.ui;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Status;

public class EventThreadDialog {
	
	public static void statusDialog(final String message){
		statusDialog(message, 2000);
	}
	
	public static void statusDialog(final String message, final int timeout){ // timeout in ms.
		UiApplication.getUiApplication().invokeLater(new Runnable(){

			public void run() {
				Status.show(message, timeout);
			}
			
		});
	}
	
	public static void informDialog(final String message){
		UiApplication.getUiApplication().invokeLater(new Runnable(){

			public void run() {
				Dialog.inform(message);
			}
			
		});
	}
	
	public static void errorDialog(final String message){
		UiApplication.getUiApplication().invokeLater(new Runnable(){

			public void run() {
				Dialog.alert(message);
			}
			
		});
	}
}
