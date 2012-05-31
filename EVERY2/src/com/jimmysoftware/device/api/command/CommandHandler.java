package com.jimmysoftware.device.api.command;

public abstract class CommandHandler {

	public abstract void execute(ReadOnlyCommandMetadata metadata, Object context);

}
