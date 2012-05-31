package com.clicknect.tv;

import java.util.Enumeration;

import javax.microedition.pim.Event;
import javax.microedition.pim.EventList;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;
import javax.microedition.pim.PIMItem;
import javax.microedition.pim.RepeatRule;


import com.clicknect.tv.TVApi.ProgramData;
import com.jimmysoftware.device.api.command.Command;
import com.jimmysoftware.device.api.command.CommandHandler;
import com.jimmysoftware.device.api.command.ReadOnlyCommandMetadata;
import net.rim.device.api.io.http.HttpDateParser;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import com.jimmysoftware.ui.CommandButtonField;

public class PIMEvent {
	public static EventList events = null;
	
	private static PopupScreen popupRecurrence;
	private static final String reminder[] = {"6 Hours", "5 Hours", "4 Hours", "3 Hours", "2 Hours", "1 Hour",
			"45 Mins", "30 Mins", "15 Mins", "10 Mins", "5 Mins", "0 Mins", "None"}; 
	private static ObjectChoiceField reminderChoiceField;
	
	private static final String recurrence[] = {"None", "Daily", "Weekly", "Monthly", "Yearly"};
	private static ObjectChoiceField recurrenceChoiceField = new ObjectChoiceField("Recurrence:", recurrence, 0);
	
	private static CommandButtonField okButtonField = new CommandButtonField("OK", Field.FIELD_HCENTER);
	
	
	private static final int ONEMINUTE = 60;
	private static final int ONEHOUR = 60*ONEMINUTE;
	private static final int TimeInSeconds[] = {6*ONEHOUR, 5*ONEHOUR, 4*ONEHOUR, 3*ONEHOUR, 2*ONEHOUR, 1*ONEHOUR,
		45*ONEMINUTE, 30*ONEMINUTE, 15*ONEMINUTE, 10*ONEMINUTE, 5*ONEMINUTE, 0, -1};
	
	private static final int Recurrence[] = {-1, RepeatRule.DAILY, RepeatRule.WEEKLY, RepeatRule.MONTHLY, RepeatRule.YEARLY};
	public static void addEvent(ProgramData data){
		popupRecurrence = new PopupScreen(new VerticalFieldManager());
		reminderChoiceField = new ObjectChoiceField("Reminder:", reminder, 8);
		recurrenceChoiceField = new ObjectChoiceField("Recurrence:", recurrence, 0);
		okButtonField = new CommandButtonField("OK", Field.FIELD_HCENTER);
		okButtonField.setCommand(new Command(new CommitEventCommandHandler(data)));
		
		LabelField header = new LabelField("Appointment Properties");
		header.setFont(header.getFont().derive(Font.BOLD));
		popupRecurrence.add(header);
		popupRecurrence.add(new SeparatorField());
		popupRecurrence.add(new LabelField(""));
		popupRecurrence.add(reminderChoiceField);
		popupRecurrence.add(recurrenceChoiceField);
		popupRecurrence.add(new LabelField(""));
		popupRecurrence.add(new SeparatorField());
		popupRecurrence.add(okButtonField);
		UiApplication.getUiApplication().pushScreen(popupRecurrence);
	}
	
	public static void removeEvent(ProgramData data){
		try {
			events = (EventList) PIM.getInstance().openPIMList( PIM.EVENT_LIST, PIM.READ_WRITE );
		} catch (PIMException e1) {
			e1.printStackTrace();
			Dialog.alert("Cannot create event list");
			return;
		}
		
		try {
			Enumeration enum = events.items(data.title);
			while(enum.hasMoreElements()){
				Event event = (Event)enum.nextElement();
				long date = event.getDate(Event.START, 0);
				long parse = HttpDateParser.parse(data.beginDateTime);
				if(date==parse){
					events.removeEvent(event);
				}
			}
		} catch (PIMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(events!=null)
				try {
					events.close();
				} catch (PIMException e) {
					e.printStackTrace();
				}
		}
	}
	
	public static class CommitEventCommandHandler extends CommandHandler{
		private ProgramData data;
		public CommitEventCommandHandler(ProgramData data){
			this.data = data;
		}

		public void execute(ReadOnlyCommandMetadata metadata, Object context) {
			try {
				events = (EventList) PIM.getInstance().openPIMList( PIM.EVENT_LIST, PIM.READ_WRITE );
			} catch (PIMException e1) {
				e1.printStackTrace();
				popupRecurrence.close();
				Dialog.alert("Cannot create event list");
				return;
			}
			
			int reminder = reminderChoiceField.getSelectedIndex();
			int	recurrence = recurrenceChoiceField.getSelectedIndex();
			
			Event event = events.createEvent();
			if(events.isSupportedField(Event.SUMMARY)){
				event.addString(Event.SUMMARY, PIMItem.ATTR_NONE, data.title);
			}
			if( events.isSupportedField( Event.LOCATION )){
				event.addString( Event.LOCATION, PIMItem.ATTR_NONE, TVApi.getChannelName(data.channelId));
			}
			if(events.isSupportedField(Event.START)){
				long time = HttpDateParser.parse(data.beginDateTime);
				event.addDate(Event.START, PIMItem.ATTR_NONE, time);
			}
			if(events.isSupportedField(Event.END)){
				long time = HttpDateParser.parse(data.endDateTime);
				event.addDate(Event.END, PIMItem.ATTR_NONE, time);
			}
			if( events.isSupportedField( Event.ALARM )){
				int timeInSecond = TimeInSeconds[reminder];
				if(timeInSecond!=-1){
					event.addInt( Event.ALARM, PIMItem.ATTR_NONE, timeInSecond);
				}
			}
			if( events.isSupportedField( Event.UID )){
				event.addString( Event.UID, PIMItem.ATTR_NONE, data.id);
			}
			if( events.isSupportedField( Event.NOTE ) ){
			     event.addString( Event.NOTE, PIMItem.ATTR_NONE, "This event is create from ThaiTVSchedule Application." );
			}
			
			int rule = Recurrence[recurrence];
			if(rule!=-1){
				RepeatRule repeatRule = new RepeatRule();
				repeatRule.setInt(RepeatRule.FREQUENCY, rule );
				event.setRepeat(repeatRule);
			}
			
			try {
				event.commit();
			} catch (PIMException e) {
				e.printStackTrace();
				Dialog.alert("PIM Error: "+e.toString());
			}
			finally{
				try {
					events.close();
				} catch (PIMException e) {
					e.printStackTrace();
					Dialog.alert("PIM Error: "+e.toString());
				}
			}
			
			popupRecurrence.close();
		}
		
	}
}
