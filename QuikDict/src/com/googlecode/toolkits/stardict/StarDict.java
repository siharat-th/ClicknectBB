/*
Copyright 2009 http://code.google.com/p/toolkits/. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:
  * Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above
    copyright notice, this list of conditions and the following
    disclaimer in the documentation and/or other materials provided
    with the distribution.
  * Neither the name of http://code.google.com/p/toolkits/ nor the names of its
    contributors may be used to endorse or promote products derived
    from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.googlecode.toolkits.stardict;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.io.LineReader;
import net.rim.device.api.system.DeviceInfo;

import com.jimmysoftware.io.ByteBufferAccessFile;
import com.jimmysoftware.io.RandomAccessFile;

 
public class StarDict {	
	public static final byte LEXITRON_IFO = 0;
	public static final byte LEXITRON_YAIDX = 1;
	public static final byte LEXITRON_IDX = 2;
	public static final byte LEXITRON_DICT_DZ = 3;
	public static final byte LEXITRON_GZIP = 3;
	
	public static final int LEXITRON_FILESIZE_IFO = 279;
	public static final int LEXITRON_FILESIZE_YAIDX = 341544;
	public static final int LEXITRON_FILESIZE_IDX = 2038020;
	public static final int LEXITRON_FILESIZE_DICT_DZ = 22642688;
//	public static final int LEXITRON_FILESIZE[] = {LEXITRON_FILESIZE_IFO, LEXITRON_FILESIZE_YAIDX, LEXITRON_FILESIZE_IDX, LEXITRON_FILESIZE_DICT_DZ};
	
	private ByteBufferAccessFile ifo;
	private ByteBufferAccessFile index;
	private ByteBufferAccessFile yaindex;
	
//	private DictZipFile dz;
	private RandomAccessFile dz;
	
	private String dictname; 
	public String last_error = "";
	private int wordNum;
	private String version;
	
	public static final String DICT_FOLDER = getDefaultPath() + "dict/";
	public static final String DICT_LEXITRON = DICT_FOLDER + "lexitron"; 
	
	public static String getDefaultPath(){
    	char os = DeviceInfo.getSoftwareVersion().charAt(0);
    	if(os>='7')
    		return "file:///store/home/user/";
    	return System.getProperty("fileconn.dir.memorycard");
    }
	
	public void close() throws Exception{
		if(index!=null)index.close();
		if(yaindex!=null)yaindex.close();
		if(dz!=null) dz.close();
	}
	
	/**
	 * 
	 */
	public StarDict() {
		this(DICT_LEXITRON);
	}
	
	/**
	 * 
	 * @param dictname
	 */
	public StarDict(String dictname) {
		try {
			makeDirectory(DICT_FOLDER);
			this.dictname = dictname;
			ifo = new ByteBufferAccessFile(dictname+".ifo");
			index = new ByteBufferAccessFile(dictname+".idx");
			
//			this.dz = new DictZipFile(dictname+".dict.dz");
			dz = new RandomAccessFile(dictname+".dict.dz");
			
			yaindex = new ByteBufferAccessFile(dictname+".yaidx");
			
			wordNum = readWordNum();
			version = readVersion();
		}
		catch(Exception e) {
			last_error = e.toString();
			e.printStackTrace();
		}
	}
	
	public void makeDirectory(String path){
    	FileConnection fconn=null;
    	try {
			fconn = (FileConnection) Connector.open(path, Connector.READ_WRITE);
			if(fconn!=null && !fconn.exists()){
				fconn.mkdir();
			}
			fconn.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(fconn!=null&&fconn.isOpen())try{fconn.close();}catch(IOException e){e.printStackTrace();}
		}
    }
	
	public boolean exist(){
		if(ifo==null||index==null||dz==null||yaindex==null)
			return false;
//		return ifo.exist() &&index.exist() && dz.exist() && yaindex.exist();
		return isValidFilesize(0) && isValidFilesize(1) && isValidFilesize(2) && isValidFilesize(3);
	}
	
	public boolean dbExist(){
		if(ifo==null||index==null||yaindex==null)
			return false;
		return isValidFilesize(LEXITRON_IFO) && isValidFilesize(LEXITRON_YAIDX) && isValidFilesize(LEXITRON_IDX);
	}
	
	public boolean isValidFilesize(int file_id){
		if(file_id==LEXITRON_IFO){
			if(ifo.getFileSize()>=LEXITRON_FILESIZE_IFO)
				return true;
		}
		else if(file_id==LEXITRON_YAIDX){
			if(yaindex.getFileSize()>=LEXITRON_FILESIZE_YAIDX)
				return true;
		}
		else if(file_id==LEXITRON_IDX){
			if(index.getFileSize()>=LEXITRON_FILESIZE_IDX)
				return true;
		}
		else if(file_id==LEXITRON_DICT_DZ){
			if(dz.getFileSize()>=LEXITRON_FILESIZE_DICT_DZ)
				return true;
		}
		
		return false;
	}
	
	public static int getFileSize(String ext){
		if(ext.endsWith(".ifo")) return LEXITRON_FILESIZE_IFO;
		if(ext.endsWith(".yaidx")) return LEXITRON_FILESIZE_YAIDX;
		if(ext.endsWith(".idx")) return LEXITRON_FILESIZE_IDX;
		if(ext.endsWith("dict.dz")) return LEXITRON_FILESIZE_DICT_DZ;
		return -1; // unknow
	}
	
	public Vector getFileExtensionDownloadNecessary(){
		Vector ext = new Vector();
		if(ifo==null || !ifo.exist()|| !isValidFilesize(LEXITRON_IFO))
			ext.addElement(".ifo");
		if(yaindex==null || !yaindex.exist()|| !isValidFilesize(LEXITRON_YAIDX))
			ext.addElement(".yaidx");
		if(index==null || !index.exist()|| !isValidFilesize(LEXITRON_IDX))
			ext.addElement(".idx");
		if(dz==null || !dz.exist()|| !isValidFilesize(LEXITRON_DICT_DZ))
			ext.addElement(".dict.dz");
		
		return ext;
	}
	
	public String getWord(int p, StarDictLocation l) {
		if(l==null) {
			l = new StarDictLocation();
		}
		String word = null;
		int dataoffset = 0;
		int datasize = 0;
		int offset = 0; // the offset of the p-th word in this.index
		try {
			yaindex.seek(p*4);
			offset = yaindex.readInt();
			index.seek(offset);
			word = index.readUTF();
			dataoffset = index.readInt();
			datasize = index.readInt();
			
//			byte [] buffer = new byte[1024];
//			yaindex.seek(p*4);
//			int size = this.yaindex.read(buffer, 0, 4);
//			if (size!=4) {
//				throw new Exception("Read Index Error");
//			}
//			for(int i=0;i<4;i++) {
//				offset<<=8;
//				offset|=buffer[i]&0xff;
//			}
			
//			index.seek(offset);
//			int size = this.index.read(buffer, 0, 1024);
//			for(int i=0;i<size;i++) {
//				if (buffer[i]==0) {
//					word = new String(buffer, 0, i, "UTF-8");
//					dataoffset = 0;
//					datasize = 0;
//					for (int j=i+1;j<i+5;j++) {
//						dataoffset<<=8;
//						dataoffset|=buffer[j]&0xff;
//					}
//					for (int j=i+5;j<i+9;j++) {
//						datasize<<=8;
//						datasize|=buffer[j]&0xff;
//					}
//					break;
//				}
//			}
			l.offset = dataoffset;
			l.size = datasize;
		}
		catch(Exception e) {
			last_error = e.toString();
			e.printStackTrace();
		}
		return word;
	}
	
	public int getNearestWordIndex( String word ) {
		word = word.toLowerCase();
		int min = 0;
		int max = getWordNum();
		String w = "";
		int mid = 0;
		StarDictLocation l = new StarDictLocation();
		//return this.dz.test()+this.dz.last_error;
		///*
		while( min<=max ) {
			mid = (min + max)/2;
			w = getWord(mid, l).toLowerCase();
			if (w.compareTo(word)>0) {
				max = mid-1;
			}
			else if(w.compareTo(word)<0) {
				min = mid+1;
			} 
			else {
				break;
			}
		}
		return mid;
	}
	
	public String getNearestWord( String word, StarDictLocation l ) {
		int mid = getNearestWordIndex( word );
		String w = getWord(mid, l);
		
		return w;
	}
	
	/**
	 * 
	 * @param word
	 * @return the explanation of the word
	 */
	public String getExplanation(String word) {
		word = word.toLowerCase();
		int i = 0;
		int max = getWordNum();
		String w = "";
		int mid = 0;
		StarDictLocation l = new StarDictLocation();
		String exp = null;
		//return this.dz.test()+this.dz.last_error;
		///*
		while( i<=max ) {
			mid = (i + max)/2;
			w = getWord(mid, l).toLowerCase();
			if (w.compareTo(word)>0) {
				max = mid-1;
			}
			else if(w.compareTo(word)<0) {
				i = mid+1;
			} 
			else {
				break;
			}
		}
		//get explanation
		byte [] buffer = new byte[l.size];
		try {
			this.dz.seek(l.offset);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			this.dz.read(buffer, l.size);
		}
		catch(Exception e) {
			last_error = e.toString();
			buffer = null;
			exp = e.toString();
		}
		
		try {
			if (buffer == null) {
				exp = "Error when reading data\n"+exp;
			}
			else {
				exp = new String(buffer, "UTF8");
			}
		}
		catch(Exception e) {
			last_error = e.toString();
			e.printStackTrace();
		}
		return w+"\n"+exp;
		//return mid+"\n"+l.offset+exp+l.size;
		//*/
	}
	
	public String getVersion(){
		return version;
	}
	
	public int getWordNum(){
		return wordNum;
	}
	
	public String readVersion() {
//		FileConnection fconn = null;
//		InputStream stream = null;
//		try {
//			fconn = (FileConnection)Connector.open(DICT_LEXITRON+".ifo", Connector.READ);
//			stream = fconn.openInputStream();
//			LineReader lineReader = new LineReader(stream);
//			byte b[] = null;
//			b = lineReader.readLine();
//			while(b!=null){
//				String line = new String(b);
//				if(line.startsWith("version")){
//					int index = line.indexOf('=');
//					String version = line.substring(index+1);
//					stream.close();
//					fconn.close();
//					return version;
//				}
//				b = lineReader.readLine();
//			}
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		finally{
//			try{if(stream!=null) stream.close();}catch(IOException e){};
//			try{if(fconn!=null && fconn.isOpen()) fconn.close();}catch(IOException e){};
//		}
//		
//		return "UNKNOWN VERSION";
		
		return "2.4.2";
	}
	
	public int readWordNum() {
//		InputStream stream = getClass().getResourceAsStream("/lexitron.ifo");
//		LineReader lineReader = new LineReader(stream); 
//		
//		byte b[] = null;
//		try {
//			b = lineReader.readLine();
//			while(b!=null){
//				String line = new String(b);
//				if(line.startsWith("wordcount")){
//					int index = line.indexOf('=');
//					String wordcount = line.substring(index+1);
//					return Integer.parseInt(wordcount);
//				}
//				b = lineReader.readLine();
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		FileConnection fconn = null;
//		InputStream stream = null;
//		try {
//			fconn = (FileConnection)Connector.open(DICT_LEXITRON+".ifo", Connector.READ);
//			stream = fconn.openInputStream();
//			LineReader lineReader = new LineReader(stream);
//			byte b[] = null;
//			b = lineReader.readLine();
//			while(b!=null){
//				String line = new String(b);
//				if(line.startsWith("wordcount")){
//					int index = line.indexOf('=');
//					String wordcount = line.substring(index+1);
//					stream.close();
//					fconn.close();
//					return Integer.parseInt(wordcount);
//				}
//				b = lineReader.readLine();
//			}
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		finally{
//			try{if(stream!=null) stream.close();}catch(IOException e){};
//			try{if(fconn!=null && fconn.isOpen()) fconn.close();}catch(IOException e){};
//		}
//		
//		return 0;
		return 85385;
	}
}
