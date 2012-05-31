package com.clicknect.tv;

import java.util.Enumeration;
import java.util.Vector;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.container.FlowFieldManager;

import com.clicknect.tv.TVApi.ChannelData;
import com.clicknect.tv.TVApi.XMLNode;
import com.jimmysoftware.device.api.command.Command;
import com.jimmysoftware.device.api.command.CommandHandler;
import com.jimmysoftware.device.api.command.ReadOnlyCommandMetadata;
import com.jimmysoftware.ui.ActionScreen;
import com.jimmysoftware.ui.BitmapButtonField;

public class ChannelView{
	public static final String ACTION_ENTER = "open today schedule";
	public Manager manager;
	private Vector channelData;
	private ActionScreen parent;
	
	public ChannelView(ActionScreen parent, Vector nodes){
		this.parent = parent;
		manager = new FlowFieldManager(Manager.VERTICAL_SCROLLBAR|Manager.VERTICAL_SCROLL){
			protected void sublayout(int width, int height){
				
				int numberOfFields = getFieldCount();
				if(numberOfFields<=0) return;
				Field field = getField(0); //get the field
		    	BitmapButtonField button = (BitmapButtonField)field;
		    	int w = button.getBitmapWidth();
		    	int sw = Display.getWidth();
		    	int n = sw/w;
		    	int padding = (sw-(w*n))/(n*2);
		    	int vpadding = padding>10? 10 : padding;
		    	for (int i = 0;i < numberOfFields;i++) {
			    	Field f = getField(i);
			    	f.setPadding(vpadding, padding, vpadding, padding);
			    }
		    	
				super.sublayout(width, height);
			}
		};
		channelData = new Vector();
		createChannelView(manager, nodes);
	}
	
	private void createChannelView(Manager manager, Vector xmlNodes) {
		Enumeration enum = xmlNodes.elements();
		while (enum.hasMoreElements()) {
			XMLNode node = (XMLNode) enum.nextElement();
			if (node.getNode().equalsIgnoreCase("row")) {
				XMLNode id = (XMLNode) enum.nextElement();
				XMLNode name = (XMLNode) enum.nextElement();
				XMLNode icon = (XMLNode) enum.nextElement();

				ChannelData cdata = ThaiTVSchedule.tvapi.new ChannelData();
				cdata.id = id.getElement();
				cdata.name = name.getElement();
				cdata.logo = TVApi.getChannelLogo(cdata.id);
				cdata.logoHover = TVApi.getChannelLogoHover(cdata.id);
				cdata.thumbnail = TVApi.getChannelThumbnail(cdata.id);

				channelData.addElement(cdata);
			}
		}

		// sorted channel by id
		ChannelData channelDataArray[] = new ChannelData[channelData.size()];
		channelData.copyInto(channelDataArray);
		channelData.removeAllElements();
		for (int i = 0; i < TVApi.CHANNEL_ID_ORDER.length; i++) {
			final String id = TVApi.CHANNEL_ID_ORDER[i];
			for (int j = 0; j < channelDataArray.length; j++) {
				if (channelDataArray[j].id.equalsIgnoreCase(id)) {
					Bitmap normal = channelDataArray[j].logo;
					Bitmap hover = channelDataArray[j].logoHover;
					BitmapButtonField bmp = new BitmapButtonField(normal, hover);
					bmp.setCommand(new Command(new ChannelCommandHandler(id)));
					
					manager.add(bmp);
					channelData.addElement(channelDataArray[j]);
					break;
				}
			}
		}
	}
	
	class ChannelCommandHandler extends CommandHandler{
		private String channelID;
		public ChannelCommandHandler(String id) {
			channelID = id;
		}

		public void execute(ReadOnlyCommandMetadata metadata, Object context) {
			parent.fireAction(ACTION_ENTER, channelID);
		}
		
	}
}