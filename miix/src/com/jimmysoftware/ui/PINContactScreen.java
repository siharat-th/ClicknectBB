package com.jimmysoftware.ui;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.pim.Contact;
import javax.microedition.pim.ContactList;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;

import net.rim.blackberry.api.invoke.Invoke;
import net.rim.blackberry.api.invoke.MessageArguments;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.Message.RecipientType;
import net.rim.blackberry.api.mail.PINAddress;
import net.rim.blackberry.api.pdap.BlackBerryContact;
import net.rim.device.api.system.Alert;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.ui.component.Status;

public class PINContactScreen extends ActionScreen implements ListFieldCallback {
	private static final Bitmap BMP_BBM_LOGO = Bitmap.getBitmapResource("bbm_logo.png");
	private static final int PADDING = 4;
	private static final int LIST_HEIGHT = BMP_BBM_LOGO.getHeight()+PADDING*2;
	
	private static final String NO_PIN = "";
	private static final String APP_NAME = ApplicationDescriptor.currentApplicationDescriptor().getName();
	private static final String MESSAGE_SUBJECT = "Check out "+APP_NAME+".";
	private static final String MESSAGE_BODY = "For download "+APP_NAME+"! You can check out by using \"App Delivery\" download available at http://bbcenter.clicknect.com";
	
	private ListField listField;
	private ContactList blackBerryContactList = null;
	private BlackBerryContact blackBerryContact = null;
	private Vector blackBerryContacts = null;
	private String pinContact[] = null;
	
	public PINContactScreen(){
		super(false);
		setTitle("Contacts");
		
		listField = new ListField(){
			public boolean navigationClick(int status, int time){
				int index = getSelectedIndex();
				String pin = pinContact[index];
				if(pin.trim().length()==8){
					//Status.show("Send pin message to PIN:"+pin, 1500);
					BlackBerryContact item = (BlackBerryContact)blackBerryContacts.elementAt(index);      
					String displayName = getDisplayName(item); 
					try {
						PINAddress pa = new PINAddress(pin.trim(), displayName);
						PINAddress[] addresses = {pa};
						Message m = new Message();
						m.addRecipients(RecipientType.TO, addresses);
						m.setSubject(MESSAGE_SUBJECT);
						m.setContent(MESSAGE_BODY);
						Invoke.invokeApplication(Invoke.APP_TYPE_MESSAGES, new MessageArguments(m));
					} catch (Exception e) {
						e.printStackTrace();
						Dialog.alert("Compose error "+e.toString());
					}
				}
				else{
					Alert.startVibrate(250);
					Status.show("Message could not be send. This contact not have a PIN number!", 1500);
				}
				return true;
			}
		};
		listField.setRowHeight(LIST_HEIGHT);
		listField.setCallback(this);
		add(listField);
		
		loadContactList();
	}
	
	public void loadContactList(){
		try {
			blackBerryContactList = (ContactList) PIM.getInstance()
					.openPIMList(PIM.CONTACT_LIST, PIM.READ_ONLY);
			Enumeration allContacts = blackBerryContactList.items();
			blackBerryContacts = enumToVector(allContacts);
			
			int size = blackBerryContacts.size();
			pinContact = new String[size];
			for(int i=0; i<size; i++){
				blackBerryContact = (BlackBerryContact)blackBerryContacts.elementAt(i);
				pinContact[i] = NO_PIN;
				int fieldsWithData[] = blackBerryContact.getFields();
				for(int j=0; j<fieldsWithData.length; j++){
					if(fieldsWithData[j]==BlackBerryContact.PIN){
						pinContact[i] = blackBerryContact.getString(BlackBerryContact.PIN, 0);
						break;
					}
				}
				
			}
			
			listField.setSize(blackBerryContacts.size());
		} catch (PIMException e) {
			e.printStackTrace();
		}
	}
	
	private Vector enumToVector(Enumeration contactEnum) {
		Vector v = new Vector();    
		if (contactEnum == null)      
			return v;    
		while (contactEnum.hasMoreElements()){      
			v.addElement(contactEnum.nextElement());    
		}    
		return v;
	}

	public void drawListRow(ListField listField, Graphics graphics, int index, int y, int width) {
		//if(listField==this.listField && index<blackBerryContacts.size()){
			BlackBerryContact item = (BlackBerryContact)blackBerryContacts.elementAt(index);      
			String displayName = getDisplayName(item);      
			int h = graphics.getFont().getHeight();
			graphics.drawText(displayName, PADDING, y+(LIST_HEIGHT-h)/2, DrawStyle.ELLIPSIS, width-PADDING*3-BMP_BBM_LOGO.getWidth());
			String pin = pinContact[index];
			if(pin.trim().length()==8){
				graphics.drawBitmap(width-PADDING-BMP_BBM_LOGO.getWidth(), y+PADDING, BMP_BBM_LOGO.getWidth(), BMP_BBM_LOGO.getHeight(), BMP_BBM_LOGO, 0, 0);
			}
			
			graphics.setColor(0x9b9b9b);
			graphics.fillRect(0, y+LIST_HEIGHT-1, width, 1);
		//}
	}

	public Object get(ListField listField, int index) {
		return blackBerryContacts.elementAt(index);
	}

	public int getPreferredWidth(ListField listField) {
		return Display.getWidth();
	}

	public int indexOfList(ListField listField, String prefix, int start) {
		return blackBerryContacts.indexOf(prefix, start);
	}
	
	private String getDisplayName(Contact contact) {
		if (contact == null) {
			return null;
		}
		String displayName = null;
		// First, see if there is a meaningful name set for the contact.
		if (contact.countValues(Contact.NAME) > 0) {
			final String[] name = contact.getStringArray(Contact.NAME, 0);
			final String firstName = name[Contact.NAME_GIVEN];
			final String lastName = name[Contact.NAME_FAMILY];
			if (firstName != null && lastName != null) {
				displayName = firstName + " " + lastName;
			} else if (firstName != null) {
				displayName = firstName;
			} else if (lastName != null) {
				displayName = lastName;
			}
			if (displayName != null) {
				final String namePrefix = name[Contact.NAME_PREFIX];
				if (namePrefix != null) {
					displayName = namePrefix + " " + displayName;
				}
				return displayName;
			}
		}
		// If not, use the company name.
		if (contact.countValues(Contact.ORG) > 0) {
			final String companyName = contact.getString(Contact.ORG, 0);
			if (companyName != null) {
				return companyName;
			}
		}
		return displayName;   
	}
}
