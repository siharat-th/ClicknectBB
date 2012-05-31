package com.jimmysoftware.device.api.command;

import net.rim.device.api.ui.UiApplication;

public class Command {
	private CommandHandler commandHandler;

	public Command(CommandHandler commandHandler) {
		this.commandHandler = commandHandler;
	}

	public boolean canExecute(Object obj) {
		// TODO Auto-generated method stub
		return obj!=UiApplication.getEventLock();
	}

	public void execute(Object obj) {
		// TODO Auto-generated method stub
		commandHandler.execute(null, null);
	}

}
