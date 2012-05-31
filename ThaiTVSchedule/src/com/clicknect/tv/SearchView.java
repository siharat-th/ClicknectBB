package com.clicknect.tv;

import java.util.Vector;

import com.clicknect.tv.TVApi.ProgramData;
import com.clicknect.util.MsgBox;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.BackgroundFactory;
import net.rim.device.api.ui.decor.Border;
import net.rim.device.api.ui.decor.BorderFactory;

public class SearchView implements ListFieldCallback {
	private Manager manager;
	private BasicEditField searchField;
	private ListField listField;
	private Vector listData;
	private HomeScreen homeScreen;
	
	public SearchView(HomeScreen homeScreen){
		this.homeScreen = homeScreen;
		manager = new VerticalFieldManager(Manager.NO_VERTICAL_SCROLL){
			public boolean keyChar(char c, int status, int time){
				if(!searchField.isFocus()){
					String oldText = searchField.getText();
					searchField.setFocus();
					String newText = c==Keypad.KEY_DELETE ? oldText : oldText+c;
					searchField.setText(newText.trim());
					return true;
				}
				else if(c==Keypad.KEY_ESCAPE){
					if(searchField.getText().length()>0){
						searchField.setText("");
						return true;
					}
				}
				return super.keyChar(c, status, time);
			}
		};
		searchField = new BasicEditField(Field.USE_ALL_WIDTH){
			public boolean navigationClick(int status, int time){
				if(getText().trim().length()>0){
					SearchView.this.homeScreen.fireActionSearch();
					return true;
				}
				return super.navigationClick(status, time);
			}
			
			public boolean keyChar(char c, int status, int time){
				if(c==Keypad.KEY_ENTER){
					if(getText().trim().length()>0){
						SearchView.this.homeScreen.fireActionSearch();
						return true;
					}
				}
				else if(getText().length()==0&&c==Keypad.KEY_SPACE){
					return true;
				}
				return super.keyChar(c, status, time);
			}
			
			public void paint(Graphics g){
				if(getText().length()==0){
					g.setColor(0xbbbbbb);
					g.drawText(" Search", 0, 0);
				}
				else{
					g.setColor(0);
					super.paint(g);
				}
			}
		};
		
		
		//int defaultFontHeight = Font.getDefault().getHeight();
		//searchField.setFont(Font.getDefault().derive(Font.PLAIN, defaultFontHeight+8));
		Border border = BorderFactory.createRoundedBorder(new XYEdges(4, 4, 4, 4));
		searchField.setBorder(border);
		searchField.setBackground(BackgroundFactory.createSolidBackground(0xffffff));
		manager.add(searchField);
		manager.add(new SeparatorField());
		
		//manager.setBackground(HomeScreen.mainBackground);
		listField = new ListField(0, Manager.USE_ALL_HEIGHT){
			public boolean navigationClick(int status, int time){
				if(SearchView.this.listData.size()>0){
					int index = getSelectedIndex();
					if(isMoreButton(index)){
						increaseListFieldSize();
						invalidate();
						return true;
					}
					ProgramData data = (ProgramData)SearchView.this.listData.elementAt(index);
					data.isFavorite = !data.isFavorite;
					if(data.isFavorite){
						FavoritePersistent.addFavorite(data.id);
						PIMEvent.addEvent(data);
					}
					else{
						FavoritePersistent.removeFavorite(data.id);
						PIMEvent.removeEvent(data);
					}
					invalidate();
					return true;
				}
				return super.navigationClick(status, time);
			}
			
			public void paint(Graphics g){
				int ty = getContentTop()+ getContentHeight()/2 - g.getFont().getHeight()/2;
				if(ThaiTVSchedule.isFetching()){
					ThaiTVSchedule.paintFetching(g, ty);
				}
				else if(this.getSize()<1){
					g.setColor(0);
					int x = (getWidth() - g.getFont().getAdvance("No data"))/2;
					g.drawText("No data", x, ty);
				}
				else{
					super.paint(g);
				}
			}
			
			public boolean isFocusable(){
				return !ThaiTVSchedule.isFetching();
			}
		};
		//listField.setBackground(HomeScreen.mainBackground);
		listField.setRowHeight(ScheduleScreen.LIST_HEIGHT);
		listField.setCallback(this);
		listData = new Vector();
		listField.setSize(0);
		listField.setFont(Font.getDefault().derive(Font.PLAIN, 20));
		VerticalFieldManager _vfm = new VerticalFieldManager(Manager.VERTICAL_SCROLL|Manager.VERTICAL_SCROLLBAR);
		_vfm.add(listField);
		manager.add(_vfm);
		
		//autoFocus();
	}
	
	public void autoFocus(){
		searchField.setFocus();
	}
	
	public Manager getManager() {
		return manager;
	}
	
	public boolean isMoreButton(int index){
		int maxSize = listData.size();
		int currentSize = listField.getSize();
//		if(index<currentSize-1 && index<maxSize-1)
//			return false;
//		return true;
		if(maxSize>currentSize && index==currentSize-1)
			return true;
		return false;
	}
	
	private void increaseListFieldSize(){
		int currentSize = listField.getSize();
		int maxSize = listData.size();
		int newSize = currentSize+10;
		if(newSize>maxSize) newSize = maxSize;
		listField.setSize(newSize);
		listField.setSelectedIndex(currentSize-1);
	}

	public void drawListRow(ListField listField, Graphics g, int index, int y, int width) {
		if(isMoreButton(index)){
			Font bold = Font.getDefault().derive(Font.BOLD);
			g.setFont(bold);
			String strMore = "More";
			int xx = (width - bold.getAdvance(strMore)) /2;
			int yy = y + (ScheduleScreen.LIST_HEIGHT - bold.getHeight())/2;
			if(listField.getSelectedIndex()==index){
				g.setColor(0);
				g.drawText(strMore, xx+1, yy+1);
				g.setColor(0xffffff);
				g.drawText(strMore, xx, yy);
			}
			else{
				g.setColor(0);
				g.drawText(strMore, xx, yy);
			}
			return;
		}
		ProgramData data = (ProgramData)listData.elementAt(index);
//		if(!listField.isFocus() || index!=listField.getSelectedIndex()){
//			for(int x=0; x<Display.getWidth(); x+= ScheduleScreen.LIST_BACKGROUND.getWidth())
//				g.drawBitmap(x, y, ScheduleScreen.LIST_BACKGROUND.getWidth(), ScheduleScreen.LIST_BACKGROUND.getHeight(), ScheduleScreen.LIST_BACKGROUND, 0, 0);
//		}
		if(!listField.isFocus() || index!=listField.getSelectedIndex()){
			for(int x=0; x<Display.getWidth(); x+=ScheduleScreen.LIST_BACKGROUND.getWidth())
				g.drawBitmap(x, y, ScheduleScreen.LIST_BACKGROUND.getWidth(), ScheduleScreen.LIST_BACKGROUND.getHeight(), ScheduleScreen.LIST_BACKGROUND, 0, 0);
		}
		Bitmap thumb = TVApi.getChannelThumbnail(data.channelId);
		if(thumb!=null){
			int padding = (ScheduleScreen.LIST_HEIGHT-thumb.getHeight())/2;
			int ty = y+padding;
			int tx = padding;
			g.drawBitmap(tx, ty, thumb.getWidth(), thumb.getHeight(), thumb, 0, 0);
			g.setColor(0);
			g.drawText(data.beginTime+" - "+data.endTime, tx+thumb.getWidth()+tx, ty);
			Bitmap favorite = data.isFavorite ? ScheduleScreen.LIST_FAVORITE_ON : ScheduleScreen.LIST_FAVORITE_OFF;
			int titleMaxWidth = width - thumb.getWidth() - favorite.getWidth() - (padding*4);
			g.drawText(data.title, tx+thumb.getWidth()+tx, ty+thumb.getHeight(), DrawStyle.BASELINE|DrawStyle.ELLIPSIS, titleMaxWidth);
			
			
			g.drawBitmap(width-padding-favorite.getWidth(), y+(ScheduleScreen.LIST_HEIGHT-favorite.getHeight())/2,
					favorite.getWidth(), favorite.getHeight(), favorite, 0, 0);
		}
	}
	public Object get(ListField listField, int index) {
		return listData.elementAt(index);
	}
	public int getPreferredWidth(ListField listField) {
		return Display.getWidth();
	}
	public int indexOfList(ListField listField, String prefix, int start) {
		return listData.indexOf(prefix, start);
	}
	
	public boolean onSavePrompt(){
		return true;
	}

	public String getKeyword() {
		return searchField.getText().trim();
	}

	public void setData(Vector programData) {
		listData = programData;
		int size = listData.size();
		if(size>10) size=11;
		listField.setSize(size);
		listField.setFocus();
	}

	public void resetSearchField() {
		searchField.setText("");
	}

	public boolean searchFieldEmpty() {
		return searchField.getText().length()==0;
	}
}