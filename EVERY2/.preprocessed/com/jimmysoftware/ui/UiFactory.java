
package com.jimmysoftware.ui;





import net.rim.device.api.ui.decor.BackgroundFactory;
import net.rim.device.api.ui.Color;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;

public class UiFactory {
	public static String APPLICATION_NAME;
	
	public static Bitmap bgHeaderBitmap = EncodedImage.getEncodedImageResource("bg_header_landscape.png").getBitmap();
	public static Bitmap bgGradientBitmap = EncodedImage.getEncodedImageResource("lanscape_gradient.png").getBitmap();
	public static Bitmap moreButtonBitmap = EncodedImage.getEncodedImageResource("more_button.png").getBitmap();
	public static Bitmap moreHoverButtonBitmap = EncodedImage.getEncodedImageResource("more_button_hover.png").getBitmap();
	public static Bitmap leftButtonBitmap = EncodedImage.getEncodedImageResource("l_button.png").getBitmap();
	public static Bitmap leftHoverButtonBitmap = EncodedImage.getEncodedImageResource("l_button_hover.png").getBitmap();
	public static Bitmap rightButtonBitmap = EncodedImage.getEncodedImageResource("r_button.png").getBitmap();
	public static Bitmap rightHoverButtonBitmap = EncodedImage.getEncodedImageResource("r_button_hover.png").getBitmap();
	
	public static void setApplicationName(final String appName){
		APPLICATION_NAME = appName;
	}
	


















	
	public static int getDefaultBackgroundColor(){
		return 0xb5aead;
	}
}
