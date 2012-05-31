package com.jimmysoftware.quickdict;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.io.Base64OutputStream;
import net.rim.device.api.io.FileNotFoundException;
import net.rim.device.api.io.LineReader;
import net.rim.device.api.io.http.HttpProtocolConstants;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.FontFamily;
import net.rim.device.api.ui.FontManager;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.component.TextField;

import com.googlecode.toolkits.stardict.StarDict;
import com.jimmysoftware.ui.ActionScreen;

public class ExplanationScreen extends ActionScreen {
	public static final String ACTION_CLOSE = "word screen";
	
	private BrowserField htmlForm;
	private StarDict starDict;
	//private LabelField explanation, htmlCode;
	
	public ExplanationScreen(StarDict starDict){
		super(Manager.HORIZONTAL_SCROLL|Manager.VERTICAL_SCROLL);
		this.starDict = starDict;
		//explanation = new LabelField();
		
		htmlForm = new BrowserField();
//		Hashtable headers = new Hashtable();
//		headers.put(HttpProtocolConstants.HEADER_ACCEPT_ENCODING, System.getProperty("microedition.encoding"));
//		headers.put(HttpProtocolConstants.HEADER_ACCEPT_LANGUAGE, "th");
//		headers.put(HttpProtocolConstants.HEADER_CONTENT_TYPE, HttpProtocolConstants.CONTENT_TYPE_APPLICATION_X_WWW_FORM_URLENCODED);
//		htmlForm.addStandardRequestHeaders(headers, false);
		
		//htmlCode = new LabelField();
		
		//add(explanation);
		//addSeparator();
		add( htmlForm );
		//addSeparator();
		//add(htmlCode);
	}
	
	public void showExplaination(String word){
		String exp = starDict.getExplanation(word);
		// format html
		Vector lines = new Vector();
		try {
			InputStream is = new ByteArrayInputStream(exp.getBytes("utf-8"));
			LineReader lineReader = new LineReader(is);
			for(;;){
		        try{
		            String line = new String(lineReader.readLine(), "utf-8");
		            lines.addElement(line);
		        }
		        catch(EOFException eof){
		            break;
		        }
		        catch(IOException ioe){}                
		    }

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		StringBuffer buff = new StringBuffer();
		buff.append("<html><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
		buff.append("<meta name='viewport' content='width=320; user-scalable=no; initial-scale=1.0'/>");
		buff.append("<style type=\"text/css\">*.{line-height: 300%}</style>");
		buff.append("<body style='font-family:JSSans'>");
		buff.append("<p style='font-family:JSSans;font-size:18px;color:blue;'><b>");
		buff.append(word);
		buff.append("</b></p>");
		buff.append("<table border='0' width='100%'><tr><td style='background-color:silver;font-size=100%;'>&nbsp;");
		buff.append("LEXiTRON 2.0");
		buff.append("</td></tr></table>");
		buff.append("<br><table border='0' width='100%'>");
		
		int length = lines.size();
		for(int i=0; i<length; i++){
			String line = (String)lines.elementAt(i);
			
			if( line.length() == 0 )
				continue;
			
			if( line.startsWith(" ") == false ) {
				if( i != 1 )
					buff.append("<tr valign='top' style='height:8px;'></tr>");
				buff.append("<tr valign='top'>");
				
				String tdDetail = "<td style='font-size:100%'>";
				String tdWordType = "<td style='font-size:100%;text-align:right;color:blue;font-style:italic;padding-top:4px;'>";
		
				if( line.startsWith("n. ") ) {
					buff.append(tdWordType + "n.&nbsp;</td>" + tdDetail);
					line = line.substring(3);
				}
				else if( line.startsWith("v. ") ) {
					buff.append(tdWordType + "v.&nbsp;</td>" + tdDetail);
					line = line.substring(3);
				}
				else if( line.startsWith("vi. ") ) {
					buff.append(tdWordType + "vi.&nbsp;</td>" + tdDetail);
					line = line.substring(4);
				}
				else if( line.startsWith("vt. ") ) {
					buff.append(tdWordType + "vt.&nbsp;</td>" + tdDetail);
					line = line.substring(4);
				}
				else if( line.startsWith("vt.,") ) { //Dr.Wit
					buff.append(tdWordType + "vt.&nbsp;</td>" + tdDetail);
					line = line.substring(4);
				}
				else if( line.startsWith("sl. ") ) {
					buff.append(tdWordType + "sl.&nbsp;</td>" + tdDetail);
					line = line.substring(4);
				}
				else if( line.startsWith("adj. ") ) {
					buff.append(tdWordType + "adj.&nbsp;</td>" + tdDetail);
					line = line.substring(5);
				}
				else if( line.startsWith("aux. ") ) {
					buff.append(tdWordType + "aux.&nbsp;</td>" + tdDetail);
					line = line.substring(5);
				}
				else if( line.startsWith("adv. ") ) {
					buff.append(tdWordType + "adv.&nbsp;</td>" + tdDetail);
					line = line.substring(5);
				}
				else if( line.startsWith("idm. ") ) {
					buff.append(tdWordType + "idm.&nbsp;</td>" + tdDetail);
					line = line.substring(5);
				}
				else if( line.startsWith("suf. ") ) {
					buff.append(tdWordType + "suf.&nbsp;</td>" + tdDetail);
					line = line.substring(5);
				}
				else if( line.startsWith("prf. ") ) {
					buff.append(tdWordType + "prf.&nbsp;</td>" + tdDetail);
					line = line.substring(5);
				}
				else if( line.startsWith("det. ") ) {
					buff.append(tdWordType + "det.&nbsp;</td>" + tdDetail);
					line = line.substring(5);
				}
				else if( line.startsWith("conj. ") ) {
					buff.append(tdWordType + "conj.&nbsp;</td>" + tdDetail);
					line = line.substring(6);
				}
				else if( line.startsWith("phrv. ") ) {
					buff.append(tdWordType + "phrv.&nbsp;</td>" + tdDetail);
					line = line.substring(6);
				}
				else if( line.startsWith("pron. ") ) {
					buff.append(tdWordType + "pron.&nbsp;</td>" + tdDetail);
					line = line.substring(6);
				}
				else if( line.startsWith("abbr. ") ) {
					buff.append(tdWordType + "abbr.&nbsp;</td>" + tdDetail);
					line = line.substring(6);
				}
				else if( line.startsWith("prep. ") ) {
					buff.append(tdWordType + "prep.&nbsp;</td>" + tdDetail);
					line = line.substring(6);
				}
				else if( line.startsWith("vt, vi. ") ) {
					buff.append(tdWordType + "vt,&nbsp;vi.&nbsp;</td>" + tdDetail);
					line = line.substring(8);
				}
				else {
					buff.append("<td colspan='2' style='height:8px;font-size:20px'>&nbsp;");
				}
				
				int sa_location = 0;
				int sa_length = 0;
				line.regionMatches(true, 0, "syn:", sa_location, sa_length);
				
				if( sa_length == 0 ) {
					line.regionMatches(true, 0, "ant:", sa_location, sa_length);
					if( sa_length == 0 ) {
						buff.append(line);
						line = "";
					}
				}
				if( sa_length != 0 ) {
					buff.append(line.substring(0, sa_location));
					buff.append("</td></tr>");
					line = line.substring(sa_location);
				}
				
			} // end if
			while( line.length() > 0 )
			{
				buff.append("<tr><td></td><td style='font-size:100%; line-height:150%;'>");
				
				line.trim();
				int c_location = line.indexOf(":");
				
				if( c_location == -1 ) {
					buff.append("<font color='green'>");
					buff.append(line);
					buff.append("</font>");
					line = "";
				}
				else {
					buff.append("<font color='maroon'>");
					buff.append(line.substring(0, c_location+1));
					buff.append("</font>");
					buff.append("<font color='green'>");
					buff.append(line.substring(c_location+1));
					buff.append("</font>");
					line = "";
				}
				buff.append("</td></tr>");
			}// end while
		}// end for
		
		buff.append("</table><br>");
		buff.append("</body></html>");
	
		String html = buff.toString();

		//explanation.setText(exp);
		//.htmlCode.setText(html);
		//htmlForm.displayContent(html, "http://localhost");
		try {
			htmlForm.displayContent(html.getBytes("utf-8"), "text/html;charset=utf-8", "http://localhost");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public boolean keyChar(char c, int status, int time){
		fireAction(ACTION_CLOSE);
		return true;
	}
	
	public boolean navigationClick(int status, int time){
		fireAction(ACTION_CLOSE);
		return true;
}
}
