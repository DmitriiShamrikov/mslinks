package io;

import java.io.IOException;
import java.io.OutputStream;

public class ByteWriter extends OutputStream {

	private OutputStream stream;
	private Endianness end = Endianness.LITTLE_ENDIAN;
	private int pos = 0;
	
	
	public ByteWriter(OutputStream out) {
		stream = out;
	}
	
	public ByteWriter setBigEndian() {
		end = Endianness.BIG_ENDIAN;
		return this;
	}
	
	public ByteWriter setLittleEndian() {
		end = Endianness.LITTLE_ENDIAN;
		return this;
	}
	
	public ByteWriter changeEndiannes() {
		if (isLitteEndian())
			setBigEndian();
		else 
			setLittleEndian();
		return this;
	}
	
	public boolean isBigEndian() {
		return end == Endianness.BIG_ENDIAN;
	}
	
	public boolean isLitteEndian() {
		return end == Endianness.LITTLE_ENDIAN;
	}
	
	public int getPosition() {
		return pos;
	}
	
	@Override
	public void write(int b) throws IOException {
		pos++;
		stream.write(b);
	}
	
	public void write(long b) throws IOException {
		write((int)b);
	}
	
	public void write2bytes(long n) throws IOException {
		long b0 = n & 0xff;
		long b1 = (n & 0xff00) >> 8;
		if (isLitteEndian()) {
			write(b0); write(b1);
		} else {
			write(b1); write(b0);
		}
	}
	
	public void write3bytes(long n) throws IOException {
		long b0 = n & 0xff;
		long b1 = (n & 0xff00) >> 8;
		long b2 = (n & 0xff0000) >> 16;
		if (isLitteEndian()) {
			write(b0); write(b1); write(b2);
		} else {
			write(b2); write(b1); write(b0);
		}
	}
	
	public void write4bytes(long n) throws IOException {
		long b0 = n & 0xff;
		long b1 = (n & 0xff00) >> 8;
		long b2 = (n & 0xff0000) >> 16;
		long b3 = (n & 0xff000000) >>> 24;
		if (isLitteEndian()) {
			write(b0); write(b1); write(b2); write(b3);
		} else {
			write(b3); write(b2); write(b1); write(b0);
		}
	}
	
	public void write5bytes(long n) throws IOException {
		long b0 = n & 0xff;
		long b1 = (n & 0xff00) >> 8;
		long b2 = (n & 0xff0000) >> 16;
		long b3 = (n & 0xff000000) >>> 24;
		long b4 = (n & 0xff00000000L) >> 32;
		if (isLitteEndian()) {
			write(b0); write(b1); write(b2); write(b3); write(b4);
		} else {
			write(b4); write(b3); write(b2); write(b1); write(b0);
		}
	}
	
	public void write6bytes(long n) throws IOException {
		long b0 = n & 0xff;
		long b1 = (n & 0xff00) >> 8;
		long b2 = (n & 0xff0000) >> 16;
		long b3 = (n & 0xff000000) >>> 24;
		long b4 = (n & 0xff00000000L) >> 32;
		long b5 = (n & 0xff0000000000L) >> 40;
		if (isLitteEndian()) {
			write(b0); write(b1); write(b2); write(b3); write(b4); write(b5);
		} else {
			write(b5); write(b4); write(b3); write(b2); write(b1); write(b0);
		}
	}
	
	public void write7bytes(long n) throws IOException {
		long b0 = n & 0xff;
		long b1 = (n & 0xff00) >> 8;
		long b2 = (n & 0xff0000) >> 16;
		long b3 = (n & 0xff000000) >>> 24;
		long b4 = (n & 0xff00000000L) >> 32;
		long b5 = (n & 0xff0000000000L) >> 40;
		long b6 = (n & 0xff000000000000L) >> 48;
		if (isLitteEndian()) {
			write(b0); write(b1); write(b2); write(b3); write(b4); write(b5); write(b6);
		} else {
			write(b6); write(b5); write(b4); write(b3); write(b2); write(b1); write(b0);
		}
	}
	
	public void write8bytes(long n) throws IOException {
		long b0 = n & 0xff;
		long b1 = (n & 0xff00) >> 8;
		long b2 = (n & 0xff0000) >> 16;
		long b3 = (n & 0xff000000) >>> 24;
		long b4 = (n & 0xff00000000L) >> 32;
		long b5 = (n & 0xff0000000000L) >> 40;
		long b6 = (n & 0xff000000000000L) >> 48;
		long b7 = (n & 0xff00000000000000L) >>> 56;
		if (isLitteEndian()) {
			write(b0); write(b1); write(b2); write(b3); write(b4); write(b5); write(b6); write(b7);
		} else {
			write(b7); write(b6); write(b5); write(b4); write(b3); write(b2); write(b1); write(b0);
		}
	}
	
	public void writeBytes(byte[] b) throws IOException {
		for (byte i : b) 
			write(i);
	}
	
	public void writeUnicodeString(String s) throws IOException {
		write2bytes(s.length());
		for (int i=0; i<s.length(); i++)
			write2bytes(s.charAt(i));
	}
}
