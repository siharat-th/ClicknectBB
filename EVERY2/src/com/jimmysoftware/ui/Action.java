package com.jimmysoftware.ui;

public class Action {
	private Object source;
	private String action;
	private Object data = null;
	
	public Action(Object source, String action, Object data) {
		this.source = source;
		this.action = action;
		this.data = data;
	}
	
	public Object getSource() {
		return source;
	}

	public String getAction() {
		return action;
	}
	
	public Object getData() {
		return data;
	}
}
