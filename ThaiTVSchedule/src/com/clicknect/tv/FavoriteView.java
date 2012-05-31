package com.clicknect.tv;

import java.util.Vector;

import com.clicknect.tv.TVApi.ProgramData;
import com.jimmysoftware.ui.ActionScreen;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.ui.container.VerticalFieldManager;

public class FavoriteView implements ListFieldCallback {
	private Manager manager;
	private ListField listField;
	private Vector listData;
	
	public FavoriteView(ActionScreen parent){
		manager = new VerticalFieldManager(Manager.VERTICAL_SCROLL|Manager.VERTICAL_SCROLLBAR);
		listData = new Vector();
		listField= new ListField(){
			public boolean navigationClick(int status, int time){
				int index = getSelectedIndex();
				ProgramData data = (ProgramData)FavoriteView.this.listData.elementAt(index);
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
		listField.setCallback(this);
		listField.setRowHeight(ScheduleScreen.LIST_HEIGHT);
		listField.setSize(0);
		listField.setFont(Font.getDefault().derive(Font.PLAIN, 20));
		manager.add(listField);
	}

	public Manager getManager() {
		return manager;
	}
	
	public void setData(Vector listData){
		this.listData = listData;
		listField.setSize(listData.size());
		listField.setFocus();
	}

	public void drawListRow(ListField listField, Graphics g, int index, int y, int width) {
		ProgramData data = (ProgramData)listData.elementAt(index);
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
			g.drawText(data.day+" "+ data.beginTime+" - "+data.endTime, tx+thumb.getWidth()+tx, ty);
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

}
