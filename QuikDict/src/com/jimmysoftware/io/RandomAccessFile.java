package com.jimmysoftware.io;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.io.ByteArrayInputConnection;

// pom
// implement read only random access file
public class RandomAccessFile implements DataOutput, DataInput, Closeable {
	private FileConnection fconn;
//	private ByteArrayInputConnection fconn;
	private DataInputStream dis;
//	private ByteArrayInputStream dis;
//	private byte[] data;
	private long filesize;

	public RandomAccessFile(String name) throws IOException, NotImplementsException{
		this(name, "r");
	}
	
	public RandomAccessFile(String name, String mode) throws IOException, NotImplementsException {
		filesize = 0;
		if(!mode.equalsIgnoreCase("r")) throw new NotImplementsException("only acceptmode r(read), other mode not implement yet.");
		open(name);
	}

	private void open(String name) throws IOException {
		dis = null;
		fconn = (FileConnection) Connector.open(name, Connector.READ);
//		fconn = (ByteArrayInputConnection)Connector.open(name);
		if (!fconn.exists()){
//		if(fconn==null){
			fconn=null;
			dis=null;
		}
		else {
			filesize = fconn.fileSize();
			dis = fconn.openDataInputStream();
//			dis = (ByteArrayInputStream)fconn.openInputStream();
//			data = new byte[(int) fconn.fileSize()];
			fconn.close();
		}
	}
	
	public boolean exist(){
		return dis!=null;
	}
	
	public long getFileSize(){
		return filesize;
	}

	public void write(int arg0) throws IOException {
		// TODO Auto-generated method stub
	}

	public void write(byte[] b) throws IOException {
		// TODO Auto-generated method stub

	}

	public void write(byte[] b, int off, int len) throws IOException {
		// TODO Auto-generated method stub

	}

	public void writeBoolean(boolean v) throws IOException {
		// TODO Auto-generated method stub

	}

	public void writeByte(int v) throws IOException {
		// TODO Auto-generated method stub

	}

	public void writeChar(int v) throws IOException {
		// TODO Auto-generated method stub

	}

	public void writeChars(String s) throws IOException {
		// TODO Auto-generated method stub

	}

	public void writeDouble(double v) throws IOException {
		// TODO Auto-generated method stub

	}

	public void writeFloat(float v) throws IOException {
		// TODO Auto-generated method stub

	}

	public void writeInt(int v) throws IOException {
		// TODO Auto-generated method stub

	}

	public void writeLong(long v) throws IOException {
		// TODO Auto-generated method stub

	}

	public final void writeShort(int v) throws IOException  {
		// TODO Auto-generated method stub
	}

	public void writeUTF(String str) throws IOException{
		// TODO Auto-generated method stub
		//throw new NotImplementsException();
	}

	public final byte readByte() throws IOException {
		int ch = this.read();
		if (ch < 0)
			throw new EOFException();
		return (byte) (ch);
	}

	public final int readUnsignedByte() throws IOException {
		int ch = this.read();
		if (ch < 0)
			throw new EOFException();
		return ch;
	}

	public final short readShort() throws IOException {
		int ch1 = this.read();
		int ch2 = this.read();
		if ((ch1 | ch2) < 0)
			throw new EOFException();
		return (short) ((ch1 << 8) + (ch2 << 0));
	}

	public final int readUnsignedShort() throws IOException {
		int ch1 = this.read();
		int ch2 = this.read();
		if ((ch1 | ch2) < 0)
			throw new EOFException();
		return (ch1 << 8) + (ch2 << 0);
	}


	public final char readChar() throws IOException {
		int ch1 = this.read();
		int ch2 = this.read();
		if ((ch1 | ch2) < 0)
			throw new EOFException();
		return (char) ((ch1 << 8) + (ch2 << 0));
	}

	public final int readInt() throws IOException {
		int ch1 = this.read();
		int ch2 = this.read();
		int ch3 = this.read();
		int ch4 = this.read();
		if ((ch1 | ch2 | ch3 | ch4) < 0)
			throw new EOFException();
		return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
	}

	
	public final long readLong() throws IOException {
		return ((long) (readInt()) << 32) + (readInt() & 0xFFFFFFFFL);
	}

	public final float readFloat() throws IOException {
		return Float.intBitsToFloat(readInt());
	}

	public final double readDouble() throws IOException {
		return Double.longBitsToDouble(readLong());
	}

	public final String readLine() throws IOException, NotImplementsException {
//		StringBuffer input = new StringBuffer();
//		int c = -1;
//		boolean eol = false;
//
//		while (!eol) {
//			switch (c = read()) {
//			case -1:
//			case '\n':
//				eol = true;
//				break;
//			case '\r':
//				eol = true;
//				long cur = getFilePointer();
//				if ((read()) != '\n') {
//					seek(cur);
//				}
//				break;
//			default:
//				input.append((char) c);
//				break;
//			}
//		}
//
//		if ((c == -1) && (input.length() == 0)) {
//			return null;
//		}
//		return input.toString();
		throw new NotImplementsException();
	}

	public final String readUTF() throws IOException {
		byte b[] = new byte[1024];
		int c = -1;
		boolean eol = false;
		int index = 0;
		while (!eol) {
			switch (c = read()) {
			case 0:
				b[index++] = (byte)c; // append (pom)
				eol=true;
				break;
			//case -1:
			default:
				b[index++] = (byte)c;
				break;
			}
		}
		
		return new String(b, 0, index-1, "UTF-8"); //index-1
	}

	public int skipBytes(int n) throws IOException {
		// TODO Auto-generated method stub
		int _skip = dis.skipBytes(n);
		fileIndexPointer+=_skip;
		return _skip;
		
//		return (int)dis.skip(n);
	}

	public void close() throws IOException {
		// TODO Auto-generated method stub
		if (dis != null)
			dis.close();
		if (fconn != null && fconn.isOpen())
			fconn.close();
	}

	private long fileIndexPointer = 0;
	public void seek(long pos) throws IOException {
		dis.reset();
		fileIndexPointer=dis.skip(pos);
	}

//	public void seekNext(long offset) throws IOException {
//		fileIndexPointer+=dis.skip(offset);
//	}

	public int read() throws IOException {
		return dis.read();
	}

	public int read(byte b[]) throws IOException {
		return readBytes(b, 0, b.length);
	}
	
	public int read(byte[] buff, int size) throws IOException {
		return readBytes(buff, 0, size);
	}

	private int readBytes(byte b[], int off, int len) throws IOException {
//		int size = dis.read(data, off, len);
//		System.arraycopy(data, off, b, 0, len);
		int size = dis.read(b, off, len);
		return size;
	}

	public int read(byte b[], int off, int len) throws IOException {
		return readBytes(b, off, len);
	}

	public boolean readBoolean() throws IOException {
		int ch = this.read();
		if (ch < 0)
		    throw new EOFException();
		return (ch != 0);
	}

	public void readFully(byte[] b) throws IOException {
		readFully(b, 0, b.length);
	}

	public void readFully(byte[] b, int off, int len) throws IOException {
		int n = 0;
		do {
		    int count = this.read(b, off + n, len - n);
		    if (count < 0)
			throw new EOFException();
		    n += count;
		} while (n < len);
	}
}
