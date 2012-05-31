package com.jimmysoftware.io;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

public class ByteBufferAccessFile implements DataInput, Closeable {
	private boolean exist;
	private byte buff[];
	private ByteArrayInputStream bis;
	private long fileSize; 
	
	public ByteBufferAccessFile(String name)throws IOException {
		exist = false;
		open(name);
	}
	
	private void open(String name)throws IOException{
		fileSize = 0;
		FileConnection fconn = (FileConnection) Connector.open(name);
		if (!fconn.exists()){
			exist = false;
			fconn=null;
		}
		else {
			fileSize = fconn.fileSize();
			buff = new byte[(int) fconn.fileSize()];
			InputStream is = fconn.openInputStream();
			is.read(buff);
			bis = new ByteArrayInputStream(buff);
			
			is.close();
			fconn.close();
			exist = true;
		}
	}
	
	public boolean exist(){
		return exist;
	}
	
	public long getFileSize(){
		return fileSize;
	}
	
	
	public void close() throws IOException {
		if(bis!=null){
			bis.close();
		}
		if(buff!=null)
			buff = null;
	}
	
	public void seek(long pos) throws IOException {
		bis.reset();
		bis.skip(pos);
	}
	
	public int read() throws IOException {
		return bis.read();
	}
	
	public int read(byte b[]) throws IOException {
		return readBytes(b, 0, b.length);
	}
	
	public int read(byte[] buff, int size) throws IOException {
		return readBytes(buff, 0, size);
	}

	private int readBytes(byte b[], int off, int len) throws IOException {
		int size = read(b, off, len);
		return size;
	}

	public int read(byte b[], int off, int len) throws IOException {
		return readBytes(b, off, len);
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
				b[index++] = (byte)c; // append this line (pom)
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
		return (int) bis.skip(n);
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
