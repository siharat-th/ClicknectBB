package com.clicknect.bbbuddy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.ApplicationManagerException;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.Background;
import net.rim.device.api.ui.decor.BackgroundFactory;
import net.rim.device.api.ui.decor.Border;
import net.rim.device.api.ui.decor.BorderFactory;
import net.rim.device.api.ui.picker.FilePicker;
import net.rim.device.api.util.StringUtilities;

import com.jimmysoftware.ui.ActionScreen;

public class DetailScreenAppCenter extends ActionScreen {
	public static final String ACTION_CLOSE = "close detail screen"; 
	VerticalFieldManager vfm;
	
	public DetailScreenAppCenter(final ListInfo listInfo){
		super(false, Manager.NO_VERTICAL_SCROLL);
		setFont(HomeScreenAppCenter.fontNormal);
		setTitle("Details");
		Bitmap bitmap = Bitmap.getBitmapResource("background.png");
		getMainManager().setBackground(BackgroundFactory.createBitmapBackground(bitmap, Background.POSITION_X_LEFT, Background.POSITION_Y_TOP, Background.REPEAT_SCALE_TO_FIT));
		
		vfm = new VerticalFieldManager(Manager.VERTICAL_SCROLL);
		//vfm.setBorder(BorderFactory.createRoundedBorder(new XYEdges(5, 5, 5, 5), 0x555555, Border.STYLE_SOLID));
		vfm.setMargin(0, 5, 5, 5);
		
		// detailVFM
		VerticalFieldManager detailVfm = new VerticalFieldManager(Manager.USE_ALL_WIDTH);
		detailVfm.setBorder(BorderFactory.createRoundedBorder(new XYEdges(5, 5, 5, 5), 0x555555, Border.STYLE_SOLID));
		HorizontalFieldManager detailHfm = new HorizontalFieldManager();
		detailHfm.setPadding(5, 5, 5, 5);
		detailHfm.add(new BitmapField(listInfo.icon));
		LabelField nameField = new LabelField(listInfo.name){
			public void paint(Graphics g){
				g.setColor(0xffffff);
				super.paint(g);
			}
		};
		
		nameField.setFont(HomeScreenAppCenter.fontNormalBold);
		VerticalFieldManager secondVfm = new VerticalFieldManager();
		secondVfm.setPadding(0, 0, 0, 5);
		detailHfm.add(secondVfm);
		
		secondVfm.add(nameField);
		LabelField vendor = new LabelField(listInfo.vendor){
			public void paint(Graphics g){
//				g.setColor(0x222222);
//				g.translate(1, 1);
//				super.paint(g);
//				g.setColor(0x999999);
//				g.translate(-1, -1);
//				super.paint(g);
				g.setColor(0x999999);
				super.paint(g);
			}
		};
		vendor.setFont(HomeScreenAppCenter.fontSmall);
		secondVfm.add(vendor);
		
		if(listInfo.isInstalled){
			LabelField version = new LabelField("Installed ("+listInfo.version+")"){
				public void paint(Graphics g){
//					g.setColor(0x222222);
//					g.translate(1, 1);
//					super.paint(g);
//					g.setColor(0x999999);
//					g.translate(-1, -1);
//					super.paint(g);
					g.setColor(0x999999);
					super.paint(g);
				}
			};
			version.setFont(HomeScreenAppCenter.fontSmall);
			secondVfm.add(version);
			
			secondVfm.add(new SeparatorField(){
				public void paint(Graphics g){
					g.setColor(0x666666);
					super.paint(g);
				}
			});
			
			if(listInfo.hasUpdate()){
				LabelField updateversion = new LabelField("Update("+listInfo.updateversion+")"){
					public void paint(Graphics g){
						g.setColor(0xffda00);
						super.paint(g);
					}
				};
				updateversion.setFont(HomeScreenAppCenter.fontNormal);
				secondVfm.add(updateversion);
			}
		}
		else{
			secondVfm.add(new SeparatorField(){
				public void paint(Graphics g){
					g.setColor(0x666666);
					super.paint(g);
				}
			});
			
			LabelField newversion = new LabelField("Version("+listInfo.updateversion+")"){
				public void paint(Graphics g){
					g.setColor(0xffda00);
					super.paint(g);
				}
			};
			newversion.setFont(HomeScreenAppCenter.fontNormal);
			secondVfm.add(newversion);
		}
		
		detailVfm.add(detailHfm);
		
		ButtonField button;
		boolean addButton = true;
		if(!listInfo.isInstalled){
			button= new ButtonField("  Install  ", Manager.USE_ALL_WIDTH|Manager.FIELD_HCENTER){
				public void drawFocus(Graphics g, boolean on){
					if(on){
						g.setColor(0x0000ff);
					}
					super.drawFocus(g,on);
				}
				
				public boolean navigationClick(int status, int time){		
					String url = listInfo.url;
//					if(url.endsWith("ThaiNews.jad")){
//						url = "file:///SDCard/clickconnect/every/ThaiNews.jad";		
//					}
//					else if(url.endsWith("HoroScope.jad")){
//						url = "file:///SDCard/clickconnect/horoscope/HoroScope.jad";					
//					}
//					else if(url.endsWith("QuikDict.jad")){
//						url = "file:///SDCard/clickconnect/quikdict/QuikDict.jad";
//					}
//					else if(url.endsWith("Thai_TV_Schedule.jad")){
//						url= "file:///SDCard/clickconnect/thaitv/Thai_TV_Schedule.jad";
//					}

//					if(url.startsWith("http://")){
//						int fromIndex = url.indexOf("ebuddy/");
//						int beginIndex = url.indexOf('/', fromIndex);
//						String fullpath = "file:///SDCard/clickconnect/" + url.substring(beginIndex+1);
//						if(BBbuddy.isFileExist(fullpath)){
//							url = fullpath;
//						}
//					}
//					else if(url.startsWith("file:///")){
//						if(!BBbuddy.isFileExist(url)){
//							int fromIndex = url.indexOf("clickconnect/");
//							int beginIndex = url.indexOf('/', fromIndex);
//							String newURL = BBbuddy.BASE_URL + url.substring(beginIndex+1);
//							url = newURL;
//						}
//					}
//					
//					if(url.startsWith("file:///")){
//						installCodeModule(url);
//					}
//					else{
//						BrowserSession browserSession = Browser.getDefaultSession();
//						browserSession.displayPage(url);
//						fireAction(ACTION_CLOSE);
//					}
					
					BrowserSession browserSession = Browser.getDefaultSession();
					browserSession.displayPage(url);
					UiApplication.getUiApplication().invokeLater(new Runnable(){

						public void run() {
							fireAction(ACTION_CLOSE);
						}
						
					});
					
					
					return true;
				}

				private void installCodeModule(String url){
					PopupScreen popup = new PopupScreen(new HorizontalFieldManager());
					popup.add(new BitmapField(Bitmap.getPredefinedBitmap(Bitmap.HOURGLASS), Field.FIELD_HCENTER));
					popup.add(new LabelField("Installing please wait...", Field.FIELD_HCENTER));
					UiApplication.getUiApplication().pushScreen(popup);
					int index = url.indexOf(".jad");
					url = url.substring(0, index);
					boolean exist = true;
					Vector datas = new Vector();
					int cid = 0;
					int totalSize = 0;
					do{
						String path = cid==0? url+".cod" : url+"-"+cid+".cod";
						FileConnection fconn = null;
						InputStream is = null;
						try {
							fconn = (FileConnection)Connector.open(path);
							if(fconn.exists()){
								is = fconn.openInputStream();
								byte[] data = IOUtilities.streamToBytes(is);
								datas.addElement(data);
								totalSize+=data.length;
							}
							else{
								exist = false;
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							Dialog.alert("Install error please check your SDCard");
						}
						finally{
							if(is!=null) try{is.close();}catch(IOException e){e.printStackTrace();}
							if(fconn!=null && fconn.isOpen()) try{fconn.close();}catch(IOException e){e.printStackTrace();}
							cid++;
						}
					}while(exist);
					
					boolean done = datas.size()>0;
					
					for(int i=0;i<datas.size(); i++){
						byte[] data = (byte[])datas.elementAt(i);
						int length = data.length;
						if(length>65535){
							int handle = CodeModuleManager.createNewModule(length, data, 65535);
							CodeModuleManager.writeNewModule(handle, 65535, data, 65535, length-65535);
							int ret = CodeModuleManager.saveNewModule(handle);
							boolean ok = (ret==CodeModuleManager.CMM_OK);
							done|=ok;
						}
						else{
							int handle = CodeModuleManager.createNewModule(length, data, length);
							int ret = CodeModuleManager.saveNewModule(handle);
							boolean ok = (ret==CodeModuleManager.CMM_OK);
							done|=ok;
						}
					}
					
					popup.close();
					if(done) Status.show("Install complete.", 1000);
					fireAction(ACTION_CLOSE);
				}
			};
		}
		else{
			if(listInfo.hasUpdate()){
				button= new ButtonField("  Update  ", Manager.USE_ALL_WIDTH|Manager.FIELD_HCENTER){
					public boolean navigationClick(int status, int time){
						//Dialog.inform(listInfo.url);
						BrowserSession browserSession = Browser.getDefaultSession();
						browserSession.displayPage(listInfo.url);
						return true;
					}
				};
			}
			else{
				button= new ButtonField("  Run  ", Manager.USE_ALL_WIDTH|Manager.FIELD_HCENTER){
					public boolean navigationClick(int status, int time){
						String moduleName = CodeModuleManager.getModuleName(listInfo.handle);
						try {
							ApplicationManager.getApplicationManager().launchApplication(moduleName);
						} catch (ApplicationManagerException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return true;
					}
				};
				
				if(listInfo.name.equalsIgnoreCase("miix")){
					addButton=false;
				}
			}
		}
		button.setFont(HomeScreenAppCenter.fontNormalBold);
		//button.setBackground(BackgroundFactory.createSolidBackground(0));
		//button.setBorder(BorderFactory.createRoundedBorder(new XYEdges(5,5,5,5), 0xffffff, Border.STYLE_SOLID));
		if(addButton) detailVfm.add(button);
		// descVFM
		VerticalFieldManager descVfm = new VerticalFieldManager(Manager.USE_ALL_WIDTH);
		descVfm.setBorder(BorderFactory.createRoundedBorder(new XYEdges(5, 5, 5, 5), 0x555555, Border.STYLE_SOLID));
		descVfm.setMargin(5, 0, 0, 0);
		descVfm.setPadding(5, 5, 5, 5);
		RichTextField descHeaderField = new RichTextField("Description"){
			public void paint(Graphics g){
				g.setColor(0xffffff);
				super.paint(g);
			}
		};
		descHeaderField.setFont(HomeScreenAppCenter.fontNormalBold);
		descVfm.add(descHeaderField);
		RichTextField descField = new RichTextField(listInfo.desc){
			public void paint(Graphics g){
				g.setColor(0xffffff);
				super.paint(g);
			}
		};
		descField.setFont(HomeScreenAppCenter.fontNormal);
		descVfm.add(descField);
		
		vfm.add(detailVfm);
		vfm.add(descVfm);
		add(vfm);
	}
	
	public boolean onClose(){
		fireAction(ACTION_CLOSE);
		return false;
	}
}
