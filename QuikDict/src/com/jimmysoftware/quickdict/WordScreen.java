package com.jimmysoftware.quickdict;

import java.util.Vector;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.XYRect;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.BackgroundFactory;
import net.rim.device.api.ui.decor.Border;
import net.rim.device.api.ui.decor.BorderFactory;

import com.googlecode.toolkits.stardict.StarDict;
import com.googlecode.toolkits.stardict.StarDictLocation;
import com.jimmysoftware.ui.ActionScreen;
import java.util.Hashtable;

public class WordScreen extends ActionScreen implements FieldChangeListener, ListFieldCallback {
	public static final String ACTION_ENTER = "explanation";
	public static final String ACTION_RECOMMENED = "recommened application";
	
	private BasicEditField editField;
	private ObjectListField listField;
	private StarDict starDict;
	private Vector words;
	private int listHeight;
//	private Hashtable hashtable;
	
	public WordScreen(StarDict starDict){
		super(Manager.NO_VERTICAL_SCROLL);
		this.starDict = starDict;
		words = new Vector();
//		hashtable = new Hashtable();
		
		editField = new BasicEditField(Field.USE_ALL_WIDTH);
		editField.setFont(Font.getDefault().derive(Font.PLAIN, 24));
		Border border = BorderFactory.createRoundedBorder(new XYEdges(2,2,2,2));
		editField.setBorder(border);
		add(editField);
		add(new SeparatorField());
		
		VerticalFieldManager vfm = new VerticalFieldManager(Manager.VERTICAL_SCROLL|Manager.VERTICAL_SCROLLBAR);
		listHeight = Font.getDefault().getHeight()*2;
		listField = new ObjectListField(Field.FOCUSABLE){
			public boolean navigationClick(int status, int time){
				onEnter();
				return true;
			}
			
			public boolean trackwheelClick(int status, int time){
				onEnter();
				return true;
			}
			
			public boolean isFocusable(){
				return true;
			}
		};
		listField.select(true);
		listField.setRowHeight(listHeight);
		vfm.add(listField);
		add(vfm);
		
		editField.setChangeListener(this);
		listField.setChangeListener(this);  
		listField.setCallback(this);
		
		MenuItem recommenedMenuItem = new MenuItem("Recommened", 101, 2){
			public void run() {
				fireAction(ACTION_RECOMMENED);
			}
		};
		
		//this.addMenuItem(recommenedMenuItem);
	}
	
	protected boolean keyChar(char c, int status, int time){
		if(c==Keypad.KEY_ENTER){
			String word = editField.getText().trim();
			if(word.length()>0){
				return onEnter();
			}
			return true;
		}
		else if(!editField.isFocus()){
			String oldText = editField.getText();
			editField.setFocus();
			String newText = c==Keypad.KEY_DELETE ? oldText : oldText+c;
			editField.setText(newText.trim());
			return true;
		}
		else if(c==Keypad.KEY_ESCAPE){
			if(editField.getText().length()>0){
				editField.setText("");
				return true;
			}
			else if(editField.getText().length()<=0){
				int select = Dialog.ask(Dialog.D_YES_NO, "Exit QuikDict?");
				if(select==Dialog.YES){
					try {
						if(starDict!=null && starDict.exist())
							starDict.close();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					finally{
						try {
							starDict.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
						System.exit(0);
					}
				}
				return true;
			}
		}
		return super.keyChar(c, status, time);
	}
	
	private String previousWord = "";
	private boolean onEnter(){
		if(editField.isFocus()){
// enable-auto-suggestion
			String word = editField.getText().trim().toLowerCase();
			String nearestWord = starDict.getNearestWord(word, null);
			if(word.equalsIgnoreCase(nearestWord)){
				fireAction(ACTION_ENTER, nearestWord);
			}
			else{
				int index=-1;
				for(int i=0; i<words.size(); i++){
					String _word = (String)words.elementAt(i);
					if(_word.equalsIgnoreCase(word)){
						index=i;
						break;
					}
				}
				if(index!=-1){
					listField.setSelectedIndex(index);
					listField.setFocus();
				}
				else{
					if(words.size()>0){
						listField.setSelectedIndex(0);
						listField.setFocus();
					}
				}
			}
			return true;
			
// disable-auto-suggestion
//			String word = editField.getText().trim();
//			if(word.length()>0){
//				if(!word.equalsIgnoreCase(previousWord)){
//					PopupScreen popup = new PopupScreen(new HorizontalFieldManager());
//					popup.add(new BitmapField(Bitmap.getPredefinedBitmap(Bitmap.HOURGLASS)));
//					popup.add(new LabelField("Searching..."));
//					UiApplication.getUiApplication().pushScreen(popup);
//					UiApplication.getUiApplication().repaint();
//					
//					previousWord = word;
//					words.removeAllElements();
//					listField.setSize(0);
//					StarDictLocation loc = new StarDictLocation();
//					int nword_idx = starDict.getNearestWordIndex(word);
//					int num_word = starDict.getWordNum();
//					
//					String nearestWord = starDict.getWord(nword_idx, loc);
//					words.addElement(nearestWord);
//					nword_idx++;
//					while( nword_idx < num_word) {
//						nearestWord = starDict.getWord(nword_idx, loc).toLowerCase();
//						if( word.length() == 0 && nearestWord.startsWith("a") )
//							break;
//						if( word.length() == 1 && nearestWord.length() > 1 )
//							break;
//						if( nearestWord.startsWith(word) == false )
//							break;
//						words.addElement(nearestWord);
//						nword_idx++;
//					}
//					
//					String nearestWords[] = new String[words.size()];
//					words.copyInto(nearestWords);
//					listField.set(nearestWords);
//					
//					popup.close();
//				}
//				
//				int index=0;
//				for(int i=0; i<words.size(); i++){
//					String _word = (String)words.elementAt(i);
//					if(_word.equalsIgnoreCase(word)){
//						index=i;
//						break;
//					}
//				}
//				listField.setSelectedIndex(index);
//				listField.setFocus();
//				
//				return true;
//			}
		}
//		//#ifdef BlackBerrySDK6.0.0
//		else if(listField.isFocus() && words.size()>0){
//		//#endif
			
//		//#ifdef BlackBerrySDK5.0.0
//		else if(words.size()>0){
//		//#endif	
		else if(words.size()>0){
			int index = listField.getSelectedIndex();
			String word = (String)listField.get(listField, index);
			fireAction(ACTION_ENTER, word);
			return true;
		}
		return false;
	}

	protected boolean navigationClick(int status, int time) {
		return onEnter();
	}

	private GetWordThread thread = null;
	
	public void fieldChanged(Field field, int context) {
		// TODO Auto-generated method stub
		if(field==editField){
//			UiApplication.getUiApplication().invokeLater(new Runnable(){
//
//				public void run() {
//					StarDictLocation loc = new StarDictLocation();
//					
//					String word = editField.getText();
//					int nword_idx = starDict.getNearestWordIndex(word);
//					int num_word = starDict.getWordNum();
//					
//					words.removeAllElements();
//					String nearestWord = starDict.getWord(nword_idx, loc);
//					words.addElement(nearestWord);
//					nword_idx++;
//					int count = 0;
//					while( nword_idx < num_word && count<20) {
//						nearestWord = starDict.getWord(nword_idx, loc).toLowerCase();
//						if( word.length() == 0 && nearestWord.startsWith("a") )
//							break;
//						if( word.length() == 1 && nearestWord.length() > 1 )
//							break;
//						if( nearestWord.startsWith(word) == false )
//							break;
//						words.addElement(nearestWord);
//						nword_idx++;
//						count++;
//					}
//					
//					int size = words.size();
//					String nearestWords[] = new String[words.size()];
//					words.copyInto(nearestWords);
//					listField.set(nearestWords);
//				}
//				
//			});
			
			String word = editField.getText().trim();
			words.removeAllElements();
			if(word.length()<=0)listField.setSize(0);
			if(word.length()>0){
				if(thread!=null){
					if(thread.isRunning())
						thread.stopFetch();
					
					thread = new GetWordThread();
					thread.startFetch();
				}
				else{
					thread = new GetWordThread();
					thread.startFetch();
				}
				UiApplication.getUiApplication().invokeLater(thread);
			}
		}
	}
	
	private class GetWordThread extends Thread{
		private boolean running = false;
		
		public void startFetch(){
			running = true;
			//start();
			//run();
		}
		
		public void stopFetch(){
			running = false;
		}
		
		public boolean isRunning(){
			return running;
		}
		
		public void run(){
			String word = editField.getText();
//			boolean bFound = hashtable.containsKey(word);
//			if(bFound){
//				String nearestWords[] = (String[])hashtable.get(word);
//				listField.set(nearestWords);
//			}
			if(running){
//				words.removeAllElements();
//				listField.setSize(0);
				
				StarDictLocation loc = new StarDictLocation();
				
				if(!running) return;
				int nword_idx = starDict.getNearestWordIndex(word);
				int num_word = starDict.getWordNum();
				
				if(!running) return;
				String nearestWord = starDict.getWord(nword_idx, loc);
				if(nearestWord.startsWith(word))
					words.addElement(nearestWord);
				//listField.insert(0, nearestWord);
				nword_idx++;
				int count = 1;
//				while( nword_idx < num_word && count<20 && running) {
				while( nword_idx < num_word && running) {
					if(running==false) return;
					nearestWord = starDict.getWord(nword_idx, loc).toLowerCase();
					if( word.length() == 0 && nearestWord.startsWith("a") )
						break;
					if( word.length() == 1 && nearestWord.length() > 1 )
						break;
					if( nearestWord.startsWith(word) == false )
						break;
					
					if(nearestWord.startsWith(word))
						words.addElement(nearestWord);
					//listField.insert(count, nearestWord);
					//UiApplication.getUiApplication().repaint();
					nword_idx++;
					count++;
				}
				
				String nearestWords[] = new String[words.size()];
				words.copyInto(nearestWords);
				listField.set(nearestWords);
//				hashtable.put(word,nearestWords);
			}
			
			//int size = words.size();
			//listField.setSize(size);
			//String nearestWords[] = new String[words.size()];
			//words.copyInto(nearestWords);
			//listField.set(nearestWords);
			
			
			stopFetch();
		}
	}

	public void drawListRow(ListField listField, Graphics g, int index, int y, int width) {
		// TODO Auto-generated method stub
		if(words.size()>index){
			int h = listHeight>>1;
			String word = (String)words.elementAt(index);
			g.setColor(Color.BLACK);
			g.drawText(word, 2, y+h, DrawStyle.VCENTER);  
			g.setColor(Color.GRAY);
			g.drawLine(0, y+listHeight-1, width, y+listHeight-1);
		}
	}

	public Object get(ListField listField, int index) {
		// TODO Auto-generated method stub
		if(words.size()>index)
			return words.elementAt(index);
		return null;
	}

	public int getPreferredWidth(ListField listField) {
		// TODO Auto-generated method stub
		return Display.getWidth();
	}

	public int indexOfList(ListField listField, String prefix, int start) {
		// TODO Auto-generated method stub
		return words.indexOf(prefix, start);
	}
}
