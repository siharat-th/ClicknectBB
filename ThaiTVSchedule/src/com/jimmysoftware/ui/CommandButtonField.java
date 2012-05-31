package com.jimmysoftware.ui;

import com.jimmysoftware.device.api.command.Command;

import net.rim.device.api.ui.component.ButtonField;

public class CommandButtonField extends ButtonField {
	private Command command;
	
	public CommandButtonField(String caption, long style) {
		super(caption,style);
	}

	public void setCommand(Command command){
		this.command = command;
	}
	
	public boolean navigationClick(int status, int time){
		if(command.canExecute(this)){
			command.execute(this);
			return true;
		}
		return false;
	}
}
